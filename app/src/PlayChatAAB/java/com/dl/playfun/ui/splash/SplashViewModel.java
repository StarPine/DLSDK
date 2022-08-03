package com.dl.playfun.ui.splash;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;

import com.appsflyer.AppsFlyerLib;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.EaringlSwitchUtil;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseDisposableObserver;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.AllConfigEntity;
import com.dl.playfun.entity.ApiConfigManagerEntity;
import com.dl.playfun.entity.CityAllEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.ui.login.LoginFragment;
import com.dl.playfun.ui.main.MainFragment;
import com.dl.playfun.utils.ExceptionReportUtils;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
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
                                model.saveApiConfigManager(apiConfigManager);
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
        //RaJava模拟登录
        model.getUserData()
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .subscribe(new BaseObserver<BaseDataResponse<UserDataEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<UserDataEntity> response) {
                        UserDataEntity userDataEntity = response.getData();
                        model.saveUserData(userDataEntity);
                        AppsFlyerLib.getInstance().setCustomerUserId(String.valueOf(userDataEntity.getId()));
                        AppContext.instance().mFirebaseAnalytics.setUserId(String.valueOf(userDataEntity.getId()));
                        try {
                            //添加崩溃人员id
                            FirebaseCrashlytics.getInstance().setUserId(String.valueOf(userDataEntity.getId()));
                        } catch (Exception ignored) {

                        }
                        initIM();
                        AppContext.instance().logEvent(AppsFlyerEvent.Silent_login);
                        startWithPop(MainFragment.class.getCanonicalName());
                    }

                    @Override
                    public void onError(RequestException e) {
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
                .subscribe(new BaseDisposableObserver<BaseDataResponse<AllConfigEntity>>() {

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }

                    @Override
                    public void onSuccess(BaseDataResponse<AllConfigEntity> response) {
                        try {
                            model.saveHeightConfig(response.getData().getHeight());
                            model.saveWeightConfig(response.getData().getWeight());
                            model.saveReportReasonConfig(response.getData().getReportReason());
                            model.saveFemaleEvaluateConfig(response.getData().getEvaluate().getEvaluateFemale());
                            model.saveMaleEvaluateConfig(response.getData().getEvaluate().getEvaluateMale());
                            model.saveHopeObjectConfig(response.getData().getHopeObject());
                            model.saveOccupationConfig(response.getData().getOccupation());
                            model.saveCityConfig(response.getData().getCity());
                            model.saveSystemConfig(response.getData().getConfig());
                            model.saveSystemConfigTask(response.getData().getTask());
                            model.saveDefaultHomePageConfig(response.getData().getDefaultHomePage());
                            model.saveGameConfig(response.getData().getGame());
                            model.saveCrystalDetailsConfig(response.getData().getCrystalDetailsConfig());
                            model.putSwitches(EaringlSwitchUtil.KEY_TIPS, response.getData().getIsTips());
                        } catch (Exception e) {
                            ExceptionReportUtils.report(e);
                        }
                        initCityConfigAll();
                    }

                    @Override
                    public void onError(RequestException e) {
                        e.printStackTrace();
                        hintRetryShow.set(true);
                    }

                });
    }

    public void initCityConfigAll() {
        model.getCityConfigAll()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<CityAllEntity>>() {
                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }

                    @Override
                    public void onSuccess(BaseDataResponse<CityAllEntity> response) {
                        CityAllEntity cityData = response.getData();
                        if (ObjectUtils.isNotEmpty(cityData.getCityAll())) {
                            model.saveCityConfigAll(cityData.getCityAll());
                            initData();
                        } else {
                            hintRetryShow.set(true);
                        }
                    }

                    @Override
                    public void onError(RequestException e) {
                        e.printStackTrace();
                        hintRetryShow.set(true);
                    }

                });
    }
}
