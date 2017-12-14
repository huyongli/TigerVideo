package cn.ittiger.player.listener;

/**
 * 播放时，触摸视频的监听(处理touch video时各状态控件的展示与否)
 * @author: laohu on 2017/12/14
 * @site: http://ittiger.cn
 */
public interface VideoTouchListener {

    /**
     * 显示所有的播放状态视图(底部控制条(与进度条显示与否互斥)，播放暂停按钮，全屏时的头部等)
     */
    void showAllPlayStateView();

    /**
     * 隐藏所有的播放状态视图(底部控制条(与进度条显示与否互斥)，播放暂停按钮，全屏时的头部等)
     */
    void hideAllPlayStateView();
}
