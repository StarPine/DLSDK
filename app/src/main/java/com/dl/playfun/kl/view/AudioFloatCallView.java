package com.dl.playfun.kl.view;


import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.StringUtils;
import com.bumptech.glide.Glide;
import com.dl.lib.util.log.MPTimber;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.Injection;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.AudioCallingBarrageEntity;
import com.dl.playfun.entity.CallingInfoEntity;
import com.dl.playfun.entity.CallingStatusEntity;
import com.dl.playfun.entity.RestartActivityEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.widget.image.CircleImageView;
import com.dl.rtc.calling.DLRTCFloatWindowService;
import com.dl.rtc.calling.base.DLRTCCalling;
import com.dl.rtc.calling.ui.BaseDLRTCCallView;
import com.google.gson.Gson;
import com.tencent.custom.GiftEntity;
import com.tencent.imsdk.v2.V2TIMAdvancedMsgListener;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMMessageReceipt;
import com.tencent.qcloud.tuicore.Status;
import com.tencent.qcloud.tuikit.tuichat.bean.message.TUIMessageBean;
import com.tencent.qcloud.tuikit.tuichat.util.ChatMessageBuilder;
import com.tencent.trtc.TRTCCloudDef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.utils.RxUtils;

public class AudioFloatCallView extends BaseDLRTCCallView {
    private static final String TAG = "AudioFloatCallView";
    private CircleImageView ivAvatar;
    private TextView mTextViewTimeCount;
    private boolean isRestart = false;
    private Integer roomId = 0;
    private CallingInfoEntity.FromUserProfile otherUserProfile;
    private ArrayList<AudioCallingBarrageEntity> audioBarrageList;

    public int mTimeCount = 0;


    public AudioFloatCallView(Context context, DLRTCCalling.Role role, DLRTCCalling.Type type, String[] userIDs,
                              String sponsorID, String groupID, boolean isFromGroup,
                              CallingInfoEntity.FromUserProfile otherUserProfile, int timeCount,
                              Integer roomId, ArrayList<AudioCallingBarrageEntity> audioBarrageList) {
        super(context, role, type, userIDs, sponsorID, groupID, isFromGroup);
        initData(otherUserProfile, timeCount, roomId,audioBarrageList);
    }

    private void initData(CallingInfoEntity.FromUserProfile otherUserProfile, int timeCount, Integer roomId, ArrayList<AudioCallingBarrageEntity> audioBarrageList) {
        Glide.with(AppContext.instance())
                .asDrawable()
                .load(StringUtil.getFullImageUrl(otherUserProfile.getAvatar()))
                .error(R.drawable.default_avatar) //异常时候显示的图片
                .placeholder(R.drawable.default_avatar) //加载成功前显示的图片
                .fallback(R.drawable.default_avatar) //url为空的时候,显示的图片
                .into(ivAvatar);
        this.roomId = roomId;
        this.otherUserProfile = otherUserProfile;
        this.audioBarrageList = audioBarrageList;
        showTimeCount(mTextViewTimeCount, timeCount);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initIMListener();
        initListener();
        showFloatWindow();
    }

