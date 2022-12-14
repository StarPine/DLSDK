package com.dl.playfun.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
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

import com.dl.lib.util.FastClickUtil;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetailsParams;
import com.blankj.utilcode.util.ObjectUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.BillingClientLifecycle;
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
import com.dl.playfun.utils.Utils;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Author: ?????????
 * Time: 2021/12/22 20:03
 * Description: This is WebViewDialog
 */
public class WebViewDialog extends BaseDialog {
    private final static String TAG = "WebViewDialog??????";
    private final Context context;
    private Dialog dialog;
    private final String webUrl;
    private final AppCompatActivity mActivity;
    private final ConfirmOnclick confirmOnclick;
    private ImageView iv_default;
    //????????????
    public Integer pay_good_day = 0;
    private volatile VipPackageItemEntity vipPackageItemEntity = new VipPackageItemEntity();
    private String orderNumber = null;
    //????????????????????? or  ??????vip
    private boolean GooglePayInApp = false;
    private ViewGroup loadingView;
    /**
     * ??????????????????
     */
    public BillingClientLifecycle billingClientLifecycle;

    public WebViewDialog(@NonNull Context context, AppCompatActivity activity, String url, ConfirmOnclick confirmOnclick) {
        super(context);
        this.context = context;
        this.mActivity = activity;
        this.webUrl = url;
        this.confirmOnclick = confirmOnclick;
        initGooglePay();
    }


