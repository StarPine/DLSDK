package com.dl.rtc.calling.manager

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.dl.lib.util.log.MPTimber
import com.dl.rtc.calling.DLRTCCallService
import com.dl.rtc.calling.R
import com.dl.rtc.calling.base.DLRTCCalling
import com.dl.rtc.calling.base.impl.DLRTCInternalListenerManager
import com.dl.rtc.calling.model.*
import com.dl.rtc.calling.model.bean.DLRTCCallModel
import com.dl.rtc.calling.util.DLRTCSignallingUtil
import com.dl.rtc.calling.util.MediaPlayHelper
import com.dl.rtc.calling.util.PermissionUtil
import com.google.gson.GsonBuilder
import com.tencent.imsdk.v2.*
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.trtc.TRTCCloud
import com.tencent.trtc.TRTCCloud.sharedInstance
import com.tencent.trtc.TRTCCloudDef.*
import com.tencent.trtc.TRTCCloudListener
import me.goldze.mvvmhabit.base.AppManager
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 *Author: 彭石林
 *Time: 2022/11/3 15:47
 * Description: This is DLRTCStartManager
 */
class DLRTCStartManager {
    val TAGLOG = "DLRTCStartManager";

    private val mMainHandler = Handler(Looper.getMainLooper())


    ///当前用户的ID
    private var currentId : String = ""
    ///接收方的ID
    var acceptUserId : String = ""
    ///发起方的ID
    var inviteUserId : String = ""
    ///腾讯生成的邀请ID
    var inviteId : String = ""
    /// 当前rtc的房间
    var inviteRTCRoomId : Int = 0
    ///邀请的类型，纯音频还是音视频一起
    var inviteTypeMsg : String = ""
    ///是否发起了邀请
    var isBeginInvite : Boolean = false
    ///是否收到了邀请
    var isReceiveNewInvite : Boolean = false

    fun setLoginSuccessUser(userId : String) {
        this@DLRTCStartManager.currentId = userId
    }

    ///代理的结合，实现多处响应
    private val delegates by lazy { ArrayList<DLRTCStartManagerDelegate>()}
    fun initParams() {
        ///取消后，数值清空
        this.inviteUserId = ""
        this.inviteId = ""
        this.inviteTypeMsg = ""
        this.isBeginInvite = false
        this.isReceiveNewInvite = false
        this.inviteRTCRoomId = 0
        this.acceptUserId = ""
    }

    fun addRtcListener(listener: DLRTCStartManagerDelegate) {
        if(!delegates.contains(listener)){
            delegates.add(listener)
        }
    }

    fun removeRtcListener(listener : DLRTCStartManagerDelegate) {
        delegates.remove(listener)
    }

    /**
     * 超时时间，单位秒
     */
    val DLInviteTimeout = 30

    //通话邀请缓存,便于查询通话是否有效
    private val mInviteMap: MutableMap<String, DLRTCCallModel> = HashMap()


    val gsonBuilder by lazy { GsonBuilder() }

    private var initFlag = false


    /**
     * 上层传入回调
     */
    private var mTRTCInternalListenerManager: DLRTCInternalListenerManager? = null

    companion object{
        val instance by lazy {
            DLRTCStartManager()
        }
    }

    var mContext : Context? = null
    /**
     * 底层SDK调用实例
     */
    var mTRTCCloud: TRTCCloud? = null

    /**
     * 进行初始化，程序理应有1个静默式的接收处理逻辑（只调用一次）
     */
    fun init(mContexts: Context) {
        mContext = mContexts
        if(initFlag){
            return
        }
        mTRTCInternalListenerManager = DLRTCInternalListenerManager.instance
        mTRTCCloud = TRTCCloud.sharedInstance(mContext)
        initFlag = true
        DLRTCIMSignallingManager.getInstance().addSignalingListener(mTIMSignallingListener)
        initParams()
    }

