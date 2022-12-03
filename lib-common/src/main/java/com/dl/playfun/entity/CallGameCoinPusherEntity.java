package com.dl.playfun.entity;

import java.io.Serializable;

/**
 * Author: 彭石林
 * Time: 2022/11/22 22:51
 * Description: 活动入口推币机消息体
 */
public class CallGameCoinPusherEntity implements Serializable {
    //进房
    public final static String enterGame = "enterGame";
    //推房
    public final static String leaveGame = "leaveGame";

    //活动icon
    private String icon;
    //拓展字段。列入推送H5小游戏。打开网址
    private String webLink;
    ////游戏方ID -IM ID 只有此用户才能发起游戏
    private String playUserId;

    private ActivityData actData;


    public class ActivityData implements Serializable{

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
            return "activityData{" +
                    "state='" + state + '\'' +
                    ", circuses=" + circuses +
                    ", streamUrl='" + streamUrl + '\'' +
                    ", clientWsRtcId='" + clientWsRtcId + '\'' +
                    ", roomId=" + roomId +
                    ", totalGold=" + totalGold +
                    ", payGameMoney=" + payGameMoney +
                    ", levelId=" + levelId +
                    ", nickname='" + nickname + '\'' +
                    '}';
        }
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getWebLink() {
        return webLink;
    }

    public void setWebLink(String webLink) {
        this.webLink = webLink;
    }

    public String getPlayUserId() {
        return playUserId;
    }

    public void setPlayUserId(String playUserId) {
        this.playUserId = playUserId;
    }

    public ActivityData getActData() {
        return actData;
    }

    public void setActData(ActivityData actData) {
        this.actData = actData;
    }

    @Override
    public String toString() {
        return "CallGameCoinPusherEntity{" +
                "icon='" + icon + '\'' +
                ", webLink='" + webLink + '\'' +
                ", playUserId='" + playUserId + '\'' +
                ", actData=" + actData +
                '}';
    }
}
