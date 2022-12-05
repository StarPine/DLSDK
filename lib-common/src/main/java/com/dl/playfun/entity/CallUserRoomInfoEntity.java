package com.dl.playfun.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Author: 彭石林
 * Time: 2022/11/24 10:40
 * Description: 新的通话接口
 */
public class CallUserRoomInfoEntity implements Serializable {
    //房间ID
    private int roomId;
    //字符串形式的房间ID
    private String roomIdStr;
    //付费发的imId
    private String payerImId;
    //扣费单价
    private BigDecimal coinPerMinutes;
    //收益
    private BigDecimal profitPerMinutes;
    //心跳间隔秒数
    private int heartBeatInterval;
    //拨打超时时间
    private int inviteTimeout;
    private CustomShowMsg customShowMsg;

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomIdStr() {
        return roomIdStr;
    }

    public void setRoomIdStr(String roomIdStr) {
        this.roomIdStr = roomIdStr;
    }

    public String getPayerImId() {
        return payerImId;
    }

    public void setPayerImId(String payerImId) {
        this.payerImId = payerImId;
    }

    public BigDecimal getCoinPerMinutes() {
        return coinPerMinutes;
    }

    public void setCoinPerMinutes(BigDecimal coinPerMinutes) {
        this.coinPerMinutes = coinPerMinutes;
    }

    public BigDecimal getProfitPerMinutes() {
        return profitPerMinutes;
    }

    public void setProfitPerMinutes(BigDecimal profitPerMinutes) {
        this.profitPerMinutes = profitPerMinutes;
    }

    public int getHeartBeatInterval() {
        return heartBeatInterval;
    }

    public void setHeartBeatInterval(int heartBeatInterval) {
        this.heartBeatInterval = heartBeatInterval;
    }

    public int getInviteTimeout() {
        return inviteTimeout;
    }

    public void setInviteTimeout(int inviteTimeout) {
        this.inviteTimeout = inviteTimeout;
    }

    public CustomShowMsg getCustomShowMsg() {
        return customShowMsg;
    }

    public void setCustomShowMsg(CustomShowMsg customShowMsg) {
        this.customShowMsg = customShowMsg;
    }

    public static class CustomShowMsg implements Serializable{
        private String callingMsg;
        private String handUpMsg;
        private String callPlayMsg;
        private String giftAwardMsg;

        public String getCallingMsg() {
            return callingMsg;
        }

        public void setCallingMsg(String callingMsg) {
            this.callingMsg = callingMsg;
        }

        public String getHandUpMsg() {
            return handUpMsg;
        }

        public void setHandUpMsg(String handUpMsg) {
            this.handUpMsg = handUpMsg;
        }

        public String getCallPlayMsg() {
            return callPlayMsg;
        }

        public void setCallPlayMsg(String callPlayMsg) {
            this.callPlayMsg = callPlayMsg;
        }

        public String getGiftAwardMsg() {
            return giftAwardMsg;
        }

        public void setGiftAwardMsg(String giftAwardMsg) {
            this.giftAwardMsg = giftAwardMsg;
        }

        @Override
        public String toString() {
            return "CustomShowMsg{" +
                    "callingMsg='" + callingMsg + '\'' +
                    ", handUpMsg='" + handUpMsg + '\'' +
                    ", callPlayMsg='" + callPlayMsg + '\'' +
                    ", giftAwardMsg='" + giftAwardMsg + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "CallUserRoomInfoEntity{" +
                "roomId=" + roomId +
                ", roomIdStr='" + roomIdStr + '\'' +
                ", payerImId='" + payerImId + '\'' +
                ", coinPerMinutes=" + coinPerMinutes +
                ", profitPerMinutes=" + profitPerMinutes +
                ", heartBeatInterval=" + heartBeatInterval +
                ", customShowMsg=" + customShowMsg +
                '}';
    }
}
