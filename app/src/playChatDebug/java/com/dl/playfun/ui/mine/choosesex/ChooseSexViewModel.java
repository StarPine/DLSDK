package com.dl.playfun.ui.mine.choosesex;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.ui.login.profile.ChooseNameFragment;
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

    public void setSex(int sex) {
        Bundle bundle = ChooseNameFragment.getStartBundle(sex);
        start(ChooseNameFragment.class.getCanonicalName(), bundle);
    }

    public void reportUserLocation(String latitude, String longitude) {
        model.reportUserLocation(latitude, longitude)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.schedulersTransformer())
                .subscribe(new BaseObserver() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {

                    }
                });
    }

    public class UIChangeObservable {
        public SingleLiveEvent clickConfird = new SingleLiveEvent<>();
        public SingleLiveEvent clickChooseMale = new SingleLiveEvent<>();
        public SingleLiveEvent clickChooseFemale = new SingleLiveEvent<>();
    }

}
