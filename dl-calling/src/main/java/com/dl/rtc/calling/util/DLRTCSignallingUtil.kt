package com.dl.rtc.calling.util

import android.text.TextUtils
import com.dl.lib.util.log.MPTimber
import com.dl.rtc.calling.model.bean.DLRTCCallModel
import com.dl.rtc.calling.model.bean.DLRTCSignallingData
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

/**
 *Author: 彭石林
 *Time: 2022/11/3 18:46
 * Description: 信令处理工具类
 */
object  DLRTCSignallingUtil {
    //公开静态代码块
        val TAG_LOG = "DLRTCSignallingUtil"

        /**
         * 信令数据模型转换
         */
        fun convert2CallingData(data : String): DLRTCSignallingData {
            val signallingData : DLRTCSignallingData = DLRTCSignallingData()
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
                        signallingData.version = version.toInt()
                    } else {
                        MPTimber.tag(TAG_LOG).e( "version is not Double, value is :$version")
                    }
                }
                if (extraMap.containsKey(DLRTCCallModel.KEY_PLATFORM)) {
                    val platform =
                        extraMap[DLRTCCallModel.KEY_PLATFORM]
                    if (platform is String) {
                        signallingData.platform = platform
                    } else {
                        MPTimber.tag(TAG_LOG).e("platform is not string, value is :$platform")
                    }
                }
                if (extraMap.containsKey(DLRTCCallModel.KEY_BUSINESS_ID)) {
                    val businessId =
                        extraMap[DLRTCCallModel.KEY_BUSINESS_ID]
                    if (businessId is String) {
                        signallingData.businessID = businessId
                    } else {
                        MPTimber.tag(TAG_LOG).e("businessId is not string, value is :$businessId")
                    }
                }

                //兼容老版本某些字段
                if (extraMap.containsKey(DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_TYPE)) {
                    val callType =
                        extraMap[DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_TYPE]
                    if (callType is Double) {
                        signallingData.callType = callType.toInt()
                    } else {
                        MPTimber.tag(TAG_LOG).e( "callType is not Double, value is :$callType")
                    }
                }
                if (extraMap.containsKey(DLRTCCallModel.SIGNALING_EXTRA_KEY_ROOM_ID)) {
                    val roomId =
                        extraMap[DLRTCCallModel.SIGNALING_EXTRA_KEY_ROOM_ID]
                    if (roomId is Double) {
                        signallingData.roomId = roomId.toInt()
                    } else {
                        MPTimber.tag(TAG_LOG).e( "roomId is not Double, value is :$roomId")
                    }
                }
                if (extraMap.containsKey(DLRTCCallModel.SIGNALING_EXTRA_KEY_LINE_BUSY)) {
                    val lineBusy =
                        extraMap[DLRTCCallModel.SIGNALING_EXTRA_KEY_LINE_BUSY]
                    if (lineBusy is String) {
                        signallingData.lineBusy = lineBusy
                    } else {
                        MPTimber.tag(TAG_LOG).e( "lineBusy is not string, value is :$lineBusy")
                    }
                }
                if (extraMap.containsKey(DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_END)) {
                    val callEnd =
                        extraMap[DLRTCCallModel.SIGNALING_EXTRA_KEY_CALL_END]
                    if (callEnd is Double) {
                        signallingData.callEnd =callEnd.toInt()
                    } else {
                        MPTimber.tag(TAG_LOG).e("callEnd is not Double, value is :$callEnd")
                    }
                }
                if (extraMap.containsKey(DLRTCCallModel.SIGNALING_EXTRA_KEY_SWITCH_AUDIO_CALL)) {
                    val switchToAudioCall =
                        extraMap[DLRTCCallModel.SIGNALING_EXTRA_KEY_SWITCH_AUDIO_CALL]
                    if (switchToAudioCall is String) {
                        signallingData.switchToAudioCall = switchToAudioCall
                    } else {
                        MPTimber.tag(TAG_LOG).e(
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
                        signallingData.data = dataInfo
                    } else {
                        MPTimber.tag(TAG_LOG).e( "dataMapObj is not map, value is :$dataMapObj")
                    }
                }
                if (extraMap.containsKey(DLRTCCallModel.KEY_CALLACTION)) {
                    val callAction =
                        extraMap[DLRTCCallModel.KEY_CALLACTION]
                    if (callAction is Double) {
                        signallingData.callAction = callAction.toInt()
                    } else {
                        MPTimber.tag(TAG_LOG).e( "callAciton is not Double, value is :$callAction")
                    }
                }
                if (extraMap.containsKey(DLRTCCallModel.KEY_CALLID)) {
                    val callId =
                        extraMap[DLRTCCallModel.KEY_CALLID]
                    if (callId is String) {
                        signallingData.callId = callId
                    } else {
                        MPTimber.tag(TAG_LOG).e("callId is not String, value is :$callId")
                    }
                }
                if (extraMap.containsKey(DLRTCCallModel.KEY_USER)) {
                    val user =
                        extraMap[DLRTCCallModel.KEY_USER]
                    if (user is String) {
                        signallingData.user = user
                    } else {
                        MPTimber.tag(TAG_LOG).e( "user is not String, value is :$user")
                    }
                }
            } catch (e: JsonSyntaxException) {
                MPTimber.tag(TAG_LOG).e( "convert2CallingDataBean json parse error")
            }
            return signallingData
        }

        fun convert2DataInfo(dataMap: Map<String, Any>): DLRTCSignallingData.DataInfo {
            val dataInfo: DLRTCSignallingData.DataInfo =
                DLRTCSignallingData.DataInfo()
            try {
                if (dataMap.containsKey(DLRTCCallModel.KEY_CMD)) {
                    val cmd = dataMap[DLRTCCallModel.KEY_CMD]
                    if (cmd is String) {
                        dataInfo.cmd = cmd
                    } else {
                        MPTimber.tag(TAG_LOG).e("cmd is not string, value is :$cmd")
                    }
                }
                if (dataMap.containsKey(DLRTCCallModel.KEY_USERIDS)) {
                    val userIDs =
                        dataMap[DLRTCCallModel.KEY_USERIDS]
                    if (userIDs is List<*>) {
                        dataInfo.userIDs = userIDs as List<String>?
                    } else {
                        MPTimber.tag(TAG_LOG).e("userIDs is not List, value is :$userIDs")
                    }
                }
                if (dataMap.containsKey(DLRTCCallModel.KEY_ROOM_ID)) {
                    val roomId =
                        dataMap[DLRTCCallModel.KEY_ROOM_ID]
                    if (roomId is Double) {
                        dataInfo.roomID = (roomId.toInt())
                    } else {
                        MPTimber.tag(TAG_LOG).e("roomId is not Double, value is :$roomId")
                    }
                }
                if (dataMap.containsKey(DLRTCCallModel.KEY_MESSAGE)) {
                    val message =
                        dataMap[DLRTCCallModel.KEY_MESSAGE]
                    if (message is String) {
                        dataInfo.message = message
                    } else {
                        MPTimber.tag(TAG_LOG).e("message is not string, value is :$message")
                    }
                }
            } catch (e: JsonSyntaxException) {
                MPTimber.tag(TAG_LOG).e("onReceiveNewInvitation JsonSyntaxException:$e")
            }
            return dataInfo
        }

        //是否是6-30改造后的信令版本
        fun isNewSignallingVersion(signallingData: DLRTCSignallingData): Boolean {
            return !TextUtils.isEmpty(signallingData.platform) && !TextUtils.isEmpty(
                signallingData.businessID
            )
        }

        fun getSwitchAudioRejectMessage(signallingData: DLRTCSignallingData): String? {
            if (isNewSignallingVersion(signallingData)) {
                val dataInfo: DLRTCSignallingData.DataInfo =
                    signallingData.data
                        ?: return ""
                return dataInfo.message
            }
            val message: String? = signallingData.switchToAudioCall
            return if (TextUtils.isEmpty(message)) "" else message
        }

    /**
     * 是否是拨打信令消息
     */
    fun isCallingData(signallingData: DLRTCSignallingData): Boolean {
        val businessId: String? = signallingData.businessID
        // 判断新/旧版信令
        return if (!isNewSignallingVersion(signallingData)) {
            TextUtils.isEmpty(businessId)
            // 是旧版calling信令，则返回true
        } else DLRTCCallModel.VALUE_BUSINESS_ID == businessId
    }

     fun isSwitchAudioData(signallingData: DLRTCSignallingData): Boolean {
        if (!isNewSignallingVersion(signallingData)) {
            return !TextUtils.isEmpty(signallingData.switchToAudioCall)
        }
        return if (signallingData.data == null) {
            false
        } else DLRTCCallModel.VALUE_CMD_SWITCH_TO_AUDIO == signallingData.data!!
            .cmd
    }

     fun isLineBusy(signallingData: DLRTCSignallingData): Boolean {
        if (isNewSignallingVersion(signallingData)) {
            val dataInfo = signallingData.data?: return false
            return DLRTCCallModel.VALUE_MSG_LINE_BUSY == dataInfo.message
        }
        return DLRTCCallModel.SIGNALING_EXTRA_KEY_LINE_BUSY == signallingData.lineBusy
    }


    /**
     * 创建拨打信令模型
     */
    fun createSignallingData(): DLRTCSignallingData {
        val signallingData = DLRTCSignallingData()
        signallingData.version = DLRTCCallModel.VALUE_VERSION
        signallingData.businessID =DLRTCCallModel.VALUE_BUSINESS_ID
        signallingData.platform = DLRTCCallModel.VALUE_PLATFORM
        return signallingData
    }


}