package cn.ittiger.player.demo;

import android.app.Application;

import cn.ittiger.player.Config;
import cn.ittiger.player.PlayerManager;
import cn.ittiger.player.factory.ExoPlayerFactory;

/**
 * @author: ylhu
 * @time: 17-9-18
 */

public class App extends Application {

    @Override
    public void onCreate() {

        super.onCreate();
        AppContext.init(this);
        PlayerManager.loadConfig(
                new Config.Builder(this)
                .buildPlayerFactory(new ExoPlayerFactory(this))
                .enableSmallWindowPlay()
                .cache(true)
                .build()
        );
    }
}
