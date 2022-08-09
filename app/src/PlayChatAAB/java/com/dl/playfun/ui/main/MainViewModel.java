package com.dl.playfun.ui.main;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.BannerItemEntity;
import com.dl.playfun.entity.BubbleEntity;
import com.dl.playfun.entity.CoinWalletEntity;
import com.dl.playfun.entity.LikeRecommendEntity;
import com.dl.playfun.entity.MqBroadcastGiftEntity;
import com.dl.playfun.entity.MqGiftDataEntity;
import com.dl.playfun.entity.VersionEntity;
import com.dl.playfun.event.BubbleTopShowEvent;
import com.dl.playfun.event.MainTabEvent;
import com.dl.playfun.event.MessageCountChangeEvent;
import com.dl.playfun.event.MessageGiftNewEvent;
import com.dl.playfun.event.RewardRedDotEvent;
import com.dl.playfun.event.TaskMainTabEvent;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.manager.LocationManager;
import com.dl.playfun.utils.FastCallFunUtil;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.google.gson.Gson;
import com.tencent.coustom.GiftEntity;
import com.tencent.coustom.IMGsonUtils;
import com.tencent.imsdk.v2.V2TIMAdvancedMsgListener;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.qcloud.tuikit.tuichat.bean.message.TUIMessageBean;
import com.tencent.qcloud.tuikit.tuichat.util.ChatMessageBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
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
    UIChangeObservable uc = new UIChangeObservable();
    private Disposable mSubscription, taskMainTabEventReceive, mainTabEventReceive, rewardRedDotEventReceive, BubbleTopShowEventSubscription;

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
        lockPassword.set(model.readPassword());
        if (!StringUtil.isEmpty(lockPassword.get())) {
            uc.lockDialog.call();
        }
