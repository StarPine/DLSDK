package com.dl.playfun.ui.mine.setting;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.ElkLogEventReport;
import com.dl.playfun.databinding.FragmentMeSettingBinding;
import com.dl.playfun.entity.VersionEntity;
import com.dl.playfun.kl.view.VideoPresetActivity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.tim.TUIUtils;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.widget.dialog.MVDialog;
import com.dl.playfun.widget.dialog.version.view.UpdateDialogView;
import com.luck.picture.lib.permissions.PermissionChecker;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.qcloud.tuicore.util.BackgroundTasks;

import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Author: 彭石林
 * Time: 2021/10/25 14:38
 * Description: This is MeSettingFragment
 */
public class MeSettingFragment extends BaseToolbarFragment<FragmentMeSettingBinding, MeSettingViewModel> {
    @Override
    public int initContentView(LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return R.layout.fragment_me_setting;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initParam() {
        super.initParam();
    }

    @Override
    public MeSettingViewModel initViewModel() {
        //使用自定义的ViewModelFactory来创建ViewModel，如果不重写该方法，则默认会调用LoginViewModel(@NonNull Application application)构造方法
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(MeSettingViewModel.class);
    }

    @Override
    public void initData() {
        super.initData();
    }

    ActivityResultLauncher<String> toPermissionIntent = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (result) {
            Intent intent = new Intent(mActivity, VideoPresetActivity.class);
            mActivity.startActivity(intent);
        } else {
            Toast.makeText(_mActivity, R.string.picture_camera, Toast.LENGTH_SHORT).show();
            if (!ActivityCompat.shouldShowRequestPermissionRationale(_mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // 只要有一个权限没有被授予, 则直接返回 false
                PermissionChecker.launchAppDetailsSettings(getContext());
            }
        }
    });

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        //美颜点击
        viewModel.uc.starFacebeautyActivity.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void aBoolean) {
                toPermissionIntent.launch(Manifest.permission.CAMERA);
            }
        });
        viewModel.uc.versionEntitySingl.observe(this, new Observer<VersionEntity>() {
            @Override
            public void onChanged(VersionEntity versionEntity) {
                BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (versionEntity.getVersion_code().intValue() <= AppConfig.VERSION_CODE.intValue()) {
                            ToastUtils.showShort(R.string.playfun_version_latest);
                        } else {
                            boolean isUpdate = versionEntity.getIs_update().intValue() == 1;
                            UpdateDialogView.getInstance(mActivity)
                                    .getUpdateDialogView(versionEntity.getVersion_name(),
                                            versionEntity.getContent(),
                                            versionEntity.getUrl(),
                                            isUpdate, "playchat", versionEntity.getLinkUrl()).show();
                        }
                    }
                });
            }
        });
        viewModel.uc.clickLogout.observe(this, aVoid ->{
            ElkLogEventReport.reportLoginModule.reportSignOut(ElkLogEventReport._click,"clickSignOut",ConfigManager.getInstance().getLoginSource());
            MVDialog.getInstance(this.getContext())
                    .setContent(getString(R.string.playfun_conflirm_log_out))
                    .setConfirmOnlick(dialog -> {
                        ElkLogEventReport.reportLoginModule.reportSignOut(ElkLogEventReport._click,"confirmSignOut",ConfigManager.getInstance().getLoginSource());
                        TUIUtils.logout(new V2TIMCallback() {
                            @Override
                            public void onSuccess() {
                                ConfigManager.getInstance().getAppRepository().saveOldUserData();
                                viewModel.logout();
                            }

                            @Override
                            public void onError(int i, String s) {
                                ConfigManager.getInstance().getAppRepository().saveOldUserData();
                                viewModel.logout();
                            }
                        });
                    })
                    .chooseType(MVDialog.TypeEnum.CENTERWARNED)
                    .show();
        });
    }
}
