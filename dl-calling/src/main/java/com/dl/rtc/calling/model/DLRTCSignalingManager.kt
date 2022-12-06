package com.dl.rtc.calling.model

import android.text.TextUtils
import com.dl.lib.util.log.MPTimber
import com.dl.rtc.calling.model.bean.DLRTCCallModel
import com.dl.rtc.calling.model.bean.DLRTCSignallingData
import com.dl.rtc.calling.util.DLRTCSignallingUtil
import com.google.gson.GsonBuilder
import com.tencent.imsdk.v2.*
import java.util.*

/**
 *Author: 彭石林
 *Time: 2022/11/4 13:02
 * Description: 管理发送信令
 */
object DLRTCSignalingManager {
    private val TAG_LOG = "DLRTCSignalingManager"
    /**
     * room id 的取值范围
     */
    private const val ROOM_ID_MIN = 1
    private const val ROOM_ID_MAX = Int.MAX_VALUE

    fun generateModel(action: Int,mLastCallModel: DLRTCCallModel): DLRTCCallModel {
        val callModel: DLRTCCallModel =
            mLastCallModel.clone() as DLRTCCallModel
        callModel.action = action
        return callModel
    }

    fun generateRoomID(): Int {
        val random = Random()
        return random.nextInt(ROOM_ID_MAX - ROOM_ID_MIN + 1) + ROOM_ID_MIN
    }

    fun sendInvite(
        receiver: String,
        data: String,
        info: V2TIMOfflinePushInfo? = null,
        timeout: Int,
        callback: V2TIMCallback?
    ): String? {
        MPTimber.tag(TAG_LOG).d(String.format("sendInvite, receiver=%s, data=%s", receiver, data))
        return V2TIMManager.getSignalingManager()
            .invite(receiver, data, false, info, timeout, object : V2TIMCallback {
                override fun onError(code: Int, desc: String) {
                    callback?.onError(code, desc)
                }

                override fun onSuccess() {
                    callback?.onSuccess()
                }
            })
    }

    fun sendGroupInvite(
        groupId: String,
        inviteeList: List<String>,
        data: String,
        timeout: Int,
        callback: V2TIMCallback?
    ): String? {
        MPTimber.tag(TAG_LOG).d(
            String.format(
                "sendGroupInvite, groupId=%s, inviteeList=%s, data=%s",
                groupId,
                inviteeList.toTypedArray().contentToString(),
                data
            )
        )
        return V2TIMManager.getSignalingManager()
            .inviteInGroup(groupId, inviteeList, data, false, timeout, object : V2TIMCallback {
                override fun onError(code: Int, desc: String) {
                    callback?.onError(code, desc)
                }

                override fun onSuccess() {
                    callback?.onSuccess()
                }
            })
    }

    fun acceptInvite(inviteId: String, data: String, callback: V2TIMCallback?) {
        MPTimber.tag(TAG_LOG).d(
            String.format("acceptInvite, inviteId=%s, data=%s", inviteId, data)
        )
        V2TIMManager.getSignalingManager().accept(inviteId, data, object : V2TIMCallback {
            override fun onError(code: Int, desc: String) {
                callback?.onError(code, desc)
            }

            override fun onSuccess() {
                callback?.onSuccess()
            }
        })
    }

    fun rejectInvite(inviteId: String, data: String, callback: V2TIMCallback?) {
        MPTimber.tag(TAG_LOG).d(String.format("rejectInvite, inviteId=%s, data=%s", inviteId, data))
        V2TIMManager.getSignalingManager().reject(inviteId, data, object : V2TIMCallback {
            override fun onError(code: Int, desc: String) {
                callback?.onError(code, desc)
            }

            override fun onSuccess() {
                callback?.onSuccess()
            }
        })
    }

    fun cancelInvite(inviteId: String, data: String, callback: V2TIMCallback?) {
        MPTimber.tag(TAG_LOG).d(
            String.format("cancelInvite, inviteId=%s, data=%s", inviteId, data)
        )
        V2TIMManager.getSignalingManager().cancel(inviteId, data, object : V2TIMCallback {
            override fun onError(code: Int, desc: String) {
                callback?.onError(code, desc)
            }

            override fun onSuccess() {
                callback?.onSuccess()
            }
        })
    }

    /**
     * c2c多人通话增加:
     * A呼叫B,C,D; A是主叫,循环向B,C,D发送一条C2C单聊消息,A能监听到B,C,D的状态,但是B,C,D之间无法互相感知;
     * 因此A做为中间管理器,通过此方法中转消息.
     * 例如:B超时,A收到B超时的回调,通过下列方法将B超时的信息传递给C,D
     *
     * @param action  接听,超时,取消,拒绝
     * @param invitee 发生上述action事件的用户,B
     * @param userId  需要接收action事件的用户,C/D
     * @param data    需转发的信息,忙线和视频切语音需要从data中获取到message,然后主叫转发,其他不需要
     */
    fun sendInviteAction(action: Int, invitee: String, userId: String, mData: String ? =null,callIDWithUserID : String,mCurInvitedList : List<String>) {
        var transferData: DLRTCSignallingData? = null
        if (!TextUtils.isEmpty(mData)) {
            transferData = mData?.let { DLRTCSignallingUtil.convert2CallingData(it) }
        }
        MPTimber.tag(TAG_LOG).d("action:$action ,invitee:$invitee ,userId:$userId ,data:$mData")
        val callDataInfo = DLRTCSignallingData.DataInfo()
        callDataInfo.cmd = (DLRTCCallModel.VALUE_MSG_SYNC_INFO)
        callDataInfo.userIDs = (mCurInvitedList)
        transferData?.data?.apply{
            callDataInfo.message = (message)
        }
        val signallingData = DLRTCSignallingUtil.createSignallingData()
        signallingData.apply {
            data = callDataInfo
            callAction = action
            callId = callIDWithUserID
            user = invitee
        }
        val gsonBuilder = GsonBuilder()
        val dataStr = gsonBuilder.create().toJson(signallingData)
        V2TIMManager.getInstance().sendC2CCustomMessage(dataStr.toByteArray(), userId,
            object : V2TIMValueCallback<V2TIMMessage> {
                override fun onSuccess(v2TIMMessage: V2TIMMessage) {
                    MPTimber.tag(TAG_LOG).d("onSuccess: v2TIMMessage = $v2TIMMessage")
                }

                override fun onError(errorCode: Int, errorMsg: String) {
                    MPTimber.tag(TAG_LOG).d("onError: errorCode = $errorCode , errorMsg = $errorMsg")
                }
            })
    }

}