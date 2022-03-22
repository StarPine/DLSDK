package com.dl.playfun.ui.mine.profile.choosesex;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.databinding.FragmentChooseSexBinding;

import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * 选择性别
 *
 * @author wulei
 */
public class ChooseSexFragment extends BaseToolbarFragment<FragmentChooseSexBinding, ChooseSexViewModel> {

    private int sex = -1;

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
        //使用自定义的ViewModelFactory来创建ViewModel
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
                    ToastUtils.showShort(R.string.playfun_reg_user_sex_error);
                    return;
                }
                viewModel.setSex(sex);
            }
        });
        viewModel.uc.clickChooseMale.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                sex = 1;
                binding.ivMale.setBackground(getResources().getDrawable(R.drawable.ic_gender_male_chk));
                binding.ivFemale.setBackground(getResources().getDrawable(R.drawable.ic_gender_female));
            }
        });
        viewModel.uc.clickChooseFemale.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                sex = 0;
                binding.ivFemale.setBackground(getResources().getDrawable(R.drawable.ic_gender_female_chk));
                binding.ivMale.setBackground(getResources().getDrawable(R.drawable.ic_gender_male));
            }
        });
    }
}
