package com.dl.playfun.ui.mine.profile.choosesex;

import android.app.Application;

import androidx.annotation.NonNull;

import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.ui.main.MainFragment;
import com.dl.playfun.viewmodel.BaseViewModel;

import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * @author wulei
 */
public class ChooseSexViewModel extends BaseViewModel<AppRepository> {

    UIChangeObservable uc = new UIChangeObservable();
    public BindingCommand chooseMale = new BindingCommand(() -> {
        uc.clickChooseMale.call();
    });
    public BindingCommand chooseFemale = new BindingCommand(() -> uc.clickChooseFemale.call());
    public BindingCommand confird = new BindingCommand(() -> uc.clickConfird.call());

    public ChooseSexViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
    }
    //提交性别
    public void setSex(int sex) {
        //修改性别后。进入主页面
        model.upUserSex(sex)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>(){
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {
                        dismissHUD();
                        UserDataEntity userDataEntity =  model.readUserData();
                        userDataEntity.setSex(sex);
                        model.saveUserData(userDataEntity);
                        startWithPop(MainFragment.class.getCanonicalName());
                    }
                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public class UIChangeObservable {
        public SingleLiveEvent clickConfird = new SingleLiveEvent<>();
        public SingleLiveEvent clickChooseMale = new SingleLiveEvent<>();
        public SingleLiveEvent clickChooseFemale = new SingleLiveEvent<>();
    }

}
