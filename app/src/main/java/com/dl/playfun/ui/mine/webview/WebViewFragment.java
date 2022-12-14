package com.dl.playfun.ui.mine.webview;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.ElkLogEventReport;
import com.dl.playfun.databinding.WebviewFragmentBinding;
import com.dl.playfun.entity.GoodsEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseFragment;
import com.dl.playfun.ui.certification.certificationfemale.CertificationFemaleFragment;
import com.dl.playfun.ui.certification.certificationmale.CertificationMaleFragment;
import com.dl.playfun.ui.webview.BrowserView;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.utils.AutoSizeUtils;
import com.dl.playfun.utils.LogUtils;
import com.dl.playfun.widget.action.StatusAction;
import com.dl.playfun.widget.action.StatusLayout;
import com.dl.playfun.widget.coinrechargesheet.CoinExchargeItegralDialog;
import com.dl.playfun.widget.coinrechargesheet.CoinRechargeSheetView;
import com.dl.playfun.widget.dialog.MVDialog;
import com.google.gson.Gson;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Author: ?????????
 * Time: 2021/11/9 1:24
 * Description: This is FukubukuroViewFragment
 */
public class WebViewFragment extends BaseFragment<WebviewFragmentBinding, WebViewViewModel> implements StatusAction {

    BrowserView webView;
    String webUrl;

    int RESULT_CODE = 0;
    ValueCallback<Uri> mUploadMessage;
    ValueCallback<Uri[]> mUploadMessageArray;

    private ProgressBar mProgressBar;
    private StatusLayout mStatusLayout;


    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AutoSizeUtils.applyAdapt(this.getResources());
        return R.layout.webview_fragment;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initParam() {
        webUrl = getArguments().getString("link", AppConfig.FukubukuroWebUrl);
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

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void initData() {
        super.initData();

        mStatusLayout = binding.hlBrowserHint;
        mProgressBar = binding.pbBrowserProgress;
        webView = binding.webView;
        // ?????? WebView ????????????
        webView.setLifecycleOwner(this);
        WebSettings settings = binding.webView.getSettings();
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
        binding.webView.addJavascriptInterface(new ShareJavaScriptInterface(mActivity), "Native");
        //??????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        webView.setGson(new Gson());
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        //????????????????????????
        showLoading();
        setCookie(mActivity,webUrl);
        //???????????????????????????
        webView.loadUrl(webUrl);
    }

    public static void setCookie(Context context, String url) {
        try {
            CookieSyncManager.createInstance(context);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.removeSessionCookie();//??????
            cookieManager.removeAllCookie();
            cookieManager.setCookie(url, "local="+context.getString(R.string.playfun_local_language));
            cookieManager.setCookie(url, "appId="+AppConfig.APPID);
            CookieSyncManager.getInstance().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //??????????????????
    public void reloadWebRul(String wenUrl) {
        //????????????????????????
        showLoading();
        //???????????????????????????
        webView.loadUrl(wenUrl);
    }

    @Override
    public WebViewViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(WebViewViewModel.class);
    }

    /**
     * @return boolean
     * @Desc TODO(???????????????????????????????????????)
     * @author ?????????
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
    public void initViewObservable() {
        super.initViewObservable();
    }

    public void pickFile() {
        Intent chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooserIntent.setType("image/*");
        startActivityForResult(chooserIntent, RESULT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == RESULT_CODE) {
            if (null == mUploadMessage && null == mUploadMessageArray) {
                return;
            }
            if (null != mUploadMessage && null == mUploadMessageArray) {
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }

            if (null == mUploadMessage && null != mUploadMessageArray) {
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                if (result != null) {
                    mUploadMessageArray.onReceiveValue(new Uri[]{result});
                }
                mUploadMessageArray = null;
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.destroy();
        }
    }

    @Override
    public StatusLayout getStatusLayout() {
        return mStatusLayout;
    }

    /**
     * ?????????????????????
     */
    private void reload() {
        webView.reload();
    }

    public void DialogCoinExchangeIntegralShow(Dialog dialog){
        CoinExchargeItegralDialog coinExchargeItegralSheetView = new CoinExchargeItegralDialog(WebViewFragment.this.getContext(),mActivity);
        coinExchargeItegralSheetView.setCoinRechargeSheetViewListener(new CoinExchargeItegralDialog.CoinExchargeIntegralAdapterListener() {
            @Override
            public void onPaySuccess(CoinExchargeItegralDialog sheetView, GoodsEntity sel_goodsEntity) {
                coinExchargeItegralSheetView.dismiss();
                dialog.dismiss();
                ToastUtils.showShort(R.string.playfun_dialog_exchange_integral_success);
            }
            @Override
            public void onPayFailed(CoinExchargeItegralDialog sheetView, String msg) {
                coinExchargeItegralSheetView.dismiss();
                ToastUtils.showShort(msg);
                AppContext.instance().logEvent(AppsFlyerEvent.Failed_to_top_up);
            }
        });
        coinExchargeItegralSheetView.show();
    }

    /**
     * {@link OnRefreshListener}
     */


    private class MyBrowserViewClient extends BrowserView.BrowserViewClient {

        /**
         * ???????????????????????????????????????????????? onPageFinished ????????????
         */
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            // ????????????????????????????????????????????????????????????????????? onReceivedError ????????? onPageFinished
            post(() -> showError(v -> reload()));
        }

        /**
         * ??????????????????
         */
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        /**
         * ??????????????????
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            mProgressBar.setVisibility(View.GONE);
            showComplete();
        }

        /**
         * @Desc TODO(??????????????????????????????????????????????????????????????????????????????????????????)
         * @author ?????????
         * @parame [view, request]
         * @return android.webkit.WebResourceResponse
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
            mUploadMessage = uploadMsg;
            pickFile();
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            mUploadMessageArray = filePathCallback;
            pickFile();
            return true;
        }

        /**
         * ????????????????????????
         */
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            mProgressBar.setProgress(newProgress);
        }
    }

