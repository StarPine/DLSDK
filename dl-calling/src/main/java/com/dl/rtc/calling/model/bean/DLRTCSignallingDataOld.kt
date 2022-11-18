package com.dl.rtc.calling.model.bean

import com.google.gson.annotations.SerializedName

/**
 *Author: 彭石林
 *Time: 2022/11/18 16:49
 * Description: This is DLRTCSignallingDataOld
 */
class DLRTCSignallingDataOld {
    //版本号
    var version = 0

    //信令ID
    var businessID: String? = null
    var platform: String? = null
    var extInfo: String? = null
    var data: String? = null

    //多人通话custom message增加字段
    @SerializedName("call_action")
    var callAction = 0

    @SerializedName("callid")
    var callId: String? = null
    var user: String? = null


    //兼容IM老版本字段，待废弃字段
    @Deprecated("")
    @SerializedName("call_type")
    var callType = 0

    @Deprecated("")
    @SerializedName("room_id")
    var roomId = 0

    @Deprecated("")
    @SerializedName("call_end")
    var callEnd = 0

    @Deprecated("")
    @SerializedName("switch_to_audio_call")
    var switchToAudioCall: String? = null

    @Deprecated("")
    @SerializedName("line_busy")
    var lineBusy: String? = null

    class DataInfo {
        @SerializedName("room_id")
        var roomID = 0
        var cmd: String? = null
        var cmdInfo: String? = null
        var message: String? = null
        var userIDs: List<String>? = null
        var businessID: String? = null


        @SerializedName("dl_rtc_key")
        val DLRtcVersionTag = "dl_rtc_new_tag"    ///新版本data的json结构标识 默认无需改变

        @SerializedName("dl_rtc_signaling_type")
        var DLRTCMessageType = "dl_rtc_message_invite"  ///新版的连麦发送信令的消息类型的key

        @SerializedName("dl_rtc_new_tag")
        var DLRTCInviteTimeOut : Int = 30      ///连麦邀请的超时时间

        @SerializedName("dl_rtc_room_Id")
        var DLRTCInviteRoomID : Int = 0      ///当前RTC的音视频房间号

        @SerializedName("dl_rtc_invite_user_Id")
        var DLRtcInviteUserID : String? = null ///邀请用户的userID

        @SerializedName("dl_rtc_accept_user_id")
        var DLRTCAcceptUserID : String? = null  ///接受对端邀请的ID

        override fun toString(): String {
            return "DataInfo(roomID=$roomID, cmd=$cmd, cmdInfo=$cmdInfo, message=$message, userIDs=$userIDs, businessID=$businessID)"
        }

    }

    override fun toString(): String {
        return "DLRTCSignallingData(version=$version, businessID=$businessID, platform=$platform, extInfo=$extInfo, data=$data, callAction=$callAction, callId=$callId, user=$user, callType=$callType, roomId=$roomId, callEnd=$callEnd, switchToAudioCall=$switchToAudioCall, lineBusy=$lineBusy)"
    }

}