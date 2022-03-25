package com.dl.playfun.ui.message.chatdetail;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.data.source.local.LocalDataSourceImpl;
import com.dl.playfun.entity.AlbumPhotoEntity;
import com.dl.playfun.entity.CoinExchangePriceInfo;
import com.dl.playfun.entity.EvaluateItemEntity;
import com.dl.playfun.entity.GiftBagEntity;
import com.dl.playfun.entity.GoodsEntity;
import com.dl.playfun.entity.LocalMessageIMEntity;
import com.dl.playfun.entity.MessageRuleEntity;
import com.dl.playfun.entity.PhotoAlbumEntity;
import com.dl.playfun.entity.TagEntity;
import com.dl.playfun.entity.TaskRewardReceiveEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.ui.certification.certificationfemale.CertificationFemaleFragment;
import com.dl.playfun.ui.dialog.GiftBagDialog;
import com.dl.playfun.ui.mine.myphotoalbum.MyPhotoAlbumFragment;
import com.dl.playfun.ui.mine.wallet.girl.TwDollarMoneyFragment;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.utils.ChatUtils;
import com.dl.playfun.utils.ImageUtils;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.utils.PictureSelectorUtil;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.utils.Utils;
import com.dl.playfun.widget.bottomsheet.BottomSheet;
import com.dl.playfun.widget.coinrechargesheet.GameCoinExchargeSheetView;
import com.dl.playfun.widget.dialog.MMAlertDialog;
import com.dl.playfun.widget.dialog.MVDialog;
import com.dl.playfun.widget.dialog.MessageDetailDialog;
import com.dl.playfun.widget.dialog.TraceDialog;
import com.google.gson.Gson;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.databinding.FragmentChatDetailBinding;
import com.dl.playfun.ui.certification.certificationmale.CertificationMaleFragment;
import com.dl.playfun.ui.message.chooselocation.ChooseLocationFragment;
import com.dl.playfun.ui.message.coinredpackagedetail.CoinRedPackageDetailFragment;
import com.dl.playfun.ui.message.photoreview.PhotoReviewFragment;
import com.dl.playfun.ui.message.sendcoinredpackage.SendCoinRedPackageFragment;
import com.dl.playfun.ui.userdetail.detail.UserDetailFragment;
import com.dl.playfun.ui.userdetail.locationmaps.LocationMapsFragment;
import com.dl.playfun.ui.userdetail.photobrowse.PhotoBrowseFragment;
import com.dl.playfun.ui.userdetail.report.ReportUserFragment;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.coustom.CustomIMTextEntity;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.tencent.qcloud.tuicore.util.FileUtil;
import com.tencent.qcloud.tuikit.tuichat.bean.ChatInfo;
import com.tencent.qcloud.tuikit.tuichat.bean.MessageInfo;
import com.tencent.qcloud.tuikit.tuichat.component.AudioPlayer;
import com.tencent.qcloud.tuikit.tuichat.presenter.C2CChatPresenter;
import com.tencent.qcloud.tuikit.tuichat.presenter.ChatPresenter;
import com.tencent.qcloud.tuikit.tuichat.ui.interfaces.OnItemLongClickListener;
import com.tencent.qcloud.tuikit.tuichat.ui.view.input.InputView;
import com.tencent.qcloud.tuikit.tuichat.ui.view.input.inputmore.InputMoreFragment;
import com.tencent.qcloud.tuikit.tuichat.ui.view.message.MessageRecyclerView;
import com.tencent.qcloud.tuikit.tuichat.util.ChatMessageInfoUtil;
import com.tencent.qcloud.tuikit.tuichat.util.TUIChatLog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.jessyan.autosize.internal.CustomAdapt;
import me.yokeyword.fragmentation.ISupportFragment;

/**
 * @author wulei
 */
public class ChatDetailFragment extends BaseToolbarFragment<FragmentChatDetailBinding, ChatDetailViewModel> implements CustomChatInputFragment.CustomChatInputFragmentListener, InputView.SendOnClickCallback, CustomAdapt {
    public static final String CHAT_INFO = "chatInfo";

    public static final String TAG = "ChatDetailFragment";
    Integer message_page = 0;
    private ChatInfo mChatInfo;
    private InputView inputLayout;
    private String toSendMessageText = null;

    private String AudioProfitTips = null;
    private String VideoProfitTips = null;

    //快速评价点击更多延迟2秒
    private Long intervalTime = null;

    private ChatCustomChatInputFragmentListener customChatInputFragmentListener;