    private void initGooglePay(){
        this.billingClientLifecycle = ((AppContext)mActivity.getApplication()).getBillingClientLifecycle();
        this.billingClientLifecycle.PAYMENT_SUCCESS.observe(this, billingPurchasesState -> {
            Log.e("BillingClientLifecycle","????????????????????????");
            switch (billingPurchasesState.getBillingFlowNode()){
                //??????????????????
                case querySkuDetails:
                    break;
                case launchBilling: //????????????
                    break;
                case purchasesUpdated: //?????????????????? ????????????????????? or ????????????
                    break;
                case acknowledgePurchase:  // ???????????????????????? --> ?????????????????? ?????????????????????????????????????????????????????????????????????????????????????????? ?????? ?????????????????? ????????????
                    Purchase purchase = billingPurchasesState.getPurchase();
                    if(purchase!=null){
                        try {
                            AppContext.instance().logEvent(AppsFlyerEvent.Successful_top_up, vipPackageItemEntity.getPrice(), purchase);
                        } catch (Exception e) {

                        }
                        String packageName = purchase.getPackageName();
                        paySuccessNotify(packageName, orderNumber, purchase.getSkus(), purchase.getPurchaseToken(), 1,vipPackageItemEntity);
                        Log.e("BillingClientLifecycle","dialog?????????????????????"+purchase.toString());
                    }
                    break;
            }
        });
        this.billingClientLifecycle.PAYMENT_FAIL.observe(this, billingPurchasesState -> {
            Log.e("BillingClientLifecycle","????????????????????????");
            switch (billingPurchasesState.getBillingFlowNode()){
                //??????????????????-->??????
                case querySkuDetails:
                    break;
                case launchBilling: //????????????-->??????
                    break;
                case purchasesUpdated: //?????????????????? ????????????????????? or ???????????? -->??????
                    break;
                case acknowledgePurchase:  // ???????????????????????? --> ?????????????????? ?????????????????????????????????????????????????????????????????????????????????????????? ?????? ?????????????????? ???????????? -->??????
                    break;
            }
        });
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
        // ??????????????????
        settings.setAllowFileAccess(true);
        //?????????????????????
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //??????WebView?????????????????????Javascript??????
        settings.setJavaScriptEnabled(true);
        //?????????????????????true????????????Webivew??????<meta>?????????viewport??????
        settings.setUseWideViewPort(true);
        //??????????????????????????????
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        //??????????????????
        settings.setLoadWithOverviewMode(true);
        //???js????????????????????????
        webView.setBrowserViewClient(new MyBrowserViewClient());
        // ??????????????????????????????????????????????????????????????????
        settings.setLoadsImagesAutomatically(true);
        // ?????? DOM ??????????????????????????????????????????????????????
        settings.setDomStorageEnabled(true);
        // ?????? WebView ???????????????
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        // ????????????????????????
        settings.setAppCacheEnabled(true);
        // ?????? AppCache ???????????????(?????????????????????????????????????????????)
        settings.setAppCacheMaxSize(8 * 1024 * 1024);
        // Android ???????????????????????????????????????setAppCachePath?????????WebView???????????????????????????
        settings.setAppCachePath(AppContext.instance().getCacheDir().getAbsolutePath());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // ?????? Android 5.0 ??? WebView ????????????????????? Http ??? Https ????????????
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        // ??????????????????
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        //??????????????????
        webView.setBrowserChromeClient(new MyBrowserChromeClient(webView));
        webView.addJavascriptInterface(new ShareJavaScriptInterface(context), "Native");
        //??????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        webView.setGson(new Gson());
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        setCookie(mActivity,webUrl);
        //???????????????????????????
        webView.loadUrl(webUrl);

        //??????????????????,????????????
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(view);
        //????????????????????????
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER); //?????????dialog?????????
        window.getDecorView().setPadding(0, 0, 0, 0); //????????????
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //????????????????????????
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        return dialog;
    }

    public static void setCookie(Context context, String url) {
        try {
            CookieSyncManager.createInstance(context);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.removeSessionCookie();//??????
            cookieManager.removeAllCookie();
            cookieManager.setCookie(url, "local="+context.getString(R.string.playfun_local_language));
            cookieManager.setCookie(url, "appId="+ AppConfig.APPID);
            CookieSyncManager.getInstance().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MyBrowserViewClient extends BrowserView.BrowserViewClient {


        /**
         * @return android.webkit.WebResourceResponse
         * @Desc TODO(???????????????????????? ??? ??????????????????????????? ??? ?????????????????????????????? ???)
         * @author ?????????
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
         * ??????????????????
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
         * ????????????????????????
         */
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
        }
    }

    //????????????
    private void pay(String payCode) {
        List<String> skuList = new ArrayList<>();
        skuList.add(payCode);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(GooglePayInApp ? BillingClient.SkuType.SUBS : BillingClient.SkuType.INAPP);
        billingClientLifecycle.querySkuDetailsLaunchBillingFlow(params,mActivity,orderNumber);
    }

    //????????????
    public void createOrder(Integer goodId, String payCode) {
        loadingView.setVisibility(View.VISIBLE);
        //1????????????  2????????????
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
                        //????????????
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

    //??????????????????
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
                                    //???????????????
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
                                    //5??????????????????
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
        //?????????vip??????
         void webToVipRechargeVC(Dialog dialog);

        //??????????????????
         void moreRechargeDiamond(Dialog dialog);

        //????????????
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
            //????????????
            //vip??????
            vipPackageItemEntity = new Gson().fromJson(data, VipPackageItemEntity.class);
            if(vipPackageItemEntity!=null && vipPackageItemEntity.getGoogleGoodsId()!=null){
                String googleGoodsId = vipPackageItemEntity.getGoogleGoodsId();
                //????????????
                createOrder(vipPackageItemEntity.getId(), googleGoodsId);
            }
        }

        @JavascriptInterface
        public void vipRechargeDiamond(String data) {
            GooglePayInApp = false;
            //????????????
            vipPackageItemEntity = new Gson().fromJson(data, VipPackageItemEntity.class);
            if(vipPackageItemEntity!=null && vipPackageItemEntity.getGoogleGoodsId()!=null){
                String googleGoodsId = vipPackageItemEntity.getGoogleGoodsId();
                //????????????
                createOrder(vipPackageItemEntity.getId(), googleGoodsId);
            }
        }

        @JavascriptInterface
        public void back() {
            dialog.dismiss();
        }

    }
}
