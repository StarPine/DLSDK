package com.dl.playfun.ui.main;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.BubbleEntity;
import com.dl.playfun.entity.CallingInviteInfo;
import com.dl.playfun.entity.DayRewardInfoEntity;
import com.dl.playfun.entity.MqBroadcastGiftEntity;
import com.dl.playfun.entity.MqGiftDataEntity;
import com.dl.playfun.entity.RestartActivityEntity;
import com.dl.playfun.entity.VersionEntity;
import com.dl.playfun.event.BubbleTopShowEvent;
import com.dl.playfun.event.CoinPusherRoomEvent;
import com.dl.playfun.event.DailyAccostEvent;
import com.dl.playfun.event.MainTabEvent;
import com.dl.playfun.event.MessageCountChangeEvent;
import com.dl.playfun.event.MessageGiftNewEvent;
import com.dl.playfun.event.RewardRedDotEvent;
import com.dl.playfun.event.TaskMainTabEvent;
import com.dl.playfun.kl.Utils;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.manager.LocationManager;
import com.dl.playfun.manager.V2TIMCustomManagerUtil;
import com.dl.playfun.utils.FastCallFunUtil;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.google.gson.Gson;
import com.tencent.custom.GiftEntity;
import com.tencent.custom.IMGsonUtils;
import com.tencent.imsdk.v2.V2TIMAdvancedMsgListener;
import com.tencent.imsdk.v2.V2TIMCustomElem;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.qcloud.tuicore.Status;
import com.tencent.qcloud.tuicore.custom.CustomConstants;
import com.tencent.qcloud.tuicore.custom.entity.VideoEvaluationEntity;
import com.tencent.qcloud.tuicore.custom.entity.VideoPushEntity;
import com.tencent.qcloud.tuikit.tuichat.bean.message.TUIMessageBean;
import com.tencent.qcloud.tuikit.tuichat.util.ChatMessageBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.disposables.Disposable;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.RxSubscriptions;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author wulei
 */
public class MainViewModel extends BaseViewModel<AppRepository> {

    public ObservableField<String> lockPassword = new ObservableField<>("");
    public ObservableField<Boolean> isHaveRewards = new ObservableField<>(false);
    public List<MqBroadcastGiftEntity> publicScreenBannerGiftEntity = new ArrayList<>();
    public boolean playing = false;
    public boolean isShowedReward = true;//???????????????????????????
    public int giveCoin = 0;
    public int videoCard = 0;
    public int chatCardNum = 0;
    public int assostCardNum = 0;
    public int nextGiveCoin = 0;
    public int nextVideoCard = 0;
    public String dayRewardKey = "";

    UIChangeObservable uc = new UIChangeObservable();
    private Disposable mSubscription, taskMainTabEventReceive, mainTabEventReceive, rewardRedDotEventReceive, BubbleTopShowEventSubscription, ResatrtActSubscription2, videoEvaluationSubscription;

    private Disposable mCoinPusherRoomEvent;

    private IMAdvancedMsgListener imAdvancedMsgListener;

    public MainViewModel(@NonNull Application application, AppRepository appRepository) {
        super(application, appRepository);
        if (appRepository.readUserData() != null && !ObjectUtils.isEmpty(appRepository.readUserData().getSex())) {
            uc.gender.set(appRepository.readUserData().getSex() != 1);
        }
    }

    @Override
    public void onEnterAnimationEnd() {
        super.onEnterAnimationEnd();
        initIMListener();
        LocationManager.getInstance().initloadLocation(new LocationManager.LocationListener() {
            @Override
            public void onLocationSuccess(double lat, double lng) {
                loadSendLocation(lat, lng, null, null);//????????????????????????
            }

            @Override
            public void onLocationFailed() {
                Log.e("????????????????????????????????????", "");
            }
        });
        sendInviteCode();
        //?????????????????????
        getSensitiveWords();

        //????????????
        dayRewardKey = StringUtil.getDailyFlag("dailyReward");
        String value = model.readKeyValue(dayRewardKey);
        if (value == null){
            isShowedReward = false;
        }else {
            isShowedReward = true;
        }

    }

