package com.dl.playfun.app;

import com.blankj.utilcode.util.ObjectUtils;
import com.dl.lib.elk.StatisticsAnalysis;
import com.dl.playfun.utils.ElkLogEventUtils;

import java.math.BigDecimal;

/**
 * Author: 彭石林
 * Time: 2022/10/11 10:25
 * Description: This is ElkLogEventReport
 */
public class ElkLogEventReport {

    /**
     * 点击事件
     *
     * @param lt   所属模块
     * @param et   当前页面
     * @param ct   expose: 曝光 click：点击
     * @param dt   任意拓展
     */
    public static String commonClickString(String lt, String et, String ct, String dt){
        return getCommentFiled()+"`lt="+ ElkLogEventUtils.isNullConverterSky(lt)+"`et="+ElkLogEventUtils.isNullConverterSky(et)+"`ct="+ElkLogEventUtils.isNullConverterSky(ct)+"`dt="+ElkLogEventUtils.isNullConverterSky(dt);
    }

    /**
    * @Desc TODO(获取当前公共参数)
    * @author 彭石林
    * @parame []
    * @return java.lang.String
    * @Date 2022/10/11
    */
    private static String getCommentFiled(){
        return ElkLogEventUtils.getCommonFile()+ElkLogEventUtils.getUserDataEvent()+ElkLogEventUtils.getMediaSource();
    }

    public static class reportMediaGallery{
        //红包照片模块打点
        static final String lt= "redPackage";

        static String isVideoStr(boolean isVideo){
            return isVideo ? "video" : "image";
        }

        /**
        * @Desc TODO(查看资源的时候，添加这个打点，为了统计有多少人选择付费资源)
        * @author 彭石林
        * @parame [dt=image/video判断是视频还是图片, unlockPrice =当前付费资源解锁价格, stateSnapshot=是否是快照, statePhotoPay=是否是付费照片, msgKey=上报服务器返回的msgkey, stateVideoPay=是否是付费视频, isUnLocked=是否已读]
        * @Date 2022/10/11
        */
        public static void reportReadMediaGallery(boolean isVideo, String unlockPrice, String stateSnapshot, String msgKey, String statePay,String isUnLocked){
            String doSendStatistics = commonClickString(lt,"sourceCheck","click",isVideoStr(isVideo)) +
                     "`unlockPrice="+isNullConverterSky(unlockPrice)
                     +"`stateSnapshot="+isNullConverterSky(stateSnapshot)
                    +"`msgKey="+isNullConverterSky(msgKey)
                    +"`isUnLocked="+isNullConverterSky(isUnLocked);
            if (!isVideo){
                doSendStatistics += "`statePhotoPay="+statePay;
            }else{
                doSendStatistics += "`stateVideoPay="+statePay;
            }
            StatisticsAnalysis.doSendStatistics(doSendStatistics);
        }

        /**
        * @Desc TODO(评价当前资源的时候 ， 添加这个打点)
        * @author 彭石林
        * @parame [dt=image/video判断是视频还是图片, toUserId=评价谁, evaluteState=好评还是差评, msgKey=上报服务器返回的msgkey]
        * @Date 2022/10/11
        */
        public static void reportEvaluteMediaGallery(boolean isVideo, Object toUserId, String evaluteState, String msgKey){
            String doSendStatistics = commonClickString(lt,"evalute","click",isVideoStr(isVideo)) +
                    "`toUserId="+isNullConverterSky(toUserId)
                    +"`evaluteState="+isNullConverterSky(evaluteState)
                    +"`msgKey="+isNullConverterSky(msgKey);
            StatisticsAnalysis.doSendStatistics(doSendStatistics);
        }

        /**
        * @Desc TODO(发送视频和图片资源的时候，添加这个打点统计)
        * @author 彭石林
        * @parame [dt=image/video判断是视频还是图片, toUserId=发送给谁, stateSnapshot=是否是快照, statePhotoPay=是否是付费照片, unlockPrice=当前付费资源解锁价格, stateVideoPay=是否是付费视频, configId, configIndexString]
        * @Date 2022/10/11
        */
        public static void reportSendMediaGallery(boolean isVideo, Object toUserId, String stateSnapshot, boolean statePay, BigDecimal unlockPrice, Integer configId, String configIndexString){
            String doSendStatistics = commonClickString(lt,"sourceSend","click",isVideoStr(isVideo)) +
                    "`unlockPrice="+isNullConverterSky(unlockPrice)
                    +"`stateSnapshot="+isNullConverterSky(stateSnapshot)
                    +"`toUserId="+isNullConverterSky(toUserId)
                    +"`configId="+isNullConverterSky(configId)
                    +"`configIndexString="+isNullConverterSky(configIndexString);
            if(!isVideo){
                doSendStatistics += "`statePhotoPay="+isNullConverterSky(statePay);
            }else{
                doSendStatistics += "`stateVideoPay="+isNullConverterSky(statePay);
            }
            StatisticsAnalysis.doSendStatistics(doSendStatistics);
        }
    }

    /**
     * @Desc TODO(空指针转成空字符串)
     * @author 彭石林
     * @parame [obj]
     * @return java.lang.String
     * @Date 2022/10/10
     */
    public static String isNullConverterSky(Object obj){
        if(ObjectUtils.isEmpty(obj)){
            return "";
        }
        return String.valueOf(obj);
    }
}
