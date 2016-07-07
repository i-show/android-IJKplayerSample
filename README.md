# IJKplayerSample

本库是基于 Bilibili [ijkplayer](https://github.com/Bilibili/ijkplayer "ijkplayer") 封装而成，只使用的是最简单的播放功能，更加全面的功能请自行添加.


###一. 效果演示
![效果演示](https://github.com/BrightYu/IJKplayerSample/blob/master/images/01.gif)

###二. 使用方法

##### 1. 导入到工程中
方法a: 直接把整个VideoPlayer整合到工程中并引用

```
1. 在setting.gradle 中加入
...
include ':VideoPlayer'
...

2. 主App里面中添加
...
// 视频播放库
compile project(':VideoPlayer')
...

```
方法b: 使用jcenter直接引用

```
...
compile 'com.yuhaiyang:videoplayer:0.0.1'
...

```
#######注：2016年7月7日 上传中审核中

##### 2. 代码使用

a. xml引用
```
	...
	...

	<!--  解决竖屏问题-->
    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="220dp"
        android:background="@android:color/black">

        <com.bright.videoplayer.widget.media.VideoView
            android:id="@+id/video_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:layout_gravity="center" />
    </FrameLayout>
	...
	...

```

注意： 外面有包裹一个FrameLayout ,为了防止横竖屏切换VideoView大小的问题

b. java实现

```
		...
		...
        // init UI
        mMediaController = new MediaController(this);
        mMediaController.setCallBack(mCallBack);
        mMediaController.setPlayNextVisibility(View.GONE);
        mMediaController.setTitle("变形金刚2");

        mVideoView = (VideoView) findViewById(R.id.video_view);
        mVideoView.setMediaController(mMediaController);
        // prefer mVideoPath
        mVideoView.setVideoPath("播放地址");

        mVideoView.start();
		...
		...
```
    

License
=======

    Copyright 2015 Haiyang Yu
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.