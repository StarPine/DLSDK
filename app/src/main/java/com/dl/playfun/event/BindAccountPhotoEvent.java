package com.dl.playfun.event;

/**
 * Author: 彭石林
 * Time: 2022/10/20 14:59
 * Description: This is BindAccountPhotoEvemt
 */
public class BindAccountPhotoEvent {
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BindAccountPhotoEvent(String phone) {
        this.phone = phone;
    }
}
