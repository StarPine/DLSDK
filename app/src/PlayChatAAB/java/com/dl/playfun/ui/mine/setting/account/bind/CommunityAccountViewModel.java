package com.dl.playfun.ui.mine.setting.account.bind;

import android.app.Application;
import android.os.CountDownTimer;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.lib.util.MPDeviceUtils;
import com.dl.playfun.R;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.ChooseAreaItemEntity;
import com.dl.playfun.entity.SystemConfigEntity;
import com.dl.playfun.entity.TokenEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.event.BindAccountPhotoEvent;
import com.dl.playfun.event.ItemChooseAreaEvent;
import com.dl.playfun.ui.login.choose.ChooseAreaFragment;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.viewmodel.BaseViewModel;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.RxSubscriptions;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Author: 彭石林
 * Time: 2022/10/14 10:36
 * Description: This is CommunityAccountViewModel
 */
public class CommunityAccountViewModel extends BaseViewModel<AppRepository> {

    public CommunityAccountViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
    }
    private boolean isDownTime = false;
    public ObservableField<String> downTimeStr = new ObservableField<>(StringUtils.getString(R.string.playfun_send_code));
    public ObservableField<String> mobile = new ObservableField<>();
    public ObservableField<ChooseAreaItemEntity> areaCode = new ObservableField<>();
    public ObservableField<String> code = new ObservableField<>();

    public SingleLiveEvent<String> getCodeSuccess = new SingleLiveEvent<>();
    public SingleLiveEvent<String> setAreaSuccess = new SingleLiveEvent<>();

    //选择地区
    public BindingCommand<Void> ChooseAreaView = new BindingCommand<>(()->{
        start(ChooseAreaFragment.class.getCanonicalName());
    });

    /**
     * 发送短信按钮点击事件
     */
    public BindingCommand<Void> sendRegisterSmsOnClickCommand = new BindingCommand<>(this::reqVerifyCode);

    private Disposable ItemChooseAreaSubscription;

    /**
     * 注册按钮的点击事件
     */
    public BindingCommand<Void> registerUserOnClickCommand = new BindingCommand<>(() -> v2Login());

    public String getAreaPhoneCode(ChooseAreaItemEntity chooseAreaItem) {
        if (chooseAreaItem != null) {
            if (chooseAreaItem.getCode() != null) {
                return "+" + chooseAreaItem.getCode();
            }
        }
        return null;
    }

    //获取用户ip地址区号
    public void getUserIpCode() {
        SystemConfigEntity systemConfigEntity = model.readSystemConfig();
        if (systemConfigEntity != null) {
            ChooseAreaItemEntity chooseAreaItemEntity = new ChooseAreaItemEntity();
            chooseAreaItemEntity.setCode(systemConfigEntity.getRegionCode());
            areaCode.set(chooseAreaItemEntity);
        }
    }

    private void reqVerifyCode() {
        if (TextUtils.isEmpty(mobile.get())) {
            ToastUtils.showShort(R.string.mobile_hint);
            return;
        }
        if (ObjectUtils.isEmpty(areaCode.get())) {
            ToastUtils.showShort(R.string.register_error_hint);
            return;
        }
        if (!isDownTime) {
            Map<String, String> mapData = new HashMap<String, String>();
            mapData.put("regionCode", areaCode.get().getCode());
            mapData.put("phone", mobile.get());
            model.verifyCodePost(ApiUitl.getBody(GsonUtils.toJson(mapData)))
                    .doOnSubscribe(this)
                    .compose(RxUtils.schedulersTransformer())
                    .compose(RxUtils.exceptionTransformer())
                    .doOnSubscribe(disposable -> showHUD())
                    .subscribe(new BaseObserver<BaseResponse>() {
                        @Override
                        public void onSuccess(BaseResponse baseResponse) {
                            ToastUtils.showShort(R.string.code_sended);

                            /**
                             * 倒计时60秒，一次1秒
                             */
                            CountDownTimer timer = new CountDownTimer(60 * 1000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    isDownTime = true;
                                    if (millisUntilFinished / 1000 == 58){
                                        getCodeSuccess.call();
                                    }
                                    downTimeStr.set(StringUtils.getString(R.string.again_send) + "(" + millisUntilFinished / 1000 + "）");
                                }

                                @Override
                                public void onFinish() {
                                    isDownTime = false;
                                    downTimeStr.set(StringUtils.getString(R.string.again_send));
                                }
                            }.start();

                        }

                        @Override
                        public void onComplete() {
                            super.onComplete();
                            dismissHUD();
                        }
                    });
        }
    }

    /**
     * 手机号码直接登录
     */
    private void v2Login() {
        if (ObjectUtils.isEmpty(areaCode.get())) {
            ToastUtils.showShort(R.string.register_error_hint);
            return;
        }
        if (TextUtils.isEmpty(mobile.get())) {
            ToastUtils.showShort(StringUtils.getString(R.string.mobile_hint));
            return;
        }
        if (TextUtils.isEmpty(code.get())) {
            ToastUtils.showShort(R.string.playfun_please_input_code);
            return;
        }
        Map<String, Object> mapData = new HashMap<>();
        mapData.put("type","phone");
        //当type为phone时，该字段必填，手机号码
        mapData.put("phone", mobile.get());
        //	当type为phone时，该字段必填，验证码
        mapData.put("code", code.get());
        mapData.put("region_code", areaCode.get().getCode());
        mapData.put("AndroidDeviceInfo", MPDeviceUtils.getDeviceInfo());
        model.bindAccount(ApiUitl.getBody(GsonUtils.toJson(mapData)))
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {
                        dismissHUD();
                        ToastUtils.showShort(R.string.playfun_binding_auth_success);
                        UserDataEntity userDataEntity = model.readUserData();
                        userDataEntity.setBindPhone(1);
                        model.saveUserData(userDataEntity);
                        RxBus.getDefault().post(new BindAccountPhotoEvent(mobile.get()));
                        pop();
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }
    @Override
    public void registerRxBus() {
        super.registerRxBus();
        ItemChooseAreaSubscription = RxBus.getDefault().toObservable(ItemChooseAreaEvent.class)
                .subscribe(event -> {
                    if (event.getChooseAreaItemEntity() != null) {
                        areaCode.set(event.getChooseAreaItemEntity());
                    }
                    setAreaSuccess.call();
                });
        RxSubscriptions.add(ItemChooseAreaSubscription);
    }

    @Override
    public void removeRxBus() {
        super.removeRxBus();
        RxSubscriptions.remove(ItemChooseAreaSubscription);
    }
}
