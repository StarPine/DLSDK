package com.dl.playfun.entity;

import com.google.gson.annotations.SerializedName;

/**
 * @ClassName TagEntity
 * @Description TODO
 * @Author 彭石林
 * @Date 2021/7/3 10:46
 * @Phone 16620350375
 * @email 15616314565@163.com
 * @Version 1.0
 **/
public class TagEntity {

    @SerializedName("this_is_gg")
    private Integer thisIsGg; //当前用户是否GG 0否 1是

    @SerializedName("to_is_gg")
    private Integer toIsGg; //	对方用户是否GG 0否 1是
    @SerializedName("to_is_invite")
    private Integer toIsInvite; //	对方用户是否填写邀请码 0否 1是
    @SerializedName("is_online")
    private Integer isOnline;
    @SerializedName("calling_status")
    private int callingStatus;
    @SerializedName("is_blacklist")
    private int isBlacklist;
    @SerializedName("blacklist_status")
    private int blacklistStatus;
    private int isChatPush;

    public int getIsChatPush() {
        return isChatPush;
    }

    public void setIsChatPush(int isChatPush) {
        this.isChatPush = isChatPush;
    }

    public int getIsBlacklist() {
        return isBlacklist;
    }

    public void setIsBlacklist(int isBlacklist) {
        this.isBlacklist = isBlacklist;
    }

    public int getBlacklistStatus() {
        return blacklistStatus;
    }

    public void setBlacklistStatus(int blacklistStatus) {
        this.blacklistStatus = blacklistStatus;
    }

    public Integer getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(Integer isOnline) {
        this.isOnline = isOnline;
    }

    public int getCallingStatus() {
        return callingStatus;
    }

    public void setCallingStatus(int callingStatus) {
        this.callingStatus = callingStatus;
    }

    public Integer getThisIsGg() {
        return thisIsGg;
    }

    public void setThisIsGg(Integer thisIsGg) {
        this.thisIsGg = thisIsGg;
    }

    public Integer getToIsGg() {
        return toIsGg;
    }

    public void setToIsGg(Integer toIsGg) {
        this.toIsGg = toIsGg;
    }

    public Integer getToIsInvite() {
        return toIsInvite;
    }

    public void setToIsInvite(Integer toIsInvite) {
        this.toIsInvite = toIsInvite;
    }
}