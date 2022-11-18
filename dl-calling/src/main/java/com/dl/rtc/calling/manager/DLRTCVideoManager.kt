package com.dl.rtc.calling.manager

import com.dl.lib.util.log.MPTimber
import com.dl.rtc.calling.base.DLRTCCallingDelegate
import com.dl.rtc.calling.base.DLRTCCallingItFace
import com.dl.rtc.calling.base.impl.DLRTCInternalListenerManager
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.rtmp.ui.TXCloudVideoView
import com.tencent.trtc.TRTCCloudDef
import com.tencent.trtc.TRTCCloudDef.TRTCParams
import com.tencent.trtc.TRTCCloudDef.TRTCVideoEncParam

/**
 *Author: 彭石林
 *Time: 2022/11/3 12:03
 * Description: 视频通话管理类
 */
class DLRTCVideoManager : DLRTCCallingItFace {
    val TAG_LOG = "DLRTCVideoManager"

    private var mTRTCInternalListenerManager : DLRTCInternalListenerManager? = null

    companion object {
        // 单例可防止同时打开多个实例（双重校验锁式（Double Check)单例）。
        @Volatile
        private var sINSTANCE: DLRTCVideoManager? = null
        @JvmStatic
        fun getInstance(): DLRTCVideoManager {
            //如果 INSTANCE 不为空，则返回它，如果是，则创建数据库
            return sINSTANCE ?: synchronized(this) {
                //创建对象后。引用apply作用域操作当前对象本身内置方法
                val instance = DLRTCVideoManager().apply {
                    mTRTCInternalListenerManager = DLRTCInternalListenerManager.instance
                }
                sINSTANCE = instance
                // 返回实例
                instance
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
        // 进房前需要设置一下关键参数
        val encParam = TRTCVideoEncParam()
        encParam.videoResolution = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_1280_720
        encParam.videoFps = 15
        //encParam.videoBitrate = 1000;
        encParam.minVideoBitrate = 1200
        encParam.videoResolutionMode = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_PORTRAIT
        encParam.enableAdjustRes = true
        DLRTCStartManager.getInstance().mTRTCCloud?.apply {
            setVideoEncoderParam(encParam);
            val trtcParams = TRTCParams(
                TUILogin.getSdkAppId(), TUILogin.getUserId(),
                TUILogin.getUserSig(), mCurRoomID, "", ""
            )
            trtcParams.role = TRTCCloudDef.TRTCRoleAnchor
            enableAudioVolumeEvaluation(300)
            setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_SPEAKER)
            startLocalAudio(3)
            enterRoom(trtcParams,TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL)
        }
        MPTimber.tag(TAG_LOG).i("enterTRTCRoom: " + TUILogin.getUserId() + " room:" + mCurRoomID)
    }

    override fun hangup() {
        DLRTCStartManager.getInstance().hangup()
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

    /**
     * 打开相机流
     */
    fun openCamera(isFrontCamera: Boolean, txCloudVideoView: TXCloudVideoView?) {
        DLRTCStartManager.getInstance().openCamera(isFrontCamera, txCloudVideoView)
    }

    /**
     * 关闭相机流
     */
    fun closeCamera(){
        DLRTCStartManager.getInstance().closeCamera()
    }

    /**
     * 渲染视频
     */
    fun startRemoteView(userId: String?, txCloudVideoView: TXCloudVideoView?) {
        DLRTCStartManager.getInstance().startRemoteView(userId,txCloudVideoView)
    }
    /**
     * 关闭渲染视频
     */
    fun stopRemoteView(userId: String?) {
        DLRTCStartManager.getInstance().stopRemoteView(userId)
    }

    fun setMicMute(enable: Boolean){
        DLRTCStartManager.getInstance().mTRTCCloud?.muteLocalAudio(enable)
    }

    fun switchCamera(enable: Boolean){
        DLRTCStartManager.getInstance().switchCamera(enable)
    }
}