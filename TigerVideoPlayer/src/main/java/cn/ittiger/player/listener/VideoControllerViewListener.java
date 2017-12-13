package cn.ittiger.player.listener;

/**
 * @author: ylhu
 * @time: 2017/12/13
 */
public interface VideoControllerViewListener {

    void startVideoProgressUpdate();

    void stopVideoProgressUpdate();

    void onVideoDurationChanged(int duration);
}
