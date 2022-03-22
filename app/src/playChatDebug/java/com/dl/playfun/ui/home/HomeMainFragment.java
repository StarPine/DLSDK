package com.dl.playfun.ui.home;

import static com.dl.playfun.app.AppConfig.FEMALE;
import static com.dl.playfun.app.AppConfig.MALE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.IntentUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.Injection;
import com.dl.playfun.databinding.FragmentHomeMainBinding;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.event.CityChangeEvent;
import com.dl.playfun.event.LocationChangeEvent;
import com.dl.playfun.event.MessageTagEvent;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.manager.LocationManager;
import com.dl.playfun.ui.base.BaseFragment;
import com.dl.playfun.ui.dialog.CityChooseDialog;
import com.dl.playfun.ui.dialog.HomeAccostDialog;
import com.dl.playfun.utils.ChatUtils;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.widget.dialog.MVDialog;
import com.google.android.material.tabs.TabLayout;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;

import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.jessyan.autosize.AutoSizeCompat;
import me.jessyan.autosize.internal.CustomAdapt;

/**
 * @author wulei
 */
public class HomeMainFragment extends BaseFragment<FragmentHomeMainBinding, HomeMainViewModel> implements CustomAdapt {

    private List<ConfigItemEntity> citys;

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AutoSizeCompat.autoConvertDensityOfGlobal(this.getResources());
        return R.layout.fragment_home_main;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public HomeMainViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(HomeMainViewModel.class);
    }

    @Override
    public void initData() {
        super.initData();
        AppContext.instance().logEvent(AppsFlyerEvent.Nearby);
        citys = Injection.provideDemoRepository().readCityConfig();
        ConfigItemEntity nearItemEntity = new ConfigItemEntity();
        nearItemEntity.setName(getStringByResId(R.string.tab_female_1));
        citys.add(0, nearItemEntity);
        binding.tvLocationWarn.setOnClickListener(view -> {
            Intent intent = IntentUtils.getLaunchAppDetailsSettingsIntent(mActivity.getPackageName());
            startActivity(intent);
        });

        //boolean b = viewModel.gender.get();
        //
        boolean b = ConfigManager.getInstance().isMale();
        HomeMainTabPagerAdapter adapter = new HomeMainTabPagerAdapter(mActivity, this.getChildFragmentManager(), !b ? MALE : FEMALE, viewModel);
        binding.viewPager.setOffscreenPageLimit(2);
        binding.viewPager.setAdapter(adapter);
        binding.tabs.setSelectedTabIndicatorHeight(0);
        binding.tabs.setupWithViewPager(binding.viewPager);

        createTabs();

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
        try {

            new RxPermissions(this)
                    .request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                    .subscribe(granted -> {
                        if (granted) {
                            viewModel.locationService.set(true);
                            if (ConfigManager.getInstance().isLocation()) {
                                viewModel.showLocationAlert.set(true);
                            } else {
                                viewModel.showLocationAlert.set(false);
                            }
                            startLocation();
                        } else {
                            viewModel.locationService.set(false);
                            RxBus.getDefault().post(new LocationChangeEvent(LocationChangeEvent.LOCATION_STATUS_FAILED));
                            viewModel.showLocationAlert.set(true);
                        }
                    });
        } catch (Exception e) {

        }
    }

    private void createTabs() {
        boolean b = ConfigManager.getInstance().isMale();
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
                    button.setText(!b ? HomeMainTabPagerAdapter.TAB_MALE_TITLES[i] : HomeMainTabPagerAdapter.TAB_FEMALE_TITLES[i]);
                }
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) { //页面再次进入时
            try {
                if (!ObjectUtils.isEmpty(viewModel.loadLocalUserData().getPermanentCityIds())) {
                    viewModel.showLocationAlert.set(false);
                }
            } catch (Exception e) {
                viewModel.showLocationAlert.set(true);
            }

        }
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.uc.isLoad.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean){
                    showHud();
                }else {
                    dismissHud();
                }
            }
        });
        //搭讪弹窗
        viewModel.uc.clickAccountDialog.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String isShow) {
                HomeAccostDialog homeAccostDialog = new HomeAccostDialog(getContext());
                homeAccostDialog.setIncomplete(isShow);
                homeAccostDialog.setDialogAccostClicksListener(new HomeAccostDialog.DialogAccostClicksListener() {
                    @Override
                    public void onSubmitClick(HomeAccostDialog dialog, List<Integer> listData) {
                        dialog.dismiss();
                        viewModel.putAccostList(listData);
                    }

                    @Override
                    public void onCancelClick(HomeAccostDialog dialog) {
                        AppContext.instance().logEvent(AppsFlyerEvent.accost_close);
                        dialog.dismiss();
                    }
                });
                homeAccostDialog.show();
            }
        });
        //加载背景图片
