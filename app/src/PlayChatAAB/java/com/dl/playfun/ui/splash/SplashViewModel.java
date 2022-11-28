package com.dl.playfun.ui.splash;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;

import com.appsflyer.AppsFlyerLib;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.EaringlSwitchUtil;
import com.dl.playfun.app.ElkLogEventReport;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.local.LocalDataSourceImpl;
import com.dl.playfun.entity.AllConfigEntity;
import com.dl.playfun.entity.ApiConfigManagerEntity;
import com.dl.playfun.entity.TokenEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.event.LoginExpiredEvent;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.login.LoginFragment;
import com.dl.playfun.ui.login.LoginOauthFragment;
import com.dl.playfun.ui.main.MainFragment;
import com.dl.playfun.utils.ElkLogEventUtils;
import com.dl.playfun.utils.ExceptionReportUtils;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.dl.rtc.calling.manager.DLRTCStartManager;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.tencent.qcloud.tuicore.TUILogin;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * @author wulei
 */
public class SplashViewModel extends BaseViewModel<AppRepository> {

    public ObservableBoolean hintRetryShow = new ObservableBoolean(false);

    public SplashViewModel(@NonNull Application application, AppRepository appRepository) {
        super(application, appRepository);
    }

    public BindingCommand RetryCLick = new BindingCommand(this::initApiConfig);

    @Override
    public void onEnterAnimationEnd() {
        super.onEnterAnimationEnd();
        initApiConfig();
    }
    private void initData() {
        UserDataEntity oldUserData = model.readOldUserData();
        //上次登录信息不为空。进入上次登录信息页面
        if(oldUserData!=null && oldUserData.getSex() != null && oldUserData.getSex() != -1 && !StringUtil.isEmpty(oldUserData.getNickname())){
            Observable.just("")
                    .delay(300, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    // Be notified on the main thread
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> {
                        initIM();
                        startWithPopTo(LoginOauthFragment.class.getCanonicalName(), SplashFragment.class.getCanonicalName(), true);
                    });
            return;
        }
        if (model.readLoginInfo() != null && !StringUtils.isEmpty(model.readLoginInfo().getToken()) && model.readUserData() != null && model.readUserData().getSex() != null && model.readUserData().getSex() != -1 && !StringUtil.isEmpty(model.readUserData().getNickname())) {
            loadProfile();
        } else {
            Observable.just("")
                    .delay(300, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    // Be notified on the main thread
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> {
                        initIM();
                        startWithPopTo(LoginFragment.class.getCanonicalName(), SplashFragment.class.getCanonicalName(), true);
                    });
        }
    }
    /**
     * @Desc TODO(初始化获取api配置)
     * @author 彭石林
     * @parame []
     * @return void
     * @Date 2022/7/2
     */
    private void initApiConfig () {
        model.initApiConfig()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<ApiConfigManagerEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<ApiConfigManagerEntity> response) {
                        if (!response.isDataEmpty()) {
                            ApiConfigManagerEntity apiConfigManager = response.getData();
                            if (apiConfigManager != null) {
                                AppConfig.CHAT_SERVICE_USER_ID = apiConfigManager.getCustomerId();
                                model.saveApiConfigManager(apiConfigManager);
                                model.putKeyValue(LocalDataSourceImpl.KEY_ELK_URL_DATA,apiConfigManager.getAppLoggerUrl());
                                initSettingConfig();
                            } else {
                                hintRetryShow.set(true);
                            }
                        } else {
                            hintRetryShow.set(true);
                        }
                    }

