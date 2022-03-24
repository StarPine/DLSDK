package com.dl.playfun.ui.mine.bindmobile;

import android.app.Application;
import android.os.CountDownTimer;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.appsflyer.AppsFlyerLib;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.TokenEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.manager.ThirdPushTokenMgr;
import com.dl.playfun.ui.login.login.LoginFragment;
import com.dl.playfun.ui.login.viewmodel.LoginViewModel;
import com.dl.playfun.ui.main.MainFragment;
import com.dl.playfun.ui.mine.choosesex.ChooseSexFragment;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.google.firebase.messaging.FirebaseMessaging;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.utils.KLog;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author wulei
 */
public class BindMobileViewModel extends BaseViewModel<AppRepository> {

    public ObservableField<String> mobile = new ObservableField<>("");
    public ObservableField<String> code = new ObservableField<>("");
    public ObservableField<String> password = new ObservableField<>("");
    //确定
    public BindingCommand confirmOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            updatePhone(mobile.get(), Integer.valueOf(code.get()), password.get());
        }
    });
    public ObservableField<String> downTimeStr = new ObservableField<>(StringUtils.getString(R.string.send_code));
    private boolean isDownTime = false;
    /**
     * 发送短信按钮点击事件
     */
    public BindingCommand sendSmsOnClickCommand = new BindingCommand(() -> reqVerifyCode());


    public BindMobileViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
    }

    /**
     * 修改手机号
     */
    private void updatePhone(String phone, int code, String password) {
        model.updatePhone(phone, code, password)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        model.login(phone, password)
                                .doOnSubscribe(BindMobileViewModel.this)
                                .compose(RxUtils.schedulersTransformer())
                                .compose(RxUtils.exceptionTransformer())
                                .subscribe(new BaseObserver<BaseDataResponse<TokenEntity>>() {
                                    @Override
                                    public void onSuccess(BaseDataResponse<TokenEntity> response) {
                                        model.saveLoginInfo(response.getData());
                                        loadProfile();
                                    }

                                    @Override
                                    public void onComplete() {
                                        dismissHUD();
                                    }
                                });
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
                    .compose(RxUtils.schedulersTransformer())
                    .compose(RxUtils.exceptionTransformer())
                    .doOnSubscribe(this)
                    .doOnSubscribe(disposable -> showHUD())
                    .subscribe(new BaseObserver<BaseResponse>() {
                        @Override
                        public void onSuccess(BaseResponse response) {
                            dismissHUD();
                            /**
                             * 倒计时60秒，一次1秒
                             */
                            CountDownTimer timer = new CountDownTimer(60 * 1000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    isDownTime = true;
                                    downTimeStr.set(StringUtils.getString(R.string.again_send) + "（" + millisUntilFinished / 1000 + "）");
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
                        //MobclickAgent.onProfileSignIn(String.valueOf(userDataEntity.getId()));
                        AppsFlyerLib.getInstance().setCustomerUserId(String.valueOf(userDataEntity.getId()));
                        AppContext.instance().mFirebaseAnalytics.setUserId(String.valueOf(userDataEntity.getId()));
                        model.saveUserData(userDataEntity);
                        if (userDataEntity.getCertification() == 1) {
                            model.saveNeedVerifyFace(true);
                        }
                        if (!userDataEntity.isCompleteInfo()) {
                            dismissHUD();
                            if (userDataEntity.getSex() != null && userDataEntity.getSex() >= 0) {
                                startWithPopTo(MainFragment.class.getCanonicalName(), LoginFragment.class.getCanonicalName(), true);
                            } else {
                                start(ChooseSexFragment.class.getCanonicalName());
                            }
                        } else {
                            startWithPopTo(MainFragment.class.getCanonicalName(), LoginFragment.class.getCanonicalName(), true);
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }


}