package com.dl.playfun.kl.viewmodel;

import android.app.Application;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.databinding.ObservableList;

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
import com.dl.playfun.entity.CallingInviteInfo;
import com.dl.playfun.entity.CustomMessageIMTextEntity;
import com.dl.playfun.entity.GiftBagEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.entity.UserProfileInfo;
import com.dl.playfun.event.CallVideoUserEnterEvent;
import com.dl.playfun.kl.view.JMTUICallVideoView;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.dialog.GiftBagDialog;
import com.dl.playfun.utils.ChatUtils;
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
import com.tencent.liteav.trtccalling.model.TUICalling;
import com.tencent.qcloud.tuikit.tuichat.bean.MessageInfo;
import com.tencent.qcloud.tuikit.tuichat.util.ChatMessageInfoUtil;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

public class VideoCallViewModel extends BaseViewModel<AppRepository> {
    public int TimeCount = 0;
    //是否发送过礼物
    public boolean sendGiftBagSuccess = false;
    public boolean isCallingInviteInfoNull = false;

    public ObservableField<Boolean> isShowCountdown = new ObservableField(false);

    //录音文案数组坐标
    public int sayHiePosition = 0;
    public int sayHiePage = 1;
    //订阅者
    private Disposable userEnterSubscription;

    //是否是拨打方
    public boolean userCall = false;
    //对方用户信息
    public ObservableField<CallingInviteInfo> callingInviteInfoField = new ObservableField<>();
    public Integer $coinBalance = 0;
    // 被叫还没接听时的遮挡层
    public ObservableField<Boolean> isShelterShowBinding = new ObservableField<>(false);
    public ObservableField<String> myNicknameBinding = new ObservableField<>("");
    // 名字右边的标签
    public ObservableField<Boolean> isLabel1ShowBinding = new ObservableField<>(false);
    public ObservableField<Boolean> isLabel2ShowBinding = new ObservableField<>(false);
    public ObservableField<Boolean> isCalledWaitingBinding = new ObservableField<>(true);
    // 是否被叫
    public ObservableField<Boolean> isCalledBinding = new ObservableField<>(false);
    // 未接听时的收益提示（黄色字体）
    public ObservableField<String> callHintBinding = new ObservableField<>("");

    public ObservableField<Boolean> mainVIewShow = new ObservableField<>(false);

    protected TRTCCalling mTRTCCalling;
    protected JMTUICallVideoView mCallVideoView;


    public void hangup() {
        mCallVideoView.hangup();
    }

    public String mfromUserId;
    public String mtoUserId;

    public BigDecimal coinTotal;
    //当前用户是否男性
    public boolean isMale = false;
    //钻石余额(仅男用户)
    public Integer coinBalance;

