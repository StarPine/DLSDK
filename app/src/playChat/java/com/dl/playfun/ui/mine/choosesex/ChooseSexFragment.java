package com.dl.playfun.ui.mine.choosesex;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentChooseSexBinding;
import com.dl.playfun.manager.LocationManager;
import com.dl.playfun.ui.base.BaseToolbarFragment;

import me.goldze.mvvmhabit.utils.ToastUtils;
import me.jessyan.autosize.AutoSizeConfig;

/**
 * 选择性别
 *
 * @author wulei
 */
public class ChooseSexFragment extends BaseToolbarFragment<FragmentChooseSexBinding, ChooseSexViewModel> {
    private ImageView ivMale;
    private ImageView ivFemale;

    private int sex = -1;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AutoSizeConfig.getInstance().setCustomFragment(true);
        ivMale = view.findViewById(R.id.iv_male);
        ivFemale = view.findViewById(R.id.iv_female);
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_choose_sex;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public ChooseSexViewModel initViewModel() {
        //使用自定义的ViewModelFactory来创建ViewModel，如果不重写该方法，则默认会调用LoginViewModel(@NonNull Application application)构造方法
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(ChooseSexViewModel.class);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.uc.clickConfird.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                if (sex < 0) {
                    ToastUtils.showShort(R.string.reg_user_sex_error);
                    return;
                }
                viewModel.setSex(sex);
            }
        });
        viewModel.uc.clickChooseMale.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                sex = 1;
                ivMale.setBackground(getResources().getDrawable(R.drawable.ic_gender_male_chk));
                ivFemale.setBackground(getResources().getDrawable(R.drawable.ic_gender_female));
            }
        });
        viewModel.uc.clickChooseFemale.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                sex = 0;
                ivFemale.setBackground(getResources().getDrawable(R.drawable.ic_gender_female_chk));
                ivMale.setBackground(getResources().getDrawable(R.drawable.ic_gender_male));
            }
        });
        try {//如果开启定位权限
            startLocation();
        } catch (Exception e) {
            Log.e("上报定位异常，描述", e.getMessage());
        }
    }

    //
    private void shouChooseSex() {
        final Dialog bottomDialog = new Dialog(this.getContext(), R.style.BottomDialog);
        View contentView = LayoutInflater.from(this.getContext()).inflate(R.layout.dialog_choose_sex_warn, null);
        bottomDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = getResources().getDisplayMetrics().widthPixels - (getResources().getDisplayMetrics().widthPixels / 5);
        contentView.setLayoutParams(layoutParams);
        bottomDialog.getWindow().setGravity(Gravity.CENTER);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        contentView.findViewById(R.id.iv_dialog_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomDialog.dismiss();
            }
        });
        contentView.findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ToastUtils.showShort("確定");
                bottomDialog.dismiss();
            }
        });
        bottomDialog.show();
    }

    @Override
    public boolean onBackPressedSupport() {
        return true;
    }


    @SuppressLint("MissingPermission")
    private void startLocation() {
        LocationManager.getInstance().getLastLocation(new LocationManager.LocationListener() {
            @Override
            public void onLocationSuccess(double lat, double lng) {
                viewModel.reportUserLocation(String.valueOf(lat), String.valueOf(lng));
            }

            @Override
            public void onLocationFailed() {
                //附近页面定位失败。通知一直下发 RxBus.getDefault().post(new LocationChangeEvent());
                Log.e("获取定位权限失败", "===============");
            }
        });
    }
}