    @Override
    public void registerRxBus() {
        super.registerRxBus();
        mSubscription = RxBus.getDefault().toObservable(MessageCountChangeEvent.class)
                .subscribe(messageCountChangeEvent -> uc.allMessageCountChange.postValue(messageCountChangeEvent.getCount()));
        rewardRedDotEventReceive = RxBus.getDefault().toObservable(RewardRedDotEvent.class).subscribe(event -> {
            isHaveRewards.set(event.isHaveReward());
        });

        BubbleTopShowEventSubscription = RxBus.getDefault().toObservable(BubbleTopShowEvent.class).subscribe(event -> {
            uc.bubbleTopShow.postValue(false);
        });
        ResatrtActSubscription2 = RxBus.getDefault().toObservable(RestartActivityEntity.class).subscribe(event -> {
            uc.restartActivity.postValue(event.getIntent());
        });
        taskMainTabEventReceive = RxBus.getDefault().toObservable(TaskMainTabEvent.class)
                .compose(RxUtils.exceptionTransformer())
                .compose(RxUtils.schedulersTransformer())
                .subscribe(o -> uc.taskCenterclickTab.postValue(((TaskMainTabEvent) o)));
        //?????????????????????
        mCoinPusherRoomEvent = RxBus.getDefault().toObservable(CoinPusherRoomEvent.class).subscribe(event -> {
            uc.coinPusherRoomEvent.postValue(null);
        });
        //???????????????????????????
        RxSubscriptions.add(taskMainTabEventReceive);
        RxSubscriptions.add(mainTabEventReceive);
        RxSubscriptions.add(rewardRedDotEventReceive);
        RxSubscriptions.add(BubbleTopShowEventSubscription);
        RxSubscriptions.add(ResatrtActSubscription2);
        RxSubscriptions.add(videoEvaluationSubscription);
        RxSubscriptions.add(mCoinPusherRoomEvent);
    }

    @Override
    public void removeRxBus() {
        super.removeRxBus();
        RxSubscriptions.remove(mSubscription);
        RxSubscriptions.remove(taskMainTabEventReceive);
        RxSubscriptions.remove(mainTabEventReceive);
        RxSubscriptions.remove(rewardRedDotEventReceive);
        RxSubscriptions.remove(BubbleTopShowEventSubscription);
        RxSubscriptions.remove(ResatrtActSubscription2);
        RxSubscriptions.remove(videoEvaluationSubscription);
        RxSubscriptions.remove(mCoinPusherRoomEvent);
        removeIMListener();
    }

    //??????????????????
    public void versionOnClickCommand() {
        model.detectionVersion("Android").compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<VersionEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<VersionEntity> versionEntityBaseDataResponse) {
                        dismissHUD();
                        VersionEntity versionEntity = versionEntityBaseDataResponse.getData();
                        if (versionEntity != null) {
                            uc.versionEntitySingl.postValue(versionEntity);
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public void loadSendLocation(Double lat, Double lng, String county_name, String province_name) {
        if (lat == null || lng == null) {
            return;
        }
        model.coordinate(lat, lng, county_name, province_name)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {

                    }
                });
    }

