package com.dl.playfun.ui.mine.broadcast;

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
import com.dl.playfun.databinding.FragmentBroadcastBinding;
import com.dl.playfun.event.UMengCustomEvent;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.ui.radio.issuanceprogram.IssuanceProgramFragment;
import com.dl.playfun.ui.radio.publishdynamic.PublishDynamicFragment;
import com.dl.playfun.widget.RadioPublishChooseSheet;
import com.google.android.material.tabs.TabLayout;

import me.goldze.mvvmhabit.bus.RxBus;

/**
 * 我的動態fragment
 */
public class BroadcastFragment extends BaseToolbarFragment<FragmentBroadcastBinding, BroadcastViewModel> {
    private BroadcastPagerAdapter adapter;

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_broadcast;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public BroadcastViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(BroadcastViewModel.class);
    }

    @Override
    public void initData() {
        super.initData();
        binding.tabs.setSelectedTabIndicatorHeight(0);
        binding.tabs.setupWithViewPager(binding.viewPager);
        adapter = new BroadcastPagerAdapter(mActivity, this.getChildFragmentManager());
        binding.viewPager.setAdapter(adapter);

        for (int i = 0; i < binding.tabs.getTabCount(); i++) {
            TabLayout.Tab tab = binding.tabs.getTabAt(i);
            tab.setCustomView(R.layout.tab_item);
            if (i == 0) {
                tab.getCustomView().findViewById(R.id.tab_text).setSelected(true);
            }
            Button button = tab.getCustomView().findViewById(R.id.tab_text);
            button.setOnClickListener(v -> tab.select());
            button.setText(BroadcastPagerAdapter.TAB_TITLES[i]);
        }
        binding.tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getCustomView().findViewById(R.id.tab_text).setSelected(true);
                //tab被选的时候回调
                //binding.viewPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //tab未被选择的时候回调
                tab.getCustomView().findViewById(R.id.tab_text).setSelected(false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //tab重新选择的时候回调
            }
        });
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.uc.clickIssuance.observe(this, o -> {

            //binding.ivRadioCreate.setVisibility(View.GONE);
            new RadioPublishChooseSheet.Builder(mActivity)
                    .setOnTypeSelectedListener((bottomSheet, type) -> {
                        bottomSheet.dismiss();
                        binding.ivRadioCreate.setVisibility(View.VISIBLE);
                        if (type == 1) {
                            RxBus.getDefault().post(new UMengCustomEvent(UMengCustomEvent.EVENT_MY_BROADCAST_PUBLISH_PROGRAM));
                            viewModel.checkTopical();
                        } else if (type == 2) {
                            RxBus.getDefault().post(new UMengCustomEvent(UMengCustomEvent.EVENT_MY_BROADCAST_PUBLISH_DYNAMIC));
                            viewModel.start(PublishDynamicFragment.class.getCanonicalName());
                        }
                    })
                    .setCancelButton(getString(R.string.cancel), new RadioPublishChooseSheet.CancelClickListener() {
                        @Override
                        public void onCancelClick(RadioPublishChooseSheet sheet) {
                            sheet.dismiss();
                            binding.ivRadioCreate.setVisibility(View.VISIBLE);
                        }
                    }).build().show();

        });
        viewModel.uc.programSubject.observe(this, o -> {
            if (viewModel.themes != null) {
                viewModel.start(IssuanceProgramFragment.class.getCanonicalName());
            }
        });
        viewModel.uc.switchPosion.observe(this, o -> {
            if (o != null) {
                binding.tabs.setScrollPosition(0, (int) o, true);
                binding.viewPager.setCurrentItem((int) o, true);
            }
        });
    }

    @Override
    protected boolean isUmengReportPage() {
        return false;
    }
}
