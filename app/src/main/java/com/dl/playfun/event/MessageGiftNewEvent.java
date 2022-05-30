package com.dl.playfun.event;

import com.tencent.coustom.GiftEntity;

/**
 * Author: 彭石林
 * Time: 2022/3/12 14:48
 * Description: This is MessageGiftNewEvent
 */
public class MessageGiftNewEvent {
    private GiftEntity giftEntity;
    //IM消息Id 做防抖用
    private String msgId;
    //消息发送放ID
    private String fromUser;


    public MessageGiftNewEvent(GiftEntity giftEntity,String msgId) {
        this.giftEntity = giftEntity;
        this.msgId = msgId;
    }

    public MessageGiftNewEvent(GiftEntity giftEntity,String msgId,String fromUsers) {
        this.giftEntity = giftEntity;
        this.msgId = msgId;
        this.fromUser =  fromUsers;
    }

    public GiftEntity getGiftEntity() {
        return giftEntity;
    }

    public void setGiftEntity(GiftEntity giftEntity) {
        this.giftEntity = giftEntity;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }
}
