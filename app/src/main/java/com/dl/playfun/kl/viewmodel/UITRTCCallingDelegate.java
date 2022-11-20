package com.dl.playfun.kl.viewmodel;

import android.util.Log;

import com.dl.rtc.calling.base.DLRTCCallingDelegate;
import com.tencent.trtc.TRTCCloudDef;

import java.util.ArrayList;
import java.util.Map;

public class UITRTCCallingDelegate implements DLRTCCallingDelegate {
    private static final String TAG = "trtcJoy";

    @Override
    public void onError(int code, String msg) {
        Log.e(TAG, "onError: " + code + " " + msg);
    }

    @Override
    public void onUserEnter(String userId) {
        Log.i(TAG, "onUserEnter: " + userId);
    }

    @Override
    public void onUserLeave(String userId) {
        Log.i(TAG, "onUserLeave: " + userId);
    }

    @Override
    public void onReject(String userId) {
        Log.i(TAG, "onReject: " + userId);
    }


    @Override
    public void onLineBusy(String userId) {
        Log.i(TAG, "onLineBusy: " + userId);
    }

    @Override
    public void onCallingCancel() {
        Log.i(TAG, "onCallingCancel: ");
    }

    @Override
    public void onCallingTimeout() {
        Log.i(TAG, "onCallingTimeout: ");
    }

    @Override
    public void onCallEnd() {
        Log.i(TAG, "onCallEnd: ");
    }

    @Override
    public void onUserVideoAvailable(String userId, boolean isVideoAvailable) {
        Log.i(TAG, "onUserVideoAvailable: " + userId + ", " + isVideoAvailable);
    }

    @Override
    public void onUserAudioAvailable(String userId, boolean isVideoAvailable) {
        Log.i(TAG, "onUserAudioAvailable: ");
    }

    @Override
    public void onUserVoiceVolume(Map<String, Integer> volumeMap) {
//        Log.i(TAG, "onUserVoiceVolume: ");
    }

    @Override
    public void onNetworkQuality(TRTCCloudDef.TRTCQuality localQuality, ArrayList<TRTCCloudDef.TRTCQuality> remoteQuality) {
        Log.i(TAG, "onNetworkQuality: ");
    }

    @Override
    public void onTryToReconnect() {
        Log.i(TAG, "onTryToReconnect: ");
    }
}
