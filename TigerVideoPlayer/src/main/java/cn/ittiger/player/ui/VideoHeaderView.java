package cn.ittiger.player.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.ittiger.player.R;
import cn.ittiger.player.listener.FullScreenToggleListener;
import cn.ittiger.player.state.ScreenState;
import cn.ittiger.player.util.Utils;

/**
 * 视频头部显示视图
 * @author: ylhu
 * @time: 2017/12/15
 */
public class VideoHeaderView extends LinearLayout implements View.OnClickListener {
    /**
     * 全屏播放时的返回按钮
     */
    protected ImageView mVideoFullScreenBackView;
    /**
     * 视频标题
     */
    protected TextView mVideoTitleView;

    private FullScreenToggleListener mFullScreenToggleListener;
    /**
     * 正常状态下的标题是否显示
     */
    protected boolean mNormalStateShowTitle = true;

    public VideoHeaderView(Context context) {

        super(context);
        initWidgetView(context);
    }

    public VideoHeaderView(Context context, @Nullable AttributeSet attrs) {

        super(context, attrs);
        initWidgetView(context);
    }

    public VideoHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        initWidgetView(context);
    }

    protected void initWidgetView(Context context) {

        setOrientation(HORIZONTAL);
        setVisibility(GONE);
        inflate(context, getVideoTitleViewResLayoutId(), this);
        mVideoFullScreenBackView = (ImageView) findViewById(R.id.vp_video_fullScreen_back);
        mVideoTitleView = (TextView) findViewById(R.id.vp_video_title);

        mVideoFullScreenBackView.setOnClickListener(this);
    }

    protected int getVideoTitleViewResLayoutId() {

        return R.layout.vp_layout_video_header;
    }

    @Override
    public void onClick(View v) {

        if(mFullScreenToggleListener != null) {
            mFullScreenToggleListener.onExitFullScreen();
        }
    }

    public void setTitle(CharSequence title) {

        if(!TextUtils.isEmpty(title)) {
            mVideoTitleView.setText(title);
        }
    }

    public void setFullScreenToggleListener(FullScreenToggleListener fullScreenToggleListener) {

        mFullScreenToggleListener = fullScreenToggleListener;
    }

    /**
     * 修改当前视图的状态
     * @param screenState
     * @param isShow
     */
    public void onChangeVideoHeaderViewState(int screenState, boolean isShow) {

        if(isShow == false) {
            Utils.hideViewIfNeed(this);
            return;
        }
        if(ScreenState.isFullScreen(screenState)) {
            Utils.showViewIfNeed(this);
            Utils.showViewIfNeed(mVideoFullScreenBackView);
        } else if(ScreenState.isNormal(screenState)) {
            if(mNormalStateShowTitle) {
                Utils.showViewIfNeed(this);
                Utils.hideViewIfNeed(mVideoFullScreenBackView);
            } else {
                Utils.hideViewIfNeed(this);
            }
        } else {
            //小窗口播放隐藏视频头部视图
            Utils.hideViewIfNeed(this);
        }
    }

    public void toggleFullScreenBackViewVisibility(boolean isShow) {

        if(isShow) {
            Utils.showViewIfNeed(mVideoFullScreenBackView);
        } else {
            Utils.hideViewIfNeed(mVideoFullScreenBackView);
        }
    }
}
