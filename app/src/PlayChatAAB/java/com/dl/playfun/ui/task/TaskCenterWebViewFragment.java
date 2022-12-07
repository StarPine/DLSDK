package com.dl.playfun.ui.task;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.ElkLogEventReport;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.databinding.TaskCenterFragmentWebBinding;
import com.dl.playfun.entity.TokenEntity;
import com.dl.playfun.entity.WebViewDataEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseFragment;
import com.dl.playfun.ui.certification.certificationfemale.CertificationFemaleFragment;
import com.dl.playfun.ui.certification.certificationmale.CertificationMaleFragment;
import com.dl.playfun.ui.mine.myphotoalbum.MyPhotoAlbumFragment;
import com.dl.playfun.ui.mine.profile.EditProfileFragment;
import com.dl.playfun.ui.mine.setting.account.bind.CommunityAccountBindFragment;
import com.dl.playfun.ui.radio.issuanceprogram.IssuanceProgramFragment;
import com.dl.playfun.ui.webview.BrowserView;
import com.dl.playfun.utils.WebViewUtils;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.dl.playfun.widget.action.StatusAction;
import com.dl.playfun.widget.action.StatusLayout;
import com.google.gson.Gson;

import java.util.Map;
import java.util.Objects;

/**
 * @author Shuotao Gong
 * @time 2022/11/25
 */
public class TaskCenterWebViewFragment extends BaseFragment<TaskCenterFragmentWebBinding, BaseViewModel<AppRepository>> implements StatusAction {


    BrowserView webView;
    String webUrl;

    private ProgressBar mProgressBar;
    private StatusLayout mStatusLayout;

    private static final String TAG = "TASK_CENTER";

    private HomePageRouter router;

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.task_center_fragment_web;
    }

    @Override
    public BaseViewModel initViewModel() {
        return new ViewModelProvider(this).get(BaseViewModel.class);
    }

    @Override
    public void initData() {

        mStatusLayout = binding.hlBrowserHint;
        mProgressBar = binding.pbBrowserProgress;
        webView = binding.webView;
        // 设置 WebView 生命管控
        webView.setLifecycleOwner(this);
        WebViewUtils.initSettings(webView);
        //设置谷歌引擎
        //使js可以调用安卓方法
        webView.setBrowserViewClient(new MyBrowserViewClient());
        webView.setBrowserChromeClient(new MyBrowserChromeClient(webView));
        webView.addJavascriptInterface(new ShareJavaScriptInterface(), "Native");
        webView.setGson(new Gson());
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        //正在加载网页动画
        showLoading();
        webView.loadUrl(ConfigManager.getInstance().getAppRepository().readApiConfigManagerEntity().getPlayChatWebUrl() + AppConfig.TASK_CENTER_URL);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (webView != null) {
            webView.loadUrl("javascript:newData()");
        }
    }

    @Override
    public int initVariableId() {
        return 0;
    }

    public interface HomePageRouter {
        void routeTo(int pageId);
    }

    public void setOnClickListener(HomePageRouter router) {
        this.router = router;
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
        public String getCurrentUserInfo() {
            WebViewDataEntity webViewDataEntity = new WebViewDataEntity();
            //当前配置
            WebViewDataEntity.SettingInfo settingInfo = new WebViewDataEntity.SettingInfo();
            settingInfo.setAppId(AppConfig.APPID);
            settingInfo.setCurrentLanguage(StringUtils.getString(R.string.playfun_local_language));
            AppRepository appRepository = ConfigManager.getInstance().getAppRepository();
            String userToken = null;
            TokenEntity tokenEntity = appRepository.readLoginInfo();
            if (ObjectUtils.isNotEmpty(tokenEntity)) {
                userToken = tokenEntity.getToken();
            }
            settingInfo.setCurrentToken(userToken);
            settingInfo.setCurrentVersion(AppConfig.VERSION_NAME);
            webViewDataEntity.setSettingInfo(settingInfo);
            //当前本地用户
            webViewDataEntity.setUserInfo(appRepository.readUserData());
            return GsonUtils.toJson(webViewDataEntity);
        }

        @JavascriptInterface
        public void taskJumpWeb(String s) {
            Map<String, String> map = GsonUtils.fromJson(s, GsonUtils.getMapType(String.class, String.class));
            String link = map.get("link");
            Log.d(TAG, "route -> " + link);
            getActivity().runOnUiThread(() -> {
                boolean isMale = ConfigManager.getInstance().isMale();
                if (Objects.equals(link, "home")) router.routeTo(0);
                else if (Objects.equals(link, "broadcast")) router.routeTo(1);
                else if (Objects.equals(link, "message")) router.routeTo(3);
                else if (Objects.equals(link, "bindPhone")) {
                    viewModel.start(CommunityAccountBindFragment.class.getCanonicalName());
                } else if (Objects.equals(link, "EditProfile")) {
                    if (ConfigManager.getInstance().isMale()) {
                        AppContext.instance().logEvent(AppsFlyerEvent.task_improve_data_M);
                    } else {
                        AppContext.instance().logEvent(AppsFlyerEvent.task_improve_data_F);
                    }
                    viewModel.start(EditProfileFragment.class.getCanonicalName());
                } else if (Objects.equals(link, "Certification")) {
                    AppContext.instance().logEvent(AppsFlyerEvent.Verify_Your_Profile);
                    if (isMale) {
                        viewModel.start(CertificationMaleFragment.class.getCanonicalName());
                    } else {
                        AppContext.instance().logEvent(AppsFlyerEvent.task_auth_F);
                        AppContext.instance().logEvent(AppsFlyerEvent.task_answer);
                        viewModel.start(CertificationFemaleFragment.class.getCanonicalName());
                    }
                } else if (Objects.equals(link, "MyPhotoAlbum")) {
                    if (isMale) {
                        AppContext.instance().logEvent(AppsFlyerEvent.task_uploading_image_M);
                    } else {
                        AppContext.instance().logEvent(AppsFlyerEvent.task_upload_image_snap_F);
                    }
                    viewModel.start(MyPhotoAlbumFragment.class.getCanonicalName());
                } else if (Objects.equals(link, "PublishDynamic")) {
                    if (!isMale) {
                        AppContext.instance().logEvent(AppsFlyerEvent.task_post_mood_date_F);
                    }
                    viewModel.start(IssuanceProgramFragment.class.getCanonicalName());
                }
            });
        }
        @JavascriptInterface
        public void reportTaskEvent(String s) {
            Map<String, String> map = GsonUtils.fromJson(s, GsonUtils.getMapType(String.class, String.class));
            String event = map.get("event");
            Log.d(TAG, "event -> " + event);
            if (Objects.equals(event, "enterMissionCenter")) {
                ElkLogEventReport.reportTaskCenterModule.reportEnterTaskCenter();
            } else if (Objects.equals(event, "sign")) {
                ElkLogEventReport.reportTaskCenterModule.reportClickSign();
            } else if (Objects.equals(event, "get")) {
                ElkLogEventReport.reportTaskCenterModule.reportClickGet();
            }
        }
    }

}
