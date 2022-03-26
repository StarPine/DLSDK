package com.dl.playfun.event;

/**
 * @author wulei
 */
public class OnlineChangeEvent {
    boolean isOnline;

    public OnlineChangeEvent(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
