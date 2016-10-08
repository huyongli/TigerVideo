package cn.ittiger.video.http.service;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * http://c.m.163.com/recommend/getChanListNews?channel=T1457068979049&size=20&offset=0&fn=3&passport=h3o88AuDhdH7tlyrE3hlILX2WMCoMqapk08GhEzPqX4%3D&devId=DWT861zlolJo7mHnyynnGA%3D%3D&version=15.0&net=wifi&ts=1474185450&sign=JmMhXTnPo%2BqgTgwyxKstDgS9lmS5Pv%2BUCP5tZ%2FrWevV48ErR02zJ6%2FKXOnxX046I&encryption=1
 *
 * 网易视频
 *
 * @author laohu
 * @site http://ittiger.cn
 */
public interface NetEasyApi {

    @GET("recommend/getChanListNews?channel=T1457068979049&fn=3&passport=h3o88AuDhdH7tlyrE3hlILX2WMCoMqapk08GhEzPqX4%3D&devId=DWT861zlolJo7mHnyynnGA%3D%3D&version=15.0&net=wifi&ts=1474185450&sign=JmMhXTnPo%2BqgTgwyxKstDgS9lmS5Pv%2BUCP5tZ%2FrWevV48ErR02zJ6%2FKXOnxX046I&encryption=1")
    Observable<String> getVideos(@Query("size") int size, @Query("offset") int offset);
}
