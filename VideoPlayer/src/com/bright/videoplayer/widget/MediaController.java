/**
 * Copyright (C) 2016 The yuhaiyang Android Source Project
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author: y.haiyang@qq.com
 */

package com.bright.videoplayer.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bright.videoplayer.R;
import com.bright.videoplayer.utils.ScreenOrientationUtils;
import com.bright.videoplayer.utils.VideoUtils;
import com.bright.videoplayer.widget.media.IMediaController;
import com.bright.videoplayer.widget.media.VideoView;

import tv.danmaku.ijk.media.player.IMediaPlayer;


public class MediaController extends FrameLayout implements IMediaController, View.OnClickListener {
    private static final String TAG = "MediaController";

    /**
     * 更新进度条
     */
    private static final int HANDLER_UPDATE_PROGRESS = 1003;
    /**
     * 设置Activity位sensor控制
     */
    private static final int HANDLER_SCREEN_SENSOR = 1004;
    /**
     * 多长时间后重新设置为sensor控制
     */
    private static final int DEFAULT_DELAY_TIME_SET_SENSOR = 5000;
    private boolean mDragging;

    private VideoView mVideoView;
    /**
     * 普通功能的包裹区域
     */
    private View mNormalFeaturesContent;
    // Top panel 中包裹的
    private TextView mTitle;
    // 进度条
    private SeekBar mProgress;
    // 当前时间和总共多长时间
    private TextView mCurrentTime, mEndTime;

    private ImageView mFullScreenView;
    private ImageView mStartOrPauseView;
    private ImageView mPlayNextView;
    /**
     * 加载功能的包裹区域
     */
    private View mLoadingContent;
    /**
     * 滑动功能区
     */
    private View mSlideContent;
    private ImageView mSlideIcon;
    private TextView mSlideTargetTime;
    private TextView mSlideTotleTime;
    /**
     * 广告区
     */
    private RelativeLayout mADContent;

    private ViewGroup mPortraitVideoRootView;
    private ViewGroup mLandVideoRootView;
    private MediaPlayerControl mPlayer;

    private CallBack mCallBack;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_UPDATE_PROGRESS:
                    int pos = setProgress();
                    Log.i(TAG, "isPlaying = " + mPlayer.isPlaying());
                    boolean isVisiable = mNormalFeaturesContent.getVisibility() == View.VISIBLE;
                    Log.i(TAG, "isVisiable = " + isVisiable);
                    if (mPlayer.isPlaying() && isVisiable) {
                        msg = obtainMessage(HANDLER_UPDATE_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }

                    break;
                case HANDLER_SCREEN_SENSOR:
                    Log.i(TAG, "handleMessage: HANDLER_SCREEN_SENSOR");
                    ScreenOrientationUtils.setSensor(getContext());
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

        mNormalFeaturesContent = findViewById(R.id.normal_content);
        ImageView back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(this);
        mTitle = (TextView) findViewById(R.id.title);

        mFullScreenView = (ImageView) findViewById(R.id.full_screen);
        mFullScreenView.setOnClickListener(this);

        mStartOrPauseView = (ImageView) findViewById(R.id.start_or_pause);
        mStartOrPauseView.setOnClickListener(this);

        mPlayNextView = (ImageView) findViewById(R.id.play_next);
        mPlayNextView.setOnClickListener(this);

        mProgress = (SeekBar) findViewById(R.id.progress);
        mProgress.setOnSeekBarChangeListener(mSeekListener);
        mProgress.setMax(1000);

        mCurrentTime = (TextView) findViewById(R.id.current_time);
        mEndTime = (TextView) findViewById(R.id.end_time);

        // Loading 区域
        mLoadingContent = findViewById(R.id.loading_content);

        mSlideContent = findViewById(R.id.slide_content);
        mSlideIcon = (ImageView) findViewById(R.id.slide_icon);
        mSlideTargetTime = (TextView) findViewById(R.id.slide_time_target);
        mSlideTotleTime = (TextView) findViewById(R.id.slide_time_totle);

        mADContent = (RelativeLayout) findViewById(R.id.ad_content);
    }

    @Override
    public void hide() {
        Log.i(TAG, "timeout hide");
        mNormalFeaturesContent.setVisibility(GONE);
        mHandler.removeMessages(HANDLER_UPDATE_PROGRESS);
    }

    @Override
    public void show() {
        Log.i(TAG, "show");
        /**
         * 1. 先进性设置播放状态
         * 2. 更新进度条
         * 3. 显示
         */
        syncPlayStatus();
        mHandler.sendEmptyMessage(HANDLER_UPDATE_PROGRESS);
        mNormalFeaturesContent.setVisibility(VISIBLE);
    }

    @Override
    public boolean isShowing() {
        return mNormalFeaturesContent.getVisibility() == View.VISIBLE;
    }

    @Override
    public void showLoading() {
        if (mLoadingContent.getVisibility() == VISIBLE) {
            return;
        }
        mLoadingContent.setVisibility(VISIBLE);
    }

    @Override
    public void hideLoading() {
        mLoadingContent.setVisibility(GONE);
    }

    @Override
    public void showSlideView(long position, float distance) {
        mSlideContent.setVisibility(VISIBLE);
        int duration = mPlayer.getDuration();

        if (distance > 0) {
            mSlideIcon.setImageResource(R.drawable.ic_forward);
        } else {
            mSlideIcon.setImageResource(R.drawable.ic_backward);
        }


        mSlideTargetTime.setText(VideoUtils.generatePlayTime(position));
        mSlideTotleTime.setText(VideoUtils.generatePlayTime(duration));
    }

