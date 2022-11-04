package com.dl.rtc.calling.model.bean

/**
 *Author: 彭石林
 *Time: 2022/11/3 14:32
 * Description: 离线推送消息模型
 */
class DLRTCOfflineMessageBean {
    companion object {
        val DEFAULT_VERSION = 1
        val REDIRECT_ACTION_CHAT = 1
        val REDIRECT_ACTION_CALL = 2
        var version = DEFAULT_VERSION
        //V2TIMConversation.V2TIM_C2C;
        var chatType: Int = 1

    }

    var action = REDIRECT_ACTION_CHAT
    var sender = ""
    var nickname = ""
    var faceUrl = ""
    var content = ""

    // 发送时间戳，单位秒
    var sendTime: Long = 0

    override fun toString(): String {
        return "OfflineMessageBean{" +
                "version=" + version +
                ", chatType='" + chatType + '\'' +
                ", action=" + action +
                ", sender=" + sender +
                ", nickname=" + nickname +
                ", faceUrl=" + faceUrl +
                ", content=" + content +
                ", sendTime=" + sendTime +
                '}'
    }
}