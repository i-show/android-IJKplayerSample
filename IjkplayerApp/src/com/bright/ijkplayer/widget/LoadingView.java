package com.bright.ijkplayer.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bright.ijkplayer.R;
import com.bright.ijkplayer.widget.media.IMediaController;
import com.bright.ijkplayer.widget.media.IjkVideoView;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by yuhaiyang on 2016/1/1.
 */
public class LoadingView extends ProgressBar implements IMediaPlayer.OnInfoListener {
    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.loading_progress));
    }

    public void setAnchorView(IjkVideoView view) {
        if (view == null) {
            return;
        }
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        view.addView(this, lp);
        view.bringChildToFront(this);
        view.setOnInfoListener(this);
    }

    @Override
    public boolean onInfo(IMediaPlayer mp, int what, int extra) {
        return false;
    }
}
