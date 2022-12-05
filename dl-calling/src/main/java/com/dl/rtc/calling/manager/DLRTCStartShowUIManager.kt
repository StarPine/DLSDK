package com.dl.rtc.calling.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.dl.lib.util.log.MPTimber
import com.dl.rtc.calling.DLRTCCallService
import com.dl.rtc.calling.R
import com.dl.rtc.calling.base.DLRTCCalling
import com.dl.rtc.calling.base.impl.DLRTCInternalListenerManager
import com.dl.rtc.calling.model.*
import com.dl.rtc.calling.model.bean.DLRTCSignallingData
import com.dl.rtc.calling.util.MediaPlayHelper
import com.google.gson.GsonBuilder
import com.tencent.imsdk.v2.V2TIMSimpleMsgListener
import com.tencent.imsdk.v2.V2TIMUserInfo
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.qcloud.tuicore.custom.CustomConstants
import com.tencent.trtc.TRTCCloudDef.TRTCQuality
import com.tencent.trtc.TRTCCloudDef.TRTCVolumeInfo
import com.tencent.trtc.TRTCCloudListener
import me.goldze.mvvmhabit.base.AppManager
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 *Author: 彭石林
 *Time: 2022/11/19 13:16
 * Description: This is DLRTCStartShowUIManager
 */
class DLRTCStartShowUIManager : DLRTCStartManagerDelegate, V2TIMSimpleMsgListener() {
    val TAG_LOG = "DLRTCStartShowUIManager"
    //是否是主动拨打
    var inviteUserCall = false
    //是否是被动接听
    var receiveUserCall = false
    //保存信令模型
    var lastRtcModel : DLRTCStartModel ? = null

    var mContext : Context? = null

    private fun initParams(){
        inviteUserCall = false
        receiveUserCall = false
    }
    companion object{
        val instance by lazy {
            DLRTCStartShowUIManager()
        }
    }


    private var mMediaPlayHelper: MediaPlayHelper? = null
    private var initFlag = false

    fun init(mContexts: Context){
        mContext = mContexts
        if(initFlag){
            return
        }
        initFlag = true
        mMediaPlayHelper = MediaPlayHelper(mContext)
        DLRTCStartManager.instance.addRtcListener(this)
        DLRTCIMSignallingManager.getInstance().addSimpleMsgListener(this)
    }

    /// 用户离开房间
    fun exitRTCRoom(){
        DLRTCStartManager.instance.mTRTCCloud?.apply {
            stopLocalPreview()
            stopLocalAudio()
            exitRoom()
        }
        DLRTCStartManager.instance.mContext?.let { DLRTCCallService.stop(it) }
    }


    /**
     * 播放铃声 （这里为了兼容第三方拓展逻辑处理。应在主动拨打、进入语音、视频页面才进行播放。否则不调用）
     */
     fun startRing() {
        mMediaPlayHelper?.start(R.raw.phone_dialing)
    }

     fun stopRing() {
        stopMusic()
    }

     fun playHangupMusic() {
        mMediaPlayHelper?.start(R.raw.phone_hangup, 2000)
    }

     fun stopMusic() {
        mMediaPlayHelper?.stop()
    }

    /**
     * 拨打电话
     */
    fun inviteUserRTC(inviteUser : String, inviteType : DLRTCDataMessageType.DLInviteRTCType, roomId : Int, dLInviteTimeout : Int?,inviteExtJson : String?, dlrtcStartUiClosuer: DLRTCStartUiClosuer){
        DLRTCStartManager.instance.inviteUserRTC(inviteUser,inviteType,roomId, dLInviteTimeout, inviteExtJson, object : DLRTCModuleClosuer{
            override fun callback(_success: Boolean, _errorCode: Int, _errorMsg: String?) {
                inviteUserCall = _success
                dlrtcStartUiClosuer.callback(_success, _errorCode, _errorMsg)
                sendC2CMessage(inviteUser,DLRTCDataMessageType.invite, inviteType.name)
            }
        })
    }
    //接听电话
    fun inviteUserAccept(){
        lastRtcModel?.apply {
            DLRTCStartManager.instance.inviteUserAccept(DLRTCStartManager.instance.inviteId,"",object : DLRTCModuleClosuer{
                override fun callback(_success: Boolean, _errorCode: Int, _errorMsg: String?) {
                   MPTimber.tag(TAG_LOG).e("inviteUserAccept 接听电话 ： $_success $_errorCode $_errorMsg")
                    if(rtcInviteType == DLRTCDataMessageType.DLInviteRTCType.dl_rtc_audio.name){
                        DLRTCAudioManager.instance.enterRTCRoom(rtcInviteRoomId)
                    }else{
                        DLRTCVideoManager.instance.enterRTCRoom(rtcInviteRoomId)
                    }
                    DLRTCStartManager.instance.mTRTCCloud?.setListener(mTRTCCloudListener)
                    sendC2CMessage(inviteUserId,DLRTCDataMessageType.accept, rtcInviteType)
                }

            })
        }
    }

