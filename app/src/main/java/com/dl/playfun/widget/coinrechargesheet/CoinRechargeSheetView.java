package com.dl.playfun.widget.coinrechargesheet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetailsParams;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.BillingClientLifecycle;
import com.dl.playfun.app.Injection;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.CoinWalletEntity;
import com.dl.playfun.entity.CreateOrderEntity;
import com.dl.playfun.entity.GoodsEntity;
import com.dl.playfun.event.UMengCustomEvent;
import com.dl.playfun.ui.base.BasePopupWindow;
import com.dl.playfun.ui.dialog.adapter.CoinRechargeAdapter;

import java.util.ArrayList;
import java.util.List;

import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author litchi
 */
public class CoinRechargeSheetView extends BasePopupWindow implements View.OnClickListener, CoinRechargeAdapter.CoinRechargeAdapterListener {
    public static final String TAG = "CoinRechargeSheetView";

    private final AppCompatActivity mActivity;
    private View mPopView;
    private RecyclerView recyclerView;
    private TextView tvBalance;
    private ImageView ivRefresh;
    private ViewGroup loadingView;
    private CoinRechargeAdapter adapter;
    private BillingClientLifecycle billingClientLifecycle;
    private List<GoodsEntity> mGoodsList;
    private String orderNumber;

    private CoinRechargeSheetViewListener coinRechargeSheetViewListener;


    private GoodsEntity sel_goodsEntity;

    public CoinRechargeSheetView(AppCompatActivity activity) {
        super(activity);
        this.mActivity = activity;
        init(activity);
        setPopupWindow();
    }

    public CoinRechargeSheetViewListener getCoinRechargeSheetViewListener() {
        return coinRechargeSheetViewListener;
    }

    public void setCoinRechargeSheetViewListener(CoinRechargeSheetViewListener coinRechargeSheetViewListener) {
        this.coinRechargeSheetViewListener = coinRechargeSheetViewListener;
    }

    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        mPopView = inflater.inflate(R.layout.view_coin_recharge_sheet, null);
        recyclerView = mPopView.findViewById(R.id.recycler_view);
        tvBalance = mPopView.findViewById(R.id.tv_balance);
        ivRefresh = mPopView.findViewById(R.id.iv_refresh);
        loadingView = mPopView.findViewById(R.id.rl_loading);
        ivRefresh.setOnClickListener(this);

        adapter = new CoinRechargeAdapter(recyclerView);
        adapter.setCoinRechargeAdapterListener(this);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        recyclerView.setAdapter(adapter);

        this.billingClientLifecycle = ((AppContext)mActivity.getApplication()).getBillingClientLifecycle();

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
                            AppContext.instance().logEvent(AppsFlyerEvent.Successful_top_up, sel_goodsEntity.getPrice(),purchase);
                        } catch (Exception e) {

                        }
                        String packageName = purchase.getPackageName();
                        paySuccessNotify(packageName,orderNumber,purchase.getSkus(),purchase.getPurchaseToken(),1);
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

        loadBalance();
        //查询商品价格
        loadGoods();
    }

    /**
     * 设置窗口的相关属性
     */
    @SuppressLint("InlinedApi")
    private void setPopupWindow() {
        this.setContentView(mPopView);// 设置View
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);// 设置弹出窗口的宽
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);// 设置弹出窗口的高
        this.setOutsideTouchable(false);//设置popupwindow外部可点击
        this.setFocusable(true);// 设置弹出窗口可
        this.setTouchable(true);
        this.setAnimationStyle(R.style.popup_window_anim);// 设置动画
        this.setBackgroundDrawable(new ColorDrawable(0x00000000));// 设置背景透明
