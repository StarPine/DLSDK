package com.dl.playfun.ui.mine.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.CheckNicknameEntity;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.entity.OccupationConfigItemEntity;
import com.dl.playfun.entity.UnlockSocialAccountConfigEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.event.AvatarChangeEvent;
import com.dl.playfun.event.ProfileChangeEvent;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.utils.FileUploadUtils;
import com.dl.playfun.viewmodel.BaseViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author wulei
 */
public class EditProfileViewModel extends BaseViewModel<AppRepository> {
    public ObservableField<UserDataEntity> userDataEntity = new ObservableField<>();
    public ObservableField<UnlockSocialAccountConfigEntity> sociaAccountEntity = new ObservableField<>();
    public ObservableField<Boolean> lineChoose = new ObservableField<>(true);
    public ObservableField<Boolean> insgramChoose = new ObservableField<>(false);
    //    期望对象
    public List<ConfigItemEntity> hope = new ArrayList<>();
    //    节目
    public List<ConfigItemEntity> program = new ArrayList<>();
    //    身高
    public List<ConfigItemEntity> height = new ArrayList<>();
    //    体重
    public List<ConfigItemEntity> weight = new ArrayList<>();
    //    社群账号价格
    public List<UnlockSocialAccountConfigEntity.PriceInfosBean> price = new ArrayList<>();
    //    职业
    public List<OccupationConfigItemEntity> occupation = new ArrayList<>();
    //    城市
    public List<ConfigItemEntity> city = new ArrayList<>();

    UIChangeObservable uc = new UIChangeObservable();
    public BindingCommand uploadAvatarOnClickCommand = new BindingCommand(() -> {
        uc.clickAvatar.call();
    });

    //设置社群价格
    public BindingCommand setUnlockPrice = new BindingCommand(() -> {
        loadSocialAccountConfig();

    });

    //    选择城市
    public BindingCommand chooseCity = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
//            ToastUtils.showShort("选择城市");
            uc.clickCity.call();

        }
    });
    //    选择生日
    public BindingCommand chooseBirthday = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
//            ToastUtils.showShort("选择生日");
            uc.clickBirthday.call();
        }
    });
    //    选择职业
    public BindingCommand chooseOccupation = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
//            ToastUtils.showShort("选择职业");
            uc.clickOccupation.call();
        }
    });
    //    选择交友节目
    public BindingCommand chooseProgram = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
//            ToastUtils.showShort("选择交友节目");
            uc.clickProgram.call();
        }
    });
    //    选择期望对象
    public BindingCommand chooseHope = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
//            ToastUtils.showShort("选择期望对象");
            uc.clickHope.call();
        }
    });
    //    选择身高
    public BindingCommand chooseHeight = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
//            ToastUtils.showShort("选择身高");
            uc.clickHeight.call();
        }
    });
    //    选择体重
    public BindingCommand chooseWeight = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
