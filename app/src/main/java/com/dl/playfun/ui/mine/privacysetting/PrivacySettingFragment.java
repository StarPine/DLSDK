package com.dl.playfun.ui.mine.privacysetting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.databinding.FragmentPrivacySettingBinding;

/**
 * @author wulei
 */
public class PrivacySettingFragment extends BaseToolbarFragment<FragmentPrivacySettingBinding, PrivacySettingViewModel> {

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_privacy_setting;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public PrivacySettingViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(PrivacySettingViewModel.class);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
    }
}
