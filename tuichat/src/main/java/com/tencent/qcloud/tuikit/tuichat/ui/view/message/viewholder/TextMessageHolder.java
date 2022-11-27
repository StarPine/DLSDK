package com.tencent.qcloud.tuikit.tuichat.ui.view.message.viewholder;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.qcloud.tuicore.TUIThemeManager;
import com.tencent.qcloud.tuikit.tuichat.R;
import com.tencent.qcloud.tuikit.tuichat.TUIChatService;
import com.tencent.qcloud.tuikit.tuichat.bean.message.TUIMessageBean;
import com.tencent.qcloud.tuikit.tuichat.bean.message.TextMessageBean;
import com.tencent.qcloud.tuikit.tuichat.component.face.FaceManager;
import com.tencent.qcloud.tuikit.tuichat.ui.view.message.SelectTextHelper;
import com.tencent.qcloud.tuikit.tuichat.util.TUIChatLog;

public class TextMessageHolder extends MessageContentHolder {

    protected TextView msgBodyText;

    public TextMessageHolder(View itemView) {
        super(itemView);
        msgBodyText = itemView.findViewById(R.id.msg_body_tv);
    }

    @Override
    public int getVariableLayout() {
        return R.layout.message_adapter_content_text;
    }

    @Override
    public void layoutVariableViews(TUIMessageBean msg, int position) {
        if (!(msg instanceof TextMessageBean)) {
            return;
        }
        TextMessageBean textMessageBean = (TextMessageBean) msg;
        Log.e("当前消息为进入文本消息",textMessageBean.toString());
        Log.e("当前自定义消息文本：",String.valueOf(new String(textMessageBean.getCustomElemData())));
        Log.e("当前自定义消息文1本：", String.valueOf(V2TIMManager.getSignalingManager().getSignalingInfo(msg.getV2TIMMessage()) == null));
        if (isForwardMode || isReplyDetailMode || !textMessageBean.isSelf()) {
            int otherTextColorResId = TUIThemeManager.getAttrResId(msgBodyText.getContext(), R.attr.chat_other_msg_text_color);
            int otherTextColor = msgBodyText.getResources().getColor(otherTextColorResId);
            msgBodyText.setTextColor(otherTextColor);
        } else {
            int selfTextColorResId = TUIThemeManager.getAttrResId(msgBodyText.getContext(), R.attr.chat_self_msg_text_color);
            int selfTextColor = msgBodyText.getResources().getColor(selfTextColorResId);
            msgBodyText.setTextColor(selfTextColor);
        }

        msgBodyText.setVisibility(View.VISIBLE);

        if (properties.getChatContextFontSize() != 0) {
            msgBodyText.setTextSize(properties.getChatContextFontSize());
        }
        if (textMessageBean.isSelf()) {
            if (properties.getRightChatContentFontColor() != 0) {
                msgBodyText.setTextColor(properties.getRightChatContentFontColor());
            }
        } else {
            if (properties.getLeftChatContentFontColor() != 0) {
                msgBodyText.setTextColor(properties.getLeftChatContentFontColor());
            }
        }

        msgContentFrame.setOnLongClickListener(view -> {
//                mSelectableTextHelper.selectAll();
            if (onItemClickListener != null) {
                onItemClickListener.onMessageLongClick(view, position, msg);
            }
            return true;
        });

        //int actionType = Double.valueOf(String.valueOf(signallingData.get("actionType"))).intValue();
        //int callType = Double.valueOf(String.valueOf(callData.get("call_type"))).intValue();

//        if (msg.isSelf()) {
//            itemView.findViewById(R.id.left_call_img).setVisibility(View.GONE);
//            if (actionType == 1) {//发起通话
//                itemView.findViewById(R.id.right_call_img).setBackgroundResource(callType == 1 ? R.drawable.custom_audio_right_img_1 : R.drawable.custom_video_right_img_1);
//                itemView.findViewById(R.id.right_call_img).setVisibility(View.VISIBLE);
//            } else if (actionType == 2) {//取消通话
//                itemView.findViewById(R.id.right_call_img).setBackgroundResource(callType == 1 ? R.drawable.custom_audio_right_img_2 : R.drawable.custom_video_right_img_1);
//                itemView.findViewById(R.id.right_call_img).setVisibility(View.VISIBLE);
//            } else if (actionType == 4) {//接听电话
//                itemView.findViewById(R.id.right_call_img).setBackgroundResource(callType == 1 ? R.drawable.custom_audio_right_img_2 : R.drawable.custom_video_right_img_1);
//                itemView.findViewById(R.id.right_call_img).setVisibility(View.VISIBLE);
//            } else if (actionType == 7) {//接听电话
//                itemView.findViewById(R.id.right_call_img).setBackgroundResource(callType == 1 ? R.drawable.custom_audio_right_img_2 : R.drawable.custom_video_right_img_1);
//                itemView.findViewById(R.id.right_call_img).setVisibility(View.VISIBLE);
//            }
//        } else {
//            itemView.findViewById(R.id.right_call_img).setVisibility(View.GONE);
//            if (actionType == 1) {//发起通话
//                itemView.findViewById(R.id.left_call_img).setBackgroundResource(callType == 1 ? R.drawable.custom_audio_left_img_1 : R.drawable.custom_video_left_img_1);
//                itemView.findViewById(R.id.left_call_img).setVisibility(View.VISIBLE);
//            } else if (actionType == 2) {//取消通话
//                itemView.findViewById(R.id.left_call_img).setBackgroundResource(callType == 1 ? R.drawable.custom_audio_left_img_2 : R.drawable.custom_video_left_img_1);
//                itemView.findViewById(R.id.left_call_img).setVisibility(View.VISIBLE);
//            } else if (actionType == 4) {//接听电话
//                itemView.findViewById(R.id.left_call_img).setBackgroundResource(callType == 1 ? R.drawable.custom_audio_left_img_2 : R.drawable.custom_video_left_img_1);
//                itemView.findViewById(R.id.left_call_img).setVisibility(View.VISIBLE);
//            } else if (actionType == 7) {//接听电话
//                itemView.findViewById(R.id.left_call_img).setBackgroundResource(callType == 1 ? R.drawable.custom_audio_left_img_2 : R.drawable.custom_video_left_img_1);
//                itemView.findViewById(R.id.left_call_img).setVisibility(View.VISIBLE);
//            }
//        }

        boolean isEmoji = false;
        String extra = msg.getExtra();
        if (extra != null && extra.contains("href") && extra.contains("</a>")) {
            CharSequence charSequence = Html.fromHtml(extra);
            msgBodyText.setText(charSequence);
            msgBodyText.setOnClickListener(view -> {
                if (onItemClickListener !=null)
                    onItemClickListener.onTextTOWebView(msg);
            });
        } else {
            FaceManager.handlerEmojiText(msgBodyText, extra, false);
        }
//        if (textMessageBean.getText() != null) {
//            isEmoji = FaceManager.handlerEmojiText(msgBodyText, textMessageBean.getText(), false);
//        } else if (!TextUtils.isEmpty(textMessageBean.getExtra())) {
//            isEmoji = FaceManager.handlerEmojiText(msgBodyText, textMessageBean.getExtra(), false);
//        } else {
//            isEmoji = FaceManager.handlerEmojiText(msgBodyText, TUIChatService.getAppContext().getString(R.string.no_support_msg), false);
//        }
//        if (isForwardMode || isReplyDetailMode) {
//            return;
//        }
//        setSelectableTextHelper(msg, msgBodyText, position, isEmoji);
    }

}