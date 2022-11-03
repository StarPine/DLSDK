package com.dl.rtc.calling.manager

import android.text.TextUtils
import com.dl.lib.util.log.MPTimber
import com.tencent.imsdk.v2.V2TIMSignalingListener
import com.tencent.imsdk.v2.V2TIMSimpleMsgListener
import com.tencent.imsdk.v2.V2TIMUserInfo
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.trtc.TRTCCloud

/**
 *Author: 彭石林
 *Time: 2022/11/3 15:47
 * Description: This is DLRTCStartManager
 */
class DLRTCStartManager {
    val TAGLOG = "DLRTCStartManager";

    /**
     * 底层SDK调用实例
     */
    private val mTRTCCloud: TRTCCloud? = null


    /**
     * 是否首次邀请
     */
    private val isOnCalling = false
    private val mCurCallID = ""
    private val mSwitchToAudioCallID = ""
    private val mCurRoomID = 0

    private var initFlag = false

    /**
     * 进行初始化，程序理应有1个静默式的接收处理逻辑（只调用一次）
     */
    fun init() {
        if(initFlag){
            return
        }
        initFlag = false
        DLRTCSignallingManager.getInstance().addSignalingListener(mTIMSignallingListener)
        DLRTCSignallingManager.getInstance().addSimpleMsgListener(mTIMSimpleMsgListener)
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
            val signallingData: SignallingData = convert2CallingData(customStr)
            if (null == signallingData || null == signallingData.getData() || null == signallingData.getBusinessID() || signallingData.getBusinessID() != com.tencent.liteav.trtccalling.model.impl.base.CallModel.VALUE_BUSINESS_ID
            ) {
                TRTCLogger.d(TRTCCalling.TAG, "this is not the calling scene")
                return
            }
            if (null == signallingData.getData().getCmd()
                || signallingData.getData()
                    .getCmd() != DLRTCCallModel.VALUE_MSG_SYNC_INFO
            ) {
                TRTCLogger.d(TRTCCalling.TAG, "onRecvC2CCustomMessage: invalid message")
                return
            }
            TRTCLogger.d(
                TRTCCalling.TAG,
                "onRecvC2CCustomMessage inviteID:" + msgID + ", sender:" + sender.userID
                        + " data:" + customStr
            )
            val inviter: String = signallingData.getUser()
            when (signallingData.getCallAction()) {
                com.tencent.liteav.trtccalling.model.impl.base.CallModel.VIDEO_CALL_ACTION_ACCEPT -> mTIMSignallingListener.onInviteeAccepted(
                    signallingData.getCallid(),
                    inviter,
                    customStr
                )
                com.tencent.liteav.trtccalling.model.impl.base.CallModel.VIDEO_CALL_ACTION_REJECT -> mTIMSignallingListener.onInviteeRejected(
                    signallingData.getCallid(),
                    inviter,
                    customStr
                )
                com.tencent.liteav.trtccalling.model.impl.base.CallModel.VIDEO_CALL_ACTION_SPONSOR_CANCEL -> mTIMSignallingListener.onInvitationCancelled(
                    signallingData.getCallid(),
                    inviter,
                    customStr
                )
                com.tencent.liteav.trtccalling.model.impl.base.CallModel.VIDEO_CALL_ACTION_SPONSOR_TIMEOUT -> {
                    val userList: MutableList<String> = ArrayList()
                    userList.add(signallingData.getUser())
                    mTIMSignallingListener.onInvitationTimeout(signallingData.getCallid(), userList)
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
                TRTCLogger.d(TRTCCalling.TAG, "onInviteeAccepted isOnCalling : $isOnCalling")
                return
            }
            TRTCLogger.d(
                TRTCCalling.TAG, "onInviteeAccepted inviteID:" + inviteID
                        + ", invitee:" + invitee + " data:" + data
            )
            val signallingData: SignallingData = convert2CallingData(data)
            if (!isCallingData(signallingData)) {
                TRTCLogger.d(TRTCCalling.TAG, "this is not the calling scene ")
                return
            }
            if (isSwitchAudioData(signallingData)) {
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
                        sendInviteAction(
                            com.tencent.liteav.trtccalling.model.impl.base.CallModel.VIDEO_CALL_ACTION_ACCEPT,
                            invitee,
                            id,
                            data
                        )
                    }
                }
            }
            mCurInvitedList.remove(invitee)
        }

        override fun onInviteeRejected(inviteID: String, invitee: String, data: String) {
            TRTCLogger.d(
                TRTCCalling.TAG, "onInviteeRejected inviteID:" + inviteID
                        + ", invitee:" + invitee + " data:" + data
            )
            if (!isOnCalling) {
                TRTCLogger.d(TRTCCalling.TAG, "onInviteeRejected isOnCalling : $isOnCalling")
                return
            }
            val signallingData: SignallingData = convert2CallingData(data)
            if (!isCallingData(signallingData)) {
                TRTCLogger.d(TRTCCalling.TAG, "this is not the calling scene ")
                return
            }
            if (isSwitchAudioData(signallingData)) {
                val message: String = getSwitchAudioRejectMessage(signallingData)
                onSwitchToAudio(false, message)
                return
            }
            val curCallID: String
            if (checkIsHasGroupIDCall()) {
                for (id in mCurRoomRemoteUserSet) {
                    if (invitee != id) {
                        sendInviteAction(
                            com.tencent.liteav.trtccalling.model.impl.base.CallModel.VIDEO_CALL_ACTION_REJECT,
                            invitee,
                            id,
                            data
                        )
                    }
                }
                curCallID = getCallIDWithUserID(invitee)
            } else {
                curCallID = mCurCallID
            }
            TRTCLogger.d(TRTCCalling.TAG, "onInviteeRejected: curCallID = $curCallID")
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
            if (isLineBusy(signallingData)) {
                if (mTRTCInternalListenerManager != null) {
                    mTRTCInternalListenerManager.onLineBusy(invitee)
                }
            } else {
                if (mTRTCInternalListenerManager != null) {
                    mTRTCInternalListenerManager.onReject(invitee)
                }
            }
            TRTCLogger.d(TRTCCalling.TAG, "mIsInRoom=$mIsInRoom")
            preExitRoom(null)
            stopDialingMusic()
            unregisterSensorEventListener()
        }

        override fun onInvitationCancelled(inviteID: String, inviter: String, data: String) {
            TRTCLogger.d(
                TRTCCalling.TAG,
                "onInvitationCancelled inviteID:$inviteID inviter:$inviter data:$data"
            )
            val signallingData: SignallingData = convert2CallingData(data)
            if (!isCallingData(signallingData)) {
                TRTCLogger.d(TRTCCalling.TAG, "this is not the calling scene ")
                return
            }
            val curCallId: String
            if (checkIsHasGroupIDCall()) {
                curCallId = getCallIDWithUserID(inviter)
                for (id in mCurRoomRemoteUserSet) {
                    if (inviter != id) {
                        sendInviteAction(
                            com.tencent.liteav.trtccalling.model.impl.base.CallModel.VIDEO_CALL_ACTION_SPONSOR_CANCEL,
                            inviter,
                            id,
                            data
                        )
                    }
                }
            } else {
                curCallId = mCurCallID
            }
            TRTCLogger.d(TRTCCalling.TAG, "onInvitationCancelled: curCallId = $curCallId")
            if (inviteID == curCallId) {
                playHangupMusic()
                stopCall()
                exitRoom()
                if (mTRTCInternalListenerManager != null) {
                    mTRTCInternalListenerManager.onCallingCancel()
                }
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
            TRTCLogger.d(
                TRTCCalling.TAG,
                "onInvitationTimeout inviteID : " + inviteID + " , mCurCallID : " + mCurCallID
                        + " ,inviteeList: " + inviteeList
            )
            //移除缓存数据
            mInviteMap.remove(inviteID)
            val curCallId: String
            if (checkIsHasGroupIDCall()) {
                curCallId = getCallIDWithUserID(inviteeList[0])
                val invitee = inviteeList[0]
                for (id in mCurRoomRemoteUserSet) {
                    if (invitee != id) {
                        sendInviteAction(
                            com.tencent.liteav.trtccalling.model.impl.base.CallModel.VIDEO_CALL_ACTION_SPONSOR_TIMEOUT,
                            invitee,
                            id,
                            null
                        )
                    }
                }
            } else {
                curCallId = mCurCallID
            }
            TRTCLogger.d(TRTCCalling.TAG, "curCallId : $curCallId , mCurCallID : $mCurCallID")
            if (inviteID != curCallId && inviteID != mSwitchToAudioCallID) {
                return
            }
            // 邀请者
            if (TextUtils.isEmpty(mCurSponsorForMe)) {
                //1.主叫所有用户都超时,也就是没人接听->主叫处理退房逻辑;
                if (mRemoteUserInTRTCRoom.size == 0) {
                    for (userID in inviteeList) {
                        if (mTRTCInternalListenerManager != null) {
                            mTRTCInternalListenerManager.onNoResp(userID)
                        }
                        mCurInvitedList.remove(userID)
                        mCurRoomRemoteUserSet.remove(userID)
                    }
                    stopDialingMusic()
                    preExitRoom(null)
                    playHangupMusic()
                    unregisterSensorEventListener()
                } else {
                    //2.主叫端:某个用户接听后,还有其他用户超时信息,则只回调到上层更新主叫界面该超时用户的UI
                    for (userID in inviteeList) {
                        if (mTRTCInternalListenerManager != null) {
                            mTRTCInternalListenerManager.onNoResp(userID)
                        }
                        mCurInvitedList.remove(userID)
                        mCurRoomRemoteUserSet.remove(userID)
                    }
                }
            } else {
                //被邀请者
                TRTCLogger.d(
                    TRTCCalling.TAG, "mCurInvitedList = " + mCurInvitedList
                            + " , mCurRoomRemoteUserSet = " + mCurRoomRemoteUserSet
                )
                // 1.自己超时
                if (inviteeList.contains(TUILogin.getUserId())) {
                    stopCall()
                    if (mTRTCInternalListenerManager != null) {
                        mTRTCInternalListenerManager.onCallingTimeout()
                    }
                    mCurInvitedList.removeAll(inviteeList)
                    mCurRoomRemoteUserSet.removeAll(inviteeList)
                    preExitRoom(null)
                    playHangupMusic()
                    unregisterSensorEventListener()
                    return
                }
                //2.其他人超时,不处理退房逻辑,只更新超时用户的UI
                for (id in inviteeList) {
                    if (mTRTCInternalListenerManager != null) {
                        mTRTCInternalListenerManager.onNoResp(id)
                    }
                    mCurInvitedList.remove(id)
                    mCurRoomRemoteUserSet.remove(id)
                }
            }
        }
    }

}