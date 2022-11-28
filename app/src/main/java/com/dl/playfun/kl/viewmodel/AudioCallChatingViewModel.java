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

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.CallGameCoinPusherEntity;
import com.dl.playfun.entity.CallUserInfoEntity;
import com.dl.playfun.entity.CallUserRoomInfoEntity;
import com.dl.playfun.entity.CallingInfoEntity;
import com.dl.playfun.entity.CallingStatusEntity;
import com.dl.playfun.entity.GiftBagEntity;
import com.dl.playfun.entity.MallWithdrawTipsInfoEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.event.AudioCallingCancelEvent;
import com.dl.playfun.event.CallingToGamePlayingEvent;
import com.dl.playfun.kl.CallChatingConstant;
import com.dl.playfun.kl.Utils;
import com.dl.playfun.kl.view.Ifinish;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.manager.V2TIMCustomManagerUtil;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.utils.LogUtils;
import com.dl.playfun.utils.ToastCenterUtils;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.dl.rtc.calling.base.DLRTCCallingDelegate;
import com.dl.rtc.calling.base.impl.DLRTCInternalListenerManager;
import com.dl.rtc.calling.manager.DLRTCAudioManager;
import com.dl.rtc.calling.manager.DLRTCStartShowUIManager;
import com.google.gson.Gson;
import com.tencent.custom.GiftEntity;
import com.tencent.custom.IMGsonUtils;
import com.tencent.imsdk.v2.V2TIMAdvancedMsgListener;
import com.tencent.imsdk.v2.V2TIMCustomElem;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMMessageReceipt;
import com.tencent.qcloud.tuicore.custom.CustomConstants;
import com.tencent.qcloud.tuicore.custom.CustomConvertUtils;
import com.tencent.qcloud.tuikit.tuichat.bean.message.TUIMessageBean;
import com.tencent.qcloud.tuikit.tuichat.ui.view.MyImageSpan;
import com.tencent.qcloud.tuikit.tuichat.util.ChatMessageBuilder;
import com.tencent.trtc.TRTCCloudDef;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    //通话时长-秒
    public int TimeCount = 0;
    //是否发送过礼物
    public boolean sendGiftBagSuccess = false;
    public Integer roomId;
    public String roomIdStr;
    public String fromUserId;
    public String toUserId;
    //心跳包发送间隔
    public int heartBeatInterval = 10;
    //通话数据加载完成
    public boolean callInfoLoaded = false;
    //男生钻石总余额
    public int maleBalanceMoney = 0;
    //男生总分钟数
    public int totalMinutes = 0;
    //男生剩余聊天分钟数
    public int totalMinutesRemaining = 0;
    //聊天收益
    public double payeeProfits;
    //断网总时间
    int disconnectTime = 0;

    //录音文案数组坐标
    public int sayHiePosition = 0;
    public int sayHiePage = 1;

    //当前用户是否男性
    public boolean isMale = false;
    //收益开关
    public boolean isShowTipMoney = false;
    //当前用户是否为收款人
    public boolean isPayee = false;
    //是否已追踪0未追踪1已追踪
    public Boolean collected;
    //余额不足临界提示分钟数
    public int balanceNotEnoughTipsMinutes;
    //余额不足提示标记
    public boolean flagMoneyNotWorth = false;
    public ObservableField<Boolean> isShowCountdown = new ObservableField(false);
    //防录屏提示开关
    public ObservableField<Boolean> tipSwitch = new ObservableField(true);

    //时间提示
    public ObservableField<String> timeTextField = new ObservableField<>();
    //对方用户信息
    public ObservableField<CallUserInfoEntity> otherUserInfoField = new ObservableField<>();
    //当前用户信息
    public ObservableField<UserDataEntity> currentUserInfoField = new ObservableField<>();

    //是否已经显示过兑换规则
    public ObservableField<Boolean> isHideExchangeRules = new ObservableField<>(false);
    //男生收益框是否展示
    public ObservableBoolean maleTextLayoutSHow = new ObservableBoolean(false);
    //男性收益内容
    public ObservableField<String> maleTextMoneyField = new ObservableField();
    //女性收益弹窗是否显示
    public ObservableBoolean girlEarningsField = new ObservableBoolean(false);
    //收益文字
    public ObservableField<SpannableString> girlEarningsText = new ObservableField<>();
    public ObservableField<CallingInfoEntity> audioCallingInfoEntity = new ObservableField<>();
    //是否已经追踪
    public ObservableBoolean collectedField = new ObservableBoolean(false);
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
    public BindingCommand<Void> referMoney = new BindingCommand<>(() -> uc.sendUserGiftError.postValue(false));
    /**
     * 水晶兑换规则
     */
    public BindingCommand<Void> crystalOnClick = new BindingCommand<>(() -> getMallWithdrawTipsInfo(1));

    protected Ifinish mView;
    protected DLRTCCallingDelegate mTRTCCallingDelegate;
    //    private TUICalling.Role mRole;
    public View.OnClickListener closeOnclick = v -> uc.closeViewHint.call();
    //发送礼物
    public BindingCommand<Void> giftBagOnClickCommand = new BindingCommand<>(new BindingAction() {
        @Override
        public void call() {
            AppContext.instance().logEvent(AppsFlyerEvent.voicecall_gift);
            uc.callGiftBagAlert.call();
        }
    });
    //关闭男生隐藏余额不足提示
    public BindingCommand<Void> closeMoney = new BindingCommand<>(() -> maleTextLayoutSHow.set(false));
    //显示推币机按钮
    public ObservableBoolean coinPusherRoomShow = new ObservableBoolean(false);
    //信令消息
    public CallGameCoinPusherEntity _callGameCoinPusherEntity;
    //关闭女生界面男生余额不足提示
    public BindingCommand<Void> closeMoney2 = new BindingCommand<>(() -> {
        isShowCountdown.set(false);
        girlEarningsField.set(false);
    });

    //调用推币机
    public BindingCommand<Void> coinPusherRoomClick = new BindingCommand<>(() -> {
       uc.coinPusherRoomEvent.call();
    });

    //订阅者
    private Disposable mSubscription;
    private Disposable mCallingToGameSubscription;
    private long mSelfLowQualityTime;
    private long mOtherPartyLowQualityTime;

    public AudioCallChatingViewModel(@NonNull @NotNull Application application, AppRepository model) {
        super(application, model);
    }

    //关注
    public BindingCommand<Void> addlikeOnClickCommand = new BindingCommand<>(() -> {
        AppContext.instance().logEvent(AppsFlyerEvent.voicecall_follow);
        addLike(false);
    });

    //禁用麦克风点击
    public BindingCommand<Void> micMuteOnClickCommand = new BindingCommand<>(() -> {
        if (micMuteField.get()) {//开启免提
            ToastUtils.showShort(R.string.playfun_call_message_deatail_txt_4);
        } else {
            ToastUtils.showShort(R.string.playfun_call_message_deatail_txt_3);
        }
        boolean minMute = !micMuteField.get();
        micMuteField.set(minMute);
        DLRTCAudioManager.Companion.getInstance().audioRoute(minMute);
    });

    //声音展示
    public BindingCommand<Void> handsFreeOnClickCommand = new BindingCommand<>(() -> {
        if (handsFreeField.get()) {//开启免提
            ToastUtils.showShort(R.string.playfun_call_message_deatail_txt_2);
        } else {
            ToastUtils.showShort(R.string.playfun_call_message_deatail_txt_1);
        }
        boolean handsFree = !handsFreeField.get();
        handsFreeField.set(handsFree);
        DLRTCAudioManager.Companion.getInstance().audioRoute(handsFree);
    });
    //切换破冰文案提示
    public BindingCommand<Void> upSayHiEntityOnClickCommand = new BindingCommand<>(() -> {
        AppContext.instance().logEvent(AppsFlyerEvent.voicecall_ice_change);
        uc.startUpSayHiAnimotor.call();
//            getSayHiList();
    });
    //关闭破冰文案提示
    public BindingCommand<Void> colseSayHiEntityOnClickCommand = new BindingCommand<>(() -> {
        AppContext.instance().logEvent(AppsFlyerEvent.voicecall_ice_close);
        sayHiEntityHidden.set(true);
    });

    //查询房间信息
    public void callingInviteUser(String inviterImId,String receiverImId){
//        callingType	是	Integer	通话类型：1=语音，2=视频
//                * inviterImId	是	String	拔打人IM ID
//     * receiverImId	是	String	接收人IM ID
        Map<String, Object> mapData = new HashMap<>();
        mapData.put("callingType",1);
        mapData.put("inviterImId",inviterImId);
        mapData.put("receiverImId",receiverImId);
        model.callingInviteUser(ApiUitl.getBody(GsonUtils.toJson(mapData)))
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<CallUserRoomInfoEntity>>(){
                    @Override
                    public void onSuccess(BaseDataResponse<CallUserRoomInfoEntity> response) {
                        CallUserRoomInfoEntity callingInviteInfo = response.getData();
                        if(callingInviteInfo!=null){
                            if(!callingInviteInfo.getPayerImId().equals(ConfigManager.getInstance().getUserImID())){
                                //付费方
                                isPayee = true;
                            }
                            //心跳间隔大于0秒才进行赋值。否则默认10秒
                            if(callingInviteInfo.getHeartBeatInterval() > 0){
                                heartBeatInterval = callingInviteInfo.getHeartBeatInterval();
                            }
                        }
                        uc.callAudioStart.call();
                        //主动拨打
                        DLRTCAudioManager.Companion.getInstance().enableAGC(true);
                        DLRTCAudioManager.Companion.getInstance().enableAEC(true);
                        DLRTCAudioManager.Companion.getInstance().enableANS(true);
                        //gameUI.acceptCallingEvent.call();
                    }
                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //发送礼物
    public void sendUserGift(Dialog dialog, GiftBagEntity.GiftEntity giftEntity, Integer to_user_id, Integer amount) {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("giftId", giftEntity.getId());
        map.put("toUserId", to_user_id);
        map.put("type", 2);
        map.put("amount", amount);
        model.sendUserGift(ApiUitl.getBody(GsonUtils.toJson(map)))
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
                        if (isMale) {
                            textTip = StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt_male);
                        } else {
                            textTip = StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt_gift);
                        }
                        String nickname = otherUserInfoField.get().getNickname();
                        textTip += " " + nickname;
                        int startLength = textTip.length();
                        textTip += " " + giftEntity.getName() + " x" + amount;
                        SpannableString stringBuilder = new SpannableString(textTip);

                        ForegroundColorSpan blueSpanWhite = new ForegroundColorSpan(ColorUtils.getColor(R.color.white));
                        stringBuilder.setSpan(blueSpanWhite, 0, startLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                            ToastCenterUtils.showToast(R.string.playfun_dialog_exchange_integral_total_text1);
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
        mCallingToGameSubscription = RxBus.getDefault().toObservable(CallingToGamePlayingEvent.class)
                .subscribe(event -> {
                    if(event!=null){
                        CallGameCoinPusherEntity callGameCoinPusherEntity = event.getCallGameCoinPusherEntity();
                        if(callGameCoinPusherEntity!=null){
                            uc.callingToGamePlayingEvent.postValue(callGameCoinPusherEntity);
                        }
                    }

                });
        //将订阅者加入管理站
        RxSubscriptions.add(mSubscription);
        RxSubscriptions.add(mCallingToGameSubscription);
    }

    //移除RxBus
    @Override
    public void removeRxBus() {
        super.removeRxBus();
        //将订阅者从管理站中移除
        RxSubscriptions.remove(mSubscription);
        RxSubscriptions.remove(mCallingToGameSubscription);
    }

    public void init(Ifinish iview) {
        isMale = ConfigManager.getInstance().isMale();
        isShowTipMoney = ConfigManager.getInstance().getTipMoneyShowFlag();
        initListener();
        mView = iview;
        //监听IM消息
        initIMListener();
    }

    public void hangup() {
        updateCallingStatus(CallChatingConstant.exitRoom);
        unListener();
        DLRTCStartShowUIManager.Companion.getInstance().inviteUserReject();
        mView.finishView();
        Utils.show(AppContext.instance().getString(R.string.playfun_call_ended));
    }

    private void endChattingAndShowHint(String msg) {
        Utils.runOnUiThread(() -> {
            unListener();
            mView.finishView();
            Utils.show(msg);
        });
    }

    protected void initListener() {
        mTRTCCallingDelegate = new UITRTCCallingDelegate() {
            @Override
            public void onError(int code, String msg) {
                Log.e(TAG, "onError: " + code + " " + msg);
                endChattingAndShowHint(AppContext.instance().getString(R.string.trtccalling_toast_call_error_msg, code, msg));
            }

            @Override
            public void onCallEnd() {
                updateCallingStatus(CallChatingConstant.exitRoom);
                DLRTCStartShowUIManager.Companion.getInstance().exitRTCRoom();
                Log.i(TAG, "onCallEnd: ");
                endChattingAndShowHint(AppContext.instance().getString(R.string.playfun_call_ended));
            }

            @Override
            public void onNetworkQuality(TRTCCloudDef.TRTCQuality localQuality, ArrayList<TRTCCloudDef.TRTCQuality> remoteQuality) {
                //两秒回调一次
                if (localQuality.quality == 6 || remoteQuality.isEmpty()) {
                    disconnectTime++;
                    if (disconnectTime > 30 || (remoteQuality.isEmpty() && disconnectTime >15)) {
                        hangup();
                    }
                }else {
                    disconnectTime  = 0;
                }
                updateNetworkQuality(localQuality, remoteQuality);
            }

            @Override
            public void onTryToReconnect() {
            }

            @Override
            public void onCallingCancel() {
                unListener();
                DLRTCInternalListenerManager.Companion.getInstance().onCallingCancel();
                mView.finishView();
                Utils.show("對方取消通話");
            }
        };
        DLRTCInternalListenerManager.Companion.getInstance().addDelegate(mTRTCCallingDelegate);
    }

    protected void unListener() {
        if (null != mTRTCCallingDelegate) {
            DLRTCInternalListenerManager.Companion.getInstance().removeDelegate(mTRTCCallingDelegate);
        }
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

    public Drawable getVipGodsImg(CallUserInfoEntity fromUserProfile) {
        if (fromUserProfile != null) {
            if (fromUserProfile.getSex() == 1) {
                if (fromUserProfile.getIsVip() == 1) {
                    return AppContext.instance().getDrawable(R.drawable.ic_vip);
                } else {
                    if (fromUserProfile.getCertification() == 1) {
                        return AppContext.instance().getDrawable(R.drawable.ic_real_man);
                    }
                }
            } else {//女生
                if (fromUserProfile.getIsVip() == 1) {
                    return AppContext.instance().getDrawable(R.drawable.ic_goddess);
                } else {
                    if (fromUserProfile.getCertification() == 1) {
                        return AppContext.instance().getDrawable(R.drawable.ic_real_man);
                    }
                }
            }
        }
        return null;
    }
    public Drawable getVipGodsImg(UserDataEntity fromUserProfile) {
        if (fromUserProfile != null) {
            if (fromUserProfile.getSex() == 1) {
                if (fromUserProfile.getIsVip() == 1) {
                    return AppContext.instance().getDrawable(R.drawable.ic_vip);
                } else {
                    if (fromUserProfile.getCertification() == 1) {
                        return AppContext.instance().getDrawable(R.drawable.ic_real_man);
                    }
                }
            } else {//女生
                if (fromUserProfile.getIsVip() == 1) {
                    return AppContext.instance().getDrawable(R.drawable.ic_goddess);
                } else {
                    if (fromUserProfile.getCertification() == 1) {
                        return AppContext.instance().getDrawable(R.drawable.ic_real_man);
                    }
                }
            }
        }
        return null;
    }

    public String gameUrl(String gameChannel) {
        return ConfigManager.getInstance().getGameUrl(gameChannel);
    }

    public boolean isEmpty(String obj) {
        return obj == null || obj.equals("");
    }

    private void updateLowQualityTip(boolean isSelf) {
        long currentTime = System.currentTimeMillis();
        if (isSelf) {
            if (currentTime - mSelfLowQualityTime > MIN_DURATION_SHOW_LOW_QUALITY) {
                Toast.makeText(AppContext.instance(), R.string.trtccalling_self_network_low_quality, Toast.LENGTH_SHORT).show();
                mSelfLowQualityTime = currentTime;
            }
        } else {
            if (currentTime - mOtherPartyLowQualityTime > MIN_DURATION_SHOW_LOW_QUALITY) {
                Toast.makeText(AppContext.instance(), R.string.trtccalling_other_party_network_low_quality, Toast.LENGTH_SHORT).show();
                mOtherPartyLowQualityTime = currentTime;
            }
        }
    }

    //监听IM消息
    private void initIMListener() {
        V2TIMManager.getMessageManager().addAdvancedMsgListener(new V2TIMAdvancedMsgListener() {
            @Override
            public void onRecvNewMessage(V2TIMMessage msg) {//新消息提醒
                if (msg != null && otherUserInfoField.get() != null) {
                    TUIMessageBean info = ChatMessageBuilder.buildMessage(msg);
                    if (info != null) {
                        switch (info.getMsgType()){
                            //文本类型消息
                            case 1:
                                if (info.getV2TIMMessage().getSender().equals(otherUserInfoField.get().getImId())) {
                                    String text = String.valueOf(info.getExtra());
                                    if (isJSON2(text) && text.indexOf("type") != -1) {//做自定义通知判断
                                        Map<String, Object> map_data = new Gson().fromJson(text, Map.class);
                                        //礼物消息
                                        if (map_data != null && map_data.get("type") != null && map_data.get("type").equals("message_gift")
                                                && map_data.get("is_accost") == null) {
                                            GiftEntity giftEntity = IMGsonUtils.fromJson(String.valueOf(map_data.get("data")), GiftEntity.class);
                                            uc.acceptUserGift.postValue(giftEntity);
                                            //显示礼物弹幕
                                            showGiftBarrage(giftEntity);
                                            //礼物收益提示
                                            giftIncome(giftEntity);
                                        }else if (map_data != null && map_data.get("type") != null && map_data.get("type").equals("message_countdown")) {//对方余额不足
                                            if (isPayee && isShowTipMoney) {
                                                String data = (String) map_data.get("data");
                                                Map<String, Object> dataMapCountdown = new Gson().fromJson(data, Map.class);
                                                String isShow = (String) dataMapCountdown.get("is_show");
                                                if (isShow != null && isShow.equals("1")) {
                                                    isShowCountdown.set(true);
                                                    girlEarningsField.set(true);
                                                    String girlEarningsTex = StringUtils.getString(R.string.playfun_insufficient_balance_of_counterparty);
                                                    SpannableString stringBuilder = new SpannableString(girlEarningsTex);
                                                    stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.white)), 0, girlEarningsTex.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                    girlEarningsText.set(stringBuilder);

                                                }else if (isShow != null && isShow.equals("0")){
                                                    isShowCountdown.set(false);
                                                }
                                            }
                                        }
                                    }
                                }
                                break;
                            //自定义消息类型
                            case 2:
                                V2TIMCustomElem v2TIMCustomElem = info.getV2TIMMessage().getCustomElem();
                                Map<String, Object> contentBody = CustomConvertUtils.CustomMassageConvertMap(v2TIMCustomElem);
                                if (ObjectUtils.isNotEmpty(contentBody)) {
                                    Log.e("语音接受到的自定义讯息为", String.valueOf(contentBody));
                                    //模块类型--判断
                                    if (contentBody.containsKey(CustomConstants.Message.MODULE_NAME_KEY)) {
                                        //获取moudle-pushCoinGame 推币机
                                        if (CustomConvertUtils.ContainsMessageModuleKey(contentBody, CustomConstants.Message.MODULE_NAME_KEY, CustomConstants.CoinPusher.MODULE_NAME)) {
                                            V2TIMCustomManagerUtil.CoinPusherManager(contentBody);
                                        }
                                        //通话模块
                                        if(CustomConvertUtils.ContainsMessageModuleKey(contentBody, CustomConstants.Message.MODULE_NAME_KEY, CustomConstants.CallingMessage.MODULE_NAME)){
                                            Map<String,Object> pushCoinGame = CustomConvertUtils.ConvertMassageModule(contentBody,CustomConstants.Message.MODULE_NAME_KEY,CustomConstants.CallingMessage.MODULE_NAME,CustomConstants.Message.CUSTOM_CONTENT_BODY);
                                            if(ObjectUtils.isNotEmpty(pushCoinGame)){
                                                //消息类型--判断
                                                if(pushCoinGame.containsKey(CustomConstants.Message.CUSTOM_MSG_KEY)){
                                                    if (CustomConvertUtils.ContainsMessageModuleKey(pushCoinGame,CustomConstants.Message.CUSTOM_MSG_KEY,CustomConstants.CallingMessage.CALLING_PROFIT_TIPS)){
                                                        // //通话中收益
                                                        Map<String,Object> startWinning = CustomConvertUtils.ConvertMassageModule(pushCoinGame,CustomConstants.Message.CUSTOM_MSG_KEY,CustomConstants.CallingMessage.CALLING_PROFIT_TIPS,CustomConstants.Message.CUSTOM_MSG_BODY);
                                                        if(ObjectUtils.isNotEmpty(startWinning)){
                                                            CallingStatusEntity callingStatusEntity = GsonUtils.fromJson(GsonUtils.toJson(startWinning),CallingStatusEntity.class);
                                                            if(callingStatusEntity!=null){
                                                                Integer roomStatus = callingStatusEntity.getRoomStatus();
                                                                if (roomStatus != null && roomStatus != 101) {
                                                                    hangup();
                                                                }
                                                                maleBalanceMoney = callingStatusEntity.getPayerCoinBalance();
                                                                payeeProfits = callingStatusEntity.getPayeeProfits().doubleValue();
                                                                totalMinutes = callingStatusEntity.getTotalMinutes() * 60;
                                                                totalMinutesRemaining = totalMinutes - TimeCount;
                                                                callInfoLoaded = true;
                                                            }

                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                break;
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

    /**
     * 显示礼物弹幕
     *
     * @param giftEntity
     */
    private void showGiftBarrage(GiftEntity giftEntity) {
        String sexText = isMale ? StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt3) : StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt2);
        String messageText = otherUserInfoField.get().getNickname() + " " + StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt1) + " " + sexText + " " + giftEntity.getTitle() + "x" + giftEntity.getAmount();
        SpannableString stringBuilder = new SpannableString(messageText);
        ForegroundColorSpan blueSpanWhite = new ForegroundColorSpan(ColorUtils.getColor(R.color.white));
        stringBuilder.setSpan(blueSpanWhite, 0, messageText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        putRcvItemMessage(stringBuilder, giftEntity.getImgPath(), false);
    }

    /**
     * 礼物收益提示
     *
     * @param giftEntity
     */
    private void giftIncome(GiftEntity giftEntity) {
        double total = giftEntity.getAmount().intValue() * giftEntity.getProfitTwd().doubleValue();
        String itemMessage = String.format(StringUtils.getString(R.string.profit), String.format("%.2f", total));
        SpannableString itemMessageBuilder = new SpannableString(itemMessage);
        itemMessageBuilder.setSpan(new MyImageSpan(getApplication(),R.drawable.icon_crystal),0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        itemMessageBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.white)), 1, itemMessage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        putRcvItemMessage(itemMessageBuilder, null, false);
    }

    public void getMallWithdrawTipsInfo(Integer channel){
        model.getMallWithdrawTipsInfo(channel)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<MallWithdrawTipsInfoEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<MallWithdrawTipsInfoEntity> response) {
                        MallWithdrawTipsInfoEntity data = response.getData();
                        uc.clickCrystalExchange.setValue(data);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //发送心跳包
    public void callingKeepAlive(Integer roomId,String roomIdStr){
        Map<String,Object> mapData = new HashMap<>();
        mapData.put("roomId",roomId);
        mapData.put("roomIdStr",roomIdStr);
        model.callingKeepAlive(ApiUitl.getBody(GsonUtils.toJson(mapData)))
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {

                    }
                });
    }

    public void updateCallingStatus(int eventType){
        CallChatingConstant.updateCallingStatus(roomId, roomIdStr, eventType);
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
                            if (sayHiEntityList.size() > 1) {
                                sayHiEntityHidden.set(false);
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
        //水晶兑换规则
        public SingleLiveEvent<MallWithdrawTipsInfoEntity> clickCrystalExchange = new SingleLiveEvent<>();
        //接听成功
        public SingleLiveEvent<Void> callAudioStart = new SingleLiveEvent<>();
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
        //打开推币机弹窗
        public SingleLiveEvent<Void> coinPusherRoomEvent = new SingleLiveEvent<>();
        //处理推币机信令转主线程
        public SingleLiveEvent<CallGameCoinPusherEntity> callingToGamePlayingEvent = new SingleLiveEvent<>();
    }

    //追踪
    public void addLike(boolean isHangup) {
        model.addCollect(otherUserInfoField.get().getId())
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
                            collected = true;
                            collectedField.set(true);
                            ToastUtils.showShort(R.string.playfun_cancel_zuizong_3);
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
        boolean result;
        try {
            new Gson().fromJson(str, Map.class);
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return result;

    }

}
