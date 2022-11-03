package com.dl.rtc.calling.util

import com.dl.lib.util.log.MPTimber
import com.dl.rtc.calling.model.bean.DLRTCCallModel
import com.dl.rtc.calling.model.bean.DLRTCSignallingData
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

/**
 *Author: 彭石林
 *Time: 2022/11/3 18:46
 * Description: This is DLRTCSignallingUtil
 */
class DLRTCSignallingUtil {
    val TAG_LOG = "DLRTCSignallingUtil"
    /**
     * 信令数据模型转换
     */
    fun convert2CallingData(data : String): DLRTCSignallingData {
        val signallingData = DLRTCSignallingData()
        val extraMap: Map<String, Any>?
        try {
            extraMap = Gson().fromJson<Map<*, *>>(data, MutableMap::class.java) as Map<String, Any>?
            if (extraMap == null) {
                MPTimber.tag(TAG_LOG).e("onReceiveNewInvitation extraMap is null, ignore")
                return signallingData
            }
            if (extraMap.containsKey(DLRTCCallModel.KEY_VERSION)) {
                val version =
                    extraMap[DLRTCCallModel.KEY_VERSION]
                if (version is Double) {
                    signallingData.setVersion(version.toInt())
                } else {
                    TRTCLogger.e(TRTCCalling.TAG, "version is not Double, value is :$version")
                }
            }
            if (extraMap.containsKey(DLRTCCallModel.KEY_PLATFORM)) {
                val platform =
                    extraMap[DLRTCCallModel.KEY_PLATFORM]
                if (platform is String) {
                    signallingData.setPlatform(platform as String?)
                } else {
                    TRTCLogger.e(TRTCCalling.TAG, "platform is not string, value is :$platform")
                }
            }
            if (extraMap.containsKey(DLRTCCallModel.KEY_BUSINESS_ID)) {
                val businessId =
                    extraMap[DLRTCCallModel.KEY_BUSINESS_ID]
                if (businessId is String) {
                    signallingData.setBusinessID(businessId as String?)
                } else {
                    TRTCLogger.e(TRTCCalling.TAG, "businessId is not string, value is :$businessId")
                }
            }

            //兼容老版本某些字段
            if (extraMap.containsKey(DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_TYPE)) {
                val callType =
                    extraMap[DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_TYPE]
                if (callType is Double) {
                    signallingData.setCallType(callType.toInt())
                } else {
                    TRTCLogger.e(TRTCCalling.TAG, "callType is not Double, value is :$callType")
                }
            }
            if (extraMap.containsKey(DLRTCCallModel.SIGNALING_EXTRA_KEY_ROOM_ID)) {
                val roomId =
                    extraMap[DLRTCCallModel.SIGNALING_EXTRA_KEY_ROOM_ID]
                if (roomId is Double) {
                    signallingData.setRoomId(roomId.toInt())
                } else {
                    TRTCLogger.e(TRTCCalling.TAG, "roomId is not Double, value is :$roomId")
                }
            }
            if (extraMap.containsKey(DLRTCCallModel.SIGNALING_EXTRA_KEY_LINE_BUSY)) {
                val lineBusy =
                    extraMap[DLRTCCallModel.SIGNALING_EXTRA_KEY_LINE_BUSY]
                if (lineBusy is String) {
                    signallingData.setLineBusy(lineBusy as String?)
                } else {
                    TRTCLogger.e(TRTCCalling.TAG, "lineBusy is not string, value is :$lineBusy")
                }
            }
            if (extraMap.containsKey(DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_END)) {
                val callEnd =
                    extraMap[DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_END]
                if (callEnd is Double) {
                    signallingData.setCallEnd(callEnd.toInt())
                } else {
                    TRTCLogger.e(TRTCCalling.TAG, "callEnd is not Double, value is :$callEnd")
                }
            }
            if (extraMap.containsKey(DLRTCCallModel.SIGNALING_EXTRA_KEY_SWITCH_AUDIO_CALL)) {
                val switchToAudioCall =
                    extraMap[DLRTCCallModel.SIGNALING_EXTRA_KEY_SWITCH_AUDIO_CALL]
                if (switchToAudioCall is String) {
                    signallingData.setSwitchToAudioCall(switchToAudioCall as String?)
                } else {
                    TRTCLogger.e(
                        TRTCCalling.TAG,
                        "switchToAudioCall is not string, value is :$switchToAudioCall"
                    )
                }
            }
            if (extraMap.containsKey(DLRTCCallModel.KEY_DATA)) {
                val dataMapObj =
                    extraMap[DLRTCCallModel.KEY_DATA]
                if (dataMapObj != null && dataMapObj is Map<*, *>) {
                    val dataMap = dataMapObj as Map<String, Any>
                    val dataInfo: DLRTCSignallingData.DataInfo =
                        convert2DataInfo(dataMap)
                    signallingData.setData(dataInfo)
                } else {
                    TRTCLogger.e(TRTCCalling.TAG, "dataMapObj is not map, value is :$dataMapObj")
                }
            }
            if (extraMap.containsKey(DLRTCCallModel.KEY_CALLACTION)) {
                val callAction =
                    extraMap[DLRTCCallModel.KEY_CALLACTION]
                if (callAction is Double) {
                    signallingData.setCallAction(callAction.toInt())
                } else {
                    TRTCLogger.e(TRTCCalling.TAG, "callAciton is not Double, value is :$callAction")
                }
            }
            if (extraMap.containsKey(DLRTCCallModel.KEY_CALLID)) {
                val callId =
                    extraMap[DLRTCCallModel.KEY_CALLID]
                if (callId is String) {
                    signallingData.setcallid(callId as String?)
                } else {
                    TRTCLogger.e(TRTCCalling.TAG, "callId is not String, value is :$callId")
                }
            }
            if (extraMap.containsKey(DLRTCCallModel.KEY_USER)) {
                val user =
                    extraMap[DLRTCCallModel.KEY_USER]
                if (user is String) {
                    signallingData.setUser(user as String?)
                } else {
                    TRTCLogger.e(TRTCCalling.TAG, "user is not String, value is :$user")
                }
            }
        } catch (e: JsonSyntaxException) {
            TRTCLogger.e(TRTCCalling.TAG, "convert2CallingDataBean json parse error")
        }
        return signallingData
    }
}