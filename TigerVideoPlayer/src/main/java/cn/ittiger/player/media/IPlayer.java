package cn.ittiger.player.media;

import android.view.TextureView;

import cn.ittiger.player.state.PlayState;

/**
 * 播放器接口
 * @author: ylhu
 * @time: 17-9-8
 */
public interface IPlayer {

    /**
     * 开始播放指定Url 的视频
     * @param url
     */
    void start(String url);

    /**
     * 继续播放当前视频
     */
    void play();

    /**
     * 暂停播放
     */
    void pause();

    /**
     * 停止播放,即结束当前视频的播放操作，但不释放资源
     */
    void stop();

    /**
     * 释放资源
     */
    void release();

    /**
     * 设置播放状态
     *
     * @param state {@link PlayState}
     */
    void setState(int state);

    /**
     * 获取当前的播放状态
     *
     * @return {@link PlayState}
     */
    int getState();

    /**
     * 当前是否正在播放
     *
     * @return boolean
     */
    boolean isPlaying();

    /**
     * 获取当前的播放进度
     *
     * @return pos 播放进度，默认为0
     */
    int getCurrentPosition();

    /**
     * 获取视频时长
     * @return
     */
    int getDuration();

    /**
     * 跳跃到position位置开始播放
     *
     * @param position
     */
    void seekTo(int position);

    /**
     * 设置播放回调函数
     * @param playCallback
     */
    void setPlayCallback(PlayCallback playCallback);

    /**
     * @param textureView
     */
    void setTextureView(TextureView textureView);

    interface PlayCallback {

        void onError(String error);

        void onComplete();

        void onPlayStateChanged(int state);

        void onDurationChanged(int duration);
    }
}
