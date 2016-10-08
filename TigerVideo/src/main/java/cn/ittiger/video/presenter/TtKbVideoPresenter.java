package cn.ittiger.video.presenter;

import cn.ittiger.video.bean.TtKb;
import cn.ittiger.video.factory.RetrofitFactory;
import cn.ittiger.video.http.DataType;
import cn.ittiger.video.util.DBManager;
import rx.Observable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public class TtKbVideoPresenter extends VideoPresenter {
    private int mRefreshPageNum = 0;//刷新页

    @Override
    public Observable<String> getHttpCallObservable(int curPage) {

        String startKey = "";
        String newKey = "";

        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("categoryId", "799999");
        if(curPage == 1) {//刷新
            mRefreshPageNum += -1;
            fieldMap.put("count", "10");
            fieldMap.put("pgnum", String.valueOf(mRefreshPageNum));
            TtKb ttKb = DBManager.getInstance().getSQLiteDB().queryOne(TtKb.class, "isRefresh=?", new String[]{"1"});
            if(ttKb != null) {
                newKey = ttKb.getNewkey();
                startKey = ttKb.getEndkey();
            }
        } else {
            fieldMap.put("count", "20");
            fieldMap.put("pgnum", String.valueOf(curPage));
            TtKb ttKb = DBManager.getInstance().getSQLiteDB().queryOne(TtKb.class, "isRefresh=?", new String[]{"0"});
            if(ttKb != null) {
                startKey = ttKb.getEndkey();
            }
        }
        fieldMap.put("startkey", startKey);
        fieldMap.put("newkey", newKey);
        fieldMap.put("param", "TouTiaoKuaiBao%09TTKBAndroid%09860582034297685%09huawei160918%09TTKB%091.4.0%09Android5.0.2%09null%09null%09PLK-AL10");

        return RetrofitFactory.getTtKbVideoService().getVideos(fieldMap);
    }

    @Override
    public DataType getType() {

        return DataType.TTKB;
    }
}
