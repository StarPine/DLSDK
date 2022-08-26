package com.dl.playfun.entity;

import java.util.List;

/**
 * Author: 彭石林
 * Time: 2022/8/23 14:54
 * Description: 推币机-兑换列表
 */
public class CoinPusherConverInfoEntity {
    //金币余额
    private int totalGold;
    //金币提示词
    private String goldTips;
    //钻石提示词
    private String diamondsTips;
    //金币对话钻石列表
    private List<DiamondsInfo> diamondsList;
    //宝盒列表
    private List<GoldCoinInfo> goldCoinList;


    public int getTotalGold() {
        return totalGold;
    }

    public void setTotalGold(int totalGold) {
        this.totalGold = totalGold;
    }

    public String getGoldTips() {
        return goldTips;
    }

    public void setGoldTips(String goldTips) {
        this.goldTips = goldTips;
    }

    public String getDiamondsTips() {
        return diamondsTips;
    }

    public void setDiamondsTips(String diamondsTips) {
        this.diamondsTips = diamondsTips;
    }

    public List<DiamondsInfo> getDiamondsList() {
        return diamondsList;
    }

    public void setDiamondsList(List<DiamondsInfo> diamondsList) {
        this.diamondsList = diamondsList;
    }

    public List<GoldCoinInfo> getGoldCoinList() {
        return goldCoinList;
    }

    public void setGoldCoinList(List<GoldCoinInfo> goldCoinList) {
        this.goldCoinList = goldCoinList;
    }

    public class GoldCoinInfo {
        //礼盒命
        private String name;
        //礼盒图片
        private String icon;
        //价格
        private int coin;
        private List<GoldCoinItem> item;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public int getCoin() {
            return coin;
        }

        public void setCoin(int coin) {
            this.coin = coin;
        }

        public List<GoldCoinItem> getItem() {
            return item;
        }

        public void setItem(List<GoldCoinItem> item) {
            this.item = item;
        }

        public class GoldCoinItem {
            //id
            private int id;
            //明细
            private String name;
            //明细图片
            private String icon;
            //明细内容
            private int value;
            //明细类型
            private int type;

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

            public String getIcon() {
                return icon;
            }

            public void setIcon(String icon) {
                this.icon = icon;
            }

            public int getValue() {
                return value;
            }

            public void setValue(int value) {
                this.value = value;
            }

            public int getType() {
                return type;
            }

            public void setType(int type) {
                this.type = type;
            }
        }
    }
    //金币对话钻石
    public class DiamondsInfo{
        private int id;
        private String name;
        private int value;

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

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    @Override
    public String toString() {
        return "CoinPusherConverInfoEntity{" +
                "totalGold=" + totalGold +
                ", goldTips=" + goldTips +
                ", diamondsTips=" + diamondsTips +
                ", diamondsList=" + diamondsList +
                ", goldCoinList=" + goldCoinList +
                '}';
    }
}
