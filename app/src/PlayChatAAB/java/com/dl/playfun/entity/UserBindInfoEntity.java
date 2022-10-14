package com.dl.playfun.entity;

/**
 * Author: 彭石林
 * Time: 2022/10/13 16:30
 * Description: This is UserbindInfoEntity
 */
public class UserBindInfoEntity {
    //手机号码，没有绑定返回空字符串
    private String phone;

    //是否绑定第三方账号 	0未绑定， 1 苹果;2 fabook;3 google
    private int bindAuth;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getBindAuth() {
        return bindAuth;
    }

    public void setBindAuth(int bindAuth) {
        this.bindAuth = bindAuth;
    }
}
