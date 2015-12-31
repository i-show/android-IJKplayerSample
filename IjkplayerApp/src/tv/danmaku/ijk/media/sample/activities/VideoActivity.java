/*
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.danmaku.ijk.media.sample.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.sample.R;
import tv.danmaku.ijk.media.sample.utils.VDVideoScreenOrientation;
import tv.danmaku.ijk.media.sample.widget.media.AndroidMediaController;
import tv.danmaku.ijk.media.sample.widget.media.IjkVideoView;

public class VideoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "VideoActivity";


    private AndroidMediaController mMediaController;
    private IjkVideoView mVideoView;

    private boolean mBackPressed;
    private ViewGroup mPGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // init UI
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        mMediaController = new AndroidMediaController(this, false);
        mMediaController.setSupportActionBar(actionBar);

        // init player
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        mPGroup = (ViewGroup) findViewById(R.id.content_p);
        mVideoView = (IjkVideoView) findViewById(R.id.video_view);
        mVideoView.setMediaController(mMediaController);
        // prefer mVideoPath
        //mVideoView.setVideoPath("http://mss.pinet.co/index.php/api/retrieve/3da4edce-b445-42c8-88a7-3b8a1997d61c/playlist.m3u8");
        mVideoView.setVideoPath("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear4/prog_index.m3u8");
        mVideoView.start();

        Button bu1 = (Button) findViewById(R.id.button);
        bu1.setOnClickListener(this);
        Button bu2 = (Button) findViewById(R.id.button2);
        bu2.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        mBackPressed = true;

        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBackPressed || !mVideoView.isBackgroundPlayEnabled()) {
            mVideoView.stopPlayback();
            mVideoView.release(true);
            mVideoView.stopBackgroundPlay();
        } else {
            mVideoView.enterBackground();
        }
        IjkMediaPlayer.native_profileEnd();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_toggle_ratio) {
            int aspectRatio = mVideoView.toggleAspectRatio();
            // String aspectRatioText = MeasureHelper.getAspectRatioText(this, aspectRatio);
            return true;
        } else if (id == R.id.action_toggle_player) {
            int player = mVideoView.togglePlayer();
            String playerText = IjkVideoView.getPlayerText(this, player);
            return true;
        } else if (id == R.id.action_toggle_render) {
            int render = mVideoView.toggleRender();
            String renderText = IjkVideoView.getRenderText(this, render);
            return true;
        } else if (id == R.id.action_show_info) {
            mVideoView.showMediaInfo();
        } else if (id == R.id.action_show_tracks) {

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                VDVideoScreenOrientation.setPortrait(this);
                VDVideoScreenOrientation.setStatusBarVisible(this, false);
                changeToP();
                break;
            case R.id.button2:
                VDVideoScreenOrientation.setLandscape(this);
                VDVideoScreenOrientation.setStatusBarVisible(this, true);
                changeToRoot();
                break;
        }
    }

    /**
     * 将当前给定的容器，提升到activity的顶层容器中
     */
    private void changeToRoot() {
        mPGroup.removeAllViews();
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        if (root != null) {
            try {
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                root.addView(mVideoView, lp);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void changeToP() {
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.removeView(mVideoView);
        if (root != null) {
            try {
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                mPGroup.addView(mVideoView, lp);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


}
