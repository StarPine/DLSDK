package com.dl.lib.elk.log;

import android.content.Context;
import android.content.ContextWrapper;
import android.text.TextUtils;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.dl.lib.util.MPDeviceUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Author: 彭石林
 * Time: 2022/9/29 11:28
 * Description: This is AppLogPackHelper
 */
public class AppLogPackHelper {

    public static String getCommonParamString(Context context){
        if(context!=null){
            return ((ContextWrapper) context).getClass().getSimpleName();
        }
        return null;
    }
    //将map转换为字符串
    public static String getStatisticsString(Map<String,String> mapData){
        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<String, String> entry : mapData.entrySet()) {
            stringBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("`");
        }
        return stringBuilder.substring(0,stringBuilder.length()-1);
    }
    public static Map<String,String> getCommonEventMessage(String dt, String et, String ft){
        Map<String,String> mapData = new HashMap<>();
        putNotNull(mapData,"dt",dt);
        putNotNull(mapData,"et",et);
        putNotNull(mapData,"ct",ft);
        return mapData;
    }

    private static void putNotNull(Map<String,String> mapData, String key, String value){
        if(!TextUtils.isEmpty(value)){
            mapData.put(key, value);
        }
    }

    private static void putObjNotNull(Map<String,String> mapData, String key, Object value){
        if(!ObjectUtils.isEmpty(value)){
            mapData.put(key, GsonUtils.toJson(value));
        }
    }

    public interface AppLogPackListener{
        void callback();
    }
}
