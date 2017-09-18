package cn.ittiger.player;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import cn.ittiger.player.message.BackPressedMessage;
import cn.ittiger.player.message.DurationMessage;
import cn.ittiger.player.message.Message;
import cn.ittiger.player.message.UIStateMessage;
import cn.ittiger.player.state.GestureTouchState;
import cn.ittiger.player.state.PlayState;
import cn.ittiger.player.state.ScreenState;
import cn.ittiger.player.util.Utils;

import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 播放器视图，只起到展示视频的作用，视频播放的真正控制交由{@link PlayerManager}实现
 * @author: ylhu
 * @time: 17-9-8
 */
public class VideoPlayerView extends RelativeLayout implements
    View.OnClickListener,
    View.OnTouchListener,
    SeekBar.OnSeekBarChangeListener,
    AudioManager.OnAudioFocusChangeListener,
    Observer {
    /**
     * 视频显示媒介容器(TextureView的父容器)
     */
    protected FrameLayout mVideoTextureViewContainer;
    /**
     * 视频预览图
     */
    protected ImageView mVideoThumbView;
    /**
     * 底部显示播放进度的进度条
     */
    protected ProgressBar mBottomProgressBar;
    /**
     * 视频加载进度
     */
    protected ProgressBar mVideoLoadingBar;
    /**
     * 视频播放按钮
     */
    protected ImageView mVideoPlayView;
    /**
     * 视频加载失败的提示View
     */
    protected View mVideoErrorView;

    /**
     * 底部播放控制条
     */
    protected View mVideoControllerView;
    /**
     * 底部 视频当前播放时间
     */
    protected TextView mVideoPlayTimeView;
    /**
     * 底部 视频总时长
     */
    protected TextView mVideoTotalTimeView;
    /**
     * 底部 视频播放进度
     */
    protected SeekBar mVideoPlaySeekBar;
    /**
     * 底部 全屏播放按钮
     */
    protected ImageView mVideoFullScreenView;
    /**
     * 小窗口播放时的关闭按钮
     */
    protected ImageView mVideoSmallWindowBackView;
    /**
     * 视频顶部显示全屏播放返回按钮和视频标题的父容器
     */
    protected View mVideoHeaderViewContainer;
    /**
     * 全屏播放时的返回按钮
     */
    protected ImageView mVideoFullScreenBackView;
    /**
     * 视频标题
     */
    protected TextView mVideoTitleView;

    /**
     * 播放时底部控制条自动隐藏任务
     */
    protected DismissControllerViewTimerTask mDismissControllerViewTimerTask;
    /**
     * 播放时底部控制条自动隐藏触发器
     */
    protected Timer mDismissControllerViewTimer;
    /**
     * 播放时底部控制条自动隐藏间隔时间
     */
    protected int mAutoDismissTime = 2000;
    /**
     * 当前Observer（即：VideoPlayerView本身）对象的hashcode
     */
    private int mViewHash;
    /**
     * 视频标题
     */
    private CharSequence mVideoTitle;
    /**
     * 视频地址
     */
    private String mVideoUrl;
    /**
     * 视频时长，miliseconds
     */
    private int mDuration = 0;
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
     * 屏幕高度
     */
    private int mScreenHeight;
    /**
     * 小窗口的宽度
     */
    private int mSmallWindowWidth;
    /**
     * 小窗口的高度
     */
    private int mSmallWindowHeight;
    /**
     * 正常状态下的标题是否显示
     */
    private boolean mShowNormalStateTitleView = true;

    public VideoPlayerView(Context context) {

        super(context);
        initView(context);
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {

        super(context, attrs);
        initView(context);
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    protected int getPlayerLayoutId() {

        return R.layout.vp_layout_videoplayer;
    }

    private void initView(Context context) {

        mViewHash = this.toString().hashCode();
        mScreenWidth = Utils.getWindowWidth(context);
        mScreenHeight = Utils.getWindowHeight(context);
        mSmallWindowWidth = mScreenWidth / 2;
        mSmallWindowHeight = (int) (mSmallWindowWidth * 1.0f / 16 * 9 + 0.5f);

        inflate(context, getPlayerLayoutId(), this);
        //避免ListView中item点击无法响应的问题
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        setBackgroundColor(Color.BLACK);

        findAndBindView();
    }

    protected void findAndBindView() {

        mVideoTextureViewContainer = (FrameLayout) findViewById(R.id.vp_video_surface_container);
        mVideoThumbView = (ImageView) findViewById(R.id.vp_video_thumb);
        mBottomProgressBar = (ProgressBar) findViewById(R.id.vp_video_bottom_progress);
        mVideoLoadingBar = (ProgressBar) findViewById(R.id.vp_video_loading);
        mVideoPlayView = (ImageView) findViewById(R.id.vp_video_play);
        mVideoErrorView = findViewById(R.id.vp_video_play_error_view);
        mVideoControllerView = findViewById(R.id.vp_video_bottom_controller_view);
        mVideoPlayTimeView = (TextView) findViewById(R.id.vp_video_play_time);
        mVideoTotalTimeView = (TextView) findViewById(R.id.vp_video_total_time);
        mVideoPlaySeekBar = (SeekBar) findViewById(R.id.vp_video_seek_progress);
        mVideoFullScreenView = (ImageView) findViewById(R.id.vp_video_fullscreen);
        mVideoSmallWindowBackView = (ImageView) findViewById(R.id.vp_video_small_window_back);
        mVideoHeaderViewContainer = findViewById(R.id.vp_video_header_view);
        mVideoFullScreenBackView = (ImageView) findViewById(R.id.vp_video_fullScreen_back);
        mVideoTitleView = (TextView) findViewById(R.id.vp_video_title);
        mFullScreenViewStub = (ViewStub) findViewById(R.id.vp_fullscreen_view_stub);

        mVideoPlayView.setOnClickListener(this);
        mVideoThumbView.setOnClickListener(this);
        mVideoTextureViewContainer.setOnClickListener(this);
        mVideoTextureViewContainer.setOnTouchListener(this);
        mVideoErrorView.setOnClickListener(this);
        mVideoFullScreenView.setOnClickListener(this);
        mVideoPlaySeekBar.setOnTouchListener(this);
        mVideoErrorView.setOnClickListener(this);
        mVideoControllerView.setOnTouchListener(this);
        mVideoPlaySeekBar.setOnSeekBarChangeListener(this);
        mVideoSmallWindowBackView.setOnClickListener(this);
        mVideoFullScreenBackView.setOnClickListener(this);
    }

    private void resetViewState() {

        mCurrentState = PlayState.STATE_NORMAL;
        mCurrentScreenState = ScreenState.SCREEN_STATE_NORMAL;
        onPlayStateChanged(mCurrentState);
    }

    /**
     * 绑定数据
     * @param videoUrl
     */
    public void bind(String videoUrl, CharSequence title, boolean showNormalStateTitleView) {

        mShowNormalStateTitleView = showNormalStateTitleView;
        mVideoTitle = title;
        mVideoUrl = videoUrl;
        if(!TextUtils.isEmpty(mVideoTitle)) {
            mVideoTitleView.setText(mVideoTitle);
        }
        resetViewState();
    }

    /**
     * 绑定数据
     * @param videoUrl
     */
    public void bind(String videoUrl, CharSequence title) {

        bind(videoUrl, title, mShowNormalStateTitleView);
        resetViewState();
    }

    public void bind(String videoUrl) {

        bind(videoUrl, null);
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

        if (R.id.vp_video_play == id) {
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
        } else if(R.id.vp_video_thumb == id) {
            startPlayVideo();
        } else if(R.id.vp_video_fullscreen == id) {
            //全屏播放
            toggleFullScreen();
        } else if(R.id.vp_video_play_error_view == id) {
            startPlayVideo();
        } else if(R.id.vp_video_small_window_back == id) {
            //关闭小窗口播放，并停止播放当前视频
            exitSmallWindowPlay(true);
        } else if(R.id.vp_video_fullScreen_back == id) {
            exitFullScreen();
        }
    }

    /**
     * 开始播放视频
     */
    public void startPlayVideo() {

        if(!Utils.isConnected(getContext())) {
            if(!PlayerManager.getInstance().isCached(mVideoUrl)) {
                Toast.makeText(getContext(), R.string.vp_no_network, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        requestAudioFocus();
        //先移除播放器关联的TextureView
        PlayerManager.getInstance().removeTextureView();

        TextureView textureView = createTextureView();
        mVideoTextureViewContainer.addView(textureView);
        //准备开始播放
        PlayerManager.getInstance().start(mVideoUrl, mViewHash);
        PlayerManager.getInstance().setTextureView(textureView);
    }

    public TextureView createTextureView() {

        //重新为播放器关联TextureView
        TextureView textureView = newTextureView();
        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER);
        textureView.setLayoutParams(params);
        return textureView;
    }

    /**
     * 创建一个TextureView
     * 此处单独写成一个方法可以方便后续自定义扩展TextureView
     *
     * @return
     */
    protected TextureView newTextureView() {

        return new TextureView(getContext());
    }

    public ImageView getThumbImageView() {

        return mVideoThumbView;
    }

    /************************ 底部SeekBar监听 ********************************/

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if(fromUser) {
            int seekToTime = seekBar.getProgress() * mDuration / 100;
            PlayerManager.getInstance().seekTo(seekToTime);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
    }

    /************************ 使用观察者模式监听播放状态的变化 ********************************/



    @Override
    protected void onAttachedToWindow() {

        super.onAttachedToWindow();
        Utils.log("attached to window, view hash:" + mViewHash);
        PlayerManager.getInstance().addObserver(this);
        mToggleFullScreen = false;
        if(ScreenState.isSmallWindow(mCurrentScreenState)) {
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

                    onDurationChanged(((DurationMessage) arg).getDuration());
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
     * 点击返回键时的处理
     * 此时如果全屏播放则会退出全屏
     *
     * 小窗口默认不做处理，如果需要处理小窗口，可以重载此方法实现
     * @param message
     */
    protected void onBackPressed(BackPressedMessage message) {

        if(ScreenState.isFullScreen(message.getScreenState())) {
            exitFullScreen();
        }
    }

    /**
     * 播放状态发生改变时调用
     * @param state
     */
    private void onPlayStateChanged(int state) {

        mCurrentState = state;
        onChangeUIState(state);
        switch (state) {
            case PlayState.STATE_NORMAL:
                Utils.log("state change to: STATE_NORMAL");
                resetDuration();
                stopVideoProgressUpdate();
                abandonAudioFocus();
                break;
            case PlayState.STATE_LOADING:
                Utils.log("state change to: STATE_LOADING");
                break;
            case PlayState.STATE_PLAYING:
                Utils.log("state change to: STATE_PLAYING");
                startVideoProgressUpdate();
                break;
            case PlayState.STATE_PAUSE:
                Utils.log("state change to: STATE_PAUSE");
                stopVideoProgressUpdate();
                break;
            case PlayState.STATE_PLAYING_BUFFERING_START:
                Utils.log("state change to: STATE_PLAYING_BUFFERING_START");
                break;
            case PlayState.STATE_AUTO_COMPLETE:
                Utils.log("state change to: STATE_AUTO_COMPLETE");
                stopVideoProgressUpdate();
                exitFullScreen();
                exitSmallWindowPlay(true);
                break;
            case PlayState.STATE_ERROR:
                Utils.log("state change to: STATE_ERROR");
                resetDuration();
                stopVideoProgressUpdate();
                abandonAudioFocus();
                break;
            default:
                throw new IllegalStateException("Illegal Play State:" + state);
        }
    }

    /**
     * 重置视频时长
     */
    private void resetDuration() {

        mDuration = 0;
    }

    /************************ UI状态更新 ********************************/

    /**
     * 更新各个播放状态下的UI
     * @param state    播放状态消息
     */
    public void onChangeUIState(int state) {

        switch (state) {
            case PlayState.STATE_NORMAL:
                onChangeUINormalState();
                break;
            case PlayState.STATE_LOADING:
                onChangeUILoadingState();
                break;
            case PlayState.STATE_PLAYING:
                onChangeUIPlayingState();
                break;
            case PlayState.STATE_PAUSE:
                onChangeUIPauseState();
                break;
            case PlayState.STATE_PLAYING_BUFFERING_START:
                onChangeUISeekBufferingState();
                break;
            case PlayState.STATE_AUTO_COMPLETE:
                onChangeUICompleteState();
                break;
            case PlayState.STATE_ERROR:
                onChangeUIErrorState();
                break;
            default:
                throw new IllegalStateException("Illegal Play State:" + state);
        }
    }

    /**
     * 更新视频时长信息
     * */
    public void onDurationChanged(int duration) {

        mDuration = duration;
        String time = Utils.formatVideoTimeLength(duration);
        mVideoTotalTimeView.setText(time);
    }

    /**
     * 更改头部视图状态
     * @param showHeaderView    是否显示头部视图
     */
    private void onChangeVideoHeaderViewState(boolean showHeaderView) {

        if(showHeaderView == false) {
            Utils.hideViewIfNeed(mVideoHeaderViewContainer);
            return;
        }
        if(ScreenState.isFullScreen(mCurrentScreenState)) {
            Utils.showViewIfNeed(mVideoHeaderViewContainer);
            Utils.showViewIfNeed(mVideoFullScreenBackView);
        } else if(ScreenState.isNormal(mCurrentScreenState)) {
            if(mShowNormalStateTitleView) {
                Utils.showViewIfNeed(mVideoHeaderViewContainer);
                Utils.hideViewIfNeed(mVideoFullScreenBackView);
            } else {
                Utils.hideViewIfNeed(mVideoHeaderViewContainer);
            }
        } else {
            //小窗口播放隐藏视频头部视图
            Utils.hideViewIfNeed(mVideoHeaderViewContainer);
        }
    }

    /**
     * UI状态更新为Normal状态，即初始状态
     */
    public void onChangeUINormalState() {

        //显示视频预览图
        Utils.showViewIfNeed(mVideoThumbView);
        //隐藏加载loading
        Utils.hideViewIfNeed(mVideoLoadingBar);
        //显示播放按钮
        mVideoPlayView.setImageResource(R.drawable.vp_play_selector);
        Utils.showViewIfNeed(mVideoPlayView);
        //隐藏底部控制条
        Utils.hideViewIfNeed(mVideoControllerView);
        //隐藏底部播放进度
        Utils.hideViewIfNeed(mBottomProgressBar);
        //隐藏播放错误文案
        Utils.hideViewIfNeed(mVideoErrorView);
        if(ScreenState.isSmallWindow(mCurrentScreenState)) {
            //隐藏小窗口关闭按钮
            Utils.showViewIfNeed(mVideoSmallWindowBackView);
        } else if(ScreenState.isFullScreen(mCurrentScreenState)){
            //隐藏小窗口关闭按钮
            Utils.hideViewIfNeed(mVideoSmallWindowBackView);
        }
        onChangeVideoHeaderViewState(true);
    }

    /**
     * UI状态更新为Loading状态，即视频加载状态
     */
    public void onChangeUILoadingState() {

        //显示视频预览图
        Utils.hideViewIfNeed(mVideoThumbView);
        //显示加载loading
        Utils.showViewIfNeed(mVideoLoadingBar);
        //隐藏播放按钮
        Utils.hideViewIfNeed(mVideoPlayView);
        //隐藏底部控制条
        Utils.hideViewIfNeed(mVideoControllerView);
        //隐藏底部播放进度
        Utils.hideViewIfNeed(mBottomProgressBar);
        //隐藏播放错误文案
        Utils.hideViewIfNeed(mVideoErrorView);
        if(ScreenState.isSmallWindow(mCurrentScreenState)) {
            //隐藏小窗口关闭按钮
            Utils.showViewIfNeed(mVideoSmallWindowBackView);
        } else {
            //隐藏小窗口关闭按钮
            Utils.hideViewIfNeed(mVideoSmallWindowBackView);
        }
        onChangeVideoHeaderViewState(false);
    }

    /**
     * UI状态更新为Playing状态，即视频播放状态
     */
    public void onChangeUIPlayingState() {

        //隐藏视频预览图
        Utils.hideViewIfNeed(mVideoThumbView);
        //隐藏加载loading
        Utils.hideViewIfNeed(mVideoLoadingBar);
        //隐藏播放错误文案
        Utils.hideViewIfNeed(mVideoErrorView);
        if(ScreenState.isSmallWindow(mCurrentScreenState)) {
            //隐藏底部控制条
            Utils.hideViewIfNeed(mVideoControllerView);
            cancelDismissControllerViewTimer();
            //显示底部播放进度
            Utils.showViewIfNeed(mBottomProgressBar);
            //隐藏暂停按钮
            Utils.hideViewIfNeed(mVideoPlayView);
            //显示小窗口关闭按钮
            Utils.showViewIfNeed(mVideoSmallWindowBackView);
        } else {
            //显示底部控制条
            Utils.showViewIfNeed(mVideoControllerView);
            startDismissControllerViewTimer();
            //隐藏底部播放进度
            Utils.hideViewIfNeed(mBottomProgressBar);
            //显示暂停按钮
            mVideoPlayView.setImageResource(R.drawable.vp_pause_selector);
            Utils.showViewIfNeed(mVideoPlayView);
            //隐藏小窗口关闭按钮
            Utils.hideViewIfNeed(mVideoSmallWindowBackView);
        }
        onChangeVideoHeaderViewState(true);
    }

    /**
     * UI状态更新为SeekBuffer状态，即视拖动进度条
     */
    public void onChangeUISeekBufferingState() {

        //隐藏视频预览图
        Utils.hideViewIfNeed(mVideoThumbView);
        //隐藏加载loading
        Utils.showViewIfNeed(mVideoLoadingBar);
        //隐藏暂停按钮
        Utils.hideViewIfNeed(mVideoPlayView);
        //隐藏播放错误文案
        Utils.hideViewIfNeed(mVideoErrorView);
        if(ScreenState.isSmallWindow(mCurrentScreenState)) {
            //隐藏底部控制条
            Utils.hideViewIfNeed(mVideoControllerView);
            cancelDismissControllerViewTimer();
            //显示底部播放进度
            Utils.showViewIfNeed(mBottomProgressBar);
            //显示小窗口关闭按钮
            Utils.showViewIfNeed(mVideoSmallWindowBackView);
        } else {
            //显示底部控制条
            Utils.showViewIfNeed(mVideoControllerView);
            cancelDismissControllerViewTimer();
            //隐藏底部播放进度
            Utils.hideViewIfNeed(mBottomProgressBar);
            //隐藏小窗口关闭按钮
            Utils.hideViewIfNeed(mVideoSmallWindowBackView);
        }
        onChangeVideoHeaderViewState(false);
    }

    /**
     * UI状态更新为Pause状态，即视频暂停播放状态
     */
    public void onChangeUIPauseState() {

        //隐藏视频预览图
        Utils.hideViewIfNeed(mVideoThumbView);
        //隐藏加载loading
        Utils.hideViewIfNeed(mVideoLoadingBar);
        //显示底部控制条
        Utils.showViewIfNeed(mVideoControllerView);
        cancelDismissControllerViewTimer();
        //隐藏底部播放进度
        Utils.hideViewIfNeed(mBottomProgressBar);
        //隐藏播放错误文案
        Utils.hideViewIfNeed(mVideoErrorView);
        if(ScreenState.isSmallWindow(mCurrentScreenState)) {
            //显示小窗口关闭按钮
            Utils.showViewIfNeed(mVideoSmallWindowBackView);
            //隐藏播放按钮
            Utils.hideViewIfNeed(mVideoPlayView);
        } else {
            //显示播放按钮
            mVideoPlayView.setImageResource(R.drawable.vp_play_selector);
            Utils.showViewIfNeed(mVideoPlayView);
            //隐藏小窗口关闭按钮
            Utils.hideViewIfNeed(mVideoSmallWindowBackView);
        }
        onChangeVideoHeaderViewState(true);
    }

    /**
     * UI状态更新为Complete状态，即视频播放结束状态
     */
    public void onChangeUICompleteState() {

        //显示视频预览图
        Utils.showViewIfNeed(mVideoThumbView);
        //隐藏加载loading
        Utils.hideViewIfNeed(mVideoLoadingBar);
        //显示再次播放按钮
        mVideoPlayView.setImageResource(R.drawable.vp_replay_selector);
        Utils.showViewIfNeed(mVideoPlayView);
        //显示底部控制条
        Utils.hideViewIfNeed(mVideoControllerView);
        cancelDismissControllerViewTimer();
        //隐藏底部播放进度
        Utils.hideViewIfNeed(mBottomProgressBar);
        //隐藏播放错误文案
        Utils.hideViewIfNeed(mVideoErrorView);
        if(ScreenState.isSmallWindow(mCurrentScreenState)) {
            //显示小窗口关闭按钮
            Utils.showViewIfNeed(mVideoSmallWindowBackView);
        } else {
            //隐藏小窗口关闭按钮
            Utils.hideViewIfNeed(mVideoSmallWindowBackView);
        }
        updateProgress(mDuration);
        onChangeVideoHeaderViewState(true);
    }

    /**
     * UI状态更新为Error状态，即视频播放错误状态
     */
    public void onChangeUIErrorState() {

        //隐藏视频预览图
        Utils.hideViewIfNeed(mVideoThumbView);
        //隐藏加载loading
        Utils.hideViewIfNeed(mVideoLoadingBar);
        //隐藏播放按钮
        Utils.hideViewIfNeed(mVideoPlayView);
        //隐藏底部控制条
        Utils.hideViewIfNeed(mVideoControllerView);
        cancelDismissControllerViewTimer();
        //隐藏底部播放进度
        Utils.hideViewIfNeed(mBottomProgressBar);
        //显示播放错误文案
        Utils.showViewIfNeed(mVideoErrorView);
        if(ScreenState.isSmallWindow(mCurrentScreenState)) {
            //显示小窗口关闭按钮
            Utils.showViewIfNeed(mVideoSmallWindowBackView);
        } else {
            //隐藏小窗口关闭按钮
            Utils.hideViewIfNeed(mVideoSmallWindowBackView);
        }
        onChangeVideoHeaderViewState(false);
    }

    /**
     * 当触摸视频时更新相关UI状态
     */
    public void onChangeUIWhenTouchVideoView() {

        if(mCurrentState != PlayState.STATE_PLAYING) {
            return;
        }
        boolean isAllShown = Utils.isViewShown(mVideoPlayView) && Utils.isViewShown(mVideoControllerView);
        if(isAllShown) {
            hideFullScreenTouchStateView();
        } else {
            showFullScreenTouchStateView();
        }
    }

    private void hideFullScreenTouchStateView() {

        Utils.hideViewIfNeed(mVideoPlayView);
        Utils.hideViewIfNeed(mVideoControllerView);
        Utils.showViewIfNeed(mBottomProgressBar);
        onChangeVideoHeaderViewState(false);
        cancelDismissControllerViewTimer();
    }

    private void showFullScreenTouchStateView() {

        Utils.showViewIfNeed(mVideoPlayView);
        Utils.showViewIfNeed(mVideoControllerView);
        Utils.hideViewIfNeed(mBottomProgressBar);
        startDismissControllerViewTimer();
        onChangeVideoHeaderViewState(true);
    }

    /**
     * 开始计时，mAutoDismissTime时间后自动隐藏底部控制条和播放按钮
     */
    public void startDismissControllerViewTimer() {

        cancelDismissControllerViewTimer();
        mDismissControllerViewTimer = new Timer();
        mDismissControllerViewTimerTask = new DismissControllerViewTimerTask();
        mDismissControllerViewTimer.schedule(mDismissControllerViewTimerTask, mAutoDismissTime);
    }

    /**
     * 取消自动隐藏底部控制条任务
     */
    public void cancelDismissControllerViewTimer() {

        if (mDismissControllerViewTimer != null) {
            mDismissControllerViewTimer.cancel();
        }
        if (mDismissControllerViewTimerTask != null) {
            mDismissControllerViewTimerTask.cancel();
        }

    }

    /**
     * 播放时，控制条3秒自动隐藏任务
     */
    public class DismissControllerViewTimerTask extends TimerTask {

        @Override
        public void run() {

            int state = mCurrentState;
            if (state != PlayState.STATE_NORMAL
                    && state != PlayState.STATE_ERROR
                    && state != PlayState.STATE_AUTO_COMPLETE) {
                if (getContext() != null && getContext() instanceof Activity) {
                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            hideFullScreenTouchStateView();
                        }
                    });
                }
            }
        }
    }

    /************************ 更新播放进度 ********************************/

    private static final int PROGRESS_UPDATE_INTERNAL = 300;
    private static final int PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
    private final ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mScheduleFuture;

    /**
     * 开始更新播放进度
     */
    private void startVideoProgressUpdate() {

        stopVideoProgressUpdate();
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {

                            post(mUpdateProgressTask);
                        }
                    }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 停止更新播放进度
     */
    private void stopVideoProgressUpdate() {

        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }

    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {

            int position = PlayerManager.getInstance().getCurrentPosition();
            updateProgress(position);
        }
    };

    private void updateProgress(int position) {

        int progress = position * 100 / (mDuration == 0 ? 1 : mDuration);
        mVideoPlayTimeView.setText(Utils.formatVideoTimeLength(position));
        mVideoPlaySeekBar.setProgress(progress);
        mBottomProgressBar.setProgress(progress);
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
     */
    public void toggleFullScreen() {

        if(ScreenState.isFullScreen(mCurrentScreenState)) {
            exitFullScreen();
        } else if(ScreenState.isNormal(mCurrentScreenState)){
            startFullScreen();
        } else {
            throw new IllegalStateException("the screen state is error, state=" + mCurrentScreenState);
        }
    }

    /**
     * 开始全屏播放
     *
     * 使用全屏播放功能时一定要在对应的Activity声明中添加配置：
     * android:configChanges="orientation|screenSize|keyboardHidden"
     */
    public void startFullScreen() {

        mToggleFullScreen = true;
        PlayerManager.getInstance().setScreenState(mCurrentScreenState = ScreenState.SCREEN_STATE_FULLSCREEN);
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

        viewStubFullScreenGestureView();
        Utils.getActivity(getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        Utils.getActivity(getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mVideoFullScreenView.setImageResource(R.drawable.vp_ic_minimize);
        PlayerManager.getInstance().play();
    }

    /**
     * 退出全屏播放
     */
    public void exitFullScreen() {

        if(!ScreenState.isFullScreen(mCurrentScreenState)) {
            return;
        }
        mToggleFullScreen = true;
        PlayerManager.getInstance().setScreenState(mCurrentScreenState = ScreenState.SCREEN_STATE_NORMAL);
        PlayerManager.getInstance().pause();

        ViewGroup windowContent = (ViewGroup) (Utils.getActivity(getContext())).findViewById(Window.ID_ANDROID_CONTENT);
        windowContent.removeView(this);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(mVideoWidth, mVideoHeight);
        mOldParent.addView(this, mOldIndex, lp);

        Utils.getActivity(getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        Utils.getActivity(getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mVideoFullScreenView.setImageResource(R.drawable.vp_ic_fullscreen);
        mOldParent = null;
        mOldIndex = 0;
        if(mCurrentState != PlayState.STATE_AUTO_COMPLETE) {
            PlayerManager.getInstance().play();
        }
    }

    /************************ 小窗口播放 ********************************/

    /**
     * 切换小窗口播放
     */
    public void toggleSmallWindow() {

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
        if(ScreenState.isNormal(mCurrentScreenState)) {
            startSmallWindowPlay();
        } else {
            exitSmallWindowPlay(false);
        }
    }

    /**
     * 开始小窗口播放
     */
    public void startSmallWindowPlay() {

        stopVideoProgressUpdate();
        PlayerManager.getInstance().setScreenState(mCurrentScreenState = ScreenState.SCREEN_STATE_SMALL_WINDOW);
        VideoPlayerView videoPlayerView = new VideoPlayerView(getContext());
        videoPlayerView.setId(R.id.vp_small_window_view_id);
        videoPlayerView.mDuration = mDuration;
        videoPlayerView.mVideoUrl = mVideoUrl;
        videoPlayerView.mViewHash = mViewHash;
        videoPlayerView.mShowNormalStateTitleView = mShowNormalStateTitleView;
        TextureView textureView = videoPlayerView.createTextureView();
        videoPlayerView.mVideoTextureViewContainer.addView(textureView);
        PlayerManager.getInstance().setTextureView(textureView);

        ViewGroup windowContent = (ViewGroup) (Utils.getActivity(getContext())).findViewById(Window.ID_ANDROID_CONTENT);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(mSmallWindowWidth, mSmallWindowHeight);
        lp.gravity = Gravity.RIGHT|Gravity.BOTTOM;
        windowContent.addView(videoPlayerView, lp);

        /**
         * 先将小窗口播放视频的View添加到Window后再设置小窗口View的屏幕状态，
         * 否则会导致小窗口触发attach window时又立刻执行了toggleSmallWindow()从而又退出了小窗口播放
         *
         * 下面状态的设置必须在addView之后执行，否则会进入死循环
         */
        videoPlayerView.mCurrentScreenState = mCurrentScreenState;
        videoPlayerView.mCurrentState = mCurrentState;

        videoPlayerView.onPlayStateChanged(mCurrentState);
    }

    /**
     * 退出小窗口播放
     *
     * @param forceStop 退出小窗口时是否强制停止播放
     */
    public void exitSmallWindowPlay(boolean forceStop) {

        if(ScreenState.isSmallWindow(mCurrentScreenState) == false) {
            return;
        }

        ViewGroup windowContent = (ViewGroup) (Utils.getActivity(getContext())).findViewById(Window.ID_ANDROID_CONTENT);
        VideoPlayerView smallWindowView = (VideoPlayerView) windowContent.findViewById(R.id.vp_small_window_view_id);
        smallWindowView.stopVideoProgressUpdate();
        PlayerManager.getInstance().setScreenState(mCurrentScreenState = ScreenState.SCREEN_STATE_NORMAL);
        PlayerManager.getInstance().setTextureView(null);
        smallWindowView.mVideoTextureViewContainer.removeAllViews();

        mDuration = smallWindowView.mDuration;
        mVideoUrl = smallWindowView.mVideoUrl;
        mViewHash = smallWindowView.mViewHash;
        mCurrentState = smallWindowView.mCurrentState;
        mShowNormalStateTitleView = smallWindowView.mShowNormalStateTitleView;

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

    /************************ 手势操作逻辑处理 ********************************/

    private boolean mIsTouchControllerView = false;

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int id = v.getId();
        if(ScreenState.isSmallWindow(mCurrentScreenState)) {
            //小窗口播放时不响应手势操作
            return false;
        }
        onTouchToVideoView(event);
        if(R.id.vp_video_surface_container == id) {
        } else if(R.id.vp_video_bottom_controller_view == id) {
//            onTouchToControllerView(event);
        }

        return false;
    }

    /**
     * 底部Controller View Touch后的逻辑处理
     *
     * @param event
     */
    public void onTouchToControllerView(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsTouchControllerView = true;
                cancelDismissControllerViewTimer();
                break;
            case MotionEvent.ACTION_UP:
                //Touch到Controller View时该View不会触发ActionUp事件，只触发VideoView的ActionUp事件
                break;
        }
    }

    /**
     * TextureView Touch后的逻辑处理
     *
     * 主要处理逻辑为：
     * 1.触摸该View时显示或因此底部控制条
     * 2.控制音量，亮度，快进和后退
     * @param event
     */
    public void onTouchToVideoView(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                cancelDismissControllerViewTimer();
                break;
            case MotionEvent.ACTION_UP:
                if(mIsTouchControllerView) {
                    startDismissControllerViewTimer();
                } else {
                    onChangeUIWhenTouchVideoView();
                }
                mIsTouchControllerView = false;
                break;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchDownX = event.getRawX();
                mTouchDownY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if(mCurrentState != PlayState.STATE_PLAYING && mCurrentState != PlayState.STATE_PAUSE) {
                    break;
                }
                float xDis = Math.abs(mTouchDownX - event.getRawX());
                float yDis = Math.abs(event.getRawY() - mTouchDownY);
                Utils.logTouch("TouchSlop:" + mTouchSlop + ", xDis:" + xDis + ", yDis:" + yDis);
                if(isFlingLeft(mTouchDownX, mTouchDownY, event)) {//向左滑，退后
                    hideFullScreenTouchStateView();
                    Utils.logTouch("Fling Left");
                    videoSeek(false);
                    mTouchDownX = event.getRawX();
                    mTouchDownY = event.getRawY();
                } else if(isFlingRight(mTouchDownX, mTouchDownY, event)) {//向右滑，快进
                    hideFullScreenTouchStateView();
                    Utils.logTouch("Fling Right");
                    videoSeek(true);
                    mTouchDownX = event.getRawX();
                    mTouchDownY = event.getRawY();
                } else if(isScrollVertical(mTouchDownX, mTouchDownY, event)) {//垂直方向滑
                    hideFullScreenTouchStateView();
                    if(isScrollVerticalRight(mTouchDownX, event)) {//屏幕右边上下滑
                        Utils.logTouch("isScrollVerticalRight");
                        if(Math.abs(event.getRawY() - mTouchDownY) >= mVolumeDistance) {
                            changeVideoVolume(event.getRawY() < mTouchDownY);
                            mTouchDownX = event.getRawX();
                            mTouchDownY = event.getRawY();
                        }
                    } else if(isScrollVerticalLeft(mTouchDownX, event)) {//屏幕左边上下滑
                        Utils.logTouch("isScrollVerticalLeft");
                        if(Math.abs(event.getRawY() - mTouchDownY) >= mBrightnessDistance) {
                            changeBrightness(event.getRawY() < mTouchDownY);
                            mTouchDownX = event.getRawX();
                            mTouchDownY = event.getRawY();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                switch (mCurrentGestureState) {
                    case GestureTouchState.STATE_VIDEO_PROGRESS:
                        if(mGestureSeekToPosition != -1) {
                            PlayerManager.getInstance().seekTo(mGestureSeekToPosition);
                            mGestureSeekToPosition = -1;
                            Utils.hideViewIfNeed(mVideoChangeProgressView);
                            showFullScreenTouchStateView();
                        }
                        break;
                    case GestureTouchState.STATE_VOLUME:
                        Utils.hideViewIfNeed(mVideoVolumeView);
                        break;
                    case GestureTouchState.STATE_BRIGHTNESS:
                        Utils.hideViewIfNeed(mVideoBrightnessView);
                        break;
                }
                break;
        }
    }

    /**
     * 全屏播放时手势调节音量，亮度，快进后退时的视图
     */
    protected ViewStub mFullScreenViewStub;
    /**
     * 全屏手势调节 音量视图
     */
    protected LinearLayout mVideoVolumeView;
    /**
     * 全屏手势调节 音量进度条
     */
    protected ProgressBar mVideoVolumeProgress;
    /**
     * 全屏手势调节 亮度视图
     */
    protected LinearLayout mVideoBrightnessView;
    /**
     * 全屏手势调节 亮度进度条
     */
    protected ProgressBar mVideoBrightnessProgress;
    /**
     * 全屏手势调节 快进后退视图
     */
    protected View mVideoChangeProgressView;
    /**
     * 全屏手势调节 快进后退图标
     */
    protected ImageView mVideoChangeProgressIcon;
    /**
     * 全屏手势调节 快进后退当前播放进度
     */
    protected TextView mVideoChangeProgressCurrPro;
    /**
     * 全屏手势调节 快进后退 视频长度
     */
    protected TextView mVideoChangeProgressTotal;
    /**
     * 全屏手势调节 快进后退进度条
     */
    protected ProgressBar mVideoChangeProgressBar;
    /**
     * 手势触摸点坐标
     */
    private float mTouchDownX, mTouchDownY;
    private static final int TOTAL_PERCENT = 100;
    /**
     * 每次快进后退的大小
     */
    private static final int VIDEO_SEEK_STEP = 2000;
    /**
     * 音量每次改变的大小
     */
    private static final int VOLUME_STEP = 1;
    /**
     * 亮度每次改变的大小
     */
    private static final float BRIGHTNESS_STEP = 0.08f;
    /**
     * 最大亮度值
     */
    private static final float MAX_BRIGHTNESS = 1.0f;
    /**
     * 看作为滑动操作的阀值
     */
    private int mTouchSlop = 0;
    /**
     * 最大音量
     */
    private int mMaxVolume;
    /**
     * 调节音量滑动的距离阀值
     */
    private float mVolumeDistance = 0;
    /**
     * 调节亮度滑动的距离阀值
     */
    private float mBrightnessDistance = 0;
    /**
     * 手势操作状态
     */
    private int mCurrentGestureState = GestureTouchState.STATE_NONE;
    /**
     * 手势操作时，最终要seek to的位置
     */
    private int mGestureSeekToPosition = -1;
    private AudioManager mAudioManager;

    /**
     * 初始化全屏播放时手势操作相关参数信息
     */
    private void initFullScreenGestureParams() {

        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolumeDistance = mScreenHeight / 3.0f / mMaxVolume;
        mBrightnessDistance = mScreenHeight / 3.0f / (MAX_BRIGHTNESS / BRIGHTNESS_STEP);

        int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mVideoVolumeProgress.setProgress((int)(volume * 1.0 / mMaxVolume * TOTAL_PERCENT + 0.5f));

        //获取当前屏幕亮度,获取失败则返回255
        int currLight = android.provider.Settings.System.getInt(getContext().getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS,
                255);
        float screenLight = currLight / 255f;

        WindowManager.LayoutParams window = ((Activity)getContext()).getWindow().getAttributes();
        window.screenBrightness = screenLight;
        mVideoBrightnessProgress.setProgress((int)(screenLight * TOTAL_PERCENT));
    }

    /**
     * 加载并初始化全屏播放时手势操作相关视图
     */
    protected void viewStubFullScreenGestureView() {

        if(mFullScreenViewStub == null) {
            return;
        }
        mFullScreenViewStub.setVisibility(VISIBLE);
        mVideoVolumeView = (LinearLayout) findViewById(R.id.vp_video_volume);
        mVideoVolumeProgress = (ProgressBar) findViewById(R.id.vp_video_volume_progressbar);
        mVideoBrightnessView = (LinearLayout) findViewById(R.id.vp_video_brightness);
        mVideoBrightnessProgress = (ProgressBar) findViewById(R.id.vp_video_brightness_progressbar);
        mVideoChangeProgressView = findViewById(R.id.vp_video_change_progress_view);
        mVideoChangeProgressIcon = (ImageView) findViewById(R.id.vp_video_change_progress_icon);
        mVideoChangeProgressCurrPro = (TextView) findViewById(R.id.vp_video_change_progress_current);
        mVideoChangeProgressTotal = (TextView) findViewById(R.id.vp_video_change_progress_total);
        mVideoChangeProgressBar = (ProgressBar) findViewById(R.id.vp_video_change_progress_bar);
        initFullScreenGestureParams();
    }

    /**
     * 调整视频音量大小
     *
     * @param isTurnUp    是否调大音量
     */
    private void changeVideoVolume(boolean isTurnUp) {

        mCurrentGestureState = GestureTouchState.STATE_VOLUME;
        Utils.showViewIfNeed(mVideoVolumeView);
        int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if(isTurnUp) {
            volume = volume + VOLUME_STEP >= mMaxVolume ? mMaxVolume : volume + VOLUME_STEP;
        } else {
            volume = volume - VOLUME_STEP > 0 ? volume - VOLUME_STEP : 0;
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        mVideoVolumeProgress.setProgress((int)(volume * 1.0 / mMaxVolume * TOTAL_PERCENT + 0.5f));
    }

    /**
     * 调整屏幕亮度
     *
     * @param isDodge  是否调亮
     */
    private void changeBrightness(boolean isDodge) {

        mCurrentGestureState = GestureTouchState.STATE_BRIGHTNESS;
        Utils.showViewIfNeed(mVideoBrightnessView);
        WindowManager.LayoutParams mWindowAttr = ((Activity)getContext()).getWindow().getAttributes();
        float brightness = mWindowAttr.screenBrightness;
        if(isDodge) {
            brightness = brightness < MAX_BRIGHTNESS ? brightness + BRIGHTNESS_STEP : MAX_BRIGHTNESS;
        } else {
            brightness = brightness > 0f ? brightness - BRIGHTNESS_STEP : 0f;
        }
        //只会改变当前屏幕的亮度
        mWindowAttr.screenBrightness = brightness;
        ((Activity)getContext()).getWindow().setAttributes(mWindowAttr);
        mVideoBrightnessProgress.setProgress((int)(brightness * TOTAL_PERCENT));
    }

    /**
     * 视频前进后退
     * @param isForward
     */
    private void videoSeek(boolean isForward) {

        mCurrentGestureState = GestureTouchState.STATE_VIDEO_PROGRESS;
        Utils.showViewIfNeed(mVideoChangeProgressView);
        int step = VIDEO_SEEK_STEP;//每次前进后退1秒
        if(mGestureSeekToPosition == -1) {
            mGestureSeekToPosition = PlayerManager.getInstance().getCurrentPosition();//当前播放时长
        }
        if(isForward) {//前进
            mVideoChangeProgressIcon.setImageResource(R.drawable.vp_ic_fast_forward);
            mGestureSeekToPosition = mGestureSeekToPosition + step >= mDuration ? mDuration : mGestureSeekToPosition + step;
        } else {
            mVideoChangeProgressIcon.setImageResource(R.drawable.vp_ic_fast_back);
            mGestureSeekToPosition = mGestureSeekToPosition - step <= 0 ? 0 : mGestureSeekToPosition - step;
        }
        mVideoChangeProgressCurrPro.setText(Utils.formatVideoTimeLength(mGestureSeekToPosition));
        mVideoChangeProgressTotal.setText("/" + Utils.formatVideoTimeLength(mDuration));
        mVideoChangeProgressBar.setProgress((int) (mGestureSeekToPosition * 1.0f / mDuration * TOTAL_PERCENT + 0.5f));
    }

    private boolean isFlingRight(float downX, float downY, MotionEvent e2) {

        return (e2.getRawX() - downX > mTouchSlop)
                && (Math.abs(e2.getRawY() - downY) < mTouchSlop);
    }

    private boolean isFlingLeft(float downX, float downY, MotionEvent e2) {

        return (downX - e2.getRawX() > mTouchSlop)
                && (Math.abs(e2.getRawY() - downY) < mTouchSlop);
    }

    private boolean isScrollVertical(float downX, float downY, MotionEvent e2) {

        return (Math.abs(e2.getRawX() - downX) < mTouchSlop)
                && (Math.abs(e2.getRawY() - downY) > mTouchSlop);
    }

    private boolean isScrollVerticalRight(float downX, MotionEvent e2) {

        return downX > mScreenWidth / 2 && e2.getRawX() > mScreenWidth / 2;
    }

    private boolean isScrollVerticalLeft(float downX, MotionEvent e2) {

        return downX < mScreenWidth / 2 && e2.getRawX() < mScreenWidth / 2;
    }
}
