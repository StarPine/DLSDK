package com.dl.rtc.calling.model

/**
 *Author: 彭石林
 *Time: 2022/11/5 16:11
 * Description: This is DLRTCCallingConstants
 */
object DLRTCCallingConstants {

    const val TYPE_UNKNOWN = 0
    const val TYPE_AUDIO_CALL = 1
    const val TYPE_VIDEO_CALL = 2

    const val PARAM_NAME_ROLE = "role"


    const val DLRTCInviteUserID = "dl_rtc_invite_user_Id" ///邀请用户的userID
    const val DLRTCAcceptUserID = "dl_rtc_accept_user_id"  ///接受对端邀请的ID
    //当前是否是拨打人
    const val DLRTCInviteSelf = "dl_rtc_invite_self"
    //通话房间ID
    const val RTCInviteRoomID = "rtcInviteRoomId"
    //拨打通话ext拓展字段
    const val inviteExtJson = "inviteExtJson"
}