package com.dl.rtc.calling.manager

import android.text.TextUtils
import com.dl.lib.util.log.MPTimber
import com.dl.rtc.calling.model.bean.DLRTCUserModel
import com.tencent.imsdk.v2.V2TIMManager
import com.tencent.imsdk.v2.V2TIMUserFullInfo
import com.tencent.imsdk.v2.V2TIMValueCallback

/**
 *Author: 彭石林
 *Time: 2022/11/5 18:54
 * Description: 查询用户信息
 */
class DLRTCCallingInfoManager {
    private val TAG_LOG = "DLRTCCallingInfoManager"

    companion object{
        val instance by lazy {
            DLRTCCallingInfoManager()
        }
        // 通过userid/phone获取用户信息回调
        interface UserCallback {
            fun onSuccess(model: DLRTCUserModel?)
            fun onFailed(code: Int, msg: String?)
        }
    }

    fun getUserInfoByUserId(userId: String?, callback: UserCallback?) {
        if (TextUtils.isEmpty(userId)) {
            MPTimber.tag(TAG_LOG).e("get user info list fail, user list is empty.")
            callback?.onFailed(-1, "get user info list fail, user list is empty.")
            return
        }
        val userList: MutableList<String?> = ArrayList()
        userList.add(userId)
        MPTimber.tag(TAG_LOG).i("get user info list $userList")
        V2TIMManager.getInstance()
            .getUsersInfo(userList, object : V2TIMValueCallback<List<V2TIMUserFullInfo>?> {
                override fun onError(errorCode: Int, errorMsg: String) {
                    MPTimber.tag(TAG_LOG).e("getUsersInfo fail, code: $errorCode, errorMsg: $errorMsg")
                    callback?.onFailed(errorCode, errorMsg)
                }

                override fun onSuccess(v2TIMUserFullInfos: List<V2TIMUserFullInfo>?) {
                    if (v2TIMUserFullInfos == null || v2TIMUserFullInfos.size <= 0) {
                        MPTimber.tag(TAG_LOG).d( "getUserInfoByUserId result ignored")
                        callback?.onFailed(-1, "getUserInfoByUserId result ignored")
                        return
                    }
                    val list = ArrayList<DLRTCUserModel>()
                    for (i in v2TIMUserFullInfos.indices) {
                        val model = DLRTCUserModel()
                        model.userName = v2TIMUserFullInfos[i].nickName
                        model.userId = v2TIMUserFullInfos[i].userID
                        model.userAvatar = v2TIMUserFullInfos[i].faceUrl
                        list.add(model)
                        MPTimber.tag(TAG_LOG).d(String.format("getUserInfoByUserId, userId=%s, userName=%s, userAvatar=%s", model.userId, model.userName, model.userAvatar))
                        if (TextUtils.isEmpty(model.userName)) {
                            model.userName = model.userId
                        }
                    }
                    callback?.onSuccess(list[0])
                }
            })
    }


}