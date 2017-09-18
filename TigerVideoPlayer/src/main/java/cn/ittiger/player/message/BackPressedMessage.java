package cn.ittiger.player.message;

/**
 * 全屏状态点击返回键时的消息
 * @author: ylhu
 * @time: 17-9-14
 */
public class BackPressedMessage extends Message {
    private int mScreenState;

    public BackPressedMessage(int screenState, int hash, String videoUrl) {

        super(hash, videoUrl);
        mScreenState = screenState;
    }

    public int getScreenState() {

        return mScreenState;
    }
}
