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

    public static void deleteFriend(int userId) {
        if(provider==null){
            provider = new ContactProvider();
        }
        List<String> idList = new ArrayList<>();
        idList.add(getImUserId(userId));
        provider.deleteFriend(idList, new IUIKitCallback<Void>() {
            @Override
            public void onSuccess(Void data) {

            }

            @Override
            public void onError(String module, int errCode, String errMsg) {

            }
        });
    }

    public static void chatUser(String userId, String name, BaseViewModel baseViewModel) {
        String serviceUserId = userId;
        if(provider==null){
            provider = new ContactProvider();
        }

        provider.addFriend(userId, null, new IUIKitCallback<Pair<Integer, String>>() {
            @Override
            public void onSuccess(Pair<Integer, String> data) {
                switch (data.first) {

                    case FriendApplicationBean.ERR_SUCC:
                        toChatUser(serviceUserId, name, baseViewModel);
                        break;
                    case FriendApplicationBean.ERR_SVR_FRIENDSHIP_INVALID_PARAMETERS:
                        if (TextUtils.equals(data.second, "Err_SNS_FriendAdd_Friend_Exist")) {
                            toChatUser(serviceUserId, name, baseViewModel);
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

    public static void chatUser(String userId, String name, BaseViewModel baseViewModel, String MessageText) {
        String serviceUserId = userId;

        if(provider==null){
            provider = new ContactProvider();
        }
        provider.addFriend(userId, null, new IUIKitCallback<Pair<Integer, String>>() {
            @Override
            public void onSuccess(Pair<Integer, String> data) {
                switch (data.first) {

                    case FriendApplicationBean.ERR_SUCC:
                        toChatUser(serviceUserId, name, baseViewModel, MessageText);
                        break;
                    case FriendApplicationBean.ERR_SVR_FRIENDSHIP_INVALID_PARAMETERS:
                        if (TextUtils.equals(data.second, "Err_SNS_FriendAdd_Friend_Exist")) {
                            toChatUser(serviceUserId, name, baseViewModel, MessageText);
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

    private static void toChatUser(String serviceUserId, String name, BaseViewModel baseViewModel) {
        ChatInfo chatInfo = new ChatInfo();
        chatInfo.setType(V2TIMConversation.V2TIM_C2C);
        chatInfo.setId(serviceUserId);
        chatInfo.setChatName(name);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CHAT_INFO, chatInfo);
        baseViewModel.start(ChatDetailFragment.class.getCanonicalName(), bundle);
    }

    private static void toChatUser(String serviceUserId, String name, BaseViewModel baseViewModel, String messageText) {
        ChatInfo chatInfo = new ChatInfo();
        chatInfo.setType(V2TIMConversation.V2TIM_C2C);
        chatInfo.setId(serviceUserId);
        chatInfo.setChatName(name);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CHAT_INFO, chatInfo);
        bundle.putString("message", messageText);
        //Bundle bundle = ChatDetailFragment.getStartBundle(chatInfo,messageText);
        baseViewModel.start(ChatDetailFragment.class.getCanonicalName(), bundle);
    }

    public static void chatUser(int userId, String name, BaseViewModel baseViewModel) {
        String serviceUserId = String.format(StringUtils.getString(R.string.playfun_imsdk_user_id), userId);
        chatUser(serviceUserId, name, baseViewModel);
    }

    public static void audioCall(String userId, String name) {
        String[] userList ={userId};
        TUICallingManager.sharedInstance().call(userList,TUICalling.Type.AUDIO);
    }

    public static int imUserIdToSystemUserId(String userId) {
        try {
            String strId = userId.replaceFirst("ru_", "");
            return Integer.parseInt(strId);
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getImUserId(int userId) {
        return String.format(StringUtils.getString(R.string.playfun_imsdk_user_id), userId);
    }

    public static void addToBlackList(int userId) {
        addToBlackList(userId, null);
    }

    public static void deleteFromBlackList(int userId) {
        deleteFromBlackList(userId, null);
    }

    public static void addToBlackList(int userId, V2TIMValueCallback<List<V2TIMFriendOperationResult>> callback) {
        List<String> idList = new ArrayList<>();
        idList.add(getImUserId(userId));
        V2TIMManager.getFriendshipManager().addToBlackList(idList, callback);
    }

    public static void deleteFromBlackList(int userId, V2TIMValueCallback<List<V2TIMFriendOperationResult>> callback) {
        List<String> idList = new ArrayList<>();
        idList.add(getImUserId(userId));
        V2TIMManager.getFriendshipManager().deleteFromBlackList(idList, callback);
    }


}
