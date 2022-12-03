package com.dl.rtc.calling.model

import com.dl.lib.util.log.MPTimber
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
    fun sendInviteAction(rtcModel : DLRTCStartModel) {

        val gsonBuilder = GsonBuilder()
        val dataStr = gsonBuilder.create().toJson(rtcModel)
        V2TIMManager.getInstance().sendC2CCustomMessage(dataStr.toByteArray(), rtcModel.inviteUserId,
            object : V2TIMValueCallback<V2TIMMessage> {
                override fun onSuccess(v2TIMMessage: V2TIMMessage) {
                    MPTimber.tag(TAG_LOG).d("onSuccess: v2TIMMessage = $v2TIMMessage")
                }

                override fun onError(errorCode: Int, errorMsg: String) {
                    MPTimber.tag(TAG_LOG).d("onError: errorCode = $errorCode , errorMsg = $errorMsg")
                }
            })
    }

    fun sendC2CCustomMessage(dataStr : String,userId: String) {
        V2TIMManager.getInstance().sendC2CCustomMessage(dataStr.toByteArray(), userId,
            object : V2TIMValueCallback<V2TIMMessage> {
                override fun onSuccess(v2TIMMessage: V2TIMMessage) {
                    MPTimber.tag(TAG_LOG).d("sendC2CCustomMessage onSuccess: v2TIMMessage = $v2TIMMessage")
                }

                override fun onError(errorCode: Int, errorMsg: String) {
                    MPTimber.tag(TAG_LOG).d("sendC2CCustomMessage onError: errorCode = $errorCode , errorMsg = $errorMsg")
                }
            })
    }



    /**
     * 发送C2C消息
     */
    fun sendC2CMessage(custom : String, receiverUserID: String){
        val v2TIMMessage = V2TIMManager.getMessageManager().createCustomMessage(custom.toByteArray())
        V2TIMManager.getMessageManager().sendMessage(v2TIMMessage, receiverUserID, null, V2TIMMessage.V2TIM_PRIORITY_DEFAULT, false, null, object : V2TIMSendCallback<V2TIMMessage?> {
                override fun onProgress(progress: Int) {}
                override fun onError(errorCode: Int, errorMsg: String) {
                    MPTimber.tag(TAG_LOG).d("sendC2CMessage onError: errorCode = $errorCode , errorMsg = $errorMsg")
                }
                override fun onSuccess(v2TIMMessage: V2TIMMessage?) {
                    MPTimber.tag(TAG_LOG).d("sendC2CMessage onSuccess")
                }
            })
    }

}