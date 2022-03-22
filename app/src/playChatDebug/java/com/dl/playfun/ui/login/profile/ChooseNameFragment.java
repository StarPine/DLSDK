package com.dl.playfun.ui.login.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentChooseNameBinding;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.utils.StringUtil;

import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @ClassName ChooseNameFragment
 * @Description TODO
 * @Author 彭石林
 * @Date 2021/6/23 16:27
 * @Phone 16620350375
 * @email 15616314565@163.com
 * @Version 1.0
 **/
public class ChooseNameFragment extends BaseToolbarFragment<FragmentChooseNameBinding, ProfileViewModel> {

    Integer sex;

    public static Bundle getStartBundle(Integer sex) {
        Bundle bundle = new Bundle();
        bundle.putInt("sex", sex);
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
        return R.layout.fragment_choose_name;
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
    }

    @Override
    public void initViewObservable() {
        viewModel.uc.reploadName.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                binding.userName.setText(s);
            }
        });
        binding.userName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        viewModel.uc.clickUserName.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                String name = binding.userName.getText().toString().trim();
                if (StringUtil.isEmpty(name.trim())) {
                    ToastUtils.showShort(R.string.reg_user_name_emp);
                } else {
                    if (name.length() > 10) {
                        ToastUtils.showShort(R.string.reg_user_name_maxlen);
                        return;
                    }
                    //Bundle bundle = ChooseBirthdayFragment.getStartBundle(sex,name);
                    //viewModel.start(ChooseBirthdayFragment.class.getCanonicalName(),bundle);
                    viewModel.checkNickname(name, sex);
                }
            }
        });

        binding.userName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = binding.userName.getText().toString();
                if (!StringUtil.isEmpty(text)) {
                    binding.tvSize.setText(text.length() + "/10");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }
}