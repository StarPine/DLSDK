package com.dl.rtc.calling.manager

import com.dl.lib.util.log.MPTimber
import com.dl.rtc.calling.DLRTCCallService
import com.dl.rtc.calling.base.DLRTCCallingItFace
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.trtc.TRTCCloudDef

/**
 *Author: 彭石林
 *Time: 2022/11/3 12:04
 * Description: 语音通话管理类
 */
class DLRTCAudioManager : DLRTCCallingItFace {

    val TAG_LOG = "DLRTCAudioManager"

    companion object{
        val instance by lazy {
            DLRTCAudioManager()
        }
    }

    /// 用进入RTC房间
/// - Parameter roomId: 整型的rtc房间id
/// - Parameter roomIds: 字符串类型的房间id，方便后续扩展
    override fun enterRTCRoom(roomId : Int,roomIds : String?){
        MPTimber.tag(TAG_LOG).i("enterTRTCRoom: " + TUILogin.getUserId() + " roomId:" + roomId)
        DLRTCStartManager.instance.mTRTCCloud?.apply {
            val rtcParams = TRTCCloudDef.TRTCParams(
                TUILogin.getSdkAppId(), TUILogin.getUserId(),
                TUILogin.getUserSig(), roomId, "", ""
            )
            rtcParams.role = TRTCCloudDef.TRTCRoleAnchor
            enableAudioVolumeEvaluation(300)
            setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_SPEAKER)
            startLocalAudio(3)
            DLRTCStartManager.instance.setFramework()
            enterRoom(rtcParams, TRTCCloudDef.TRTC_APP_SCENE_AUDIOCALL)
        }
        DLRTCStartManager.instance.mContext?.let { DLRTCCallService.start(it) }
    }

    fun startLocalAudio(){
        DLRTCStartManager.instance.mTRTCCloud?.startLocalAudio(3)
    }

    fun stopLocalAudio(){
        DLRTCStartManager.instance.mTRTCCloud?.stopLocalAudio()
    }


    override fun muteLocalAudio(enable : Boolean) {
        DLRTCStartManager.instance.muteLocalAudio(enable)
    }

    /// 禁止远端音频
/// - Parameters:
///   - mute: 是否静音
///   - userId: 静音的用户id
    fun muteRemoteAudio(enable : Boolean,userId : String){
        DLRTCStartManager.instance.mTRTCCloud?.muteRemoteAudio(userId,enable)
    }

    //禁止所有远端的音频，静音所有的音频流
    fun muteAllRemoteAudio(enable : Boolean){
        DLRTCStartManager.instance.mTRTCCloud?.muteAllRemoteAudio(enable)
    }

    override fun audioRoute(route: Boolean) {
        DLRTCStartManager.instance.audioRoute(route)
    }

    override fun enableAGC(enable: Boolean) {
        DLRTCStartManager.instance.enableAGC(enable)
    }

    override fun enableAEC(enable: Boolean) {
        DLRTCStartManager.instance.enableAEC(enable)
    }

    override fun enableANS(enable: Boolean) {
        DLRTCStartManager.instance.enableANS(enable)
    }
}