package com.dl.playfun.ui.login.facerecognitionsuccess;

import android.app.Application;

import androidx.annotation.NonNull;

import com.dl.playfun.data.AppRepository;
import com.dl.playfun.viewmodel.BaseViewModel;

import me.goldze.mvvmhabit.binding.command.BindingCommand;

/**
 * @author wulei
 */
public class FaceRecognitionSuccessViewModel extends BaseViewModel<AppRepository> {

    public BindingCommand finishOnClickCommand = new BindingCommand(() -> {
        pop();
    });

    public FaceRecognitionSuccessViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
    }

}