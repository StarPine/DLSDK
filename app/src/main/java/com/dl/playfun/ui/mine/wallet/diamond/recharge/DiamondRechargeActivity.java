package com.dl.playfun.ui.mine.wallet.diamond.recharge;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import androidx.lifecycle.ViewModelProviders;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetailsParams;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.BillingClientLifecycle;
import com.dl.playfun.databinding.ActivityDiamondRechargeBinding;
import com.dl.playfun.entity.DiamondPaySuccessEntity;
import com.dl.playfun.entity.GoodsEntity;
import com.dl.playfun.ui.base.BaseActivity;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.widget.BasicToolbar;
import com.dl.playfun.widget.dialog.TraceDialog;

import java.util.ArrayList;
import java.util.List;

import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * 修改备注：钻石充值activity
 *
 * @Name： PlayFun_Google
 * @Description：
 * @Author： liaosf
 * @Date： 2022/8/22 14:22
 */
public class DiamondRechargeActivity extends BaseActivity<ActivityDiamondRechargeBinding, DiamondRechargeViewModel> implements BasicToolbar.ToolbarListener{


    private BillingClientLifecycle billingClientLifecycle;
    private boolean isFinsh = false;

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_diamond_recharge;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public DiamondRechargeViewModel initViewModel() {
        //使用自定义的ViewModelFactory来创建ViewModel，如果不重写该方法，则默认会调用LoginViewModel(@NonNull Application application)构造方法
        AppViewModelFactory factory = AppViewModelFactory.getInstance(getApplication());
        return ViewModelProviders.of(this, factory).get(DiamondRechargeViewModel.class);
    }

    @Override
    public void initParam() {
        super.initParam();
    }

    @Override
    public void initData() {
        super.initData();
        this.billingClientLifecycle = ((AppContext)getApplication()).getBillingClientLifecycle();
        if(billingClientLifecycle!=null){
            //查询并消耗本地历史订单类型： INAPP 支付购买  SUBS订阅
            //billingClientLifecycle.queryAndConsumePurchase(BillingClient.SkuType.INAPP);
            billingClientLifecycle.queryAndConsumePurchase(BillingClient.SkuType.INAPP);
            billingClientLifecycle.queryAndConsumePurchase(BillingClient.SkuType.SUBS);
            billingClientLifecycle.queryPurchasesAsync(BillingClient.SkuType.INAPP);
            billingClientLifecycle.queryPurchasesAsync(BillingClient.SkuType.SUBS);
        }
        viewModel.getRechargeList();
        binding.rcvDiamondRecharge.setNestedScrollingEnabled(false);
        binding.basicToolbar.setToolbarListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ImmersionBarUtils.setupStatusBar(this, false, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ImmersionBarUtils.setupStatusBar(this, true, true);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();

        viewModel.localReportEvent.observe(this , unused->{
            billingClientLifecycle.queryPurchasesAsyncToast(BillingClient.SkuType.INAPP);
            billingClientLifecycle.queryPurchasesAsyncToast(BillingClient.SkuType.SUBS);
        });

        viewModel.payOnClick.observe(this, payCode -> {
            viewModel.showHUD();
            pay(payCode);
        });

        viewModel.paySuccess.observe(this, goodsEntity -> {
            showRewardDialog();
        });

        this.billingClientLifecycle.PAYMENT_SUCCESS.observe(this, billingPurchasesState -> {
            Log.e("BillingClientLifecycle","支付购买成功回调");
            viewModel.dismissHUD();
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
            viewModel.dismissHUD();
            ToastUtils.showShort(StringUtils.getString(R.string.playfun_pay_fail));
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

        //查询消耗本地历史订单
        this.billingClientLifecycle.PurchaseHistory.observe(this,billingPurchasesState -> {
            Log.e("BillingClientLifecycle","查询本地历史订单。没有消耗确认的商品");
            switch (billingPurchasesState.getBillingFlowNode()){
                //有历史订单支付。开始消耗
                case queryPurchaseHistory:
                    break;
                //确认收货
                case acknowledgePurchase:
                    break;
            }
        });
    }


    /**
     * 显示奖励dialog
     */
    private void showRewardDialog() {
        GoodsEntity goodsEntity = viewModel.selectedGoodsEntity.get();
        if (goodsEntity == null) {
            isFinsh = true;
            viewModel.getRechargeList();
            return;
        }
        int totalReward ;
        if (goodsEntity.getType() == 1){
            totalReward = goodsEntity.getGiveCoin() + goodsEntity.getActualValue() + goodsEntity.getGoldPrice();
        }else {
            totalReward = goodsEntity.getGiveCoin();
        }
        TraceDialog.getInstance(this)
                .setTitle(getString(R.string.playfun_recharge_success))
                .setConfirmOnlick(dialog -> {
                    dialog.dismiss();
                    isFinsh = true;
                    viewModel.getRechargeList();
                    RxBus.getDefault().post(new DiamondPaySuccessEntity());
                })
                .dayRewardDialog(true,
                        viewModel.selectedGoodsEntity.get().getDayGiveCoin(),
                        viewModel.selectedGoodsEntity.get().getDayGiveVideoCard(),
                        totalReward,
                        viewModel.selectedGoodsEntity.get().getVideoCard())
                .show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    //进行支付
    private void pay(String payCode) {
        List<String> skuList = new ArrayList<>();
        skuList.add(payCode);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        if (viewModel.selectedGoodsEntity.get().getType() == 2){
            params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        }else {
            params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        }
        billingClientLifecycle.querySkuDetailsLaunchBillingFlow(params,this,viewModel.orderNumber);
    }

    @Override
    public void onBackPressed() {
        if (!isFinsh) {
            TraceDialog.getInstance(this)
                    .setConfirmOnlick(dialog -> {
                        isFinsh = true;
                        dialog.dismiss();
                        finish();
                    })
                    .rechargeRetainDialog()
                    .show();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onBackClick(BasicToolbar toolbar) {
        onBackPressed();
    }
}

