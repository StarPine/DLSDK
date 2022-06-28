package com.tencent.qcloud.tuikit.tuichat.ui.view.message.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tencent.qcloud.tuikit.tuichat.R;
import com.tencent.qcloud.tuikit.tuichat.bean.message.CallingMessageBean;
import com.tencent.qcloud.tuikit.tuichat.bean.message.TUIMessageBean;

public class CallingMessageHolder extends TextMessageHolder{

    private TextView msgBodyText;
    private ImageView mLeftView, mRightView;
    private LinearLayout mCallingLayout;

    public CallingMessageHolder(View itemView) {
        super(itemView);
        msgBodyText = itemView.findViewById(R.id.msg_body_tv);
        mLeftView = itemView.findViewById(R.id.left_icon);
        mRightView = itemView.findViewById(R.id.right_icon);
        mCallingLayout = itemView.findViewById(R.id.calling_layout);
    }

    @Override
    public int getVariableLayout() {
        return R.layout.message_adapter_content_calling;
    }

    @Override
    public void layoutVariableViews(TUIMessageBean msg, int position) {
        super.layoutVariableViews(msg, position);

        if (!(msg instanceof CallingMessageBean)) {
            return;
        }
        CallingMessageBean callingMessageBean = (CallingMessageBean) msg;
        if (msg.isSelf()) {
            mLeftView.setVisibility(View.GONE);
            mRightView.setVisibility(View.VISIBLE);
            if (callingMessageBean.getCallType() == CallingMessageBean.ACTION_ID_AUDIO_CALL) {
                mRightView.setImageResource(R.drawable.custom_audio_right_img_2);
            } else if (callingMessageBean.getCallType() == CallingMessageBean.ACTION_ID_VIDEO_CALL) {
                mRightView.setImageResource(R.drawable.custom_video_right_img_1);
            }
        } else {
            mRightView.setVisibility(View.GONE);
            mLeftView.setVisibility(View.VISIBLE);
            if (callingMessageBean.getCallType() == CallingMessageBean.ACTION_ID_AUDIO_CALL) {
                mLeftView.setImageResource(R.drawable.custom_audio_left_img_2);
            } else if (callingMessageBean.getCallType() == CallingMessageBean.ACTION_ID_VIDEO_CALL) {
                mLeftView.setImageResource(R.drawable.custom_video_left_img_1);
            }
        }

        if (callingMessageBean.getCallType() == CallingMessageBean.ACTION_ID_AUDIO_CALL ||
                callingMessageBean.getCallType() == CallingMessageBean.ACTION_ID_VIDEO_CALL) {
            mCallingLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onRecallClick(view, position, msg);
                }
            });
        }
    }
}
