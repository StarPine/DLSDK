package com.dl.playfun.ui.login;

import android.app.Application;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.appsflyer.AppsFlyerLib;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.lib.util.MPDeviceUtils;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.ElkLogEventReport;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.ChooseAreaItemEntity;
import com.dl.playfun.entity.SystemConfigEntity;
import com.dl.playfun.entity.TokenEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.event.ItemChooseAreaEvent;
import com.dl.playfun.manager.ThirdPushTokenMgr;
import com.dl.playfun.ui.login.choose.ChooseAreaFragment;
import com.dl.playfun.ui.login.register.RegisterFragment;
import com.dl.playfun.ui.main.MainFragment;
import com.dl.playfun.ui.mine.profile.PerfectProfileFragment;
import com.dl.playfun.ui.mine.webdetail.WebDetailFragment;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.RxSubscriptions;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.KLog;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;


/**
 * @author wulei
 */
public class LoginViewModel extends BaseViewModel<AppRepository>  {

    public ObservableField<String> mobile = new ObservableField<>();
    public ObservableField<ChooseAreaItemEntity> areaCode = new ObservableField<>();
    public ObservableField<String> code = new ObservableField<>();
    public ObservableField<Boolean> agree = new ObservableField<>(true);

    public SingleLiveEvent<String> getCodeSuccess = new SingleLiveEvent<>();
    public SingleLiveEvent<String> setAreaSuccess = new SingleLiveEvent<>();

    private Disposable ItemChooseAreaSubscription;
    //选择地区
    public BindingCommand ChooseAreaView = new BindingCommand(()->{
        start(ChooseAreaFragment.class.getCanonicalName());
    });
    /**
     * 注册按钮的点击事件
     */
    public BindingCommand registerOnClickCommand = new BindingCommand(() -> register());
    public BindingCommand mobileLoginBackOnClickCommand = new BindingCommand(() -> onBackPressed());
    /**
     * 注册按钮的点击事件
     */
    public BindingCommand registerUserOnClickCommand = new BindingCommand(() -> v2Login());
    /**
     * 服務條款
     */
    public BindingCommand termsOfServiceOnClickCommand = new BindingCommand(() -> {
        Bundle bundle = WebDetailFragment.getStartBundle(AppConfig.TERMS_OF_SERVICE_URL);
        start(WebDetailFragment.class.getCanonicalName(), bundle);
    });
    /**
     * 隱私政策
     */
    public BindingCommand usageSpecificationOnClickCommand = new BindingCommand(() -> {
        Bundle bundle = WebDetailFragment.getStartBundle(AppConfig.PRIVACY_POLICY_URL);
        start(WebDetailFragment.class.getCanonicalName(), bundle);
    });
    public ObservableField<String> downTimeStr = new ObservableField<>(StringUtils.getString(R.string.playfun_send_code));
    private boolean isDownTime = false;
    /**
     * 发送短信按钮点击事件
     */
    public BindingCommand sendRegisterSmsOnClickCommand = new BindingCommand(() -> reqVerifyCode());

