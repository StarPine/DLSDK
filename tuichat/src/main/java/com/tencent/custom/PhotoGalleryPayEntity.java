package com.tencent.custom;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Author: 彭石林
 * Time: 2022/9/12 15:40
 * Description: TIM发送付费照片 - 照片模块
 */
public class PhotoGalleryPayEntity implements Serializable {
    ////是否快照
    private boolean stateSnapshot;
    //付费照片
    private boolean statePhotoPay;
    //是否解锁
    private boolean stateUnlockPhoto;
    //解锁金额
    private BigDecimal unlockPrice;
    //oss相对地址
    private String imgPath;
    //图片宽
    private int imgWidth;
    //图片高
    private int imgHeight;

    public boolean isStateSnapshot() {
        return stateSnapshot;
    }

    public void setStateSnapshot(boolean stateSnapshot) {
        this.stateSnapshot = stateSnapshot;
    }

    public boolean isStatePhotoPay() {
        return statePhotoPay;
    }

    public void setStatePhotoPay(boolean statePhotoPay) {
        this.statePhotoPay = statePhotoPay;
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

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
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
}
