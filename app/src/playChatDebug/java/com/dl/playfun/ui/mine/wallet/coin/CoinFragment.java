package com.dl.playfun.ui.mine.wallet.coin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.databinding.FragmentCoinBinding;
import com.dl.playfun.entity.GoodsEntity;
import com.dl.playfun.ui.base.BaseRefreshToolbarFragment;
import com.dl.playfun.utils.SoftKeyBoardListener;
import com.dl.playfun.widget.coinrechargesheet.CoinRechargeSheetView;
import com.dl.playfun.widget.dialog.MMAlertDialog;
import com.dl.playfun.widget.dialog.MVDialog;

import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author wulei
 */
@SuppressLint("StringFormatMatches")
public class CoinFragment extends BaseRefreshToolbarFragment<FragmentCoinBinding, CoinViewModel> implements View.OnClickListener {
    public static final String TAG = "CoinFragment";
    protected InputMethodManager inputMethodManager;

    private boolean SoftKeyboardShow = false;

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_coin;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public CoinViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(CoinViewModel.class);
    }

    @Override
    public void initData() {
        super.initData();
        binding.btnRecharge.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_recharge:
                AppContext.instance().logEvent(AppsFlyerEvent.Top_up);
                CoinRechargeSheetView coinRechargeSheetView = new CoinRechargeSheetView(mActivity);
                coinRechargeSheetView.show();
                coinRechargeSheetView.setCoinRechargeSheetViewListener(new CoinRechargeSheetView.CoinRechargeSheetViewListener() {
                    @Override
                    public void onPaySuccess(CoinRechargeSheetView sheetView, GoodsEntity sel_goodsEntity) {
                        sheetView.dismiss();
                        MVDialog.getInstance(CoinFragment.this.getContext())
                                .setTitle(getStringByResId(R.string.recharge_coin_success))
                                .setConfirmText(getStringByResId(R.string.confirm))
                                .setConfirmOnlick(dialog -> {
                                    dialog.dismiss();
                                    viewModel.loadDatas(1);
                                })
                                .chooseType(MVDialog.TypeEnum.CENTER)
                                .show();
                    }

                    @Override
                    public void onPayFailed(CoinRechargeSheetView sheetView, String msg) {
                        sheetView.dismiss();
                        ToastUtils.showShort(msg);
                        AppContext.instance().logEvent(AppsFlyerEvent.Failed_to_top_up);
                    }
                });
                break;
        }


    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        inputMethodManager = (InputMethodManager) this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        SoftKeyBoardListener.setListener(mActivity, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                SoftKeyboardShow = true;
            }

            @Override
            public void keyBoardHide(int height) {
                SoftKeyboardShow = false;
            }
        });
        viewModel.uc.clickSetWithdrawAccount.observe(this, aVoid -> showSetWithdrawDialog());
        viewModel.uc.clickWithdraw.observe(this, entity -> MVDialog.getInstance(mActivity)
                .setTitle(getString(R.string.dialog_title_apply_withdraw))
                .setContent(String.format(getString(R.string.dialog_content_apply_withdraw),
                        entity.getTotalBalance(), entity.getMoney(), entity.getBalance()))
                .setConfirmOnlick(dialog -> {
                    dialog.dismiss();
                    viewModel.cashOut();
                })
                .chooseType(MVDialog.TypeEnum.CENTER)
                .show());
        viewModel.uc.withdrawComplete.observe(this, aVoid -> MVDialog.getInstance(mActivity)
                .setTitle(getString(R.string.dialog_title_withdraw_complete))
                .setContent(getString(R.string.dialog_content_withdraw_complete))
                .setConfirmOnlick(dialog -> dialog.dismiss())
                .chooseType(MVDialog.TypeEnum.CENTER)
                .show());
    }

    private void showSetWithdrawDialog() {
        if (viewModel.coinWalletEntity.get() == null) {
            return;
        }
        MMAlertDialog.WithdrawAccountDialog(getContext(), true, viewModel.coinWalletEntity.get().getAccountNumber(), viewModel.coinWalletEntity.get().getRealName(), new MMAlertDialog.WithdrawAccountDialogListener() {
            @Override
            public void onivClose(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

            @Override
            public void onConfirmClick(DialogInterface dialog, int which, String account, String name) {
                if (StringUtils.isEmpty(account)) {
                    ToastUtils.showShort(getString(R.string.dialog_set_withdraw_account_act_hint));
                    return;
                }
                if (StringUtils.isEmpty(name)) {
                    ToastUtils.showShort(getString(R.string.dialog_set_withdraw_account_name_hint));
                    return;
                }
                dialog.dismiss();
                viewModel.setWithdrawAccount(account, name);
            }

            @Override
            public void setOnDismissListener() {
                hideSoftKeyboard();
            }
        }).show();
    }

    /**
     *
     */
    protected void hideSoftKeyboard() {
        if (SoftKeyboardShow) {
            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }
    }


}
