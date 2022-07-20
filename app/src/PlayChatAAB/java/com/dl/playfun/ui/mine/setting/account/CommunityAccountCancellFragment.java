package com.dl.playfun.ui.mine.setting.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.databinding.FragmentSettingAccountCancellBinding;
import com.dl.playfun.entity.GoodsEntity;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.ui.login.LoginFragment;
import com.dl.playfun.widget.coinrechargesheet.CoinRechargeSheetView;
import com.dl.playfun.widget.dialog.MMAlertDialog;

public class CommunityAccountCancellFragment extends BaseToolbarFragment<FragmentSettingAccountCancellBinding, CommunityAccountCancellViewModel> {
    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_setting_account_cancell;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public CommunityAccountCancellViewModel initViewModel() {
        //使用自定义的ViewModelFactory来创建ViewModel
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(CommunityAccountCancellViewModel.class);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        binding.btnCancell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.cancellation();
            }
        });
        viewModel.cancellType.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean type) {
                if (type) {
                    MMAlertDialog.AlertAccountCancell(mActivity, (dialog, which) -> {
                        //跳转到登录界面
                        startWithPopTo(new LoginFragment(), CommunityAccountCancellFragment.class, true);
                    }).show();
                } else {
                    //调用充值钻石弹窗
                    CoinRechargeSheetView coinRechargeSheetView = new CoinRechargeSheetView(mActivity);
                    coinRechargeSheetView.show();
                    coinRechargeSheetView.setCoinRechargeSheetViewListener(new CoinRechargeSheetView.CoinRechargeSheetViewListener() {
                        @Override
                        public void onPaySuccess(CoinRechargeSheetView sheetView, GoodsEntity sel_goodsEntity) {
                            AppContext.instance().logEvent(AppsFlyerEvent.success_diamond_top_up);
                        }

                        @Override
                        public void onPayFailed(CoinRechargeSheetView sheetView, String msg) {
                            // do nothing
                        }
                    });
                }
            }
        });
    }
}
