package com.dl.playfun.ui.mine.invitewebdetail;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.appsflyer.CreateOneLinkHttpTask;
import com.appsflyer.share.LinkGenerator;
import com.appsflyer.share.ShareInviteHelper;
import com.blankj.utilcode.util.IntentUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentInviteWebDetailBinding;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.utils.SoftKeyBoardListener;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.widget.dialog.MMAlertDialog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * 邀请网页详细
 *
 * @author wulei
 */
public class InviteWebDetailFragment extends BaseToolbarFragment<FragmentInviteWebDetailBinding, InviteWebDetailViewModel> {

    public static final String ARG_WEB_URL = "arg_web_url";
    public static final String ARG_USER_ID = "arg_user_id";
    final Handler myHandler = new Handler();
    private final String TAG = "邀请网页详细";
    protected InputMethodManager inputMethodManager;
    private String url;
    private String userId;
    private boolean SoftKeyboardShow = false;

    public static Bundle getStartBundle(String url) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_WEB_URL, url);
        return bundle;
    }

    public static Bundle getStartBundle(String url, String userId) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_WEB_URL, url);
        bundle.putString(ARG_USER_ID, userId);
        return bundle;
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_invite_web_detail;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initParam() {
        super.initParam();
        url = getArguments().getString(ARG_WEB_URL);
        userId = getArguments().getString(ARG_USER_ID);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        inputMethodManager = (InputMethodManager) this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        SoftKeyBoardListener.setListener(mActivity, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                SoftKeyboardShow = true;
            }

            @Override
            public void keyBoardHide(int height) {
                SoftKeyboardShow = false;
            }
        });
        viewModel.clickPay.observe(this, payCode -> pay(payCode));
    }

    /**
     * 如果软键盘显示就隐藏软键盘
     */
    protected void hideSoftKeyboard() {
        if (SoftKeyboardShow) {
            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }
    }

    @Override
    public InviteWebDetailViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(InviteWebDetailViewModel.class);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void initData() {
        super.initData();
        WebSettings settings = binding.webView.getSettings();
//        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        //如果不设置WebViewClient，请求会跳转系统浏览器
        binding.webView.setWebViewClient(new MyWebViewClient());
        binding.webView.setWebChromeClient(new WebChromeClient());
        binding.webView.addJavascriptInterface(new ShareJavaScriptInterface(mActivity), "Native");
        if (ConfigManager.getInstance().isMale()) {
            viewModel.billingClient = BillingClient.newBuilder(getContext()).setListener(new PurchasesUpdatedListener() {
                @Override
                public void onPurchasesUpdated(@NonNull @NotNull BillingResult billingResult, @Nullable @org.jetbrains.annotations.Nullable List<Purchase> purchases) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                        for (final Purchase purchase : purchases) {
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                // Acknowledge purchase and grant the item to the user
                                Log.i(TAG, "Purchase success");
                                //确认购买交易，不然三天后会退款给用户
                                if (!purchase.isAcknowledged()) {
                                    acknowledgePurchase(purchase);
                                }
                                //TODO:发放商品
                            } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                                //需要用户确认
                                Log.i(TAG, "Purchase pending,need to check");
                            }
                        }
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                        //用户取消
                        Log.i(TAG, "Purchase cancel");
                    } else {
                        //支付错误
                        Log.i(TAG, "Pay result error,code=" + billingResult.getResponseCode() + "\nerrorMsg=" + billingResult.getDebugMessage());
                    }
                }
            }).enablePendingPurchases().build();
            //连接google服务器
            viewModel.billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(@NonNull @NotNull BillingResult billingResult) {
                    Log.i(TAG, "billingResult Code=" + billingResult.getResponseCode());
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        // The BillingClient is ready. You can query purchases here.
                        Log.i(TAG, "Init success,The BillingClient is ready");
                        //每次进行重连的时候都应该消耗之前缓存的商品，不然可能会导致用户支付不了
                        viewModel.queryAndConsumePurchase();
                    } else {
                        Log.i(TAG, "Init failed,The BillingClient is not ready,code=" + billingResult.getResponseCode() + "\nMsg=" + billingResult.getDebugMessage());
                        ToastUtils.showShort(billingResult.getDebugMessage());
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                    //通过调用 startConnection() 方法在下一个请求// Google Play 时重新启动连接。
                }
            });
        }
    }

    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        super.onEnterAnimationEnd(savedInstanceState);
        if (url != null) {
            binding.webView.loadUrl(url);
//            if(ConfigManager.getInstance().isMale()){
//                binding.webView.loadUrl("file:///android_asset/index.html");
//            }else{
//                binding.webView.loadUrl("file:///android_asset/woman.html");
//            }

        }
    }

    //确认订单
    private void acknowledgePurchase(Purchase purchase) {
        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Acknowledge purchase success");
                    String packageName = purchase.getPackageName();
                    String sku = purchase.getSku();
                    String pToken = purchase.getPurchaseToken();
                    viewModel.paySuccessNotify(packageName, sku, pToken, billingResult.getResponseCode());
                } else {
                    String packageName = purchase.getPackageName();
                    String sku = purchase.getSku();
                    String pToken = purchase.getPurchaseToken();
                    viewModel.paySuccessNotify(packageName, sku, pToken, billingResult.getResponseCode());
                    Log.i(TAG, "Acknowledge purchase failed,code=" + billingResult.getResponseCode() + ",\nerrorMsg=" + billingResult.getDebugMessage());
                }
            }
        };
        viewModel.billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
    }

    //进行支付
    private void pay(String payCode) {
        List<String> skuList = new ArrayList<>();
        skuList.add(payCode);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        viewModel.billingClient.querySkuDetailsAsync(params.build(),
                (billingResult, skuDetailsList) -> {
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
                                int responseCode = viewModel.billingClient.launchBillingFlow(mActivity, flowParams).getResponseCode();
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
                        }
                    } else {
                        Log.i(TAG, "Get SkuDetails Failed,Msg=" + billingResult.getDebugMessage());
                    }
                });
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView webView, String url) {
            webView.loadUrl("javascript:" + url);
        }
    }

    public class ShareJavaScriptInterface {
        Context mContext;

        ShareJavaScriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void back() {
            pop();
        }

        @JavascriptInterface
        public void AlertShareVip(String goodId) {
            MMAlertDialog.AlertShareVip(mActivity, true, new MMAlertDialog.RegUserAlertInterface() {
                @Override
                public void confirm(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (StringUtil.isEmpty(goodId)) {
                        ToastUtils.showShort(R.string.invite_web_detail_error3);
                        return;
                    }
                    viewModel.querySkuOrder(goodId);
                }
            }).show();
        }

        @JavascriptInterface
        public void AlertShareVipGet() {
            AlertDialog alertDialog = MMAlertDialog.AlertShareVipGet(mActivity, true, new MMAlertDialog.DilodAlertMessageInterface() {
                @Override
                public void confirm(DialogInterface dialog, int which, int sel_Index, String code) {
                    dialog.dismiss();
                    if (!StringUtil.isEmpty(code)) {
                        viewModel.saveInviteCode(code);
                    } else {
                        ToastUtils.showShort(R.string.invite_web_detail_error4);
                    }
                }

                @Override
                public void cancel(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.show();
            alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    hideSoftKeyboard();
                }
            });
        }

        @JavascriptInterface
        public void AlertShareWriteCode(String code) {
            viewModel.saveInviteCode(code);
        }


        @JavascriptInterface
        public void shareText(String title, String url) {
            viewModel.showHUD();
            LinkGenerator linkGenerator = ShareInviteHelper.generateInviteUrl(mActivity);
            //渠道
            linkGenerator.setChannel("GoogleAppstore");//
            //自定义用户ID
            linkGenerator.setReferrerCustomerId(userId);
            //重定向：
            linkGenerator.addParameter("deep_link_value", "AppStore-PlayChat-Google-TW");
            linkGenerator.addParameter("is_retargeting", "true");
            linkGenerator.addParameter("af_sub1", userId);
            linkGenerator.addParameter("af_sub2", "invite");
            // optional - set a brand domain to the user invite link
            //LinHuang:
            CreateOneLinkHttpTask.ResponseListener listener = new CreateOneLinkHttpTask.ResponseListener() {
                @Override
                public void onResponse(String oneLink) {
                    oneLink = oneLink.substring(oneLink.lastIndexOf('/') + 1);
                    String urls_one = url + "?code=" + userId + "&onelink=" + oneLink;
                    // write logic to let user share the invite link
                    Map<String, String> mapData = new HashMap<>();
                    mapData.put("af_sub1", userId);
                    mapData.put("customer_user_id", userId);
                    mapData.put("af_sub2", "invite");
                    ShareInviteHelper.logInvite(mActivity, "com.ld.play.chat-Taiwan", mapData);

                    AppContext.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            viewModel.dismissHUD();
                            ClipboardManager clipboard = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("playchat", title + "\n" + urls_one);
                            clipboard.setPrimaryClip(clip);
                        }
                    });
                    Intent intent = IntentUtils.getShareTextIntent(title + "\n" + urls_one);
                    startActivity(intent);
                }

                @Override
                public void onResponseError(String s) {
                    // handle response error
                }
            };
            linkGenerator.generateLink(mActivity, listener);
        }

        @JavascriptInterface
        public void shareImage(String content, String imgUrl) {
            if (imgUrl == null) {
                return;
            }
            Intent intent = IntentUtils.getShareImageIntent(content, Uri.parse(imgUrl));
            startActivity(intent);
        }
    }
}
