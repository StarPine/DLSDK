package com.dl.playfun.entity;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class RtcRoomMessageEntity implements Serializable {

    //推币机消息类型
    public static final String coinPusherGame = "coinPusherGame";


    //页面跳转事件。示列：coinPusherGame 跳转推币机
    private String activityType;
    private CallGameCoinPusherEntity activityData;

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public CallGameCoinPusherEntity getActivityData() {
        return activityData;
    }

    public void setActivityData(CallGameCoinPusherEntity activityData) {
        this.activityData = activityData;
    }

    @Override
    public String toString() {
        return "RtcRoomMessageEntity{" +
                "activityType='" + activityType + '\'' +
                ", activityData=" + activityData +
                '}';
    }
}
