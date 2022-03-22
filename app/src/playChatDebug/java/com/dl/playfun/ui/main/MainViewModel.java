package com.dl.playfun.ui.main;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.Injection;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.BannerItemEntity;
import com.dl.playfun.entity.BubbleEntity;
import com.dl.playfun.entity.CoinWalletEntity;
import com.dl.playfun.entity.FaceVerifyResultEntity;
import com.dl.playfun.entity.FaceVerifyTokenEntity;
import com.dl.playfun.entity.LikeRecommendEntity;
import com.dl.playfun.entity.RecommendUserEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.entity.VersionEntity;
import com.dl.playfun.event.BubbleTopShowEvent;
import com.dl.playfun.event.MainTabEvent;
import com.dl.playfun.event.MessageCountChangeEvent;
import com.dl.playfun.event.MessageGiftNewEvent;
import com.dl.playfun.event.RewardRedDotEvent;
import com.dl.playfun.event.TaskMainTabEvent;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.manager.LocationManager;
import com.dl.playfun.ui.login.facerecognitionfailed.FaceRecognitionFailedFragment;
import com.dl.playfun.ui.login.facerecognitionsuccess.FaceRecognitionSuccessFragment;
import com.dl.playfun.ui.login.login.LoginFragment;
import com.dl.playfun.utils.LogUtils;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.utils.Utils;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.google.gson.Gson;
import com.tencent.coustom.GiftEntity;
import com.tencent.coustom.IMGsonUtils;
import com.tencent.imsdk.v2.V2TIMAdvancedMsgListener;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.qcloud.tuikit.tuichat.bean.MessageInfo;
import com.tencent.qcloud.tuikit.tuichat.util.ChatMessageInfoUtil;
import com.tencent.qcloud.tuikit.tuichat.util.TUIChatLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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
    public List<BannerItemEntity> bannerEntity;
    public LikeRecommendEntity likeRecommendEntity;
    public boolean isNewUser = false;
    UIChangeObservable uc = new UIChangeObservable();
    //退出登录
    public BindingCommand logoutOnClickCommand = new BindingCommand(() -> uc.clickLogout.call());
    private String bizId;
    private Disposable mSubscription, taskMainTabEventReceive, mainTabEventReceive, rewardRedDotEventReceive, BubbleTopShowEventSubscription, newUserEventSubscription;

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
        new Handler().postDelayed(() -> {
//            loadBanner();
            loadGuessYouLike();
        }, 3000);

        ConfigManager configManager = ConfigManager.getInstance();
        if (configManager.getRecommendClose()) {
            return;
        }
        Map<String, String> map = model.redMessageTagUser();
        if (!ObjectUtils.isEmpty(map)) {
            UserDataEntity userDataEntity = model.readUserData();
            if (userDataEntity != null) {
                if (map.get("userId").equals(String.valueOf(userDataEntity.getId()))) {
                    String date = Utils.formatday.format(new Date());
                    String localDate = map.get("date");
                    if (date.equals(localDate)) {
                        Integer num = Integer.parseInt(map.get("num"));
                        if (num.intValue() == 1) {
                            //recommendMsg(map.get("userId"),String.valueOf(2),30*1000);
                            recommendMsg(map.get("userId"), String.valueOf(2), configManager.getRecommendTwoTime() * 1000);
                            //recommendMsg(map.get("userId"),String.valueOf(1),30*1000);
                        }
                    } else {
                        recommendMsg(map.get("userId"), String.valueOf(1), configManager.getRecommendOneTime() * 1000);
                    }
                } else {
                    recommendMsg(String.valueOf(userDataEntity.getId()), String.valueOf(1), configManager.getRecommendOneTime() * 1000);
                }
            }
        } else {
            UserDataEntity userDataEntity = model.readUserData();
            recommendMsg(String.valueOf(userDataEntity.getId()), String.valueOf(1), configManager.getRecommendOneTime() * 1000);
        }
        //加载屏蔽字数据
        getSensitiveWords();
    }
        public void initIMListener () {
            V2TIMManager.getMessageManager().addAdvancedMsgListener(new V2TIMAdvancedMsgListener() {
                @Override
                public void onRecvNewMessage(V2TIMMessage msg) {
                   // super.onRecvNewMessage(msg);
                    MessageInfo info = ChatMessageInfoUtil.createMessageInfo(msg);
                    if (info != null) {
                        String text = String.valueOf(info.getExtra());
                        if (StringUtil.isJSON2(text) && text.contains("type")) {//做自定义通知判断
                        Map<String, Object> map_data = new Gson().fromJson(text, Map.class);
                        if (map_data != null && map_data.get("type") != null) {
                            String type = Objects.requireNonNull(map_data.get("type")).toString();
                            String data = (String) map_data.get("data");
                            if (StringUtil.isJSON2(data)) {
                                switch (type) {
                                    case "message_pushGreet"://今日搭訕
                                        if (AppContext.isHomePage)
                                            uc.clickAccountDialog.setValue("1");
                                        break;
                                    case "message_pushPay"://未支付儲值鑽石
                                        if (AppContext.isShowNotPaid){
                                            Map<String, Object> dataMapPushPay = new Gson().fromJson(data, Map.class);
                                            String dataType = Objects.requireNonNull(dataMapPushPay.get("type")).toString();
                                            if (dataType.equals("1") || dataType.equals("1.0")) {
                                                uc.notPaidDialog.setValue("1");
                                            } else {
                                                uc.notPaidDialog.setValue("2");
                                            }
                                        }
                                        remove(msg);
                                        break;
                                    case "message_gift"://接收礼物
                                        if (map_data.get("is_accost") == null) {//不是搭讪礼物
                                            LogUtils.i("onRecvNewMessage: message_gift");
                                            GiftEntity giftEntity = IMGsonUtils.fromJson(data, GiftEntity.class);
                                            //是特效礼物才发送订阅通知事件
                                            if (!StringUtils.isEmpty(giftEntity.getSvgaPath())) {
                                                RxBus.getDefault().post(new MessageGiftNewEvent(giftEntity));
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    }
                }
//                } else {
//                    LogUtils.i("onRecvNewMessage:当前界面不在首页");
//                }
            }
        });
    }

    /**
     * 删除本地和云端的数据
     * @param msg
     */
    private void remove(V2TIMMessage msg) {
        List<V2TIMMessage> v2TIMMessages = new ArrayList<>();
        v2TIMMessages.add(msg);

        V2TIMManager.getMessageManager().deleteMessages(v2TIMMessages, new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                LogUtils.i("onError: "+code);
            }

            @Override
            public void onSuccess() {
                LogUtils.i("onSuccess: ");
            }
        });
    }

    //显示公告
    public void showAnnouncemnet() {
        boolean versionAlert = model.readVersion();
        if (ConfigManager.getInstance().isMale()) {//只有男生才弹公告
            if (!versionAlert) {
                model.saveVersion(AppConfig.VERSION_NAME);
                uc.versionAlertSl.call();
                return;
            }
        }
        uc.newUserRegis.postValue(false);
    }

    private void recommendMsg(String userId, String num, long timeOut) {
        /**
         * 进入首页延迟30秒后请求后台推荐匹配好友
         */
        AppContext.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, String> maps = new HashMap<>();
                    maps.put("userId", userId);
                    maps.put("date", Utils.formatday.format(new Date()));
                    maps.put("num", num);
                    model.saveMessageTagUser(maps);
                    AppContext.instance().recommendMsg();
                    AppContext.sUiThreadHandler.removeCallbacks(this);
                } catch (Exception e) {
                    AppContext.sUiThreadHandler.removeCallbacks(this);
                }
            }
        }, timeOut);
    }

    @Override
    public void registerRxBus() {
        super.registerRxBus();
        mSubscription = RxBus.getDefault().toObservable(MessageCountChangeEvent.class)
                .subscribe(messageCountChangeEvent -> uc.allMessageCountChange.postValue(messageCountChangeEvent.getCount()));
        taskMainTabEventReceive = RxBus.getDefault().toObservable(TaskMainTabEvent.class)
                .compose(RxUtils.exceptionTransformer())
                .compose(RxUtils.schedulersTransformer())
                .subscribe(new Consumer() {
                    @Override
                    public void accept(Object o) throws Exception {
                        uc.taskCenterclickTab.postValue(((TaskMainTabEvent) o));
                    }
                });
        mainTabEventReceive = RxBus.getDefault().toObservable(MainTabEvent.class).subscribe(event -> {
            uc.mainTab.postValue(event);
        });
        rewardRedDotEventReceive = RxBus.getDefault().toObservable(RewardRedDotEvent.class).subscribe(event -> {
            isHaveRewards.set(event.isHaveReward());
        });

        BubbleTopShowEventSubscription = RxBus.getDefault().toObservable(BubbleTopShowEvent.class).subscribe(event -> {
            uc.bubbleTopShow.postValue(false);
        });


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
        startWithPopTo(LoginFragment.class.getCanonicalName(), MainFragment.class.getCanonicalName(), true);
    }

