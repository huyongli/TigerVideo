package cn.ittiger.player.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import cn.ittiger.player.PlayerManager;

import java.util.Arrays;
import java.util.List;

/**
 * @author: ylhu
 * @time: 17-9-18
 */

public class ViewPagerFragmentActivity extends AppCompatActivity {
    ViewPager mViewPager;
    TabLayout mTabPageIndicator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager_fragment);
        mViewPager = (ViewPager) findViewById(R.id.viewpager_video_tab);
        mTabPageIndicator = (TabLayout) findViewById(R.id.indicator_tab_container);

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

        List<String> tabs = Arrays.asList(getResources().getStringArray(R.array.tabs));
        mViewPager.setAdapter(new TabAdapter(getSupportFragmentManager(), tabs));
        mTabPageIndicator.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        PlayerManager.getInstance().release();
    }
}
