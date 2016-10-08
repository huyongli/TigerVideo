package cn.ittiger.video.http.service;

import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import rx.Observable;

import java.util.Map;

/**
 *
 * http://video.toutiaokuaibao.com/app_video/getvideos
 *
 * 头条快报视频
 *
 * @author laohu
 * @site http://ittiger.cn
 */
public interface TtKbApi {

    @Headers({/*"Content-Type: application/x-www-form-urlencoded;charset=UTF-8",*/
              "Accept-Encoding: gzip",
              "User-Agent: okhttp/3.2.0"})
    @FormUrlEncoded
    @POST("app_video/getvideos")
    Observable<String> getVideos(@FieldMap Map<String, String> fieldMap);
}
