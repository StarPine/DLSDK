package com.dl.rtc.calling.manager

import android.content.ComponentName
import android.content.Intent
import com.dl.lib.util.log.MPTimber
import com.dl.rtc.calling.DLRTCCallService
import com.dl.rtc.calling.base.DLRTCCalling
import com.dl.rtc.calling.base.DLRTCCallingDelegate
import com.dl.rtc.calling.base.DLRTCCallingItFace
import com.dl.rtc.calling.base.impl.DLRTCInternalListenerManager
import com.dl.rtc.calling.model.DLRTCCallingConstants
import com.dl.rtc.calling.model.DLRTCDataMessageType
import com.faceunity.core.enumeration.CameraFacingEnum
import com.faceunity.nama.FURenderer
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.rtmp.ui.TXCloudVideoView
import com.tencent.trtc.TRTCCloudDef
import com.tencent.trtc.TRTCCloudDef.TRTCParams
import com.tencent.trtc.TRTCCloudDef.TRTCVideoEncParam
import com.tencent.trtc.TRTCCloudListener

/**
 *Author: 彭石林
 *Time: 2022/11/3 12:03
 * Description: 视频通话管理类
 */
class DLRTCVideoManager : DLRTCCallingItFace {
    val TAG_LOG = "DLRTCVideoManager"

    //当前相机使用前后摄像头吧
    private var mIsUseFrontCamera = false
    //美颜相关
    private var mIsFuEffect = false
    //美颜相关
    private var mFURenderer: FURenderer? = null


    companion object{

        val instance by lazy {
            DLRTCVideoManager()
        }
    }

