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
import com.dl.playfun.entity.CallGameCoinPusherEntity;
import com.dl.playfun.entity.CallUserInfoEntity;
import com.dl.playfun.entity.CallUserRoomInfoEntity;
import com.dl.playfun.entity.CallingInfoEntity;
import com.dl.playfun.entity.CallingStatusEntity;
import com.dl.playfun.entity.CoinPusherBalanceDataEntity;
import com.dl.playfun.entity.GiftBagEntity;
import com.dl.playfun.entity.RtcRoomMessageEntity;
import com.dl.playfun.event.CallingStatusEvent;
import com.dl.playfun.event.CoinPusherGamePlayingEvent;
import com.dl.playfun.event.RtcRoomMessageEvent;
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
 * Author: 彭石林
 * Time: 2022/8/26 11:08
 * Description: This is CoinPusherGameViewModel
 */
public class CoinPusherGameViewModel extends BaseViewModel <AppRepository> {
    //加载状态
    public final String loadingPlayer = "loadingPlayer";
    //游戏状态
    public String gamePlayingState;

    //livedata页面交互
    public UIChangeObservable gameUI = new UIChangeObservable();
    public ObservableInt totalMoney = new ObservableInt(0);

    //消费者
    private Disposable coinPusherGamePlayingSubscription;
    private Disposable mRtcRoomMessageSubscription;
    private Disposable mCallingStatusEventSubscription;

    private IMAdvancedMsgListener imAdvancedMsgListener;

    //围观模式-不能投币。切不能操作雨刷
    public ObservableBoolean circuseeStatus = new ObservableBoolean(false);
    //默认叠起
    public boolean triangleSwitch = true;

    public GameCallEntity gameCallEntity;
    //视频通话窗口默认小布局
    public ObservableBoolean callZoomOuViewFlag = new ObservableBoolean(false);

    public int mTimeCount = 0;
    //拨打中
    public ObservableBoolean callingOnTheLine = new ObservableBoolean(false);
    //是否挂断
    public ObservableBoolean callingDropped = new ObservableBoolean(true);
    //当前通话对方用户信息
    public ObservableField<CallUserInfoEntity> otherCallInfoEntity = new ObservableField<>();
    //当前通话房间信息
    public ObservableField<CallUserRoomInfoEntity> callUserRoomInfoEntity = new ObservableField<>();
    //通话中价格提示
    public ObservableField<String> maleCallingHint = new ObservableField<>("");
    //通话时长提示
    public ObservableField<String> callDurationTime = new ObservableField<>();
    //当前用户是否为收款人
    public boolean isCallingPay = false;
    //余额不足临界提示分钟数
    public int balanceNotEnoughTipsMinutes;
    //付费放钻石总余额
    public int payUserBalanceMoney = 0;

    //聊天收益
    public double payeeProfits;

    //游戏房间ID
    public Integer _gameRoomId = 0;
    //通话中状态
    public int callingState = 0;

    //推币机禁音
    public ObservableBoolean muteEnabled = new ObservableBoolean(false);
    //是否静音
    public ObservableBoolean micMuteField = new ObservableBoolean(false);
    //是否免提
    public ObservableBoolean handsFreeField = new ObservableBoolean(false);
    //回拨图标 语言or视讯拨打
    public ObservableBoolean makeCallTypeField = new ObservableBoolean(true);


