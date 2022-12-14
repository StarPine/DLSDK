package com.tencent.qcloud.tuikit.tuichat.ui.view.message.viewholder;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.qcloud.tuicore.util.DateTimeUtil;
import com.tencent.qcloud.tuikit.tuichat.R;
import com.tencent.qcloud.tuikit.tuichat.TUIChatService;
import com.tencent.qcloud.tuikit.tuichat.bean.MessageProperties;
import com.tencent.qcloud.tuikit.tuichat.bean.message.TUIMessageBean;
import com.tencent.qcloud.tuikit.tuichat.ui.interfaces.ICommonMessageAdapter;
import com.tencent.qcloud.tuikit.tuichat.ui.interfaces.OnItemClickListener;
import com.tencent.qcloud.tuikit.tuichat.ui.view.MyImageSpan;
import com.tencent.qcloud.tuikit.tuichat.ui.view.message.MessageRecyclerView;
import com.tencent.qcloud.tuikit.tuichat.ui.view.message.reply.ChatFlowReactView;
import com.tencent.qcloud.tuikit.tuichat.util.TUIChatUtils;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class MessageBaseHolder extends RecyclerView.ViewHolder {
    public static final int MSG_TYPE_HEADER_VIEW = -99;

    public ICommonMessageAdapter mAdapter;
    public MessageProperties properties = MessageProperties.getInstance();
    public OnItemClickListener onItemClickListener;

    public TextView chatTimeText, profitTip;
    public FrameLayout msgContentFrame,msgContentReservFrame,customJsonMsgContentFrame;
    public LinearLayout msgReplyDetailLayout;
    public LinearLayout msgArea;
    public LinearLayout msgAreaAndReply;
    public ChatFlowReactView reactView;
    public CheckBox mMutiSelectCheckBox;
    public RelativeLayout rightGroupLayout;
    public RelativeLayout mContentLayout;
    private ValueAnimator highLightAnimator;
    public final Context appContext;

    public MessageBaseHolder(View itemView) {
        super(itemView);
        appContext = TUIChatService.getAppContext();
        chatTimeText = itemView.findViewById(R.id.message_top_time_tv);
        profitTip = itemView.findViewById(R.id.tv_chat_item_profit_tip);
        msgContentFrame = itemView.findViewById(R.id.msg_content_fl);
        msgContentReservFrame = itemView.findViewById(R.id.msg_content_reserv_fl);
        customJsonMsgContentFrame = itemView.findViewById(R.id.custom_json_msg_content_fl);
        reactView = itemView.findViewById(R.id.reacts_view);
        msgArea = itemView.findViewById(R.id.msg_area);
        msgAreaAndReply = itemView.findViewById(R.id.msg_area_and_reply);
        rightGroupLayout = itemView.findViewById(R.id.right_group_layout);
//        msgReplyDetailLayout = itemView.findViewById(R.id.msg_reply_detail_fl);
//        mMutiSelectCheckBox = itemView.findViewById(R.id.select_checkbox);
//        mContentLayout = itemView.findViewById(R.id.messsage_content_layout);
        initVariableLayout();

    }

    public abstract int getVariableLayout();

    private void setVariableLayout(int resId) {
        if (msgContentFrame.getChildCount() == 0) {
            View.inflate(itemView.getContext(), resId, msgContentFrame);
        }
    }

    private void initVariableLayout() {
        if (getVariableLayout() != 0) {
            setVariableLayout(getVariableLayout());
        }
    }

    public void setAdapter(ICommonMessageAdapter adapter) {
        mAdapter = adapter;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public OnItemClickListener getOnItemClickListener() {
        return this.onItemClickListener;
    }

    public void layoutViews(final TUIMessageBean msg, final int position) {
        setContentLayoutVisibility(true);
        //??????????????????????????????????????????
        msgContentFrame.setVisibility(View.VISIBLE);
        if (msgContentReservFrame != null){
            msgContentReservFrame.removeAllViews();
        }
        if (customJsonMsgContentFrame != null){
            customJsonMsgContentFrame.removeAllViews();
        }
        //????????????????????????
        showProfitView(msg);

        //// ???????????????
        if (properties.getChatTimeBubble() != null) {
            chatTimeText.setBackground(properties.getChatTimeBubble());
        }
        if (properties.getChatTimeFontColor() != 0) {
            chatTimeText.setTextColor(properties.getChatTimeFontColor());
        }
        if (properties.getChatTimeFontSize() != 0) {
            chatTimeText.setTextSize(properties.getChatTimeFontSize());
        }

        if (position > 1) {
            TUIMessageBean last = mAdapter.getItem(position - 1);
            if (last != null) {
                long valueTime = msg.getMessageTime() - last.getMessageTime();
                if (valueTime >= 5 * 60 || valueTime < 0) {
                    chatTimeText.setVisibility(View.VISIBLE);
                    chatTimeText.setText(DateTimeUtil.getTimeFormatText(new Date(msg.getMessageTime() * 1000)));
                } else {
                    chatTimeText.setVisibility(View.GONE);
                }
            }
        } else {
            chatTimeText.setVisibility(View.VISIBLE);
            chatTimeText.setText(DateTimeUtil.getTimeFormatText(new Date(msg.getMessageTime() * 1000)));
        }
    }

    /**
     * ????????????view
     * @param msg
     */
    private void showProfitView(TUIMessageBean msg) {
        if (profitTip == null)return;
        profitTip.setVisibility(View.GONE);
        String loginUser = V2TIMManager.getInstance().getLoginUser();
        String cloudCustomData = msg.getV2TIMMessage().getCloudCustomData();
        if (TUIChatUtils.isJSON2(cloudCustomData)){
            String cost = TUIChatUtils.json2Massage(cloudCustomData, "cost");
            String refundMoney = TUIChatUtils.json2Massage(cloudCustomData, "isRefundMoney");
            String payId = TUIChatUtils.json2Massage(cloudCustomData, "payId");//?????????id
            if (cost != null && !cost.equals("0.0") && msg.isSelf() && payId != null && !payId.equals(loginUser)){//???????????????,????????????????????????????????????
                setProfitDetails(cost, profitTip);
            }
            if (refundMoney != null  && !msg.isSelf()){
                profitTip.setText(appContext.getString(R.string.custom_message_txt_girl));
                profitTip.setVisibility(View.VISIBLE);
            }
        }
        if (!MessageRecyclerView.isFlagTipMoney()){
            profitTip.setVisibility(View.GONE);
        }
    }

    /**
     * ??????????????????
     * @param profit
     * @param profitTipView
     */
    public void setProfitDetails(String profit, TextView profitTipView) {
        if (profit == null)return;
        String profitContent = appContext.getString(R.string.profit);
        if (!MessageRecyclerView.isCertification()) {
            profitContent = appContext.getString(R.string.custom_message_txt2_test2);
            profitTipView.setOnClickListener(v -> onItemClickListener.onClickCustomText());
        }
        try {
            profit = String.format("%.2f", Double.parseDouble(profit));
        }catch (Exception ignored){
            Log.e("imsdk", "??????????????????: " + profit);
        }
        String format = String.format(profitContent, profit);
        SpannableString iconSpannable = matcherSearchText("#A72DFE", format, appContext.getString(R.string.custom_message_txt1_key));
        iconSpannable.setSpan(new MyImageSpan(TUIChatService.getAppContext(), R.drawable.icon_crystal), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        profitTipView.setText(iconSpannable);
        if (!MessageRecyclerView.isFlagTipMoney()){
            profitTipView.setVisibility(View.GONE);
        }else {
            profitTipView.setVisibility(View.VISIBLE);
        }
    }


    /**
     * ???????????? ??????????????????SpannableString ??????????????????????????????
     */
    public SpannableString matcherSearchText(String color, String text, String keyword) {
        if (text == null || TextUtils.isEmpty(text)) {
            return SpannableString.valueOf("");
        }
        SpannableString spannableString = new SpannableString(text);
        //?????? keyword
        Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
        //??????
        Matcher matcher = pattern.matcher(spannableString);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            //ForegroundColorSpan ??????new ??????????????????????????????
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(color)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        //???????????????????????????
        return spannableString;
    }

    public void stopHighLight() {
        if (highLightAnimator != null) {
            highLightAnimator.cancel();
        }
        clearHighLightBackground();
    }

    // ???????????????????????????????????????
    public void startHighLight() {
        int highLightColorDark = itemView.getResources().getColor(R.color.chat_message_bubble_high_light_dark_color);
        int highLightColorLight = itemView.getResources().getColor(R.color.chat_message_bubble_high_light_light_color);

        if (highLightAnimator == null) {
            ArgbEvaluator argbEvaluator = new ArgbEvaluator();
            highLightAnimator = new ValueAnimator();
            highLightAnimator.setIntValues(highLightColorDark, highLightColorLight);
            highLightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Integer color = (Integer) animation.getAnimatedValue();
                    setHighLightBackground(color);
                }
            });
            highLightAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    clearHighLightBackground();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    clearHighLightBackground();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            highLightAnimator.setEvaluator(argbEvaluator);
            highLightAnimator.setRepeatCount(3);
            highLightAnimator.setDuration(250);
            highLightAnimator.setRepeatMode(ValueAnimator.REVERSE);
        }
        highLightAnimator.start();
    }

    public void setHighLightBackground(int color) {
        Drawable drawable = msgArea.getBackground();
        if (drawable != null) {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }

    public void clearHighLightBackground() {
        Drawable drawable = msgArea.getBackground();
        if (drawable != null) {
            drawable.setColorFilter(null);
        }
    }

    /**
     * ??????recycleview??????item
     * @param isVisible
     */
    public void setContentLayoutVisibility(boolean isVisible){
        RecyclerView.LayoutParams param = (RecyclerView.LayoutParams)itemView.getLayoutParams();
        if (isVisible){
            param.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            param.width = LinearLayout.LayoutParams.MATCH_PARENT;
            itemView.setVisibility(View.VISIBLE);
        }else{
            itemView.setVisibility(View.GONE);
            param.height = 0;
            param.width = 0;
        }
        itemView.setLayoutParams(param);
    }


    /**
     * ??????????????????????????? dp ????????? ????????? px(??????)
     */
    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}