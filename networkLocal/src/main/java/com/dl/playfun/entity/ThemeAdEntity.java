package com.dl.playfun.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @ClassName ThemeAdEntity
 * @Description TODO
 * @Author 彭石林
 * @Date 2021/6/25 11:32
 * @Phone 16620350375
 * @email 15616314565@163.com
 * @Version 1.0
 **/
public class ThemeAdEntity {

    @SerializedName("them")
    private List<ThemeItemEntity> them;
    @SerializedName("ad")
    private List<AdItemEntity> ad;

    public List<ThemeItemEntity> getThem() {
        return them;
    }

    public void setThem(List<ThemeItemEntity> them) {
        this.them = them;
    }

    public List<AdItemEntity> getAd() {
        return ad;
    }

    public void setAd(List<AdItemEntity> ad) {
        this.ad = ad;
    }
}