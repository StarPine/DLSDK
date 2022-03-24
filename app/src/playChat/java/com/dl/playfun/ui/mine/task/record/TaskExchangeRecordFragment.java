package com.dl.playfun.ui.mine.task.record;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.TaskExchangeRecordFragmentBinding;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.google.android.material.tabs.TabLayout;

/**
 * Author: 彭石林
 * Time: 2021/8/12 11:09
 * Description: 兑换记录页面
 */
public class TaskExchangeRecordFragment extends BaseToolbarFragment<TaskExchangeRecordFragmentBinding, TaskExchangeRecordViewModel> {

    int sel_idx = -1;

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return R.layout.task_exchange_record_fragment;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ImmersionBarUtils.setupStatusBar(this, true, true);
        return view;
    }

    @Override
    public TaskExchangeRecordViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(TaskExchangeRecordViewModel.class);
    }

    @Override
    public void initParam() {
        super.initParam();
        sel_idx = getArguments().getInt("sel_idx");
    }

    @Override
    public void initData() {
        super.initData();
        TaskExchangeRecordTabPageAdapter adapter = new TaskExchangeRecordTabPageAdapter(this.getChildFragmentManager(), mActivity, 0);
        binding.viewPagers.setOffscreenPageLimit(1);
        binding.viewPagers.setAdapter(adapter);
        binding.tabs.setSelectedTabIndicatorHeight(0);
        binding.tabs.setupWithViewPager(binding.viewPagers);
        createTabs();
        binding.tabs.getTabAt(sel_idx).select();
        binding.tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                try {
                    tab.getCustomView().findViewById(R.id.tab_text).setSelected(true);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                try {
                    //tab未被选择的时候回调
                    tab.getCustomView().findViewById(R.id.tab_text).setSelected(false);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //tab重新选择的时候回调
            }
        });
    }

    private void createTabs() {
        for (int i = 0; i < binding.tabs.getTabCount(); i++) {
            TabLayout.Tab tab = binding.tabs.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(R.layout.tab_item);
                View customView = tab.getCustomView();
                if (customView != null) {
                    if (i == 0) {
                        customView.findViewById(R.id.tab_text).setSelected(true);
                    }
                    Button button = customView.findViewById(R.id.tab_text);
                    button.setOnClickListener(v -> tab.select());
                    button.setText(TaskExchangeRecordTabPageAdapter.TAB_MALE_TITLES[i]);
                }
            }
        }
    }

    @Override
    public void initViewObservable() {

    }

}
