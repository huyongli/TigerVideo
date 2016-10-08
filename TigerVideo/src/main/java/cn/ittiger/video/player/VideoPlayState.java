package cn.ittiger.video.player;

/**
 * @author: laohu on 2016/8/11
 * @site: http://ittiger.cn
 */
public enum VideoPlayState {
    STOP,           //停止播放
    PREPARE_LOAD,   //加载前的播放器准备
    LOADING,        //正在加载视频
    PLAY,           //正在播放
    PAUSE,          //暂停播放
    FINISH          //播放完成
}
