package com.dl.playfun.entity;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.dl.common.BR;

import java.util.Date;
import java.util.List;

/**
 * Author: 彭石林
 * Time: 2022/10/17 18:10
 * Description: This is FrequentContactEntity
 */
public class FrequentContactEntity extends BaseObservable {

    private List<ItemEntity> userList;


    public List<ItemEntity> getUserList() {
        return userList;
    }

    public void setUserList(List<ItemEntity> userList) {
        this.userList = userList;
    }

    public static class ItemEntity extends BaseObservable {
        private UserProfile userProfile;
        private Integer distance;
        //是否搭讪过:0=未搭讪过;1=已搭讪过;
        @Bindable
        private Integer isAccost;

        public UserProfile getUserProfile() {
            return userProfile;
        }

        public void setUserProfile(UserProfile userProfile) {
            this.userProfile = userProfile;
        }

        public Integer getDistance() {
            return distance;
        }

        public void setDistance(Integer distance) {
            this.distance = distance;
        }

        public Integer getIsAccost() {
            return isAccost;
        }

        public void setIsAccost(Integer isAccost) {
            this.isAccost = isAccost;
            notifyPropertyChanged(BR.isAccost);
        }
    }

    public static class UserProfile extends BaseObservable {
        private Integer id;
        private String imId;
        private String nickname;
        private String avatar;
        private Integer sex;
        private Integer isVip;
        private Integer certification;
        private Integer cityId;
        private String cityName;
        private Integer age;
        private String constellation;
        private Integer occupationId;
        private String occupation;
        private Integer status;
        private Integer isOnline;
        private Integer callingStatus;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
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

        public Integer getSex() {
            return sex;
        }

        public void setSex(Integer sex) {
            this.sex = sex;
        }

        public Integer getIsVip() {
            return isVip;
        }

        public void setIsVip(Integer isVip) {
            this.isVip = isVip;
        }

        public Integer getCertification() {
            return certification;
        }

        public void setCertification(Integer certification) {
            this.certification = certification;
        }

        public Integer getCityId() {
            return cityId;
        }

        public void setCityId(Integer cityId) {
            this.cityId = cityId;
        }

        public String getCityName() {
            return cityName;
        }

        public void setCityName(String cityName) {
            this.cityName = cityName;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getConstellation() {
            return constellation;
        }

        public void setConstellation(String constellation) {
            this.constellation = constellation;
        }

        public Integer getOccupationId() {
            return occupationId;
        }

        public void setOccupationId(Integer occupationId) {
            this.occupationId = occupationId;
        }

        public String getOccupation() {
            return occupation;
        }

        public void setOccupation(String occupation) {
            this.occupation = occupation;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public Integer getIsOnline() {
            return isOnline;
        }

        public void setIsOnline(Integer isOnline) {
            this.isOnline = isOnline;
        }

        public Integer getCallingStatus() {
            return callingStatus;
        }

        public void setCallingStatus(Integer callingStatus) {
            this.callingStatus = callingStatus;
        }

    }
}
