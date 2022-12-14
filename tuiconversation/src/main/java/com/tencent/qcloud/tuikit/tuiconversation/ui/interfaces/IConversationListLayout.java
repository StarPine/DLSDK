package com.tencent.qcloud.tuikit.tuiconversation.ui.interfaces;

import com.tencent.qcloud.tuikit.tuiconversation.ui.view.ConversationListAdapter;
import com.tencent.qcloud.tuikit.tuiconversation.ui.view.ConversationListLayout;

/**
 * ConversationListLayout 的适配器，用户可自定义实现
 */

public interface IConversationListLayout {

    /**
     * 设置会话界面背景，非ListView区域
     *
     * @param resId
     */
    void setBackground(int resId);

    /**
     * 设置会话Item点击监听
     *
     * @param listener
     */
    void setOnItemClickListener(ConversationListLayout.OnItemClickListener listener);

    /**
     * 设置会话Item长按监听
     *
     * @param listener
     */
    void setOnItemLongClickListener(ConversationListLayout.OnItemLongClickListener listener);

    /**
     * 不显示小红点未读消息条数开关
     *
     * @param flag 默认false，表示显示
     */
    void disableItemUnreadDot(boolean flag);

    /**
     * 设置会话Item头像圆角
     *
     * @param radius
     */
    void setItemAvatarRadius(int radius);

    /**
     * 设置会话Item顶部字体大小
     *
     * @param size
     */
    void setItemTopTextSize(int size);

    /**
     * 设置会话Item底部字体大小
     *
     * @param size
     */
    void setItemBottomTextSize(int size);

    /**
     * 设置会话Item日期字体大小
     *
     * @param size
     */
    void setItemDateTextSize(int size);

    /**
     * 获取会话列表ListView
     *
     * @return
     */
    ConversationListLayout getListLayout();

    /**
     * 获取会话列表Adapter
     *
     * @return
     */
    ConversationListAdapter getAdapter();

    /**
     * 设置会话Adapter
     *
     * @param adapter
     */
    void setAdapter(IConversationListAdapter adapter);

    /**
     * 会话列表头像点击
     * @param listener
     */
    void setOnItemAvatarClickListener(ConversationListLayout.OnItemAvatarClickListener listener);

    /**
     * 删除所有封号会话列表
     * @param listener
     */
    void setBanConversationDelListener(ConversationListLayout.BanConversationDelListener listener);

}
