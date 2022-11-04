package com.dl.rtc.calling.model.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 *Author: 彭石林
 *Time: 2022/11/3 14:21
 * Description: This is DLRtcCallModel
 */
class DLRTCCallModel :Cloneable, Serializable {
    private val TAG: String = DLRTCCallModel::class.java.simpleName
    companion object {
        var KEY_VERSION = "version"
        var KEY_PLATFORM = "platform"
        var KEY_BUSINESS_ID = "businessID"
        var KEY_DATA = "data"
        var KEY_ROOM_ID = "room_id"
        var KEY_CMD = "cmd"
        var KEY_USERIDS = "userIDs"
        var KEY_MESSAGE = "message"
        var KEY_CALLACTION = "call_action"
        var KEY_CALLID = "callid"
        var KEY_USER = "user"

        val VALUE_VERSION = 4
        val VALUE_BUSINESS_ID = "av_call" //calling场景

        val VALUE_PLATFORM = "Android" //当前平台

        val VALUE_CMD_VIDEO_CALL = "videoCall" //视频电话呼叫

        val VALUE_CMD_AUDIO_CALL = "audioCall" //语音电话呼叫

        val VALUE_CMD_HAND_UP = "hangup" //挂断

        val VALUE_CMD_SWITCH_TO_AUDIO = "switchToAudio" //切换为语音通话

        val VALUE_MSG_LINE_BUSY = "lineBusy" //忙线

        val VALUE_MSG_SYNC_INFO = "sync_info" //C2C多人通话,主叫向其他人同步信息


        /**
         * 系统错误
         */
        val VIDEO_CALL_ACTION_ERROR = -1

        /**
         * 未知信令
         */
        val VIDEO_CALL_ACTION_UNKNOWN = 0

        /**
         * 正在呼叫
         */
        val VIDEO_CALL_ACTION_DIALING = 1

        /**
         * 发起人取消
         */
        val VIDEO_CALL_ACTION_SPONSOR_CANCEL = 2

        /**
         * 拒接电话
         */
        val VIDEO_CALL_ACTION_REJECT = 3

        /**
         * 无人接听
         */
        val VIDEO_CALL_ACTION_SPONSOR_TIMEOUT = 4

        /**
         * 挂断
         */
        val VIDEO_CALL_ACTION_HANGUP = 5

        /**
         * 电话占线
         */
        val VIDEO_CALL_ACTION_LINE_BUSY = 6

        /**
         * 接听电话
         */
        val VIDEO_CALL_ACTION_ACCEPT = 7

        /**
         * 切换语音通话
         */
        val VIDEO_CALL_SWITCH_TO_AUDIO_CALL = 8

        /**
         * 接受切换为语音通话
         */
        val VIDEO_CALL_ACTION_ACCEPT_SWITCH_TO_AUDIO = 9

        /**
         * 拒绝切换为语音通话
         */
        val VIDEO_CALL_ACTION_REJECT_SWITCH_TO_AUDIO = 10


        //兼容老版本字段，待废弃字段
        var SIGNALING_EXTRA_KEY_CALL_TYPE = "call_type"
        var SIGNALING_EXTRA_KEY_ROOM_ID = "room_id"
        var SIGNALING_EXTRA_KEY_LINE_BUSY = "line_busy"
        var SIGNALING_EXTRA_KEY_CALL_END = "call_end"
        var SIGNALING_EXTRA_KEY_SWITCH_AUDIO_CALL = "switch_to_audio_call"
    }


    @SerializedName("version")
    var version = 0

    /**
     * 表示一次通话的唯一ID
     */
    @SerializedName("call_id")
    var callId: String? = null

    /**
     * TRTC的房间号
     */
    @SerializedName("room_id")
    var roomId = 0

    /**
     * IM的群组id，在群组内发起通话时使用
     */
    @SerializedName("group_id")
    var groupId = ""

    /**
     * 信令动作
     */
    @SerializedName("action")
    var action = VIDEO_CALL_ACTION_UNKNOWN

    /**
     * 通话类型
     * 0-未知
     * 1-语音通话
     * 2-视频通话
     */
    @SerializedName("call_type")
    var callType = 0

    /**
     * 正在邀请的列表
     */
    @SerializedName("invited_list")
    var invitedList: List<String>? = null

    @SerializedName("duration")
    var duration = 0

    @SerializedName("code")
    var code = 0

    var timestamp: Long = 0
    var sender: String? = null

    // 超时时间，单位秒
    var timeout = 0
    var data: String? = null

    public override fun clone(): Any {
        var callModel: DLRTCCallModel? = null
        try {
            callModel = super.clone() as DLRTCCallModel
            if (invitedList != null) {
                callModel.invitedList = ArrayList(invitedList)
            }
        } catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }
        return callModel as Any
    }



    override fun toString(): String {
        return "CallModel{" +
                "version=" + version +
                ", callId='" + callId + '\'' +
                ", roomId=" + roomId +
                ", groupId='" + groupId + '\'' +
                ", action=" + action +
                ", callType=" + callType +
                ", invitedList=" + invitedList +
                ", duration=" + duration +
                ", code=" + code +
                ", timestamp=" + timestamp +
                ", sender=" + sender +
                '}'
    }
}