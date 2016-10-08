package cn.ittiger.video.mvpview;

import cn.ittiger.video.bean.VideoData;

import com.hannesdorfmann.mosby.mvp.lce.MvpLceView;

import java.util.List;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public interface VideoMvpView extends MvpLceView<List<VideoData>> {

    void showLoadMoreErrorView();

    void showLoadMoreView();

    void setLoadMoreData(List<VideoData> videos);
}
