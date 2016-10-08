package cn.ittiger.video.fragment;

import cn.ittiger.video.R;
import cn.ittiger.video.factory.RetrofitFactory;
import cn.ittiger.video.http.DataType;
import cn.ittiger.video.presenter.IFengTabPresenter;
import cn.ittiger.video.presenter.VideoTabPresenter;
import retrofit2.Call;

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
