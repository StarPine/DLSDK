package com.dl.playfun.entity;

import com.google.gson.annotations.SerializedName;

/**
 * 系统配置参数
 *
 * @author litchi
 */
public class SystemConfigEntity {

    private double CashOutServiceFee;
    private double CoinExchangeMoney;
    private Integer VideoRedPackageMoney;
    private Integer ImageRedPackageMoney;
    @SerializedName("man_user")
    private SystemRoleMoneyConfigEntity manUser;
    @SerializedName("man_real")
    private SystemRoleMoneyConfigEntity manReal;
    @SerializedName("man_vip")
    private SystemRoleMoneyConfigEntity manVip;
    @SerializedName("woman_user")
    private SystemRoleMoneyConfigEntity womanUser;
    @SerializedName("woman_real")
    private SystemRoleMoneyConfigEntity womanReal;
    @SerializedName("woman_vip")
    private SystemRoleMoneyConfigEntity womanVip;
    private SystemConfigContentEntity content;

    //会话列表限制数量
    @SerializedName("conversation_astrict_count")
    private Integer conversationAstrictCount;
    //默认区号
    @SerializedName("region_code")
    private String regionCode;

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public Integer getConversationAstrictCount() {
        return conversationAstrictCount;
    }

    public void setConversationAstrictCount(Integer conversationAstrictCount) {
        this.conversationAstrictCount = conversationAstrictCount;
    }

    public double getCashOutServiceFee() {
        return CashOutServiceFee;
    }

    public void setCashOutServiceFee(double cashOutServiceFee) {
        CashOutServiceFee = cashOutServiceFee;
    }

    public double getCoinExchangeMoney() {
        return CoinExchangeMoney;
    }

    public void setCoinExchangeMoney(double coinExchangeMoney) {
        CoinExchangeMoney = coinExchangeMoney;
    }

    public Integer getVideoRedPackageMoney() {
        return VideoRedPackageMoney;
    }

    public void setVideoRedPackageMoney(Integer videoRedPackageMoney) {
        VideoRedPackageMoney = videoRedPackageMoney;
    }

    public Integer getImageRedPackageMoney() {
        return ImageRedPackageMoney;
    }

    public void setImageRedPackageMoney(Integer imageRedPackageMoney) {
        ImageRedPackageMoney = imageRedPackageMoney;
    }

    public SystemRoleMoneyConfigEntity getManUser() {
        return manUser;
    }

    public void setManUser(SystemRoleMoneyConfigEntity manUser) {
        this.manUser = manUser;
    }

    public SystemRoleMoneyConfigEntity getManReal() {
        return manReal;
    }

    public void setManReal(SystemRoleMoneyConfigEntity manReal) {
        this.manReal = manReal;
    }

    public SystemRoleMoneyConfigEntity getManVip() {
        return manVip;
    }

    public void setManVip(SystemRoleMoneyConfigEntity manVip) {
        this.manVip = manVip;
    }

    public SystemRoleMoneyConfigEntity getWomanUser() {
        return womanUser;
    }

    public void setWomanUser(SystemRoleMoneyConfigEntity womanUser) {
        this.womanUser = womanUser;
    }

    public SystemRoleMoneyConfigEntity getWomanReal() {
        return womanReal;
    }

    public void setWomanReal(SystemRoleMoneyConfigEntity womanReal) {
        this.womanReal = womanReal;
    }

    public SystemRoleMoneyConfigEntity getWomanVip() {
        return womanVip;
    }

    public void setWomanVip(SystemRoleMoneyConfigEntity womanVip) {
        this.womanVip = womanVip;
    }

    public SystemConfigContentEntity getContent() {
        return content;
    }

    public void setContent(SystemConfigContentEntity content) {
        this.content = content;
    }
}
