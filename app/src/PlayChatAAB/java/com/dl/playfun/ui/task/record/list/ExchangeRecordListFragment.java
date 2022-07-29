package com.dl.playfun.ui.task.record.list;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.BillingClientLifecycle;
import com.dl.playfun.databinding.FragmentTaskRecordListBinding;
import com.dl.playfun.entity.AddressEntity;
import com.dl.playfun.ui.base.BaseFragment;
import com.dl.playfun.ui.task.address.AddressEditFragment;
import com.dl.playfun.ui.task.address.list.AddressListFragment;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.widget.dialog.MMAlertDialog;
import com.dl.playfun.widget.dialog.TraceDialog;

import java.util.ArrayList;
import java.util.List;

import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Author: 彭石林
 * Time: 2021/8/12 11:33
 * Description: This is ExchangeRecordListFragment
 */
public class ExchangeRecordListFragment extends BaseFragment<FragmentTaskRecordListBinding, ExchangeRecordListViewModel> {
    private static final String TAG = "兑换记录签到领取会员";

    private Integer grend;

    private DisplayMetrics dm;
    /*谷歌支付*/
    public BillingClientLifecycle billingClientLifecycle;
    public static ExchangeRecordListFragment newInstance(int grend) {
        ExchangeRecordListFragment fragment = new ExchangeRecordListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("grend", grend);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        //
        return R.layout.fragment_task_record_list;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initParam() {
        super.initParam();
        grend = getArguments().getInt("grend", 0);
    }

    @Override
    public ExchangeRecordListViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        ExchangeRecordListViewModel exchangeRecordListViewModel = ViewModelProviders.of(this, factory).get(ExchangeRecordListViewModel.class);
        exchangeRecordListViewModel.grend = this.grend;
        return exchangeRecordListViewModel;
    }

    @Override
    public void  initData(){
        billingClientLifecycle = ((AppContext)mActivity.getApplication()).getBillingClientLifecycle();
    }

    @Override
    public void initViewObservable() {
        dm = getResources().getDisplayMetrics();

        this.billingClientLifecycle.PAYMENT_SUCCESS.observe(this, billingPurchasesState -> {
            Log.e("BillingClientLifecycle","支付购买成功回调");
            switch (billingPurchasesState.getBillingFlowNode()){
                //查询商品阶段
                case querySkuDetails:
                    break;
                case launchBilling: //启动购买
                    break;
                case purchasesUpdated: //用户购买操作 可在此购买成功 or 取消支付
                    break;
                case acknowledgePurchase:  // 用户操作购买成功 --> 商家确认操作 需要手动确定收货（消耗这笔订单并且发货（给与用户购买奖励）） 否则 到达一定时间 自动退款
                    Purchase purchase = billingPurchasesState.getPurchase();
                    if(purchase!=null){
                        String packageName = purchase.getPackageName();
                        viewModel.paySuccessNotify(packageName, purchase.getSkus(), purchase.getPurchaseToken(), 1);
                        Log.e("BillingClientLifecycle","dialog支付购买成功："+purchase.toString());
                    }
                    break;
            }
        });
        this.billingClientLifecycle.PAYMENT_FAIL.observe(this, billingPurchasesState -> {
            Log.e("BillingClientLifecycle","支付购买失败回调");
            switch (billingPurchasesState.getBillingFlowNode()){
                //查询商品阶段-->异常
                case querySkuDetails:
                    break;
                case launchBilling: //启动购买-->异常
                    break;
                case purchasesUpdated: //用户购买操作 可在此购买成功 or 取消支付 -->异常
                    break;
                case acknowledgePurchase:  // 用户操作购买成功 --> 商家确认操作 需要手动确定收货（消耗这笔订单并且发货（给与用户购买奖励）） 否则 到达一定时间 自动退款 -->异常
                    break;
            }
        });
        binding.checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.clickSelAll.execute();
            }
        });
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

        viewModel.uc.checkAddressed.observe(this, new Observer<AddressEntity>() {
            @Override
            public void onChanged(AddressEntity addressEntity) {
                ApiUitl.$address = null;
                MMAlertDialog.DialogAddress(getContext(), true, addressEntity,
                        new MMAlertDialog.AlertAddressesInterface() {
                            @Override
                            public void toAddress(DialogInterface dialog, boolean isEmpty) {
                                dialog.dismiss();
                                if (isEmpty) {
                                    viewModel.start(AddressEditFragment.class.getCanonicalName());
                                } else {
                                    viewModel.start(AddressListFragment.class.getCanonicalName());
                                }
                            }

                            @Override
                            public void confirmSub(DialogInterface dialog) {
                                dialog.dismiss();
                                if (ObjectUtils.isEmpty(addressEntity)) {
                                    ToastUtils.showShort(R.string.address_error);
                                    return;
                                }
                                viewModel.subSupply(viewModel.sub_key, addressEntity.getId());
                            }
                        }).show();
            }
        });
        viewModel.uc.subSupplySuccess.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {
                TraceDialog.getInstance(ExchangeRecordListFragment.this.getContext())
                        .setTitle(getString(R.string.address_sub_title))
                        .setContent(getString(R.string.address_sub_content))
                        .setConfirmText(getString(R.string.playfun_mine_trace_delike_confirm))
                        .setConfirmOnlick(new TraceDialog.ConfirmOnclick() {
                            @Override
                            public void confirm(Dialog dialog) {
                                dialog.dismiss();
                                viewModel.loadDatas(1);
                            }
                        }).AlertTaskBonus().show();
            }
        });
        viewModel.uc.toSubVipPlay.observe(this, goods_type -> initGoogle(goods_type));
        //谷歌支付
        viewModel.clickPay.observe(this, payCode -> pay(payCode));

        //拉起订阅
        viewModel.uc.querySkuOrderEvent.observe(this, s -> querySkuOrder(s));
    }

    public void initGoogle(Integer goods_type) {
        viewModel.createOrder(goods_type);
    }

    //根据iD查询谷歌商品。并且订购它 7days_free
    public void querySkuOrder(String goodId) {
        ArrayList<String> goodList = new ArrayList<>();
        goodList.add(goodId);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(goodList).setType(BillingClient.SkuType.SUBS);
        if (!billingClientLifecycle.isConnectionSuccessful()) {
            ToastUtils.showShort(R.string.playfun_invite_web_detail_error);
            return;
        }
        billingClientLifecycle.querySkuDetailsAsync(params, (billingResult, skuDetailsList) -> {
            int responseCode = billingResult.getResponseCode();
            String debugMessage = billingResult.getDebugMessage();
            if (responseCode == BillingClient.BillingResponseCode.OK) {
                Log.i(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                if (skuDetailsList == null) {
                    //订阅找不到
                    Log.w(TAG, "onSkuDetailsResponse: null SkuDetails list");
                    ToastUtils.showShort(R.string.playfun_invite_web_detail_error2);
                } else {
                    for (SkuDetails skuDetails : skuDetailsList) {
                        if (skuDetails.getSku().equals(goodId)) {
                            viewModel.goodSkuDetails = skuDetails;
                            viewModel.clickPay.postValue(skuDetails.getSku());
                            break;
                        }
                    }
                }
            }
        });
    }

    //进行支付
    private void pay(String payCode) {
        List<String> skuList = new ArrayList<>();
        skuList.add(payCode);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        billingClientLifecycle.querySkuDetailsLaunchBillingFlow(params,mActivity,viewModel.orderNumber);
    }

}
