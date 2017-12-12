package cn.ittiger.player;

import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.ViewGroup;

import cn.ittiger.player.message.BackPressedMessage;
import cn.ittiger.player.message.DurationMessage;
import cn.ittiger.player.message.Message;
import cn.ittiger.player.message.UIStateMessage;
import cn.ittiger.player.state.PlayState;
import cn.ittiger.player.state.ScreenState;
import cn.ittiger.player.util.Utils;
import cn.ittiger.player.media.AbsSimplePlayer;
import cn.ittiger.player.media.IPlayer;

import java.util.Observable;
import java.util.Observer;

/**
 * 视频播放管理类，主要与视频展示展示UI进行交互，视频播放的具体操作交由播放器抽象类{@link AbsSimplePlayer}实现
 * 通过此管理类达到视频播放控制与UI层的解耦，同时便于自定义播放器
 *
 * @author: laohu on 2017/9/9
 * @site: http://ittiger.cn
 */
public final class PlayerManager implements IPlayer.PlayCallback {
    private static final String TAG = "PlayerManager";
    private static volatile PlayerManager sPlayerManager;
    private AbsSimplePlayer mPlayer;
    private PlayStateObservable mPlayStateObservable;
    private String mVideoUrl;
    private int mObserverHash = -1;
    private int mScreenState = ScreenState.SCREEN_STATE_NORMAL;
    private Config mConfig;

    private PlayerManager(Config config) {

        mConfig = config;
        createPlayer();

        mPlayStateObservable = new PlayStateObservable();
    }

    private void createPlayer() {

        mPlayer = mConfig.getPlayerFactory().create();
        mPlayer.setPlayCallback(this);
    }

    public Config getConfig() {

        return mConfig;
    }

    /**
     * 加载配置
     * @param config
     */
    public static void loadConfig(Config config) {

        if(sPlayerManager == null) {
            sPlayerManager = new PlayerManager(config);
        }
    }

    public static PlayerManager getInstance() {

        if(sPlayerManager == null) {
            synchronized (PlayerManager.class) {
                if(sPlayerManager == null) {
                    //加载默认配置
                    loadConfig(new Config.Builder().build());
                }
            }
        }
        if(sPlayerManager.mPlayer == null) {
            synchronized (PlayerManager.class) {
                if(sPlayerManager.mPlayer == null) {
                    sPlayerManager.createPlayer();
                }
            }
        }
        return sPlayerManager;
    }

    public void removeTextureView() {

        if(mPlayer.getTextureView() != null &&
            mPlayer.getTextureView().getParent() != null) {
            ((ViewGroup)mPlayer.getTextureView().getParent()).removeView(mPlayer.getTextureView());
            setTextureView(null);
            if(mPlayer.getTextureView() != null) {
                Utils.log("remove TextureView:" + mPlayer.getTextureView().toString());
            }
        }
    }

    public void setTextureView(TextureView textureView) {

        if(textureView != null) {
            Utils.log("set TextureView:" + textureView.toString());
        }
        mPlayer.setTextureView(textureView);
    }

    /**
     * 待播放视频是否已经缓存
     * @param videoUrl
     * @return
     */
    boolean isCached(String videoUrl) {

        if(mConfig.isCacheEnable() && mConfig.getCacheProxy().isCached(videoUrl)) {
            return true;
        }
        return false;
    }

    /**
     * 获取正在播放的视频地址，必须在stop或release方法调用之前获取
     * @return
     */
    public String getVideoUrl() {

        return mVideoUrl;
    }

    public void start(String url, int observerHash) {

        bindPlayerView(url, observerHash);

        onPlayStateChanged(PlayState.STATE_LOADING);
        Utils.log(String.format("start loading video, hash=%d, url=%s", mObserverHash, mVideoUrl));
        String wrapperUrl = url;
        if(mConfig.isCacheEnable()) {
            wrapperUrl = mConfig.getCacheProxy().getProxyUrl(url);
        }
        mPlayer.start(wrapperUrl);
    }

    void bindPlayerView(String url, int observerHash) {

        this.mVideoUrl = url;
        this.mObserverHash = observerHash;
    }

