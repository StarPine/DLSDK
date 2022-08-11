package com.dl.playfun.kl.view;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dl.playfun.R;
import com.dl.playfun.app.GlideEngine;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.widget.image.CircleImageView;
import com.tencent.liteav.trtccalling.TUICalling;
import com.tencent.liteav.trtccalling.ui.base.BaseTUICallView;
import com.tencent.liteav.trtccalling.ui.base.Status;
import com.tencent.liteav.trtccalling.ui.floatwindow.FloatWindowService;

public class AudioFloatCallView extends BaseTUICallView {
    private static final String TAG = "AudioFloatCallView";
    private ImageView maximize;
    private CircleImageView ivAvatar;
    private TextView mTextViewTimeCount;


    public AudioFloatCallView(Context context, TUICalling.Role role, TUICalling.Type type, String[] userIDs,
                              String sponsorID, String groupID, boolean isFromGroup,String avatar,int timeCount) {
        super(context, role, type, userIDs, sponsorID, groupID, isFromGroup);
        initData(avatar, timeCount);
    }

    private void initData(String avatar, int timeCount) {
        GlideEngine.createGlideEngine().loadImage(getContext(), StringUtil.getFullThumbImageUrl(avatar), ivAvatar);
        showTimeCount(mTextViewTimeCount,timeCount);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initListener();
        showFloatWindow();
    }

    @Override
    protected void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.audio_floatwindow_layout, this);
        maximize = findViewById(R.id.iv_maximize);
        ivAvatar = findViewById(R.id.iv_avatar);
        mTextViewTimeCount = findViewById(R.id.tv_time);
    }

    //更新显示
    private void showFloatWindow() {
        Status.mIsShowFloatWindow = true;
    }


    //通话时长,注意UI更新需要在主线程中进行
    protected void showTimeCount(TextView view,int timeCount) {
        if (mTimeRunnable != null) {
            return;
        }
        mTimeCount = timeCount;
        if (null != view) {
            view.setText(getShowTime(++mTimeCount));
        }
        mTimeRunnable = new Runnable() {
            @Override
            public void run() {
                mTimeCount++;
                Status.mBeginTime = mTimeCount;
                if (null != view) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isDestroyed()) {
                                view.setText(getShowTime(mTimeCount));
                            }
                        }
                    });
                }
                mTimeHandler.postDelayed(mTimeRunnable, 1000);
            }
        };
        mTimeHandler.postDelayed(mTimeRunnable, 1000);
    }

    /**
     * 重新进入后台activity
     */
    public void restartActivity(){}

    private void initListener() {
        maximize.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                restartActivity();
            }
        });
    }

    @Override
    public void onUserEnter(String userId) {
        super.onUserEnter(userId);
        if (!Status.mIsShowFloatWindow) {
            return;
        }
    }

    @Override
    public void onCallEnd() {
        super.onCallEnd();
        //通话结束,停止悬浮窗显示
        if (Status.mIsShowFloatWindow) {
            FloatWindowService.stopService(mContext);
            finish();
        }
    }

    @Override
    public void onTryToReconnect() {

    }

}
