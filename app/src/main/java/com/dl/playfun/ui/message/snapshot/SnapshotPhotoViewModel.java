package com.dl.playfun.ui.message.snapshot;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;

import com.dl.playfun.data.AppRepository;
import com.dl.playfun.viewmodel.BaseViewModel;

import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;

/**
 * Author: 彭石林
 * Time: 2022/9/9 12:03
 * Description: This is SnapshotPhotoViewModel
 */
public class SnapshotPhotoViewModel extends BaseViewModel<AppRepository> {
    public ObservableBoolean isVideoSetting = new ObservableBoolean(false);
    public ObservableBoolean isBurn = new ObservableBoolean(false);

    public SingleLiveEvent<Void> settingEvent = new SingleLiveEvent<>();

    public SnapshotPhotoViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
    }

    public BindingCommand settingClick = new BindingCommand(()->{
        settingEvent.call();
    });

    public BindingCommand burnOnClickCommand = new BindingCommand(() -> {
    });


}
