package com.dl.playfun.kl.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.Group;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.dl.playfun.R;
import com.dl.playfun.entity.CallingVideoTryToReconnectEvent;
import com.dl.playfun.event.CallVideoUserEnterEvent;
import com.dl.playfun.utils.LogUtils;
import com.dl.playfun.widget.dialog.TraceDialog;
import com.dl.rtc.calling.base.DLRTCCalling;
import com.dl.rtc.calling.manager.DLRTCCallingInfoManager;
import com.dl.rtc.calling.manager.DLRTCStartManager;
import com.dl.rtc.calling.manager.DLRTCVideoManager;
import com.dl.rtc.calling.model.DLRTCCallingConstants;
import com.dl.rtc.calling.model.bean.DLRTCUserModel;
import com.dl.rtc.calling.ui.BaseDLRTCCallView;
import com.dl.rtc.calling.ui.videolayout.DLRTCVideoLayout;
import com.dl.rtc.calling.ui.videolayout.DLRTCVideoLayoutManager;
import com.dl.rtc.calling.ui.videolayout.VideoLayoutFactory;
import com.tencent.trtc.TRTCCloudDef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import me.goldze.mvvmhabit.bus.RxBus;

public class JMTUICallVideoView extends BaseDLRTCCallView {

    private static final int MAX_SHOW_INVITING_USER = 4;
    private static final String TAG = "TUICallVideoView";
    /**
     * 拨号相关成员变量
     */
    private final List<DLRTCUserModel> mCallUserInfoList = new ArrayList<>(); // 呼叫方
    private final Map<String, DLRTCUserModel> mCallUserModelMap = new HashMap<>();
    private final boolean mIsMuteMic = false;
    private final boolean mIsCameraOpen = true;
    private final boolean mIsAudioMode = false;
    private final boolean mIsCalledClick = false;  // 被叫方点击转换语音
    private DLRTCVideoLayoutManager mLayoutManagerTrtc;
    private Group mInvitingGroup;
    private LinearLayout mImgContainerLl;
    private TextView mTimeTv;
    private View mShadeSponsor;
    private Runnable mTimeRunnable;
    private int mTimeCount;
    private Handler mTimeHandler;
    private HandlerThread mTimeHandlerThread;
    private DLRTCUserModel mSponsorUserInfo;                      // 被叫方
    private List<DLRTCUserModel> mOtherInvitingUserInfoList;
    private boolean mIsHandsFree = true;
    private boolean mIsFrontCamera = true;
    private boolean isStartRemoteView = false;
    private boolean isChatting = false;  // 是否已经接通
    private int roomId = 0;
    //断网总时间
    int disconnectTime = 0;

    public JMTUICallVideoView(Context context, DLRTCCalling.Role role, String[] userIDs, String sponsorID, String groupID, boolean isFromGroup, VideoLayoutFactory videoLayoutFactory) {
        super(context, role, DLRTCCalling.Type.VIDEO, userIDs, sponsorID, groupID, isFromGroup);
        mLayoutManagerTrtc.initVideoFactory(videoLayoutFactory);
    }

    public JMTUICallVideoView(Context context, DLRTCCalling.Role role, String[] userIDs, String sponsorID, String groupID, boolean isFromGroup, int roomId,VideoLayoutFactory videoLayoutFactory) {
        this(context, role, userIDs, sponsorID, groupID, isFromGroup,videoLayoutFactory);
        this.roomId = roomId;

    }

    @Override
    protected void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.jm_trtccalling_videocall_activity_call_main, this);
        mLayoutManagerTrtc = findViewById(R.id.trtc_layout_manager);
        mInvitingGroup = findViewById(R.id.group_inviting);
        mImgContainerLl = findViewById(R.id.ll_img_container);
        mTimeTv = findViewById(R.id.tv_time);
        mShadeSponsor = findViewById(R.id.shade_sponsor);
