package com.dl.playfun.app;

import com.dl.playfun.BuildConfig;
import com.dl.playfun.entity.BannerItemEntity;

/**
 * @author wulei
 */
public class AppConfig {
    //是否开启日志打印
    public static final boolean isDebug = true;
    /**
     * 版本编号
     */
    public static final Integer VERSION_CODE = 100;
    public static final String VERSION_NAME = "PlayFun";
    //上报
    public static final String VERSION_NAME_PUSH = "1.0.1";
    /**
     * 腾讯IM Appkey
     */
    public static final int IM_APP_KEY = BuildConfig.IM_APP_KEY;
    /**
     * 女
     */
    public static final int FEMALE = 0;
    /**
     * 男
     */
    public static final int MALE = 1;
    /**
     * 腾讯IM离线推送ID
     */
    public static final int GOOGLE_FCM_PUSH_BUZID = BuildConfig.GOOGLE_FCM_PUSH_BUZID;
    /**
     * 客服名称
     */
    public static final String CHAT_SERVICE_USER_NAME = "administrator";
    /**
     * 客服id
     */
    public static final String CHAT_SERVICE_USER_ID = "administrator";
    /**
     * 联系客服id
     */
    public static final String CHAT_SERVICE_USER_ID_SEND = BuildConfig.CHAT_SERVICE_USER_ID_SEND;
    /**
     * 服务端接口HOST
     */
    public static final String API_BASE_URL = BuildConfig.API_BASE_URL;

    /**
     * H5 WEB 端HOST
     */
    public static final String WEB_BASE_URL = BuildConfig.WEB_BASE_URL;

    public static final String IMAGE_BASE_URL = BuildConfig.IMAGE_BASE_URL;
    // 访问的endpoint地址
    public static final String OSS_ENDPOINT = "https://oss-ap-southeast-1.aliyuncs.com";
//    // 或者根据工程sts_local_server目录中本地鉴权服务脚本代码启动本地STS 鉴权服务器。详情参见sts_local_server 中的脚本内容。
    public static final String STS_SERVER_URL = API_BASE_URL + "api/aliyun/sts";//STS 地址
    public static final String BUCKET_NAME = BuildConfig.BUCKET_NAME;
    //设置首页弹窗是否显示
    public static boolean radioAlertFlagShow = false;
    //callback 测试地址
//    public static final String OSS_CALLBACK_URL = "http://oss-demo.aliyuncs.com:23450";
    // STS 鉴权服务器地址，使用前请参照文档 https://help.aliyun.com/document_detail/31920.html 介绍配置STS 鉴权服务器地址。
    // 或者根据工程sts_local_server目录中本地鉴权服务脚本代码启动本地STS 鉴权服务器。详情参见sts_local_server 中的脚本内容。
//    public static final String STS_SERVER_URL = API_BASE_URL + "api/aliyun/sts";//STS 地址
//
//    public static final String BUCKET_NAME = "jmasktest";
    public static BannerItemEntity radioAlertFlagEntity = null;
    //新IM 发送图片oss文件夹定义
    public static String OSS_CUSTOM_FILE_NAME_CHAT = "chat";
    /**
     * @Desc TODO(跳转页面地址)
     * @author 彭石林
     * @parame
     * @return
     * @Date 2021/8/9
     */
    public static String homePageName = "";
    /**
     * 用户主动点击退出登录
     */
    public static boolean userClickOut = false;

    /**
     * @Desc TODO(阿里云剪辑是否触发)
     * @author 彭石林
     * @Date 2021/10/29
     */
    public static boolean isCorpAliyun = false;

    /**
     * @Desc TODO(福袋网址)
     * @author 彭石林
     * @parame
     * @return
     * @Date 2021/11/6
     */
    public static String FukubukuroWebUrl = null;

    public static final String GAME_SOURCES_HEADER_KEY = "game_sources_header_key";

    public static final String GAME_SOURCES_AES_KEY = "game_sources_key";
    //游戏activity绝对路径 （实际包名+类名）
    public static final String GAME_SOURCES_ACTIVITY_NAME = "game_sources_activity_name";
    //游戏请求头APP id
    public static final String GAME_SOURCES_APP_ID = "game_sources_app_id";
    //游戏配置类
    public static final String GAME_SOURCES_APP_CONFIG = "game_sources_app_config";
}
