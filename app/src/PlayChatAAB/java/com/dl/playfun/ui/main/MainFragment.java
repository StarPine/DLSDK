package com.dl.playfun.ui.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager2.widget.ViewPager2;

import com.aliyun.svideo.common.utils.ScreenUtils;
import com.blankj.utilcode.util.StringUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AliYunMqttClientLifecycle;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.Injection;
import com.dl.playfun.app.config.TbarCenterImgConfig;
import com.dl.playfun.databinding.FragmentMainBinding;
import com.dl.playfun.entity.MqBroadcastGiftEntity;
import com.dl.playfun.entity.MqBroadcastGiftUserEntity;
import com.dl.playfun.entity.VersionEntity;
import com.dl.playfun.event.MainTabEvent;
import com.dl.playfun.event.TaskListEvent;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseFragment;
import com.dl.playfun.ui.coinpusher.CoinPusherConvertDialog;
import com.dl.playfun.ui.coinpusher.CoinPusherRoomListDialog;
import com.dl.playfun.ui.dialog.LockDialog;
import com.dl.playfun.ui.home.HomeMainFragment;
import com.dl.playfun.ui.home.accost.HomeAccostDialog;
import com.dl.playfun.ui.message.MessageMainFragment;
import com.dl.playfun.ui.mine.MineFragment;
import com.dl.playfun.ui.radio.radiohome.RadioFragment;
import com.dl.playfun.ui.task.main.TaskMainFragment;
import com.dl.playfun.ui.userdetail.detail.UserDetailFragment;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.widget.dialog.MVDialog;
import com.dl.playfun.widget.dialog.TraceDialog;
import com.dl.playfun.widget.dialog.version.view.UpdateDialogView;
import com.dl.playfun.widget.pageview.FragmentAdapter;
import com.tencent.qcloud.tuicore.util.BackgroundTasks;
import com.tencent.qcloud.tuikit.tuiconversation.ui.view.ConversationCommonHolder;

import java.util.List;

import me.goldze.mvvmhabit.bus.RxBus;

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

    private ImageView selTabImgLayout;
    private ViewPager2 mainViewPager;

    private static final long WAIT_TIME = 3000L;

    private long TOUCH_TIME = 0;

    private AliYunMqttClientLifecycle aliYunMqttClientLifecycle;

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

    //异步移除view
    private void postRemoveView(ViewGroup viewGroup, View IiageTrans) {
        viewGroup.post(new Runnable() {
            public void run() {
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        viewGroup.removeView(IiageTrans);
                    }
                });
            }
        });
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        CoinPusherConvertDialog coinpusherDialog = new CoinPusherConvertDialog(mActivity);
        coinpusherDialog.show();
        AppContext.instance().logEvent(AppsFlyerEvent.main_open);
