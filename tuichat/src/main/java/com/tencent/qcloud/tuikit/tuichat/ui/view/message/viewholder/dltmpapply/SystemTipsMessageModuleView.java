package com.tencent.qcloud.tuikit.tuichat.ui.view.message.viewholder.dltmpapply;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.tencent.custom.tmp.CustomDlTempMessage;
import com.tencent.qcloud.tuicore.custom.CustomConstants;
import com.tencent.qcloud.tuicore.custom.entity.SystemTipsEntity;
import com.tencent.qcloud.tuikit.tuichat.R;
import com.tencent.qcloud.tuikit.tuichat.bean.message.TUIMessageBean;
import com.tencent.qcloud.tuikit.tuichat.ui.view.message.viewholder.CustomDlTempMessageHolder;

/**
 * Author: 彭石林
 * Time: 2022/9/20 11:26
 * Description: 系统提示模块
 */
public class SystemTipsMessageModuleView extends BaseMessageModuleView{

    public SystemTipsMessageModuleView(CustomDlTempMessageHolder customDlTempMessageHolder) {
        super(customDlTempMessageHolder);
    }
    public void layoutVariableViews(TUIMessageBean msg, int position, FrameLayout rootView, CustomDlTempMessage.MsgBodyInfo msgModuleInfo){
        if (CustomConstants.SystemTipsMessage.TYPE_DISABLE_CALLS.equals(msgModuleInfo.getCustomMsgType())
                || CustomConstants.SystemTipsMessage.TYPE_JUMP_WEB.equals(msgModuleInfo.getCustomMsgType())) {
            loadSystemTipsView(position, msg, msgModuleInfo);
        }else{
            customDlTempMessageHolder.defaultLayout(rootView,msg.isSelf());
        }
    }
    private void loadSystemTipsView(int position, TUIMessageBean msg, CustomDlTempMessage.MsgBodyInfo msgModuleInfo) {
        customDlTempMessageHolder.hideWithAvatarView();
        SystemTipsEntity systemTipsEntity = new Gson().fromJson(new Gson().toJson(msgModuleInfo.getCustomMsgBody()), SystemTipsEntity.class);
        View systemTipsView = View.inflate(getContext(), R.layout.message_adapter_content_server_tip, null);
        TextView msgBody = systemTipsView.findViewById(R.id.custom_tip_text);
        msgBody.setText(Html.fromHtml(systemTipsEntity.getContent()));
        systemTipsView.setOnClickListener(v -> {
            if (customDlTempMessageHolder.onItemClickListener != null)
                customDlTempMessageHolder.onItemClickListener.systemTipsOnClick(position, msg, systemTipsEntity);
        });
        customDlTempMessageHolder.customJsonMsgContentFrame.addView(systemTipsView);
    }

    public Context getContext(){
        return customDlTempMessageHolder.getContext();
    }
}
