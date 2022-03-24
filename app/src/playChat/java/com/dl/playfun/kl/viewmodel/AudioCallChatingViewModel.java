package com.dl.playfun.kl.viewmodel;

import android.app.Application;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.databinding.ObservableList;

import com.aliyun.common.utils.ToastUtil;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.EaringlSwitchUtil;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.CallingInfoEntity;
import com.dl.playfun.entity.CustomMessageIMTextEntity;
import com.dl.playfun.entity.GiftBagEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.event.AudioCallingCancelEvent;
import com.dl.playfun.kl.Utils;
import com.dl.playfun.kl.view.Ifinish;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.utils.ChatUtils;
import com.dl.playfun.utils.LogUtils;
import com.dl.playfun.utils.ToastCenterUtils;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.google.gson.Gson;
import com.tencent.coustom.GiftEntity;
import com.tencent.coustom.IMGsonUtils;
import com.tencent.imsdk.v2.V2TIMAdvancedMsgListener;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMMessageReceipt;
import com.tencent.liteav.trtccalling.model.TRTCCalling;
import com.tencent.liteav.trtccalling.model.TRTCCallingDelegate;
import com.tencent.qcloud.tuikit.tuichat.bean.MessageInfo;
import com.tencent.qcloud.tuikit.tuichat.util.ChatMessageInfoUtil;
import com.tencent.trtc.TRTCCloudDef;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.disposables.Disposable;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.RxSubscriptions;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

public class AudioCallChatingViewModel extends BaseViewModel<AppRepository> {
    public int TimeCount = 0;
    //是否发送过礼物
    public boolean sendGiftBagSuccess = false;
    public Integer roomId;
    public Integer fromUserId;
    public Integer toUserId;

    //录音文案数组坐标
    public int sayHiePosition = 0;
    public int sayHiePage = 1;

    public BigDecimal coinTotal;
    //当前用户是否男性
    public boolean isMale = false;
    //钻石余额(仅男用户)
    public Integer coinBalance;
    public Integer $coinBalance = 0;
    //已经取下标数
    public Integer formSelIndex = 0;
    //收入从第N分钟开始
    public Integer fromMinute;
    //单价
    public BigDecimal unitPrice;
    //每秒收入
    public BigDecimal timePrice;
    //是否已追踪0未追踪1已追踪
    public Integer collected;
    //是否使用道具
    public Integer useProp;
    //余额不足提示分钟数
    public Integer balanceNotEnoughTipsMinutes;
    public int maleBalanceMoney = 0;
    public boolean flagMoney = false;
    public ObservableField<Boolean> isShowCountdown = new ObservableField(false);
    //通话收入提示间隔秒数
    public Integer profitTipsIntervalSeconds;
    //价格配置表
    public List<CallingInfoEntity.CallingUnitPriceInfo> unitPriceList;

