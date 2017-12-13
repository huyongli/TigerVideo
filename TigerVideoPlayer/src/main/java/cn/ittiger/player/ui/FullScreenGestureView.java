package cn.ittiger.player.ui;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.ittiger.player.PlayerManager;
import cn.ittiger.player.R;
import cn.ittiger.player.listener.FullScreenGestureListener;
import cn.ittiger.player.listener.FullScreenGestureStateListener;
import cn.ittiger.player.state.GestureTouchState;
import cn.ittiger.player.state.PlayState;
import cn.ittiger.player.util.Utils;

/**
 * @author: ylhu
 * @time: 2017/12/12
 */

public class FullScreenGestureView extends FrameLayout implements FullScreenGestureListener {
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
     * 屏幕宽度
     */
    private int mScreenWidth;
    /**
     * 屏幕高度
     */
    private int mScreenHeight;
    /**
     * 视频时长，miliseconds
     */
    private int mDuration = 0;

    public FullScreenGestureView(@NonNull Context context) {

        super(context);
        init(context);
    }

    public FullScreenGestureView(@NonNull Context context, @Nullable AttributeSet attrs) {

        super(context, attrs);
        init(context);
    }

    public FullScreenGestureView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FullScreenGestureView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {

        inflate(context, getFullScreenGestureViewLayoutResId(), this);
        mScreenWidth = Utils.getWindowWidth(context);
        mScreenHeight = Utils.getWindowHeight(context);
        initWidgetView();
        initFullScreenGestureParams();
    }

    protected int getFullScreenGestureViewLayoutResId() {

        return R.layout.vp_fullscreen_gesture_view;
    }

    protected void initWidgetView() {

        mVideoVolumeView = (LinearLayout) findViewById(R.id.vp_video_volume);
        mVideoVolumeProgress = (ProgressBar) findViewById(R.id.vp_video_volume_progressbar);
        mVideoBrightnessView = (LinearLayout) findViewById(R.id.vp_video_brightness);
        mVideoBrightnessProgress = (ProgressBar) findViewById(R.id.vp_video_brightness_progressbar);
        mVideoChangeProgressView = findViewById(R.id.vp_video_change_progress_view);
        mVideoChangeProgressIcon = (ImageView) findViewById(R.id.vp_video_change_progress_icon);
        mVideoChangeProgressCurrPro = (TextView) findViewById(R.id.vp_video_change_progress_current);
        mVideoChangeProgressTotal = (TextView) findViewById(R.id.vp_video_change_progress_total);
        mVideoChangeProgressBar = (ProgressBar) findViewById(R.id.vp_video_change_progress_bar);
    }

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

    @Override
    public void onTouch(MotionEvent event, FullScreenGestureStateListener fullScreenGestureStateListener,
                        int duration, int currentPlayState) {

        this.mDuration = duration;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchDownX = event.getRawX();
                mTouchDownY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if(currentPlayState != PlayState.STATE_PLAYING && currentPlayState != PlayState.STATE_PAUSE) {
                    break;
                }
                float xDis = Math.abs(mTouchDownX - event.getRawX());
                float yDis = Math.abs(event.getRawY() - mTouchDownY);
                Utils.logTouch("TouchSlop:" + mTouchSlop + ", xDis:" + xDis + ", yDis:" + yDis);
                if(isFlingLeft(mTouchDownX, mTouchDownY, event)) {//向左滑，退后
                    fullScreenGestureStateListener.onFullScreenGestureStart();
                    Utils.logTouch("Fling Left");
                    videoSeek(false);
                    mTouchDownX = event.getRawX();
                    mTouchDownY = event.getRawY();
                } else if(isFlingRight(mTouchDownX, mTouchDownY, event)) {//向右滑，快进
                    fullScreenGestureStateListener.onFullScreenGestureStart();
                    Utils.logTouch("Fling Right");
                    videoSeek(true);
                    mTouchDownX = event.getRawX();
                    mTouchDownY = event.getRawY();
                } else if(isScrollVertical(mTouchDownX, mTouchDownY, event)) {//垂直方向滑
                    fullScreenGestureStateListener.onFullScreenGestureStart();
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
                            fullScreenGestureStateListener.onFullScreenGestureFinish();
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
     * 调整视频音量大小
     *
     * 重写此方法可以改变音量的改变速率
     *
     * @param isTurnUp    是否调大音量
     */
    protected void changeVideoVolume(boolean isTurnUp) {

        mCurrentGestureState = GestureTouchState.STATE_VOLUME;
        int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if(isTurnUp) {
            volume = volume + VOLUME_STEP >= mMaxVolume ? mMaxVolume : volume + VOLUME_STEP;
        } else {
            volume = volume - VOLUME_STEP > 0 ? volume - VOLUME_STEP : 0;
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        updateVolumeView((int)(volume * 1.0 / mMaxVolume * TOTAL_PERCENT + 0.5f));
    }

    /**
     * 更新音量百分比视图
     * @param volumePercent
     */
    protected void updateVolumeView(int volumePercent) {

        Utils.showViewIfNeed(mVideoVolumeView);
        mVideoVolumeProgress.setProgress(volumePercent);
    }

    /**
     * 调整屏幕亮度
     *
     * 重写此方法可以改变亮度的变化速率
     *
     * @param isDodge  是否调亮
     */
    protected void changeBrightness(boolean isDodge) {

        mCurrentGestureState = GestureTouchState.STATE_BRIGHTNESS;
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
        updateBrightnessView((int)(brightness * TOTAL_PERCENT));
    }

    /**
     * 更新亮度百分比视图
     * @param brightnessPercent
     */
    protected void updateBrightnessView(int brightnessPercent) {

        Utils.showViewIfNeed(mVideoBrightnessView);
        mVideoBrightnessProgress.setProgress(brightnessPercent);
    }

    /**
     * 视频前进后退
     *
     * 重写此方法可以改变前进后退的速率
     * @param isForward
     */
    protected void videoSeek(boolean isForward) {

        mCurrentGestureState = GestureTouchState.STATE_VIDEO_PROGRESS;
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
        updateSeekView(Utils.formatVideoTimeLength(mGestureSeekToPosition),
                "/" + Utils.formatVideoTimeLength(mDuration),
                (int) (mGestureSeekToPosition * 1.0f / mDuration * TOTAL_PERCENT + 0.5f));
    }

    /**
     * 更新快进后退显示视图
     * @param currTime
     * @param duration
     * @param currProgress
     */
    protected void updateSeekView(String currTime, String duration, int currProgress) {

        Utils.showViewIfNeed(mVideoChangeProgressView);
        mVideoChangeProgressCurrPro.setText(currTime);
        mVideoChangeProgressTotal.setText(duration);
        mVideoChangeProgressBar.setProgress(currProgress);
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
