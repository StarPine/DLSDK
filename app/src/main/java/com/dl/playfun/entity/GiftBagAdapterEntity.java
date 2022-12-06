package com.dl.playfun.entity;

import java.util.List;

/**
 * Author: 彭石林
 * Time: 2021/12/7 17:47
 * Description: 礼物列表适配器bean
 */
public class GiftBagAdapterEntity {
    private  int idx;
    private List<? extends GiftBagEntity.GiftEntity> giftBagEntity;

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public List<? extends GiftBagEntity.GiftEntity> getGiftBagEntity() {
        return giftBagEntity;
    }

    public void setGiftBagEntity(List<? extends GiftBagEntity.GiftEntity> giftBagEntity) {
        this.giftBagEntity = giftBagEntity;
    }

    public GiftBagAdapterEntity(int idx, List<? extends GiftBagEntity.GiftEntity> giftBagEntity) {
        this.idx = idx;
        this.giftBagEntity = giftBagEntity;
    }

    @Override
    public String toString() {
        return "GiftBagAdapterEntity{" +
                "idx=" + idx +
                ", giftBagEntity=" + giftBagEntity +
                '}';
    }
}
