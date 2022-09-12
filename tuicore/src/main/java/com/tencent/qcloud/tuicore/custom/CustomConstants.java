package com.tencent.qcloud.tuicore.custom;

/**
 * Author: 彭石林
 * Time: 2022/9/2 17:01
 * Description: This is CustomConstants
 */
public class CustomConstants {
    public static final class Message {
        //BUSINESS_ID 模板标识
        public static final String CUSTOM_BUSINESS_ID_KEY = "dl_custom_tmp";
        //内容体
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
        //落币开始
        public static final String START_WINNING = "startWinning";
        //落币结束
        public static final String END_WINNING = "endWinning";
        //落币数量
        public static final String DROP_COINS = "dropCoins";
    }
    //照片、视频模块
    public static final class MediaGallery {
        //当前模块名
        public static final String MODULE_NAME = "mediaGalleryPay";
        //照片
        public static final String PHOTO_GALLERY = "photoGalleryPay";
        //视频
        public static final String  VIDEO_GALLERY = "videoGalleryPay";
    }
}
