package cn.ittiger.player.factory;

import cn.ittiger.player.media.AbsSimplePlayer;
import cn.ittiger.player.util.Utils;
import cn.ittiger.player.media.VideoMediaPlayer;

/**
 * 创建基于MediaPlayer实现的播放器
 * @author: ylhu
 * @time: 17-9-13
 */
public class MediaPlayerFactory implements IPlayerFactory {

    @Override
    public AbsSimplePlayer create() {

        Utils.log("create MediaPlayer");
        return new VideoMediaPlayer();
    }
}
