package com.dl.playfun.event;

import com.dl.playfun.entity.CallingStatusEntity;

public class CallingStatusEvent {
    private CallingStatusEntity callingStatusEntity;

    public CallingStatusEntity getCallingStatusEntity() {
        return callingStatusEntity;
    }

    public void setCallingStatusEntity(CallingStatusEntity callingStatusEntity) {
        this.callingStatusEntity = callingStatusEntity;
    }

    public CallingStatusEvent(CallingStatusEntity callingStatusEntity) {
        this.callingStatusEntity = callingStatusEntity;
    }
}
