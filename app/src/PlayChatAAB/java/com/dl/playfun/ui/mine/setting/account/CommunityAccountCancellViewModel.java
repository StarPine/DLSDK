package com.dl.playfun.ui.mine.setting.account;

import android.app.Application;

import androidx.annotation.NonNull;

import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.ui.login.LoginFragment;
import com.dl.playfun.ui.splash.SplashFragment;
import com.dl.playfun.viewmodel.BaseViewModel;

import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;

public class CommunityAccountCancellViewModel extends BaseViewModel<AppRepository> {
    public SingleLiveEvent<Boolean> cancellType = new SingleLiveEvent();

    public CommunityAccountCancellViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
    }

    /**
     * 注销账号
     *
     * @return
     */
    public void cancellation() {
        model.cancellation()
                .doOnSubscribe(this)
                .compose(RxUtils.exceptionTransformer())
                .compose(RxUtils.schedulersTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {
                        dismissHUD();
                        cancellType.setValue(true);
                    }

                    @Override
                    public void onError(RequestException e) {
                        super.onError(e);
                        dismissHUD();
                        if (e.getCode() == 10101) {
                            cancellType.setValue(false);
                        }
                    }
                });
    }

    public void startLoginView() {
        model.logout();
        //跳转到登录界面
        startWithPopTo(LoginFragment.class.getCanonicalName(), CommunityAccountCancellFragment.class.getCanonicalName(), true);
    }

}
