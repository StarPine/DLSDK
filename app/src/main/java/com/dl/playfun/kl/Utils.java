package com.dl.playfun.kl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.dl.playfun.app.AppContext;
import com.dl.rtc.calling.DLRTCCallService;
import com.dl.rtc.calling.base.DLRTCCalling;
import com.dl.rtc.calling.manager.DLRTCInterceptorCall;
import com.dl.rtc.calling.manager.DLRTCStartManager;
import com.dl.rtc.calling.manager.DLRTCStartShowUIManager;
import com.dl.rtc.calling.manager.DLRTCStartUiClosuer;
import com.dl.rtc.calling.model.DLRTCCallingConstants;
import com.dl.rtc.calling.model.DLRTCDataMessageType;
import com.tencent.qcloud.tuicore.TUILogin;

public class Utils {
    protected static final Handler mMainHandler = new Handler(Looper.getMainLooper());

    /**
     * 开始呼叫某人
     */
    public static void startCallSomeone(int type, String toUserId, int roomId,boolean startView) {
        DLRTCDataMessageType.DLInviteRTCType dlInviteRTCType = null;
        if (type == 1) {
            dlInviteRTCType = DLRTCDataMessageType.DLInviteRTCType.dl_rtc_audio;
        } else if (type == 2) {
            dlInviteRTCType = DLRTCDataMessageType.DLInviteRTCType.dl_rtc_video;
        }
        inviteUserRTC(toUserId, dlInviteRTCType, roomId, 30,startView,null);
    }

    public static void inviteUserRTC(String inviteUser, DLRTCDataMessageType.DLInviteRTCType inviteType, int roomId,Integer dLInviteTimeout,boolean lanuchView,String inviteExtJson){
        DLRTCStartShowUIManager.Companion.getInstance().inviteUserRTC(inviteUser, inviteType, roomId, dLInviteTimeout, inviteExtJson, (_success, _errorCode, _errorMsg) -> {
            if(_success){
                Context mContext = DLRTCStartManager.Companion.getInstance().getMContext();
                if(lanuchView){
                    // 首次拨打电话，生成id
                    // 单聊发送C2C消息; 用C2C实现的多人通话,需要保存每个userId对应的callId
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    if (inviteType == DLRTCDataMessageType.DLInviteRTCType.dl_rtc_audio) {
                        intent.setComponent(new ComponentName(mContext.getApplicationContext(), DLRTCInterceptorCall.Companion.getInstance().getAudioCallActivity()));
                    } else {
                        intent.setComponent(new ComponentName(mContext.getApplicationContext(), DLRTCInterceptorCall.Companion.getInstance().getVideoCallActivity()));
                    }
                    intent.putExtra(DLRTCCallingConstants.DLRTCInviteUserID, TUILogin.getLoginUser());
                    intent.putExtra(DLRTCCallingConstants.PARAM_NAME_ROLE, DLRTCCalling.Role.CALL);
                    intent.putExtra(DLRTCCallingConstants.DLRTCAcceptUserID, inviteUser);
                    intent.putExtra(DLRTCCallingConstants.DLRTCInviteSelf,false);
                    intent.putExtra(DLRTCCallingConstants.RTCInviteRoomID,roomId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(DLRTCCallingConstants.inviteExtJson,inviteExtJson);
                    mContext.startActivity(intent);

                }
                DLRTCCallService.Companion.start(mContext);
            }
        });
    }


    public static void tryStartCallSomeone(int type, String userId, int roomId) {
        startCallSomeone(type, userId, roomId,true);
    }

    public static void StartGameCallSomeone(int type, String userId, int roomId) {
        startCallSomeone(type, userId, roomId,false);
    }

    public static void show(String message) {
        ToastUtils.showLong(message);
    }

    public static void runOnUiThread(Runnable task) {
        if (null != task) {
            mMainHandler.post(task);
        }
    }

    public static void runOnUiThread(Runnable task, long delay) {
        if (null != task) {
            mMainHandler.postDelayed(task, delay);
        }
    }

}
