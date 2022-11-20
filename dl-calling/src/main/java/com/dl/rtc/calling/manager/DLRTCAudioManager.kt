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
import com.dl.rtc.calling.model.DLRTCStartModel
import com.tencent.imsdk.v2.V2TIMSimpleMsgListener
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.trtc.TRTCCloudDef
import me.goldze.mvvmhabit.base.AppManager

/**
 *Author: 彭石林
 *Time: 2022/11/3 12:04
 * Description: 语音通话管理类
 */
class DLRTCAudioManager : DLRTCCallingItFace, DLRTCStartManagerDelegate {

    val TAG_LOG = "DLRTCAudioManager"
    //是否是主动拨打
    var inviteUserCall = false
    //是否是被动接听
    var receiveUserCall = false
    //保存信令模型
    var lastRtcModel : DLRTCStartModel? = null

    private var mTRTCInternalListenerManager : DLRTCInternalListenerManager? = null

    private fun initParams(){
        inviteUserCall = false
        receiveUserCall = false
    }

    companion object{
        val instance by lazy {
            DLRTCAudioManager().apply {
                mTRTCInternalListenerManager = DLRTCInternalListenerManager.instance
                DLRTCStartManager.instance.addRtcListener(this)
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
        DLRTCStartManager.instance.mTRTCCloud?.apply {
            val rtcParams = TRTCCloudDef.TRTCParams(
                TUILogin.getSdkAppId(), TUILogin.getUserId(),
                TUILogin.getUserSig(), mCurRoomID, "", ""
            )
            rtcParams.role = TRTCCloudDef.TRTCRoleAnchor
            enableAudioVolumeEvaluation(300)
            setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_SPEAKER)
            startLocalAudio(3)
            DLRTCStartManager.instance.setFramework()
            enterRoom(rtcParams, TRTCCloudDef.TRTC_APP_SCENE_AUDIOCALL)
        }
    }

    /**
     * 挂断电话
     */
    override fun hangup() {
        //DLRTCStartManager.instance.hangup()
    }

    /**
     * 进入房间
     */
    override fun enterRoom(roomId : Int) {
        enterRTCRoom(roomId)
    }

    /// 用进入RTC房间
/// - Parameter roomId: 整型的rtc房间id
/// - Parameter roomIds: 字符串类型的房间id，方便后续扩展
    public fun enterRTCRoom(roomId : Int,roomIds : String? = null){
        DLRTCStartManager.instance.accept()
    }

    /// 用户离开房间
    public fun exitRTCRoom(){
        DLRTCStartManager.instance.mTRTCCloud?.apply {
            stopLocalPreview()
            stopLocalAudio()
            exitRoom()
        }
    }

    /**
     * 推出房间
     */
    override fun exitRoom() {
        DLRTCStartManager.instance.exitRoom()
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

    override fun RTCStartManagerReciveMsg(manager: DLRTCStartManager, rtcModel: DLRTCStartModel) {
        MPTimber.tag(TAG_LOG).d("RTCStartManagerReciveMsg ： $rtcModel")
        MPTimber.tag(TAG_LOG).d("当前信令类型：${rtcModel.rtcDataMessageType}")
        when(rtcModel.rtcDataMessageType){
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
     * 拨打电话
     */
    fun inviteUserRTC(inviteUser : String, inviteType : String, roomId : Int, data :String){
        DLRTCStartManager.instance.inviteUserRTC(inviteUser,inviteType,roomId, object : DLRTCModuleClosuer{
            override fun callback(_success: Boolean, _errorCode: Int, _errorMsg: String?) {
                inviteUserCall = _success
                if(_success){
                    val mContext = DLRTCStartManager.instance.mContext!!
                    // 首次拨打电话，生成id
                    // 单聊发送C2C消息; 用C2C实现的多人通话,需要保存每个userId对应的callId
                    val intent = Intent(Intent.ACTION_VIEW)
                    if (inviteType == DLRTCDataMessageType.onlyAudio) {
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
    override fun inviteUserAccept(){
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
    }
}