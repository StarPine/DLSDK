package com.tencent.qcloud.tuikit.tuichat.ui.view.message.viewholder.dltmpapply;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.custom.IMGsonUtils;
import com.tencent.custom.tmp.CustomDlTempMessage;
import com.tencent.qcloud.tuicore.custom.CustomConstants;
import com.tencent.qcloud.tuicore.custom.entity.CallingRejectEntity;
import com.tencent.qcloud.tuikit.tuichat.R;
import com.tencent.qcloud.tuikit.tuichat.bean.message.TUIMessageBean;
import com.tencent.qcloud.tuikit.tuichat.ui.view.message.viewholder.CustomDlTempMessageHolder;

/**
 * Author: 彭石林
 * Time: 2022/9/20 11:20
 * Description: 通话模块
 */
public class CallingMessageModuleView extends BaseMessageModuleView{

    public CallingMessageModuleView(CustomDlTempMessageHolder customDlTempMessageHolder) {
        super(customDlTempMessageHolder);
    }

    public void layoutVariableViews(TUIMessageBean msg, FrameLayout rootView, CustomDlTempMessage.MsgBodyInfo msgModuleInfo){
        if (CustomConstants.CallingMessage.TYPE_CALLING_FAILED.equals(msgModuleInfo.getCustomMsgType())) {
            CallingRejectEntity callingRejectEntity = IMGsonUtils.fromJson(IMGsonUtils.toJson(msgModuleInfo.getCustomMsgBody()), CallingRejectEntity.class);
            loadCallingView(msg, callingRejectEntity, rootView);
        } else {
            customDlTempMessageHolder.defaultLayout(rootView, msg.isSelf());
        }
    }

    private void loadCallingView(TUIMessageBean msg, CallingRejectEntity callingRejectEntity,FrameLayout rootView) {
        View callingView = View.inflate(getContext(), R.layout.message_adapter_content_json_text, null);
        TextView msgBody = callingView.findViewById(R.id.msg_body_tv);
        ImageView leftView = callingView.findViewById(R.id.left_icon);
        ImageView rightView = callingView.findViewById(R.id.right_icon);
        customDlTempMessageHolder.setBackColor(msg, msgBody);
        setCallingMsgIconStyle(msg, leftView, rightView, callingRejectEntity.getCallingType());
        msgBody.setText(callingRejectEntity.getContent());
        rootView.addView(callingView);
    }

    private void setCallingMsgIconStyle(TUIMessageBean msg, ImageView leftView, ImageView rightView, int callingType) {
        leftView.setVisibility(View.GONE);
        rightView.setVisibility(View.GONE);
        if (msg.isSelf()) {
            rightView.setBackgroundResource(callingType == 1 ?
                    R.drawable.custom_audio_right_img_2 : R.drawable.custom_video_right_img_1);
            rightView.setVisibility(View.VISIBLE);
        } else {
            leftView.setBackgroundResource(callingType == 1 ?
                    R.drawable.custom_audio_left_img_2 : R.drawable.custom_video_left_img_1);
            leftView.setVisibility(View.VISIBLE);
        }
    }
}
