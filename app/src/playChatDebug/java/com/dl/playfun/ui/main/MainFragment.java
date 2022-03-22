package com.dl.playfun.ui.main;

import android.app.Dialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager2.widget.ViewPager2;

import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.Injection;
import com.dl.playfun.databinding.FragmentMainBinding;
import com.dl.playfun.entity.BannerItemEntity;
import com.dl.playfun.entity.GoodsEntity;
import com.dl.playfun.entity.LikeRecommendEntity;
import com.dl.playfun.entity.VersionEntity;
import com.dl.playfun.event.MainTabEvent;
import com.dl.playfun.event.ReceiveNewUserRewardEvent;
import com.dl.playfun.event.TaskListEvent;
import com.dl.playfun.event.TaskMainTabEvent;
import com.dl.playfun.event.UMengCustomEvent;
import com.dl.playfun.helper.JumpHelper;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.tim.TUIUtils;
import com.dl.playfun.ui.base.BaseFragment;
import com.dl.playfun.ui.dialog.AdDialog;
import com.dl.playfun.ui.dialog.GuessYouLikeDialog;
import com.dl.playfun.ui.dialog.HomeAccostDialog;
import com.dl.playfun.ui.dialog.LockDialog;
import com.dl.playfun.ui.dialog.MyEvaluateDialog;
import com.dl.playfun.ui.home.HomeMainFragment;
import com.dl.playfun.ui.message.MessageMainFragment;
import com.dl.playfun.ui.mine.MineFragment;
import com.dl.playfun.ui.mine.task.main.TaskMainFragment;
import com.dl.playfun.ui.mine.vipsubscribe.VipSubscribeFragment;
import com.dl.playfun.ui.radio.radiohome.RadioFragment;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.widget.coinpaysheet.CoinPaySheet;
import com.dl.playfun.widget.coinrechargesheet.CoinRechargeSheetView;
import com.dl.playfun.widget.dialog.MVDialog;
import com.dl.playfun.widget.dialog.TraceDialog;
import com.dl.playfun.widget.dialog.WebViewDialog;
import com.dl.playfun.widget.dialog.version.view.UpdateDialogView;
import com.dl.playfun.widget.pageview.FragmentAdapter;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.qcloud.tuicore.util.BackgroundTasks;
import com.tencent.qcloud.tuikit.tuiconversation.ui.view.ConversationCommonHolder;

import java.util.List;

import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * @author wulei
 */
public class MainFragment extends BaseFragment<FragmentMainBinding, MainViewModel> {

    public static final int FIRST = 0;
    public static final int SECOND = 1;
    public static final int THIRD = 2;
    public static final int FOURTH = 3;
    public static final int FIFTH = 4;

    private final BaseFragment[] mFragments = new BaseFragment[5];
    private TextView tvBadgeNum;
    private Dialog lockDialog;
    private final int lastfragment = 0;

    private RelativeLayout selRelativeLayout;
    private ViewPager2 mainViewPager;