    public static Bundle getStartBundle(ChatInfo chatInfo) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(CHAT_INFO, chatInfo);
        return bundle;
    }

    public static Bundle getStartBundle(ChatInfo chatInfo, String message) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(CHAT_INFO, chatInfo);
        bundle.putString("message", message);
        return bundle;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ImmersionBarUtils.setupStatusBar(this, true, true);
        return view;
    }

    @Override
    public void initParam() {
        super.initParam();
        mChatInfo = (ChatInfo) getArguments().getSerializable(CHAT_INFO);
        toSendMessageText = getArguments().getString("message");
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_chat_detail;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public ChatDetailViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(ChatDetailViewModel.class);
    }

    @Override
    public void initData() {
        super.initData();
        viewModel.getLocalUserData();

        binding.chatLayout.getTitleBar().setVisibility(View.GONE);
    }

    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        super.onEnterAnimationEnd(savedInstanceState);
        if (mChatInfo == null) {
            return;
        }
        customChatInputFragmentListener = new ChatCustomChatInputFragmentListener();
        viewModel.loadUserInfo(getTaUserIdIM());
        //加载用户标签
        viewModel.loadTagUser(String.valueOf(getTaUserIdIM()));
        //initChatView();
        initChatView();
        int userId = getTaUserIdIM(); //获取当前聊天对象的ID
        if (userId != 0) {
            //加载聊天规则
            viewModel.getMessageRule();
            //聊天价格配置
            viewModel.getPriceConfig(userId);
            viewModel.verifyGoddessTips(userId);
        }
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.uc.sendUserGiftError.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {
                dialogRechargeShow(true);
            }
        });
        viewModel.uc.resultMessageRule.observe(this, new Observer<List<MessageRuleEntity>>() {
            @Override
            public void onChanged(List<MessageRuleEntity> messageRuleEntities) {
                //遍历聊天规则
                for(MessageRuleEntity messageRuleEntity : messageRuleEntities) {
                    //相册
                    if(messageRuleEntity.getType().intValue()==1){
                        if(messageRuleEntity.getRuleType()==1){//按时间
                            Integer ruleValue = messageRuleEntity.getRuleValue();
                            if(ruleValue!=null && ruleValue.intValue()>0){
                                String eventId = mChatInfo.getId()+"_photoAlbum";
                                LocalMessageIMEntity localMessageIMEntity = LocalDataSourceImpl.getInstance().readLocalMessageIM(eventId);
                                if(localMessageIMEntity == null){
                               // Log.e("发送聊天信息规则", "1111111111111");
                                    viewModel.getPhotoAlbum(getTaUserIdIM());
                                }else{
                                    long sendTime = localMessageIMEntity.getSendTime();
                                    long localTime = System.currentTimeMillis();
                                    if ((localTime / 1000) - (sendTime / 1000) > ruleValue.intValue()) {//满足发送时间
                                        //LocalDataSourceImpl.getInstance().removeLocalMessage(eventId);
                                        //removeLocalMessage(localMessageIMEntity,eventId);
                                        //插入相册
                                        viewModel.getPhotoAlbum(getTaUserIdIM());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        //删除评价
        viewModel.uc.removeEvaluateMessage.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {
                String eventId = mChatInfo.getId()+"_evaluate";
                LocalMessageIMEntity localMessageIMEntity = LocalDataSourceImpl.getInstance().readLocalMessageIM(eventId);
                if(localMessageIMEntity!=null) {
                    removeLocalMessage(localMessageIMEntity,eventId,true);
                }
            }
        });
        //点击更多评价
        viewModel.uc.AlertMEvaluate.observe(this, new Observer<List<EvaluateItemEntity>>() {
            @Override
            public void onChanged(List<EvaluateItemEntity> evaluateItemEntities) {
                MMAlertDialog.DialogChatDetail(getContext(),false,0,evaluateItemEntities,new MMAlertDialog.DilodAlertInterface(){
                    @Override
                    public void confirm(DialogInterface dialog, int which, int sel_Index) {
                        viewModel.commitUserEvaluate(getTaUserIdIM(),evaluateItemEntities.get(sel_Index).getTagId(),dialog);
                    }
                    @Override
                    public void cancel(DialogInterface dialog, int which) {

                    }
                }).show();
            }
        });
        //发送IM评价插入
        viewModel.uc.sendIMEvaluate.observe(this, new Observer<List<EvaluateItemEntity>>() {
            @Override
            public void onChanged(List<EvaluateItemEntity> evaluateItemEntities) {
                String eventId = mChatInfo.getId()+"_evaluate";
                LocalMessageIMEntity localMessageIMEntity = LocalDataSourceImpl.getInstance().readLocalMessageIM(eventId);
                //if(localMessageIMEntity==null) {
                try {
                    addLocalMessage("message_evaluate", eventId, GsonUtils.toJson(evaluateItemEntities, List.class));
                } catch (Exception e) {

                }
                //}else{
                    //removeLocalMessage(localMessageIMEntity,eventId);
                //}
            }
        });
        //效验用户是否已经评价 已经评价不会发出
        viewModel.uc.canEvaluate.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean flagBoolean) {
                if(flagBoolean){
                    viewModel.getUserEvaluate(getTaUserIdIM(),true);
                }else{
                    String eventId = mChatInfo.getId()+"_evaluate";
                    LocalMessageIMEntity localMessageIMEntity = LocalDataSourceImpl.getInstance().readLocalMessageIM(eventId);
                    if(localMessageIMEntity!=null) {
                        removeLocalMessage(localMessageIMEntity,eventId,true);
                    }
                }
            }
        });
        //插入相册
        viewModel.uc.putPhotoAlbumEntity.observe(this, new Observer<PhotoAlbumEntity>() {
            @Override
            public void onChanged(PhotoAlbumEntity photoAlbumEntity) {
                String eventId = mChatInfo.getId() + "_photoAlbum";
                try {
                    addLocalMessage("message_photo", eventId, GsonUtils.toJson(photoAlbumEntity));
                } catch (Exception e) {

                }

                //LocalDataSourceImpl.getInstance().removeLocalMessage(eventId);
                //removeLocalMessage(localMessageIMEntity,eventId);
            }
        });
        viewModel.uc.clickConnMic.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void aVoid) {
                if (inputLayout != null) {
                    //拨打语音电话
                    inputLayout.startAudioCall();
                }
            }
        });
        viewModel.uc.loadTag.observe(this, new Observer<TagEntity>() {
            @Override
            public void onChanged(TagEntity tagEntity) {
                if (tagEntity.getThisIsGg().intValue() == 1) {//当前用户是GG
                    viewModel.isTagShow.set(true);
                    if (tagEntity.getToIsInvite().intValue() == 1) {//是否填写邀请码 0否 1是
                        binding.tagTitle.setText(R.string.playfun_user_message_tag2);
                    } else {
                        binding.tagTitle.setText(R.string.playfun_user_message_tag1);
                    }
                }
                if (tagEntity.getToIsGg().intValue() == 1) {//对方用户是GG
                    //创建订单时绑定关系
                    viewModel.ChatInfoId = getTaUserIdIM();
                }
            }
        });
        viewModel.uc.clickMore.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                int userId = getTaUserIdIM();
                if (mChatInfo == null || userId < 1) {
                    return;
                }
                String[] items = new String[]{getString(R.string.playfun_pull_black_shield_both_sides), getString(R.string.playfun_report_user_title)};
                if (viewModel.inBlacklist.get()) {
                    items[0] = getString(R.string.playfun_remove_black_shield_both_sides);
                }

                new BottomSheet.Builder(mActivity).setDatas(items).setOnItemSelectedListener(new BottomSheet.ItemSelectedListener() {
                    @Override
                    public void onItemSelected(BottomSheet bottomSheet, int position) {
                        bottomSheet.dismiss();
                        if (position == 0) {
                            if (viewModel.inBlacklist.get()) {
                                viewModel.delBlackList(userId);
                            } else {
                                MVDialog.getInstance(mActivity)
                                        .setContent(getString(R.string.playfun_dialog_add_blacklist_content))
                                        .setConfirmText(getString(R.string.playfun_dialog_add_blacklist_content2))
                                        .setConfirmOnlick(dialog -> {
                                            viewModel.addBlackList(userId);
                                        })
                                        .chooseType(MVDialog.TypeEnum.CENTERWARNED)
                                        .show();
                            }
                        } else if (position == 1) {
                            Bundle bundle = ReportUserFragment.getStartBundle("home", userId);
                            ReportUserFragment reportUserFragment = new ReportUserFragment();
                            reportUserFragment.setArguments(bundle);
                            start(reportUserFragment);
                        }
                    }
                }).setCancelButton(getString(R.string.playfun_cancel), new BottomSheet.CancelClickListener() {
                    @Override
                    public void onCancelClick(BottomSheet bottomSheet) {
                        bottomSheet.dismiss();
                    }
                }).build().show();
            }
        });
        //首次收益弹窗显示
        viewModel.uc.firstImMsgDialog.observe(this, new Observer<TaskRewardReceiveEntity>() {
            @Override
            public void onChanged(TaskRewardReceiveEntity taskRewardReceiveEntity) {
                if(taskRewardReceiveEntity!=null){
                    TraceDialog.getInstance(getContext())
                            .setConfirmOnlick(new TraceDialog.ConfirmOnclick() {
                                @Override
                                public void confirm(Dialog dialog) {
                                    AppContext.instance().logEvent(AppsFlyerEvent.task_first_profit_toWith);
                                    dialog.dismiss();
                                    //女性是否进行过真人认证
                                    if (!ConfigManager.getInstance().isCertification()) {
                                        Bundle bundle = new Bundle();
                                        bundle.putBoolean("dialog_tw_money", true);
                                        viewModel.start(CertificationFemaleFragment.class.getCanonicalName(),bundle);
                                    } else {
                                        viewModel.start(TwDollarMoneyFragment.class.getCanonicalName());
                                    }
                                }
                            })
                            .setCannelOnclick(new TraceDialog.CannelOnclick() {
                                @Override
                                public void confirm(Dialog dialog) {
                                    dialog.dismiss();
                                }
                            })
                            .AlertTaskMoney(getContext().getDrawable(R.drawable.attendance_success_back),taskRewardReceiveEntity.getTaskType(),taskRewardReceiveEntity.getTaskName(),taskRewardReceiveEntity.getMsg()).show();
                }
            }
        });
    }

    private void initChatView() {
        //CustomChatInputFragment.isAdmin = mChatInfo.getId() != null && mChatInfo.getId().equals(AppConfig.CHAT_SERVICE_USER_ID);
        //初始化
        setTitleBarTitle(mChatInfo.getChatName());
        binding.chatLayout.initDefault();
        C2CChatPresenter presenter = new C2CChatPresenter();
        presenter.setChatInfo(mChatInfo);
        binding.chatLayout.setPresenter(presenter);
        presenter.setCustomChatInputFragmentListener(customChatInputFragmentListener);
        binding.chatLayout.setChatInfo(mChatInfo);
        inputLayout = binding.chatLayout.getInputLayout();
//        inputLayout.enableAudioCall();
        Integer ViewMessagesNumber = ConfigManager.getInstance().getViewMessagesNumber();
        Integer SendMessagesNumber = ConfigManager.getInstance().getSendMessagesNumber();
        CustomChatInputFragment customChatInputFragment = new CustomChatInputFragment();
        customChatInputFragment.setCustomChatInputFragmentListener(this);
        inputLayout.replaceMoreInput(customChatInputFragment);
        //设置客服聊天隐藏
        //inputLayout.setVipType(getTaUserIdIM() == 0);
        inputLayout.setSendOnClickCallbacks(this);//添加发送按钮拦截事件
        MessageRecyclerView.is_read_Map = null;
        MessageRecyclerView messageLayout = binding.chatLayout.getMessageLayout();
        MessageRecyclerView.setCertification(ConfigManager.getInstance().isCertification());
        messageLayout.setSex(ConfigManager.getInstance().isMale());//设置性别
        messageLayout.setIsVip(ConfigManager.getInstance().isVip());//设置VIP状态
        String key = viewModel.getLocalUserDataEntity().getId() + "_" + getTaUserIdIM() + "like";
        //存储追踪成功改变样式
        MessageRecyclerView.setAddLikeMsgId(viewModel.readKeyValue(key));
        MessageRecyclerView.setFlagTipMoney(ConfigManager.getInstance().getTipMoneyShowFlag());
        if (SendMessagesNumber != null) {
            messageLayout.setSend_num(SendMessagesNumber);
        } else {
            messageLayout.setSend_num(1);
        }
        if (SendMessagesNumber != null) {
            messageLayout.setRead_sum(ViewMessagesNumber);
        } else {
            messageLayout.setRead_sum(1);
        }
        if (mChatInfo.getId() != null && mChatInfo.getId().equals(AppConfig.CHAT_SERVICE_USER_ID)) {
            messageLayout.setIsVip(true);
            messageLayout.setSend_num(-1);
            messageLayout.setRead_sum(-1);
        }
        // 设置自己聊天气泡的背景
        messageLayout.setRightBubble(mActivity.getResources().getDrawable(R.drawable.chat_self_bg));
        // 设置朋友聊天气泡的背景
        messageLayout.setLeftBubble(mActivity.getResources().getDrawable(R.drawable.chat_opposite_bg));
        // 设置聊天内容字体大小，朋友和自己用一种字体大小
        messageLayout.setChatContextFontSize(14);
        // 设置自己聊天内容字体颜色
        messageLayout.setRightChatContentFontColor(0xFFFFFFFF);
        // 设置朋友聊天内容字体颜色
        messageLayout.setLeftChatContentFontColor(0xFF666666);
        // 设置聊天时间线的背景
        messageLayout.setChatTimeBubble(new ColorDrawable(0x00000000));
        // 设置聊天时间的字体大小
        messageLayout.setChatTimeFontSize(11);
        // 设置聊天时间的字体颜色
        messageLayout.setChatTimeFontColor(0xFF999999);
        // 设置提示的背景
        messageLayout.setTipsMessageBubble(new ColorDrawable(0x00000000));
        // 设置提示的字体大小
        messageLayout.setTipsMessageFontSize(11);
        // 设置提示的字体颜色
        messageLayout.setTipsMessageFontColor(0xFF999999);
        // 设置默认头像，默认与朋友与自己的头像相同
        messageLayout.setAvatar(R.drawable.default_avatar);
        // 设置头像圆角，不设置则默认不做圆角处理
        messageLayout.setAvatarRadius(50);
        // 设置头像大小
        messageLayout.setAvatarSize(new int[]{48, 48});
        // 从 ChatLayout 里获取 NoticeLayout
        //NoticeLayout noticeLayout = binding.chatLayout.getNoticeLayout();
        // 可以使通知区域一致展示
        //noticeLayout.alwaysShow(true);
        // 设置通知主题
        // noticeLayout.getContent().setText("當前聊天對象可能是機器人！");
        // 设置通知提醒文字
        //noticeLayout.getContentExtra().setText("参看有奖");

        messageLayout.setOnItemClickListener(new OnItemLongClickListener() {
            @Override
            public void onMessageLongClick(View view, int position, MessageInfo messageInfo) {
                //因为adapter中第一条为加载条目，位置需减1
                messageLayout.showItemPopMenu(position - 1, messageInfo, view);
            }

            @Override
            public void onUserIconClick(View view, int position, MessageInfo messageInfo) {
                if (null == messageInfo) {
                    return;
                }
                int userId = ChatUtils.imUserIdToSystemUserId(messageInfo.getFromUser());
                if (userId == AppContext.instance().appRepository.readUserData().getId()) {
                    return;
                }
                Bundle bundle = UserDetailFragment.getStartBundle(userId);
                viewModel.start(UserDetailFragment.class.getCanonicalName(), bundle);
            }

            @Override
            public void onToastVipText(MessageInfo messageInfo) {
                String text = String.valueOf(messageInfo.getExtra());
                if (Utils.isJSON2(text)) {
                    Map<String, Object> map_data = new Gson().fromJson(text, Map.class);
                    if (map_data != null && map_data.get("type") != null && map_data.get("type").equals("toast_local")) {
                        if (map_data.get("status") != null && map_data.get("status").equals("3")) {//发送真人认证提示 :已经发送过
                            CertificationFemaleFragment certificationFemaleFragment = new CertificationFemaleFragment();
                            start(certificationFemaleFragment);
                            return;
                        } else if (map_data.get("status") != null && map_data.get("status").equals("2")) {
                            //添加社区账号、前往聊天对象个人主页
                            if (null == messageInfo) {
                                return;
                            }
                            int userId = getTaUserIdIM(); //获取当前聊天对象的ID
                            if (userId == AppContext.instance().appRepository.readUserData().getId()) {
                                return;
                            }
                            Bundle bundle = UserDetailFragment.getStartBundle(userId);
                            viewModel.start(UserDetailFragment.class.getCanonicalName(), bundle);
                        }
                    }
                }
            }

            @Override
            public void onTextReadUnlock(TextView textView, View view, MessageInfo messageInfo) {
                AppContext.instance().logEvent(AppsFlyerEvent.IM_Unlock);
            }

            @Override
            public void onTextTOWebView(MessageInfo messageInfo) {
                try {
                    String extra = messageInfo.getExtra().toString();
                    if (extra != null && extra.indexOf("href") != -1 && extra.indexOf("</a>") != -1) {
                        String str = ApiUitl.getRegHref(extra);
                        if (str != null) {
                            Uri uri = Uri.parse(str);
                            Intent web = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(web);
                            //viewModel.start(WebDetailFragment.class.getCanonicalName(), WebDetailFragment.getStartBundle(str));
                        }

                    }
                } catch (Exception e) {

                }
            }

            //去往用户主页
            @Override
            public void toUserHome() {
                int userId = getTaUserIdIM(); //获取当前聊天对象的ID
                if (userId == AppContext.instance().appRepository.readUserData().getId()) {
                    return;
                }
                AppContext.instance().logEvent(AppsFlyerEvent.Pchat_photo);
                Bundle bundle = UserDetailFragment.getStartBundle(userId);
                viewModel.start(UserDetailFragment.class.getCanonicalName(), bundle);
            }

            @Override
            public void openUserImage(com.tencent.coustom.PhotoAlbumItemEntity itemEntity) {
                if(itemEntity!=null) {
                    PictureSelectorUtil.previewImage(mActivity, StringUtil.getFullImageWatermarkUrl(itemEntity.getSrc()));
                    //AppContext.instance().logEvent(AppsFlyerEvent.Pchat_photo);
                }
            }
            //评价
            @Override
            public void onClickEvaluate(int position,MessageInfo messageInfo,com.tencent.coustom.EvaluateItemEntity evaluateItemEntity, boolean more) {
                AppContext.instance().logEvent(AppsFlyerEvent.Pchat_Evaluation);
                try {
                    if (more) {//更多
                        long dayTime = System.currentTimeMillis();
                        if (intervalTime != null && (dayTime / 1000) - intervalTime.longValue() <= 2) {
                            return;
                        }
                        intervalTime = dayTime / 1000;
                        viewModel.getUserEvaluate(getTaUserIdIM(), false);
                    } else {
                        viewModel.commitUserEvaluate(getTaUserIdIM(), evaluateItemEntity.getTagId(), null);
                        //messageInfo.remove();
                        //viewModel.commitUserEvaluate(getTaUserIdIM(),evaluateItemEntity.getTagId());
                        //ToastUtils.showShort("你选择了 "+evaluateItemEntity.getName());
                    }
                } catch (Exception e) {

                }
            }

            @Override
            public void onClickCustomText(int position, MessageInfo messageInfo, CustomIMTextEntity customIMTextEntity) {
                if (customIMTextEntity != null) {
                    if (customIMTextEntity.getEvent() == 1) {//上传照片
                        AppContext.instance().logEvent(AppsFlyerEvent.im_tips_photo);
                        viewModel.start(MyPhotoAlbumFragment.class.getCanonicalName());
                    } else if (customIMTextEntity.getEvent() == 2) {//送礼物
                        AppContext.instance().logEvent(AppsFlyerEvent.im_tips_gifts);
                        giftBagDialogShow();
                    } else if (customIMTextEntity.getEvent() == 3) {//追踪
                        AppContext.instance().logEvent(AppsFlyerEvent.im_tips_follow);
                        viewModel.addLike(getTaUserIdIM(), messageInfo.getId());
                    } else if (customIMTextEntity.getEvent() == 4) {//唤醒语音视频聊天
                        AppContext.instance().logEvent(AppsFlyerEvent.im_tips_vv);
                        DialogCallPlayUser();
                    } else if (customIMTextEntity.getEvent() == 11) {//真人认证
                        AppContext.instance().logEvent(AppsFlyerEvent.im_tips_auth);
                        if (ConfigManager.getInstance().isMale()) {
                            viewModel.start(CertificationMaleFragment.class.getCanonicalName());
                        } else {
                            viewModel.start(CertificationFemaleFragment.class.getCanonicalName());
                        }
                    }
                }
            }

            @Override
            public void onClickDialogRechargeShow() {
                dialogRechargeShow(false);
            }

            @Override
            public void clickToUserMain() {
                int userId = getTaUserIdIM(); //获取当前聊天对象的ID
                if (userId == AppContext.instance().appRepository.readUserData().getId()) {
                    return;
                }
                Bundle bundle = UserDetailFragment.getStartBundle(userId);
                viewModel.start(UserDetailFragment.class.getCanonicalName(), bundle);
            }

            @Override
            public void onClickCustomText() {

            }
        });

        messageLayout.setOnCustomMessageDrawListener(new CustomMessageDraw(new CustomMessageDraw.CustomMessageListener() {
            @Override
            public void onLocationMessageClick(CustomMessageData customMessageData) {
                Bundle bundle = LocationMapsFragment.getStartBundle(customMessageData.getText(), customMessageData.getAddress(), customMessageData.getLat(), customMessageData.getLng());
                LocationMapsFragment locationMapsFragment = new LocationMapsFragment();
                locationMapsFragment.setArguments(bundle);
                start(locationMapsFragment);
            }

            @Override
            public void onCoinRedPackageMessageClick(CustomMessageData customMessageData) {
                Bundle bundle = CoinRedPackageDetailFragment.getStartBundle(customMessageData.getId(), customMessageData.getMsgId(), customMessageData.getSenderUserID() == AppContext.instance().appRepository.readUserData().getId());
                CoinRedPackageDetailFragment coinRedPackageDetailFragment = new CoinRedPackageDetailFragment();
                coinRedPackageDetailFragment.setArguments(bundle);
                start(coinRedPackageDetailFragment);
            }

            @Override
            public void onBurnMessageClick(CustomMessageData customMessageData) {
                AlbumPhotoEntity albumPhotoEntity = new AlbumPhotoEntity();
                albumPhotoEntity.setId(0);
                albumPhotoEntity.setMsgId(customMessageData.getMsgId());
                albumPhotoEntity.setSrc(customMessageData.getImgPath());
                if (AppContext.instance().appRepository.readCahtCustomMessageStatus(customMessageData.getMsgId()) == 1) {
                    albumPhotoEntity.setBurnStatus(1);
                } else {
                    albumPhotoEntity.setBurnStatus(0);
                }
                albumPhotoEntity.setIsBurn(1);
                albumPhotoEntity.setType(1);
                Bundle bundle = PhotoBrowseFragment.getStartChatBundle(albumPhotoEntity);
                PhotoBrowseFragment photoBrowseFragment = new PhotoBrowseFragment();
                photoBrowseFragment.setArguments(bundle);
                start(photoBrowseFragment);
            }
        }));

        viewModel.uc.loadMessage.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    messageLayout.setIsVip(true);//设置VIP状态
                    messageLayout.setRead_sum(-1);
                    messageLayout.setSend_num(-1);
                    MessageRecyclerView messageRecyclerView = binding.chatLayout.getMessageLayout();
                    //binding.chatLayout.loadMessages();
                    messageRecyclerView.getAdapter().notifyDataSetChanged();
                    //viewModel.loadUserInfo();
                    ConfigManager.getInstance().DesInstance();
                    ConfigManager.DesInstance();
                }
            }
        });
        viewModel.uc.addLikeSuccess.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String msgId) {
                String key = viewModel.getLocalUserDataEntity().getId() + "_" + getTaUserIdIM() + "like";
                //存储追踪成功改变样式
                viewModel.putKeyValue(key, msgId);
                MessageRecyclerView.setAddLikeMsgId(msgId);
                List<MessageInfo> listDataSource = messageLayout.getAdapter().getDataSource();
                for (int i = 0; i < listDataSource.size(); i++) {
                    if (listDataSource.get(i).getId().equals(msgId)) {
                        messageLayout.getAdapter().notifyItemChanged(i);
                        messageLayout.getAdapter().notifyItemChanged(i + 1);
                        break;
                    }
                }
            }
        });
        if (!StringUtil.isEmpty(toSendMessageText)) {
            Map<String, Object> custom_local_data = new HashMap<>();
            custom_local_data.put("type", "message_tag");
            custom_local_data.put("text", toSendMessageText);
            String str = GsonUtils.toJson(custom_local_data);
            inputLayout.getMessageHandler().sendMessage(ChatMessageInfoUtil.buildTextMessage(str));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        AudioPlayer.getInstance().stopPlay();
    }

    @Override
    public void onPictureActionClick() {
        PictureSelectorUtil.selectImage(mActivity, true, 1, new OnResultCallbackListener<LocalMedia>() {
            @Override
            public void onResult(List<LocalMedia> result) {
                LocalMedia localMedia = result.get(0);
                MessageInfo info = ChatMessageInfoUtil.buildImageMessage(Uri.fromFile(new File(localMedia.getCompressPath())), true);
                binding.chatLayout.sendMessage(info, false);
            }

            @Override
            public void onCancel() {
            }
        });
    }

    @Override
    public void onVideoActionClick() {
        PictureSelectorUtil.selectVideo(mActivity, true, 1, new OnResultCallbackListener<LocalMedia>() {
            @Override
            public void onResult(List<LocalMedia> result) {
                LocalMedia localMedia = result.get(0);

                String path = "";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    path = localMedia.getAndroidQToPath();
                    if (path == null || path.isEmpty()) {
                        path = localMedia.getRealPath();
                    }
                } else {
                    path = localMedia.getRealPath();
                }

                if (StringUtil.isEmpty(path)) {
                    path = localMedia.getPath();
                }

                String thumbPath = ImageUtils.getVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND, 0, 0);
                MessageInfo info = ChatMessageInfoUtil.buildVideoMessage(thumbPath, path, localMedia.getWidth(), localMedia.getHeight(), localMedia.getDuration());
                binding.chatLayout.sendMessage(info, false);
            }

            @Override
            public void onCancel() {
            }
        });
    }

    @Override
    public void onLocationActionClick() {
        //男生发送判断
        if(!sendVerifyMale()){
            dialogRechargeShow(false);
            return;
        }
        startForResult(new ChooseLocationFragment(), 1001);
    }

    @Override
    public void onBurnActionClick() {
        PictureSelectorUtil.selectImage(mActivity, true, 1, new OnResultCallbackListener<LocalMedia>() {
            @Override
            public void onResult(List<LocalMedia> result) {
                PhotoReviewFragment photoReviewFragment = new PhotoReviewFragment();
                photoReviewFragment.setArguments(PhotoReviewFragment.getStartBundle(result.get(0).getCompressPath()));
                startForResult(photoReviewFragment, 2001);
            }

            @Override
            public void onCancel() {
            }
        });

    }

    @Override
    public void onRedPackageActionClick() {
        System.out.println();
    }

    @Override
    public void onCoinRedPackageActionClick() {
        //男生发送判断
        if(!sendVerifyMale()){
            dialogRechargeShow(false);
            return;
        }
        if (mChatInfo == null) {
            return;
        }
        if (mChatInfo.getId() != null && mChatInfo.getId().equals(AppConfig.CHAT_SERVICE_USER_ID)) {
            return;
        }
        int userId = Integer.parseInt(mChatInfo.getId().replace("ru_", ""));
        Bundle bundle = SendCoinRedPackageFragment.getStartBundle(userId);
        SendCoinRedPackageFragment sendCoinRedPackageFragment = new SendCoinRedPackageFragment();
        sendCoinRedPackageFragment.setArguments(bundle);
        startForResult(sendCoinRedPackageFragment, 2002);
    }

    @Override
    public void onMicActionClick() {
        if (mChatInfo != null) {
            try {
                String userId = mChatInfo.getId().replaceFirst("ru_", "");
                viewModel.checkConnMic(Integer.parseInt(userId));
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtils.showShort("error");
            }
        }
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle data) {
        if (resultCode != ISupportFragment.RESULT_OK) {
            return;
        }
        if (requestCode == 1001) {
            String name = data.getString(ChooseLocationFragment.ARG_ADDRESS_NAME);
            String address = data.getString(ChooseLocationFragment.ARG_ADDRESS);
            double lat = data.getDouble(ChooseLocationFragment.ARG_ADDRESS_LAT);
            double lng = data.getDouble(ChooseLocationFragment.ARG_ADDRESS_LNG);
            CustomMessageData customMessageData = CustomMessageData.genLocationMessage(name, address, lat, lng);
            MessageInfo info = ChatMessageInfoUtil.buildCustomMessage(GsonUtils.toJson(customMessageData),null,null);
            binding.chatLayout.sendMessage(info, false);
        } else if (requestCode == 2002) {
            int id = data.getInt(SendCoinRedPackageFragment.ARG_RED_PACKAGE_ID);
            String desc = data.getString(SendCoinRedPackageFragment.ARG_DESC);
            int number = data.getInt(SendCoinRedPackageFragment.ARG_NUMBER, 0);
            CustomMessageData customMessageData = CustomMessageData.genCoinRedPackageMessage(id, number, desc);
            MessageInfo info = ChatMessageInfoUtil.buildCustomMessage(GsonUtils.toJson(customMessageData),null,null);
            binding.chatLayout.sendMessage(info, false);
        } else if (requestCode == 1003) {

        } else if (requestCode == 2001) {
            String imageSrcKey = data.getString(PhotoReviewFragment.ARG_IMAGE_SRC_KEY);
            CustomMessageData customMessageData = CustomMessageData.genBurnMessage(imageSrcKey);
            MessageInfo info = ChatMessageInfoUtil.buildCustomMessage(GsonUtils.toJson(customMessageData),null,null);
            binding.chatLayout.sendMessage(info, false);
        }else if(requestCode == InputMoreFragment.REQUEST_CODE_PHOTO){//选中视频、图片
            String uri = data.toString();
            if (TextUtils.isEmpty(uri)){
                TUIChatLog.e(TAG, "uri is empty");
                return;
            }
            String videoPath = FileUtil.getPathFromUri((Uri) data.get("data"));
            String fileExtension = FileUtil.getFileExtensionFromUrl(videoPath);
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
            if (TextUtils.isEmpty(mimeType)) {
                TUIChatLog.e(TAG, "mimeType is empty.");
                return;
            }
            if (mimeType.contains("video")){
                MessageInfo msg = buildVideoMessage(FileUtil.getPathFromUri((Uri) data.get("data")));
                if (msg == null){
                    TUIChatLog.e(TAG, "start send video error data: " + data);
                } else{
                    inputLayout.getMessageHandler().sendMessage(msg);
                    hideSoftInput();
                }
            } else if (mimeType.contains("image")){
                MessageInfo info = ChatMessageInfoUtil.buildImageMessage((Uri) data.get("data"), true);
                inputLayout.getMessageHandler().sendMessage(info);
                hideSoftInput();
            } else {
                TUIChatLog.e(TAG, "Send photo or video failed , invalid mimeType : " + mimeType);
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode == InputMoreFragment.REQUEST_CODE_PHOTO){//选中视频、图片
            String uri = intent.toString();
            if (TextUtils.isEmpty(uri)){
                TUIChatLog.e(TAG, "uri is empty");
                return;
            }
            String videoPath = FileUtil.getPathFromUri(intent.getData());
            String fileExtension = FileUtil.getFileExtensionFromUrl(videoPath);
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
            if (TextUtils.isEmpty(mimeType)) {
                TUIChatLog.e(TAG, "mimeType is empty.");
                return;
            }
            if (mimeType.contains("video")){
                MessageInfo msg = buildVideoMessage(FileUtil.getPathFromUri(intent.getData()));
                if (msg == null){
                } else{
                    inputLayout.getMessageHandler().sendMessage(msg);
                    hideSoftInput();
                }
            } else if (mimeType.contains("image")){
                MessageInfo info = ChatMessageInfoUtil.buildImageMessage(intent.getData(), true);
                inputLayout.getMessageHandler().sendMessage(info);
                hideSoftInput();
            } else {
                TUIChatLog.e(TAG, "Send photo or video failed , invalid mimeType : " + mimeType);
            }
        }
    }

    private MessageInfo buildVideoMessage(String mUri)
    {
        android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();
        try {
            mmr.setDataSource(mUri);
            String sDuration = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);//时长(毫秒)
            Bitmap bitmap = mmr.getFrameAtTime(0, android.media.MediaMetadataRetriever.OPTION_NEXT_SYNC);//缩略图

            if (bitmap == null){
                TUIChatLog.e(TAG, "buildVideoMessage() bitmap is null");
                return null;
            }

            String imgPath = FileUtil.saveBitmap("JCamera", bitmap);
            String videoPath = mUri;
            int imgWidth = bitmap.getWidth();
            int imgHeight = bitmap.getHeight();
            long duration = Long.valueOf(sDuration);
            MessageInfo msg = ChatMessageInfoUtil.buildVideoMessage(imgPath, videoPath, imgWidth, imgHeight, duration);

            return msg;
        } catch (Exception ex)
        {
            TUIChatLog.e(TAG, "MediaMetadataRetriever exception " + ex);
        } finally {
            mmr.release();
        }

        return null;
    }

    @Override
    public void onDestroy() {
        if (binding.chatLayout != null) {
            binding.chatLayout.exitChat();
        }
        binding.chatLayout.getMessageLayout().setIsReadMap();//清除本次会话可看信息
        super.onDestroy();
    }

    /**
     * 发送语音消息拦截
     *
     * @param messageHandler
     * @param messageInfo
     */
    @Override
    public void sendOnClickAudioMessage(InputView.MessageHandler messageHandler, MessageInfo messageInfo) {
        //男生发送判断
        if(!sendVerifyMale()){
            dialogRechargeShow(false);
            return;
        }
        if (messageHandler != null) {
            messageHandler.sendMessage(messageInfo);
        }
    }

    @Override
    public void onClickPhoneVideo() {//点击选中图片、视频
        //没有钻石、聊天卡唤醒充值
        //男生发送判断
        if(!sendVerifyMale()){
            dialogRechargeShow(false);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimetypes = {"image/*", "video/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);

        startActivityForResult(intent, InputMoreFragment.REQUEST_CODE_PHOTO);
    }

    @Override
    public void onClickGift() {//点击调用礼物
        AppContext.instance().logEvent(AppsFlyerEvent.im_gifts);
        giftBagDialogShow();
    }

    public void giftBagDialogShow() {
        GiftBagDialog giftBagDialog = new GiftBagDialog(getContext(), false, 0, 0);
        giftBagDialog.setGiftOnClickListener(new GiftBagDialog.GiftOnClickListener() {
            @Override
            public void sendGiftClick(Dialog dialog, int number, GiftBagEntity.giftEntity giftEntity) {
                AppContext.instance().logEvent(AppsFlyerEvent.im_send_gifts);
                viewModel.sendUserGift(dialog, giftEntity.getId(), getTaUserIdIM(), number);
            }

            @Override
            public void rechargeStored(Dialog dialog) {
                AppContext.instance().logEvent(AppsFlyerEvent.im_gifts_topup);
                dialog.dismiss();
                dialogRechargeShow(false);
            }
        });
        giftBagDialog.show();
    }

    @Override
    public void onClickCallPlayUser() {//点击调用拨打通话
        DialogCallPlayUser();
    }

    @Override
    public void onClickSendLocation() {//发送位置
        startForResult(new ChooseLocationFragment(), 1001);
    }

    @Override
    public void sendBlackStatus(int status) {

    }

    private void DialogCallPlayUser() {
        if (viewModel.priceConfigEntityField != null) {
            AudioProfitTips = viewModel.priceConfigEntityField.getCurrent().getAudioProfitTips();
            VideoProfitTips = viewModel.priceConfigEntityField.getCurrent().getVideoProfitTips();
        }
        MessageDetailDialog.AudioAndVideoCallDialog(mActivity,
                true,
                AudioProfitTips,
                VideoProfitTips,
                new MessageDetailDialog.AudioAndVideoCallOnClickListener() {
                    @Override
                    public void audioOnClick() {
                        new RxPermissions(mActivity)
                                .request(Manifest.permission.RECORD_AUDIO)
                                .subscribe(granted -> {
                                    if (granted) {
                                        AppContext.instance().logEvent(AppsFlyerEvent.im_voice_call);
                                        viewModel.getCallingInvitedInfo(1, getTaUserIdIM(), mChatInfo.getId());
                                    } else {
                                        TraceDialog.getInstance(mActivity)
                                                .setCannelOnclick(new TraceDialog.CannelOnclick() {
                                                    @Override
                                                    public void confirm(Dialog dialog) {

                                                    }
                                                })
                                                .setConfirmOnlick(new TraceDialog.ConfirmOnclick() {
                                                    @Override
                                                    public void confirm(Dialog dialog) {
                                                        new RxPermissions(mActivity)
                                                                .request(Manifest.permission.RECORD_AUDIO)
                                                                .subscribe(granted -> {
                                                                    if (granted) {
                                                                        AppContext.instance().logEvent(AppsFlyerEvent.im_voice_call);
                                                                        viewModel.getCallingInvitedInfo(1, getTaUserIdIM(), mChatInfo.getId());
                                                                    }
                                                                });
                                                    }
                                                }).AlertCallAudioPermissions().show();
                                    }
                                });
                    }

                    @Override
                    public void videoOnClick() {
                        new RxPermissions(mActivity)
                                .request(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
                                .subscribe(granted -> {
                                    if (granted) {
                                        AppContext.instance().logEvent(AppsFlyerEvent.im_video_call);
                                        viewModel.getCallingInvitedInfo(2, getTaUserIdIM(), mChatInfo.getId());
                                    } else {
                                        TraceDialog.getInstance(mActivity)
                                                .setCannelOnclick(new TraceDialog.CannelOnclick() {
                                                    @Override
                                                    public void confirm(Dialog dialog) {

                                                    }
                                                })
                                                .setConfirmOnlick(new TraceDialog.ConfirmOnclick() {
                                                    @Override
                                                    public void confirm(Dialog dialog) {
                                                        new RxPermissions(mActivity)
                                                                .request(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
                                                                .subscribe(granted -> {
                                                                    if (granted) {
                                                                        AppContext.instance().logEvent(AppsFlyerEvent.im_video_call);
                                                                        viewModel.getCallingInvitedInfo(2, getTaUserIdIM(), mChatInfo.getId());
                                                                    }
                                                                });
                                                    }
                                                }).AlertCallAudioPermissions().show();
                                    }
                                });
                    }
                }).show();
    }

    //弹出钻石充值
    private void dialogRechargeShow(boolean isGiftSend) {
        if (!isGiftSend) {
            AppContext.instance().logEvent(AppsFlyerEvent.im_topup);
        }
//        ChatDetailCoinRechargeSheetView coinRechargeSheetView = new ChatDetailCoinRechargeSheetView(mActivity, getTaUserIdIM(), 1, isGiftSend, false);
//        coinRechargeSheetView.show();
//        coinRechargeSheetView.setCoinRechargeSheetViewListener(new ChatDetailCoinRechargeSheetView.CoinRechargeSheetViewListener() {
//            @Override
//            public void onPaySuccess(ChatDetailCoinRechargeSheetView sheetView, GoodsEntity sel_goodsEntity) {
//                sheetView.dismiss();
//                viewModel.maleBalance = sel_goodsEntity.getCoins();
//            }
//
//            @Override
//            public void onPayFailed(ChatDetailCoinRechargeSheetView sheetView, String msg) {
//                sheetView.dismiss();
//                // do nothing
//            }
//        });

        GameCoinExchargeSheetView coinRechargeSheetView = new GameCoinExchargeSheetView(mActivity);
        coinRechargeSheetView.show();
        coinRechargeSheetView.setCoinRechargeSheetViewListener(new GameCoinExchargeSheetView.CoinRechargeSheetViewListener() {
            @Override
            public void onPaySuccess(GameCoinExchargeSheetView sheetView, CoinExchangePriceInfo sel_goodsEntity) {
                sheetView.dismiss();
                viewModel.maleBalance += sel_goodsEntity.getCoins();
            }

            @Override
            public void onPayFailed(GameCoinExchargeSheetView sheetView, String msg) {
                sheetView.dismiss();
                me.goldze.mvvmhabit.utils.ToastUtils.showShort(msg);
                AppContext.instance().logEvent(AppsFlyerEvent.Failed_to_top_up);
            }
        });

    }
    //男士发送消息效验
    private boolean sendVerifyMale() {
        if (getTaUserIdIM().intValue() == 0) {
            return true;
        }
        if (viewModel.isFollower) {
            return true;
        }
        if (viewModel.isPlay) {
            //有聊天卡
            if (viewModel.maleCardNumber > 0) {
                viewModel.maleCardNumber--;
                return true;
            } else {
                if (viewModel.maleBalance == 0) {
                    return false;
                } else {
                    if(viewModel.maleBalance-viewModel.maleMessagePrice>=0){
                        viewModel.maleBalance = viewModel.maleBalance-viewModel.maleMessagePrice;
                        return true;
                    }else{
                        return false;
                    }
                }
            }
        }else{
            return true;
        }
    }

    @Override
    public void sendOnClickCallbackOk(InputView.MessageHandler messageHandler, MessageInfo messageInfo) {
        if (messageHandler != null) {
            UserDataEntity userDataEntity = viewModel.getLocalUserDataEntity();
            if (userDataEntity == null) {
                return;
            }
            int sex = userDataEntity.getSex();
            int isVip = userDataEntity.getIsVip();
            String value = String.valueOf(messageInfo.getExtra());
            if (isVip != 1 && MessageInfo.MSG_TYPE_TEXT == messageInfo.getMsgType() && sex == 1) { //賴  瀨  line
                if (binding.chatLayout.getMessageLayout().getAdapter().getDataSource().size() < 3 && (value.indexOf("賴") != -1 || value.indexOf("瀨") != -1 || value.indexOf("line") != -1)) {
                    sendLocalMessage(value);
                    return;
                }
            }
            if (mChatInfo.getId().equals(AppConfig.CHAT_SERVICE_USER_ID)) { //客服放行
                messageHandler.sendMessage(messageInfo);
                return;
            }
            if (sex == 0) {//女性用户
                if (viewModel.isFollower) {
                    messageHandler.sendMessage(messageInfo);
                    return;
                }else {
                    //男生发送判断
                    if (!sendVerifyMale()) {
                        dialogRechargeShow(false);
                        sendLocalMessage(messageInfo.getExtra().toString());
                        sendLocalMessage("send_male_error", "send_male_error", null);
                        return;
                    } else {
                        messageHandler.sendMessage(messageInfo);
                    }
                }
            } else {
                if (viewModel.isFollower) {
                    messageHandler.sendMessage(messageInfo);
                } else {
                    //男生发送判断
                    if (!sendVerifyMale()) {
                        dialogRechargeShow(false);
                        sendLocalMessage(messageInfo.getExtra().toString());
                        sendLocalMessage("send_male_error", "send_male_error", null);
                        return;
                    } else {
                        messageHandler.sendMessage(messageInfo);
                    }
                }
            }

        }
    }

    /**
     * 发送自定义消息。只能自己看
     */
    public void sendLocalMessage(String value, String type, Object status) {
        Map<String, Object> custom_local_data = new HashMap<>();
        custom_local_data.put("type", type);
        custom_local_data.put("status", status);
        custom_local_data.put("text", value);
        String str = GsonUtils.toJson(custom_local_data);
        //发送本地消息，并且自定它
        sendLocalMessage(str);
    }

    /**
     * 发送自定义消息。只能自己看
     */
    public void sendLocalMessage(String value) {
        //发送本地消息，并且自定它
        ChatMessageInfoUtil.C2CMessageToLocal(value, mChatInfo.getId(), new V2TIMValueCallback() {
            @Override
            public void onError(int code, String desc) {
            }

            @Override
            public void onSuccess(Object o) {
                V2TIMMessage v2TIMMessage = (V2TIMMessage) o;
                String msgId = v2TIMMessage.getMsgID();
                List<String> list = new ArrayList<>();
                list.add(msgId);
                V2TIMManager.getMessageManager().findMessages(list, new V2TIMValueCallback() {
                    @Override
                    public void onError(int code, String desc) {
                    }

                    @Override
                    public void onSuccess(Object o) {
                        ArrayList<V2TIMMessage> messages = (ArrayList) o;
                        MessageInfo msgInfos = ChatMessageInfoUtil.convertTIMMessage2MessageInfo(messages.get(0));
                        ChatPresenter chatPresenter = binding.chatLayout.getChatPresenter();
                        boolean bl = chatPresenter.addMessageList(msgInfos, false);
                    }
                });
            }
        });
    }

    //将相册、评价排到消息列表最顶端
    public void sortMessageList(){
        //接收到消息列表已经渲染过来数据
        MessageRecyclerView messageRecyclerView = binding.chatLayout.getMessageLayout();
        if(!ObjectUtils.isEmpty(messageRecyclerView)){
            List<MessageInfo> listDataSource  = messageRecyclerView.getAdapter().getDataSource();
            int itemCount = listDataSource.size();
            for (int i = 0; i < itemCount; i++){
                MessageInfo lastMsg = listDataSource.get(i);
                if (lastMsg != null) {
                    if (lastMsg.getExtra() != null) {
                        if (lastMsg.isSelf()) {
                            if (isJSON2(lastMsg.getExtra().toString())) {//判断C2c本地添加记录自定义。通常用来做弹窗
                                Map<String, Object> map_data = new Gson().fromJson(lastMsg.getExtra().toString(), Map.class);
                                if (map_data != null && map_data.get("type") != null) {
                                    if (map_data.get("type").equals("message_photo")) {//相册类型置顶
                                        if(i==0){
                                            return;
                                        }
                                        messageRecyclerView.getAdapter().getDataSource().add(0,listDataSource.remove(i));
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //发送本地消息记录 照片、评价
    public void addLocalMessage(String type,final String EventId,String objData) {
        Map<String, Object> custom_local_data = new HashMap<>();
        custom_local_data.put("type", type);
        custom_local_data.put("data", objData);
        String str = GsonUtils.toJson(custom_local_data);
        //LocalMessageIMEntity localMessageIMEntity = LocalDataSourceImpl.getInstance().readLocalMessageIM(EventId);
        LocalMessageIMEntity localMessageIMEntity = null;
        MessageRecyclerView messageRecyclerView = binding.chatLayout.getMessageLayout();
        if (messageRecyclerView != null) {
            if (ObjectUtils.isEmpty(localMessageIMEntity)) {//没有历史消息
//            Log.e("聊天规则配置插入相册","没有历史消息");
                //发送本地消息，并且自定它
                ChatMessageInfoUtil.C2CMessageToLocal(str, mChatInfo.getId(), new V2TIMValueCallback() {
                    @Override
                    public void onError(int code, String desc) {
                    }

                    @Override
                    public void onSuccess(Object o) {
                        V2TIMMessage v2TIMMessage = (V2TIMMessage) o;
                        String msgId = v2TIMMessage.getMsgID();
                        List<String> list = new ArrayList<>();
                        list.add(msgId);
                        V2TIMManager.getMessageManager().findMessages(list, new V2TIMValueCallback() {
                            @Override
                            public void onError(int code, String desc) {
                            }

                            @Override
                            public void onSuccess(Object o) {
                                ArrayList<V2TIMMessage> messages = (ArrayList) o;
                                MessageInfo msgInfos = ChatMessageInfoUtil.convertTIMMessage2MessageInfo(messages.get(0));
                                if (type.equals("message_photo")) {
                                    binding.chatLayout.getChatPresenter().addMessageInfo(msgInfos);
                                } else {
                                    boolean bl = binding.chatLayout.getChatPresenter().addMessageList(msgInfos, true);
                                }
                                //添加本地记录
                                LocalDataSourceImpl.getInstance().putLocalMessageIM(EventId, msgInfos.getId(), System.currentTimeMillis());
                                // binding.chatLayout.setDataProvider(iChatProvider);
                            }
                        });
                    }
                });
            } else {
//            Log.e("聊天规则配置插入相册","有历史消息");
                LocalDataSourceImpl.getInstance().removeLocalMessage(EventId);
                removeLocalMessage(localMessageIMEntity, EventId, false);
                String LocalMsgId = localMessageIMEntity.getMsgId();
                List<MessageInfo> listMessage = messageRecyclerView.getAdapter().getDataSource();
                boolean flag = false;
                String msgIds = null;
                Integer toUserId = getTaUserIdIM();
                if (LocalMsgId.indexOf(getTaUserIdIM()) != -1) {
                    flag = true;
                    msgIds = LocalMsgId.replace(toUserId + "-", "");
                }
                for (int i = 0; i < listMessage.size(); i++) {
                    if (flag && (listMessage.get(i).getId().indexOf(msgIds) != -1 || listMessage.get(i).getTimMessage().getMsgID().indexOf(msgIds) != -1)) {
                        messageRecyclerView.getAdapter().getItem(i).setExtra(objData);
                        messageRecyclerView.getAdapter().getItem(i).notify();
                        //iChatProvider.getAdapter().getItem(i).notify();
                    }
                }
                //发送本地消息，并且自定它
                ChatMessageInfoUtil.C2CMessageToLocal(str, mChatInfo.getId(), new V2TIMValueCallback() {
                    @Override
                    public void onError(int code, String desc) {
                    }

                    @Override
                    public void onSuccess(Object o) {
                        V2TIMMessage v2TIMMessage = (V2TIMMessage) o;
                        String msgId = v2TIMMessage.getMsgID();
                        LocalDataSourceImpl.getInstance().putLocalMessageIM(EventId, msgId, System.currentTimeMillis());
                    }
                });
            }
        }
    }
    public static boolean isJSON2(String str) {
        boolean result = false;
        try {
            new Gson().fromJson(str, Map.class);
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return result;

    }


    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return 360;
    }
    //获取聊天对象的UserId
    public Integer getTaUserIdIM() {
        return ChatUtils.imUserIdToSystemUserId(mChatInfo.getId());
    }

    public synchronized void removeLocalMessage(LocalMessageIMEntity localMessageIMEntity,String eventId,boolean updateView){
        List<String> list = new ArrayList<>();
        list.add(localMessageIMEntity.getMsgId());
        V2TIMManager.getMessageManager().findMessages(list, new V2TIMValueCallback() {
            @Override
            public void onError(int code, String desc) {
            }
            @Override
            public void onSuccess(Object o) {
                ArrayList<V2TIMMessage> messages = (ArrayList) o;
                if(messages==null || messages.isEmpty()){
                    return;
                }
                //String toUserId = V2TIMManager.getInstance().getLoginUser();
                //binding.chatLayout.getChatManager().removeMessage(localMessageIMEntity.getMsgId(),toUserId);
                if(updateView){
                    String toUserId = V2TIMManager.getInstance().getLoginUser();
                    binding.chatLayout.getChatPresenter().removeMessage(localMessageIMEntity.getMsgId(),toUserId);
                }
                V2TIMManager.getMessageManager().deleteMessageFromLocalStorage(messages.get(0), new V2TIMCallback() {
                    @Override
                    public void onError(int code, String desc) {
                    }

                    @Override
                    public void onSuccess() {

                      //  binding.chatLayout.getChatManager().deleteMessageInfo(messages.get(0));
                        //LocalDataSourceImpl.getInstance().removeLocalMessage(eventId);
                    }
                });

            }
        });
    }

    //加载IM信息回调
    public class ChatCustomChatInputFragmentListener implements C2CChatPresenter.CustomImMessageLoadListener{

        @Override
        public void layoutLoadMessage(ChatPresenter provider) {
            //sortMessageList();
        }
    }
}
