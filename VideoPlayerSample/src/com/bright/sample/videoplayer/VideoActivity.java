/**
 * Copyright (C) 2016 The yuhaiyang Android Source Project
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author: y.haiyang@qq.com
 */

package com.bright.sample.videoplayer;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.baidu.mobads.AdSize;
import com.baidu.mobads.InterstitialAd;
import com.bright.sample.videoplayer.listener.VideoBeforeListener;
import com.bright.sample.videoplayer.listener.VideoPauseListener;
import com.bright.videoplayer.utils.ScreenOrientationUtils;
import com.bright.videoplayer.widget.MediaController;
import com.bright.videoplayer.widget.media.VideoView;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class VideoActivity extends AppCompatActivity {
    private static final String TAG = "VideoActivity";
    private static final String BAIDU_ID = "2431069";
    private MediaController mMediaController;
    private VideoView mVideoView;
    private InterstitialAd mBaiduBeforeAd;
    private InterstitialAd mBaiduPauseAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // init UI
        mMediaController = new MediaController(this);
        mMediaController.setCallBack(mCallBack);
        mMediaController.setTitle("变形金刚2");

        mVideoView = (VideoView) findViewById(R.id.video_view);
        mVideoView.setMediaController(mMediaController);
        // prefer mVideoPath
        mVideoView.setVideoPath("http://mss.pinet.co/index.php/api/retrieve/3da4edce-b445-42c8-88a7-3b8a1997d61c/playlist.m3u8");
        //mVideoView.setVideoPath("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear4/prog_index.m3u8");

        mVideoView.start();
        createAd();
    }

    @Override
    protected void onResume() {
        super.onResume();


        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "run: 33333");
                mBaiduBeforeAd.loadAdForVideoApp(mVideoView.getWidth(), mVideoView.getHeight());
                mBaiduPauseAd.loadAdForVideoApp(mVideoView.getWidth(), mVideoView.getHeight());
            }
        }, 500);
    }

    private Handler mHandler = new Handler() {
    };

    @Override
    protected void onStop() {
        super.onStop();
        if (mVideoView.canPause()) {
            mVideoView.pause();
        }
    }

    @Override
    public void onBackPressed() {
        if (ScreenOrientationUtils.isLandscape(this)) {
            mMediaController.changePortrait(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        mVideoView.stopPlayback();
        mVideoView.release(true);
        IjkMediaPlayer.native_profileEnd();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 切换到横屏
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mMediaController.changeLand(true);
        } else {
            mMediaController.changePortrait(true);
        }
    }


    private MediaController.CallBack mCallBack = new MediaController.CallBack() {
        @Override
        public void onPlay(boolean isPlaying) {
            if (!isPlaying) {
                mBaiduPauseAd.showAdInParentForVideoApp(VideoActivity.this, mMediaController.getAdView());
            }
        }

        @Override
        public void onComplete() {

        }

        @Override
        public void onPlayNext() {

        }
    };

    private void createAd() {
        mBaiduBeforeAd = new InterstitialAd(this, AdSize.InterstitialForVideoBeforePlay, BAIDU_ID);
        mBaiduBeforeAd.setListener(new VideoBeforeListener(this, mMediaController, mBaiduBeforeAd, mVideoView));

        mBaiduPauseAd = new InterstitialAd(this, AdSize.InterstitialForVideoPausePlay, BAIDU_ID);
        mBaiduPauseAd.setListener(new VideoPauseListener(mBaiduPauseAd, mVideoView));
    }
}
