package com.dl.playfun.ui.login.profile;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.appsflyer.AppsFlyerLib;
import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.CheckNicknameEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.manager.ThirdPushTokenMgr;
import com.dl.playfun.ui.login.login.LoginFragment;
import com.dl.playfun.ui.login.viewmodel.LoginViewModel;
import com.dl.playfun.ui.main.MainFragment;
import com.dl.playfun.utils.FileUploadUtils;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.KLog;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author wulei
 */
public class ProfileViewModel extends BaseViewModel<AppRepository> {

    public UIChangeObservable uc = new UIChangeObservable();

    public ObservableField<Integer> UserSex = new ObservableField<>();
    public ObservableField<String> UserName = new ObservableField<>();
    public ObservableField<String> UserBirthdays = new ObservableField<>("1995年01月01日");
    public ObservableField<String> UserBirthday = new ObservableField<>("1995-1-1");
    public ObservableField<String> UserPortrait = new ObservableField<>();
    //填写昵称界面-按钮点击
    public BindingCommand ConfirmName = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            uc.clickUserName.call();
        }
    });
    //填写生日界面-按钮点击
    public BindingCommand clickBirthdaySel = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
//            ToastUtils.showShort("选择城市");
            uc.clickBirthday.call();
        }
    });
    //填写生日界面-按钮点击
    public BindingCommand ConfirmBirthday = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            //uc.clickConfirmBirthday.call();
            String birthday = UserBirthday.get();
            if (StringUtil.isEmpty(birthday)) {
                ToastUtils.showShort(R.string.reg_user_name_birthday);
            } else {
                Bundle bundle = ChoosePortraitFragment.getStartBundle(UserSex.get(), UserName.get(), UserBirthday.get());
                start(ChoosePortraitFragment.class.getCanonicalName(), bundle);
            }
        }
    });
    //上传头像-按钮点击
    public BindingCommand clickPortait = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            uc.clickAvatar.call();
        }
    });
    //上传头像界面-按钮点击
    public BindingCommand ConfirmPortait = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            String avater = UserPortrait.get();
            if (StringUtil.isEmpty(avater)) {
                ToastUtils.showShort(R.string.reg_user_phone);
                return;
            }
            saveAvatar(avater);
        }
    });

    public ProfileViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);

    }

    @Override
    public void onEnterAnimationEnd() {
        super.onEnterAnimationEnd();
    }

    /**
     * 加载用户资料
     */
    public void loadProfile(boolean goMain) {
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
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<UserDataEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<UserDataEntity> response) {
                        dismissHUD();
                        UserDataEntity userDataEntity = response.getData();
                        model.saveUserData(userDataEntity);
                        AppsFlyerLib.getInstance().setCustomerUserId(String.valueOf(userDataEntity.getId()));
                        if (userDataEntity.getCertification() != null && userDataEntity.getCertification().intValue() == 1) {
                            model.saveNeedVerifyFace(true);
                        }
                        if (goMain) {
                            AppContext.instance().logEvent(AppsFlyerEvent.LOG_Edit_Profile);
                            ToastUtils.showShort(R.string.submit_success);
                            startWithPopTo(MainFragment.class.getCanonicalName(), LoginFragment.class.getCanonicalName(), true);
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }


    // 保存修改

    /**
     * 上传头像
     *
     * @param filePath
     */
    public void saveAvatar(String filePath) {
        Observable.just(filePath)
                .doOnSubscribe(this)
                .compose(RxUtils.exceptionTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribeOn(Schedulers.io())
                .map((Function<String, String>) s -> FileUploadUtils.ossUploadFile("avatar", FileUploadUtils.FILE_TYPE_IMAGE, s))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String fileKey) {
                        dismissHUD();
                        regUser(fileKey);
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissHUD();
                        ToastUtils.showShort(R.string.upload_failed);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public void regUser(String filePath) {
        String channel = model.readChannelAF();
        model.regUser(UserName.get(), filePath, UserBirthday.get(), UserSex.get(), channel)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {
                        uc.showAlertHint.call();
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                        model.clearChannelAF();
                    }
                });
    }

    public void checkNickname(String name, Integer sex) {
        model.checkNickname(name)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<CheckNicknameEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CheckNicknameEntity> checkNicknameEntityBaseDataResponse) {
                        CheckNicknameEntity checkNicknameEntity = checkNicknameEntityBaseDataResponse.getData();
                        if (checkNicknameEntity != null && checkNicknameEntity.getStatus() == 1) {
                            ToastUtils.showShort(R.string.reg_user_name_error);
                            uc.reploadName.postValue(checkNicknameEntity.getRecommend());
                        } else {
                            Bundle bundle = ChooseBirthdayFragment.getStartBundle(sex, name);
                            start(ChooseBirthdayFragment.class.getCanonicalName(), bundle);
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public boolean getCode() {
        Map<String, String> map = model.readOneLinkCode();
        if (ObjectUtils.isEmpty(map)) {
            return false;
        } else {
            return !StringUtil.isEmpty(map.get("code"));
        }

    }

    public class UIChangeObservable {
        public SingleLiveEvent clickBirthday = new SingleLiveEvent<>();
        public SingleLiveEvent clickConfirmBirthday = new SingleLiveEvent<>();
        public SingleLiveEvent clickUserName = new SingleLiveEvent<>();
        public SingleLiveEvent<Void> clickAvatar = new SingleLiveEvent<>();
        public SingleLiveEvent showAlertHint = new SingleLiveEvent();

        public SingleLiveEvent<String> reploadName = new SingleLiveEvent();
    }

}