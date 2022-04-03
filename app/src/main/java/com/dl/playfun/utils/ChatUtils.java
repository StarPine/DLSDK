package com.dl.playfun.utils;

import static com.dl.playfun.ui.message.chatdetail.ChatDetailFragment.CHAT_INFO;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.R;
import com.dl.playfun.ui.message.chatdetail.ChatDetailFragment;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMFriendOperationResult;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.tencent.liteav.trtccalling.model.TUICalling;
import com.tencent.liteav.trtccalling.model.impl.TUICallingManager;
import com.tencent.qcloud.tuicore.component.interfaces.IUIKitCallback;
import com.tencent.qcloud.tuicore.util.ToastUtil;
import com.tencent.qcloud.tuikit.tuichat.bean.ChatInfo;
import com.tencent.qcloud.tuikit.tuicontact.bean.FriendApplicationBean;
import com.tencent.qcloud.tuikit.tuicontact.model.ContactProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @author litchi
 */
public class ChatUtils {
    public static final String TAG = "ChatUtils";

    private static ContactProvider provider;

    public static void chatUserView(String serviceUserId, Integer userId, String name, BaseViewModel baseViewModel) {
        if(provider==null){
            provider = new ContactProvider();
        }

        provider.addFriend(serviceUserId, null, new IUIKitCallback<Pair<Integer, String>>() {
            @Override
            public void onSuccess(Pair<Integer, String> data) {
                switch (data.first) {

                    case FriendApplicationBean.ERR_SUCC:
                        toChatUser(serviceUserId, userId, name, baseViewModel);
                        break;
                    case FriendApplicationBean.ERR_SVR_FRIENDSHIP_INVALID_PARAMETERS:
                        if (TextUtils.equals(data.second, "Err_SNS_FriendAdd_Friend_Exist")) {
                            toChatUser(serviceUserId, userId, name, baseViewModel);
                            break;
                        }
                    case FriendApplicationBean.ERR_SVR_FRIENDSHIP_COUNT_LIMIT:
                        ToastUtil.toastShortMessage(StringUtils.getString(com.tencent.qcloud.tuikit.tuicontact.R.string.friend_limit));
                        break;
                    case FriendApplicationBean.ERR_SVR_FRIENDSHIP_PEER_FRIEND_LIMIT:
                        ToastUtil.toastShortMessage(StringUtils.getString(com.tencent.qcloud.tuikit.tuicontact.R.string.other_friend_limit));
                        break;
                    case FriendApplicationBean.ERR_SVR_FRIENDSHIP_IN_SELF_BLACKLIST:
                        ToastUtil.toastShortMessage(StringUtils.getString(com.tencent.qcloud.tuikit.tuicontact.R.string.in_blacklist));
                        break;
                    case FriendApplicationBean.ERR_SVR_FRIENDSHIP_ALLOW_TYPE_DENY_ANY:
                        ToastUtil.toastShortMessage(StringUtils.getString(com.tencent.qcloud.tuikit.tuicontact.R.string.forbid_add_friend));
                        break;
                    case FriendApplicationBean.ERR_SVR_FRIENDSHIP_IN_PEER_BLACKLIST:
                        ToastUtil.toastShortMessage(StringUtils.getString(com.tencent.qcloud.tuikit.tuicontact.R.string.set_in_blacklist));
                        break;
                    case FriendApplicationBean.ERR_SVR_FRIENDSHIP_ALLOW_TYPE_NEED_CONFIRM:
                        ToastUtil.toastShortMessage(StringUtils.getString(com.tencent.qcloud.tuikit.tuicontact.R.string.wait_agree_friend));
                        break;
                    default:
                        ToastUtil.toastLongMessage(data.first + " " + data.second);
                        break;
                }
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                if (errCode == 30003) {
                    ToastUtils.showShort(R.string.playfun_chat_account_not_exit);
                    return;
                }
                ToastUtil.toastShortMessage("Error code = " + errCode);
            }
        });
    }

