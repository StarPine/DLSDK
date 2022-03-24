package com.dl.playfun.ui.webview;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentPlayGamesBinding;
import com.dl.playfun.entity.GoodsEntity;
import com.dl.playfun.ui.base.BaseFragment;
import com.dl.playfun.widget.coinrechargesheet.CoinRechargeSheetView;
import com.dl.playfun.widget.dialog.MVDialog;
import com.github.lzyzsd.jsbridge.DeLiBridgeWebView;
import com.github.lzyzsd.jsbridge.OnBridgeCallback;
import com.google.gson.Gson;

import java.util.Map;

import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Author: 彭石林
 * Time: 2021/9/18 12:15
 * Description: This is PlayGameFragment1
 */
public class PlayGameFragment1 extends BaseFragment<FragmentPlayGamesBinding, PlayGameViewModel1> {

    public static String TAG = "闪闪游戏";
    int RESULT_CODE = 0;
    ValueCallback<Uri> mUploadMessage;
    ValueCallback<Uri[]> mUploadMessageArray;
    DeLiBridgeWebView webView;
    String webUrl;

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return R.layout.fragment_play_games;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public PlayGameViewModel1 initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(PlayGameViewModel1.class);
    }

    @Override
    public void initData() {
        super.initData();
        webView = binding.webView;
        WebView.setWebContentsDebuggingEnabled(true);
        WebSettings settings = webView.getSettings();
        settings.setCacheMode(WebSettings.LOAD_DEFAULT); // 不建议使用cache_else_network, 会破坏http cache机制
        settings.setDomStorageEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setBlockNetworkLoads(false);
        settings.setDatabaseEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setBlockNetworkImage(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setDisplayZoomControls(true);
        settings.setJavaScriptEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 解决 Android 5.0 上 WebView 默认不允许加载 Http 与 Https 混合内容
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webView.setWebChromeClient(new WebChromeClient());
        //使js可以调用安卓方法
        webView.setWebViewClient(new WebViewClient());
        // 第二个参数一定要是deliApp，且一定要继承BaseJavascriptInterface父类
        webView.addJavascriptInterface(new MainJavascrotInterface(webView.getCallbacks(), webView), "deliApp");
        webView.setGson(new Gson());
        // onBackPress交给webview处理，必须设置
        webView.setBackpressRunnable(new Runnable() {
            @Override
            public void run() {
                pop();
            }
        });
        webView.loadUrl("https://avggame.shangame.com/JoyH5/");

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
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.uc.sendWebBalance.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer value) {
                //调用js
                webView.callHandler("receiveBalance", String.valueOf(value), new OnBridgeCallback() {
                    @Override
                    public void onCallBack(String data) {
                        Log.e(TAG, "余额onCallBack: " + data);
                    }
                });
            }
        });
        viewModel.uc.sendWebUserData.observe(this, new Observer<Map<String, Object>>() {
            @Override
            public void onChanged(Map<String, Object> stringObjectMap) {
                String data = "";
                if (ObjectUtils.isEmpty(stringObjectMap)) {
                    data = null;
                } else {
                    data = new Gson().toJson(stringObjectMap);
                }
                //调用js
                webView.callHandler("receiveUserData", data, new OnBridgeCallback() {
                    @Override
                    public void onCallBack(String data) {
                        Log.e(TAG, "用户信息onCallBack: " + data);
                    }
                });
            }
        });
        viewModel.uc.loadCoinSheet.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {
                CoinRechargeSheetView coinRechargeSheetView = new CoinRechargeSheetView(mActivity);
                coinRechargeSheetView.show();
                coinRechargeSheetView.setCoinRechargeSheetViewListener(new CoinRechargeSheetView.CoinRechargeSheetViewListener() {
                    @Override
                    public void onPaySuccess(CoinRechargeSheetView sheetView, GoodsEntity sel_goodsEntity) {
                        sheetView.dismiss();
                        MVDialog.getInstance(PlayGameFragment1.this.getContext())
                                .setTitle(getStringByResId(R.string.recharge_coin_success))
                                .setConfirmText(getStringByResId(R.string.confirm))
                                .setConfirmOnlick(dialog -> {
                                    dialog.dismiss();
                                    //调用js
                                    webView.callHandler("topUpDiamondCall", "success", new OnBridgeCallback() {
                                        @Override
                                        public void onCallBack(String data) {
                                            Log.e(TAG, "支付onCallBack: " + data);
                                        }
                                    });
                                })
                                .chooseType(MVDialog.TypeEnum.CENTER)
                                .show();
                    }

                    @Override
                    public void onPayFailed(CoinRechargeSheetView sheetView, String msg) {
                        sheetView.dismiss();
                        ToastUtils.showShort(msg);
                        //调用js
                        webView.callHandler("topUpDiamondCall", "fail", new OnBridgeCallback() {
                            @Override
                            public void onCallBack(String data) {
                                Log.e(TAG, "支付失败onCallBack: " + data);
                            }
                        });
                    }
                });
            }
        });
    }

    public class MainJavascrotInterface extends DeLiBridgeWebView.BaseJavascriptInterface {

        public MainJavascrotInterface(Map<String, OnBridgeCallback> callbacks, DeLiBridgeWebView webView) {
            super(callbacks, webView);
        }

        @Override
        public String send(String data) {
            return "it is default response";
        }

        @JavascriptInterface
        public String getToken(String data, String callbackId) {
            return "Bearer " + viewModel.getToken();
        }

        @JavascriptInterface
        public String getBalance(String data, String callbackId) {
            viewModel.getBalance();
            return "success";
        }

        @JavascriptInterface
        public String getUserData(String data, String callbackId) {
            viewModel.getUserData();
            return "success";
        }

        @JavascriptInterface
        public String topUpDiamond(String data, String callbackId) {
            viewModel.uc.loadCoinSheet.postValue(null);
            return "success";
        }
    }
}
