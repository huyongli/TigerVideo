package cn.ittiger.video.presenter;

import cn.ittiger.video.bean.VideoTabData;
import cn.ittiger.video.factory.ResultParseFactory;
import cn.ittiger.video.mvpview.VideoTabMvpView;
import cn.ittiger.video.util.DBManager;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;

import java.util.List;

/**
 * @author: laohu on 2016/10/8
 * @site: http://ittiger.cn
 */
public abstract class VideoTabPresenter extends MvpBasePresenter<VideoTabMvpView> implements TypePresenter {

    public void queryVideoTab(final boolean pullToRefresh) {

        Observable.just(getType().value())
                .subscribeOn(Schedulers.io())
                .map(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {

                        String[] whereArgs = {String.valueOf(integer.intValue())};
                        return DBManager.getInstance().getSQLiteDB().queryIfExist(VideoTabData.class, "type=?", whereArgs);
                    }
                })
                .flatMap(new Func1<Boolean, Observable<List<VideoTabData>>>() {
                    @Override
                    public Observable<List<VideoTabData>> call(Boolean aBoolean) {

                        if(aBoolean.booleanValue()) {
                            String[] whereArgs = {String.valueOf(getType().value())};
                            List<VideoTabData> tabs = DBManager.getInstance().getSQLiteDB().query(VideoTabData.class, "type=?", whereArgs);
                            return Observable.just(tabs);
                        }
                        return getHttpCallObservable()
                                .flatMap(new Func1<String, Observable<List<VideoTabData>>>() {
                                    @Override
                                    public Observable<List<VideoTabData>> call(String s) {
                                        List<VideoTabData> tabs = ResultParseFactory.parseTab(s, getType());
                                        if(tabs == null || tabs.size() == 0) {
                                            return Observable.error(new NullPointerException("not load video tab data"));
                                        }
                                        DBManager.getInstance().getSQLiteDB().save(tabs);
                                        return Observable.just(tabs);
                                    }
                                });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<VideoTabData>>() {
                    @Override
                    public void onCompleted() {

                        getView().showContent();
                    }

                    @Override
                    public void onError(Throwable e) {

                        getView().showError(e, pullToRefresh);
                    }

                    @Override
                    public void onNext(List<VideoTabData> tabs) {

                        if(isViewAttached()) {
                            getView().setData(tabs);
                        }
                    }
                });
    }

    public abstract Observable<String> getHttpCallObservable();
}
