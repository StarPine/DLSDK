package com.dl.playfun.ui.mine.language;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.ActivityLanguageSwitchBinding;
import com.dl.playfun.event.LanguageChangeEvent;
import com.dl.playfun.ui.base.BaseActivity;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.widget.BasicToolbar;

import me.goldze.mvvmhabit.bus.RxBus;

/**
 * Author: 彭石林
 * Time: 2022/10/10 11:38
 * Description: This is LanguageActivity
 */
public class LanguageSwitchActivity extends BaseActivity<ActivityLanguageSwitchBinding,LanguageSwitchViewModel> {
    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_language_switch;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ImmersionBarUtils.setupStatusBar(this, false, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ImmersionBarUtils.setupStatusBar(this, true, true);
    }


    @Override
    public LanguageSwitchViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(getApplication());
        return ViewModelProviders.of(this, factory).get(LanguageSwitchViewModel.class);
    }

    @Override
    public void initData() {
        viewModel.initData();
        //返回按键返回页面
        binding.basicToolbar.setToolbarListener(toolbar -> {
            finish();
        });
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.languageSwitchEvent.observe(this, unused -> restartActivity());
    }

    //重启当前页面
    public void restartActivity(){
        Intent intent = getApplication().getPackageManager().getLaunchIntentForPackage(getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
//        recreate();
//        RxBus.getDefault().post(new LanguageChangeEvent());
    }
}