    @Override
    public void hideSlideView() {
        mSlideContent.setVisibility(GONE);
    }

    @Override
    public void effectiveSlide(long position) {
        mPlayer.seekTo((int) (position));
    }

    @Override
    public RelativeLayout getAdView() {
        return mADContent;
    }

    @Override
    public void setAnchorView(View view) {
        if (view == null) {
            return;
        }
        // 如果已经设置了 那么就直接退出就好
        if (mVideoView != null) {
            return;
        }

        // 如果有父类 那么先移除
        ViewParent parent = getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(this);
        }

        mVideoView = (VideoView) view;
        mVideoView.addView(this);
        mVideoView.bringChildToFront(this);
        mVideoView.setOnCompletionListener(mCompletionListener);

        mPortraitVideoRootView = (ViewGroup) mVideoView.getParent();

        mHandler.sendEmptyMessage(HANDLER_SCREEN_SENSOR);
    }

    /**
     * 设置横屏时候的RootView
     */
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


    public void setPlayNextVisibility(int visiablilty) {
        mPlayNextView.setVisibility(visiablilty);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.full_screen) {
            if (ScreenOrientationUtils.isLandscape(getContext())) {
                changePortrait(false);
            } else {
                changeLand(false);
            }
        } else if (id == R.id.start_or_pause) {

            if (mCallBack != null) {
                mCallBack.onPlay(!mPlayer.isPlaying());
            }

            if (mPlayer.isPlaying()) {
                mStartOrPauseView.setImageResource(R.drawable.ic_play_play);
                mPlayer.pause();
            } else {
                mStartOrPauseView.setImageResource(R.drawable.ic_play_pause);
                mPlayer.start();
                mHandler.sendEmptyMessageDelayed(HANDLER_UPDATE_PROGRESS, 1000);
            }
        } else if (id == R.id.play_next) {
            if (mCallBack != null) {
                mCallBack.onPlayNext();
            }
        } else if (id == R.id.back) {
            if (ScreenOrientationUtils.isLandscape(getContext())) {
                changePortrait(false);
            } else {
                Activity activity = (Activity) getContext();
                activity.finish();
            }
        }
    }


    /**
     * 切换成横屏模式
     *
     * @param bySensor 是否是通过sensor来切换的
     */
    public void changeLand(boolean bySensor) {

        Activity activity = (Activity) getContext();
        mPortraitVideoRootView.removeView(mVideoView);
        mFullScreenView.setImageResource(R.drawable.ic_to_smallscreen);
        ViewGroup root;
        if (mLandVideoRootView != null) {
            root = mLandVideoRootView;
        } else {
            root = (ViewGroup) activity.findViewById(android.R.id.content);
        }
        if (root != null) {
            try {
                // 如果是通过sensor来切换的那么非强制更换
                ScreenOrientationUtils.setLandscape(activity, !bySensor);
                ScreenOrientationUtils.setStatusBarVisible(activity, true);
                mHandler.removeMessages(HANDLER_SCREEN_SENSOR);
                mHandler.sendEmptyMessageDelayed(HANDLER_SCREEN_SENSOR, DEFAULT_DELAY_TIME_SET_SENSOR);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                root.addView(mVideoView, lp);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 切换成竖屏模式
     *
     * @param bySensor 是否是通过sensor来切换的
     */
    public void changePortrait(boolean bySensor) {
        Activity activity = (Activity) getContext();
        ViewGroup root;
        if (mLandVideoRootView != null) {
            root = mLandVideoRootView;
        } else {
            root = (ViewGroup) activity.findViewById(android.R.id.content);
        }
        root.removeView(mVideoView);
        mFullScreenView.setImageResource(R.drawable.ic_to_fullscreen);
        try {
            ScreenOrientationUtils.setPortrait(activity, !bySensor);
            ScreenOrientationUtils.setStatusBarVisible(activity, false);
            mHandler.removeMessages(HANDLER_SCREEN_SENSOR);
            mHandler.sendEmptyMessageDelayed(HANDLER_SCREEN_SENSOR, DEFAULT_DELAY_TIME_SET_SENSOR);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            mPortraitVideoRootView.addView(mVideoView, lp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 同步播放状态
     */
    private void syncPlayStatus() {
        if (mPlayer.isPlaying()) {
            mStartOrPauseView.setImageResource(R.drawable.ic_play_pause);
        } else {
            mStartOrPauseView.setImageResource(R.drawable.ic_play_play);
        }
    }

    /**
     * 设置进度
     */
    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mNormalFeaturesContent == null) {
            return position;
        }

        if (duration > 0) {
            // use long to avoid overflow
            long pos = 1000L * position / duration;
            mProgress.setProgress((int) pos);
        }
        int percent = mPlayer.getBufferPercentage();
        mProgress.setSecondaryProgress(percent * 10);

        mEndTime.setText(VideoUtils.generatePlayTime(duration));
        mCurrentTime.setText(VideoUtils.generatePlayTime(position));
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
            mHandler.removeMessages(HANDLER_UPDATE_PROGRESS);
            mVideoView.removeHideAction();
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

            mVideoView.setShowAction();
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
