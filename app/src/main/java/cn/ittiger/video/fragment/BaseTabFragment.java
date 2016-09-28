package cn.ittiger.video.fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ittiger.video.R;
import cn.ittiger.video.adapter.TabAdapter;
import cn.ittiger.video.bean.VideoTabData;
import cn.ittiger.video.factory.ResultParseFactory;
import cn.ittiger.video.http.DataType;
import cn.ittiger.video.player.VideoPlayerHelper;
import cn.ittiger.video.util.CallbackHandler;
import cn.ittiger.video.util.DBManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;

import java.util.List;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public abstract class BaseTabFragment extends BaseFragment {

    @BindView(R.id.viewpager_video_tab)
    ViewPager mViewPager;
    @BindView(R.id.indicator_tab_container)
    TabLayout mTabPageIndicator;
    private TabAdapter mTabAdapter;

    @Override
    public View getContentView(LayoutInflater inflater, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.base_tab_fragment, null);
        ButterKnife.bind(this, view);

        mTabPageIndicator.setTabMode(TabLayout.MODE_SCROLLABLE);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                VideoPlayerHelper.getInstance().stop();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return view;
    }

    @Override
    public void refreshData() {

        queryTab(new CallbackHandler<List<VideoTabData>>() {

            @Override
            public void callback(List<VideoTabData> baseTabs) {

                if (baseTabs == null || baseTabs.size() == 0) {
                    refreshFailed();
                    return;
                }
                mTabAdapter = new TabAdapter(getChildFragmentManager(), baseTabs, getType());
                mViewPager.setAdapter(mTabAdapter);
                mTabPageIndicator.setVisibility(View.VISIBLE);
                mTabPageIndicator.setupWithViewPager(mViewPager);

                refreshSuccess();
            }
        });
    }

    private void queryTab(final CallbackHandler<List<VideoTabData>> callback) {

        String[] whereArgs = {String.valueOf(getType().value())};
        boolean isExist = DBManager.getInstance().getSQLiteDB().queryIfExist(VideoTabData.class, "type=?", whereArgs);
        if(isExist) {
            List<VideoTabData> tabs = DBManager.getInstance().getSQLiteDB().query(VideoTabData.class, "type=?", whereArgs);
            queryHandler(tabs, callback);
            return;
        }
        Call<String> call = getHttpCall();
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                if (response.isSuccessful()) {
                    List<VideoTabData> tabs = ResultParseFactory.parseTab(response.body(), getType());
                    DBManager.getInstance().getSQLiteDB().save(tabs);
                    queryHandler(tabs, callback);
                } else {
                    onFailure(call, new NullPointerException("not query tabs"));
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

                queryHandler(null, callback);
            }
        });
    }

    private void queryHandler(final List<VideoTabData> datas, final CallbackHandler<List<VideoTabData>> callback) {

        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (callback != null) {
                    callback.callback(datas);
                }
            }
        });
    }

    public abstract Call<String> getHttpCall();

    public abstract DataType getType();
}