    //取消电话
    fun inviteUserCanceled(){
            DLRTCStartManager.instance.inviteUserCanceled(DLRTCStartManager.instance.inviteId,object : DLRTCModuleClosuer{
                override fun callback(_success: Boolean, _errorCode: Int, _errorMsg: String?) {
                    MPTimber.tag(TAG_LOG).e("inviteUserCanceled 取消电话 ： $_success $_errorCode $_errorMsg")
                    sendC2CMessage(DLRTCStartManager.instance.acceptUserId,DLRTCDataMessageType.cancel, DLRTCStartManager.instance.inviteTypeMsg)
                    exitRTCRoom()
                    DLRTCStartManager.instance.RTCRoomExitRoom()
                }
            })
    }

    //拒绝当前邀请
    fun inviteUserReject(){
        DLRTCStartManager.instance.inviteUserReject(DLRTCStartManager.instance.inviteId,"",object : DLRTCModuleClosuer{
            override fun callback(_success: Boolean, _errorCode: Int, _errorMsg: String?) {
                MPTimber.tag(TAG_LOG).e("inviteUserReject 拒绝当前邀请 ： $_success $_errorCode $_errorMsg")
                if(receiveUserCall){
                    sendC2CMessage(DLRTCStartManager.instance.inviteUserId,DLRTCDataMessageType.reject, DLRTCStartManager.instance.inviteTypeMsg)
                }else{
                    sendC2CMessage(DLRTCStartManager.instance.acceptUserId,DLRTCDataMessageType.reject, DLRTCStartManager.instance.inviteTypeMsg)
                }
                exitRTCRoom()
                DLRTCStartManager.instance.RTCRoomExitRoom()
            }
        })
    }


