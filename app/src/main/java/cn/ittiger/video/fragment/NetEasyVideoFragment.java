package cn.ittiger.video.fragment;

import cn.ittiger.video.R;
import cn.ittiger.video.http.DataType;
import cn.ittiger.video.factory.RetrofitFactory;
import retrofit2.Call;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public class NetEasyVideoFragment extends VideoFragment {
    private static final int PAGE_SIZE = 20;

    @Override
    public Call<String> getHttpCall(int curPage) {

        int offset = (curPage - 1) * PAGE_SIZE;
        return RetrofitFactory.getNetEasyVideoService().getVideos(PAGE_SIZE, offset);
    }

    @Override
    public DataType getType() {

        return DataType.NET_EASY;
    }

    @Override
    public int getName() {

        return R.string.net_easy_video;
    }
}
