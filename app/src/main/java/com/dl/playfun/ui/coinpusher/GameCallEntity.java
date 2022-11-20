package com.dl.playfun.ui.coinpusher;

import com.dl.rtc.calling.base.DLRTCCalling;
import com.dl.rtc.calling.model.DLRTCDataMessageType;

/**
 * Author: 彭石林
 * Time: 2022/11/10 22:11
 * Description: 游戏中打电话处理
 */
public class GameCallEntity {
    private String userId;
    private String avatar;
    private int roomId;
    private String fromUserId;
    private String toUserId;
    private DLRTCDataMessageType.DLInviteRTCType callingType;
    private DLRTCCalling.Role callingRole;

    private boolean isCalling;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public DLRTCDataMessageType.DLInviteRTCType getCallingType() {
        return callingType;
    }

    public void setCallingType(DLRTCDataMessageType.DLInviteRTCType callingType) {
        this.callingType = callingType;
    }

    public DLRTCCalling.Role getCallingRole() {
        return callingRole;
    }

    public void setCallingRole(DLRTCCalling.Role callingRole) {
        this.callingRole = callingRole;
    }

    public boolean isCalling() {
        return isCalling;
    }

    public void setCalling(boolean calling) {
        isCalling = calling;
    }
}
