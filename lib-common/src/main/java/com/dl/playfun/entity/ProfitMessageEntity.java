package com.dl.playfun.entity;

import com.google.gson.annotations.SerializedName;

/**
 * 收益提醒消息
 *
 * @author wulei
 */
public class ProfitMessageEntity {

    private int id;
    @SerializedName("created_at")
    private String createdAt;
    private String content;
    private ItemUserEntity user;
    /**
     * 1相册收益 2红包照片收益 money 金额
     */
    private Integer type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }


    public ItemUserEntity getUser() {
        return user;
    }

    public void setUser(ItemUserEntity user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }


}