    public class ShareJavaScriptInterface {
        Context mContext;

        ShareJavaScriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void toTaskTop() {
            ApiUitl.taskTop = true;
            pop();
        }

        @JavascriptInterface
        public String getLocal() {
            return "zh-TW";
        }

        //?????????????????????
        @JavascriptInterface
        public String getMultilingualFlag(){
            return mContext.getString(R.string.playfun_local_language);
        }

        @JavascriptInterface
        public void back() {
            pop();
        }

        @JavascriptInterface
        public void toTaskuroAction() {
            //????????????????????????
            ApiUitl.taskBottom = true;
            pop();
        }

        //??????????????????
        @JavascriptInterface
        public void toTaskChangeView() {
            ApiUitl.taskDisplay = true;
            pop();
            //
        }
        //????????????AppId
        @JavascriptInterface
        public String getAppId(){
            return AppConfig.APPID;
        }

        @JavascriptInterface
        public String getToken() {
            return viewModel.getToken();
        }

        @JavascriptInterface
        public String getVersionTask() {
            return AppConfig.VERSION_NAME;
        }

        @JavascriptInterface
        public void humanAuthentication() {//????????????
            LogUtils.i("humanAuthentication: ");
            MVDialog.getInstance(WebViewFragment.this.getContext())
                    .setTitele(getString(R.string.playfun_fragment_certification_tip))
                    .setContent(getString(R.string.playfun_fragment_certification_content))
                    .setConfirmText(getString(R.string.playfun_task_fragment_task_new11))
                    .chooseType(MVDialog.TypeEnum.CENTER)
                    .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
                        @Override
                        public void confirm(MVDialog dialog) {
                            if (ConfigManager.getInstance().getAppRepository().readUserData().getSex() == AppConfig.MALE) {
                                viewModel.start(CertificationMaleFragment.class.getCanonicalName());
                                return;
                            } else if (ConfigManager.getInstance().getAppRepository().readUserData().getSex() == AppConfig.FEMALE) {
                                viewModel.start(CertificationFemaleFragment.class.getCanonicalName());
                                return;
                            }
                            com.blankj.utilcode.util.ToastUtils.showShort(R.string.playfun_sex_unknown);
                            dialog.dismiss();
                        }
                    })
                    .chooseType(MVDialog.TypeEnum.CENTER)
                    .show();
        }

        @JavascriptInterface
        public void exchangeDiamond() {//????????????
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toRecharge();
                }
            });

        }

    }

    /**
     * ?????????
     */
    private void toRecharge() {
        ElkLogEventReport.reportCoinRecharge.reportSheetViewOpen(6);
        CoinRechargeSheetView coinRechargeFragmentView = new CoinRechargeSheetView(mActivity);
        coinRechargeFragmentView.show();
    }

}
