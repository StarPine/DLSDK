package com.dl.playfun.ui.mine.task.golddetail;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.databinding.TaskCenterFragmentBinding;
import com.dl.playfun.entity.ExchangeIntegraEntity;
import com.dl.playfun.entity.ExchangeIntegraOuterEntity;
import com.dl.playfun.entity.GoodsEntity;
import com.dl.playfun.ui.base.BaseRefreshToolbarFragment;
import com.dl.playfun.utils.ToastCenterUtils;
import com.dl.playfun.widget.coinrechargesheet.CoinExchargeItegralDialog;
import com.dl.playfun.widget.dialog.TaskFukubukuroDialog;

import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Author: 彭石林
 * Time: 2021/8/11 10:28
 * Description: This is GoldDetailFragment
 */
public class GoldDetailFragment extends BaseRefreshToolbarFragment<TaskCenterFragmentBinding, GoldDetailViewModel> {
    @Override
    public int initContentView(LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return R.layout.task_center_gold_detail_fragment;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public GoldDetailViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(GoldDetailViewModel.class);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        //加载兑换钻石弹窗
        viewModel.goldUc.DialogExchangeIntegral.observe(this, new Observer<ExchangeIntegraOuterEntity>() {
            @Override
            public void onChanged(ExchangeIntegraOuterEntity listData) {
                TaskFukubukuroDialog.exchangeIntegralDialog(GoldDetailFragment.this.getContext(),
                        true, String.valueOf(listData.getTotalBonus()),String.valueOf(listData.getTotalCoin()),0,
                        listData.getData(),
                        new TaskFukubukuroDialog.ExchangeIntegraleClick() {
                            @Override
                            public void clickSelectItem(Dialog dialog, ExchangeIntegraEntity itemEntity) {
                                if(!ObjectUtils.isEmpty(itemEntity)){
                                    if(listData.getTotalCoin().intValue()>=itemEntity.getCoinValue().intValue()){
                                        dialog.dismiss();
                                        viewModel.ExchangeIntegraBuy(itemEntity.getId(),listData.getTotalBonus().intValue(),itemEntity.getBonusValue().intValue());
                                    }else{
                                        ToastCenterUtils.showToast(R.string.dialog_exchange_integral_total_text1);
                                        DialogCoinExchangeIntegralShow(dialog);
                                    }
                                }
                            }
                        }).show();
            }
        });
    }

    public void DialogCoinExchangeIntegralShow(Dialog dialog){
        CoinExchargeItegralDialog coinExchargeItegralSheetView = new CoinExchargeItegralDialog(GoldDetailFragment.this.getContext(),mActivity);
        coinExchargeItegralSheetView.setCoinRechargeSheetViewListener(new CoinExchargeItegralDialog.CoinExchargeIntegralAdapterListener() {
            @Override
            public void onPaySuccess(CoinExchargeItegralDialog sheetView, GoodsEntity sel_goodsEntity) {
                coinExchargeItegralSheetView.dismiss();
                dialog.dismiss();
                ToastUtils.showShort(R.string.dialog_exchange_integral_success);
                //viewModel.showHUD("儲值中…");
                //viewModel.BonusExchange(sel_goodsEntity.getActualValue());
            }
            @Override
            public void onPayFailed(CoinExchargeItegralDialog sheetView, String msg) {
                coinExchargeItegralSheetView.dismiss();
                ToastUtils.showShort(msg);
                AppContext.instance().logEvent(AppsFlyerEvent.Failed_to_top_up);
            }
        });
        coinExchargeItegralSheetView.show();
    }
}
