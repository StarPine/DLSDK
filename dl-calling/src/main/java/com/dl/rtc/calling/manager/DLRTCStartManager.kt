package com.dl.rtc.calling.manager

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.text.TextUtils
import com.blankj.utilcode.util.ServiceUtils
import com.dl.lib.util.log.MPTimber
import com.dl.rtc.calling.DLRTCCallService
import com.dl.rtc.calling.R
import com.dl.rtc.calling.base.impl.DLRTCInternalListenerManager
import com.dl.rtc.calling.model.DLRTCOfflineMessageModel
import com.dl.rtc.calling.model.DLRTCSignalingManager
import com.dl.rtc.calling.model.bean.DLRTCCallModel
import com.dl.rtc.calling.model.bean.DLRTCSignallingData
import com.dl.rtc.calling.util.DLRTCSignallingUtil
import com.dl.rtc.calling.util.MediaPlayHelper
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tencent.imsdk.v2.*
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.trtc.TRTCCloud
import com.tencent.trtc.TRTCCloudDef.*
import com.tencent.trtc.TRTCCloudListener

/**
 *Author: 彭石林
 *Time: 2022/11/3 15:47
 * Description: This is DLRTCStartManager
 */
class DLRTCStartManager {
    val TAGLOG = "DLRTCStartManager";

    private var mSensorManager: SensorManager? = null
    private val mSensorEventListener: SensorEventListener? = null

    /**
     * 超时时间，单位秒
     */
    val TIME_OUT_COUNT = 30

    /**
     * C2C多人通话添加: 记录每个userId对应的CallId
     */
    private val mUserCallIDMap = HashMap<String, Any>()

    /**
     * C2C多人通话添加: 记录已经接通在TRTC房间内的远端用户
     */
    private val mRemoteUserInTRTCRoom = ArrayList<String>()

    private val mMediaPlayHelper: MediaPlayHelper? = null

    /**
     * 最近使用的通话信令，用于快速处理
     */
    private var mLastCallModel: DLRTCCallModel = DLRTCCallModel()

    //通话邀请缓存,便于查询通话是否有效
    private val mInviteMap: MutableMap<String, DLRTCCallModel> = HashMap()

    val TYPE_UNKNOWN = 0
    val TYPE_AUDIO_CALL = 1
    val TYPE_VIDEO_CALL = 2


    /**
     * 当前通话的类型
     */
    private var mCurCallType: Int = TYPE_UNKNOWN

    private var mIsBeingCalled = true // 默认是被叫

    private val mEnableMuteMode = false // 是否开启静音模式

    private val mCallingBellPath = "" // 被叫铃音路径

    //多端登录增加字段:用于标记当前是否是自己发给自己的请求(多端触发),以及自己是否处理了该请求.
    private var mIsProcessedBySelf = false // 被叫方: 主动操作后标记是否自己处理了请求或回调


    /**
     * 是否首次邀请
     */
    private var isOnCalling = false
    private var mCurCallID = ""
    private val mSwitchToAudioCallID = ""
    private var mCurRoomID = 0

    /**
     * 当前是否在TRTC房间中
     */
    private var mIsInRoom = false
    private var mEnterRoomTime: Long = 0

    /**
     * 当前邀请列表
     * C2C通话时会记录自己邀请的用户
     * IM群组通话时会同步群组内邀请的用户
     * 当用户接听、拒绝、忙线、超时会从列表中移除该用户
     */
    private val mCurInvitedList = ArrayList<String>()

    /**
     * 当前语音通话中的远端用户
     */
    private val mCurRoomRemoteUserSet: MutableSet<String> = HashSet()

    /**
     * C2C通话的邀请人
     * 例如A邀请B，B存储的mCurSponsorForMe为A
     */
    private var mCurSponsorForMe = ""

    private var mCurGroupId = ""

    private var initFlag = false

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

