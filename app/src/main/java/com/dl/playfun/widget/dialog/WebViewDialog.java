package com.dl.playfun.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.svideo.common.utils.FastClickUtil;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.Injection;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.CreateOrderEntity;
import com.dl.playfun.entity.VipPackageItemEntity;
import com.dl.playfun.event.UserUpdateVipEvent;
import com.dl.playfun.ui.base.BaseDialog;
import com.dl.playfun.ui.webview.BrowserView;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.utils.Utils;
import com.google.gson.Gson;
import com.jakewharton.rxbinding2.view.RxView;

import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Author: 彭石林
 * Time: 2021/12/22 20:03
 * Description: This is WebViewDialog
 */
public class WebViewDialog extends BaseDialog implements PurchasesUpdatedListener, BillingClientStateListener {
    private final static String TAG = "WebViewDialog支付";
    private final Handler handler = new Handler();
    private final Context context;
    private Dialog dialog;
    private final String webUrl;
    private final AppCompatActivity mActivity;
    private final ConfirmOnclick confirmOnclick;
    private ImageView iv_default;
    //会员天数
    public Integer pay_good_day = 0;
    private volatile VipPackageItemEntity vipPackageItemEntity = new VipPackageItemEntity();
    private String orderNumber = null;
    //是否是购买商品 or  订阅vip
    private boolean GooglePayInApp = false;
    private ViewGroup loadingView;
    /**
     * 谷歌支付连接
     */
    public BillingClient billingClient;
    private final int consumeImmediately = 0;
    private final int consumeDelay = 1;

    public WebViewDialog(@NonNull Context context, AppCompatActivity activity, String url, ConfirmOnclick confirmOnclick) {
        super(context);
        this.context = context;
        this.mActivity = activity;
        this.webUrl = url;
        this.confirmOnclick = confirmOnclick;
        initGooglePay();
    }


    private void initGooglePay(){
        billingClient = BillingClient.newBuilder(mActivity).setListener(this).enablePendingPurchases().build();
        //连接google服务器
        billingClient.startConnection(this);
    }

