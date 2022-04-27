package com.dl.playfun.ui.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.entity.VersionEntity;
import com.dl.playfun.kl.view.VideoPresetActivity;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.databinding.FragmentMeSettingBinding;
import com.dl.playfun.widget.dialog.version.view.UpdateDialogView;
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

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        //美颜点击
        viewModel.uc.starFacebeautyActivity.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void aBoolean) {
                Intent intent = new Intent(mActivity, VideoPresetActivity.class);
                mActivity.startActivity(intent);
            }
        });
        viewModel.uc.versionEntitySingl.observe(this, new Observer<VersionEntity>() {
            @Override
            public void onChanged(VersionEntity versionEntity) {
                BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (versionEntity.getVersion_code().intValue() <= AppConfig.VERSION_CODE.intValue()) {
                            ToastUtils.showShort(R.string.version_latest);
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
    }
}
