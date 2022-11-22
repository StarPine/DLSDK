package com.dl.playfun.kl.view;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dl.lib.util.log.MPTimber;
import com.dl.manager.LocaleManager;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.databinding.ActivityCallVideoBinding;
import com.dl.playfun.entity.CallingInfoEntity;
import com.dl.playfun.entity.CallingInviteInfo;
import com.dl.playfun.entity.CoinPusherDataInfoEntity;
import com.dl.playfun.entity.CrystalDetailsConfigEntity;
import com.dl.playfun.entity.GiftBagEntity;
import com.dl.playfun.entity.GoodsEntity;
import com.dl.playfun.event.CallVideoUserEnterEvent;
import com.dl.playfun.kl.Utils;
import com.dl.playfun.kl.viewmodel.UITRTCCallingDelegate;
import com.dl.playfun.kl.viewmodel.VideoCallViewModel;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.coinpusher.CoinPusherGameActivity;
import com.dl.playfun.ui.coinpusher.GameCallEntity;
import com.dl.playfun.ui.coinpusher.dialog.CoinPusherRoomListDialog;
import com.dl.playfun.ui.dialog.GiftBagDialog;
import com.dl.playfun.utils.AutoSizeUtils;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.utils.LogUtils;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.widget.coinrechargesheet.CoinRechargeSheetView;
import com.dl.playfun.widget.dialog.MessageDetailDialog;
import com.dl.playfun.widget.dialog.TraceDialog;
import com.dl.playfun.widget.image.CircleImageView;
import com.dl.rtc.calling.base.DLRTCCalling;
import com.dl.rtc.calling.base.impl.DLRTCInternalListenerManager;
import com.dl.rtc.calling.manager.DLRTCStartShowUIManager;
import com.dl.rtc.calling.manager.DLRTCVideoManager;
import com.dl.rtc.calling.model.DLRTCCallingConstants;
import com.dl.rtc.calling.model.DLRTCDataMessageType;
import com.dl.rtc.calling.ui.videolayout.DLRTCVideoLayout;
import com.dl.rtc.calling.ui.videolayout.VideoLayoutFactory;
import com.faceunity.nama.FURenderer;
import com.faceunity.nama.data.FaceUnityDataFactory;
import com.faceunity.nama.ui.FaceUnityView;
import com.google.gson.Gson;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGASoundManager;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.tencent.custom.GiftEntity;
import com.tencent.qcloud.tuicore.util.ConfigManagerUtil;
import com.tencent.trtc.TRTCCloudDef;

import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import me.goldze.mvvmhabit.base.BaseActivity;
import me.goldze.mvvmhabit.bus.RxBus;
import me.tatarka.bindingcollectionadapter2.BR;

public class CallingVideoActivity extends BaseActivity<ActivityCallVideoBinding, VideoCallViewModel>  {

    /**
     * 美颜相关
     */
    private FaceUnityView mFaceUnityView;
    private FaceUnityDataFactory mFaceUnityDataFactory;
    private FURenderer mFURenderer;
    private final boolean isFuEffect = true;

    private Context mContext;

    private ObjectAnimator rotation;

    private CallingInviteInfo callingInviteInfo;
    //拨打方UserId
    private String inviteUserID;
    //接听方
    private String acceptUserID;
    private Integer roomId;
    private DLRTCCalling.Role role;

    private int mTimeCount;
    //每个10秒+1
    private int mTimeTen;
    private Timer timer;

    private SVGAImageView giftEffects;
    //当前页面是否重启（逻辑为通话中再次进入当前通话页面）
    private boolean isRestart = false;

