package com.dl.playfun.ui.message.mediagallery.photo;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;

import com.dl.playfun.viewmodel.BaseViewModel;

import me.goldze.mvvmhabit.base.BaseModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;

/**
 * Author: 彭石林
 * Time: 2022/9/14 14:23
 * Description: This is MediaGalleryPhotoPayViewModel
 */
public class MediaGalleryPhotoPayViewModel extends BaseViewModel {
    //是否需要评价
    public ObservableBoolean evaluationState = new ObservableBoolean(false);

    public MediaGalleryPhotoPayViewModel(@NonNull Application application, BaseModel model) {
        super(application, model);
    }
    //返回上一页
    public BindingCommand<Void> onBackViewClick = new BindingCommand<>(this::finish);
}