    /**
     * 主动呼叫方
     * inviteUser 接听人 inviteType 呼叫类型 roomId 房间ID closure 当前调用回调
     */
    fun inviteUserRTC(inviteUser : String, inviteType : DLRTCDataMessageType.DLInviteRTCType, roomId : Int, closure :DLRTCModuleClosuer?){
        ///如果已经开始在邀请,防止重复发起邀请
        if (isBeginInvite && !this.inviteUserId.isNullOrEmpty()) {
            closure?.callback(false,10010,"当前已经在邀请中")
            return
        }
        if(this@DLRTCStartManager.currentId.isEmpty()){
            this@DLRTCStartManager.currentId = TUILogin.getLoginUser()
        }
        isBeginInvite = true
        this.inviteUserId = currentId
        this.inviteTypeMsg = inviteType.name
        this.inviteRTCRoomId = roomId
        this.acceptUserId = inviteUser
        val pushInfo = V2TIMOfflinePushInfo()
        ///构建额外的信息
        val dataParams = buildRTCParams(DLRTCDataMessageType.invite,acceptUserId,currentId)
        dataParams[DLRTCDataMessageType.DLRTCInviteTimeOut] = DLInviteTimeout
        val dataString = gsonBuilder.create().toJson(dataParams)

        val logParams = buildRTCLogParams(acceptUserId,currentId)
        this.inviteId = V2TIMManager.getSignalingManager().invite(inviteUser, dataString, true, pushInfo, DLInviteTimeout,object : V2TIMCallback{
            override fun onSuccess() {
                closure?.callback(true,0,null)
                ///添加打点
                logParams["inviteRet"] = 1
                //logParams[inviteIdLogKey] = self?.inviteId
                //self?.addRtcLog(ct: startInviteCTLog, dt: "closureRet", otherParams: logParams)
                ///发送邀请成功,回掉
                val model = DLRTCStartModel()
                model.rtcDataMessageType = DLRTCDataMessageType.inviteSucc
                model.inviteId = inviteId
                model.inviteUserId = currentId
                model.acceptUserId = acceptUserId
                model.rtcInviteRoomId = inviteRTCRoomId
                model.rtcInviteType = inviteType.name
                for (delegate in delegates){
                    delegate.RTCStartManagerReciveMsg(this@DLRTCStartManager,model)
                }
            }

            override fun onError(err_code: Int, err_msg: String?) {
                closure?.callback(false,err_code,err_msg)
                var errorMsg : String = "邀请消息发送失败"
                err_msg?.apply {
                    errorMsg = err_msg
                }
                closure?.callback(false,err_code,errorMsg)
                logParams["inviteRet"] = 0
                logParams["err_code"] = err_code
                logParams["err_msg"] = errorMsg
                //self?.addRtcLog(ct: startInviteCTLog, dt: "closureRet", otherParams: logParams)
                initParams()
            }

        })

    }

    /// 发起方调用取消邀请的接口
    /// - Parameters:
    ///   - inviteId: 邀请的id，由腾讯生成的id
    ///   - closure: 结果反馈
    fun inviteUserCanceled(inviteId : String, closure :  DLRTCModuleClosuer) {
        if(inviteId.isEmpty() || !isBeginInvite){
            return
        }
        //log打点
        val logParams = buildRTCLogParams(acceptUserId,currentId)
        ///额外信息添加
        val dataParams = buildRTCParams(DLRTCDataMessageType.cancel)
        val dataString = gsonBuilder.create().toJson(dataParams)
        V2TIMManager.getSignalingManager().cancel(inviteId, dataString, object : V2TIMCallback{
            override fun onSuccess() {
                ///添加log记录
                logParams["inviteCancelRet"] = 1
                //self?.addRtcLog(ct: cancelInviteCTLog, dt: "closureRet", otherParams: logParams)
                closure.callback(true,0,null)
            }
            override fun onError(err_code: Int, err_msg: String?) {
                var errorMsg  = "发起取消当前邀请失败"
                err_msg?.apply{ errorMsg = err_msg }
                //添加log记录
                logParams["inviteCancelRet"] = 0
                logParams["err_code"] = err_code
                logParams["err_msg"] = errorMsg
               // self?.addRtcLog(ct: cancelInviteCTLog, dt: "closureRet", otherParams: logParams)
                closure.callback(false,err_code,err_msg)
            }

        })
        initParams()
    }

