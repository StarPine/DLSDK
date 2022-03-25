package com.dl.playfun.widget.coinrechargesheet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.Injection;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.CoinWalletEntity;
import com.dl.playfun.entity.CreateOrderEntity;
import com.dl.playfun.entity.GoodsEntity;
import com.dl.playfun.event.UMengCustomEvent;
import com.dl.playfun.ui.base.BaseDialog;
import com.dl.playfun.ui.dialog.adapter.CoinExchargeIntegralAdapter;
import com.dl.playfun.utils.StringUtil;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Author: 彭石林
 * Time: 2021/9/29 14:31
 * Description: This is CoinExchargeItegralDialog
 */
public class CoinExchargeItegralDialog extends BaseDialog implements View.OnClickListener, PurchasesUpdatedListener, BillingClientStateListener {

    public static final String TAG = "CoinExchargeItegralDialog";
    private static CoinExchargeItegralDialog.CoinExchargeIntegralAdapterListener coinExchargeIntegralAdapterListener;
    private static GoodsEntity sel_goodsEntity;
    private final AppCompatActivity mActivity;
    private final Handler handler = new Handler();
    private final int consumeImmediately = 0;
    private final int consumeDelay = 1;
    private final int selPosition = 0;
    private View mPopView;
    private RecyclerView recyclerView;
    private TextView tvBalance;
    private ImageView ivRefresh;
    private ViewGroup loadingView;
    private CoinExchargeIntegralAdapter adapter;
    private BillingClient billingClient;
    private List<GoodsEntity> mGoodsList;
    private String orderNumber;

    public CoinExchargeItegralDialog(@NonNull Context context, AppCompatActivity activity) {
        super(context);
        this.mActivity = activity;
        init(activity);
    }

    public CoinExchargeItegralDialog.CoinExchargeIntegralAdapterListener getCoinRechargeSheetViewListener() {
        return coinExchargeIntegralAdapterListener;
    }

    public void setCoinRechargeSheetViewListener(CoinExchargeItegralDialog.CoinExchargeIntegralAdapterListener coinExchargeIntegralAdapterListener) {
        CoinExchargeItegralDialog.coinExchargeIntegralAdapterListener = coinExchargeIntegralAdapterListener;
    }

    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        mPopView = inflater.inflate(R.layout.view_coin_exrcharge_integral_sheet, null);

        recyclerView = mPopView.findViewById(R.id.recycler_view);
        tvBalance = mPopView.findViewById(R.id.tv_balance);
        ivRefresh = mPopView.findViewById(R.id.iv_refresh);
        loadingView = mPopView.findViewById(R.id.rl_loading);
        ivRefresh.setOnClickListener(this);

