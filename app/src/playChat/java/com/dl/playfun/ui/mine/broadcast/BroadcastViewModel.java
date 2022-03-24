package com.dl.playfun.ui.mine.broadcast;

import android.app.Application;

import androidx.annotation.NonNull;

import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.entity.ThemeItemEntity;
import com.dl.playfun.event.BadioEvent;
import com.dl.playfun.event.UMengCustomEvent;
import com.dl.playfun.viewmodel.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.RxSubscriptions;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;

public class BroadcastViewModel extends BaseViewModel<AppRepository> {
    public List<ThemeItemEntity> themes = null;
    UIChangeObservable uc = new UIChangeObservable();
    public BindingCommand issuanceOnClickCommand = new BindingCommand(() -> {
        RxBus.getDefault().post(new UMengCustomEvent(UMengCustomEvent.EVENT_MY_BROADCAST_PUBLISH));
        uc.clickIssuance.call();
    });
    private Disposable badioEvent;

    public BroadcastViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        loadTheme();
    }

    @Override
    public void registerRxBus() {
        super.registerRxBus();
        badioEvent = RxBus.getDefault().toObservable(BadioEvent.class)
                .subscribe(event -> {
                    uc.switchPosion.setValue(event.getType());
                });
    }

    @Override
    public void removeRxBus() {
        super.removeRxBus();
        RxSubscriptions.remove(badioEvent);
    }

    private void loadTheme() {
        List<ConfigItemEntity> configItemEntityList = model.readThemeConfig();
        List<ThemeItemEntity> listData = new ArrayList<>();
        for (ConfigItemEntity configItemEntity : configItemEntityList) {
            ThemeItemEntity themeItemEntity = new ThemeItemEntity();
            themeItemEntity.setId(configItemEntity.getId());
            themeItemEntity.setTitle(configItemEntity.getName());
            themeItemEntity.setIcon(configItemEntity.getIcon());
            themeItemEntity.setSmallIcon(configItemEntity.getSmallIcon());
            listData.add(themeItemEntity);
        }
        if (listData != null) {
            themes = listData;
        }
    }

    public void checkTopical() {
        model.checkTopical()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
//                        start(ProgramSubjectFragment.class.getCanonicalName());
                        uc.programSubject.call();
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public class UIChangeObservable {
        public SingleLiveEvent clickIssuance = new SingleLiveEvent<>();
        public SingleLiveEvent programSubject = new SingleLiveEvent<>();
        public SingleLiveEvent switchPosion = new SingleLiveEvent<>();
    }
}
