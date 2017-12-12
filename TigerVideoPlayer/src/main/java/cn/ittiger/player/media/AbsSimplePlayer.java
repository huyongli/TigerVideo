package cn.ittiger.player.media;

import android.graphics.SurfaceTexture;
import android.view.TextureView;

import cn.ittiger.player.state.PlayState;

/**
 * @author: laohu on 2017/9/10
 * @site: http://ittiger.cn
 */
public abstract class AbsSimplePlayer implements IPlayer, TextureView.SurfaceTextureListener {
    protected TextureView mTextureView;
    protected SurfaceTexture mSurfaceTexture;
    protected PlayCallback mPlayCallback;

    /**
     * 此函数中给播放器设置数据源开始loading视频数据
     *
     * 此时TextureView已经初始化完成
     */
    protected abstract void prepare();

    @Override
    public void setPlayCallback(PlayCallback playCallback) {

        mPlayCallback = playCallback;
    }

    @Override
    public void setTextureView(TextureView textureView) {

        if(mTextureView != null) {
            mTextureView.setSurfaceTextureListener(null);
        }
        mSurfaceTexture = null;
        mTextureView = textureView;
        if(textureView != null) {
            mTextureView.setSurfaceTextureListener(this);
        }
    }

    @Override
    public boolean isPlaying() {

        return (getState() == PlayState.STATE_PLAYING ||
                        getState() == PlayState.STATE_PLAYING_BUFFERING_START) &&
                getCurrentPosition() < getDuration();
    }

    /**---------- TextureView.SurfaceTextureListener ------------**/
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        if(mSurfaceTexture == null && (getState() == PlayState.STATE_NORMAL || getState() == PlayState.STATE_LOADING)) {
            prepare();
        }
        mSurfaceTexture = surface;
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

    public TextureView getTextureView() {

        return mTextureView;
    }
}
