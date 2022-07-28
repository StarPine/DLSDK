package com.dl.playfun.ui.mine.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.ui.main.MainFragment;
import com.dl.playfun.utils.FileUploadUtils;
import com.dl.playfun.viewmodel.BaseViewModel;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
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

    UIChangeObservable uc = new UIChangeObservable();
    public class UIChangeObservable {
        //选择头像
        public SingleLiveEvent<Void> clickAvatar = new SingleLiveEvent<>();
        //选择男生
        public SingleLiveEvent clickChooseMale = new SingleLiveEvent<>();
        //选择女生
        public SingleLiveEvent clickChooseGirl = new SingleLiveEvent<>();
        //选择生日
        public SingleLiveEvent clickBirthday = new SingleLiveEvent<>();
    }
    public PerfectProfileViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
    }

    //点击我的头像
    public BindingCommand avatarOnClickCommand = new BindingCommand(() -> uc.clickAvatar.call());

    public BindingCommand chooseMaleClick = new BindingCommand(() -> uc.clickChooseMale.call());
    public BindingCommand chooseGirlClick = new BindingCommand(() -> uc.clickChooseGirl.call());
    //填写生日界面-按钮点击
    public BindingCommand chooseBirthdayClick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            uc.clickBirthday.call();
        }
    });

    //提交
    public BindingCommand submitClick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            if(StringUtils.isEmpty(UserAvatar.get())){
                ToastUtils.showShort(R.string.playfun_fragment_perfect_avatar);
                return;
            }
            if(StringUtils.isEmpty(UserName.get())){
                ToastUtils.showShort(R.string.playfun_fragment_perfect_name_hint);
                return;
            }
            if(ObjectUtils.isEmpty(UserSex.get())){
                ToastUtils.showShort(R.string.playfun_fragment_perfect_sex_hint);
                return;
            }
            saveAvatar(UserAvatar.get());
        }
    });

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
                        me.goldze.mvvmhabit.utils.ToastUtils.showShort(R.string.playfun_upload_failed);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }
    /**
    * @Desc TODO(完善用户资料)
    * @author 彭石林
    * @parame [filePath]
    * @return void
    * @Date 2022/4/4
    */
    public void regUser(String filePath) {
        model.regUser(UserName.get(), filePath, UserBirthday.get(), UserSex.get())
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<UserDataEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<UserDataEntity> response) {
                        dismissHUD();
                        UserDataEntity userDataEntity = response.getData();
                        model.saveUserData(userDataEntity);
                        userDataEntity.setSex(UserSex.get());
                        startWithPop(MainFragment.class.getCanonicalName());
                    }
                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }
}
