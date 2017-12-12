package cn.ittiger.player.factory;

import cn.ittiger.player.media.AbsSimplePlayer;

/**
 * 播放器工厂，实现此接口可以自定义实现各种播放器，如基于MediaPlayer和ExoPlayer实现播放器
 * @author: ylhu
 * @time: 17-9-14
 */
public interface IPlayerFactory {

    AbsSimplePlayer create();
}
