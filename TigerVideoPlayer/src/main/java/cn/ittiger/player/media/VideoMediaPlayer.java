package cn.ittiger.player.media;

import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import android.view.TextureView;

import cn.ittiger.player.PlayerManager;
import cn.ittiger.player.state.PlayState;

/**
 * 继承此类时，如果想要获取当前正在播放的视频url，请用如下方式获取：
 * {@link PlayerManager#getVideoUrl()}
 * 因为此处的{@link #mUrl} 在开启缓存时是视频的缓存代理地址，不是用户播放视频时所传入的地址
 *
 * @author: laohu on 2017/9/9
 * @site: http://ittiger.cn
 */
public class VideoMediaPlayer extends AbsSimplePlayer implements
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnBufferingUpdateListener,
    MediaPlayer.OnSeekCompleteListener,
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnInfoListener {
    private static final String TAG = "VideoMediaPlayer";
    private static final int MSG_PREPARE = 1;
    private static final int MSG_RELEASE = 2;
    protected MediaPlayer mMediaPlayer;
    private HandlerThread mMediaHandlerThread;
    private MediaHandler mMediaHandler;
    protected int mState = PlayState.STATE_NORMAL;
    private String mUrl;

    public VideoMediaPlayer() {

        mMediaPlayer = new MediaPlayer();
        mMediaHandlerThread = new HandlerThread(TAG);
        mMediaHandlerThread.start();
        mMediaHandler = new MediaHandler(mMediaHandlerThread.getLooper());
    }

    @Override
    public void setTextureView(TextureView textureView) {

        if(textureView == null && mSurfaceTexture != null) {
            mSurfaceTexture.release();
        }
        super.setTextureView(textureView);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        try {
            mMediaPlayer.setSurface(new Surface(surface));
        } catch (Exception e) {

        }
        super.onSurfaceTextureAvailable(surface, width, height);
    }

    class MediaHandler extends Handler {

        public MediaHandler(Looper looper) {

            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_RELEASE:
                    mMediaPlayer.release();
                    break;
                case MSG_PREPARE:
                    try {
                        mMediaPlayer.release();
                        mMediaPlayer = new MediaPlayer();
                        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mMediaPlayer.setOnPreparedListener(VideoMediaPlayer.this);
                        mMediaPlayer.setOnCompletionListener(VideoMediaPlayer.this);
                        mMediaPlayer.setOnBufferingUpdateListener(VideoMediaPlayer.this);
                        mMediaPlayer.setScreenOnWhilePlaying(true);
                        mMediaPlayer.setOnSeekCompleteListener(VideoMediaPlayer.this);
                        mMediaPlayer.setOnErrorListener(VideoMediaPlayer.this);
                        mMediaPlayer.setOnInfoListener(VideoMediaPlayer.this);
                        mMediaPlayer.setDataSource(mUrl);
                        mMediaPlayer.prepareAsync();
                        mMediaPlayer.setSurface(new Surface(mSurfaceTexture));
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    @Override
    protected void prepare() {

        mMediaHandler.obtainMessage(MSG_PREPARE).sendToTarget();
    }

    @Override
    public void start(String url) {

        mUrl = url;
    }

    @Override
    public void play() {

        mMediaPlayer.start();
    }

    @Override
    public void pause() {

        if(getState() == PlayState.STATE_PLAYING) {
            mMediaPlayer.pause();
        }
    }

    @Override
    public void stop() {

        mMediaHandler.obtainMessage(MSG_RELEASE).sendToTarget();
    }

    @Override
    public void release() {

        mMediaHandler.obtainMessage(MSG_RELEASE).sendToTarget();
    }

    @Override
    public void setState(int state) {

        mState = state;
    }

    @Override
    public int getState() {

        return mState;
    }

    @Override
    public int getCurrentPosition() {

        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {

        return mMediaPlayer.getDuration();
    }

    @Override
    public void seekTo(int position) {

        mMediaPlayer.seekTo(position);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

        if(mPlayCallback != null && isPlaying()) {
            mPlayCallback.onPlayStateChanged(PlayState.STATE_PLAYING);
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        if(mPlayCallback != null) {
            mPlayCallback.onComplete();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        if(mPlayCallback != null) {
            mPlayCallback.onError("Play error, what=" + what + ", extra=" + extra);
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {

        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        if(mPlayCallback != null) {
            mPlayCallback.onDurationChanged(mp.getDuration());
            mPlayCallback.onPlayStateChanged(PlayState.STATE_PLAYING);
        }
        play();
    }
}
