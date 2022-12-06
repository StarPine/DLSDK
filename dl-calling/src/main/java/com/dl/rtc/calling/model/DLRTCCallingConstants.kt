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
    const val PARAM_NAME_FLOATWINDOW = "enableFloatWindow"
    const val EVENT_ACTIVE_HANGUP = "active_hangup"

    //onCallEvent常用类型定义
    const val EVENT_CALL_HANG_UP = "Hangup"
    const val EVENT_CALL_LINE_BUSY = "LineBusy"
    const val EVENT_CALL_CNACEL = "Cancel"
    const val EVENT_CALL_TIMEOUT = "Timeout"
    const val EVENT_CALL_NO_RESP = "NoResp"
    const val EVENT_CALL_SWITCH_TO_AUDIO = "SwitchToAudio"

    const val TC_TUICALLING_COMPONENT = 3
    const val TC_TIMCALLING_COMPONENT = 10
    const val TC_TRTC_FRAMEWORK = 1

    var component = TC_TUICALLING_COMPONENT
}