    public CoinPusherGameViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
    }
    //关闭页面点击
    public BindingCommand<Void> gameCloseView = new BindingCommand<>(()->gameUI.backViewApply.call());
    //投币按钮点击
    public BindingCommand<Void> playCoinClick = new BindingCommand<>(() -> {
        playingCoinPusherThrowCoin(_gameRoomId);
    });
    //雨刷控制开关
    public BindingCommand<Void> playPusherActClick = new BindingCommand<>(() -> {
        playingCoinPusherAct(_gameRoomId);
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
        DLRTCAudioManager.Companion.getInstance().muteLocalAudio(minMute);
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

    //推币机音频开关
    public BindingCommand<Void> muteEnabledClick = new BindingCommand<>(() -> {
        boolean enabled = !muteEnabled.get();
        muteEnabled.set(enabled);
        gameUI.muteEnabledEvent.postValue(enabled);
    });
    //用户头像点击折叠
    public BindingCommand<Void> triangleClick = new BindingCommand<>(() -> {
        triangleSwitch = !triangleSwitch;
        gameUI.triangleEvent.postValue(triangleSwitch);
    });
    //点击挂断电话
    public BindingCommand<Void> callRejectClick = new BindingCommand<>(() -> {
        Log.e("CoinPusherGameActivity","点击挂断电话======================");
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
    //点击接听电话
    public BindingCommand<Void> callAcceptClick = new BindingCommand<>(() -> {
        gameUI.callCheckPermissionEvent.call();
    });

    //发送礼物
    public BindingCommand<Void> giftBagOnClickCommand = new BindingCommand<>(new BindingAction() {
        @Override
        public void call() {
            AppContext.instance().logEvent(AppsFlyerEvent.voicecall_gift);
            gameUI.sendGiftBagEvent.call();
        }
    });

    //回拨-拨打电话
    public BindingCommand<Void> makeCallUserClick = new BindingCommand<>(() -> {
        if(gameCallEntity!=null){
            int callingType;
            //拨打语音
            if(gameCallEntity.getCallingType() == DLRTCDataMessageType.DLInviteRTCType.dl_rtc_audio){
                callingType = 1;
            }else{
                //拨打视频
                callingType = 2;
            }
            String fromUserId = TUILogin.getUserId();
            String answerUserId;
            MPTimber.tag("CoinPusherGameActivity").d("fromUserId： "+fromUserId+" ,上个拨打人"+gameCallEntity.getInviteUserId() +" ==="+fromUserId.equals(gameCallEntity.getInviteUserId()));
            if(!fromUserId.equals(gameCallEntity.getInviteUserId())){
                answerUserId = gameCallEntity.getInviteUserId();
            }else{
                answerUserId = gameCallEntity.getAcceptUserId();
            }
            gameCallEntity.setCallingRole(DLRTCCalling.Role.CALL);
            callingInviteUser(callingType, fromUserId, answerUserId);
        }
    });

    //点击放大缩小视频界面
    public BindingCommand<Void> callZoomOuViewCLick = new BindingCommand<>(() -> {
        callZoomOuViewFlag.set(!callZoomOuViewFlag.get());
        gameUI.callZoomOuViewEvent.postValue(callZoomOuViewFlag.get());
    });

    //是否是小游戏状态
    private boolean isLittleGameWinning(){
        if(StringUtils.isEmpty(gamePlayingState)){
            return false;
        }
        return gamePlayingState.equals(CustomConstants.CoinPusher.LITTLE_GAME_WINNING);
    }
    //投币
    public void playingCoinPusherThrowCoin(Integer roomId){
        //当前处于中奖状态、并且是小游戏中奖状态 什么都不处理
        if(isLittleGameWinning()){
            gamePlayingState = CustomConstants.CoinPusher.LITTLE_GAME_WINNING;
        }else{
            gamePlayingState = loadingPlayer;
        }
        Log.e("推币机进行投币","房间ID:"+String.valueOf(roomId));
        model.playingCoinPusherThrowCoin(roomId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> {
                    loadingShow();
                    //禁用投币按钮
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
                        //当前处于中奖状态、并且是小游戏中奖
                        if(isLittleGameWinning()){
                            //取消倒计时
                            gameUI.cancelDownTimeEvent.postValue(null);
                        }else{
                            //否则重新开始倒计时。且清除游戏状态
                            gameUI.resetDownTimeEvent.postValue(null);
                            gamePlayingState = null;
                        }

                    }

                    @Override
                    public void onError(RequestException e) {
                        //余额不足
                        if(e.getCode() == 21001){
                            gameUI.payDialogViewEvent.call();
                            gameUI.playingBtnEnable.postValue(true);
                            if(!isLittleGameWinning()){
                                //清除当前投币状态
                                gamePlayingState = null;
                            }
                        }else if(e.getCode() == 72000){
                            //中奖--置灰并停止倒计时
                            gameUI.playingBtnEnable.postValue(false);
                            //开始落币
                            gamePlayingState = CustomConstants.CoinPusher.START_WINNING;
                            gameUI.cancelDownTimeEvent.postValue(null);
                        }else{
                            gameUI.playingBtnEnable.postValue(true);
                            //清除当前投币状态
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
    //操作雨刷
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
    //查询用户资料
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

    //拨打电话给指定用户
    public void callingInviteUser(int callingType, String inviterImId,String receiverImId){
//        callingType	是	Integer	通话类型：1=语音，2=视频
//                * inviterImId	是	String	拔打人IM ID
//     * receiverImId	是	String	接收人IM ID
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
                        if(callingInviteInfo != null){
                            callUserRoomInfoEntity.set(callingInviteInfo);
                            //主动拨打
                            callingOnTheLine.set(true);
                            callingState = 1;
                            Utils.inviteUserRTC(receiverImId, callingType==1 ? DLRTCDataMessageType.DLInviteRTCType.dl_rtc_audio : DLRTCDataMessageType.DLInviteRTCType.dl_rtc_video,
                                    callingInviteInfo.getRoomId(), callingInviteInfo.getInviteTimeout(),false, GsonUtils.toJson(callingInviteInfo));
                            DLRTCAudioManager.Companion.getInstance().enableAGC(true);
                            DLRTCAudioManager.Companion.getInstance().enableAEC(true);
                            DLRTCAudioManager.Companion.getInstance().enableANS(true);
                        }
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
    //来电提示文案
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


    //刷新当前用户剩余金币
    public String tvTotalMoneyRefresh(int moneyNum){
        return moneyNum > 99999 ? "99999+" : moneyNum+"";
    }

    public static class UIChangeObservable{
        //取消倒计时
        public SingleLiveEvent<Void> cancelDownTimeEvent = new SingleLiveEvent<>();
        //重置倒计时
        public SingleLiveEvent<Void> resetDownTimeEvent = new SingleLiveEvent<>();
        //开始显示loading进度条
        public SingleLiveEvent<Void> loadingShow = new SingleLiveEvent<>();
        //关闭进度条Loading显示
        public SingleLiveEvent<Void> loadingHide = new SingleLiveEvent<>();
        //toast弹窗居中
        public SingleLiveEvent<CoinPusherGamePlayingEvent> toastCenter = new SingleLiveEvent<>();
        //禁止投币按钮操作
        public SingleLiveEvent<Boolean> playingBtnEnable = new SingleLiveEvent<>();
        //返回上一页
        public SingleLiveEvent<Void> backViewApply = new SingleLiveEvent<>();
        //余额不足。弹出充值弹窗
        public SingleLiveEvent<Void> payDialogViewEvent = new SingleLiveEvent<>();
        //开关推币机语音
        public SingleLiveEvent<Boolean> muteEnabledEvent = new SingleLiveEvent<>();
        //右上角折叠状态 : 展开、折叠
        public SingleLiveEvent<Boolean> triangleEvent = new SingleLiveEvent<>();
        //调整视频通话窗口大小
        public SingleLiveEvent<Boolean> callZoomOuViewEvent = new SingleLiveEvent<>();
        //效验语音视频通话权限弹窗
        public SingleLiveEvent<Void> callCheckPermissionEvent = new SingleLiveEvent<>();
        //进房成功
        public SingleLiveEvent<Void> acceptCallingEvent = new SingleLiveEvent<>();
        //挂断电话
        public SingleLiveEvent<Void> hangupCallingEvent = new SingleLiveEvent<>();
        //唤醒发送礼物弹窗
        public SingleLiveEvent<Void> sendGiftBagEvent = new SingleLiveEvent<>();
        //发送礼物效果
        public SingleLiveEvent<Map<String, Object>> sendUserGiftAnim = new SingleLiveEvent<>();
        //接收礼物效果
        public SingleLiveEvent<GiftEntity> acceptUserGift = new SingleLiveEvent<>();
        //通话中挂断电话。处理返回通话中逻辑
        public SingleLiveEvent<Void> callChatFinish = new SingleLiveEvent<>();
        //刷新收益
        public SingleLiveEvent<Void> refreshEarnings = new SingleLiveEvent<>();
    }
    //显示loading
    public void loadingShow(){
        gameUI.loadingShow.call();
    }
    //隐藏loading
    public void loadingHide(){
        gameUI.loadingHide.call();
    }

    //添加IM消息监听器
    public void initIMListener () {
        if(imAdvancedMsgListener==null){
            imAdvancedMsgListener = new IMAdvancedMsgListener();
            V2TIMManager.getMessageManager().addAdvancedMsgListener(imAdvancedMsgListener);
        }
    }

    //移除IM消息监听
    public void removeIMListener(){
        if(imAdvancedMsgListener!=null){
            V2TIMManager.getMessageManager().removeAdvancedMsgListener(imAdvancedMsgListener);
        }
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

                    @Override
                    public void onError(RequestException e) {
                    }
                });
    }
    //IM消息监听
    private class IMAdvancedMsgListener extends V2TIMAdvancedMsgListener {
        @Override
        public void onRecvNewMessage(V2TIMMessage msg) {
            TUIMessageBean info = ChatMessageBuilder.buildMessage(msg);
            if (info != null) {
                if (info.getMsgType() == 2) { //自定义消息类型
                    V2TIMCustomElem v2TIMCustomElem = info.getV2TIMMessage().getCustomElem();
                    Map<String, Object> contentBody = CustomConvertUtils.CustomMassageConvertMap(v2TIMCustomElem);
                    if (ObjectUtils.isNotEmpty(contentBody)) {
                        //模块类型--判断
                        if (contentBody.containsKey(CustomConstants.Message.MODULE_NAME_KEY)) {
                            //获取moudle-pushCoinGame 推币机
                            if (CustomConvertUtils.ContainsMessageModuleKey(contentBody, CustomConstants.Message.MODULE_NAME_KEY, CustomConstants.CoinPusher.MODULE_NAME)) {
                                V2TIMCustomManagerUtil.CoinPusherManager(contentBody);
                            }
                            //RTC通话中推送消息模块
                            if(CustomConvertUtils.ContainsMessageModuleKey(contentBody, CustomConstants.Message.MODULE_NAME_KEY,CustomConstants.RtcRoomMessage.MODULE_NAME)){
                                Map<String,Object> rtcRoomMsg = CustomConvertUtils.ConvertMassageModule(contentBody,CustomConstants.Message.MODULE_NAME_KEY,CustomConstants.RtcRoomMessage.MODULE_NAME,CustomConstants.Message.CUSTOM_CONTENT_BODY);
                                V2TIMCustomManagerUtil.RtcRoomMessageManager(rtcRoomMsg);
                            }
                        }
                    }
                }else if(info.getMsgType() == 1){
                    if (otherCallInfoEntity.get()!=null && info.getV2TIMMessage().getSender().equals(otherCallInfoEntity.get().getImId())) {
                        String text = String.valueOf(info.getExtra());
                        if (StringUtil.isJSON2(text) && text.contains("type")) {//做自定义通知判断
                            Map<String, Object> map_data = new Gson().fromJson(text, Map.class);
                            //礼物消息
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
                                //开始落币
                                gamePlayingState = CustomConstants.CoinPusher.START_WINNING;
                                gameUI.cancelDownTimeEvent.postValue(null);
                                gameUI.playingBtnEnable.postValue(false);
                                break;
                            case CustomConstants.CoinPusher.END_WINNING:
                                //落币结束
                                gamePlayingState = null;
                                gameUI.resetDownTimeEvent.postValue(null);
                                gameUI.playingBtnEnable.postValue(true);
                                break;
                            case CustomConstants.CoinPusher.DROP_COINS:
                                //落币奖励
                                //gamePlayingState = null;
                                gameUI.toastCenter.postValue(coinPusherGamePlayingEvent);
                                break;
                            case CustomConstants.CoinPusher.LITTLE_GAME_WINNING:
                                //中奖 小游戏（叠叠乐、小玛利）
                                gamePlayingState = CustomConstants.CoinPusher.LITTLE_GAME_WINNING;
                                gameUI.cancelDownTimeEvent.postValue(null);
                                break;
                        }
                    }
                });
        //通话活动入口
        mRtcRoomMessageSubscription = RxBus.getDefault().toObservable(RtcRoomMessageEvent.class)
                .subscribe(rtcRoomMessageEvent -> {
                    RtcRoomMessageEntity rtcRoomMessageEntity = rtcRoomMessageEvent.getRtcRoomMessageEntity();
                    if(rtcRoomMessageEntity!=null){
                        CallGameCoinPusherEntity callGameCoinPusherEntity = rtcRoomMessageEntity.getActivityData();
                        if(callGameCoinPusherEntity!=null){
                            CallGameCoinPusherEntity.ActivityData activityData = callGameCoinPusherEntity.getActData();
                            if(activityData != null && activityData.getState() != null){
                                String state = callGameCoinPusherEntity.getActData().getState();
                                //推币机主玩用户退出房间
                                if(Objects.equals(state,CallGameCoinPusherEntity.leaveGame)){
                                    gameUI.callChatFinish.call();
                                }
                            }
                        }
                    }

                });
        mCallingStatusEventSubscription = RxBus.getDefault().toObservable(CallingStatusEvent.class)
                .subscribe(callingStatusEvent -> {

                    if(callingStatusEvent!=null && callingStatusEvent.getCallingStatusEntity()!=null){
                        CallingStatusEntity callingStatusEntity = callingStatusEvent.getCallingStatusEntity();
                        Integer roomStatus = callingStatusEntity.getRoomStatus();
                        if (roomStatus != null && roomStatus != 101) {
                            callRejectClick.execute();
                        }
                        payeeProfits = callingStatusEntity.getPayeeProfits().doubleValue();
                        gameUI.refreshEarnings.call();
                    }
                });
        //将订阅者加入管理站
        RxSubscriptions.add(coinPusherGamePlayingSubscription);
        RxSubscriptions.add(mRtcRoomMessageSubscription);
        RxSubscriptions.add(mCallingStatusEventSubscription);
    }

    @Override
    public void removeRxBus() {
        removeIMListener();
        RxSubscriptions.remove(coinPusherGamePlayingSubscription);
        RxSubscriptions.remove(mRtcRoomMessageSubscription);
        RxSubscriptions.remove(mCallingStatusEventSubscription);
    }
}
