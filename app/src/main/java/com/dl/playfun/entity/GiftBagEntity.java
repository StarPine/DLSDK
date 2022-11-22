package com.dl.playfun.entity;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Author: 彭石林
 * Time: 2021/12/7 15:57
 * Description: 礼物背包
 */
public class GiftBagEntity {
    @SerializedName("is_first")
    private Integer isFirst;
    @SerializedName("total_coin")
    private Integer totalCoin;
    private Double totalProfit;
    private List<DiamondGiftEntity> gift;
    private List<propEntity> prop;
    @SerializedName("crystalGift")
    private List<CrystalGiftEntity> crystal;

    public Integer getIsFirst() {
        return isFirst;
    }

    public void setIsFirst(Integer isFirst) {
        this.isFirst = isFirst;
    }

    public Double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(Double totalProfit) {
        this.totalProfit = totalProfit;
    }

    public Integer getTotalCoin() {
        return totalCoin;
    }

    public void setTotalCoin(Integer totalCoin) {
        this.totalCoin = totalCoin;
    }

    public List<DiamondGiftEntity> getGift() {
        return gift;
    }

    public void setGift(List<DiamondGiftEntity> gift) {
        this.gift = gift;
    }

    public List<propEntity> getProp() {
        return prop;
    }

    public void setProp(List<propEntity> prop) {
        this.prop = prop;
    }

    public List<CrystalGiftEntity> getCrystal() {
        return crystal;
    }

    public void setCrystal(List<CrystalGiftEntity> crystal) {
        this.crystal = crystal;
    }

    public abstract static class GiftEntity {
        protected Integer id;
        protected String name;
        protected Integer money;
        protected String img;
        protected String link;
        protected boolean isFirst;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getMoney() {
            return money;
        }

        public void setMoney(Integer money) {
            this.money = money;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public boolean isFirst() {
            return isFirst;
        }

        public void setFirst(boolean first) {
            isFirst = first;
        }
    }

    public class DiamondGiftEntity extends GiftEntity {

        @SerializedName("icon_id")
        private Integer iconId;
        private iconEntity icon;

        public Integer getIconId() {
            return iconId;
        }

        public void setIconId(Integer iconId) {
            this.iconId = iconId;
        }

        public iconEntity getIcon() {
            return icon;
        }

        public void setIcon(iconEntity icon) {
            this.icon = icon;
        }

        public class iconEntity {
            private Integer id;
            private String img;
            private String name;

            public Integer getId() {
                return id;
            }

            public void setId(Integer id) {
                this.id = id;
            }

            public String getImg() {
                return img;
            }

            public void setImg(String img) {
                this.img = img;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }

        @NonNull
        @Override
        public String toString() {
            return "DiamondGiftEntity{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", money=" + money +
                    ", img='" + img + '\'' +
                    ", link='" + link + '\'' +
                    ", isFirst=" + isFirst +
                    ", iconId=" + iconId +
                    ", icon=" + icon +
                    '}';
        }
    }

    public class propEntity {
        private Integer id;
        private Integer total = 0;
        private String name;
        private String icon;
        @SerializedName("prop_type")
        private Integer propType;
        private String desc;

        public Integer getPropType() {
            return propType;
        }

        public void setPropType(Integer propType) {
            this.propType = propType;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getTotal() {
            return total;
        }

        public void setTotal(Integer total) {
            this.total = total;
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
    }

    @NonNull
    @Override
    public String toString() {
        return "GiftBagEntity{" +
                "isFirst=" + isFirst +
                ", totalCoin=" + totalCoin +
                ", gift=" + gift +
                ", prop=" + prop +
                '}';
    }

    public class CrystalGiftEntity extends GiftEntity {
        private Integer type;
        @SerializedName("gift_languages")
        private String lang;

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        @NonNull
        @Override
        public String toString() {
            return "CrystalGift{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", money=" + money +
                    ", img='" + img + '\'' +
                    ", link='" + link + '\'' +
                    ", isFirst=" + isFirst +
                    ", type=" + type +
                    ", lang='" + lang + '\'' +
                    '}';
        }
    }
}