    override fun RTCStartManagerReciveMsg(manager: DLRTCStartManager, rtcModel: DLRTCStartModel) {
        MPTimber.tag(TAG_LOG).d("RTCStartManagerReciveMsg ： $rtcModel")
        MPTimber.tag(TAG_LOG).d("当前信令类型：${rtcModel.rtcDataMessageType}")
        //DLRTCSignalingManager.sendInviteAction(rtcModel)
        when(rtcModel.rtcDataMessageType){
            DLRTCDataMessageType.inviteSucc -> {
               // sendC2CCustomMessage(acceptUserId = rtcModel.acceptUserId,inviteeId = rtcModel.inviteId, invitee = rtcModel.inviteUserId,1);
            }
            //开始邀请
            DLRTCDataMessageType.invite ->{
                //此处应处理接听页面唤醒
                MPTimber.tag(TAG_LOG).e("接受到邀请信令")
                //此处处理进房逻辑 - 被动接听
                if(!inviteUserCall){
                    lastRtcModel = rtcModel
                    setStartManagerParams(rtcModel)
                    receiveCall(rtcModel)
                }
            }
            ///拒绝邀请
            DLRTCDataMessageType.reject -> {
                //拒绝电话
                MPTimber.tag(TAG_LOG).e("拒绝邀请")
                DLRTCInternalListenerManager.instance.onReject(rtcModel.acceptUserId)
                initParams()
                exitRTCRoom()
                DLRTCStartManager.instance.mContext?.let { DLRTCCallService.stop(it) }
                DLRTCStartManager.instance.RTCRoomExitRoom()
            }
            ///接受邀请
            DLRTCDataMessageType.accept -> {
                MPTimber.tag(TAG_LOG).e("接受邀请")
                stopMusic()
                if(inviteUserCall){
                    setStartManagerParams(rtcModel)
                    DLRTCStartManager.instance.mTRTCCloud?.setListener(mTRTCCloudListener)
                    if(rtcModel.rtcInviteType == DLRTCDataMessageType.DLInviteRTCType.dl_rtc_audio.name){
                        DLRTCAudioManager.instance.enterRTCRoom(rtcModel.rtcInviteRoomId)
                    }else{
                        DLRTCVideoManager.instance.enterRTCRoom(rtcModel.rtcInviteRoomId)
                    }
                }
            }
            ///发起方取消邀请
            DLRTCDataMessageType.cancel -> {
                MPTimber.tag(TAG_LOG).e("发起方取消邀请")
                DLRTCInternalListenerManager.instance.onCallingCancel()
                initParams()
                DLRTCStartManager.instance.mContext?.let { DLRTCCallService.stop(it) }
                DLRTCStartManager.instance.RTCRoomExitRoom()
            }
            ///邀请超时
            DLRTCDataMessageType.timeout -> {
                MPTimber.tag(TAG_LOG).e("邀请超时")
                DLRTCInternalListenerManager.instance.onCallingTimeout()
                initParams()
                DLRTCStartManager.instance.mContext?.let { DLRTCCallService.stop(it) }
                DLRTCStartManager.instance.RTCRoomExitRoom()
            }
            /// 离开音视频房间
            DLRTCDataMessageType.exitRoom -> {
                MPTimber.tag(TAG_LOG).e("离开音视频房间")
                DLRTCInternalListenerManager.instance.onCallEnd()
                initParams()
                DLRTCStartManager.instance.mContext?.let { DLRTCCallService.stop(it) }
                DLRTCStartManager.instance.RTCRoomExitRoom()
            }
        }
    }

    /**
     * IM自定义信令
     */
    override fun onRecvC2CCustomMessage(msgID: String?, sender: V2TIMUserInfo?, customData: ByteArray?) {
        if(customData==null){ return }
        val customDataString = String(customData)
        if (TextUtils.isEmpty(customDataString)) {
            return
        }
        MPTimber.tag(TAG_LOG).d("onRecvC2CCustomMessage ： $msgID , sender：$sender , customData：$customDataString")
//        val signallingData  = DLRTConversionUtil.convert2CallingData(customDataString)
//        if (signallingData?.data == null || null == signallingData.businessID || signallingData.businessID != "av_call") {
//            MPTimber.d(TAG_LOG, "this is not the calling scene")
//            return
//        }
    }

    /**
     * 接听页面回调处理
     */
    private fun receiveCall(dLRTCStartModel :DLRTCStartModel){
        //验证当前activity是否出现在需要进行音视频通话拦截额外处理的地方
        receiveUserCall = true;
        val interceptorResult = DLRTCInterceptorCall.instance.containsActivity(AppManager.getAppManager().currentActivity().javaClass)

        MPTimber.tag(TAG_LOG).e(" ${DLRTCStartManager.instance.inviteId} 当前处理接听页面回调 $interceptorResult")
        if(!interceptorResult){
                startRing()
                val intent = Intent(Intent.ACTION_VIEW)
                if (DLRTCDataMessageType.onlyAudio == dLRTCStartModel.rtcInviteType) {
                    intent.component = ComponentName(mContext!!.applicationContext, DLRTCInterceptorCall.instance.audioCallActivity)
                } else {
                    intent.component = ComponentName(mContext!!.applicationContext, DLRTCInterceptorCall.instance.videoCallActivity)
                }
                intent.putExtra(DLRTCCallingConstants.DLRTCInviteUserID, dLRTCStartModel.inviteUserId)
                intent.putExtra(DLRTCCallingConstants.PARAM_NAME_ROLE, DLRTCCalling.Role.CALLED)
                intent.putExtra(DLRTCCallingConstants.DLRTCAcceptUserID, dLRTCStartModel.acceptUserId)
                intent.putExtra(DLRTCCallingConstants.DLRTCInviteSelf,false)
                intent.putExtra(DLRTCCallingConstants.RTCInviteRoomID,dLRTCStartModel.rtcInviteRoomId)
                intent.putExtra(DLRTCCallingConstants.inviteExtJson,dLRTCStartModel.inviteExtJson)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                mContext!!.startActivity(intent)
        }else{
            DLRTCInterceptorCall.instance.notifyInterceptorCall(dLRTCStartModel.acceptUserId, dLRTCStartModel.inviteUserId, DLRTCDataMessageType.DLInviteRTCType.valueOf(dLRTCStartModel.rtcInviteType), dLRTCStartModel.rtcInviteRoomId, null)
        }
    }

