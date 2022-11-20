package com.dl.rtc.calling.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.dl.lib.util.log.MPTimber
import com.dl.rtc.calling.DLRTCCallService
import com.dl.rtc.calling.R
import com.dl.rtc.calling.base.DLRTCCalling
import com.dl.rtc.calling.base.impl.DLRTCInternalListenerManager
import com.dl.rtc.calling.model.DLRTCCallingConstants
import com.dl.rtc.calling.model.DLRTCDataMessageType
import com.dl.rtc.calling.model.DLRTCStartModel
import com.dl.rtc.calling.model.bean.DLRTCCallModel
import com.dl.rtc.calling.util.DLRTConversionUtil
import com.dl.rtc.calling.util.MediaPlayHelper
import com.tencent.imsdk.v2.V2TIMSimpleMsgListener
import com.tencent.imsdk.v2.V2TIMUserInfo
import com.tencent.qcloud.tuicore.TUILogin
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
                    receiveCall(rtcModel)
                }
            }
            ///拒绝邀请
            DLRTCDataMessageType.reject -> {
                //拒绝电话
                MPTimber.tag(TAG_LOG).e("拒绝邀请")
            }
            ///接受邀请
            DLRTCDataMessageType.accept -> {

                MPTimber.tag(TAG_LOG).e("接受邀请")
            }
            ///发起方取消邀请
            DLRTCDataMessageType.cancel -> {
                DLRTCInternalListenerManager.instance.onCallingCancel()
                MPTimber.tag(TAG_LOG).e("发起方取消邀请")
            }
            ///邀请超时
            DLRTCDataMessageType.timeout -> {
                DLRTCInternalListenerManager.instance.onCallingTimeout()
                initParams()
                MPTimber.tag(TAG_LOG).e("邀请超时")

            }
            /// 离开音视频房间
            DLRTCDataMessageType.exitRoom -> {
                initParams()
                MPTimber.tag(TAG_LOG).e("离开音视频房间")
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
        MPTimber.tag(TAG_LOG).e("当前处理接听页面回调 $interceptorResult")
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
}