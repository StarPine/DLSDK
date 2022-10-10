package com.dl.playfun.utils;

import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.Utils;
import com.dl.lib.util.MPDeviceUtils;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.manager.LocaleManager;

import java.util.Locale;

/**
 * Author: 彭石林
 * Time: 2022/10/10 18:30
 * Description: This is ElkLogEventUtils
 */
public class ElkLogEventUtils {

    /**
    * @Desc TODO(获取当前用户的公共参数)
    * @author 彭石林
    * @parame []
    * @return java.lang.String
    * @Date 2022/10/10
    */
    public String getUserDataEvent(){
        StringBuilder value = new StringBuilder();
        UserDataEntity userDataEntity = ConfigManager.getInstance().getAppRepository().readUserData();
        if(ObjectUtils.isNotEmpty(userDataEntity)){
            //用户的平台id
            //当前用户是否是vip
            //用户的昵称
            value
                //用户的平台id
                .append("`uid=").append(isNullConverterSky(userDataEntity.getId()))
                //当前用户是否是vip
                .append("`is_vip=").append(isNullConverterSky(userDataEntity.getIsVip()))
                //用户的昵称
                .append("`nickname=").append(isNullConverterSky(userDataEntity.getNickname()))
                //用户性别
                .append("`sex=").append(isNullConverterSky(userDataEntity.getSex()))
                //是不是工会主播
                .append("`is_anchor=").append(isNullConverterSky(userDataEntity.getAnchor()))
                //是否开启视频通话
                .append("`a_video=").append(isNullConverterSky(userDataEntity.isAllowVideo()))
                //是否开启语音通话
                .append("`a_audio=").append(isNullConverterSky(userDataEntity.isAllowAudio()));
        }
        return value.toString();
    }
    /**
    * @Desc TODO(获取公共参数)
    * @author 彭石林
    * @parame []
    * @return java.lang.String
    * @Date 2022/10/10
    */
    public static String getCommonFile(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                //当前所属平台
                .append("plat=Android")
                //app版本
                .append("`build_v=" + AppConfig.VERSION_CODE)
                //app编译版本
                .append("`project_v=1.4.5")
                //型号
                .append("`devi").append(MPDeviceUtils.MODEL)
                //playFun版本号
                .append("`devi="+AppConfig.SDK_VERSION_NAME_PUSH)
                //手机名称	和plat相似，iOS使用Apple
                .append("`bd=").append(MPDeviceUtils.BRAND)
                //当前app的渠道id
                .append("`appId="+AppConfig.APPID)
                //当前属于手机语言
                .append("`la="+ Locale.getDefault().getLanguage())
                //如果是强制使用语言包，可以添加
                .append("`la_v="+ LocaleManager.getSystemLocale(Utils.getApp()))
                //是测试服还是正式服，“test”/"master"
                .append("`envir="+(AppConfig.isDebug ? "test" : "master"))
                //当前移动端所属的网络状况，例如wifi或者wwan
                .append("`nt=")
                //当前移动端所属的ip地址
                .append("`ip=");
        return stringBuilder.toString();
    }

    public static String getMediaSource(){

    }

    /**
    * @Desc TODO(讲空指针转成空字符串)
    * @author 彭石林
    * @parame [obj]
    * @return java.lang.String
    * @Date 2022/10/10
    */
    String isNullConverterSky(Object obj){
        if(ObjectUtils.isEmpty(obj)){
            return "";
        }
        return String.valueOf(obj);
    }
}
