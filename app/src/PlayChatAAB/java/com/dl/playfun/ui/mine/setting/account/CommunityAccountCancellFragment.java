package com.dl.playfun.ui.mine.setting.account;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.dl.playfun.ui.mine.wallet.recharge.RechargeActivity;
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
                    CoinRechargeSheetView coinRechargeFragmentView = new CoinRechargeSheetView(mActivity);
                    coinRechargeFragmentView.setClickListener(new CoinRechargeSheetView.ClickListener() {
                        @Override
                        public void toGooglePlayView(GoodsEntity goodsEntity) {
                            Intent intent = new Intent(mActivity, RechargeActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("Goods_info", goodsEntity);
                            intent.putExtras(bundle);
                            toGooglePlayIntent.launch(intent);
                        }
                    });
                    coinRechargeFragmentView.show();
                }
            }
        });
    }

    //跳转谷歌支付act
    ActivityResultLauncher<Intent> toGooglePlayIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        Log.e("进入支付页面回调","=========");
        if (result.getData() != null) {
            Intent intentData = result.getData();
            GoodsEntity goodsEntity = (GoodsEntity) intentData.getSerializableExtra("goodsEntity");
            if(goodsEntity!=null){
                Log.e("支付成功","===============");
            }
        }
    });
}
