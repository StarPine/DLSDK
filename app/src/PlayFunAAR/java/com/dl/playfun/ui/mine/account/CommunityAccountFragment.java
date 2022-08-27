package com.dl.playfun.ui.mine.account;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentSettingAccountBinding;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.ui.dialog.GmailBindDialog;
import com.dl.playfun.ui.mine.account.bind.EmailMangerBindActivity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collection;

import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @ClassName CommunityAccount
 * @Description TODO
 * @Author 彭石林
 * @Date 2021/4/29 10:31
 * @Phone 16620350375
 * @email 15616314565@163.com
 * @Version 1.0
 **/
public class CommunityAccountFragment extends BaseToolbarFragment<FragmentSettingAccountBinding, CommunityAccountModel> {

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_setting_account;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public CommunityAccountModel initViewModel() {
        //使用自定义的ViewModelFactory来创建ViewModel，
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(CommunityAccountModel.class);
    }

    @Override
    public void initData(){
        super.initData();
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        //跳转绑定邮箱页面
        viewModel.UC.dialogBindEmail.observe(this, unused -> startBindEmailView(true));
        //弹出绑定邮箱提示
        viewModel.UC.alertEmailHintView.observe(this, isBind -> {
                    GmailBindDialog.getHintEmailDialog(getContext(), false, () -> {
                        startBindEmailView(isBind);
                    }).show();
                }
        );
    }
    //跳转绑定邮箱弹窗view
    public void startBindEmailView(boolean bindEmail){
        Bundle bundle = new Bundle();
        bundle.putBoolean("bindEmail",bindEmail);
        startActivity(EmailMangerBindActivity.class,bundle);
    }

}