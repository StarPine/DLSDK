package com.dl.playfun.ui.mine.vipsubscribe;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetailsParams;
import com.blankj.utilcode.util.ObjectUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.BillingClientLifecycle;
import com.dl.playfun.databinding.FragmentVipSubscribeBinding;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.utils.AutoSizeUtils;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wulei
 */
public class VipSubscribeFragment extends BaseToolbarFragment<FragmentVipSubscribeBinding, VipSubscribeViewModel> {
    public static final String TAG = "VipSubscribeFragment";

    public BillingClientLifecycle billingClientLifecycle;

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
        super.onDestroy();
    }

    @Override
    public void initData() {
        super.initData();
        billingClientLifecycle = ((AppContext)mActivity.getApplication()).getBillingClientLifecycle();
        //查询商品价格
        viewModel.loadPackage();

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
                        try {
                            AppContext.instance().logEvent(AppsFlyerEvent.Subscribe_Successfully, viewModel.$vipPackageItemEntity.getPrice(),purchase);
                        } catch (Exception e) {

                        }
                        String packageName = purchase.getPackageName();
                        List<String> sku = purchase.getSkus();
                        String pToken = purchase.getPurchaseToken();
                        viewModel.paySuccessNotify(packageName, sku, pToken, 1);
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
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.pay_good_day = 0;
        viewModel.uc.clickPay.observe(this, payCode -> pay(payCode));
        viewModel.uc.successBack.observe(this, aBoolean -> {
            onDestroy();
            pop();
        });
        viewModel.uc.loadVipImageNoEmpty.observe(this, unused -> {
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
        });
    }

    //进行支付
    private void pay(String payCode) {
        AppContext.instance().logEvent(AppsFlyerEvent.Subscribe);
        List<String> skuList = new ArrayList<>();
        skuList.add(payCode);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        billingClientLifecycle.querySkuDetailsLaunchBillingFlow(params,mActivity,viewModel.orderNumber);
    }

}
