package com.dl.playfun.entity;

import androidx.annotation.NonNull;

/**
 * Author: 彭石林
 * Time: 2022/11/22 22:51
 * Description: This is CallGameCoinPusherEntity
 */
public class CallGameCoinPusherEntity {
    //进房
    public final static String enterGame = "enterGame";
    //推房
    public final static String leaveGame = "leaveGame";
    //当前状态：enterGame 、leaveGame
    private String state;
    //当前是否围观方
    private boolean circuses;
    //拉流地址
    private String streamUrl;
    //拉流客户端ID
    private String clientWsRtcId;
    //房间ID
    private int roomId;

    private Integer totalGold;
    private Integer payGameMoney;
    private long outTime;
    private long countdown;
    private int levelId;
    private String nickname;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isCircuses() {
        return circuses;
    }

    public void setCircuses(boolean circuses) {
        this.circuses = circuses;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public String getClientWsRtcId() {
        return clientWsRtcId;
    }

    public void setClientWsRtcId(String clientWsRtcId) {
        this.clientWsRtcId = clientWsRtcId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public Integer getTotalGold() {
        return totalGold;
    }

    public void setTotalGold(Integer totalGold) {
        this.totalGold = totalGold;
    }

    public Integer getPayGameMoney() {
        return payGameMoney;
    }

    public void setPayGameMoney(Integer payGameMoney) {
        this.payGameMoney = payGameMoney;
    }

    public long getOutTime() {
        return outTime;
    }

    public void setOutTime(long outTime) {
        this.outTime = outTime;
    }

    public long getCountdown() {
        return countdown;
    }

    public void setCountdown(long countdown) {
        this.countdown = countdown;
    }

    public int getLevelId() {
        return levelId;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String toString() {
        return "CallGameCoinPusherEntity{" +
                "state='" + state + '\'' +
                ", circuses=" + circuses +
                ", streamUrl='" + streamUrl + '\'' +
                ", clientWsRtcId='" + clientWsRtcId + '\'' +
                ", roomId=" + roomId +
                ", totalGold=" + totalGold +
                ", payGameMoney=" + payGameMoney +
                ", outTime=" + outTime +
                ", countdown=" + countdown +
                ", levelId=" + levelId +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
