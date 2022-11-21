package com.dl.playfun.entity;

import java.util.List;

/**
 * @author Shuotao Gong
 * @time 2022/11/19
 */
public class CrystalGiftBagAdapterEntity {

    private int idx;
    private List<GiftBagEntity.CrystalGift> crystalGiftEntity;

    public CrystalGiftBagAdapterEntity(int idx, List<GiftBagEntity.CrystalGift> crystalGiftEntity) {
        this.idx = idx;
        this.crystalGiftEntity = crystalGiftEntity;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public List<GiftBagEntity.CrystalGift> getCrystalGiftEntity() {
        return crystalGiftEntity;
    }

    public void setCrystalGiftEntity(List<GiftBagEntity.CrystalGift> crystalGiftEntity) {
        this.crystalGiftEntity = crystalGiftEntity;
    }

    @Override
    public String toString() {
        return "CrystalGiftBagAdapterEntity{" +
                "idx=" + idx +
                ", crystalGiftEntity=" + crystalGiftEntity +
                '}';
    }
}