                    @Override
                    public void onError(RequestException e) {
                        super.onError(e);
                        hintRetryShow.set(true);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    private void initIM(){
        AppContext appContext = ((AppContext)getApplication());
        appContext.initIM();
        appContext.initActivityLifecycleCallbacks();
    }

    /**
     * 加载用户资料
     */
    private void loadProfile () {
        ElkLogEventReport.reportLoginModule.reportLogin(null,"silentLogin", ConfigManager.getInstance().getLoginSource());
        //RaJava模拟登录
        model.getUserData()
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .subscribe(new BaseObserver<BaseDataResponse<UserDataEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<UserDataEntity> response) {
                        UserDataEntity cacheUserData = model.readUserData();
                        UserDataEntity userDataEntity = response.getData();
                        userDataEntity.setToken(cacheUserData.getToken());
                        model.saveUserData(userDataEntity);
                        //更新IM userSig
                        if (!TextUtils.isEmpty(userDataEntity.getUserSig())) {
                            TokenEntity tokenEntity = model.readLoginInfo();
                            if (tokenEntity == null){
                                RxBus.getDefault().post(new LoginExpiredEvent());
                                return;
                            }
                            tokenEntity.setUserSig(userDataEntity.getUserSig());
                            model.saveLoginInfo(tokenEntity);
                        }
                        AppsFlyerLib.getInstance().setCustomerUserId(String.valueOf(userDataEntity.getId()));
                        AppContext.instance().mFirebaseAnalytics.setUserId(String.valueOf(userDataEntity.getId()));
                        try {
                            //添加崩溃人员id
                            FirebaseCrashlytics.getInstance().setUserId(String.valueOf(userDataEntity.getId()));
                        } catch (Exception crashErr){
                            Log.e("Crashlytics setUserid ",crashErr.getMessage());
                        }
                        initIM();
                        AppContext.instance().logEvent(AppsFlyerEvent.Silent_login);
                        DLRTCStartManager.Companion.getInstance().setLoginSuccessUser(userDataEntity.getImUserId());
                        startWithPop(MainFragment.class.getCanonicalName());
                    }

                    @Override
                    public void onError(RequestException e) {
                        if(e.getCode()==10100){
                            ElkLogEventReport.reportLoginModule.reportLogin(null,"loginExpired",null);
                        }
                        initIM();
                        if (model.readUserData() != null) {
                            startWithPop(MainFragment.class.getCanonicalName());
                        } else {
                            startWithPop(LoginFragment.class.getCanonicalName());
                        }
                    }
                });
    }

    /**
     * @Desc TODO(初始化执行获取系统配置)
     * @author 彭石林
     * @parame [initSettingConfigListener]
     * @return boolean
     * @Date 2022/5/16
     */
    public void initSettingConfig () {
        model.getAllConfig()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<AllConfigEntity>>() {

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }

                    @Override
                    public void onSuccess(BaseDataResponse<AllConfigEntity> response) {
                        try {
                            AllConfigEntity allConfigEntity = response.getData();
                            model.putKeyValue(ElkLogEventUtils.UserIsNew,allConfigEntity.getDeviceUseType()+"");
                            model.saveHeightConfig(allConfigEntity.getHeight());
                            model.saveWeightConfig(allConfigEntity.getWeight());
                            model.saveReportReasonConfig(allConfigEntity.getReportReason());
                            model.saveFemaleEvaluateConfig(allConfigEntity.getEvaluate().getEvaluateFemale());
                            model.saveMaleEvaluateConfig(allConfigEntity.getEvaluate().getEvaluateMale());
                            model.saveHopeObjectConfig(allConfigEntity.getHopeObject());
                            model.saveOccupationConfig(allConfigEntity.getOccupation());
                            model.saveCityConfig(allConfigEntity.getCity());
                            model.saveSystemConfig(allConfigEntity.getConfig());
                            model.saveSystemConfigTask(allConfigEntity.getTask());
                            model.saveDefaultHomePageConfig(allConfigEntity.getDefaultHomePage());
                            model.saveGameConfig(allConfigEntity.getGame());
                            model.saveCrystalDetailsConfig(allConfigEntity.getCrystalDetailsConfig());
                            model.putSwitches(EaringlSwitchUtil.KEY_TIPS, allConfigEntity.getIsTips());
                            model.putSwitches(EaringlSwitchUtil.KEY_DELETE_ACCOUNT, allConfigEntity.getConfig().getDeleteAccount());
                            model.putSwitches(EaringlSwitchUtil.KEY_SQUARE_DISLIKE,allConfigEntity.getSquareDislike());
                        } catch (Exception e) {
                            ExceptionReportUtils.report(e);
                        }
                        initData();
                    }

                    @Override
                    public void onError(RequestException t) {
                        super.onError(t);
                        hintRetryShow.set(true);
                    }
                });
    }
}