    fun inviteUserAccept(inviteId : String,source : String? = null,closure :  DLRTCModuleClosuer) {
        if (inviteId.isEmpty() || !isReceiveNewInvite) {
            return
        }
        val dataParams = buildRTCParams(DLRTCDataMessageType.accept)
        val dataString = gsonBuilder.create().toJson(dataParams)

        val logParams = buildRTCLogParams()
        var sourceString = ""
        source?.apply { sourceString =source }
        //logParams[DL_SOURCE] = sourceString

        V2TIMManager.getSignalingManager().accept(inviteId, dataString,object : V2TIMCallback{
            override fun onSuccess() {
//                if let _self = self {
//                    logParams["acceptRet"] = 1
//                    _self.addRtcLog(ct: acceptInviteCTLog, dt: "closureRet", otherParams: logParams)
//                }
                closure.callback(true,0,null)
            }

            override fun onError(err_code: Int, err_msg: String?) {
                var errorMsg : String = "接受当前邀请成功失败"
                err_msg?.apply{
                    errorMsg = err_msg
                }
                closure.callback(false,err_code,errorMsg)
                logParams["acceptRet"] = 0
                logParams["err_code"] = err_code
                logParams["err_msg"] = errorMsg
                //self?.addRtcLog(ct: acceptInviteCTLog, dt: "closureRet", otherParams: logParams)
            }

        })
    }

     fun inviteUserReject(inviteId : String,source : String? = null, closure :  DLRTCModuleClosuer?) {
        if (inviteId.isEmpty() || !isReceiveNewInvite) {
            return
        }

        val dataParams = buildRTCParams(DLRTCDataMessageType.reject)
        val dataString = gsonBuilder.create().toJson(dataParams)

        val logParams = buildRTCLogParams()
        var sourceString = ""
        source?.apply { sourceString =source }
        //logParams[DL_SOURCE] = sourceString

        V2TIMManager.getSignalingManager().reject(inviteId, dataString, object : V2TIMCallback{
            override fun onSuccess() {
//                if let _self = self {
//                    logParams["rejectRet"] = 1
//                    _self.addRtcLog(ct: rejectInviteCTLog, dt: "closureRet", otherParams: logParams)
//                }
                closure?.callback(true,0,null)
            }

            override fun onError(err_code: Int, err_msg: String?) {
                var errorMsg : String = "拒绝当前邀请失败"
                err_msg?.apply{
                    errorMsg = err_msg
                }
                closure?.callback(false,err_code,errorMsg)
                logParams["rejectRet"] = 0
                logParams["err_code"] = err_code
                logParams["err_msg"] = errorMsg
                //self?.addRtcLog(ct: rejectInviteCTLog, dt: "closureRet", otherParams: logParams)
            }

        })
        initParams()
    }

    fun RTCRoomExitRoom() {
        initParams()
    }

    //应用在后台且没有拉起应用的权限时,上层主动调用该方法,查询有效的通话请求,拉起界面
    fun queryOfflineCallingInfo() {
        if (mInviteMap.isEmpty()) {
            MPTimber.tag(TAGLOG).d("queryOfflineCalledInfo: no offline call request")
            return
        }
        //有权限时,直接在onReceiveNewInvitation邀请回调中处理,这里不再重复处理
        if (PermissionUtil.hasPermission(mContext)) {
            MPTimber.tag(TAGLOG).d("queryOfflineCalledInfo: call request has processed")
            return
        }
        var inviteId = ""
        var model: DLRTCCallModel? = null
        mInviteMap.iterator().forEach {
            inviteId = it.key
            model = it.value
        }
        if (null == model) {
            return
        }
        MPTimber.tag(TAGLOG).d("queryOfflineCalledInfo: inviteId = $inviteId ,model = $model")
        mTIMSignallingListener.onReceiveNewInvitation(
            inviteId, model!!.sender,
            model!!.groupId, model!!.invitedList, model!!.data
        )
    }

