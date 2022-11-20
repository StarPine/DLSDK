package com.dl.rtc.calling.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.dl.lib.util.log.MPTimber
import com.dl.rtc.calling.DLRTCCallService
import com.dl.rtc.calling.R
import com.dl.rtc.calling.base.DLRTCCalling
import com.dl.rtc.calling.base.impl.DLRTCInternalListenerManager
import com.dl.rtc.calling.model.DLRTCCallingConstants
import com.dl.rtc.calling.model.DLRTCDataMessageType
import com.dl.rtc.calling.model.DLRTCStartModel
import com.dl.rtc.calling.util.MediaPlayHelper
import com.tencent.imsdk.v2.V2TIMSimpleMsgListener
import com.tencent.imsdk.v2.V2TIMUserInfo
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.trtc.TRTCCloudDef.TRTCQuality
import com.tencent.trtc.TRTCCloudDef.TRTCVolumeInfo
import com.tencent.trtc.TRTCCloudListener
import me.goldze.mvvmhabit.base.AppManager


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
    private fun startRing() {
        mMediaPlayHelper?.start(R.raw.phone_dialing)
    }

    private fun stopRing() {
        stopMusic()
    }

    private fun playHangupMusic() {
        mMediaPlayHelper?.start(R.raw.phone_hangup, 2000)
    }

    private fun stopMusic() {
        mMediaPlayHelper?.stop()
    }

    /**
     * 拨打电话
     */
    fun inviteUserRTC(inviteUser : String, inviteType : DLRTCDataMessageType.DLInviteRTCType, roomId : Int, data :String){
        DLRTCStartManager.instance.inviteUserRTC(inviteUser,inviteType,roomId, object : DLRTCModuleClosuer{
            override fun callback(_success: Boolean, _errorCode: Int, _errorMsg: String?) {
                inviteUserCall = _success
                if(_success){
                    val mContext = DLRTCStartManager.instance.mContext!!
                    // 首次拨打电话，生成id
                    // 单聊发送C2C消息; 用C2C实现的多人通话,需要保存每个userId对应的callId
                    val intent = Intent(Intent.ACTION_VIEW)
                    if (inviteType == DLRTCDataMessageType.DLInviteRTCType.dl_rtc_audio) {
                        intent.component = ComponentName(mContext.applicationContext, DLRTCInterceptorCall.instance.audioCallActivity)
                    } else {
                        intent.component = ComponentName(mContext.applicationContext, DLRTCInterceptorCall.instance.videoCallActivity)
                    }
                    intent.putExtra(DLRTCCallingConstants.PARAM_NAME_SPONSORID, TUILogin.getLoginUser())
                    intent.putExtra(DLRTCCallingConstants.PARAM_NAME_ROLE, DLRTCCalling.Role.CALL)
                    intent.putExtra("userProfile", data)
                    intent.putExtra(DLRTCCallingConstants.PARAM_NAME_USERIDS, inviteUser)
                    intent.putExtra("roomId", roomId)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    mContext.startActivity(intent)
                    DLRTCCallService.start(mContext)
                }else{

                }
            }
        })
    }
    //接听电话
    fun inviteUserAccept(){
        lastRtcModel?.apply {
            DLRTCStartManager.instance.inviteUserAccept(inviteId,"",object : DLRTCModuleClosuer{
                override fun callback(_success: Boolean, _errorCode: Int, _errorMsg: String?) {
                   MPTimber.tag(TAG_LOG).e("inviteUserAccept 接听电话 ： $_success $_errorCode $_errorMsg")
                    if(rtcInviteType == DLRTCDataMessageType.DLInviteRTCType.dl_rtc_audio.name){
                        DLRTCAudioManager.instance.enterRTCRoom(rtcInviteRoomId)
                    }else{
                        DLRTCVideoManager.instance.enterRTCRoom(rtcInviteRoomId)
                    }
                    DLRTCStartManager.instance.mTRTCCloud?.setListener(mTRTCCloudListener)
                }

            })
        }
    }

    //取消电话
    fun inviteUserCanceled(){
        lastRtcModel?.apply {
            DLRTCStartManager.instance.inviteUserCanceled(inviteId,object : DLRTCModuleClosuer{
                override fun callback(_success: Boolean, _errorCode: Int, _errorMsg: String?) {
                    MPTimber.tag(TAG_LOG).e("inviteUserCanceled 取消电话 ： $_success $_errorCode $_errorMsg")
                    exitRTCRoom()
                    DLRTCStartManager.instance.RTCRoomExitRoom()
                }
            })
        }
    }

    //拒绝当前邀请
    fun inviteUserReject(){
        lastRtcModel?.apply {
            DLRTCStartManager.instance.inviteUserReject(inviteId,"",object : DLRTCModuleClosuer{
                override fun callback(_success: Boolean, _errorCode: Int, _errorMsg: String?) {
                    MPTimber.tag(TAG_LOG).e("inviteUserReject 拒绝当前邀请 ： $_success $_errorCode $_errorMsg")
                    exitRTCRoom()
                    DLRTCStartManager.instance.RTCRoomExitRoom()
                }
            })
        }
    }


    override fun RTCStartManagerReciveMsg(manager: DLRTCStartManager, rtcModel: DLRTCStartModel) {
        MPTimber.tag(TAG_LOG).d("RTCStartManagerReciveMsg ： $rtcModel")
        MPTimber.tag(TAG_LOG).d("当前信令类型：${rtcModel.rtcDataMessageType}")
        when(rtcModel.rtcDataMessageType){
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
                DLRTCStartManager.instance.mContext?.let { DLRTCCallService.stop(it) }
                DLRTCStartManager.instance.RTCRoomExitRoom()
            }
            ///接受邀请
            DLRTCDataMessageType.accept -> {
                MPTimber.tag(TAG_LOG).e("接受邀请")
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
//        val customDataString = String(customData)
//        if (TextUtils.isEmpty(customDataString)) {
//            return
//        }
//        MPTimber.tag(TAG_LOG).d("onRecvC2CCustomMessage ： $msgID , sender：$sender , customData：$customDataString")
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
                intent.putExtra(DLRTCCallingConstants.PARAM_NAME_SPONSORID, dLRTCStartModel.inviteUserId)
                intent.putExtra(DLRTCCallingConstants.PARAM_NAME_ROLE, DLRTCCalling.Role.CALLED)
                intent.putExtra(DLRTCCallingConstants.PARAM_NAME_USERIDS, dLRTCStartModel.acceptUserId)
                intent.putExtra(DLRTCCallingConstants.PARAM_NAME_GROUPID, "")
                intent.putExtra("roomId", dLRTCStartModel.rtcInviteRoomId)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                mContext!!.startActivity(intent)
        }else{
//            var userIdResult = ""
//            if(!userIDs.isNullOrEmpty()){
//                userIdResult = userIDs[0].toString()
//            }
           // DLRTCInterceptorCall.instance.notifyInterceptorCall(userIdResult, type, roomId, data, isFromGroup, sponsorID)
        }
    }

    fun setStartManagerParams(dLRTCStartModel :DLRTCStartModel){
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
            if (reason == 1 || reason == 2) {
                DLRTCInternalListenerManager.instance.onCallEnd()
            }
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
    }
}