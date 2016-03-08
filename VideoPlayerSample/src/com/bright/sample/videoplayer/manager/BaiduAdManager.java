package com.bright.sample.videoplayer.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;

import com.baidu.mobads.AdSize;
import com.baidu.mobads.BaiduManager;
import com.baidu.mobads.InterstitialAd;
import com.bright.sample.videoplayer.R;
import com.bright.sample.videoplayer.listener.VideoBeforeListener;
import com.bright.sample.videoplayer.listener.VideoPauseListener;

/**
 * Created by yuhaiyang on 2016/3/8.
 */
public class BaiduAdManager {
    private static final String VIDEO_AD_ID = "2058626";
    private static final int HANDLE_LOAD_VIDEO_BEFORE_AD = 10001;
    private static final int HANDLE_LOAD_VIDEO_PAUSE_AD = 10002;
    private static BaiduAdManager sInstance;
    private InterstitialAd mVideoBeforeAd;
    private InterstitialAd mVideoPauseAd;
    /**
     * 使用的是Application的Context
     */
    private Context mContext;

    private int mVideoWith;
    private int mVideoHeight;

    private boolean isBeforeAdOk = false;
    private boolean isPauseAdOk = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLE_LOAD_VIDEO_BEFORE_AD:
                    if (!mVideoBeforeAd.isAdReady()) {
                        mVideoBeforeAd.loadAdForVideoApp(mVideoWith, mVideoHeight);
                        mHandler.sendEmptyMessageDelayed(HANDLE_LOAD_VIDEO_BEFORE_AD, 2000);
                    }
                    break;
                case HANDLE_LOAD_VIDEO_PAUSE_AD:
                    if (!mVideoPauseAd.isAdReady()) {
                        mVideoPauseAd.loadAdForVideoApp(mVideoWith, mVideoHeight);
                        mHandler.sendEmptyMessageDelayed(HANDLE_LOAD_VIDEO_PAUSE_AD, 2000);
                    }
                    break;
            }
        }
    };

    private BaiduAdManager(Context context) {
        mContext = context;
        BaiduManager.init(mContext);
        mVideoBeforeAd = new InterstitialAd(mContext, AdSize.InterstitialForVideoBeforePlay, VIDEO_AD_ID);
        mVideoPauseAd = new InterstitialAd(mContext, AdSize.InterstitialForVideoPausePlay, VIDEO_AD_ID);

        mVideoWith = context.getResources().getDimensionPixelSize(R.dimen.dp_360);
        mVideoHeight = context.getResources().getDimensionPixelSize(R.dimen.dp_220);

        mHandler.sendEmptyMessageDelayed(HANDLE_LOAD_VIDEO_BEFORE_AD, 1000);
        mHandler.sendEmptyMessageDelayed(HANDLE_LOAD_VIDEO_PAUSE_AD, 1000);
    }

    public static BaiduAdManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (BaiduAdManager.class) {
                if (sInstance == null) {
                    sInstance = new BaiduAdManager(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    public InterstitialAd getVideoBeforeAd() {
        return mVideoBeforeAd;
    }

    public InterstitialAd getVideoPauseAd() {
        return mVideoPauseAd;
    }
}
