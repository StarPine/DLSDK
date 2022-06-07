package com.tencent.qcloud.tuikit.tuiconversation.ui.view;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMUserFullInfo;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.tencent.qcloud.tuicore.util.DateTimeUtil;
import com.tencent.qcloud.tuikit.tuichat.component.face.FaceManager;
import com.tencent.qcloud.tuikit.tuiconversation.R;
import com.tencent.qcloud.tuikit.tuiconversation.TUIConversationConstants;
import com.tencent.qcloud.tuikit.tuiconversation.TUIConversationService;
import com.tencent.qcloud.tuikit.tuiconversation.bean.ConversationInfo;
import com.tencent.qcloud.tuikit.tuiconversation.bean.ConversationMessageInfo;
import com.tencent.qcloud.tuikit.tuiconversation.bean.DraftInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConversationCommonHolder extends ConversationBaseHolder {

    public static boolean sexMale = false;

    //彭石林修改
    protected final ImageView certification;
    protected final ImageView iv_vip;
    public ConversationIconView conversationIconView;
    protected LinearLayout leftItemLayout;
    protected TextView titleText;
    protected TextView messageText;
    protected TextView timelineText;
    protected TextView unreadText;
    protected TextView atInfoText;
    protected ImageView disturbView;
    protected CheckBox multiSelectCheckBox;
    private boolean isForwardMode = false;

    public ConversationCommonHolder(View itemView) {
        super(itemView);
        leftItemLayout = rootView.findViewById(R.id.item_left);
        conversationIconView = rootView.findViewById(R.id.conversation_icon);
        titleText = rootView.findViewById(R.id.conversation_title);
        messageText = rootView.findViewById(R.id.conversation_last_msg);
        timelineText = rootView.findViewById(R.id.conversation_time);
        unreadText = rootView.findViewById(R.id.conversation_unread);
        atInfoText = rootView.findViewById(R.id.conversation_at_msg);
        disturbView = rootView.findViewById(R.id.not_disturb);
        multiSelectCheckBox = rootView.findViewById(R.id.select_checkbox);

        certification = rootView.findViewById(R.id.certification);
        iv_vip = rootView.findViewById(R.id.iv_vip);
    }

    private static String emojiJudge(String text){
        if (TextUtils.isEmpty(text)){
            return "";
        }

        String[] emojiList = FaceManager.getEmojiFilters();
        if (emojiList ==null || emojiList.length == 0){
            return text;
        }

        SpannableStringBuilder sb = new SpannableStringBuilder(text);
        String regex = "\\[(\\S+?)\\]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        ArrayList<EmojiData> emojiDataArrayList = new ArrayList<>();
        //遍历找到匹配字符并存储
        int lastMentionIndex = -1;
        while (m.find()) {
            String emojiName = m.group();
            int start;
            if (lastMentionIndex != -1) {
                start = text.indexOf(emojiName, lastMentionIndex);
            } else {
                start = text.indexOf(emojiName);
            }
            int end = start + emojiName.length();
            lastMentionIndex = end;

            int index = findeEmoji(emojiName);
            String[] emojiListValues = FaceManager.getEmojiFiltersValues();
            if (index != -1 && emojiListValues != null && emojiListValues.length >= index){
                emojiName = emojiListValues[index];
            }


            EmojiData emojiData =new EmojiData();
            emojiData.setStart(start);
            emojiData.setEnd(end);
            emojiData.setEmojiText(emojiName);

            emojiDataArrayList.add(emojiData);
        }

        //倒叙替换
        if (emojiDataArrayList.isEmpty()){
            return text;
        }
        for (int i = emojiDataArrayList.size() - 1; i >= 0; i--){
            EmojiData emojiData = emojiDataArrayList.get(i);
            String emojiName = emojiData.getEmojiText();
            int start = emojiData.getStart();
            int end = emojiData.getEnd();

            if (!TextUtils.isEmpty(emojiName) && start != -1 && end != -1) {
                sb.replace(start, end, emojiName);
            }
        }
        return sb.toString();
    }

    private static int findeEmoji(String text){
        int result = -1;
        if (TextUtils.isEmpty(text)){
            return result;
        }

        String[] emojiList = FaceManager.getEmojiFilters();
        if (emojiList ==null || emojiList.length == 0){
            return result;
        }

        for (int i = 0; i < emojiList.length; i++){
            if (text.equals(emojiList[i])){
                result = i;
                break;
            }
        }

        return result;
    }

    public void setForwardMode(boolean forwardMode) {
        isForwardMode = forwardMode;
    }

    public static boolean isJSON2(String str) {
        boolean result = false;
        try {
            new Gson().fromJson(str, Map.class);
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return result;

    }

    public void layoutVariableViews(ConversationInfo conversationInfo, int position) {

    }

    public void layoutViews(ConversationInfo conversation, int position) {
        ConversationMessageInfo lastMsg = conversation.getLastMessage();
        if (lastMsg != null && lastMsg.getStatus() == ConversationMessageInfo.MSG_STATUS_REVOKE) {
            if (lastMsg.isSelf()) {
                lastMsg.setExtra(TUIConversationService.getAppContext().getString(R.string.revoke_tips_you));
            } else if (lastMsg.isGroup()) {
                String message = TUIConversationConstants.covert2HTMLString(
                        TextUtils.isEmpty(lastMsg.getGroupNameCard())
                                ? lastMsg.getFromUser()
                                : lastMsg.getGroupNameCard());
                lastMsg.setExtra(message + TUIConversationService.getAppContext().getString(R.string.revoke_tips));
            } else {
                lastMsg.setExtra(TUIConversationService.getAppContext().getString(R.string.revoke_tips_other));
            }
        }

        if (conversation.isTop() && !isForwardMode) {
            leftItemLayout.setBackgroundColor(rootView.getResources().getColor(R.color.conversation_top_color));
        } else {
            leftItemLayout.setBackgroundColor(Color.WHITE);
        }

        titleText.setText(conversation.getTitle());
        messageText.setText("");
        timelineText.setText("");
        DraftInfo draftInfo = conversation.getDraft();
        if (draftInfo != null && !TextUtils.isEmpty(draftInfo.getDraftText())) {
            messageText.setText(draftInfo.getDraftText());
            timelineText.setText(DateTimeUtil.getTimeFormatText(new Date(draftInfo.getDraftTime() * 1000)));
        } else {
            if (lastMsg != null) {
                if (lastMsg.getExtra() != null) {
                    if (sexMale) {
                        if (lastMsg.isSelf()) {
                            if (isJSON2(lastMsg.getExtra().toString())) {
                                messageText.setText(rootView.getResources().getText(R.string.default_message_content3));
                            } else {
                                FaceManager.handlerEmojiText(messageText, (lastMsg.getExtra().toString()), false);
                            }
                        } else {
                                messageText.setText(rootView.getResources().getText(R.string.default_message_content));
                        }
                    } else {
                        if (lastMsg.isSelf()) {
                            if (isJSON2(lastMsg.getExtra().toString())) {
                                messageText.setText(rootView.getResources().getText(R.string.default_message_content3));
                            } else {
                                FaceManager.handlerEmojiText(messageText, (lastMsg.getExtra().toString()), false);
                            }
                        } else {
                            if (isJSON2(lastMsg.getExtra().toString())) {
                                messageText.setText(rootView.getResources().getText(R.string.default_message_content));
                            } else {
                                messageText.setText(rootView.getResources().getText(R.string.text_message2));
                            }
                        }
                    }
                    messageText.setTextColor(rootView.getResources().getColor(R.color.list_bottom_text_bg));
                }
                timelineText.setText(DateTimeUtil.getTimeFormatText(new Date(lastMsg.getMsgTime() * 1000)));
            }
        }
        //额外处理判断。如果是客服人员则默认显示===收到一则讯息
        if (conversation.getId().trim().contains("administrator")) {
            messageText.setText(rootView.getResources().getText(R.string.text_message));
        }

        if (conversation.getUnRead() > 0 && !conversation.isShowDisturbIcon()) {
            unreadText.setVisibility(View.VISIBLE);
            if (conversation.getUnRead() > 99) {
                unreadText.setText("99+");
            } else {
                unreadText.setText("" + conversation.getUnRead());
            }
        } else {
            unreadText.setVisibility(View.GONE);
        }

        if (draftInfo != null && !TextUtils.isEmpty(draftInfo.getDraftText())) {
            atInfoText.setVisibility(View.VISIBLE);
            atInfoText.setText(R.string.drafts);
            atInfoText.setTextColor(Color.RED);
        } else {
            if (conversation.getAtInfoText().isEmpty()) {
                atInfoText.setVisibility(View.GONE);
            } else {
                atInfoText.setVisibility(View.VISIBLE);
                atInfoText.setText(conversation.getAtInfoText());
                atInfoText.setTextColor(Color.RED);
            }
        }

        conversationIconView.setRadius(mAdapter.getItemAvatarRadius());
        if (mAdapter.getItemDateTextSize() != 0) {
            timelineText.setTextSize(mAdapter.getItemDateTextSize());
        }
        if (mAdapter.getItemBottomTextSize() != 0) {
            messageText.setTextSize(mAdapter.getItemBottomTextSize());
        }
        if (mAdapter.getItemTopTextSize() != 0) {
            titleText.setTextSize(mAdapter.getItemTopTextSize());
        }
        if (!mAdapter.hasItemUnreadDot()) {
            unreadText.setVisibility(View.GONE);
        }

        conversationIconView.setConversation(conversation);

        conversationIconView.setTag(conversation);
        conversationIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAdapter.mOnItemAvatarClickListener != null) {
                    ConversationInfo conversation = (ConversationInfo) view.getTag();
                    mAdapter.mOnItemAvatarClickListener.onItemAvatarClick(view,position, conversation);
                }
            }
        });

        if (conversation.isShowDisturbIcon()) {
            disturbView.setVisibility(View.VISIBLE);
        } else {
            disturbView.setVisibility(View.GONE);
        }

        if (isForwardMode) {
            messageText.setVisibility(View.GONE);
            timelineText.setVisibility(View.GONE);
            unreadText.setVisibility(View.GONE);
            atInfoText.setVisibility(View.GONE);
        }

        //// 由子类设置指定消息类型的views
        layoutVariableViews(conversation, position);

        //彭石林修改
        //待获取用户资料的用户列表
        List<String> users = new ArrayList<String>();

        users.add(conversation.getId());
        //获取用户资料
        V2TIMManager.getInstance().getUsersInfo(users,new V2TIMValueCallback<List<V2TIMUserFullInfo>>(){
            @Override
            public void onSuccess(List<V2TIMUserFullInfo> v2TIMUserFullInfos) {
                for(V2TIMUserFullInfo res : v2TIMUserFullInfos){
                    int level = res.getLevel();
                    // certification;
                    // iv_vip;
                    if(level==1){ //vip
                        iv_vip.setImageResource(R.drawable.ic_vip);
                        iv_vip.setVisibility(View.VISIBLE);
                        certification.setVisibility(View.GONE);
                    }else if(level==2){ //真人
                        certification.setImageResource(R.drawable.ic_real_man);
                        iv_vip.setVisibility(View.GONE);
                        certification.setVisibility(View.VISIBLE);
                    }else if(level==3 || level==5){
                        iv_vip.setImageResource(R.drawable.ic_goddess);
                        certification.setVisibility(View.GONE);
                        iv_vip.setVisibility(View.VISIBLE);
                    }else if(level==4){
                        certification.setImageResource(R.drawable.ic_real_man);
                        iv_vip.setImageResource(R.drawable.ic_vip);
                        iv_vip.setVisibility(View.VISIBLE);
                        certification.setVisibility(View.VISIBLE);
                    }else{
                        iv_vip.setVisibility(View.GONE);
                        certification.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onError(int code, String desc){
                //错误码 code 和错误描述 desc，可用于定位请求失败原因
                //错误码 code 列表请参见错误码表
                Log.e("获取用户信息失败", "getUsersProfile failed: " + code + " desc");
            }
        });
    }

    private static class EmojiData{
        private int start;
        private int end;
        private String emojiText;

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public String getEmojiText() {
            return emojiText;
        }

        public void setEmojiText(String emojiText) {
            this.emojiText = emojiText;
        }
    }

    public int imUserIdToSystemUserId(String userId) {
        try {
            String strId = userId.replaceFirst("user_", "");
            return Integer.parseInt(strId);
        } catch (Exception e) {
            return 0;
        }
    }
}
