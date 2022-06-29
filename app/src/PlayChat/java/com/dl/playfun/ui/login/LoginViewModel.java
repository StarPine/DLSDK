package com.dl.playfun.ui.login;

import android.app.Application;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.appsflyer.AppsFlyerLib;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.TokenEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.manager.ThirdPushTokenMgr;
import com.dl.playfun.ui.login.register.RegisterFragment;
import com.dl.playfun.ui.main.MainFragment;
import com.dl.playfun.ui.mine.profile.PerfectProfileFragment;
import com.dl.playfun.ui.mine.webdetail.WebDetailFragment;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Map;

import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.utils.KLog;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;


/**
 * @author wulei
 */
public class LoginViewModel extends BaseViewModel<AppRepository>  {

    public ObservableField<String> mobile = new ObservableField<>();
    public ObservableField<String> password = new ObservableField<>();
    public ObservableField<String> code = new ObservableField<>();
    public ObservableField<Boolean> agree = new ObservableField<>(true);
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
     * l
     * 网络模拟一个登陆操作
     **/
    private void mobileLogin() {
        if (TextUtils.isEmpty(mobile.get())) {
            ToastUtils.showShort(StringUtils.getString(R.string.mobile_hint));
            return;
        }
        if (TextUtils.isEmpty(password.get())) {
            ToastUtils.showShort(R.string.playfun_mine_setting_account_pwd_hint1);
            return;
        } else if (password.get().length() < 8) {
            ToastUtils.showShort(R.string.playfun_please_input_password_min_len);
            return;
        }

        model.login(mobile.get(), password.get())
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<TokenEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<TokenEntity> response) {
                        dismissHUD();
                        model.saveLoginInfo(response.getData());
                        loadProfile();
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    /**
     * 手机号码直接登录
     */
    private void v2Login() {
        if (TextUtils.isEmpty(mobile.get())) {
            ToastUtils.showShort(StringUtils.getString(R.string.mobile_hint));
            return;
        }
        if (TextUtils.isEmpty(code.get())) {
            ToastUtils.showShort(R.string.playfun_please_input_code);
            return;
        }

        model.v2Login(mobile.get(), code.get(), ApiUitl.getAndroidId())
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
                        if (response.getData() != null && response.getData().getIsNewUser() != null && response.getData().getIsNewUser().intValue() == 1) {
                            AppContext.instance().logEvent(AppsFlyerEvent.register_start);
                            model.saveIsNewUser(true);
                        }
                        AppContext.instance().logEvent(AppsFlyerEvent.LOG_IN_WITH_PHONE_NUMBER);
                        loadProfile();
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
        start(RegisterFragment.class.getCanonicalName());
    }

    private void userRegister() {
        if (TextUtils.isEmpty(mobile.get())) {
            ToastUtils.showShort(StringUtils.getString(R.string.mobile_hint));
            return;
        }
        if (TextUtils.isEmpty(code.get())) {
            ToastUtils.showShort(R.string.playfun_please_input_code);
            return;
        }
        if (TextUtils.isEmpty(password.get())) {
            ToastUtils.showShort(R.string.playfun_mine_setting_account_pwd_hint1);
            return;
        } else if (password.get().length() < 8) {
            ToastUtils.showShort(R.string.playfun_please_input_password_min_len);
            return;
        }
        if (!agree.get()) {
            ToastUtils.showShort(R.string.playfun_warn_agree_terms);
            return;
        }
        model.register(mobile.get(), password.get(), code.get())
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<TokenEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<TokenEntity> response) {
                        dismissHUD();
                        model.saveLoginInfo(response.getData());
                        model.saveUserData(new UserDataEntity());
                        start(PerfectProfileFragment.class.getCanonicalName());
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
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
        id += type;
        model.authLoginPost(id, type, email, avatar, nickname, ApiUitl.getAndroidId(), business)
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
                        dismissHUD();
                        UserDataEntity userDataEntity = response.getData();
                        //友盟登录统计
                        // MobclickAgent.onProfileSignIn(String.valueOf(userDataEntity.getId()));
                        AppsFlyerLib.getInstance().setCustomerUserId(String.valueOf(userDataEntity.getId()));
                        AppContext.instance().mFirebaseAnalytics.setUserId(String.valueOf(userDataEntity.getId()));
                        model.saveUserData(userDataEntity);
                        if (userDataEntity.getCertification() == 1) {
                            model.saveNeedVerifyFace(true);
                        }
                        dismissHUD();
                        AppConfig.userClickOut = false;
                        if (userDataEntity.getSex() != null && userDataEntity.getSex() >= 0 && !StringUtil.isEmpty(userDataEntity.getNickname()) && !StringUtil.isEmpty(userDataEntity.getBirthday()) && !StringUtil.isEmpty(userDataEntity.getAvatar())) {
                            startWithPopTo(MainFragment.class.getCanonicalName(), LoginFragment.class.getCanonicalName(), true);
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

    private void reqVerifyCode() {
        if (TextUtils.isEmpty(mobile.get())) {
            ToastUtils.showShort(R.string.mobile_hint);
            return;
        }
        if (!isDownTime) {
            model.verifyCodePost(mobile.get())
                    .doOnSubscribe(this)
                    .compose(RxUtils.schedulersTransformer())
                    .compose(RxUtils.exceptionTransformer())
                    .doOnSubscribe(disposable -> showHUD())
                    .subscribe(new BaseObserver<BaseResponse>() {
                        @Override
                        public void onSuccess(BaseResponse baseResponse) {
                            dismissHUD();
                            ToastUtils.showShort(R.string.code_sended);
                            /**
                             * 倒计时60秒，一次1秒
                             */
                            CountDownTimer timer = new CountDownTimer(60 * 1000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    isDownTime = true;
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
}