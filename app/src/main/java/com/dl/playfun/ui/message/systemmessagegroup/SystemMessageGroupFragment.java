package com.dl.playfun.ui.message.systemmessagegroup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.ui.base.BaseRefreshFragment;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.databinding.FragmentSystemMessageGroupBinding;
import com.dl.playfun.widget.BasicToolbar;

/**
 * 系统消息分组
 *
 * @author wulei
 */
public class SystemMessageGroupFragment extends BaseRefreshFragment<FragmentSystemMessageGroupBinding, SystemMessageGroupViewModel> implements BasicToolbar.ToolbarListener {

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_system_message_group;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public SystemMessageGroupViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(SystemMessageGroupViewModel.class);
    }

    @Override
    public void initData() {
        super.initData();
        binding.refreshLayout.setEnableAutoLoadMore(false);
        binding.refreshLayout.setEnableLoadMore(false);
        binding.basicToolbar.setToolbarListener(this);
        viewModel.initData();
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();

    }

    @Override
    public void onBackClick(BasicToolbar toolbar) {
        mActivity.onBackPressed();
    }
}