//        viewModel.uc.loadSysConfigTask.observe(this, new Observer<SystemConfigTaskEntity>() {
//            @Override
//            public void onChanged(SystemConfigTaskEntity systemConfigTaskEntity) {
//                if (!StringUtils.isTrimEmpty(systemConfigTaskEntity.getFloatingImg())) {
//                    Glide.with(HomeMainFragment.this.getContext()).asGif().load(StringUtil.getFullImageUrl(systemConfigTaskEntity.getFloatingImg()))
//                            .error(R.drawable.nearby_attendance)
//                            .placeholder(R.drawable.nearby_attendance)
//                            .into(binding.floatingImg);
//
//                }
//            }
//        });
        viewModel.uc.genderCheckedChange.observe(this, integer -> {
            HomeMainTabPagerAdapter adapter = new HomeMainTabPagerAdapter(mActivity, HomeMainFragment.this.getChildFragmentManager(), integer, viewModel);
            binding.viewPager.setAdapter(adapter);
            startLocation();
            createTabs();
//                if (location != null) {
//                    RxBus.getDefault().post(new LocationChangeEvent(LocationChangeEvent.LOCATION_STATUS_SUCCESS, location.getLatitude(), location.getLongitude()));
//                } else {
//                    RxBus.getDefault().post(new LocationChangeEvent(LocationChangeEvent.LOCATION_STATUS_FAILED));
//                }
        });

        viewModel.uc.clickRegion.observe(this, o -> {
            CityChooseDialog dialog = new CityChooseDialog(citys);
            dialog.show(getChildFragmentManager(), CityChooseDialog.class.getCanonicalName());
            dialog.setCityChooseDialogListener(new CityChooseDialog.CityChooseDialogListener() {
                @Override
                public void onClickConfirm(CityChooseDialog dialog) {
                    dialog.dismiss();
                }

                @Override
                public void onItemClick(CityChooseDialog dialog, ConfigItemEntity itemEntity) {
                    dialog.dismiss();
                    for (ConfigItemEntity city : citys) {
                        city.setIsChoose(false);
                    }
                    itemEntity.setIsChoose(true);
                    viewModel.cityId.set(itemEntity.getId());
                    viewModel.regionTitle.set(itemEntity.getName());
                    if (itemEntity.getId() == null || itemEntity.getId() == 0) {
                        startLocation();
                    } else {
                        RxBus.getDefault().post(new CityChangeEvent(itemEntity));
                    }
                }
            });
        });
        viewModel.uc.clickLocationSel.observe(this, o -> {
            MVDialog.getCityDialogs(mActivity, viewModel.list_chooseCityItem, viewModel.SelConfigItemEntity.get().getId(), new MVDialog.raDioChooseCity() {
                @Override
                public void clickListItem(Dialog dialog, ConfigItemEntity ids) {
                    dialog.dismiss();
                    viewModel.SelConfigItemEntity.set(ids);
                }
            });
        });

        viewModel.uc.clickLocationConfirm.observe(this, o -> {
            ConfigItemEntity configItemEntity = viewModel.SelConfigItemEntity.get();
            if (configItemEntity == null) {
                ToastUtils.showShort(R.string.please_select_location);
            } else {
                viewModel.showLocationAlert.set(false);
                configItemEntity.setIsChoose(true);
                viewModel.cityId.set(configItemEntity.getId());
                viewModel.regionTitle.set(configItemEntity.getName());
                if (configItemEntity.getId() == null || configItemEntity.getId() == 0) {
                    startLocation();
                } else {
                    viewModel.isBindCity(configItemEntity.getId());
                    RxBus.getDefault().post(new CityChangeEvent(configItemEntity));
                }
            }
        });
        viewModel.uc.clickToMessageDetail.observe(this, o -> {
            String userId = "user_" + viewModel.messageTagEntity.get().getUserId();
            String nickname = viewModel.messageTagEntity.get().getNickname();
            String textMessage = viewModel.messageTagEntity.get().getMsg();
            RxBus.getDefault().post(new MessageTagEvent(null, false));
            ChatUtils.chatUser(userId, nickname, viewModel, textMessage);
        });
    }


    @SuppressLint("MissingPermission")
    private void startLocation() {
        LocationManager.getInstance().getLastLocation(new LocationManager.LocationListener() {
            @Override
            public void onLocationSuccess(double lat, double lng) {
                viewModel.lat.set(lat);
                viewModel.lng.set(lng);
                RxBus.getDefault().post(new LocationChangeEvent());
            }

            @Override
            public void onLocationFailed() {
                //附近页面定位失败。通知一直下发 RxBus.getDefault().post(new LocationChangeEvent());
                RxBus.getDefault().post(new LocationChangeEvent());
            }
        });
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        ImmersionBarUtils.setupStatusBar(this, true, true);
        if (!viewModel.locationService.get()) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AppContext.isHomePage = true;
        AppContext.isShowNotPaid = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        AppContext.isShowNotPaid = false;
        AppContext.isHomePage = false;
    }

    @Override
    protected boolean isUmengReportPage() {
        return false;
    }

    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return 360;
    }
}
