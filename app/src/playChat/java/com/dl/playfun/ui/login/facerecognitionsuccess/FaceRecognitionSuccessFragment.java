package com.dl.playfun.ui.login.facerecognitionsuccess;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentFaceRecognitionBinding;
import com.dl.playfun.ui.base.BaseToolbarFragment;

/**
 * @author wulei
 */
public class FaceRecognitionSuccessFragment extends BaseToolbarFragment<FragmentFaceRecognitionBinding, FaceRecognitionSuccessViewModel> {

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_face_recognition_success;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public FaceRecognitionSuccessViewModel initViewModel() {
        //使用自定义的ViewModelFactory来创建ViewModel，如果不重写该方法，则默认会调用LoginViewModel(@NonNull Application application)构造方法
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(FaceRecognitionSuccessViewModel.class);
    }

    @Override
    public void initData() {
        super.initData();
        if (basicToolbar != null) {
            basicToolbar.hiddenBack(true);
        }

    }

    @Override
    public boolean onBackPressedSupport() {
        return true;
    }

}