//        mPopView.setOnTouchListener(new View.OnTouchListener() {// 如果触摸位置在窗口外面则销毁
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                int height = mPopView.findViewById(R.id.pop_container).getTop();
//                int y = (int) event.getY();
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    Log.e("获取点击外部判断",y+"=========="+height);
//                    if (y < height) {
//                        dismiss();
//                    }
//                }
//                return true;
//            }
//        });
    }

    @Override
    public void dismiss() {
        if (mActivity != null && !mActivity.isFinishing()) {
            Window dialogWindow = mActivity.getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            lp.alpha = 1.0f;
            dialogWindow.setAttributes(lp);
        }

        coinRechargeSheetViewListener = null;
        super.dismiss();
    }

    public void show() {
        super.showLifecycle();
        if (mActivity != null && !mActivity.isFinishing()) {
            Window dialogWindow = mActivity.getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            lp.alpha = 0.5f;
            dialogWindow.setAttributes(lp);
        }
        this.showAtLocation(mActivity.getWindow().getDecorView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    @SuppressLint("StringFormatMatches")
    private void loadBalance() {
        Injection.provideDemoRepository()
                .coinWallet()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<CoinWalletEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CoinWalletEntity> response) {
                        tvBalance.setText(String.format(mActivity.getResources().getString(R.string.playfun_x_coin), response.getData().getTotalCoin()));
                    }

                    @Override
                    public void onError(RequestException e) {
                        ToastUtils.showShort(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                    }
                });
    }

    private void loadGoods() {
        Injection.provideDemoRepository()
                .goods()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<List<GoodsEntity>>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<List<GoodsEntity>> response) {
                        loadingView.setVisibility(View.GONE);
                        mGoodsList = response.getData();
                        adapter.setData(mGoodsList);
//                        querySkuList(response.getData());
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public void createOrder(GoodsEntity goodsEntity) {
        loadingView.setVisibility(View.VISIBLE);
        Injection.provideDemoRepository()
                .createOrder(goodsEntity.getId(), 1, 2, null)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<CreateOrderEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CreateOrderEntity> response) {
                        loadingView.setVisibility(View.GONE);
                        orderNumber = response.getData().getOrderNumber();
                        List<GoodsEntity> goodsEntityList = new ArrayList<>();
                        goodsEntityList.add(goodsEntity);
                        querySkuList(goodsEntityList);
                    }

                    @Override
                    public void onError(RequestException e) {
                        super.onError(e);
                        loadingView.setVisibility(View.GONE);
                    }
                });
    }


    public void paySuccessNotify(String packageName, String orderNumber, ArrayList<String> productId, String token, Integer event) {
        loadingView.setVisibility(View.VISIBLE);
        Injection.provideDemoRepository()
                .paySuccessNotify(packageName, orderNumber, productId, token, 1, event)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        loadingView.setVisibility(View.GONE);
                        if (coinRechargeSheetViewListener != null) {
                            coinRechargeSheetViewListener.onPaySuccess(CoinRechargeSheetView.this, sel_goodsEntity);
                        } else {
                            CoinRechargeSheetView.this.dismiss();
                            ToastUtils.showShort(R.string.playfun_pay_success);
                            loadBalance();
                            dismiss();
                        }
                    }

                    @Override
                    public void onError(RequestException e) {
                        loadingView.setVisibility(View.GONE);
                        if (coinRechargeSheetViewListener != null) {
                            coinRechargeSheetViewListener.onPayFailed(CoinRechargeSheetView.this, e.getMessage());
                        } else {
                            CoinRechargeSheetView.this.dismiss();
                        }
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_refresh) {
            loadBalance();
            loadGoods();
        }
    }

    @Override
    public void onBuyClick(View view, int position) {
        RxBus.getDefault().post(new UMengCustomEvent(UMengCustomEvent.EVENT_PAY_RECHARGE_TOKEN));
        GoodsEntity goodsEntity = mGoodsList.get(position);
        sel_goodsEntity = goodsEntity;
        createOrder(goodsEntity);
    }

    private void querySkuList(List<GoodsEntity> goodsList) {
        if (goodsList == null || goodsList.isEmpty()) {
            return;
        }
        List<String> skus = new ArrayList<>();
        for (GoodsEntity datum : goodsList) {
            skus.add(datum.getGoogleGoodsId());
        }
        if (!billingClientLifecycle.isConnectionSuccessful()) {
            Log.e(TAG, "querySkuList: BillingClient is not ready");
        }
        loadingView.setVisibility(View.VISIBLE);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skus).setType(BillingClient.SkuType.INAPP);
        billingClientLifecycle.querySkuDetailsAsync(params,
                (billingResult, skuDetailsList) -> {
                    loadingView.setVisibility(View.GONE);
                    int responseCode = billingResult.getResponseCode();
                    String debugMessage = billingResult.getDebugMessage();
                    switch (responseCode) {
                        case BillingClient.BillingResponseCode.OK:
                            Log.i(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                            if (skuDetailsList == null) {
                                Log.w(TAG, "onSkuDetailsResponse: null SkuDetails list");
                            } else {
                                if (skuDetailsList != null && !skuDetailsList.isEmpty()) {
                                    AppContext.instance().logEvent(AppsFlyerEvent.One_Click_Purchase);
                                    pay(skuDetailsList.get(0).getSku());
                                } else {
                                    ToastUtils.showShort(R.string.playfun_goods_not_exits);
                                }
                            }
                            break;
                        default:
                            Log.wtf(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                    }
                });
    }



    //进行支付
    private void pay(String payCode) {
        List<String> skuList = new ArrayList<>();
        skuList.add(payCode);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClientLifecycle.querySkuDetailsLaunchBillingFlow(params,mActivity,orderNumber);
    }


    public interface CoinRechargeSheetViewListener {
        void onPaySuccess(CoinRechargeSheetView sheetView, GoodsEntity goodsEntity);

        void onPayFailed(CoinRechargeSheetView sheetView, String msg);
    }
}