package com.dl.rtc.calling.model.bean

import com.google.gson.annotations.SerializedName

/**
 *Author: 彭石林
 *Time: 2022/11/3 11:22
 * Description: 信令模型
 */
class DLRTCSignallingData {
    //版本号
    var version = 0

    //信令ID
    var businessID: String? = null
    var platform: String? = null
    var extInfo: String? = null
    var data: DataInfo? = null

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
        var roomID = 0
        var cmd: String? = null
        var cmdInfo: String? = null
        var message: String? = null
        var userIDs: List<String>? = null
    }


}