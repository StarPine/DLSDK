package com.dl.playfun.entity;

import java.util.List;

/**
 * Author: 彭石林
 * Time: 2022/8/24 10:47
 * Description: 推币机房间列表
 */
public class CoinPusherRoomInfoEntity {
    //金币余额
    private int totalGold;
    //设备级别列表
    private List<DeviceTag> typeArr;
    //设备列表
    private List<DeviceInfo> list;

    public int getTotalGold() {
        return totalGold;
    }

    public void setTotalGold(int totalGold) {
        this.totalGold = totalGold;
    }

    public List<DeviceTag> getTypeArr() {
        return typeArr;
    }

    public void setTypeArr(List<DeviceTag> typeArr) {
        this.typeArr = typeArr;
    }

    public List<DeviceInfo> getList() {
        return list;
    }

    public void setList(List<DeviceInfo> list) {
        this.list = list;
    }

    public static class DeviceTag{
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private static class DeviceInfo{
        private int id;
        //房间名
        private String nickname;
        //图标
        private String icon;
        //房间ID
        private int roomId;
        //所需金币
        private int money;
        //等级
        private int levelId;
        //房间状态 0:空闲 1:热玩中
        private int status;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public int getRoomId() {
            return roomId;
        }

        public void setRoomId(int roomId) {
            this.roomId = roomId;
        }

        public int getMoney() {
            return money;
        }

        public void setMoney(int money) {
            this.money = money;
        }

        public int getLevelId() {
            return levelId;
        }

        public void setLevelId(int levelId) {
            this.levelId = levelId;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
}