//    public void clearLoginInfo() {
//        model.logout();
//    }

    //检测新的版本
    public void versionOnClickCommand() {
        model.detectionVersion("Android").compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.schedulersTransformer())
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

    public void loadBanner() {
        model.getBanner(2, 1)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .subscribe(new BaseObserver<BaseDataResponse<List<BannerItemEntity>>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<List<BannerItemEntity>> response) {
                        bannerEntity = response.getData();
                        uc.showAdDialog.postValue(response.getData());
                    }
                });
    }

    public void loadGuessYouLike() {
        model.getLikeRecommend()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<LikeRecommendEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<LikeRecommendEntity> response) {
                        if (response.getData().getUser() != null && response.getData().getUser().size() > 0) {
                            likeRecommendEntity = response.getData();
                            uc.showRecommendUserDialog.postValue(response.getData());
                        }
                    }

                    @Override
                    public void onComplete() {
                    }
                });
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

    public void callGuessYouLike(LikeRecommendEntity entity) {
        List<Integer> ids = new ArrayList<Integer>();
            for (RecommendUserEntity recommendUserEntity : entity.getUser()) {
                ids.add(recommendUserEntity.getId());
            }
            model.likeRecommendCall(ids)
                    .doOnSubscribe(this)
                    .compose(RxUtils.schedulersTransformer())
                    .compose(RxUtils.exceptionTransformer())
                    .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        ToastUtils.showShort(R.string.message_sended);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public void startFaceVerify() {
        model.faceVerifyToken()
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<FaceVerifyTokenEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<FaceVerifyTokenEntity> response) {
                        if (response.getData().getBizId() == null || response.getData().getVerifyToken() == null) {
                            ToastUtils.showShort(R.string.server_exception);
                            return;
                        }
                        bizId = response.getData().getBizId();
                        uc.startFace.postValue(response.getData().getVerifyToken());
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public void verifyFaceResult() {
        model.faceVerifyResult(bizId)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<FaceVerifyResultEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<FaceVerifyResultEntity> response) {
                        if (response.getData().getVerifyStatus() == 1) {
                            start(FaceRecognitionSuccessFragment.class.getCanonicalName());
                        } else {
                            start(FaceRecognitionFailedFragment.class.getCanonicalName());
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

    public void loadBalance() {
        Injection.provideDemoRepository().coinWallet()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<CoinWalletEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CoinWalletEntity> response) {
//                        mBalance = response.getData().getTotalCoin();
//                        tvBalance.setText(String.valueOf(response.getData().getTotalCoin()));
//                        autoPay();
                    }

                    @Override
                    public void onError(RequestException e) {
                        ToastUtils.showShort(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                    }
                });
    }


    public class UIChangeObservable {
        //气泡提示
        public SingleLiveEvent<Boolean> bubbleTopShow = new SingleLiveEvent<>();
        public SingleLiveEvent<Boolean> newUserRegis = new SingleLiveEvent<>();
        //        public SingleLiveEvent<Void> showAgreementDialog = new SingleLiveEvent<>();
        public SingleLiveEvent<Void> showFaceRecognitionDialog = new SingleLiveEvent<>();
        public SingleLiveEvent<String> startFace = new SingleLiveEvent<>();
        public SingleLiveEvent<Integer> allMessageCountChange = new SingleLiveEvent<>();
        public SingleLiveEvent<Void> lockDialog = new SingleLiveEvent<>();
        public SingleLiveEvent<Void> clickLogout = new SingleLiveEvent<>();
        public SingleLiveEvent<List<BannerItemEntity>> showAdDialog = new SingleLiveEvent<>();
        public SingleLiveEvent<LikeRecommendEntity> showRecommendUserDialog = new SingleLiveEvent<>();
        public ObservableField<Boolean> gender = new ObservableField<>(false);

        //更新版本
        public SingleLiveEvent<VersionEntity> versionEntitySingl = new SingleLiveEvent<>();
        public SingleLiveEvent<TaskMainTabEvent> taskCenterclickTab = new SingleLiveEvent<>();
        public SingleLiveEvent<MainTabEvent> mainTab = new SingleLiveEvent<>();
        //每个新版本只会弹出一次
        public SingleLiveEvent<Void> versionAlertSl = new SingleLiveEvent<>();
        //打开批量搭讪
        public SingleLiveEvent<String> clickAccountDialog = new SingleLiveEvent<>();
        //未付费弹窗
        public SingleLiveEvent<String> notPaidDialog = new SingleLiveEvent<>();
    }

}