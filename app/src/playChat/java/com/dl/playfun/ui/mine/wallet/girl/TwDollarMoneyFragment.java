package com.dl.playfun.ui.mine.wallet.girl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentWalletDollarMoneyBinding;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.ui.certification.certificationfemale.CertificationFemaleFragment;
import com.dl.playfun.ui.certification.certificationmale.CertificationMaleFragment;
import com.dl.playfun.widget.dialog.MVDialog;


/**
 * Author: 彭石林
 * Time: 2021/12/3 14:41
 * Description: 台币收入页面
 */
public class TwDollarMoneyFragment extends BaseToolbarFragment<FragmentWalletDollarMoneyBinding, TwDollarMoneyViewModel> {

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_wallet_dollar_money;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public TwDollarMoneyViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(TwDollarMoneyViewModel.class);
    }

    @Override
    public void initViewObservable() {
        viewModel.loadDatas(1);
        viewModel.uc.startRefreshing.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                //结束刷新
                binding.refreshLayout.autoRefresh();
            }
        });
        viewModel.uc.certification.observe(this,event -> {
            MVDialog.getInstance(TwDollarMoneyFragment.this.getContext())
                    .setTitle(getString(R.string.fragment_certification_tip))
                    .setContent(getString(R.string.fragment_certification_content))
                    .setConfirmText(getString(R.string.task_fragment_task_new11))
                    .chooseType(MVDialog.TypeEnum.CENTER)
                    .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
                        @Override
                        public void confirm(MVDialog dialog) {
                            if (AppContext.instance().appRepository.readUserData().getSex() == AppConfig.MALE) {
                                viewModel.start(CertificationMaleFragment.class.getCanonicalName());
                                return;
                            } else if (AppContext.instance().appRepository.readUserData().getSex() == AppConfig.FEMALE) {
                                viewModel.start(CertificationFemaleFragment.class.getCanonicalName());
                                return;
                            }
                            com.blankj.utilcode.util.ToastUtils.showShort(R.string.sex_unknown);
                            dialog.dismiss();
                        }
                    })
                    .chooseType(MVDialog.TypeEnum.CENTER)
                    .show();
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

    }

}
