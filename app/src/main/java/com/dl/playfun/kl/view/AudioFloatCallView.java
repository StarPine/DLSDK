package com.dl.playfun.kl.view;


import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.GsonUtils;
import com.bumptech.glide.Glide;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.Injection;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.AudioCallingBarrageEntity;
import com.dl.playfun.entity.CallingStatusEntity;
import com.dl.playfun.entity.RestartActivityEntity;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.widget.image.CircleImageView;
import com.tencent.liteav.trtccalling.TUICalling;
import com.tencent.liteav.trtccalling.ui.base.BaseTUICallView;
import com.tencent.liteav.trtccalling.ui.floatwindow.FloatWindowService;
import com.tencent.qcloud.tuicore.Status;

import java.util.ArrayList;
import java.util.List;

import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.utils.RxUtils;

public class AudioFloatCallView extends BaseTUICallView {
    private static final String TAG = "AudioFloatCallView";
    private ImageView maximize;
    private CircleImageView ivAvatar;
    private TextView mTextViewTimeCount;
    private boolean isRestart = false;
    private Integer roomId = 0;
    private ArrayList<AudioCallingBarrageEntity> audioCallChatingItemViewModelList;


    public AudioFloatCallView(Context context, TUICalling.Role role, TUICalling.Type type, String[] userIDs,
                              String sponsorID, String groupID, boolean isFromGroup, String avatar, int timeCount, Integer roomId, ArrayList<AudioCallingBarrageEntity> audioCallChatingItemViewModelList) {
        super(context, role, type, userIDs, sponsorID, groupID, isFromGroup);
        initData(avatar, timeCount, roomId,audioCallChatingItemViewModelList);
    }

    private void initData(String avatar, int timeCount, Integer roomId, ArrayList<AudioCallingBarrageEntity> audioCallChatingItemViewModelList) {
        Glide.with(AppContext.instance())
                .load(StringUtil.getFullImageUrl(avatar))
                .error(R.drawable.default_avatar) //异常时候显示的图片
                .placeholder(R.drawable.default_avatar) //加载成功前显示的图片
                .fallback(R.drawable.default_avatar) //url为空的时候,显示的图片
                .into(ivAvatar);
        this.roomId = roomId;
        this.audioCallChatingItemViewModelList = audioCallChatingItemViewModelList;
        showTimeCount(mTextViewTimeCount, timeCount);
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
    protected void showTimeCount(TextView view, int timeCount) {
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
                if (mTimeCount %10 ==0){
                    getRoomStatus(roomId);
                }
                Status.mBeginTime = mTimeCount;
                if (null != view) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (Status.mIsShowFloatWindow) {
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

    private void initListener() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRestart) {
                    isRestart = true;
                    Intent intent = new Intent(mContext, AudioCallChatingActivity.class);
                    intent.putExtra("fromUserId", mSponsorID);
                    intent.putExtra("toUserId", mUserIDs[0]);
                    intent.putExtra("mRole", mRole);
                    intent.putExtra("roomId", roomId);
                    intent.putExtra("timeCount", ++mTimeCount);
                    intent.putExtra("isRestart", isRestart);
                    intent.putExtra("audioCallingBarrage", GsonUtils.toJson(audioCallChatingItemViewModelList));
                    if (isBackground(mContext)) {
                        mContext.startActivity(intent);
                    } else {
                        RxBus.getDefault().post(new RestartActivityEntity(intent));
                    }
                }

            }
        });
    }

    /**
     * 判断程序是否在后台
     *
     * @param context
     * @return
     */
    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    //获取房间状态
    public void getRoomStatus(Integer roomId) {
        Injection.provideDemoRepository().getRoomStatus(roomId)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<CallingStatusEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CallingStatusEntity> response) {
                        CallingStatusEntity data = response.getData();
                        Integer roomStatus = data.getRoomStatus();
                        if (roomStatus != null && roomStatus != 101) {
                            onCallEnd();
                        }
                    }
                });
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

}
