package com.dl.rtc.calling.model.bean

/**
 *Author: 彭石林
 *Time: 2022/11/3 14:39
 * Description: This is DLRTCMessageCustom
 */
class DLRTCMessageCustom {
    companion object {
        val BUSINESS_ID_GROUP_CREATE = "group_create"
        val BUSINESS_ID_AV_CALL = "av_call"
    }
    var version = 0
    var businessID: String? = null
    var opUser: String? = null
    var content: String? = null
}