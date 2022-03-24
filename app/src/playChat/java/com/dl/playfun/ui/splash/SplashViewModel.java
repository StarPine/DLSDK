package com.dl.playfun.ui.splash;

import android.app.Application;

import androidx.annotation.NonNull;

import com.appsflyer.AppsFlyerLib;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.ui.login.login.LoginFragment;
import com.dl.playfun.ui.main.MainFragment;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.viewmodel.BaseViewModel;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * @author wulei
 */
public class SplashViewModel extends BaseViewModel<AppRepository> {

    public SplashViewModel(@NonNull Application application, AppRepository appRepository) {
        super(application, appRepository);
    }

    @Override
    public void onEnterAnimationEnd() {
        super.onEnterAnimationEnd();
        if (model.readLoginInfo() != null && !StringUtils.isEmpty(model.readLoginInfo().getToken()) && model.readUserData() != null && model.readUserData().getSex() != null && model.readUserData().getSex() != -1 && !StringUtil.isEmpty(model.readUserData().getNickname())) {
            loadProfile();
        } else {
            Observable.just("")
                    .delay(1500, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    // Be notified on the main thread
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> {
//                        if (model.readIsFrist()){
//                            startWithPopTo(GuideFragment.class.getCanonicalName(), SplashFragment.class.getCanonicalName(), true);
//                            model.saveIsFrist(false);
//                        }else {
                        startWithPopTo(LoginFragment.class.getCanonicalName(), SplashFragment.class.getCanonicalName(), true);
                        //}
                    });
        }
    }

    /**
     * 加载用户资料
     */
    private void loadProfile() {
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
                        AppContext.instance().logEvent(AppsFlyerEvent.Silent_login);
                        startWithPop(MainFragment.class.getCanonicalName());
                    }

                    @Override
                    public void onError(RequestException e) {
                        if (model.readUserData() != null) {
                            startWithPop(MainFragment.class.getCanonicalName());
                        } else {
                            startWithPop(LoginFragment.class.getCanonicalName());
                        }
                    }
                });
    }

}
