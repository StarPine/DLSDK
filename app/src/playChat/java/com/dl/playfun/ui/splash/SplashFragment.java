package com.dl.playfun.ui.splash;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentSplashBinding;
import com.dl.playfun.ui.base.BaseFragment;

/**
 * @author wulei
 */
public class SplashFragment extends BaseFragment<FragmentSplashBinding, SplashViewModel> {

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_splash;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public SplashViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(SplashViewModel.class);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
    }
}
