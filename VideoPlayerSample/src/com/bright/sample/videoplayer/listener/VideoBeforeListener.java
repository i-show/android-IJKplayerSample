package com.bright.sample.videoplayer.listener;

import android.app.Activity;
import android.util.Log;

import com.baidu.mobads.InterstitialAd;
import com.baidu.mobads.InterstitialAdListener;
import com.bright.videoplayer.widget.MediaController;
import com.bright.videoplayer.widget.media.VideoView;

/**
 * Created by yuhaiyang on 2016/3/8.
 */
public class VideoBeforeListener implements InterstitialAdListener {
    private static final String TAG = "VideoBeforeListener";
    private InterstitialAd mBaiduAd;
    private VideoView mVideoView;
    private Activity mActivity;
    private MediaController mMediaController;

    public VideoBeforeListener(Activity activity, MediaController controller, InterstitialAd ad, VideoView videoView) {
        mBaiduAd = ad;
        mVideoView = videoView;

        mActivity = activity;
        mMediaController = controller;
        if(!mBaiduAd.isAdReady()) {
            mBaiduAd.loadAdForVideoApp(mVideoView.getWidth(), mVideoView.getHeight());
        }else{
            mBaiduAd.showAdInParentForVideoApp(mActivity, mMediaController.getAdView());
        }
    }

    @Override
    public void onAdReady() {
        Log.i(TAG, "onAdReady: ");
        mBaiduAd.showAdInParentForVideoApp(mActivity, mMediaController.getAdView());
    }

    @Override
    public void onAdPresent() {
        Log.i(TAG, "onAdPresent: ");
        mVideoView.pause();
    }

    @Override
    public void onAdClick(InterstitialAd interstitialAd) {
        Log.i(TAG, "onAdClick: ");
    }

    @Override
    public void onAdDismissed() {
        Log.i(TAG, "onAdDismissed: ");
        mVideoView.start();
    }

    @Override
    public void onAdFailed(String s) {
        Log.i(TAG, "onAdFailed: ");
        mVideoView.start();
        mBaiduAd.loadAdForVideoApp(mVideoView.getWidth(), mVideoView.getHeight());
    }
}
