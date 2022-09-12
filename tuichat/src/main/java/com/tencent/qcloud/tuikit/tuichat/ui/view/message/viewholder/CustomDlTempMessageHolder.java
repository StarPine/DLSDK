package com.tencent.qcloud.tuikit.tuichat.ui.view.message.viewholder;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.tencent.custom.IMGsonUtils;
import com.tencent.custom.tmp.CustomDlTempMessage;
import com.tencent.qcloud.tuicore.TUIThemeManager;
import com.tencent.qcloud.tuicore.custom.CustomConstants;
import com.tencent.qcloud.tuicore.custom.entity.CallingRejectEntity;
import com.tencent.qcloud.tuicore.custom.entity.SystemTipsEntity;
import com.tencent.qcloud.tuikit.tuichat.R;
import com.tencent.qcloud.tuikit.tuichat.TUIChatService;
import com.tencent.qcloud.tuikit.tuichat.bean.CustomImageMessage;
import com.tencent.qcloud.tuikit.tuichat.bean.message.TUIMessageBean;
import com.tencent.qcloud.tuikit.tuichat.util.TUIChatUtils;

/**
 * Author: 彭石林
 * Time: 2022/9/8 11:49
 * Description: 自定义消息模板view渲染层
 */
public class CustomDlTempMessageHolder extends MessageContentHolder {
    private static final String TAG = "DlTempMessageHolder";
    private final FrameLayout flTmpLayout;

    public CustomDlTempMessageHolder(View itemView) {
        super(itemView);
        flTmpLayout = itemView.findViewById(R.id.fl_tmp_layout);
    }

    @Override
    public int getVariableLayout() {
        return R.layout.custom_dl_tmp_message_layout;
    }

    @Override
    public void layoutVariableViews(TUIMessageBean msg, int position) {
        if (flTmpLayout != null) {
            flTmpLayout.removeAllViews();
        }
        CustomDlTempMessage customDlTempMessage = IMGsonUtils.fromJson(new String(msg.getCustomElemData()), CustomDlTempMessage.class);
        if (customDlTempMessage == null) {
            defaultLayout(itemView.getContext(), flTmpLayout, msg.isSelf());
            return;
        }
        //判断模块
        if (customDlTempMessage.getContentBody() != null && !TextUtils.isEmpty(customDlTempMessage.getContentBody().getMsgModuleName())) {
            String moduleName = customDlTempMessage.getContentBody().getMsgModuleName();
            if (TextUtils.isEmpty(moduleName)) {
                defaultLayout(itemView.getContext(), flTmpLayout, msg.isSelf());
                return;
            }
            CustomDlTempMessage.MsgBodyInfo msgModuleInfo = customDlTempMessage.getContentBody().getContentBody();
            //红包照片模块
            if (CustomConstants.PacketSnapshot.MODULE_NAME.equals(moduleName)) {
                //照片内容
                if (CustomConstants.PacketSnapshot.IMG_PHOTO.equals(msgModuleInfo.getCustomMsgType())) {
                    CustomImageMessage customImageMessageBean = IMGsonUtils.fromJson(IMGsonUtils.toJson(msgModuleInfo.getCustomMsgBody()), CustomImageMessage.class);
                    imgLoad(itemView.getContext(), flTmpLayout, customImageMessageBean);
                }
            } else if (CustomConstants.CallingMessage.MODULE_NAME.equals(moduleName)) {
                //禁止通话模块
                if (CustomConstants.CallingMessage.TYPE_CALLING_FAILED.equals(msgModuleInfo.getCustomMsgType())) {
                    CallingRejectEntity callingRejectEntity = IMGsonUtils.fromJson(IMGsonUtils.toJson(msgModuleInfo.getCustomMsgBody()), CallingRejectEntity.class);
                    loadCallingView(msg, callingRejectEntity);
                }
            } else if (CustomConstants.SystemTipsMessage.MODULE_NAME.equals(moduleName)) {
                //系统提示模块
                if (CustomConstants.SystemTipsMessage.TYPE_DISABLE_CALLS.equals(msgModuleInfo.getCustomMsgType())) {
                    loadSystemTipsView(position, msg, msgModuleInfo);
                }
            } else {
                //默认展示解析不出的模板提示
                defaultLayout(itemView.getContext(), flTmpLayout, msg.isSelf());
            }
        } else {
            defaultLayout(itemView.getContext(), flTmpLayout, msg.isSelf());
        }

    }

