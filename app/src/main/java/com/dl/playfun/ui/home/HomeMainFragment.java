package com.dl.playfun.ui.home;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.airbnb.lottie.LottieAnimationView;
import com.blankj.utilcode.util.IntentUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.Injection;
import com.dl.playfun.databinding.FragmentHomeMainBinding;
import com.dl.playfun.entity.CoinExchangePriceInfo;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.event.LocationChangeEvent;
import com.dl.playfun.kl.view.VideoPresetActivity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.manager.LocationManager;
import com.dl.playfun.ui.base.BaseFragment;
import com.dl.playfun.ui.base.BaseRefreshFragment;
import com.dl.playfun.ui.home.accost.HomeAccostDialog;
import com.dl.playfun.utils.AutoSizeUtils;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.widget.coinrechargesheet.GameCoinExchargeSheetView;
import com.google.android.material.tabs.TabLayout;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;

import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author wulei
 */
public class HomeMainFragment extends BaseRefreshFragment<FragmentHomeMainBinding, HomeMainViewModel> {

    private List<ConfigItemEntity> citys;

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AutoSizeUtils.applyAdapt(this.getResources());
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
        nearItemEntity.setName(getStringByResId(R.string.playfun_tab_female_1));
        citys.add(0, nearItemEntity);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        binding.rcvTable.setLayoutManager(layoutManager);

        binding.tvLocationWarn.setOnClickListener(view -> {
            Intent intent = IntentUtils.getLaunchAppDetailsSettingsIntent(mActivity.getPackageName());
            startActivity(intent);
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
        } catch (Exception ignored) {

        }
        //展示首页广告位
        viewModel.getAdListBannber();
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
        viewModel.uc.clickRegion.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {

            }
        });
        viewModel.uc.starActivity.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void aBoolean) {
                Intent intent = new Intent(mActivity, VideoPresetActivity.class);
                mActivity.startActivity(intent);
            }
        });
        viewModel.uc.isLoad.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    showHud();
                } else {
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
        //搭讪相关
        viewModel.uc.sendAccostFirstError.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {
                AppContext.instance().logEvent(AppsFlyerEvent.Top_up);
                GameCoinExchargeSheetView coinRechargeSheetView = new GameCoinExchargeSheetView(mActivity);
                coinRechargeSheetView.show();
                coinRechargeSheetView.setCoinRechargeSheetViewListener(new GameCoinExchargeSheetView.CoinRechargeSheetViewListener() {
                    @Override
                    public void onPaySuccess(GameCoinExchargeSheetView sheetView, CoinExchangePriceInfo sel_goodsEntity) {
                        sheetView.dismiss();
                    }

                    @Override
                    public void onPayFailed(GameCoinExchargeSheetView sheetView, String msg) {
                        sheetView.dismiss();
                        ToastUtils.showShort(msg);
                        AppContext.instance().logEvent(AppsFlyerEvent.Failed_to_top_up);
                    }
                });
            }
        });
        //播放搭讪动画
        viewModel.uc.loadLoteAnime.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer position) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) binding.rcvLayout.getLayoutManager();
                final View child = layoutManager.findViewByPosition(position);
                if (child != null) {
                    LottieAnimationView itemLottie = child.findViewById(R.id.item_lottie);
                    if (itemLottie != null) {
                        itemLottie.setImageAssetsFolder("images/");
                        itemLottie.addAnimatorListener(new AnimatorListenerAdapter() {

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                itemLottie.removeAnimatorListener(this);
                                itemLottie.setVisibility(View.GONE);
                            }
                        });
                        if (!itemLottie.isAnimating()) {
                            itemLottie.setVisibility(View.VISIBLE);
                            itemLottie.setAnimation(R.raw.accost_animation);
                            itemLottie.playAnimation();
                        }
                    }
                }
            }
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
        viewModel.locationService.get();
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
}
