package com.dl.rtc.calling.model.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 *Author: 彭石林
 *Time: 2022/11/18 12:11
 * Description: dl自定义数据模型
 */
class DLRTCDataModel :Cloneable, Serializable {
    //新版本额外拓展字段
    @SerializedName("dl_rtc_key")
    val DLRtcVersionTag = "dl_rtc_new_tag"    ///新版本data的json结构标识 默认无需改变

    @SerializedName("dl_rtc_signaling_type")
    var DLRTCMessageType = "dl_rtc_signaling_type"  ///新版的连麦发送信令的消息类型的key

    @SerializedName("dl_rtc_new_tag")
    var DLRTCInviteTimeOut : Int = 0      ///连麦邀请的超时时间

    @SerializedName("dl_rtc_room_Id")
    var DLRTCInviteRoomID : Int = 0      ///当前RTC的音视频房间号

    @SerializedName("dl_rtc_invite_user_Id")
    var DLRtcInviteUserID : String? = null ///邀请用户的userID

    @SerializedName("dl_rtc_accept_user_id")
    var DLRTCAcceptUserID : String? = null  ///接受对端邀请的ID

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
     * 信令动作
     */
    @SerializedName("action")
    var action = DLRTCCallModel.VIDEO_CALL_ACTION_UNKNOWN

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
    var invitedList: ArrayList<String>? = null

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
        return "DLRTCDataModel(DLRtcVersionTag='$DLRtcVersionTag', DLRTCMessageType='$DLRTCMessageType', DLRTCInviteTimeOut=$DLRTCInviteTimeOut, DLRTCInviteRoomID=$DLRTCInviteRoomID, DLRtcInviteUserID=$DLRtcInviteUserID, DLRTCAcceptUserID=$DLRTCAcceptUserID, version=$version, callId=$callId, roomId=$roomId, action=$action, callType=$callType, invitedList=$invitedList, duration=$duration, code=$code, timestamp=$timestamp, sender=$sender, timeout=$timeout, data=$data)"
    }


}