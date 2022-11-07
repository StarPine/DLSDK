package com.dl.rtc.calling.util;

import com.dl.lib.util.log.MPTimber;
import com.dl.rtc.calling.model.bean.DLRTCCallModel;
import com.dl.rtc.calling.model.bean.DLRTCSignallingData;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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
            extraMap = new Gson().fromJson(data, Map.class);
            if (extraMap == null) {
                MPTimber.tag(TAG_LOG).e( "onReceiveNewInvitation extraMap is null, ignore");
                return signallingData;
            }
            if (extraMap.containsKey(DLRTCCallModel.Companion.getKEY_VERSION())) {
                Object version = extraMap.get(DLRTCCallModel.Companion.getKEY_VERSION());
                if (version instanceof Double) {
                    signallingData.setVersion(((Double) version).intValue());
                } else {
                    MPTimber.tag(TAG_LOG).e( "version is not Double, value is :" + version);
                }
            }

            if (extraMap.containsKey(DLRTCCallModel.Companion.getKEY_PLATFORM())) {
                Object platform = extraMap.get(DLRTCCallModel.Companion.getKEY_PLATFORM());
                if (platform instanceof String) {
                    signallingData.setPlatform((String) platform);
                } else {
                    MPTimber.tag(TAG_LOG).e( "platform is not string, value is :" + platform);
                }
            }

            if (extraMap.containsKey(DLRTCCallModel.Companion.getKEY_BUSINESS_ID())) {
                Object businessId = extraMap.get(DLRTCCallModel.Companion.getKEY_BUSINESS_ID());
                if (businessId instanceof String) {
                    signallingData.setBusinessID((String) businessId);
                } else {
                    MPTimber.tag(TAG_LOG).e( "businessId is not string, value is :" + businessId);
                }
            }

            //兼容老版本某些字段
            if (extraMap.containsKey(DLRTCCallModel.Companion.getSIGNALING_EXTRA_KEY_CALL_TYPE())) {
                Object callType = extraMap.get(DLRTCCallModel.Companion.getSIGNALING_EXTRA_KEY_CALL_TYPE());
                if (callType instanceof Double) {
                    signallingData.setCallType(((Double) callType).intValue());
                } else {
                    MPTimber.tag(TAG_LOG).e( "callType is not Double, value is :" + callType);
                }
            }

            if (extraMap.containsKey(DLRTCCallModel.Companion.getSIGNALING_EXTRA_KEY_ROOM_ID())) {
                Object roomId = extraMap.get(DLRTCCallModel.Companion.getSIGNALING_EXTRA_KEY_ROOM_ID());
                if (roomId instanceof Double) {
                    signallingData.setRoomId(((Double) roomId).intValue());
                } else {
                    MPTimber.tag(TAG_LOG).e( "roomId is not Double, value is :" + roomId);
                }
            }

            if (extraMap.containsKey(DLRTCCallModel.Companion.getSIGNALING_EXTRA_KEY_LINE_BUSY())) {
                Object lineBusy = extraMap.get(DLRTCCallModel.Companion.getSIGNALING_EXTRA_KEY_LINE_BUSY());
                if (lineBusy instanceof String) {
                    signallingData.setLineBusy((String) lineBusy);
                } else {
                    MPTimber.tag(TAG_LOG).e( "lineBusy is not string, value is :" + lineBusy);
                }
            }

            if (extraMap.containsKey(DLRTCCallModel.Companion.getSIGNALING_EXTRA_KEY_CALL_END())) {
                Object callEnd = extraMap.get(DLRTCCallModel.Companion.getSIGNALING_EXTRA_KEY_CALL_END());
                if (callEnd instanceof Double) {
                    signallingData.setCallEnd(((Double) callEnd).intValue());
                } else {
                    MPTimber.tag(TAG_LOG).e( "callEnd is not Double, value is :" + callEnd);
                }
            }

            if (extraMap.containsKey(DLRTCCallModel.Companion.getSIGNALING_EXTRA_KEY_SWITCH_AUDIO_CALL())) {
                Object switchToAudioCall = extraMap.get(DLRTCCallModel.Companion.getSIGNALING_EXTRA_KEY_SWITCH_AUDIO_CALL());
                if (switchToAudioCall instanceof String) {
                    signallingData.setSwitchToAudioCall((String) switchToAudioCall);
                } else {
                    MPTimber.tag(TAG_LOG).e( "switchToAudioCall is not string, value is :" + switchToAudioCall);
                }
            }

            if (extraMap.containsKey(DLRTCCallModel.Companion.getKEY_DATA())) {
                Object dataMapObj = extraMap.get(DLRTCCallModel.Companion.getKEY_DATA());
                MPTimber.tag(TAG_LOG).e((dataMapObj instanceof String) +" ==="+(dataMapObj instanceof Map));
                if (dataMapObj != null && dataMapObj instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) dataMapObj;
                    DLRTCSignallingData.DataInfo dataInfo = convert2DataInfo(dataMap);
                    signallingData.setData(dataInfo);
                } else {
                    MPTimber.tag(TAG_LOG).e( "dataMapObj is not map, value is :" + dataMapObj);
                }
            }

            if (extraMap.containsKey(DLRTCCallModel.Companion.getKEY_CALLACTION())) {
                Object callAction = extraMap.get(DLRTCCallModel.Companion.getKEY_CALLACTION());
                if (callAction instanceof Double) {
                    signallingData.setCallAction(((Double) callAction).intValue());
                } else {
                    MPTimber.tag(TAG_LOG).e( "callAciton is not Double, value is :" + callAction);
                }
            }
            if (extraMap.containsKey(DLRTCCallModel.Companion.getKEY_CALLID())) {
                Object callId = extraMap.get(DLRTCCallModel.Companion.getKEY_CALLID());
                if (callId instanceof String) {
                    signallingData.setCallId((String) callId);
                } else {
                    MPTimber.tag(TAG_LOG).e( "callId is not String, value is :" + callId);
                }
            }
            if (extraMap.containsKey(DLRTCCallModel.Companion.getKEY_USER())) {
                Object user = extraMap.get(DLRTCCallModel.Companion.getKEY_USER());
                if (user instanceof String) {
                    signallingData.setUser((String) user);
                } else {
                    MPTimber.tag(TAG_LOG).e( "user is not String, value is :" + user);
                }
            }
        } catch (JsonSyntaxException e) {
            MPTimber.tag(TAG_LOG).e( "convert2CallingDataBean json parse error");
        }
        return signallingData;
    }

    public static DLRTCSignallingData.DataInfo convert2DataInfo(Map<String, Object> dataMap) {
        DLRTCSignallingData.DataInfo dataInfo = new DLRTCSignallingData.DataInfo();
        try {
            if (dataMap.containsKey(DLRTCCallModel.Companion.getKEY_CMD())) {
                Object cmd = dataMap.get(DLRTCCallModel.Companion.getKEY_CMD());
                if (cmd instanceof String) {
                    dataInfo.setCmd((String) cmd);
                } else {
                    MPTimber.tag(TAG_LOG).e( "cmd is not string, value is :" + cmd);
                }
            }
            if (dataMap.containsKey(DLRTCCallModel.Companion.getKEY_USERIDS())) {
                Object userIDs = dataMap.get(DLRTCCallModel.Companion.getKEY_USERIDS());
                if (userIDs instanceof List) {
                    dataInfo.setUserIDs((List<String>) userIDs);
                } else {
                    MPTimber.tag(TAG_LOG).e( "userIDs is not List, value is :" + userIDs);
                }
            }
            if (dataMap.containsKey(DLRTCCallModel.Companion.getKEY_ROOM_ID())) {
                Object roomId = dataMap.get(DLRTCCallModel.Companion.getKEY_ROOM_ID());
                if (roomId instanceof Double) {
                    dataInfo.setRoomID(((Double) roomId).intValue());
                } else {
                    MPTimber.tag(TAG_LOG).e( "roomId is not Double, value is :" + roomId);
                }
            }
            if (dataMap.containsKey(DLRTCCallModel.Companion.getKEY_MESSAGE())) {
                Object message = dataMap.get(DLRTCCallModel.Companion.getKEY_MESSAGE());
                if (message instanceof String) {
                    dataInfo.setMessage((String) message);
                } else {
                    MPTimber.tag(TAG_LOG).e( "message is not string, value is :" + message);
                }
            }
        } catch (JsonSyntaxException e) {
            MPTimber.tag(TAG_LOG).e( "onReceiveNewInvitation JsonSyntaxException:" + e);
        }
        return dataInfo;
    }
}
