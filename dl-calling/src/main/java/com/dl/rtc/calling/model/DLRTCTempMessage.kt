package com.dl.rtc.calling.model

import com.tencent.qcloud.tuicore.custom.CustomConstants
import java.io.Serializable

/**
 *Author: 彭石林
 *Time: 2022/12/3 0:24
 * Description: This is DLRTCTempMessage
 */
class DLRTCTempMessage : Serializable{
    //模板ID
    private val businessID = CustomConstants.Message.CUSTOM_BUSINESS_ID_KEY

    //当前业务的模块
    private var contentBody: MsgModuleInfo? = null

    //当前用户的语言
    private var language: String? = null

    fun getBusinessID(): String {
        return businessID
    }

    fun getLanguage(): String? {
        return language
    }

    fun setLanguage(language: String?) {
        this.language = language
    }

    fun getContentBody(): MsgModuleInfo? {
        return contentBody
    }

    fun setContentBody(contentBody: MsgModuleInfo?) {
        this.contentBody = contentBody
    }

    fun CustomDlTempMessage() {}

    fun CustomDlTempMessage(contentBody: MsgModuleInfo?) {
        this.contentBody = contentBody
    }

    /**
     * @Desc TODO(当前业务的模块)
     * @author 彭石林
     * @Date 2022/9/8
     */
    class MsgModuleInfo : Serializable {
        //模块名
        var msgModuleName: String? = null

        //模块内容体
        var contentBody: MsgBodyInfo? = null

        override fun toString(): String {
            return "MsgModuleInfo{" +
                    "msgModuleName='" + msgModuleName + '\'' +
                    ", contentBody=" + contentBody +
                    '}'
        }
    }

    /**
     * @Desc TODO(消息的类型，可以自定义)
     * @author 彭石林
     * @Date 2022/9/8
     */
    class MsgBodyInfo : Serializable {
        ////消息的类型，可以自定义
        var customMsgType: String? = null
        var customMsgBody: Any? = null

        //是否隐藏ui
        var isHideUI = false

        override fun toString(): String {
            return "MsgBodyInfo{" +
                    "customMsgType='" + customMsgType + '\'' +
                    ", customMsgBody=" + customMsgBody +
                    '}'
        }
    }

    override fun toString(): String {
        return "CustomDlTempMessage{" +
                "businessID='" + businessID + '\'' +
                ", contentBody=" + contentBody +
                '}'
    }

}