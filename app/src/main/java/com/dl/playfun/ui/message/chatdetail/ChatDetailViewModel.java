package com.dl.playfun.ui.message.chatdetail;

import android.app.Application;
import android.app.Dialog;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.Injection;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.CallingInviteInfo;
import com.dl.playfun.entity.EvaluateEntity;
import com.dl.playfun.entity.EvaluateItemEntity;
import com.dl.playfun.entity.EvaluateObjEntity;
import com.dl.playfun.entity.MessageRuleEntity;
import com.dl.playfun.entity.PhotoAlbumEntity;
import com.dl.playfun.entity.PriceConfigEntity;
import com.dl.playfun.entity.StatusEntity;
import com.dl.playfun.entity.TagEntity;
import com.dl.playfun.entity.TaskRewardReceiveEntity;
import com.dl.playfun.entity.UserConnMicStatusEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.event.AddBlackListEvent;
import com.dl.playfun.event.RewardRedDotEvent;
import com.dl.playfun.kl.Utils;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.utils.ToastCenterUtils;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.google.gson.Gson;
import com.dl.playfun.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author wulei
 */
public class ChatDetailViewModel extends BaseViewModel<AppRepository> {
    public static final String TAG = "ChatDetailViewModel";
    public ObservableField<Boolean> isTagShow = new ObservableField<>(false);
    public ObservableField<Boolean> inBlacklist = new ObservableField<>(false);
    public ObservableField<Boolean> dialogShow = new ObservableField<>(false);
    //IM聊天价格配置
    public PriceConfigEntity priceConfigEntityField = null;
    //男生钻石总额
    public Integer maleBalance = 0;
    //男生聊天卡总额
    public Integer maleCardNumber = 0;
    //单条聊天信息钻石数量
    public Integer maleMessagePrice = 0;
    public boolean isCertification = false;
    //是否相互追踪
    public boolean isFollower = false;
    //是否当前用户付费
    public boolean isPlay = false;