    public void sendInviteCode() {
        Map<String, String> map = model.readOneLinkCode();
        if (ObjectUtils.isEmpty(map)) {
            return;
        }
        String code = map.get("code");
        String channel = map.get("channel");
        model.userInvite(code, 1, channel)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {
                        model.clearOneLinkCode();//???????????????????????????
                    }

                    @Override
                    public void onError(RequestException e) {
                        if (e.getCode() != null && e.getCode().intValue() == 10109) {
                            model.clearOneLinkCode();//???????????????????????????
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //????????????????????????
    public void pushGreet(Integer type) {
        model.pushGreet(type)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {

                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //????????????
    public void putAccostList(List<Integer> userIds) {
        model.putAccostList(userIds)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {

                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //????????????????????????
    public void getBubbleSetting() {
        //???????????? ????????????????????????
        if(FastCallFunUtil.getInstance().isFastCallFun("getBubbleEntity",1500)){
            return;
        }
        model.getBubbleEntity()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<BubbleEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<BubbleEntity> bubbleEntityBaseDataResponse) {
                        BubbleEntity bubble = bubbleEntityBaseDataResponse.getData();
                        if (bubble != null) {
                            if (bubble.getStatus() == 1) {
                                uc.bubbleTopShow.postValue(true);
                            }
                        }
                    }
                });
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

    public class IMAdvancedMsgListener extends V2TIMAdvancedMsgListener{
        @Override
        public void onRecvNewMessage(V2TIMMessage msg) {
            TUIMessageBean info = ChatMessageBuilder.buildMessage(msg);
            if (info != null) {
                switch (info.getMsgType()){
                    //??????????????????
                    case 1:
                        String text = String.valueOf(info.getExtra());
                        if (StringUtil.isJSON2(text)) {//????????????????????????
                            //?????????????????????
                            if (text.contains("type")){
                                Map<String, Object> map_data = new Gson().fromJson(text, Map.class);
                                if (map_data != null && map_data.get("type") != null) {
                                    String type = Objects.requireNonNull(map_data.get("type")).toString();
                                    String data = (String) map_data.get("data");
                                    if (StringUtil.isJSON2(data)) {
                                        switch (type) {
                                            case "message_pushPay"://?????????????????????
                                                if (AppContext.isShowNotPaid){
                                                    if(!FastCallFunUtil.getInstance().isFastCallFun("message_pushPay",5000)){
                                                        Map<String, Object> dataMapPushPay = new Gson().fromJson(data, Map.class);
                                                        String dataType = Objects.requireNonNull(dataMapPushPay.get("type")).toString();
                                                        if (dataType.equals("1") || dataType.equals("1.0")) {
                                                            uc.notPaidDialog.setValue("1");
                                                        } else {
                                                            uc.notPaidDialog.setValue("2");
                                                        }
                                                    }
                                                }
                                                break;
                                            case "message_gift"://????????????
                                                if (map_data.get("is_accost") == null) {//??????????????????
                                                    if (!AppContext.isCalling){
                                                        GiftEntity giftEntity = IMGsonUtils.fromJson(data, GiftEntity.class);
                                                        //??????????????????????????????????????????
                                                        if (!StringUtils.isEmpty(giftEntity.getSvgaPath())) {
                                                            RxBus.getDefault().post(new MessageGiftNewEvent(giftEntity,msg.getMsgID(),info.getV2TIMMessage().getSender()));
                                                        }
                                                    }
                                                }
                                                break;
                                        }
                                    }
                                }
                            }
                            //??????????????????
                            if (text.contains("giftBroadcast") && text.contains("messageType")) {
                                if (AppContext.isHomePage){
                                    setPublicScreenGiftData(text);
                                }
                            }
                        }
                        break;
                    case 2: //?????????????????????
                        V2TIMCustomElem v2TIMCustomElem = info.getV2TIMMessage().getCustomElem();
                        byte[] data = v2TIMCustomElem.getData();
                        if (data != null && ConfigManager.getInstance().getTipMoneyShowFlag()){
                            String customData = new String(data);
                            if (customData.contains(CustomConstants.PushMessage.VIDEO_CALL_PUSH)){
                                VideoPushEntity videoPushEntity = V2TIMCustomManagerUtil.videoPushManager(customData);
                                if (videoPushEntity != null && AppConfig.isMainPage){
                                    uc.videoPush.postValue(videoPushEntity);
                                }
                            }

                            if (customData.contains(CustomConstants.PushMessage.VIDEO_CALL_FEEDBACK)){
                                VideoEvaluationEntity evaluationEntity = V2TIMCustomManagerUtil.videoEvaluationManager(customData);
                                if (evaluationEntity != null){
                                    uc.videoEvaluation.postValue(evaluationEntity);
                                }
                            }

                        }
                        break;
                }
            }
        }
    };

    /**
     * ????????????????????????
     * @param text
     */
    private void setPublicScreenGiftData(String text) {
        try {
            MqGiftDataEntity giftDataEntity = new Gson().fromJson(text, MqGiftDataEntity.class);
            MqBroadcastGiftEntity content = giftDataEntity.getContent();
            if (content != null){
                publicScreenBannerGiftEntity.add(content);
                playBannerGift();
            }
        }catch (Exception e){
            Log.i("imsdk", "setPublicScreenGiftData: ????????????");
        }
    }

    public void playBannerGift(){
        if (publicScreenBannerGiftEntity.size() > 0 && !playing){
            uc.giftBanner.setValue(publicScreenBannerGiftEntity.get(0));
        }
    }

    public void getSensitiveWords() {
        model.getSensitiveWords()
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse>() {

                    @Override
                    public void onSuccess(BaseDataResponse baseDataResponse) {
                        String data = (String) baseDataResponse.getData();
                        if (data == null)return;
                        String[] split = data.split(",");
                        List<String> config = Arrays.asList(split);
                        model.saveSensitiveWords(config);
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    //??????????????????
    public void videoFeedback(long videoCallPushLogId, int feedback) {
        model.videoFeedback(videoCallPushLogId,feedback)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse>() {

                    @Override
                    public void onSuccess(BaseDataResponse baseDataResponse) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    //?????????????????????
    public void getCallingInvitedInfo(String myImUserId, String otherImUserId,int videoCallPushLogId) {
        if (Status.mIsShowFloatWindow){
            ToastUtils.showShort(R.string.audio_in_call);
            return;
        }
        model.callingInviteInfo(2, myImUserId, otherImUserId, 2,videoCallPushLogId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<CallingInviteInfo>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CallingInviteInfo> callingInviteInfoBaseDataResponse) {
                        if (callingInviteInfoBaseDataResponse.getCode() == 2) {//?????????
                            ToastUtils.showShort(R.string.custom_message_other_busy);
                            return;
                        }
                        CallingInviteInfo callingInviteInfo = callingInviteInfoBaseDataResponse.getData();
                        if (callingInviteInfo != null) {
                            Utils.tryStartCallSomeone(2, otherImUserId, callingInviteInfo.getRoomId());
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

    /**
     * ????????????
     */
    public void getDayReward() {
        model.getDayReward()
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<DayRewardInfoEntity>>() {

                    @Override
                    public void onSuccess(BaseDataResponse<DayRewardInfoEntity> baseDataResponse) {
                        model.putKeyValue(dayRewardKey,"true");
                        DayRewardInfoEntity dayRewardInfoEntity = baseDataResponse.getData();
                        if (dayRewardInfoEntity == null){
                            RxBus.getDefault().post(new DailyAccostEvent());
                            return;
                        }
                        nextGiveCoin = dayRewardInfoEntity.getNext();
                        nextVideoCard = dayRewardInfoEntity.getNextCard();
                        List<DayRewardInfoEntity.NowBean> now = dayRewardInfoEntity.getNow();
                        if (now == null) {
                            return;
                        }
                        for (DayRewardInfoEntity.NowBean nowBean : now) {
                            String type = nowBean.getType();
                            if (type.equals("video_card")){
                                videoCard = nowBean.getNum();
                            }else if (type.equals("coin")){
                                giveCoin = nowBean.getNum();
                            }
                        }
                        uc.showDayRewardDialog.call();
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    /**
     * ????????????
     */
    public void getRegisterReward() {
        model.getRegisterReward()
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<DayRewardInfoEntity>>() {

                    @Override
                    public void onSuccess(BaseDataResponse<DayRewardInfoEntity> baseDataResponse) {
                        DayRewardInfoEntity dayRewardInfoEntity = baseDataResponse.getData();
                        if (dayRewardInfoEntity == null){
                            RxBus.getDefault().post(new DailyAccostEvent());
                            return;
                        }
                        List<DayRewardInfoEntity.NowBean> now = dayRewardInfoEntity.getNow();
                        if (now == null) {
                            return;
                        }
                        for (DayRewardInfoEntity.NowBean nowBean : now) {
                            String type = nowBean.getType();
                            if (type.equals("accost_card")){
                                assostCardNum = nowBean.getNum();
                            }else if (type.equals("chat_card")){
                                chatCardNum = nowBean.getNum();
                            }
                        }
                        uc.showRegisterRewardDialog.call();
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    public class UIChangeObservable {
        //????????????
        public SingleLiveEvent<Boolean> bubbleTopShow = new SingleLiveEvent<>();
        public SingleLiveEvent<Intent> restartActivity = new SingleLiveEvent<>();
        //        public SingleLiveEvent<Void> showAgreementDialog = new SingleLiveEvent<>();
        //??????????????????
        public SingleLiveEvent<Void> showDayRewardDialog = new SingleLiveEvent<>();
        //????????????
        public SingleLiveEvent<Void> showRegisterRewardDialog = new SingleLiveEvent<>();
        public SingleLiveEvent<Integer> allMessageCountChange = new SingleLiveEvent<>();
        public ObservableField<Boolean> gender = new ObservableField<>(false);
        public SingleLiveEvent<MainTabEvent> mainTab = new SingleLiveEvent<>();
        //????????????
        public SingleLiveEvent<VersionEntity> versionEntitySingl = new SingleLiveEvent<>();
        //???????????????
        public SingleLiveEvent<String> notPaidDialog = new SingleLiveEvent<>();
        public SingleLiveEvent<MqBroadcastGiftEntity> giftBanner = new SingleLiveEvent<>();
        //??????????????????
        public SingleLiveEvent<TaskMainTabEvent> taskCenterclickTab = new SingleLiveEvent<>();
        public SingleLiveEvent<VideoEvaluationEntity> videoEvaluation = new SingleLiveEvent<>();
        public SingleLiveEvent<VideoPushEntity> videoPush = new SingleLiveEvent<>();
        //???????????????
        public SingleLiveEvent<Void> coinPusherRoomEvent = new SingleLiveEvent<>();

    }

}