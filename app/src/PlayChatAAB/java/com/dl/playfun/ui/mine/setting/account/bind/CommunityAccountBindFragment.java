package com.dl.playfun.ui.mine.setting.account.bind;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentCommunityAccountBindBinding;
import com.dl.playfun.entity.ChooseAreaItemEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.utils.StringUtil;
import com.google.gson.Gson;

/**
 * Author: 彭石林
 * Time: 2022/10/14 10:36
 * Description: This is CommunityAccountBindFragment
 */
public class CommunityAccountBindFragment extends BaseToolbarFragment<FragmentCommunityAccountBindBinding,CommunityAccountViewModel> {

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_community_account_bind;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public CommunityAccountViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(CommunityAccountViewModel.class);
    }

    @Override
    public void initData() {
        super.initData();
        AppConfig.overseasUserEntity = null;
        showInput(binding.etPhone);
        ChooseAreaItemEntity areaCodeInfo = getAreaCodeInfo();
        if (areaCodeInfo == null){
            viewModel.getUserIpCode();
        }else {
            viewModel.areaCode.set(areaCodeInfo);
        }
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        //done 验证码框获取焦点
        viewModel.getCodeSuccess.observe(this,s -> {
            showInput(binding.etCode);
        });
        viewModel.setAreaSuccess.observe(this,s -> {
            if (TextUtils.isEmpty(viewModel.mobile.get()))
                showInput(binding.etPhone);
        });

    }

    private ChooseAreaItemEntity getAreaCodeInfo() {
        String areaCode = ConfigManager.getInstance().getAppRepository().readKeyValue("areaCode");
        if (StringUtil.isEmpty(areaCode)) {
            return null;
        }
        try {
            return new Gson().fromJson(areaCode, ChooseAreaItemEntity.class);
        }catch (Exception ignored){

        }
        return null;
    }

    //done 弹出键盘
    private void showInput(EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        InputMethodManager inputManager =(InputMethodManager)editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(editText, 0);
    }


}
