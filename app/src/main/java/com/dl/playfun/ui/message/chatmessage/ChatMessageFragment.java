package com.dl.playfun.ui.message.chatmessage;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.Injection;
import com.dl.playfun.databinding.FragmentChatMessageBinding;
import com.dl.playfun.entity.BrowseNumberEntity;
import com.dl.playfun.entity.TokenEntity;
import com.dl.playfun.event.MessageCountChangeTagEvent;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.manager.ThirdPushTokenMgr;
import com.dl.playfun.tim.TUIUtils;
import com.dl.playfun.ui.base.BaseFragment;
import com.dl.playfun.ui.message.chatdetail.ChatDetailFragment;
import com.dl.playfun.utils.AutoSizeUtils;
import com.dl.playfun.widget.dialog.TraceDialog;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMConversationListener;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.tencent.qcloud.tuikit.tuichat.bean.ChatInfo;
import com.tencent.qcloud.tuikit.tuiconversation.bean.ConversationInfo;
import com.tencent.qcloud.tuikit.tuiconversation.presenter.ConversationPresenter;
import com.tencent.qcloud.tuikit.tuiconversation.ui.view.ConversationListLayout;

import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.utils.KLog;
import me.goldze.mvvmhabit.utils.StringUtils;

/**
 * @author litchi
 */
public class ChatMessageFragment extends BaseFragment<FragmentChatMessageBinding, ChatMessageViewModel> {

