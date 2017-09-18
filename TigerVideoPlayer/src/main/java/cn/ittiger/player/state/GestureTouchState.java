package cn.ittiger.player.state;

/**
 * 全屏播放时手势触摸操作的状态
 * @author: laohu on 2017/9/16
 * @site: http://ittiger.cn
 */
public final class GestureTouchState {
    /**
     * 无操作
     */
    public static final int STATE_NONE = 0;
    /**
     * 快进或后退
     */
    public static final int STATE_VIDEO_PROGRESS = 1;
    /**
     * 调节音量
     */
    public static final int STATE_VOLUME = 2;
    /**
     * 调节亮度
     */
    public static final int STATE_BRIGHTNESS = 3;
}