        adapter = new CoinExchargeIntegralAdapter(recyclerView);
        adapter.setCoinExchargeIntegralAdapterListener(new CoinExchargeIntegralAdapter.CoinExchargeIntegralAdapterListener() {
            @Override
            public void onBuyClick(View view, int position) {
                RxBus.getDefault().post(new UMengCustomEvent(UMengCustomEvent.EVENT_PAY_RECHARGE_TOKEN));
                GoodsEntity goodsEntity = mGoodsList.get(position);
                sel_goodsEntity = goodsEntity;
                createOrder(goodsEntity);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        billingClient = BillingClient.newBuilder(mActivity).setListener(this).enablePendingPurchases().build();
        //连接google服务器
        billingClient.startConnection(this);

        loadBalance();
        //查询商品价格
        loadGoods();
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

        // coinExchargeIntegralAdapterListener = null;
        super.dismiss();
    }

    public void show() {
        //设置背景透明,去四个角
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(mPopView);
        //设置宽度充满屏幕
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        super.show();
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
                        tvBalance.setText(String.format(mActivity.getResources().getString(R.string.x_coin), response.getData().getTotalCoin()));
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
                .pointsGoodList()
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
                .createOrder(goodsEntity.getId(), 14, 2, null)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<CreateOrderEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CreateOrderEntity> response) {
                        loadingView.setVisibility(View.GONE);
                        orderNumber = response.getData().getOrderNumber();
                        // dialog.show(mActivity.getSupportFragmentManager(), PayMethodDialog.class.getCanonicalName());
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

    public void paySuccessNotify(String packageName, String orderNumber, String productId, String token, Integer event) {
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
                        if (coinExchargeIntegralAdapterListener != null) {
                            coinExchargeIntegralAdapterListener.onPaySuccess(CoinExchargeItegralDialog.this, sel_goodsEntity);
                        } else {
                            CoinExchargeItegralDialog.this.dismiss();
                            ToastUtils.showShort(R.string.pay_success);
                            loadBalance();
                            dismiss();
                        }
                    }

                    @Override
                    public void onError(RequestException e) {
                        loadingView.setVisibility(View.GONE);
                        if (coinExchargeIntegralAdapterListener != null) {
                            coinExchargeIntegralAdapterListener.onPayFailed(CoinExchargeItegralDialog.this, e.getMessage());
                        } else {
                            CoinExchargeItegralDialog.this.dismiss();
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
        }
    }

    private void querySkuList(List<GoodsEntity> goodsList) {
        if (goodsList == null || goodsList.isEmpty()) {
            return;
        }
        List<String> skus = new ArrayList<>();
        for (GoodsEntity datum : goodsList) {
            skus.add(datum.getGoogleGoodsId());
        }
        if (!billingClient.isReady()) {
            //Log.e(TAG, "querySkuList: BillingClient is not ready");
        }
        loadingView.setVisibility(View.VISIBLE);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skus).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                (billingResult, skuDetailsList) -> {
                    loadingView.setVisibility(View.GONE);
                    int responseCode = billingResult.getResponseCode();
                    String debugMessage = billingResult.getDebugMessage();
                    switch (responseCode) {
                        case BillingClient.BillingResponseCode.OK:
                            // Log.i(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                            if (skuDetailsList == null) {
                                //Log.w(TAG, "onSkuDetailsResponse: null SkuDetails list");
                            } else {
                                if (skuDetailsList != null && !skuDetailsList.isEmpty()) {
                                    AppContext.instance().logEvent(AppsFlyerEvent.One_Click_Purchase);
                                    pay(skuDetailsList.get(0).getSku());
                                } else {
                                    ToastUtils.showShort(R.string.goods_not_exits);
                                }

//                                mGoodsList = new ArrayList<>();
//                                for (GoodsEntity goodsEntity : goodsList) {
//                                    for (SkuDetails skuDetails : skuDetailsList) {
//                                        if (skuDetails.getSku().equals(goodsEntity.getGoogleGoodsId())) {
//                                            goodsEntity.setSkuDetails(skuDetails);
//                                            mGoodsList.add(goodsEntity);
//                                            break;
//                                        }
//                                    }
//                                }
//                                adapter.setData(mGoodsList);
                            }
                            break;
                        case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                        case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                        case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                        case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                        case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                        case BillingClient.BillingResponseCode.ERROR:
                            //Log.e(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                            break;
                        case BillingClient.BillingResponseCode.USER_CANCELED:
                            //Log.i(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                            break;
                        // These response codes are not expected.
                        case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                        case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                        case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
                        default:
                            Log.wtf(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                    }
                });
    }

    //查询最近的购买交易，并消耗商品
    private void queryAndConsumePurchase() {
        //queryPurchases() 方法会使用 Google Play 商店应用的缓存，而不会发起网络请求
        loadingView.setVisibility(View.VISIBLE);
        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP,
                new PurchaseHistoryResponseListener() {
                    @Override
                    public void onPurchaseHistoryResponse(BillingResult billingResult,
                                                          List<PurchaseHistoryRecord> purchaseHistoryRecordList) {
                        loadingView.setVisibility(View.GONE);
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchaseHistoryRecordList != null) {
                            for (PurchaseHistoryRecord purchaseHistoryRecord : purchaseHistoryRecordList) {
                                // Process the result.
                                //确认购买交易，不然三天后会退款给用户
                                try {
                                    Purchase purchase = new Purchase(purchaseHistoryRecord.getOriginalJson(), purchaseHistoryRecord.getSignature());
                                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                        //消耗品 开始消耗
                                        consumePuchase(purchase, consumeImmediately);
                                        //确认购买交易
                                        if (!purchase.isAcknowledged()) {
                                            acknowledgePurchase(purchase);
                                        }
                                        //TODO：这里可以添加订单找回功能，防止变态用户付完钱就杀死App的这种
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
    }

    //消耗商品
    private void consumePuchase(final Purchase purchase, final int state) {
        loadingView.setVisibility(View.VISIBLE);
        String packageName = purchase.getPackageName();
        if (StringUtil.isEmpty(packageName)) {
            packageName = AppContext.instance().getApplicationInfo().packageName;
        }
        String sku = purchase.getSkus().toString();
        String pToken = purchase.getPurchaseToken();
        ConsumeParams.Builder consumeParams = ConsumeParams.newBuilder();
        consumeParams.setPurchaseToken(purchase.getPurchaseToken());
        String finalPackageName = packageName;
        billingClient.consumeAsync(consumeParams.build(), (billingResult, purchaseToken) -> {
            // Log.i(TAG, "onConsumeResponse, code=" + billingResult.getResponseCode());
            loadingView.setVisibility(View.GONE);
            if(sel_goodsEntity!=null){
                AppContext.instance().logEvent(AppsFlyerEvent.Successful_top_up, sel_goodsEntity.getPrice(),purchase);
            }
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                //Log.i(TAG, "onConsumeResponse,code=BillingResponseCode.OK");
//                AppContext.instance().validateGooglePlayLog(purchase, sel_goodsEntity.getPrice());
                paySuccessNotify(finalPackageName, orderNumber, sku, pToken, billingResult.getResponseCode());
//                    if (state == consumeImmediately) {
//                        viewModel.paySuccessNotify(orderId, packageName, sku, purchaseToken);
//                    }
            } else {
                //如果消耗不成功，那就再消耗一次
                // Log.i(TAG, "onConsumeResponse=getDebugMessage==" + billingResult.getDebugMessage());
                if (state == consumeDelay && billingResult.getDebugMessage().contains("Server error, please try again")) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            queryAndConsumePurchase();
                        }
                    }, 5 * 1000);
                }
            }
        });
    }

