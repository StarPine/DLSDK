package com.dl.playfun.entity;

import androidx.databinding.BaseObservable;

import com.dl.playfun.data.typeadapter.BooleanTypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * 隐私设置
 *
 * @author wulei
 */
public class PrivacyEntity extends BaseObservable {

    /**
     * is_home : 1
     * is_distance : 0
     * is_online_ime : 0
     * is_account : 0
     * is_connection : 0
     */
    @SerializedName("is_home")
    @JsonAdapter(BooleanTypeAdapter.class)
    private Boolean isHome;
    @SerializedName("is_distance")
    @JsonAdapter(BooleanTypeAdapter.class)
    private Boolean isDistance;
    @SerializedName("is_online_time")
    @JsonAdapter(BooleanTypeAdapter.class)
    private Boolean isOnlineIme;
    @SerializedName("is_connection")
    @JsonAdapter(BooleanTypeAdapter.class)
    private Boolean isConnection;
    @SerializedName("is_nearby")
    @JsonAdapter(BooleanTypeAdapter.class)
    private Boolean isNearby;
    private String phone;

    public Boolean getHome() {
        return isHome;
    }

    public void setHome(Boolean home) {
        isHome = home;
    }

    public Boolean getDistance() {
        return isDistance;
    }

    public void setDistance(Boolean distance) {
        isDistance = distance;
    }

    public Boolean getOnlineIme() {
        return isOnlineIme;
    }

    public void setOnlineIme(Boolean onlineIme) {
        isOnlineIme = onlineIme;
    }

    public Boolean getConnection() {
        return isConnection;
    }

    public void setConnection(Boolean connection) {
        isConnection = connection;
    }

    public Boolean getNearby() {
        return isNearby;
    }

    public void setNearby(Boolean nearby) {
        isNearby = nearby;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