    public Integer ChatInfoId = null;
    public UIChangeObservable uc = new UIChangeObservable();
    public BindingCommand moreOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            hideKeyboard();
            uc.clickMore.call();
        }
    });

    public ChatDetailViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
        uc.userDataEntity.postValue(model.readUserData());
    }

    @Override
    public void onEnterAnimationEnd() {
        super.onEnterAnimationEnd();
    }

    public void loadUserInfo(int userId) {
        model.isBlacklist(String.valueOf(userId))
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .subscribe(new BaseObserver<BaseDataResponse<Map<String, String>>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<Map<String, String>> response) {
                        Map<String, String> mapData = response.getData();
                        if (!ObjectUtils.isEmpty(mapData)) {
                            inBlacklist.set(mapData.get("is_blacklist").equals("1"));
                        }

                    }

                    @Override
                    public void onError(RequestException e) {

                    }
                });
    }

    /**
     * 加载用户标签
     *
     * @param toUserId
     */
    public void loadTagUser(String toUserId) {
        model.tag(toUserId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<TagEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<TagEntity> tagEntityBaseDataResponse) {
                        TagEntity tagEntity = tagEntityBaseDataResponse.getData();
                        if (tagEntity != null) {
                            uc.loadTag.postValue(tagEntity);
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    /**
     * @return void
     * @Author 彭石林
     * @Description 查询对方是否为机器人，是否在线
     * @Date 2021/3/25 18:00
     * @Phone 16620350375
     * @email 15616314565@163.com
     * Param []
     **/
    public void loadChatUserDetail(String userId) {
        model.isOnlineUser(userId)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .subscribe(new BaseObserver<BaseDataResponse<Map<String, String>>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<Map<String, String>> response) {
                        uc.ChatUserDetailEntity.setValue(response.getData());
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //获取当前用户数据
    public void getLocalUserData() {
        uc.userDataEntity.postValue(model.readUserData());
    }

    //获取当前用户数据
    public UserDataEntity getLocalUserDataEntity() {
        return model.readUserData();
    }

    public void addBlackList(int userId) {
        model.addBlack(userId)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        inBlacklist.set(true);
                        RxBus.getDefault().post(new AddBlackListEvent());
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public void delBlackList(int userId) {
        model.deleteBlack(userId)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        inBlacklist.set(false);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public void checkConnMic(int userId) {
        model.userIsConnMic(userId)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<UserConnMicStatusEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<UserConnMicStatusEntity> response) {
                        if (response.getData().getConnection()) {
                            uc.clickConnMic.call();
                        } else {
                            ToastUtils.showShort(R.string.playfun_opposite_mic_disabled);
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }
    //获取聊天照片规则
    public void getMessageRule(){
        model.getMessageRule()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<List<MessageRuleEntity>>>() {

                    @Override
                    public void onSuccess(BaseDataResponse<List<MessageRuleEntity>> listBaseDataResponse) {
                        List<MessageRuleEntity> listMessage = listBaseDataResponse.getData();
                        if(listMessage!=null){
                            uc.resultMessageRule.setValue(listMessage);
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
    //获取用户聊天相册
    public void getPhotoAlbum(Integer userId){
        model.getPhotoAlbum(userId)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .subscribe(new BaseObserver<BaseDataResponse<PhotoAlbumEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<PhotoAlbumEntity> photoAlbumEntityBaseDataResponse) {
                        if(photoAlbumEntityBaseDataResponse.getData()!=null && photoAlbumEntityBaseDataResponse.getData().getImg()!=null && photoAlbumEntityBaseDataResponse.getData().getImg().size()>0){
                            uc.putPhotoAlbumEntity.setValue(photoAlbumEntityBaseDataResponse.getData());
                        }
                    }
                    @Override
                    public void onComplete() {

                    }
                });
    }
    //效验用户是否可以评价
    public void loadCanEvaluate(Integer userId) {
        model.evaluateStatus(userId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<StatusEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<StatusEntity> response) {
                        uc.canEvaluate.postValue(response.getData().getStatus() == 1);
                    }
                    @Override
                    public void onError(RequestException e) {
                        e.printStackTrace();
                    }
                });
    }
    //根据用户ID获取评价
    public void getUserEvaluate(Integer userId,boolean sendIM) {
        model.evaluate(userId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(dispose -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<List<EvaluateEntity>>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<List<EvaluateEntity>> response) {
                        if(response.getData()!=null){
                            List<EvaluateEntity> evaluateEntityList = response.getData();
                            List<EvaluateObjEntity> list = null;
                            if (!ConfigManager.getInstance().isMale()) {
                                list = Injection.provideDemoRepository().readMaleEvaluateConfig();
                            } else {
                                list = Injection.provideDemoRepository().readFemaleEvaluateConfig();
                            }
                            List<EvaluateItemEntity> items = new ArrayList<>();
                            if(sendIM){
                                for (EvaluateObjEntity configEntity : list) {
                                    //好的评价
                                    if(configEntity.getType()==0){
                                        EvaluateItemEntity evaluateItemEntity = new EvaluateItemEntity(configEntity.getId(), configEntity.getName(), configEntity.getType() == 1);
                                        items.add(evaluateItemEntity);
                                        for (EvaluateEntity evaluateEntity : evaluateEntityList) {
                                            if (configEntity.getId() == evaluateEntity.getTagId()) {
                                                evaluateItemEntity.setNumber(evaluateEntity.getNumber());
                                            }
                                        }
                                    }
                                }
                                uc.sendIMEvaluate.setValue(items);
                            }else{
                                for (EvaluateObjEntity configEntity : list) {
                                    EvaluateItemEntity evaluateItemEntity = new EvaluateItemEntity(configEntity.getId(), configEntity.getName(), configEntity.getType() == 1);
                                    items.add(evaluateItemEntity);
                                }
                                uc.AlertMEvaluate.setValue(items);
                            }
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }
    /**
     * 提交评价
     *
     * @param tagId 评价标签ID
     */
    public void commitUserEvaluate(int userId, int tagId, DialogInterface dialog) {
        model.evaluateCreate(userId, tagId, null)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ToastUtils.showShort(R.string.playfun_submittd);
                        uc.removeEvaluateMessage.call();
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                        if(dialog!=null) {
                            dialog.dismiss();
                        }
                    }
                });
    }

    public void sendUserGift(Dialog dialog,Integer gift_id, Integer to_user_id, Integer amount){
        model.sendUserGift(gift_id,to_user_id,amount,1)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>(){
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {
                        dismissHUD();
                        dialog.dismiss();
                    }
                    @Override
                    public void onError(RequestException e) {
                        dialog.dismiss();
                        dismissHUD();
                        if (e.getCode() != null && e.getCode().intValue() == 21001) {
                            ToastCenterUtils.showToast(R.string.playfun_dialog_exchange_integral_total_text1);
                            AppContext.instance().logEvent(AppsFlyerEvent.im_gifts_Insufficient_topup);
                            uc.sendUserGiftError.call();
                        }
                    }
                });
    }

    //拨打语音、视频
    public void getCallingInvitedInfo(int callingType, Integer toUserId, String toImUserId) {
        int userId = getLocalUserDataEntity().getId();
        model.callingInviteInfo(callingType, userId, toUserId, userId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<CallingInviteInfo>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CallingInviteInfo> callingInviteInfoBaseDataResponse) {
                        CallingInviteInfo callingInviteInfo = callingInviteInfoBaseDataResponse.getData();
                        if (callingInviteInfo != null) {
                            Utils.tryStartCallSomeone(callingType, toImUserId, callingInviteInfo.getRoomId(), new Gson().toJson(callingInviteInfo));
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }
    //IM聊天价格配置
    public void getPriceConfig(Integer toUserId){
        model.getPriceConfig(toUserId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<PriceConfigEntity>>(){
                    @Override
                    public void onSuccess(BaseDataResponse<PriceConfigEntity> response) {
                        PriceConfigEntity priceConfigEntity = response.getData();
                        if(priceConfigEntity != null){
                            priceConfigEntityField = priceConfigEntity;
                            isFollower = priceConfigEntity.getIsFollow().intValue() == 1;
                            isPlay = priceConfigEntity.getIsPay().intValue() ==1;
                            maleBalance = priceConfigEntityField.getCurrent().getBalance();
                            maleCardNumber = priceConfigEntityField.getCurrent().getPropTotal();
                            maleMessagePrice = priceConfigEntityField.getCurrent().getTextPrice();
                        }
                    }
                    @Override
                    public void onComplete() {

                    }
                });
    }
    //领取积分
    public void ToaskSubBonus() {
        model.TaskRewardReceive("firstIm")    //1.3.0新接口
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<TaskRewardReceiveEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<TaskRewardReceiveEntity> response) {
                        uc.firstImMsgDialog.postValue(response.getData());
                        //隐藏小红点
                        RxBus.getDefault().post(new RewardRedDotEvent(false));
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public void verifyGoddessTips(Integer userId) {
        Boolean sendSuccess = model.readVerifyGoddessTipsUser(model.readUserData().getId() + "_" + userId);
        if (sendSuccess.booleanValue()) {
            return;
        }
        model.verifyGoddessTips(userId)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<Map<String, Integer>>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<Map<String, Integer>> response) {
                        if (response.getData() != null) {
                            Map<String, Integer> mapData = response.getData();
                            if (mapData.get("status") != null && mapData.get("status").intValue() == 1) {
                                model.putVerifyGoddessTipsUser(model.readUserData().getId() + "_" + userId, "true");
                            }
                        }
                    }
                });
    }

    //存储键值对
    public void putKeyValue(String key, String value) {
        model.putKeyValue(key, value);
    }

    public String readKeyValue(String key) {
        return model.readKeyValue(key);
    }

    //追踪
    public void addLike(Integer toUserId, String msgId) {
        model.addCollect(toUserId)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        uc.addLikeSuccess.postValue(msgId);
                    }

                    @Override
                    public void onError(RequestException e) {
                        super.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        dismissHUD();
                    }
                });
    }


    public class UIChangeObservable {
        public SingleLiveEvent<Void> clickConnMic = new SingleLiveEvent<>();
        public SingleLiveEvent clickMore = new SingleLiveEvent<>();
        //新增
        public SingleLiveEvent<List<Integer>> askUseChatNumber = new SingleLiveEvent<>();
        public SingleLiveEvent<Integer> useChatNumberSuccess = new SingleLiveEvent<>();
        public SingleLiveEvent<UserDataEntity> userDataEntity = new SingleLiveEvent<>();
        //查询对方资料 判断是否为机器人、最后在线时间-判断用户是否在线
        public SingleLiveEvent<Map<String, String>> ChatUserDetailEntity = new SingleLiveEvent<>();

        public SingleLiveEvent<TagEntity> loadTag = new SingleLiveEvent<>();

        //刷新页面数据
        public SingleLiveEvent<Boolean> loadMessage = new SingleLiveEvent<>();
        //插入相扑数据
        public SingleLiveEvent<PhotoAlbumEntity> putPhotoAlbumEntity = new SingleLiveEvent<>();
        //是否可以评价
        public SingleLiveEvent<Boolean> canEvaluate = new SingleLiveEvent<>();
        //发送IM评价插入
        public SingleLiveEvent<List<EvaluateItemEntity>> sendIMEvaluate = new SingleLiveEvent<>();
        //弹出评价框等待用户评价
        public SingleLiveEvent<List<EvaluateItemEntity>> AlertMEvaluate = new SingleLiveEvent<>();
        //删除评价窗体
        public SingleLiveEvent<Void> removeEvaluateMessage = new SingleLiveEvent<>();
        //根据聊天规则弹出相册、评论
        public SingleLiveEvent<List<MessageRuleEntity>> resultMessageRule = new SingleLiveEvent<>();
        //发送礼物失败。充值钻石
        public SingleLiveEvent<Void> sendUserGiftError = new SingleLiveEvent<>();
        //首次收益弹窗展示
        public SingleLiveEvent<TaskRewardReceiveEntity> firstImMsgDialog = new SingleLiveEvent<>();
        //追踪成功
        public SingleLiveEvent<String> addLikeSuccess = new SingleLiveEvent<>();
    }

}
