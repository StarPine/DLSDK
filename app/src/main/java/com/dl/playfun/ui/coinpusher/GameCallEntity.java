package com.dl.playfun.ui.coinpusher;

import com.dl.rtc.calling.base.DLRTCCalling;
import com.dl.rtc.calling.model.DLRTCDataMessageType;

import java.io.Serializable;

/**
 * Author: 彭石林
 * Time: 2022/11/10 22:11
 * Description: 游戏中打电话处理
 */
public class GameCallEntity implements Serializable {
    //用户名称
    private String nickname;
    //用户头像
    private String avatar;
    //房间ID
    private int roomId;
    //拨打方UserId
    private String inviteUserId;
    //接听方
    private String acceptUserId;
    //电话类型： 音频 or 视频
    private DLRTCDataMessageType.DLInviteRTCType callingType;
    //主动拨打 or 被动接听
    private DLRTCCalling.Role callingRole;

    private boolean isCalling;

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

    public String getInviteUserId() {
        return inviteUserId;
    }

    public void setInviteUserId(String inviteUserId) {
        this.inviteUserId = inviteUserId;
    }

    public String getAcceptUserId() {
        return acceptUserId;
    }

    public void setAcceptUserId(String acceptUserId) {
        this.acceptUserId = acceptUserId;
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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
