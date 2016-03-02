/**
 * 全屏部分的变量暂存以及调整当前屏幕方向等等的函数包
 *
 * @author sunxiao
 */

package com.bright.ijkplayer.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.WindowManager;

import java.lang.reflect.Field;

public class ScreenOrientationUtils {


    public static void setLandscape(Context context) {
        if (context != null) {
            if (android.os.Build.VERSION.SDK_INT >= 9) {
                ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            } else {
                ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
    }

    public static void setPortrait(Context context) {
        if (context != null) {
            if (android.os.Build.VERSION.SDK_INT >= 9) {
                ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            } else {
                ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    }


    public static boolean isLandscape(Context context) {
        if (context != null) {
            return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        }
        return false;
    }

    public static boolean isPortrait(Context context) {
        if (context != null) {
            return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        }
        return false;
    }

    public static void setStatusBarVisible(Context ctt, boolean isVisible) {
        Activity context = (Activity) ctt;
        if (context == null) {
            return;
        }
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        if (isVisible) {
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            context.getWindow().setAttributes(lp);
        } else {
            lp.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            context.getWindow().setAttributes(lp);
            context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    /**
     * 取得状态栏的高度，取法有点诡异，看看是否有其他方式可以拿到
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            return context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            return (int) (0.5D + (context.getResources().getDisplayMetrics().densityDpi / 160F) * 25);
        }
    }
}
