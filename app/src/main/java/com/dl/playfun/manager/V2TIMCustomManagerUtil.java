package com.dl.playfun.manager;

import android.util.Log;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.entity.CallGameCoinPusherEntity;
import com.dl.playfun.entity.CallingStatusEntity;
import com.dl.playfun.entity.RtcRoomMessageEntity;
import com.dl.playfun.event.CallingStatusEvent;
import com.dl.playfun.event.CallingToGamePlayingEvent;
import com.dl.playfun.event.CoinPusherGamePlayingEvent;
import com.dl.playfun.event.RtcRoomMessageEvent;
import com.google.gson.reflect.TypeToken;
import com.tencent.custom.tmp.CustomDlTempMessage;
import com.tencent.qcloud.tuicore.custom.CustomConstants;
import com.tencent.qcloud.tuicore.custom.CustomConvertUtils;
import com.tencent.qcloud.tuicore.custom.entity.CustomBaseEntity;
import com.tencent.qcloud.tuicore.custom.entity.VideoEvaluationEntity;
import com.tencent.qcloud.tuicore.custom.entity.VideoPushEntity;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import me.goldze.mvvmhabit.bus.RxBus;

/**
 * Author: 彭石林
 * Time: 2022/9/7 17:57
 * Description: IM自定义消息体处理
 */
public class V2TIMCustomManagerUtil {
    private static final String TAG = "V2TIMCustomManagerUtil";

    /**
    * @Desc TODO(创建消息模板)
    * @author 彭石林
    * @parame [msgModuleName, customMsgType, customMsgBody]
    * @return com.tencent.custom.tmp.CustomDlTempMessage
    * @Date 2022/11/23
    */
    public static CustomDlTempMessage buildDlTempMessage(String msgModuleName,String customMsgType,Object customMsgBody){
        CustomDlTempMessage.MsgBodyInfo msgBodyInfo = new CustomDlTempMessage.MsgBodyInfo();
        msgBodyInfo.setCustomMsgType(customMsgType);
        msgBodyInfo.setCustomMsgBody(customMsgBody);
        CustomDlTempMessage.MsgModuleInfo msgModuleInfo = new CustomDlTempMessage.MsgModuleInfo();
        msgModuleInfo.setMsgModuleName(msgModuleName);
        msgModuleInfo.setContentBody(msgBodyInfo);
        CustomDlTempMessage customDlTempMessage = new CustomDlTempMessage();
        customDlTempMessage.setContentBody(msgModuleInfo);
        customDlTempMessage.setLanguage(StringUtils.getString(R.string.playfun_local_language));
        return customDlTempMessage;
    }


