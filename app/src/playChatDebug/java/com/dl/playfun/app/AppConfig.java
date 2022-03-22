package com.dl.playfun.app;

import com.dl.playfun.BuildConfig;
import com.dl.playfun.entity.BannerItemEntity;

/**
 * @author wulei
 */
public class AppConfig {
    public static final boolean isDebug = BuildConfig.DEBUG;
    /**
     * 版本编号
     */
    public static final Integer VERSION_CODE = BuildConfig.VERSION_CODE;
    public static final String VERSION_NAME = BuildConfig.VERSION_NAME;
    //上报
    public static final String VERSION_NAME_PUSH = "1.3.2";
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
     * 服务端接口HOST
     */
    public static final String API_BASE_URL = BuildConfig.API_BASE_URL;

    /**
     * H5 WEB 端HOST
     */
    public static final String WEB_BASE_URL = BuildConfig.WEB_BASE_URL;
    /**
     * 图片HOST
     */
//    public static final String IMAGE_BASE_URL = "https://jmasktest.oss-ap-southeast-1.aliyuncs.com/";

    public static final String IMAGE_BASE_URL = BuildConfig.IMAGE_BASE_URL;
    /**
     * 服务条款url
     */
    public static final String TERMS_OF_SERVICE_URL = "https://sites.google.com/view/playchat-platformuseragreement";
    /**
     * 隐私政策url
     */
    public static final String PRIVACY_POLICY_URL = "https://sites.google.com/view/playchat-privacy-agreement";
    /**
     * 排行榜 帮助url
     */
    public static final String RANK_LIST_HELP = "https://news.joy-mask.com/help/3";
    // 访问的endpoint地址
    public static final String OSS_ENDPOINT = "https://oss-ap-southeast-1.aliyuncs.com";
    //任务中心兑换说明
    public static final String TASK_TITLE_HELP = "https://m.joy-mask.com/TaskRule";
    public static final String ExchangeRecord_URL = BuildConfig.WEB_BASE_URL + "ExchangeRule";
    //        // 访问的endpoint地址
//    public static final String OSS_ENDPOINT = "https://oss-ap-southeast-1.aliyuncs.com";
//    //callback 测试地址
////    public static final String OSS_CALLBACK_URL = "http://oss-demo.aliyuncs.com:23450";
//    // STS 鉴权服务器地址，使用前请参照文档 https://help.aliyun.com/document_detail/31920.html 介绍配置STS 鉴权服务器地址。
//    // 或者根据工程sts_local_server目录中本地鉴权服务脚本代码启动本地STS 鉴权服务器。详情参见sts_local_server 中的脚本内容。
    public static final String STS_SERVER_URL = API_BASE_URL + "api/aliyun/sts";//STS 地址
    public static final String BUCKET_NAME = BuildConfig.BUCKET_NAME;
    public static final String CONFIG_DEFAULT = "{\"program_time\":[{\"id\":1,\"name\":\"淩晨\"},{\"id\":2,\"name\":\"上午\"},{\"id\":3,\"name\":\"中午\"},{\"id\":4,\"name\":\"下午\"},{\"id\":5,\"name\":\"晚上\"},{\"id\":6,\"name\":\"通宵\"},{\"id\":7,\"name\":\"一整天\"}],\"hope_object\":[{\"id\":1,\"name\":\"看臉\"},{\"id\":2,\"name\":\"有趣\"},{\"id\":3,\"name\":\"大方\"},{\"id\":4,\"name\":\"關愛我\"},{\"id\":5,\"name\":\"看感覺\"},{\"id\":6,\"name\":\"無所謂\"}],\"report_reason\":[{\"id\":1,\"name\":\"發廣告\"},{\"id\":2,\"name\":\"騷擾、謾罵、不文明聊天\"},{\"id\":3,\"name\":\"虛假照片\"},{\"id\":4,\"name\":\"色情低俗\"},{\"id\":5,\"name\":\"她是騙子\"}],\"evaluate\":{\"evaluate_female\":[{\"id\":1,\"name\":\"漂亮\",\"type\":0},{\"id\":2,\"name\":\"身材好\",\"type\":0},{\"id\":3,\"name\":\"年齡真實\",\"type\":0},{\"id\":4,\"name\":\"有趣\",\"type\":0},{\"id\":5,\"name\":\"友好\",\"type\":0},{\"id\":6,\"name\":\"氣質好\",\"type\":0},{\"id\":7,\"name\":\"高冷\",\"type\":1},{\"id\":8,\"name\":\"脾氣火爆\",\"type\":1},{\"id\":9,\"name\":\"敷衍\",\"type\":1}],\"evaluate_male\":[{\"id\":10,\"name\":\"禮貌\",\"type\":0},{\"id\":11,\"name\":\"有趣\",\"type\":0},{\"id\":12,\"name\":\"大方\",\"type\":0},{\"id\":13,\"name\":\"爽塊\",\"type\":1},{\"id\":14,\"name\":\"唬烂\",\"type\":1},{\"id\":15,\"name\":\"不友好\",\"type\":1}]},\"occupation\":[{\"id\":1,\"name\":\"信息技術\",\"item\":[{\"id\":11,\"name\":\"互聯網\"},{\"id\":12,\"name\":\"IT\"},{\"id\":13,\"name\":\"通訊\"},{\"id\":14,\"name\":\"電信運營\"},{\"id\":15,\"name\":\"網絡遊戲\"}]},{\"id\":2,\"name\":\"金融保險\",\"item\":[{\"id\":16,\"name\":\"投資\"},{\"id\":17,\"name\":\"股票/基金\"},{\"id\":18,\"name\":\"保險\"},{\"id\":19,\"name\":\"銀行\"},{\"id\":20,\"name\":\"信托/擔保\"}]},{\"id\":3,\"name\":\"商業服務\",\"item\":[{\"id\":21,\"name\":\"咨詢\"},{\"id\":22,\"name\":\"個體經營\"},{\"id\":23,\"name\":\"美容美發\"},{\"id\":24,\"name\":\"旅遊\"},{\"id\":25,\"name\":\"酒店餐飲\"},{\"id\":26,\"name\":\"休閑娛樂\"},{\"id\":27,\"name\":\"貿易\"},{\"id\":28,\"name\":\"汽車\"},{\"id\":29,\"name\":\"房地產\"},{\"id\":30,\"name\":\"物業管理\"},{\"id\":31,\"name\":\"裝修/裝潢\"},{\"id\":32,\"name\":\"偵探\"}]},{\"id\":4,\"name\":\"工程制造\",\"item\":[{\"id\":33,\"name\":\"建築\"},{\"id\":34,\"name\":\"土木工程\"},{\"id\":35,\"name\":\"機械制造\"},{\"id\":36,\"name\":\"電子\"},{\"id\":37,\"name\":\"生物醫藥\"},{\"id\":38,\"name\":\"食品\"},{\"id\":39,\"name\":\"服裝\"},{\"id\":40,\"name\":\"能源\"}]},{\"id\":5,\"name\":\"交通運輸\",\"item\":[{\"id\":41,\"name\":\"航空\"},{\"id\":42,\"name\":\"鐵路\"},{\"id\":43,\"name\":\"航運/船舶\"},{\"id\":44,\"name\":\"公共交通\"},{\"id\":45,\"name\":\"物流運輸\"}]},{\"id\":6,\"name\":\"文化傳媒\",\"item\":[{\"id\":46,\"name\":\"媒體出版\"},{\"id\":47,\"name\":\"設計\"},{\"id\":48,\"name\":\"文化傳播\"},{\"id\":49,\"name\":\"廣告創意\"},{\"id\":50,\"name\":\"動漫\"},{\"id\":51,\"name\":\"公關/會展\"},{\"id\":52,\"name\":\"攝影\"}]},{\"id\":7,\"name\":\"娛樂體育\",\"item\":[{\"id\":53,\"name\":\"影視\"},{\"id\":54,\"name\":\"運動體育\"},{\"id\":55,\"name\":\"音樂\"},{\"id\":56,\"name\":\"模特\"}]},{\"id\":8,\"name\":\"公共事業\",\"item\":[{\"id\":57,\"name\":\"醫療\"},{\"id\":58,\"name\":\"法律\"},{\"id\":59,\"name\":\"教育\"},{\"id\":60,\"name\":\"政府機關\"},{\"id\":61,\"name\":\"科研\"},{\"id\":62,\"name\":\"公益\"}]},{\"id\":9,\"name\":\"學校\",\"item\":[{\"id\":63,\"name\":\"學生\"},{\"id\":64,\"name\":\"老師\"}]},{\"id\":10,\"name\":\"無\",\"item\":[{\"id\":65,\"name\":\"無\"}]}],\"city\":[{\"id\":1,\"name\":\"臺北市\"},{\"id\":2,\"name\":\"基隆市\"},{\"id\":3,\"name\":\"新北市\"},{\"id\":4,\"name\":\"宜蘭縣\"},{\"id\":5,\"name\":\"新竹縣\"},{\"id\":6,\"name\":\"桃園市\"},{\"id\":7,\"name\":\"苗栗縣\"},{\"id\":8,\"name\":\"臺中市\"},{\"id\":9,\"name\":\"彰化縣\"},{\"id\":10,\"name\":\"南投縣\"},{\"id\":11,\"name\":\"嘉義縣\"},{\"id\":12,\"name\":\"雲林縣\"},{\"id\":13,\"name\":\"臺南市\"},{\"id\":14,\"name\":\"高雄市\"},{\"id\":15,\"name\":\"澎湖縣\"},{\"id\":16,\"name\":\"屏東縣\"},{\"id\":17,\"name\":\"臺東縣\"},{\"id\":18,\"name\":\"花蓮縣\"},{\"id\":19,\"name\":\"金門縣\"},{\"id\":20,\"name\":\"連江縣\"}],\"theme\":[{\"id\":1,\"name\":\"健身私教\",\"icon\":\"images/教练.png\"},{\"id\":2,\"name\":\"社交聚會\",\"icon\":\"images/聚会.png\"},{\"id\":3,\"name\":\"遊戲陪玩\",\"icon\":\"images/游戏.png\"},{\"id\":4,\"name\":\"真人約拍\",\"icon\":\"images/约拍.png\"},{\"id\":5,\"name\":\"旅行伴遊\",\"icon\":\"images/旅游.png\"},{\"id\":6,\"name\":\"情感傾述\",\"icon\":\"images/倾诉.png\"},{\"id\":7,\"name\":\"電影KTV\",\"icon\":\"images/电影.png\"},{\"id\":8,\"name\":\"舒壓按摩\",\"icon\":\"images/按摩.png\"}],\"height\":[{\"id\":0,\"name\":\"不顯示\"},{\"id\":140,\"name\":\"140CM\"},{\"id\":141,\"name\":\"141CM\"},{\"id\":142,\"name\":\"142CM\"},{\"id\":143,\"name\":\"143CM\"},{\"id\":144,\"name\":\"144CM\"},{\"id\":145,\"name\":\"145CM\"},{\"id\":146,\"name\":\"146CM\"},{\"id\":147,\"name\":\"147CM\"},{\"id\":148,\"name\":\"148CM\"},{\"id\":149,\"name\":\"149CM\"},{\"id\":150,\"name\":\"150CM\"},{\"id\":151,\"name\":\"151CM\"},{\"id\":152,\"name\":\"152CM\"},{\"id\":153,\"name\":\"153CM\"},{\"id\":154,\"name\":\"154CM\"},{\"id\":155,\"name\":\"155CM\"},{\"id\":156,\"name\":\"156CM\"},{\"id\":157,\"name\":\"157CM\"},{\"id\":158,\"name\":\"158CM\"},{\"id\":159,\"name\":\"159CM\"},{\"id\":160,\"name\":\"160CM\"},{\"id\":161,\"name\":\"161CM\"},{\"id\":162,\"name\":\"162CM\"},{\"id\":163,\"name\":\"163CM\"},{\"id\":164,\"name\":\"164CM\"},{\"id\":165,\"name\":\"165CM\"},{\"id\":166,\"name\":\"166CM\"},{\"id\":167,\"name\":\"167CM\"},{\"id\":168,\"name\":\"168CM\"},{\"id\":169,\"name\":\"169CM\"},{\"id\":170,\"name\":\"170CM\"},{\"id\":171,\"name\":\"171CM\"},{\"id\":172,\"name\":\"172CM\"},{\"id\":173,\"name\":\"173CM\"},{\"id\":174,\"name\":\"174CM\"},{\"id\":175,\"name\":\"175CM\"},{\"id\":176,\"name\":\"176CM\"},{\"id\":177,\"name\":\"177CM\"},{\"id\":178,\"name\":\"178CM\"},{\"id\":179,\"name\":\"179CM\"},{\"id\":180,\"name\":\"180CM\"},{\"id\":181,\"name\":\"181CM\"},{\"id\":182,\"name\":\"182CM\"},{\"id\":183,\"name\":\"183CM\"},{\"id\":184,\"name\":\"184CM\"},{\"id\":185,\"name\":\"185CM\"},{\"id\":186,\"name\":\"186CM\"},{\"id\":187,\"name\":\"187CM\"},{\"id\":188,\"name\":\"188CM\"},{\"id\":189,\"name\":\"189CM\"},{\"id\":190,\"name\":\"190CM\"},{\"id\":191,\"name\":\"191CM\"},{\"id\":192,\"name\":\"192CM\"},{\"id\":193,\"name\":\"193CM\"},{\"id\":194,\"name\":\"194CM\"},{\"id\":195,\"name\":\"195CM\"},{\"id\":196,\"name\":\"196CM\"},{\"id\":197,\"name\":\"197CM\"},{\"id\":198,\"name\":\"198CM\"},{\"id\":199,\"name\":\"199CM\"},{\"id\":200,\"name\":\"200CM\"},{\"id\":201,\"name\":\"201CM\"},{\"id\":202,\"name\":\"202CM\"},{\"id\":203,\"name\":\"203CM\"},{\"id\":204,\"name\":\"204CM\"},{\"id\":205,\"name\":\"205CM\"},{\"id\":206,\"name\":\"206CM\"},{\"id\":207,\"name\":\"207CM\"},{\"id\":208,\"name\":\"208CM\"},{\"id\":209,\"name\":\"209CM\"},{\"id\":210,\"name\":\"210CM\"},{\"id\":211,\"name\":\"211CM\"},{\"id\":212,\"name\":\"212CM\"},{\"id\":213,\"name\":\"213CM\"},{\"id\":214,\"name\":\"214CM\"},{\"id\":215,\"name\":\"215CM\"},{\"id\":216,\"name\":\"216CM\"},{\"id\":217,\"name\":\"217CM\"},{\"id\":218,\"name\":\"218CM\"},{\"id\":219,\"name\":\"219CM\"},{\"id\":220,\"name\":\"220CM\"},{\"id\":221,\"name\":\"221CM\"},{\"id\":222,\"name\":\"222CM\"},{\"id\":223,\"name\":\"223CM\"},{\"id\":224,\"name\":\"224CM\"},{\"id\":225,\"name\":\"225CM\"},{\"id\":226,\"name\":\"226CM\"},{\"id\":227,\"name\":\"227CM\"},{\"id\":228,\"name\":\"228CM\"},{\"id\":229,\"name\":\"229CM\"},{\"id\":230,\"name\":\"230CM\"}],\"weight\":[{\"id\":0,\"name\":\"不顯示\"},{\"id\":30,\"name\":\"30KG\"},{\"id\":31,\"name\":\"31KG\"},{\"id\":32,\"name\":\"32KG\"},{\"id\":33,\"name\":\"33KG\"},{\"id\":34,\"name\":\"34KG\"},{\"id\":35,\"name\":\"35KG\"},{\"id\":36,\"name\":\"36KG\"},{\"id\":37,\"name\":\"37KG\"},{\"id\":38,\"name\":\"38KG\"},{\"id\":39,\"name\":\"39KG\"},{\"id\":40,\"name\":\"40KG\"},{\"id\":41,\"name\":\"41KG\"},{\"id\":42,\"name\":\"42KG\"},{\"id\":43,\"name\":\"43KG\"},{\"id\":44,\"name\":\"44KG\"},{\"id\":45,\"name\":\"45KG\"},{\"id\":46,\"name\":\"46KG\"},{\"id\":47,\"name\":\"47KG\"},{\"id\":48,\"name\":\"48KG\"},{\"id\":49,\"name\":\"49KG\"},{\"id\":50,\"name\":\"50KG\"},{\"id\":51,\"name\":\"51KG\"},{\"id\":52,\"name\":\"52KG\"},{\"id\":53,\"name\":\"53KG\"},{\"id\":54,\"name\":\"54KG\"},{\"id\":55,\"name\":\"55KG\"},{\"id\":56,\"name\":\"56KG\"},{\"id\":57,\"name\":\"57KG\"},{\"id\":58,\"name\":\"58KG\"},{\"id\":59,\"name\":\"59KG\"},{\"id\":60,\"name\":\"60KG\"},{\"id\":61,\"name\":\"61KG\"},{\"id\":62,\"name\":\"62KG\"},{\"id\":63,\"name\":\"63KG\"},{\"id\":64,\"name\":\"64KG\"},{\"id\":65,\"name\":\"65KG\"},{\"id\":66,\"name\":\"66KG\"},{\"id\":67,\"name\":\"67KG\"},{\"id\":68,\"name\":\"68KG\"},{\"id\":69,\"name\":\"69KG\"},{\"id\":70,\"name\":\"70KG\"},{\"id\":71,\"name\":\"71KG\"},{\"id\":72,\"name\":\"72KG\"},{\"id\":73,\"name\":\"73KG\"},{\"id\":74,\"name\":\"74KG\"},{\"id\":75,\"name\":\"75KG\"},{\"id\":76,\"name\":\"76KG\"},{\"id\":77,\"name\":\"77KG\"},{\"id\":78,\"name\":\"78KG\"},{\"id\":79,\"name\":\"79KG\"},{\"id\":80,\"name\":\"80KG\"},{\"id\":81,\"name\":\"81KG\"},{\"id\":82,\"name\":\"82KG\"},{\"id\":83,\"name\":\"83KG\"},{\"id\":84,\"name\":\"84KG\"},{\"id\":85,\"name\":\"85KG\"},{\"id\":86,\"name\":\"86KG\"},{\"id\":87,\"name\":\"87KG\"},{\"id\":88,\"name\":\"88KG\"},{\"id\":89,\"name\":\"89KG\"},{\"id\":90,\"name\":\"90KG\"},{\"id\":91,\"name\":\"91KG\"},{\"id\":92,\"name\":\"92KG\"},{\"id\":93,\"name\":\"93KG\"},{\"id\":94,\"name\":\"94KG\"},{\"id\":95,\"name\":\"95KG\"},{\"id\":96,\"name\":\"96KG\"},{\"id\":97,\"name\":\"97KG\"},{\"id\":98,\"name\":\"98KG\"},{\"id\":99,\"name\":\"99KG\"},{\"id\":100,\"name\":\"100KG\"},{\"id\":101,\"name\":\"101KG\"},{\"id\":102,\"name\":\"102KG\"},{\"id\":103,\"name\":\"103KG\"},{\"id\":104,\"name\":\"104KG\"},{\"id\":105,\"name\":\"105KG\"},{\"id\":106,\"name\":\"106KG\"},{\"id\":107,\"name\":\"107KG\"},{\"id\":108,\"name\":\"108KG\"},{\"id\":109,\"name\":\"109KG\"},{\"id\":110,\"name\":\"110KG\"},{\"id\":111,\"name\":\"111KG\"},{\"id\":112,\"name\":\"112KG\"},{\"id\":113,\"name\":\"113KG\"},{\"id\":114,\"name\":\"114KG\"},{\"id\":115,\"name\":\"115KG\"},{\"id\":116,\"name\":\"116KG\"},{\"id\":117,\"name\":\"117KG\"},{\"id\":118,\"name\":\"118KG\"},{\"id\":119,\"name\":\"119KG\"},{\"id\":120,\"name\":\"120KG\"}],\"default_home_page\":\"home\",\"config\":{\"id\":1,\"CashOutServiceFee\":20,\"CoinExchangeMoney\":1,\"VideoRedPackageMoney\":30,\"ImageRedPackageMoney\":30,\"man_real\":{\"ImMoney\":\"150\",\"ImgTime\":\"2\",\"ImNumber\":\"0\",\"NewsMoney\":\"150\",\"TopicalMoney\":\"150\",\"BrowseHomeNumber\":\"10\"},\"man_user\":{\"ImMoney\":\"150\",\"ImgTime\":\"2\",\"ImNumber\":\"0\",\"NewsMoney\":\"150\",\"TopicalMoney\":\"150\",\"BrowseHomeNumber\":\"5\"},\"man_vip\":{\"ImMoney\":\"150\",\"ImgTime\":\"6\",\"ImNumber\":\"10\",\"NewsMoney\":\"150\",\"TopicalMoney\":\"150\",\"BrowseHomeNumber\":\"15\"},\"woman_user\":{\"ImMoney\":\"150\",\"ImgTime\":\"2\",\"ImNumber\":\"0\",\"NewsMoney\":\"150\",\"TopicalMoney\":\"150\",\"BrowseHomeNumber\":\"5\"},\"woman_real\":{\"ImMoney\":\"150\",\"ImgTime\":\"2\",\"ImNumber\":\"5\",\"NewsMoney\":\"0\",\"TopicalMoney\":\"0\",\"BrowseHomeNumber\":\"10\"},\"woman_vip\":{\"ImMoney\":\"150\",\"ImgTime\":\"6\",\"ImNumber\":\"5\",\"NewsMoney\":\"0\",\"TopicalMoney\":\"0\",\"BrowseHomeNumber\":\"15\"},\"created_at\":\"2020-05-1416:19:44\",\"updated_at\":\"2020-08-2514:25:43\"}}";
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
}
