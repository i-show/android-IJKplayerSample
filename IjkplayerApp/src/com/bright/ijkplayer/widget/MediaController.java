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

    private static final int DEFAULT_TIME_OUT = 3000;
    private boolean mDragging;

    private IjkVideoView mParentView;
    private SeekBar mProgress;
    private TextView mEndTime, mCurrentTime;
    private ImageView mFullScreenImage;
    private ImageView mStateView;

    private ViewGroup mPortraitVideoRootView;
    private MediaPlayerControl mPlayer;
    private View mLoadingView;

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
        mFullScreenImage = (ImageView) findViewById(R.id.full_screen);
        mFullScreenImage.setOnClickListener(this);

        mStateView = (ImageView) findViewById(R.id.state);
        mStateView.setOnClickListener(this);

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

        mParentView = (IjkVideoView) view;
        mParentView.addView(this);
        mParentView.bringChildToFront(this);
        mParentView.setOnInfoListener(mInfoListener);
        mPortraitVideoRootView = (ViewGroup) mParentView.getParent();
    }

    @Override
    public void setMediaPlayer(android.widget.MediaController.MediaPlayerControl player) {
        mPlayer = player;
    }

    @Override
    public void hide() {
        Log.i("nian", "timeout hide");
        setVisibility(GONE);
        mHandler.removeMessages(HANDLER_HIDE);
        mHandler.removeMessages(HANDLER_SHOW_PROGRESS);
    }

    @Override
    public void show(int timeout) {
        Log.i("nian", "timeout show = " + timeout);
        mHandler.sendEmptyMessageDelayed(HANDLER_HIDE, timeout);
    }

    @Override
    public void show() {
        setVisibility(VISIBLE);
        setPlayStatus();
        setProgress();

        mHandler.sendEmptyMessage(HANDLER_SHOW_PROGRESS);
        mHandler.sendEmptyMessageDelayed(HANDLER_HIDE, DEFAULT_TIME_OUT);
    }

    @Override
    public void showOnce(View view) {

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHandler.removeMessages(HANDLER_HIDE);
                break;
            case MotionEvent.ACTION_UP:
                mHandler.sendEmptyMessageDelayed(HANDLER_HIDE, DEFAULT_TIME_OUT);
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
        switch (v.getId()) {
            case R.id.full_screen:
                if (ScreenOrientationUtils.isLandscape(getContext())) {
                    changePortrait();
                } else {
                    changeLand();
                }
                break;
            case R.id.state:
                if (mPlayer.isPlaying()) {
                    mStateView.setImageResource(R.drawable.play_ctrl_play_bg);
                    mPlayer.pause();
                } else {
                    mStateView.setImageResource(R.drawable.play_ctrl_pause_bg);
                    mPlayer.start();
                    mHandler.sendEmptyMessageDelayed(HANDLER_SHOW_PROGRESS, 1000);
                }
                break;
        }
    }


    /**
     * 将当前给定的容器，提升到activity的顶层容器中
     */
    private void changeLand() {
        Activity activity = (Activity) getContext();
        mPortraitVideoRootView.removeAllViews();
        mFullScreenImage.setImageResource(R.drawable.play_ctrl_smallscreen_bg);
        ViewGroup root = (ViewGroup) activity.findViewById(android.R.id.content);
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

    private void changePortrait() {
        Activity activity = (Activity) getContext();
        ViewGroup root = (ViewGroup) activity.findViewById(android.R.id.content);
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
        Log.i(TAG, "setProgress");
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
            mPlayer.seekTo((int) newposition);
            if (mCurrentTime != null)
                mCurrentTime.setText(VideoUtils.generatePlayTime(newposition));
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            //updatePausePlay();
            show(DEFAULT_TIME_OUT);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(HANDLER_SHOW_PROGRESS);
        }
    };

    private IMediaPlayer.OnInfoListener mInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            Log.i(TAG, "on info changed ");
            if (isVisiable()) {
                setPlayStatus();
                setProgress();
            }

            switch (what) {
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    mLoadingView.setVisibility(VISIBLE);
                    mHandler.removeMessages(HANDLER_HIDE);
                    setVisibility(VISIBLE);
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    mLoadingView.setVisibility(GONE);
                    setVisibility(GONE);
                    break;
            }
            return false;
        }
    };
}
