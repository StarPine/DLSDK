package com.dl.playfun.ui.message.contact;

import android.app.Application;

import androidx.annotation.NonNull;

import com.dl.playfun.data.AppRepository;
import com.dl.playfun.viewmodel.BaseViewModel;

/**
 * Author: 彭石林
 * Time: 2022/8/11 17:36
 * Description: This is OftenContactViewModel
 */
public class OftenContactViewModel extends BaseViewModel<AppRepository> {
    public OftenContactViewModel(@NonNull Application application) {
        super(application);
    }

    public OftenContactViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
    }
}
