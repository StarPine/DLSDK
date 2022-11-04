package com.dl.rtc.calling.manager

import com.dl.rtc.calling.base.DLRTCCallingDelegate
import com.dl.rtc.calling.base.impl.DLRTCInternalListenerManager
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.trtc.TRTCCloudDef
import com.tencent.trtc.TRTCCloudDef.TRTCParams
import com.tencent.trtc.TRTCCloudDef.TRTCVideoEncParam

/**
 *Author: 彭石林
 *Time: 2022/11/3 12:03
 * Description: 视频通话管理类
 */
class DLRTCVideoManager {

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
                    mTRTCInternalListenerManager = DLRTCInternalListenerManager()
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
    fun enterTRTCRoom(mCurRoomID :String){

//            // 开启基础美颜
//            TXBeautyManager txBeautyManager = mTRTCCloud.getBeautyManager();
//            // 自然美颜
//            txBeautyManager.setBeautyStyle(1);
//            txBeautyManager.setBeautyLevel(6);
        // 进房前需要设置一下关键参数
        val encParam = TRTCVideoEncParam()
        encParam.videoResolution = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_1280_720
        encParam.videoFps = 15
        //encParam.videoBitrate = 1000;
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
        }
        TRTCLogger.i(
            TRTCCalling.TAG,
            "enterTRTCRoom: " + TUILogin.getUserId() + " room:" + mCurRoomID
        )
        val trtcParams = TRTCParams(
            TUILogin.getSdkAppId(), TUILogin.getUserId(),
            TUILogin.getUserSig(), mCurRoomID, "", ""
        )
        trtcParams.role = TRTCCloudDef.TRTCRoleAnchor
        mTRTCCloud!!.enableAudioVolumeEvaluation(300)
        mTRTCCloud!!.setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_SPEAKER)
        mTRTCCloud!!.startLocalAudio(3)
        // 收到来电，开始监听 trtc 的消息
        // 收到来电，开始监听 trtc 的消息
        setFramework()
        // 输出版本日志
        // 输出版本日志
        printVersionLog()
        mTRTCCloud!!.setListener(mTRTCCloudListener)
        mTRTCCloud!!.enterRoom(trtcParams,TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL)
    }
}