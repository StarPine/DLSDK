package com.dl.playfun.kl;

import android.os.Handler;
import android.os.Looper;

import com.blankj.utilcode.util.ToastUtils;
import com.dl.playfun.app.AppContext;
import com.dl.rtc.calling.base.DLRTCCalling;
import com.dl.rtc.calling.manager.DLRTCStartManager;
import com.dl.rtc.calling.manager.DLRTCStartShowUIManager;
import com.dl.rtc.calling.model.DLRTCCallingConstants;
import com.dl.rtc.calling.model.DLRTCDataMessageType;

public class Utils {
    protected static final Handler mMainHandler = new Handler(Looper.getMainLooper());

    /**
     * 开始呼叫某人
     */
    public static void startCallSomeone(int type, String toUserId, int roomId, String data,boolean startView) {
        if (type == DLRTCCallingConstants.TYPE_VIDEO_CALL) {
            String[] userIDs = {toUserId};
          //  DLRTCStartManager.Companion.getInstance().call(userIDs, DLRTCCalling.Type.VIDEO, roomId, data,startView);
        } else if (type == DLRTCCallingConstants.TYPE_AUDIO_CALL) {
            String[] userIDs = {toUserId};
          //  DLRTCStartManager.Companion.getInstance().call(userIDs, DLRTCCalling.Type.AUDIO, roomId, data,startView);
        }

    }

    public static void inviteUserRTC(String inviteUser, DLRTCDataMessageType.DLInviteRTCType inviteType, int roomId, boolean launchView, String data){
        //DLRTCStartManager.Companion.getInstance().inviteUserRTC(inviteUser, inviteType, roomId, launchView, data);
        DLRTCStartShowUIManager.Companion.getInstance().inviteUserRTC(inviteUser,inviteType,roomId,data);
    }


    public static void tryStartCallSomeone(int type, String userId, int roomId, String data) {
        startCallSomeone(type, userId, roomId, data,true);
    }

    public static void StartGameCallSomeone(int type, String userId, int roomId, String data) {
        startCallSomeone(type, userId, roomId, data,false);
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
