package com.dl.playfun.ui.mine.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.appsflyer.AppsFlyerLib;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.CheckNicknameEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.manager.ThirdPushTokenMgr;
import com.dl.playfun.ui.login.LoginViewModel;
import com.dl.playfun.ui.main.MainFragment;
import com.dl.playfun.utils.FileUploadUtils;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.KLog;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Author: 彭石林
 * Time: 2022/4/4 11:21
 * Description: This is PerfectProfileViewModel
 */
public class PerfectProfileViewModel extends BaseViewModel<AppRepository> {

    public ObservableField<Integer> UserSex = new ObservableField<>();
    public ObservableField<String> UserName = new ObservableField<>();

    public ObservableField<String> UserBirthday = new ObservableField<>("1995-01-01");
    public ObservableField<String> UserAvatar = new ObservableField<>();

    public UIChangeObservable uc = new UIChangeObservable();
    //返回上一页
    public BindingCommand backViewClick = new BindingCommand(() -> pop());
    //点击我的头像
    public BindingCommand avatarOnClickCommand = new BindingCommand(() -> uc.clickAvatar.call());
    public BindingCommand chooseMaleClick = new BindingCommand(() -> uc.clickChooseMale.call());
    public BindingCommand chooseGirlClick = new BindingCommand(() -> uc.clickChooseGirl.call());
    //填写生日界面-按钮点击
    public BindingCommand chooseBirthdayClick = new BindingCommand(() -> uc.clickBirthday.call());
    //提交
    public BindingCommand submitClick = new BindingCommand(() -> {
        if (ObjectUtils.isEmpty(UserSex.get())) {
            ToastUtils.showShort(R.string.playfun_fragment_perfect_sex_hint);
            return;
        }
        uc.getClickBirthday.call();
    });
    public BindingCommand nextViewClick = new BindingCommand(() -> {
        if (StringUtils.isEmpty(UserAvatar.get())) {
            ToastUtils.showShort(R.string.playfun_fragment_perfect_avatar);
            return;
        }
        if (StringUtils.isEmpty(UserName.get())) {
            ToastUtils.showShort(R.string.playfun_fragment_perfect_name_hint);
            return;
        } else {
            if (UserName.get().length() > 10) {
                ToastUtils.showShort(R.string.reg_user_name_maxlen);
                return;
            }
        }
        checkNickname(UserName.get());
    });

    public PerfectProfileViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
    }


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
                        ToastUtils.showShort(R.string.playfun_upload_failed);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    /**
     * @return void
     * @Desc TODO(完善用户资料)
     * @author 彭石林
     * @parame [filePath]
     * @Date 2022/4/4
     */
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
                        dismissHUD();
                        AppConfig.isRegister = true;
                        model.clearChannelAF();
                        uc.showAlertHint.call();
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
            String code = map.get("code");
            return !StringUtils.isTrimEmpty(code) && !code.equals("null");
        }

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
                        AppContext.instance().mFirebaseAnalytics.setUserId(String.valueOf(userDataEntity.getId()));
                        try {
                            //添加崩溃人员id
                            FirebaseCrashlytics.getInstance().setUserId(String.valueOf(userDataEntity.getId()));
                        } catch (Exception ignored) {

                        }
                        if (userDataEntity.getCertification() != null && userDataEntity.getCertification().intValue() == 1) {
                            model.saveNeedVerifyFace(true);
                        }
                        if (goMain) {
                            AppContext.instance().logEvent(AppsFlyerEvent.LOG_Edit_Profile);
                            ToastUtils.showShort(R.string.playfun_submit_success);
                            startWithPopTo(MainFragment.class.getCanonicalName(), PerfectProfileFragment.class.getCanonicalName(), true);
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public void checkNickname(String name) {
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
                            ToastUtils.showShort(R.string.playfun_reg_user_name_error);
                            UserName.set(checkNicknameEntity.getRecommend());
                        } else {
                            uc.verifyAvatar.call();
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public class UIChangeObservable {
        //选择头像
        public SingleLiveEvent<Void> clickAvatar = new SingleLiveEvent<>();
        //选择男生
        public SingleLiveEvent clickChooseMale = new SingleLiveEvent<>();
        //选择女生
        public SingleLiveEvent clickChooseGirl = new SingleLiveEvent<>();
        //选择生日
        public SingleLiveEvent clickBirthday = new SingleLiveEvent<>();
        public SingleLiveEvent showAlertHint = new SingleLiveEvent();
        //获取成日
        public SingleLiveEvent getClickBirthday = new SingleLiveEvent();
        //验证是否是第三方登录解析头像
        public SingleLiveEvent verifyAvatar = new SingleLiveEvent();

    }
}