    private static final long WAIT_TIME = 3000L;
    private long TOUCH_TIME = 0;

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ImmersionBarUtils.setupStatusBar(this, true, true);
        return R.layout.fragment_main;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public MainViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(MainViewModel.class);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        AppContext.instance().logEvent(AppsFlyerEvent.main_open);
        //未付费弹窗
        viewModel.uc.notPaidDialog.observe(this,s -> {
            String url;
            if (s.equals("2")) {
                url = AppConfig.WEB_BASE_URL+"recharge_vip/recharge_vip.html";
            } else {
                url = AppConfig.WEB_BASE_URL+"recharge_zuan/recharge_zuan.html";
            }
            if(AppConfig.isDebug){
                if (s.equals("2")) {
                    url = "http://t-m.joy-mask.com/recharge_vip/recharge_vip.html";
                } else {
                    url = "http://t-m.joy-mask.com/recharge_zuan/recharge_zuan.html";
                }
            }
            new WebViewDialog(getContext(), mActivity, url, new WebViewDialog.ConfirmOnclick() {
                @Override
                public void webToVipRechargeVC(Dialog dialog) {
                    if(dialog!=null){
                        dialog.dismiss();
                    }
                    viewModel.start(VipSubscribeFragment.class.getCanonicalName());
                }

                @Override
                public void vipRechargeDiamondSuccess(Dialog dialog, Integer coinValue) {
                    if(dialog!=null){
                        dialog.dismiss();
                    }
                }

                @Override
                public void moreRechargeDiamond(Dialog dialog) {
                    dialog.dismiss();
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showRecharge();
                        }
                    });
                }

                @Override
                public void cancel() {
                }
            }).noticeDialog().show();
            if (s.equals("2")){//vip
                viewModel.pushGreet(3);
            }else {
                viewModel.pushGreet(2);
            }

        });
        //搭讪弹窗-今日缘分
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
                if (isShow.equals("1")){
                    viewModel.pushGreet(1);
                }
            }
        });
        //公告展示
        viewModel.uc.versionAlertSl.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {
//                WebViewDialog.getInstance(getContext())
//                        .setWebUrl(AppConfig.WEB_BASE_URL + "notice/notice.html")
//                        .setConfirmOnlick(new WebViewDialog.ConfirmOnclick() {
//                            @Override
//                            public void webToVipRechargeVC(Dialog dialog) {
//                                dialog.dismiss();
//                                viewModel.start(VipSubscribeFragment.class.getCanonicalName());
//                            }
//
//                            @Override
//                            public void cancel() {
//                                viewModel.uc.newUserRegis.postValue(false);
//                            }
//                        }).noticeDialog().show();
            }
        });
        //新用戶註冊完成
        viewModel.uc.newUserRegis.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (ConfigManager.getInstance().isNewUser()) {

                    AppContext.instance().appRepository.saveIsNewUser(false);
                    if (ConfigManager.getInstance().getTipMoneyShowFlag() && !ConfigManager.getInstance().isMale()) {
                        //啟動註冊完成dialog
                        startNewUserDialog();
                        RxBus.getDefault().post(new ReceiveNewUserRewardEvent());
                        return;
                    }
                }
                //弹广告
                viewModel.loadBanner();
            }
        });
        //版本更新提示
        viewModel.uc.versionEntitySingl.observe(this, new Observer<VersionEntity>() {
            @Override
            public void onChanged(VersionEntity versionEntity) {
                BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (versionEntity.getVersion_code().intValue() <= AppConfig.VERSION_CODE.intValue()) {
                            //ToastUtils.showShort(R.string.version_latest);
                            viewModel.showAnnouncemnet();
                        } else {
                            boolean isUpdate = versionEntity.getIs_update().intValue() == 1;
                            UpdateDialogView.getInstance(mActivity)
                                    .getUpdateDialogView(versionEntity.getVersion_name(), versionEntity.getContent(), versionEntity.getUrl(), isUpdate, "playchat")
                                    .setConfirmOnlick(new UpdateDialogView.CancelOnclick() {
                                        @Override
                                        public void cancel() {
                                            viewModel.showAnnouncemnet();
                                        }
                                    })
                                    .show();
                        }
                    }
                });
            }
        });
        //气泡提示
        viewModel.uc.bubbleTopShow.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean tipShow) {
                if (!ConfigManager.getInstance().getTipMoneyShowFlag()) {
                    binding.bubbleTip.setVisibility(View.GONE);
                    return;
                }
                if (tipShow && !ConfigManager.getInstance().isMale()) {
                    binding.bubbleTip.setVisibility(View.VISIBLE);
                } else {
                    binding.bubbleTip.setVisibility(View.GONE);
                }
            }
        });
        viewModel.uc.taskCenterclickTab.observe(this, new Observer<TaskMainTabEvent>() {
            @Override
            public void onChanged(TaskMainTabEvent taskMainTabEvent) {
                if (taskMainTabEvent != null) {
                    if (taskMainTabEvent.isTbarClicked()) {//tbar切换到活动中心
                        setSelectedItemId(binding.navigationRank);
                    }
                }
            }
        });
        viewModel.uc.mainTab.observe(this, new Observer<MainTabEvent>() {
            @Override
            public void onChanged(MainTabEvent mainTabEvent) {
                if (mainTabEvent != null) {
                    switch (mainTabEvent.getTabName()) {
                        case "home":
                            setSelectedItemId(binding.navigationHome);//tbar切换到首頁
                            break;
                        case "plaza":
                            setSelectedItemId(binding.navigationRadio);//tbar切换到廣場
                            break;
                        case "message":
                            setSelectedItemId(binding.navigationMessage);//tbar切换到訊息
                            break;
                    }
                }
            }
        });
        viewModel.uc.showFaceRecognitionDialog.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void aVoid) {
                showFaceRecognitionDialog();
            }
        });
        viewModel.uc.clickLogout.observe(this, aVoid -> MVDialog.getInstance(MainFragment.this.getContext())
                .setContent(getString(R.string.conflirm_log_out))
                .setConfirmOnlick(dialog -> {
                    TUIUtils.logout(new V2TIMCallback() {
                        @Override
                        public void onSuccess() {
                            viewModel.logout();
                        }

                        @Override
                        public void onError(int i, String s) {
                            viewModel.logout();
                        }
                    });
                })
                .chooseType(MVDialog.TypeEnum.CENTERWARNED)
                .show());
        viewModel.uc.lockDialog.observe(this, aVoid -> {
            LockDialog dialog = new LockDialog();
            dialog.setPassword(viewModel.lockPassword.get());
            dialog.setLockDialogListener(new LockDialog.LockDialogListener() {
                @Override
                public void onLogoutClick(LockDialog lockDialog) {
                    MVDialog.getInstance(MainFragment.this.getContext())
                            .setContent(getString(R.string.conflirm_log_out))
                            .setConfirmOnlick(dialog -> {
                                lockDialog.dismiss();
                                dialog.dismiss();
                                viewModel.logout();
                            })
                            .chooseType(MVDialog.TypeEnum.CENTERWARNED)
                            .show();
                }

                @Override
                public void onVerifySuccess(LockDialog dialog) {
                    dialog.dismiss();
                }
            });
            dialog.show(getChildFragmentManager(), LockDialog.class.getCanonicalName());
        });
        viewModel.uc.startFace.observe(this, verifyToken -> {

        });
        viewModel.uc.allMessageCountChange.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                try {
                    viewModel.getBubbleSetting();
                    if (count == 0) {
                        tvBadgeNum.setVisibility(View.GONE);
                    } else {
                        tvBadgeNum.setVisibility(View.VISIBLE);
                        if (count > 98) {
                            tvBadgeNum.setText("99+");
                        } else {
                            tvBadgeNum.setText(String.valueOf(count));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //广告展示
        viewModel.uc.showAdDialog.observe(this, bannerEntity -> {
            for (BannerItemEntity bannerItemEntity : bannerEntity) {
                showAdDialog(bannerItemEntity);
            }
        });

        viewModel.uc.showRecommendUserDialog.observe(this, entity -> {
            checkShowAd(selRelativeLayout.getId());
        });
        viewModel.versionOnClickCommand();
    }

    private void startNewUserDialog() {
        TraceDialog.getInstance(getContext())
                .setConfirmOnlick(new TraceDialog.ConfirmOnclick() {
                    @Override
                    public void confirm(Dialog dialog) {
                        AppContext.instance().logEvent(AppsFlyerEvent.task_register_toChat);
                        dialog.dismiss();
                        viewModel.loadBanner();
                        setSelectedItemId(binding.navigationMessage);
                    }
                })
                .setCannelOnclick(new TraceDialog.CannelOnclick() {
                    @Override
                    public void cannel(Dialog dialog) {
                        dialog.dismiss();

                    }
                })
                .newUserRegisComplete().show();
    }

    @Override
    public void initData() {
        super.initData();
        initView();
        ConversationCommonHolder.sexMale = ConfigManager.getInstance().isMale();
        viewModel.getBubbleSetting();
    }

    /**
     * @return void
     * @Desc TODO(页面再次进入)
     * @author 彭石林
     * @parame [hidden]
     * @Date 2021/8/4
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (System.currentTimeMillis() - TOUCH_TIME > WAIT_TIME) {
                //刷新任何列表数据
                RxBus.getDefault().post(new TaskListEvent());
                TOUCH_TIME = System.currentTimeMillis();
            }
            if (!StringUtils.isEmpty(AppConfig.homePageName)) {
                switch (AppConfig.homePageName) {
                    case "home":
                        AppConfig.homePageName = null;
                        setSelectedItemId(binding.navigationHome);
                        break;
                    case "broadcast":
                        AppConfig.homePageName = null;
                        setSelectedItemId(binding.navigationRadio);
                        break;
                    case "navigation_rank":
                        AppConfig.homePageName = null;
                        setSelectedItemId(binding.navigationRank);
                        break;
                    case "message":
                        AppConfig.homePageName = null;
                        setSelectedItemId(binding.navigationMessage);
                        break;
                    case "user":
                        AppConfig.homePageName = null;
                        setSelectedItemId(binding.navigationMine);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        super.onEnterAnimationEnd(savedInstanceState);
        String homePageName = AppContext.instance().appRepository.readDefaultHomePageConfig();
        switch (homePageName) {
            case "home":
                setSelectedItemId(binding.navigationHome);
                break;
            case "broadcast":
                setSelectedItemId(binding.navigationRadio);
                break;
            case "navigation_rank":
                setSelectedItemId(binding.navigationRank);
                break;
            case "message":
                setSelectedItemId(binding.navigationMessage);
                break;
            case "user":
                setSelectedItemId(binding.navigationMine);
                break;
            default:
                break;
        }
    }

    private void checkShowAd(int id) {
        if (viewModel.likeRecommendEntity != null || (viewModel.bannerEntity != null && viewModel.bannerEntity.size() > 0)) {
            if (id == R.id.navigation_home) {
                if (viewModel.likeRecommendEntity != null) {
                    GuessYouLikeDialog dialog = GuessYouLikeDialog.newInstance(viewModel.likeRecommendEntity);
                    dialog.setGuessYouLikeDialogListener(new GuessYouLikeDialog.GuessYouLikeDialogListener() {
                        @Override
                        public void onCallClick(GuessYouLikeDialog dialog, LikeRecommendEntity entity) {
                            dialog.dismiss();
                            viewModel.likeRecommendEntity = null;
                            if (entity.getPrice() > 0) {
                                new CoinPaySheet.Builder(mActivity).setPayParams(10, Injection.provideDemoRepository().readUserData().getId(), getString(R.string.unlock_chat), false, new CoinPaySheet.CoinPayDialogListener() {
                                    @Override
                                    public void onPaySuccess(CoinPaySheet sheet, String orderNo, Integer payPrice) {
                                        sheet.dismiss();
                                        ToastUtils.showShort(R.string.pay_success);
                                        viewModel.callGuessYouLike(entity);
                                    }

                                    @Override
                                    public void onRechargeSuccess(CoinRechargeSheetView rechargeSheetView) {

                                    }
                                }).build().show();
                            } else {
                                viewModel.callGuessYouLike(entity);
                            }
                        }

                        @Override
                        public void onCloseClick(GuessYouLikeDialog dialog) {
                            dialog.dismiss();
                            viewModel.likeRecommendEntity = null;
                        }
                    });
                    dialog.show(getChildFragmentManager(), MyEvaluateDialog.class.getCanonicalName());
                } else if (viewModel.bannerEntity != null) {
                    for (BannerItemEntity bannerItemEntity : viewModel.bannerEntity) {
                        if ("1".equals(bannerItemEntity.getPosition())) {
                            //showAdDialog(bannerItemEntity);
                            if (bannerItemEntity.getPosition().equals("1")) {
                                Integer downTime = bannerItemEntity.getOpenTime();
                                if (ObjectUtils.isEmpty(downTime) || downTime.intValue() == 0) {

                                } else {
                                    /**
                                     * 倒计时后台设置多少秒，一次1秒
                                     */
                                    CountDownTimer timer = new CountDownTimer(downTime * 1000, 1000) {
                                        @Override
                                        public void onTick(long millisUntilFinished) {
                                            //没少一秒都会回调
                                        }

                                        @Override
                                        public void onFinish() {
                                            try {
                                                FragmentManager fragmentManager = _mActivity.getSupportFragmentManager();
                                                List<Fragment> fragments = fragmentManager.getFragments();
                                                for (Fragment fragment : fragments) {
                                                    if (fragment != null && fragment.isVisible())
                                                        if (fragment instanceof MainFragment) {
                                                            if (!mFragments[FIRST].isHidden()) {
                                                                AdDialog dialog = AdDialog.newInstance(AppConfig.radioAlertFlagEntity);
                                                                dialog.setAdDialogListener(new AdDialog.AdDialogListener() {
                                                                    @Override
                                                                    public void onImageClick(AdDialog dialog, BannerItemEntity entity) {
                                                                        dialog.dismiss();
                                                                        AppContext.instance().logEvent(AppsFlyerEvent.pop_ad_click);
                                                                        if ("1".equals(entity.getPosition())) {
                                                                            RxBus.getDefault().post(new UMengCustomEvent(UMengCustomEvent.EVENT_HOME_POP_AD));
                                                                        } else if ("2".equals(entity.getPosition())) {
                                                                            RxBus.getDefault().post(new UMengCustomEvent(UMengCustomEvent.EVENT_RADIO_POP_AD));
                                                                        }
                                                                        JumpHelper.jump(viewModel, entity.getLandingPage());
                                                                    }

                                                                    @Override
                                                                    public void onCloseClick(AdDialog dialog, BannerItemEntity entity) {
                                                                        dialog.dismiss();
                                                                        AppContext.instance().logEvent(AppsFlyerEvent.pop_ad_close);
                                                                    }
                                                                });
                                                                AppContext.instance().logEvent(AppsFlyerEvent.pop_ad_show);
                                                                dialog.show(getChildFragmentManager(), AdDialog.class.getCanonicalName());
                                                                AppConfig.radioAlertFlagShow = false;
                                                                AppConfig.radioAlertFlagEntity = null;
                                                            } else {
                                                                AppConfig.radioAlertFlagShow = true;
                                                            }
                                                        }
                                                }
                                            } catch (Exception e) {

                                            }

                                        }
                                    };
                                    viewModel.bannerEntity.remove(bannerItemEntity);
                                    AppConfig.radioAlertFlagEntity = bannerItemEntity;
                                    timer.start();
                                }
                            }
                            break;
                        }
                    }
                }
            } else if (id == R.id.navigation_radio) {
                for (BannerItemEntity bannerItemEntity : viewModel.bannerEntity) {
                    if ("2".equals(bannerItemEntity.getPosition())) {
                        showAdDialog(bannerItemEntity);
                        break;
                    }
                }
            }
        }
    }

    private void showFaceRecognitionDialog() {
        Animation animation = AnimationUtils.loadAnimation(MainFragment.this.getContext(), R.anim.pop_enter_anim);
        animation.setFillAfter(true);
    }

    private void showAdDialog(BannerItemEntity bannerItemEntity) {
        AdDialog dialog = AdDialog.newInstance(bannerItemEntity);
        dialog.setAdDialogListener(new AdDialog.AdDialogListener() {
            @Override
            public void onImageClick(AdDialog dialog, BannerItemEntity entity) {
                dialog.dismiss();
                AppContext.instance().logEvent(AppsFlyerEvent.pop_ad_click);
                if ("1".equals(entity.getPosition())) {
                    RxBus.getDefault().post(new UMengCustomEvent(UMengCustomEvent.EVENT_HOME_POP_AD));
                } else if ("2".equals(entity.getPosition())) {
                    RxBus.getDefault().post(new UMengCustomEvent(UMengCustomEvent.EVENT_RADIO_POP_AD));
                }
                viewModel.bannerEntity.remove(entity);
                JumpHelper.jump(viewModel, entity.getLandingPage());
            }

            @Override
            public void onCloseClick(AdDialog dialog, BannerItemEntity entity) {
                dialog.dismiss();
                AppContext.instance().logEvent(AppsFlyerEvent.pop_ad_close);
                viewModel.bannerEntity.remove(entity);
            }
        });
        AppContext.instance().logEvent(AppsFlyerEvent.pop_ad_show);
        dialog.show(getChildFragmentManager(), AdDialog.class.getCanonicalName());


    }

    private void initView() {


        BaseFragment firstFragment = findChildFragment(HomeMainFragment.class);
        if (firstFragment == null) {
            mFragments[FIRST] = new HomeMainFragment();
            mFragments[SECOND] = new RadioFragment();
            mFragments[THIRD] = new TaskMainFragment();
            mFragments[FOURTH] = new MessageMainFragment();
            mFragments[FIFTH] = new MineFragment();
        } else {
            // 这里库已经做了Fragment恢复,所有不需要额外的处理了, 不会出现重叠问题

            // 这里我们需要拿到mFragments的引用
            mFragments[FIRST] = firstFragment;
            mFragments[SECOND] = findChildFragment(HomeMainFragment.class);
            mFragments[THIRD] = findChildFragment(TaskMainFragment.class);
            mFragments[FOURTH] = findChildFragment(MessageMainFragment.class);
            mFragments[FIFTH] = findChildFragment(MineFragment.class);
        }
        tvBadgeNum = binding.tvMsgCount;
        //添加到Tab上

        //首页点击
        binding.navigationHome.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setSelectedItemId(binding.navigationHome);
            }
        });
        binding.navigationRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedItemId(binding.navigationRadio);
            }
        });
        binding.navigationRank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedItemId(binding.navigationRank);
            }
        });
        binding.navigationMessage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setSelectedItemId(binding.navigationMessage);
            }
        });
        binding.navigationMine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedItemId(binding.navigationMine);
            }
        });
        binding.bubbleTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedItemId(binding.navigationMessage);
            }
        });
        mainViewPager = binding.viewPager;
        FragmentAdapter fragmentAdapter = new FragmentAdapter(this);
        fragmentAdapter.setFragmentList(mFragments);
        // 关闭左右滑动切换页面
        mainViewPager.setUserInputEnabled(false);
        // 设置缓存数量 避免销毁重建
        mainViewPager.setOffscreenPageLimit(5);
        mainViewPager.setAdapter(fragmentAdapter);
        mainViewPager.setCurrentItem(0, false);

        if (selRelativeLayout == null) {
            selRelativeLayout = binding.navigationHome;
        } else {
            // 初始化时，强制切换tab到上一次的位置
            RelativeLayout tmp = selRelativeLayout;
            selRelativeLayout = null;
            setSelectedItemId(tmp);
            selRelativeLayout = tmp;
        }
    }

    private void setSelectedItemId(RelativeLayout view) {
        if (selRelativeLayout != null && selRelativeLayout.getId() == view.getId()) {
            return;
        }
        selRelativeLayout = view;
        resetMenuState();
        int id = view.getId();
        if (id == R.id.navigation_home) {
            mainViewPager.setCurrentItem(0, false);
            binding.navigationHomeImg.setImageResource(R.drawable.tab_home_checked);
            binding.navigationHomeText.setTextColor(getResources().getColor(R.color.navigation_checked));
        } else if (id == R.id.navigation_radio) {
            mainViewPager.setCurrentItem(1, false);
            binding.navigationRadioImg.setImageResource(R.drawable.tab_radio_checked);
            binding.navigationRadioText.setTextColor(getResources().getColor(R.color.navigation_checked));
        } else if (id == R.id.navigation_rank) {
            mainViewPager.setCurrentItem(2, false);
            binding.navigationRankImg.setImageResource(R.drawable.toolbar_icon_task_checked);
            binding.navigationRankText.setTextColor(getResources().getColor(R.color.navigation_checked));
        } else if (id == R.id.navigation_message) {
            mainViewPager.setCurrentItem(3, false);
            binding.navigationMessageImg.setImageResource(R.drawable.tab_message_checked);
            binding.navigationMessageText.setTextColor(getResources().getColor(R.color.navigation_checked));
        } else if (id == R.id.navigation_mine) {
            mainViewPager.setCurrentItem(4, false);
            if (viewModel.uc.gender.get()) {
                binding.navigationMineImg.setImageResource(R.drawable.tab_mine_male_checked);
            } else {
                binding.navigationMineImg.setImageResource(R.drawable.tab_mine_female_checked);
            }
            binding.navigationMineText.setTextColor(getResources().getColor(R.color.navigation_checked));
        }
    }


    private void showRecharge() {
        CoinRechargeSheetView coinRechargeSheetView = new CoinRechargeSheetView(mActivity);
        coinRechargeSheetView.show();
        coinRechargeSheetView.setCoinRechargeSheetViewListener(new CoinRechargeSheetView.CoinRechargeSheetViewListener() {
            @Override
            public void onPaySuccess(CoinRechargeSheetView sheetView, GoodsEntity sel_goodsEntity) {
                viewModel.loadBalance();//刷新钻石
                AppContext.instance().logEvent(AppsFlyerEvent.success_diamond_top_up);
                // payorder
            }

            @Override
            public void onPayFailed(CoinRechargeSheetView sheetView, String msg) {
                // do nothing
            }
        });
    }

    //初始化按钮状态
    private void resetMenuState() {
        binding.navigationHomeText.setTextColor(getResources().getColor(R.color.navigation_checkno));
        binding.navigationRadioText.setTextColor(getResources().getColor(R.color.navigation_checkno));
        binding.navigationRankText.setTextColor(getResources().getColor(R.color.navigation_checkno));
        binding.navigationMessageText.setTextColor(getResources().getColor(R.color.navigation_checkno));
        binding.navigationMineText.setTextColor(getResources().getColor(R.color.navigation_checkno));

        binding.navigationHomeImg.setImageResource(R.drawable.tab_home_normal);
        binding.navigationRadioImg.setImageResource(R.drawable.tab_radio_normal);
        binding.navigationRankImg.setImageResource(R.drawable.toolbar_icon_task_checked);
        binding.navigationMessageImg.setImageResource(R.drawable.tab_message_normal);
        if (viewModel.uc.gender.get()) {
            binding.navigationMineImg.setImageResource(R.drawable.tab_mine_male_image);
        } else {
            binding.navigationMineImg.setImageResource(R.drawable.tab_mine_female_normal);
        }

    }

    /**
     * start other BrotherFragment
     */
    public void startBrotherFragment(SupportFragment targetFragment) {
        start(targetFragment);
    }

    @Override
    protected boolean isUmengReportPage() {
        return false;
    }
}
