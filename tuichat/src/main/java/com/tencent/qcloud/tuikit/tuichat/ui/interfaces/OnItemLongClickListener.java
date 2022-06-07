package com.tencent.qcloud.tuikit.tuichat.ui.interfaces;

import android.view.View;
import android.widget.TextView;

import com.tencent.coustom.CustomIMTextEntity;
import com.tencent.coustom.EvaluateItemEntity;
import com.tencent.coustom.PhotoAlbumItemEntity;
import com.tencent.qcloud.tuikit.tuichat.bean.MessageInfo;

public interface OnItemLongClickListener {
    void onMessageLongClick(View view, int position, MessageInfo messageInfo);

    void onUserIconClick(View view, int position, MessageInfo messageInfo);

    //彭石林新增
    void onToastVipText(MessageInfo messageInfo);

    void onTextReadUnlock(TextView textView, View view, MessageInfo messageInfo);

    void onTextTOWebView(MessageInfo messageInfo);

    void toUserHome();

    void openUserImage(PhotoAlbumItemEntity itemEntity);

    void onClickEvaluate(int position, MessageInfo messageInfo, EvaluateItemEntity evaluateItemEntity, boolean more);

    void onClickCustomText(int position, MessageInfo messageInfo, CustomIMTextEntity customIMTextEntity);

    void onClickDialogRechargeShow();

    void clickToUserMain();

    void onClickCustomText();
}
