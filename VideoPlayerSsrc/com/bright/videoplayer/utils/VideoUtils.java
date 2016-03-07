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

package com.bright.videoplayer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dalvik.system.PathClassLoader;

public class VideoUtils {

    public enum eAndroidOS {
        UNKNOWN, MIUI, EmotionUI, Flyme, NubiaUI, Nokia_X, ColorOS, HTC, ZTE, FuntouchOS,
    }

    public static final String FORMAT_ALL_DATE = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String FORMAT_TIME = "HH:mm:ss";
    public static final String FORMAT_DATE = "yyyy-MM-dd";
    public static final String FORMAT_SIMPLE_DATE = "yyyyMMdd";

    private final static SimpleDateFormat sFORMAT = new SimpleDateFormat(
            FORMAT_ALL_DATE, Locale.CHINA);

    private static String mSystemProperty = getSystemProperty();

    private final static String TAG = "VideoUtils";

    public static String getAppVersion(Context context) {
        return "";
    }

    public static String getAppName(Context context) {
        return "";
    }

    public static String getDocumentPath(Context context) {
        return context.getFilesDir().getAbsolutePath();
    }

    public static String getSDCardDataPath(Context context) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static boolean getSDCardRemainCanWrite(Context context,
                                                  long remainSize) {
        String path = getSDCardDataPath(context);
        StatFs statFS = new StatFs(path);
        long blockSize = 0L;
        if (getSDKInt() >= 18) {
            blockSize = statFS.getBlockCountLong();
        } else {
            blockSize = statFS.getBlockSize();
        }
        long availableBlock = 0L;
        if (getSDKInt() >= 18) {
            availableBlock = statFS.getAvailableBlocksLong();
        } else {
            availableBlock = statFS.getAvailableBlocks();
        }
        long size = blockSize * availableBlock;
        if (size > remainSize) {
            return true;
        }

        return false;
    }

    public static String getSDKVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        String pkgName = context.getPackageName();
        PackageInfo pkgInfo = null;
        String ret = "";
        try {
            pkgInfo = pm.getPackageInfo(pkgName,
                    PackageManager.GET_CONFIGURATIONS);
            ret = pkgInfo.versionName;
        } catch (NameNotFoundException ex) {

        }
        return ret;
    }

    public static String generatePlayTime(long time) {
        if (time % 1000 >= 500) {
            time += 1000;
        }
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        return hours > 0 ?
                String.format(Locale.CHINA, "%02d:%02d:%02d", hours, minutes, seconds)
                :
                String.format(Locale.CHINA, "%02d:%02d", minutes, seconds);
    }

    /**
     * 用反射方式加载类
     *
     * @param context
     * @param path
     * @return
     */
    public static Object loadClass(Context context, String path) {
        try {
            String dexPath = context.getApplicationInfo().sourceDir;
            PathClassLoader pathClassLoader = new PathClassLoader(dexPath,
                    context.getClassLoader());
            Class<?> c = Class.forName(path, true, pathClassLoader);
            Object ret = c.newInstance();
            return ret;
        } catch (InstantiationException ex1) {
            ex1.printStackTrace();
        } catch (IllegalAccessException ex2) {
            ex2.printStackTrace();
        } catch (ClassNotFoundException ex3) {
            ex3.printStackTrace();
        }

        return null;
    }

    public static String generateTime(long time, boolean isLong) {
        Date date = new Date(time);
        sFORMAT.applyPattern(isLong ? FORMAT_ALL_DATE : FORMAT_TIME);
        String LgTime = null;
        try {
            LgTime = sFORMAT.format(date);
        } catch (Exception e) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(
                        isLong ? FORMAT_ALL_DATE : FORMAT_TIME, Locale.CHINA);
                LgTime = format.format(new Date());
                e.printStackTrace();
            } catch (Exception e1) {
                e1.printStackTrace();
                LgTime = "";
            }
        }
        return LgTime;
    }

    public static String getOSVersionInfo() {
        return Build.VERSION.RELEASE;
    }

    /**
     * Get mobile model, like GT-i9500 etc.
     *
     * @return
     */
    public static String getMobileModel() {
        return Build.MODEL;
    }

    public static String getSystemProperty(String propName) {
        String line = "";
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(
                    new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            Log.e(TAG, "Unable to read sysprop " + propName, ex);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }

    private static String getSystemProperty() {
        String line = "";
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop");
            input = new BufferedReader(
                    new InputStreamReader(p.getInputStream()), 2048);
            String ret = input.readLine();
            while (ret != null) {
                line += ret + "\n";
                ret = input.readLine();
            }
            input.close();
        } catch (IOException ex) {
            Log.e(TAG, "Unable to read sysprop", ex);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }

    public static eAndroidOS filterOS() {
        String prop = mSystemProperty;
        if (prop.contains("miui")) {
            return eAndroidOS.MIUI;
        } else if (prop.contains("EmotionUI")) {
            return eAndroidOS.EmotionUI;
        } else if (prop.contains("flyme")) {
            return eAndroidOS.Flyme;
        } else if (prop.contains("[ro.build.user]: [nubia]")) {
            return eAndroidOS.NubiaUI;
        } else if (prop.contains("Nokia_X")) {
            return eAndroidOS.Nokia_X;
        } else if (prop.contains("[ro.build.soft.version]: [A.")) {
            return eAndroidOS.ColorOS;
        } else if (prop.contains("ro.htc.")) {
            return eAndroidOS.HTC;
        } else if (prop.contains("[ro.build.user]: [zte")) {
            return eAndroidOS.ZTE;
        } else if (prop.contains("[ro.product.brand]: [vivo")) {
            return eAndroidOS.FuntouchOS;
        }
        return eAndroidOS.UNKNOWN;
    }

    public static String getBrand() {
        return Build.BRAND;
    }

    public static String getModel() {
        return Build.MODEL;
    }

    public static String getSDKRelease() {
        return Build.VERSION.RELEASE;
    }

    public static int getSDKInt() {
        return Build.VERSION.SDK_INT;
    }

    public static eAndroidOS getOS() {
        return filterOS();
    }
}