    //时间提示
    public ObservableField<String> timeTextField = new ObservableField<>();
    public ObservableField<CallingInfoEntity.FromUserProfile> rightUserInfoField = new ObservableField<>();
    public ObservableField<CallingInfoEntity.FromUserProfile> leftUserInfoField = new ObservableField<>();
    //男生收入框是否展示
    public ObservableBoolean maleTextLayoutSHow = new ObservableBoolean(false);
    //男性收入内容
    public ObservableField<String> maleTextMoneyField = new ObservableField();
    //女性收入弹窗是否显示
    public ObservableBoolean girlEarningsField = new ObservableBoolean(false);
    //收入文字
    public ObservableField<SpannableString> girlEarningsText = new ObservableField<>();
    public ObservableField<UserDataEntity> audioUserDataEntity = new ObservableField<>();
    public ObservableField<CallingInfoEntity> audioCallingInfoEntity = new ObservableField<>();
    //是否已经追踪
    public ObservableInt collectedField = new ObservableInt(1);
    //是否静音
    public ObservableBoolean micMuteField = new ObservableBoolean(false);
    //是否免提
    public ObservableBoolean handsFreeField = new ObservableBoolean(false);
    //破冰文案
    public List<CallingInfoEntity.SayHiEntity> sayHiEntityList = new ArrayList<>();
    //破冰文案
    public ObservableField<CallingInfoEntity.SayHiEntity> sayHiEntityField = new ObservableField<>();
    //破冰文案是否显示
    public ObservableBoolean sayHiEntityHidden = new ObservableBoolean(true);
    public BindingRecyclerViewAdapter<AudioCallChatingItemViewModel> adapter = new BindingRecyclerViewAdapter<>();
    public ObservableList<AudioCallChatingItemViewModel> observableList = new ObservableArrayList<>();
    public ItemBinding<AudioCallChatingItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_call_audio_chating);

    private final static String TAG = "trtcJoy";
    private static final int MIN_DURATION_SHOW_LOW_QUALITY = 5000; //显示网络不佳最小间隔时间

    public UIChangeObservable uc = new UIChangeObservable();
    //点击文字充值
    public BindingCommand referMoney = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            uc.sendUserGiftError.postValue(false);
        }
    });

    protected Ifinish mView;
    protected TRTCCalling mTRTCCalling;
    protected TRTCCallingDelegate mTRTCCallingDelegate;
    //    private TUICalling.Role mRole;
    public View.OnClickListener closeOnclick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            uc.closeViewHint.call();
        }
    };
    //发送礼物
    public BindingCommand giftBagOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            AppContext.instance().logEvent(AppsFlyerEvent.voicecall_gift);
            uc.callGiftBagAlert.call();
        }
    });
    //关闭男生隐藏余额不足提示
    public BindingCommand closeMoney = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            maleTextLayoutSHow.set(false);
        }
    });

    //关闭女生界面男生余额不足提示
    public BindingCommand closeMoney2 = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            isShowCountdown.set(false);
            girlEarningsField.set(false);
        }
    });


    //订阅者
    private Disposable mSubscription;
    private long mSelfLowQualityTime;
    private long mOtherPartyLowQualityTime;

    public AudioCallChatingViewModel(@NonNull @NotNull Application application, AppRepository model) {
        super(application, model);
    }

    //关注
    public BindingCommand addlikeOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            AppContext.instance().logEvent(AppsFlyerEvent.voicecall_follow);
            if (ConfigManager.getInstance().isMale()) {
                addLike(false);

            } else {
                //是女生提示
                int guideFlag = model.readSwitches(EaringlSwitchUtil.KEY_TIPS);
                //后台开关 1提示  0隐藏
                if (guideFlag == 1) {
                    uc.clickLike.call();
                } else {
                    addLike(false);
                }
            }
        }
    });

    //禁用麦克风点击
    public BindingCommand micMuteOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            if (micMuteField.get()) {//开启免提
                ToastUtils.showShort(R.string.call_message_deatail_txt_4);
            } else {
                ToastUtils.showShort(R.string.call_message_deatail_txt_3);
            }
            boolean minMute = !micMuteField.get();
            micMuteField.set(minMute);
            mTRTCCalling.setMicMute(minMute);
        }
    });

    //声音展示
    public BindingCommand handsFreeOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            if (handsFreeField.get()) {//开启免提
                ToastUtils.showShort(R.string.call_message_deatail_txt_2);
            } else {
                ToastUtils.showShort(R.string.call_message_deatail_txt_1);
            }
            boolean handsFree = !handsFreeField.get();
            handsFreeField.set(handsFree);
            mTRTCCalling.setHandsFree(handsFree);
        }
    });
    //切换破冰文案提示
    public BindingCommand upSayHiEntityOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            AppContext.instance().logEvent(AppsFlyerEvent.voicecall_ice_change);
            uc.startUpSayHiAnimotor.call();