    /// 用进入RTC房间
/// - Parameter roomId: 整型的rtc房间id
/// - Parameter roomIds: 字符串类型的房间id，方便后续扩展
    override fun enterRTCRoom(roomId : Int,roomIds : String?){
        // 进房前需要设置一下关键参数
        val encParam = TRTCVideoEncParam()
        encParam.videoResolution = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_1280_720
        encParam.videoFps = 15
        //encParam.videoBitrate = 1000;
        encParam.minVideoBitrate = 1200
        encParam.videoResolutionMode = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_PORTRAIT
        encParam.enableAdjustRes = true
        DLRTCStartManager.instance.mTRTCCloud?.apply {
            setVideoEncoderParam(encParam);
            val trtcParams = TRTCParams(
                TUILogin.getSdkAppId(), TUILogin.getUserId(),
                TUILogin.getUserSig(), roomId, "", ""
            )
            trtcParams.role = TRTCCloudDef.TRTCRoleAnchor
            enableAudioVolumeEvaluation(300)
            setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_SPEAKER)
            startLocalAudio(3)
            enterRoom(trtcParams,TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL)
        }
        MPTimber.tag(TAG_LOG).i("enterTRTCRoom: " + TUILogin.getUserId() + " roomId:" + roomId)
        DLRTCStartManager.instance.mContext?.let { DLRTCCallService.start(it) }
    }

    /// 用户离开房间
    fun exitRTCRoom(){
        DLRTCStartManager.instance.mTRTCCloud?.apply {
            stopLocalPreview()
            stopLocalAudio()
            exitRoom()
            DLRTCStartManager.instance.mContext?.let { DLRTCCallService.stop(it) }
        }
    }

    override fun muteLocalAudio(enable : Boolean) {
        DLRTCStartManager.instance.muteLocalAudio(enable)
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

    /**
     * 打开相机流
     */
    fun openCamera(isFrontCamera: Boolean, txCloudVideoView: TXCloudVideoView?) {
        if (txCloudVideoView == null) {
            return
        }
        mIsUseFrontCamera = isFrontCamera
        if (mIsFuEffect) {
            DLRTCStartManager.instance.mTRTCCloud!!.setLocalVideoProcessListener(
                TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_Texture_2D,
                TRTCCloudDef.TRTC_VIDEO_BUFFER_TYPE_TEXTURE, object :
                    TRTCCloudListener.TRTCVideoFrameListener {
                    override fun onGLContextCreated() {
                        mFURenderer!!.prepareRenderer(null)
                    }

                    override fun onProcessVideoFrame(src: TRTCCloudDef.TRTCVideoFrame, dest: TRTCCloudDef.TRTCVideoFrame): Int {
                        mFURenderer!!.cameraFacing = if (mIsUseFrontCamera) CameraFacingEnum.CAMERA_FRONT else CameraFacingEnum.CAMERA_BACK
                        dest.texture.textureId = mFURenderer!!.onDrawFrameSingleInput(
                            src.texture.textureId,
                            src.width,
                            src.height
                        )
                        return 0
                    }

                    override fun onGLContextDestory() {
                        mFURenderer!!.release()
                    }

                })
        }
        DLRTCStartManager.instance.mTRTCCloud!!.startLocalPreview(isFrontCamera, txCloudVideoView)
    }


    fun updateLocalView(txCloudVideoView: TXCloudVideoView?){
        if (txCloudVideoView == null) {
            return
        }
        if (mIsFuEffect) {
            DLRTCStartManager.instance.mTRTCCloud!!.setLocalVideoProcessListener(
                TRTCCloudDef.TRTC_VIDEO_PIXEL_FORMAT_Texture_2D,
                TRTCCloudDef.TRTC_VIDEO_BUFFER_TYPE_TEXTURE, object : TRTCCloudListener.TRTCVideoFrameListener {
                    override fun onGLContextCreated() {
                        mFURenderer!!.prepareRenderer(null)
                    }

                    override fun onProcessVideoFrame(src: TRTCCloudDef.TRTCVideoFrame, dest: TRTCCloudDef.TRTCVideoFrame): Int {
                        mFURenderer!!.cameraFacing = if (mIsUseFrontCamera) CameraFacingEnum.CAMERA_FRONT else CameraFacingEnum.CAMERA_BACK
                        dest.texture.textureId = mFURenderer!!.onDrawFrameSingleInput(
                            src.texture.textureId,
                            src.width,
                            src.height
                        )
                        return 0
                    }

                    override fun onGLContextDestory() {
                        mFURenderer!!.release()
                    }

                })
        }
        DLRTCStartManager.instance.mTRTCCloud!!.updateLocalView(txCloudVideoView)
    }

    /**
     * 关闭相机流
     */
    fun stopLocalPreview(){
        DLRTCStartManager.instance.mTRTCCloud!!.stopLocalPreview()
    }

    /**
     * 渲染视频
     */
    fun startRemoteView(userId: String?, txCloudVideoView: TXCloudVideoView?) {
        if (txCloudVideoView == null) {
            return
        }
        DLRTCStartManager.instance.mTRTCCloud!!.startRemoteView(userId,0, txCloudVideoView)
    }
    /**
     * 更新渲染视频
     */
    fun updateRemoteView(userId: String?, txCloudVideoView: TXCloudVideoView?) {
        if (txCloudVideoView == null) {
            return
        }
        DLRTCStartManager.instance.mTRTCCloud!!.updateRemoteView(userId,0, txCloudVideoView)
    }
    /**
     * 关闭渲染视频
     */
    fun stopRemoteView(userId: String?) {
        DLRTCStartManager.instance.mTRTCCloud!!.stopRemoteView(userId,0)
    }

    fun setMicMute(enable: Boolean){
        DLRTCStartManager.instance.mTRTCCloud?.muteLocalAudio(enable)
    }

    fun switchCamera(isFrontCamera: Boolean){
        if (mIsUseFrontCamera == isFrontCamera) {
            return
        }
        mIsUseFrontCamera = isFrontCamera
        if (mIsFuEffect) {
            mFURenderer!!.cameraFacing =
                if (mIsUseFrontCamera) CameraFacingEnum.CAMERA_FRONT else CameraFacingEnum.CAMERA_BACK
        }
    }

    /**
     * 美颜初始化自定义采集和渲染的对象
     *
     * @return
     */
    fun createCustomRenderer(mIsEffect: Boolean): FURenderer? {
        mIsFuEffect = mIsEffect
        if (mIsFuEffect) {
            mFURenderer = FURenderer.getInstance()
        }
        return mFURenderer
    }
}