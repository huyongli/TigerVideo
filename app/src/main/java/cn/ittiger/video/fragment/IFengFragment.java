package cn.ittiger.video.fragment;

import cn.ittiger.video.R;
import cn.ittiger.video.factory.RetrofitFactory;
import cn.ittiger.video.http.DataType;
import retrofit2.Call;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public class IFengFragment extends BaseTabFragment {

    @Override
    public Call<String> getHttpCall() {

        return RetrofitFactory.getIFengVideoService().getTabs();
    }

    @Override
    public DataType getType() {

        return DataType.IFENG;
    }

    @Override
    public int getName() {

        return R.string.ifeng_video;
    }
}
