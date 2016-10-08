package cn.ittiger.video.fragment;

import cn.ittiger.video.R;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public class AboutFragment extends BaseFragment {

    @Override
    public View getContentView(LayoutInflater inflater, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_about, null);
        return view;
    }

    @Override
    public int getName() {

        return R.string.about;
    }

    @Override
    public boolean isInitRefreshEnable() {

        return false;
    }
}
