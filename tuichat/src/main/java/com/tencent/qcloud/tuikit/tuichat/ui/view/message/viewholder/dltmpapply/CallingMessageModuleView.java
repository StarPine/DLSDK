package com.tencent.qcloud.tuikit.tuichat.ui.view.message.viewholder.dltmpapply;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.tencent.custom.IMGsonUtils;
import com.tencent.custom.tmp.CustomDlTempMessage;
import com.tencent.qcloud.tuicore.custom.CustomConstants;
import com.tencent.qcloud.tuicore.custom.entity.CallingRejectEntity;
import com.tencent.qcloud.tuicore.util.DateTimeUtil;
import com.tencent.qcloud.tuikit.tuichat.R;
import com.tencent.qcloud.tuikit.tuichat.TUIChatConstants;
import com.tencent.qcloud.tuikit.tuichat.bean.CallModel;
import com.tencent.qcloud.tuikit.tuichat.bean.message.TUIMessageBean;
import com.tencent.qcloud.tuikit.tuichat.ui.view.message.viewholder.CustomDlTempMessageHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: 彭石林
 * Time: 2022/9/20 11:20
 * Description: 通话模块
 */
public class CallingMessageModuleView extends BaseMessageModuleView{

    public CallingMessageModuleView(CustomDlTempMessageHolder customDlTempMessageHolder) {
        super(customDlTempMessageHolder);
    }

    public void layoutVariableViews(TUIMessageBean msg, FrameLayout rootView,int position, CustomDlTempMessage.MsgBodyInfo msgModuleInfo){
        if (CustomConstants.CallingMessage.TYPE_CALLING_FAILED.equals(msgModuleInfo.getCustomMsgType())) {
            loadCallingView(msg, msgModuleInfo, rootView);
        }else if (CustomConstants.CallingMessage.TYPE_CALLING_HINT.equals(msgModuleInfo.getCustomMsgType())) {
            loadCallingHintView(msg,msgModuleInfo,rootView);
        }else{
            customDlTempMessageHolder.defaultLayout(rootView, msg.isSelf(),position,msg);
        }
    }

    private void loadCallingView(TUIMessageBean msg, CustomDlTempMessage.MsgBodyInfo msgModuleInfo, FrameLayout rootView) {
        try {
            CallingRejectEntity callingRejectEntity = new Gson().fromJson(new Gson().toJson(msgModuleInfo.getCustomMsgBody()), CallingRejectEntity.class);
            View callingView = View.inflate(getContext(), R.layout.message_adapter_content_json_text, null);
            TextView msgBody = callingView.findViewById(R.id.msg_body_tv);
            ImageView leftView = callingView.findViewById(R.id.left_icon);
            ImageView rightView = callingView.findViewById(R.id.right_icon);
            customDlTempMessageHolder.setBackColor(msg, msgBody);
            setCallingMsgIconStyle(msg, leftView, rightView, callingRejectEntity.getCallingType());
            msgBody.setText(callingRejectEntity.getContent());
            rootView.addView(callingView);
        }catch (Exception e){
            customDlTempMessageHolder.setContentLayoutVisibility(false);
        }

    }

    //拨打状态提示
    private void loadCallingHintView(TUIMessageBean msg, CustomDlTempMessage.MsgBodyInfo msgModuleInfo, FrameLayout rootView) {
        try {
            //action=dl_rtc_message_invite, inviteType=dl_rtc_audio
            Log.e("进入拨打状态提示",String.valueOf(msgModuleInfo.getCustomMsgBody()));
            Log.e("拨打状态类型：","is Map="+(msgModuleInfo.getCustomMsgBody() instanceof Map)+",is String="+(msgModuleInfo.getCustomMsgBody() instanceof String));
            Map<String,String> mapData = new HashMap<>();
            if(msgModuleInfo.getCustomMsgBody() instanceof Map){
                mapData = (Map<String, String>) msgModuleInfo.getCustomMsgBody();
            }else if(msgModuleInfo.getCustomMsgBody() instanceof String){
                mapData = new Gson().fromJson(String.valueOf(msgModuleInfo.getCustomMsgBody()), Map.class);
            }
            if(mapData!=null){
                String action = mapData.get("action");
                String inviteType = mapData.get("inviteType");
                View callingView = View.inflate(getContext(), R.layout.message_adapter_content_json_text, null);
                TextView msgBody = callingView.findViewById(R.id.msg_body_tv);
                ImageView leftView = callingView.findViewById(R.id.left_icon);
                ImageView rightView = callingView.findViewById(R.id.right_icon);
                customDlTempMessageHolder.setBackColor(msg, msgBody);
                setCallingMsgIconStyle(msg, leftView, rightView, inviteType.equals("dl_rtc_audio") ? 1 : 0);
                msgBody.setText(getCallingTypeString(rightView.getContext(),action));
                rootView.addView(callingView);
            }else{
                customDlTempMessageHolder.setContentLayoutVisibility(false);
            }

        }catch (Exception e){
            customDlTempMessageHolder.setContentLayoutVisibility(false);
        }

    }

    private String getCallingTypeString(Context context,String action){
        String content;
        switch (action) {
            case TUIChatConstants.invite:
                content =  context.getString(R.string.start_call);
                break;
            case TUIChatConstants.cancel:
                content = context.getString(R.string.cancle_call);
                break;
//            case CallModel.VIDEO_CALL_ACTION_LINE_BUSY:
//                content =  context.getString(R.string.other_line_busy);
//                break;
            case TUIChatConstants.reject:
                content =  context.getString(R.string.reject_calls);
                break;
            case TUIChatConstants.timeout:
                    content =  context.getString(R.string.no_response_call);
                break;
            case TUIChatConstants.accept:
                content =  context.getString(R.string.accept_call);
                break;
            default:
                content = context.getString(R.string.invalid_command);
                break;
        }
        return content;
    }

    private void setCallingMsgIconStyle(TUIMessageBean msg, ImageView leftView, ImageView rightView, int callingType) {
        leftView.setVisibility(View.GONE);
        rightView.setVisibility(View.GONE);
        if (msg.isSelf()) {
            rightView.setBackgroundResource(callingType == 1 ? R.drawable.custom_audio_right_img_2 : R.drawable.custom_video_right_img_1);
            rightView.setVisibility(View.VISIBLE);
        } else {
            leftView.setBackgroundResource(callingType == 1 ? R.drawable.custom_audio_left_img_2 : R.drawable.custom_video_left_img_1);
            leftView.setVisibility(View.VISIBLE);
        }
    }
}
