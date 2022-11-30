package com.dl.playfun.event;

import com.dl.playfun.entity.RtcRoomMessageEntity;

/**
 * RTC通话中推送消息模块事件
 */
public class RtcRoomMessageEvent {
    private RtcRoomMessageEntity rtcRoomMessageEntity;

    public RtcRoomMessageEntity getRtcRoomMessageEntity() {
        return rtcRoomMessageEntity;
    }

    public void setRtcRoomMessageEntity(RtcRoomMessageEntity rtcRoomMessageEntity) {
        this.rtcRoomMessageEntity = rtcRoomMessageEntity;
    }

    public RtcRoomMessageEvent(RtcRoomMessageEntity rtcRoomMessageEntity) {
        this.rtcRoomMessageEntity = rtcRoomMessageEntity;
    }

    @Override
    public String toString() {
        return "RtcRoomMessageEvent{" +
                "rtcRoomMessageEntity=" + rtcRoomMessageEntity +
                '}';
    }
}
