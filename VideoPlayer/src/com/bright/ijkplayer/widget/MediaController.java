package com.bright.ijkplayer.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bright.ijkplayer.R;
import com.bright.ijkplayer.utils.ScreenOrientationUtils;
import com.bright.ijkplayer.utils.VideoUtils;
import com.bright.ijkplayer.widget.media.IMediaController;
import com.bright.ijkplayer.widget.media.IjkVideoView;

import tv.danmaku.ijk.media.player.IMediaPlayer;


public class MediaController extends FrameLayout implements IMediaController, View.OnClickListener {
    private static final String TAG = MediaController.class.getSimpleName();
    private static final int HANDLER_HIDE = 1001;
    private static final int HANDLER_SHOW_PROGRESS = 1002;

    private static final int DEFAULT_TIME_OUT = 3500;
    private boolean mDragging;

    private IjkVideoView mParentView;
    private SeekBar mProgress;
    private TextView mEndTime, mCurrentTime;
    private TextView mTitle;
    private ImageView mFullScreenImage;
    private ImageView mStateView;
    private ImageView mPlayNext;

    private ViewGroup mPortraitVideoRootView;
    private ViewGroup mLandVideoRootView;
    private MediaPlayerControl mPlayer;
    private View mLoadingView;
    private View mTopPanel;
    private CallBack mCallBack;
    private long mDownloadTime;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_HIDE:
                    setVisibility(GONE);
                    break;
                case HANDLER_SHOW_PROGRESS:
                    int pos = setProgress();
                    Log.i(TAG, "isPlaying = " + mPlayer.isPlaying());
                    Log.i(TAG, "isVisiable = " + isVisiable());
                    if (mPlayer.isPlaying() && isVisiable()) {
                        msg = obtainMessage(HANDLER_SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    };

    public MediaController(Context context) {
        this(context, null);
    }

    public MediaController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        layoutInflater.inflate(R.layout.media_controller, this, true);

        mTopPanel = findViewById(R.id.topPanel);
        ImageView back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(this);
        mTitle = (TextView) findViewById(R.id.title);

        mFullScreenImage = (ImageView) findViewById(R.id.full_screen);
        mFullScreenImage.setOnClickListener(this);

        mStateView = (ImageView) findViewById(R.id.state);
        mStateView.setOnClickListener(this);

        mPlayNext = (ImageView) findViewById(R.id.play_next);
        mPlayNext.setOnClickListener(this);

        mProgress = (SeekBar) findViewById(R.id.progress);
        mProgress.setOnSeekBarChangeListener(mSeekListener);
        mProgress.setMax(1000);
        mCurrentTime = (TextView) findViewById(R.id.current_time);
        mEndTime = (TextView) findViewById(R.id.end_time);

        mLoadingView = findViewById(R.id.loading_content);
    }


    @Override
    public boolean isShowing() {
        return isVisiable();
    }

    @Override
    public void setAnchorView(View view) {
        if (view == null) {
            return;
        }
        // 如果已经设置了 那么就直接退出就好
        if (mParentView != null) {
            return;
        }

        // 如果有父类 那么先移除
        ViewParent parent = getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(this);
        }

        mParentView = (IjkVideoView) view;
        mParentView.addView(this);
        mParentView.bringChildToFront(this);
        mParentView.setOnInfoListener(mInfoListener);
        mParentView.setOnCompletionListener(mCompletionListener);

        mPortraitVideoRootView = (ViewGroup) mParentView.getParent();

