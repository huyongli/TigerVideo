package cn.ittiger.player.state;

/**
 * 屏幕状态
 * @author: ylhu
 * @time: 17-9-14
 */
public final class ScreenState {
    /**
     * 正常播放状态
     */
    public static final int SCREEN_STATE_NORMAL = 1;
    /**
     * 全屏播放状态
     */
    public static final int SCREEN_STATE_FULLSCREEN = 2;
    /**
     * 小窗口播放状态
     */
    public static final int SCREEN_STATE_SMALL_WINDOW = 3;

    public static boolean isFullScreen(int screenState) {

        return screenState == SCREEN_STATE_FULLSCREEN;
    }

    public static boolean isSmallWindow(int screenState) {

        return screenState == SCREEN_STATE_SMALL_WINDOW;
    }

    public static boolean isNormal(int screenState) {

        return screenState == SCREEN_STATE_NORMAL;
    }
}