    /**
     * 进行初始化，程序理应有1个静默式的接收处理逻辑（只调用一次）
     */
    fun init(mContext: Context) {
        if(initFlag){
            return
        }
        mTRTCInternalListenerManager = DLRTCInternalListenerManager.instance

        mTRTCCloud = TRTCCloud.sharedInstance(mContext);
        initFlag = false
        DLRTCIMSignallingManager.getInstance().addSignalingListener(mTIMSignallingListener)
        DLRTCIMSignallingManager.getInstance().addSimpleMsgListener(mTIMSimpleMsgListener)
    }


    /**
     * 消息监听器,收到 C2C 自定义（信令）消息
     */
    private val mTIMSimpleMsgListener: V2TIMSimpleMsgListener = object : V2TIMSimpleMsgListener() {
        override fun onRecvC2CCustomMessage(
            msgID: String,
            sender: V2TIMUserInfo,
            customData: ByteArray
        ) {
            if (customData == null) return
            val customStr = String(customData)
            if (TextUtils.isEmpty(customStr)) {
                return
            }
            val signallingData: DLRTCSignallingData = DLRTCSignallingUtil.convert2CallingData(customStr)
            if (null == signallingData || null == signallingData.data || null == signallingData.businessID || signallingData.businessID != DLRTCCallModel.VALUE_BUSINESS_ID
            ) {
                MPTimber.tag(TAGLOG).d("this is not the calling scene")
                return
            }
            if (null == signallingData.data!!.cmd
                || signallingData.data!!
                    .cmd != DLRTCCallModel.VALUE_MSG_SYNC_INFO
            ) {
                MPTimber.tag(TAGLOG).d( "onRecvC2CCustomMessage: invalid message")
                return
            }
            MPTimber.tag(TAGLOG).d(
                "onRecvC2CCustomMessage inviteID:" + msgID + ", sender:" + sender.userID
                        + " data:" + customStr
            )
            val inviter: String? = signallingData.user
            when (signallingData.callAction) {
                DLRTCCallModel.VIDEO_CALL_ACTION_ACCEPT -> mTIMSignallingListener.onInviteeAccepted(
                    signallingData.callId,
                    inviter,
                    customStr
                )
                DLRTCCallModel.VIDEO_CALL_ACTION_REJECT -> mTIMSignallingListener.onInviteeRejected(
                    signallingData.callId,
                    inviter,
                    customStr
                )
                DLRTCCallModel.VIDEO_CALL_ACTION_SPONSOR_CANCEL -> mTIMSignallingListener.onInvitationCancelled(
                    signallingData.callId,
                    inviter,
                    customStr
                )
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
        override fun onReceiveNewInvitation(
            inviteID: String, inviter: String, groupID: String,
            inviteeList: List<String>, data: String
        ) {
            MPTimber.tag(TAGLOG).d("onReceiveNewInvitation inviteID:" + inviteID + ", inviter:" + inviter
                    + ", groupID:" + groupID + ", inviteeList:" + inviteeList + " data:" + data)
            handleRecvCallModel(inviteID, inviter, groupID, inviteeList, data)
        }

        override fun onInviteeAccepted(inviteID: String, invitee: String, data: String) {
            if (!isOnCalling) {
                MPTimber.tag(TAGLOG).d("onInviteeAccepted isOnCalling : $isOnCalling")
                return
            }
            MPTimber.tag(TAGLOG).d("onInviteeAccepted inviteID:" + inviteID
                        + ", invitee:" + invitee + " data:" + data)
            val signallingData: DLRTCSignallingData = DLRTCSignallingUtil.convert2CallingData(data)
            if (!DLRTCSignallingUtil.isCallingData(signallingData)) {
                MPTimber.tag(TAGLOG).d("this is not the calling scene ")
                return
            }
            if (DLRTCSignallingUtil.isSwitchAudioData(signallingData)) {
                realSwitchToAudioCall()
                return
            }

            //多端登录:A1,A2登录同一账号,账户B呼叫A,A1接听正常处理,A2退出界面
            if (!mIsProcessedBySelf && !TextUtils.isEmpty(invitee) && invitee == TUILogin.getLoginUser()) {
                stopCall()
                preExitRoom(null)
                return
            }
            if (checkIsHasGroupIDCall()) {
                for (id in mCurRoomRemoteUserSet) {
                    if (invitee != id) {
                        DLRTCSignalingManager.sendInviteAction(
                            action = DLRTCCallModel.VIDEO_CALL_ACTION_ACCEPT,
                            invitee = invitee,
                            userId = id,
                            mData = data,
                            callIDWithUserID = getCallIDWithUserID(id) as String,
                            mCurInvitedList = mCurInvitedList
                        )
                    }
                }
            }
            mCurInvitedList.remove(invitee)
        }

        override fun onInviteeRejected(inviteID: String, invitee: String, data: String) {
            MPTimber.tag(TAGLOG).d("onInviteeRejected inviteID:" + inviteID
                        + ", invitee:" + invitee + " data:" + data)
            if (!isOnCalling) {
                MPTimber.tag(TAGLOG).d("onInviteeRejected isOnCalling : $isOnCalling")
                return
            }
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
            val curCallID: String
            if (checkIsHasGroupIDCall()) {
                for (id in mCurRoomRemoteUserSet) {
                    if (invitee != id) {
                        DLRTCSignalingManager.sendInviteAction(
                            DLRTCCallModel.VIDEO_CALL_ACTION_REJECT,
                            invitee = invitee,
                            userId = id,
                            mData = data,
                            callIDWithUserID = getCallIDWithUserID(id) as String,
                            mCurInvitedList = mCurInvitedList
                        )
                    }
                }
                curCallID = getCallIDWithUserID(invitee) as String
            } else {
                curCallID = mCurCallID
            }
            MPTimber.tag(TAGLOG).d("onInviteeRejected: curCallID = $curCallID")
            if (TextUtils.isEmpty(curCallID) || inviteID != curCallID) {
                return
            }
            mCurInvitedList.remove(invitee)
            mCurRoomRemoteUserSet.remove(invitee)

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
        //取消信令
        override fun onInvitationCancelled(inviteID: String, inviter: String, data: String) {
            MPTimber.tag(TAGLOG).d("onInvitationCancelled inviteID:$inviteID inviter:$inviter data:$data")
            val signallingData = DLRTCSignallingUtil.convert2CallingData(data)
            if (!DLRTCSignallingUtil.isCallingData(signallingData)) {
                MPTimber.tag(TAGLOG).d("this is not the calling scene ")
                return
            }
            val curCallId: String
            if (checkIsHasGroupIDCall()) {
                curCallId = getCallIDWithUserID(inviter) as String
                for (id in mCurRoomRemoteUserSet) {
                    if (inviter != id) {
                        DLRTCSignalingManager.sendInviteAction(
                            action = DLRTCCallModel.VIDEO_CALL_ACTION_SPONSOR_CANCEL,
                            invitee = inviter,
                            userId = id,
                            mData = data,
                            callIDWithUserID = getCallIDWithUserID(id) as String,
                            mCurInvitedList = mCurInvitedList
                        )
                    }
                }
            } else {
                curCallId = mCurCallID
            }
            MPTimber.tag(TAGLOG).d("onInvitationCancelled: curCallId = $curCallId")
            if (inviteID == curCallId) {
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
            MPTimber.tag(TAGLOG).d("onInvitationTimeout inviteID : $inviteID , mCurCallID : $mCurCallID ,inviteeList: $inviteeList")
            //移除缓存数据
            mInviteMap.remove(inviteID)
            val curCallId: String
            if (checkIsHasGroupIDCall()) {
                curCallId = getCallIDWithUserID(inviteeList[0]) as String
                val invitee = inviteeList[0]
                for (id in mCurRoomRemoteUserSet) {
                    if (invitee != id) {
                        DLRTCSignalingManager.sendInviteAction(
                            action = DLRTCCallModel.VIDEO_CALL_ACTION_SPONSOR_TIMEOUT,
                            invitee = invitee,
                            userId = id,
                            mData = null,
                            callIDWithUserID = getCallIDWithUserID(id) as String,
                            mCurInvitedList = mCurInvitedList
                        )
                    }
                }
            } else {
                curCallId = mCurCallID
            }
            MPTimber.tag(TAGLOG).d("curCallId : $curCallId , mCurCallID : $mCurCallID")
            if (inviteID != curCallId && inviteID != mSwitchToAudioCallID) {
                return
            }
            // 邀请者
            if (TextUtils.isEmpty(mCurSponsorForMe)) {
                //1.主叫所有用户都超时,也就是没人接听->主叫处理退房逻辑;
                if (mRemoteUserInTRTCRoom.size == 0) {
                    for (userID in inviteeList) {
                        mTRTCInternalListenerManager?.onNoResp(userID)
                        mCurInvitedList.remove(userID)
                        mCurRoomRemoteUserSet.remove(userID)
                    }
                    stopMusic()
                    preExitRoom(null)
                    playHangupMusic()
                    unregisterSensorEventListener()
                } else {
                    //2.主叫端:某个用户接听后,还有其他用户超时信息,则只回调到上层更新主叫界面该超时用户的UI
                    for (userID in inviteeList) {
                        mTRTCInternalListenerManager?.onNoResp(userID)
                        mCurInvitedList.remove(userID)
                        mCurRoomRemoteUserSet.remove(userID)
                    }
                }
            } else {
                //被邀请者
                MPTimber.tag(TAGLOG).d("mCurInvitedList = $mCurInvitedList, mCurRoomRemoteUserSet = $mCurRoomRemoteUserSet")
                // 1.自己超时
                if (inviteeList.contains(TUILogin.getUserId())) {
                    stopCall()
                    mTRTCInternalListenerManager?.onCallingTimeout()
                    mCurInvitedList.removeAll(inviteeList)
                    mCurRoomRemoteUserSet.removeAll(inviteeList)
                    preExitRoom(null)
                    playHangupMusic()
                    unregisterSensorEventListener()
                    return
                }
                //2.其他人超时,不处理退房逻辑,只更新超时用户的UI
                for (id in inviteeList) {
                    mTRTCInternalListenerManager?.onNoResp(id)
                    mCurInvitedList.remove(id)
                    mCurRoomRemoteUserSet.remove(id)
                }
            }
        }
    }
    private fun handleRecvCallModel(
        inviteID: String, inviter: String, groupID: String,
        inviteeList: List<String>, data: String
    ) {
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
        mInviteMap.put(inviter, callModel)

//        //如果应用在后台,且没有允许后台拉起应用的权限时返回
//        if (!isAppRunningForeground(mContext) && !PermissionUtil.hasPermission(mContext)) {
//            TRTCLogger.d(TRTCCalling.TAG, "isAppRunningForeground is false")
//            return
//        }
//        //后台播被叫铃声
//        startRing()
//        processInvite(inviteID, inviter, groupID, inviteeList, signallingData)
    }

    /**
     * 播放铃声
     */
    private fun startRing() {
        if (null == mMediaPlayHelper || mEnableMuteMode) {
            return
        }
        mMediaPlayHelper.start(R.raw.phone_dialing)
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
            realCallModel =
                model.clone() as DLRTCCallModel
            realCallModel.action = action
        } else {
            realCallModel = DLRTCSignalingManager.generateModel(action,mLastCallModel)
        }
        val isGroup = !TextUtils.isEmpty(realCallModel.groupId)
        if (action == DLRTCCallModel.VIDEO_CALL_ACTION_HANGUP && mEnterRoomTime != 0L && !isGroup) {
            realCallModel.duration = (System.currentTimeMillis() - mEnterRoomTime).toInt() / 1000
            mEnterRoomTime = 0
        }
        var receiver = ""
        var groupId = ""
        var inviteId = ""
        if (isGroup) {
            groupId = realCallModel.groupId
        } else {
            receiver = user
        }
        inviteId = if (mUserCallIDMap.isNotEmpty()) ({
            if (isGroup) realCallModel.callId else getCallIDWithUserID(user)
        }).toString() else ({
            realCallModel.callId
        }).toString()
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
                    TYPE_AUDIO_CALL->{
                        DLRTCCallModel.VALUE_CMD_AUDIO_CALL
                    }
                    TYPE_VIDEO_CALL->{
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
                    if (isGroup) {
                        val dialingDataStr = gsonBuilder.create().toJson(signallingData)
                        callID = realCallModel.invitedList?.let {
                            DLRTCSignalingManager.sendGroupInvite(
                                groupId,
                                it,
                                dialingDataStr,
                                TIME_OUT_COUNT,
                                object : V2TIMCallback {
                                    override fun onError(code: Int, desc: String) {
                                        MPTimber.tag(TAGLOG).e("inviteInGroup failed callID:" + realCallModel.callId + ", error:" + code + " desc:" + desc)
                                    }

                                    override fun onSuccess() {
                                        MPTimber.tag(TAGLOG).d("inviteInGroup success:$realCallModel")
                                        realCallModel.callId = mCurCallID
                                        realCallModel.timeout = TIME_OUT_COUNT
                                        realCallModel.version =
                                            DLRTCCallModel.VALUE_VERSION
                                        //暂时不发送离线信令
                                       DLRTCOfflineMessageModel.sendOnlineMessageWithOfflinePushInfo(user, null,realCallModel,null)
                                    }
                                })
                        }
                    } else {
                        callDataInfo.userIDs = realCallModel.invitedList
                        val dialingDataStr = gsonBuilder.create().toJson(signallingData)
                        realCallModel.callId = mCurCallID
                        realCallModel.timeout = TIME_OUT_COUNT
                        realCallModel.version =
                            DLRTCCallModel.VALUE_VERSION
                        val pushInfo: V2TIMOfflinePushInfo =
                            DLRTCOfflineMessageModel.createV2TIMOfflinePushInfo(realCallModel, user, user,null)
                        //发送信令
                        callID = DLRTCSignalingManager.sendInvite(
                            receiver,
                            dialingDataStr,
                            pushInfo,
                            TIME_OUT_COUNT,
                            object : V2TIMCallback {
                                override fun onError(code: Int, desc: String) {
                                    MPTimber.tag(TAGLOG).e("invite failed callID:" + realCallModel.callId + ",error:" + code + " desc:" + desc)
                                }

                                override fun onSuccess() {
                                    MPTimber.tag(TAGLOG).d("invite success:$realCallModel")
                                    realCallModel.callId = mCurCallID
                                    realCallModel.timeout = TIME_OUT_COUNT
                                    realCallModel.version =
                                        DLRTCCallModel.VALUE_VERSION
                                    //                            sendOnlineMessageWithOfflinePushInfo(user, realCallModel);
                                }
                            })
                    }
                }

            }
            //接听电话
            DLRTCCallModel.VIDEO_CALL_ACTION_ACCEPT -> {
                addFilterKey(
                    gsonBuilder,
                    DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_END
                )
                val acceptDataStr = gsonBuilder.create().toJson(signallingData)
                DLRTCSignalingManager.acceptInvite(inviteId, acceptDataStr, object : V2TIMCallback {
                    override fun onError(code: Int, desc: String) {
                        MPTimber.tag(TAGLOG).e("accept failed callID:" + realCallModel.callId + ", error:" + code + " desc:" + desc)
//
//                        if (null != mTRTCInternalListenerManager) {
//                            mTRTCInternalListenerManager.onError(code, desc)
//                        }
                    }

                    override fun onSuccess() {
                        MPTimber.tag(TAGLOG).d("accept success callID:" + realCallModel.callId)
                    }
                })
            }
            //拒接电话
            DLRTCCallModel.VIDEO_CALL_ACTION_REJECT -> {
                addFilterKey(
                    gsonBuilder,
                    DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_END
                )
                val rejectDataStr = gsonBuilder.create().toJson(signallingData)
                DLRTCSignalingManager.rejectInvite(inviteId, rejectDataStr, object : V2TIMCallback {
                    override fun onError(code: Int, desc: String) {
                        MPTimber.tag(TAGLOG).e("reject failed callID:" + realCallModel.callId + ", error:" + code + " desc:" + desc)
                    }

                    override fun onSuccess() {
                        MPTimber.tag(TAGLOG).d("reject success callID:" + realCallModel.callId)
                    }
                })
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
                    DLRTCSignalingManager.rejectInvite(it, lineBusyMapStr, object : V2TIMCallback {
                        override fun onError(code: Int, desc: String) {
                            MPTimber.tag(TAGLOG).e("line_busy failed callID:" + realCallModel.callId + ", error:" + code + " desc:" + desc)
                        }

                        override fun onSuccess() {
                            MPTimber.tag(TAGLOG).d("line_busy success callID:" + realCallModel.callId)
                        }
                    })
                }
            }
            //发起人取消
            DLRTCCallModel.VIDEO_CALL_ACTION_SPONSOR_CANCEL -> {
                addFilterKey(
                    gsonBuilder,
                    DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_END
                )
                val cancelMapStr = gsonBuilder.create().toJson(signallingData)
                DLRTCSignalingManager.cancelInvite(inviteId, cancelMapStr, object : V2TIMCallback {
                    override fun onError(code: Int, desc: String) {
                        MPTimber.tag(TAGLOG).e("cancel failde callID:" + realCallModel.callId + ", error:" + code + " desc:" + desc)
                    }

                    override fun onSuccess() {
                        MPTimber.tag(TAGLOG).d("cancel success callID:" + realCallModel.callId)
                    }
                })
            }
            //挂断
            DLRTCCallModel.VIDEO_CALL_ACTION_HANGUP -> {
                val hangupDataInfo = DLRTCSignallingData.DataInfo()
                signallingData.callEnd = (realCallModel.duration)
                hangupDataInfo.cmd = (DLRTCCallModel.VALUE_CMD_HAND_UP)
                signallingData.data = (hangupDataInfo)
                val hangupMapStr = gsonBuilder.create().toJson(signallingData)
                //群聊
                if (isGroup) {
                    realCallModel.invitedList?.let {
                        DLRTCSignalingManager.sendGroupInvite(
                            groupId,
                            it,
                            hangupMapStr,
                            0,
                            object : V2TIMCallback {
                                override fun onError(code: Int, desc: String) {
                                    MPTimber.tag(TAGLOG).e("inviteInGroup-->hangup failed callID:" + realCallModel.callId + ", error:" + code + " desc:" + desc)
                                }

                                override fun onSuccess() {
                                    MPTimber.tag(TAGLOG).d("inviteInGroup-->hangup success callID:" + realCallModel.callId)
                                }
                            })
                    }
                } else {
                    DLRTCSignalingManager.sendInvite(receiver, hangupMapStr, null, 0, object : V2TIMCallback {
                        override fun onError(code: Int, desc: String) {
                            MPTimber.tag(TAGLOG).e("inviteInGroup-->hangup failed callID:" + realCallModel.callId + ", error:" + code + " desc:" + desc)
                        }

                        override fun onSuccess() {
                            MPTimber.tag(TAGLOG).d("inviteInGroup-->hangup success callID:" + realCallModel.callId)
                        }
                    })
                }
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
                    receiver,
                    switchAudioCall,
                    null,
                    TIME_OUT_COUNT,
                    object : V2TIMCallback {
                        override fun onError(code: Int, desc: String) {
                            MPTimber.tag(TAGLOG).e("invite-->switchAudioCall failed callID: " + realCallModel.callId+ ", error:" + code + " desc:" + desc)
                        }

                        override fun onSuccess() {
                            MPTimber.tag(TAGLOG).d("invite-->switchAudioCall success callID:" + realCallModel.callId)
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
            //拒绝切换为语音电话
            DLRTCCallModel.VIDEO_CALL_ACTION_REJECT_SWITCH_TO_AUDIO -> {
                addFilterKey(
                    gsonBuilder,
                    DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_END
                )
                val rejectSwitchAudioCallData = DLRTCSignallingData.DataInfo()
                rejectSwitchAudioCallData.cmd = (DLRTCCallModel.VALUE_CMD_SWITCH_TO_AUDIO)
                signallingData.switchToAudioCall = (DLRTCCallModel.VALUE_CMD_SWITCH_TO_AUDIO)
                rejectSwitchAudioCallData.message = (message)
                signallingData.data = (rejectSwitchAudioCallData)
                val rejectSwitchAudioMapStr = gsonBuilder.create().toJson(signallingData)
                DLRTCSignalingManager.rejectInvite(inviteId, rejectSwitchAudioMapStr, object : V2TIMCallback {
                    override fun onError(code: Int, desc: String) {
                        MPTimber.tag(TAGLOG).e("reject switch to audio failed callID:" + realCallModel.callId + ", error:" + code + " desc:" + desc)
                    }

                    override fun onSuccess() {
                        MPTimber.tag(TAGLOG).d("reject switch to audio success callID:" + realCallModel.callId)
                    }
                })
            }
            else -> {}
        }

        // 最后需要重新赋值
        updateLastCallModel(realCallModel, callID, model)
        MPTimber.tag(TAGLOG).d("callID=$callID , mCurCallID : $mCurCallID")
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

    //C2C多人通话:通过userId获取callId
    private fun getCallIDWithUserID(userId: String): Any? {
        return if (mUserCallIDMap.isNotEmpty()) {
            mUserCallIDMap[userId]
        } else ""
    }

    private fun updateLastCallModel(
        realCallModel: DLRTCCallModel,
        callID: String? = null,
        oldModel: DLRTCCallModel?
    ) {
        if (realCallModel.action != DLRTCCallModel.VIDEO_CALL_ACTION_REJECT && realCallModel.action != DLRTCCallModel.VIDEO_CALL_ACTION_HANGUP && realCallModel.action != DLRTCCallModel.VIDEO_CALL_ACTION_SPONSOR_CANCEL && oldModel == null) {
            mLastCallModel =
                realCallModel.clone() as DLRTCCallModel
        }
    }

    private fun realSwitchToAudioCall() {
        if (mCurCallType == TYPE_VIDEO_CALL) {
            closeCamera()
            onSwitchToAudio(true, "success")
            mCurCallType = TYPE_AUDIO_CALL
        }
    }

    private fun onSwitchToAudio(success: Boolean, message: String) {
        mTRTCInternalListenerManager?.onSwitchToAudio(success, message)
    }

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
        mCurCallID = ""
        mCurRoomID = 0
        mCurInvitedList.clear()
        mCurRoomRemoteUserSet.clear()
        mUserCallIDMap.clear()
        mRemoteUserInTRTCRoom.clear()
        mCurSponsorForMe = ""
        mLastCallModel = DLRTCCallModel()
        mLastCallModel.version = DLRTCCallModel.VALUE_VERSION
        mCurGroupId = ""
        mCurCallType = TYPE_UNKNOWN
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

    /**
     * 重要：用于判断是否需要结束本次通话
     * 在用户超时、拒绝、忙线、有人退出房间时需要进行判断
     */
    private fun preExitRoom(leaveUser: String ? = null) {
        MPTimber.tag(TAGLOG).i("preExitRoom: " + mCurRoomRemoteUserSet + " " + mCurInvitedList+ " mIsInRoom=" + mIsInRoom + " leaveUser=" + leaveUser
        )
        if (mCurRoomRemoteUserSet.isEmpty() && mCurInvitedList.isEmpty()) {
            // 当没有其他用户在房间里了，则结束通话。
            if (!TextUtils.isEmpty(leaveUser) && mIsInRoom) {
                if (TextUtils.isEmpty(mCurGroupId)) {
                    sendModel(leaveUser.orEmpty(),DLRTCCallModel.VIDEO_CALL_ACTION_HANGUP)
                } else {
                    sendModel("", DLRTCCallModel.VIDEO_CALL_ACTION_HANGUP)
                }
            }
            playHangupMusic()
            if (mIsInRoom) {
                exitRoom()
            }
            stopCall()
            mTRTCInternalListenerManager?.onCallEnd()
        }
    }

    //C2C多人通话:主叫端返回true,被叫端返回false
    private fun checkIsHasGroupIDCall(): Boolean {
        return mUserCallIDMap.size > 1
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
    fun accept() {
        mIsProcessedBySelf = true
        enterTRTCRoom()
        stopRing()
        mContext?.let { DLRTCCallService.start(it) }
    }

    /**
     * trtc 进房
     */
    private fun enterTRTCRoom() {
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
                    sendModel(
                        mCurSponsorForMe,
                        DLRTCCallModel.VIDEO_CALL_ACTION_ACCEPT
                    )
                }
            }
        }

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
            mCurRoomRemoteUserSet.add(userId)
            mRemoteUserInTRTCRoom.add(userId)
            // 只有单聊这个时间才是正确的，因为单聊只会有一个用户进群，群聊这个时间会被后面的人重置
            mEnterRoomTime = System.currentTimeMillis()
            mTRTCInternalListenerManager?.onUserEnter(userId)
            if (!mIsBeingCalled) {
                stopMusic()
            }
        }

        override fun onRemoteUserLeaveRoom(userId: String, reason: Int) {
            MPTimber.tag(TAGLOG).d( "onRemoteUserLeaveRoom userId:$userId, reason:$reason")
            mCurRoomRemoteUserSet.remove(userId)
            mCurInvitedList.remove(userId)
            mRemoteUserInTRTCRoom.remove(userId)
            // 远端用户退出房间，需要判断本次通话是否结束
            mTRTCInternalListenerManager?.onUserLeave(userId)
            //C2C多人通话增加: 只有主叫会调用
            //A呼叫BC,B接通又挂断,C接通,C应该能收到B接通的Accept回调,且能收到B reject的回调
            //B接通后挂断reject不会有信令,因此需要在B退房的时候,通过A转发给C
            if (checkIsHasGroupIDCall()) {
                for (id in mCurRoomRemoteUserSet) {
                    if (userId != id) {
                        DLRTCSignalingManager.sendInviteAction(
                            action = DLRTCCallModel.VIDEO_CALL_ACTION_REJECT,
                            invitee = userId,
                            userId = id,
                            mData = null,
                            callIDWithUserID = getCallIDWithUserID(id) as String,
                            mCurInvitedList = mCurInvitedList
                        )
                    }
                }
            }
            //作为被叫,当房间中人数为0时退出房间,一般情况下 C2C多人通话在这里处理退房
            if (mIsBeingCalled && mRemoteUserInTRTCRoom.size == 0) {
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

        override fun onUserVoiceVolume(
            userVolumes: java.util.ArrayList<TRTCVolumeInfo>,
            totalVolume: Int
        ) {
            val volumeMaps: MutableMap<String?, Int?> = java.util.HashMap()
            for (info in userVolumes) {
                val userId = if (info.userId == null) TUILogin.getUserId() else info.userId
                volumeMaps[userId] = info.volume
            }
            mTRTCInternalListenerManager?.onUserVoiceVolume(volumeMaps)
        }

        override fun onNetworkQuality(
            quality: TRTCQuality,
            arrayList: ArrayList<TRTCQuality?>?
        ) {
            mTRTCInternalListenerManager?.onNetworkQuality(quality, arrayList)
        }
    }

    fun reject() {
        playHangupMusic()
        sendModel(
            mCurSponsorForMe,
            DLRTCCallModel.VIDEO_CALL_ACTION_REJECT
        )
        stopCall()
    }


}