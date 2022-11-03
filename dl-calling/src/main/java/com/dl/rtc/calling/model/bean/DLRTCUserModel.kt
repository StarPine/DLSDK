package com.dl.rtc.calling.model.bean

/**
 *Author: 彭石林
 *Time: 2022/11/3 11:12
 * Description: This is DLRtcUserModel
 */
class DLRTCUserModel {
    var userId: String? = null
    var userAvatar: String? = null
    var userName: String? = null


    override fun toString(): String {
        return ("UserModel{"
                + "userId= '" + userId + '\''
                + ", userAvatar= '" + userAvatar + '\''
                + ", userName= '" + userName + '\''
                + '}')
    }
}