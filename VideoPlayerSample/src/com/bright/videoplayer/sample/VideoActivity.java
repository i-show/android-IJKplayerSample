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

package com.bright.videoplayer.sample;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.bright.ijkplayer.utils.ScreenOrientationUtils;
import com.bright.ijkplayer.widget.MediaController;
import com.bright.ijkplayer.widget.media.IjkVideoView;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class VideoActivity extends AppCompatActivity {
    private static final String TAG = VideoActivity.class.getSimpleName();

    private MediaController mMediaController;
    private IjkVideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // init UI
        mMediaController = new MediaController(this);
        mMediaController.setTitle("变形金刚2");
        // init player
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        mVideoView = (IjkVideoView) findViewById(R.id.video_view);
        mVideoView.setMediaController(mMediaController);
        // prefer mVideoPath
        mVideoView.setVideoPath("http://mss.pinet.co/index.php/api/retrieve/3da4edce-b445-42c8-88a7-3b8a1997d61c/playlist.m3u8");
        //mVideoView.setVideoPath("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear4/prog_index.m3u8");

        mVideoView.start();

    }

    @Override
    public void onBackPressed() {
        if (ScreenOrientationUtils.isLandscape(this)) {
            mMediaController.changePortrait();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mVideoView.canPause()) {
            mVideoView.pause();
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
            mMediaController.changeLand();
        } else {
            mMediaController.changePortrait();

        }
    }
}