    public LoginViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
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
        ElkLogEventReport.reportLoginModule.reportClickPhoneLogin(ElkLogEventReport._click,"phoneLoginNext");
        Map<String, Object> mapData = new HashMap<>();
        mapData.put("phone", mobile.get());
        mapData.put("code", code.get());
        mapData.put("device_code", ApiUitl.getAndroidId());
        mapData.put("region_code", areaCode.get().getCode());
        mapData.put("AndroidDeviceInfo", MPDeviceUtils.getDeviceInfo());
        model.v2Login(ApiUitl.getBody(GsonUtils.toJson(mapData)))
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<UserDataEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<UserDataEntity> response) {
                        dismissHUD();
                        UserDataEntity authLoginUserEntity = response.getData();
                        TokenEntity tokenEntity = new TokenEntity(authLoginUserEntity.getToken(),authLoginUserEntity.getUserID(),authLoginUserEntity.getUserSig(), authLoginUserEntity.getIsContract());
                        model.saveLoginInfo(tokenEntity);
                        model.putKeyValue("areaCode",new Gson().toJson(areaCode.get()));
                        model.putKeyValue(AppConfig.LOGIN_TYPE,"phone");
                        model.saveUserData(authLoginUserEntity);
                        if (response.getData() != null && response.getData().getIsNewUser() != null && response.getData().getIsNewUser().intValue() == 1) {
                            AppContext.instance().logEvent(AppsFlyerEvent.register_start);
                            model.saveIsNewUser(true);
                        }
                        AppContext.instance().logEvent(AppsFlyerEvent.LOG_IN_WITH_PHONE_NUMBER);
                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(task -> {
                                    if (!task.isSuccessful()) {
                                        KLog.w(LoginViewModel.class.getCanonicalName(), "getInstanceId failed exception = " + task.getException());
                                        return;
                                    }
                                    // Get new Instance ID token
                                    String token = task.getResult();
                                    KLog.d(LoginViewModel.class.getCanonicalName(), "google fcm getToken = " + token);
                                    ThirdPushTokenMgr.getInstance().setThirdPushToken(token);
                                    AppContext.instance().pushDeviceToken(token);
                                });
                        //友盟登录统计
                        // MobclickAgent.onProfileSignIn(String.valueOf(userDataEntity.getId()));
                        AppsFlyerLib.getInstance().setCustomerUserId(String.valueOf(authLoginUserEntity.getId()));
                        AppContext.instance().mFirebaseAnalytics.setUserId(String.valueOf(authLoginUserEntity.getId()));
                        try {
                            //添加崩溃人员id
                            FirebaseCrashlytics.getInstance().setUserId(String.valueOf(authLoginUserEntity.getId()));
                        }catch (Exception crashErr){
                            Log.e("Crashlytics setUserid ",crashErr.getMessage());
                        }
                        if (authLoginUserEntity.getCertification() == 1) {
                            model.saveNeedVerifyFace(true);
                        }
                        AppConfig.userClickOut = false;
                        if (authLoginUserEntity.getSex() != null && authLoginUserEntity.getSex() >= 0 && !StringUtil.isEmpty(authLoginUserEntity.getNickname()) && !StringUtil.isEmpty(authLoginUserEntity.getBirthday()) && !StringUtil.isEmpty(authLoginUserEntity.getAvatar())) {
                            popAllTo(new MainFragment());
                        } else {
                            start(PerfectProfileFragment.class.getCanonicalName());
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    private void register() {
        if (!agree.get()) {
            ToastUtils.showShort(R.string.playfun_warn_agree_terms);
            return;
        }
        ElkLogEventReport.reportLoginModule.reportClickLoginPage(ElkLogEventReport._click,"phone");
        start(RegisterFragment.class.getCanonicalName());
    }


    /**
     * 第三方登录
     *
     * @param id       唯一ID
     * @param type     类型 facebook/line
     * @param email    邮箱
     * @param avatar   头像
     * @param nickname 昵称
     */
    public void authLogin(String id, String type, String email, String avatar, String nickname, String business) {
        if (id == null || type == null) {
            ToastUtils.showShort(R.string.get_userdata_defail);
            return;
        }
        ElkLogEventReport.reportAuthModule.reportLoginAuth(type,email);
        id += type;
        Map<String, Object> mapData = new HashMap<>();
        mapData.put("id", id);
        mapData.put("type", type);
        mapData.put("email", email);
        mapData.put("avatar", avatar);
        mapData.put("nickname", nickname);
        mapData.put("device_code", ApiUitl.getAndroidId());
        mapData.put("business_token", business);
        mapData.put("AndroidDeviceInfo", MPDeviceUtils.getDeviceInfo());
        model.authLoginPost(ApiUitl.getBody(GsonUtils.toJson(mapData)))
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<Map<String, String>>>() {

                    @Override
                    public void onSuccess(BaseDataResponse<Map<String, String>> mapBaseDataResponse) {
                        dismissHUD();
                        Map<String, String> dataToken = mapBaseDataResponse.getData();
                        TokenEntity tokenEntity = new TokenEntity(dataToken.get("token"), dataToken.get("userID"), dataToken.get("userSig"), Integer.parseInt(dataToken.get("is_contract")));
                        if (mapBaseDataResponse.getData() != null && mapBaseDataResponse.getData().get("is_new_user") != null && mapBaseDataResponse.getData().get("is_new_user").equals("1")) {
                            AppContext.instance().logEvent(AppsFlyerEvent.register_start);
                        }
                        model.saveLoginInfo(tokenEntity);
                        model.putKeyValue(AppConfig.LOGIN_TYPE,type);
                        loadProfile();
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    /**
     * 加载用户资料
     */
    private void loadProfile() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        KLog.w(LoginViewModel.class.getCanonicalName(), "getInstanceId failed exception = " + task.getException());
                        return;
                    }
                    // Get new Instance ID token
                    String token = task.getResult();
                    KLog.d(LoginViewModel.class.getCanonicalName(), "google fcm getToken = " + token);
                    ThirdPushTokenMgr.getInstance().setThirdPushToken(token);
                    AppContext.instance().pushDeviceToken(token);
                });
        //RaJava模拟登录
        model.getUserData()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<UserDataEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<UserDataEntity> response) {
                        UserDataEntity authLoginUserEntity = response.getData();
                        if(ObjectUtils.isNotEmpty(authLoginUserEntity)){
                            model.saveUserData(authLoginUserEntity);
                            AppsFlyerLib.getInstance().setCustomerUserId(String.valueOf(authLoginUserEntity.getId()));
                            AppContext.instance().mFirebaseAnalytics.setUserId(String.valueOf(authLoginUserEntity.getId()));
                            try {
                                //添加崩溃人员id
                                FirebaseCrashlytics.getInstance().setUserId(String.valueOf(authLoginUserEntity.getId()));
                            }catch (Exception crashErr){
                                Log.e("Crashlytics setUserid ",crashErr.getMessage());
                            }
                            if (authLoginUserEntity.getCertification() == 1) {
                                model.saveNeedVerifyFace(true);
                            }
                            AppConfig.userClickOut = false;
                            if (authLoginUserEntity.getSex() != null && authLoginUserEntity.getSex() >= 0 && !StringUtil.isEmpty(authLoginUserEntity.getNickname()) && !StringUtil.isEmpty(authLoginUserEntity.getBirthday()) && !StringUtil.isEmpty(authLoginUserEntity.getAvatar())) {
                                popAllTo(new MainFragment());
                            } else {
                                start(PerfectProfileFragment.class.getCanonicalName());
                            }
                        }else{
                            ToastUtils.showShort(StringUtils.getString(R.string.error_unknown));
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
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
            ElkLogEventReport.reportLoginModule.reportClickPhoneLogin(ElkLogEventReport._click,"getCode");
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

    //获取用户ip地址区号
    public void getUserIpCode() {
        SystemConfigEntity systemConfigEntity = model.readSystemConfig();
        if (systemConfigEntity != null) {
            ChooseAreaItemEntity chooseAreaItemEntity = new ChooseAreaItemEntity();
            chooseAreaItemEntity.setCode(systemConfigEntity.getRegionCode());
            areaCode.set(chooseAreaItemEntity);
        }
    }

    public String getAreaPhoneCode(ChooseAreaItemEntity chooseAreaItem) {
        if (chooseAreaItem != null) {
            if (chooseAreaItem.getCode() != null) {
                return "+" + chooseAreaItem.getCode();
            }
        }
        return null;
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