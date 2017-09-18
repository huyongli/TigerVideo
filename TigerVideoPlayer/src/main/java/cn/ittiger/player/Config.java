package cn.ittiger.player;

import cn.ittiger.player.factory.IPlayerFactory;
import cn.ittiger.player.factory.MediaPlayerFactory;
import cn.ittiger.player.util.Utils;

import com.danikula.videocache.HttpProxyCacheServer;
import com.danikula.videocache.ProxyCacheUtils;
import com.danikula.videocache.file.Md5FileNameGenerator;

import android.content.Context;

import java.io.File;

/**
 * 播放器相关功能开关配置
 * @author: ylhu
 * @time: 17-9-14
 */
public final class Config {
    /**
     * 播放器工厂
     *
     * 通过配置工厂可以实现自定义播放器，不管是用MediaPlayer还是ExoPlayer，还是其他的视频播放库均可以自行定义
     */
    private IPlayerFactory mPlayerFactory;
    /**
     * 是否开启自动小窗口播放功能
     */
    private boolean mSmallWindowPlayEnable;
    /**
     * 缓存功能是否开启
     */
    private boolean mCacheEnable;
    /**
     * 缓存代理实现，{@link #mCacheEnable}必须为true才能生效
     */
    private HttpProxyCacheServer mCacheProxy;

    private Config(Builder builder) {

        this.mPlayerFactory = builder.playerFactory;
        this.mSmallWindowPlayEnable = builder.smallWindowPlayEnable;
        this.mCacheEnable = builder.cacheEnable;
        this.mCacheProxy = builder.proxy;
    }

    public IPlayerFactory getPlayerFactory() {

        return mPlayerFactory;
    }

    public boolean isSmallWindowPlayEnable() {

        return mSmallWindowPlayEnable;
    }

    public boolean isCacheEnable() {

        return mCacheEnable;
    }

    public HttpProxyCacheServer getCacheProxy() {

        return mCacheProxy;
    }

    public final static class Builder {
        private Context context;
        private IPlayerFactory playerFactory;
        private boolean smallWindowPlayEnable = false;
        /**
         * 是否开启缓存，默认不开启
         */
        private boolean cacheEnable = false;
        /**
         * 缓存代理实现
         */
        private HttpProxyCacheServer proxy;

        /**
         * @param ctx   context.getApplicationContext()
         */
        public Builder(Context ctx) {

            this.context = ctx;
        }

        protected Builder() {

        }

        /**
         * 配置Player工厂，用于创建播放器
         * 通过配置工厂可以实现自定义播放器，不管是用MediaPlayer还是ExoPlayer，还是其他的视频播放库均可以自行定义
         * @param factory
         * @return
         */
        public Builder buildPlayerFactory(IPlayerFactory factory) {

            this.playerFactory = factory;
            return this;
        }

        /**
         * 开启小窗口播放功能，默认不开启
         * @return
         */
        public Builder enableSmallWindowPlay() {

            this.smallWindowPlayEnable = true;
            return this;
        }

        /**
         * 是否开启视频缓存功能
         * @param cacheEnable   true：会使用默认缓存配置进行视频缓存
         * @return
         */
        public Builder cache(boolean cacheEnable) {

            this.cacheEnable = cacheEnable;
            return this;
        }

        /**
         * 设置缓存代理实现(请先通过{@link #cache(boolean)}开启缓存功能，否则此设置无效
         * @param cacheProxy
         * @return
         */
        public Builder cacheProxy(HttpProxyCacheServer cacheProxy) {

            this.proxy = cacheProxy;
            return this;
        }

        public Config build() {

            if(playerFactory == null) {
                playerFactory = new MediaPlayerFactory();
            }
            if(cacheEnable && proxy == null) {
                proxy = buildCacheProxy();
            }
            return new Config(this);
        }

        private HttpProxyCacheServer buildCacheProxy() {

            return new HttpProxyCacheServer
                    .Builder(context.getApplicationContext())
                    .cacheDirectory(new File(Utils.getCacheDir(context)))
                    .fileNameGenerator(new Md5FileNameGenerator() {
                        @Override
                        public String generate(String url) {

                            return ProxyCacheUtils.computeMD5(url);
                        }
                    })
                    .maxCacheFilesCount(20)
                    .build();
        }
    }
}
