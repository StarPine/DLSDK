package com.tencent.qcloud.tuikit.tuichat.ui.interfaces;

import android.view.View;
import android.widget.TextView;

import com.tencent.coustom.CustomIMTextEntity;
import com.tencent.coustom.EvaluateItemEntity;
import com.tencent.coustom.PhotoAlbumItemEntity;
import com.tencent.qcloud.tuikit.tuichat.bean.message.TUIMessageBean;

public interface OnItemClickListener {
    void onMessageLongClick(View view, int position, TUIMessageBean messageInfo);

    default void onMessageClick(View view, int position, TUIMessageBean messageInfo) {};

    void onUserIconClick(View view, int position, TUIMessageBean messageInfo);

    void onUserIconLongClick(View view, int position, TUIMessageBean messageInfo);

    void onReEditRevokeMessage(View view, int position, TUIMessageBean messageInfo);

    void onRecallClick(View view, int position, TUIMessageBean messageInfo);

    default void onReplyMessageClick(View view, int position, String originMsgId) {}

    default void onSendFailBtnClick(View view, int position, TUIMessageBean messageInfo) {};

    default void onTextSelected(View view, int position, TUIMessageBean messageInfo) {};

    //彭石林新增
    void onToastVipText(TUIMessageBean messageInfo);

    void onTextReadUnlock(TextView textView, View view, TUIMessageBean messageInfo);

    void onTextTOWebView(TUIMessageBean messageInfo);

    void toUserHome();

    void openUserImage(PhotoAlbumItemEntity itemEntity);

    void onClickEvaluate(int position, TUIMessageBean messageInfo, EvaluateItemEntity evaluateItemEntity, boolean more);

    void onClickCustomText(int position, TUIMessageBean messageInfo, CustomIMTextEntity customIMTextEntity);

    void onClickDialogRechargeShow();

    void clickToUserMain();

    void onClickCustomText();

    //DL Add lsf -- 图片点击
    default void onImageClick(TUIMessageBean messageInfo){}
}
