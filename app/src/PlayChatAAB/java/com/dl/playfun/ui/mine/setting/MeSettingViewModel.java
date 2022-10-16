package com.dl.playfun.ui.mine.setting;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.AppUtils;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.VersionEntity;
import com.dl.playfun.manager.GlideCacheManager;
import com.dl.playfun.ui.login.LoginOauthFragment;
import com.dl.playfun.ui.main.MainFragment;
import com.dl.playfun.ui.message.pushsetting.PushSettingFragment;
import com.dl.playfun.ui.mine.blacklist.BlacklistFragment;
import com.dl.playfun.ui.mine.language.LanguageSwitchActivity;
import com.dl.playfun.ui.mine.privacysetting.PrivacySettingFragment;
import com.dl.playfun.ui.mine.setting.account.CommunityAccountFragment;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.tencent.qcloud.tuicore.Status;

import org.jetbrains.annotations.NotNull;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Author: 彭石林
 * Time: 2021/10/25 14:38
 * Description: This is MeSettingViewModel
 */
public class MeSettingViewModel extends BaseViewModel<AppRepository> {

   public UIChangeObservable uc = new UIChangeObservable();

    public ObservableField<String> currentVersion = new ObservableField<>();
    public ObservableField<String> cacheSize = new ObservableField<>();
    //绑定社群账号
    public BindingCommand bindingCommunityAccount = new BindingCommand(() ->{
        start(CommunityAccountFragment.class.getCanonicalName());
    });
    //美顏跳转
    public BindingCommand facebeauty = new BindingCommand(() -> {
        uc.starFacebeautyActivity.call();
    });
    //语言设定跳转
    public BindingCommand clickLanguageView = new BindingCommand(() -> {
        //拨打语音小窗口不允许打电话
        if (Status.mIsShowFloatWindow){
            ToastUtils.showShort(R.string.audio_in_call);
            return;
        }
        startActivity(LanguageSwitchActivity.class);
    });
    //推送设置按钮的点击事件
    public BindingCommand pushSettingOnClickCommand = new BindingCommand(() -> start(PushSettingFragment.class.getCanonicalName()));
    //黑名单按钮的点击事件
    public BindingCommand blacklistOnClickCommand = new BindingCommand(() -> {
        AppContext.instance().logEvent(AppsFlyerEvent.Blocked_List);
        start(BlacklistFragment.class.getCanonicalName());
    });
    //隐私设置按钮的点击事件
    public BindingCommand clearCacheOnClickCommand = new BindingCommand(() -> {
        GlideCacheManager.getInstance().clearImageDiskCache(getApplication());
        cacheSize.set("0.00KB");
        ToastUtils.showShort(R.string.playfun_cleared_image_cache);
    });
    //隐私设置按钮的点击事件
    public BindingCommand privacySettingOnClickCommand = new BindingCommand(() -> {
        AppContext.instance().logEvent(AppsFlyerEvent.Privacy_Settings);
        start(PrivacySettingFragment.class.getCanonicalName());
    });
    //当前版本按钮的点击事件
    public BindingCommand versionOnClickCommand = new BindingCommand(() -> {
        model.detectionVersion("Android").compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<VersionEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<VersionEntity> versionEntityBaseDataResponse) {
                        dismissHUD();
                        VersionEntity versionEntity = versionEntityBaseDataResponse.getData();
                        if (versionEntity != null) {
                            uc.versionEntitySingl.postValue(versionEntity);
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }
    );
    public MeSettingViewModel(@NonNull @NotNull Application application, AppRepository model) {
        super(application, model);
    }

    //退出登录
    public BindingCommand logoutOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            uc.clickLogout.call();
        }
    });

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        String appVersionName = AppUtils.getAppVersionName();
        currentVersion.set(appVersionName);
        String strCacheSize = GlideCacheManager.getInstance().getCacheSize(getApplication());
        cacheSize.set(strCacheSize);
    }

    public void logout() {
        //友盟用户统计
        // MobclickAgent.onProfileSignOff();
        AppConfig.userClickOut = true;
        model.logout();
        startWithPopTo(LoginOauthFragment.class.getCanonicalName(), MainFragment.class.getCanonicalName(), true);
    }

    public class UIChangeObservable {
        public SingleLiveEvent<VersionEntity> versionEntitySingl = new SingleLiveEvent<>();
        public SingleLiveEvent<Void> starFacebeautyActivity = new SingleLiveEvent<>();
        public SingleLiveEvent<Void> clickLogout = new SingleLiveEvent<>();
    }
}
