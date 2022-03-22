package com.dl.playfun.entity;

import com.google.gson.annotations.SerializedName;

/**
 * @ClassName TagEntity
 * @Description TODO
 * @Author 彭石林
 * @Date 2021/7/3 10:46
 * @Phone 16620350375
 * @email 15616314565@163.com
 * @Version 1.0
 **/
public class TagEntity {

    @SerializedName("this_is_gg")
    private Integer thisIsGg; //当前用户是否GG 0否 1是

    @SerializedName("to_is_gg")
    private Integer toIsGg; //	对方用户是否GG 0否 1是
    @SerializedName("to_is_invite")
    private Integer toIsInvite; //	对方用户是否填写邀请码 0否 1是

    public Integer getThisIsGg() {
        return thisIsGg;
    }

    public void setThisIsGg(Integer thisIsGg) {
        this.thisIsGg = thisIsGg;
    }

    public Integer getToIsGg() {
        return toIsGg;
    }

    public void setToIsGg(Integer toIsGg) {
        this.toIsGg = toIsGg;
    }

    public Integer getToIsInvite() {
        return toIsInvite;
    }

    public void setToIsInvite(Integer toIsInvite) {
        this.toIsInvite = toIsInvite;
    }
}