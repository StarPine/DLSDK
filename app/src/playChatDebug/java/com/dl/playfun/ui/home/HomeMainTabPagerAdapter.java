package com.dl.playfun.ui.home;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.ui.home.homelist.HomeListFragment;

/**
 * @author wulei
 */
public class HomeMainTabPagerAdapter extends FragmentStatePagerAdapter {

    @StringRes
    public static final int[] TAB_FEMALE_TITLES = new int[]{R.string.tab_female_1, R.string.tab_female_2, R.string.tab_female_3};
    @StringRes
    public static final int[] TAB_MALE_TITLES = new int[]{R.string.tab_male_1, R.string.tab_male_2};

    private final Context mContext;

    private final int gender;

    private final HomeMainViewModel homeMainViewModel;

    public HomeMainTabPagerAdapter(Context context, FragmentManager fm, int gender, HomeMainViewModel homeMainViewModel) {
        super(fm);
        mContext = context;
        this.gender = gender;
        this.homeMainViewModel = homeMainViewModel;
    }

    @Override
    public Fragment getItem(int position) {
        if (gender == 1 && position == 1) {
            HomeListFragment homeListFragment = HomeListFragment.newInstance(4, gender);
            homeListFragment.setHomeMainViewModel(homeMainViewModel);
            return homeListFragment;
        }
        HomeListFragment homeListFragment = HomeListFragment.newInstance(position + 1, gender);
        homeListFragment.setHomeMainViewModel(homeMainViewModel);
        return homeListFragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (gender == 1) {
            if (position == 0) {
                AppContext.instance().logEvent(AppsFlyerEvent.Nearby_1);
            } else if (position == 1) {
                AppContext.instance().logEvent(AppsFlyerEvent.Nearby_VIP_Nearby);
            }
            return StringUtils.getString(TAB_MALE_TITLES[position]);
        } else {
            if (position == 0) {
                AppContext.instance().logEvent(AppsFlyerEvent.Nearby_1);
            } else if (position == 1) {
                AppContext.instance().logEvent(AppsFlyerEvent.Nearby_New);
            } else if (position == 1) {
                AppContext.instance().logEvent(AppsFlyerEvent.Nearby_Goddess);
            }
            return StringUtils.getString(TAB_FEMALE_TITLES[position]);
        }
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        if (gender == 1) {
            return TAB_MALE_TITLES.length;
        } else {
            return TAB_FEMALE_TITLES.length;
        }
    }
}