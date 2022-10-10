package com.dl.playfun.ui.mine.language;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableInt;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.manager.LocaleManager;
import com.dl.playfun.viewmodel.BaseViewModel;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;

/**
 * Author: 彭石林
 * Time: 2022/10/10 12:31
 * Description: This is LanguageSwitchViewModel
 */
public class LanguageSwitchViewModel extends BaseViewModel<AppRepository> {
    //1中文 0英语
    public ObservableInt checkLanguage = new ObservableInt(-1);

    //语言切换事件监听
    public SingleLiveEvent<Void> languageSwitchEvent = new SingleLiveEvent<>();

    public LanguageSwitchViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
    }

    public BindingCommand<Void> clickEnLangSwitch = new BindingCommand<>(() -> {
        if(checkLanguage.get()!= 0){
            LocaleManager.putLocalCacheApply("en");
            languageSwitchEvent.call();
        }

    });

    public BindingCommand<Void> clickZhLangSwitch = new BindingCommand<>(() -> {
        if(checkLanguage.get()!= 1){
            LocaleManager.putLocalCacheApply("zh");
            languageSwitchEvent.call();
        }
    });

    public void initData() {
        String localCache = model.readKeyValue(LocaleManager.dlAppLanguageLocal);
        if(!StringUtils.isEmpty(localCache)){
            if ("zh".equals(localCache)) {
                checkLanguage.set(1);
            } else {
                checkLanguage.set(0);
            }
        }
    }
}
