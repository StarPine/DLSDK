package com.dl.playfun.ui.program.chooseprogramsite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentChooseProgramSiteBinding;
import com.dl.playfun.ui.base.BaseToolbarFragment;

/**
 * @author wulei
 */
public class ChooseProgramSiteFragment extends BaseToolbarFragment<FragmentChooseProgramSiteBinding, ChooseProgramSiteViewModel> {
    private int programId;

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_choose_program_site;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public ChooseProgramSiteViewModel initViewModel() {
        //使用自定义的ViewModelFactory来创建ViewModel，如果不重写该方法，则默认会调用LoginViewModel(@NonNull Application application)构造方法
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        ChooseProgramSiteViewModel chooseProgramSiteViewModel = ViewModelProviders.of(this, factory).get(ChooseProgramSiteViewModel.class);
        chooseProgramSiteViewModel.setProgramId(programId);
        return chooseProgramSiteViewModel;
    }

    @Override
    public void initData() {

    }

    @Override
    public void initParam() {
        super.initParam();
        programId = getArguments().getInt("programId", 0);
        System.out.println();
    }
}
