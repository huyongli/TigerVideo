package cn.ittiger.video.fragment;

import cn.ittiger.video.R;
import cn.ittiger.video.presenter.TtKbVideoPresenter;
import cn.ittiger.video.presenter.VideoPresenter;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public class TtKbVideoFragment extends VideoFragment {

    @Override
    public VideoPresenter createPresenter() {

        return new TtKbVideoPresenter();
    }

    @Override
    public int getName() {

        return R.string.ttkb_video;
    }
}
