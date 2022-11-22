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

}