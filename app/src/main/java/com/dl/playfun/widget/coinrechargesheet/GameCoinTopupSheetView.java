package com.dl.playfun.widget.coinrechargesheet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.api.AppGameConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.Injection;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.CreateOrderEntity;
import com.dl.playfun.entity.GameCoinBuy;
import com.dl.playfun.entity.GameCoinWalletEntity;
import com.dl.playfun.event.MyCardPayResultEvent;
import com.dl.playfun.event.UMengCustomEvent;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.R;
//import com.dl.playfun.entity.GoodsEntity;
import com.dl.playfun.ui.base.BasePopupWindow;
import com.dl.playfun.ui.dialog.PayMethodDialog;
import com.dl.playfun.ui.dialog.adapter.GameCoinTopupAdapter;
import com.jakewharton.rxbinding2.view.RxView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author litchi
 */
public class GameCoinTopupSheetView extends BasePopupWindow implements View.OnClickListener, GameCoinTopupAdapter.GameCoinTopupAdapterListener, PurchasesUpdatedListener, BillingClientStateListener {
    public static final String TAG = "CoinRechargeSheetView";

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
    private GameCoinTopupAdapter adapter;
    private BillingClient billingClient;
    private List<GameCoinBuy> mGoodsList;
    private String orderNumber = null;

    private CoinRechargeSheetViewListener coinRechargeSheetViewListener;

    private Disposable mSubscription;

    private GameCoinBuy sel_goodsEntity;

    //游戏货币图标
    private ImageView imgGameCoin;

