package com.dl.playfun.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentLoginOauthBinding;
import com.dl.playfun.ui.base.BaseFragment;

/**
 * Author: 彭石林
 * Time: 2022/10/12 12:16
 * Description: This is LoginOauthFragment
 */
public class LoginOauthFragment extends BaseFragment<FragmentLoginOauthBinding,LoginOauthViewModel> {
    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_login_oauth;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public LoginOauthViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(LoginOauthViewModel.class);
    }

    @Override
    public void initData() {
        super.initData();
        viewModel.initData();
    }
}