    private Runnable timerRunnable = null;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            return false;
        }
    });

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.setLocal(newBase));
    }

    /**
     * 就算你在Manifest.xml设置横竖屏切换不重走生命周期。横竖屏切换还是会走这里

     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if(newConfig!=null){
            LocaleManager.setLocal(this);
        }
        super.onConfigurationChanged(newConfig);
        LocaleManager.setLocal(this);
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        LocaleManager.setLocal(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppContext.isCalling = true;
        ImmersionBarUtils.setupStatusBar(this, false, true);
        if (isFuEffect && mFURenderer != null) {
            mFaceUnityDataFactory.bindCurrentRenderer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ImmersionBarUtils.setupStatusBar(this, true, true);
    }

    @Override
    public int initContentView(Bundle savedInstanceState) {
        AutoSizeUtils.applyAdapt(this.getResources());
        return R.layout.activity_call_video;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public VideoCallViewModel initViewModel() {
        //使用自定义的ViewModelFactory来创建ViewModel，如果不重写该方法，则默认会调用LoginViewModel(@NonNull Application application)构造方法
        AppViewModelFactory factory = AppViewModelFactory.getInstance(getApplication());
        return ViewModelProviders.of(this, factory).get(VideoCallViewModel.class);
    }

    @Override
    public void initParam() {
        super.initParam();
        //防窥屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        //屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContext = this;
        SVGASoundManager.INSTANCE.init();
        SVGAParser.Companion.shareParser().init(this);

        Intent intent = getIntent();
        isRestart = intent.getBooleanExtra("isRestart",false);
        role = (DLRTCCalling.Role) intent.getExtras().get(DLRTCCallingConstants.PARAM_NAME_ROLE);
        roomId = intent.getIntExtra("roomId", 0);
        //被动接收
        acceptUserID = intent.getExtras().getString(DLRTCCallingConstants.DLRTCAcceptUserID);
        //主动呼叫
        inviteUserID = intent.getExtras().getString(DLRTCCallingConstants.DLRTCInviteUserID);
        String userData = intent.getExtras().getString("userProfile");
        if (userData != null) {
            callingInviteInfo = new Gson().fromJson(userData, CallingInviteInfo.class);
        }
        DLRTCInternalListenerManager.Companion.getInstance().addDelegate(mTRTCCallingListener);
    }

    /**
     * 去充值
     */
    private void toRecharge() {
        CoinRechargeSheetView coinRechargeFragmentView = new CoinRechargeSheetView(this);
        coinRechargeFragmentView.setClickListener(new CoinRechargeSheetView.ClickListener() {
            @Override
            public void paySuccess(GoodsEntity goodsEntity) {
                viewModel.getCallingStatus(roomId);
            }
        });
        coinRechargeFragmentView.show();
    }

    @Override
    public void initData() {
        super.initData();
        hideExchangeRules();
        giftEffects = binding.giftEffects;
        mFaceUnityView = binding.fuView;
        mFURenderer = FURenderer.getInstance();
        mFaceUnityDataFactory = new FaceUnityDataFactory(0);
        mFaceUnityView.bindDataFactory(mFaceUnityDataFactory);
        DLRTCVideoManager.Companion.getInstance().createCustomRenderer(isFuEffect);
        if(isRestart){
            viewModel.mainVIewShow.set(true);
            viewModel.isCalledWaitingBinding.set(false);
        }
        binding.rtcLayoutManager.initVideoFactory(new VideoLayoutFactory(this));
        // 不用TRTC sponsor 一套的命名，因为他们那里其实挺混乱的， 这里就用 my 和 other
        if (role == DLRTCCalling.Role.CALL) {//主动呼叫
            viewModel.userCall = true;
            if (callingInviteInfo != null) {
                viewModel.init(inviteUserID, acceptUserID, role, callingInviteInfo.getRoomId());
                viewModel.callingInviteInfoField.set(callingInviteInfo);
                if (callingInviteInfo.getUserProfileInfo().getSex() == 1) {
                    if (!ObjectUtils.isEmpty(callingInviteInfo.getMessages()) && callingInviteInfo.getMessages().size() > 0) {
                        StringBuilder valueData = new StringBuilder();
                        for (String value : callingInviteInfo.getMessages()) {
                            valueData.append(value).append("\n");
                        }
                        viewModel.callHintBinding.set(valueData.toString());
                    }
                }
            }
        } else {//被动接听
            viewModel.init(acceptUserID, inviteUserID, role);
            viewModel.getCallingInvitedInfo(2, inviteUserID);
        }
        launcherPermissionArray.launch(new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA});
    }

    //多个权限申请监听
    ActivityResultLauncher<String[]> launcherPermissionArray = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                if (result.get(Manifest.permission.CAMERA) != null && result.get(Manifest.permission.RECORD_AUDIO) != null) {
                    if (Objects.requireNonNull(result.get(Manifest.permission.CAMERA)).equals(true) && Objects.requireNonNull(result.get(Manifest.permission.RECORD_AUDIO)).equals(true)) {
                        //权限全部获取到之后的动作
                        DLRTCVideoLayout videoLayout = binding.rtcLayoutManager.allocCloudVideoView(role == DLRTCCalling.Role.CALL? inviteUserID : acceptUserID);
                        if(videoLayout!=null){
                            DLRTCVideoManager.Companion.getInstance().openCamera(true, videoLayout.getVideoView());
                        }
                    } else {
                        //有权限没有获取到的动作
                        //alertPermissions();
                    }
                }
            });

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        //水晶兑换规则
        viewModel.uc.clickCrystalExchange.observe(this, data -> {
            TraceDialog.getInstance(CallingVideoActivity.this)
                    .setConfirmOnlick(new TraceDialog.ConfirmOnclick() {
                        @Override
                        public void confirm(Dialog dialog) {
                            ConfigManagerUtil.getInstance().putExchangeRulesFlag(true);
                            viewModel.isHideExchangeRules.set(true);
                        }
                    })
                    .getCrystalExchange(data)
                    .show();
        });

        //破冰文案刷新動畫
        viewModel.uc.startVideoUpSayHiAnimotor.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {
                if (rotation == null){
                    rotation = ObjectAnimator.ofFloat(binding.ivVideoUpSayHi, "rotation", 0.0F, 360.0F);
                }
                if (!rotation.isRunning()){
//                    rotation.setRepeatMode(ValueAnimator.RESTART);
//                    rotation.setRepeatCount(-1);
                    rotation.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            viewModel.getSayHiList();
                        }
                    });
                    rotation.setDuration(800);//设置旋转时间
                    rotation.start();//开始执行动画（顺时针旋转动画
                }

            }
        });
        //公屏消息滚动到底部
        viewModel.uc.scrollToEnd.observe(this, unused -> binding.rcvLayout.scrollToPosition(viewModel.adapter.getItemCount() - 1));
        //关闭按钮点击事件回调
        viewModel.uc.closeViewHint.observe(this, unused -> onBackViewCLick());
        //接收礼物效果展示
        viewModel.uc.acceptUserGift.observe(this, giftEntity -> {
            viewModel.getCallingStatus(viewModel.roomId);
            try {
                int account = giftEntity.getAmount();
                //启动SVG动画
                startVideoAcceptSVGAnimotion(giftEntity);
                //启动横幅动画
                startVideoAcceptBannersAnimotion(giftEntity, account);
                //启动头像动画
                startVideoAcceptHeadAnimotion(giftEntity);
            } catch (Exception e) {

            }
        });
        //发送礼物效果展示
        viewModel.uc.sendUserGiftAnim.observe(this, stringObjectMap -> {
            try {
                int account = (int) stringObjectMap.get("account");
                GiftBagEntity.giftEntity giftEntity = (GiftBagEntity.giftEntity) stringObjectMap.get("giftEntity");
                //启动SVG动画
                startVideoSendSvgAnimotion(giftEntity);
                //启动横幅动画
                startVideoSendBannersAnimotion(account, giftEntity);
                //启动头像动画
                startVideoSendHeadAnimotion(giftEntity);
            } catch (Exception e) {

            }
        });
        //接听成功
        viewModel.uc.callAudioStart.observe(this, unused -> {
            //开始记时
            TimeCallMessage();
            setTimerForCallinfo();
        });
        //钻石不足充值弹窗
        viewModel.uc.sendUserGiftError.observe(this, isGiftSend -> toRecharge());
        //发送礼物弹窗
        viewModel.uc.callGiftBagAlert.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {
                if (viewModel.unitPriceList == null ||  viewModel.maleBalanceMoney == 0){
                    return;
                }
                GiftBagDialog giftBagDialog = new GiftBagDialog(mContext, true, viewModel.maleBalanceMoney, viewModel.unitPriceList.size() > 1 ? 4 : 0);
                giftBagDialog.setGiftOnClickListener(new GiftBagDialog.GiftOnClickListener() {
                    @Override
                    public void sendGiftClick(Dialog dialog, int number, GiftBagEntity.giftEntity giftEntity) {
                        dialog.dismiss();
                        AppContext.instance().logEvent(AppsFlyerEvent.videocall_send_gift);
                        viewModel.sendUserGift(dialog, giftEntity, viewModel.callingVideoInviteInfoField.get().getId(), number);
                    }

                    @Override
                    public void rechargeStored(Dialog dialog) {
                        dialog.dismiss();
                        AppContext.instance().logEvent(AppsFlyerEvent.videocall_gift_topup);
                        viewModel.uc.sendUserGiftError.postValue(false);
                    }
                });
                giftBagDialog.show();
            }
        });
        //弹出推币机选择弹窗
        viewModel.uc.coinPusherRoomEvent.observe(this, unused -> {
            //弹出推币机选择弹窗
            CoinPusherRoomListDialog coinersDialog = new CoinPusherRoomListDialog(mContext);
            coinersDialog.setDialogEventListener(new CoinPusherRoomListDialog.DialogEventListener() {
                @Override
                public void startViewing(CoinPusherDataInfoEntity itemEntity) {
                    coinersDialog.dismiss();
                    Intent intent = new Intent(mContext, CoinPusherGameActivity.class);
                    intent.putExtra("CoinPusherInfo",itemEntity);
                    //创建玩游戏模型
                    GameCallEntity gameCallEntity = new GameCallEntity();
                    gameCallEntity.setRoomId(viewModel.roomId);
                    gameCallEntity.setInviteUserId(inviteUserID);
                    gameCallEntity.setAcceptUserId(acceptUserID);
                    gameCallEntity.setCallingRole(role);
                    gameCallEntity.setCallingType(DLRTCDataMessageType.DLInviteRTCType.dl_rtc_video);
                    gameCallEntity.setCalling(true);
                    if(viewModel.callingInviteInfoField.get()!=null){
                        gameCallEntity.setNickname(viewModel.callingInviteInfoField.get().getUserProfileInfo().getCityName());
                        gameCallEntity.setAvatar(viewModel.callingInviteInfoField.get().getUserProfileInfo().getAvatar());
                    }
                    intent.putExtra("GameCallEntity",gameCallEntity);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void buyErrorPayView() {
                    toRecharge();
                }
            });
            coinersDialog.show();
        });
    }


    /**
     * 隐藏水晶兑换规则弹框
     */
    private void hideExchangeRules() {
        CrystalDetailsConfigEntity crystalDetailsConfig = ConfigManager.getInstance().getAppRepository().readCrystalDetailsConfig();
        boolean isHideExchangeRules = ConfigManagerUtil.getInstance().getExchangeRulesFlag();
        boolean isMale = ConfigManager.getInstance().isMale();
        if (isMale){
            if (crystalDetailsConfig.getMaleIsShow() != 1 || isHideExchangeRules){
                viewModel.isHideExchangeRules.set(true);
            }else {
                viewModel.isHideExchangeRules.set(false);
            }
        }else {
            if (crystalDetailsConfig.getFemaleIsShow() != 1 || isHideExchangeRules){
                viewModel.isHideExchangeRules.set(true);
            }else {
                viewModel.isHideExchangeRules.set(false);
            }
        }
    }

    private void setTimerForCallinfo() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if(isFinishing() || isDestroyed()){
                        return;
                    }
                    viewModel.getCallingStatus(viewModel.roomId);
                }catch (Exception ignored){

                }
            }
        }, 1000,10000);
    }

    private void startVideoSendHeadAnimotion(GiftBagEntity.giftEntity giftEntity) {
        ImageView giftImageTrans = new ImageView(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dip2px(50), dip2px(50));
        layoutParams.bottomMargin = dip2px(43);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParams.gravity = Gravity.BOTTOM;
        giftImageTrans.setLayoutParams(layoutParams);
        giftImageTrans.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_call_video_send_tip);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                postRemoveView(binding.mainView,giftImageTrans);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        Glide.with(mContext)
                .asBitmap()
                .load(StringUtil.getFullImageUrl(giftEntity.getImg()))
                .error(R.drawable.radio_program_list_content)
                .placeholder(R.drawable.radio_program_list_content)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(giftImageTrans);
        binding.mainView.addView(giftImageTrans);
        giftImageTrans.startAnimation(animation);
    }

    private void startVideoSendBannersAnimotion(int account, GiftBagEntity.giftEntity giftEntity) {
        if (account > 1) {
            View streamerView = View.inflate(mContext, R.layout.call_user_streamer_item, null);
            //用户头像
            CircleImageView userImg = streamerView.findViewById(R.id.user_img);
            //礼物图片
            ImageView giftImg = streamerView.findViewById(R.id.gift_img);
            //礼物图片数量
            ImageView giftNumImg = streamerView.findViewById(R.id.gift_num_img);
            //文案
            TextView tipText = streamerView.findViewById(R.id.tip_text);
            String sexText = StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt17);
            String messageText = StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt1);
            String itemTextMessage = sexText + messageText + "\n" + viewModel.callingVideoInviteInfoField.get().getNickname();
            SpannableString stringBuilder = new SpannableString(itemTextMessage);
            stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint2)), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.white)), 1, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint2)), 3, itemTextMessage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tipText.setText(stringBuilder);
            Glide.with(mContext)
                    .asBitmap()
                    .load(StringUtil.getFullImageUrl(viewModel.readUserData().getAvatar()))
                    .error(R.drawable.default_avatar)
                    .placeholder(R.drawable.default_avatar)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(userImg);
            Glide.with(mContext)
                    .asBitmap()
                    .load(StringUtil.getFullImageUrl(giftEntity.getImg()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(giftImg);
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

            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_call_audio_right_in_streamer);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    postRemoveView(binding.mainView,streamerView);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.leftMargin = dip2px(14);
            layoutParams.topMargin = dip2px(330);
            streamerView.setLayoutParams(layoutParams);
            binding.mainView.addView(streamerView);
            streamerView.startAnimation(animation);
        }
    }

    private void startVideoSendSvgAnimotion(GiftBagEntity.giftEntity giftEntity) {
        SVGAParser svgaParser = SVGAParser.Companion.shareParser();
        try {
            svgaParser.decodeFromURL(new URL(StringUtil.getFullAudioUrl(giftEntity.getLink())), new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                    giftEffects.setVisibility(View.VISIBLE);
                    giftEffects.setVideoItem(videoItem);
                    giftEffects.setLoops(1);
                    giftEffects.setCallback(new SVGACallback() {
                        @Override
                        public void onPause() {

                        }

                        @Override
                        public void onFinished() {
                            giftEffects.setVisibility(View.GONE);
                        }

                        @Override
                        public void onRepeat() {
                        }

                        @Override
                        public void onStep(int i, double v) {
                        }
                    });
                    giftEffects.startAnimation();
                }

                @Override
                public void onError() {
                }
            }, null);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void startVideoAcceptHeadAnimotion(GiftEntity giftEntity) {
        ImageView giftImageTrans = new ImageView(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dip2px(32), dip2px(32));
        layoutParams.gravity = Gravity.START;
        layoutParams.leftMargin = dip2px(17);
        layoutParams.topMargin = dip2px(44);
        giftImageTrans.setLayoutParams(layoutParams);
        giftImageTrans.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_call_video_receive_tip);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                postRemoveView(binding.mainView,giftImageTrans);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        Glide.with(mContext)
                .asBitmap()
                .load(StringUtil.getFullImageUrl(giftEntity.getImgPath()))
                .error(R.drawable.radio_program_list_content)
                .placeholder(R.drawable.radio_program_list_content)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(giftImageTrans);
        giftImageTrans.setElevation(22);
        binding.mainView.addView(giftImageTrans);
        giftImageTrans.startAnimation(animation);
    }

    //横幅动画
    private void startVideoAcceptBannersAnimotion(GiftEntity giftEntity, int account) {
        if (account > 1) {
            View streamerView = View.inflate(mContext, R.layout.call_user_streamer_item, null);
            //用户头像
            CircleImageView userImg = streamerView.findViewById(R.id.user_img);
            //礼物图片
            ImageView giftImg = streamerView.findViewById(R.id.gift_img);
            //礼物图片数量
            ImageView giftNumImg = streamerView.findViewById(R.id.gift_num_img);
            //文案
            TextView tipText = streamerView.findViewById(R.id.tip_text);
            String sexText = viewModel.callingVideoInviteInfoField.get().getNickname();
            String messageText = StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt1);
            String lastText = StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt17);
            String itemTextMessage = sexText + "\n" + messageText + lastText;
            SpannableString stringBuilder = new SpannableString(itemTextMessage);
            stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint2)), 0, sexText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.white)), sexText.length(), sexText.length() + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint2)), sexText.length() + 2, itemTextMessage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tipText.setText(stringBuilder);
            Glide.with(mContext)
                    .asBitmap()
                    .load(StringUtil.getFullImageUrl(viewModel.callingVideoInviteInfoField.get().getAvatar()))
                    .error(R.drawable.default_avatar)
                    .placeholder(R.drawable.default_avatar)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(userImg);
            Glide.with(mContext)
                    .asBitmap()
                    .load(StringUtil.getFullImageUrl(giftEntity.getImgPath()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(giftImg);
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
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_call_audio_right_in_streamer);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    postRemoveView(binding.mainView,streamerView);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.leftMargin = dip2px(14);
            layoutParams.topMargin = dip2px(330);
            streamerView.setElevation(15);
            streamerView.setLayoutParams(layoutParams);
            binding.mainView.addView(streamerView);
            streamerView.startAnimation(animation);
        }
    }

    private void startVideoAcceptSVGAnimotion(GiftEntity giftEntity) {
        if (!StringUtils.isEmpty(giftEntity.getSvgaPath())) {
            SVGAParser svgaParser = SVGAParser.Companion.shareParser();
            try {
                svgaParser.decodeFromURL(new URL(StringUtil.getFullAudioUrl(giftEntity.getSvgaPath())), new SVGAParser.ParseCompletion() {
                    @Override
                    public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                        Log.d("##", "## FromNetworkActivity load onComplete");
                        giftEffects.setVisibility(View.VISIBLE);
                        giftEffects.setVideoItem(videoItem);
                        giftEffects.setLoops(1);
                        giftEffects.setCallback(new SVGACallback() {
                            @Override
                            public void onPause() {
                            }

                            @Override
                            public void onFinished() {
                                //播放完成
                                giftEffects.setVisibility(View.GONE);
                            }

                            @Override
                            public void onRepeat() {
                            }

                            @Override
                            public void onStep(int i, double v) {
                            }
                        });
                        giftEffects.startAnimation();
                    }

                    @Override
                    public void onError() {
                    }
                }, null);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    //异步移除view
    private void postRemoveView(ViewGroup viewGroup, View IiageTrans) {
        viewGroup.post(new Runnable() {
            public void run () {
                // it works without the runOnUiThread, but all UI updates must
                // be done on the UI thread
                CallingVideoActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        viewGroup.removeView(IiageTrans);
                    }
                });
            }
        });
    }

    //返回按键
    @Override
    public void onBackPressed() {
        onBackViewCLick();
    }

    //返回按钮调用代码
    public void onBackViewCLick() {
        if (viewModel.collected == null) {
            viewModel.hangup();
            return;
        }
        if (viewModel.isMale) {
            if (viewModel.collected == 1) {//已追踪
                String title = StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt9);
                MessageDetailDialog.callAudioHint2(mContext, title, null, new MessageDetailDialog.AudioCallHintOnClickListener() {
                    @Override
                    public void check1OnClick() {
                        AppContext.instance().logEvent(AppsFlyerEvent.videocall_close_hangup_M);
                        viewModel.hangup();
                    }

                    @Override
                    public void check2OnClick() {
                        AppContext.instance().logEvent(AppsFlyerEvent.videocall_close_goon_M);
                    }
                }).show();
            } else {//没有追踪
                MessageDetailDialog.callAudioHint(mContext, new MessageDetailDialog.AudioCallHintOnClickListener() {
                    @Override
                    public void check1OnClick() {
                        AppContext.instance().logEvent(AppsFlyerEvent.videocall_close_follow_M);
                        viewModel.addLike(true);
                    }

                    @Override
                    public void check2OnClick() {
                        AppContext.instance().logEvent(AppsFlyerEvent.videocall_close_hangup_M);
                        viewModel.hangup();
                    }
                }).show();
            }
        } else {
            String title = mContext.getString(R.string.playfun_call_message_deatail_girl_txt13);
            String content = mContext.getString(R.string.playfun_call_message_deatail_girl_txt12);
            MessageDetailDialog.callAudioHint2(mContext, title, content, new MessageDetailDialog.AudioCallHintOnClickListener() {
                @Override
                public void check1OnClick() {
                    AppContext.instance().logEvent(AppsFlyerEvent.videocall_close_hangup_F);
                    viewModel.hangup();
                }

                @Override
                public void check2OnClick() {
                    AppContext.instance().logEvent(AppsFlyerEvent.videocall_close_goon_F);
                }
            }).show();
        }
    }

    /**
     * 两分钟发送一次在线信息
     */
    public void TimeCallMessage() {
        if (timerRunnable != null) {
            return;
        }
        timerRunnable = () -> {
            mTimeCount++;
            viewModel.TimeCount++;
            viewModel.timeTextField.set(mContext.getString(R.string.playfun_call_message_deatail_time_msg, mTimeCount/3600, mTimeCount / 60, mTimeCount % 60));
            if (mTimeCount>=5){viewModel.tipSwitch.set(false);}
            if (!viewModel.sayHiEntityHidden.get() && mTimeCount % 10 == 0) {
                //没10秒更新一次破冰文案
                viewModel.getSayHiList();
            }
            if (mTimeCount % 30 == 0){
                viewModel.getRoomStatus(viewModel.roomId);
            }
            if (viewModel.callInfoLoaded && viewModel.isShowTipMoney){
                //判断是否为付费方
                if (!viewModel.isPayee) {
                    if (viewModel.totalMinutesRemaining <= viewModel.balanceNotEnoughTipsMinutes * 60) {
                        viewModel.totalMinutesRemaining--;
                        if (viewModel.totalMinutesRemaining < 0) {
                            viewModel.hangup();
                            return;
                        }
                        String minute = StringUtils.getString(R.string.playfun_minute);
                        String textHint = (viewModel.totalMinutesRemaining / 60) + minute + (viewModel.totalMinutesRemaining % 60);
                        String txt = String.format(StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt14), textHint);
                        viewModel.maleTextMoneyField.set(txt);
                        if (!viewModel.flagMoneyNotWorth) {
                            moneyNoWorthSwich(true);
                        }

                    }else{
                        if (viewModel.flagMoneyNotWorth) {
                            moneyNoWorthSwich(false);
                        }
                    }
                }else {
                    setProfitTips();
                }
            }

            mHandler.postDelayed(timerRunnable, 1000);
        };
        mHandler.postDelayed(timerRunnable, 1000);
    }

    /**
     * 展示右下角收益提示
     */
    private void setProfitTips() {
        if (!viewModel.isShowCountdown.get() && viewModel.payeeProfits > 0) {//对方余额不足没有展示
            if (!viewModel.girlEarningsField.get()) {
                viewModel.girlEarningsField.set(true);
            }
            String profit = viewModel.payeeProfits + "";
            String girlEarningsTex = String.format(StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt), profit);
            SpannableString stringBuilder = new SpannableString(girlEarningsTex);
            ForegroundColorSpan blueSpan = new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint1));
            int index = girlEarningsTex.indexOf(profit);
            stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.white)), 0, girlEarningsTex.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            stringBuilder.setSpan(blueSpan, index, index + profit.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            viewModel.girlEarningsText.set(stringBuilder);
        }
    }

    /**
     * 余额不足推送与显示
     * @param isShow
     */
    private void moneyNoWorthSwich(boolean isShow) {
        viewModel.flagMoneyNotWorth = isShow;
        viewModel.maleTextLayoutSHow.set(isShow);
        //通知女生男生这边余额不足
        if (isShow){
            viewModel.getTips(viewModel.callingVideoInviteInfoField.get().getId(),2,"1");
        }else {
            viewModel.getTips(viewModel.callingVideoInviteInfoField.get().getId(),2,"0");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DLRTCInternalListenerManager.Companion.getInstance().removeDelegate(mTRTCCallingListener);
        DLRTCStartShowUIManager.Companion.getInstance().stopRing();
        AppContext.isCalling = false;
        //取消窥屏
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        //取消常亮
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (mHandler != null) {
            mHandler.removeCallbacks(timerRunnable);
            mHandler = null;
        }
        if (timer != null){
            timer.cancel();
            timer = null;
        }
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dip2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    /**
     * @Desc TODO(监听RTC信令回调)
     * @author 彭石林
     * @Date 2022/11/9
     */

    private final UITRTCCallingDelegate mTRTCCallingListener = new UITRTCCallingDelegate() {
        @Override
        public void onError(int code, String msg) {
        }

        @Override
        public void onUserEnter(String userId) {
            DLRTCStartShowUIManager.Companion.getInstance().stopRing();
            //发送订阅事件通知有人加入了视频聊天房
            RxBus.getDefault().post(new CallVideoUserEnterEvent(userId));
        }

        @Override
        public void onUserLeave(String userId) {
            stopCameraAndFinish();
        }

        @Override
        public void onReject(String userId) {
            Utils.show(AppContext.instance().getString(R.string.playfun_the_other_party_refuses_to_answer));
            stopCameraAndFinish();
        }

        @Override
        public void onCallingCancel() {
            Utils.show(AppContext.instance().getString(R.string.playfun_the_other_party_cancels_the_call));
            stopCameraAndFinish();
        }

        @Override
        public void onCallingTimeout() {
            Utils.show(AppContext.instance().getString(R.string.playfun_the_other_party_is_temporarily_unavailable));
            stopCameraAndFinish();
        }

        @Override
        public void onCallEnd() {
            Utils.show(AppContext.instance().getString(R.string.playfun_call_ended));
            stopCameraAndFinish();
        }

        @Override
        public void onUserVideoAvailable(String userId, boolean isVideoAvailable) {
            MPTimber.tag("视频聊天页面：").d("有用户进来了："+userId+" , isVideoAvailable："+isVideoAvailable);
            if(isVideoAvailable){
                //有用户的视频开启了
                DLRTCVideoLayout layout = binding.rtcLayoutManager.allocCloudVideoView(userId);
                if (layout != null) {
                    DLRTCVideoManager.Companion.getInstance().startRemoteView(userId, layout.getVideoView());
                }
            }
        }

        @Override
        public void onUserAudioAvailable(String userId, boolean isVideoAvailable) {
            super.onUserAudioAvailable(userId, isVideoAvailable);
        }

        @Override
        public void onUserVoiceVolume(Map<String, Integer> volumeMap) {
            super.onUserVoiceVolume(volumeMap);
        }

        @Override
        public void onNetworkQuality(TRTCCloudDef.TRTCQuality localQuality, ArrayList<TRTCCloudDef.TRTCQuality> remoteQuality) {
            super.onNetworkQuality(localQuality, remoteQuality);
        }

        @Override
        public void onTryToReconnect() {
            super.onTryToReconnect();
        }
    };

    private void stopCameraAndFinish() {
        DLRTCVideoManager.Companion.getInstance().stopLocalPreview();
        finish();
    }

}
