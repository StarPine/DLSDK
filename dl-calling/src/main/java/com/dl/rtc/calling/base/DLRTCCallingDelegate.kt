package com.dl.rtc.calling.base

import com.tencent.trtc.TRTCCloudDef

/**
 *Author: 彭石林
 *Time: 2022/11/3 14:46
 * Description: This is DLRTCCallingDelegate
 */
interface DLRTCCallingDelegate {
    /**
     * sdk内部发生了错误
     * @param code 错误码
     * @param msg 错误消息
     */
    fun onError(code: Int, msg: String?)

    /**
     * 如果有用户同意进入通话，那么会收到此回调
     * @param userId 进入通话的用户
     */
    fun onUserEnter(userId: String?)

    /**
     * 如果有用户同意离开通话，那么会收到此回调
     * @param userId 离开通话的用户
     */
    fun onUserLeave(userId: String?)

    /**
     * 1. 在C2C通话中，只有发起方会收到拒绝回调
     * 例如 A 邀请 B、C 进入通话，B拒绝，A可以收到该回调，但C不行
     *
     * 2. 在IM群组通话中，所有被邀请人均能收到该回调
     * 例如 A 邀请 B、C 进入通话，B拒绝，A、C均能收到该回调
     * @param userId 拒绝通话的用户
     */
    fun onReject(userId: String?)

    /**
     * 邀请方忙线
     * @param userId 忙线用户
     */
    fun onLineBusy(userId: String?)

    /**
     * 作为被邀请方会收到，收到该回调说明本次通话被取消了
     */
    fun onCallingCancel()

    /**
     * 作为被邀请方会收到，收到该回调说明本次通话超时未应答
     */
    fun onCallingTimeout()

    /**
     * 收到该回调说明本次通话结束了
     */
    fun onCallEnd()

    /**
     * 远端用户开启/关闭了摄像头
     * @param userId 远端用户ID
     * @param isVideoAvailable true:远端用户打开摄像头  false:远端用户关闭摄像头
     */
    fun onUserVideoAvailable(userId: String?, isVideoAvailable: Boolean)

    /**
     * 远端用户开启/关闭了麦克风
     * @param userId 远端用户ID
     * @param isVideoAvailable true:远端用户打开麦克风  false:远端用户关闭麦克风
     */
    fun onUserAudioAvailable(userId: String?, isVideoAvailable: Boolean)

    /**
     * 用户说话音量回调
     * @param volumeMap 音量表，根据每个userid可以获取对应的音量大小，音量最小值0，音量最大值100
     */
    fun onUserVoiceVolume(volumeMap: Map<String?, Int?>?)

    /**
     * 网络状态回调。
     *
     * @param localQuality 上行网络质量。
     * @param remoteQuality 下行网络质量。
     */
    fun onNetworkQuality(localQuality: TRTCCloudDef.TRTCQuality?, remoteQuality: ArrayList<TRTCCloudDef.TRTCQuality?>?)

    /**
     * 网络重连
     */
    fun onTryToReconnect()
}