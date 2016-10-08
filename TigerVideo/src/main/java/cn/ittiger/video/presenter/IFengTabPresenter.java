package cn.ittiger.video.presenter;

import cn.ittiger.video.factory.RetrofitFactory;
import cn.ittiger.video.http.DataType;
import rx.Observable;

/**
 * @author: laohu on 2016/10/8
 * @site: http://ittiger.cn
 */
public class IFengTabPresenter extends VideoTabPresenter {

    @Override
    public Observable<String> getHttpCallObservable() {

        return RetrofitFactory.getIFengVideoService().getTabs();
    }

    @Override
    public DataType getType() {

        return DataType.IFENG;
    }
}
