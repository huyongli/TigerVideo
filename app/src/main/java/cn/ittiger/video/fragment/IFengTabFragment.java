package cn.ittiger.video.fragment;

import cn.ittiger.video.R;
import cn.ittiger.video.bean.IFengInfo;
import cn.ittiger.video.factory.FragmentFactory;
import cn.ittiger.video.factory.RetrofitFactory;
import cn.ittiger.video.http.DataType;
import cn.ittiger.video.util.DBManager;
import cn.ittiger.video.util.DataKeeper;
import retrofit2.Call;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public class IFengTabFragment extends VideoFragment {
    private int mTabId;
    private static final int REFRESH_SIZE = 6;
    private static final int LOADMORE_SIZE = 20;

    @Override
    public View getContentView(LayoutInflater inflater, @Nullable Bundle savedInstanceState) {

        mTabId = getArguments().getInt(FragmentFactory.KEY_BUNDLE_TAB_ID);
        return super.getContentView(inflater, savedInstanceState);
    }

    @Override
    public Call<String> getHttpCall(int curPage) {

        DataKeeper.saveCurrentTabId(mTabId);
        if(curPage == 1) {
            return RetrofitFactory.getIFengVideoService().refreshVideos(mTabId, REFRESH_SIZE, String.valueOf(System.currentTimeMillis()));
        } else if(curPage == 2){
            return RetrofitFactory.getIFengVideoService().refreshVideos(mTabId, LOADMORE_SIZE, "");
        } else {
            IFengInfo info = DBManager.getInstance().getSQLiteDB().queryOne(IFengInfo.class, "tabId=?", new String[]{String.valueOf(mTabId)});
            return RetrofitFactory.getIFengVideoService().loadMoreVideos(mTabId, LOADMORE_SIZE, info.getItemId());
        }
    }

    @Override
    public DataType getType() {

        return DataType.IFENG;
    }

    @Override
    public int getName() {

        return R.string.ifeng_video;
    }

    @Override
    public boolean isInitRefreshEnable() {

        return false;
    }

    @Override
    public boolean isDelayRefreshEnable() {

        return true;
    }
}