    /**
     * 信令监听器
     */
    private val mTIMSignallingListener: V2TIMSignalingListener = object : V2TIMSignalingListener() {
        /// 收到一个新的邀请
        /// - Parameters:
        ///   - inviteID: 当次邀请的ID
        ///   - inviter: 邀请人的ID
        ///   - groupID: 所属的群组ID
        ///   - inviteeList:
        ///   - data: 扩展的json字符串
        override fun onReceiveNewInvitation(inviteID: String, inviter: String, groupID: String, inviteeList: List<String>, data: String) {
            MPTimber.tag(TAGLOG).d("onReceiveNewInvitation inviteID:" + inviteID + ", inviter:" + inviter
                    + ", groupID:" + groupID + ", inviteeList:" + inviteeList + " data:" + data)
            ///当前已经在邀请别人，或者异常情况下,同时收到同样的邀请ID
            if (isBeginInvite ||  inviteID == this@DLRTCStartManager.inviteId ){
                if (inviteID == this@DLRTCStartManager.inviteId) {
                    return
                }
                this@DLRTCStartManager.inviteUserReject(inviteId,null,null)
                return
            }
            this@DLRTCStartManager.inviteId = inviteID
            this@DLRTCStartManager.inviteUserId = inviter
            this@DLRTCStartManager.isReceiveNewInvite = true

            val dataResult = callBackDataValid(data , DLRTCDataMessageType.invite)
            dataResult?.apply {

                val params = this
                val acceptUserId = (params[DLRTCDataMessageType.DLRTCAcceptUserID] as? String) ?: ""
                if(this@DLRTCStartManager.currentId.isEmpty()){
                    this@DLRTCStartManager.currentId = TUILogin.getLoginUser()
                }
                ///查看邀请的人是不是我，不是我的话，直接就去掉
                if (acceptUserId == this@DLRTCStartManager.currentId) {
                    val _inviteUserId = params[DLRTCDataMessageType.DLRTCInviteUserID] as? String
                    _inviteUserId?.apply {
                        this@DLRTCStartManager.acceptUserId = acceptUserId
                        val model = DLRTCStartModel()
                        model.rtcDataMessageType = DLRTCDataMessageType.invite
                        model.inviteUserId = _inviteUserId
                        model.acceptUserId = acceptUserId
                        model.inviteId = inviteID
                        val rtcType = params[DLRTCDataMessageType.DLRTCInviteType] as? String
                        rtcType?.apply {
                            model.rtcInviteType = this
                            this@DLRTCStartManager.inviteTypeMsg = this
                        }
                        val rtcRoomId = getParamsRoomId(params[DLRTCDataMessageType.DLRTCInviteRoomID])
                        model.rtcInviteRoomId = rtcRoomId
                        this@DLRTCStartManager.inviteRTCRoomId = rtcRoomId
                        //拓展字段 = dl_rtc_invite_ext_json
                        val inviteExtJson = params[DLRTCDataMessageType.inviteExtJson] as? String
                        inviteExtJson?.apply {
                            model.inviteExtJson = this
                            MPTimber.tag(TAGLOG).d("当前拓展字段：inviteExtJson： $inviteExtJson")
                        }
                        for (delegate in delegates) {
                            delegate.RTCStartManagerReciveMsg(this@DLRTCStartManager, model)
                        }
                        ///添加log打点
                        val logParams = buildRTCLogParams(currentId, inviter)
                        //addRtcLog(ct: receiveNewInviteCTLog, dt: "receive",otherParams: logParams)
                    }
                }
            }
        }

        /**
         * 接听信令消息
         */
        override fun onInviteeAccepted(inviteID: String, invitee: String, data: String) {
            MPTimber.tag(TAGLOG).d("onInviteeAccepted inviteID:$inviteID, invitee:$invitee data:$data")
            if (inviteID != this@DLRTCStartManager.inviteId || invitee != this@DLRTCStartManager.acceptUserId){
                return
            }
            if (this@DLRTCStartManager.isReceiveNewInvite && !this@DLRTCStartManager.isBeginInvite) {
                return
            }
            val dataResult = callBackDataValid(data, DLRTCDataMessageType.accept)
            dataResult?.apply{
                val params = this
                val rtcRoomId = getParamsRoomId(params[DLRTCDataMessageType.DLRTCInviteRoomID])
                val rtcInviteId = params[DLRTCDataMessageType.DLRTCInviteID] as? String
                if(rtcRoomId == inviteRTCRoomId && rtcInviteId == inviteID){
                    val mModel = buildRTCModel(DLRTCDataMessageType.accept, rtcInviteId, acceptUserId,inviteUserId)
                    mModel.rtcInviteRoomId = rtcRoomId
                    mModel.rtcInviteType = this@DLRTCStartManager.inviteTypeMsg
                    for (delegate in delegates) {
                        delegate.RTCStartManagerReciveMsg(this@DLRTCStartManager, mModel)
                    }
                    ///添加log打点
//                    let logParams = buildRTCLogParams(invitee, currentId)
//                    addRtcLog(ct: receiveAcceptInviteCTLog, dt: "receive",otherParams: logParams)
                }
            }

        }

        /**
         * 拒接信令消息
         */
        override fun onInviteeRejected(inviteID: String, invitee: String, data: String) {
            MPTimber.tag(TAGLOG).d("onInviteeRejected inviteID:$inviteID, invitee:$invitee data:$data")
            if (inviteID != this@DLRTCStartManager.inviteId || invitee != this@DLRTCStartManager.acceptUserId) {
                return
            }
            if (this@DLRTCStartManager.isReceiveNewInvite && !this@DLRTCStartManager.isBeginInvite) {
                return
            }
            val dataResult = callBackDataValid(data, DLRTCDataMessageType.reject)
            dataResult?.apply {
                val params = this
                val rtcRoomId = getParamsRoomId(params[DLRTCDataMessageType.DLRTCInviteRoomID])
                val rtcInviteId = params[DLRTCDataMessageType.DLRTCInviteID] as? String
                if(rtcRoomId == this@DLRTCStartManager.inviteRTCRoomId && rtcInviteId == inviteID){
                    val mModel = buildRTCModel(DLRTCDataMessageType.reject, rtcInviteId, this@DLRTCStartManager.acceptUserId,this@DLRTCStartManager.inviteUserId)
                    mModel.rtcInviteRoomId = rtcRoomId
                    mModel.rtcInviteType = this@DLRTCStartManager.inviteTypeMsg
                    for (delegate in delegates) {
                        delegate.RTCStartManagerReciveMsg(this@DLRTCStartManager, mModel)
                    }
                    ///添加log打点
//                    let logParams = buildRTCLogParams(currentId, invitee)
//                    addRtcLog(ct: receiveRejectCTLog, dt: "receive",otherParams: logParams)
                    initParams()
                }
            }
        }

        /**
         * 取消信令消息
         */
        override fun onInvitationCancelled(inviteID: String, inviter: String, data: String) {
            MPTimber.tag(TAGLOG).d("onInvitationCancelled inviteID:$inviteID inviter:$inviter data:$data")
            if (inviteID != this@DLRTCStartManager.inviteId || inviter != this@DLRTCStartManager.inviteUserId) {
                return
            }
            if (this@DLRTCStartManager.isReceiveNewInvite && this@DLRTCStartManager.isBeginInvite) {
                return
            }

            val dataResult = callBackDataValid(data, DLRTCDataMessageType.cancel)
            dataResult?.apply {
                val params = this
                val rtcRoomId = getParamsRoomId(params[DLRTCDataMessageType.DLRTCInviteRoomID])
                val rtcInviteId = params[DLRTCDataMessageType.DLRTCInviteID] as? String
                if(rtcRoomId == this@DLRTCStartManager.inviteRTCRoomId && rtcInviteId == inviteID){
                    val mModel = buildRTCModel(DLRTCDataMessageType.cancel, rtcInviteId, this@DLRTCStartManager.acceptUserId,this@DLRTCStartManager.inviteUserId)
                    mModel.rtcInviteRoomId = rtcRoomId
                    mModel.rtcInviteType = this@DLRTCStartManager.inviteTypeMsg
                    for (delegate in delegates) {
                        delegate.RTCStartManagerReciveMsg(this@DLRTCStartManager, mModel)
                    }
                    ///添加log打点
//                    let logParams = buildRTCLogParams(inviter, currentId)
//                    addRtcLog(ct: receiveCancelInviteCTLog, dt: "receive",otherParams: logParams)
                    initParams()
                }
            }
        }

        //C2C多人通话超时逻辑:
        override fun onInvitationTimeout(inviteID: String, inviteeList: List<String>) {
            MPTimber.tag(TAGLOG).d("onInvitationTimeout inviteID : $inviteID , inviteId : $inviteId ,inviteeList: $inviteeList")
            if (inviteID != this@DLRTCStartManager.inviteId || (!this@DLRTCStartManager.isBeginInvite && !this@DLRTCStartManager.isReceiveNewInvite) ){
                return
            }
            val params = buildRTCLogParams(this@DLRTCStartManager.acceptUserId,this@DLRTCStartManager.currentId)
           // addRtcLog(ct: inviteTimeoutCTLog, dt: "inviteError", otherParams: params)

            val model = buildRTCModel(DLRTCDataMessageType.timeout, inviteID)
            model.rtcInviteRoomId = inviteRTCRoomId
            model.rtcInviteType = inviteTypeMsg
            for (delegate in delegates) {
                delegate.RTCStartManagerReciveMsg(this@DLRTCStartManager, model)
            }
            initParams()
        }
    }

