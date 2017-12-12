package cn.ittiger.player.listener;

import android.view.MotionEvent;

/**
 * 全屏播放时，手势操作监听逻辑处理
 * @author: ylhu
 * @time: 2017/12/12
 */
public interface FullScreenGestureListener {

    void onTouch(MotionEvent event, FullScreenGestureStateListener fullScreenGestureStateListener,
                 int duration, int currentPlayState);
}
