package com.dl.playfun.ui.task.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager2.widget.ViewPager2;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentTaskMainBinding;
import com.dl.playfun.ui.base.BaseFragment;
import com.dl.playfun.ui.task.TaskCenterFragment;
import com.dl.playfun.ui.task.fukubukuro.FukubukuroFragment;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.widget.pageview.FragmentAdapter;

/**
 * Author: 彭石林
 * Time: 2021/11/6 15:30
 * Description: This is TaskMainFragment
 */
public class TaskMainFragment extends BaseFragment<FragmentTaskMainBinding, TaskMainViewModel> {

    private final BaseFragment[] mFragments = new BaseFragment[2];
    private boolean fukubukuroFlag = false;

    private ViewPager2 mainViewPager;

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        ImmersionBarUtils.setupStatusBar(this, true, true);
    }


    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_task_main;
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
    public TaskMainViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(TaskMainViewModel.class);
    }

    @Override
    public void initData() {
        super.initData();
        initView();
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.uc.taskCenterHidden.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean showHideFragment) {
                if (!fukubukuroFlag) {
                    fukubukuroFlag = true;
                    ((FukubukuroFragment) mFragments[1]).reloadWebRul(AppConfig.FukubukuroWebUrl);
                }
                if (showHideFragment) {
                    mainViewPager.setCurrentItem(1,false);
                } else {
                    mainViewPager.setCurrentItem(0,false);
                }
            }
        });
    }

    private void initView() {
        BaseFragment firstFragment = findChildFragment(TaskCenterFragment.class);
        if (firstFragment == null) {
            mFragments[0] = new TaskCenterFragment();
            mFragments[1] = new FukubukuroFragment();
        } else {
            // 这里我们需要拿到mFragments的引用
            mFragments[0] = firstFragment;
            mFragments[1] = findChildFragment(FukubukuroFragment.class);
        }

        mainViewPager = binding.viewPager;
        FragmentAdapter fragmentAdapter = new FragmentAdapter(this);
        fragmentAdapter.setFragmentList(mFragments);
        // 关闭左右滑动切换页面
        mainViewPager.setUserInputEnabled(false);
        // 设置缓存数量 避免销毁重建
        mainViewPager.setOffscreenPageLimit(1);
        //取消保存页面--未知BUG
        mainViewPager.setSaveEnabled(false);
        mainViewPager.setAdapter(fragmentAdapter);
        mainViewPager.setCurrentItem(0, false);
    }
}
