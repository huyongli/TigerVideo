package cn.ittiger.player.message;

/**
 * 播放器的状态发生改变时发送消息进行对应状态的UI更新（采用观察者模式）
 *
 * 消息基类
 * @author: ylhu
 * @time: 17-9-11
 */
public class Message {
    /**
     * 观察者对象(VideoPlayer-播放器视图)的hashCode，用于自身UI更新时的身份确认
     */
    private int mHash;
    /**
     * 播放的视频地址，用于观察者更新UI时的身份确认
     */
    private String mVideoUrl;

    public Message(int hash, String videoUrl) {

        mHash = hash;
        mVideoUrl = videoUrl;
    }

    public int getHash() {

        return mHash;
    }

    public String getVideoUrl() {

        return mVideoUrl;
    }
}
