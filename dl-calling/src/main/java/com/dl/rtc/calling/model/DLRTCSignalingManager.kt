package com.dl.rtc.calling.model

import com.dl.lib.util.log.MPTimber
import com.dl.rtc.calling.model.bean.DLRTCCallModel
import com.tencent.imsdk.v2.V2TIMCallback
import com.tencent.imsdk.v2.V2TIMManager
import com.tencent.imsdk.v2.V2TIMOfflinePushInfo
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
        MPTimber.tag(TAG_LOG).d(
            String.format("rejectInvite, inviteId=%s, data=%s", inviteId, data)
        )
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

}