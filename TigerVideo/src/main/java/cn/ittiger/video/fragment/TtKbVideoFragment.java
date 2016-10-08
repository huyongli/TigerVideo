package cn.ittiger.video.fragment;

import cn.ittiger.video.R;
import cn.ittiger.video.bean.TtKb;
import cn.ittiger.video.factory.RetrofitFactory;
import cn.ittiger.video.http.DataType;
import cn.ittiger.video.presenter.TtKbVideoPresenter;
import cn.ittiger.video.presenter.VideoPresenter;
import cn.ittiger.video.util.DBManager;
import retrofit2.Call;

import java.util.HashMap;
import java.util.Map;

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
