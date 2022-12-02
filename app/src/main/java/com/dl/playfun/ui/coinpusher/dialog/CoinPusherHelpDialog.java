package com.dl.playfun.ui.coinpusher.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.databinding.DataBindingUtil;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.databinding.DialogCoinpusherHelpBinding;
import com.dl.playfun.entity.TokenEntity;
import com.dl.playfun.entity.WebViewDataEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseDialog;
import com.dl.playfun.ui.webview.BrowserView;
import com.dl.playfun.utils.WebViewUtils;
import com.dl.playfun.widget.action.StatusAction;
import com.dl.playfun.widget.action.StatusLayout;

/**
 * Author: 彭石林
 * Time: 2022/9/23 17:15
 * Description: (帮助文档)
 */
public class CoinPusherHelpDialog extends BaseDialog implements StatusAction {

    DialogCoinpusherHelpBinding binding;
    private final Context mContext;
    private final String webViewUrl;

    public CoinPusherHelpDialog(Context context,String webViewUrl) {
        super(context);
        this.mContext = context;
        this.webViewUrl = webViewUrl;
        initView();
        initClickListener();
    }

    private void initView(){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        binding = DataBindingUtil.inflate(inflater, R.layout. dialog_coinpusher_help, null, false);
        //支持LiveData绑定xml，数据改变，UI自动会更新
        binding.setLifecycleOwner(this);
        binding.imgClose.setOnClickListener(v -> {
            cancel();
        });
    }

    public void destroy() {
        try {
            binding.webView.destroy();
        }catch (Exception ignored) {

        }
    }

    public void show() {
        WebViewUtils.initSettings(binding.webView);
        binding.webView.setLifecycleOwner(this);
        binding.webView.setBrowserViewClient(new MyWebViewClient());
        binding.webView.setBrowserChromeClient(new BrowserView.BrowserChromeClient(binding.webView) {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if(!TextUtils.isEmpty(title)){
                    binding.tvTitle.post(()->binding.tvTitle.setText(title));
                }
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                binding.pbBrowserProgress.setProgress(newProgress);
            }
        });
        binding.webView.addJavascriptInterface(new ShareJavaScriptInterface(), "Native");
        showLoading();
        binding.webView.loadUrl(webViewUrl);
        //设置背景透明,去四个角
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(binding.getRoot());
        //设置宽度充满屏幕
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.show();
    }


    private void initClickListener() {
        binding.imgClose.setOnClickListener(v -> {
            binding.webView.destroy();
            dismiss();
        });
    }

    @Override
    public StatusLayout getStatusLayout() {
        return binding.hlBrowserHint;
    }

    void reload() {
        binding.webView.reload();
    }

    private class ShareJavaScriptInterface{
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
    }

    private class MyWebViewClient extends BrowserView.BrowserViewClient {

        /**
         * 网页加载错误时回调，这个方法会在 onPageFinished 之前调用
         */
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            // 这里为什么要用延迟呢？因为加载出错之后会先调用 onReceivedError 再调用 onPageFinished
            showError(v -> reload());
        }

        /**
         * 开始加载网页
         */
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            binding.pbBrowserProgress.setVisibility(View.VISIBLE);
        }

        /**
         * 完成加载网页
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            binding.pbBrowserProgress.setVisibility(View.GONE);
            showComplete();
            view.loadUrl("javascript:" + url);
        }
    }

}