    /**
    * @Desc TODO(推币机模块处理)
    * @author 彭石林
    * @parame [contentBody]
    * @return void
    * @Date 2022/9/7
    */
    public static void CoinPusherManager(Map<String,Object> contentBody){
        //获取moudle-pushCoinGame 推币机
        if(CustomConvertUtils.ContainsMessageModuleKey(contentBody, CustomConstants.Message.MODULE_NAME_KEY,CustomConstants.CoinPusher.MODULE_NAME)){
            Map<String,Object> pushCoinGame = CustomConvertUtils.ConvertMassageModule(contentBody,CustomConstants.Message.MODULE_NAME_KEY,CustomConstants.CoinPusher.MODULE_NAME,CustomConstants.Message.CUSTOM_CONTENT_BODY);
            if(ObjectUtils.isNotEmpty(pushCoinGame)){
                //消息类型--判断
                if(pushCoinGame.containsKey(CustomConstants.Message.CUSTOM_MSG_KEY)){
                    if (CustomConvertUtils.ContainsMessageModuleKey(pushCoinGame,CustomConstants.Message.CUSTOM_MSG_KEY,CustomConstants.CoinPusher.LITTLE_GAME_WINNING)){
                        //中奖 小游戏（叠叠乐、小玛利）
                        RxBus.getDefault().post(new CoinPusherGamePlayingEvent(CustomConstants.CoinPusher.LITTLE_GAME_WINNING));
                    }else if(CustomConvertUtils.ContainsMessageModuleKey(pushCoinGame,CustomConstants.Message.CUSTOM_MSG_KEY,CustomConstants.CoinPusher.START_WINNING)){
                        //开始游戏
                        RxBus.getDefault().post(new CoinPusherGamePlayingEvent(CustomConstants.CoinPusher.START_WINNING));
                    }else if (CustomConvertUtils.ContainsMessageModuleKey(pushCoinGame,CustomConstants.Message.CUSTOM_MSG_KEY,CustomConstants.CoinPusher.END_WINNING)){
                        //落币结束
                        RxBus.getDefault().post(new CoinPusherGamePlayingEvent(CustomConstants.CoinPusher.END_WINNING));
                    }else if(CustomConvertUtils.ContainsMessageModuleKey(pushCoinGame,CustomConstants.Message.CUSTOM_MSG_KEY,CustomConstants.CoinPusher.DROP_COINS)){
                        //落币奖励
                        Map<String,Object> startWinning = CustomConvertUtils.ConvertMassageModule(pushCoinGame,CustomConstants.Message.CUSTOM_MSG_KEY,CustomConstants.CoinPusher.DROP_COINS,CustomConstants.Message.CUSTOM_MSG_BODY);
                        if(ObjectUtils.isNotEmpty(startWinning)){
                            BigDecimal goldNumberDecimal = new BigDecimal(String.valueOf(ObjectUtils.getOrDefault(startWinning.get("goldNumber"),0)));
                            BigDecimal totalGoldDecimal = new BigDecimal(String.valueOf(ObjectUtils.getOrDefault(startWinning.get("totalGold"),0)));
                            RxBus.getDefault().post(new CoinPusherGamePlayingEvent(CustomConstants.CoinPusher.DROP_COINS,goldNumberDecimal.intValue(),totalGoldDecimal.intValue()));
                        }
                    }else if(CustomConvertUtils.ContainsMessageModuleKey(pushCoinGame,CustomConstants.Message.CUSTOM_MSG_KEY,CustomConstants.CoinPusher.CALL_GO_GAME_WINNING)){
                        //通话中转到游戏
                        Map<String,Object> callGoGame = CustomConvertUtils.ConvertMassageModule(pushCoinGame,CustomConstants.Message.CUSTOM_MSG_KEY,CustomConstants.CoinPusher.CALL_GO_GAME_WINNING,CustomConstants.Message.CUSTOM_MSG_BODY);
                        if(ObjectUtils.isNotEmpty(callGoGame)){
                            CallGameCoinPusherEntity callGameCoinPusherEntity = new CallGameCoinPusherEntity();
                            callGameCoinPusherEntity.setState(String.valueOf(callGoGame.get("state")));
                            callGameCoinPusherEntity.setCircuses(Boolean.getBoolean(String.valueOf(callGoGame.get("circuses"))));
                            callGameCoinPusherEntity.setClientWsRtcId(String.valueOf(callGoGame.get("clientWsRtcId")));
                            callGameCoinPusherEntity.setStreamUrl(String.valueOf(callGoGame.get("streamUrl")));
                            callGameCoinPusherEntity.setTotalGold(20);
                            BigDecimal levelId = new BigDecimal(String.valueOf(ObjectUtils.getOrDefault(callGoGame.get("levelId"),0)));
                            callGameCoinPusherEntity.setLevelId(levelId.intValue());
                            callGameCoinPusherEntity.setNickname(String.valueOf(callGoGame.get("nickname")));
                            BigDecimal payGameMoney = new BigDecimal(String.valueOf(ObjectUtils.getOrDefault(callGoGame.get("payGameMoney"),0)));
                            callGameCoinPusherEntity.setPayGameMoney(payGameMoney.intValue());
                            BigDecimal roomId = new BigDecimal(String.valueOf(ObjectUtils.getOrDefault(callGoGame.get("roomId"),0)));
                            callGameCoinPusherEntity.setRoomId(roomId.intValue());
                            RxBus.getDefault().post(new CallingToGamePlayingEvent(callGameCoinPusherEntity));
                        }
                    }
                }
            }
        }
    }

