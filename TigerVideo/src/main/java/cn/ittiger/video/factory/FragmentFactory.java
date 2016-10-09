package cn.ittiger.video.factory;

import cn.ittiger.video.fragment.BaseFragment;
import cn.ittiger.video.bean.VideoTabData;
import cn.ittiger.video.fragment.IFengTabFragment;
import cn.ittiger.video.fragment.IFengVideoFragment;
import cn.ittiger.video.fragment.NetEasyVideoFragment;
import cn.ittiger.video.fragment.TtKbVideoFragment;
import cn.ittiger.video.http.DataType;

import android.os.Bundle;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public class FragmentFactory {
    public static final String KEY_BUNDLE_TAB_ID = "tab_id";

    public static final BaseFragment createMainFragment(DataType type) {

        BaseFragment fragment = null;
        switch (type) {
            case NET_EASY:
                fragment = new NetEasyVideoFragment();
                break;
            case TTKB:
                fragment = new TtKbVideoFragment();
                break;
            case IFENG:
                fragment = new IFengTabFragment();
                break;
        }
        return fragment;
    }

    public static final BaseFragment createTabItemFragment(DataType type, VideoTabData tabItem) {

        BaseFragment fragment = null;
        switch (type) {
            case IFENG:
                fragment = new IFengVideoFragment();
                break;
        }

        Bundle bundle = new Bundle();
        bundle.putInt(KEY_BUNDLE_TAB_ID, tabItem.getTabId());
        if(fragment != null) {
            fragment.setArguments(bundle);
        }

        return fragment;
    }
}
