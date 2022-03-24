package com.dl.playfun.entity;

import androidx.databinding.BaseObservable;

import java.util.List;

public class UnlockSocialAccountConfigEntity extends BaseObservable{

    /**
     * priceInfos : [{"level":1,"coins":200,"price":18.88},{"level":2,"coins":5000,"price":450.66},{"level":3,"coins":80000,"price":7200.55}]
     * percent : 10%
     * selectedLevel : 2
     */

    private String percent;
    private int selectedLevel;
    private List<PriceInfosBean> priceInfos;

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    public int getSelectedLevel() {
        return selectedLevel;
    }

    public void setSelectedLevel(int selectedLevel) {
        this.selectedLevel = selectedLevel;
    }

    public List<PriceInfosBean> getPriceInfos() {
        return priceInfos;
    }

    public void setPriceInfos(List<PriceInfosBean> priceInfos) {
        this.priceInfos = priceInfos;
    }

    public static class PriceInfosBean{
        /**
         * level : 1
         * coins : 200
         * price : 18.88
         */

        private int level;
        private int coins;
        private double price;

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public int getCoins() {
            return coins;
        }

        public void setCoins(int coins) {
            this.coins = coins;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }
}
