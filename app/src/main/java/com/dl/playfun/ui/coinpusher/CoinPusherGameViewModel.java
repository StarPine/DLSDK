package com.dl.playfun.ui.coinpusher;

import android.app.Application;
import android.app.Dialog;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.dl.lib.util.log.MPTimber;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.CallUserInfoEntity;
import com.dl.playfun.entity.CallUserRoomInfoEntity;
import com.dl.playfun.entity.CallingInfoEntity;
import com.dl.playfun.entity.CallingStatusEntity;
import com.dl.playfun.entity.CoinPusherBalanceDataEntity;
import com.dl.playfun.entity.GiftBagEntity;
import com.dl.playfun.event.CoinPusherGamePlayingEvent;
import com.dl.playfun.kl.Utils;
import com.dl.playfun.manager.V2TIMCustomManagerUtil;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.utils.LogUtils;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.utils.ToastCenterUtils;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.dl.rtc.calling.base.DLRTCCalling;
import com.dl.rtc.calling.manager.DLRTCAudioManager;
import com.dl.rtc.calling.manager.DLRTCStartShowUIManager;
import com.dl.rtc.calling.model.DLRTCDataMessageType;
import com.google.gson.Gson;
import com.tencent.custom.GiftEntity;
import com.tencent.custom.IMGsonUtils;
import com.tencent.imsdk.v2.V2TIMAdvancedMsgListener;
import com.tencent.imsdk.v2.V2TIMCustomElem;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.qcloud.tuicore.TUILogin;
import com.tencent.qcloud.tuicore.custom.CustomConstants;
import com.tencent.qcloud.tuicore.custom.CustomConvertUtils;
import com.tencent.qcloud.tuikit.tuichat.bean.message.TUIMessageBean;
import com.tencent.qcloud.tuikit.tuichat.util.ChatMessageBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.disposables.Disposable;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.RxSubscriptions;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.StringUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Author: ?????????
 * Time: 2022/8/26 11:08
 * Description: This is CoinPusherGameViewModel
 */
public class CoinPusherGameViewModel extends BaseViewModel <AppRepository> {
    //????????????
    public final String loadingPlayer = "loadingPlayer";
    //????????????
    public String gamePlayingState;

    //livedata????????????
    public UIChangeObservable gameUI = new UIChangeObservable();
    public ObservableInt totalMoney = new ObservableInt(0);

    //?????????
    private Disposable coinPusherGamePlayingSubscription;

    private IMAdvancedMsgListener imAdvancedMsgListener;

    //????????????-????????????????????????????????????
    public ObservableBoolean circuseeStatus = new ObservableBoolean(false);
    //????????????
    public boolean triangleSwitch = true;

    public GameCallEntity gameCallEntity;
    //?????????????????????????????????
    public ObservableBoolean callZoomOuViewFlag = new ObservableBoolean(false);

    public int mTimeCount = 0;
    //?????????
    public ObservableBoolean callingOnTheLine = new ObservableBoolean(false);
    //????????????
    public ObservableBoolean callingDropped = new ObservableBoolean(true);
    //??????????????????????????????
    public ObservableField<CallUserInfoEntity> otherCallInfoEntity = new ObservableField<>();
    //????????????????????????
    public ObservableField<CallUserRoomInfoEntity> callUserRoomInfoEntity = new ObservableField<>();
    //?????????????????????
    public ObservableField<String> maleCallingHint = new ObservableField<>("");
    //??????????????????????????????
    public boolean isCallingPay = false;
    //?????????????????????????????????
    public int balanceNotEnoughTipsMinutes;
    //????????????????????????
    public int payUserBalanceMoney = 0;

    //????????????ID
    public Integer _gameRoomId = 0;

    //???????????????
    public ObservableBoolean muteEnabled = new ObservableBoolean(false);
    //????????????
    public ObservableBoolean micMuteField = new ObservableBoolean(false);
    //????????????
    public ObservableBoolean handsFreeField = new ObservableBoolean(false);
    //???????????? ??????or????????????
    public ObservableBoolean makeCallTypeField = new ObservableBoolean(true);


