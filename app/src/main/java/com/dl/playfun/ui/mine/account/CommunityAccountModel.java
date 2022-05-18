package com.dl.playfun.ui.mine.account;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;


import com.dl.playfun.data.AppRepository;
import com.dl.playfun.entity.PrivacyEntity;
import com.dl.playfun.viewmodel.BaseViewModel;

import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @ClassName CommunityAccountModel
 * @Description TODO
 * @Author 彭石林
 * @Date 2021/4/29 11:06
 * @Phone 16620350375
 * @email 15616314565@163.com
 * @Version 1.0
 **/
public class CommunityAccountModel extends BaseViewModel<AppRepository> {


    public ObservableField<PrivacyEntity> privacyEntity = new ObservableField<>(new PrivacyEntity());

    public UIChangeObservable UC = new UIChangeObservable();

    public CommunityAccountModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
    }

    @Override
    public void onEnterAnimationEnd() {
        super.onEnterAnimationEnd();
    }

    public class UIChangeObservable {
        SingleLiveEvent<Boolean> loadUserFlag = new SingleLiveEvent<>();
        SingleLiveEvent<Boolean> loginAuth = new SingleLiveEvent<>();
    }
}