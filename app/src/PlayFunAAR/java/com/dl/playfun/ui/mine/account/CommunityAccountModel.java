package com.dl.playfun.ui.mine.account;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;


import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.entity.PrivacyEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.viewmodel.BaseViewModel;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
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



    public UIChangeObservable UC = new UIChangeObservable();

    public ObservableField<UserDataEntity> userEntity = new ObservableField<>();

    public CommunityAccountModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
        userEntity.set(model.readUserData());
    }
    //绑定 、 修改邮箱
    public BindingCommand bindingEmailCommand = new BindingCommand(() -> {
        UC.dialogBindEmail.call();
    });
    //绑定 、 修改邮箱
    public BindingCommand bindingPwdCommand = new BindingCommand(() -> {
        if (StringUtils.isTrimEmpty(userEntity.get().getEmail())){
            UC.alertEmailHintView.setValue(true);
        }else{
            UC.alertEmailHintView.setValue(false);
        }
    });

    @Override
    public void onEnterAnimationEnd() {
        super.onEnterAnimationEnd();
    }

    public class UIChangeObservable {
        SingleLiveEvent<Boolean> alertEmailHintView = new SingleLiveEvent<>();
        SingleLiveEvent<Void> dialogBindEmail = new SingleLiveEvent<>();
    }
}