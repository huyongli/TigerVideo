package cn.ittiger.player.message;

/**
 * 播放状态发生改变时发送的消息
 * @author: ylhu
 * @time: 17-9-11
 */
public class UIStateMessage extends Message {
    /**
     * 当前播放状态
     */
    private int mState;

    public UIStateMessage(int hash, String videoUrl, int state) {

        super(hash, videoUrl);
        mState = state;
    }

    public int getState() {

        return mState;
    }
}
