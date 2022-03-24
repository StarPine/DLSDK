package com.dl.playfun.kl;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;

import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.liteav.trtccalling.model.TRTCCallingDelegate;
import com.tencent.liteav.trtccalling.model.TUICalling;
import com.tencent.trtc.TRTCCloudDef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TRTCAcceptCalling implements TRTCCallingDelegate {

    Application mApplication;

    public TRTCAcceptCalling(Application application) {
        this.mApplication = application;
    }

    @Override
    public void onError(int code, String msg) {

    }

    /**
     * 被邀请通话回调
     *
     * @param sponsor     邀请者
     * @param userIdList  同时还被邀请的人
     * @param isFromGroup 是否IM群组邀请
     * @param callType    邀请类型 1-语音通话，2-视频通话
     */
    @Override
    public void onInvited(String sponsor, List<String> userIdList, boolean isFromGroup, int callType) {
        if (callType == 0) {// audio
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("play_chat://pet.master.tw/call"));
            intent.putExtra("callType", callType);
            intent.putExtra("toUserId", V2TIMManager.getInstance().getLoginUser());
            intent.putExtra("role", TUICalling.Role.CALLED);
            intent.putExtra("fromUserId", sponsor);
            mApplication.startActivity(intent);
        } else {

        }
    }

    @Override
    public void onGroupCallInviteeListUpdate(List<String> userIdList) {

    }

    @Override
    public void onUserEnter(String userId) {

    }

    @Override
    public void onUserLeave(String userId) {

    }

    @Override
    public void onReject(String userId) {

    }

    @Override
    public void onNoResp(String userId) {

    }

    @Override
    public void onLineBusy(String userId) {

    }

    @Override
    public void onCallingCancel() {

    }

    @Override
    public void onCallingTimeout() {

    }

    @Override
    public void onCallEnd() {

    }

    @Override
    public void onUserVideoAvailable(String userId, boolean isVideoAvailable) {

    }

    @Override
    public void onUserAudioAvailable(String userId, boolean isVideoAvailable) {

    }

    @Override
    public void onUserVoiceVolume(Map<String, Integer> volumeMap) {

    }

    @Override
    public void onNetworkQuality(TRTCCloudDef.TRTCQuality localQuality, ArrayList<TRTCCloudDef.TRTCQuality> remoteQuality) {

    }

    @Override
    public void onSwitchToAudio(boolean success, String message) {

    }
}
