package cn.ittiger.video.presenter;

import cn.ittiger.video.factory.RetrofitFactory;
import cn.ittiger.video.http.DataType;
import rx.Observable;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public class NetEasyVideoPresenter extends VideoPresenter {
    private static final int PAGE_SIZE = 20;

    @Override
    public Observable<String> getHttpCallObservable(int curPage) {

        int offset = (curPage - 1) * PAGE_SIZE;
        return RetrofitFactory.getNetEasyVideoService().getVideos(PAGE_SIZE, offset);
    }

    @Override
    public DataType getType() {

        return DataType.NET_EASY;
    }
}
