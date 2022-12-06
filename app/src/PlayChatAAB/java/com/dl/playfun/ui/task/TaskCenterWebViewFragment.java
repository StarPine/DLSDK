package com.dl.playfun.ui.task;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

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
import com.dl.playfun.utils.WebViewUtils;
import com.dl.playfun.viewmodel.BaseViewModel;

import java.util.Map;
import java.util.Objects;

/**
 * @author Shuotao Gong
 * @time 2022/11/25
 */
public class TaskCenterWebViewFragment extends BaseFragment<TaskCenterFragmentWebBinding, BaseViewModel<AppRepository>> {

    private static final String TAG = "TASK_CENTER";

    private HomePageRouter router;

    private WebView wv;

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
        super.initData();
        wv = binding.taskCenterH5;
        wv.loadUrl("http://t-m.joy-mask.com/TaskCenter");
        WebViewUtils.initSettings(wv);
        wv.addJavascriptInterface(new Object() {
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
        }, "Native");

    }

    @Override
    public void onResume() {
        super.onResume();
        if (wv != null) {
            wv.loadUrl("javascript:newData()");
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

}