    /**
     * 判断应用后台
     */
    private fun isAppRunningForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcessInfos = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName
        for (appProcessInfo in runningAppProcessInfos) {
            if (appProcessInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && appProcessInfo.processName == packageName
            ) {
                return true
            }
        }
        return false
    }

    /**
     * trtc 退房
     */
    fun exitRoom() {
        mTRTCCloud?.apply {
            stopLocalPreview()
            stopLocalAudio()
            exitRoom()
        }
    }

    /**
     * @return void
     * @Desc TODO(是否开启自动增益补偿功能, 可以自动调麦克风的收音量到一定的音量水平)
     * @author 彭石林
     * @parame [enable]
     * @Date 2022/2/14
     */
    fun enableAGC(enable: Boolean) {
        try {
            val jsonObject = JSONObject().apply {
                put("api", "enableAudioAGC")
                val params = JSONObject().apply {
                    put("enable", if (enable) 1 else 0)
                    put("level", "100") //支持的取值有: 0、30、60、100，0 表示关闭 AGC
                }
                put("params", params)
            }
            mTRTCCloud?.callExperimentalAPI(jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    /**
     * @return void
     * @Desc TODO(回声消除器 ， 可以消除各种延迟的回声)
     * @author 彭石林
     * @parame [enable]
     * @Date 2022/2/14
     */
    fun enableAEC(enable: Boolean) {
        try {
            val jsonObject = JSONObject().apply {
                put("api", "enableAudioAEC")
                val params = JSONObject().apply {
                    put("enable", if (enable) 1 else 0)
                    put("level", "100") //支持的取值有: 0、30、60、100，0 表示关闭 AEC
                }
                put("params", params)
            }
            mTRTCCloud?.callExperimentalAPI(jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    /**
     * @return void
     * @Desc TODO(背景噪音抑制功能 ， 可探测出背景固定频率的杂音并消除背景噪音)
     * @author 彭石林
     * @parame [enable]
     * @Date 2022/2/14
     */
    fun enableANS(enable: Boolean) {
        try {
            val jsonObject = JSONObject().apply {
                put("api", "enableAudioANS")
                val params = JSONObject().apply {
                    put("enable", if (enable) 1 else 0)
                    put("level", "100") //支持的取值有: 0、30、60、100，0 表示关闭 ANS
                }
                put("params", params)
            }
            mTRTCCloud?.callExperimentalAPI(jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun muteLocalAudio(enable : Boolean){
        mTRTCCloud?.muteLocalAudio(enable)
    }

    fun audioRoute(enable : Boolean){
        mTRTCCloud?.setAudioRoute(if(enable)TRTC_AUDIO_ROUTE_SPEAKER else TRTC_AUDIO_ROUTE_EARPIECE)
    }

    fun setFramework() {
        try {
            val jsonObject = JSONObject().apply {
                put("api", "setFramework")
                val params = JSONObject().apply {
                    put("framework", 1)
                    put("component", 3)
                }
                put("params", params)
            }
            mTRTCCloud?.callExperimentalAPI(jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

/// 构建一个需要发送的消息
    /// - Parameter rtcMessage: 当前最新的消息类型
    /// - Returns: 返回字典
    private fun buildRTCParams(rtcMessage : String,_acceptUserId : String? = null,_inviteUserId : String? = null): Hashtable<String, Any> {
        val params = Hashtable<String, Any>()
        params[DLRTCDataMessageType.DLRTCMessageType] = rtcMessage
        params[DLRTCDataMessageType.DLRTCVersionTag] = DLRTCDataMessageType.DLRTCNewTag
        inviteId.isNotEmpty().apply {
            params[DLRTCDataMessageType.DLRTCInviteID] = inviteId
        }

        params[DLRTCDataMessageType.DLRTCInviteRoomID] = inviteRTCRoomId
        inviteTypeMsg.isNotEmpty().apply {
            params[DLRTCDataMessageType.DLRTCInviteType] = inviteTypeMsg
        }
        _acceptUserId?.isNotEmpty().apply {
            params[DLRTCDataMessageType.DLRTCAcceptUserID] = acceptUserId
        }
        _inviteUserId?.isNotEmpty().apply {
            params[DLRTCDataMessageType.DLRTCInviteUserID] = inviteUserId
        }

        return params
    }

    private fun buildRTCLogParams(acceptUserId : String? = null,inviteUserId : String? = null): Hashtable<String, Any> {
        val logParams = Hashtable<String, Any>()
//        logParams[inviteRTCRoomIdLogKey] = inviteRTCRoomId
//        if !inviteTypeMsg.isEmpty {
//            logParams[inviteRTCRoomTypeLogKey] = inviteTypeMsg
//        }
//        if !inviteId.isEmpty {
//            logParams[inviteIdLogKey] = inviteId
//        }
//        if !(acceptUserId?.isEmpty ?? true) {
//            logParams[DLRTCAcceptUserID] = acceptUserId!
//        }
//        if !(inviteUserId?.isEmpty ?? true) {
//            logParams[DLRTCInviteUserID] = inviteUserId!
//        }
        return logParams
    }

    private fun buildRTCModel(rtcDataMessageType : String,inviteId : String? = null,acceptUserId : String? = null,inviteUserId : String? = null): DLRTCStartModel {
        val model = DLRTCStartModel()
        model.rtcDataMessageType = rtcDataMessageType
        inviteId?.apply {
            model.inviteId = this
        }
        acceptUserId?.apply {
            model.acceptUserId = this
        }
        inviteUserId?.apply {
            model.inviteUserId = this
        }
        return model
    }

    private fun convertDataToDict(dataString : String?): Hashtable<String, Any> {
        dataString?.apply {
            return gsonBuilder.create().fromJson(dataString,Hashtable::class.java) as Hashtable<String, Any>
        }
        return Hashtable<String, Any>()
    }

    /// 判定IM服务器返回的数据完整性
    /// - Parameters:
    ///   - data: data
    ///   - rtcMsgType: 邀请的类型
    /// - Returns: 是否合法的数据
    private fun callBackDataValid(dataString : String?,rtcMsgType : String) :  Hashtable<String, Any>? {
        dataString?.apply {
            val params = convertDataToDict(this)
            if (params.keys.contains(DLRTCDataMessageType.DLRTCVersionTag)) {
                val messageType = params[DLRTCDataMessageType.DLRTCMessageType]
                if(messageType is String && messageType == rtcMsgType){
                    return params
                }
            }
        }
        return null
    }

    //转换出roomId
    fun getParamsRoomId(roomId: Any?): Int{
        roomId?.apply {
            var rtcRoomId = 0
            if(this is Double){
                rtcRoomId = this.toInt()
            }else if(this is String){
                rtcRoomId = this.toInt()
            }else if(this is Int){
                rtcRoomId = this.toInt()
            }else if(this is Long){
                rtcRoomId = this.toInt()
            }
            return rtcRoomId
        }
        return 0
    }

}

interface DLRTCModuleClosuer{
    fun callback(_success : Boolean,_errorCode : Int,_errorMsg : String?)
}

interface DLRTCStartManagerDelegate{
    /// 当前RTC收到的信息
    /// - Parameters:
    ///   - manager: 当前manager
    ///   - rtcModel: 当前的RTC模型
    ///   通过rtcModel的rtcDataMessageType的类型判断消息的类型，具体的定义详细在DLRTCDataMessageType
    fun RTCStartManagerReciveMsg(manager : DLRTCStartManager, rtcModel : DLRTCStartModel)
}