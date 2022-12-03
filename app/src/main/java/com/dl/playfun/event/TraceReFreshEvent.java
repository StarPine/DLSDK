package com.dl.playfun.event;

/**
 * Author: 彭石林
 * Time: 2021/8/3 18:25
 * Description: This is TraceEvent
 */
public class TraceReFreshEvent {
    int index;

    public TraceReFreshEvent(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