    private fun setStartManagerParams(dLRTCStartModel :DLRTCStartModel){
        if(DLRTCStartManager.instance.inviteId.isEmpty()){
            DLRTCStartManager.instance.inviteId = dLRTCStartModel.inviteId
        }
        if(DLRTCStartManager.instance.acceptUserId.isEmpty()){
            DLRTCStartManager.instance.acceptUserId = dLRTCStartModel.acceptUserId
        }
        if(DLRTCStartManager.instance.inviteUserId.isEmpty()){
            DLRTCStartManager.instance.inviteUserId = dLRTCStartModel.inviteUserId
        }
        if(DLRTCStartManager.instance.inviteRTCRoomId == 0){
            DLRTCStartManager.instance.inviteRTCRoomId = dLRTCStartModel.rtcInviteRoomId
        }
        if(DLRTCStartManager.instance.inviteTypeMsg.isEmpty()){
            DLRTCStartManager.instance.inviteTypeMsg = dLRTCStartModel.rtcInviteType
        }
        DLRTCStartManager.instance.isReceiveNewInvite = true
    }

    /**
     * TRTC的监听器
     */
    private val mTRTCCloudListener: TRTCCloudListener = object : TRTCCloudListener() {
        override fun onError(errCode: Int, errMsg: String, extraInfo: Bundle) {
            MPTimber.tag(TAG_LOG).d("onError errCode : $errCode , errMsg : $errMsg")
            DLRTCInternalListenerManager.instance.onError(errCode,errMsg)
        }
        //有人进入房间
        override fun onEnterRoom(result: Long) {
            MPTimber.tag(TAG_LOG).d("onEnterRoom result : $result ")
            if (result > 0) {
                lastRtcModel?.apply {
                    val userEnterId: String = if(inviteUserCall){
                        acceptUserId
                    }else{
                        inviteUserId
                    }
                    DLRTCInternalListenerManager.instance.onUserEnter(userEnterId)
                }
            } else {
            }
        }

        override fun onExitRoom(reason: Int) {
            MPTimber.tag(TAG_LOG).d("onExitRoom reason : $reason ")
            //1 后台执行踢出房间。 2 后台解散房间
//            if (reason == 1 || reason == 2) {
//                DLRTCInternalListenerManager.instance.onCallEnd()
//            }
            DLRTCInternalListenerManager.instance.onCallEnd()
            initParams()
            DLRTCStartManager.instance.mContext?.let { DLRTCCallService.stop(it) }
            DLRTCStartManager.instance.RTCRoomExitRoom()
        }

        override fun onRemoteUserEnterRoom(userId: String) {
            MPTimber.tag(TAG_LOG).d("onRemoteUserEnterRoom userId : $userId ")
            // 只有单聊这个时间才是正确的，因为单聊只会有一个用户进群，群聊这个时间会被后面的人重置
            DLRTCInternalListenerManager.instance.onUserEnter(userId)
        }
        //有一端用户离开了房间
        override fun onRemoteUserLeaveRoom(userId: String, reason: Int) {
            MPTimber.tag(TAG_LOG).d("onRemoteUserLeaveRoom userId : $userId , reason : $reason")
            DLRTCInternalListenerManager.instance.onCallEnd()
            initParams()
            DLRTCStartManager.instance.mContext?.let { DLRTCCallService.stop(it) }
            DLRTCStartManager.instance.RTCRoomExitRoom()
        }

        override fun onUserVideoAvailable(userId: String, available: Boolean) {
            MPTimber.tag(TAG_LOG).d("onUserVideoAvailable userId : $userId , available : $available")
            DLRTCInternalListenerManager.instance.onUserVideoAvailable(userId, available)
        }

        override fun onUserAudioAvailable(userId: String, available: Boolean) {
            MPTimber.tag(TAG_LOG).d("onUserAudioAvailable userId : $userId , available : $available")
            DLRTCInternalListenerManager.instance.onUserAudioAvailable(userId, available)
        }

        override fun onUserVoiceVolume(userVolumes: ArrayList<TRTCVolumeInfo>, totalVolume: Int) {
            val volumeMaps: MutableMap<String?, Int> = HashMap()
            for (info in userVolumes) {
                val userId = if (info.userId == null) TUILogin.getUserId() else info.userId
                volumeMaps[userId] = info.volume
            }
            DLRTCInternalListenerManager.instance.onUserVoiceVolume(volumeMaps)
        }

        override fun onNetworkQuality(quality: TRTCQuality, arrayList: ArrayList<TRTCQuality?>?) {
            DLRTCInternalListenerManager.instance.onNetworkQuality(quality, arrayList)
        }

        override fun onFirstVideoFrame(userId: String?, streamType: Int, width: Int, height: Int) {
            MPTimber.tag(TAG_LOG).d("onFirstVideoFrame userId : $userId , streamType : $streamType")
            DLRTCInternalListenerManager.instance.onFirstVideoFrame(userId, streamType, width, height)
        }

        override fun onFirstAudioFrame(userId: String?) {
            MPTimber.tag(TAG_LOG).d("onFirstAudioFrame userId : $userId ")
            DLRTCInternalListenerManager.instance.onFirstAudioFrame(userId)
        }

        override fun onRemoteVideoStatusUpdated(userId: String?, streamType: Int, status: Int, reason: Int, extraInfo: Bundle?) {
            MPTimber.tag(TAG_LOG).d("onRemoteVideoStatusUpdated userId : $userId , streamType : $streamType , status : $status , reason : $reason")
            DLRTCInternalListenerManager.instance.onRemoteVideoStatusUpdated(userId, streamType, status, reason, extraInfo)
        }

        override fun onRemoteAudioStatusUpdated(userId: String?, status: Int, reason: Int, extraInfo: Bundle?) {
            MPTimber.tag(TAG_LOG).d("onRemoteAudioStatusUpdated userId : $userId , status : $status , reason : $reason")
            DLRTCInternalListenerManager.instance.onRemoteAudioStatusUpdated(userId, status, reason, extraInfo)
        }
    }

