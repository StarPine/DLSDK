package com.dl.playfun.ui.mine.task.record.list;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentTaskRecordListBinding;
import com.dl.playfun.entity.AddressEntity;
import com.dl.playfun.ui.base.BaseFragment;
import com.dl.playfun.ui.mine.address.AddressEditFragment;
import com.dl.playfun.ui.mine.address.list.AddressListFragment;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.widget.dialog.MMAlertDialog;
import com.dl.playfun.widget.dialog.TraceDialog;

import org.jetbrains.annotations.NotNull;

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
    private int lastX, lastY;
    private int oldLastX, oldLastY;

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
    public void initViewObservable() {
        dm = getResources().getDisplayMetrics();
        binding.checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.clickSelAll.execute();
            }
        });
//        binding.ivHelpAdmin.setTouchEventCallvack(new MoveImage.TouchEvent() {
//            @Override
//            public void ACTION_DOWN() {
//                binding.refreshLayout.setEnableRefresh(false);
//                binding.refreshLayout.setEnableLoadMore(false);
//            }
//
//            @Override
//            public void ACTION_MOVE() {
//            }
//
//            @Override
//            public void ACTION_UP() {
//                binding.refreshLayout.setEnableRefresh(true);
//                binding.refreshLayout.setEnableLoadMore(true);
//            }
//        });
//        binding.ivPhoneBar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                viewModel.toUseAdminMessage.execute();
//            }
//        });
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
                        .setConfirmText(getString(R.string.mine_trace_delike_confirm))
                        .setConfirmOnlick(new TraceDialog.ConfirmOnclick() {
                            @Override
                            public void confirm(Dialog dialog) {
                                dialog.dismiss();
                                viewModel.loadDatas(1);
                            }
                        }).AlertTaskBonus().show();
            }
        });
        viewModel.uc.toSubVipPlay.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer goods_type) {
                initGoogle(goods_type);
            }
        });
        //谷歌支付
        viewModel.clickPay.observe(this, payCode -> pay(payCode));
    }

    public void initGoogle(Integer goods_type) {
        viewModel.billingClient = BillingClient.newBuilder(getContext()).setListener(new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(@NonNull @NotNull BillingResult billingResult, @Nullable @org.jetbrains.annotations.Nullable List<Purchase> purchases) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (final Purchase purchase : purchases) {
                        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                            // Acknowledge purchase and grant the item to the user
                            Log.i(TAG, "Purchase success");
                            //确认购买交易，不然三天后会退款给用户
                            if (!purchase.isAcknowledged()) {
                                acknowledgePurchase(purchase);
                            }
                            //TODO:发放商品
                        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                            //需要用户确认
                            Log.i(TAG, "Purchase pending,need to check");
                        }
                    }
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    //用户取消
                    Log.i(TAG, "Purchase cancel");
                } else {
                    //支付错误
                    Log.i(TAG, "Pay result error,code=" + billingResult.getResponseCode() + "\nerrorMsg=" + billingResult.getDebugMessage());
                }
            }
        }).enablePendingPurchases().build();
        //连接google服务器
        viewModel.billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull @NotNull BillingResult billingResult) {
                Log.i(TAG, "billingResult Code=" + billingResult.getResponseCode());
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Log.i(TAG, "Init success,The BillingClient is ready");
                    //每次进行重连的时候都应该消耗之前缓存的商品，不然可能会导致用户支付不了
                    viewModel.queryAndConsumePurchase();
                    viewModel.createOrder(goods_type);
                } else {
                    Log.i(TAG, "Init failed,The BillingClient is not ready,code=" + billingResult.getResponseCode() + "\nMsg=" + billingResult.getDebugMessage());
                    ToastUtils.showShort(billingResult.getDebugMessage());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                //通过调用 startConnection() 方法在下一个请求// Google Play 时重新启动连接。
            }
        });
    }

    //确认订单
    private void acknowledgePurchase(Purchase purchase) {
        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Acknowledge purchase success");
                    String packageName = purchase.getPackageName();
                    String sku = purchase.getSku();
                    String pToken = purchase.getPurchaseToken();
                    viewModel.paySuccessNotify(packageName, sku, pToken, billingResult.getResponseCode());
                } else {
                    String packageName = purchase.getPackageName();
                    String sku = purchase.getSku();
                    String pToken = purchase.getPurchaseToken();
                    viewModel.paySuccessNotify(packageName, sku, pToken, billingResult.getResponseCode());
                    Log.i(TAG, "Acknowledge purchase failed,code=" + billingResult.getResponseCode() + ",\nerrorMsg=" + billingResult.getDebugMessage());
                }
            }
        };
        viewModel.billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
    }

    //进行支付
    private void pay(String payCode) {
        List<String> skuList = new ArrayList<>();
        skuList.add(payCode);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        viewModel.billingClient.querySkuDetailsAsync(params.build(),
                (billingResult, skuDetailsList) -> {
                    Log.i(TAG, "querySkuDetailsAsync=getResponseCode==" + billingResult.getResponseCode() + ",skuDetailsList.size=");
                    // Process the result.
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        if (skuDetailsList != null && skuDetailsList.size() > 0) {
                            for (SkuDetails skuDetails : skuDetailsList) {
                                String sku = skuDetails.getSku();
                                String price = skuDetails.getPrice();
                                Log.i(TAG, "Sku=" + sku + ",price=" + price);
                                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                        .setSkuDetails(skuDetails)
                                        .build();
                                int responseCode = viewModel.billingClient.launchBillingFlow(mActivity, flowParams).getResponseCode();
                                if (responseCode == BillingClient.BillingResponseCode.OK) {
                                    Log.i(TAG, "成功啟動google支付");
                                } else {
                                    //BILLING_RESPONSE_RESULT_OK	0	成功
                                    //BILLING_RESPONSE_RESULT_USER_CANCELED	1	用户按上一步或取消对话框
                                    //BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE	2	网络连接断开
                                    //BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE	3	所请求的类型不支持 Google Play 结算服务 AIDL 版本
                                    //BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE	4	请求的商品已不再出售
                                    //BILLING_RESPONSE_RESULT_DEVELOPER_ERROR	5	提供给 API 的参数无效。此错误也可能说明应用未针对 Google Play 结算服务正确签名或设置，或者在其清单中缺少必要的权限。
                                    //BILLING_RESPONSE_RESULT_ERROR	6	API 操作期间出现严重错误
                                    //BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED	7	未能购买，因为已经拥有此商品
                                    //BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED	8	未能消费，因为尚未拥有此商品
                                    Log.i(TAG, "LaunchBillingFlow Fail,code=" + responseCode);
                                }
                            }
                        } else {
                            Log.i(TAG, "skuDetailsList is empty.");
                        }
                    } else {
                        Log.i(TAG, "Get SkuDetails Failed,Msg=" + billingResult.getDebugMessage());
                    }
                });
    }

}
