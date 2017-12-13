package cn.ittiger.player;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import cn.ittiger.player.listener.FullScreenToggleListener;
import cn.ittiger.player.listener.UIStateChangeListener;

/**
 * 底部播放控制视图
 * @author: ylhu
 * @time: 2017/12/12
 */
public class VideoControllerView extends LinearLayout implements UIStateChangeListener {
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
        setOrientation(HORIZONTAL);

        initWidgetView();
    }

    /**
     * 初始化底部控制条各个控件
     */
    protected void initWidgetView() {

        mVideoPlayTimeView = (TextView) findViewById(R.id.vp_video_play_time);
        mVideoTotalTimeView = (TextView) findViewById(R.id.vp_video_total_time);
        mVideoPlaySeekBar = (SeekBar) findViewById(R.id.vp_video_seek_progress);
        mVideoFullScreenView = (ImageView) findViewById(R.id.vp_video_fullscreen);
    }

    protected int getControllerViewLayoutResId() {

        return R.layout.vp_layout_bottom_controller;
    }

    @Override
    public void onChangeUINormalState(int screenState) {

    }

    @Override
    public void onChangeUILoadingState(int screenState) {

    }

    @Override
    public void onChangeUIPlayingState(int screenState) {

    }

    @Override
    public void onChangeUIPauseState(int screenState) {

    }

    @Override
    public void onChangeUISeekBufferingState(int screenState) {

    }

    @Override
    public void onChangeUICompleteState(int screenState) {

    }

    @Override
    public void onChangeUIErrorState(int screenState) {

    }

    public void setFullScreenToggleListener(FullScreenToggleListener fullScreenToggleListener) {

        mFullScreenToggleListener = fullScreenToggleListener;
    }
}
