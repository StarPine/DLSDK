package com.dl.playfun.ui.program.programsubject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.ui.base.BaseFragment;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.databinding.FragmentProgramSubjectBinding;

/**
 * @author wulei
 */
public class ProgramSubjectFragment extends BaseFragment<FragmentProgramSubjectBinding, ProgramSubjectViewModel> {

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_program_subject;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public ProgramSubjectViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(ProgramSubjectViewModel.class);
    }
}