//            ToastUtils.showShort("选择体重");
            uc.clickWeight.call();
        }
    });
    private boolean showFlag = false;
    public BindingCommand clickSave = new BindingCommand(() -> {
        saveProfile();
    });

    public EditProfileViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
        hope.addAll(model.readHopeObjectConfig());
        program.addAll(model.readThemeConfig());
        height.addAll(model.readHeightConfig());
        weight.addAll(model.readWeightConfig());
        occupation.addAll(model.readOccupationConfig());
        city.addAll(model.readCityConfig());

    }

    @Override
    public void onEnterAnimationEnd() {
        super.onEnterAnimationEnd();
        loadProfile();
    }

    private void loadSocialAccountConfig() {
        model.getUnlockSocialAccountConfig()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<UnlockSocialAccountConfigEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<UnlockSocialAccountConfigEntity> baseResponse) {
                        dismissHUD();
                        UnlockSocialAccountConfigEntity data = baseResponse.getData();
                        sociaAccountEntity.set(data);
                        price.addAll(data.getPriceInfos());
                        uc.clickUnlock.call();
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public void updateSocialLevel(int level) {
        model.updateSocialLevel(level)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(EditProfileViewModel.this)
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {
                    }

                });
    }



    //获取个人资料
    private void loadProfile() {
        //RaJava模拟登录
        model.getUserData()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<UserDataEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<UserDataEntity> response) {
                        UserDataEntity data = response.getData();
                        showFlag = data.isPerfect();
                        model.saveUserData(data);
                        userDataEntity.set(data);
                        String birthdayString = userDataEntity.get().getBirthday();
                        if (!StringUtils.isEmpty(birthdayString)) {
                            // 数据返回不为空，设置生日
                            String[] str = birthdayString.split("-");
                            if (str.length == 3) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(Calendar.YEAR, Integer.parseInt(str[0]));
                                calendar.set(Calendar.MONTH, Integer.parseInt(str[1]) - 1);
                                calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(str[2]));
                                userDataEntity.get().setBirthday(calendar);
                            }
                        }
                        if (!StringUtils.isEmpty(userDataEntity.get().getWeixin())) {
                            lineChoose.set(true);
                        } else if (!StringUtils.isEmpty(userDataEntity.get().getInsgram())) {
                            insgramChoose.set(true);
                        }
                    }
                });
    }

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
                        userDataEntity.get().setAvatar(fileKey);
                        updataAvatar(fileKey);
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

    private void updataAvatar(String fileKey) {
        model.updateAvatar(fileKey)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(EditProfileViewModel.this)
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {
                        dismissHUD();
                        ToastUtils.showShort(R.string.updata_head_success);
                        RxBus.getDefault().post(new AvatarChangeEvent(fileKey));
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        dismissHUD();
                    }
                });
    }

    public void checkNickname(String nickname) {
        if (StringUtils.isEmpty(nickname)) {
            return;
        }
        model.checkNickname(nickname)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(EditProfileViewModel.this)
                .subscribe(new BaseObserver<BaseDataResponse<CheckNicknameEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CheckNicknameEntity> response) {
                        if (response.getData().getStatus() == 1) {
                            uc.nicknameOccupy.postValue(response.getData().getRecommend());
                        }
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                    }
                });
    }

    //保存修复
    public void saveProfile() {
        UserDataEntity userEntity = userDataEntity.get();
        if (userEntity.getAvatar() == null || userEntity.getAvatar().length() == 0) {
            ToastUtils.showShort(R.string.warn_avatar_not_null);
            return;
        }
        if (StringUtils.isEmpty(userEntity.getNickname())) {
            ToastUtils.showShort(R.string.name_nust);
            return;
        }
        if (userEntity.getNickname().length() > 10) {
            ToastUtils.showShort(R.string.name_number_too_long);
            return;
        }

        if (userEntity.getPermanentCityIds() == null) {
            ToastUtils.showShort(R.string.city_nust);
            return;
        }
        if (userEntity.getBirthdayCal() == null) {
            ToastUtils.showShort(R.string.brithday_must);
            return;
        }
        if (userEntity.getOccupationId() == null || userEntity.getOccupationId().intValue() == 0) {
            ToastUtils.showShort(R.string.occupation_must);
            return;
        }
        if (userEntity.getProgramIds() == null || userEntity.getProgramIds().size() == 0) {
            ToastUtils.showShort(R.string.program_must);
            return;
        }
        if (userEntity.getHopeObjectIds() == null || userEntity.getHopeObjectIds().size() == 0) {
            ToastUtils.showShort(R.string.hope_object_must);
            return;
        }
        //女性用户进入
        if (userEntity.getSex() == 0) {
            if (ApiUitl.isContainChinese(userEntity.getWeixin())) {
                ToastUtils.showShort(R.string.line_error_hanzi);
                return;
            }
        }

        // 保存数据
        String birthdayStr = "";
        Calendar birthdayCal = userEntity.getBirthdayCal();
        if (birthdayCal != null) {
            birthdayStr = birthdayCal.get(Calendar.YEAR) + "-" + (birthdayCal.get(Calendar.MONTH) + 1) + "-" + birthdayCal.get(Calendar.DAY_OF_MONTH);
        }
        model.updateUserData(
                userEntity.getNickname(),
                userEntity.getPermanentCityIds(),
                birthdayStr,
                String.valueOf(userEntity.getOccupationId()),
                userEntity.getProgramIds(),
                userEntity.getHopeObjectIds(),
                lineChoose.get() ? userEntity.getWeixin() : null,
                insgramChoose.get() ? userEntity.getInsgram() : null,
                userEntity.isWeixinShow() ? 1 : 0,
                userEntity.getHeight(),
                userEntity.getWeight(),
                userEntity.getDesc()
        )
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        model.saveUserData(userEntity);
                        RxBus.getDefault().post(new ProfileChangeEvent());
                        ToastUtils.showShort(R.string.alter_success);
                        pop();

                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public boolean getTipMoneyShowFlag() {
        return ConfigManager.getInstance().getTipMoneyShowFlag();
    }

    public class UIChangeObservable {
        public SingleLiveEvent<Void> clickAvatar = new SingleLiveEvent<>();
        public SingleLiveEvent clickCity = new SingleLiveEvent<>();
        public SingleLiveEvent clickBirthday = new SingleLiveEvent<>();
        public SingleLiveEvent clickOccupation = new SingleLiveEvent<>();
        public SingleLiveEvent clickProgram = new SingleLiveEvent<>();
        public SingleLiveEvent clickHope = new SingleLiveEvent<>();
        public SingleLiveEvent clickUnlock = new SingleLiveEvent<>();
        public SingleLiveEvent clickHeight = new SingleLiveEvent<>();
        public SingleLiveEvent clickWeight = new SingleLiveEvent<>();
        public SingleLiveEvent clickUploadingHead = new SingleLiveEvent<>();
        public SingleLiveEvent<String> nicknameOccupy = new SingleLiveEvent<>();

        public SingleLiveEvent showFlagClick = new SingleLiveEvent();
    }

}