    private ConversationInfo selectedConversationInfo;

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AutoSizeUtils.applyAdapt(this.getResources());
        return R.layout.fragment_chat_message;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public ChatMessageViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        ChatMessageViewModel viewModel = ViewModelProviders.of(this, factory).get(ChatMessageViewModel.class);
        return viewModel;
    }

    private final V2TIMConversationListener unreadListener = new V2TIMConversationListener() {
        @Override
        public void onTotalUnreadMessageCountChanged(long totalUnreadCount) {
            RxBus.getDefault().post(new MessageCountChangeTagEvent((int) totalUnreadCount));
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        V2TIMManager.getConversationManager().removeConversationListener(unreadListener);
    }

    private void registerUnreadListener() {
        V2TIMManager.getConversationManager().addConversationListener(unreadListener);
        V2TIMManager.getConversationManager().getTotalUnreadMessageCount(new V2TIMValueCallback<Long>() {
            @Override
            public void onSuccess(Long totalUnreadCount) {
                RxBus.getDefault().post(new MessageCountChangeTagEvent(totalUnreadCount.intValue()));
            }

            @Override
            public void onError(int code, String desc) {

            }
        });
    }

    @Override
    public void initData() {
        super.initData();
        //腾讯IM登录
        TokenEntity tokenEntity = Injection.provideDemoRepository().readLoginInfo();
        if (tokenEntity != null) {
            TUIUtils.login(tokenEntity.getUserID(), tokenEntity.getUserSig(), new V2TIMCallback() {
                @Override
                public void onSuccess() {
                    initIM();
                    ThirdPushTokenMgr.getInstance().setPushTokenToTIM();
                    registerUnreadListener();
                }

                @Override
                public void onError(int code, String desc) {
                    KLog.e("tencent im login error  errCode = " + code + ", errInfo = " + desc);
                }
            });
        }
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
    }

    private void initIM() {
        ConversationPresenter presenter = new ConversationPresenter();
        presenter.setConversationListener();
        binding.conversationLayout.setPresenter(presenter);
        binding.conversationLayout.initDefault();
        ConversationListLayout listLayout = binding.conversationLayout.getConversationList();
        // 设置adapter item中top文字大小
        listLayout.setItemTopTextSize(16);
        // 设置adapter item中bottom文字大小
        listLayout.setItemBottomTextSize(12);
        // 设置adapter item中timeline文字大小
        listLayout.setItemDateTextSize(10);
        // 设置adapter item头像圆角大小
        listLayout.setItemAvatarRadius(SizeUtils.dp2px(50));
        // 设置adapter item是否不显示未读红点，默认显示
        listLayout.disableItemUnreadDot(false);

        binding.conversationLayout.getConversationList().setOnItemAvatarClickListener(new ConversationListLayout.OnItemAvatarClickListener() {
            @Override
            public void onItemAvatarClick(View view, int position, ConversationInfo messageInfo) {
                //点击用户头像
                String id = messageInfo.getId();
                if(id==null){
                    return;
                }
                if (id.trim().contains(AppConfig.CHAT_SERVICE_USER_ID)) {
                    return;
                }
                viewModel.transUserIM(id,true);
            }
        });

        binding.conversationLayout.getConversationList().setBanConversationDelListener(new ConversationListLayout.BanConversationDelListener() {
            @Override
            public void banConversationDel() {

                viewModel.dismissHUD();
            }
        });

        binding.conversationLayout.getConversationList().setOnItemClickListener(new ConversationListLayout.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, ConversationInfo messageInfo) {
                //点击用户头像
                String id = messageInfo.getId();
                if (id.trim().contains(AppConfig.CHAT_SERVICE_USER_ID)) {
                    startChatActivity(messageInfo,0);
                }else{
                    selectedConversationInfo = messageInfo;
                    viewModel.transUserIM(id,false);
                }
            }
        });

        binding.conversationLayout.getConversationList().setOnItemLongClickListener(new ConversationListLayout.OnItemLongClickListener() {
            @Override
            public void OnItemLongClick(View view, int position, ConversationInfo messageInfo) {

                TraceDialog.getInstance(ChatMessageFragment.this.getContext())
                        .setConfirmOnlick(dialog -> {
                            //置顶会话
                            binding.conversationLayout.setConversationTop(messageInfo,null);

                        })
                        .setConfirmTwoOnlick(dialog -> {
                            //删除会话
                            binding.conversationLayout.deleteConversation(messageInfo);
                            binding.conversationLayout.clearConversationMessage(messageInfo);
                        })
                        .setConfirmThreeOnlick(dialog -> {
                            TraceDialog.getInstance(ChatMessageFragment.this.getContext())
                                    .setTitle(getString(R.string.playfun_del_banned_account_content))
                                    .setTitleSize(18)
                                    .setCannelText(getString(R.string.playfun_cancel))
                                    .setConfirmText(getString(R.string.playfun_mine_trace_delike_confirm))
                                    .chooseType(TraceDialog.TypeEnum.CENTER)
                                    .setConfirmOnlick(new TraceDialog.ConfirmOnclick() {
                                        @Override
                                        public void confirm(Dialog dialog) {
                                            dialog.dismiss();
                                            //删除所有封号会话
                                            viewModel.showHUD();
                                            binding.conversationLayout.deleteAllBannedConversation();
                                        }
                                    }).show();
                        })
                        .convasationItemMenuDialog(messageInfo)
                        .show();
            }
        });
        viewModel.uc.startChatUserView.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer toUserId) {
                if(selectedConversationInfo!=null){
                    startChatActivity(selectedConversationInfo,toUserId);
                }
            }
        });
        viewModel.uc.loadBrowseNumber.observe(this, new Observer<BrowseNumberEntity>() {
            @Override
            public void onChanged(BrowseNumberEntity browseNumberEntity) {
                if (!ObjectUtils.isEmpty(browseNumberEntity)) {
                    if (ConfigManager.getInstance().isMale()) {
                        if (ObjectUtils.isEmpty(browseNumberEntity.getBrowseNumber()) || browseNumberEntity.getBrowseNumber().intValue() < 1) {
                            viewModel.NewNumberText.set(null);
                            binding.conversationLastMsg.setText(R.string.playfun_char_message_text2);
                        } else {
                            Integer number = browseNumberEntity.getBrowseNumber();
                            String text = String.format(StringUtils.getString(R.string.playfun_char_message_text1),number) ;
                            binding.conversationLastMsg.setText(text);
                            if (number > 99) {
                                viewModel.NewNumberText.set("99+");
                            } else {
                                viewModel.NewNumberText.set(String.valueOf(number));
                            }

                        }
                        binding.itemLeft.setVisibility(View.VISIBLE);
                    }
                } else {
                    binding.itemLeft.setVisibility(View.GONE);
                }

            }
        });
    }

    public void loadBrowseNumberCall(){
        try {
            if (viewModel != null) {
                viewModel.newsBrowseNumber();
            }
        } catch (Exception e) {

        }
    }

    private void startChatActivity(ConversationInfo conversationInfo,Integer toUserId) {
        ChatInfo chatInfo = new ChatInfo();
        chatInfo.setType(V2TIMConversation.V2TIM_C2C);
        chatInfo.setId(conversationInfo.getId());
        chatInfo.setChatName(conversationInfo.getTitle());
        AppContext.instance().logEvent(AppsFlyerEvent.IM);
        Bundle bundle = new Bundle();
        bundle.putSerializable(ChatDetailFragment.CHAT_INFO, chatInfo);
        bundle.putSerializable("toUserId", toUserId);
        startContainerActivity(ChatDetailFragment.class.getCanonicalName(), bundle);
    }

}
