package com.dl.rtc.calling.model

import android.text.TextUtils
import com.blankj.utilcode.util.StringUtils
import com.dl.lib.util.log.MPTimber
import com.dl.rtc.calling.R
import com.dl.rtc.calling.model.bean.DLRTCCallModel
import com.dl.rtc.calling.model.bean.DLRTCMessageCustom
import com.dl.rtc.calling.model.bean.DLRTCOfflineMessageBean
import com.dl.rtc.calling.model.bean.DLRTCOfflineMessageContainerBean
import com.google.gson.Gson
import com.tencent.imsdk.v2.*
import com.tencent.qcloud.tuicore.TUILogin

/**
 *Author: 彭石林
 *Time: 2022/11/4 14:32
 * Description: 离线推送管理
 */
object DLRTCOfflineMessageModel {
    private val TAG_LOG = "DLRTCOfflineMessageModel"

    /**
     * 发送离线推送信息
     */
    fun sendOnlineMessageWithOfflinePushInfo(userId : String, nicknames : String? = null, model : DLRTCCallModel,mFaceUrl : String? = null){
        val invitedList: MutableList<String> = java.util.ArrayList()
        val containerBean = DLRTCOfflineMessageContainerBean()
        val entity = DLRTCOfflineMessageBean().apply {
            content = Gson().toJson(model)
            sender = TUILogin.getLoginUser() // 发送者肯定是登录账号
            action = DLRTCOfflineMessageBean.REDIRECT_ACTION_CALL
            sendTime = System.currentTimeMillis() / 1000
            nickname = nicknames.orEmpty()
            faceUrl = mFaceUrl.orEmpty()
        }
        model.sender = TUILogin.getLoginUser()
        model.apply {
            val isGroup = !TextUtils.isEmpty(groupId)
            if (isGroup) {
                entity.chatType = V2TIMConversation.V2TIM_GROUP
                invitedList.addAll(invitedList)
            }else {
                invitedList.add(userId)
            }
        }
        containerBean.entity = entity
        val v2TIMOfflinePushInfo = V2TIMOfflinePushInfo().apply {
            ext = Gson().toJson(containerBean).toByteArray()
            // OPPO必须设置ChannelID才可以收到推送消息，这个channelID需要和控制台一致
            setAndroidOPPOChannelID("tuikit")
            desc = StringUtils.getString(R.string.trtccalling_title_have_a_call_invitation)
            title = nicknames
        }
        val custom = DLRTCMessageCustom()
        custom.businessID = DLRTCMessageCustom.BUSINESS_ID_AV_CALL
        val message = V2TIMManager.getMessageManager().createCustomMessage(Gson().toJson(custom).toByteArray())
        for (receiver in invitedList) {
            MPTimber.tag(TAG_LOG).i("sendOnlineMessage to $receiver")
            V2TIMManager.getMessageManager().sendMessage(message, receiver, null, V2TIMMessage.V2TIM_PRIORITY_DEFAULT,
                    true, v2TIMOfflinePushInfo, object : V2TIMSendCallback<V2TIMMessage?> {
                        override fun onError(code: Int, desc: String) {
                            MPTimber.tag(TAG_LOG).e("sendOnlineMessage failed, code:$code, desc:$desc")
                        }
                        override fun onProgress(progress: Int) {}
                        override fun onSuccess(v2TIMMessage: V2TIMMessage?) {
                            v2TIMMessage?.apply{
                                MPTimber.tag(TAG_LOG).i("sendOnlineMessage msgId: $msgID")
                            }
                        }
                })
        }
    }

    /**
     * 创建离线推送消息
     */
    fun createV2TIMOfflinePushInfo(
        callModel: DLRTCCallModel,
        userId: String,
        nickname: String,
        mFaceUrl : String?
    ): V2TIMOfflinePushInfo {
        val containerBean = DLRTCOfflineMessageContainerBean()
        val entity: DLRTCOfflineMessageBean =
            DLRTCOfflineMessageBean()
        callModel.sender = TUILogin.getLoginUser()
        entity.content = Gson().toJson(callModel)
        entity.sender = TUILogin.getLoginUser() // 发送者肯定是登录账号
        entity.action = DLRTCOfflineMessageBean.REDIRECT_ACTION_CALL
        entity.sendTime = System.currentTimeMillis() / 1000
        entity.nickname = nickname
        if (mFaceUrl != null) {
            entity.faceUrl = mFaceUrl
        }
        containerBean.entity = entity
        val invitedList: MutableList<String> = ArrayList()
        MPTimber.tag(TAG_LOG).d("createV2TIMOfflinePushInfo: entity = $entity")
        invitedList.add(userId)
        val v2TIMOfflinePushInfo = V2TIMOfflinePushInfo()
        v2TIMOfflinePushInfo.ext = Gson().toJson(containerBean).toByteArray()
        // OPPO必须设置ChannelID才可以收到推送消息，这个channelID需要和控制台一致
        v2TIMOfflinePushInfo.setAndroidOPPOChannelID("tuikit")
        v2TIMOfflinePushInfo.desc =
            StringUtils.getString(R.string.trtccalling_title_have_a_call_invitation)
        v2TIMOfflinePushInfo.title = nickname
        //设置自定义铃声
        v2TIMOfflinePushInfo.setIOSSound("phone_ringing.mp3")
        return v2TIMOfflinePushInfo
    }
}