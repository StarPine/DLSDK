package com.dl.rtc.calling.base

import android.view.View

/**
 *Author: 彭石林
 *Time: 2022/11/3 15:58
 * Description: This is DLRTCCalling
 */
interface DLRTCCalling {
    /* 呼叫类型 */
    enum class Type {
        AUDIO,  //语音
        VIDEO //视频
    }

    /* 角色 */
    enum class Role {
        CALL,  //呼叫方
        CALLED //被叫方
    }

    enum class Event {
        CALL_START,  // 通话开始
        CALL_SUCCEED,  // 通话接通成功
        CALL_END,  // 通话结束
        CALL_FAILED
        // 通话失败
    }
}