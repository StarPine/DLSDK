package com.dl.playfun.ui.message.chatdetail.notepad;

import android.app.Application;

import androidx.annotation.NonNull;

import com.dl.playfun.data.AppRepository;
import com.dl.playfun.viewmodel.BaseViewModel;

import org.jetbrains.annotations.NotNull;

/**
 * @Name： PlayFun_Google
 * @Description：
 * @Author： liaosf
 * @Date： 2022/6/15 18:23
 * 修改备注：
 */
public class NotepadViewModel extends BaseViewModel<AppRepository> {


    public NotepadViewModel(@NonNull @NotNull Application application, AppRepository model) {
        super(application, model);
    }
}
