package cn.ittiger.video.fragment;

import cn.ittiger.video.R;
import cn.ittiger.video.presenter.IFengTabPresenter;
import cn.ittiger.video.presenter.VideoTabPresenter;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public class IFengTabFragment extends BaseTabFragment {

    @Override
    public VideoTabPresenter createPresenter() {

        return new IFengTabPresenter();
    }

    @Override
    public int getName() {

        return R.string.ifeng_video;
    }
}
