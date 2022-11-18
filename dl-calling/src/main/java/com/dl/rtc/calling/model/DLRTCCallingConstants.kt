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

    const val PARAM_NAME_TYPE = "type"
    const val PARAM_NAME_ROLE = "role"
    const val PARAM_NAME_USERIDS = "userIDs"
    const val PARAM_NAME_GROUPID = "groupId"
    const val PARAM_NAME_SPONSORID = "sponsorID"
    const val PARAM_NAME_ISFROMGROUP = "isFromGroup"

    const val TC_TUICALLING_COMPONENT = 3

    var component = TC_TUICALLING_COMPONENT


    ///开始邀请
    const val invite = "dl_rtc_message_invite"
    ///邀请成功
    const val  inviteSucc = "dl_rtc_message_inviteSuccess"
    ///拒绝邀请
    const val  reject = "dl_rtc_message_reject"
    ///接受邀请
    const val  accept = "dl_rtc_message_accept"
    ///发起方取消邀请
    const val  cancel = "dl_rtc_message_cancel"
    ///邀请超时
    const val  timeout = "dl_rtc_message_timeout"
    /// 离开音视频房间
    const val  exitRoom = "dl_rtc_message_exitRoom"
}