package com.dl.rtc.calling.manager

import com.dl.lib.util.log.MPTimber
import com.dl.rtc.calling.base.DLRTCCallingDelegate
import com.dl.rtc.calling.base.DLRTCCallingItFace
import com.dl.rtc.calling.base.impl.DLRTCInternalListenerManager
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.trtc.TRTCCloudDef

/**
 *Author: 彭石林
 *Time: 2022/11/3 12:04
 * Description: 语音通话管理类
 */
class DLRTCAudioManager : DLRTCCallingItFace {

    val TAG_LOG = "DLRTCAudioManager"

    private var mTRTCInternalListenerManager : DLRTCInternalListenerManager? = null

    companion object{
        val instance by lazy {
            DLRTCAudioManager().apply {
                mTRTCInternalListenerManager = DLRTCInternalListenerManager.instance
            }
        }
    }
    fun addDelegate(delegate: DLRTCCallingDelegate?) {
        if (delegate != null) {
            mTRTCInternalListenerManager?.addDelegate(delegate)
        }
    }

    fun removeDelegate(delegate: DLRTCCallingDelegate?) {
        if (delegate != null) {
            mTRTCInternalListenerManager?.removeDelegate(delegate)
        }
    }
    /**
     * 进入房间
     */
    private fun enterRTCRoom(mCurRoomID :Int){
        MPTimber.tag(TAG_LOG).i("enterTRTCRoom: " + TUILogin.getUserId() + " room:" + mCurRoomID)
        DLRTCStartManager.getInstance().mTRTCCloud?.apply {
            val trtcParams = TRTCCloudDef.TRTCParams(
                TUILogin.getSdkAppId(), TUILogin.getUserId(),
                TUILogin.getUserSig(), mCurRoomID, "", ""
            )
            trtcParams.role = TRTCCloudDef.TRTCRoleAnchor
            enableAudioVolumeEvaluation(300)
            setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_SPEAKER)
            startLocalAudio(3)
            enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL)
        }
    }

    override fun hangup() {
        DLRTCStartManager.getInstance().hangup()
    }

    /**
     * 挂断电话
     */
    override fun reject() {
        DLRTCStartManager.getInstance().reject()
    }

    /**
     * 接听电话
     */
    override fun accept() {
        DLRTCStartManager.getInstance().accept()
    }

    /**
     * 进入房间
     */
    override fun enterRoom(roomId : Int) {
        enterRTCRoom(roomId)
    }

    /**
     * 推出房间
     */
    override fun exitRoom() {
        DLRTCStartManager.getInstance().exitRoom()
    }

    override fun muteLocalAudio(enable : Boolean) {
        DLRTCStartManager.getInstance().muteLocalAudio(enable)
    }

    override fun audioRoute(route: Boolean) {
        DLRTCStartManager.getInstance().audioRoute(route)
    }

    override fun enableAGC(enable: Boolean) {
        DLRTCStartManager.getInstance().enableAGC(enable)
    }

    override fun enableAEC(enable: Boolean) {
        DLRTCStartManager.getInstance().enableAEC(enable)
    }

    override fun enableANS(enable: Boolean) {
        DLRTCStartManager.getInstance().enableANS(enable)
    }
}