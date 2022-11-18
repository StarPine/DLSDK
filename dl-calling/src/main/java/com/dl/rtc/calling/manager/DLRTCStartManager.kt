package com.dl.rtc.calling.manager

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.text.TextUtils
import com.blankj.utilcode.util.ServiceUtils
import com.dl.lib.util.log.MPTimber
import com.dl.rtc.calling.DLRTCCallService
import com.dl.rtc.calling.R
import com.dl.rtc.calling.base.DLRTCCalling
import com.dl.rtc.calling.base.impl.DLRTCInternalListenerManager
import com.dl.rtc.calling.model.DLRTCCallingConstants
import com.dl.rtc.calling.model.DLRTCOfflineMessageModel
import com.dl.rtc.calling.model.DLRTCSignalingManager
import com.dl.rtc.calling.model.bean.DLRTCCallModel
import com.dl.rtc.calling.model.bean.DLRTCSignallingData
import com.dl.rtc.calling.util.DLRTCSignallingUtil
import com.dl.rtc.calling.util.MediaPlayHelper
import com.dl.rtc.calling.util.PermissionUtil
import com.faceunity.core.enumeration.CameraFacingEnum
import com.faceunity.nama.FURenderer
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tencent.imsdk.v2.*
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.qcloud.tuicore.util.ConfigManagerUtil
import com.tencent.rtmp.ui.TXCloudVideoView
import com.tencent.trtc.TRTCCloud
import com.tencent.trtc.TRTCCloudDef.*
import com.tencent.trtc.TRTCCloudListener
import com.tencent.trtc.TRTCCloudListener.TRTCVideoFrameListener
import me.goldze.mvvmhabit.base.AppManager
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.abs

/**
 *Author: 彭石林
 *Time: 2022/11/3 15:47
 * Description: This is DLRTCStartManager
 */
class DLRTCStartManager {
    val TAGLOG = "DLRTCStartManager";

    //亮屏方案
    private var mSensorManager: SensorManager? = null
    private var mSensorEventListener: SensorEventListener? = null

    private val mMainHandler = Handler(Looper.getMainLooper())

    /**
     * 超时时间，单位秒
     */
    val TIME_OUT_COUNT = 30

    /**
     * C2C多人通话添加: 记录已经接通在TRTC房间内的远端用户
     */

    private var mMediaPlayHelper: MediaPlayHelper? = null

    /**
     * 最近使用的通话信令，用于快速处理
     */
    private var mLastCallModel: DLRTCCallModel = DLRTCCallModel()

    //通话邀请缓存,便于查询通话是否有效
    private val mInviteMap: MutableMap<String, DLRTCCallModel> = HashMap()


    /**
     * 当前通话的类型
     */
    private var mCurCallType: Int = DLRTCCallingConstants.TYPE_UNKNOWN

    private var mIsBeingCalled = true // 默认是被叫

    //多端登录增加字段:用于标记当前是否是自己发给自己的请求(多端触发),以及自己是否处理了该请求.
    private var mIsProcessedBySelf = false // 被叫方: 主动操作后标记是否自己处理了请求或回调


    private val CHECK_INVITE_PERIOD = 10 //邀请信令的检测周期（毫秒）

    private val CHECK_INVITE_DURATION = 100 //邀请信令的检测总时长（毫秒）

    val gsonBuilder by lazy { GsonBuilder() }

    ///当前model的类型，参考DLRTCDataMessageType的值
    var rtcDataMessageType : String? = null
    /// 当前发起邀请的ID
    var inviteUserId = ""
    ///接收方的id
    var acceptUserId = ""
    // /// 发起邀请成功，腾讯生成的一个Id
    var rtcInviteId  = ""
    ///发送邀请的类型
    var rtcInviteType : DLRTCCalling.DLInviteRTCType? = null
    var rtcInviteRoomId : Int = 0    /// 发起邀请的房间ID
    var rtcSignallingData = DLRTCSignallingData()

    /**
     * 是否首次邀请
     */
    private var isOnCalling = false

    /**
     * 当前是否在TRTC房间中
     */
    private var mIsInRoom = false
    private var mEnterRoomTime: Long = 0

    /**
     * C2C通话的邀请人
     * 例如A邀请B，B存储的mCurSponsorForMe为A
     */
    private var mCurSponsorForMe = ""


    private var initFlag = false

    //当前相机使用前后摄像头吧
    private var mIsUseFrontCamera = false

    /**
     * 上层传入回调
     */
    private var mTRTCInternalListenerManager: DLRTCInternalListenerManager? = null

    companion object {
        fun getInstance() = InstanceHelper.singleInstance
    }
    object InstanceHelper {
        val singleInstance = DLRTCStartManager()
    }

    var mContext : Context? = null
    /**
     * 底层SDK调用实例
     */
    var mTRTCCloud: TRTCCloud? = null

    //美颜相关
    private var mIsFuEffect = false
    private var mFURenderer: FURenderer? = null

    /**
     * 进行初始化，程序理应有1个静默式的接收处理逻辑（只调用一次）
     */
    fun init(mContexts: Context) {
        mContext = mContexts
        if(initFlag){
            return
        }
        mTRTCInternalListenerManager = DLRTCInternalListenerManager.instance
        mMediaPlayHelper = MediaPlayHelper(mContext)
        mTRTCCloud = TRTCCloud.sharedInstance(mContext);
        mTRTCCloud!!.setListener(mTRTCCloudListener)
        initFlag = false
        DLRTCIMSignallingManager.getInstance().addSignalingListener(mTIMSignallingListener)
        DLRTCIMSignallingManager.getInstance().addSimpleMsgListener(mTIMSimpleMsgListener)
    }

    /**
     * 主动呼叫方
     * inviteUser 接听人 inviteType 呼叫类型 roomId 房间ID launchView 是否启动view  data 自定义数据
     */
    fun inviteUserRTC(inviteUser : String, inviteType : DLRTCCalling.DLInviteRTCType, roomId : Int, launchView :Boolean, data :String){
        acceptUserId = inviteUser
        rtcInviteType = inviteType
        rtcInviteRoomId = roomId
        // 填充通话信令的model
        // 填充通话信令的model
        mLastCallModel.action = DLRTCCallModel.VIDEO_CALL_ACTION_DIALING
        mLastCallModel.invitedList = getAcceptUserId()
        mLastCallModel.roomId = rtcInviteRoomId
        mLastCallModel.callType = mCurCallType
        // 首次拨打电话，生成id
        // 单聊发送C2C消息; 用C2C实现的多人通话,需要保存每个userId对应的callId
        rtcInviteId = sendRTCModel(inviteUser,DLRTCCallModel.VIDEO_CALL_ACTION_DIALING)
        if(launchView){
            val intent = Intent(Intent.ACTION_VIEW)
            if (DLRTCCalling.DLInviteRTCType.dl_rtc_audio == inviteType) {
                intent.component = ComponentName(mContext!!.applicationContext, DLRTCInterceptorCall.instance.audioCallActivity)
            } else {
                intent.component = ComponentName(mContext!!.applicationContext, DLRTCInterceptorCall.instance.videoCallActivity)
            }
            intent.putExtra(DLRTCCallingConstants.PARAM_NAME_SPONSORID, TUILogin.getLoginUser())
            intent.putExtra(DLRTCCallingConstants.PARAM_NAME_ROLE, DLRTCCalling.Role.CALL)
            intent.putExtra("userProfile", data)
            intent.putExtra(DLRTCCallingConstants.PARAM_NAME_USERIDS, inviteUser)
            intent.putExtra("roomId", roomId)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            mContext!!.startActivity(intent)
        }

        mLastCallModel.callId = rtcInviteId
        DLRTCCallService.start(mContext!!)

    }

