package com.dl.playfun.ui.login.register;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetailsParams;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.BillingClientLifecycle;
import com.dl.playfun.app.ElkLogEventReport;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.databinding.FragmentFriendswilLwebviewBinding;
import com.dl.playfun.entity.GoodsEntity;
import com.dl.playfun.entity.TokenEntity;
import com.dl.playfun.entity.WebViewDataEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseFragment;
import com.dl.playfun.ui.main.MainFragment;
import com.dl.playfun.ui.mine.profile.PerfectProfileFragment;
import com.dl.playfun.ui.webview.BrowserView;
import com.dl.playfun.widget.action.StatusAction;
import com.dl.playfun.widget.action.StatusLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author: 彭石林
 * Time: 2022/10/18 17:49
 * Description: 交友意愿H5
 */
public class FriendsWillWebViewFragment extends BaseFragment<FragmentFriendswilLwebviewBinding, FriendsWillWebViewViewModel> implements StatusAction {

    BrowserView webView;
    String webUrl;

    private ProgressBar mProgressBar;
    private StatusLayout mStatusLayout;

    //回调JS方法-购买成功
    private final String buySuccess = "buySuccess()";
    //礼包购买成功
    private final String novice_items_successful = "novice_items_successful";
    //礼包购买失败
    private final String novice_items_fail = "novice_items_fail";
    //礼包购买意图
    private final String click_novice_items = "click_novice_items";

    public BillingClientLifecycle billingClientLifecycle;