    private void loadSystemTipsView(int position, TUIMessageBean msg, CustomDlTempMessage.MsgBodyInfo msgModuleInfo) {
        hideWithAvatarView();
        SystemTipsEntity systemTipsEntity = new Gson().fromJson(new Gson().toJson(msgModuleInfo.getCustomMsgBody()), SystemTipsEntity.class);
        View systemTipsView = View.inflate(itemView.getContext(), R.layout.message_adapter_content_server_tip, null);
        TextView msgBody = systemTipsView.findViewById(R.id.custom_tip_text);
        msgBody.setText(Html.fromHtml(systemTipsEntity.getContent()));
        systemTipsView.setOnClickListener(v -> {
            if (onItemClickListener != null)
                onItemClickListener.systemTipsOnClick(position, msg, systemTipsEntity.getType());
        });

        customJsonMsgContentFrame.addView(systemTipsView);
    }

    private void loadCallingView(TUIMessageBean msg, CallingRejectEntity callingRejectEntity) {
        View callingView = View.inflate(itemView.getContext(), R.layout.message_adapter_content_json_text, null);
        TextView msgBody = callingView.findViewById(R.id.msg_body_tv);
        ImageView leftView = callingView.findViewById(R.id.left_icon);
        ImageView rightView = callingView.findViewById(R.id.right_icon);
        setBackColor(msg, msgBody);
        setCallingMsgIconStyle(msg, leftView, rightView, callingRejectEntity.getCallingType());
        msgBody.setText(callingRejectEntity.getContent());
        flTmpLayout.addView(callingView);
    }

    //默认消息模板
    public void defaultLayout(Context context, FrameLayout rootView, boolean isSelf) {
        View defaultView = View.inflate(context, R.layout.test_custom_message_layout1, null);
        TextView textView = defaultView.findViewById(R.id.test_custom_message_tv);
        TextView linkView = defaultView.findViewById(R.id.link_tv);
        linkView.setVisibility(View.GONE);
        if (!isSelf) {
            textView.setTextColor(textView.getResources().getColor(TUIThemeManager.getAttrResId(textView.getContext(), R.attr.chat_other_custom_msg_text_color)));
            linkView.setTextColor(textView.getResources().getColor(TUIThemeManager.getAttrResId(textView.getContext(), R.attr.chat_other_custom_msg_link_color)));
        } else {
            textView.setTextColor(textView.getResources().getColor(TUIThemeManager.getAttrResId(textView.getContext(), R.attr.chat_self_custom_msg_text_color)));
            linkView.setTextColor(textView.getResources().getColor(TUIThemeManager.getAttrResId(textView.getContext(), R.attr.chat_self_custom_msg_link_color)));
        }
        textView.setText(R.string.no_support_msg);
        msgContentFrame.setClickable(true);
        rootView.addView(defaultView);
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


    /**
     * 设置内容字体和颜色
     *
     * @param msg
     */
    public void setBackColor(TUIMessageBean msg, TextView msgBodyText) {
        if (properties.getChatContextFontSize() != 0) {
            msgBodyText.setTextSize(properties.getChatContextFontSize());
        }
        if (msg.isSelf()) {
            if (properties.getRightChatContentFontColor() != 0) {
                msgBodyText.setTextColor(properties.getRightChatContentFontColor());
            }
        } else {
            if (properties.getLeftChatContentFontColor() != 0) {
                msgBodyText.setTextColor(properties.getLeftChatContentFontColor());
            }
        }
    }

    //测试自定义图片渲染
    public void imgLoad(Context context, FrameLayout rootView, CustomImageMessage customImageMessageBean) {
        View customImageView = View.inflate(context, R.layout.custom_image_message_layout, null);
        ImageView customImage = customImageView.findViewById(R.id.iv_custom_image);
        String imagePath = TUIChatUtils.getFullImageUrl(customImageMessageBean.getImgPath());
        Glide.with(TUIChatService.getAppContext())
                .asBitmap()
                .load(imagePath)
                .error(R.drawable.chat_custom_image_error)
                .centerCrop()
                .placeholder(R.drawable.chat_custom_image_load)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(customImage);
        rootView.addView(customImageView);
    }

}
