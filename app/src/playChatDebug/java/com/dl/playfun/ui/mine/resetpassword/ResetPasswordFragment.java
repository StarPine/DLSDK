package com.dl.playfun.ui.mine.resetpassword;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentResetPasswordBinding;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.utils.ImmersionBarUtils;

/**
 * @author wulei
 */
public class ResetPasswordFragment extends BaseToolbarFragment<FragmentResetPasswordBinding, ResetPasswordViewModel> {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ImmersionBarUtils.setupStatusBar(this, true, true);
        return view;
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_reset_password;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public ResetPasswordViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(ResetPasswordViewModel.class);
    }
}
