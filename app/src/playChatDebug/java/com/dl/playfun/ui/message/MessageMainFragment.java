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
import com.dl.playfun.utils.StringUtil;

/**
 * @author wulei
 */
public class MessageMainFragment extends BaseFragment<FragmentMessageMainBinding, MessageMainViewModel> {

    private final BaseFragment[] mFragments = new BaseFragment[2];

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
            // 这里库已经做了Fragment恢复,所有不需要额外的处理了, 不会出现重叠问题
            // 这里我们需要拿到mFragments的引用
            mFragments[0] = firstFragment;
            mFragments[1] = findChildFragment(SystemMessageGroupFragment.class);
        }
        MessagePagerAdapter fragmentAdapter = new MessagePagerAdapter(this);
        fragmentAdapter.setFragmentList(mFragments);

        // 关闭左右滑动切换页面
        binding.viewPager.setUserInputEnabled(false);
        // 设置缓存数量 避免销毁重建
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
        //加载背景图片
        viewModel.loadSysConfigTask.observe(this, new Observer<SystemConfigTaskEntity>() {
            @Override
            public void onChanged(SystemConfigTaskEntity systemConfigTaskEntity) {
                if (!StringUtils.isTrimEmpty(systemConfigTaskEntity.getFloatingImg())) {
                    Glide.with(MessageMainFragment.this.getContext()).asGif().load(StringUtil.getFullImageUrl(systemConfigTaskEntity.getFloatingImg()))
                            .error(R.drawable.nearby_attendance)
                            .placeholder(R.drawable.nearby_attendance)
                            .into(binding.floatingImg);
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
