package cn.ittiger.player.listener;

/**
 * 视频播放控制条相关处理监听
 * @author: ylhu
 * @time: 2017/12/13
 */
public interface VideoControllerViewListener {

    /**
     * 开始更新视频播放进度
     */
    void startVideoProgressUpdate();

    /**
     * 停止更新视频播放进度
     */
    void stopVideoProgressUpdate();

    /**
     * 视频时长发生变化时的回调处理
     * @param duration
     */
    void onVideoDurationChanged(int duration);
}
