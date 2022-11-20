package com.dl.rtc.calling.base

/**
 * 定义接口层统一约束规范
 */
interface DLRTCCallingItFace {

    /**
     * 进入房间
     */
    fun enterRTCRoom(roomId : Int,roomIds : String? = null)

    /**
     *  暂停/恢复发布本地的音频流
     */
    fun muteLocalAudio(enable : Boolean)

    /**
     * 音频路由，即声音由哪里输出（扬声器、听筒）
     */
    fun audioRoute(route : Boolean)

    /**
     * @Desc TODO(是否开启自动增益补偿功能, 可以自动调麦克风的收音量到一定的音量水平)
     * @author 彭石林
     * @parame [enable]
     * @Date 2022/2/14
     */
    fun enableAGC(enable : Boolean)

    /**
     * @Desc TODO(回声消除器 ， 可以消除各种延迟的回声)
     * @author 彭石林
     * @parame [enable]
     * @Date 2022/2/14
     */
    fun enableAEC(enable : Boolean)

    /**
     * @Desc TODO(背景噪音抑制功能 ， 可探测出背景固定频率的杂音并消除背景噪音)
     * @author 彭石林
     * @parame [enable]
     * @Date 2022/2/14
     */
    fun enableANS(enable : Boolean)
}