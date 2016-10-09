# TigerVideo

此App是为了练习Android视频播放而完成的。

# 主要使用的开源库
Mosby(一个MVP框架)、RxJava、Retrofit2、ButterKnife、Glide、Systembartint、TigerDB、Material Design相关控件

# 功能
主要完成了如下功能：  
1. 列表中播放视频  
2. 列表滑动时如果正在播放视频，则列表中播放的视频自动切换为小窗口浮动播放  
3. 列表播放切换到全屏播放模式  
4. 视频播放时也添加了完整的暂停、播放等控制操作，也添加了视频播放进度，视频缓冲进度等显示功能  
5. 视频全屏播放时支持左右滑动实现视频的快进和后退，支持右边上下滑动调整播放音量，左边上下滑动调整播放器的亮度等手势控制操作  

# 播放器
* 播放器采用`Android`中自带的`TextureView + MediaPlayer`实现，同时视频播放过程中因为涉及到列表、小窗口、全屏续播等问题，所以整个视频播放过程中，播放器采用的是全局单例模式实现，这样就可以达到无缝切换续播的问题  
*  切换到全屏播放时，是直接重新开启一个横向的`Activity`来实现全屏播放  

# 效果图
<img src="screenshots/1.jpg" width = "360" height = "640" alt=""/>
<img src="screenshots/2.jpg" width = "360" height = "640" alt=""/>
<img src="screenshots/3.jpg" width = "360" height = "640" alt=""/>
<img src="screenshots/4.jpg" width = "360" height = "640" alt=""/>
<img src="screenshots/5.jpg" width = "640" height = "360" alt=""/>
<img src="screenshots/6.jpg" width = "360" height = "640" alt=""/>

# 安装Demo
[点击下载](http://fir.im/7qpv)

# 声明
本项目使用的视频数据分别来自于网易，头条快报，凤凰视频，在此表示感谢，视频数据API版权归原所属公司所有，请勿用于其他用途！若内容有侵权请联系本人进行删除处理，本项目仅供测试学习使用，他人不得滥用其中数据API，他人用于其他用途所造成的纠纷与本人无关。

# 关于作者
Email：huyongl1989@163.com  
博客：[http://ittiger.cn](http://ittiger.cn)  

# LICENSE

Copyright 2016 huyongli(老胡)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
