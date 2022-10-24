package com.dl.playfun.event;

/**
 * Author: 彭石林
 * Time: 2022/10/24 1:18
 * Description: This is ToastUIEvent
 */
public class ToastUIEvent {
    private int resId = -1;
    private long showTime =-1;

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public long getShowTime() {
        return showTime;
    }

    public void setShowTime(long showTime) {
        this.showTime = showTime;
    }

    public ToastUIEvent(int resId) {
        this.resId = resId;
    }

    public ToastUIEvent(int resId, long showTime) {
        this.resId = resId;
        this.showTime = showTime;
    }
}
