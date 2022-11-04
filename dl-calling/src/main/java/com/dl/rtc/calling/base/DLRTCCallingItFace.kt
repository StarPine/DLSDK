package com.dl.rtc.calling.base

/**
 * 定义接口层统一约束规范
 */
interface DLRTCCallingItFace {
    /**
     * 挂断
     */
    fun reject()
    /**
     * 接听
     */
    fun accept()

    /**
     * 进入房间
     */
    fun enterRoom(roomId : Int)

    /**
     * 离开房间
     */
    fun exitRoom();
}