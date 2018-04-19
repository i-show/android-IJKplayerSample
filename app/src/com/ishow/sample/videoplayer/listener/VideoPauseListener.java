package com.ishow.sample.videoplayer.listener;

import android.util.Log;

import com.baidu.mobads.InterstitialAd;
import com.baidu.mobads.InterstitialAdListener;
import com.ishow.videoplayer.widget.media.VideoView;

/**
 * Created by yuhaiyang on 2016/3/8.
 */
public class VideoPauseListener implements InterstitialAdListener {
    private static final String TAG = "VideoPauseListener";
    private InterstitialAd mBaiduAd;
    private VideoView mVideoView;

    public VideoPauseListener(InterstitialAd ad, VideoView videoView) {
        mBaiduAd = ad;
        mVideoView = videoView;
    }

    @Override
    public void onAdReady() {
        Log.i(TAG, "onAdReady: ");
    }

    @Override
    public void onAdPresent() {
        Log.i(TAG, "onAdPresent: ");
    }

    @Override
    public void onAdClick(InterstitialAd interstitialAd) {
        Log.i(TAG, "onAdClick: ");
    }

    @Override
    public void onAdDismissed() {
        Log.i(TAG, "onAdDismissed: ");
        mVideoView.setShowAction();
        mBaiduAd.loadAdForVideoApp(mVideoView.getWidth(), mVideoView.getHeight());
    }

    @Override
    public void onAdFailed(String s) {
        Log.i(TAG, "onAdFailed: ");
        mBaiduAd.loadAdForVideoApp(mVideoView.getWidth(), mVideoView.getHeight());
    }
}
