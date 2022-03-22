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
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.dl.playfun.entity.MyCardOrderEntity;
import com.dl.playfun.event.MyCardPayResultEvent;
import com.dl.playfun.ui.base.BasePopupWindow;
import com.dl.playfun.ui.dialog.PayMethodDialog;
import com.dl.playfun.ui.dialog.adapter.CoinRechargeAdapter;
import com.dl.playfun.utils.StringUtil;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Author: 彭石林
 * Time: 2021/12/31 2:00
 * Description: This is ChatDetailCoinRechargeSheetView
 */
public class ChatDetailCoinRechargeSheetView extends BasePopupWindow implements View.OnClickListener, CoinRechargeAdapter.CoinRechargeAdapterListener, PurchasesUpdatedListener, BillingClientStateListener {
    private final String TAG = "ChatDetailCoinRechargeSheetView";

    private final AppCompatActivity mActivity;
    private final Handler handler = new Handler();
    private final int consumeImmediately = 0;
    private final int consumeDelay = 1;
    private final int selPosition = 0;
    //是否在IM聊天页面
    private final int channel;
    private final Integer toUserId;
    //是否发送礼物
    private boolean isGiftSend = false;
    private View mPopView;
    private RecyclerView recyclerView;
    private TextView tvBalance;
    private ImageView ivRefresh;
    private ViewGroup loadingView;
    private CoinRechargeAdapter adapter;
    private BillingClient billingClient;
    private List<GoodsEntity> mGoodsList;
    private String orderNumber;
    private CoinRechargeSheetViewListener coinRechargeSheetViewListener;

    private Disposable mSubscription;

    private GoodsEntity sel_goodsEntity;
    //是否是语音通话页面
    private boolean isCallAUdio = false;

