package cn.ittiger.player.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

import cn.ittiger.player.R;
import cn.ittiger.player.listener.FullScreenGestureStateListener;
import cn.ittiger.player.listener.FullScreenToggleListener;
import cn.ittiger.player.listener.UIStateChangeListener;
import cn.ittiger.player.listener.VideoTouchListener;
import cn.ittiger.player.state.PlayState;
import cn.ittiger.player.state.ScreenState;
import cn.ittiger.player.util.Utils;
import cn.ittiger.player.util.ViewIndex;

/**
 * 视频播放时界面上各控件的显示控制
 * @author: ylhu
 * @time: 2017/12/15
 */
public abstract class StandardVideoView extends RelativeLayout implements
        View.OnClickListener,
        View.OnTouchListener,
        FullScreenGestureStateListener,
        FullScreenToggleListener,
        UIStateChangeListener,
        VideoTouchListener {
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
    protected FullScreenGestureView mFullScreenGestureView;


    /**
     * 视频加载进度
     */
    protected ProgressBar mVideoLoadingBar;
    /**
     * 视频播放按钮
     */
    protected ImageView mVideoPlayButton;


    /**
     * 视频加载失败的提示View
     */
    protected View mVideoErrorView;
    /**
     * 自定义视频加载失败的提示View 的layout resId
     */
    private int mVideoErrorViewRes = -1;


    /**
     * 小窗口播放时的关闭按钮
     */
    protected ImageView mVideoSmallWindowBackView;


    /**
     * 视频顶部的视频标题和全屏时的返回按钮
     */
    protected VideoHeaderView mVideoHeaderView;
    /**
     * 自定义视频顶部的视频标题和全屏时的返回按钮layout res id
     * 必须继承{@link VideoHeaderView}
     */
    private int mVideoHeaderViewRes = -1;


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
     * 视频标题
     */
    protected CharSequence mVideoTitle;
    /**
     * 视频地址
     */
    protected String mVideoUrl;
    /**
     * 全屏播放时，是否已经锁屏不允许操作
     */
    protected boolean mFullScreenLocked = false;

    public StandardVideoView(Context context) {

        this(context, null);
    }

    public StandardVideoView(Context context, AttributeSet attrs) {

        super(context, attrs);
        initView(context, attrs);
    }

    public StandardVideoView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StandardVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs);
    }

    protected int getPlayerLayoutId() {

        return R.layout.vp_layout_videoplayer;
    }

    protected void initView(Context context, AttributeSet attrs) {

        if(attrs != null) {
            initCustomViewAttributes(context, attrs);
        }

        inflate(context, getPlayerLayoutId(), this);
        //避免ListView中item点击无法响应的问题
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        setBackgroundColor(Color.BLACK);

        findAndBindView();

        initVideoThumbView();
        initVideoControllerView();
        initVideoPlayErrorView();
        initVideoHeaderView();

        bindViewListener();
    }

    protected void findAndBindView() {

        mVideoTextureViewContainer = (FrameLayout) findViewById(R.id.vp_video_surface_container);
        mVideoLoadingBar = (ProgressBar) findViewById(R.id.vp_video_loading);
        mVideoPlayButton = (ImageView) findViewById(R.id.vp_video_play);
        mVideoSmallWindowBackView = (ImageView) findViewById(R.id.vp_video_small_window_back);
        mVideoFullScreenLockView = (ImageView) findViewById(R.id.vp_fullscreen_lock);
    }

    protected void bindViewListener() {

        mVideoPlayButton.setOnClickListener(this);
        mVideoThumbView.setOnClickListener(this);
        mVideoTextureViewContainer.setOnClickListener(this);
        mVideoTextureViewContainer.setOnTouchListener(this);
        mVideoErrorView.setOnClickListener(this);
        mVideoControllerView.setOnTouchListener(this);
        mVideoSmallWindowBackView.setOnClickListener(this);
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
        mVideoErrorViewRes = attr.getResourceId(R.styleable.VideoPlayerView_vpVideoErrorViewLayoutRes, -1);
        mVideoHeaderViewRes = attr.getResourceId(R.styleable.VideoPlayerView_vpVideoHeaderViewLayoutRes, -1);

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
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mVideoThumbView, ViewIndex.VIDEO_THUMB_VIEW_INDEX, params);
    }

    /**
     * 初始化底部控制台View
     */
    protected void initVideoControllerView() {

        if(mVideoControllerView != null) {
            return;
        }
        if(mVideoControllerViewRes == -1) {
            mVideoControllerView = new VideoControllerView(getContext());
        } else {
            mVideoControllerView = (VideoControllerView) inflate(getContext(), mVideoControllerViewRes, null);
        }
        mVideoControllerView.setFullScreenToggleListener(this);
        mVideoControllerView.setCurrentScreenState(getCurrentScreenState());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mFullScreenGestureView, ViewIndex.FULLSCREEN_GESTURE_VIEW_INDEX, params);
    }

    /**
     * 初始化视频加载失败视图
     */
    protected void initVideoPlayErrorView() {

        if(mVideoErrorView != null) {
            return;
        }
        if(mVideoErrorViewRes == -1) {
            mVideoErrorViewRes = R.layout.vp_layout_play_error;
        }
        mVideoErrorView = inflate(getContext(), mVideoErrorViewRes, null);
        mVideoErrorView.setOnClickListener(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        int idx = indexOfChild(mVideoPlayButton);
        this.addView(mVideoErrorView, idx + 1, params);
    }

    /**
     * 初始化视频头部标题视图
     */
    protected void initVideoHeaderView() {

        if(mVideoHeaderView != null) {
            return;
        }
        if(mVideoHeaderViewRes == -1) {
            mVideoHeaderView = new VideoHeaderView(getContext());
        } else {
            mVideoHeaderView = (VideoHeaderView) inflate(getContext(), mVideoHeaderViewRes, null);
        }
        mVideoHeaderView.setFullScreenToggleListener(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        int idx = indexOfChild(mVideoSmallWindowBackView);
        this.addView(mVideoHeaderView, idx + 1, params);
    }

    /**
     * 绑定数据
     * @param videoUrl                  视频地址
     * @param title                     视频标题
     * @param normalStateShowTitle      正常状态下是否显示视频标题
     */
    public void bind(String videoUrl, CharSequence title, boolean normalStateShowTitle) {

        mVideoHeaderView.mNormalStateShowTitle = normalStateShowTitle;
        mVideoTitle = title;
        mVideoUrl = videoUrl;
        mVideoHeaderView.setTitle(mVideoTitle);
    }

    /**
     * 绑定数据
     * @param videoUrl    视频地址
     * @param title       视频标题
     */
    public void bind(String videoUrl, CharSequence title) {

        bind(videoUrl, title, mVideoHeaderView.mNormalStateShowTitle);
    }

    /**
     * 绑定数据
     * @param videoUrl  视频地址
     */
    public void bind(String videoUrl) {

        bind(videoUrl, null);
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
     * 更改头部视图状态
     * @param isShow    是否显示头部视图
     */
    protected void onChangeVideoHeaderViewState(boolean isShow) {

        mVideoHeaderView.onChangeVideoHeaderViewState(getCurrentScreenState(), isShow);
        if(isShow && ScreenState.isFullScreen(getCurrentScreenState())) {
            Utils.showViewIfNeed(mVideoFullScreenLockView);
        }
    }

    /**
     * UI状态更新为Normal状态，即初始状态
     */
    @Override
    public void onChangeUINormalState(int screenState) {

        //显示视频预览图
        Utils.showViewIfNeed(mVideoThumbView);
        //隐藏加载loading
        Utils.hideViewIfNeed(mVideoLoadingBar);
        //显示播放按钮
        mVideoPlayButton.setImageResource(R.drawable.vp_play_selector);
        Utils.showViewIfNeed(mVideoPlayButton);
        //隐藏播放错误文案
        Utils.hideViewIfNeed(mVideoErrorView);
        if(ScreenState.isSmallWindow(screenState)) {
            //隐藏小窗口关闭按钮
            Utils.showViewIfNeed(mVideoSmallWindowBackView);
        } else if(ScreenState.isFullScreen(screenState)){
            //隐藏小窗口关闭按钮
            Utils.hideViewIfNeed(mVideoSmallWindowBackView);
        }
        mVideoControllerView.onChangeUINormalState(screenState);
        onChangeVideoHeaderViewState(true);
    }

    /**
     * UI状态更新为Loading状态，即视频加载状态
     */
    @Override
    public void onChangeUILoadingState(int screenState) {

        //显示视频预览图
        Utils.hideViewIfNeed(mVideoThumbView);
        //显示加载loading
        Utils.showViewIfNeed(mVideoLoadingBar);
        //隐藏播放按钮
        Utils.hideViewIfNeed(mVideoPlayButton);
        //隐藏播放错误文案
        Utils.hideViewIfNeed(mVideoErrorView);
        if(ScreenState.isSmallWindow(screenState)) {
            //隐藏小窗口关闭按钮
            Utils.showViewIfNeed(mVideoSmallWindowBackView);
        } else {
            //隐藏小窗口关闭按钮
            Utils.hideViewIfNeed(mVideoSmallWindowBackView);
        }
        mVideoControllerView.onChangeUILoadingState(screenState);
        onChangeVideoHeaderViewState(false);
    }

    /**
     * UI状态更新为Playing状态，即视频播放状态
     */
    @Override
    public void onChangeUIPlayingState(int screenState) {


        //隐藏视频预览图
        Utils.hideViewIfNeed(mVideoThumbView);
        //隐藏加载loading
        Utils.hideViewIfNeed(mVideoLoadingBar);
        //隐藏播放错误文案
        Utils.hideViewIfNeed(mVideoErrorView);
        if(ScreenState.isSmallWindow(screenState)) {
            cancelDismissControllerViewTimer();
            //隐藏暂停按钮
            Utils.hideViewIfNeed(mVideoPlayButton);
            //显示小窗口关闭按钮
            Utils.showViewIfNeed(mVideoSmallWindowBackView);
        } else {
            startDismissControllerViewTimer();
            //显示暂停按钮
            mVideoPlayButton.setImageResource(R.drawable.vp_pause_selector);
            Utils.showViewIfNeed(mVideoPlayButton);
            //隐藏小窗口关闭按钮
            Utils.hideViewIfNeed(mVideoSmallWindowBackView);
        }
        mVideoControllerView.onChangeUIPlayingState(screenState);
        onChangeVideoHeaderViewState(true);
    }

    /**
     * UI状态更新为SeekBuffer状态，即视拖动进度条
     */
    @Override
    public void onChangeUISeekBufferingState(int screenState) {

        //隐藏视频预览图
        Utils.hideViewIfNeed(mVideoThumbView);
        //隐藏加载loading
        Utils.showViewIfNeed(mVideoLoadingBar);
        //隐藏暂停按钮
        Utils.hideViewIfNeed(mVideoPlayButton);
        //隐藏播放错误文案
        Utils.hideViewIfNeed(mVideoErrorView);
        if(ScreenState.isSmallWindow(screenState)) {
            cancelDismissControllerViewTimer();
            //显示小窗口关闭按钮
            Utils.showViewIfNeed(mVideoSmallWindowBackView);
        } else {
            cancelDismissControllerViewTimer();
            //隐藏小窗口关闭按钮
            Utils.hideViewIfNeed(mVideoSmallWindowBackView);
        }
        mVideoControllerView.onChangeUISeekBufferingState(screenState);
        onChangeVideoHeaderViewState(false);
    }

    /**
     * UI状态更新为Pause状态，即视频暂停播放状态
     */
    @Override
    public void onChangeUIPauseState(int screenState) {

        //隐藏视频预览图
        Utils.hideViewIfNeed(mVideoThumbView);
        //隐藏加载loading
        Utils.hideViewIfNeed(mVideoLoadingBar);
        cancelDismissControllerViewTimer();
        //隐藏播放错误文案
        Utils.hideViewIfNeed(mVideoErrorView);
        if(ScreenState.isSmallWindow(screenState)) {
            //显示小窗口关闭按钮
            Utils.showViewIfNeed(mVideoSmallWindowBackView);
            //隐藏播放按钮
            Utils.hideViewIfNeed(mVideoPlayButton);
        } else {
            //显示播放按钮
            mVideoPlayButton.setImageResource(R.drawable.vp_play_selector);
            Utils.showViewIfNeed(mVideoPlayButton);
            //隐藏小窗口关闭按钮
            Utils.hideViewIfNeed(mVideoSmallWindowBackView);
        }
        mVideoControllerView.onChangeUIPauseState(screenState);
        onChangeVideoHeaderViewState(true);
    }

    /**
     * UI状态更新为Complete状态，即视频播放结束状态
     */
    @Override
    public void onChangeUICompleteState(int screenState) {

        //显示视频预览图
        Utils.showViewIfNeed(mVideoThumbView);
        //隐藏加载loading
        Utils.hideViewIfNeed(mVideoLoadingBar);
        //显示再次播放按钮
        mVideoPlayButton.setImageResource(R.drawable.vp_replay_selector);
        Utils.showViewIfNeed(mVideoPlayButton);
        cancelDismissControllerViewTimer();
        //隐藏播放错误文案
        Utils.hideViewIfNeed(mVideoErrorView);
        if(ScreenState.isSmallWindow(screenState)) {
            //显示小窗口关闭按钮
            Utils.showViewIfNeed(mVideoSmallWindowBackView);
        } else {
            //隐藏小窗口关闭按钮
            Utils.hideViewIfNeed(mVideoSmallWindowBackView);
        }
        mVideoControllerView.onChangeUICompleteState(screenState);
        onChangeVideoHeaderViewState(true);
        Utils.hideViewIfNeed(mVideoFullScreenLockView);
    }

    /**
     * UI状态更新为Error状态，即视频播放错误状态
     */
    @Override
    public void onChangeUIErrorState(int screenState) {

        //隐藏视频预览图
        Utils.hideViewIfNeed(mVideoThumbView);
        //隐藏加载loading
        Utils.hideViewIfNeed(mVideoLoadingBar);
        //隐藏播放按钮
        Utils.hideViewIfNeed(mVideoPlayButton);
        cancelDismissControllerViewTimer();
        //显示播放错误文案
        Utils.showViewIfNeed(mVideoErrorView);
        if(ScreenState.isSmallWindow(screenState)) {
            //显示小窗口关闭按钮
            Utils.showViewIfNeed(mVideoSmallWindowBackView);
        } else {
            //隐藏小窗口关闭按钮
            Utils.hideViewIfNeed(mVideoSmallWindowBackView);
        }
        mVideoControllerView.onChangeUIErrorState(screenState);
        onChangeVideoHeaderViewState(false);
    }

    /**
     * 当触摸视频时更新相关UI状态
     */
    public void onChangeUIWhenTouchVideoView() {

        if(getCurrentState() != PlayState.STATE_PLAYING) {
            return;
        }
        if(mFullScreenLocked) {
            if(Utils.isViewShown(mVideoFullScreenLockView)) {
                cancelDismissControllerViewTimer();
                Utils.hideViewIfNeed(mVideoFullScreenLockView);
            } else {
                Utils.showViewIfNeed(mVideoFullScreenLockView);
                startDismissControllerViewTimer();
            }
        } else {
            boolean isAllShown = Utils.isViewShown(mVideoPlayButton) && Utils.isViewShown(mVideoControllerView);
            if(isAllShown) {
                hideAllPlayStateView();
            } else {
                showAllPlayStateView();
            }
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

        startDismissControllerViewTimer();
        if(ScreenState.isFullScreen(getCurrentScreenState())) {
            Utils.showViewIfNeed(mVideoFullScreenLockView);
        }
        Utils.showViewIfNeed(mVideoPlayButton);
        mVideoControllerView.showAllPlayStateView();
        onChangeVideoHeaderViewState(true);
    }

    /**
     * 隐藏所有的播放控制视图（除了全屏播放时的锁屏按钮之外）
     */
    public void hideAllPlayStateViewExcludeLockView() {

        Utils.hideViewIfNeed(mVideoPlayButton);
        mVideoControllerView.hideAllPlayStateView();
        onChangeVideoHeaderViewState(false);
        cancelDismissControllerViewTimer();
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

            int state = getCurrentState();
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

        if(ScreenState.isSmallWindow(getCurrentScreenState())) {
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
                    mVideoControllerView.getDuration(), getCurrentState());
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

    /**
     * 获取当前的屏幕状态(全屏|小窗口|正常)
     * @return
     */
    public abstract int getCurrentScreenState();

    /**
     * 获取当前的播放状态
     * @return
     */
    public abstract int getCurrentState();
}
