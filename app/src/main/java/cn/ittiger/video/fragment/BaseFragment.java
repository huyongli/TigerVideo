package cn.ittiger.video.fragment;

import cn.ittiger.video.R;
import cn.ittiger.video.util.PageLoadingHelper;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public abstract class BaseFragment extends Fragment {

    protected Context mContext;
    PageLoadingHelper mPageLoadingHelper;
    private boolean mIsLoaded = false;
    private Handler mHandler = new Handler();

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.base_fragment_layout, container, false);

        view.addView(getContentView(inflater, savedInstanceState), 0);

        mPageLoadingHelper = new PageLoadingHelper(view);
        mPageLoadingHelper.setOnLoadingClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                clickToRefresh();
            }
        });
        if(isInitRefreshEnable() && isDelayRefreshEnable() == false) {
            clickToRefresh();
        }
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && isInitRefreshEnable() == false &&
            isDelayRefreshEnable() && mIsLoaded == false) {
            delayLoad();
        }
    }

    private void delayLoad() {

        if(mPageLoadingHelper != null) {
            clickToRefresh();
            mIsLoaded = true;
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    delayLoad();
                }
            }, 100);
        }
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
        mIsLoaded = false;
    }

    /**
     * Fragment数据视图
     * @param inflater
     * @param savedInstanceState
     * @return
     */
    public abstract View getContentView(LayoutInflater inflater, @Nullable Bundle savedInstanceState);

    /**
     * 点击刷新加载
     */
    private void clickToRefresh() {

        startRefresh();
        refreshData();
    }

    public boolean isInitRefreshEnable() {

        return true;
    }

    public boolean isDelayRefreshEnable() {

        return false;
    }

    public abstract int getName();

    /**
     * 初始化数据
     */
    public void refreshData() {}

    /**
     * 开始加载数据
     */
    public void startRefresh() {

        mPageLoadingHelper.startRefresh();
    }

    /**
     * 加载失败
     */
    public void refreshFailed() {

        mPageLoadingHelper.refreshFailed();
    }

    /**
     * 加载成功
     */
    public void refreshSuccess() {

        mPageLoadingHelper.refreshSuccess();
    }
}
