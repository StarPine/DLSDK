package com.dl.playfun.api;

/**
 * Author: 彭石林
 * Time: 2022/4/19 15:20
 * Description: This is AppGameConfig
 */
public class AppGameConfig {
    private String appId;
    //区服ID
    private String serverId;
    //角色ID
    private String roleId;
    //返回游戏图标
    private int GamePlayLogoImg;
    //金币小图
    private int GamePlayCoinSmallImg;
    //金币大图
    private int GamePlayCoinBigImg;
    //服务条款url
    private String termsOfServiceUrl;
    //隐私政策url
    private String privacyPolicyUrl;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public int getGamePlayLogoImg() {
        return GamePlayLogoImg;
    }

    public void setGamePlayLogoImg(int gamePlayLogoImg) {
        GamePlayLogoImg = gamePlayLogoImg;
    }

    public int getGamePlayCoinSmallImg() {
        return GamePlayCoinSmallImg;
    }

    public void setGamePlayCoinSmallImg(int gamePlayCoinSmallImg) {
        GamePlayCoinSmallImg = gamePlayCoinSmallImg;
    }

    public int getGamePlayCoinBigImg() {
        return GamePlayCoinBigImg;
    }

    public void setGamePlayCoinBigImg(int gamePlayCoinBigImg) {
        GamePlayCoinBigImg = gamePlayCoinBigImg;
    }

    public String getTermsOfServiceUrl() {
        return termsOfServiceUrl;
    }

    public void setTermsOfServiceUrl(String termsOfServiceUrl) {
        this.termsOfServiceUrl = termsOfServiceUrl;
    }

    public String getPrivacyPolicyUrl() {
        return privacyPolicyUrl;
    }

    public void setPrivacyPolicyUrl(String privacyPolicyUrl) {
        this.privacyPolicyUrl = privacyPolicyUrl;
    }
}
