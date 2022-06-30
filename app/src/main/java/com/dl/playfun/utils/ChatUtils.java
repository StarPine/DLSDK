package com.dl.playfun.utils;

import static com.dl.playfun.ui.message.chatdetail.ChatDetailFragment.CHAT_INFO;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.R;
import com.dl.playfun.ui.message.chatdetail.ChatDetailFragment;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMUserFullInfo;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.tencent.liteav.trtccalling.TUICalling;
import com.tencent.liteav.trtccalling.TUICallingImpl;
import com.tencent.qcloud.tuicore.component.interfaces.IUIKitCallback;
import com.tencent.qcloud.tuicore.util.ToastUtil;
import com.tencent.qcloud.tuikit.tuichat.bean.ChatInfo;
import com.tencent.qcloud.tuikit.tuicontact.TUIContactConstants;
import com.tencent.qcloud.tuikit.tuicontact.TUIContactService;
import com.tencent.qcloud.tuikit.tuicontact.bean.ContactItemBean;
import com.tencent.qcloud.tuikit.tuicontact.bean.FriendApplicationBean;
import com.tencent.qcloud.tuikit.tuicontact.model.ContactProvider;
import com.tencent.qcloud.tuikit.tuicontact.ui.pages.FriendProfileActivity;

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
        //待获取用户资料的用户列表
        List<String> users = new ArrayList<String>();
        users.add(serviceUserId);
        //获取用户资料
        V2TIMManager.getInstance().getUsersInfo(users,new V2TIMValueCallback<List<V2TIMUserFullInfo>>(){
            @Override
            public void onSuccess(List<V2TIMUserFullInfo> v2TIMUserFullInfos) {
                if(v2TIMUserFullInfos!=null && !v2TIMUserFullInfos.isEmpty()){
                    toChatUser(serviceUserId, userId, name, baseViewModel);
                }
            }

            @Override
            public void onError(int code, String desc){
                //错误码 code 和错误描述 desc，可用于定位请求失败原因
                Log.e("获取用户信息失败", "getUsersProfile failed: " + code + " desc");
                ToastUtil.toastLongMessage(code + " " + desc);
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

}
