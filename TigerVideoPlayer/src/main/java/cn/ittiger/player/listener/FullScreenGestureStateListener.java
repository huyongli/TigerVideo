package cn.ittiger.player.listener;

/**
 * 全屏播放时，手势开始或结束监听
 * @author: ylhu
 * @time: 2017/12/12
 */
public interface FullScreenGestureStateListener {

    /**
     * 手势开始
     */
    void onFullScreenGestureStart();

    /**
     * 手势结束
     */
    void onFullScreenGestureFinish();
}
