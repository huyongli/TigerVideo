package cn.ittiger.player;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import cn.ittiger.player.listener.FullScreenGestureStateListener;
import cn.ittiger.player.listener.FullScreenToggleListener;
import cn.ittiger.player.listener.VideoTouchListener;
import cn.ittiger.player.message.BackPressedMessage;
import cn.ittiger.player.message.DurationMessage;
import cn.ittiger.player.message.Message;
import cn.ittiger.player.message.UIStateMessage;
import cn.ittiger.player.state.PlayState;
import cn.ittiger.player.state.ScreenState;
import cn.ittiger.player.ui.FullScreenGestureView;
import cn.ittiger.player.util.Utils;
import cn.ittiger.player.util.ViewIndex;

import java.lang.reflect.Constructor;
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
    VideoTouchListener,
    FullScreenGestureStateListener,
    FullScreenToggleListener,
    AudioManager.OnAudioFocusChangeListener,
    Observer {

    /**
     * 视频显示媒介容器(TextureView的父容器)
     */
    protected FrameLayout mVideoTextureViewContainer;

    /**
     * 底部播放控制条
     */
    protected VideoControllerView mVideoControllerView;
    /**
     * 自定义底部播放控制台视图layout id
     * 必须继承{@link VideoControllerView}
     */
    private int mVideoControllerViewRes = -1;

    /**
     * 视频预览图
     */
    protected ImageView mVideoThumbView;
    /**
     * 自定义视频预览图视图的layout id
     * 必须是{@link ImageView} 子类
     */
    private int mVideoThumbViewRes = -1;

    /**
     * 全屏播放时的自定义手势操作视图layout id
     * 自定义时必须继承{@link FullScreenGestureView}
     */
    private int mFullScreenGestureViewRes = -1;
    /**
     * 全屏播放时的手势操作视图
     */
    private FullScreenGestureView mFullScreenGestureView;


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
     * 全屏播放时锁屏按钮
     */
    protected ImageView mVideoFullScreenLockView;


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
     * 当前播放状态
     */
    private int mCurrentState = PlayState.STATE_NORMAL;
    /**
     * 当前屏幕播放状态
     */
    protected int mCurrentScreenState = ScreenState.SCREEN_STATE_NORMAL;
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
    /**
     * 全屏播放时，是否已经锁屏不允许操作
     */
    protected boolean mFullScreenLocked = false;

    public VideoPlayerView(Context context) {

        this(context, null);
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {

        super(context, attrs);
        initView(context, attrs);
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs);
    }

    protected int getPlayerLayoutId() {

        return R.layout.vp_layout_videoplayer;
    }

    private void initView(Context context, AttributeSet attrs) {

        if(attrs != null) {
            initCustomViewAttributes(context, attrs);
        }

        mViewHash = this.toString().hashCode();
        mScreenWidth = Utils.getWindowWidth(context);
        mScreenHeight = Utils.getWindowHeight(context);
        mSmallWindowWidth = mScreenWidth / 2;
        mSmallWindowHeight = (int) (mSmallWindowWidth * 1.0f / 16 * 9 + 0.5f);

        inflate(context, getPlayerLayoutId(), this);
        //避免ListView中item点击无法响应的问题
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        setBackgroundColor(Color.BLACK);

        initVideoThumbView();
        initVideoControllerView();

        findAndBindView();
    }

    protected void findAndBindView() {

        mVideoTextureViewContainer = (FrameLayout) findViewById(R.id.vp_video_surface_container);
        mVideoLoadingBar = (ProgressBar) findViewById(R.id.vp_video_loading);
        mVideoPlayView = (ImageView) findViewById(R.id.vp_video_play);
        mVideoErrorView = findViewById(R.id.vp_video_play_error_view);
        mVideoSmallWindowBackView = (ImageView) findViewById(R.id.vp_video_small_window_back);
        mVideoHeaderViewContainer = findViewById(R.id.vp_video_header_view);
        mVideoFullScreenBackView = (ImageView) findViewById(R.id.vp_video_fullScreen_back);
        mVideoTitleView = (TextView) findViewById(R.id.vp_video_title);
        mVideoFullScreenLockView = (ImageView) findViewById(R.id.vp_fullscreen_lock);

        mVideoPlayView.setOnClickListener(this);
        mVideoThumbView.setOnClickListener(this);
        mVideoTextureViewContainer.setOnClickListener(this);
        mVideoTextureViewContainer.setOnTouchListener(this);
        mVideoErrorView.setOnClickListener(this);
        mVideoErrorView.setOnClickListener(this);
        mVideoControllerView.setOnTouchListener(this);
        mVideoSmallWindowBackView.setOnClickListener(this);
        mVideoFullScreenBackView.setOnClickListener(this);
        mVideoFullScreenLockView.setOnClickListener(this);
    }

    /**
     * 初始化自定义视图的相关参数信息
     * @param context
     * @param attributeSet
     */
    private void initCustomViewAttributes(Context context, AttributeSet attributeSet) {

        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.VideoPlayerView, 0, 0);

        mFullScreenGestureViewRes = attr.getResourceId(R.styleable.VideoPlayerView_vpFullScreenGestureViewLayoutRes, -1);
        mVideoThumbViewRes = attr.getResourceId(R.styleable.VideoPlayerView_vpVideoThumbViewLayoutRes, -1);
        mVideoControllerViewRes = attr.getResourceId(R.styleable.VideoPlayerView_vpVideoControllerViewLayoutRes, -1);

        attr.recycle();
    }

    /**
     * 初始化视频预览图视图
     */
    protected void initVideoThumbView() {

        if(mVideoThumbView != null) {
            return;
        }
        if(mVideoThumbViewRes == -1) {
            mVideoThumbView = new ImageView(getContext());
            mVideoThumbView.setBackgroundResource(android.R.color.black);
            mVideoThumbView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            mVideoThumbView = (ImageView) inflate(getContext(), mVideoThumbViewRes, null);
        }
        RelativeLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mVideoThumbView, ViewIndex.VIDEO_THUMB_VIEW_INDEX, params);
    }

    /**
     * 初始化底部控制台View
     */
    protected void initVideoControllerView() {

        if(mVideoControllerView != null) {
            return;
        }
        if(mVideoThumbViewRes == -1) {
            mVideoControllerView = new VideoControllerView(getContext());
        } else {
            mVideoControllerView = (VideoControllerView) inflate(getContext(), mVideoControllerViewRes, null);
        }
        mVideoControllerView.setFullScreenToggleListener(this);
        RelativeLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.addView(mVideoControllerView, ViewIndex.VIDEO_CONTROLLER_VIEW_INDEX, params);
    }

    /**
     * 初始化全屏播放手势操作视图
     */
    protected void initFullScreenGestureView() {

        if(mFullScreenGestureView != null) {
            return;
        }
        if(mFullScreenGestureViewRes != -1) {
            mFullScreenGestureView = (FullScreenGestureView) inflate(getContext(), mFullScreenGestureViewRes, null);
        } else {
            mFullScreenGestureView = new FullScreenGestureView(getContext());
        }
        RelativeLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mFullScreenGestureView, ViewIndex.FULLSCREEN_GESTURE_VIEW_INDEX, params);
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

        if(v == mVideoThumbView) {
            startPlayVideo();
        } else if (R.id.vp_video_play == id) {
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
        } else if(R.id.vp_video_play_error_view == id) {
            startPlayVideo();
        } else if(R.id.vp_video_small_window_back == id) {
            //关闭小窗口播放，并停止播放当前视频
            exitSmallWindowPlay(true);
        } else if(R.id.vp_video_fullScreen_back == id) {
            onFullScreenHeaderBackClick(v);
        } else if(R.id.vp_fullscreen_lock == id) {
            onToggleFullScreenLockState(!mFullScreenLocked);
        }
    }

    /**
     * 全屏播放时，头部的返回按钮点击事件
     * @param view
     */
    protected void onFullScreenHeaderBackClick(View view) {

        exitFullScreen();
    }

    /**
     * 全屏播放时，锁屏按钮点击事件
     */
    protected void onToggleFullScreenLockState(boolean locked) {

        mFullScreenLocked = locked;
        if(mFullScreenLocked) {
            mVideoFullScreenLockView.setImageResource(R.drawable.vp_ic_fullscreen_lock);
            hideAllPlayStateViewExcludeLockView();
            startDismissControllerViewTimer();
        } else {
            mVideoFullScreenLockView.setImageResource(R.drawable.vp_ic_fullscreen_unlocked);
            showAllPlayStateView();
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
    protected void onPlayStateChanged(int state) {

        mCurrentState = state;
        onChangeUIState(state);
        switch (state) {
            case PlayState.STATE_NORMAL:
                Utils.log("state change to: STATE_NORMAL");
                mVideoControllerView.onVideoDurationChanged(0);
                mVideoControllerView.stopVideoProgressUpdate();
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
                exitFullScreen();
                exitSmallWindowPlay(true);
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
     * 更改头部视图状态
     * @param showHeaderView    是否显示头部视图
     */
    protected void onChangeVideoHeaderViewState(boolean showHeaderView) {

        if(showHeaderView == false) {
            Utils.hideViewIfNeed(mVideoHeaderViewContainer);
            return;
        }
        if(ScreenState.isFullScreen(mCurrentScreenState)) {
            Utils.showViewIfNeed(mVideoHeaderViewContainer);
            Utils.showViewIfNeed(mVideoFullScreenBackView);
            showFullScreenLockView();
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
    protected void onChangeUINormalState() {

        //显示视频预览图
        Utils.showViewIfNeed(mVideoThumbView);
        //隐藏加载loading
        Utils.hideViewIfNeed(mVideoLoadingBar);
        //显示播放按钮
        mVideoPlayView.setImageResource(R.drawable.vp_play_selector);
        Utils.showViewIfNeed(mVideoPlayView);
        //隐藏播放错误文案
        Utils.hideViewIfNeed(mVideoErrorView);
        if(ScreenState.isSmallWindow(mCurrentScreenState)) {
            //隐藏小窗口关闭按钮
            Utils.showViewIfNeed(mVideoSmallWindowBackView);
        } else if(ScreenState.isFullScreen(mCurrentScreenState)){
            //隐藏小窗口关闭按钮
            Utils.hideViewIfNeed(mVideoSmallWindowBackView);
        }
        mVideoControllerView.onChangeUINormalState(mCurrentScreenState);
        onChangeVideoHeaderViewState(true);
    }

    /**
     * UI状态更新为Loading状态，即视频加载状态
     */
    protected void onChangeUILoadingState() {

        //显示视频预览图
        Utils.hideViewIfNeed(mVideoThumbView);
        //显示加载loading
        Utils.showViewIfNeed(mVideoLoadingBar);
        //隐藏播放按钮
        Utils.hideViewIfNeed(mVideoPlayView);
        //隐藏播放错误文案
        Utils.hideViewIfNeed(mVideoErrorView);
        if(ScreenState.isSmallWindow(mCurrentScreenState)) {
            //隐藏小窗口关闭按钮
            Utils.showViewIfNeed(mVideoSmallWindowBackView);
        } else {
            //隐藏小窗口关闭按钮
            Utils.hideViewIfNeed(mVideoSmallWindowBackView);
        }
        mVideoControllerView.onChangeUILoadingState(mCurrentScreenState);
        onChangeVideoHeaderViewState(false);
    }

    /**
     * UI状态更新为Playing状态，即视频播放状态
     */
    protected void onChangeUIPlayingState() {


        //隐藏视频预览图
        Utils.hideViewIfNeed(mVideoThumbView);
        //隐藏加载loading
        Utils.hideViewIfNeed(mVideoLoadingBar);
        //隐藏播放错误文案
        Utils.hideViewIfNeed(mVideoErrorView);
        if(ScreenState.isSmallWindow(mCurrentScreenState)) {
            cancelDismissControllerViewTimer();
            //隐藏暂停按钮
            Utils.hideViewIfNeed(mVideoPlayView);
            //显示小窗口关闭按钮
            Utils.showViewIfNeed(mVideoSmallWindowBackView);
        } else {
            startDismissControllerViewTimer();
            //显示暂停按钮
            mVideoPlayView.setImageResource(R.drawable.vp_pause_selector);
            Utils.showViewIfNeed(mVideoPlayView);
            //隐藏小窗口关闭按钮
            Utils.hideViewIfNeed(mVideoSmallWindowBackView);
        }
        mVideoControllerView.onChangeUIPlayingState(mCurrentScreenState);
        onChangeVideoHeaderViewState(true);
    }

    /**
     * UI状态更新为SeekBuffer状态，即视拖动进度条
     */
    protected void onChangeUISeekBufferingState() {

        //隐藏视频预览图
        Utils.hideViewIfNeed(mVideoThumbView);
        //隐藏加载loading
        Utils.showViewIfNeed(mVideoLoadingBar);
        //隐藏暂停按钮
        Utils.hideViewIfNeed(mVideoPlayView);
        //隐藏播放错误文案
        Utils.hideViewIfNeed(mVideoErrorView);
        if(ScreenState.isSmallWindow(mCurrentScreenState)) {
            cancelDismissControllerViewTimer();
            //显示小窗口关闭按钮
            Utils.showViewIfNeed(mVideoSmallWindowBackView);
        } else {
            cancelDismissControllerViewTimer();
            //隐藏小窗口关闭按钮
            Utils.hideViewIfNeed(mVideoSmallWindowBackView);
        }
        mVideoControllerView.onChangeUISeekBufferingState(mCurrentScreenState);
        onChangeVideoHeaderViewState(false);
    }

    /**
     * UI状态更新为Pause状态，即视频暂停播放状态
     */
    protected void onChangeUIPauseState() {

        //隐藏视频预览图
        Utils.hideViewIfNeed(mVideoThumbView);
        //隐藏加载loading
        Utils.hideViewIfNeed(mVideoLoadingBar);
        cancelDismissControllerViewTimer();
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
        mVideoControllerView.onChangeUIPauseState(mCurrentScreenState);
        onChangeVideoHeaderViewState(true);
    }

    /**
     * UI状态更新为Complete状态，即视频播放结束状态
     */
    protected void onChangeUICompleteState() {

        //显示视频预览图
        Utils.showViewIfNeed(mVideoThumbView);
        //隐藏加载loading
        Utils.hideViewIfNeed(mVideoLoadingBar);
        //显示再次播放按钮
        mVideoPlayView.setImageResource(R.drawable.vp_replay_selector);
        Utils.showViewIfNeed(mVideoPlayView);
        cancelDismissControllerViewTimer();
        //隐藏播放错误文案
        Utils.hideViewIfNeed(mVideoErrorView);
        if(ScreenState.isSmallWindow(mCurrentScreenState)) {
            //显示小窗口关闭按钮
            Utils.showViewIfNeed(mVideoSmallWindowBackView);
        } else {
            //隐藏小窗口关闭按钮
            Utils.hideViewIfNeed(mVideoSmallWindowBackView);
        }
        mVideoControllerView.onChangeUICompleteState(mCurrentScreenState);
        onChangeVideoHeaderViewState(true);
    }

    /**
     * UI状态更新为Error状态，即视频播放错误状态
     */
    protected void onChangeUIErrorState() {

        //隐藏视频预览图
        Utils.hideViewIfNeed(mVideoThumbView);
        //隐藏加载loading
        Utils.hideViewIfNeed(mVideoLoadingBar);
        //隐藏播放按钮
        Utils.hideViewIfNeed(mVideoPlayView);
        cancelDismissControllerViewTimer();
        //显示播放错误文案
        Utils.showViewIfNeed(mVideoErrorView);
        if(ScreenState.isSmallWindow(mCurrentScreenState)) {
            //显示小窗口关闭按钮
            Utils.showViewIfNeed(mVideoSmallWindowBackView);
        } else {
            //隐藏小窗口关闭按钮
            Utils.hideViewIfNeed(mVideoSmallWindowBackView);
        }
        mVideoControllerView.onChangeUIErrorState(mCurrentScreenState);
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
            hideAllPlayStateView();
        } else {
            showAllPlayStateView();
        }
    }

    /**
     * 隐藏所有的播放控制视图
     */
    @Override
    public void hideAllPlayStateView() {

        Utils.hideViewIfNeed(mVideoFullScreenLockView);
        hideAllPlayStateViewExcludeLockView();
    }

    /**
     * 显示所有的播放控制视图(播放状态下)
     */
    @Override
    public void showAllPlayStateView() {

        showFullScreenLockView();
        startDismissControllerViewTimer();
        if(mFullScreenLocked == false) {
            Utils.showViewIfNeed(mVideoPlayView);
            mVideoControllerView.showAllPlayStateView();
            onChangeVideoHeaderViewState(true);
        }
    }

    /**
     * 隐藏所有的播放控制视图（除了全屏播放时的锁屏按钮之外）
     */
    public void hideAllPlayStateViewExcludeLockView() {

        Utils.hideViewIfNeed(mVideoPlayView);
        mVideoControllerView.hideAllPlayStateView();
        onChangeVideoHeaderViewState(false);
        cancelDismissControllerViewTimer();
    }

    /**
     * 显示全屏播放时的锁屏按钮
     */
    private void showFullScreenLockView() {

        if(ScreenState.isFullScreen(mCurrentScreenState)) {
            Utils.showViewIfNeed(mVideoFullScreenLockView);
        } else {
            Utils.hideViewIfNeed(mVideoFullScreenLockView);
        }
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

                            hideAllPlayStateView();
                        }
                    });
                }
            }
        }
    }

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
    @Override
    public void onToggleFullScreen() {

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
    protected void startFullScreen() {

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

        initFullScreenGestureView();
        Utils.getActivity(getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        Utils.getActivity(getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mVideoFullScreenView.setImageResource(R.drawable.vp_ic_minimize);
        PlayerManager.getInstance().play();
        onToggleFullScreenLockState(false);
    }

    /**
     * 退出全屏播放
     */
    protected void exitFullScreen() {

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
        if(ScreenState.isNormal(mCurrentScreenState)) {
            startSmallWindowPlay();
        } else {
            exitSmallWindowPlay(false);
        }
    }

    /**
     * 开始小窗口播放
     */
    protected void startSmallWindowPlay() {

        mVideoControllerView.stopVideoProgressUpdate();
        PlayerManager.getInstance().setScreenState(mCurrentScreenState = ScreenState.SCREEN_STATE_SMALL_WINDOW);
        VideoPlayerView videoPlayerView = new VideoPlayerView(getContext());
        videoPlayerView.setId(R.id.vp_small_window_view_id);
        videoPlayerView.mVideoControllerView = mVideoControllerView;
        videoPlayerView.mFullScreenGestureView = mFullScreenGestureView;
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
    protected void exitSmallWindowPlay(boolean forceStop) {

        if(ScreenState.isSmallWindow(mCurrentScreenState) == false) {
            return;
        }

        ViewGroup windowContent = (ViewGroup) (Utils.getActivity(getContext())).findViewById(Window.ID_ANDROID_CONTENT);
        VideoPlayerView smallWindowView = (VideoPlayerView) windowContent.findViewById(R.id.vp_small_window_view_id);
        smallWindowView.mVideoControllerView.stopVideoProgressUpdate();
        PlayerManager.getInstance().setScreenState(mCurrentScreenState = ScreenState.SCREEN_STATE_NORMAL);
        PlayerManager.getInstance().setTextureView(null);
        smallWindowView.mVideoTextureViewContainer.removeAllViews();

        mFullScreenGestureView = smallWindowView.mFullScreenGestureView;
        mVideoControllerView = smallWindowView.mVideoControllerView;
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

    /**
     * TextureView Touch后的逻辑处理
     *
     * 主要处理逻辑为：
     * 1.触摸该View时显示或隐藏底部控制条
     * 2.控制音量，亮度，快进和后退
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if(ScreenState.isSmallWindow(mCurrentScreenState)) {
            //小窗口播放时不响应手势操作
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                cancelDismissControllerViewTimer();
                break;
            case MotionEvent.ACTION_UP:
                onChangeUIWhenTouchVideoView();
                break;
        }

        if(mFullScreenLocked) {
            return false;
        }

        if(mFullScreenGestureView != null) {
            mFullScreenGestureView.onTouch(event, this,
                    mVideoControllerView.getDuration(), mCurrentState);
        }

        return false;
    }

    @Override
    public void onFullScreenGestureStart() {

        hideAllPlayStateView();
    }

    @Override
    public void onFullScreenGestureFinish() {

        showAllPlayStateView();
    }
}
