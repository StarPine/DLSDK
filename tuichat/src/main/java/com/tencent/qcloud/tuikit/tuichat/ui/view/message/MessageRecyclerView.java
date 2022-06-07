package com.tencent.qcloud.tuikit.tuichat.ui.view.message;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.coustom.CustomIMTextEntity;
import com.tencent.coustom.EvaluateItemEntity;
import com.tencent.coustom.PhotoAlbumItemEntity;
import com.tencent.qcloud.tuicore.component.CustomLinearLayoutManager;
import com.tencent.qcloud.tuicore.component.PopupList;
import com.tencent.qcloud.tuicore.component.action.PopActionClickListener;
import com.tencent.qcloud.tuicore.component.action.PopMenuAction;
import com.tencent.qcloud.tuikit.tuichat.R;
import com.tencent.qcloud.tuikit.tuichat.TUIChatConstants;
import com.tencent.qcloud.tuikit.tuichat.bean.MessageInfo;
import com.tencent.qcloud.tuikit.tuichat.bean.MessageProperties;
import com.tencent.qcloud.tuikit.tuichat.ui.interfaces.IMessageLayout;
import com.tencent.qcloud.tuikit.tuichat.ui.interfaces.IOnCustomMessageDrawListener;
import com.tencent.qcloud.tuikit.tuichat.ui.interfaces.OnItemLongClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageRecyclerView extends RecyclerView implements IMessageLayout {

    public static final int DATA_CHANGE_TYPE_REFRESH = 0;
    public static final int DATA_CHANGE_TYPE_LOAD = 1;
    public static final int DATA_CHANGE_TYPE_ADD_FRONT = 2;
    public static final int DATA_CHANGE_TYPE_ADD_BACK = 3;
    public static final int DATA_CHANGE_TYPE_UPDATE = 4;
    public static final int DATA_CHANGE_TYPE_DELETE = 5;
    public static final int DATA_CHANGE_TYPE_CLEAR = 6;
    public static final int DATA_CHANGE_SCROLL_TO_POSITION = 7;
    public static final int DATA_CHANGE_NEW_MESSAGE = 8;
    public static Map<String, String> is_read_Map = null;
    public static boolean isVip = false;//自定义是否是VIP
    public static boolean sex = false;
    public static int read_sum = 1;//能看消息总数
    public static int send_num = 1;//能发送消息总数
    //追踪消息的msgId
    public static String addLikeMsgId = null;
    public static boolean certification = false;
    public static boolean flagTipMoney = false;//收入提示开关
    private final MessageProperties properties = MessageProperties.getInstance();
    protected OnItemLongClickListener mOnItemLongClickListener;
    protected MessageRecyclerView.OnLoadMoreHandler mHandler;
    protected MessageRecyclerView.OnEmptySpaceClickListener mEmptySpaceClickListener;
    protected MessageAdapter mAdapter;
    protected List<PopMenuAction> mPopActions = new ArrayList<>();
    protected List<PopMenuAction> mMorePopActions = new ArrayList<>();
    protected MessageRecyclerView.OnPopActionClickListener mOnPopActionClickListener;

    public MessageRecyclerView(Context context) {
        super(context);
        init();
    }

    public MessageRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MessageRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public static String getAddLikeMsgId() {
        return addLikeMsgId;
    }

    public static void setAddLikeMsgId(String addLikeMsgId) {
        MessageRecyclerView.addLikeMsgId = addLikeMsgId;
    }

    public static boolean isCertification() {
        return certification;
    }

    public static void setCertification(boolean certification) {
        MessageRecyclerView.certification = certification;
    }

    public static boolean isFlagTipMoney() {
        return flagTipMoney;
    }

    public static void setFlagTipMoney(boolean flagTipMoney) {
        MessageRecyclerView.flagTipMoney = flagTipMoney;
    }

    public int getRead_sum() {
        return read_sum;
    }

    public void setRead_sum(int read_sum) {
        MessageRecyclerView.read_sum = read_sum;
    }

    public int getSend_num() {
        return send_num;
    }

    public  void setSend_num(int send_num) {
        MessageRecyclerView.send_num = send_num;
    }

    public void setSex(boolean sexs){
        sex = sexs;
    }

    public void setIsReadMap(){
        is_read_Map = null;
    }

    public void setIsVip(boolean vip){
        isVip = vip;
    }

    private void init() {
        setLayoutFrozen(false);
//        setItemViewCacheSize(0);
        setHasFixedSize(true);
        setFocusableInTouchMode(false);
        //开启缓存模式
        //彭石林新增---数据缓存机制
        setItemViewCacheSize(500);
        //setItemViewCacheSize(20);
        setDrawingCacheEnabled(true);
        setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        LinearLayoutManager linearLayoutManager = new CustomLinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        setLayoutManager(linearLayoutManager);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_UP) {
            View child = findChildViewUnder(e.getX(), e.getY());
            if (child == null) {
                if (mEmptySpaceClickListener != null)
                    mEmptySpaceClickListener.onClick();
            } else if (child instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) child;
                final int count = group.getChildCount();
                float x = e.getRawX();
                float y = e.getRawY();
                View touchChild = null;
                for (int i = count - 1; i >= 0; i--) {
                    final View innerChild = group.getChildAt(i);
                    int[] position = new int[2];
                    innerChild.getLocationOnScreen(position);
                    if (x >= position[0]
                            && x <= position[0] + innerChild.getMeasuredWidth()
                            && y >= position[1]
                            && y <= position[1] + innerChild.getMeasuredHeight()) {
                        touchChild = innerChild;
                        break;
                    }
                }
                if (touchChild == null) {
                    if (mEmptySpaceClickListener != null) {
                        mEmptySpaceClickListener.onClick();
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(e);
    }

    public void showItemPopMenu(final int index, final MessageInfo messageInfo, View view) {
        initPopActions(messageInfo);
        if (mPopActions.size() == 0) {
            return;
        }

        final PopupList popupList = new PopupList(getContext());
        List<String> mItemList = new ArrayList<>();
        for (PopMenuAction action : mPopActions) {
            mItemList.add(action.getActionName());
        }
        popupList.show(view, mItemList, new PopupList.PopupListListener() {
            @Override
            public boolean showPopupList(View adapterView, View contextView, int contextPosition) {
                return true;
            }

            @Override
            public void onPopupListClick(View contextView, int contextPosition, int position) {
                PopMenuAction action = mPopActions.get(position);
                if (action.getActionClickListener() != null) {
                    action.getActionClickListener().onActionClick(index, messageInfo);
                }
            }
        });
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (popupList != null) {
                    popupList.hidePopupListWindow();
                }
            }
        }, 10000); // 10s后无操作自动消失
    }

    public void onMsgAddBack() {
        if (mAdapter != null) {
            // 如果当前显示最后一条消息，则消息刷新跳转到底部，否则不跳转
            if (isLastItemVisibleCompleted()) {
                scrollToEnd();
            }
        }
    }

    public boolean isLastItemVisibleCompleted() {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getLayoutManager();
        if (linearLayoutManager == null) {
            return false;
        }
        int lastPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
        int childCount = linearLayoutManager.getChildCount();
        int firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
        return lastPosition >= firstPosition + childCount - 1;
    }

    private void initPopActions(final MessageInfo msg) {
        if (msg == null) {
            return;
        }
        List<PopMenuAction> actions = new ArrayList<>();
        PopMenuAction action = new PopMenuAction();
        if (msg.getMsgType() == MessageInfo.MSG_TYPE_TEXT) {
            action.setActionName(getContext().getString(R.string.copy_action));
            action.setActionClickListener(new PopActionClickListener() {
                @Override
                public void onActionClick(int position, Object data) {
                    mOnPopActionClickListener.onCopyClick(position, (MessageInfo) data);
                }
            });
            actions.add(action);
        }
        action = new PopMenuAction();
        action.setActionName(getContext().getString(R.string.delete_action));
        action.setActionClickListener(new PopActionClickListener() {
            @Override
            public void onActionClick(int position, Object data) {
                mOnPopActionClickListener.onDeleteMessageClick(position, (MessageInfo) data);
            }
        });
        actions.add(action);
//        if (msg.isSelf()) {
//            action = new PopMenuAction();
//            if (msg.getStatus() != MessageInfo.MSG_STATUS_SEND_FAIL) {
//                action.setActionName(getContext().getString(R.string.revoke_action));
//                action.setActionClickListener(new PopActionClickListener() {
//                    @Override
//                    public void onActionClick(int position, Object data) {
//                        mOnPopActionClickListener.onRevokeMessageClick(position, (MessageInfo) data);
//                    }
//                });
//                actions.add(action);
//            } else {
//                action = new PopMenuAction();
//                action.setActionName(getContext().getString(R.string.resend_action));
//                action.setActionClickListener(new PopActionClickListener() {
//                    @Override
//                    public void onActionClick(int position, Object data) {
//                        mOnPopActionClickListener.onSendMessageClick(msg, true);
//                    }
//                });
//                actions.add(action);
//            }
//        }
//
//        //多选
//        action = new PopMenuAction();
//        action.setActionName(getContext().getString(R.string.titlebar_mutiselect));
//        action.setActionClickListener(new PopActionClickListener() {
//            @Override
//            public void onActionClick(int position, Object data) {
//                mOnPopActionClickListener.onMultiSelectMessageClick(position, (MessageInfo) data);
//            }
//        });
//        actions.add(action);
//
//        //转发
//        if (msg.getStatus() != MessageInfo.MSG_STATUS_SEND_FAIL) {
//            action = new PopMenuAction();
//            action.setActionName(getContext().getString(R.string.forward_button));
//            action.setActionClickListener(new PopActionClickListener() {
//                @Override
//                public void onActionClick(int position, Object data) {
//                    mOnPopActionClickListener.onForwardMessageClick(position, (MessageInfo) data);
//                }
//            });
//            actions.add(action);
//        }


        mPopActions.clear();
        mPopActions.addAll(actions);
        mPopActions.addAll(mMorePopActions);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            if (mHandler != null) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
                int firstPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                int lastPosition = layoutManager.findLastCompletelyVisibleItemPosition();
                if (firstPosition == 0 && ((lastPosition - firstPosition + 1) < getAdapter().getItemCount())) {
                    if (getAdapter() instanceof MessageAdapter) {
                        getAdapter().showLoading();
                    }
                    mHandler.loadMore(TUIChatConstants.GET_MESSAGE_FORWARD);
                } else if (isListEnd(lastPosition)){
                    if (getAdapter() instanceof MessageAdapter) {
                        getAdapter().showLoading();
                    }
                    mHandler.loadMore(TUIChatConstants.GET_MESSAGE_BACKWARD);
                }
            }
        }
    }

    private boolean isListEnd(int lastPosition) {
       return mHandler.isListEnd(lastPosition);
    }

    public void scrollToEnd() {
        if (getAdapter() != null) {
            scrollToPosition(getAdapter().getItemCount() - 1);
        }
    }

    public void scrollToPosition(int position) {
        if (getAdapter() != null && position < getAdapter().getItemCount()) {
            super.scrollToPosition(position);
        }
    }

    public void setHighShowPosition(int position) {
        if (mAdapter != null) {
            mAdapter.setHighShowPosition(position);
        }
    }

    public OnLoadMoreHandler getLoadMoreHandler() {
        return mHandler;
    }

    public void setLoadMoreMessageHandler(OnLoadMoreHandler mHandler) {
        this.mHandler = mHandler;
    }

    public OnEmptySpaceClickListener getEmptySpaceClickListener() {
        return mEmptySpaceClickListener;
    }

    public void setEmptySpaceClickListener(OnEmptySpaceClickListener mEmptySpaceClickListener) {
        this.mEmptySpaceClickListener = mEmptySpaceClickListener;
    }

    public void setPopActionClickListener(OnPopActionClickListener listener) {
        mOnPopActionClickListener = listener;
    }

    public void postSetAdapter(MessageAdapter adapter) {
        mAdapter.setOnItemClickListener(new OnItemLongClickListener() {
            @Override
            public void onMessageLongClick(View view, int position, MessageInfo messageInfo) {
                if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.onMessageLongClick(view, position, messageInfo);
                }
            }

            @Override
            public void onUserIconClick(View view, int position, MessageInfo info) {
                if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.onUserIconClick(view, position, info);
                }
            }

            @Override
            public void onToastVipText(MessageInfo messageInfo) {
                if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.onToastVipText(messageInfo);
                }
            }

            @Override
            public void onTextReadUnlock(TextView textView, View view, MessageInfo messageInfo) {
                if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.onTextReadUnlock(textView,view, messageInfo);
                }
            }

            @Override
            public void onTextTOWebView(MessageInfo messageInfo) {
                if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.onTextTOWebView(messageInfo);
                }
            }

            @Override
            public void toUserHome() {
                if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.toUserHome();
                }
            }

            @Override
            public void openUserImage(PhotoAlbumItemEntity itemEntity) {
                if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.openUserImage(itemEntity);
                }
            }

            @Override
            public void onClickEvaluate(int position, MessageInfo messageInfo, EvaluateItemEntity evaluateItemEntity, boolean more) {
                if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.onClickEvaluate(position,messageInfo,evaluateItemEntity,more);
                }
            }

            @Override
            public void onClickCustomText(int position, MessageInfo messageInfo, CustomIMTextEntity customIMTextEntity) {
                if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.onClickCustomText(position, messageInfo, customIMTextEntity);
                }
            }

            @Override
            public void onClickDialogRechargeShow() {
                if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.onClickDialogRechargeShow();
                }
            }


            @Override
            public void clickToUserMain() {
                if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.clickToUserMain();
                }
            }

            @Override
            public void onClickCustomText() {

            }
        });
    }


    @Override
    public int getAvatarRadius() {
        return properties.getAvatarRadius();
    }

    @Override
    public void setAvatarRadius(int radius) {
        properties.setAvatarRadius(radius);
    }

    @Override
    public int[] getAvatarSize() {
        return properties.getAvatarSize();
    }

    @Override
    public void setAvatarSize(int[] size) {
        properties.setAvatarSize(size);
    }

    @Override
    public int getAvatar() {
        return properties.getAvatar();
    }

    @Override
    public void setAvatar(int resId) {
        properties.setAvatar(resId);
    }

    @Override
    public Drawable getRightBubble() {
        return properties.getRightBubble();
    }

    @Override
    public void setRightBubble(Drawable bubble) {
        properties.setRightBubble(bubble);
    }

    @Override
    public Drawable getLeftBubble() {
        return properties.getLeftBubble();
    }

    @Override
    public void setLeftBubble(Drawable bubble) {
        properties.setLeftBubble(bubble);
    }

    @Override
    public int getNameFontSize() {
        return properties.getNameFontSize();
    }

    @Override
    public void setNameFontSize(int size) {
        properties.setNameFontSize(size);
    }

    @Override
    public int getNameFontColor() {
        return properties.getNameFontColor();
    }

    @Override
    public void setNameFontColor(int color) {
        properties.setNameFontColor(color);
    }

    @Override
    public int getLeftNameVisibility() {
        return properties.getLeftNameVisibility();
    }

    @Override
    public void setLeftNameVisibility(int visibility) {
        properties.setLeftNameVisibility(visibility);
    }

    @Override
    public int getRightNameVisibility() {
        return properties.getRightNameVisibility();
    }

    @Override
    public void setRightNameVisibility(int visibility) {
        properties.setRightNameVisibility(visibility);
    }

    @Override
    public int getChatContextFontSize() {
        return properties.getChatContextFontSize();
    }

    @Override
    public void setChatContextFontSize(int size) {
        properties.setChatContextFontSize(size);
    }

    @Override
    public int getRightChatContentFontColor() {
        return properties.getRightChatContentFontColor();
    }

    @Override
    public void setRightChatContentFontColor(int color) {
        properties.setRightChatContentFontColor(color);
    }

    @Override
    public int getLeftChatContentFontColor() {
        return properties.getLeftChatContentFontColor();
    }

    @Override
    public void setLeftChatContentFontColor(int color) {
        properties.setLeftChatContentFontColor(color);
    }

    @Override
    public Drawable getTipsMessageBubble() {
        return properties.getTipsMessageBubble();
    }

    @Override
    public void setTipsMessageBubble(Drawable bubble) {
        properties.setTipsMessageBubble(bubble);
    }

    @Override
    public int getTipsMessageFontSize() {
        return properties.getTipsMessageFontSize();
    }

    @Override
    public void setTipsMessageFontSize(int size) {
        properties.setTipsMessageFontSize(size);
    }

    @Override
    public int getTipsMessageFontColor() {
        return properties.getTipsMessageFontColor();
    }

    @Override
    public void setTipsMessageFontColor(int color) {
        properties.setTipsMessageFontColor(color);
    }

    @Override
    public Drawable getChatTimeBubble() {
        return properties.getChatTimeBubble();
    }

    @Override
    public void setChatTimeBubble(Drawable bubble) {
        properties.setChatTimeBubble(bubble);
    }

    @Override
    public int getChatTimeFontSize() {
        return properties.getChatTimeFontSize();
    }

    @Override
    public void setChatTimeFontSize(int size) {
        properties.setChatTimeFontSize(size);
    }

    @Override
    public int getChatTimeFontColor() {
        return properties.getChatTimeFontColor();
    }

    @Override
    public void setChatTimeFontColor(int color) {
        properties.setChatTimeFontColor(color);
    }

    @Override
    public OnItemLongClickListener getOnItemClickListener() {
        return mAdapter.getOnItemClickListener();
    }

    @Override
    public void setOnItemClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
        mAdapter.setOnItemClickListener(listener);
    }

    //彭石林新增
    public MessageAdapter getAdapter(){
        return mAdapter;
    }

    @Override
    public void setAdapter(MessageAdapter adapter) {
        super.setAdapter(adapter);
        mAdapter = adapter;
        postSetAdapter(adapter);
    }

    @Override
    public List<PopMenuAction> getPopActions() {
        return mPopActions;
    }

    @Override
    public void addPopAction(PopMenuAction action) {
        mMorePopActions.add(action);
    }

    /**
    * @Desc TODO()
    * @author 彭石林
    * @parame [iOnCustomMessageDrawListener]
    * @return void
    * @Date 2021/11/27
    */
    @Override
    public void setOnCustomMessageDrawListener(IOnCustomMessageDrawListener iOnCustomMessageDrawListener) {
        mAdapter.setOnCustomMessageDrawListener(iOnCustomMessageDrawListener);
    }


    public interface OnLoadMoreHandler {
        void loadMore(int type);
        boolean isListEnd(int postion);
    }

    public interface OnEmptySpaceClickListener {
        void onClick();
    }

    public interface OnPopActionClickListener {

        void onCopyClick(int position, MessageInfo msg);

        void onSendMessageClick(MessageInfo msg, boolean retry);

        void onDeleteMessageClick(int position, MessageInfo msg);

        void onRevokeMessageClick(int position, MessageInfo msg);

        void onMultiSelectMessageClick(int position, MessageInfo msg);

        void onForwardMessageClick(int position, MessageInfo msg);
    }
}
