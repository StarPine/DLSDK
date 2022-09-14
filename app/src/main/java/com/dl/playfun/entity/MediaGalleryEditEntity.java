package com.dl.playfun.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Author: 彭石林
 * Time: 2022/9/14 18:59
 * Description: 发送图片、视频信息类
 */
public class MediaGalleryEditEntity implements Serializable {
    //是否是视频
    private boolean isVideoSetting;
    //是否快照
    private boolean stateSnapshot;
    //是否付费
    private boolean statePay;
    //是否解锁
    private boolean stateUnlockPhoto;
    //解锁金额
    private BigDecimal unlockPrice;
    //oss相对地址
    private String srcPath;
    //图片宽
    private int imgWidth;
    //图片高
    private int imgHeight;

    public boolean isVideoSetting() {
        return isVideoSetting;
    }

    public void setVideoSetting(boolean videoSetting) {
        isVideoSetting = videoSetting;
    }

    public boolean isStateSnapshot() {
        return stateSnapshot;
    }

    public void setStateSnapshot(boolean stateSnapshot) {
        this.stateSnapshot = stateSnapshot;
    }

    public boolean isStatePay() {
        return statePay;
    }

    public void setStatePay(boolean statePay) {
        this.statePay = statePay;
    }

    public boolean isStateUnlockPhoto() {
        return stateUnlockPhoto;
    }

    public void setStateUnlockPhoto(boolean stateUnlockPhoto) {
        this.stateUnlockPhoto = stateUnlockPhoto;
    }

    public BigDecimal getUnlockPrice() {
        return unlockPrice;
    }

    public void setUnlockPrice(BigDecimal unlockPrice) {
        this.unlockPrice = unlockPrice;
    }

    public String getSrcPath() {
        return srcPath;
    }

    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath;
    }

    public int getImgWidth() {
        return imgWidth;
    }

    public void setImgWidth(int imgWidth) {
        this.imgWidth = imgWidth;
    }

    public int getImgHeight() {
        return imgHeight;
    }

    public void setImgHeight(int imgHeight) {
        this.imgHeight = imgHeight;
    }

    @Override
    public String toString() {
        return "MediaGalleryEditEntity{" +
                "isVideoSetting=" + isVideoSetting +
                ", stateSnapshot=" + stateSnapshot +
                ", statePay=" + statePay +
                ", stateUnlockPhoto=" + stateUnlockPhoto +
                ", unlockPrice=" + unlockPrice +
                ", srcPath='" + srcPath + '\'' +
                ", imgWidth=" + imgWidth +
                ", imgHeight=" + imgHeight +
                '}';
    }
}
