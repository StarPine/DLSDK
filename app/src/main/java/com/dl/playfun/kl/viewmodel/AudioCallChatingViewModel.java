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
import com.dl.playfun.entity.RtcRoomMessageEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.event.AudioCallingCancelEvent;
import com.dl.playfun.event.CallingStatusEvent;
import com.dl.playfun.event.CallingToGamePlayingEvent;
import com.dl.playfun.event.RtcRoomMessageEvent;
import com.dl.playfun.kl.CallChatingConstant;
import com.dl.playfun.kl.Utils;
import com.dl.playfun.kl.view.Ifinish;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.manager.V2TIMCustomManagerUtil;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.utils.LogUtils;
import com.dl.playfun.utils.StringUtil;
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
    //????????????-???
    public int TimeCount = 0;
    //?????????????????????
    public boolean sendGiftBagSuccess = false;
    public Integer roomId;
    public String roomIdStr;
    public String fromUserId;
    public String toUserId;
    //?????????????????????
    public int heartBeatInterval = 10;
    //????????????????????????
    public boolean callInfoLoaded = false;
    //?????????????????????
    public int maleBalanceMoney = 0;
    //??????????????????
    public int totalMinutes = 0;
    //???????????????????????????
    public int totalMinutesRemaining = 0;
    //????????????
    public double payeeProfits;
    //???????????????
    int disconnectTime = 0;

    //????????????????????????
    public int sayHiePosition = 0;
    public int sayHiePage = 1;

    //????????????????????????
    public boolean isMale = false;
    //????????????
    public boolean isShowTipMoney = false;
    //??????????????????????????????
    public boolean isPayee = false;
    //???????????????0?????????1?????????
    public Boolean collected;
    //?????????????????????????????????
    public int balanceNotEnoughTipsMinutes;
    //????????????????????????
    public boolean flagMoneyNotWorth = false;
    public ObservableField<Boolean> isShowCountdown = new ObservableField(false);
    //?????????????????????
    public ObservableField<Boolean> tipSwitch = new ObservableField(true);

    //????????????
    public ObservableField<String> timeTextField = new ObservableField<>();
    //??????????????????
    public ObservableField<CallUserInfoEntity> otherUserInfoField = new ObservableField<>();
    //??????????????????
    public ObservableField<UserDataEntity> currentUserInfoField = new ObservableField<>();

    //?????????????????????????????????
    public ObservableField<Boolean> isHideExchangeRules = new ObservableField<>(false);
    //???????????????????????????
    public ObservableBoolean maleTextLayoutSHow = new ObservableBoolean(false);
    //??????????????????
    public ObservableField<String> maleTextMoneyField = new ObservableField();
    //??????????????????????????????
    public ObservableBoolean girlEarningsField = new ObservableBoolean(false);
    //????????????
    public ObservableField<SpannableString> girlEarningsText = new ObservableField<>();
    //??????????????????
    public ObservableBoolean collectedField = new ObservableBoolean(false);
    //????????????
    public ObservableBoolean micMuteField = new ObservableBoolean(false);
    //????????????
    public ObservableBoolean handsFreeField = new ObservableBoolean(false);
    //????????????
    public List<CallingInfoEntity.SayHiEntity> sayHiEntityList = new ArrayList<>();
    //????????????
    public ObservableField<CallingInfoEntity.SayHiEntity> sayHiEntityField = new ObservableField<>();
    //????????????????????????
    public ObservableBoolean sayHiEntityHidden = new ObservableBoolean(true);
    public BindingRecyclerViewAdapter<AudioCallChatingItemViewModel> adapter = new BindingRecyclerViewAdapter<>();
    public ObservableList<AudioCallChatingItemViewModel> observableList = new ObservableArrayList<>();
    public ItemBinding<AudioCallChatingItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_call_audio_chating);
    //??????????????????
    public ObservableField<RtcRoomMessageEvent> rtcRoomMessageField = new ObservableField<>();

    private final static String TAG = "trtcJoy";
    private static final int MIN_DURATION_SHOW_LOW_QUALITY = 5000; //????????????????????????????????????

    public UIChangeObservable uc = new UIChangeObservable();
    //??????????????????
    public BindingCommand<Void> referMoney = new BindingCommand<>(() -> uc.sendUserGiftError.postValue(false));
    /**
     * ??????????????????
     */
    public BindingCommand<Void> crystalOnClick = new BindingCommand<>(() -> getMallWithdrawTipsInfo(1));

    protected Ifinish mView;
    protected DLRTCCallingDelegate mTRTCCallingDelegate;
    //    private TUICalling.Role mRole;
    public View.OnClickListener closeOnclick = v -> uc.closeViewHint.call();
    //????????????
    public BindingCommand<Void> giftBagOnClickCommand = new BindingCommand<>(new BindingAction() {
        @Override
        public void call() {
            AppContext.instance().logEvent(AppsFlyerEvent.voicecall_gift);
            uc.callGiftBagAlert.call();
        }
    });
    //????????????????????????????????????
    public BindingCommand<Void> closeMoney = new BindingCommand<>(() -> maleTextLayoutSHow.set(false));
    //?????????????????????
    public ObservableBoolean coinPusherRoomShow = new ObservableBoolean(false);
    //????????????
    public CallGameCoinPusherEntity _callGameCoinPusherEntity;
    //??????????????????????????????????????????
    public BindingCommand<Void> closeMoney2 = new BindingCommand<>(() -> {
        isShowCountdown.set(false);
        girlEarningsField.set(false);
    });

    //???????????????
    public BindingCommand<Void> coinPusherRoomClick = new BindingCommand<>(() -> {
       uc.coinPusherRoomEvent.call();
    });

    //?????????
    private Disposable mSubscription;
    private Disposable mCallingToGameSubscription;
    private Disposable mRtcRoomMessageSubscription;
    private Disposable mCallingStatusEventSubscription;
    private long mSelfLowQualityTime;
    private long mOtherPartyLowQualityTime;

    public AudioCallChatingViewModel(@NonNull @NotNull Application application, AppRepository model) {
        super(application, model);
    }

    //??????
    public BindingCommand<Void> addlikeOnClickCommand = new BindingCommand<>(() -> {
        AppContext.instance().logEvent(AppsFlyerEvent.voicecall_follow);
        addLike(false);
    });

    //?????????????????????
    public BindingCommand<Void> micMuteOnClickCommand = new BindingCommand<>(() -> {
        if (micMuteField.get()) {//????????????
            ToastUtils.showShort(R.string.playfun_call_message_deatail_txt_4);
        } else {
            ToastUtils.showShort(R.string.playfun_call_message_deatail_txt_3);
        }
        boolean minMute = !micMuteField.get();
        micMuteField.set(minMute);
        DLRTCAudioManager.Companion.getInstance().audioRoute(minMute);
    });

    //????????????
    public BindingCommand<Void> handsFreeOnClickCommand = new BindingCommand<>(() -> {
        if (handsFreeField.get()) {//????????????
            ToastUtils.showShort(R.string.playfun_call_message_deatail_txt_2);
        } else {
            ToastUtils.showShort(R.string.playfun_call_message_deatail_txt_1);
        }
        boolean handsFree = !handsFreeField.get();
        handsFreeField.set(handsFree);
        DLRTCAudioManager.Companion.getInstance().audioRoute(handsFree);
    });
    //????????????????????????
    public BindingCommand<Void> upSayHiEntityOnClickCommand = new BindingCommand<>(() -> {
        AppContext.instance().logEvent(AppsFlyerEvent.voicecall_ice_change);
        uc.startUpSayHiAnimotor.call();
//            getSayHiList();
    });
    //????????????????????????
    public BindingCommand<Void> colseSayHiEntityOnClickCommand = new BindingCommand<>(() -> {
        AppContext.instance().logEvent(AppsFlyerEvent.voicecall_ice_close);
        sayHiEntityHidden.set(true);
    });

    //??????????????????
    public void callingInviteUser(String inviterImId,String receiverImId){
//        callingType	???	Integer	???????????????1=?????????2=??????
//                * inviterImId	???	String	?????????IM ID
//     * receiverImId	???	String	?????????IM ID
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
                        callingInviteUserApply(callingInviteInfo);
                    }
                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }
    //??????????????????
    public void callingInviteUserApply(CallUserRoomInfoEntity callingInviteInfo){
        if(callingInviteInfo!=null){
            if(!callingInviteInfo.getPayerImId().equals(ConfigManager.getInstance().getUserImID())){
                //?????????
                isPayee = true;
            }
            //??????????????????0?????????????????????????????????10???
            if(callingInviteInfo.getHeartBeatInterval() > 0){
                heartBeatInterval = callingInviteInfo.getHeartBeatInterval();
            }
        }
        uc.callAudioStart.call();
        //????????????
        DLRTCAudioManager.Companion.getInstance().enableAGC(true);
        DLRTCAudioManager.Companion.getInstance().enableAEC(true);
        DLRTCAudioManager.Companion.getInstance().enableANS(true);
    }


    //????????????
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

    //??????RxBus
    @Override
    public void registerRxBus() {
        super.registerRxBus();
        mSubscription = RxBus.getDefault().toObservable(AudioCallingCancelEvent.class)
                .subscribe(event -> {
                    // ??????????????????
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
        //??????????????????
        mRtcRoomMessageSubscription = RxBus.getDefault().toObservable(RtcRoomMessageEvent.class)
                .subscribe(rtcRoomMessageEvent -> {
                    Log.e("???????????????????????????",rtcRoomMessageEvent.toString());
                    RtcRoomMessageEntity rtcRoomMessageEntity = rtcRoomMessageEvent.getRtcRoomMessageEntity();
                    if(Objects.equals(rtcRoomMessageEntity.getActivityData().getPlayUserId(),currentUserInfoField.get().getImUserId())){
                        rtcRoomMessageField.set(rtcRoomMessageEvent);
                        coinPusherRoomShow.set(true);
                    }
                });
        mCallingStatusEventSubscription = RxBus.getDefault().toObservable(CallingStatusEvent.class)
                .subscribe(callingStatusEvent -> {

                    if(callingStatusEvent!=null && callingStatusEvent.getCallingStatusEntity()!=null){
                        CallingStatusEntity callingStatusEntity = callingStatusEvent.getCallingStatusEntity();
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
                });
        //???????????????????????????
        RxSubscriptions.add(mSubscription);
        RxSubscriptions.add(mCallingToGameSubscription);
        RxSubscriptions.add(mRtcRoomMessageSubscription);
    }

    //??????RxBus
    @Override
    public void removeRxBus() {
        super.removeRxBus();
        //?????????????????????????????????
        RxSubscriptions.remove(mSubscription);
        RxSubscriptions.remove(mCallingToGameSubscription);
        RxSubscriptions.remove(mRtcRoomMessageSubscription);
        destroyIMListener();
    }

    public void init(Ifinish iview) {
        isMale = ConfigManager.getInstance().isMale();
        isShowTipMoney = ConfigManager.getInstance().getTipMoneyShowFlag();
        initListener();
        mView = iview;
        //??????IM??????
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
                //??????????????????
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
                Utils.show("??????????????????");
            }

            @Override
            public void onUserAudioAvailable(String userId, boolean isVideoAvailable) {
                Log.i(TAG, "onUserAudioAvailable: userId???"+userId + ", onUserAudioAvailable ="+isVideoAvailable);
                updateCallingStatus(CallChatingConstant.pusherAudioStart);
            }

        };
        DLRTCInternalListenerManager.Companion.getInstance().addDelegate(mTRTCCallingDelegate);
    }

    protected void unListener() {
        if (null != mTRTCCallingDelegate) {
            DLRTCInternalListenerManager.Companion.getInstance().removeDelegate(mTRTCCallingDelegate);
        }
    }

    //localQuality ????????????????????? remoteQualityList??????????????????????????????????????????1v1?????????????????????
    protected void updateNetworkQuality(TRTCCloudDef.TRTCQuality localQuality, List<TRTCCloudDef.TRTCQuality> remoteQualityList) {
        //????????????????????????????????????????????????????????????????????????
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
            } else {//??????
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
            } else {//??????
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

    //??????IM??????
    private void initIMListener() {
        V2TIMManager.getMessageManager().addAdvancedMsgListener(v2TIMAdvancedMsgListener);
    }

    private void destroyIMListener(){
        V2TIMManager.getMessageManager().removeAdvancedMsgListener(v2TIMAdvancedMsgListener);
    }

    V2TIMAdvancedMsgListener v2TIMAdvancedMsgListener = new V2TIMAdvancedMsgListener() {
        @Override
        public void onRecvNewMessage(V2TIMMessage msg) {//???????????????
            if (msg != null && otherUserInfoField.get() != null) {
                TUIMessageBean info = ChatMessageBuilder.buildMessage(msg);
                if (info != null) {
                    switch (info.getMsgType()){
                        //??????????????????
                        case 1:
                            if (info.getV2TIMMessage().getSender().equals(otherUserInfoField.get().getImId())) {
                                String text = String.valueOf(info.getExtra());
                                if (isJSON2(text) && text.indexOf("type") != -1) {//????????????????????????
                                    Map<String, Object> map_data = new Gson().fromJson(text, Map.class);
                                    //????????????
                                    if (map_data != null && map_data.get("type") != null && map_data.get("type").equals("message_gift")
                                            && map_data.get("is_accost") == null) {
                                        GiftEntity giftEntity = IMGsonUtils.fromJson(String.valueOf(map_data.get("data")), GiftEntity.class);
                                        uc.acceptUserGift.postValue(giftEntity);
                                        //??????????????????
                                        showGiftBarrage(giftEntity);
                                        //??????????????????
                                        giftIncome(giftEntity);
                                    }else if (map_data != null && map_data.get("type") != null && map_data.get("type").equals("message_countdown")) {//??????????????????
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
                        //?????????????????????
                        case 2:
                            V2TIMCustomElem v2TIMCustomElem = info.getV2TIMMessage().getCustomElem();
                            Map<String, Object> contentBody = CustomConvertUtils.CustomMassageConvertMap(v2TIMCustomElem);
                            if (ObjectUtils.isNotEmpty(contentBody)) {
                                Log.e("????????????????????????????????????", String.valueOf(contentBody));
                                //????????????--??????
                                if (contentBody.containsKey(CustomConstants.Message.MODULE_NAME_KEY)) {
                                    //??????moudle-pushCoinGame ?????????
                                    if (CustomConvertUtils.ContainsMessageModuleKey(contentBody, CustomConstants.Message.MODULE_NAME_KEY, CustomConstants.CoinPusher.MODULE_NAME)) {
                                        V2TIMCustomManagerUtil.CoinPusherManager(contentBody);
                                    }
                                    //RTC???????????????????????????
                                    if(CustomConvertUtils.ContainsMessageModuleKey(contentBody, CustomConstants.Message.MODULE_NAME_KEY,CustomConstants.RtcRoomMessage.MODULE_NAME)){
                                        Map<String,Object> rtcRoomMsg = CustomConvertUtils.ConvertMassageModule(contentBody,CustomConstants.Message.MODULE_NAME_KEY,CustomConstants.RtcRoomMessage.MODULE_NAME,CustomConstants.Message.CUSTOM_CONTENT_BODY);
                                        V2TIMCustomManagerUtil.RtcRoomMessageManager(rtcRoomMsg);
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
    };

    /**
     * ??????????????????
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
     * ??????????????????
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

    //???????????????
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

    //??????????????????
    public void getSayHiList() {
        //????????????????????????
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

    //?????????????????????
    public void putRcvItemMessage(SpannableString stringBuilder, String imgPath, boolean sendGiftBagShow) {
        observableList.add(new AudioCallChatingItemViewModel(AudioCallChatingViewModel.this, stringBuilder, imgPath, sendGiftBagShow));
        uc.scrollToEnd.postValue(null);
    }

    public class UIChangeObservable {
        //??????????????????
        public SingleLiveEvent<MallWithdrawTipsInfoEntity> clickCrystalExchange = new SingleLiveEvent<>();
        //????????????
        public SingleLiveEvent<Void> callAudioStart = new SingleLiveEvent<>();
        //????????????????????????
        public SingleLiveEvent<Void> callGiftBagAlert = new SingleLiveEvent<>();
        //?????????????????????????????????
        public SingleLiveEvent<Boolean> sendUserGiftError = new SingleLiveEvent<>();
        //??????????????????
        public SingleLiveEvent<Map<String, Object>> sendUserGiftAnim = new SingleLiveEvent<>();
        //??????????????????
        public SingleLiveEvent<GiftEntity> acceptUserGift = new SingleLiveEvent<>();
        //????????????
        public SingleLiveEvent<Void> closeViewHint = new SingleLiveEvent<>();
        //?????????????????????
        public SingleLiveEvent<Void> scrollToEnd = new SingleLiveEvent<>();
        public SingleLiveEvent<Void> startUpSayHiAnimotor = new SingleLiveEvent<>();
        //?????????????????????
        public SingleLiveEvent<Void> coinPusherRoomEvent = new SingleLiveEvent<>();
        //?????????????????????????????????
        public SingleLiveEvent<CallGameCoinPusherEntity> callingToGamePlayingEvent = new SingleLiveEvent<>();
    }

    //??????
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
