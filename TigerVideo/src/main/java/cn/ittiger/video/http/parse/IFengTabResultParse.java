package cn.ittiger.video.http.parse;

import cn.ittiger.video.bean.IFengInfo;
import cn.ittiger.video.bean.VideoData;
import cn.ittiger.video.bean.VideoTabData;
import cn.ittiger.video.http.DataType;
import cn.ittiger.video.util.DataKeeper;
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
public class IFengTabResultParse implements ResultParse {
    private static final String KEY_TAB_LIST = "channelInfo";
    private static final String KEY_TAB_ID = "channelId";
    private static final String KEY_TAB_NAME = "channelName";

    private static final String MEMBER_TYPE = "video";
    private static final String KEY_DATA_LIST = "bodyList";
    private static final String KEY_DATA_ITEMID = "itemId";
    private static final String KEY_DATA_ID = "infoId";
    private static final String KEY_DATA_TITLE = "title";
    private static final String KEY_DATA_TYPE = "memberType";
    private static final String KEY_DATA_VIDEO_ITEM = "memberItem";
    private static final String KEY_DATA_VIDEO_DURATION = "duration";
    private static final String KEY_DATA_VIDEO_IMAGE = "image";
    private static final String KEY_DATA_VIDEO_DATA_LIST = "videoFiles";
    private static final String KEY_DATA_VIDEO_URL = "mediaUrl";

    @Override
    public List<VideoData> parse(JSONObject json) throws JSONException {

        List<VideoData> list = new ArrayList<>();

        JSONArray array = json.getJSONArray(KEY_DATA_LIST);
        JSONObject item;
        for(int i = 0; i < array.length(); i++) {
            item = array.getJSONObject(i);
            String type = item.optString(KEY_DATA_TYPE);
            if(!MEMBER_TYPE.equals(type)) {
                continue;
            }
            VideoData videoData = new VideoData();
            videoData.setDataType(DataType.IFENG.value());
            videoData.setId(item.optString(KEY_DATA_ID));
            videoData.setTitle(item.optString(KEY_DATA_TITLE));

            JSONObject video = item.getJSONObject(KEY_DATA_VIDEO_ITEM);
            videoData.setImageUrl(video.optString(KEY_DATA_VIDEO_IMAGE));
            long duration = video.optLong(KEY_DATA_VIDEO_DURATION);
            videoData.setDuration(Utils.formatTimeLength(duration));
            video = video.getJSONArray(KEY_DATA_VIDEO_DATA_LIST).getJSONObject(1);
            videoData.setVideoUrl(video.optString(KEY_DATA_VIDEO_URL));

            list.add(videoData);

            if(i == array.length() - 1) {
                String itemId = item.optString(KEY_DATA_ITEMID);
                int tabId = DataKeeper.getCurrentTabId();
                IFengInfo.save(new IFengInfo(tabId, itemId));
            }
        }
        return list;
    }

    @Override
    public List<VideoTabData> parseTab(JSONObject json) throws JSONException {

        List<VideoTabData> list = new ArrayList<>();

        JSONArray array = json.getJSONArray(KEY_TAB_LIST);
        JSONObject item;
        for(int i = 0; i < array.length(); i++) {
            item = array.getJSONObject(i);
            int tabId = item.optInt(KEY_TAB_ID);
            String tabName = item.optString(KEY_TAB_NAME);
            list.add(new VideoTabData(tabId, tabName, DataType.IFENG.value()));
        }
        return list;
    }
}
