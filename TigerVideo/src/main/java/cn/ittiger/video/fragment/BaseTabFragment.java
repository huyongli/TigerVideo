package cn.ittiger.video.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ittiger.player.PlayerManager;
import cn.ittiger.video.R;
import cn.ittiger.video.adapter.TabAdapter;
import cn.ittiger.video.bean.VideoTabData;
import cn.ittiger.video.mvpview.VideoTabMvpView;
import cn.ittiger.video.presenter.VideoTabPresenter;

import java.util.List;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public abstract class BaseTabFragment extends
        BaseFragment<LinearLayout, List<VideoTabData>, VideoTabMvpView, VideoTabPresenter>
        implements VideoTabMvpView {

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

                PlayerManager.getInstance().stop();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return view;
    }

    @Override
    public void setData(List<VideoTabData> tabs) {

        mTabAdapter = new TabAdapter(getChildFragmentManager(), tabs, presenter.getType());
        mViewPager.setAdapter(mTabAdapter);
        mTabPageIndicator.setVisibility(View.VISIBLE);
        mTabPageIndicator.setupWithViewPager(mViewPager);
    }

    @Override
    public void loadData(boolean pullToRefresh) {

        showLoading(pullToRefresh);
        presenter.queryVideoTab(pullToRefresh);
    }
}