    public boolean videoSuccess = false;
    public BindingCommand acceptOnclick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            if (!videoSuccess) {
                return;
            }
            mainVIewShow.set(true);
            mCallVideoView.acceptCall();
            //getCallingInfo(roomId, ChatUtils.imUserIdToSystemUserId(mMyUserId), ChatUtils.imUserIdToSystemUserId(mOtherUserId));
            if (mRole == TUICalling.Role.CALLED) {
                isCalledWaitingBinding.set(false);
            }
        }
    });

    //点击文字充值
    public BindingCommand referMoney = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            AppContext.instance().logEvent(AppsFlyerEvent.videocall_topup);
            uc.sendUserGiftError.postValue(false);
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



    //已经取下标数
    public Integer formSelIndex = 0;
    //收益从第N分钟开始
    public Integer fromMinute;
    //单价
    public BigDecimal unitPrice;
    //每秒收益
    public BigDecimal timePrice;
    //是否已追踪0未追踪1已追踪
    public Integer collected;
    //是否使用道具
    public Integer useProp;
    //余额不足提示分钟数
    public Integer balanceNotEnoughTipsMinutes;
    public int maleBalanceMoney;
    public boolean flagMoney = false;
    //通话收益提示间隔秒数
    public Integer profitTipsIntervalSeconds;
    //价格配置表
    public List<CallingInfoEntity.CallingUnitPriceInfo> unitPriceList;

    //时间提示
    public ObservableField<String> timeTextField = new ObservableField<>();
    //对方用户信息
    public ObservableField<CallingInfoEntity.FromUserProfile> callingVideoInviteInfoField = new ObservableField<>();
    //男生收益框是否展示
    public ObservableBoolean maleTextLayoutSHow = new ObservableBoolean(false);
    //男性收益内容
    public ObservableField<String> maleTextMoneyField = new ObservableField();
    //女性收益弹窗是否显示
    public ObservableBoolean girlEarningsField = new ObservableBoolean(false);
    //收益文字
    public ObservableField<SpannableString> girlEarningsText = new ObservableField<>();
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

    public BindingRecyclerViewAdapter<VideoCallChatingItemViewModel> adapter = new BindingRecyclerViewAdapter<>();
    public ObservableList<VideoCallChatingItemViewModel> observableList = new ObservableArrayList<>();
    public ItemBinding<VideoCallChatingItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_call_video_chating);
    public UIChangeObservable uc = new UIChangeObservable();
    public BindingCommand closeOnclick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            uc.closeViewHint.call();
            //mCallVideoView.hangup();
        }
    });
    //发送礼物
    public BindingCommand giftBagOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            AppContext.instance().logEvent(AppsFlyerEvent.videocall_gift);
            uc.callGiftBagAlert.call();
        }
    });

    //禁用麦克风点击
    public BindingCommand micMuteOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            if (micMuteField.get()) {//开启免提
                ToastUtils.showShort(R.string.playfun_call_message_deatail_txt_4);
            } else {
                ToastUtils.showShort(R.string.playfun_call_message_deatail_txt_3);
            }
            boolean minMute = !micMuteField.get();
            micMuteField.set(minMute);
            mCallVideoView.setMicMute(minMute);
        }
    });

    //声音展示
    public BindingCommand handsFreeOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            if (handsFreeField.get()) {//开启免提
                ToastUtils.showShort(R.string.playfun_call_message_deatail_txt_2);
            } else {
                ToastUtils.showShort(R.string.playfun_call_message_deatail_txt_1);
            }
            boolean handsFree = !handsFreeField.get();
            handsFreeField.set(handsFree);
            mCallVideoView.setHandsFree(handsFree);
        }
    });

    public BindingCommand switchCameraOnclick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            mCallVideoView.switchCamera();
        }
    });
    protected int roomId = 0;// 房间ID必须持有

    public BindingCommand rejectOnclick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            mCallVideoView.hangup();
        }
    });

    //关注
    public BindingCommand addlikeOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            AppContext.instance().logEvent(AppsFlyerEvent.videocall_follow);
            addLike(false);
//            if (ConfigManager.getInstance().isMale()){
//                addLike(false);
//            }else {
//                //是女生提示
//                int guideFlag = model.readSwitches(EaringlSwitchUtil.KEY_TIPS);
//                //后台开关 1提示  0隐藏
//                if (guideFlag == 1) {
//                    uc.clickLike.call();
//                } else {
//                    addLike(false);
//                }
//            }
        }
    });
    //切换破冰文案提示
    public BindingCommand upSayHiEntityOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            AppContext.instance().logEvent(AppsFlyerEvent.videocall_ice_change);
            uc.startVideoUpSayHiAnimotor.call();
