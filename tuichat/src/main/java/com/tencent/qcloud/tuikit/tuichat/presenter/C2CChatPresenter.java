package com.tencent.qcloud.tuikit.tuichat.presenter;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.tencent.coustom.CustomIMTextEntity;
import com.tencent.coustom.IMGsonUtils;
import com.tencent.qcloud.tuicore.component.interfaces.IUIKitCallback;
import com.tencent.qcloud.tuikit.tuichat.TUIChatService;
import com.tencent.qcloud.tuikit.tuichat.bean.ChatInfo;
import com.tencent.qcloud.tuikit.tuichat.TUIChatConstants;
import com.tencent.qcloud.tuikit.tuichat.bean.C2CMessageReceiptInfo;
import com.tencent.qcloud.tuikit.tuichat.bean.MessageReceiptInfo;
import com.tencent.qcloud.tuikit.tuichat.bean.message.TUIMessageBean;
import com.tencent.qcloud.tuikit.tuichat.interfaces.C2CChatEventListener;
import com.tencent.qcloud.tuikit.tuichat.ui.view.message.MessageRecyclerView;
import com.tencent.qcloud.tuikit.tuichat.util.TUIChatLog;
import com.tencent.qcloud.tuikit.tuichat.util.TUIChatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class C2CChatPresenter extends ChatPresenter {
    private static final String TAG = C2CChatPresenter.class.getSimpleName();
    //临时改动
    //彭石林临时添加
    CustomImMessageLoadListener customImMessageLoad;
    private ChatInfo chatInfo;

    private C2CChatEventListener chatEventListener;

    public C2CChatPresenter() {
        super();
        TUIChatLog.i(TAG, "C2CChatPresenter Init");
    }

    public void initListener() {
        chatEventListener = new C2CChatEventListener() {
            @Override
            public void onReadReport(List<MessageReceiptInfo> receiptList) {
                C2CChatPresenter.this.onReadReport(receiptList);
            }

            @Override
            public void exitC2CChat(String chatId) {
                C2CChatPresenter.this.onExitChat(chatId);
            }

            @Override
            public void handleRevoke(String msgId) {
                C2CChatPresenter.this.handleRevoke(msgId);
            }

            @Override
            public void onRecvNewMessage(TUIMessageBean message) {
                if (chatInfo == null || !TextUtils.equals(message.getUserId(), chatInfo.getId())) {
                    TUIChatLog.i(TAG, "receive a new message , not belong to current chat.");
                } else {
                    C2CChatPresenter.this.onRecvNewMessage(message);
                }
            }

            @Override
            public void onFriendNameChanged(String userId, String newName) {
                if (chatInfo == null || !TextUtils.equals(userId, chatInfo.getId())) {
                    return;
                }
                C2CChatPresenter.this.onFriendInfoChanged();
            }

            @Override
            public void onRecvMessageModified(TUIMessageBean messageBean) {
                if (chatInfo == null || !TextUtils.equals(messageBean.getUserId(), chatInfo.getId())) {
                    return;
                }
                C2CChatPresenter.this.onRecvMessageModified(messageBean);
            }

            @Override
            public void addMessage(TUIMessageBean messageBean, String chatId) {
                if (TextUtils.equals(chatId, chatInfo.getId())) {
                    addMessageInfo(messageBean);
                }
            }
        };
        TUIChatService.getInstance().addC2CChatEventListener(chatEventListener);
        initMessageSender();
    }

    /**
     * 拉取消息
     * @param type 向前，向后或者前后同时拉取
     * @param lastMessageInfo 拉取消息的起始点
     */
    @Override
    public void loadMessage(int type, TUIMessageBean lastMessageInfo, IUIKitCallback<List<TUIMessageBean>> callback) {
        if (chatInfo == null || isLoading) {
            return;
        }
        isLoading = true;

        String chatId = chatInfo.getId();
        // 向前拉取更旧的消息
        if (type == TUIChatConstants.GET_MESSAGE_FORWARD) {
            provider.loadC2CMessage(chatId, MSG_PAGE_COUNT, lastMessageInfo, new IUIKitCallback<List<TUIMessageBean>>() {

                @Override
                public void onSuccess(List<TUIMessageBean> data) {
                    TUIChatLog.i(TAG, "load c2c message success " + data.size());
                    if (lastMessageInfo == null) {
                        isHaveMoreNewMessage = false;
                    }
                    int itemCount = data.size();
                    for (int i = 0; i < itemCount; i++) {
                        TUIMessageBean lastMsg = data.get(i);
                        if (lastMsg != null && lastMsg.getExtra() != null) {
                            if (isJSON2(lastMsg.getExtra().toString())) {//判断后台自定义消息体
                                Map<String, Object> map_data = new Gson().fromJson(lastMsg.getExtra().toString(), Map.class);
                                if (map_data != null && map_data.get("type") != null) {
                                    if (map_data.get("type").equals("message_photo")) {//相册类型置顶
                                        if (i != itemCount - 1) {
                                            data.add(itemCount - 1, data.remove(i));
                                            continue;
                                        }
                                    }
                                    if (map_data.get("type").equals("chat_earnings")) {
                                        CustomIMTextEntity customIMTextEntity = IMGsonUtils.fromJson(String.valueOf(map_data.get("data")), CustomIMTextEntity.class);
                                        if (customIMTextEntity != null) {
                                            String msgID = customIMTextEntity.getMsgID();
                                            if (msgID != null) {
                                                for (int j = 0; j < itemCount; j++) {
                                                    TUIMessageBean backMsg = data.get(j);
                                                    if (backMsg.getId().lastIndexOf(msgID) != -1) {//收益提示追加到指定文案后
                                                        data.add(i, data.remove(j));
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    TUIChatUtils.callbackOnSuccess(callback, data);
                    onMessageLoadCompleted(data, type);
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    TUIChatLog.e(TAG, "load c2c message failed " + errCode + "  " + errMsg);
                    TUIChatUtils.callbackOnError(callback, errCode, errMsg);
                }
            });
        } else { // 向后拉更新的消息 或者 前后同时拉消息
            loadHistoryMessageList(chatId, false, type, MSG_PAGE_COUNT, lastMessageInfo, callback);
        }
    }

    // 加载消息成功之后会调用此方法
    @Override
    protected void onMessageLoadCompleted(List<TUIMessageBean> data, int getType) {
        c2cReadReport(chatInfo.getId());
        getMessageReadReceipt(data, getType);
        //彭石林新增
        if(customImMessageLoad!=null){
            customImMessageLoad.layoutLoadMessage(this);
        }
    }

    public void setChatInfo(ChatInfo chatInfo) {
        this.chatInfo = chatInfo;
    }

    @Override
    public ChatInfo getChatInfo() {
        return chatInfo;
    }

    public void onFriendNameChanged(String newName) {
        if (chatNotifyHandler != null) {
            chatNotifyHandler.onFriendNameChanged(newName);
        }
    }

    public void onReadReport(List<MessageReceiptInfo> receiptList) {
        if (chatInfo != null) {
            List<MessageReceiptInfo> processReceipts = new ArrayList<>();
            for (MessageReceiptInfo messageReceiptInfo : receiptList) {
                if (!TextUtils.equals(messageReceiptInfo.getUserID(), chatInfo.getId())) {
                    continue;
                }
                processReceipts.add(messageReceiptInfo);
            }
            onMessageReadReceiptUpdated(loadedMessageInfoList, processReceipts);
        }
    }

    public void onFriendInfoChanged() {
        provider.getFriendName(chatInfo.getId(), new IUIKitCallback<String[]>() {
            @Override
            public void onSuccess(String[] data) {
                String displayName = chatInfo.getId();
                if (!TextUtils.isEmpty(data[0])) {
                    displayName = data[0];
                } else if (!TextUtils.isEmpty(data[1])) {
                    displayName = data[1];
                }
                onFriendNameChanged(displayName);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {

            }
        });
    }

    public void setCustomChatInputFragmentListener(CustomImMessageLoadListener listener){
        this.customImMessageLoad = listener;
    }

    public interface CustomImMessageLoadListener{
        void layoutLoadMessage(ChatPresenter provider);
    }
}