//        mSwitchCameraImg = findViewById(R.id.switch_camera);

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initData();
        initListener();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopTimeCount();
        mTimeHandlerThread.quit();
    }

    private void initData() {
        // 初始化成员变量
        mTimeHandlerThread = new HandlerThread("time-count-thread");
        mTimeHandlerThread.start();
        mTimeHandler = new Handler(mTimeHandlerThread.getLooper());
        try {
            PermissionUtils.permission(PermissionConstants.CAMERA, PermissionConstants.MICROPHONE).callback(new PermissionUtils.FullCallback() {
                @Override
                public void onGranted(List<String> permissionsGranted) {
                    initViewData();
                }

                @Override
                public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                    if(isDestroyed()){
                        return;
                    }
                    TraceDialog.getInstance(getContext())
                            .setCannelOnclick(dialog -> {
                                DLRTCVideoManager.getInstance().reject();
                                ToastUtils.showShort(R.string.trtccalling_tips_start_camera_audio);
                                finish();
                            })
                            .setConfirmOnlick(new TraceDialog.ConfirmOnclick() {
                                @Override
                                public void confirm(Dialog dialog) {
                                    PermissionUtils.permission(PermissionConstants.CAMERA, PermissionConstants.MICROPHONE).callback(new PermissionUtils.FullCallback() {
                                        @Override
                                        public void onGranted(List<String> permissionsGranted) {
                                            initViewData();
                                        }

                                        @Override
                                        public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                                            DLRTCVideoManager.getInstance().reject();
                                            ToastUtils.showShort(R.string.trtccalling_tips_start_camera_audio);
                                            finish();
                                        }
                                    }).request();
                                }
                            }).AlertCallAudioPermissions().show();
                }
            }).request();
        } catch (Exception e) {

        }

    }

    private void initViewData() {
        if (mRole == DLRTCCalling.Role.CALLED) {
            // 作为被叫
            if (!TextUtils.isEmpty(mSponsorID)) {
                mSponsorUserInfo = new DLRTCUserModel();
                mSponsorUserInfo.setUserId(mSponsorID);
               // mSponsorUserInfo.setUserAvatar(AvatarConstant.USER_AVATAR_ARRAY[new Random().nextInt(AvatarConstant.USER_AVATAR_ARRAY.length)]);
            }
            showWaitingResponseView();
        } else {
            // 主叫方
            if (mUserIDs != null) {
                for (String userId : mUserIDs) {
                    DLRTCUserModel userModel = new DLRTCUserModel();
                    userModel.setUserId(userId);
                    mCallUserInfoList.add(userModel);
                    mCallUserModelMap.put(userModel.getUserId(), userModel);
                }
                showInvitingView();
                startInviting();
            }
        }
    }

    private void initListener() {

//        mSwitchCameraImg.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!mIsCameraOpen) {
//                    ToastUtils.showShort(R.string.trtccalling_switch_camera_hint);
//                    return;
//                }
//                mIsFrontCamera = !mIsFrontCamera;
//                mTRTCCalling.switchCamera(mIsFrontCamera);
//                mSwitchCameraImg.setActivated(mIsFrontCamera);
//                ToastUtils.showLong(R.string.trtccalling_toast_switch_camera);
//            }
//        });
    }

    private void startInviting() {
        final List<String> list = new ArrayList<>();
        for (DLRTCUserModel userInfo : mCallUserInfoList) {
            list.add(userInfo.getUserId());
        }
        PermissionUtils.permission(PermissionConstants.CAMERA, PermissionConstants.MICROPHONE).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(List<String> permissionsGranted) {
                DLRTCVideoLayout layout = mLayoutManagerTrtc.findCloudView(mSelfModel.getUserId());
                if (null != layout) {
                    DLRTCVideoManager.getInstance().openCamera(true, layout.getVideoView());
                }
                //DLRTCVideoManager.getInstance().groupCall(roomId, list, DLRTCCallingConstants.TYPE_VIDEO_CALL, "");
            }

            @Override
            public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                ToastUtils.showShort(R.string.trtccalling_tips_start_camera_audio);
                finish();
            }
        }).request();
    }

    @Override
    public void onError(int code, String msg) {
        //发生了错误，报错并退出该页面
        ToastUtils.showLong(getContext().getString(R.string.trtccalling_toast_call_error_msg, code, msg));
        stopCameraAndFinish();
    }

    @Override
    public void onInvited(String sponsor, List<String> userIdList, boolean isFromGroup, int callType) {
    }

    @Override
    public void onGroupCallInviteeListUpdate(List<String> userIdList) {
    }

    @Override
    public void onUserEnter(final String userId) {
        runOnUiThread(() -> {
            //用户进入房间
            showCallingView();
            //发送订阅事件通知有人加入了视频聊天房
            RxBus.getDefault().post(new CallVideoUserEnterEvent(userId));
            DLRTCUserModel userModel = new DLRTCUserModel();
            userModel.setUserId(userId);
            mCallUserModelMap.put(userId, userModel);
            DLRTCVideoLayout videoLayout = showVideoView(userModel);
            if (!isStartRemoteView){//没有拉流时，重新拉流
                onUserVideoAvailable(userId,true);
            }
            loadUserInfo(userModel, videoLayout);
        });
    }

    //查询昵称和头像
    private void loadUserInfo(final DLRTCUserModel userModel, DLRTCVideoLayout layout) {
        if (null == userModel || null == layout) {
            return;
        }
        DLRTCCallingInfoManager.Companion.getInstance().getUserInfoByUserId(userModel.getUserId(), new DLRTCCallingInfoManager.Companion.UserCallback() {
            @Override
            public void onSuccess(DLRTCUserModel model) {
                userModel.setUserName(model.getUserName());
                userModel.setUserAvatar(model.getUserAvatar());
                runOnUiThread(() -> {
                    if (isDestroyed()) {
                        return;
                    }
                    layout.setUserName(userModel.getUserName());
//                    ImageLoader.loadImage(mContext, layout.getHeadImg(), userModel.getUserAvatar(),
//                            R.drawable.trtccalling_ic_avatar);
                });
            }

            @Override
            public void onFailed(int code, String msg) {
                ToastUtils.showLong(getContext().getString(R.string.trtccalling_toast_search_fail, msg));
            }
        });
    }

    @Override
    public void onUserLeave(final String userId) {
        Log.i("JM_trtc", "onUserLeave: ");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //1. 回收界面元素
                mLayoutManagerTrtc.recyclerCloudViewView(userId);
                //2. 删除用户model
                DLRTCUserModel userInfo = mCallUserModelMap.remove(userId);
                if (userInfo != null) {
                    mCallUserInfoList.remove(userInfo);
                }
            }
        });
    }

    @Override
    public void onReject(final String userId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCallUserModelMap.containsKey(userId)) {
                    // 进入拒绝环节
                    //1. 回收界面元素
                    mLayoutManagerTrtc.recyclerCloudViewView(userId);
                    //2. 删除用户model
                    DLRTCUserModel userInfo = mCallUserModelMap.remove(userId);
                    if (userInfo != null) {
                        mCallUserInfoList.remove(userInfo);
                        ToastUtils.showLong(getContext().getString(R.string.trtccalling_toast_user_reject_call, userInfo.getUserName()));
                    }
                }
            }
        });
    }

    @Override
    public void onNoResp(final String userId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCallUserModelMap.containsKey(userId)) {
                    // 进入无响应环节
                    //1. 回收界面元素
                    mLayoutManagerTrtc.recyclerCloudViewView(userId);
                    //2. 删除用户model
                    DLRTCUserModel userInfo = mCallUserModelMap.remove(userId);
                    if (userInfo != null) {
                        mCallUserInfoList.remove(userInfo);
                        ToastUtils.showLong(getContext().getString(R.string.trtccalling_toast_user_not_response, userInfo.getUserName()));
                    }
                }
            }
        });
    }

    @Override
    public void onLineBusy(String userId) {
        if (mCallUserModelMap.containsKey(userId)) {
            // 进入无响应环节
            //1. 回收界面元素
            mLayoutManagerTrtc.recyclerCloudViewView(userId);
            //2. 删除用户model
            DLRTCUserModel userInfo = mCallUserModelMap.remove(userId);
            if (userInfo != null) {
                mCallUserInfoList.remove(userInfo);
                ToastUtils.showLong(getContext().getString(R.string.trtccalling_toast_user_busy, userInfo.getUserName()));
            }
        }
    }

    @Override
    public void onCallingCancel() {
        if (mSponsorUserInfo != null) {
            ToastUtils.showLong(getContext().getString(R.string.trtccalling_toast_user_cancel_call, mSponsorUserInfo.getUserName()));
        }
        stopCameraAndFinish();
    }

    @Override
    public void onCallingTimeout() {
        if (mSponsorUserInfo != null) {
            ToastUtils.showLong(getContext().getString(R.string.trtccalling_toast_user_timeout, mSponsorUserInfo.getUserName()));
        }
        stopCameraAndFinish();
    }

    @Override
    public void onCallEnd() {
        Log.i("JM_trtc", "onCallEnd: ");
        if (mSponsorUserInfo != null) {
            ToastUtils.showLong(getContext().getString(R.string.trtccalling_toast_user_end, mSponsorUserInfo.getUserName()));
        }
        stopCameraAndFinish();
    }

    @Override
    public void onUserVideoAvailable(final String userId, final boolean isVideoAvailable) {
        //有用户的视频开启了
        DLRTCVideoLayout layout = mLayoutManagerTrtc.findCloudView(userId);
        if (layout != null) {
            layout.setVideoAvailable(isVideoAvailable);
            if (isVideoAvailable) {
                isStartRemoteView = true;
                DLRTCVideoManager.getInstance().startRemoteView(userId, layout.getVideoView());
            } else {
                isStartRemoteView = false;
                DLRTCVideoManager.getInstance().stopRemoteView(userId);
            }
        } else {

        }
    }

    @Override
    public void onUserAudioAvailable(String userId, boolean isVideoAvailable) {

    }

    @Override
    public void onUserVoiceVolume(Map<String, Integer> volumeMap) {
        for (Map.Entry<String, Integer> entry : volumeMap.entrySet()) {
            String userId = entry.getKey();
            DLRTCVideoLayout layout = mLayoutManagerTrtc.findCloudView(userId);
            if (layout != null) {
                layout.setAudioVolumeProgress(entry.getValue());
            }
        }
    }

    @Override
    public void onNetworkQuality(TRTCCloudDef.TRTCQuality localQuality, ArrayList<TRTCCloudDef.TRTCQuality> remoteQuality) {
        //两秒回调一次
        if (localQuality.quality == 6 || remoteQuality.isEmpty()) {
            disconnectTime++;
            if (disconnectTime > 30 || (remoteQuality.isEmpty() && disconnectTime >15)) {
                hangup();
            }
        }else {
            disconnectTime  = 0;
        }
        updateNetworkQuality(localQuality, remoteQuality);
    }

    @Override
    public void onTryToReconnect() {
        LogUtils.i("onTryToReconnect: vidoe");
        RxBus.getDefault().post(new CallingVideoTryToReconnectEvent());
    }

    @Override
    public void onSwitchToAudio(boolean success, String message) {
//

    }

    private void enableHandsFree(boolean enable) {
        mIsHandsFree = enable;
        DLRTCVideoManager.getInstance().audioRoute(mIsHandsFree);
    }

    /**
     * 被叫方
     * 等待接听界面
     */
    public void showWaitingResponseView() {
        //1. 展示自己的画面
        mLayoutManagerTrtc.setMySelfUserId(mSelfModel.getUserId());
        final DLRTCVideoLayout videoLayout = addUserToManager(mSelfModel);
        if (videoLayout == null) {
            return;
        }
        videoLayout.setVideoAvailable(true);
        DLRTCVideoManager.getInstance().openCamera(true, videoLayout.getVideoView());
        //2. 展示对方的头像和蒙层
        visibleSponsorGroup(true);
        DLRTCCallingInfoManager.Companion.getInstance().getUserInfoByUserId(mSponsorUserInfo.getUserId(), new DLRTCCallingInfoManager.Companion.UserCallback() {
            @Override
            public void onSuccess(DLRTCUserModel model) {
                mSponsorUserInfo.setUserName(model.getUserName());
                mSponsorUserInfo.setUserAvatar(model.getUserAvatar());
            }

            @Override
            public void onFailed(int code, String msg) {
                ToastUtils.showLong(getContext().getString(R.string.trtccalling_toast_search_fail, msg));
            }
        });
        //3. 展示电话对应界面
        //3. 设置对应的listener

        //4. 展示其他用户界面
        showOtherInvitingUserView();
    }

    private void visibleSponsorGroup(boolean visible) {
        if (visible) {
            mShadeSponsor.setVisibility(View.VISIBLE);
        } else {
            mShadeSponsor.setVisibility(View.GONE);
        }
    }

    /**
     * 主叫方调用
     * 展示邀请列表
     */
    public void showInvitingView() {
        //1. 展示自己的界面
        mLayoutManagerTrtc.setMySelfUserId(mSelfModel.getUserId());
        final DLRTCVideoLayout videoLayout = addUserToManager(mSelfModel);
        if (videoLayout == null) {
            return;
        }
        videoLayout.setVideoAvailable(true);

        //2. 设置底部栏
//        mSwitchCameraImg.setVisibility(View.GONE);
        //3. 隐藏中间他们也在界面
        hideOtherInvitingUserView();
        //4. sponsor画面也隐藏
        visibleSponsorGroup(true);
        mSponsorUserInfo = mCallUserInfoList.get(0);
        DLRTCCallingInfoManager.Companion.getInstance().getUserInfoByUserId(mSponsorUserInfo.getUserId(), new DLRTCCallingInfoManager.Companion.UserCallback() {
            @Override
            public void onSuccess(DLRTCUserModel model) {
                mSponsorUserInfo.setUserName(model.getUserName());
                mSponsorUserInfo.setUserAvatar(model.getUserAvatar());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isDestroyed()) {
                            return;
                        }
                    }
                });
            }

            @Override
            public void onFailed(int code, String msg) {
                ToastUtils.showLong(getContext().getString(R.string.trtccalling_toast_search_fail, msg));
            }
        });
    }

    /**
     * 展示通话中的界面
     */
    public void showCallingView() {
        super.showCallingView();
        //1. 蒙版消失
        visibleSponsorGroup(false);
        //2. 底部状态栏
//        mSwitchCameraImg.setVisibility(mIsAudioMode ? View.GONE : View.VISIBLE);
        showTimeCount();
        hideOtherInvitingUserView();
    }

    private void showTimeCount() {
        if (mTimeRunnable != null) {
            return;
        }
        mTimeCount = 0;
        mTimeTv.setText(getShowTime(mTimeCount));
        if (mTimeRunnable == null) {
            mTimeRunnable = new Runnable() {
                @Override
                public void run() {
                    mTimeCount++;
                    if (mTimeTv != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!isDestroyed()) {
                                    mTimeTv.setText(getShowTime(mTimeCount));
                                }
                            }
                        });
                    }
                    mTimeHandler.postDelayed(mTimeRunnable, 1000);
                }
            };
        }
        mTimeHandler.postDelayed(mTimeRunnable, 1000);
    }

    private void stopTimeCount() {
        mTimeHandler.removeCallbacks(mTimeRunnable);
        mTimeRunnable = null;
    }

