package com.dl.rtc.calling.util;

import com.blankj.utilcode.util.GsonUtils;
import com.dl.lib.util.log.MPTimber;
import com.dl.rtc.calling.model.bean.DLRTCCallModel;
import com.dl.rtc.calling.model.bean.DLRTCSignallingData;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Author: 彭石林
 * Time: 2022/11/7 17:54
 * Description: This is DLRTConversionUtil
 */
public class DLRTConversionUtil {
    
    private static final String TAG_LOG = "DLRTConversionUtil";
    
    public static DLRTCSignallingData convert2CallingData(String data) {
        DLRTCSignallingData signallingData = new DLRTCSignallingData();
        Map<String, Object> extraMap;
        try {
            extraMap = GsonUtils.fromJson(data, Map.class);
            if (extraMap == null) {
                MPTimber.tag(TAG_LOG).e( "onReceiveNewInvitation extraMap is null, ignore");
                return signallingData;
            }
            MPTimber.tag(TAG_LOG).e("当前信令模型解成MAP："+extraMap);
            Object version = extraMap.get("version");
            if (version != null) {
                signallingData.setVersion(new BigDecimal(String.valueOf(version)).intValue());
            } else {
                MPTimber.tag(TAG_LOG).e( "version is not Double, value is :" + version);
            }

            Object platform = extraMap.get("platform");
            if (platform != null ) {
                signallingData.setPlatform(String.valueOf(platform));
            } else {
                MPTimber.tag(TAG_LOG).e( "platform is not string, value is :" + platform);
            }

            Object businessId = extraMap.get("businessID");
            if (businessId != null) {
                signallingData.setBusinessID(String.valueOf(businessId));
            } else {
                MPTimber.tag(TAG_LOG).e( "businessId is not string, value is :" + businessId);
            }

            Object callType = extraMap.get("call_type");
            //兼容老版本某些字段
            if (callType != null) {
                signallingData.setCallType(new BigDecimal(String.valueOf(callType)).intValue());
            } else {
                MPTimber.tag(TAG_LOG).e( "callType is not Double, value is :" + callType);
            }

            Object roomId = extraMap.get("room_id");
            if (roomId != null) {
                signallingData.setRoomId(new BigDecimal(String.valueOf(roomId)).intValue());
            } else {
                MPTimber.tag(TAG_LOG).e( "roomId is not Double, value is :" + roomId);
            }

            Object lineBusy = extraMap.get("line_busy");
            if (lineBusy != null ) {
                signallingData.setLineBusy(String.valueOf(lineBusy));
            } else {
                MPTimber.tag(TAG_LOG).e( "lineBusy is not string, value is :" + lineBusy);
            }

            Object callEnd = extraMap.get("call_end");
            if (callEnd != null ) {
                signallingData.setCallEnd(new BigDecimal(String.valueOf(callEnd)).intValue());
            } else {
                MPTimber.tag(TAG_LOG).e( "callEnd is not Double, value is :" + callEnd);
            }

            Object switchToAudioCall = extraMap.get("switch_to_audio_call");
            if (switchToAudioCall != null) {
                signallingData.setSwitchToAudioCall(String.valueOf(switchToAudioCall));
            } else {
                MPTimber.tag(TAG_LOG).e( "switchToAudioCall is not string, value is :" + switchToAudioCall);
            }

            Object dataMapObj = extraMap.get("data");
            MPTimber.tag(TAG_LOG).e((dataMapObj instanceof String) +" ==="+(dataMapObj instanceof Map));
            if (dataMapObj != null && dataMapObj instanceof Map) {
                Map<String, Object> dataMap = (Map<String, Object>) dataMapObj;
                DLRTCSignallingData.DataInfo dataInfo = convert2DataInfo(dataMap);
                signallingData.setBusinessID(dataInfo.getBusinessID());
                signallingData.setData(dataInfo);
            } else {
                MPTimber.tag(TAG_LOG).e( "dataMapObj is not map, value is :" + dataMapObj);
                if(dataMapObj != null && dataMapObj instanceof String){
                    MPTimber.tag(TAG_LOG).e( "dataMapObj is String, value is :" + dataMapObj);
                    Map<String, Object> dataMap = GsonUtils.fromJson(String.valueOf(dataMapObj),Map.class);
                    MPTimber.tag(TAG_LOG).e( "dataMapObj  is :" + String.valueOf(dataMapObj));
                    DLRTCSignallingData.DataInfo dataInfo = convert2DataInfo(dataMap);
                    signallingData.setBusinessID(dataInfo.getBusinessID());
                    signallingData.setData(dataInfo);
                }
            }

            Object callAction = extraMap.get("call_action");
            if (callAction != null) {
                signallingData.setCallAction(new BigDecimal(String.valueOf(callAction)).intValue());
            } else {
                MPTimber.tag(TAG_LOG).e( "callAciton is not Double, value is :" + callAction);
            }

            Object callId = extraMap.get("callid");
            if (callId != null) {
                signallingData.setCallId(String.valueOf(callId));
            } else {
                MPTimber.tag(TAG_LOG).e( "callId is not String, value is :" + callId);
            }

            Object user = extraMap.get("user");
            if (user !=null ) {
                signallingData.setUser(String.valueOf(user));
            } else {
                MPTimber.tag(TAG_LOG).e( "user is not String, value is :" + user);
            }

        } catch (JsonSyntaxException e) {
            MPTimber.tag(TAG_LOG).e( "convert2CallingDataBean json parse error");
        }
        MPTimber.tag(TAG_LOG).e("当前模型解析完成："+signallingData.toString());
        return signallingData;
    }

    public static DLRTCSignallingData.DataInfo convert2DataInfo(Map<String, Object> dataMap) {
        DLRTCSignallingData.DataInfo dataInfo = new DLRTCSignallingData.DataInfo();
        try {
                Object cmd = dataMap.get(DLRTCCallModel.Companion.getKEY_CMD());
                if (cmd != null) {
                    dataInfo.setCmd(String.valueOf(cmd));
                } else {
                    MPTimber.tag(TAG_LOG).e( "cmd is not string, value is :" + cmd);
                }
                Object userIDs = dataMap.get(DLRTCCallModel.Companion.getKEY_USERIDS());
                if (userIDs != null && userIDs instanceof List) {
                    dataInfo.setUserIDs((List<String>) userIDs);
                } else {
                    MPTimber.tag(TAG_LOG).e( "userIDs is not List, value is :" + userIDs);
                }
                Object roomId = dataMap.get(DLRTCCallModel.Companion.getKEY_ROOM_ID());
                if (roomId != null) {
                    dataInfo.setRoomID(new BigDecimal(String.valueOf(roomId)).intValue());
                } else {
                    MPTimber.tag(TAG_LOG).e( "roomId is not Double, value is :" + roomId);
                }
                Object message = dataMap.get(DLRTCCallModel.Companion.getKEY_MESSAGE());
                if (message != null) {
                    dataInfo.setMessage(String.valueOf(message));
                } else {
                    MPTimber.tag(TAG_LOG).e( "message is not string, value is :" + message);
                }
        } catch (JsonSyntaxException e) {
            MPTimber.tag(TAG_LOG).e( "onReceiveNewInvitation JsonSyntaxException:" + e);
        }
        return dataInfo;
    }
}
