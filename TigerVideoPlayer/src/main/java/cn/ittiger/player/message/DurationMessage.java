package cn.ittiger.player.message;

/**
 * 当视频时长解析成功时发送此消息进行视频时长的更新
 * @author: ylhu
 * @time: 17-9-11
 */
public class DurationMessage extends Message {
    private int mDuration;

    public DurationMessage(int hash, String videoUrl, int duration) {

        super(hash, videoUrl);
        mDuration = duration;
    }

    public int getDuration() {

        return mDuration;
    }
}
