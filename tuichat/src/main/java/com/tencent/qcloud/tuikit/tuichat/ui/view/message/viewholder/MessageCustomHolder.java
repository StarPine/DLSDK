package com.tencent.qcloud.tuikit.tuichat.ui.view.message.viewholder;

import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.qcloud.tuikit.tuichat.R;
import com.tencent.qcloud.tuikit.tuichat.bean.message.TUIMessageBean;
import com.tencent.qcloud.tuikit.tuichat.ui.interfaces.ICustomMessageViewGroup;


public class MessageCustomHolder extends MessageContentHolder implements ICustomMessageViewGroup {
    public static final String TAG = MessageCustomHolder.class.getSimpleName();

    private TUIMessageBean mTUIMessageBean;
    private int mPosition;

    private TextView msgBodyText;

    private boolean isShowMutiSelect = false;

    public MessageCustomHolder(View itemView) {
        super(itemView);
        msgBodyText = itemView.findViewById(R.id.msg_body_tv);

    }

    public void setShowMutiSelect(boolean showMutiSelect) {
        isShowMutiSelect = showMutiSelect;
    }

    @Override
    public int getVariableLayout() {
        return R.layout.message_adapter_content_text;
    }

    @Override
    public void layoutViews(TUIMessageBean msg, int position) {
        mTUIMessageBean = msg;
        mPosition = position;
        super.layoutViews(msg, position);
    }

    @Override
    public void layoutVariableViews(final TUIMessageBean msg, final int position) {
        rootView.findViewById(R.id.user_content).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.full_toast).setVisibility(View.GONE);
        if (msg.getExtra() != null) {
            if (TextUtils.equals("[自定义消息]", msg.getExtra().toString())) {
                msgBodyText.setText(Html.fromHtml("[不支持的自定义消息]"));
            } else {
                msgBodyText.setText(msg.getExtra().toString());
            }
        }
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

    private void hideAll() {
        for (int i = 0; i < ((RelativeLayout) rootView).getChildCount(); i++) {
            ((RelativeLayout) rootView).getChildAt(i).setVisibility(View.GONE);
        }
    }

    @Override
    public void addMessageItemView(View view) {
        hideAll();
        if (view != null) {
            ((RelativeLayout) rootView).removeView(view);
            ((RelativeLayout) rootView).addView(view);
        }
    }

    @Override
    public void addMessageContentView(View view) {
        // item有可能被复用，因为不能确定是否存在其他自定义view，这里把所有的view都隐藏之后重新layout
        hideAll();
        super.layoutViews(mTUIMessageBean, mPosition);

        if (view != null) {
            for (int i = 0; i < msgContentFrame.getChildCount(); i++) {
                msgContentFrame.getChildAt(i).setVisibility(View.GONE);
            }
            msgContentFrame.removeView(view);
            msgContentFrame.addView(view);
        }
    }

}
