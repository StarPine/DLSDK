package com.dl.playfun.ui.userdetail.remark;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentBindMobileBinding;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.utils.ImmersionBarUtils;

/**
 * @author wulei
 */
public class RemarkUserFragment extends BaseToolbarFragment<FragmentBindMobileBinding, RemarkUserViewModel> {
    public static final String ARG_REMARK_USER_ID = "arg_remark_user_id";

    private int userId;

    public static Bundle getStartBundle(int userId) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_REMARK_USER_ID, userId);
        return bundle;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ImmersionBarUtils.setupStatusBar(this, true, false);
        return view;
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_remark_user;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initParam() {
        super.initParam();
        userId = getArguments().getInt(ARG_REMARK_USER_ID, 0);
    }

    @Override
    public RemarkUserViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        RemarkUserViewModel viewModel = ViewModelProviders.of(this, factory).get(RemarkUserViewModel.class);
        viewModel.userId.set(userId);
        return viewModel;
    }
}