    @Override
    protected void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.audio_floatwindow_layout, this);
        ivAvatar = findViewById(R.id.iv_avatar);
        mTextViewTimeCount = findViewById(R.id.tv_time);
    }

    @Override
    protected void timeCountListener(int times) {
        mTimeCount = times;
        if(mTextViewTimeCount!=null){
            mTextViewTimeCount.post(()->mTextViewTimeCount.setText(getShowTime(mTimeCount)));
        }
    }

    //更新显示
    private void showFloatWindow() {
        Status.mIsShowFloatWindow = true;
    }

    //通话时长,注意UI更新需要在主线程中进行
    protected void showTimeCount(TextView view, int timeCount) {
        super.showTimeCount(view,timeCount);
    }

    //监听IM消息
    private void initIMListener() {
        V2TIMManager.getMessageManager().addAdvancedMsgListener(v2TIMAdvancedMsgListener);
    }
    V2TIMAdvancedMsgListener v2TIMAdvancedMsgListener = new V2TIMAdvancedMsgListener() {
        @Override
        public void onRecvNewMessage(V2TIMMessage msg) {//新消息提醒
            try {
                if (msg != null && otherUserProfile != null) {
                    TUIMessageBean info = ChatMessageBuilder.buildMessage(msg);
                    if (info != null) {
                        if (info.getV2TIMMessage().getSender().equals(otherUserProfile.getImId())) {
                            String text = String.valueOf(info.getExtra());
                            if (StringUtil.isJSON2(text) && text.contains("type")) {//做自定义通知判断
                                Map<String, Object> map_data = new Gson().fromJson(text, Map.class);
                                //礼物消息
                                if (map_data != null
                                        && map_data.get("type") != null
                                        && map_data.get("type").equals("message_gift")
                                        && map_data.get("is_accost") == null) {
                                    GiftEntity giftEntity = new Gson().fromJson(String.valueOf(map_data.get("data")), GiftEntity.class);
                                    //显示礼物弹幕
                                    showGiftBarrage(giftEntity);
                                    //礼物收益提示
                                    giftIncome(giftEntity);
                                }
                            }
                        }
                    }
                }
            }catch (Exception e){

            }

        }

        @Override
        public void onRecvC2CReadReceipt(List<V2TIMMessageReceipt> receiptList) {
            super.onRecvC2CReadReceipt(receiptList);
        }

        @Override
        public void onRecvMessageRevoked(String msgID) {
            super.onRecvMessageRevoked(msgID);
        }

        @Override
        public void onRecvMessageModified(V2TIMMessage msg) {
            super.onRecvMessageModified(msg);
        }
    };

    private void initListener() {
        setOnClickListener(v -> {
            if (!isRestart) {
                isRestart = true;
                Intent intent = new Intent(getContext(), AudioCallChatingActivity.class);
                intent.putExtra("fromUserId", mSponsorID);
                intent.putExtra("toUserId", mUserIDs[0]);
                intent.putExtra("mRole", mRole);
                intent.putExtra("roomId", roomId);
                intent.putExtra("timeCount", ++mTimeCount);
                intent.putExtra("isRestart", isRestart);
                intent.putExtra("audioCallingBarrage", GsonUtils.toJson(audioBarrageList));
                if (isBackground(getContext())) {
                    getContext().startActivity(intent);
                } else {
                    RxBus.getDefault().post(new RestartActivityEntity(intent));
                }
            }
        });
    }


    /**
     * 显示礼物弹幕
     *
     * @param giftEntity
     */
    private void showGiftBarrage(GiftEntity giftEntity) {
        String sexText = isMale() ? StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt3) : StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt2);
        String messageText = otherUserProfile.getNickname() + " " + StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt1) + " " + sexText + " " + giftEntity.getTitle() + "x" + giftEntity.getAmount();
        AudioCallingBarrageEntity barrageEntity = new AudioCallingBarrageEntity(messageText ,giftEntity.getImgPath(),true);
        audioBarrageList.add(barrageEntity);
    }

    private boolean isMale(){
        return ConfigManager.getInstance().isMale();
    }

    /**
     * 礼物收益提示
     *
     * @param giftEntity
     */
    private void giftIncome(GiftEntity giftEntity) {
        double total = giftEntity.getAmount().intValue() * giftEntity.getProfitTwd().doubleValue();
        String itemMessage = String.format(StringUtils.getString(R.string.profit), String.format("%.2f", total));
        AudioCallingBarrageEntity barrageEntity = new AudioCallingBarrageEntity(itemMessage ,"",false);
        audioBarrageList.add(barrageEntity);
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
                return appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
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
            DLRTCFloatWindowService.stopService(getContext());
            finish();
        }
        V2TIMManager.getMessageManager().removeAdvancedMsgListener(v2TIMAdvancedMsgListener);
    }

    @Override
    public void onNetworkQuality(@Nullable TRTCCloudDef.TRTCQuality localQuality, @Nullable ArrayList<TRTCCloudDef.TRTCQuality> remoteQuality) {

    }
}
