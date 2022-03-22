package com.dl.playfun.ui.login.profile;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentChoosePortraitBinding;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.utils.PictureSelectorUtil;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.widget.dialog.MMAlertDialog;
import com.dl.playfun.widget.dialog.MVDialog;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import java.util.List;

import me.jessyan.autosize.AutoSizeCompat;
import me.jessyan.autosize.internal.CustomAdapt;

/**
 * @ClassName ChoosePortraitFragment
 * @Description TODO
 * @Author 彭石林
 * @Date 2021/6/23 16:29
 * @Phone 16620350375
 * @email 15616314565@163.com
 * @Version 1.0
 **/
public class ChoosePortraitFragment extends BaseToolbarFragment<FragmentChoosePortraitBinding, ProfileViewModel> implements CustomAdapt {
    Integer sex;
    String name;
    String birthday;

    public static Bundle getStartBundle(Integer sex, String name, String birthday) {
        Bundle bundle = new Bundle();
        bundle.putInt("sex", sex);
        bundle.putString("name", name);
        bundle.putString("birthday", birthday);
        return bundle;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ImmersionBarUtils.setupStatusBar(this, true, true);
        return view;
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        AutoSizeCompat.autoConvertDensityOfGlobal(this.getResources());
        return R.layout.fragment_choose_portrait;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public ProfileViewModel initViewModel() {
        //使用自定义的ViewModelFactory来创建ViewModel，如果不重写该方法，则默认会调用LoginViewModel(@NonNull Application application)构造方法
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(ProfileViewModel.class);
    }

    @Override
    public void initParam() {
        super.initParam();
        sex = getArguments().getInt("sex");
        name = getArguments().getString("name");
        birthday = getArguments().getString("birthday");
        //themeItemEntity = getArguments().getParcelable(ARG_PROGRAM_ENTITY);
    }

    @Override
    public void initViewObservable() {
        viewModel.UserSex.set(sex);
        viewModel.UserName.set(name);
        viewModel.UserBirthday.set(birthday);
        viewModel.uc.clickAvatar.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void aVoid) {
                chooseAvatar();
            }
        });
        viewModel.uc.showAlertHint.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                boolean code = viewModel.getCode();
                if (!code) {
                    MVDialog.AlertInviteWrite(mActivity, true, new MVDialog.DilodAlertMessageInterface() {
                        @Override
                        public void confirm(DialogInterface dialog, int which, int sel_Index, String swiftMessageEntity) {
                            dialog.dismiss();
                            if (StringUtil.isEmpty(swiftMessageEntity)) {
                                viewModel.loadProfile(true);
                            } else {
                                AppContext.instance().pushInvite(swiftMessageEntity, 2, null);
                                viewModel.loadProfile(true);
                            }
                        }

                        @Override
                        public void cancel(DialogInterface dialog, int which) {

                        }
                    }).show();
                } else {
                    MMAlertDialog.RegUserAlert(mActivity, true, new MMAlertDialog.RegUserAlertInterface() {
                        @Override
                        public void confirm(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            viewModel.loadProfile(true);
                        }
                    });
                }
            }
        });
    }

    private void chooseAvatar() {
        PictureSelectorUtil.selectImageAndCrop(mActivity, true, 1, 1, new OnResultCallbackListener<LocalMedia>() {
            @Override
            public void onResult(List<LocalMedia> result) {
                viewModel.UserPortrait.set(result.get(0).getCutPath());
            }

            @Override
            public void onCancel() {
            }
        });
    }

    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return 360;
    }
}