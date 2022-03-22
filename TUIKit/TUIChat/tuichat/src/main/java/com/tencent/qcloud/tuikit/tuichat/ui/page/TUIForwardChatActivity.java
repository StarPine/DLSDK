package com.tencent.qcloud.tuikit.tuichat.ui.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.tencent.coustom.CustomIMTextEntity;
import com.tencent.coustom.EvaluateItemEntity;
import com.tencent.coustom.PhotoAlbumItemEntity;
import com.tencent.qcloud.tuicore.component.CustomLinearLayoutManager;
import com.tencent.qcloud.tuicore.component.TitleBarLayout;
import com.tencent.qcloud.tuicore.component.activities.BaseLightActivity;
import com.tencent.qcloud.tuicore.component.interfaces.ITitleBarLayout;
import com.tencent.qcloud.tuikit.tuichat.R;
import com.tencent.qcloud.tuikit.tuichat.TUIChatConstants;
import com.tencent.qcloud.tuikit.tuichat.bean.MergeMessageElemBean;
import com.tencent.qcloud.tuikit.tuichat.bean.MessageInfo;
import com.tencent.qcloud.tuikit.tuichat.presenter.ForwardPresenter;
import com.tencent.qcloud.tuikit.tuichat.ui.interfaces.OnItemLongClickListener;
import com.tencent.qcloud.tuikit.tuichat.ui.view.message.MessageAdapter;
import com.tencent.qcloud.tuikit.tuichat.ui.view.message.MessageRecyclerView;
import com.tencent.qcloud.tuikit.tuichat.util.TUIChatLog;

public class TUIForwardChatActivity extends BaseLightActivity {

    private static final String TAG = TUIForwardChatActivity.class.getSimpleName();

    private TitleBarLayout mTitleBar;
    private MessageRecyclerView mFowardChatMessageRecyclerView;
    private MessageAdapter mForwardChatAdapter;

    private MessageInfo mMessageInfo;
    private String mTitle;

    private ForwardPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forward_chat_layout);
        mFowardChatMessageRecyclerView = findViewById(R.id.chat_message_layout);
        mFowardChatMessageRecyclerView.setLayoutManager(new CustomLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mForwardChatAdapter = new MessageAdapter();
        mForwardChatAdapter.setForwardMode(true);
        presenter = new ForwardPresenter();
        presenter.setMessageListAdapter(mForwardChatAdapter);

        mFowardChatMessageRecyclerView.setAdapter(mForwardChatAdapter);

        mTitleBar = findViewById(R.id.chat_title_bar);
        mTitleBar.setOnLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mFowardChatMessageRecyclerView.setOnItemClickListener(new OnItemLongClickListener() {
            @Override
            public void onMessageLongClick(View view, int position, MessageInfo messageInfo) {

            }

            @Override
            public void onUserIconClick(View view, int position, MessageInfo messageInfo) {
                if (null == messageInfo) {
                    return;
                }

                MergeMessageElemBean mergeMessageElemBean = MergeMessageElemBean.createMergeMessageElemBean(messageInfo);
                if (mergeMessageElemBean != null){
                    Intent intent = new Intent(getBaseContext(), TUIForwardChatActivity.class);
                    Bundle bundle=new Bundle();
                    bundle.putSerializable(TUIChatConstants.FORWARD_MERGE_MESSAGE_KEY, messageInfo);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }

            @Override
            public void onToastVipText(MessageInfo messageInfo) {

            }

            @Override
            public void onTextReadUnlock(TextView textView, View view, MessageInfo messageInfo) {

            }

            @Override
            public void onTextTOWebView(MessageInfo messageInfo) {

            }


            @Override
            public void toUserHome() {

            }

            @Override
            public void openUserImage(PhotoAlbumItemEntity itemEntity) {

            }

            @Override
            public void onClickEvaluate(int position, MessageInfo messageInfo, EvaluateItemEntity evaluateItemEntity, boolean more) {

            }

            @Override
            public void onClickCustomText(int position, MessageInfo messageInfo, CustomIMTextEntity customIMTextEntity) {

            }

            @Override
            public void onClickDialogRechargeShow() {

            }

            @Override
            public void clickToUserMain() {

            }
        });

        init();
    }

    private void init(){
        Intent intent = getIntent();
        if (intent != null) {
            mTitleBar.setTitle(mTitle, ITitleBarLayout.Position.MIDDLE);
            mTitleBar.getRightGroup().setVisibility(View.GONE);

            mMessageInfo = (MessageInfo) intent.getSerializableExtra(TUIChatConstants.FORWARD_MERGE_MESSAGE_KEY);
            if (null == mMessageInfo) {
                TUIChatLog.e(TAG, "mMessageInfo is null");
                return;
            }
            MergeMessageElemBean mergeMessageElemBean = MergeMessageElemBean.createMergeMessageElemBean(mMessageInfo);
            presenter.downloadMergerMessage(mergeMessageElemBean);
        }
    }

}