//    private String getShowTime(int count) {
//        return mContext.getString(R.string.trtccalling_called_time_format, count / 60, count % 60);
//    }

    private void showOtherInvitingUserView() {
        if (mOtherInvitingUserInfoList == null || mOtherInvitingUserInfoList.size() == 0) {
            return;
        }
        mInvitingGroup.setVisibility(View.VISIBLE);
        int squareWidth = getResources().getDimensionPixelOffset(R.dimen.dlrtccalling_small_image_size);
        int leftMargin = getResources().getDimensionPixelOffset(R.dimen.dlrtccalling_small_image_left_margin);
        for (int index = 0; index < mOtherInvitingUserInfoList.size() && index < MAX_SHOW_INVITING_USER; index++) {
            DLRTCUserModel userInfo = mOtherInvitingUserInfoList.get(index);
            ImageView imageView = new ImageView(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(squareWidth, squareWidth);
            if (index != 0) {
                layoutParams.leftMargin = leftMargin;
            }
            imageView.setLayoutParams(layoutParams);
            //ImageLoader.loadImage(getContext(), imageView, userInfo.getUserAvatar(), R.drawable.dlrtccalling_ic_avatar);
            mImgContainerLl.addView(imageView);
        }
    }

    private void hideOtherInvitingUserView() {
        mInvitingGroup.setVisibility(View.GONE);
    }

    private int dp2px(float dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    // 自己的video变成小窗口， 对方video变成全屏显示
    private DLRTCVideoLayout showVideoView(final DLRTCUserModel userInfo) {
        isChatting = true;
        // 添加到 TRTCVideoLayoutManager, 返回的是对方的layout
        DLRTCVideoLayout videoLayout = addUserToManager(userInfo);
        if (videoLayout == null) {
            return null;
        }
        // kl 添加以下代码，一对一视频聊天的
        DLRTCVideoLayout myLayout = mLayoutManagerTrtc.findCloudView(mSelfModel.getUserId());
        RelativeLayout.LayoutParams oriParams = (RelativeLayout.LayoutParams) myLayout.getLayoutParams();
//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) myLayout.getLayoutParams();
        int height = oriParams.height;
        int width = oriParams.width;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        params.rightMargin = dp2px(17);
        params.topMargin = dp2px(87);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        myLayout.setLayoutParams(params);
        // -----------------
        videoLayout.setVideoAvailable(!mIsAudioMode);
        videoLayout.setRemoteIconAvailable(mIsAudioMode);
        return videoLayout;
    }

    private DLRTCVideoLayout addUserToManager(DLRTCUserModel userInfo) {
        return mLayoutManagerTrtc.allocCloudVideoView(userInfo.getUserId());
    }

    private void stopCameraAndFinish() {
        DLRTCVideoManager.getInstance().closeCamera();
        finish();
    }

    // ===================kl add fellow=================
    // 挂断电话， 绑定给close按钮的，任何时候可以调用
    public void hangup() {
        if (mRole == DLRTCCalling.Role.CALLED && !isChatting) {
            DLRTCVideoManager.getInstance().reject();
        } else {
            DLRTCVideoManager.getInstance().hangup();
        }
        stopCameraAndFinish();
        // 主叫还没接听的时候主动挂断
        if (mRole == DLRTCCalling.Role.CALL && !isChatting) {
            //mEventHandler.sendEmptyMessage(EventHandler.EVENT_TYPE_ACTIVE_HANGUP);
        }
    }

    public void setHandsFree(boolean mIsHandsFree){
        DLRTCVideoManager.getInstance().muteLocalAudio(mIsHandsFree);
    }
    public void setMicMute(boolean isMicMute){
        DLRTCVideoManager.getInstance().setMicMute(isMicMute);
    }

    // 接听电话， 给接听按钮用的
    public void acceptCall() {
        DLRTCVideoManager.getInstance().accept();
        showCallingView();
    }

    public void switchCamera() {
        if (!mIsCameraOpen) {
            ToastUtils.showShort(R.string.trtccalling_switch_camera_hint);
            return;
        }
        mIsFrontCamera = !mIsFrontCamera;
        DLRTCVideoManager.getInstance().switchCamera(mIsFrontCamera);
//        mSwitchCameraImg.setActivated(mIsFrontCamera);
        ToastUtils.showLong(R.string.trtccalling_toast_switch_camera);
    }

    public void switchVideoView() {
        mLayoutManagerTrtc.switchVideoView();
    }

    /**
     * @return void
     * @Desc TODO(是否开启自动增益补偿功能, 可以自动调麦克风的收音量到一定的音量水平)
     * @author 彭石林
     * @parame [openAGC]
     * @Date 2022/2/14
     */
    public void enableAGC(boolean openAGC) {
        DLRTCStartManager.Companion.getInstance().enableAGC(openAGC);
    }

    /**
     * @return void
     * @Desc TODO(回声消除器 ， 可以消除各种延迟的回声)
     * @author 彭石林
     * @parame [openAEC]
     * @Date 2022/2/14
     */
    public void enableAEC(boolean openAEC) {
        DLRTCStartManager.Companion.getInstance().enableAEC(openAEC);
    }

    /**
     * @return void
     * @Desc TODO(背景噪音抑制功能 ， 可探测出背景固定频率的杂音并消除背景噪音)
     * @author 彭石林
     * @parame [openANS]
     * @Date 2022/2/14
     */
    public void enableANS(boolean openANS) {
        DLRTCStartManager.Companion.getInstance().enableANS(openANS);
    }

}