//        if (model.readLoginInfo().getIsContract() != 1) {
//            uc.showAgreementDialog.call();
//        }
        if (model.readNeedVerifyFace()) {
            uc.showFaceRecognitionDialog.call();
        }
        LocationManager.getInstance().initloadLocation(new LocationManager.LocationListener() {
            @Override
            public void onLocationSuccess(double lat, double lng) {
                loadSendLocation(lat, lng, null, null);//调用上报当前定位
            }

            @Override
            public void onLocationFailed() {
                Log.e("初始化当前定位获取异常：", "");
            }
        });
        sendInviteCode();
        //加载屏蔽字数据
        getSensitiveWords();
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
        taskMainTabEventReceive = RxBus.getDefault().toObservable(TaskMainTabEvent.class)
                .compose(RxUtils.exceptionTransformer())
                .compose(RxUtils.schedulersTransformer())
                .subscribe(o -> uc.taskCenterclickTab.postValue(((TaskMainTabEvent) o)));

        //将订阅者加入管理站
        RxSubscriptions.add(taskMainTabEventReceive);
        RxSubscriptions.add(mainTabEventReceive);
        RxSubscriptions.add(rewardRedDotEventReceive);
        RxSubscriptions.add(BubbleTopShowEventSubscription);
    }

    @Override
    public void removeRxBus() {
        super.removeRxBus();
        RxSubscriptions.remove(mSubscription);
        RxSubscriptions.remove(taskMainTabEventReceive);
        RxSubscriptions.remove(mainTabEventReceive);
        RxSubscriptions.remove(rewardRedDotEventReceive);
        RxSubscriptions.remove(BubbleTopShowEventSubscription);
    }

    public void logout() {
        model.logout();
        //startWithPopTo(LoginFragment.class.getCanonicalName(), MainFragment.class.getCanonicalName(), true);
    }

    //显示公告
    public void showAnnouncemnet() {
        try {
            boolean versionAlert = model.readVersion();
            if (ConfigManager.getInstance().isMale()) {//只有男生才弹公告
                if (!versionAlert) {
                    model.saveVersion(AppConfig.VERSION_NAME);
                    uc.versionAlertSl.call();
                    return;
                }
            }
        }catch (Exception e){

        }
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
                        model.clearOneLinkCode();//清除本地邀请码缓存
                    }

                    @Override
                    public void onError(RequestException e) {
                        if (e.getCode() != null && e.getCode().intValue() == 10109) {
                            model.clearOneLinkCode();//清除本地邀请码缓存
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //今日缘分打开上报
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

    //批量搭讪
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

    //回复收入气泡提示
    public void getBubbleSetting() {
        //防抖拦截 最多两秒触发一次
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

    //添加IM消息监听器
    public void initIMListener () {
        V2TIMManager.getMessageManager().addAdvancedMsgListener(new V2TIMAdvancedMsgListener() {
            @Override
            public void onRecvNewMessage(V2TIMMessage msg) {
                TUIMessageBean info = ChatMessageBuilder.buildMessage(msg);
                if (info != null) {
                    String text = String.valueOf(info.getExtra());
                    if (StringUtil.isJSON2(text)) {//做自定义通知判断

                        //普通自定义类型
                        if (text.contains("type")){
                            Map<String, Object> map_data = new Gson().fromJson(text, Map.class);
                            if (map_data != null && map_data.get("type") != null) {
                                String type = Objects.requireNonNull(map_data.get("type")).toString();
                                String data = (String) map_data.get("data");
                                if (StringUtil.isJSON2(data)) {
                                    switch (type) {
                                        case "message_pushGreet"://今日搭訕
                                            if (AppContext.isHomePage){
                                                if(!FastCallFunUtil.getInstance().isFastCallFun("message_pushGreet",5000)){
                                                    uc.clickAccountDialog.setValue("1");
                                                }
                                            }
                                            break;
                                        case "message_pushPay"://未支付儲值鑽石
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
                                        case "message_gift"://接收礼物
                                            if (map_data.get("is_accost") == null) {//不是搭讪礼物
                                                if (!AppContext.isCalling){
                                                    GiftEntity giftEntity = IMGsonUtils.fromJson(data, GiftEntity.class);
                                                    //是特效礼物才发送订阅通知事件
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

                        //公屏礼物数据
                        if (text.contains("sendGift") && text.contains("messageType")) {
                            if (AppContext.isHomePage){
                                setPublicScreenGiftData(text);
                            }
                        }

                    }
                }
            }
        });
    }

    /**
     * 设置公屏礼物数据
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
            Log.i("imsdk", "setPublicScreenGiftData: 解析异常");
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
                        String[] split = data.split(",");
                        List<String> config = Arrays.asList(split);
                        model.saveSensitiveWords(config);
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    public class UIChangeObservable {
        //气泡提示
        public SingleLiveEvent<Boolean> bubbleTopShow = new SingleLiveEvent<>();
        //        public SingleLiveEvent<Void> showAgreementDialog = new SingleLiveEvent<>();
        public SingleLiveEvent<Void> showFaceRecognitionDialog = new SingleLiveEvent<>();
        public SingleLiveEvent<String> startFace = new SingleLiveEvent<>();
        public SingleLiveEvent<Integer> allMessageCountChange = new SingleLiveEvent<>();
        public SingleLiveEvent<Void> lockDialog = new SingleLiveEvent<>();
        public ObservableField<Boolean> gender = new ObservableField<>(false);
        public SingleLiveEvent<MainTabEvent> mainTab = new SingleLiveEvent<>();
        //更新版本
        public SingleLiveEvent<VersionEntity> versionEntitySingl = new SingleLiveEvent<>();
        //每个新版本只会弹出一次
        public SingleLiveEvent<Void> versionAlertSl = new SingleLiveEvent<>();
        //打开批量搭讪
        public SingleLiveEvent<String> clickAccountDialog = new SingleLiveEvent<>();
        //未付费弹窗
        public SingleLiveEvent<String> notPaidDialog = new SingleLiveEvent<>();
        public SingleLiveEvent<MqBroadcastGiftEntity> giftBanner = new SingleLiveEvent<>();
        //任务中心跳转
        public SingleLiveEvent<TaskMainTabEvent> taskCenterclickTab = new SingleLiveEvent<>();
    }

}