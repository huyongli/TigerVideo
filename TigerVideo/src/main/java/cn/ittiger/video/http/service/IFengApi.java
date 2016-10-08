package cn.ittiger.video.http.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public interface IFengApi {

    @GET("api/channelInfo?platformType=androidPhone&adapterNo=7.0.0&pid=&recommendNo=3,2&positionId=&pageSize=20&protocol=1.0.0")
    Call<String> getTabs();

    @GET("api/homePageList?platformType=androidPhone&isNotModified=0&adapterNo=7.0.0&protocol=1.0.0")
    Call<String> refreshVideos(@Query("channelId") int tabId, @Query("pageSize") int pageSize, @Query("requireTime") String requireTime);

    @GET("api/homePageList?platformType=androidPhone&isNotModified=0&adapterNo=7.0.0&protocol=1.0.0")
    Call<String> loadMoreVideos(@Query("channelId") int tabId, @Query("pageSize") int pageSize, @Query("positionId") String positionId);
}
