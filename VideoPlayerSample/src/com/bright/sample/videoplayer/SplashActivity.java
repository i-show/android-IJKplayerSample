package com.bright.sample.videoplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import com.baidu.mobads.SplashAd;
import com.baidu.mobads.SplashAdListener;
import com.bright.sample.videoplayer.constant.Configure;

/**
 * 实时开屏，广告实时请求并且立即展现
 */
public class SplashActivity extends Activity {
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Configure.SHOW_BAIDU_AD) {
            Intent intent = new Intent(SplashActivity.this, VideoActivity.class);
            startActivity(intent);
            SplashActivity.this.finish();
            finish();
            return;
        }

        setContentView(R.layout.activity_splash);
        RelativeLayout adContent = (RelativeLayout) this.findViewById(R.id.ad);
        new SplashAd(this, adContent, mSplashAdListener, Configure.BAIDU_SPLASH_ID, true);
    }

    /**
     * 监听
     */
    private SplashAdListener mSplashAdListener = new SplashAdListener() {
        @Override
        public void onAdDismissed() {
            Log.i(TAG, "onAdDismissed");
            Intent intent = new Intent(SplashActivity.this, VideoActivity.class);
            startActivity(intent);
            SplashActivity.this.finish();
        }

        @Override
        public void onAdFailed(String arg0) {
            Log.i(TAG, "onAdFailed");
            Intent intent = new Intent(SplashActivity.this, VideoActivity.class);
            startActivity(intent);
            SplashActivity.this.finish();
        }

        @Override
        public void onAdPresent() {
            Log.i(TAG, "onAdPresent");
        }

        @Override
        public void onAdClick() {
            Log.i("SplashActivity", "onAdClick");
            // 设置开屏可接受点击时，该回调可用
        }
    };


}
