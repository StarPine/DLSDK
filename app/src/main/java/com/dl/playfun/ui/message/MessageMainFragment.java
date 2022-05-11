package com.dl.playfun.ui.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.StringUtils;
import com.bumptech.glide.Glide;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.databinding.FragmentMessageMainBinding;
import com.dl.playfun.entity.SystemConfigTaskEntity;
import com.dl.playfun.ui.base.BaseFragment;
import com.dl.playfun.ui.message.chatmessage.ChatMessageFragment;
import com.dl.playfun.ui.message.systemmessagegroup.SystemMessageGroupFragment;
import com.dl.playfun.utils.AutoSizeUtils;
import com.dl.playfun.utils.StringUtil;

/**
 * @author wulei
 */
public class MessageMainFragment extends BaseFragment<FragmentMessageMainBinding, MessageMainViewModel> {

    private final BaseFragment[] mFragments = new BaseFragment[2];

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AutoSizeUtils.applyAdapt(this.getResources());
        return R.layout.fragment_message_main;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public MessageMainViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(MessageMainViewModel.class);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void initData() {
        super.initData();
        AppContext.instance().logEvent(AppsFlyerEvent.Messages);
        viewModel.onViewCreated();

        BaseFragment firstFragment = findChildFragment(ChatMessageFragment.class);
        if (firstFragment == null) {
            mFragments[0] = new ChatMessageFragment();
            mFragments[1] = new SystemMessageGroupFragment();
        } else {
            mFragments[0] = firstFragment;
            mFragments[1] = findChildFragment(SystemMessageGroupFragment.class);
        }
        MessagePagerAdapter fragmentAdapter = new MessagePagerAdapter(this);
        fragmentAdapter.setFragmentList(mFragments);

        // �ر����һ����л�ҳ��
        binding.viewPager.setUserInputEnabled(false);
        // ���û������� ���������ؽ�
        binding.viewPager.setOffscreenPageLimit(2);
        binding.viewPager.setAdapter(fragmentAdapter);
        binding.viewPager.setCurrentItem(0, false);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.tabSelectEvent.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean flag) {
                if (flag) {
                    AppContext.instance().logEvent(AppsFlyerEvent.System_Messages);
                    binding.viewPager.setCurrentItem(0, false);
                } else {
                    AppContext.instance().logEvent(AppsFlyerEvent.Chat);
                    binding.viewPager.setCurrentItem(1, false);
                }

            }
        });
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        // ImmersionBarUtils.setupStatusBar(this, false, true);
        if(mFragments.length>0){
            ChatMessageFragment chatmessage = (ChatMessageFragment)mFragments[0];
            chatmessage.loadBrowseNumberCall();
        }
    }


    @Override
    protected boolean isUmengReportPage() {
        return false;
    }
}
