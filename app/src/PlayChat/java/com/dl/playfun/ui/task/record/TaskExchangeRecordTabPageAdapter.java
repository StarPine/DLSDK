package com.dl.playfun.ui.task.record;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.ui.task.record.list.ExchangeRecordListFragment;

import org.jetbrains.annotations.NotNull;

/**
 * Author: 彭石林
 * Time: 2021/8/12 12:06
 * Description: This is TaskExchangeRecordTabPageAdapter
 */
public class TaskExchangeRecordTabPageAdapter extends FragmentStatePagerAdapter {

    @StringRes
    public static final int[] TAB_MALE_TITLES = new int[]{R.string.task_exchange_record_fragment_tab, R.string.task_exchange_record_fragment_tab2, R.string.task_exchange_record_fragment_tab3};

    private final Context mContext;

    private final int gender;

    public TaskExchangeRecordTabPageAdapter(FragmentManager fm, Context context, int gender) {
        super(fm);
        mContext = context;
        this.gender = gender;
    }

    @NonNull
    @NotNull
    @Override
    public Fragment getItem(int position) {
        ExchangeRecordListFragment exchangeRecordListFragment = ExchangeRecordListFragment.newInstance(position);
        return exchangeRecordListFragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return StringUtils.getString(TAB_MALE_TITLES[position]);
    }

    @Override
    public int getCount() {
        return TAB_MALE_TITLES.length;
    }
}