    fun sendC2CMessage(receiveUserId : String, action : String, inviteType : String){
        val signallingData = buildDLRTCTempMessage(CustomConstants.CallingMessage.MODULE_NAME, CustomConstants.CallingMessage.TYPE_CALLING_HINT,action,inviteType)
        val gsonBuilder = GsonBuilder()
        val dataStr = gsonBuilder.create().toJson(signallingData)
        DLRTCSignalingManager.sendC2CMessage(dataStr,receiveUserId)
    }

    private fun buildDLRTCTempMessage(msgModuleName : String, customMsgType : String,action: String, inviteType : String, isHideUI : Boolean =false) : DLRTCTempMessage {
        val dLRTCTempMessage = DLRTCTempMessage()
        val moduleMessage = DLRTCTempMessage.MsgModuleInfo()
        moduleMessage.msgModuleName = msgModuleName
        moduleMessage.contentBody = DLRTCTempMessage.MsgBodyInfo().also{
            it.customMsgType = customMsgType
            it.customMsgBody = Hashtable<String, Any>().apply {
                put("action",action)
                put("inviteType",inviteType)
            }
            it.isHideUI = isHideUI
        }
        dLRTCTempMessage.setContentBody(moduleMessage)
        return dLRTCTempMessage
    }
}
interface DLRTCStartUiClosuer{
    fun callback(_success : Boolean,_errorCode : Int,_errorMsg : String?)
}