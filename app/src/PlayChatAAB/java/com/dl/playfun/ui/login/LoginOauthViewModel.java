package com.dl.playfun.ui.login;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.appsflyer.AppsFlyerLib;
import com.blankj.utilcode.util.GsonUtils;
import com.dl.lib.util.MPDeviceUtils;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.ElkLogEventReport;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.TokenEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.ui.main.MainFragment;
import com.dl.playfun.ui.mine.webdetail.WebDetailFragment;
import com.dl.playfun.ui.splash.SplashFragment;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.HashMap;
import java.util.Map;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * Author: 彭石林
 * Time: 2022/10/12 12:16
 * Description: This is LoginOauthViewModel
 */
public class LoginOauthViewModel extends BaseViewModel<AppRepository> {

    public ObservableField<Boolean> agree = new ObservableField<>(true);
    public ObservableField<UserDataEntity> currentUserData = new ObservableField<>();

    public LoginOauthViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
    }

    public void initData() {
        UserDataEntity userDataEntity = model.readOldUserData();
        if(userDataEntity!=null){
            currentUserData.set(userDataEntity);
        }
    }

    /**
     * 服務條款
     */
    public BindingCommand<Void> termsOfServiceOnClickCommand = new BindingCommand(() -> {
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
    /**
    * @Desc TODO(跳转登录页面)
    * @Date 2022/10/12
    */
    public BindingCommand<Void> clickOtherLoginView = new BindingCommand<>(() -> {
        startWithPop(LoginFragment.class.getCanonicalName());
        //startWithPopTo(LoginFragment.class.getCanonicalName(), LoginOauthFragment.class.getCanonicalName(), true);
    });

    public BindingCommand<Void> clickLoginOnClickCommand = new BindingCommand<>(this::tokenLogin);

    private void tokenLogin(){
        ElkLogEventReport.reportLoginModule.reportClickLoginPage(ElkLogEventReport._click,"oneClickLogin");
        Map<String,Object> mapData = new HashMap<>();
        mapData.put("AndroidDeviceInfo", MPDeviceUtils.getDeviceInfo());
        model.oldUserTokenLogin(ApiUitl.getBody(GsonUtils.toJson(mapData)))
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<UserDataEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<UserDataEntity> response) {
                        UserDataEntity authLoginUserEntity = response.getData();
                        TokenEntity tokenEntity = new TokenEntity(authLoginUserEntity.getToken(),authLoginUserEntity.getUserID(),authLoginUserEntity.getUserSig(), authLoginUserEntity.getIsContract());
                        model.saveLoginInfo(tokenEntity);

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
                        model.saveUserData(authLoginUserEntity);
                        if (authLoginUserEntity.getCertification() == 1) {
                            model.saveNeedVerifyFace(true);
                        }
                        dismissHUD();
                        AppConfig.userClickOut = false;
                        startWithPopTo(MainFragment.class.getCanonicalName(), LoginOauthFragment.class.getCanonicalName(), true);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        dismissHUD();
                    }
                });
    }

}
