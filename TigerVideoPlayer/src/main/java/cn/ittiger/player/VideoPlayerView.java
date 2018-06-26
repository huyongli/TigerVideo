package cn.ittiger.player;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import cn.ittiger.player.message.BackPressedMessage;
import cn.ittiger.player.message.DurationMessage;
import cn.ittiger.player.message.Message;
import cn.ittiger.player.message.UIStateMessage;
import cn.ittiger.player.state.PlayState;
import cn.ittiger.player.state.ScreenState;
import cn.ittiger.player.ui.StandardVideoView;
import cn.ittiger.player.util.Utils;

import java.util.Observable;
import java.util.Observer;

/**
 * 播放器，主要处理视频的播放暂停，全屏播放切换，小窗口播放切换等逻辑
 *
 * 视频播放的真正控制交由{@link PlayerManager}实现
 * @author: ylhu
 * @time: 17-9-8
 */
public class VideoPlayerView extends StandardVideoView implements
    AudioManager.OnAudioFocusChangeListener,
    Observer {
    /**
     * 当前Observer（即：VideoPlayerView本身）对象的hashcode
     */
    private int mViewHash;
    /**
     * 当前播放状态
     */
    private int mCurrentState = PlayState.STATE_NORMAL;
    /**
     * 当前屏幕播放状态
     */
    private int mCurrentScreenState = ScreenState.SCREEN_STATE_NORMAL;
    /**
     * 屏幕宽度
     */
    private int mScreenWidth;
    /**
     * 小窗口的宽度
     */
    private int mSmallWindowWidth;
    /**
     * 小窗口的高度
     */
    private int mSmallWindowHeight;

    public VideoPlayerView(Context context) {

        this(context, null);
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {

        super(context, attrs);
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void initView(Context context, AttributeSet attrs) {

        super.initView(context, attrs);

        mViewHash = this.toString().hashCode();
        mScreenWidth = Utils.getWindowWidth(context);
        mSmallWindowWidth = mScreenWidth / 2;
        mSmallWindowHeight = (int) (mSmallWindowWidth * 1.0f / 16 * 9 + 0.5f);
    }

    private void resetViewState() {

        setCurrentScreenState(ScreenState.SCREEN_STATE_NORMAL);
        onPlayStateChanged(PlayState.STATE_NORMAL);
    }

    protected void setCurrentScreenState(int currentScreenState) {

        mCurrentScreenState = currentScreenState;
        mVideoControllerView.setCurrentScreenState(currentScreenState);
        PlayerManager.getInstance().setScreenState(currentScreenState);
    }

    @Override
    public int getCurrentScreenState() {

        return mCurrentScreenState;
    }

    @Override
    public int getCurrentState() {

        return mCurrentState;
    }

    /**
     * 绑定数据
     * @param videoUrl
     */
    @Override
    public void bind(String videoUrl, CharSequence title, boolean showNormalStateTitleView) {

        super.bind(videoUrl, title, showNormalStateTitleView);
        resetViewState();
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if(R.id.vp_video_surface_container == id) {
            return;
        }
        if(!PlayerManager.getInstance().isViewPlaying(mViewHash)) {
            //存在正在播放的视频，先将上一个视频停止播放，再继续下一个视频的操作
            PlayerManager.getInstance().stop();
        }
        int state = PlayerManager.getInstance().getState();

        if(v == mVideoThumbView) {
            //点击视频缩略图
            startPlayVideo();
        } else if(v == mVideoErrorView) {
            //点击播放错误视图
            startPlayVideo();
        } else if (R.id.vp_video_play == id) {
            //点击播放按钮
            if(TextUtils.isEmpty(mVideoUrl)) {
                Toast.makeText(getContext(), R.string.vp_no_url, Toast.LENGTH_SHORT).show();
                return;
            }
            switch (state) {
                case PlayState.STATE_NORMAL:
                case PlayState.STATE_ERROR:
                    startPlayVideo();
                    break;
                case PlayState.STATE_PLAYING:
                    PlayerManager.getInstance().pause();
                    break;
                case PlayState.STATE_PAUSE:
                    PlayerManager.getInstance().play();
                    break;
                case PlayState.STATE_AUTO_COMPLETE:
                    PlayerManager.getInstance().seekTo(0);
                    PlayerManager.getInstance().play();
                    break;
            }
        } else if(R.id.vp_video_small_window_back == id) {
            //关闭小窗口播放，并停止播放当前视频
            onExitSmallWindowPlay(true);
        } else if(R.id.vp_fullscreen_lock == id) {
            //全屏播放时锁定当前播放状态
            onToggleFullScreenLockState(!mFullScreenLocked);
        }
    }

    /**
     * 开始播放视频
     */
    public void startPlayVideo() {

        if(canPlay()) {
            play();
        } else {
            Toast.makeText(getContext(), R.string.vp_no_network, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 开始播放
     */
    protected void play() {

        ((Activity)getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestAudioFocus();
        //先移除播放器关联的TextureView
        PlayerManager.getInstance().removeTextureView();

        TextureView textureView = createTextureView();
        mVideoTextureViewContainer.addView(textureView);
        //准备开始播放
        PlayerManager.getInstance().start(mVideoUrl, mViewHash);
        PlayerManager.getInstance().setTextureView(textureView);
    }

    /**
     * 是否可以播放，用于播放前的判断
     * @return
     */
    protected boolean canPlay() {

        return Utils.isConnected(getContext()) || PlayerManager.getInstance().isCached(mVideoUrl);
    }

    /************************ 使用观察者模式监听播放状态的变化 ********************************/
    /************************ 播放状态发生改变时的相关逻辑处理 ********************************/

    @Override
    public final void update(Observable o, final Object arg) {

        if(getContext() == null) {
            return;
        }
        if(!(arg instanceof Message)) {
            return;
        }
        if(mViewHash != ((Message) arg).getHash() ||
            !mVideoUrl.equals(((Message) arg).getVideoUrl())) {
            return;
        }

        if(arg instanceof DurationMessage) {
            ((Activity)getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mVideoControllerView.onVideoDurationChanged(((DurationMessage) arg).getDuration());
                }
            });
            return;
        }

        if(arg instanceof BackPressedMessage) {
            onBackPressed((BackPressedMessage) arg);
            return;
        }

        if(!(arg instanceof UIStateMessage)) {
            return;
        }
        ((Activity)getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                onPlayStateChanged(((UIStateMessage) arg).getState());
            }
        });
    }

    /**
     * 播放状态发生改变时调用
     * @param state
     */
    protected void onPlayStateChanged(int state) {

        mCurrentState = state;
        onChangeUIState(state);
        switch (state) {
            case PlayState.STATE_NORMAL:
                Utils.log("state change to: STATE_NORMAL");
                mVideoControllerView.onVideoDurationChanged(0);
                mVideoControllerView.stopVideoProgressUpdate();
                onExitSmallWindowPlay(true);//stop时，关闭小窗口
                abandonAudioFocus();
                ((Activity)getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                break;
            case PlayState.STATE_LOADING:
                Utils.log("state change to: STATE_LOADING");
                break;
            case PlayState.STATE_PLAYING:
                Utils.log("state change to: STATE_PLAYING");
                mVideoControllerView.startVideoProgressUpdate();
                break;
            case PlayState.STATE_PAUSE:
                Utils.log("state change to: STATE_PAUSE");
                mVideoControllerView.stopVideoProgressUpdate();
                break;
            case PlayState.STATE_PLAYING_BUFFERING_START:
                Utils.log("state change to: STATE_PLAYING_BUFFERING_START");
                break;
            case PlayState.STATE_AUTO_COMPLETE:
                Utils.log("state change to: STATE_AUTO_COMPLETE");
                mVideoControllerView.stopVideoProgressUpdate();
                onExitFullScreen();
                onExitSmallWindowPlay(true);
                break;
            case PlayState.STATE_ERROR:
                Utils.log("state change to: STATE_ERROR");
                mVideoControllerView.onVideoDurationChanged(0);
                mVideoControllerView.stopVideoProgressUpdate();
                abandonAudioFocus();
                break;
            default:
                throw new IllegalStateException("Illegal Play State:" + state);
        }
    }

    /************************ UI状态更新 ********************************/

    /**
     * 更新各个播放状态下的UI
     * @param state    播放状态消息
     */
    private void onChangeUIState(int state) {

        switch (state) {
            case PlayState.STATE_NORMAL:
                onChangeUINormalState(getCurrentScreenState());
                break;
            case PlayState.STATE_LOADING:
                onChangeUILoadingState(getCurrentScreenState());
                break;
            case PlayState.STATE_PLAYING:
                onChangeUIPlayingState(getCurrentScreenState());
                break;
            case PlayState.STATE_PAUSE:
                onChangeUIPauseState(getCurrentScreenState());
                break;
            case PlayState.STATE_PLAYING_BUFFERING_START:
                onChangeUISeekBufferingState(getCurrentScreenState());
                break;
            case PlayState.STATE_AUTO_COMPLETE:
                onChangeUICompleteState(getCurrentScreenState());
                break;
            case PlayState.STATE_ERROR:
                onChangeUIErrorState(getCurrentScreenState());
                break;
            default:
                throw new IllegalStateException("Illegal Play State:" + state);
        }
    }

    @Override
    protected void onAttachedToWindow() {

        super.onAttachedToWindow();
        Utils.log("attached to window, view hash:" + mViewHash);
        PlayerManager.getInstance().addObserver(this);
        mToggleFullScreen = false;
        if(ScreenState.isSmallWindow(getCurrentScreenState())) {
            /***
             * 进入小窗口播放后，视频在列表中原本播放视频的View因滑动进入可视范围从而又重新触发attach window
             * 因为原本播放视频的View与小窗口的mScreenState始终保持一致(小窗口状态{@link ScreenState.SCREEN_STATE_SMALL_WINDOW})，
             * 即使因点击小窗口关闭按钮停止播放后依然保持一致(理解这点很重要，这也是为什么在{@link #toggleSmallWindow()}中不存在视频播放时要重置屏幕状态的原因)，因此会触发此代码块
             */
            toggleSmallWindow();
        }
    }

    @Override
    protected void onDetachedFromWindow() {

        super.onDetachedFromWindow();
        Utils.log("detached from window, view hash:" + mViewHash);
        PlayerManager.getInstance().removeObserver(this);
        if(mToggleFullScreen) {
            /**
             * 因为全屏播放切换触发detach window不做处理
             *
             * 全屏播放与小窗口播放不可能同时存在
             */
            return;
        }
        boolean isSmallWindowEnable = PlayerManager.getInstance().getConfig().isSmallWindowPlayEnable();
        if(isSmallWindowEnable) {
            /**
             * 如果小窗口功能启用则触发detach window有如下几种情况：
             * 1.视频列表中滑动触发
             * 2.进入小窗口视频播放后又退出小窗口播放，小窗口View会触发detach window
             *
             * getId() == R.id.vp_small_window_view_id时是退出小窗口播放时小窗口View触发的detach window
             * 而由退出小窗口播放导致小窗口View触发的detach window不做处理，因为该view已经从window中移除，不需要做其他处理
             * 如果做了处理反倒会影响播放状态
             */
            if(getId() != R.id.vp_small_window_view_id) {
                /**
                 * 视频列表滑动时触发detach window，此时切换小窗口播放状态，即：滑动自动开启小窗口播放
                 */
                toggleSmallWindow();
            }
        } else {
            /**
             * 小窗口播放功能未开启时，如果触发了detach window则直接停止视频播放(如：视频列表滑动触发)
             */
            if(mCurrentState != PlayState.STATE_NORMAL) {
                PlayerManager.getInstance().stop();
            }
            onPlayStateChanged(PlayState.STATE_NORMAL);
        }
    }

    /************************ 全屏播放相关操作 ********************************/
    /**
     * 切换全屏播放状态
     */
    private boolean mToggleFullScreen = false;
    /**
     * 切换全屏播放前当前VideoPlayerView的父容器
     */
    private ViewGroup mOldParent;
    /**
     * 切换全屏播放前当前VideoPlayerView的在父容器中的索引
     */
    private int mOldIndex = 0;
    /**
     * 切换到全屏播放前当前VideoPlayerView的宽度
     */
    private int mVideoWidth;
    /**
     * 切换到全屏播放前当前VideoPlayerView的高度
     */
    private int mVideoHeight;

    /**
     * 全屏与非全屏切换
     *
     * 全屏播放实现逻辑：
     * 1.将当前VideoPlayerView从父容器中移除
     * 2.然后再将当前VideoPlayerView添加到当前Activity的顶级容器Window.ID_ANDROID_CONTENT中
     * 3.设置当前Activity为全屏状态
     * 4.设置横屏
     *
     * 步骤1和2保证了所有的播放操作均为同一对象，不存在播放状态的变化，因而可以有效的避免播放状态导致的异常崩溃
     *
     *
     * 开始全屏播放
     *
     * 使用全屏播放功能时一定要在对应的Activity声明中添加配置：
     * android:configChanges="orientation|screenSize|keyboardHidden"
     */
    @Override
    public void onStartFullScreen() {

        mToggleFullScreen = true;
        setCurrentScreenState(ScreenState.SCREEN_STATE_FULLSCREEN);
        PlayerManager.getInstance().pause();

        ViewGroup windowContent = (ViewGroup) (Utils.getActivity(getContext())).findViewById(Window.ID_ANDROID_CONTENT);
        mVideoWidth = this.getWidth();
        mVideoHeight = this.getHeight();
        mOldParent = (ViewGroup)this.getParent();
        mOldIndex = mOldParent.indexOfChild(this);
        mOldParent.removeView(this);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        windowContent.addView(this, lp);

        initFullScreenGestureView();
        Utils.getActivity(getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        Utils.getActivity(getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        PlayerManager.getInstance().play();
        onToggleFullScreenLockState(false);
    }

    /**
     * 退出全屏播放
     */
    @Override
    public void onExitFullScreen() {

        if(!ScreenState.isFullScreen(getCurrentScreenState())) {
            return;
        }
        mToggleFullScreen = true;
        setCurrentScreenState(ScreenState.SCREEN_STATE_NORMAL);
        PlayerManager.getInstance().pause();

        ViewGroup windowContent = (ViewGroup) (Utils.getActivity(getContext())).findViewById(Window.ID_ANDROID_CONTENT);
        windowContent.removeView(this);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(mVideoWidth, mVideoHeight);
        mOldParent.addView(this, mOldIndex, lp);

        Utils.getActivity(getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        Utils.getActivity(getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mOldParent = null;
        mOldIndex = 0;
        if(mCurrentState != PlayState.STATE_AUTO_COMPLETE) {
            PlayerManager.getInstance().play();
        }
        onToggleFullScreenLockState(false);
    }

    /************************ 小窗口播放 ********************************/

    /**
     * 切换小窗口播放
     */
    protected void toggleSmallWindow() {

        if(mCurrentState == PlayState.STATE_NORMAL) {
            //不处于播放状态
            return;
        }
        if(PlayerManager.getInstance().hasViewPlaying() == false) {
            /**
             * 点击小窗口的关闭按钮停止播放后，视频原本的View再次attach window后依然会进入此代码块
             * 而在停止播放时，因视频原本的View已经detach window了，所以无法更新其播放状态为PlayState.STATE_NORMAL
             * 因此此种情况仍然会进入此代码块，所以此时将该View状态重置
             */
            resetViewState();
            //界面上没有视频在播放
            return;
        }
        //Screen处于正常状态
        if(ScreenState.isNormal(getCurrentScreenState())) {
            onStartSmallWindowPlay();
        } else {
            onExitSmallWindowPlay(false);
        }
    }

    /**
     * 开始小窗口播放
     */
    protected void onStartSmallWindowPlay() {

        mVideoControllerView.stopVideoProgressUpdate();
        setCurrentScreenState(ScreenState.SCREEN_STATE_SMALL_WINDOW);
        PlayerManager.getInstance().setTextureView(null);
        mVideoTextureViewContainer.removeAllViews();
        VideoPlayerView smallWindowView = new VideoPlayerView(getContext());
        smallWindowView.setId(R.id.vp_small_window_view_id);
        smallWindowView.mVideoControllerView.cloneState(mVideoControllerView);
        smallWindowView.mVideoHeaderView.setTitle(mVideoTitle);
        smallWindowView.mVideoUrl = mVideoUrl;
        smallWindowView.mViewHash = mViewHash;
        TextureView textureView = smallWindowView.createTextureView();
        smallWindowView.mVideoTextureViewContainer.addView(textureView);
        PlayerManager.getInstance().setTextureView(textureView);

        ViewGroup windowContent = (ViewGroup) (Utils.getActivity(getContext())).findViewById(Window.ID_ANDROID_CONTENT);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(mSmallWindowWidth, mSmallWindowHeight);
        lp.gravity = Gravity.RIGHT|Gravity.BOTTOM;
        windowContent.addView(smallWindowView, lp);

        /**
         * 先将小窗口播放视频的View添加到Window后再设置小窗口View的屏幕状态，
         * 否则会导致小窗口触发attach window时又立刻执行了toggleSmallWindow()从而又退出了小窗口播放
         *
         * 下面状态的设置必须在addView之后执行，否则会进入死循环
         */
        smallWindowView.mCurrentScreenState = getCurrentScreenState();
        smallWindowView.mCurrentState = mCurrentState;

        smallWindowView.onPlayStateChanged(mCurrentState);
    }

    /**
     * 退出小窗口播放
     *
     * @param forceStop 退出小窗口时是否强制停止播放
     */
    protected void onExitSmallWindowPlay(boolean forceStop) {

        if(ScreenState.isSmallWindow(getCurrentScreenState()) == false) {
            return;
        }

        ViewGroup windowContent = (ViewGroup) (Utils.getActivity(getContext())).findViewById(Window.ID_ANDROID_CONTENT);
        VideoPlayerView smallWindowView = (VideoPlayerView) windowContent.findViewById(R.id.vp_small_window_view_id);
        smallWindowView.mVideoControllerView.stopVideoProgressUpdate();
        setCurrentScreenState(ScreenState.SCREEN_STATE_NORMAL);
        PlayerManager.getInstance().setTextureView(null);
        smallWindowView.mVideoTextureViewContainer.removeAllViews();

        mVideoControllerView.cloneState(smallWindowView.mVideoControllerView);
        mVideoUrl = smallWindowView.mVideoUrl;
        mViewHash = smallWindowView.mViewHash;
        mCurrentState = smallWindowView.mCurrentState;

        if(forceStop) {
            PlayerManager.getInstance().stop();
            windowContent.removeView(smallWindowView);
        } else {
            windowContent.removeView(smallWindowView);

            TextureView textureView = createTextureView();
            mVideoTextureViewContainer.addView(textureView);
            PlayerManager.getInstance().setTextureView(textureView);

            onPlayStateChanged(mCurrentState);
        }
    }

    /************************ 音频焦点获取与释放 ********************************/

    @Override
    public void onAudioFocusChange(int focusChange) {

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if(PlayerManager.getInstance().getState() == PlayState.STATE_PAUSE) {
                    PlayerManager.getInstance().play();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                //长时间失去Focus
                PlayerManager.getInstance().stop();
                Utils.log("AudioManager.AUDIOFOCUS_LOSS");
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                //短时间失去Focus
                if (PlayerManager.getInstance().isPlaying()) {
                    PlayerManager.getInstance().pause();
                }
                Utils.log("AudioManager.AUDIOFOCUS_LOSS_TRANSIENT");
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                break;
        }
    }

    /**
     * 请求获取AudioFocus
     */
    private void requestAudioFocus() {

        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }

    /**
     * 释放AudioFocus
     */
    private void abandonAudioFocus() {

        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(this);
    }

    /**
     * 点击返回键时的处理
     * 此时如果全屏播放则会退出全屏
     *
     * 小窗口默认不做处理，如果需要处理小窗口，可以重载此方法实现
     * @param message
     */
    protected void onBackPressed(BackPressedMessage message) {

        if(ScreenState.isFullScreen(message.getScreenState())) {
            onExitFullScreen();
        }
    }
}
