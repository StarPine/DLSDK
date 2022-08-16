package com.dl.playfun.ui.message.contact;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.SizeUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.Injection;
import com.dl.playfun.databinding.FragmentOftenContactBinding;
import com.dl.playfun.entity.TokenEntity;
import com.dl.playfun.manager.ThirdPushTokenMgr;
import com.dl.playfun.tim.TUIUtils;
import com.dl.playfun.ui.base.BaseFragment;
import com.dl.playfun.ui.message.chatmessage.ChatMessageFragment;
import com.dl.playfun.utils.ChatUtils;
import com.dl.playfun.widget.dialog.TraceDialog;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.qcloud.tuicore.TUILogin;
import com.tencent.qcloud.tuikit.tuiconversation.bean.ConversationInfo;
import com.tencent.qcloud.tuikit.tuiconversation.presenter.ConversationPresenter;
import com.tencent.qcloud.tuikit.tuiconversation.ui.view.ConversationListLayout;

import me.goldze.mvvmhabit.utils.KLog;

/**
 * Author: 彭石林
 * Time: 2022/8/11 17:36
 * Description: This is OftenContactFragment
 */
public class OftenContactFragment extends BaseFragment<FragmentOftenContactBinding,OftenContactViewModel> {

    private ConversationInfo selectedConversationInfo;

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_often_contact;
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
    public OftenContactViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(OftenContactViewModel.class);
    }

    @Override
    public void initData() {
        super.initData();
        //腾讯IM登录
        TokenEntity tokenEntity = Injection.provideDemoRepository().readLoginInfo();
        if (tokenEntity != null) {
            if(TUILogin.isUserLogined()){
                initIM();
            }else{
                TUIUtils.login(tokenEntity.getUserID(), tokenEntity.getUserSig(), new V2TIMCallback() {
                    @Override
                    public void onSuccess() {
                        initIM();
                        ThirdPushTokenMgr.getInstance().setPushTokenToTIM();
                    }

                    @Override
                    public void onError(int code, String desc) {
                        KLog.e("tencent im login error  errCode = " + code + ", errInfo = " + desc);
                    }
                });
            }

        }
    }
    private void initIM(){
        ConversationPresenter presenter = new ConversationPresenter();
        presenter.setFriendConversation(true);
        presenter.setLoadConversationCallback(new ConversationPresenter.LoadConversationCallback() {
            @Override
            public void totalUnreadCount(int count) {
                Log.e("当前会话列表回调数量",count+"=================");
            }

            @Override
            public void isConversationEmpty(boolean empty) {
                //好友会话列表为空
                if(empty) {
                    binding.conversationLayoutContact.setVisibility(View.GONE);
                    binding.ivEmpty.setVisibility(View.VISIBLE);
                }else{
                    binding.conversationLayoutContact.setVisibility(View.VISIBLE);
                    binding.ivEmpty.setVisibility(View.GONE);
                }
            }
        });
        presenter.setConversationListener();
        binding.conversationLayoutContact.setPresenter(presenter);
        binding.conversationLayoutContact.initDefault(true);
        ConversationListLayout listLayout = binding.conversationLayoutContact.getConversationList();
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
        initConversationListener();
    }
    public void initConversationListener(){
        binding.conversationLayoutContact.getConversationList().setOnItemAvatarClickListener((view, position, messageInfo) -> {
            //点击用户头像
            String id = messageInfo.getId();
            if(id==null){
                return;
            }
            if (id.trim().contains(AppConfig.CHAT_SERVICE_USER_ID)) {
                return;
            }
            viewModel.transUserIM(id,true);
        });

        binding.conversationLayoutContact.getConversationList().setBanConversationDelListener(() -> viewModel.dismissHUD());

        binding.conversationLayoutContact.getConversationList().setOnItemClickListener((view, position, messageInfo) -> {
            //点击用户头像
            String id = messageInfo.getId();
            if (id.trim().contains(AppConfig.CHAT_SERVICE_USER_ID)) {
                ChatUtils.startChatActivity(messageInfo,0,viewModel);
            }else{
                selectedConversationInfo = messageInfo;
                viewModel.transUserIM(id,false);
            }
        });

        binding.conversationLayoutContact.getConversationList().setOnItemLongClickListener((view, position, messageInfo) -> TraceDialog.getInstance(getContext())
                .setConfirmOnlick(dialog -> {
                    //置顶会话
                    binding.conversationLayoutContact.setConversationTop(messageInfo,null);

                })
                .setConfirmTwoOnlick(dialog -> {
                    //删除会话
                    binding.conversationLayoutContact.deleteConversation(messageInfo);
                    binding.conversationLayoutContact.clearConversationMessage(messageInfo);
                })
                .setConfirmThreeOnlick(dialog -> {
                    TraceDialog.getInstance(getContext())
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
                                    binding.conversationLayoutContact.deleteAllBannedConversation();
                                }
                            }).show();
                })
                .convasationItemMenuDialog(messageInfo)
                .show());
        viewModel.startChatUserView.observe(this, (Observer<Integer>) toUserId -> {
            if(selectedConversationInfo!=null){
                ChatUtils.startChatActivity(selectedConversationInfo,toUserId,viewModel);
            }
        });
    }

    @Override
    public void initViewObservable() {

    }
}