    @Override
    public void initParam() {
            webUrl = ConfigManager.getInstance().getInterestWebUrl();
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_friendswil_lwebview;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public FriendsWillWebViewViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(FriendsWillWebViewViewModel .class);
    }
    @Override
    public void initData() {
        super.initData();
        ElkLogEventReport.reportLoginModule.reportClickDatingPurpose(ElkLogEventReport._expose,"enterDatingPurpose",null);
        billingClientLifecycle = ((AppContext) mActivity.getApplication()).getBillingClientLifecycle();
        if (billingClientLifecycle != null) {
            //查询并消耗本地历史订单类型： INAPP 支付购买  SUBS订阅
            billingClientLifecycle.queryAndConsumePurchase(BillingClient.SkuType.INAPP);
            billingClientLifecycle.queryAndConsumePurchase(BillingClient.SkuType.SUBS);
            billingClientLifecycle.queryPurchasesAsync(BillingClient.SkuType.INAPP);
            billingClientLifecycle.queryPurchasesAsync(BillingClient.SkuType.SUBS);
        }

        mStatusLayout = binding.hlBrowserHint;
        mProgressBar = binding.pbBrowserProgress;
        webView = binding.webView;
        // 设置 WebView 生命管控
        webView.setLifecycleOwner(this);
        WebSettings settings = binding.webView.getSettings();
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
        binding.webView.addJavascriptInterface(new ShareJavaScriptInterface(), "Native");
        //视频自动播放
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        webView.setGson(new Gson());
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        //正在加载网页动画
        showLoading();
        //设置打开的页面地址
        webView.loadUrl(webUrl);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        this.billingClientLifecycle.PAYMENT_SUCCESS.observe(this, billingPurchasesState -> {
            Log.e("BillingClientLifecycle", "支付购买成功回调");
            viewModel.dismissHUD();
            switch (billingPurchasesState.getBillingFlowNode()) {
                //查询商品阶段
                case querySkuDetails:
                    break;
                case launchBilling: //启动购买
                    break;
                case purchasesUpdated: //用户购买操作 可在此购买成功 or 取消支付
                    break;
                case acknowledgePurchase:  // 用户操作购买成功 --> 商家确认操作 需要手动确定收货（消耗这笔订单并且发货（给与用户购买奖励）） 否则 到达一定时间 自动退款
                    Purchase purchase = billingPurchasesState.getPurchase();
                    if (purchase != null) {
                        try {
                            AppContext.instance().logEvent(novice_items_successful, viewModel.goodsEntity.getPrice(), purchase);
                        } catch (Exception ignored) {

                        }
                        ElkLogEventReport.reportLoginModule.reportClickDatingPurpose(ElkLogEventReport._click,"noviceItemsSuccessful",null);
                        String packageName = purchase.getPackageName();
                        List<String> sku = purchase.getSkus();
                        String pToken = purchase.getPurchaseToken();
                        viewModel.paySuccessNotify(packageName, sku, pToken, 0);
                        binding.getRoot().post(()->{
                            webView.loadUrl("javascript:"+buySuccess);
                        });
                    }
                    break;
            }
        });
        this.billingClientLifecycle.PAYMENT_FAIL.observe(this, billingPurchasesState -> {
            Log.e("BillingClientLifecycle", "支付购买失败回调");
            AppContext.instance().logEvent(novice_items_fail);
            viewModel.dismissHUD();
            switch (billingPurchasesState.getBillingFlowNode()) {
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
        viewModel.payOnClick.observe(this, payCode -> {
            viewModel.showHUD();
            pay(payCode);
        });
    }

    /**
     * @return boolean
     * @Desc TODO(是否对用户按下返回按键放行)
     * @author 彭石林
     * @parame []
     * @Date 2021/9/4
     */
    @Override
    public boolean onBackPressedSupport() {
        if (webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.destroy();
        }
    }

    /**
     * 重新加载当前页
     */
    private void reload() {
        webView.reload();
    }

    @Override
    public StatusLayout getStatusLayout() {
        return mStatusLayout;
    }

    private class MyBrowserViewClient extends BrowserView.BrowserViewClient {

        /**
         * 网页加载错误时回调，这个方法会在 onPageFinished 之前调用
         */
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            // 这里为什么要用延迟呢？因为加载出错之后会先调用 onReceivedError 再调用 onPageFinished
            post(() -> showError(v -> reload()));
        }

        /**
         * 开始加载网页
         */
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        /**
         * 完成加载网页
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            mProgressBar.setVisibility(View.GONE);
            showComplete();
        }
    }

    private class MyBrowserChromeClient extends BrowserView.BrowserChromeClient {

        private MyBrowserChromeClient(BrowserView view) {
            super(view);
        }

        /**
         * 收到加载进度变化
         */
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            mProgressBar.setProgress(newProgress);
        }
    }

    public class ShareJavaScriptInterface {
        @JavascriptInterface
        public String getMultilingualFlag() {
            return StringUtils.getString(R.string.playfun_local_language);
        }
        @JavascriptInterface
        public String getCurrentUserInfo() {
            WebViewDataEntity webViewDataEntity = new WebViewDataEntity();
            //当前配置
            WebViewDataEntity.SettingInfo settingInfo = new WebViewDataEntity.SettingInfo();
            settingInfo.setAppId(AppConfig.APPID);
            settingInfo.setCurrentLanguage(StringUtils.getString(R.string.playfun_local_language));
            AppRepository appRepository = ConfigManager.getInstance().getAppRepository();
            String userToken = null;
            TokenEntity tokenEntity = appRepository.readLoginInfo();
            if(ObjectUtils.isNotEmpty(tokenEntity)){
                userToken = tokenEntity.getToken();
            }
            settingInfo.setCurrentToken(userToken);
            settingInfo.setCurrentVersion(AppConfig.VERSION_NAME);
            webViewDataEntity.setSettingInfo(settingInfo);
            //当前本地用户
            webViewDataEntity.setUserInfo(appRepository.readUserData());
            return GsonUtils.toJson(webViewDataEntity);
        }

        //购买礼包
        @JavascriptInterface
        public void initiateDiamondStored(String eventData){
            if(!StringUtils.isEmpty(eventData)){
                viewModel.goodsEntity = GsonUtils.fromJson(eventData, GoodsEntity.class);
                if(viewModel.goodsEntity!=null){
                    ElkLogEventReport.reportLoginModule.reportClickDatingPurpose(ElkLogEventReport._click,"noviceItems",null);
                    AppContext.instance().logEvent(click_novice_items);
                    viewModel.createOrder();
                }
            }
        }
        //跳过
        @JavascriptInterface
        public void skipFriendsWill(String eventData){
            ElkLogEventReport.reportLoginModule.reportClickDatingPurpose(ElkLogEventReport._click,"jumpOver",null);
            startMainFragment(eventData,false);
        }
        //下一步
        @JavascriptInterface
        public void setNextAction(String eventData){
            startMainFragment(eventData,true);
        }
    }

    private void startMainFragment(String eventData,boolean nextPage){
        if(!StringUtils.isEmpty(eventData)){
            Map<String,String> mapData = GsonUtils.fromJson(eventData, Map.class);
            if(ObjectUtils.isNotEmpty(mapData)){
                String keyValue = mapData.get("event");
                if(!StringUtils.isEmpty(keyValue)){
                    AppContext.instance().logEvent(keyValue);
                    if (nextPage){
                        ElkLogEventReport.reportLoginModule.reportClickDatingPurpose(ElkLogEventReport._click,"datingPurposeNext",keyValue);
                    }
                    //结束当前页面去往主页
                    viewModel.popAllTo(new MainFragment());
                }
            }
        }
    }

    //进行支付
    private void pay(String payCode) {
        List<String> skuList = new ArrayList<>();
        skuList.add(payCode);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        if (viewModel.goodsEntity.getType() == 2){
            params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        }else {
            params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        }
        billingClientLifecycle.querySkuDetailsLaunchBillingFlow(params,mActivity,viewModel.orderNumber);
    }
}