    fun getAcceptUserId() : ArrayList<String>{
        return ArrayList<String>().apply { add(acceptUserId) }
    }

    //应用在后台且没有拉起应用的权限时,上层主动调用该方法,查询有效的通话请求,拉起界面
    fun queryOfflineCallingInfo() {
        if (mInviteMap.isEmpty()) {
            MPTimber.tag(TAGLOG).d("queryOfflineCalledInfo: no offline call request")
            return
        }
        //有权限时,直接在onReceiveNewInvitation邀请回调中处理,这里不再重复处理
        if (PermissionUtil.hasPermission(mContext)) {
            MPTimber.tag(TAGLOG).d("queryOfflineCalledInfo: call request has processed")
            return
        }
        var inviteId = ""
        var model: DLRTCCallModel? = null
        mInviteMap.iterator().forEach {
            inviteId = it.key
            model = it.value
        }
        if (null == model) {
            return
        }
        MPTimber.tag(TAGLOG).d("queryOfflineCalledInfo: inviteId = $inviteId ,model = $model")
        mTIMSignallingListener.onReceiveNewInvitation(
            inviteId, model!!.sender,
            model!!.groupId, model!!.invitedList, model!!.data
        )
    }

    /**
     * 接听页面回调处理
     */
    private fun receiveCall(userIDs: Array<String?>?, type: DLRTCCalling.Type, roomId: Int, data: String?, isFromGroup : Boolean, sponsorID : String){
        //验证当前activity是否出现在需要进行音视频通话拦截额外处理的地方
        val interceptorResult = DLRTCInterceptorCall.instance.containsActivity(AppManager.getAppManager().currentActivity().javaClass)
        if(!interceptorResult){
            mMainHandler.post {
                startRing()
                val intent = Intent(Intent.ACTION_VIEW)
                if (DLRTCCalling.Type.AUDIO == type) {
                    intent.component = ComponentName(mContext!!.applicationContext, DLRTCInterceptorCall.instance.audioCallActivity)
                } else {
                    intent.component = ComponentName(mContext!!.applicationContext, DLRTCInterceptorCall.instance.videoCallActivity)
                }
                intent.putExtra(DLRTCCallingConstants.PARAM_NAME_SPONSORID, sponsorID)
                intent.putExtra(DLRTCCallingConstants.PARAM_NAME_ISFROMGROUP, isFromGroup)
                intent.putExtra(DLRTCCallingConstants.PARAM_NAME_ROLE, DLRTCCalling.Role.CALLED)
                intent.putExtra("userProfile", data)
                intent.putExtra(DLRTCCallingConstants.PARAM_NAME_USERIDS, userIDs)
                intent.putExtra(DLRTCCallingConstants.PARAM_NAME_GROUPID, "")
                intent.putExtra("roomId", roomId)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                mContext!!.startActivity(intent)
            }
        }else{
            var userIdResult = ""
            if(!userIDs.isNullOrEmpty()){
                userIdResult = userIDs[0].toString()
            }
            DLRTCInterceptorCall.instance.notifyInterceptorCall(userIdResult, type, roomId, data, isFromGroup, sponsorID)
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


    /**
     * 消息监听器,收到 C2C 自定义（信令）消息
     */
    private val mTIMSimpleMsgListener: V2TIMSimpleMsgListener = object : V2TIMSimpleMsgListener() {
        /**
         * 接收C2C自定义消息
         */
        override fun onRecvC2CCustomMessage(msgID: String, sender: V2TIMUserInfo, customData: ByteArray) {
            MPTimber.tag(TAGLOG).d("onRecvC2CCustomMessage ：Start")
            val customStr = String(customData)
            if (TextUtils.isEmpty(customStr)) {
                return
            }
            MPTimber.tag(TAGLOG).d("onRecvC2CCustomMessage ：customData ： $customStr")
            val signallingData = DLRTCSignallingUtil.convert2CallingData(customStr)
            MPTimber.tag(TAGLOG).d("信令接收处理：$signallingData")
            if (null == signallingData.data || null == signallingData.businessID || signallingData.businessID != DLRTCCallModel.VALUE_BUSINESS_ID) {
                MPTimber.tag(TAGLOG).d("this is not the calling scene")
                return
            }
            if (null == signallingData.data!!.cmd || signallingData.data!!.cmd != DLRTCCallModel.VALUE_MSG_SYNC_INFO) {
                MPTimber.tag(TAGLOG).d( "onRecvC2CCustomMessage: invalid message")
                return
            }
            MPTimber.tag(TAGLOG).d("onRecvC2CCustomMessage inviteID: $msgID, sender: ${sender.userID} data:" + customStr)
            val inviter: String? = signallingData.user
            when (signallingData.callAction) {
                //接听电话
                DLRTCCallModel.VIDEO_CALL_ACTION_ACCEPT -> mTIMSignallingListener.onInviteeAccepted(signallingData.callId, inviter, customStr)
                //拒接电话
                DLRTCCallModel.VIDEO_CALL_ACTION_REJECT -> mTIMSignallingListener.onInviteeRejected(signallingData.callId, inviter, customStr)
                //发起人取消
                DLRTCCallModel.VIDEO_CALL_ACTION_SPONSOR_CANCEL -> mTIMSignallingListener.onInvitationCancelled(signallingData.callId, inviter, customStr)
                //无人接听
                DLRTCCallModel.VIDEO_CALL_ACTION_SPONSOR_TIMEOUT -> {
                    val userList: MutableList<String> = ArrayList()
                    signallingData.user?.let { userList.add(it) }
                    mTIMSignallingListener.onInvitationTimeout(signallingData.callId, userList)
                }
                else -> {}
            }
        }
    }

    /**
     * 信令监听器
     */
    private val mTIMSignallingListener: V2TIMSignalingListener = object : V2TIMSignalingListener() {
        /**
         * 接收新的信令消息
         */
        override fun onReceiveNewInvitation(inviteID: String, inviter: String, groupID: String, inviteeList: List<String>, data: String) {
            MPTimber.tag(TAGLOG).d("onReceiveNewInvitation inviteID:" + inviteID + ", inviter:" + inviter
                    + ", groupID:" + groupID + ", inviteeList:" + inviteeList + " data:" + data)
            handleRecvCallModel(inviteID, inviter, groupID, inviteeList as ArrayList, data)
        }

        /**
         * 接听信令消息
         */
        override fun onInviteeAccepted(inviteID: String, invitee: String, data: String) {
            MPTimber.tag(TAGLOG).d("onInviteeAccepted inviteID:$inviteID, invitee:$invitee data:$data")
            val signallingData: DLRTCSignallingData = DLRTCSignallingUtil.convert2CallingData(data)
            if (!DLRTCSignallingUtil.isCallingData(signallingData)) {
                MPTimber.tag(TAGLOG).d("接听信令消息 this is not the calling scene ")
                return
            }

            //多端登录:A1,A2登录同一账号,账户B呼叫A,A1接听正常处理,A2退出界面
            if (!mIsProcessedBySelf && !TextUtils.isEmpty(invitee) && invitee == TUILogin.getLoginUser()) {
                stopCall()
                preExitRoom(null)
                return
            }
            MPTimber.tag(TAGLOG).d("当前远端用户为：${acceptUserId} ")
                    DLRTCSignalingManager.sendInviteAction(
                        action = DLRTCCallModel.VIDEO_CALL_ACTION_ACCEPT,
                        invitee = rtcInviteId,
                        userId = inviteUserId,
                        mData = data,
                        callIDWithUserID = rtcInviteId,
                        mCurInvitedList = getAcceptUserId()
                    )
        }

        /**
         * 拒接信令消息
         */
        override fun onInviteeRejected(inviteID: String, invitee: String, data: String) {
            MPTimber.tag(TAGLOG).d("onInviteeRejected inviteID:$inviteID, invitee:$invitee data:$data")
            val signallingData: DLRTCSignallingData = DLRTCSignallingUtil.convert2CallingData(data)
            if (!DLRTCSignallingUtil.isCallingData(signallingData)) {
                MPTimber.tag(TAGLOG).d( "this is not the calling scene ")
                return
            }
            if (DLRTCSignallingUtil.isSwitchAudioData(signallingData)) {
                val message: String? = DLRTCSignallingUtil.getSwitchAudioRejectMessage(signallingData)
                onSwitchToAudio(false, message.orEmpty())
                return
            }
                    if (invitee != acceptUserId) {
                        DLRTCSignalingManager.sendInviteAction(
                            DLRTCCallModel.VIDEO_CALL_ACTION_REJECT,
                            invitee = invitee,
                            userId = acceptUserId,
                            mData = data,
                            callIDWithUserID = rtcInviteId,
                            mCurInvitedList = getAcceptUserId()
                        )
                    }
            MPTimber.tag(TAGLOG).d("onInviteeRejected: rtcInviteId = $rtcInviteId")
            if (TextUtils.isEmpty(rtcInviteId) || inviteID != rtcInviteId) {
                return
            }

            //多端登录增加逻辑:如果是自己的话,不在处理后续
            if (!TextUtils.isEmpty(invitee) && invitee == TUILogin.getLoginUser()) {
                stopCall()
                preExitRoom(null)
                stopRing()
                return
            }
            if (DLRTCSignallingUtil.isLineBusy(signallingData)) {
                mTRTCInternalListenerManager?.onLineBusy(invitee)
            } else {
                mTRTCInternalListenerManager?.onReject(invitee)
            }
            MPTimber.tag(TAGLOG).d("mIsInRoom=$mIsInRoom")
            preExitRoom(null)
            stopMusic()
            unregisterSensorEventListener()
        }

        /**
         * 取消信令消息
         */
        override fun onInvitationCancelled(inviteID: String, inviter: String, data: String) {
            MPTimber.tag(TAGLOG).d("onInvitationCancelled inviteID:$inviteID inviter:$inviter data:$data")
            val signallingData = DLRTCSignallingUtil.convert2CallingData(data)
            if (!DLRTCSignallingUtil.isCallingData(signallingData)) {
                MPTimber.tag(TAGLOG).d("this is not the calling scene ")
                return
            }
            val curCallId: String
            MPTimber.tag(TAGLOG).d(" 当前是否是多人会话${rtcInviteId} 数据为：${acceptUserId.toString()}")
            if (inviter != acceptUserId) {
                DLRTCSignalingManager.sendInviteAction(action = DLRTCCallModel.VIDEO_CALL_ACTION_SPONSOR_CANCEL, invitee = inviter, userId = acceptUserId, mData = data, callIDWithUserID = acceptUserId, mCurInvitedList = getAcceptUserId())
            }
            MPTimber.tag(TAGLOG).d("onInvitationCancelled: inviteUserId = $inviteUserId")
            if (inviteID == rtcInviteId) {
                playHangupMusic()
                stopCall()
                exitRoom()
                mTRTCInternalListenerManager?.onCallingCancel()
            }
            //移除缓存数据
            mInviteMap.remove(inviteID)
            stopRing()
        }

        //C2C多人通话超时逻辑:
        //对主叫来说:
        //1. C2C多人通话只有所有用户都超时,主叫需要退出,提示"**无响应";
        //2. 某个用户已经接听后,主叫不再处理退房等超时逻辑,只更新UI,提示"**无响应";
        //对被叫来说(有唯一的callID)
        //1.如果自己超时,直接退房并通知主叫;
        //2.收到某个用户的超时,不需要处理退房逻辑,自己只需要更新UI,提示"**无响应";
        override fun onInvitationTimeout(inviteID: String, inviteeList: List<String>) {
            MPTimber.tag(TAGLOG).d("onInvitationTimeout inviteID : $inviteID , rtcInviteId : $rtcInviteId ,inviteeList: $inviteeList")
            //移除缓存数据
            mInviteMap.remove(inviteID)
            preExitRoom(null)
            mTRTCInternalListenerManager?.onNoResp(acceptUserId)
            stopMusic()
            unregisterSensorEventListener()
//            val curCallId: String
//            if (checkIsHasGroupIDCall()) {
//                curCallId = getCallIDWithUserID(inviteeList[0]) as String
//                val invitee = inviteeList[0]
//                    if (invitee != acceptUserId) {
//                        DLRTCSignalingManager.sendInviteAction(
//                            action = DLRTCCallModel.VIDEO_CALL_ACTION_SPONSOR_TIMEOUT,
//                            invitee = invitee,
//                            userId = acceptUserId,
//                            mData = null,
//                            callIDWithUserID = getCallIDWithUserID(acceptUserId) as String,
//                            mCurInvitedList = getAcceptUserId()
//                        )
//                    }
//            } else {
//                curCallId = rtcInviteId
//            }
//            MPTimber.tag(TAGLOG).d("curCallId : $curCallId , rtcInviteId : $rtcInviteId")
//            if (inviteID != curCallId) {
//                return
//            }
//            // 邀请者
//            if (TextUtils.isEmpty(mCurSponsorForMe)) {
//                //1.主叫所有用户都超时,也就是没人接听->主叫处理退房逻辑;
//                if (mRemoteUserInTRTCRoom.size == 0) {
//                    for (userID in inviteeList) {
//                        mTRTCInternalListenerManager?.onNoResp(acceptUserId)
//                        mCurInvitedList.remove(userID)
//                        acceptUserId = null
//                    }
//                    stopMusic()
//                    preExitRoom(null)
//                    playHangupMusic()
//                    unregisterSensorEventListener()
//                } else {
//                    //2.主叫端:某个用户接听后,还有其他用户超时信息,则只回调到上层更新主叫界面该超时用户的UI
//                    for (userID in inviteeList) {
//                        mTRTCInternalListenerManager?.onNoResp(userID)
//                        mCurInvitedList.remove(userID)
//                        acceptUserId = null
//                    }
//                }
//            } else {
//                //被邀请者
//                MPTimber.tag(TAGLOG).d("mCurInvitedList = $mCurInvitedList, mCurRoomRemoteUserSet = $mCurRoomRemoteUserSet")
//                // 1.自己超时
//                if (inviteeList.contains(TUILogin.getUserId())) {
//                    stopCall()
//                    mTRTCInternalListenerManager?.onCallingTimeout()
//                    mCurInvitedList.removeAll(inviteeList)
//                    mCurRoomRemoteUserSet.removeAll(inviteeList)
//                    preExitRoom(null)
//                    playHangupMusic()
//                    unregisterSensorEventListener()
//                    return
//                }
//                //2.其他人超时,不处理退房逻辑,只更新超时用户的UI
//                for (id in inviteeList) {
//                    mTRTCInternalListenerManager?.onNoResp(id)
//                    mCurInvitedList.remove(id)
//                    mCurRoomRemoteUserSet.remove(id)
//                }
//            }
        }
    }

    /**
     * 处理收到拨打信令过来
     */
    private fun handleRecvCallModel(inviteID: String, inviter: String, groupID: String, inviteeList: ArrayList<String>, data: String) {
        val signallingData: DLRTCSignallingData = DLRTCSignallingUtil.convert2CallingData(data)
        if (!DLRTCSignallingUtil.isCallingData(signallingData)) {
            MPTimber.tag(TAGLOG).d("this is not the calling scene ")
            return
        }
        if (null != inviteeList && !inviteeList.contains(TUILogin.getUserId())) {
            MPTimber.tag(TAGLOG).d("this invitation is not for me")
            return
        }
        if (!TextUtils.isEmpty(inviter) && inviter == TUILogin.getLoginUser()) {
            MPTimber.tag(TAGLOG).d("this is MultiTerminal invitation ,ignore")
            return
        }

        //被叫端缓存收到的通话请求
        val callModel = DLRTCCallModel()
        callModel.sender = inviter
        callModel.groupId = groupID
        callModel.invitedList = inviteeList
        callModel.data = data
        mInviteMap[inviter] = callModel

        //如果应用在后台,且没有允许后台拉起应用的权限时返回
        if (!mContext?.let { isAppRunningForeground(it) }!! && !PermissionUtil.hasPermission(mContext)) {
            MPTimber.tag(TAGLOG).d("isAppRunningForeground is false")
            return
        }
        //创建对象后调用作用域。持有当前对象本身进行赋值
        val handleCallModel = DLRTCCallModel().apply {
            callId = inviteID
            groupId = groupID
            action = DLRTCCallModel.VIDEO_CALL_ACTION_DIALING
            invitedList = inviteeList
        }
        if (DLRTCSignallingUtil.isNewSignallingVersion(signallingData)) {
            handleNewSignallingInvite(signallingData, handleCallModel, inviter)
        } else {
            handleOldSignallingInvite(signallingData, handleCallModel, inviter)
        }
    }

    private fun handleOldSignallingInvite(signallingData: DLRTCSignallingData, callModel: DLRTCCallModel, inviter: String) {
        callModel.callType = signallingData.callType
        mCurCallType = callModel.callType
        callModel.roomId = signallingData.roomId
        if (signallingData.callEnd != 0) {
            preExitRoom(null)
            return
        }
        if (DLRTCCallModel.SIGNALING_EXTRA_KEY_SWITCH_AUDIO_CALL == signallingData.switchToAudioCall) {
            return
        }
        handleDialing(callModel, inviter)
        if (rtcInviteId == callModel.callId) {
            mLastCallModel = callModel.clone() as DLRTCCallModel
        }
    }

    private fun handleDialing(callModel: DLRTCCallModel, user: String) {
        if (!TextUtils.isEmpty(rtcInviteId)) {
            // 正在通话时，收到了一个邀请我的通话请求,需要告诉对方忙线
            if (isOnCalling && callModel.invitedList!!.contains(TUILogin.getUserId())) {
                sendModel(user, DLRTCCallModel.VIDEO_CALL_ACTION_LINE_BUSY, callModel, null)
                return
            }
        }

        // 虽然是群组聊天，但是对方并没有邀请我，我不做处理
        if (!TextUtils.isEmpty(callModel.groupId) && !callModel.invitedList?.contains(TUILogin.getUserId())!!) {
            return
        }
        // 开始接通电话
        startCall()
        rtcInviteId = callModel.callId.toString()
        rtcInviteRoomId = callModel.roomId
        mCurCallType = callModel.callType
        mCurSponsorForMe = user
        // 邀请列表中需要移除掉自己
        callModel.invitedList?.remove(TUILogin.getUserId())
        val onInvitedUserListParam: List<String>? = callModel.invitedList
        callModel.invitedList?.let { getAcceptUserId() }
        if (mTRTCInternalListenerManager != null) {
            val startTime = System.currentTimeMillis()
            val callType = mCurCallType
            val task: Runnable = object : Runnable {
                override fun run() {
                    if (mIsBeingCalled && System.currentTimeMillis() - startTime < CHECK_INVITE_DURATION) { // 接听方
                        MPTimber.tag(TAGLOG).d("check invitation...")
                        if (isValidInvite()) {
                            mMainHandler.postDelayed(this, CHECK_INVITE_PERIOD.toLong()) // 多次检测
                        } else {
                            MPTimber.tag(TAGLOG).w("this invitation is invalid")
                            mMainHandler.removeCallbacks(this)
                        }
                        return
                    }
                    mMainHandler.removeCallbacks(this)
                    mTRTCInternalListenerManager!!.onInvited(user, onInvitedUserListParam, false, callType)
                }
            }
            mMainHandler.post(task)
        }
        if(mCurCallType != 0){
            val callType: DLRTCCalling.Type = if(mCurCallType == 1){
                DLRTCCalling.Type.AUDIO
            }else{
                DLRTCCalling.Type.VIDEO
            }
            val userLists : Array<String?> = arrayOf(mCurSponsorForMe)
            receiveCall(userLists, callType, callModel.roomId, null,false,mCurSponsorForMe);
        }
        inviteUserId = user
    }
    private fun startCall() {
        isOnCalling = true
        registerSensorEventListener()
    }

    fun isValidInvite(): Boolean {
        if (mInviteMap.isEmpty()) {
            MPTimber.tag(TAGLOG).d("isValidInvite: mInviteMap = $mInviteMap")
            return false
        }
        MPTimber.tag(TAGLOG).d("isValidInvite rtcInviteId = $rtcInviteId ,mInviteMap = $mInviteMap")
        val model: DLRTCCallModel? = mInviteMap[rtcInviteId]
        return model != null
    }

    private fun isCollectionEmpty(coll: Collection<*>?): Boolean {
        return coll == null || coll.isEmpty()
    }

    /**
     * 新消息信令处理
     */
    private fun handleNewSignallingInvite(signallingData: DLRTCSignallingData, callModel: DLRTCCallModel, inviter: String) {
        val dataInfo: DLRTCSignallingData.DataInfo? = signallingData.data
        if (dataInfo == null) {
            MPTimber.tag(TAGLOG).i("signallingData dataInfo is null")
            return
        }
        if (TextUtils.isEmpty(callModel.groupId)) {
            val list: List<String>? = dataInfo.userIDs
            callModel.invitedList = (list ?: callModel.invitedList as ArrayList) as ArrayList<String>?
        }
        callModel.roomId = dataInfo.roomID
        //语音电话呼叫
        if (DLRTCCallModel.VALUE_CMD_AUDIO_CALL == dataInfo.cmd) {
            callModel.callType = DLRTCCallingConstants.TYPE_AUDIO_CALL
            mCurCallType = callModel.callType
        } else if (DLRTCCallModel.VALUE_CMD_VIDEO_CALL == dataInfo.cmd) {
            //视频电话呼叫
            callModel.callType = DLRTCCallingConstants.TYPE_VIDEO_CALL
            mCurCallType = callModel.callType
        }
        //挂断
        if (DLRTCCallModel.VALUE_CMD_HAND_UP == dataInfo.cmd) {
            preExitRoom(null)
            return
        }
        //切换为语音通话
        if (DLRTCCallModel.VALUE_CMD_SWITCH_TO_AUDIO == dataInfo.cmd) {
            return
        }
        handleDialing(callModel, inviter)
        if (rtcInviteId == callModel.callId) {
            mLastCallModel = callModel.clone() as DLRTCCallModel
        }
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
     * 判断应用后台
     */
    private fun isAppRunningForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcessInfos = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName
        for (appProcessInfo in runningAppProcessInfos) {
            if (appProcessInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && appProcessInfo.processName == packageName
            ) {
                return true
            }
        }
        return false
    }

    private fun sendModel(user: String,action: Int): String? {
        return sendModel(user, action,null,null)
    }

    /**
     * 发送信令
     */
    private fun sendRTCModel(user: String,rtcAction: Int): String{
        //返回信令
        var resultInvite : String? = null
        val realCallModel = DLRTCSignalingManager.generateModel(rtcAction,mLastCallModel)
        if (rtcAction == DLRTCCallModel.VIDEO_CALL_ACTION_HANGUP && mEnterRoomTime != 0L) {
            realCallModel.duration = (System.currentTimeMillis() - mEnterRoomTime).toInt() / 1000
            mEnterRoomTime = 0
        }
        realCallModel.callType = if(rtcInviteType == DLRTCCalling.DLInviteRTCType.dl_rtc_audio) DLRTCCallingConstants.TYPE_AUDIO_CALL else DLRTCCallingConstants.TYPE_VIDEO_CALL
        val inviteId : String = rtcInviteId
        val signallingData: DLRTCSignallingData = DLRTCSignallingUtil.createSignallingData()
        signallingData.callType = realCallModel.callType
        signallingData.roomId = realCallModel.roomId
        realCallModel.action = rtcAction
        signallingData.callAction = rtcAction
        when (realCallModel.action) {
            //正在呼叫
            DLRTCCallModel.VIDEO_CALL_ACTION_DIALING -> {
                val callDataInfo = DLRTCSignallingData.DataInfo()
                callDataInfo.DLRTCInviteRoomID = realCallModel.roomId
                callDataInfo.DLRtcInviteUserID = user
                callDataInfo.DLRTCAcceptUserID = TUILogin.getLoginUser()

                callDataInfo.cmd = when(realCallModel.callType){
                    DLRTCCallingConstants.TYPE_AUDIO_CALL->{
                        DLRTCCallModel.VALUE_CMD_AUDIO_CALL
                    }
                    DLRTCCallingConstants.TYPE_VIDEO_CALL->{
                        DLRTCCallModel.VALUE_CMD_VIDEO_CALL
                    }
                    else -> {
                        null
                    }
                }
                if(callDataInfo.cmd !=null){
                    callDataInfo.roomID = realCallModel.roomId
                    signallingData.data = callDataInfo
                    addFilterKey(gsonBuilder, DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_END)
                    callDataInfo.userIDs = realCallModel.invitedList
                    val dialingDataStr = gsonBuilder.create().toJson(signallingData)
                    realCallModel.callId = rtcInviteId
                    realCallModel.timeout = TIME_OUT_COUNT
                    realCallModel.version = DLRTCCallModel.VALUE_VERSION
                    val pushInfo: V2TIMOfflinePushInfo = DLRTCOfflineMessageModel.createV2TIMOfflinePushInfo(realCallModel, user, user,null)
                    //发送信令
                    resultInvite = DLRTCSignalingManager.sendInvite(user, dialingDataStr, pushInfo, TIME_OUT_COUNT, object : V2TIMCallback {
                            override fun onError(code: Int, desc: String) {
                                MPTimber.tag(TAGLOG).e("invite failed callID:" + realCallModel.callId + ",error:" + code + " desc:" + desc)
                            }

                            override fun onSuccess() {
                                MPTimber.tag(TAGLOG).d("invite success:$realCallModel")
                                realCallModel.callId = rtcInviteId
                                realCallModel.timeout = TIME_OUT_COUNT
                                realCallModel.version =
                                    DLRTCCallModel.VALUE_VERSION
                                //                            sendOnlineMessageWithOfflinePushInfo(user, realCallModel);
                            }
                        })
                }

            }
            //接听电话
            DLRTCCallModel.VIDEO_CALL_ACTION_ACCEPT -> {
                inviteUserAccept(inviteId)
            }
            //拒接电话
            DLRTCCallModel.VIDEO_CALL_ACTION_REJECT -> {
                inviteUserReject(inviteId!!);
            }
            //电话占线
            DLRTCCallModel.VIDEO_CALL_ACTION_LINE_BUSY -> {
                addFilterKey(gsonBuilder, DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_END)
                val lineBusyDataInfo = DLRTCSignallingData.DataInfo()
                signallingData.lineBusy = (DLRTCCallModel.SIGNALING_EXTRA_KEY_LINE_BUSY)
                lineBusyDataInfo.message =(DLRTCCallModel.VALUE_MSG_LINE_BUSY)
                signallingData.data = (lineBusyDataInfo)
                val lineBusyMapStr = Gson().toJson(signallingData)
                realCallModel.callId?.let {
                    inviteUserReject(it);
                }
            }
            //发起人取消
            DLRTCCallModel.VIDEO_CALL_ACTION_SPONSOR_CANCEL -> {
                inviteUserCancel(inviteId)
            }
            //挂断
            DLRTCCallModel.VIDEO_CALL_ACTION_HANGUP -> {
                val hangupDataInfo = DLRTCSignallingData.DataInfo()
                signallingData.callEnd = (realCallModel.duration)
                hangupDataInfo.cmd = (DLRTCCallModel.VALUE_CMD_HAND_UP)
                signallingData.data = (hangupDataInfo)
                val hangupMapStr = gsonBuilder.create().toJson(signallingData)
                DLRTCSignalingManager.sendInvite(user, hangupMapStr, null, 0, object : V2TIMCallback {
                        override fun onError(code: Int, desc: String) {
                            MPTimber.tag(TAGLOG)
                                .e("inviteInGroup-->hangup failed callID:" + realCallModel.callId + ", error:" + code + " desc:" + desc)
                        }

                        override fun onSuccess() {
                            MPTimber.tag(TAGLOG)
                                .d("inviteInGroup-->hangup success callID:" + realCallModel.callId)
                        }
                    })
            }
        }
        return if(resultInvite.isNullOrEmpty()){
            ""
        }else{
            resultInvite
        }
    }

    /**
     * 信令发送函数，当CallModel 存在groupId时会向群组发送信令
     *
     * @param user
     * @param action
     * @param model
     * @param message
     */
    private fun sendModel(
        user: String,
        action: Int,
        model: DLRTCCallModel? = null,
        message: String? = null
    ): String? {
        var callID: String? = null
        val realCallModel: DLRTCCallModel
        if (model != null) {
            realCallModel = model.clone() as DLRTCCallModel
            realCallModel.action = action
        } else {
            realCallModel = DLRTCSignalingManager.generateModel(action,mLastCallModel)
        }
        if (action == DLRTCCallModel.VIDEO_CALL_ACTION_HANGUP && mEnterRoomTime != 0L) {
            realCallModel.duration = (System.currentTimeMillis() - mEnterRoomTime).toInt() / 1000
            mEnterRoomTime = 0
        }
        val inviteId = rtcInviteId
        val signallingData: DLRTCSignallingData = DLRTCSignallingUtil.createSignallingData()
        signallingData.callType = realCallModel.callType
        signallingData.roomId = realCallModel.roomId
        val gsonBuilder = GsonBuilder()
        when (realCallModel.action) {
            //正在呼叫
            DLRTCCallModel.VIDEO_CALL_ACTION_DIALING -> {
                signallingData.roomId = realCallModel.roomId
                val callDataInfo = DLRTCSignallingData.DataInfo()
                callDataInfo.cmd = when(realCallModel.callType){
                    DLRTCCallingConstants.TYPE_AUDIO_CALL->{
                        DLRTCCallModel.VALUE_CMD_AUDIO_CALL
                    }
                    DLRTCCallingConstants.TYPE_VIDEO_CALL->{
                        DLRTCCallModel.VALUE_CMD_VIDEO_CALL
                    }
                    else -> {
                        null
                    }
                }
                if(callDataInfo.cmd !=null){
                    callDataInfo.roomID = realCallModel.roomId
                    signallingData.data = callDataInfo
                    addFilterKey(
                        gsonBuilder,
                        DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_END
                    )
                    callDataInfo.userIDs = realCallModel.invitedList
                    val dialingDataStr = gsonBuilder.create().toJson(signallingData)
                    realCallModel.callId = rtcInviteId
                    realCallModel.timeout = TIME_OUT_COUNT
                    realCallModel.version = DLRTCCallModel.VALUE_VERSION
                    val pushInfo: V2TIMOfflinePushInfo = DLRTCOfflineMessageModel.createV2TIMOfflinePushInfo(realCallModel, user, user,null)
                    //发送信令
                    callID = DLRTCSignalingManager.sendInvite(
                        user,
                        dialingDataStr,
                        pushInfo,
                        TIME_OUT_COUNT,
                        object : V2TIMCallback {
                            override fun onError(code: Int, desc: String) {
                                MPTimber.tag(TAGLOG)
                                    .e("invite failed callID:" + realCallModel.callId + ",error:" + code + " desc:" + desc)
                            }

                            override fun onSuccess() {
                                MPTimber.tag(TAGLOG).d("invite success:$realCallModel")
                                realCallModel.callId = rtcInviteId
                                realCallModel.timeout = TIME_OUT_COUNT
                                realCallModel.version =
                                    DLRTCCallModel.VALUE_VERSION
                                //                            sendOnlineMessageWithOfflinePushInfo(user, realCallModel);
                            }
                        })
                }

            }
            //接听电话
            DLRTCCallModel.VIDEO_CALL_ACTION_ACCEPT -> {
                addFilterKey(gsonBuilder, DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_END)
                val acceptDataStr = gsonBuilder.create().toJson(signallingData)
                DLRTCSignalingManager.acceptInvite(inviteId, acceptDataStr, object : V2TIMCallback {
                    override fun onError(code: Int, desc: String) {
                        MPTimber.tag(TAGLOG).e("accept failed callID:" + realCallModel.callId + ", error:" + code + " desc:" + desc)
                        mTRTCInternalListenerManager?.onError(code, desc)
                    }
                    override fun onSuccess() {
                        MPTimber.tag(TAGLOG).d("accept success callID:" + realCallModel.callId)
                    }
                })
            }
            //拒接电话
            DLRTCCallModel.VIDEO_CALL_ACTION_REJECT -> {
                addFilterKey(gsonBuilder, DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_END)
                val rejectDataStr = gsonBuilder.create().toJson(signallingData)
                MPTimber.tag(TAGLOG).e(" rejectInvite send inviteId ${inviteId.toString()} , rejectDataStr： $rejectDataStr")
                inviteUserReject(inviteId)
            }
            //电话占线
            DLRTCCallModel.VIDEO_CALL_ACTION_LINE_BUSY -> {
                addFilterKey(
                    gsonBuilder,
                    DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_END
                )
                val lineBusyDataInfo = DLRTCSignallingData.DataInfo()
                signallingData.lineBusy = (DLRTCCallModel.SIGNALING_EXTRA_KEY_LINE_BUSY)
                lineBusyDataInfo.message =(DLRTCCallModel.VALUE_MSG_LINE_BUSY)
                signallingData.data = (lineBusyDataInfo)
                val lineBusyMapStr = Gson().toJson(signallingData)
                realCallModel.callId?.let {
                    inviteUserReject(it);
                }
            }
            //发起人取消
            DLRTCCallModel.VIDEO_CALL_ACTION_SPONSOR_CANCEL -> {
                inviteUserCancel(inviteId)
            }
            //挂断
            DLRTCCallModel.VIDEO_CALL_ACTION_HANGUP -> {
                val hangupDataInfo = DLRTCSignallingData.DataInfo()
                signallingData.callEnd = (realCallModel.duration)
                hangupDataInfo.cmd = (DLRTCCallModel.VALUE_CMD_HAND_UP)
                signallingData.data = (hangupDataInfo)
                val hangupMapStr = gsonBuilder.create().toJson(signallingData)
                DLRTCSignalingManager.sendInvite(
                    user,
                    hangupMapStr,
                    null,
                    0,
                    object : V2TIMCallback {
                        override fun onError(code: Int, desc: String) {
                            MPTimber.tag(TAGLOG)
                                .e("inviteInGroup-->hangup failed callID:" + realCallModel.callId + ", error:" + code + " desc:" + desc)
                        }

                        override fun onSuccess() {
                            MPTimber.tag(TAGLOG)
                                .d("inviteInGroup-->hangup success callID:" + realCallModel.callId)
                        }
                    })
            }
            //切换语音电话
            DLRTCCallModel.VIDEO_CALL_SWITCH_TO_AUDIO_CALL -> {
                addFilterKey(
                    gsonBuilder,
                    DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_END
                )
                val switchAudioCallDataInfo = DLRTCSignallingData.DataInfo()
                switchAudioCallDataInfo.cmd = (DLRTCCallModel.VALUE_CMD_SWITCH_TO_AUDIO)
                signallingData.switchToAudioCall = (DLRTCCallModel.VALUE_CMD_SWITCH_TO_AUDIO)
                signallingData.data = (switchAudioCallDataInfo)
                val switchAudioCall = gsonBuilder.create().toJson(signallingData)
                callID = DLRTCSignalingManager.sendInvite(
                    user,
                    switchAudioCall,
                    null,
                    TIME_OUT_COUNT,
                    object : V2TIMCallback {
                        override fun onError(code: Int, desc: String) {
                            MPTimber.tag(TAGLOG)
                                .e("invite-->switchAudioCall failed callID: " + realCallModel.callId + ", error:" + code + " desc:" + desc)
                        }

                        override fun onSuccess() {
                            MPTimber.tag(TAGLOG)
                                .d("invite-->switchAudioCall success callID:" + realCallModel.callId)
                        }
                    })
            }
            //接收切换为语音电话
            DLRTCCallModel.VIDEO_CALL_ACTION_ACCEPT_SWITCH_TO_AUDIO -> {
                addFilterKey(
                    gsonBuilder,
                    DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_END
                )
                val acceptSwitchAudioCallData = DLRTCSignallingData.DataInfo()
                acceptSwitchAudioCallData.cmd = (DLRTCCallModel.VALUE_CMD_SWITCH_TO_AUDIO)
                signallingData.switchToAudioCall = (DLRTCCallModel.VALUE_CMD_SWITCH_TO_AUDIO)
                signallingData.data = (acceptSwitchAudioCallData)
                val acceptSwitchAudioDataStr = gsonBuilder.create().toJson(signallingData)
                DLRTCSignalingManager.acceptInvite(inviteId, acceptSwitchAudioDataStr, object : V2TIMCallback {
                    override fun onError(code: Int, desc: String) {
                        MPTimber.tag(TAGLOG).e("accept switch audio call failed callID:" + realCallModel.callId + ", error:" + code + " desc:" + desc)
                    }

                    override fun onSuccess() {
                        MPTimber.tag(TAGLOG).d("accept switch audio call success callID:" + realCallModel.callId)
                        //realSwitchToAudioCall()
                    }
                })
            }
            else -> {}
        }

        // 最后需要重新赋值
        updateLastCallModel(realCallModel, callID, model)
        MPTimber.tag(TAGLOG).d("callID=$callID , rtcInviteId : $rtcInviteId")
        return callID
    }

    //在json中过滤不需要的key
    private fun addFilterKey(builder: GsonBuilder, vararg keys: String) {
        for (key in keys) {
            builder.setExclusionStrategies(object : ExclusionStrategy {
                override fun shouldSkipField(f: FieldAttributes): Boolean {
                    return f.name.contains(key)
                }

                override fun shouldSkipClass(clazz: Class<*>?): Boolean {
                    return false
                }
            })
        }
    }

    private fun updateLastCallModel(realCallModel: DLRTCCallModel, callID: String? = null, oldModel: DLRTCCallModel?) {
        if (realCallModel.action != DLRTCCallModel.VIDEO_CALL_ACTION_REJECT && realCallModel.action != DLRTCCallModel.VIDEO_CALL_ACTION_HANGUP && realCallModel.action != DLRTCCallModel.VIDEO_CALL_ACTION_SPONSOR_CANCEL && oldModel == null) {
            mLastCallModel =
                realCallModel.clone() as DLRTCCallModel
        }
    }

    private fun onSwitchToAudio(success: Boolean, message: String) {
        mTRTCInternalListenerManager?.onSwitchToAudio(success, message)
    }

    /**
     * 渲染视频
     */
    fun startRemoteView(userId: String?, txCloudVideoView: TXCloudVideoView?) {
        if (txCloudVideoView == null) {
            return
        }
        mTRTCCloud!!.startRemoteView(userId,0, txCloudVideoView)
    }

    /**
     * 停止渲染视频
     */
    fun stopRemoteView(userId: String?) {
        mTRTCCloud!!.stopRemoteView(userId,0)
    }

    /**
     * 切换相机
     */
    fun switchCamera(isFrontCamera: Boolean) {
        if (mIsUseFrontCamera == isFrontCamera) {
            return
        }
        mIsUseFrontCamera = isFrontCamera
        mTRTCCloud!!.switchCamera()
        if (mIsFuEffect) {
            mFURenderer!!.cameraFacing =
                if (mIsUseFrontCamera) CameraFacingEnum.CAMERA_FRONT else CameraFacingEnum.CAMERA_BACK
        }
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
            mTRTCCloud!!.setLocalVideoProcessListener(
                TRTC_VIDEO_PIXEL_FORMAT_Texture_2D,
                TRTC_VIDEO_BUFFER_TYPE_TEXTURE, object : TRTCVideoFrameListener {
                    override fun onGLContextCreated() {
                        mFURenderer!!.prepareRenderer(null)
                    }

                    override fun onProcessVideoFrame(src: TRTCVideoFrame, dest: TRTCVideoFrame): Int {
                        mFURenderer!!.cameraFacing = if (mIsUseFrontCamera) CameraFacingEnum.CAMERA_FRONT else CameraFacingEnum.CAMERA_BACK
                        val start = System.nanoTime()
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
        mTRTCCloud!!.startLocalPreview(isFrontCamera, txCloudVideoView)
    }

    //关闭相机流
    fun closeCamera() {
        mTRTCCloud!!.stopLocalPreview()
    }

    /**
     * 停止此次通话，把所有的变量都会重置
     */
    fun stopCall() {
        MPTimber.tag(TAGLOG).d("stopCall")
        mInviteMap.clear()
        isOnCalling = false
        mIsInRoom = false
        mIsBeingCalled = true
        mEnterRoomTime = 0
        rtcInviteId = ""
        rtcInviteRoomId = 0
        mCurSponsorForMe = ""
        mLastCallModel = DLRTCCallModel()
        mLastCallModel.version = DLRTCCallModel.VALUE_VERSION
        mCurCallType = DLRTCCallingConstants.TYPE_UNKNOWN
        stopMusic()
        unregisterSensorEventListener()
        mIsProcessedBySelf = false
        if (ServiceUtils.isServiceRunning(DLRTCCallService::class.java)) {
            mContext?.let { DLRTCCallService.stop(it) }
        }
    }

    /**
     * 销毁接口注册监听
     */
    private fun unregisterSensorEventListener() {
        if(mSensorEventListener==null){
            return
        }
        mSensorManager?.apply {
            val sensor = getDefaultSensor(Sensor.TYPE_PROXIMITY)
            unregisterListener(mSensorEventListener,sensor)
            mSensorManager = null
        }
    }
    //注册接口注册监听
    private fun registerSensorEventListener() {
        if (null != mSensorManager) {
            return
        }
        mSensorManager = mContext!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        val pm = mContext!!.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            "TUICalling:TRTCAudioCallWakeLock"
        )
        mSensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (mIsFuEffect && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    val y = event.values[1]
                    if (abs(x) > 3 || abs(y) > 3) {
                        if (abs(x) > abs(y)) mFURenderer?.deviceOrientation = if (x > 0) 0 else 180 else mFURenderer?.deviceOrientation = if (y > 0) 90 else 270
                    }
                }
                when (event.sensor.type) {
                    Sensor.TYPE_PROXIMITY ->                         // 靠近手机
                        if (event.values[0].toDouble() == 0.0) {
                            if (wakeLock.isHeld) {
                                // 检查WakeLock是否被占用
                                return
                            } else {
                                // 申请设备电源锁
                                wakeLock.acquire(10*60*1000L /*10 minutes*/)
                            }
                        } else {
                            if (!wakeLock.isHeld) {
                                return
                            } else {
                                wakeLock.setReferenceCounted(false)
                                // 释放设备电源锁
                                wakeLock.release()
                            }
                        }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        mSensorManager!!.registerListener(mSensorEventListener, sensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    /**
     * 重要：用于判断是否需要结束本次通话
     * 在用户超时、拒绝、忙线、有人退出房间时需要进行判断
     */
    private fun preExitRoom(leaveUser: String ? = null) {
        MPTimber.tag(TAGLOG).i("preExitRoom: $inviteUserId $acceptUserId mIsInRoom=$mIsInRoom leaveUser=$leaveUser")
        if (rtcInviteId.isEmpty()) {
            // 当没有其他用户在房间里了，则结束通话。
            if (!TextUtils.isEmpty(leaveUser) && mIsInRoom) {
                sendModel(leaveUser.orEmpty(),DLRTCCallModel.VIDEO_CALL_ACTION_HANGUP)
            }
            playHangupMusic()
            if (mIsInRoom) {
                exitRoom()
            }
            stopCall()
            mTRTCInternalListenerManager?.onCallEnd()
        }
    }

    /**
     * trtc 退房
     */
    fun exitRoom() {
        mTRTCCloud?.apply {
            stopLocalPreview()
            stopLocalAudio()
            exitRoom()
        }
    }
    //接听电话
    fun accept() {
        mIsProcessedBySelf = true
        stopRing()
        mContext?.let { DLRTCCallService.start(it) }
    }


    /**
     * TRTC的监听器
     */
    private val mTRTCCloudListener: TRTCCloudListener = object : TRTCCloudListener() {
        override fun onError(errCode: Int, errMsg: String, extraInfo: Bundle) {
            MPTimber.tag(TAGLOG).e("onError: $errCode $errMsg")
            stopCall()
            mTRTCInternalListenerManager?.onError(errCode, errMsg)
        }

        override fun onEnterRoom(result: Long) {
            MPTimber.tag(TAGLOG).d("onEnterRoom result:$result , mCurSponsorForMe = $mCurSponsorForMe , mIsBeingCalled = $mIsBeingCalled")
            if (result < 0) {
                stopCall()
            } else {
                mIsInRoom = true
                //如果自己是被叫,接收到通话请求后进房,进房成功后发送accept信令给主叫端;如果自己是主叫,不处理
                if (mIsBeingCalled) {
                    sendModel(mCurSponsorForMe, DLRTCCallModel.VIDEO_CALL_ACTION_ACCEPT)
                }
            }
        }
        //后台执行解散房间
        override fun onExitRoom(reason: Int) {
            MPTimber.tag(TAGLOG).d( "onExitRoom reason:$reason")
            //1 后台执行踢出房间。 2 后台解散房间
            if (reason == 1 || reason == 2) {
                //执行挂断 变量值初始化
                stopCall()
                //执行挂断，返回上一页
                mTRTCInternalListenerManager?.onCallEnd()
            }
        }

        override fun onRemoteUserEnterRoom(userId: String) {
            MPTimber.tag(TAGLOG).d("onRemoteUserEnterRoom userId:$userId")
            // 只有单聊这个时间才是正确的，因为单聊只会有一个用户进群，群聊这个时间会被后面的人重置
            mEnterRoomTime = System.currentTimeMillis()
            mTRTCInternalListenerManager?.onUserEnter(userId)
            if (!mIsBeingCalled) {
                stopMusic()
            }
        }

        override fun onRemoteUserLeaveRoom(userId: String, reason: Int) {
            MPTimber.tag(TAGLOG).d( "onRemoteUserLeaveRoom userId:$userId, reason:$reason")
            // 远端用户退出房间，需要判断本次通话是否结束
            mTRTCInternalListenerManager?.onUserLeave(userId)
            //C2C多人通话增加: 只有主叫会调用
//            if (checkIsHasGroupIDCall()) {
//                for (id in mCurRoomRemoteUserSet) {
//                    if (userId != id) {
//                        DLRTCSignalingManager.sendInviteAction(
//                            action = DLRTCCallModel.VIDEO_CALL_ACTION_REJECT,
//                            invitee = userId,
//                            userId = id,
//                            mData = null,
//                            callIDWithUserID = getCallIDWithUserID(id) as String,
//                            mCurInvitedList = mCurInvitedList
//                        )
//                    }
//                }
//            }
            //作为被叫,当房间中人数为0时退出房间,一般情况下 C2C多人通话在这里处理退房
            if (mIsBeingCalled ) {
                playHangupMusic()
                exitRoom()
                stopCall()
                mTRTCInternalListenerManager?.onCallEnd()
                return
            }
            preExitRoom(userId)
        }

        override fun onUserVideoAvailable(userId: String, available: Boolean) {
            MPTimber.tag(TAGLOG).d("onUserVideoAvailable userId:$userId, available:$available")
            mTRTCInternalListenerManager?.onUserVideoAvailable(userId, available)
        }

        override fun onUserAudioAvailable(userId: String, available: Boolean) {
            MPTimber.tag(TAGLOG).d("onUserAudioAvailable userId:$userId, available:$available")
            mTRTCInternalListenerManager?.onUserAudioAvailable(userId, available)
        }

        override fun onUserVoiceVolume(userVolumes: java.util.ArrayList<TRTCVolumeInfo>, totalVolume: Int) {
            val volumeMaps: MutableMap<String?, Int?> = java.util.HashMap()
            for (info in userVolumes) {
                val userId = if (info.userId == null) TUILogin.getUserId() else info.userId
                volumeMaps[userId] = info.volume
            }
            mTRTCInternalListenerManager?.onUserVoiceVolume(volumeMaps)
        }

        override fun onNetworkQuality(quality: TRTCQuality, arrayList: ArrayList<TRTCQuality?>?) {
            mTRTCInternalListenerManager?.onNetworkQuality(quality, arrayList)
        }
    }
    //拒绝电话
    private fun reject() {
        playHangupMusic()
        sendModel(mCurSponsorForMe, DLRTCCallModel.VIDEO_CALL_ACTION_REJECT)
        stopCall()
    }
    //取消电话
    fun hangup() {
        // 1. 如果还没有在通话中，说明还没有接通，所以直接拒绝了
        if (!isOnCalling || mIsBeingCalled) {
            reject()
            return
        }
        playHangupMusic()
        MPTimber.tag(TAGLOG).d( "singleHangup")
        singleHangup()
    }
    //取消电话
    private fun singleHangup() {
        sendModel(acceptUserId, DLRTCCallModel.VIDEO_CALL_ACTION_SPONSOR_CANCEL)
        stopCall()
        exitRoom()
    }

    private fun inviteUserReject(_inviteId: String){
        MPTimber.tag(TAGLOG).d("rejectInvite, inviteId=$_inviteId, data=${rtcSignallingData}")
        val acceptDataStr = gsonBuilder.create().toJson(rtcSignallingData)
        V2TIMManager.getSignalingManager().reject(_inviteId, acceptDataStr, object : V2TIMCallback {
            override fun onError(code: Int, desc: String) {
                MPTimber.tag(TAGLOG).e("reject switch to audio failed callID:$_inviteId, error:$code desc:$desc")
            }

            override fun onSuccess() {
                MPTimber.tag(TAGLOG).d("reject switch to audio success callID:$_inviteId")
            }
        })
    }


    private fun inviteUserAccept(_inviteId : String){
        addFilterKey(gsonBuilder, DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_END)
        val acceptDataStr = gsonBuilder.create().toJson(rtcSignallingData)
        MPTimber.tag(TAGLOG).d("acceptInvite, inviteId=$_inviteId, data=$acceptDataStr")
        V2TIMManager.getSignalingManager().accept(_inviteId, acceptDataStr, object : V2TIMCallback {
            override fun onError(code: Int, desc: String) {
                MPTimber.tag(TAGLOG).e("accept failed callID:$_inviteId, error:$code desc:$desc")
                mTRTCInternalListenerManager?.onError(code, desc)
            }
            override fun onSuccess() {
                MPTimber.tag(TAGLOG).d("accept success callID:$_inviteId")
            }
        })
    }

    private fun inviteUserCancel(inviteId: String) {
        addFilterKey(gsonBuilder, DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_END)
        val acceptDataStr = gsonBuilder.create().toJson(rtcSignallingData)
        MPTimber.tag(TAGLOG).d("cancelInvite, inviteId=$inviteId, data=$acceptDataStr")
        V2TIMManager.getSignalingManager().cancel(inviteId, acceptDataStr, object : V2TIMCallback {
            override fun onError(code: Int, desc: String) {
                MPTimber.tag(TAGLOG).e("cancel failde callID:$inviteId, error:$code desc:$desc")
            }

            override fun onSuccess() {
                MPTimber.tag(TAGLOG).d("cancel success callID:$inviteId")
            }
        })
    }


    /**
     * @return void
     * @Desc TODO(是否开启自动增益补偿功能, 可以自动调麦克风的收音量到一定的音量水平)
     * @author 彭石林
     * @parame [enable]
     * @Date 2022/2/14
     */
    fun enableAGC(enable: Boolean) {
        try {
            val jsonObject = JSONObject().apply {
                put("api", "enableAudioAGC")
                val params = JSONObject().apply {
                    put("enable", if (enable) 1 else 0)
                    put("level", "100") //支持的取值有: 0、30、60、100，0 表示关闭 AGC
                }
                put("params", params)
            }
            mTRTCCloud?.callExperimentalAPI(jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    /**
     * @return void
     * @Desc TODO(回声消除器 ， 可以消除各种延迟的回声)
     * @author 彭石林
     * @parame [enable]
     * @Date 2022/2/14
     */
    fun enableAEC(enable: Boolean) {
        try {
            val jsonObject = JSONObject().apply {
                put("api", "enableAudioAEC")
                val params = JSONObject().apply {
                    put("enable", if (enable) 1 else 0)
                    put("level", "100") //支持的取值有: 0、30、60、100，0 表示关闭 AEC
                }
                put("params", params)
            }
            mTRTCCloud?.callExperimentalAPI(jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    /**
     * @return void
     * @Desc TODO(背景噪音抑制功能 ， 可探测出背景固定频率的杂音并消除背景噪音)
     * @author 彭石林
     * @parame [enable]
     * @Date 2022/2/14
     */
    fun enableANS(enable: Boolean) {
        try {
            val jsonObject = JSONObject().apply {
                put("api", "enableAudioANS")
                val params = JSONObject().apply {
                    put("enable", if (enable) 1 else 0)
                    put("level", "100") //支持的取值有: 0、30、60、100，0 表示关闭 ANS
                }
                put("params", params)
            }
            mTRTCCloud?.callExperimentalAPI(jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun muteLocalAudio(enable : Boolean){
        mTRTCCloud?.muteLocalAudio(enable)
    }

    fun audioRoute(enable : Boolean){
        mTRTCCloud?.setAudioRoute(if(enable)TRTC_AUDIO_ROUTE_SPEAKER else TRTC_AUDIO_ROUTE_EARPIECE)
    }


}