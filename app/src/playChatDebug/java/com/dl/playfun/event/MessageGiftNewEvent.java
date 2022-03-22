package com.dl.playfun.event;

import com.tencent.coustom.GiftEntity;

/**
 * Author: 彭石林
 * Time: 2022/3/12 14:48
 * Description: This is MessageGiftNewEvent
 */
public class MessageGiftNewEvent {
    private GiftEntity giftEntity;

    public MessageGiftNewEvent(GiftEntity giftEntity) {
        this.giftEntity = giftEntity;
    }

    public GiftEntity getGiftEntity() {
        return giftEntity;
    }

    public void setGiftEntity(GiftEntity giftEntity) {
        this.giftEntity = giftEntity;
    }
}
