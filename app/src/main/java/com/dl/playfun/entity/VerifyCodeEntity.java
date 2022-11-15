package com.dl.playfun.entity;

/**
 * Author: 彭石林
 * Time: 2022/11/15 0:28
 * Description: This is VerifyCodeEntity
 */
public class VerifyCodeEntity {
    //手机号已存在(0不存在;1已存在)
    private Integer verifyCode;

    public Integer getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(Integer verifyCode) {
        this.verifyCode = verifyCode;
    }
}
