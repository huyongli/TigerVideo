package cn.ittiger.player.ui;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import cn.ittiger.player.PlayerManager;
import cn.ittiger.player.R;
import cn.ittiger.player.listener.FullScreenToggleListener;
import cn.ittiger.player.listener.UIStateChangeListener;
import cn.ittiger.player.listener.VideoControllerViewListener;
import cn.ittiger.player.listener.VideoTouchListener;
import cn.ittiger.player.state.ScreenState;
import cn.ittiger.player.util.Utils;

/**
 * 底部播放控制视图
 * @author: ylhu
 * @time: 2017/12/12
 */
public class VideoControllerView extends RelativeLayout implements
        UIStateChangeListener,
        View.OnClickListener,
        VideoControllerViewListener,
        VideoTouchListener,
        SeekBar.OnSeekBarChangeListener {
    private static final int PROGRESS_UPDATE_INTERNAL = 300;
    private static final int PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
    protected View mVideoControllerInternalView;
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
    protected ImageView mVideoFullScreenButton;
    /**
     * 控制条不显示后，显示播放进度的进度条(不可点击)
     */
    protected ProgressBar mBottomProgressBar;

    /**
     * 当前屏幕播放状态
     */
    protected int mCurrentScreenState = ScreenState.SCREEN_STATE_NORMAL;
    /**
     * 视频时长，miliseconds
     */
    private int mDuration = 0;

    private final ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mScheduleFuture;

    /**
     * 全屏与非全屏切换操作监听
     */
    private FullScreenToggleListener mFullScreenToggleListener;

    public VideoControllerView(Context context) {

        super(context);
        loadLayoutRes(context);
    }

    public VideoControllerView(Context context, @Nullable AttributeSet attrs) {

        super(context, attrs);
        loadLayoutRes(context);
    }

    public VideoControllerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        loadLayoutRes(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoControllerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        super(context, attrs, defStyleAttr, defStyleRes);
        loadLayoutRes(context);
    }

    private void loadLayoutRes(Context context) {

        inflate(context, getControllerViewLayoutResId(), this);
        setVisibility(GONE);
        initWidgetView();
    }

    public void cloneState(VideoControllerView controllerView) {

        this.mCurrentScreenState = controllerView.mCurrentScreenState;
        this.mDuration = controllerView.mDuration;
    }

    /**
     * 初始化底部控制条各个控件
     */
    protected void initWidgetView() {

        mVideoControllerInternalView = findViewById(R.id.vp_video_bottom_controller_view);
        mVideoPlayTimeView = (TextView) findViewById(R.id.vp_video_play_time);
        mVideoTotalTimeView = (TextView) findViewById(R.id.vp_video_total_time);
        mVideoPlaySeekBar = (SeekBar) findViewById(R.id.vp_video_seek_progress);
        mVideoFullScreenButton = (ImageView) findViewById(R.id.vp_video_fullscreen);
        mBottomProgressBar = (ProgressBar) findViewById(R.id.vp_video_bottom_progress);

        mVideoFullScreenButton.setOnClickListener(this);
        mVideoPlaySeekBar.setOnSeekBarChangeListener(this);
    }

    protected int getControllerViewLayoutResId() {

        return R.layout.vp_layout_bottom_controller;
    }

    /**
     * 全屏播放按钮点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {

        if(mFullScreenToggleListener == null) {
            return;
        }
        if(ScreenState.isFullScreen(mCurrentScreenState)) {
            mFullScreenToggleListener.onExitFullScreen();
            mVideoFullScreenButton.setImageResource(R.drawable.vp_ic_fullscreen);
        } else if(ScreenState.isNormal(mCurrentScreenState)){
            mFullScreenToggleListener.onStartFullScreen();
            mVideoFullScreenButton.setImageResource(R.drawable.vp_ic_minimize);
        } else {
            throw new IllegalStateException("the screen state is error, state=" + mCurrentScreenState);
        }
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
    /************************ 结束底部SeekBar监听 ********************************/

    @Override
    public void onChangeUINormalState(int screenState) {

        Utils.hideViewIfNeed(this);
        //隐藏播放控制条
        Utils.hideViewIfNeed(mVideoControllerInternalView);
        //隐藏底部播放进度
        Utils.hideViewIfNeed(mBottomProgressBar);
    }

    @Override
    public void onChangeUILoadingState(int screenState) {

        Utils.hideViewIfNeed(this);
        //隐藏播放控制条
        Utils.hideViewIfNeed(mVideoControllerInternalView);
        //隐藏底部播放进度
        Utils.hideViewIfNeed(mBottomProgressBar);
    }

    @Override
    public void onChangeUIPlayingState(int screenState) {

        Utils.showViewIfNeed(this);
        if(ScreenState.isSmallWindow(screenState)) {
            //隐藏播放控制条
            Utils.hideViewIfNeed(mVideoControllerInternalView);
            //显示底部播放进度
            Utils.showViewIfNeed(mBottomProgressBar);
        } else {
            //显示播放控制条
            Utils.showViewIfNeed(mVideoControllerInternalView);
            //隐藏底部播放进度
            Utils.hideViewIfNeed(mBottomProgressBar);
        }
    }

    @Override
    public void onChangeUIPauseState(int screenState) {

        Utils.showViewIfNeed(this);
        //显示播放控制条
        Utils.showViewIfNeed(mVideoControllerInternalView);
        //隐藏底部播放进度
        Utils.hideViewIfNeed(mBottomProgressBar);
    }

    @Override
    public void onChangeUISeekBufferingState(int screenState) {

        Utils.showViewIfNeed(this);
        if(ScreenState.isSmallWindow(screenState)) {
            //隐藏播放控制条
            Utils.hideViewIfNeed(mVideoControllerInternalView);
            //显示底部播放进度
            Utils.showViewIfNeed(mBottomProgressBar);
        } else {
            //显示播放控制条
            Utils.showViewIfNeed(mVideoControllerInternalView);
            //隐藏底部播放进度
            Utils.hideViewIfNeed(mBottomProgressBar);
        }
    }

    @Override
    public void onChangeUICompleteState(int screenState) {

        Utils.hideViewIfNeed(this);
        //隐藏播放控制条
        Utils.hideViewIfNeed(mVideoControllerInternalView);
        //隐藏底部播放进度
        Utils.hideViewIfNeed(mBottomProgressBar);
        updateProgress(mDuration);
        mVideoFullScreenButton.setImageResource(R.drawable.vp_ic_fullscreen);
    }

    @Override
    public void onChangeUIErrorState(int screenState) {

        Utils.hideViewIfNeed(this);
        //隐藏播放控制条
        Utils.hideViewIfNeed(mVideoControllerInternalView);
        //隐藏底部播放进度
        Utils.hideViewIfNeed(mBottomProgressBar);
    }

    @Override
    public void showAllPlayStateView() {

        Utils.showViewIfNeed(this);
        //显示播放控制条
        Utils.showViewIfNeed(mVideoControllerInternalView);
        //隐藏底部播放进度
        Utils.hideViewIfNeed(mBottomProgressBar);
    }

    @Override
    public void hideAllPlayStateView() {

        Utils.showViewIfNeed(this);
        //隐藏播放控制条
        Utils.hideViewIfNeed(mVideoControllerInternalView);
        //显示底部播放进度
        Utils.showViewIfNeed(mBottomProgressBar);
    }

    @Override
    public void onVideoDurationChanged(int duration) {

        mDuration = duration;
        String time = Utils.formatVideoTimeLength(duration);
        mVideoTotalTimeView.setText(time);
    }

    /**
     * 开始更新播放进度
     */
    @Override
    public void startVideoProgressUpdate() {

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
    @Override
    public void stopVideoProgressUpdate() {

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

    public void setFullScreenToggleListener(FullScreenToggleListener fullScreenToggleListener) {

        mFullScreenToggleListener = fullScreenToggleListener;
    }

    public int getDuration() {

        return mDuration;
    }

    public void setCurrentScreenState(int currentScreenState) {

        mCurrentScreenState = currentScreenState;
    }

    public void toggleFullScreenButtonVisibility(boolean isShow) {

        if(isShow) {
            Utils.showViewIfNeed(mVideoFullScreenButton);
        } else {
            Utils.hideViewIfNeed(mVideoFullScreenButton);
        }
    }
}
