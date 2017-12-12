package cn.ittiger.player.listener;

/**
 * UI状态更新监听
 * @author: ylhu
 * @time: 2017/12/12
 */
public interface UIStateChangeListener {

    /**
     * UI状态更新为Normal状态，即初始状态
     * @param screenState   当前屏幕状态{@link cn.ittiger.player.state.ScreenState}
     */
    void onChangeUINormalState(int screenState);

    /**
     * UI状态更新为Loading状态，即视频加载状态
     * @param screenState   当前屏幕状态{@link cn.ittiger.player.state.ScreenState}
     */
    void onChangeUILoadingState(int screenState);

    /**
     * UI状态更新为Playing状态，即视频播放状态
     * @param screenState   当前屏幕状态{@link cn.ittiger.player.state.ScreenState}
     */
    void onChangeUIPlayingState(int screenState);

    /**
     * UI状态更新为Pause状态，即视频暂停播放状态
     * @param screenState   当前屏幕状态{@link cn.ittiger.player.state.ScreenState}
     */
    void onChangeUIPauseState(int screenState);

    /**
     * UI状态更新为SeekBuffer状态，即拖动进度条后导致的加载loading
     * @param screenState   当前屏幕状态{@link cn.ittiger.player.state.ScreenState}
     */
    void onChangeUISeekBufferingState(int screenState);

    /**
     * UI状态更新为Complete状态，即视频播放结束状态
     * @param screenState   当前屏幕状态{@link cn.ittiger.player.state.ScreenState}
     */
    void onChangeUICompleteState(int screenState);

    /**
     * UI状态更新为Error状态，即视频播放错误状态
     * @param screenState   当前屏幕状态{@link cn.ittiger.player.state.ScreenState}
     */
    void onChangeUIErrorState(int screenState);
}
