package cn.ittiger.video.player;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ittiger.video.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by laohu on 16-7-29.
 */
public class VideoPlayerView extends RelativeLayout
        implements View.OnClickListener, TextureView.SurfaceTextureListener {

    private static final String TAG = "VideoPlayerView";

    @BindView(R.id.video_player_view_container)
    RelativeLayout mVideoContainer;
    @BindView(R.id.videoControllerView)
    VideoPlayerControllerView mVideoPlayerControllerView;//视频底部的播放控制器
    @BindView(R.id.video_loading)
    ProgressBar mProgressBar;//视频加载过程中的进度条
    @BindView(R.id.iv_video_play_btn)
    ImageView mVideoPlayButton;

    private VideoPlayState mVideoPlayState = VideoPlayState.STOP;//视频播放的当前状态：播放，暂停
    private TextureView mTextureView;//视频播放容器
    private Surface mSurface = null;
    private MediaPlayer mPlayer;
    //mVideoUrl = "/storage/emulated/0/tencent/MicroMsg/WeiXin/1461625479791.mp4";
    ///storage/emulated/0/download/1461625479791.mp4
    private String mVideoUrl = "";
    private int mSecProgress = 0;
    private ExitFullScreenListener mExitFullScreenListener;

    //滑动调节音量相关视图
    @BindView(R.id.video_volume)
    LinearLayout mVideoVolumeView;
    @BindView(R.id.video_volume_progressbar)
    ProgressBar mVideoVolumeProgress;

    //滑动调节亮度相关视图
    @BindView(R.id.video_brightness)
    LinearLayout mVideoBrightnessView;
    @BindView(R.id.video_brightness_progressbar)
    ProgressBar mVideoBrightnessProgress;

    //滑动快进快退相关视图
    @BindView(R.id.video_change_progress_view)
    View mVideoChangeProgressView;
    @BindView(R.id.video_change_progress_icon)
    ImageView mVideoChangeProgressIcon;
    @BindView(R.id.video_change_progress_current)
    TextView mVideoChangeProgressCurrPro;
    @BindView(R.id.video_change_progress_total)
    TextView mVideoChangeProgressTotal;
    @BindView(R.id.video_change_progress_bar)
    ProgressBar mVideoChangeProgressBar;

    public VideoPlayerView(Context context) {

        this(context, null);
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {

        this(context, attrs, 0);
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {

        inflate(context, R.layout.video_player_view_layout, this);
        ButterKnife.bind(this);
        mVideoPlayerControllerView.setVideoControlListener(mVideoControlListener);
    }

    /**
     * 创建视频播放控制器
     */
    private void createMediaPlayer() {

        mPlayer = new MediaPlayer();
        addVideoPlayListener();
        createVideoSurface();
    }

    /**
     * 创建视频显示介质
     */
    private void createVideoSurface() {

        if(mTextureView == null) {
            mTextureView = new TextureView(getContext());
            mTextureView.setId(R.id.id_video_texture_view);
            mTextureView.setOnClickListener(this);
            mTextureView.setKeepScreenOn(true);
            mTextureView.setSurfaceTextureListener(this);
            mTextureView.setOnTouchListener(new VideoOnTouchListener());
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            mTextureView.setLayoutParams(params);
            mVideoContainer.addView(mTextureView, 0);
        }
    }

    @OnClick(R.id.iv_video_play_btn)
    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.iv_video_play_btn:
                playButtonClick();
                break;
            case R.id.id_video_texture_view:
                if(getPlayScreenState() != PlayScreenState.SMALL) {
                    mVideoPlayerControllerView.showOrHide();
                }
                break;
        }
    }

    private void playButtonClick() {

        switch (mVideoPlayState) {
            case PLAY:
                pause();
                break;
            case PAUSE:
                play();
                break;
            case FINISH:
                mPlayer.seekTo(0);
                updatePlayProgress();
                play();
                break;
        }
    }

    /**
     * 开始播放指定url的视频
     *
     * @param videoUrl
     */
    public void play(String videoUrl) {

        mVideoUrl = videoUrl;
        createMediaPlayer();
        setVideoPlayState(VideoPlayState.PREPARE_LOAD);
    }

    /**
     * 暂停播放
     */
    public void pause() {

        if(mVideoPlayState == VideoPlayState.STOP) {
            return;
        }
        setVideoPlayState(VideoPlayState.PAUSE);
        mPlayer.pause();
    }

    /**
     * 开始播放
     */
    public void play() {

        setVideoPlayState(VideoPlayState.PLAY);
        mPlayer.start();
    }

    /**
     * 播放完成
     */
    public void finish() {

        mPlayer.seekTo(mPlayer.getDuration());
        mPlayer.pause();
        setVideoPlayState(VideoPlayState.FINISH);
    }

    /**
     * 为播放器添加监听
     */
    private void addVideoPlayListener() {

        mPlayer.setOnPreparedListener(mOnPreparedListener);
        mPlayer.setOnCompletionListener(mOnCompletionListener);
        mPlayer.setOnErrorListener(mErrorListener);
        mPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);//缓冲监听
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setScreenOnWhilePlaying(true);
    }

    /**
     * 加载准备指定的视频
     *
     * @param videoUrl
     */
    private void loadVideo(String videoUrl) {

        try {
            mPlayer.setSurface(mSurface);
            if(mVideoPlayState == VideoPlayState.STOP ||
                mVideoPlayState == VideoPlayState.PREPARE_LOAD) {
                setVideoPlayState(VideoPlayState.LOADING);
                mPlayer.setDataSource(videoUrl);
                mPlayer.prepareAsync();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新播放进度
     */
    private void updatePlayProgress() {

        try {
            int duration = mPlayer.getDuration();
            int playTime = mPlayer.getCurrentPosition();
            mVideoPlayerControllerView.setVideoDuration(duration);
            mVideoPlayerControllerView.setVideoPlayTime(playTime);
            mVideoPlayerControllerView.setSecondaryProgress(mSecProgress);
        } catch(Exception e) {
            Log.d(TAG, "update play progress failure", e);
        }

        if(mVideoPlayState == VideoPlayState.PLAY) {

            postDelayed(new Runnable() {

                @Override
                public void run() {

                    updatePlayProgress();
                }
            }, 1000);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {

        mSurface = new Surface(surfaceTexture);
        loadVideo(mVideoUrl);
    }

    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {

            play();
        }
    };

    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            if (getWindowToken() != null) {
                String message = "播放失败";

                Log.e("mErrorListener", message);
                onDestroy();
            }
            return true;
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {

            mSecProgress = percent;
        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {

            //播放结束
            if(getPlayScreenState() == PlayScreenState.FULL_SCREEN && mExitFullScreenListener != null) {//全屏播放结束
                mExitFullScreenListener.exitFullScreen();
            } if(getPlayScreenState() == PlayScreenState.SMALL) {//小窗口播放结束
                VideoPlayerHelper.getInstance().stop();
            } else {
                finish();
            }
        }
    };

    private VideoPlayerControllerView.VideoControlListener mVideoControlListener =
            new VideoPlayerControllerView.VideoControlListener() {

        @Override
        public void onProgressChanged(int seekTime) {

            mPlayer.seekTo(seekTime);
            updatePlayProgress();
        }

        @Override
        public void fullScreen() {

            VideoPlayerHelper.getInstance().gotoFullScreen(getContext());
        }

        @Override
        public void exitFullScreen() {

            if(mExitFullScreenListener != null) {
                mExitFullScreenListener.exitFullScreen();
            }
        }

        @Override
        public void onControllerShow() {

            if(getPlayScreenState() == PlayScreenState.FULL_SCREEN && mVideoChangeProgressView.isShown()) {
                //当全屏进行手势操作时不显示播放暂停按钮
                hidePlayButtonIfNeed();
            } else {
                showPlayButtonIfNeed();
            }
        }

        @Override
        public void onControllerHide() {

            if(mVideoPlayState == VideoPlayState.PLAY) {
                //播放状态下，底部控制器隐藏时同时隐藏播放器上的暂停按钮
                hidePlayButtonIfNeed();
            }
        }
    };

    public void onDestroy() {

        if(mVideoPlayState == VideoPlayState.STOP) {
            return;
        }
        setVideoPlayState(VideoPlayState.STOP);
        mVideoPlayerControllerView.onDestroy();
        if(mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        if(mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        mVideoContainer.removeView(mTextureView);
        mTextureView = null;
        ((ViewGroup) getParent()).removeView(this);
        VideoPlayerHelper.getInstance().clear();
    }

    public VideoPlayState getVideoPlayState() {

        return mVideoPlayState;
    }

    private PlayScreenState getScreenState() {

        return mVideoPlayerControllerView.getPlayScreenState();
    }

    /**---------------- 界面UI控制 ----------------------**/
    /**
     * 显示控制条
     */
    private void showControllerViewIfNeed() {

        if(mVideoPlayerControllerView.getVisibility() == GONE) {
            mVideoPlayerControllerView.setVisibility(VISIBLE);
        }
    }

    /**
     * 显示加载进度条
     */
    private void showLoadingBarIfNeed() {

        if(mProgressBar.getVisibility() == GONE) {
            mProgressBar.setVisibility(VISIBLE);
        }
    }

    /**
     * 隐藏加载加载进度条
     */
    private void hideLoadingBarIfNeed() {

        if(mProgressBar.getVisibility() == VISIBLE) {
            mProgressBar.setVisibility(GONE);
        }
    }

    /**
     * 显示播放暂停按钮
     */
    private void showPlayButtonIfNeed() {

        if(mVideoPlayButton.getVisibility() == GONE) {
            mVideoPlayButton.setVisibility(VISIBLE);
        }
    }

    /**
     * 隐藏播放暂停按钮
     */
    private void hidePlayButtonIfNeed() {

        if(mVideoPlayButton.getVisibility() == VISIBLE) {
            mVideoPlayButton.setVisibility(GONE);
        }
    }

    private void setVideoPlayState(VideoPlayState state) {
        mVideoPlayState = state;
        switch (state) {
            case STOP:
                hideLoadingBarIfNeed();
                updatePlayButtonIcon();//更新播放按钮的图标
                hidePlayButtonIfNeed();
                setPlayScreenState(PlayScreenState.NORMAL);
                break;
            case PREPARE_LOAD:
            case LOADING:
                showLoadingBarIfNeed();
                break;
            case PAUSE:
                showPlayButtonIfNeed();
                updatePlayButtonIcon();//更新播放按钮的图标
                mVideoPlayerControllerView.show();
                break;
            case PLAY:
                showControllerViewIfNeed();
                updatePlayProgress();
                hideLoadingBarIfNeed();
                hidePlayButtonIfNeed();
                updatePlayButtonIcon();//更新播放按钮的图标
                mVideoPlayerControllerView.hide();
                break;
            case FINISH:
                showControllerViewIfNeed();
                updatePlayProgress();
                mVideoPlayerControllerView.show();
                updatePlayButtonIcon();//更新播放按钮的图标
                break;
        }
    }

    /**
     * 更新播放按钮的图标
     */
    public void updatePlayButtonIcon() {

        int resId = R.drawable.ic_play;
        switch (mVideoPlayState) {
            case PLAY:
                if(getPlayScreenState() == PlayScreenState.FULL_SCREEN) {
                    resId = R.drawable.ic_pause_fullscreen;
                } else {
                    resId = R.drawable.ic_pause;
                }
                break;
            case STOP:
            case PAUSE:
            case FINISH:
                if(getPlayScreenState() == PlayScreenState.FULL_SCREEN) {
                    resId = R.drawable.ic_play_fullscreen;
                } else {
                    resId = R.drawable.ic_play;
                }
                break;
        }
        mVideoPlayButton.setImageResource(resId);
    }

    public void setPlayScreenState(PlayScreenState state) {

        mVideoPlayerControllerView.setPlayScreenState(state);
    }

    public PlayScreenState getPlayScreenState() {

        return mVideoPlayerControllerView.getPlayScreenState();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public interface ExitFullScreenListener {

        void exitFullScreen();
    }

    public void setExitFullScreenListener(ExitFullScreenListener exitFullScreenListener) {

        mExitFullScreenListener = exitFullScreenListener;
    }

    /**------------- 全屏播放手势操作相关 --------------**/
    private class VideoOnTouchListener implements OnTouchListener {

        private static final int TOTAL_PERCENT = 100;
        private static final int ONE_SECOND = 1000;
        private static final int VOLUME_STEP = 1;
        private static final float BRIGHTNESS_STEP = 0.08f;
        private static final float MAX_BRIGHTNESS = 1.0f;
        private int mTouchSlop = 0;
        private int mWidth = 0;
        private int mHeight = 0;
        private AudioManager mAudioManager;
        private int mMaxVolume;//最大音量
        private float mVolumeDistance = 0;//调节音量滑动的距离阀值
        private float mBrightnessDistance = 0;//调节亮度滑动的距离阀值

        private VideoOnTouchListener() {
            mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
            mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

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

            return downX > mWidth / 2 && e2.getRawX() > mWidth / 2;
        }

        private boolean isScrollVerticalLeft(float downX, MotionEvent e2) {

            return downX < mWidth / 2 && e2.getRawX() < mWidth / 2;
        }

        float mDownX, mDownY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if(!(getPlayScreenState() == PlayScreenState.FULL_SCREEN)) {
                return false;
            }
            if(mWidth == 0) {
                mWidth = ((Activity)getContext()).getWindowManager().getDefaultDisplay().getWidth();
                mHeight = ((Activity)getContext()).getWindowManager().getDefaultDisplay().getHeight();
                mVolumeDistance = mHeight / 3.0f / mMaxVolume;
                mBrightnessDistance = mHeight / 3.0f / (MAX_BRIGHTNESS / BRIGHTNESS_STEP);
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDownX = event.getRawX();
                    mDownY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(isFlingLeft(mDownX, mDownY, event)) {//向左滑，退后
                        videoSeek(false);
                        mDownX = event.getRawX();
                        mDownY = event.getRawY();
                    } else if(isFlingRight(mDownX, mDownY, event)) {//向右滑，快进
                        videoSeek(true);
                        mDownX = event.getRawX();
                        mDownY = event.getRawY();
                    } else if(isScrollVertical(mDownX, mDownY, event)) {//垂直方向滑
                        if(isScrollVerticalRight(mDownX, event)) {//屏幕右边上下滑
                            if(Math.abs(event.getRawY() - mDownY) >= mVolumeDistance) {
                                changeVideoVolume(event.getRawY() < mDownY);
                                mDownX = event.getRawX();
                                mDownY = event.getRawY();
                            }
                        } else if(isScrollVerticalLeft(mDownX, event)) {//屏幕左边上下滑
                            if(Math.abs(event.getRawY() - mDownY) >= mBrightnessDistance) {
                                changeBrightness(event.getRawY() < mDownY);
                                mDownX = event.getRawX();
                                mDownY = event.getRawY();
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return false;
        }

        /**
         * 调整视频音量大小
         *
         * @param isTurnUp    是否调大音量
         */
        private void changeVideoVolume(boolean isTurnUp) {

            if(mVideoPlayState != VideoPlayState.PLAY &&
                    mVideoPlayState != VideoPlayState.PAUSE) {//进入播放
                return;
            }
            removeCallbacks(mVolumeRunnable);
            if(mVideoVolumeView.getVisibility() == GONE) {
                mVideoVolumeView.setVisibility(VISIBLE);
            }
            int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if(isTurnUp) {
                volume = volume + VOLUME_STEP >= mMaxVolume ? mMaxVolume : volume + VOLUME_STEP;
            } else {
                volume = volume - VOLUME_STEP > 0 ? volume - VOLUME_STEP : 0;
            }
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
            mVideoVolumeProgress.setProgress((int)(volume * 1.0 / mMaxVolume * TOTAL_PERCENT + 0.5f));
            postDelayed(mVolumeRunnable, ONE_SECOND);
        }

        Runnable mVolumeRunnable = new Runnable() {
            @Override
            public void run() {

                mVideoVolumeView.setVisibility(GONE);
            }
        };

        /**
         * 调整屏幕亮度
         *
         * @param isDodge  是否调亮
         */
        private void changeBrightness(boolean isDodge) {

            if(mVideoPlayState != VideoPlayState.PLAY &&
                    mVideoPlayState != VideoPlayState.PAUSE) {//进入播放
                return;
            }
            removeCallbacks(mBrightnessRunnable);
            if(mVideoBrightnessView.getVisibility() == GONE) {
                mVideoBrightnessView.setVisibility(VISIBLE);
            }
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
            postDelayed(mBrightnessRunnable, ONE_SECOND);
        }

        Runnable mBrightnessRunnable = new Runnable() {
            @Override
            public void run() {

                mVideoBrightnessView.setVisibility(GONE);
            }
        };

        /**
         * 视频前进后退
         * @param isForward
         */
        private void videoSeek(boolean isForward) {

            if(mVideoPlayState != VideoPlayState.PLAY &&
                    mVideoPlayState != VideoPlayState.PAUSE) {//进入播放
                return;
            }
            try {
                if(mPlayer != null) {
                    if(mVideoChangeProgressView.getVisibility() == GONE) {
                        mVideoPlayerControllerView.show();
                        mVideoChangeProgressView.setVisibility(VISIBLE);
                    }
                    removeCallbacks(mVideoSeekRunnable);
                    int duration = mPlayer.getDuration();//总时长
                    int step = ONE_SECOND;//每次前进后退1秒
                    int current = mPlayer.getCurrentPosition();//当前播放时长
                    if(isForward) {//前进
                        mVideoChangeProgressIcon.setImageResource(R.drawable.ic_fast_forward);
                        current = current + step >= duration ? duration : current + step;
                    } else {
                        mVideoChangeProgressIcon.setImageResource(R.drawable.ic_fast_back);
                        current = current - step <= 0 ? 0 : current - step;
                    }
                    mPlayer.seekTo(current);
                    updatePlayProgress();
                    mVideoChangeProgressCurrPro.setText(mVideoPlayerControllerView.
                            formatVideoTimeLength((int) (current / ONE_SECOND + 0.5f)));
                    mVideoChangeProgressTotal.setText("/" + mVideoPlayerControllerView.
                            formatVideoTimeLength((int) (duration / ONE_SECOND + 0.5f)));
                    mVideoChangeProgressBar.setProgress((int) (current * 1.0f / duration * TOTAL_PERCENT + 0.5f));
                    postDelayed(mVideoSeekRunnable, ONE_SECOND);
                }
            } catch(Exception e) {
                Log.d(TAG, "video forward and backward error", e);
            }
        }

        Runnable mVideoSeekRunnable = new Runnable() {
            @Override
            public void run() {

                mVideoChangeProgressView.setVisibility(GONE);
                mVideoPlayerControllerView.hide();
            }
        };
    }
}
