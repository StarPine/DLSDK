package com.dl.rtc.calling.manager

import com.dl.lib.util.log.MPTimber
import com.tencent.imsdk.v2.V2TIMManager
import com.tencent.imsdk.v2.V2TIMSignalingListener
import com.tencent.imsdk.v2.V2TIMSimpleMsgListener
import com.tencent.imsdk.v2.V2TIMUserInfo

/**
 *Author: 彭石林
 *Time: 2022/11/3 17:33
 * Description: This is DLRTCSignallingManager
 */
class DLRTCSignallingManager {

    private val TAG_LOG = "DLRTCSignallingManager"

    private var mV2TIMSimpleMsgListenerList = ArrayList<V2TIMSimpleMsgListener>()

    private var mV2TIMSignalListenerList = ArrayList<V2TIMSignalingListener>()

    private val mLockObject = Any()

    companion object {
        fun getInstance() = InstanceHelper.singleInstance
    }
    object InstanceHelper {
        val singleInstance = DLRTCSignallingManager()
    }


    fun init() {
        V2TIMManager.getSignalingManager().addSignalingListener(mTIMSignallingListener)
        V2TIMManager.getInstance().addSimpleMsgListener(mTIMSimpleMsgListener)
    }
    fun destroy() {
            //必要的清除逻辑
        V2TIMManager.getSignalingManager().removeSignalingListener(mTIMSignallingListener)
        V2TIMManager.getInstance().removeSimpleMsgListener(mTIMSimpleMsgListener)
    }

    fun addSimpleMsgListener(v2TIMSimpleMsgListener: V2TIMSimpleMsgListener?) {
        if (v2TIMSimpleMsgListener == null) {
            return
        }
        synchronized(mLockObject) {
            if (mV2TIMSimpleMsgListenerList.contains(v2TIMSimpleMsgListener)) {
                return
            }
            this.mV2TIMSimpleMsgListenerList.add(v2TIMSimpleMsgListener)
        }
    }

    fun removeSimpleMsgListener(v2TIMSimpleMsgListener: V2TIMSimpleMsgListener?) {
        if (v2TIMSimpleMsgListener == null) {
            return
        }
        synchronized(mLockObject) { mV2TIMSimpleMsgListenerList.remove(v2TIMSimpleMsgListener) }
    }

    fun addSignalingListener(v2TIMSignalingListener: V2TIMSignalingListener?) {
        if (v2TIMSignalingListener == null) {
            return
        }
        synchronized(mLockObject) {
            if (mV2TIMSignalListenerList.contains(v2TIMSignalingListener)) {
                return
            }
            this.mV2TIMSignalListenerList.add(v2TIMSignalingListener)
        }
    }

    fun removeSignalingListener(v2TIMSignalingListener: V2TIMSignalingListener?) {
        if (v2TIMSignalingListener == null) {
            return
        }
        synchronized(mLockObject) { mV2TIMSignalListenerList.remove(v2TIMSignalingListener) }
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
            for(iterator in mV2TIMSimpleMsgListenerList){
                iterator.onRecvC2CCustomMessage(msgID, sender, customData)
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
            MPTimber.tag(TAG_LOG).d("onReceiveNewInvitation inviteID:" + inviteID + ", inviter:" + inviter
                        + ", groupID:" + groupID + ", inviteeList:" + inviteeList + " data:" + data
            )
            for(iterator in mV2TIMSignalListenerList){
                iterator.onReceiveNewInvitation(inviteID, inviter, groupID, inviteeList, data)
            }
        }

        override fun onInviteeAccepted(inviteID: String, invitee: String, data: String) {
            for(iterator in mV2TIMSignalListenerList){
                iterator.onInviteeAccepted(inviteID, invitee, data)
            }
        }

        override fun onInviteeRejected(inviteID: String, invitee: String, data: String) {
            MPTimber.tag(TAG_LOG).d( "onInviteeRejected inviteID:" + inviteID
                        + ", invitee:" + invitee + " data:" + data
            )
            for(iterator in mV2TIMSignalListenerList){
                iterator.onInviteeRejected(inviteID, invitee, data)
            }
        }

        override fun onInvitationCancelled(inviteID: String, inviter: String, data: String) {
            MPTimber.tag(TAG_LOG).d("onInvitationCancelled inviteID:$inviteID inviter:$inviter data:$data")
            for(iterator in mV2TIMSignalListenerList){
                iterator.onInvitationCancelled(inviteID, inviter, data)
            }
        }

        //C2C多人通话超时逻辑:
        //对主叫来说:
        //1. C2C多人通话只有所有用户都超时,主叫需要退出,提示"**无响应";
        //2. 某个用户已经接听后,主叫不再处理退房等超时逻辑,只更新UI,提示"**无响应";
        //对被叫来说(有唯一的callID)
        //1.如果自己超时,直接退房并通知主叫;
        //2.收到某个用户的超时,不需要处理退房逻辑,自己只需要更新UI,提示"**无响应";
        override fun onInvitationTimeout(inviteID: String, inviteeList: List<String>) {
            MPTimber.tag(TAG_LOG).d( "onInvitationTimeout inviteID : " + inviteID
                        + " ,inviteeList: " + inviteeList
            )
            for(iterator in mV2TIMSignalListenerList){
                iterator.onInvitationTimeout(inviteID, inviteeList)
            }
        }
    }
}