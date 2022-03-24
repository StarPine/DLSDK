package com.dl.playfun.ui.mine.wallet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentWalletBinding;
import com.dl.playfun.ui.base.BaseToolbarFragment;

/**
 * @author wulei
 */
public class WalletFragment extends BaseToolbarFragment<FragmentWalletBinding, WalletViewModel> {

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_wallet;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public WalletViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(WalletViewModel.class);
    }

    @Override
    public void initData() {
        super.initData();
        viewModel.getUserAccount();
    }

}
