package com.dl.rtc.calling.util

import android.text.TextUtils
import com.dl.lib.util.log.MPTimber
import com.dl.rtc.calling.model.bean.DLRTCCallModel
import com.dl.rtc.calling.model.bean.DLRTCSignallingData
import com.dl.rtc.calling.util.DLRTCSignallingUtil.convert2CallingData
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.json.JSONException
import org.json.JSONObject

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
            return DLRTConversionUtil.convert2CallingData(data)
        }

        private fun convert2DataInfo(dataMap: Map<String, Any>): DLRTCSignallingData.DataInfo {
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

    fun getMap(jsonString: String): HashMap<String, Any> {
        val jsonObject: JSONObject
        try {
            jsonObject = JSONObject(jsonString)
            val keyIter: Iterator<String> = jsonObject.keys()
            var key: String
            var value: Any
            val valueMap = HashMap<String, Any>()
            while (keyIter.hasNext()) {
                key = keyIter.next()
                value = jsonObject[key] as Any
                valueMap[key] = value
            }
            return valueMap
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return HashMap()
    }

}