//            getSayHiList();
        }
    });
    //关闭破冰文案提示
    public BindingCommand colseSayHiEntityOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            AppContext.instance().logEvent(AppsFlyerEvent.voicecall_ice_close);
            sayHiEntityHidden.set(true);
        }
    });

    //发送礼物
    public void sendUserGift(Dialog dialog, GiftBagEntity.giftEntity giftEntity, Integer to_user_id, Integer amount) {
        model.sendUserGift(giftEntity.getId(), to_user_id, amount, 2)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {
                        dismissHUD();
                        sendGiftBagSuccess = true;
//                        dialog.dismiss();
                        String textTip = null;
                        //礼物数量*礼物钻石
                        int amountMoney = giftEntity.getMoney().intValue() * amount;
                        if (isMale) {
                            if (TimeCount < 60) {//不满1分钟
                                if (unitPriceList.size() > 1) {//聊天卡
                                    //总钻石 - 每分钟花费钻石得到 多余钻石
                                    int myMoney = coinBalance.intValue() % unitPrice.intValue();
                                    Log.e("有聊天卡。总钻石余额为", myMoney + "=======" + coinBalance.intValue() + "=====" + unitPrice.intValue());
                                    //剩余钻石-礼物所需钻石
                                    int costMoney = myMoney - amountMoney;
                                    Log.e("剩余钻石-礼物所需钻石", costMoney + "=======" + myMoney + "=====" + amountMoney);
                                    //多余钻石-礼物所需钻石。不计算剩余时长。只扣除钻石
                                    if (costMoney > 0) {
                                        coinBalance -= amountMoney;
                                        $coinBalance += amountMoney;
                                    } else {
                                        Log.e("扣除分钟前剩余分钟", maleBalanceMoney + "========");
                                        //计算出剩余钻石换算成分钟
                                        maleBalanceMoney = ((((coinBalance + unitPrice.intValue()) - amountMoney) / unitPrice.intValue()) * 60) - TimeCount;
                                        Log.e("扣除分钟后剩余分钟", maleBalanceMoney + "========");
                                        //扣除钻石
                                        coinBalance -= amountMoney;
                                        $coinBalance += amountMoney;
                                    }
                                    if (maleBalanceMoney < 10) {
                                        maleBalanceMoney += 30;
                                    }
                                } else {//不是聊天卡
                                    int myMoney = coinBalance.intValue() % unitPrice.intValue();
                                    //剩余钻石-礼物所需钻石
                                    int costMoney = myMoney - amountMoney;
                                    //多余钻石-礼物所需钻石。不计算剩余时长。只扣除钻石
                                    if (costMoney > 0) {
                                        Log.e("扣除多余钻石", costMoney + "====" + coinBalance);
                                        coinBalance -= amountMoney;
                                        $coinBalance += amountMoney;
                                        Log.e("剩余钻石:", coinBalance + "=====已消费钻石==" + $coinBalance + "====" + amountMoney);
                                    } else {
                                        Log.e("多余钻石不足消费分钟", maleBalanceMoney + "======" + coinBalance);
                                        //计算出剩余钻石换算成分钟
                                        maleBalanceMoney = ((((coinBalance + unitPrice.intValue()) - amountMoney) / unitPrice.intValue()) * 60) - TimeCount;
                                        Log.e("剩余使用分钟", maleBalanceMoney + "");
                                        //扣除钻石
                                        coinBalance -= amountMoney;
                                        $coinBalance += amountMoney;
                                        Log.e("剩余使用分钟", maleBalanceMoney + "====" + coinBalance + "=====" + $coinBalance);
                                    }
                                    if (maleBalanceMoney < 10) {
                                        maleBalanceMoney += 30;
                                    }
                                }
                            } else {
                                int myMoney = coinBalance.intValue() % unitPrice.intValue();
                                //剩余钻石-礼物所需钻石
                                int costMoney = myMoney - amountMoney;
                                //多余钻石-礼物所需钻石。不计算剩余时长。只扣除钻石
                                if (costMoney > 0) {
                                    coinBalance -= amountMoney;
                                    $coinBalance += amountMoney;
                                } else {
                                    Log.e("钻石余额", coinBalance + "======" + (coinBalance + unitPrice.intValue()) + "==========" + TimeCount);
                                    //计算出剩余钻石换算成分钟
                                    maleBalanceMoney = ((((coinBalance + unitPrice.intValue()) - amountMoney) / unitPrice.intValue()) * 60) - (TimeCount % 60);
                                    //扣除钻石
                                    coinBalance -= amountMoney;
                                    $coinBalance += amountMoney;
                                }
                                if (maleBalanceMoney < 10) {
                                    maleBalanceMoney += 30;
                                }
                            }
                        }

                        if (isMale) {
                            textTip = StringUtils.getString(R.string.call_message_deatail_girl_txt_male);
                        } else {
                            textTip = StringUtils.getString(R.string.call_message_deatail_girl_txt_gift);
                        }
                        String nickname = leftUserInfoField.get().getNickname();
                        textTip += " " + nickname;
                        int startLength = textTip.length();
                        textTip += " " + giftEntity.getName() + " x" + amount;
                        SpannableString stringBuilder = new SpannableString(textTip);

                        ForegroundColorSpan blueSpan = new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint2));
                        ForegroundColorSpan blueSpanWhite = new ForegroundColorSpan(ColorUtils.getColor(R.color.white));
                        stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint2)), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.white)), 2, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        stringBuilder.setSpan(blueSpan, 5, 5 + nickname.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        stringBuilder.setSpan(blueSpanWhite, startLength, textTip.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        putRcvItemMessage(stringBuilder, giftEntity.getImg(), false);
                        Map<String, Object> mapData = new HashMap<>();
                        mapData.put("account", amount);
                        mapData.put("giftEntity", giftEntity);
                        uc.sendUserGiftAnim.postValue(mapData);
                    }

                    @Override
                    public void onError(RequestException e) {
                        dialog.dismiss();
                        dismissHUD();
                        if (e.getCode() != null && e.getCode().intValue() == 21001) {
                            ToastCenterUtils.showToast(R.string.dialog_exchange_integral_total_text1);
                            AppContext.instance().logEvent(AppsFlyerEvent.voicecall_gift_Ins_topup);
                            uc.sendUserGiftError.postValue(true);
                        }
                    }
                });
    }

    //注册RxBus
    @Override
    public void registerRxBus() {
        super.registerRxBus();
        mSubscription = RxBus.getDefault().toObservable(AudioCallingCancelEvent.class)
                .subscribe(event -> {
                    // 彈出確定框吧
                    hangup();
                });
        //将订阅者加入管理站
        RxSubscriptions.add(mSubscription);
    }

    //移除RxBus
    @Override
    public void removeRxBus() {
        super.removeRxBus();
        //将订阅者从管理站中移除
        RxSubscriptions.remove(mSubscription);
    }

    public void init(Ifinish iview) {
        isMale = ConfigManager.getInstance().isMale();
        mTRTCCalling = TRTCCalling.sharedInstance(AppContext.instance());
        initListener();
        mView = iview;
        //监听IM消息
        initIMListener();
    }

    public void hangup() {
        unListener();
        mTRTCCalling.hangup();
        mView.finishView();
        Utils.show("通話結束");
    }

    private void endChattingAndShowHint(String msg) {
        Utils.runOnUiThread(() -> {
            unListener();
            mView.finishView();
            Utils.show(msg);
        });
    }

    protected void initListener() {
        mTRTCCallingDelegate = new EmptyTRTCCallingDelegate() {
            @Override
            public void onError(int code, String msg) {
                Log.e(TAG, "onError: " + code + " " + msg);
                endChattingAndShowHint(AppContext.instance().getString(com.tencent.liteav.trtccalling.R.string.trtccalling_toast_call_error_msg, code, msg));
            }

            @Override
            public void onCallEnd() {
                Log.i(TAG, "onCallEnd: ");
                endChattingAndShowHint("通話結束");
            }

            @Override
            public void onNetworkQuality(TRTCCloudDef.TRTCQuality localQuality, ArrayList<TRTCCloudDef.TRTCQuality> remoteQuality) {
                updateNetworkQuality(localQuality, remoteQuality);
            }
        };
        mTRTCCalling.addDelegate(mTRTCCallingDelegate);
    }

    protected void unListener() {
        Utils.runOnUiThread(() -> {
            if (null != mTRTCCallingDelegate) {
                mTRTCCalling.removeDelegate(mTRTCCallingDelegate);
            }
        });
    }

    //localQuality 己方网络状态， remoteQualityList对方网络状态列表，取第一个为1v1通话的网络状态
    protected void updateNetworkQuality(TRTCCloudDef.TRTCQuality localQuality, List<TRTCCloudDef.TRTCQuality> remoteQualityList) {
        //如果己方网络和对方网络都很差，优先显示己方网络差
        boolean isLocalLowQuality = isLowQuality(localQuality);
        if (isLocalLowQuality) {
            updateLowQualityTip(true);
        } else {
            if (!remoteQualityList.isEmpty()) {
                TRTCCloudDef.TRTCQuality remoteQuality = remoteQualityList.get(0);
                if (isLowQuality(remoteQuality)) {
                    updateLowQualityTip(false);
                }
            }
        }
    }

    private boolean isLowQuality(TRTCCloudDef.TRTCQuality qualityInfo) {
        if (qualityInfo == null) {
            return false;
        }
        int quality = qualityInfo.quality;
        boolean lowQuality;
        switch (quality) {
            case TRTCCloudDef.TRTC_QUALITY_Vbad:
            case TRTCCloudDef.TRTC_QUALITY_Down:
                lowQuality = true;
                break;
            default:
                lowQuality = false;
        }
        return lowQuality;
    }

    public Drawable getVipGodsImg(CallingInfoEntity.FromUserProfile fromUserProfile) {
        if (fromUserProfile != null) {
            if (fromUserProfile.getSex() == 1) {
                if (fromUserProfile.getCertification() == 1) {
                    if (fromUserProfile.getIsVip() == 1) {
                        return AppContext.instance().getDrawable(R.drawable.ic_vip);
                    }
                    return AppContext.instance().getDrawable(R.drawable.call_label_real);
                }
                if (fromUserProfile.getIsVip() == 1) {
                    return AppContext.instance().getDrawable(R.drawable.ic_vip);
                }
            } else {
                if (fromUserProfile.getCertification() == 1) {
                    if (fromUserProfile.getIsVip() == 1) {
                        return AppContext.instance().getDrawable(R.drawable.ic_goddess);
                    }
                    return AppContext.instance().getDrawable(R.drawable.call_label_real);
                }
                if (fromUserProfile.getIsVip() == 1) {
                    return AppContext.instance().getDrawable(R.drawable.ic_goddess);
                }
            }
        }
        return null;
    }

    private void updateLowQualityTip(boolean isSelf) {
        long currentTime = System.currentTimeMillis();
        if (isSelf) {
            if (currentTime - mSelfLowQualityTime > MIN_DURATION_SHOW_LOW_QUALITY) {
                Toast.makeText(AppContext.instance(), com.tencent.liteav.trtccalling.R.string.trtccalling_self_network_low_quality, Toast.LENGTH_SHORT).show();
                mSelfLowQualityTime = currentTime;
            }
        } else {
            if (currentTime - mOtherPartyLowQualityTime > MIN_DURATION_SHOW_LOW_QUALITY) {
                Toast.makeText(AppContext.instance(), com.tencent.liteav.trtccalling.R.string.trtccalling_other_party_network_low_quality, Toast.LENGTH_SHORT).show();
                mOtherPartyLowQualityTime = currentTime;
            }
        }
    }

    //监听IM消息
    private void initIMListener() {
        V2TIMManager.getMessageManager().addAdvancedMsgListener(new V2TIMAdvancedMsgListener() {
            @Override
            public void onRecvNewMessage(V2TIMMessage msg) {//新消息提醒
                super.onRecvNewMessage(msg);
                if (msg != null && leftUserInfoField.get() != null) {
                    MessageInfo info = ChatMessageInfoUtil.createMessageInfo(msg);
                    if (info != null) {
                        Integer msgFromUserId = ChatUtils.imUserIdToSystemUserId(info.getFromUser());
                        if (msgFromUserId.intValue() == leftUserInfoField.get().getId().intValue()) {
                            Log.e("确定是聊天对象发送的消息", "==================");
                            String text = String.valueOf(info.getExtra());
                            Log.e("聊天消息体未", text);
                            if (isJSON2(text) && text.indexOf("type") != -1) {//做自定义通知判断
                                Map<String, Object> map_data = new Gson().fromJson(text, Map.class);
                                //礼物消息
                                if (map_data != null && map_data.get("type") != null && map_data.get("type").equals("message_gift")
                                        && map_data.get("is_accost") == null) {
                                    Log.e("该消息是聊天消息", "===============");
                                    int nickNameLength = leftUserInfoField.get().getNickname().length();
                                    GiftEntity giftEntity = IMGsonUtils.fromJson(String.valueOf(map_data.get("data")), GiftEntity.class);
                                    uc.acceptUserGift.postValue(giftEntity);
                                    String sexText = isMale ? StringUtils.getString(R.string.call_message_deatail_girl_txt3) : StringUtils.getString(R.string.call_message_deatail_girl_txt2);
                                    String messageText = leftUserInfoField.get().getNickname() + " " + StringUtils.getString(R.string.call_message_deatail_girl_txt1) + " " + sexText + " " + giftEntity.getTitle() + "x" + giftEntity.getAmount();
                                    SpannableString stringBuilder = new SpannableString(messageText);
                                    stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint2)), 0, nickNameLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.white)), nickNameLength, nickNameLength + 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint2)), nickNameLength + 3, nickNameLength + 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.white)), nickNameLength + 5, messageText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    putRcvItemMessage(stringBuilder, giftEntity.getImgPath(), false);
                                    if (!isMale) {
                                        String itemMessage = String.format(StringUtils.getString(R.string.call_message_deatail_girl_txt5), String.format("%.2f", giftEntity.getAmount().intValue() * giftEntity.getProfitTwd().doubleValue()));
                                        SpannableString itemMessageBuilder = new SpannableString(itemMessage);
                                        itemMessageBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.white)), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        itemMessageBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint1)), 4, itemMessage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        putRcvItemMessage(itemMessageBuilder, null, false);
                                    } else {
                                        String itemMessage = String.format(StringUtils.getString(R.string.custom_message_txt7), giftEntity.getAmount() * giftEntity.getProfitDiamond());
                                        SpannableString itemMessageBuilder = new SpannableString(itemMessage);
                                        itemMessageBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.white)), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        itemMessageBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint1)), 4, itemMessage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        putRcvItemMessage(itemMessageBuilder, null, false);
                                        int amountMoney = giftEntity.getAmount().intValue() * giftEntity.getProfitDiamond().intValue();
                                        coinBalance += amountMoney;
                                        maleBalanceMoney = ((((coinBalance + unitPrice.intValue())) / unitPrice.intValue()) * 60) - TimeCount;
                                    }
                                } else if (map_data != null && map_data.get("type") != null && map_data.get("type").equals("message_tracking")) {//追踪提示
                                    CustomMessageIMTextEntity giftEntity = IMGsonUtils.fromJson(String.valueOf(map_data.get("data")), CustomMessageIMTextEntity.class);
                                    if (giftEntity != null) {
                                        String sexText = isMale ? StringUtils.getString(R.string.call_message_deatail_girl_txt3) : StringUtils.getString(R.string.call_message_deatail_girl_txt2);
                                        String msgText = giftEntity.getToName() + StringUtils.getString(R.string.call_message_deatail_girl_txt4) + sexText;
                                        SpannableString stringBuilder = new SpannableString(msgText);
                                        ForegroundColorSpan blueSpan = new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint2));
                                        stringBuilder.setSpan(blueSpan, 0, msgText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        putRcvItemMessage(stringBuilder, null, false);
                                    }
                                } else if (map_data != null && map_data.get("type") != null && map_data.get("type").equals("message_countdown")) {//对方余额不足
                                    LogUtils.i("onRecvNewMessage: ");
                                    if (!isMale && ConfigManager.getInstance().getTipMoneyShowFlag()) {
                                        String data = (String) map_data.get("data");
                                        Map<String, Object> dataMapCountdown = new Gson().fromJson(data, Map.class);
                                        String isShow = (String) dataMapCountdown.get("is_show");
                                        if (isShow != null && isShow.equals("1")) {
                                            isShowCountdown.set(true);
                                            girlEarningsField.set(true);
                                            String girlEarningsTex = StringUtils.getString(R.string.insufficient_balance_of_counterparty);
                                            SpannableString stringBuilder = new SpannableString(girlEarningsTex);
                                            stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.white)), 0, girlEarningsTex.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            girlEarningsText.set(stringBuilder);

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onRecvC2CReadReceipt(List<V2TIMMessageReceipt> receiptList) {
                super.onRecvC2CReadReceipt(receiptList);
            }

            @Override
            public void onRecvMessageRevoked(String msgID) {
                super.onRecvMessageRevoked(msgID);
            }

            @Override
            public void onRecvMessageModified(V2TIMMessage msg) {
                super.onRecvMessageModified(msg);
            }
        });
    }

    public void getCallingInfo(Integer roomId, Integer fromUserId, Integer toUserId) {
        model.getCallingInfo(roomId, 1, fromUserId, toUserId, model.readUserData().getId())
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<CallingInfoEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CallingInfoEntity> response) {
                        CallingInfoEntity callingInviteInfo = response.getData();
                        UserDataEntity userDataEntity = model.readUserData();
                        audioUserDataEntity.set(userDataEntity);
                        audioCallingInfoEntity.set(callingInviteInfo);
                        uc.callAudioStart.call();
                        sayHiEntityList = callingInviteInfo.getSayHiList().getData();
                        if (sayHiEntityList.size() > 1) {
                            sayHiEntityHidden.set(false);
                            sayHiEntityField.set(sayHiEntityList.get(0));
                        }
                        if (userDataEntity.getId().intValue() == callingInviteInfo.getFromUserProfile().getId().intValue()) {
                            rightUserInfoField.set(callingInviteInfo.getFromUserProfile());
                            leftUserInfoField.set(callingInviteInfo.getToUserProfile());
                        } else {
                            rightUserInfoField.set(callingInviteInfo.getToUserProfile());
                            leftUserInfoField.set(callingInviteInfo.getFromUserProfile());
                        }
                        //钻石余额(仅男用户)
                        coinBalance = callingInviteInfo.getCoinBalance();
                        Log.e("当前男用户钻石余额", String.valueOf(coinBalance));
                        //是否已追踪0未追踪1已追踪
                        collected = callingInviteInfo.getCollected();
                        collectedField.set(collected);
                        //是否使用道具
                        useProp = callingInviteInfo.getUseProp();
                        //余额不足提示分钟数
                        balanceNotEnoughTipsMinutes = callingInviteInfo.getBalanceNotEnoughTipsMinutes();
                        //通话收入提示间隔秒数
                        profitTipsIntervalSeconds = callingInviteInfo.getProfitTipsIntervalSeconds();
                        //价格配置表
                        unitPriceList = callingInviteInfo.getUnitPriceList();
                        unitPrice = unitPriceList.get(0).getUnitPrice();
                        fromMinute = unitPriceList.get(0).getFromMinute();
                        timePrice = unitPrice.divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);

                        mTRTCCalling.enableAGC(true);
                        mTRTCCalling.enableAEC(true);
                        mTRTCCalling.enableANS(true);

                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public void getTips(Integer toUserId,int type,String isShowCountdown){
        model.getTips(toUserId, type,isShowCountdown)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse>() {
                    @Override
                    public void onSuccess(BaseDataResponse baseDataResponse) {
                        LogUtils.i("onSuccess: "+baseDataResponse.toString());
                    }

                    @Override
                    public void onError(RequestException e) {

                    }
                });
    }
    //获取破冰文案
    public void getSayHiList() {
        //录音文案数组坐标
        if (!ObjectUtils.isEmpty(sayHiEntityList) && sayHiePosition + 1 <= sayHiEntityList.size() - 1) {
            sayHiePosition++;
            sayHiEntityField.set(sayHiEntityList.get(sayHiePosition));
            return;
        }
        sayHiePosition++;
        if (sayHiePosition < sayHiEntityList.size()) {
            return;
        }
        sayHiePage++;
        model.getSayHiList(sayHiePage, 30)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<CallingInfoEntity.SayHiList>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CallingInfoEntity.SayHiList> response) {
                        try {
                            List<CallingInfoEntity.SayHiEntity> dataList = response.getData().getData();
                            if (!ObjectUtils.isEmpty(dataList)) {
                                sayHiEntityList.addAll(dataList);
                                if (sayHiEntityList.size() > 1) {
                                    sayHiEntityField.set(sayHiEntityList.get(sayHiePosition++));
                                }
                            } else {
                                sayHiePosition = 0;
                                sayHiEntityField.set(sayHiEntityList.get(0));
                            }
                        } catch (Exception e) {

                        }
                    }
                });
    }

    //添加文案到公屏
    public void putRcvItemMessage(SpannableString stringBuilder, String imgPath, boolean sendGiftBagShow) {
        observableList.add(new AudioCallChatingItemViewModel(AudioCallChatingViewModel.this, stringBuilder, imgPath, sendGiftBagShow));
        uc.scrollToEnd.postValue(null);
    }

    public class UIChangeObservable {
        //接听成功
        public SingleLiveEvent<Void> callAudioStart = new SingleLiveEvent<>();
        //关注点击
        public SingleLiveEvent<Void> clickLike = new SingleLiveEvent<>();
        //调用发送礼物弹窗
        public SingleLiveEvent<Void> callGiftBagAlert = new SingleLiveEvent<>();
        //发送礼物失败。充值钻石
        public SingleLiveEvent<Boolean> sendUserGiftError = new SingleLiveEvent<>();
        //发送礼物效果
        public SingleLiveEvent<Map<String, Object>> sendUserGiftAnim = new SingleLiveEvent<>();
        //接收礼物效果
        public SingleLiveEvent<GiftEntity> acceptUserGift = new SingleLiveEvent<>();
        //关闭消息
        public SingleLiveEvent<Void> closeViewHint = new SingleLiveEvent<>();
        //滚动到屏幕底部
        public SingleLiveEvent<Void> scrollToEnd = new SingleLiveEvent<>();
        public SingleLiveEvent<Void> startUpSayHiAnimotor = new SingleLiveEvent<>();
    }

    //追踪
    public void addLike(boolean isHangup) {
        model.addIMCollect(leftUserInfoField.get().getId(), 1)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        if (isHangup) {
                            hangup();
                        } else {
                            collected = 1;
                            collectedField.set(1);
                            ToastUtil.showToast(AppContext.instance(), R.string.cancel_zuizong_3);
                            String sexText = isMale ? StringUtils.getString(R.string.call_message_deatail_girl_txt3) : StringUtils.getString(R.string.call_message_deatail_girl_txt2);
                            String msgText = sexText + StringUtils.getString(R.string.call_message_deatail_girl_txt4) + leftUserInfoField.get().getNickname();
                            SpannableString stringBuilder = new SpannableString(msgText);
                            ForegroundColorSpan blueSpan = new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint2));
                            stringBuilder.setSpan(blueSpan, 0, msgText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            putRcvItemMessage(stringBuilder, null, false);
                        }
                    }

                    @Override
                    public void onError(RequestException e) {
                        super.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public static boolean isJSON2(String str) {
        boolean result = false;
        try {
            new Gson().fromJson(str, Map.class);
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return result;

    }

    /**
     * 正则匹配 返回值是一个SpannableString 即经过变色处理的数据
     */
    private SpannableString matcherSearchText(int color, String text, String keyword) {
        if (text == null || TextUtils.isEmpty(text)) {
            return SpannableString.valueOf("");
        }
        SpannableString spannableString = new SpannableString(text);
        //条件 keyword
        Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
        //匹配
        Matcher matcher = pattern.matcher(spannableString);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            //ForegroundColorSpan 需要new 不然也只能是部分变色
            spannableString.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        //返回变色处理的结果
        return spannableString;
    }

}
