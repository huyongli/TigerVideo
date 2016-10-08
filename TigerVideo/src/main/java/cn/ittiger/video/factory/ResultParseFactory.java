package cn.ittiger.video.factory;

import cn.ittiger.video.bean.VideoData;
import cn.ittiger.video.bean.VideoTabData;
import cn.ittiger.video.http.DataType;
import cn.ittiger.video.http.parse.IFengTabResultParse;
import cn.ittiger.video.http.parse.NetEasyResultParse;
import cn.ittiger.video.http.parse.ResultParse;
import cn.ittiger.video.http.parse.TtKbResultParse;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public class ResultParseFactory {

    public static ResultParse create(DataType type) {

        ResultParse parse = null;
        switch (type) {
            case NET_EASY:
                parse = new NetEasyResultParse();
                break;
            case TTKB:
                parse = new TtKbResultParse();
                break;
            case IFENG:
                parse = new IFengTabResultParse();
                break;
        }
        return parse;
    }

    public static List<VideoData> parse(String value, DataType type) {

        try {
            JSONObject json = new JSONObject(value);
            ResultParse parse = create(type);
            return parse.parse(json);
        } catch(Exception e) {
            return new ArrayList<>(0);
        }
    }

    public static List<VideoTabData> parseTab(String value, DataType type) {

        try {
            JSONObject json = new JSONObject(value);
            ResultParse parse = create(type);
            return parse.parseTab(json);
        } catch(Exception e) {
            return new ArrayList<>(0);
        }
    }
}
