package cn.ittiger.player.state;

/**
 * 播放状态
 * @author: ylhu
 * @time: 17-9-8
 */
public final class PlayState {
    public static final int STATE_NORMAL = 0;
    public static final int STATE_LOADING = 1;
    public static final int STATE_PLAYING = 2;
    public static final int STATE_PLAYING_BUFFERING_START = 3;
    public static final int STATE_PAUSE = 4;
    public static final int STATE_AUTO_COMPLETE = 5;
    public static final int STATE_ERROR = 6;
}
