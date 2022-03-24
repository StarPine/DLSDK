package com.dl.playfun.ui.message.chatmessage;

import static com.dl.playfun.ui.message.chatdetail.ChatDetailFragment.CHAT_INFO;

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
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.event.MessageCountChangeTagEvent;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.manager.ThirdPushTokenMgr;
import com.dl.playfun.tim.TUIUtils;
import com.dl.playfun.ui.base.BaseFragment;
import com.dl.playfun.ui.certification.certificationfemale.CertificationFemaleFragment;
import com.dl.playfun.ui.message.chatdetail.ChatDetailFragment;
import com.dl.playfun.ui.mine.vipsubscribe.VipSubscribeFragment;
import com.dl.playfun.ui.userdetail.detail.UserDetailFragment;
import com.dl.playfun.utils.ChatUtils;
import com.dl.playfun.widget.coinpaysheet.CoinPaySheet;
import com.dl.playfun.widget.coinrechargesheet.CoinRechargeSheetView;
import com.dl.playfun.widget.dialog.MVDialog;
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
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author litchi
 */
public class ChatMessageFragment extends BaseFragment<FragmentChatMessageBinding, ChatMessageViewModel> {

    private ConversationInfo selectedConversationInfo;

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
        viewModel.uc.askUseChatNumber.observe(this, param -> {
            UserDataEntity userDataEntity = viewModel.uc.localUserDataEntity.getValue();
            if (param.get(1) == -1 || userDataEntity.getSex() == 0 || (userDataEntity.getSex() == AppConfig.MALE && userDataEntity.getIsVip() == 0)) { //判断如果是男用户，且不是VIP状态下不做限制
                startChatActivity(selectedConversationInfo);
            } else if (param.get(1) == 0) {
                //次数用完
                int sex = AppContext.instance().appRepository.readUserData().getSex();
                MVDialog.getInstance(ChatMessageFragment.this.getContext())
                        .setContent(getString(R.string.free_unlock_chat_empty))
                        .setConfirmText(getString(sex == 1 ? R.string.vip_free_chat : R.string.goddess_free_chat))
                        .setConfirmOnlick(dialog -> {
                            dialog.dismiss();
                            if (sex == AppConfig.MALE) {
                                viewModel.start(VipSubscribeFragment.class.getCanonicalName());
                            } else {
                                viewModel.start(CertificationFemaleFragment.class.getCanonicalName());
                            }
                        })
                        .setConfirmTwoText(String.format(getString(R.string.pay_coin_chat), ConfigManager.getInstance().getImMoney()))
                        .setConfirmTwoOnclick(new MVDialog.ConfirmTwoOnclick() {
                            @Override
                            public void confirm(MVDialog dialog) {
                                new CoinPaySheet.Builder(mActivity).setPayParams(6, param.get(0), getString(R.string.check_detail), false, new CoinPaySheet.CoinPayDialogListener() {
                                    @Override
                                    public void onPaySuccess(CoinPaySheet sheet, String orderNo, Integer payPrice) {
                                        sheet.dismiss();
                                        ToastUtils.showShort(R.string.pay_success);
                                        Bundle bundle = UserDetailFragment.getStartBundle(param.get(0));
                                        startFragment(UserDetailFragment.class.getCanonicalName(), bundle);
                                    }

                                    @Override
                                    public void onRechargeSuccess(CoinRechargeSheetView rechargeSheetView) {

                                    }
                                }).build().show();
                            }
                        })
                        .chooseType(MVDialog.TypeEnum.CENTER)
                        .show();
            } else {
                MVDialog.getInstance(ChatMessageFragment.this.getContext())
                        .setContent(String.format(getString(R.string.use_one_unlock_chat), param.get(1)))
                        .setConfirmText(getString(R.string.use_one_chance))
                        .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
                            @Override
                            public void confirm(MVDialog dialog) {
                                dialog.dismiss();
                                viewModel.useChatNumber(param.get(0));
                            }
                        })
                        .chooseType(MVDialog.TypeEnum.CENTER)
                        .show();
            }
        });

        viewModel.uc.useChatNumberSuccess.observe(this, integer -> {
            startChatActivity(selectedConversationInfo);
        });
    }

    private void initIM() {
        ConversationPresenter presenter = new ConversationPresenter();
        presenter.setConversationListener();
        binding.conversationLayout.setPresenter(presenter);
        binding.conversationLayout.initDefault();
        binding.conversationLayout.getTitleBar().setVisibility(View.GONE);
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
                String id = messageInfo.getId();
                if ("administrator".equals(id)) {
                    return;
                }
                int userId = ChatUtils.imUserIdToSystemUserId(id);
                if (userId == 0) {
                    return;
                }
                Bundle bundle = UserDetailFragment.getStartBundle(userId);
                viewModel.start(UserDetailFragment.class.getCanonicalName(), bundle);
            }
        });

        binding.conversationLayout.getConversationList().setOnItemClickListener(new ConversationListLayout.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, com.tencent.qcloud.tuikit.tuiconversation.bean.ConversationInfo messageInfo) {
                int userId = ChatUtils.imUserIdToSystemUserId(messageInfo.getId());
                startChatActivity(messageInfo);
//                if (userId != 0) {
//                    selectedConversationInfo = messageInfo;
//                    viewModel.checkChatNumber(userId);
//                } else {
//                    startChatActivity(messageInfo);
//                }
            }
        });

        binding.conversationLayout.getConversationList().setOnItemLongClickListener(new ConversationListLayout.OnItemLongClickListener() {
            @Override
            public void OnItemLongClick(View view, int position, com.tencent.qcloud.tuikit.tuiconversation.bean.ConversationInfo messageInfo) {
                String[] items = new String[]{getString(R.string.top_chat), getString(R.string.delet_chat)};
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setSingleChoiceItems(items, 0, (dialog, which) -> {
                    dialog.dismiss();
                    if (which == 0) {
                        //置顶会话
                        binding.conversationLayout.setConversationTop(messageInfo,null);
                    } else if (which == 1) {
                        //删除会话
                        binding.conversationLayout.deleteConversation(messageInfo);
                        binding.conversationLayout.clearConversationMessage(messageInfo);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.setCanceledOnTouchOutside(true);
            }
        });
        viewModel.uc.loadBrowseNumber.observe(this, new Observer<BrowseNumberEntity>() {
            @Override
            public void onChanged(BrowseNumberEntity browseNumberEntity) {
                if (!ObjectUtils.isEmpty(browseNumberEntity)) {
                    if (ConfigManager.getInstance().isMale()) {
                        if (ObjectUtils.isEmpty(browseNumberEntity.getBrowseNumber()) || browseNumberEntity.getBrowseNumber().intValue() < 1) {
                            viewModel.NewNumberText.set(null);
                            binding.conversationLastMsg.setText(R.string.char_message_text2);
                        } else {
                            Integer number = browseNumberEntity.getBrowseNumber();
                            String text = String.format(StringUtils.getString(R.string.char_message_text1), number);
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

    private void startChatActivity(ConversationInfo conversationInfo) {
        ChatInfo chatInfo = new ChatInfo();
        chatInfo.setType(V2TIMConversation.V2TIM_C2C);
        chatInfo.setId(conversationInfo.getId());
        chatInfo.setChatName(conversationInfo.getTitle());
        AppContext.instance().logEvent(AppsFlyerEvent.IM);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CHAT_INFO, chatInfo);
        startContainerActivity(ChatDetailFragment.class.getCanonicalName(), bundle);
    }

}
