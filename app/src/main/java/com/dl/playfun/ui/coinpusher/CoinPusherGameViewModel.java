package com.dl.playfun.ui.coinpusher;

import android.app.Application;
import android.app.Dialog;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

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
import com.dl.playfun.entity.CallingInfoEntity;
import com.dl.playfun.entity.CallingInviteInfo;
import com.dl.playfun.entity.CallingStatusEntity;
import com.dl.playfun.entity.CoinPusherBalanceDataEntity;
import com.dl.playfun.entity.CoinPusherDataInfoEntity;
import com.dl.playfun.entity.GiftBagEntity;
import com.dl.playfun.entity.UserProfileInfo;
import com.dl.playfun.event.CoinPusherGamePlayingEvent;
import com.dl.playfun.kl.Utils;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.manager.V2TIMCustomManagerUtil;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.utils.LogUtils;
import com.dl.playfun.utils.ToastCenterUtils;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.dl.rtc.calling.base.DLRTCCalling;
import com.dl.rtc.calling.manager.DLRTCAudioManager;
import com.dl.rtc.calling.manager.DLRTCVideoManager;
import com.dl.rtc.calling.model.DLRTCDataMessageType;
import com.google.gson.Gson;
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
    public CoinPusherDataInfoEntity coinPusherDataInfoEntity;

    //消费者
    private Disposable coinPusherGamePlayingSubscription;

    private IMAdvancedMsgListener imAdvancedMsgListener;

    //默认叠起
    public boolean triangleSwitch = true;

    public GameCallEntity gameCallEntity;
    //视频通话窗口默认小布局
    public ObservableBoolean callZoomOuViewFlag = new ObservableBoolean(false);

    public int mTimeCount = 0;
    //拨打中
    public ObservableBoolean callingOnTheLine = new ObservableBoolean(false);
    //当前通话对方用户信息
    public ObservableField<UserProfileInfo> otherCallInfoEntity = new ObservableField<>();
    //通话中价格提示
    public ObservableField<String> maleCallingHint = new ObservableField<>("");
    //是否能接听
    public boolean callingAcceptFlag = false;
    //当前用户是否为收款人
    public boolean isCallingPay = false;
    //余额不足临界提示分钟数
    public int balanceNotEnoughTipsMinutes;
    //价格配置表
    public List<CallingInfoEntity.CallingUnitPriceInfo> unitPriceList;
    //付费放钻石总余额
    public int payUserBalanceMoney = 0;

    //推币机禁音
    public ObservableBoolean muteEnabled = new ObservableBoolean(false);
    //是否静音
    public ObservableBoolean micMuteField = new ObservableBoolean(false);
    //是否免提
    public ObservableBoolean handsFreeField = new ObservableBoolean(false);

    public CoinPusherGameViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
    }
    //关闭页面点击
    public BindingCommand<Void> gameCloseView = new BindingCommand<>(()->gameUI.backViewApply.call());
    //投币按钮迪纳基
    public BindingCommand<Void> playCoinClick = new BindingCommand<>(() -> {
        playingCoinPusherThrowCoin(coinPusherDataInfoEntity.getRoomInfo().getRoomId());
    });
    //雨刷控制开关
    public BindingCommand<Void> playPusherActClick = new BindingCommand<>(() -> {
        playingCoinPusherAct(coinPusherDataInfoEntity.getRoomInfo().getRoomId());
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
                DLRTCAudioManager.Companion.getInstance().hangup();
            }else{
                DLRTCVideoManager.Companion.getInstance().hangup();
            }
            gameUI.hangupCallingEvent.call();
        }
    });
    //点击接听电话
    public BindingCommand<Void> callAcceptClick = new BindingCommand<>(() -> {
        Log.e("CoinPusherGameActivity","点击接听电话======================");
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
            String answerUserId = null;
            MPTimber.tag("CoinPusherGameActivity").d("fromUserId： "+fromUserId+" ,上个拨打人"+gameCallEntity.getFromUserId() +" ==="+fromUserId.equals(gameCallEntity.getFromUserId()));
            if(!fromUserId.equals(gameCallEntity.getFromUserId())){
                answerUserId = gameCallEntity.getFromUserId();
            }else{
                answerUserId = gameCallEntity.getToUserId();
            }
            getCallingInvitedInfo(callingType,fromUserId,answerUserId,false);
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

    /**
    * @Desc TODO(拨打语音、视频---接听人)
    * @author 彭石林
    * @parame [callingType 拨打类型, fromUserId 拨打人, answerUserId 接听人, passiveCall 是否是被动接听]
    * @return void
    * @Date 2022/11/16
    */
    public void getCallingInvitedInfo(int callingType, String fromUserId,String answerUserId,boolean passiveCall) {
        model.callingInviteInfo(callingType, fromUserId, answerUserId, 0)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<CallingInviteInfo>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CallingInviteInfo> callingInviteInfoBaseDataResponse) {
                        CallingInviteInfo callingInviteInfo = callingInviteInfoBaseDataResponse.getData();
                        otherCallInfoEntity.set(callingInviteInfo.getUserProfileInfo());
                        gameCallEntity.setRoomId(callingInviteInfo.getRoomId());
                        if (model.readUserData().getSex() == 0 && ConfigManager.getInstance().getTipMoneyShowFlag()) {
                            if (!ObjectUtils.isEmpty(callingInviteInfo.getMessages()) && callingInviteInfo.getMessages().size() > 0) {
                                StringBuilder valueData = new StringBuilder();
                                for (String value : callingInviteInfo.getMessages()) {
                                    valueData.append(value).append("\n");
                                }
                                maleCallingHint.set(valueData.toString());
                            }
                        }
                        if (callingInviteInfo.getMinutesRemaining() != null && callingInviteInfo.getMinutesRemaining().intValue() <= 0) {
                            if(gameCallEntity != null){
                                if(gameCallEntity.getCallingType() == DLRTCDataMessageType.DLInviteRTCType.dl_rtc_audio){
                                    DLRTCAudioManager.Companion.getInstance().hangup();
                                }else{
                                    DLRTCVideoManager.Companion.getInstance().hangup();
                                }
                            }
                            return;
                        }
                        if (callingInviteInfo.getMinutesRemaining() != null && callingInviteInfo.getMinutesRemaining().intValue() > 0) {
                            callingAcceptFlag = true;
                        }
                        //主动拨打
                        if(!passiveCall){
                            callingOnTheLine.set(true);
                            Utils.StartGameCallSomeone(callingType, answerUserId, callingInviteInfo.getRoomId(), new Gson().toJson(callingInviteInfo));
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }
    /**
    * @Desc TODO(通话中)
    * @author 彭石林
    * @parame [roomId 房间号,callingType 通话类型：1=语音，2=视频  fromUserId 拔打人用户ID, toUserId 接收人用户ID]
    * @Date 2022/11/11
    */
    public void getCallingInfo(int callingType) {
        model.getCallingInfo(gameCallEntity.getRoomId(), callingType, gameCallEntity.getFromUserId(), gameCallEntity.getToUserId())
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<CallingInfoEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CallingInfoEntity> response) {
                        CallingInfoEntity callingInviteInfo = response.getData();
                        if (callingInviteInfo.getPaymentRelation().getPayeeImId().equals(ConfigManager.getInstance().getUserImID())){
                            isCallingPay = true;
                        }
                        //余额不足提示分钟数
                        balanceNotEnoughTipsMinutes = callingInviteInfo.getBalanceNotEnoughTipsMinutes();
                        //价格配置表
                        unitPriceList = callingInviteInfo.getUnitPriceList();
                        //通话类型：1=语音，2=视频
                        if(callingType == 1){
                            //DLRTCAudioManager.Companion.getInstance().accept();
                            //DLRTCAudioManager.Companion.getInstance().enterRoom(gameCallEntity.getRoomId());
                        }else{
                            //DLRTCVideoManager.Companion.getInstance().accept();
                            //DLRTCVideoManager.Companion.getInstance().enterRoom(gameCallEntity.getRoomId());
                        }
                        DLRTCAudioManager.Companion.getInstance().enableAGC(true);
                        DLRTCAudioManager.Companion.getInstance().enableAEC(true);
                        DLRTCAudioManager.Companion.getInstance().enableANS(true);
                        gameUI.acceptCallingEvent.call();
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public int isRealManVisible(UserProfileInfo itemEntity) {
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

    public int isVipVisible(UserProfileInfo itemEntity) {
        if (itemEntity != null && itemEntity.getSex() == 1 && itemEntity.getIsVip() == 1) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    public int isGoddessVisible(UserProfileInfo itemEntity) {
        if (itemEntity != null && itemEntity.getSex() == 0 && itemEntity.getIsVip() == 1) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }
    //来电提示文案
    public String getCallingLayoutTitles(UserProfileInfo itemEntity) {
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
    //获取房间状态
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
    //发送礼物
    public void sendUserGift(Dialog dialog, GiftBagEntity.giftEntity giftEntity, Integer to_user_id, Integer amount) {
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
    //IM消息监听
    private static class IMAdvancedMsgListener extends V2TIMAdvancedMsgListener {
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
        //将订阅者加入管理站
        RxSubscriptions.add(coinPusherGamePlayingSubscription);
    }

    @Override
    public void removeRxBus() {
        removeIMListener();
        RxSubscriptions.remove(coinPusherGamePlayingSubscription);
    }
}