    public GameCoinTopupSheetView(AppCompatActivity activity) {
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
        mPopView = inflater.inflate(R.layout.view_game_coin_topup_sheet, null);
        imgGameCoin = mPopView.findViewById(R.id.icon_game_coin);
        //设置游戏货币图标。根据用户传递
        AppGameConfig appGameConfig = ConfigManager.getInstance().getAppRepository().readGameConfigSetting();
        if(!ObjectUtils.isEmpty(appGameConfig)){
            if(appGameConfig.getGamePlayCoinSmallImg()!=0){
                imgGameCoin.setImageResource(appGameConfig.getGamePlayCoinSmallImg());
            }
        }
        recyclerView = mPopView.findViewById(R.id.recycler_view);
        tvBalance = mPopView.findViewById(R.id.tv_balance);
        ivRefresh = mPopView.findViewById(R.id.iv_refresh);
        loadingView = mPopView.findViewById(R.id.rl_loading);
        ivRefresh.setOnClickListener(this);

        adapter = new GameCoinTopupAdapter(recyclerView);
        adapter.setCoinRechargeAdapterListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        mSubscription = RxBus.getDefault().toObservable(MyCardPayResultEvent.class)
                .subscribe(event -> {
                    if (event.getStatus() == MyCardPayResultEvent.PAY_SUCCESS) {
//                        notifyMyCardOrder(event.getOrderNo());
                    } else if (event.getStatus() == MyCardPayResultEvent.PAY_ERROR) {
                        if (coinRechargeSheetViewListener != null) {
                            coinRechargeSheetViewListener.onPayFailed(GameCoinTopupSheetView.this, event.getErrorMsg());
                        } else {
                            GameCoinTopupSheetView.this.dismiss();
                            ToastUtils.showShort(R.string.playfun_pay_success);
                            loadBalance();
                            dismiss();
                        }
                    } else if (event.getStatus() == MyCardPayResultEvent.PAY_CANCEL) {
                        ToastUtils.showShort(R.string.playfun_pay_cancel);
//                        GameCoinRechargeSheetView.this.dismiss();
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
        this.setFocusable(true);// 设置弹出窗口可
        this.setAnimationStyle(R.style.popup_window_anim);// 设置动画
        this.setBackgroundDrawable(new ColorDrawable(0x00000000));// 设置背景透明
        mPopView.setOnTouchListener(new View.OnTouchListener() {// 如果触摸位置在窗口外面则销毁

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = mPopView.findViewById(R.id.pop_container).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void dismiss() {
        if (mSubscription != null) {
            mSubscription.dispose();
        }
        Log.e(TAG,"dismiss view destory google connection");
        if (mActivity != null && !mActivity.isFinishing()) {
            Window dialogWindow = mActivity.getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            lp.alpha = 1.0f;
            dialogWindow.setAttributes(lp);
        }
        try{
            if(mActivity!=null && mActivity.isFinishing()){
                endGooglePlayConnect();
            }
        }catch(Exception e){
            endGooglePlayConnect();
        }

//        if(handler!=null){
//            handler.removeCallbacksAndMessages(null);
//        }
//        if(billingClient!=null){
//            billingClient.endConnection();
//            billingClient = null;
//        }
        coinRechargeSheetViewListener = null;
        super.dismiss();
    }

    public void endGooglePlayConnect(){
        if(handler!=null){
            handler.removeCallbacksAndMessages(null);
        }
        if(billingClient!=null){
            billingClient.endConnection();
            billingClient = null;
        }
    }

    public void show() {
        if(billingClient==null){
            billingClient = BillingClient.newBuilder(mActivity).setListener(this).enablePendingPurchases().build();
            //连接google服务器
            billingClient.startConnection(this);
        }

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
        // 要用新接口：getUserAccountPageInfo
        Injection.provideDemoRepository()
                .getUserAccountPageInfo()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<GameCoinWalletEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<GameCoinWalletEntity> response) {
                        tvBalance.setText(String.valueOf(response.getData().getTotalAppCoins()));
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
                .buyGameCoins()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<List<GameCoinBuy>>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<List<GameCoinBuy>> response) {
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

    public void createOrder(GameCoinBuy goodsEntity) {
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
                        PayMethodDialog dialog = new PayMethodDialog(String.valueOf(response.getData().getMoney()));
                        dialog.setPayMethodDialogListener(new PayMethodDialog.PayMethodDialogListener() {
                            @Override
                            public void onConfirmClick(PayMethodDialog dialog, int payMethod) {
                                dialog.dismiss();
                                if (payMethod == PayMethodDialog.PAY_METHOD_MYCARD) {
//                                    payMyCardOrder(response.getData().getOrderNumber());
                                } else if (payMethod == PayMethodDialog.PAY_METHOD_GOOGLE_PAY) {
                                    List<GameCoinBuy> goodsEntityList = new ArrayList<>();
                                    goodsEntityList.add(goodsEntity);
                                    querySkuList(goodsEntityList);
                                }
                            }

                            @Override
                            public void onCancelClick(PayMethodDialog dialog) {
                                dialog.dismiss();
                            }
                        });
                        // dialog.show(mActivity.getSupportFragmentManager(), PayMethodDialog.class.getCanonicalName());
                        List<GameCoinBuy> goodsEntityList = new ArrayList<>();
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


    public void paySuccessNotify(String packageName, String orderNumber, List<String> productId, String token, Integer event) {
        Log.e("in repolt pay notify","==============1");
        try{
            loadingView.setVisibility(View.VISIBLE);
        }catch(Exception e){

        }
       //Log.e("开始执行上上报请求",packageName+"==="+orderNumber+"======"+token+"======="+event+"======"+productId.toString());
        Injection.provideDemoRepository()
                .paySuccessNotify(packageName, orderNumber, productId, token, 1, event)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        Log.e("pay notify uccess","===========");
                        try{
                            loadingView.setVisibility(View.GONE);
                        }catch(Exception e){

                        }
                        if (coinRechargeSheetViewListener != null) {
                            loadGoods();
                            loadBalance();
                            coinRechargeSheetViewListener.onPaySuccess(GameCoinTopupSheetView.this, sel_goodsEntity);
                        } else {
                            GameCoinTopupSheetView.this.dismiss();

                            RxView.clicks(tvBalance)
                                    .throttleFirst(5, TimeUnit.SECONDS)  // 才发送 5s内第1次点击按钮的事件
                                    .subscribe(new Consumer<Object>() {
                                        @Override
                                        public void accept(Object object) throws Exception {
                                            ToastUtils.showShort(R.string.playfun_pay_success);
                                        }
                                    });
                            loadBalance();
                            dismiss();
                        }
                    }

                    @Override
                    public void onError(RequestException e) {
                        Log.e("pay notify error","==========="+e.getMessage());
                        loadingView.setVisibility(View.GONE);
                        if (coinRechargeSheetViewListener != null) {
                            coinRechargeSheetViewListener.onPayFailed(GameCoinTopupSheetView.this, e.getMessage());
                        } else {
                            GameCoinTopupSheetView.this.dismiss();
                        }
                    }

                    @Override
                    public void onComplete() {
                        Log.e("pay notify Complete","===========");
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_refresh) {
            loadBalance();
        }
    }

    @Override
    public void onBuyClick(View view, int position) {
        RxBus.getDefault().post(new UMengCustomEvent(UMengCustomEvent.EVENT_PAY_RECHARGE_TOKEN));
        GameCoinBuy goodsEntity = mGoodsList.get(position);
        sel_goodsEntity = goodsEntity;
        createOrder(goodsEntity);
    }

    private void querySkuList(List<GameCoinBuy> goodsList) {
        if (goodsList == null || goodsList.isEmpty()) {
            return;
        }
        List<String> skus = new ArrayList<>();
        for (GameCoinBuy datum : goodsList) {
            skus.add(datum.getGoogleGoodsId());
        }
        if (!billingClient.isReady()) {
            Log.e(TAG, "querySkuList: BillingClient is not ready");
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
                            Log.e(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                            break;
                        case BillingClient.BillingResponseCode.USER_CANCELED:
                            Log.i(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
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
        List<String>  sku = purchase.getSkus();
        String pToken = purchase.getPurchaseToken();
        ConsumeParams.Builder consumeParams = ConsumeParams.newBuilder();
        consumeParams.setPurchaseToken(purchase.getPurchaseToken());
        String finalPackageName = packageName;
        billingClient.consumeAsync(consumeParams.build(), (billingResult, purchaseToken) -> {
            Log.i(TAG, "onConsumeResponse, code=" + billingResult.getResponseCode());
            loadingView.setVisibility(View.GONE);
            try {
                AppContext.instance().logEvent(AppsFlyerEvent.Successful_top_up, sel_goodsEntity.getPrice(),purchase);
            } catch (Exception e) {

            }
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.i(TAG, "onConsumeResponse,code=BillingResponseCode.OK");
//                AppContext.instance().validateGooglePlayLog(purchase, sel_goodsEntity.getPrice());
                paySuccessNotify(finalPackageName, orderNumber, sku, pToken, billingResult.getResponseCode());
//                    if (state == consumeImmediately) {
//                        viewModel.paySuccessNotify(orderId, packageName, sku, purchaseToken);
//                    }
            } else {
                //如果消耗不成功，那就再消耗一次
                Log.i(TAG, "onConsumeResponse=getDebugMessage==" + billingResult.getDebugMessage());
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
                    Log.i(TAG, "Acknowledge purchase success");
                    String packageName = purchase.getPackageName();
                    List<String>  sku = purchase.getSkus();
                    String pToken = purchase.getPurchaseToken();
                    paySuccessNotify(packageName,orderNumber, sku, pToken, billingResult.getResponseCode());
                } else {
                    String packageName = purchase.getPackageName();
                    List<String>  sku = purchase.getSkus();
                    String pToken = purchase.getPurchaseToken();
                    paySuccessNotify(packageName,orderNumber, sku, pToken, billingResult.getResponseCode());
                    Log.i(TAG, "Acknowledge purchase failed,code=" + billingResult.getResponseCode() + ",\nerrorMsg=" + billingResult.getDebugMessage());
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
                                    int responseCode = billingClient.launchBillingFlow(mActivity, flowParams).getResponseCode();
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
                                loadingView.setVisibility(View.GONE);
                            }
                        } else {
                            Log.i(TAG, "Get SkuDetails Failed,Msg=" + billingResult.getDebugMessage());
                            loadingView.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
        Log.i(TAG, "billingResult Code=" + billingResult.getResponseCode());
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            // The BillingClient is ready. You can query purchases here.
            Log.i(TAG, "Init success,The BillingClient is ready");
            //每次进行重连的时候都应该消耗之前缓存的商品，不然可能会导致用户支付不了
            queryAndConsumePurchase();
        } else {
            Log.i(TAG, "Init failed,The BillingClient is not ready,code=" + billingResult.getResponseCode() + "\nMsg=" + billingResult.getDebugMessage());
            ToastUtils.showShort(billingResult.getDebugMessage());
        }
    }

    @Override
    public void onBillingServiceDisconnected() {
        Log.d(TAG, "onBillingServiceDisconnected");
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
                    Log.i(TAG, "Purchase pending,need to check");

                }
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            //用户取消
            Log.i(TAG, "Purchase cancel");
            loadingView.setVisibility(View.GONE);
        } else {
            //支付错误
            Log.i(TAG, "Pay result error,code=" + billingResult.getResponseCode() + "\nerrorMsg=" + billingResult.getDebugMessage());
            loadingView.setVisibility(View.GONE);
        }
    }

    public interface CoinRechargeSheetViewListener {
        void onPaySuccess(GameCoinTopupSheetView sheetView, GameCoinBuy goodsEntity);

        void onPayFailed(GameCoinTopupSheetView sheetView, String msg);
    }
}