    public ChatDetailCoinRechargeSheetView(AppCompatActivity activity, Integer toUserIds, Integer isChannel, boolean isGiftSend, boolean isCallAUdio) {
        super(activity);
        this.mActivity = activity;
        this.channel = isChannel;
        this.isGiftSend = isGiftSend;
        this.isCallAUdio = isCallAUdio;
        this.toUserId = toUserIds;
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
        if (channel == 0) {
            mPopView = inflater.inflate(R.layout.view_coin_recharge_sheet_call_chat, null);
        } else {
            mPopView = inflater.inflate(R.layout.view_coin_recharge_sheet, null);
        }


        recyclerView = mPopView.findViewById(R.id.recycler_view);
        tvBalance = mPopView.findViewById(R.id.tv_balance);
        ivRefresh = mPopView.findViewById(R.id.iv_refresh);
        loadingView = mPopView.findViewById(R.id.rl_loading);
        ivRefresh.setOnClickListener(this);

        adapter = new CoinRechargeAdapter(recyclerView);
        adapter.setCoinRechargeAdapterListener(this);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        recyclerView.setAdapter(adapter);

        billingClient = BillingClient.newBuilder(mActivity).setListener(this).enablePendingPurchases().build();
        //连接google服务器
        billingClient.startConnection(this);

        mSubscription = RxBus.getDefault().toObservable(MyCardPayResultEvent.class)
                .subscribe(event -> {
                    if (event.getStatus() == MyCardPayResultEvent.PAY_SUCCESS) {
                        notifyMyCardOrder(event.getOrderNo());
                    } else if (event.getStatus() == MyCardPayResultEvent.PAY_ERROR) {
                        if (coinRechargeSheetViewListener != null) {
                            coinRechargeSheetViewListener.onPayFailed(ChatDetailCoinRechargeSheetView.this, event.getErrorMsg());
                        } else {
                            ChatDetailCoinRechargeSheetView.this.dismiss();
                            ToastUtils.showShort(R.string.pay_success);
                            loadBalance();
                            dismiss();
                        }
                    } else if (event.getStatus() == MyCardPayResultEvent.PAY_CANCEL) {
                        ToastUtils.showShort(R.string.pay_cancel);
//                        ChatDetailCoinRechargeSheetView.this.dismiss();
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
                .createChatDetailOrder(goodsEntity.getId(), 1, 2, toUserId, channel)
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
                                    payMyCardOrder(response.getData().getOrderNumber());
                                } else if (payMethod == PayMethodDialog.PAY_METHOD_GOOGLE_PAY) {
                                    List<GoodsEntity> goodsEntityList = new ArrayList<>();
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

    public void payMyCardOrder(String orderNo) {
        loadingView.setVisibility(View.VISIBLE);
        Injection.provideDemoRepository().myCardOrder(orderNo)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<MyCardOrderEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<MyCardOrderEntity> response) {
                        loadingView.setVisibility(View.GONE);
//                        if (!StringUtils.isEmpty(response.getData().getAuthCode())) {
//                            MyCardSDK sdk = new MyCardSDK(mActivity);
//                            sdk.StartPayActivityForResult(false, response.getData().getAuthCode());
//                        } else {
//                            ToastUtils.showShort(R.string.mycard_auth_code_error);
//                        }
                    }

                    @Override
                    public void onError(RequestException e) {
                        super.onError(e);
                        loadingView.setVisibility(View.GONE);
                    }
                });
    }

    public void notifyMyCardOrder(String orderNo) {
        loadingView.setVisibility(View.VISIBLE);

        Injection.provideDemoRepository().myCardNotify(orderNo)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        loadingView.setVisibility(View.GONE);
                        if (coinRechargeSheetViewListener != null) {
                            coinRechargeSheetViewListener.onPaySuccess(ChatDetailCoinRechargeSheetView.this, sel_goodsEntity);
                        } else {
                            ChatDetailCoinRechargeSheetView.this.dismiss();
                            ToastUtils.showShort(R.string.pay_success);
                        }
                    }

                    @Override
                    public void onError(RequestException e) {
                        super.onError(e);
                        loadingView.setVisibility(View.GONE);
                        ChatDetailCoinRechargeSheetView.this.dismiss();
                        ToastUtils.showShort(R.string.order_pay_success_delay_refresh);
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
                        if (coinRechargeSheetViewListener != null) {
                            coinRechargeSheetViewListener.onPaySuccess(ChatDetailCoinRechargeSheetView.this, sel_goodsEntity);
                        } else {
                            ChatDetailCoinRechargeSheetView.this.dismiss();
                            ToastUtils.showShort(R.string.pay_success);
                            loadBalance();
                            dismiss();
                        }
                    }

                    @Override
                    public void onError(RequestException e) {
                        loadingView.setVisibility(View.GONE);
                        if (coinRechargeSheetViewListener != null) {
                            coinRechargeSheetViewListener.onPayFailed(ChatDetailCoinRechargeSheetView.this, e.getMessage());
                        } else {
                            ChatDetailCoinRechargeSheetView.this.dismiss();
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

    @Override
    public void onBuyClick(View view, int position) {
        if (channel == 1) {
            if (isGiftSend) {
                AppContext.instance().logEvent(AppsFlyerEvent.im_gifts_topup_purchase);
            } else {
                AppContext.instance().logEvent(AppsFlyerEvent.im_topup_purchase);
            }
        } else {
            if (isCallAUdio) {
                if (isGiftSend) {
                    AppContext.instance().logEvent(AppsFlyerEvent.voicecall_gift_topup_pur);
                }
            } else {
                if (isGiftSend) {
                    AppContext.instance().logEvent(AppsFlyerEvent.videocall_gift_topup_pur);
                }
            }
        }

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
                                    pay(skuDetailsList.get(0).getSku());
                                } else {
                                    ToastUtils.showShort(R.string.goods_not_exits);
                                }
                                if (channel == 1) {
                                    if (isGiftSend) {
                                        AppContext.instance().logEvent(AppsFlyerEvent.im_gifts_topup_success);
                                    } else {
                                        AppContext.instance().logEvent(AppsFlyerEvent.im_topup_success);
                                    }
                                } else {
                                    if (isCallAUdio) {
                                        if (isGiftSend) {
                                            AppContext.instance().logEvent(AppsFlyerEvent.voicecall_gift_topup_suc);
                                        } else {
                                            AppContext.instance().logEvent(AppsFlyerEvent.voicecall_topup_success);
                                        }
                                    } else {
                                        if (isGiftSend) {
                                            AppContext.instance().logEvent(AppsFlyerEvent.videocall_gift_topup_suc);
                                        } else {
                                            AppContext.instance().logEvent(AppsFlyerEvent.videocall_topup_success);
                                        }
                                    }
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
                            if (channel == 1) {
                                if (isGiftSend) {
                                    AppContext.instance().logEvent(AppsFlyerEvent.im_gifts_topup_fail);
                                } else {
                                    AppContext.instance().logEvent(AppsFlyerEvent.im_topup_fail);
                                }
                            } else {
                                if (isCallAUdio) {
                                    if (isGiftSend) {
                                        AppContext.instance().logEvent(AppsFlyerEvent.voicecall_gift_topup_fail);
                                    } else {
                                        AppContext.instance().logEvent(AppsFlyerEvent.voicecall_topup_fail);
                                    }
                                } else {
                                    if (isGiftSend) {
                                        AppContext.instance().logEvent(AppsFlyerEvent.videocall_gift_topup_fail);
                                    } else {
                                        AppContext.instance().logEvent(AppsFlyerEvent.videocall_topup_fail);
                                    }
                                }
                            }
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
        String sku = purchase.getSku();
        String pToken = purchase.getPurchaseToken();
        ConsumeParams.Builder consumeParams = ConsumeParams.newBuilder();
        consumeParams.setPurchaseToken(purchase.getPurchaseToken());
        String finalPackageName = packageName;
        billingClient.consumeAsync(consumeParams.build(), (billingResult, purchaseToken) -> {
            Log.i(TAG, "onConsumeResponse, code=" + billingResult.getResponseCode());
            loadingView.setVisibility(View.GONE);
            try {
                AppContext.instance().logEvent(AppsFlyerEvent.Successful_top_up, sel_goodsEntity.getPrice(), purchase);
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
                } else {
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
        void onPaySuccess(ChatDetailCoinRechargeSheetView sheetView, GoodsEntity goodsEntity);

        void onPayFailed(ChatDetailCoinRechargeSheetView sheetView, String msg);
    }
}
