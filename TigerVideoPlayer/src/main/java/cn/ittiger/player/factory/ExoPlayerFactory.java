package cn.ittiger.player.factory;

import android.content.Context;

import cn.ittiger.player.media.AbsSimplePlayer;
import cn.ittiger.player.util.Utils;
import cn.ittiger.player.media.VideoExoPlayer;

/**
 * 创建基于ExoPlayer实现的播放器
 * @author: ylhu
 * @time: 17-9-13
 */
public class ExoPlayerFactory implements IPlayerFactory {
    protected Context mContext;

    public ExoPlayerFactory(Context context) {

        mContext = context;
    }

    @Override
    public AbsSimplePlayer create() {

        Utils.log("create ExoPlayer");
        return new VideoExoPlayer(mContext);
    }
}