//            getSayHiList();
        }
    });
    //关闭破冰文案提示
    public BindingCommand colseSayHiEntityOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            AppContext.instance().logEvent(AppsFlyerEvent.videocall_ice_close);
            sayHiEntityHidden.set(true);
        }
    });
    private String mMyUserId;
    private String mOtherUserId;
    private TUICalling.Role mRole;

    //拨打语音、视频
    public void getCallingInvitedInfo(int callingType, String fromUserId) {
        String userId = model.readUserData().getImUserId();
        model.callingInviteInfo(callingType, fromUserId, userId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<CallingInviteInfo>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CallingInviteInfo> callingInviteInfoBaseDataResponse) {
                        CallingInviteInfo callingInviteInfo = callingInviteInfoBaseDataResponse.getData();
                        roomId = callingInviteInfo.getRoomId();
                        Log.e("当前用户进入房间ID", roomId + "=====================");
                        mfromUserId = fromUserId;
                        mtoUserId = userId;
                        callingInviteInfoField.set(callingInviteInfo);
                        if (model.readUserData().getSex() == 0) {
                            if (!ObjectUtils.isEmpty(callingInviteInfo.getMessages()) && callingInviteInfo.getMessages().size() > 0) {
                                String valueData = "";
                                for (String value : callingInviteInfo.getMessages()) {
                                    valueData += value + "\n";
                                }
                                callHintBinding.set(valueData);
                            }
                        }
                        if (callingInviteInfo.getMinutesRemaining() != null && callingInviteInfo.getMinutesRemaining().intValue() <= 0) {
                            hangup();
                            return;
                        }
                        if (callingInviteInfo.getMinutesRemaining() != null && callingInviteInfo.getMinutesRemaining().intValue() > 0) {
                            videoSuccess = true;
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public void getTips(Integer toUserId, int type, String isShowCountdown){
        model.getTips(toUserId, type,isShowCountdown)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse>() {
                    @Override
                    public void onSuccess(BaseDataResponse baseDataResponse) {
                    }

                    @Override
                    public void onError(RequestException e) {

                    }
                });
    }

    public VideoCallViewModel(@NonNull @NotNull Application application) {
        super(application);
    }

    public VideoCallViewModel(@NonNull @NotNull Application application, AppRepository model) {
        super(application, model);
    }

    //    protected TRTCCallingDelegate mTRTCCallingDelegate;
    public void init(String myUserId, String otherUserId, TUICalling.Role role, JMTUICallVideoView view) {
        this.mMyUserId = myUserId;
        this.mOtherUserId = otherUserId;
        this.mRole = role;
        this.mCallVideoView = view;
        this.isMale = ConfigManager.getInstance().isMale();
        this.isCalledBinding.set(role == TUICalling.Role.CALLED);
        this.isCalledWaitingBinding.set(role == TUICalling.Role.CALLED);
    }

    public void init(String myUserId, String otherUserId, TUICalling.Role role, JMTUICallVideoView view, Integer roomId) {
        this.mMyUserId = myUserId;
        this.mOtherUserId = otherUserId;
        this.mRole = role;
        this.mCallVideoView = view;
        this.isMale = ConfigManager.getInstance().isMale();
        this.isCalledBinding.set(role == TUICalling.Role.CALLED);
        this.isCalledWaitingBinding.set(role == TUICalling.Role.CALLED);
        this.roomId = roomId;
    }

    public Drawable getVipGodsImg(CallingInviteInfo callingInviteInfo) {
        if (callingInviteInfo != null) {
            UserProfileInfo userProfileInfo = callingInviteInfo.getUserProfileInfo();
            if (userProfileInfo != null) {
                if (userProfileInfo.getSex() == 1) {
                    if (userProfileInfo.getIsVip() == 1) {
                        return AppContext.instance().getDrawable(R.drawable.ic_vip);
                    } else {
                        if (userProfileInfo.getCertification() == 1) {
                            return AppContext.instance().getDrawable(R.drawable.ic_real_man);
                        }
                    }
                } else {//女生
                    if (userProfileInfo.getIsVip() == 1) {
                        return AppContext.instance().getDrawable(R.drawable.ic_goddess);
                    } else {
                        if (userProfileInfo.getCertification() == 1) {
                            return AppContext.instance().getDrawable(R.drawable.ic_real_man);
                        }
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

    public String ageAndConstellation(CallingInviteInfo callingInviteInfo) {
        if (callingInviteInfo != null) {
            return String.format(StringUtils.getString(R.string.playfun_age_and_constellation), callingInviteInfo.getUserProfileInfo().getAge());
        }
        return "";
    }

    //添加文案到公屏
    public void putRcvItemMessage(SpannableString stringBuilder, String imgPath, boolean sendGiftBagShow) {
        observableList.add(new VideoCallChatingItemViewModel(VideoCallViewModel.this, stringBuilder, imgPath, sendGiftBagShow));
        uc.scrollToEnd.postValue(null);
    }

    //获取破冰文案
    public void getSayHiList() {
        //录音文案数组坐标
        if (!ObjectUtils.isEmpty(sayHiEntityList) && sayHiePosition + 1 <= sayHiEntityList.size() - 1) {
            sayHiePosition++;
            sayHiEntityField.set(sayHiEntityList.get(sayHiePosition));
            return;
        }
        if (sayHiePosition < sayHiEntityList.size()) {
            return;
        }
        sayHiePage++;
        model.getSayHiList(sayHiePage, 20)
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
                                sayHiEntityField.set(sayHiEntityList.get(0));
                            }
                        } catch (Exception e) {

                        }
                    }
                });
    }

    //通话中获取资料
    public void getCallingInfo(Integer roomId, String fromUserId, String toUserId) {
        if (isCallingInviteInfoNull) {
            return;
        }
        isCallingInviteInfoNull = true;
        Log.e("通话中调用接口", "====================" + roomId + "======" + fromUserId + "==========" + toUserId);
        model.getCallingInfo(roomId, 2, fromUserId, toUserId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<CallingInfoEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CallingInfoEntity> response) {
                        Log.e("通话中获取资料", "================");
                        isCallingInviteInfoNull = true;
                        CallingInfoEntity callingInviteInfo = response.getData();
                        UserDataEntity userDataEntity = model.readUserData();
                        mCallVideoView.acceptCall();
                        uc.callAudioStart.call();
                        sayHiEntityList = callingInviteInfo.getSayHiList().getData();
                        if (sayHiEntityList.size() > 1) {
                            sayHiEntityHidden.set(false);
                            sayHiEntityField.set(sayHiEntityList.get(0));
                        }
                        if (userDataEntity.getId().intValue() == callingInviteInfo.getFromUserProfile().getId().intValue()) {
                            callingVideoInviteInfoField.set(callingInviteInfo.getToUserProfile());
                        } else {
                            callingVideoInviteInfoField.set(callingInviteInfo.getFromUserProfile());
                        }
                        //钻石余额(仅男用户)
                        coinBalance = callingInviteInfo.getCoinBalance();
                        //是否已追踪0未追踪1已追踪
                        collected = callingInviteInfo.getCollected();
                        collectedField.set(collected);
                        //是否使用道具
                        useProp = callingInviteInfo.getUseProp();
                        //余额不足提示分钟数
                        balanceNotEnoughTipsMinutes = callingInviteInfo.getBalanceNotEnoughTipsMinutes();
                        //通话收益提示间隔秒数
                        profitTipsIntervalSeconds = callingInviteInfo.getProfitTipsIntervalSeconds();
                        //价格配置表
                        unitPriceList = callingInviteInfo.getUnitPriceList();
                        unitPrice = unitPriceList.get(0).getUnitPrice();
                        fromMinute = unitPriceList.get(0).getFromMinute();
                        timePrice = unitPrice.divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
                        initIMListener();
                        mCallVideoView.enableAGC(true);
                        mCallVideoView.enableAEC(true);
                        mCallVideoView.enableANS(true);
                    }

                    @Override
                    public void onError(RequestException e) {
                        Log.e("接口调用失败", "失败原因：" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

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
                        dialog.dismiss();
                        String textTip = null;
                        //礼物数量*礼物钻石
                        int amountMoney = giftEntity.getMoney().intValue() * amount;
                        ((GiftBagDialog) dialog).setBalanceValue(amountMoney);
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
                            textTip = StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt_male);
                        } else {
                            textTip = StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt_gift);
                        }
                        String nickname = callingVideoInviteInfoField.get().getNickname();
                        textTip += " " + nickname;
                        int startLength = textTip.length();
                        textTip += " " + giftEntity.getName() + " x" + amount;
                        int nicknameIndex = textTip.indexOf(nickname);
                        SpannableString stringBuilder = new SpannableString(textTip);

                        ForegroundColorSpan blueSpan = new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint2));
                        ForegroundColorSpan blueSpan2 = new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint2));
                        ForegroundColorSpan blueSpanWhite = new ForegroundColorSpan(ColorUtils.getColor(R.color.white));
                        stringBuilder.setSpan(blueSpanWhite, 0, textTip.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        stringBuilder.setSpan(blueSpan, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        stringBuilder.setSpan(blueSpan2, nicknameIndex, nicknameIndex + nickname.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                            AppContext.instance().logEvent(AppsFlyerEvent.videocall_gift_Insu_topup);
                            uc.sendUserGiftError.postValue(true);
                        }
                    }
                });
    }

    //追踪
    public void addLike(boolean isHangup) {
        model.addIMCollect(callingVideoInviteInfoField.get().getId(), 2)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        if (isHangup) {
                            mCallVideoView.hangup();
                        } else {
                            collected = 1;
                            collectedField.set(1);
                            ToastUtils.showShort(R.string.playfun_cancel_zuizong_3);
                            String sexText = isMale ? StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt3) : StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt2);
                            String msgText = sexText + StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt4) + callingVideoInviteInfoField.get().getNickname();
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

    //注册RxBus
    @Override
    public void registerRxBus() {
        super.registerRxBus();
        userEnterSubscription = RxBus.getDefault().toObservable(CallVideoUserEnterEvent.class)
                .subscribe(event -> {
                    mainVIewShow.set(true);
                    if (roomId != 0 && mfromUserId != null && mtoUserId != null) {
                        getCallingInfo(roomId, mfromUserId, mtoUserId);
                    } else {
                        getCallingInfo(roomId, model.readUserData().getImUserId(), event.getUserId());
                    }

                });
        //将订阅者加入管理站
        RxSubscriptions.add(userEnterSubscription);
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

    //监听IM消息
    private void initIMListener() {
        V2TIMManager.getMessageManager().addAdvancedMsgListener(new V2TIMAdvancedMsgListener() {
            @Override
            public void onRecvNewMessage(V2TIMMessage msg) {//新消息提醒
                if (msg != null && callingVideoInviteInfoField.get() != null) {
                    MessageInfo info = ChatMessageInfoUtil.createMessageInfo(msg);
                    if (info != null) {
                        if (info.getFromUser().equals(callingVideoInviteInfoField.get().getImId())) {
                            Log.e("确定是聊天对象发送的消息", "==================");
                            String text = String.valueOf(info.getExtra());
                            Log.e("聊天消息体未", text);
                            if (isJSON2(text) && text.indexOf("type") != -1) {//做自定义通知判断
                                Map<String, Object> map_data = new Gson().fromJson(text, Map.class);
                                //礼物消息
                                if (map_data != null && map_data.get("type") != null && map_data.get("type").equals("message_gift")
                                        && map_data.get("is_accost") == null) {
                                    Log.e("该消息是聊天消息", "===============");
                                    GiftEntity giftEntity = IMGsonUtils.fromJson(String.valueOf(map_data.get("data")), GiftEntity.class);
                                    uc.acceptUserGift.postValue(giftEntity);
                                    //显示礼物弹幕
                                    showGiftBarrage(giftEntity);

                                    //礼物收益提示
                                    giftIncome(giftEntity);
                                } else if (map_data != null && map_data.get("type") != null && map_data.get("type").equals("message_tracking")) {//追踪提示
                                    CustomMessageIMTextEntity giftEntity = IMGsonUtils.fromJson(String.valueOf(map_data.get("data")), CustomMessageIMTextEntity.class);
                                    if (giftEntity != null) {
                                        String sexText = isMale ? StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt3) : StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt2);
                                        String msgText = giftEntity.getToName() + StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt4) + sexText;
                                        SpannableString stringBuilder = new SpannableString(msgText);
                                        ForegroundColorSpan blueSpan = new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint2));
                                        stringBuilder.setSpan(blueSpan, 0, msgText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        putRcvItemMessage(stringBuilder, null, false);
                                    }
                                }else if (map_data != null && map_data.get("type") != null && map_data.get("type").equals("message_countdown")) {//对方余额不足
                                    if (!isMale && ConfigManager.getInstance().getTipMoneyShowFlag()) {
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

    /**
     * 显示礼物弹幕
     *
     * @param giftEntity
     */
    private void showGiftBarrage(GiftEntity giftEntity) {
        int nickNameLength = callingVideoInviteInfoField.get().getNickname().length();
        String sexText = isMale ? StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt3) : StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt2);
        String messageText = callingVideoInviteInfoField.get().getNickname() + " " + StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt1) + " " + sexText + " " + giftEntity.getTitle() + "x" + giftEntity.getAmount();
        int youIndex = messageText.indexOf(sexText);
        SpannableString stringBuilder = new SpannableString(messageText);

        stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.white)), 0, messageText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint2)), 0, nickNameLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint2)), youIndex, youIndex + sexText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        putRcvItemMessage(stringBuilder, giftEntity.getImgPath(), false);
    }

    /**
     * 礼物收益提示
     *
     * @param giftEntity
     */
    private void giftIncome(GiftEntity giftEntity) {
        try {
            if(!isMale){
                String itemMessage = String.format(StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt5), String.format("%.2f", giftEntity.getAmount().intValue() * giftEntity.getProfitTwd().doubleValue()));
                SpannableString itemMessageBuilder = new SpannableString(itemMessage);
                itemMessageBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.white)), 0, itemMessage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                itemMessageBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint1)), itemMessage.indexOf("+"), itemMessage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                putRcvItemMessage(itemMessageBuilder, null, false);
            }
        }catch (Exception e) {}
    }

    public UserDataEntity readUserData() {
        return model.readUserData();
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
        public SingleLiveEvent<Void> startVideoUpSayHiAnimotor = new SingleLiveEvent<>();
    }

    //移除RxBus
    @Override
    public void removeRxBus() {
        super.removeRxBus();
        //将订阅者从管理站中移除
        RxSubscriptions.remove(userEnterSubscription);
    }


}
