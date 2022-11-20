package com.dl.rtc.calling.model

/**
 *Author: 彭石林
 *Time: 2022/11/19 12:18
 * Description: This is DLRTCDataMessageType
 */
object DLRTCDataMessageType {

    val DLRTCVersionTag = "dl_rtc_key"    ///新版本data的json结构标识
    val DLRTCNewTag = "dl_rtc_new_tag"    ///新版本data的json结构标识的key值
    val DLRTCMessageType = "dl_rtc_signaling_type"  ///新版的连麦发送信令的消息类型的key
    val DLRTCInviteTimeOut = "dl_rtc_time_out"      ///连麦邀请的超时时间
    val DLRTCInviteRoomID = "dl_rtc_room_Id"       ///当前RTC的音视频房间号
    val DLRTCInviteUserID = "dl_rtc_invite_user_Id" ///邀请用户的userID
    val DLRTCAcceptUserID = "dl_rtc_accept_user_id"  ///接受对端邀请的ID

    val DLRTCInviteType = "dl_rtc_type"

    var DLRTCInviteID = "InviteID"
    ///开始邀请
    val invite = "dl_rtc_message_invite"
    ///邀请成功
    val inviteSucc = "dl_rtc_message_inviteSuccess"
    ///拒绝邀请
    val reject = "dl_rtc_message_reject"
    ///接受邀请
    val accept = "dl_rtc_message_accept"
    ///发起方取消邀请
    val cancel = "dl_rtc_message_cancel"
    ///邀请超时
    val timeout = "dl_rtc_message_timeout"
    /// 离开音视频房间
    val exitRoom = "dl_rtc_message_exitRoom"


    val onlyAudio = "dl_rtc_audio"    ///纯音频
    val video     = "dl_rtc_video"       ///音视频邀请
    enum class DLInviteRTCType {
        //  ///纯音频
        dl_rtc_audio,
        ///音视频邀请
        dl_rtc_video
    }
}