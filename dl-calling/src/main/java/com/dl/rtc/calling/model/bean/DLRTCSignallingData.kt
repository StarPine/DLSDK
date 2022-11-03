package com.dl.rtc.calling.model.bean

import com.google.gson.annotations.SerializedName

/**
 *Author: 彭石林
 *Time: 2022/11/3 11:22
 * Description: 信令模型
 */
class DLRTCSignallingData {
    //版本号
    private var version = 0
    //信令ID
    private var businessID: String? = null
    private var platform: String? = null
    private val extInfo: String? = null
    private var data: DataInfo? = null

    //多人通话custom message增加字段
    @SerializedName("call_action")
    private var callAction = 0
    @SerializedName("callid")
    private var callId: String? = null
    private var user: String? = null


    //兼容IM老版本字段，待废弃字段
    @Deprecated("")
    @SerializedName("call_type")
    private var callType = 0

    @Deprecated("")
    @SerializedName("room_id")
    private var roomId = 0

    @Deprecated("")
    @SerializedName("call_end")
    private var callEnd = 0

    @Deprecated("")
    @SerializedName("switch_to_audio_call")
    private var switchToAudioCall: String? = null

    @Deprecated("")
    @SerializedName("line_busy")
    private var lineBusy: String? = null

    class DataInfo {
        var roomID = 0
        var cmd: String? = null
        var cmdInfo: String? = null
        var message: String? = null
        var userIDs: List<String>? = null
    }


}