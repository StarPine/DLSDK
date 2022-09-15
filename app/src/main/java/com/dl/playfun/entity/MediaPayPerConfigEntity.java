package com.dl.playfun.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Author: 彭石林
 * Time: 2022/9/15 16:05
 * Description: This is MediaPayPerConfigEntity
 */
public class MediaPayPerConfigEntity implements Serializable{
    private itemTagEntity video;
    private itemTagEntity photo;

    public itemTagEntity getVideo() {
        return video;
    }

    public void setVideo(itemTagEntity video) {
        this.video = video;
    }

    public itemTagEntity getPhoto() {
        return photo;
    }

    public void setPhoto(itemTagEntity photo) {
        this.photo = photo;
    }

    public static class itemTagEntity implements Serializable{
        private List<itemEntity> content;

        public List<itemEntity> getContent() {
            return content;
        }

        public void setContent(List<itemEntity> content) {
            this.content = content;
        }
    }

    public static class itemEntity implements Serializable {
        private int coin;
        private BigDecimal profit;

        public int getCoin() {
            return coin;
        }

        public void setCoin(int coin) {
            this.coin = coin;
        }

        public BigDecimal getProfit() {
            return profit;
        }

        public void setProfit(BigDecimal profit) {
            this.profit = profit;
        }
    }
}