    public void play() {

        Utils.log(String.format("play video, hash=%d, url=%s", mObserverHash, mVideoUrl));
        mPlayer.play();
        onPlayStateChanged(PlayState.STATE_PLAYING);
    }

    public void resume() {

        if(getState() == PlayState.STATE_PAUSE) {
            Utils.log(String.format("resume video, hash=%d, url=%s", mObserverHash, mVideoUrl));
            play();
        }
    }

    public void pause() {

        if(getState() == PlayState.STATE_PLAYING) {
            Utils.log(String.format("pause video, hash=%d, url=%s", mObserverHash, mVideoUrl));
            mPlayer.pause();
            onPlayStateChanged(PlayState.STATE_PAUSE);
        } else {
            Utils.log(String.format("pause video for state: %d, hash=%d, url=%s", getState(), mObserverHash, mVideoUrl));
        }
    }

    public void stop() {

        Utils.log(String.format("stop video, hash=%d, url=%s", mObserverHash, mVideoUrl));
        onPlayStateChanged(PlayState.STATE_NORMAL);
        mPlayer.stop();
        removeTextureView();
        mObserverHash = -1;
        mVideoUrl = null;
        mScreenState = ScreenState.SCREEN_STATE_NORMAL;
    }

    public void release() {

        Utils.log("release player");
        mPlayer.setState(PlayState.STATE_NORMAL);
        removeTextureView();
        mPlayer.release();
        mPlayer = null;
        mObserverHash = -1;
        mVideoUrl = null;
        mScreenState = ScreenState.SCREEN_STATE_NORMAL;
    }

    /**
     * 界面上是否存在视频播放
     * @return
     */
    public boolean hasViewPlaying() {

        return mObserverHash != -1;
    }

    /**
     * 判断视频播放时是否需要自己处理返回键事件
     * @return
     */
    public boolean onBackPressed() {

        boolean consume = ScreenState.isNormal(mScreenState);
        if(consume == false) {
            mPlayStateObservable.notify(new BackPressedMessage(mScreenState, mObserverHash, mVideoUrl));
            return true;
        }
        return false;
    }

    public boolean isPlaying() {

        return mPlayer.isPlaying();
    }

    /**
     * 指定View是否在播放视频
     * @param viewHash
     * @return
     */
    boolean isViewPlaying(int viewHash) {

        return mObserverHash == viewHash;
    }

    public int getCurrentPosition() {

        return mPlayer.getCurrentPosition();
    }

    public void seekTo(int position) {

        if(isPlaying()) {
            onPlayStateChanged(PlayState.STATE_PLAYING_BUFFERING_START);
        }
        mPlayer.seekTo(position);
    }

    public int getState() {

        return sPlayerManager.mPlayer.getState();
    }

    @Override
    public void onError(String error) {

        Utils.log("error video, error= " +  error == null ? "null" : error + ", url=" + mVideoUrl);
        if(!TextUtils.isEmpty(error)) {
            Log.d(TAG, error);
        }
        mPlayer.stop();
        changeUIState(PlayState.STATE_ERROR);
    }

    @Override
    public void onComplete() {

        changeUIState(PlayState.STATE_AUTO_COMPLETE);
    }

    @Override
    public void onPlayStateChanged(int state) {

        changeUIState(state);
    }

    @Override
    public void onDurationChanged(int duration) {

        mPlayStateObservable.notify(new DurationMessage(mObserverHash, mVideoUrl, duration));
    }

    void addObserver(Observer observer) {

        mPlayStateObservable.addObserver(observer);
    }

    void removeObserver(Observer observer) {

        mPlayStateObservable.deleteObserver(observer);
    }

    private void changeUIState(int state) {

        mPlayer.setState(state);

        mPlayStateObservable.notify(new UIStateMessage(mObserverHash, mVideoUrl, state));
    }

    public void setScreenState(int screenState) {

        mScreenState = screenState;
    }

    class PlayStateObservable extends Observable {

        private void setObservableChanged() {

            this.setChanged();
        }

        public void notify(Message message) {

            setObservableChanged();
            notifyObservers(message);
        }
    }
}