    //确认订单
    private void acknowledgePurchase(Purchase purchase) {
        loadingView.setVisibility(View.VISIBLE);
        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                loadingView.setVisibility(View.GONE);
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    //Log.i(TAG, "Acknowledge purchase success");
                } else {
                    // Log.i(TAG, "Acknowledge purchase failed,code=" + billingResult.getResponseCode() + ",\nerrorMsg=" + billingResult.getDebugMessage());
                }

            }
        };
        billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
    }

    //进行支付
    private void pay(String payCode) {
        List<String> skuList = new ArrayList<>();
        skuList.add(payCode);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                        //Log.i(TAG, "querySkuDetailsAsync=getResponseCode==" + billingResult.getResponseCode() + ",skuDetailsList.size=" + skuDetailsList.size());
                        // Process the result.
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            if (skuDetailsList != null && skuDetailsList.size() > 0) {
                                for (SkuDetails skuDetails : skuDetailsList) {
                                    String sku = skuDetails.getSku();
                                    String price = skuDetails.getPrice();
                                    // Log.i(TAG, "Sku=" + sku + ",price=" + price);
                                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                            .setSkuDetails(skuDetails)
                                            .build();
                                    int responseCode = billingClient.launchBillingFlow(mActivity, flowParams).getResponseCode();
                                    if (responseCode == BillingClient.BillingResponseCode.OK) {
                                        // Log.i(TAG, "成功啟動google支付");
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
                                    }
                                }
                            } else {
                                loadingView.setVisibility(View.GONE);
                            }
                        } else {
                            loadingView.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            // The BillingClient is ready. You can query purchases here.
            //每次进行重连的时候都应该消耗之前缓存的商品，不然可能会导致用户支付不了
            queryAndConsumePurchase();
        } else {
            ToastUtils.showShort(billingResult.getDebugMessage());
        }
    }

    @Override
    public void onBillingServiceDisconnected() {
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (final Purchase purchase : purchases) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    // Acknowledge purchase and grant the item to the user
                    //确认购买交易，不然三天后会退款给用户
                    if (!purchase.isAcknowledged()) {
                        acknowledgePurchase(purchase);
                    }
                    //消耗品 开始消耗
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            consumePuchase(purchase, consumeDelay);
                        }
                    }, 1000);
                    //TODO:发放商品
                } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                    //需要用户确认

                }
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            //用户取消
            loadingView.setVisibility(View.GONE);
        } else {
            //支付错误
            loadingView.setVisibility(View.GONE);
        }
    }

    public interface CoinExchargeIntegralAdapterListener {
        void onPaySuccess(CoinExchargeItegralDialog sheetView, GoodsEntity goodsEntity);

        void onPayFailed(CoinExchargeItegralDialog sheetView, String msg);
    }
}