    public CoinPusherGameViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
    }
    //??????????????????
    public BindingCommand<Void> gameCloseView = new BindingCommand<>(()->gameUI.backViewApply.call());
    //??????????????????
    public BindingCommand<Void> playCoinClick = new BindingCommand<>(() -> {
        playingCoinPusherThrowCoin(_gameRoomId);
    });
    //??????????????????
    public BindingCommand<Void> playPusherActClick = new BindingCommand<>(() -> {
        playingCoinPusherAct(_gameRoomId);
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
        DLRTCAudioManager.Companion.getInstance().muteLocalAudio(minMute);
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

    //?????????????????????
    public BindingCommand<Void> muteEnabledClick = new BindingCommand<>(() -> {
        boolean enabled = !muteEnabled.get();
        muteEnabled.set(enabled);
        gameUI.muteEnabledEvent.postValue(enabled);
    });
    //????????????????????????
    public BindingCommand<Void> triangleClick = new BindingCommand<>(() -> {
        triangleSwitch = !triangleSwitch;
        gameUI.triangleEvent.postValue(triangleSwitch);
    });
    //??????????????????
    public BindingCommand<Void> callRejectClick = new BindingCommand<>(() -> {
        Log.e("CoinPusherGameActivity","??????????????????======================");
        ToastUtils.showShort(AppContext.instance().getString(R.string.playfun_the_other_party_refuses_to_answer));
        if(gameCallEntity != null){
            if(gameCallEntity.getCallingType() == DLRTCDataMessageType.DLInviteRTCType.dl_rtc_audio){
                //DLRTCAudioManager.Companion.getInstance().hangup();
            }else{
                //DLRTCVideoManager.Companion.getInstance().hangup();
            }
            DLRTCStartShowUIManager.Companion.getInstance().inviteUserReject();
            gameUI.hangupCallingEvent.call();
        }
    });
    //??????????????????
    public BindingCommand<Void> callAcceptClick = new BindingCommand<>(() -> {
        gameUI.callCheckPermissionEvent.call();
    });

    //????????????
    public BindingCommand<Void> giftBagOnClickCommand = new BindingCommand<>(new BindingAction() {
        @Override
        public void call() {
            AppContext.instance().logEvent(AppsFlyerEvent.voicecall_gift);
            gameUI.sendGiftBagEvent.call();
        }
    });

    //??????-????????????
    public BindingCommand<Void> makeCallUserClick = new BindingCommand<>(() -> {
        if(gameCallEntity!=null){
            int callingType;
            //????????????
            if(gameCallEntity.getCallingType() == DLRTCDataMessageType.DLInviteRTCType.dl_rtc_audio){
                callingType = 1;
            }else{
                //????????????
                callingType = 2;
            }
            String fromUserId = TUILogin.getUserId();
            String answerUserId;
            MPTimber.tag("CoinPusherGameActivity").d("fromUserId??? "+fromUserId+" ,???????????????"+gameCallEntity.getInviteUserId() +" ==="+fromUserId.equals(gameCallEntity.getInviteUserId()));
            if(!fromUserId.equals(gameCallEntity.getInviteUserId())){
                answerUserId = gameCallEntity.getInviteUserId();
            }else{
                answerUserId = gameCallEntity.getAcceptUserId();
            }
            gameCallEntity.setCallingRole(DLRTCCalling.Role.CALL);
            callingInviteUser(callingType, fromUserId, answerUserId);
        }
    });

    //??????????????????????????????
    public BindingCommand<Void> callZoomOuViewCLick = new BindingCommand<>(() -> {
        callZoomOuViewFlag.set(!callZoomOuViewFlag.get());
        gameUI.callZoomOuViewEvent.postValue(callZoomOuViewFlag.get());
    });

    //????????????????????????
    private boolean isLittleGameWinning(){
        if(StringUtils.isEmpty(gamePlayingState)){
            return false;
        }
        return gamePlayingState.equals(CustomConstants.CoinPusher.LITTLE_GAME_WINNING);
    }
    //??????
    public void playingCoinPusherThrowCoin(Integer roomId){
        //????????????????????????????????????????????????????????? ??????????????????
        if(isLittleGameWinning()){
            gamePlayingState = CustomConstants.CoinPusher.LITTLE_GAME_WINNING;
        }else{
            gamePlayingState = loadingPlayer;
        }
        Log.e("?????????????????????","??????ID:"+String.valueOf(roomId));
        model.playingCoinPusherThrowCoin(roomId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> {
                    loadingShow();
                    //??????????????????
                    gameUI.playingBtnEnable.postValue(false);
                })
                .subscribe(new BaseObserver<BaseDataResponse<CoinPusherBalanceDataEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CoinPusherBalanceDataEntity> coinPusherDataEntityResponse) {
                        CoinPusherBalanceDataEntity coinPusherBalanceDataEntity = coinPusherDataEntityResponse.getData();
                        if(ObjectUtils.isNotEmpty(coinPusherBalanceDataEntity)){
                            totalMoney.set(coinPusherBalanceDataEntity.getTotalGold());
                        }
                        gameUI.playingBtnEnable.postValue(true);
                        //???????????????????????????????????????????????????
                        if(isLittleGameWinning()){
                            //???????????????
                            gameUI.cancelDownTimeEvent.postValue(null);
                        }else{
                            //???????????????????????????????????????????????????
                            gameUI.resetDownTimeEvent.postValue(null);
                            gamePlayingState = null;
                        }

                    }

                    @Override
                    public void onError(RequestException e) {
                        //????????????
                        if(e.getCode() == 21001){
                            gameUI.payDialogViewEvent.call();
                            gameUI.playingBtnEnable.postValue(true);
                            if(!isLittleGameWinning()){
                                //????????????????????????
                                gamePlayingState = null;
                            }
                        }else if(e.getCode() == 72000){
                            //??????--????????????????????????
                            gameUI.playingBtnEnable.postValue(false);
                            //????????????
                            gamePlayingState = CustomConstants.CoinPusher.START_WINNING;
                            gameUI.cancelDownTimeEvent.postValue(null);
                        }else{
                            gameUI.playingBtnEnable.postValue(true);
                            //????????????????????????
                            gamePlayingState = null;
                        }
                        super.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        loadingHide();
                    }
                });
    }
    //????????????
    public void playingCoinPusherAct(Integer roomId){
        model.playingCoinPusherAct(roomId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> loadingShow())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {

                    }

                    @Override
                    public void onComplete() {
                        loadingHide();
                    }
                });
    }
    //??????????????????
    public void getCallingUserInfo(Integer userId, String imId){
        model.callingUserInfo(userId, imId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> loadingShow())
                .subscribe(new BaseObserver<BaseDataResponse<CallUserInfoEntity>>(){
                    @Override
                    public void onSuccess(BaseDataResponse<CallUserInfoEntity> response) {
                        CallUserInfoEntity callingInviteInfo = response.getData();
                        otherCallInfoEntity.set(callingInviteInfo);
                        //gameCallEntity.setRoomId(callingInviteInfo);
                    }
                    @Override
                    public void onComplete() {
                        loadingHide();
                    }
                });
    }

    //???????????????????????????
    public void callingInviteUser(int callingType, String inviterImId,String receiverImId){
//        callingType	???	Integer	???????????????1=?????????2=??????
//                * inviterImId	???	String	?????????IM ID
//     * receiverImId	???	String	?????????IM ID
        Map<String, Object> mapData = new HashMap<>();
        mapData.put("callingType",callingType);
        mapData.put("inviterImId",inviterImId);
        mapData.put("receiverImId",receiverImId);
        model.callingInviteUser(ApiUitl.getBody(GsonUtils.toJson(mapData)))
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> loadingShow())
                .subscribe(new BaseObserver<BaseDataResponse<CallUserRoomInfoEntity>>(){
                    @Override
                    public void onSuccess(BaseDataResponse<CallUserRoomInfoEntity> response) {
                        CallUserRoomInfoEntity callingInviteInfo = response.getData();
                        callUserRoomInfoEntity.set(callingInviteInfo);
                        //????????????
                        callingOnTheLine.set(true);
                        Utils.StartGameCallSomeone(callingType, receiverImId, callingInviteInfo.getRoomId());
                        DLRTCAudioManager.Companion.getInstance().enableAGC(true);
                        DLRTCAudioManager.Companion.getInstance().enableAEC(true);
                        DLRTCAudioManager.Companion.getInstance().enableANS(true);
                        //gameUI.acceptCallingEvent.call();
                    }
                    @Override
                    public void onComplete() {
                        loadingHide();
                    }
                });
    }

    public int isRealManVisible(CallUserInfoEntity itemEntity) {
        if (itemEntity != null && itemEntity.getIsVip() != 1) {
            if (itemEntity.getCertification() == 1) {
                return View.VISIBLE;
            } else {
                return View.GONE;
            }
        }else {
            return View.GONE;
        }
    }

    public int isVipVisible(CallUserInfoEntity itemEntity) {
        if (itemEntity != null && itemEntity.getSex() == 1 && itemEntity.getIsVip() == 1) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    public int isGoddessVisible(CallUserInfoEntity itemEntity) {
        if (itemEntity != null && itemEntity.getSex() == 0 && itemEntity.getIsVip() == 1) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }
    //??????????????????
    public String getCallingLayoutTitles(CallUserInfoEntity itemEntity) {
        if(gameCallEntity!=null){
            if(gameCallEntity.getCallingType() == DLRTCDataMessageType.DLInviteRTCType.dl_rtc_audio){
                return StringUtils.getString(R.string.playfun_game_audio_hint);
            }else{
                return StringUtils.getString(R.string.playfun_game_video_hint);
            }
        }
        return null;
    }


    //??????????????????????????????
    public String tvTotalMoneyRefresh(int moneyNum){
        return moneyNum > 99999 ? "99999+" : moneyNum+"";
    }

    public static class UIChangeObservable{
        //???????????????
        public SingleLiveEvent<Void> cancelDownTimeEvent = new SingleLiveEvent<>();
        //???????????????
        public SingleLiveEvent<Void> resetDownTimeEvent = new SingleLiveEvent<>();
        //????????????loading?????????
        public SingleLiveEvent<Void> loadingShow = new SingleLiveEvent<>();
        //???????????????Loading??????
        public SingleLiveEvent<Void> loadingHide = new SingleLiveEvent<>();
        //toast????????????
        public SingleLiveEvent<CoinPusherGamePlayingEvent> toastCenter = new SingleLiveEvent<>();
        //????????????????????????
        public SingleLiveEvent<Boolean> playingBtnEnable = new SingleLiveEvent<>();
        //???????????????
        public SingleLiveEvent<Void> backViewApply = new SingleLiveEvent<>();
        //?????????????????????????????????
        public SingleLiveEvent<Void> payDialogViewEvent = new SingleLiveEvent<>();
        //?????????????????????
        public SingleLiveEvent<Boolean> muteEnabledEvent = new SingleLiveEvent<>();
        //????????????????????? : ???????????????
        public SingleLiveEvent<Boolean> triangleEvent = new SingleLiveEvent<>();
        //??????????????????????????????
        public SingleLiveEvent<Boolean> callZoomOuViewEvent = new SingleLiveEvent<>();
        //????????????????????????????????????
        public SingleLiveEvent<Void> callCheckPermissionEvent = new SingleLiveEvent<>();
        //????????????
        public SingleLiveEvent<Void> acceptCallingEvent = new SingleLiveEvent<>();
        //????????????
        public SingleLiveEvent<Void> hangupCallingEvent = new SingleLiveEvent<>();
        //????????????????????????
        public SingleLiveEvent<Void> sendGiftBagEvent = new SingleLiveEvent<>();
        //??????????????????
        public SingleLiveEvent<Map<String, Object>> sendUserGiftAnim = new SingleLiveEvent<>();
        //??????????????????
        public SingleLiveEvent<GiftEntity> acceptUserGift = new SingleLiveEvent<>();
    }
    //??????loading
    public void loadingShow(){
        gameUI.loadingShow.call();
    }
    //??????loading
    public void loadingHide(){
        gameUI.loadingHide.call();
    }

    //??????IM???????????????
    public void initIMListener () {
        if(imAdvancedMsgListener==null){
            imAdvancedMsgListener = new IMAdvancedMsgListener();
            V2TIMManager.getMessageManager().addAdvancedMsgListener(imAdvancedMsgListener);
        }
    }

    //??????IM????????????
    public void removeIMListener(){
        if(imAdvancedMsgListener!=null){
            V2TIMManager.getMessageManager().removeAdvancedMsgListener(imAdvancedMsgListener);
        }
    }
    //??????????????????
    public void getRoomStatus(Integer roomId) {
        model.getRoomStatus(roomId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<CallingStatusEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CallingStatusEntity> response) {
                        CallingStatusEntity data = response.getData();
                        Integer roomStatus = data.getRoomStatus();
                        LogUtils.i("onSuccess: " + roomStatus);
                        if (roomStatus != null && roomStatus != 101) {
                            callRejectClick.execute();
                        }
                    }
                });
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
                        Map<String, Object> mapData = new HashMap<>();
                        mapData.put("account", amount);
                        mapData.put("giftEntity", giftEntity);
                        gameUI.sendUserGiftAnim.postValue(mapData);
                    }

                    @Override
                    public void onError(RequestException e) {
                        dialog.dismiss();
                        dismissHUD();
                        if (e.getCode() != null && e.getCode().intValue() == 21001) {
                            ToastCenterUtils.showToast(R.string.playfun_dialog_exchange_integral_total_text1);
                            gameUI.payDialogViewEvent.postValue(null);
                        }
                    }
                });
    }
    //IM????????????
    private class IMAdvancedMsgListener extends V2TIMAdvancedMsgListener {
        @Override
        public void onRecvNewMessage(V2TIMMessage msg) {
            TUIMessageBean info = ChatMessageBuilder.buildMessage(msg);
            if (info != null) {
                if (info.getMsgType() == 2) { //?????????????????????
                    V2TIMCustomElem v2TIMCustomElem = info.getV2TIMMessage().getCustomElem();
                    Map<String, Object> contentBody = CustomConvertUtils.CustomMassageConvertMap(v2TIMCustomElem);
                    if (ObjectUtils.isNotEmpty(contentBody)) {
                        //????????????--??????
                        if (contentBody.containsKey(CustomConstants.Message.MODULE_NAME_KEY)) {
                            //??????moudle-pushCoinGame ?????????
                            if (CustomConvertUtils.ContainsMessageModuleKey(contentBody, CustomConstants.Message.MODULE_NAME_KEY, CustomConstants.CoinPusher.MODULE_NAME)) {
                                V2TIMCustomManagerUtil.CoinPusherManager(contentBody);
                            }
                        }
                    }
                }else if(info.getMsgType() == 1){
                    if (otherCallInfoEntity.get()!=null && info.getV2TIMMessage().getSender().equals(otherCallInfoEntity.get().getImId())) {
                        String text = String.valueOf(info.getExtra());
                        if (StringUtil.isJSON2(text) && text.contains("type")) {//????????????????????????
                            Map<String, Object> map_data = new Gson().fromJson(text, Map.class);
                            //????????????
                            if (map_data != null && map_data.get("type") != null && Objects.equals(map_data.get("type"), "message_gift")
                                    && map_data.get("is_accost") == null) {
                                GiftEntity giftEntity = IMGsonUtils.fromJson(String.valueOf(map_data.get("data")), GiftEntity.class);
                                gameUI.acceptUserGift.postValue(giftEntity);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void registerRxBus() {
        initIMListener();
        coinPusherGamePlayingSubscription = RxBus.getDefault().toObservable(CoinPusherGamePlayingEvent.class)
                .subscribe(coinPusherGamePlayingEvent -> {
                    if(coinPusherGamePlayingEvent!=null){
                        switch (coinPusherGamePlayingEvent.getState()) {
                            case CustomConstants.CoinPusher.START_WINNING:
                                //????????????
                                gamePlayingState = CustomConstants.CoinPusher.START_WINNING;
                                gameUI.cancelDownTimeEvent.postValue(null);
                                gameUI.playingBtnEnable.postValue(false);
                                break;
                            case CustomConstants.CoinPusher.END_WINNING:
                                //????????????
                                gamePlayingState = null;
                                gameUI.resetDownTimeEvent.postValue(null);
                                gameUI.playingBtnEnable.postValue(true);
                                break;
                            case CustomConstants.CoinPusher.DROP_COINS:
                                //????????????
                                //gamePlayingState = null;
                                gameUI.toastCenter.postValue(coinPusherGamePlayingEvent);
                                break;
                            case CustomConstants.CoinPusher.LITTLE_GAME_WINNING:
                                //?????? ????????????????????????????????????
                                gamePlayingState = CustomConstants.CoinPusher.LITTLE_GAME_WINNING;
                                gameUI.cancelDownTimeEvent.postValue(null);
                                break;
                        }
                    }
                });
        //???????????????????????????
        RxSubscriptions.add(coinPusherGamePlayingSubscription);
    }

    @Override
    public void removeRxBus() {
        removeIMListener();
        RxSubscriptions.remove(coinPusherGamePlayingSubscription);
    }
}
