package com.dl.playfun.app;

import com.dl.playfun.BuildConfig;
import com.dl.playfun.entity.BannerItemEntity;
import com.dl.playfun.entity.OverseasUserEntity;

/**
 * @author wulei
 */
public class AppConfig {
    public static boolean isOpenDialog = false;
    public static boolean isRegister = false;
    public static boolean isRegisterAccost = false;
    public static boolean isMainPage = true;
    public static boolean inChating = false;

    //是否开启日志打印
    public static final boolean isDebug = BuildConfig.DEBUG;
    /**
     * 版本编号
     */
    public static final Integer VERSION_CODE = BuildConfig.VERSION_CODE;
    public static final String VERSION_NAME = BuildConfig.VERSION_NAME;
    //上报
    public static final String SDK_VERSION_NAME_PUSH = "1.4.0";
    //source 来源ID 1642158125=喵遊(俄语) 1648520220=杜拉克 //playchat 1648626888
    public static final String APPID = "1648626888";
    public static String DEVICE_CODE = "";
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
     * 客服id
     */
    public static String CHAT_SERVICE_USER_ID = "administrator";

    /**
     * 服务端默认接口HOST
     */
    public static final String DEFAULT_API_URL = BuildConfig.DEFAULT_API_URL;

    /**
     * H5 WEB 端HOST
     */
    public static final String WEB_BASE_URL = BuildConfig.WEB_BASE_URL;

    public static final String IMAGE_BASE_URL = BuildConfig.IMAGE_BASE_URL;
    // 访问的endpoint地址
    public static final String OSS_ENDPOINT = "https://oss-ap-southeast-1.aliyuncs.com";
    // oss缩放图片格式
    public static final String OSS_END_RESIZE = "?x-oss-process=image/resize,m_lfit,h_%s,w_%s";
//    // 或者根据工程sts_local_server目录中本地鉴权服务脚本代码启动本地STS 鉴权服务器。详情参见sts_local_server 中的脚本内容。
    public static final String STS_SERVER_URL =  "/api/aliyun/sts";//STS 地址
    public static final String BUCKET_NAME = BuildConfig.BUCKET_NAME;

    //临时存放第三方登录用户信息。用来注册默认读取
    public static OverseasUserEntity overseasUserEntity = null;
    //记录推币机离开页面没有投币的状态展示弹窗
    public static boolean CoinPusherGameNotPushed = false;
    /**
     * 服务条款url
     */
    public static final String TERMS_OF_SERVICE_URL = "https://sites.google.com/view/playchat-platformuseragreement";
    /**
     * 隐私政策url
     */
    public static final String PRIVACY_POLICY_URL = "https://sites.google.com/view/playchat-privacy-agreement";
//
//    public static final String BUCKET_NAME = "jmasktest";
    //新IM 发送图片oss文件夹定义
    public static String OSS_CUSTOM_FILE_NAME_CHAT = "chat";
    //兑换记录
    public static final String ExchangeRecord_URL = BuildConfig.WEB_BASE_URL + "ExchangeRule";
    //H5充值网址
    public static final String  PAY_RECHARGE_URL = "/recharge/recharge.html";
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
     * @Desc TODO(福袋网址)
     * @author 彭石林
     * @parame
     * @return
     * @Date 2021/11/6
     */
    public static String FukubukuroWebUrl = null;

    public static final String KEY_DL_AES_ENCRY = "key_dl_aes_encry";

    public static final String GAME_SOURCES_HEADER_KEY = "game_sources_header_key";

    public static final String GAME_SOURCES_AES_KEY = "game_sources_key";
    //游戏activity绝对路径 （实际包名+类名）
    public static final String GAME_SOURCES_ACTIVITY_NAME = "game_sources_activity_name";
    //游戏请求头APP id
    public static final String GAME_SOURCES_APP_ID = "game_sources_app_id";
    //游戏配置类
    public static final String GAME_SOURCES_APP_CONFIG = "game_sources_app_config";
    //登录方式
    public static String LOGIN_TYPE = "0";
}
