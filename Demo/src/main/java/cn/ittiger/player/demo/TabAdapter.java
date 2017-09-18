package cn.ittiger.player.demo;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * 分类数据适配器
 * @author laohu
 */
public class TabAdapter extends FragmentPagerAdapter {

    private List<String> mList;

    public TabAdapter(FragmentManager fm, List<String> list) {

        super(fm);
        this.mList = list;
    }

    @Override
    public Fragment getItem(int position) {

        return new VideoFragment();
    }

    @Override
    public CharSequence getPageTitle(int position) {

        return mList.get(position);
    }

    @Override
    public int getCount() {

        return mList.size();
    }
}