    public static void chatUserView(String serviceUserId, Integer userId,String name, BaseViewModel baseViewModel, String MessageText) {

        if(provider==null){
            provider = new ContactProvider();
        }
        provider.addFriend(serviceUserId, null, new IUIKitCallback<Pair<Integer, String>>() {
            @Override
            public void onSuccess(Pair<Integer, String> data) {
                switch (data.first) {

                    case FriendApplicationBean.ERR_SUCC:
                        toChatUser(serviceUserId, userId, name, baseViewModel, MessageText);
                        break;
                    case FriendApplicationBean.ERR_SVR_FRIENDSHIP_INVALID_PARAMETERS:
                        if (TextUtils.equals(data.second, "Err_SNS_FriendAdd_Friend_Exist")) {
                            toChatUser(serviceUserId, userId, name, baseViewModel, MessageText);
                            break;
                        }
                    case FriendApplicationBean.ERR_SVR_FRIENDSHIP_COUNT_LIMIT:
                        ToastUtil.toastShortMessage(StringUtils.getString(com.tencent.qcloud.tuikit.tuicontact.R.string.friend_limit));
                        break;
                    case FriendApplicationBean.ERR_SVR_FRIENDSHIP_PEER_FRIEND_LIMIT:
                        ToastUtil.toastShortMessage(StringUtils.getString(com.tencent.qcloud.tuikit.tuicontact.R.string.other_friend_limit));
                        break;
                    case FriendApplicationBean.ERR_SVR_FRIENDSHIP_IN_SELF_BLACKLIST:
                        ToastUtil.toastShortMessage(StringUtils.getString(com.tencent.qcloud.tuikit.tuicontact.R.string.in_blacklist));
                        break;
                    case FriendApplicationBean.ERR_SVR_FRIENDSHIP_ALLOW_TYPE_DENY_ANY:
                        ToastUtil.toastShortMessage(StringUtils.getString(com.tencent.qcloud.tuikit.tuicontact.R.string.forbid_add_friend));
                        break;
                    case FriendApplicationBean.ERR_SVR_FRIENDSHIP_IN_PEER_BLACKLIST:
                        ToastUtil.toastShortMessage(StringUtils.getString(com.tencent.qcloud.tuikit.tuicontact.R.string.set_in_blacklist));
                        break;
                    case FriendApplicationBean.ERR_SVR_FRIENDSHIP_ALLOW_TYPE_NEED_CONFIRM:
                        ToastUtil.toastShortMessage(StringUtils.getString(com.tencent.qcloud.tuikit.tuicontact.R.string.wait_agree_friend));
                        break;
                    default:
                        ToastUtil.toastLongMessage(data.first + " " + data.second);
                        break;
                }
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                if (errCode == 30003) {
                    ToastUtils.showShort(R.string.playfun_chat_account_not_exit);
                    return;
                }
                ToastUtil.toastShortMessage("Error code = " + errCode);
            }
        });
    }

    private static void toChatUser(String serviceUserId, Integer userId,String name, BaseViewModel baseViewModel) {
        ChatInfo chatInfo = new ChatInfo();
        chatInfo.setType(V2TIMConversation.V2TIM_C2C);
        chatInfo.setId(serviceUserId);
        chatInfo.setChatName(name);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CHAT_INFO, chatInfo);
        bundle.putInt("toUserId", userId);
        baseViewModel.start(ChatDetailFragment.class.getCanonicalName(), bundle);
    }

    private static void toChatUser(String serviceUserId,Integer userId, String name, BaseViewModel baseViewModel, String messageText) {
        ChatInfo chatInfo = new ChatInfo();
        chatInfo.setType(V2TIMConversation.V2TIM_C2C);
        chatInfo.setId(serviceUserId);
        chatInfo.setChatName(name);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CHAT_INFO, chatInfo);
        bundle.putInt("toUserId", userId);
        bundle.putString("message", messageText);
        //Bundle bundle = ChatDetailFragment.getStartBundle(chatInfo,messageText);
        baseViewModel.start(ChatDetailFragment.class.getCanonicalName(), bundle);
    }

    /**
    * @Desc TODO(进入1v1单聊页面)
    * @author 彭石林
    * @parame [ImUserId, name, baseViewModel]
    * @return void
    * @Date 2022/4/2
    */
    public static void chatUser(String ImUserId,Integer userId, String name, BaseViewModel baseViewModel) {
        chatUserView(ImUserId,userId, name, baseViewModel);
    }

    public static void audioCall(String userId, String name) {
        String[] userList ={userId};
        TUICallingManager.sharedInstance().call(userList,TUICalling.Type.AUDIO);
    }

}
