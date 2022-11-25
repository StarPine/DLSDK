package com.dl.playfun.entity;

import java.io.Serializable;

/**
 * Author: 彭石林
 * Time: 2022/11/24 10:48
 * Description: 新的用户资料
 */
public class CallUserInfoEntity implements Serializable {
    //id
    private int id;
    //IM id
    private String imId;
    //姓名
    private String nickname;
    //头像
    private String avatar;
    //性别
    private int sex = -1;
    //是否是vip or 女神
    private int isVip;
    //真人认证
    private int certification;
    //城市id
    private int cityId = -1;
    //城市名
    private String cityName;
    private int age;
    //星座ID
    private int constellationId;
    //星座
    private String constellation;
    //职业ID
    private int occupationId;
    //职业
    private String occupation;
    //状态:0=正常;1=封号
    private int status;
    //在线状态:0=不在线;1=在线;
    private int isOnline;
    //是否 已追踪0未追踪1已追踪2相互追踪
    private int collected;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImId() {
        return imId;
    }

    public void setImId(String imId) {
        this.imId = imId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getIsVip() {
        return isVip;
    }

    public void setIsVip(int isVip) {
        this.isVip = isVip;
    }

    public int getCertification() {
        return certification;
    }

    public void setCertification(int certification) {
        this.certification = certification;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getConstellationId() {
        return constellationId;
    }

    public void setConstellationId(int constellationId) {
        this.constellationId = constellationId;
    }

    public String getConstellation() {
        return constellation;
    }

    public void setConstellation(String constellation) {
        this.constellation = constellation;
    }

    public int getOccupationId() {
        return occupationId;
    }

    public void setOccupationId(int occupationId) {
        this.occupationId = occupationId;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(int isOnline) {
        this.isOnline = isOnline;
    }

    public int getCollected() {
        return collected;
    }

    public void setCollected(int collected) {
        this.collected = collected;
    }
}
