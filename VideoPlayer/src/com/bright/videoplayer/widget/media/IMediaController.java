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

package com.bright.videoplayer.widget.media;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;

public interface IMediaController {
    /**
     * 显示普通功能区
     */
    void hide();

    /**
     * 隐藏普通功能区
     */
    void show();

    /**
     * 普通功能区是否显示
     */
    boolean isShowing();

    /**
     * 显示Loading
     */
    void showLoading();

    /**
     * 隐藏loading
     */
    void hideLoading();

    /**
     * 显示滑动的View
     */
    void showSlideView(long position, float distance);

    /**
     * 隐藏滑动的View
     */
    void hideSlideView();

    /**
     * 生效距离
     */
    void effectiveSlide(long position);

    /**
     * 获取广告View
     */
    RelativeLayout getAdView();

    void setAnchorView(View view);

    void setEnabled(boolean enabled);

    void setMediaPlayer(MediaController.MediaPlayerControl player);


}