//        aliYunMqttClientLifecycle.broadcastGiftEvent.observe(this, new Observer<MqBroadcastGiftEntity>() {
//            @Override
//            public void onChanged(MqBroadcastGiftEntity mqBroadcastGiftEntity) {
//                viewModel.publicScreenBannerGiftEntity.add(mqBroadcastGiftEntity);
//                viewModel.playBannerGift();
//            }
//        });
        //未付费弹窗
        viewModel.uc.notPaidDialog.observe(this,s -> {

        });
        //每日奖励
        viewModel.uc.showDayRewardDialog.observe(this,s -> {
            showRewardDialog();
        });
        //主页公屏礼物
        viewModel.uc.giftBanner.observe(this,mqttMessageEntity -> {
            MqBroadcastGiftUserEntity leftUser = mqttMessageEntity.getFromUser();
            MqBroadcastGiftUserEntity rightUser = mqttMessageEntity.getToUser();
            View streamerView = View.inflate(MainFragment.this.getContext(), R.layout.fragment_main_gift_banner, null);
            setGiftViewBanner(mqttMessageEntity, leftUser, rightUser, streamerView);
            Animation animation = AnimationUtils
                    .loadAnimation(MainFragment.this.getContext(), R.anim.anim_main_gift_banner_right_to_left);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    postRemoveView(binding.container, streamerView);
                    if (viewModel.publicScreenBannerGiftEntity.size() > 0)
                        viewModel.publicScreenBannerGiftEntity.remove(0);
                    viewModel.playing = false;
                    viewModel.playBannerGift();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            int deviceStatusHeight = ScreenUtils.getDeviceStatusHeight(mActivity);
            layoutParams.setMargins(0,deviceStatusHeight+5,0,0);
            streamerView.setLayoutParams(layoutParams);
            binding.container.addView(streamerView);
            animation.setInterpolator(new AccelerateInterpolator());
            streamerView.startAnimation(animation);
            viewModel.playing = true;
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
        viewModel.uc.restartActivity.observe(this, intent -> {
            startActivity(intent);
            mActivity.overridePendingTransition(R.anim.anim_zoom_in, R.anim.anim_stay);
        });
        viewModel.uc.mainTab.observe(this, new Observer<MainTabEvent>() {
            @Override
            public void onChanged(MainTabEvent mainTabEvent) {
                if (mainTabEvent != null) {
                    switch (mainTabEvent.getTabName()) {
                        case "home":
                            setSelectedItemId(binding.navigationHomeImg);//tbar切换到首頁
                            break;
                        case "plaza":
                            setSelectedItemId(binding.navigationRadioImg);//tbar切换到廣場
                            break;
                        case "message":
                            setSelectedItemId(binding.navigationMessageImg);//tbar切换到訊息
                            break;
                    }
                }
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
                                    .getUpdateDialogView(versionEntity.getVersion_name(), versionEntity.getContent(), versionEntity.getUrl(), isUpdate, "playchat", versionEntity.getLinkUrl())
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
        viewModel.uc.showFaceRecognitionDialog.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void aVoid) {
                showFaceRecognitionDialog();
            }
        });
        viewModel.uc.lockDialog.observe(this, aVoid -> {
            LockDialog dialog = new LockDialog();
            dialog.setPassword(viewModel.lockPassword.get());
            dialog.setLockDialogListener(new LockDialog.LockDialogListener() {
                @Override
                public void onLogoutClick(LockDialog lockDialog) {
                    MVDialog.getInstance(MainFragment.this.getContext())
                            .setContent(getString(R.string.playfun_conflirm_log_out))
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

        viewModel.uc.mainTab.observe(this, new Observer<MainTabEvent>() {
            @Override
            public void onChanged(MainTabEvent mainTabEvent) {
                if (mainTabEvent != null) {
                    switch (mainTabEvent.getTabName()) {
                        case "home":
                            setSelectedItemId(binding.navigationHomeImg);//tbar切换到首頁
                            break;
                        case "redio":
                            setSelectedItemId(binding.navigationRadioImg);//tbar切换到廣場
                            break;
                        case "message":
                            setSelectedItemId(binding.navigationMessageImg);//tbar切换到訊息
                            break;
                        case "mine":
                            setSelectedItemId(binding.navigationMineImg);//tbar切换到訊息
                            break;
                    }
                }
            }
        });
        viewModel.uc.taskCenterclickTab.observe(this, taskMainTabEvent -> {
            if (taskMainTabEvent != null) {
                if (taskMainTabEvent.isTbarClicked()) {//tbar切换到活动中心
                    setSelectedItemId(binding.navigationRankImg);
                }
            }
        });
        viewModel.uc.allMessageCountChange.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                try {
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
    }


    /**
     * 显示奖励dialog
     */
    private void showRewardDialog() {
        if (viewModel.giveCoin <= 0 && viewModel.videoCard <= 0){
            return;
        }
        TraceDialog.getInstance(mActivity)
                .setConfirmOnlick(dialog -> {
                    Injection.provideDemoRepository().putKeyValue(viewModel.dayRewardKey,"true");
                    dialog.dismiss();
                })
                .dayRewardDialog(true,
                        viewModel.nextGiveCoin,
                        viewModel.nextVideoCard,
                        viewModel.giveCoin,
                        viewModel.videoCard)
                .show();
    }

    private void setGiftViewBanner(MqBroadcastGiftEntity mqttMessageEntity, MqBroadcastGiftUserEntity leftUser, MqBroadcastGiftUserEntity rightUser, View streamerView) {
        ImageView leftUserImg = streamerView.findViewById(R.id.left_user_img);
        TextView leftUserName = streamerView.findViewById(R.id.left_user_name);
        leftUserName.setText(leftUser.getNickname());
        ImageView leftUserIcon = streamerView.findViewById(R.id.left_user_icon);
        broadcastGiftImg(leftUser,leftUserIcon);

        Glide.with(getContext())
                .asBitmap()
                .load(StringUtil.getFullImageUrl(leftUser.getAvatar()))
                .error(R.drawable.radio_program_list_content)
                .placeholder(R.drawable.radio_program_list_content)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(leftUserImg);
        leftUserImg.setOnClickListener(v -> {
            Bundle bundle = UserDetailFragment.getStartBundle(leftUser.getId());
            viewModel.start(UserDetailFragment.class.getCanonicalName(), bundle);
        });
        ImageView rightUserImg = streamerView.findViewById(R.id.right_user_img);
        TextView rightUserName = streamerView.findViewById(R.id.right_user_name);
        ImageView rightUserIcon = streamerView.findViewById(R.id.right_user_icon);
        rightUserName.setText(rightUser.getNickname());
        Glide.with(getContext())
                .asBitmap()
                .load(StringUtil.getFullImageUrl(rightUser.getAvatar()))
                .error(R.drawable.radio_program_list_content)
                .placeholder(R.drawable.radio_program_list_content)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(rightUserImg);
        rightUserImg.setOnClickListener(v -> {
            Bundle bundle = UserDetailFragment.getStartBundle(rightUser.getId());
            viewModel.start(UserDetailFragment.class.getCanonicalName(), bundle);
        });
        broadcastGiftImg(rightUser,rightUserIcon);

        TextView giftTitle = streamerView.findViewById(R.id.gift_title);
        giftTitle.setText(mqttMessageEntity.getGiftName());
        ImageView giftNumImg = streamerView.findViewById(R.id.gift_count);
        int account = mqttMessageEntity.getAmount();
        setGiftNumImg(giftNumImg, account);
    }

    private void setGiftNumImg(ImageView giftNumImg, int account) {
        if (account == 1) {
            giftNumImg.setImageResource(R.drawable.img_gift_num1);
        } else if (account == 10) {
            giftNumImg.setImageResource(R.drawable.img_gift_num10);
        } else if (account == 38) {
            giftNumImg.setImageResource(R.drawable.img_gift_num38);
        } else if (account == 66) {
            giftNumImg.setImageResource(R.drawable.img_gift_num66);
        } else if (account == 188) {
            giftNumImg.setImageResource(R.drawable.img_gift_num188);
        } else if (account == 520) {
            giftNumImg.setImageResource(R.drawable.img_gift_num520);
        } else if (account == 1314) {
            giftNumImg.setImageResource(R.drawable.img_gift_num1314);
        } else if (account == 3344) {
            giftNumImg.setImageResource(R.drawable.img_gift_num3344);
        }
    }

    private void broadcastGiftImg(MqBroadcastGiftUserEntity giftUserEntity,ImageView userImg){
        if(giftUserEntity.getSex()!=null && giftUserEntity.getSex()==1){
            if(giftUserEntity.getCertification()!=null && giftUserEntity.getCertification()==1){
                userImg.setImageResource(R.drawable.ic_real_man);
                userImg.setVisibility(View.VISIBLE);
            }
            if(giftUserEntity.getIsVip()!=null && giftUserEntity.getIsVip()==1){
                userImg.setImageResource(R.drawable.ic_vip);
                userImg.setVisibility(View.VISIBLE);
            }
        }else{
            if(giftUserEntity.getCertification()!=null && giftUserEntity.getCertification()==1){
                userImg.setImageResource(R.drawable.ic_real_man);
                userImg.setVisibility(View.VISIBLE);
            }
            if(giftUserEntity.getIsVip()!=null && giftUserEntity.getIsVip()==1){
                userImg.setImageResource(R.drawable.ic_goddess);
                userImg.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void initData() {
        super.initData();
        int ImgSrcPath = TbarCenterImgConfig.getInstance().getImgSrcPath();
        if(ImgSrcPath!=-1){
            binding.navigationRankImg.setImageResource(ImgSrcPath);
        }
//        aliYunMqttClientLifecycle = ((AppContext)mActivity.getApplication()).getBillingClientLifecycle();
//        getLifecycle().addObserver(aliYunMqttClientLifecycle);
        initView();
        try{
            ConversationCommonHolder.sexMale = ConfigManager.getInstance().isMale();
        }catch (Exception exception){

        }


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
        AppContext.isHomePage = !hidden;
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
                        setSelectedItemId(binding.navigationHomeImg);
                        break;
                    case "broadcast":
                        AppConfig.homePageName = null;
                        setSelectedItemId(binding.navigationRadioImg);
                        break;
                    case "navigation_rank":
                        AppConfig.homePageName = null;
                        setSelectedItemId(binding.navigationRankImg);
                        break;
                    case "message":
                        AppConfig.homePageName = null;
                        setSelectedItemId(binding.navigationMessageImg);
                        break;
                    case "user":
                        AppConfig.homePageName = null;
                        setSelectedItemId(binding.navigationMineImg);
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
        String homePageName = ConfigManager.getInstance().getAppRepository().readDefaultHomePageConfig();
        switch (homePageName) {
            case "home":
                setSelectedItemId(binding.navigationHomeImg);
                break;
            case "broadcast":
                setSelectedItemId(binding.navigationRadioImg);
                break;
            case "navigation_rank":
                setSelectedItemId(binding.navigationRankImg);
                break;
            case "message":
                setSelectedItemId(binding.navigationMessageImg);
                break;
            case "user":
                setSelectedItemId(binding.navigationMineImg);
                break;
            default:
                break;
        }
    }

    private void showFaceRecognitionDialog() {
        Animation animation = AnimationUtils.loadAnimation(MainFragment.this.getContext(), R.anim.pop_enter_anim);
        animation.setFillAfter(true);
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
        binding.navigationHome.setOnClickListener(v -> setSelectedItemId(binding.navigationHomeImg));
        binding.navigationRadio.setOnClickListener(v -> setSelectedItemId(binding.navigationRadioImg));
        binding.navigationRank.setOnClickListener(v -> setSelectedItemId(binding.navigationRankImg));
        binding.navigationMessage.setOnClickListener(v -> setSelectedItemId(binding.navigationMessageImg));
        binding.navigationMine.setOnClickListener(v -> setSelectedItemId(binding.navigationMineImg));
        binding.bubbleTip.setOnClickListener(v -> setSelectedItemId(binding.navigationMessageImg));
        mainViewPager = binding.viewPager;
        FragmentAdapter fragmentAdapter = new FragmentAdapter(this);
        fragmentAdapter.setFragmentList(mFragments);
        // 关闭左右滑动切换页面
        mainViewPager.setUserInputEnabled(false);
        // 设置缓存数量 避免销毁重建
        mainViewPager.setOffscreenPageLimit(5);
        mainViewPager.setAdapter(fragmentAdapter);
        mainViewPager.setCurrentItem(0, false);

        if (selTabImgLayout == null) {
            selTabImgLayout = binding.navigationHomeImg;
        } else {
            // 初始化时，强制切换tab到上一次的位置
            ImageView tmp = selTabImgLayout;
            setSelectedItemId(tmp);
            selTabImgLayout = tmp;
        }
        lottieAddAnimatorListener();
    }

    private void setSelectedItemId(ImageView view) {
        if(selTabImgLayout==null){
            selTabImgLayout = view;
        }
        if (selTabImgLayout != null && selTabImgLayout.getId() != view.getId()) {
            resetMenuState(selTabImgLayout.getId());
            int id = view.getId();
            if (id == R.id.navigation_home_img) {
                mainViewPager.setCurrentItem(0, false);
                //binding.navigationHomeImg.setVisibility(View.VISIBLE);
                binding.navigationHomeImg.setVisibility(View.INVISIBLE);
                binding.navigationHomeImgLottie.setVisibility(View.VISIBLE);
                binding.navigationHomeImgLottie.playAnimation();
                binding.navigationHomeImg.setImageResource(R.drawable.tab_home_checked);
            } else if (id == R.id.navigation_radio_img) {
                mainViewPager.setCurrentItem(1, false);
                binding.navigationRadioImg.setVisibility(View.INVISIBLE);
                binding.navigationRadioImgLottie.setVisibility(View.VISIBLE);
                binding.navigationRadioImgLottie.playAnimation();
                binding.navigationRadioImg.setImageResource(R.drawable.tab_radio_checked);
            } else if (id == R.id.navigation_rank_img) {
                mainViewPager.setCurrentItem(2, false);
            } else if (id == R.id.navigation_message_img) {
                mainViewPager.setCurrentItem(3, false);
                binding.navigationMessageImg.setVisibility(View.INVISIBLE);
                binding.navigationMessageImgLottie.setVisibility(View.VISIBLE);
                binding.navigationMessageImgLottie.playAnimation();
                binding.navigationMessageImg.setImageResource(R.drawable.tab_message_checked);
            } else if (id == R.id.navigation_mine_img) {
                mainViewPager.setCurrentItem(4, false);
                binding.navigationMineImg.setVisibility(View.INVISIBLE);
                binding.navigationMineImgLottie.setVisibility(View.VISIBLE);
                binding.navigationMineImgLottie.playAnimation();
                if (viewModel.uc.gender.get()) {
                    binding.navigationMineImg.setImageResource(R.drawable.tab_mine_male_checked);
                } else {
                    binding.navigationMineImg.setImageResource(R.drawable.tab_mine_female_checked);
                }
            }
            selTabImgLayout = view;
        }
    }

    //初始化按钮状态
    private void resetMenuState(int id) {
        if (id == R.id.navigation_home_img) { //首页
            binding.navigationHomeImg.setImageResource(R.drawable.tab_home_normal);
        } else if (id == R.id.navigation_radio_img) { //广场
            binding.navigationRadioImg.setImageResource(R.drawable.tab_radio_normal);
        } else if (id == R.id.navigation_rank_img) { //任务中心
            int ImgSrcPath = TbarCenterImgConfig.getInstance().getImgSrcPath();
            if(ImgSrcPath!=-1){
                binding.navigationRankImg.setImageResource(ImgSrcPath);
            }else{
                binding.navigationRankImg.setImageResource(R.drawable.toolbar_icon_task_checked);
            }
            binding.navigationRankText.setTextColor(getResources().getColor(R.color.navigation_checkno));
        } else if (id == R.id.navigation_message_img) { //讯息页面
            binding.navigationMessageImg.setImageResource(R.drawable.tab_message_normal);
        } else if (id == R.id.navigation_mine_img) { //我的页面
            if (viewModel.uc.gender.get()) {
                binding.navigationMineImg.setImageResource(R.drawable.tab_mine_male_image);
            } else {
                binding.navigationMineImg.setImageResource(R.drawable.tab_mine_female_normal);
            }
        }
    }
    //添加lottie监听
    private void lottieAddAnimatorListener(){
        binding.navigationHomeImgLottie.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                binding.navigationHomeImgLottie.setVisibility(View.GONE);
                binding.navigationHomeImg.setVisibility(View.VISIBLE);
            }
        });
        binding.navigationRadioImgLottie.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                binding.navigationRadioImgLottie.setVisibility(View.GONE);
                binding.navigationRadioImg.setVisibility(View.VISIBLE);
            }
        });
        binding.navigationMessageImgLottie.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                binding.navigationMessageImgLottie.setVisibility(View.GONE);
                binding.navigationMessageImg.setVisibility(View.VISIBLE);
            }
        });
        binding.navigationMineImgLottie.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                binding.navigationMineImgLottie.setVisibility(View.GONE);
                binding.navigationMineImg.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected boolean isUmengReportPage() {
        return false;
    }
}
