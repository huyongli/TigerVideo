package cn.ittiger.video.http.parse;

import cn.ittiger.video.bean.TtKb;
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
public class TtKbResultParse implements ResultParse {
    private static final String KEY_VIDEO_LIST = "data";
    private static final String KEY_COUNT = "count";
    private static final String KEY_END_KEY = "endkey";
    private static final String KEY_NEW_KEY = "newkey";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "topic";
    private static final String KEY_VIDEO_URL = "video_link";
    private static final String KEY_DURATION = "videoalltime";

    private static final String KEY_IMAGE_ITEM = "lbimg";
    private static final String KEY_IMAGE = "src";


    @Override
    public List<VideoData> parse(JSONObject json) throws JSONException {

        List<VideoData> list = new ArrayList<>();

        int count = json.optInt(KEY_COUNT);
        String newkey = json.optString(KEY_NEW_KEY);
        String endkey = json.optString(KEY_END_KEY);
        TtKb.save(newkey, endkey, count == 10 ? 1 : 0);

        JSONArray videos = json.getJSONArray(KEY_VIDEO_LIST);
        JSONObject item;
        for(int i = 0; i < videos.length(); i++) {
            item = videos.getJSONObject(i);
            VideoData videoData = new VideoData();
            videoData.setDataType(DataType.TTKB.value());
            videoData.setId(item.optString(KEY_ID));
            videoData.setTitle(item.optString(KEY_TITLE));
            videoData.setVideoUrl(item.optString(KEY_VIDEO_URL));

            long duration = item.optLong(KEY_DURATION);
            videoData.setDuration(Utils.formatTimeLength(duration / 1000));

            item = item.getJSONArray(KEY_IMAGE_ITEM).getJSONObject(0);
            videoData.setImageUrl(item.optString(KEY_IMAGE));
            list.add(videoData);
        }
        return list;
    }

    @Override
    public List<VideoTabData> parseTab(JSONObject json) throws JSONException {

        return null;
    }
}
