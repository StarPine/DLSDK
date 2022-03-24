package com.dl.playfun.ui.login.facerecognitionfailed;

import android.app.Application;

import androidx.annotation.NonNull;

import com.dl.playfun.data.AppRepository;
import com.dl.playfun.viewmodel.BaseViewModel;

import me.goldze.mvvmhabit.binding.command.BindingCommand;

/**
 * @author wulei
 */
public class FaceRecognitionFailedViewModel extends BaseViewModel<AppRepository> {

    public BindingCommand finishOnClickCommand = new BindingCommand(() -> {
        pop();
    });

    public FaceRecognitionFailedViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
    }

}