    public static byte[] syncLoad(String url, String type) {
        Bitmap.CompressFormat imgtype = Bitmap.CompressFormat.JPEG;
        if (type.endsWith(".png")) {
            imgtype = Bitmap.CompressFormat.PNG;
        } else if (type.endsWith(".jpg") || type.endsWith(".jpeg")) {
            imgtype = Bitmap.CompressFormat.JPEG;
        }
        FutureTarget<Bitmap> target = Glide.with(AppContext.instance())
                .asBitmap().dontAnimate().diskCacheStrategy(DiskCacheStrategy.ALL).load(url).submit();
        try {
            Bitmap bitmap = target.get();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(imgtype, 100, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public Dialog noticeDialog() {
        dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        View view = View.inflate(context, R.layout.alert_notice_web_view, null);
        ImageView ic_dialog_close = view.findViewById(R.id.ic_dialog_close);
        BrowserView webView = view.findViewById(R.id.web_view);
        iv_default = view.findViewById(R.id.iv_default);
        loadingView = view.findViewById(R.id.rl_loading);
        ic_dialog_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                webView.destroy();
                if (confirmOnclick != null)confirmOnclick.cancel();
            }
        });

        WebSettings settings = webView.getSettings();
        // 允许文件访问
        settings.setAllowFileAccess(true);
        //开启网页自适应
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //设置WebView属性，能够执行Javascript脚本
        settings.setJavaScriptEnabled(true);
        //这里需要设置为true，才能让Webivew支持<meta>标签的viewport属性
        settings.setUseWideViewPort(true);
        //提高网页渲染的优先级
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        //支持屏幕缩放
        settings.setLoadWithOverviewMode(true);
        //使js可以调用安卓方法
        webView.setBrowserViewClient(new MyBrowserViewClient());
        // 加快网页加载完成的速度，等页面完成再加载图片
        settings.setLoadsImagesAutomatically(true);
        // 本地 DOM 存储（解决加载某些网页出现白板现象）
        settings.setDomStorageEnabled(true);
        // 设置 WebView 的缓存模式
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        // 支持启用缓存模式
        settings.setAppCacheEnabled(true);
        // 设置 AppCache 最大缓存值(现在官方已经不提倡使用，已废弃)
        settings.setAppCacheMaxSize(8 * 1024 * 1024);
        // Android 私有缓存存储，如果你不调用setAppCachePath方法，WebView将不会产生这个目录
        settings.setAppCachePath(AppContext.instance().getCacheDir().getAbsolutePath());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 解决 Android 5.0 上 WebView 默认不允许加载 Http 与 Https 混合内容
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        // 不显示滚动条
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        //设置谷歌引擎
        webView.setBrowserChromeClient(new MyBrowserChromeClient(webView));
        webView.addJavascriptInterface(new ShareJavaScriptInterface(context), "Native");
        //视频自动播放
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        webView.setGson(new Gson());
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        //设置打开的页面地址
        webView.loadUrl(webUrl);

        //设置背景透明,去四个角
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(view);
        //设置宽度充满屏幕
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        return dialog;
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
        }else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            //用户取消
            Log.i(TAG, "Purchase cancel");
            loadingView.setVisibility(View.GONE);
        } else {
            //支付错误
            Log.i(TAG, "Pay result error,code=" + billingResult.getResponseCode() + "\nerrorMsg=" + billingResult.getDebugMessage());
            loadingView.setVisibility(View.GONE);
        }
    }

    //消耗商品
    private void consumePuchase(final Purchase purchase, final int state) {
        loadingView.setVisibility(View.VISIBLE);
        String packageName = purchase.getPackageName();
        if (StringUtil.isEmpty(packageName)) {
            packageName = AppContext.instance().getApplicationInfo().packageName;
        }
        List<String> sku = purchase.getSkus();
        String pToken = purchase.getPurchaseToken();
        ConsumeParams.Builder consumeParams = ConsumeParams.newBuilder();
        consumeParams.setPurchaseToken(purchase.getPurchaseToken());
        String finalPackageName = packageName;
        billingClient.consumeAsync(consumeParams.build(), (billingResult, purchaseToken) -> {
            Log.i(TAG, "onConsumeResponse, code=" + billingResult.getResponseCode());
            loadingView.setVisibility(View.GONE);
            try {
                AppContext.instance().logEvent(AppsFlyerEvent.Successful_top_up, vipPackageItemEntity.getPrice(), purchase);
            } catch (Exception e) {

            }
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.i(TAG, "onConsumeResponse,code=BillingResponseCode.OK");
//                AppContext.instance().validateGooglePlayLog(purchase, sel_goodsEntity.getPrice());
                paySuccessNotify(finalPackageName, orderNumber, sku, pToken, billingResult.getResponseCode(),vipPackageItemEntity);
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

    private class MyBrowserViewClient extends BrowserView.BrowserViewClient {


        /**
         * @return android.webkit.WebResourceResponse
         * @Desc TODO(拦截网页加载图片 。 安卓本地做缓存处理 。 提高网页加载图片速度 ；)
         * @author 彭石林
         * @parame [view, request]
         * @Date 2021/9/22
         */
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String usrPath = request.getUrl().getPath();
            if (usrPath.endsWith(".png") || usrPath.endsWith(".jpg") || usrPath.endsWith(".jpeg")) {
                String mimeType = null;
                if (usrPath.endsWith(".png")) {
                    mimeType = "image/png";
                } else if (usrPath.endsWith(".jpg") || usrPath.endsWith(".jpeg")) {
                    mimeType = "image/jpeg";
                }
                String url = request.getUrl().toString();
                byte[] bytes = syncLoad(url, usrPath);
                if (bytes != null) {
                    return new WebResourceResponse(
                            mimeType, "UTF-8",
                            new ByteArrayInputStream(bytes));
                }
            }
            return null;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            iv_default.setVisibility(View.GONE);
        }
    }

    private class MyBrowserChromeClient extends BrowserView.BrowserChromeClient {

        private MyBrowserChromeClient(BrowserView view) {
            super(view);
        }

        /**
         * 收到网页标题
         */
        @Override
        public void onReceivedTitle(WebView view, String title) {
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
        }

        @SuppressWarnings("unused")
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
            this.openFileChooser(uploadMsg);
        }

        @SuppressWarnings("unused")
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
            this.openFileChooser(uploadMsg);
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            return true;
        }

        /**
         * 收到加载进度变化
         */
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
        }
    }

    //进行支付
    private void pay(String payCode) {
        List<String> skuList = new ArrayList<>();
        skuList.add(payCode);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(GooglePayInApp == true ? BillingClient.SkuType.SUBS : BillingClient.SkuType.INAPP);
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


    //查询最近的购买交易，并消耗商品
    private void queryAndConsumePurchase() {
        loadingView.setVisibility(View.VISIBLE);
        //queryPurchases() 方法会使用 Google Play 商店应用的缓存，而不会发起网络请求
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

    //创建订单
    public void createOrder(Integer goodId, String payCode) {
        loadingView.setVisibility(View.VISIBLE);
        //1购买商品  2订阅商品
        int types = GooglePayInApp == false ? 1 : 2;
        Injection.provideDemoRepository()
                .createOrder(goodId, types, 2, null)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<CreateOrderEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CreateOrderEntity> response) {
                        loadingView.setVisibility(View.GONE);
                        orderNumber = response.getData().getOrderNumber();
                        //会员天数
                        pay_good_day = response.getData().getActual_value();
                        pay(payCode);
                    }

                    @Override
                    public void onError(RequestException e) {
                        super.onError(e);
                        loadingView.setVisibility(View.GONE);
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

    //支付成功上报
    public void paySuccessNotify(String packageName, String orderNumber, List<String> productId, String token, Integer event,VipPackageItemEntity vipItemEntity) {
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
                        //dialog.dismiss();
                        try{
                            if(vipItemEntity==null){
                                ToastUtils.showShort(R.string.playfun_coin_custom_text);
                                return;
                            }
                            if (GooglePayInApp) {
                                RxBus.getDefault().post(new UserUpdateVipEvent(Utils.formatday.format(Utils.addDate(new Date(), pay_good_day)), 1));
                            } else {
                                if (confirmOnclick != null) {
                                    int countCoin = 0;
                                    Integer purchased = vipItemEntity.getPurchased();
                                    if(purchased == null){
                                        purchased = -1;
                                    }
                                    //未进行首充
                                    if (!ObjectUtils.isEmpty(purchased) && purchased.intValue() == 0) {
                                        Integer actualValue = vipItemEntity.getActualValue();
                                        Integer giveCoin = vipItemEntity.getGiveCoin();
                                        if (actualValue != null) {
                                            countCoin += actualValue.intValue();
                                        }
                                        if (giveCoin != null) {
                                            countCoin += giveCoin.intValue();
                                        }

                                    } else {
                                        countCoin = vipItemEntity.getActualValue();
                                    }
                                    //5秒最多发一次
                                    if(!FastClickUtil.isFastCallFun("WebViewDialogSuccess")){
                                        if (confirmOnclick != null) {
                                            confirmOnclick.vipRechargeDiamondSuccess(dialog, countCoin);
                                        }
                                    }
                                }
                            }
                        }catch (Exception e){
                            ToastUtils.showShort(R.string.playfun_coin_custom_text);
                        }

                    }

                    @Override
                    public void onError(RequestException e) {
                        loadingView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public interface  ConfirmOnclick {
        //跳转到vip界面
         void webToVipRechargeVC(Dialog dialog);

        //更多钻石储值
         void moreRechargeDiamond(Dialog dialog);

        //充值钻石
         void vipRechargeDiamondSuccess(Dialog dialog, Integer coinMoney);

         void cancel();
    }

    public class ShareJavaScriptInterface {
        Context mContext;

        ShareJavaScriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public String getToken() {
            String token = Injection.provideDemoRepository().readLoginInfo().getToken();
            Log.e("当前给H5token",token);
            return token;
        }

        @JavascriptInterface
        public void webToVipRechargeVC() {
            if (confirmOnclick != null) {
                confirmOnclick.webToVipRechargeVC(dialog);
            }
        }

        @JavascriptInterface
        public void moreRechargeDiamond() {
            if (confirmOnclick != null) {
                confirmOnclick.moreRechargeDiamond(dialog);
            }
        }

        @JavascriptInterface
        public void nonVipRecharge(String data) {
            GooglePayInApp = true;
            //购买商品
            //vip充值
            vipPackageItemEntity = new Gson().fromJson(data, VipPackageItemEntity.class);
            if(vipPackageItemEntity!=null && vipPackageItemEntity.getGoogleGoodsId()!=null){
                String googleGoodsId = vipPackageItemEntity.getGoogleGoodsId();
                //创建订单
                createOrder(vipPackageItemEntity.getId(), googleGoodsId);
            }
        }

        @JavascriptInterface
        public void vipRechargeDiamond(String data) {
            GooglePayInApp = false;
            //购买商品
            vipPackageItemEntity = new Gson().fromJson(data, VipPackageItemEntity.class);
            if(vipPackageItemEntity!=null && vipPackageItemEntity.getGoogleGoodsId()!=null){
                String googleGoodsId = vipPackageItemEntity.getGoogleGoodsId();
                //创建订单
                createOrder(vipPackageItemEntity.getId(), googleGoodsId);
            }
        }

        @JavascriptInterface
        public void back() {
            dialog.dismiss();
        }

    }
}