        setVisibility(GONE);
    }

    public void setLandVideoRootView(ViewGroup root) {
        mLandVideoRootView = root;
    }

    @Override
    public void setMediaPlayer(android.widget.MediaController.MediaPlayerControl player) {
        mPlayer = player;
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    @Override
    public void hide() {
        Log.i(TAG, "timeout hide");
        setVisibility(GONE);
        mHandler.removeMessages(HANDLER_HIDE);
        mHandler.removeMessages(HANDLER_SHOW_PROGRESS);
    }

    @Override
    public void show() {
        Log.i(TAG, "show");
        show(DEFAULT_TIME_OUT);
    }

    @Override
    public void show(int timeout) {
        Log.i(TAG, "timeout show = " + timeout);

        setVisibility(VISIBLE);
        setPlayStatus();
        setProgress();

        boolean isLand = ScreenOrientationUtils.isLandscape(getContext());
        Log.i(TAG, "isLand = " + isLand);
        mHandler.sendEmptyMessage(HANDLER_SHOW_PROGRESS);
        mHandler.sendEmptyMessageDelayed(HANDLER_HIDE, timeout);
    }


    @Override
    public void showOnce(View view) {

    }

    public void setPlayNextVisibility(int visiablilty) {
        mPlayNext.setVisibility(visiablilty);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "dispatchTouchEvent ACTION_DOWN");
                mDownloadTime = System.currentTimeMillis();
                mHandler.removeMessages(HANDLER_HIDE);
                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "dispatchTouchEvent ACTION_UP");
                long time = System.currentTimeMillis();
                if (time - mDownloadTime < 500) {
                    mHandler.sendEmptyMessage(HANDLER_HIDE);
                } else {
                    mHandler.sendEmptyMessageDelayed(HANDLER_HIDE, DEFAULT_TIME_OUT);
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    private boolean isVisiable() {
        return getVisibility() == VISIBLE;
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.full_screen) {
            if (ScreenOrientationUtils.isLandscape(getContext())) {
                changePortrait();
            } else {
                changeLand();
            }
        } else if (id == R.id.state) {

            if (mCallBack != null) {
                mCallBack.onPlay(!mPlayer.isPlaying());
            }

            if (mPlayer.isPlaying()) {
                mStateView.setImageResource(R.drawable.play_ctrl_play_bg);
                mPlayer.pause();
            } else {
                mStateView.setImageResource(R.drawable.play_ctrl_pause_bg);
                mPlayer.start();
                mHandler.sendEmptyMessageDelayed(HANDLER_SHOW_PROGRESS, 1000);
            }
        } else if (id == R.id.play_next) {
            if (mCallBack != null) {
                mCallBack.onPlayNext();
            }
        } else if (id == R.id.back) {
            if (ScreenOrientationUtils.isLandscape(getContext())) {
                changePortrait();
            } else {
                Activity activity = (Activity) getContext();
                activity.finish();
            }
        }
    }


    /**
     * 将当前给定的容器，提升到activity的顶层容器中
     */
    private void changeLand() {

        Activity activity = (Activity) getContext();
        mPortraitVideoRootView.removeView(mParentView);
        mFullScreenImage.setImageResource(R.drawable.play_ctrl_smallscreen_bg);
        //mTopPanel.setVisibility(VISIBLE);
        ViewGroup root;
        if (mLandVideoRootView != null) {
            root = mLandVideoRootView;
        } else {
            root = (ViewGroup) activity.findViewById(android.R.id.content);
        }
        if (root != null) {
            try {
                ScreenOrientationUtils.setLandscape(activity);
                ScreenOrientationUtils.setStatusBarVisible(activity, true);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                root.addView(mParentView, lp);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void changePortrait() {
        Activity activity = (Activity) getContext();
        ViewGroup root;
        //mTopPanel.setVisibility(GONE);
        if (mLandVideoRootView != null) {
            root = mLandVideoRootView;
        } else {
            root = (ViewGroup) activity.findViewById(android.R.id.content);
        }
        root.removeView(mParentView);
        mFullScreenImage.setImageResource(R.drawable.play_ctrl_fullscreen_bg);
        try {
            ScreenOrientationUtils.setPortrait(activity);
            ScreenOrientationUtils.setStatusBarVisible(activity, false);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            mPortraitVideoRootView.addView(mParentView, lp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setPlayStatus() {
        if (mPlayer.isPlaying()) {
            mStateView.setImageResource(R.drawable.play_ctrl_pause_bg);
        } else {
            mStateView.setImageResource(R.drawable.play_ctrl_play_bg);
        }
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null) {
            mEndTime.setText(VideoUtils.generatePlayTime(duration));
        }
        if (mCurrentTime != null) {
            mCurrentTime.setText(VideoUtils.generatePlayTime(position));
        }
        return position;
    }

    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the posititon of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(HANDLER_SHOW_PROGRESS);
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {

            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
            //mPlayer.seekTo((int) newposition);
            if (mCurrentTime != null) {
                mCurrentTime.setText(VideoUtils.generatePlayTime(newposition));
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;

            long duration = mPlayer.getDuration();
            long newposition = (duration * bar.getProgress()) / 1000L;
            mPlayer.seekTo((int) newposition);

            //setProgress();
            //updatePausePlay();
            show(DEFAULT_TIME_OUT);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            //mHandler.sendEmptyMessage(HANDLER_SHOW_PROGRESS);
        }
    };

    private IMediaPlayer.OnInfoListener mInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            Log.i(TAG, "on info changed ");
            if (isVisiable()) {
                setPlayStatus();
                mHandler.sendEmptyMessage(HANDLER_SHOW_PROGRESS);
            }

            switch (what) {
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    mLoadingView.setVisibility(VISIBLE);
                    mHandler.removeMessages(HANDLER_HIDE);
                    setVisibility(VISIBLE);
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    mLoadingView.setVisibility(GONE);
                    mHandler.removeMessages(HANDLER_HIDE);
                    mHandler.sendEmptyMessageDelayed(HANDLER_HIDE, DEFAULT_TIME_OUT);
                    break;
            }
            return false;
        }
    };


    private IMediaPlayer.OnCompletionListener mCompletionListener = new IMediaPlayer.OnCompletionListener() {
        public void onCompletion(IMediaPlayer mp) {
            if (mCallBack != null) {
                mCallBack.onComplete();
            }
        }
    };

    public void setCallBack(CallBack callBack) {
        mCallBack = callBack;
    }

    public interface CallBack {
        void onPlay(boolean isPlaying);

        void onComplete();

        void onPlayNext();
    }
}
