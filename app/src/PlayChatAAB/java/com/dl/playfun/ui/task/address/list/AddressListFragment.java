package com.dl.playfun.ui.task.address.list;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentAddressListBinding;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.widget.dialog.TraceDialog;

import java.util.Map;

/**
 * Author: 彭石林
 * Time: 2021/8/14 0:19
 * Description: This is AddressListFragment
 */
public class AddressListFragment extends BaseToolbarFragment<FragmentAddressListBinding, AddressListViewModel> {

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return R.layout.fragment_address_list;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initParam() {
        super.initParam();
    }

    @Override
    public AddressListViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(AddressListViewModel.class);
    }

    @Override
    public void initData() {
        super.initData();
    }

    /**
     * @return void
     * @Desc TODO(页面再次进入)
     * @author 彭石林
     * @parame [hidden]
     * @Date 2021/8/4
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            viewModel.loadDatas(1);
        }
    }

    @Override
    public void initViewObservable() {
        viewModel.uc.startRefreshing.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                //结束刷新
                binding.refreshLayout.autoRefresh();
            }
        });

        //监听下拉刷新完成
        viewModel.uc.finishRefreshing.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                //结束刷新
                binding.refreshLayout.finishRefresh(100);
            }
        });

        //监听上拉加载完成
        viewModel.uc.finishLoadmore.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                //结束刷新
                binding.refreshLayout.finishLoadMore(100);
            }
        });
        viewModel.uc.alertDelteAddress.observe(this, new Observer<Map<String, Integer>>() {
            @Override
            public void onChanged(Map<String, Integer> map) {
                TraceDialog.getInstance(AddressListFragment.this.getContext())
                        .setTitle(getString(R.string.address_delete_title))
                        .setCannelText(getString(R.string.cancel))
                        .setConfirmText(getString(R.string.playfun_mine_trace_delike_confirm))
                        .chooseType(TraceDialog.TypeEnum.CENTER)
                        .setConfirmOnlick(new TraceDialog.ConfirmOnclick() {
                            @Override
                            public void confirm(Dialog dialog) {
                                dialog.dismiss();
                                viewModel.removeAddress(map.get("id"), map.get("index"));
                            }
                        }).show();
            }
        });
    }
}
