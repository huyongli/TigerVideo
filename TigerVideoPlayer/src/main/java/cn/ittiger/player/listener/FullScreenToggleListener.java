package cn.ittiger.player.listener;

/**
 * 全屏与非全屏切换监听
 * @author: ylhu
 * @time: 2017/12/13
 */
public interface FullScreenToggleListener {

    /**
     * 开始进入全屏
     */
    void onStartFullScreen();

    /**
     * 开始推出全屏
     */
    void onExitFullScreen();
}
