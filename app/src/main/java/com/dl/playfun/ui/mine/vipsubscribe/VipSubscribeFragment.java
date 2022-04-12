package com.dl.playfun.ui.mine.vipsubscribe;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.databinding.FragmentVipSubscribeBinding;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.entity.VipPackageItemEntity;
import com.dl.playfun.event.VipRechargeSuccessEvent;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.utils.AutoSizeUtils;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.widget.coinpaysheet.CoinPaySheet;
import com.dl.playfun.widget.coinrechargesheet.GameCoinTopupSheetView;

import java.util.ArrayList;
import java.util.List;

import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author wulei
 */
public class VipSubscribeFragment extends BaseToolbarFragment<FragmentVipSubscribeBinding, VipSubscribeViewModel> implements View.OnClickListener, PurchasesUpdatedListener, BillingClientStateListener {
    public static final String TAG = "VipSubscribeFragment";

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        ImmersionBarUtils.setupStatusBar(this, false, true);
    }

    @Override
    public void onSupportInvisible() {
        super.onSupportInvisible();
        ImmersionBarUtils.setupStatusBar(this, true, true);
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AutoSizeUtils.applyAdapt(this.getResources());
        return R.layout.fragment_vip_subscribe;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public VipSubscribeViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(VipSubscribeViewModel.class);
    }

    public static void setVipSubscribeImageUri(ImageView imageView, String ImageUrl, int errorImg, int placeImg) {
        if (!ObjectUtils.isEmpty(ImageUrl)) {
            Glide.with(imageView.getContext()).load(StringUtil.getFullImageUrl(ImageUrl))
                    .error(errorImg)
                    .placeholder(placeImg)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
        }
    }

    @Override
    public void onDestroy() {
        if (viewModel.billingClient != null) {
            viewModel.billingClient.endConnection();
            viewModel.billingClient = null;
        }
        super.onDestroy();
    }

    @Override
    public void initData() {
        super.initData();
        viewModel.billingClient = BillingClient.newBuilder(mActivity).setListener(this).enablePendingPurchases().build();
        //连接google服务器
        viewModel.billingClient.startConnection(this);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.pay_good_day = 0;
        viewModel.uc.clickPay.observe(this, payCode -> pay(payCode));
        viewModel.uc.successBack.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer aBoolean) {
                onDestroy();
                pop();
            }
        });
        viewModel.uc.loadVipImageNoEmpty.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {
                if (!ObjectUtils.isEmpty(viewModel.$taskEntity1)) {
                    binding.itemImage1.setVisibility(View.VISIBLE);
                    setVipSubscribeImageUri(binding.itemImage1, viewModel.$taskEntity1.getImg(), R.drawable.img_vip_sub_title_default, R.drawable.img_vip_sub_title_default);
                }
                if (!ObjectUtils.isEmpty(viewModel.$taskEntity2)) {
                    binding.itemImage2.setVisibility(View.VISIBLE);
                    setVipSubscribeImageUri(binding.itemImage2, viewModel.$taskEntity2.getImg(), R.drawable.img_vip_sub_title_default, R.drawable.img_vip_sub_title_default);
                }
                if (!ObjectUtils.isEmpty(viewModel.$taskEntity3)) {
                    binding.itemImage3.setVisibility(View.VISIBLE);
                    setVipSubscribeImageUri(binding.itemImage3, viewModel.$taskEntity3.getImg(), R.drawable.img_vip_sub_back2_default, R.drawable.img_vip_sub_back2_default);
                }
                if (!ObjectUtils.isEmpty(viewModel.$taskEntity4)) {
                    binding.itemImage4.setVisibility(View.VISIBLE);
                    setVipSubscribeImageUri(binding.itemImage4, viewModel.$taskEntity4.getImg(), R.drawable.img_vip_sub_title_default, R.drawable.img_vip_sub_title_default);
                }
                if (!ObjectUtils.isEmpty(viewModel.$taskEntity5)) {
                    binding.itemImage5.setVisibility(View.VISIBLE);
                    setVipSubscribeImageUri(binding.itemImage5, viewModel.$taskEntity5.getImg(), R.drawable.img_vip_sub_back2_default, R.drawable.img_vip_sub_back2_default);
                }
                if (!ObjectUtils.isEmpty(viewModel.$taskEntity6)) {
                    binding.itemImage6.setVisibility(View.VISIBLE);
                    setVipSubscribeImageUri(binding.itemImage6, viewModel.$taskEntity6.getImg(), R.drawable.img_vip_sub_back2_default, R.drawable.img_vip_sub_back2_default);
                }
                if (!ObjectUtils.isEmpty(viewModel.$taskEntity7)) {
                    binding.itemImage7.setVisibility(View.VISIBLE);
                    setVipSubscribeImageUri(binding.itemImage7, viewModel.$taskEntity7.getImg(), R.drawable.img_vip_sub_back2_default, R.drawable.img_vip_sub_back2_default);
                }
            }
        });
    }

    private void showDialog() {
        if (viewModel.selectedPosition.get() < 0) {
            ToastUtils.showShort(R.string.playfun_please_choose_top_up_package);
            return;
        }
        VipPackageItemEntity entity = viewModel.observableList.get(viewModel.selectedPosition.get()).itemEntity.get();

        new CoinPaySheet.Builder(mActivity).setPayParams(2, entity.getId(), getString(R.string.playfun_dialog_coin_pay_project), false, new CoinPaySheet.CoinPayDialogListener() {
            @Override
            public void onPaySuccess(CoinPaySheet sheet, String orderNo, Integer payPrice) {
                sheet.dismiss();
                ToastUtils.showShort(R.string.playfun_recharge_success);
                UserDataEntity userDataEntity = ConfigManager.getInstance().getAppRepository().readUserData();
                userDataEntity.setIsVip(1);
                ConfigManager.getInstance().getAppRepository().saveUserData(userDataEntity);
                RxBus.getDefault().post(new VipRechargeSuccessEvent());
                pop();
            }

            @Override
            public void onRechargeSuccess(GameCoinTopupSheetView gameCoinTopupSheetView) {

            }
        }).build().show();
    }

    //进行支付
    private void pay(String payCode) {
        AppContext.instance().logEvent(AppsFlyerEvent.Subscribe);
        List<String> skuList = new ArrayList<>();
        skuList.add(payCode);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        viewModel.billingClient.querySkuDetailsAsync(params.build(),
                (billingResult, skuDetailsList) -> {
                    Log.i(TAG, "querySkuDetailsAsync=getResponseCode==" + billingResult.getResponseCode() + ",skuDetailsList.size=" + skuDetailsList.size());
                    // Process the result.
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        if (skuDetailsList.size() > 0) {
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
                                    AppContext.instance().logEvent(AppsFlyerEvent.vip_google_start+responseCode);
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

    //确认订单
    private void acknowledgePurchase(Purchase purchase) {
        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(BillingResult billingResult) {

                try {
                    AppContext.instance().logEvent(AppsFlyerEvent.Subscribe_Successfully, viewModel.$vipPackageItemEntity.getPrice(),purchase);
                } catch (Exception e) {

                }
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Acknowledge purchase success");
                    String packageName = purchase.getPackageName();
                    List<String> sku = purchase.getSkus();
                    String pToken = purchase.getPurchaseToken();
                    viewModel.paySuccessNotify(packageName, sku, pToken, billingResult.getResponseCode());
                } else {
                    String packageName = purchase.getPackageName();
                    List<String> sku = purchase.getSkus();
                    String pToken = purchase.getPurchaseToken();
                    viewModel.paySuccessNotify(packageName, sku, pToken, billingResult.getResponseCode());
                    AppContext.instance().logEvent(AppsFlyerEvent.vip_google_play_error+billingResult.getResponseCode());
                    Log.i(TAG, "Acknowledge purchase failed,code=" + billingResult.getResponseCode() + ",\nerrorMsg=" + billingResult.getDebugMessage());
                    AppContext.instance().logEvent(AppsFlyerEvent.Failed_to_Subscribe);
                }
            }
        };
        viewModel.billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_confirm) {
            showDialog();
        }
    }

    @Override
    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
        Log.i(TAG, "billingResult Code=" + billingResult.getResponseCode());
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            // The BillingClient is ready. You can query purchases here.
            Log.i(TAG, "Init success,The BillingClient is ready");
            //每次进行重连的时候都应该消耗之前缓存的商品，不然可能会导致用户支付不了
            viewModel.queryAndConsumePurchase();
            //查询商品价格
            viewModel.loadPackage();
        } else {
            Log.i(TAG, "Init failed,The BillingClient is not ready,code=" + billingResult.getResponseCode() + "\nMsg=" + billingResult.getDebugMessage());
            ToastUtils.showShort(billingResult.getDebugMessage());
            viewModel.loadPackage();
        }
    }

    @Override
    public void onBillingServiceDisconnected() {
        Log.d(TAG, "onBillingServiceDisconnected");
        if (viewModel.billingClient != null) {
            viewModel.billingClient.startConnection(this);
        } else {
            viewModel.billingClient.startConnection(this);
            //连接google服务器
            viewModel.billingClient = BillingClient.newBuilder(mActivity).setListener(this).enablePendingPurchases().build();
        }

    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (final Purchase purchase : purchases) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    // Acknowledge purchase and grant the item to the user
                    Log.i(TAG, "Purchase success");
                    //确认购买交易，不然三天后会退款给用户
                    if (!purchase.isAcknowledged()) {
                        acknowledgePurchase(purchase);
                    }
//                    //消耗品 开始消耗
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            consumePuchase(purchase, consumeDelay);
//                        }
//                    }, 1000);
                    //TODO:发放商品
                } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                    //需要用户确认
                    Log.i(TAG, "Purchase pending,need to check");

                }
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            //用户取消
            Log.i(TAG, "Purchase cancel");
            AppContext.instance().logEvent(AppsFlyerEvent.vip_google_start_cancel);
        } else {
            //支付错误
            Log.i(TAG, "Pay result error,code=" + billingResult.getResponseCode() + "\nerrorMsg=" + billingResult.getDebugMessage());
            AppContext.instance().logEvent(AppsFlyerEvent.vip_google_play_result+billingResult.getResponseCode());
        }
    }

}
