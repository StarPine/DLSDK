package com.tencent.qcloud.tuicore.custom;

/**
 * Author: 彭石林
 * Time: 2022/9/2 17:01
 * Description: This is CustomConstants
 */
public class CustomConstants {
    public static final class Message {
        public static final String CUSTOM_BUSINESS_ID_KEY = "dl_custom_tmp";
        public static final String CUSTOM_CONTENT_BODY= "contentBody";
        public static final String CUSTOM_MSG_KEY = "customMsgType";
        public static final String CUSTOM_MSG_BODY = "customMsgBody";
        //模块名
        public static final String MODULE_NAME_KEY = "msgModuleName";
    }
    //推币机模块
    public static final class CoinPusher {
        //当前推币机模块名
        public static final String MODULE_NAME = "pushCoinGame";
        //开始落币
        public static final String START_WINNING = "startWinning";
    }
}
