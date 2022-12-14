package com.dl.playfun.ui.mine.setting;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.api.AppGameConfig;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.PrivacyEntity;
import com.dl.playfun.entity.UserInfoEntity;
import com.dl.playfun.event.IsAuthBindingEvent;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.manager.GlideCacheManager;
import com.dl.playfun.ui.mine.account.CommunityAccountFragment;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.dl.playfun.R;
import com.dl.playfun.ui.message.pushsetting.PushSettingFragment;
import com.dl.playfun.ui.mine.changepassword.ChangePasswordFragment;
import com.dl.playfun.ui.mine.creenlock.ScreenLockFragment;
import com.dl.playfun.ui.mine.webdetail.WebDetailFragment;

import io.reactivex.disposables.Disposable;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.RxSubscriptions;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.StringUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author wulei
 */
public class SettingViewModel extends BaseViewModel<AppRepository> {

    public ObservableField<PrivacyEntity> privacyEntity = new ObservableField<>(new PrivacyEntity());
    public ObservableField<String> cacheSize = new ObservableField<>();
    public ObservableField<Integer> isAuth = new ObservableField<>(0);
    //服务条款
    public ObservableBoolean showUrl = new ObservableBoolean(false);
    //隐私政策
    public ObservableBoolean showUrl2 = new ObservableBoolean(false);
    public BindingCommand isConnectionOnClickCommand = new BindingCommand(() -> {
        setConnectPrivacy();
    });
    //绑定社群账号
    public BindingCommand bindingCommunityAccount = new BindingCommand(() ->{
        start(CommunityAccountFragment.class.getCanonicalName());
    });
    //推送设置按钮的点击事件
    public BindingCommand pushSettingOnClickCommand = new BindingCommand(() -> start(PushSettingFragment.class.getCanonicalName()));
    //修改密码按钮的点击事件
    public BindingCommand changePasswordOnClickCommand = new BindingCommand(() -> start(ChangePasswordFragment.class.getCanonicalName()));
    //隐私设置按钮的点击事件
    public BindingCommand clearCacheOnClickCommand = new BindingCommand(() -> {
        GlideCacheManager.getInstance().clearImageDiskCache(getApplication());
        cacheSize.set("0.00KB");
        ToastUtils.showShort(R.string.playfun_cleared_image_cache);
    });
    public BindingCommand termsOfServiceOnClickCommand = new BindingCommand(() -> {
        Bundle bundle = WebDetailFragment.getStartBundle(model.readGameConfigSetting().getTermsOfServiceUrl());
        start(WebDetailFragment.class.getCanonicalName(), bundle);
    });
    public BindingCommand privacyPolicyPasswordOnClickCommand = new BindingCommand(() -> {
        Bundle bundle = WebDetailFragment.getStartBundle(model.readGameConfigSetting().getPrivacyPolicyUrl());
        start(WebDetailFragment.class.getCanonicalName(), bundle);
    });
    public BindingCommand settintAppLockOnClickCommand = new BindingCommand(() -> {
        start(ScreenLockFragment.class.getCanonicalName());
    });
    UIChangeObservable uc = new UIChangeObservable();
    //绑定手机号按钮的点击事件
    public BindingCommand bindMobileOnClickCommand = new BindingCommand(() -> uc.bindMobile.call());
    //退出登录
    public BindingCommand logoutOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            uc.clickLogout.call();
        }
    });
    private Disposable isAuthChangeSubscription;

    public SettingViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String strCacheSize = GlideCacheManager.getInstance().getCacheSize(getApplication());
        cacheSize.set(strCacheSize);
        AppGameConfig appGameConfig = ConfigManager.getInstance().getAppRepository().readGameConfigSetting();
        if(!ObjectUtils.isEmpty(appGameConfig)){
            if(!StringUtils.isEmpty(appGameConfig.getTermsOfServiceUrl())){
                showUrl.set(true);
            }
            if(!StringUtils.isEmpty(appGameConfig.getPrivacyPolicyUrl())){
                showUrl2.set(true);
            }
        }
    }

    @Override
    public void onEnterAnimationEnd() {
        super.onEnterAnimationEnd();
        getPrivacy();

    }

    /**
     * 获取我的隐私
     */
    private void getPrivacy() {
        model.getPrivacy()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<PrivacyEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<PrivacyEntity> response) {
                        privacyEntity.set(response.getData());
                    }
                });
    }

    /**
     * 设置我的隐私
     */
    private void setConnectPrivacy() {
        PrivacyEntity entity = new PrivacyEntity();
        entity.setConnection(privacyEntity.get().getConnection());
        model.setPrivacy(entity)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public void logout() {
        //友盟用户统计
        // MobclickAgent.onProfileSignOff();
        AppConfig.userClickOut = true;
        model.logout();
    }

    public void loadUserInfo() {
        model.getUserInfo()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> {
                    showHUD();
                })
                .subscribe(new BaseObserver<BaseDataResponse<UserInfoEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<UserInfoEntity> response) {
                        isAuth.set(response.getData().getIsAuth());
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                        super.onComplete();
                    }
                });
    }

    @Override
    public void registerRxBus() {
        super.registerRxBus();
        //第三方登录后接受绑定通知
        isAuthChangeSubscription = RxBus.getDefault().toObservable(IsAuthBindingEvent.class)
                .subscribe(event -> {
                    isAuth.set(1);
                });
    }

    @Override
    public void removeRxBus() {
        super.removeRxBus();
        RxSubscriptions.remove(isAuthChangeSubscription);
    }

    public class UIChangeObservable {
        public SingleLiveEvent<Void> bindMobile = new SingleLiveEvent<>();
        public SingleLiveEvent<Void> clickLogout = new SingleLiveEvent<>();
        public SingleLiveEvent<Void> clickClearCache = new SingleLiveEvent<>();
    }

}