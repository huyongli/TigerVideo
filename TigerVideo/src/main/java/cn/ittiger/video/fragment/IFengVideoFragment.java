package cn.ittiger.video.fragment;

import cn.ittiger.video.R;
import cn.ittiger.video.factory.FragmentFactory;
import cn.ittiger.video.presenter.IFengVideoPresenter;
import cn.ittiger.video.presenter.VideoPresenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public class IFengVideoFragment extends VideoFragment {
    private int mTabId;

    @Override
    public View getContentView(LayoutInflater inflater, @Nullable Bundle savedInstanceState) {

        mTabId = getArguments().getInt(FragmentFactory.KEY_BUNDLE_TAB_ID);
        return super.getContentView(inflater, savedInstanceState);
    }

    @Override
    public VideoPresenter createPresenter() {

        return new IFengVideoPresenter(mTabId);
    }

    @Override
    public int getName() {

        return R.string.ifeng_video;
    }

    @Override
    public boolean isInitRefreshEnable() {

        return false;
    }

    @Override
    public boolean isDelayRefreshEnable() {

        return true;
    }
}
