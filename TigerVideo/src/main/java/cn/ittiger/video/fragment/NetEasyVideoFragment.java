package cn.ittiger.video.fragment;

import cn.ittiger.video.R;
import cn.ittiger.video.presenter.NetEasyVideoPresenter;
import cn.ittiger.video.presenter.VideoPresenter;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public class NetEasyVideoFragment extends VideoFragment {

    @Override
    public VideoPresenter createPresenter() {

        return new NetEasyVideoPresenter();
    }

    @Override
    public int getName() {

        return R.string.net_easy_video;
    }
}
