package com.dl.playfun.event;

import com.dl.playfun.entity.CallGameCoinPusherEntity;

import java.io.Serializable;

/**
 * Author: 彭石林
 * Time: 2022/11/23 17:42
 * Description: 语音通话跳转推币机事件类
 */
public class CallingToGamePlayingEvent implements Serializable {
    private CallGameCoinPusherEntity callGameCoinPusherEntity;

    public CallGameCoinPusherEntity getCallGameCoinPusherEntity() {
        return callGameCoinPusherEntity;
    }

    public void setCallGameCoinPusherEntity(CallGameCoinPusherEntity callGameCoinPusherEntity) {
        this.callGameCoinPusherEntity = callGameCoinPusherEntity;
    }

    public CallingToGamePlayingEvent(CallGameCoinPusherEntity callGameCoinPusherEntity) {
        this.callGameCoinPusherEntity = callGameCoinPusherEntity;
    }
}