    /**
     * RTC通话中推送消息模块
     */
    public static void RtcRoomMessageManager(Map<String,Object> contentBody){
        if(ObjectUtils.isNotEmpty(contentBody)){
            //消息类型--判断
            if(contentBody.containsKey(CustomConstants.Message.CUSTOM_MSG_KEY)){
                //活动入口
                if (CustomConvertUtils.ContainsMessageModuleKey(contentBody,CustomConstants.Message.CUSTOM_MSG_KEY,CustomConstants.RtcRoomMessage.TYPE_ACTIVITY_ENTRANCE)){
                    // 活动入口
                    Map<String,Object> startWinning = CustomConvertUtils.ConvertMassageModule(contentBody,CustomConstants.Message.CUSTOM_MSG_KEY,CustomConstants.RtcRoomMessage.TYPE_ACTIVITY_ENTRANCE,CustomConstants.Message.CUSTOM_MSG_BODY);
                    if(ObjectUtils.isNotEmpty(startWinning)){
                        RtcRoomMessageEntity rtcRoomMessageEntity = GsonUtils.fromJson(GsonUtils.toJson(startWinning),RtcRoomMessageEntity.class);
                        if(rtcRoomMessageEntity != null){
                            //推币机活动入口信息
                            if(Objects.equals(rtcRoomMessageEntity.getActivityType(),RtcRoomMessageEntity.coinPusherGame)){
                                RxBus.getDefault().post(new RtcRoomMessageEvent(rtcRoomMessageEntity));
                            }
                        }
                    }
                }else if(CustomConvertUtils.ContainsMessageModuleKey(contentBody,CustomConstants.Message.CUSTOM_MSG_KEY,CustomConstants.RtcRoomMessage.TYPE_CALLING_PROFIT_TIPS)){
                    // //通话中收益
                    Map<String,Object> startWinning = CustomConvertUtils.ConvertMassageModule(contentBody,CustomConstants.Message.CUSTOM_MSG_KEY,CustomConstants.RtcRoomMessage.TYPE_CALLING_PROFIT_TIPS,CustomConstants.Message.CUSTOM_MSG_BODY);
                    if(ObjectUtils.isNotEmpty(startWinning)){
                        CallingStatusEntity callingStatusEntity = GsonUtils.fromJson(GsonUtils.toJson(startWinning),CallingStatusEntity.class);
                        if(callingStatusEntity!=null){
                            RxBus.getDefault().post(new CallingStatusEvent(callingStatusEntity));
                        }
                    }
                }
            }
        }
    }

    public static VideoPushEntity videoPushManager(String customData){
        try {
            Type videoPushType = new TypeToken<CustomBaseEntity<VideoPushEntity>>() {}.getType();
            return (VideoPushEntity) CustomConvertUtils.customMassageAnalyzeModule(customData,
                            CustomConstants.PushMessage.MODULE_NAME,
                            CustomConstants.PushMessage.VIDEO_CALL_PUSH,
                            videoPushType);
        }catch (Exception e){
            Log.i("V2TIMCustomManagerUtil", "自定数据解析异常");
        }
        return null;
    }

    public static VideoEvaluationEntity videoEvaluationManager(String customData){
        try {
            Type videoEvaluationType = new TypeToken<CustomBaseEntity<VideoEvaluationEntity>>() {}.getType();
            return  (VideoEvaluationEntity) CustomConvertUtils.customMassageAnalyzeModule(customData,
                            CustomConstants.PushMessage.MODULE_NAME,
                            CustomConstants.PushMessage.VIDEO_CALL_FEEDBACK,
                            videoEvaluationType);
        }catch (Exception e){
            Log.i("V2TIMCustomManagerUtil", "自定数据解析异常");
        }
        return null;
    }


}
