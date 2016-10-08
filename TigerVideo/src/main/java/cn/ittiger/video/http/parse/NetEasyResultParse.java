package cn.ittiger.video.http.parse;

import cn.ittiger.video.bean.VideoData;
import cn.ittiger.video.bean.VideoTabData;
import cn.ittiger.video.http.DataType;
import cn.ittiger.video.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public class NetEasyResultParse implements ResultParse {
    private static final String KEY_VIDEO_LIST = "视频";
    private static final String KEY_ID = "vid";
    private static final String KEY_IMAGE = "cover";
    private static final String KEY_TITLE = "title";
    private static final String KEY_VIDEO_URL = "mp4_url";
    private static final String KEY_DURATION = "playCount";

    @Override
    public List<VideoData> parse(JSONObject json) throws JSONException {

        List<VideoData> list = new ArrayList<>();

        JSONArray videos = json.getJSONArray(KEY_VIDEO_LIST);
        JSONObject item;
        for(int i = 0; i < videos.length(); i++) {
            item = videos.getJSONObject(i);
            VideoData videoData = new VideoData();
            videoData.setDataType(DataType.NET_EASY.value());
            videoData.setId(item.optString(KEY_ID));
            videoData.setImageUrl(item.optString(KEY_IMAGE));
            videoData.setTitle(item.optString(KEY_TITLE));
            videoData.setVideoUrl(item.optString(KEY_VIDEO_URL));
            long playDuration = item.optLong(KEY_DURATION);
            videoData.setDuration(Utils.formatTimeLength(playDuration));
            list.add(videoData);
        }
        return list;
    }

    @Override
    public List<VideoTabData> parseTab(JSONObject json) throws JSONException {

        return null;
    }
}
