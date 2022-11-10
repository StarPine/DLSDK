package com.dl.rtc.calling.ui

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.text.TextUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.blankj.utilcode.util.ToastUtils
import com.dl.lib.util.log.MPTimber
import com.dl.rtc.calling.DLRTCFloatWindowService
import com.dl.rtc.calling.R
import com.dl.rtc.calling.base.DLRTCCalling
import com.dl.rtc.calling.base.DLRTCCallingDelegate
import com.dl.rtc.calling.base.impl.DLRTCInternalListenerManager
import com.dl.rtc.calling.manager.DLRTCCallingInfoManager
import com.dl.rtc.calling.manager.DLRTCStartManager
import com.dl.rtc.calling.model.bean.DLRTCUserModel
import com.tencent.qcloud.tuicore.Status
import com.tencent.qcloud.tuicore.TUIConstants
import com.tencent.qcloud.tuicore.TUICore
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.qcloud.tuicore.interfaces.ITUINotification
import com.tencent.trtc.TRTCCloudDef
import com.tencent.trtc.TRTCCloudDef.TRTCQuality

/**
 *Author: 彭石林
 *Time: 2022/11/5 12:08
 * Description: This is BaseDLRTCCallView
 */
abstract class BaseDLRTCCallView constructor(
    context: Context
):FrameLayout(context), DLRTCCallingDelegate {

    private val TAG_LOG = "BaseTUICallView"

    private val MIN_DURATION_SHOW_LOW_QUALITY = 5000 //显示网络不佳最小间隔时间

    //音视频通话基础信息
     var mContext: Context? = null

    init {
        mContext = context
    }

    lateinit var mSelfModel: DLRTCUserModel
     lateinit var mRole: DLRTCCalling.Role
     var mCallType: DLRTCCalling.Type? = null
    lateinit var mUserIDs: Array<String>
    lateinit var mSponsorID :String

     var mGroupID: String? = null
     var mIsFromGroup = false
     var mMainHandler = Handler(Looper.getMainLooper())

    private var mSelfLowQualityTime: Long = 0
    private var mOtherPartyLowQualityTime: Long = 0

    //通话时长相关
    var mTimeRunnable: Runnable? = null
    var mTimeCount  = 0
    var mTimeHandler: Handler? = null
    var mTimeHandlerThread: HandlerThread? = null

    //音视频通用字段
    protected var mCallUserInfoList = ArrayList<DLRTCUserModel>() // 主叫方保存的被叫信息

    protected var mCallUserModelMap = HashMap<String, DLRTCUserModel>()
    // 被叫方保存的主叫方的信息
    protected var mSponsorUserInfo: DLRTCUserModel? = null
    protected var mOtherInviteeList = ArrayList<DLRTCUserModel>() // 被叫方保存的其他被叫的信息

    protected var mIsHandsFree = true // 默认开启扬声器

    protected var mIsMuteMic = false
    protected var mIsInRoom = false // 被叫是否已接听进房(true:已接听进房 false:未接听)

    // 被删除的用户(该用户拒接,无响应或者超时了)
    private var mRemovedUserModel: DLRTCUserModel? = null

    //视频相关字段
    protected var mIsCalling = false // 正在通话中



    //公共视图
    // 返回按钮,展示悬浮窗
    private var mImageBack: ImageView? = null

    constructor (context: Context, role: DLRTCCalling.Role, type: DLRTCCalling.Type, userIDs: Array<String>, sponsorID: String, groupID: String?, isFromGroup: Boolean) : this(context){
        mContext = context
        mSelfModel = DLRTCUserModel()
        mSelfModel.userId = TUILogin.getUserId()
        mSelfModel.userName = TUILogin.getLoginUser()
        mRole = role
        mCallType = type
        mUserIDs = userIDs
        mSponsorID = sponsorID
        mGroupID = groupID
        mIsFromGroup = isFromGroup
        initTimeHandler()
        initView()
        initData()
        initListener()
    }

//    }

    //用户是否支持显示悬浮窗:
    open fun enableFloatWindow(enable: Boolean) {
        mImageBack!!.visibility = if (enable) VISIBLE else GONE
    }

    private fun initTimeHandler() {
        // 初始化计时线程
        mTimeHandlerThread = HandlerThread("time-count-thread")
        mTimeHandlerThread!!.start()
        mTimeHandler = Handler(mTimeHandlerThread!!.looper)
    }

    protected open fun runOnUiThread(task: Runnable?) {
        if (null != task) {
            mMainHandler.post(task)
        }
    }

    private fun initData() {
        if (mRole == DLRTCCalling.Role.CALLED) {
            // 被叫方
            if (!TextUtils.isEmpty(mSponsorID)) {
                mSponsorUserInfo = DLRTCUserModel()
                mSponsorUserInfo!!.userId = mSponsorID
            }
            if (null != mUserIDs) {
                for (userId in mUserIDs) {
                    val userModel = DLRTCUserModel()
                    userModel.userId = userId
                    mOtherInviteeList.add(userModel)
                    mCallUserModelMap.put(userModel.userId!!, userModel)
                }
            }
        } else {
            // 主叫方
            if (null != mSelfModel) {
                for (userId in mUserIDs) {
                    val userModel = DLRTCUserModel()
                    userModel.userId = userId
                    mCallUserInfoList.add(userModel)
                    mCallUserModelMap.put(userModel.userId!!, userModel)
                }
            }
        }
    }

    protected open fun timeCountListener(type: Int) {

    }

    //判断是否是群聊,群聊有两种情况:
    //1.引入群组概念,多人加入群组,主叫是群主 ---IM即时通信使用该方法
    //2.主叫同时向多个用户发起单聊,本质还是C2C单人通话 ----组件多人通话使用该方法
    protected open fun isGroupCall(): Boolean {
        if (!TextUtils.isEmpty(mGroupID)) {
            return true
        }
        return if (DLRTCCalling.Role.CALL == mRole) {
            mUserIDs.size >= 2
        } else {
            mUserIDs.size >= 1 || mIsFromGroup
        }
    }

    protected open fun getRemovedUserModel(): DLRTCUserModel? {
        return mRemovedUserModel
    }

    protected open fun setImageBackView(imageView: ImageView) {
        mImageBack = imageView
    }

    protected open fun getImageBackView(): ImageView? {
        return mImageBack
    }

    protected open fun getCallType(): DLRTCCalling.Type? {
        return mCallType
    }

     protected abstract fun initView()

    //主叫端:展示邀请列表
    protected open fun showInvitingView() {
        Status.mCallStatus = Status.CALL_STATUS.WAITING
    }

    //主叫端/被叫端: 展示通话中的界面
    protected open fun showCallingView() {
        Status.mCallStatus = Status.CALL_STATUS.ACCEPT
    }

    //被叫端: 等待接听界面
    protected open fun showWaitingResponseView() {
        Status.mCallStatus = Status.CALL_STATUS.WAITING
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        DLRTCInternalListenerManager.instance.addDelegate(this)
        //开启界面后,清除通知栏消息
        val notificationManager =
            mContext!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    override fun onDetachedFromWindow() {
        MPTimber.tag(TAG_LOG).d("==== onDetachedFromWindow ====")
        super.onDetachedFromWindow()
        stopTimeCount()
        DLRTCInternalListenerManager.instance.removeDelegate(this)
        if (Status.mIsShowFloatWindow) {
            mContext?.let { DLRTCFloatWindowService.stopService(it) }
        }
    }

    protected open fun finish() {
        mOtherInviteeList.clear()
        mCallUserInfoList.clear()
        mCallUserModelMap.clear()
        mIsInRoom = false
        mIsCalling = false
        Status.mCallStatus = Status.CALL_STATUS.NONE
    }

    override fun onGroupCallInviteeListUpdate(userIdList: List<String?>?) {}

    override fun onInvited(
        sponsor: String?,
        userIdList: List<String?>?,
        isFromGroup: Boolean,
        callType: Int
    ) {
    }

    override fun onError(code: Int, msg: String?) {
        //发生了错误，报错并退出该页面
        ToastUtils.showLong(
            mContext!!.getString(
                R.string.trtccalling_toast_call_error_msg,
                code,
                msg
            )
        )
        finish()
    }

    override fun onUserEnter(userId: String?) {
        Status.mCallStatus = Status.CALL_STATUS.ACCEPT
    }

    override fun onUserLeave(userId: String?) {
        //删除用户model
        val userInfo: DLRTCUserModel? = mCallUserModelMap.remove(userId)
        if (userInfo != null) {
            mCallUserInfoList.remove(userInfo)
        }
        //有用户退出时,需提示"**结束通话";
        if (null != userInfo && !TextUtils.isEmpty(userInfo.userName)) {
            ToastUtils.showLong(
                mContext!!.getString(
                    R.string.trtccalling_toast_user_end,
                    userInfo.userName
                )
            )
        } else {
            showUserToast(userId, R.string.trtccalling_toast_user_end)
        }
    }

    override fun onReject(userId: String?) {
        //删除用户model
        val userInfo: DLRTCUserModel? = mCallUserModelMap.remove(userId)
        if (userInfo != null) {
            mCallUserInfoList.remove(userInfo)
            mRemovedUserModel = userInfo
        }
        //用户拒接时,需提示"**拒绝通话"
        if (null != userInfo && !TextUtils.isEmpty(userInfo.userName)) {
            ToastUtils.showLong(
                mContext!!.getString(
                    R.string.trtccalling_toast_user_reject_call,
                    userInfo.userName
                )
            )
        } else {
            showUserToast(userId, R.string.trtccalling_toast_user_reject_call)
        }
    }

    override fun onNoResp(userId: String?) {
        //删除用户model
        val userInfo: DLRTCUserModel? = mCallUserModelMap.remove(userId)
        if (userInfo != null) {
            mCallUserInfoList.remove(userInfo)
            mRemovedUserModel = userInfo
        }
        //用户无响应时,需提示"**无响应"
        if (null != userInfo && !TextUtils.isEmpty(userInfo.userName)) {
            ToastUtils.showLong(
                mContext!!.getString(
                    R.string.trtccalling_toast_user_not_response,
                    userInfo.userName
                )
            )
        } else {
            showUserToast(userId, R.string.trtccalling_toast_user_not_response)
        }
    }

    override fun onLineBusy(userId: String?) {
        //删除用户model
        val userInfo: DLRTCUserModel? = mCallUserModelMap.remove(userId)
        if (userInfo != null) {
            mCallUserInfoList.remove(userInfo)
            mRemovedUserModel = userInfo
        }
        //用户忙线时,需提示"**忙线"
        if (null != userInfo && !TextUtils.isEmpty(userInfo.userName)) {
            ToastUtils.showLong(
                mContext!!.getString(
                    R.string.trtccalling_toast_user_busy,
                    userInfo.userName
                )
            )
        } else {
            showUserToast(userId, R.string.trtccalling_toast_user_busy)
        }
    }

    override fun onCallingCancel() {
        //主叫取消了通话,被叫提示"主叫取消通话"
        if (mSponsorUserInfo != null) {
            ToastUtils.showLong(
                mContext!!.getString(
                    R.string.trtccalling_toast_user_cancel_call,
                    mSponsorUserInfo!!.userName
                )
            )
        }
        finish()
    }

    override fun onCallingTimeout() {
        //被叫超时,主叫/被叫都提示"通话超时",群聊不提示.
        if (!isGroupCall()) {
            ToastUtils.showLong(mContext!!.getString(R.string.trtccalling_toast_user_timeout, ""))
        }
        finish()
    }

    override fun onCallEnd() {
        //通话结束退房,被叫提示"主叫结束通话"
        if (mSponsorUserInfo != null) {
            ToastUtils.showLong(
                mContext!!.getString(
                    R.string.trtccalling_toast_user_end,
                    mSponsorUserInfo!!.userName
                )
            )
        }
        finish()
    }

    override fun onUserVoiceVolume(volumeMap: Map<String?, Int?>?) {}


    override fun onUserVideoAvailable(userId: String?, isVideoAvailable: Boolean) {}

    override fun onUserAudioAvailable(userId: String?, isAudioAvailable: Boolean) {}

    override fun onSwitchToAudio(success: Boolean, message: String?) {}

    override fun onTryToReconnect() {}

    //通话时长,注意UI更新需要在主线程中进行
    protected open fun showTimeCount(view: TextView?) {
        if (mTimeRunnable != null) {
            return
        }
        mTimeCount = Status.mBeginTime
        if (null != view) {
            view.text = getShowTime(mTimeCount)
        }
        mTimeRunnable = Runnable {
            mTimeCount++
            timeCountListener(mTimeCount)
            Status.mBeginTime = mTimeCount
            if (null != view) {
                runOnUiThread {
                    if (!isDestroyed()) {
                        view.text = getShowTime(mTimeCount)
                    }
                }
            }
            mTimeHandler!!.postDelayed(mTimeRunnable!!, 1000)
        }
        mTimeHandler!!.postDelayed(mTimeRunnable!!, 1000)
    }

    protected open fun getShowTime(count: Int): String? {
        return mContext!!.getString(R.string.trtccalling_called_time_format, count / 60, count % 60)
    }

    private fun stopTimeCount() {
        mTimeRunnable?.let { mTimeHandler?.removeCallbacks(it) }
        mTimeRunnable = null
        mTimeHandlerThread?.quit()
        mTimeCount = 0
    }

    //localQuality 己方网络状态， remoteQualityList对方网络状态列表，取第一个为1v1通话的网络状态
    protected open fun updateNetworkQuality(
        localQuality: TRTCQuality?,
        remoteQualityList: List<TRTCQuality?>
    ) {
        //如果己方网络和对方网络都很差，优先显示己方网络差
        val isLocalLowQuality = isLowQuality(localQuality)
        if (isLocalLowQuality) {
            updateLowQualityTip(true)
        } else {
            if (!remoteQualityList.isEmpty()) {
                val remoteQuality = remoteQualityList[0]
                if (isLowQuality(remoteQuality)) {
                    updateLowQualityTip(false)
                }
            }
        }
    }

    private fun isLowQuality(qualityInfo: TRTCQuality?): Boolean {
        if (qualityInfo == null) {
            return false
        }
        val quality = qualityInfo.quality
        val lowQuality: Boolean
        lowQuality =
            when (quality) {
                TRTCCloudDef.TRTC_QUALITY_Vbad, TRTCCloudDef.TRTC_QUALITY_Down -> true
                else -> false
            }
        return lowQuality
    }

    private fun updateLowQualityTip(isSelf: Boolean) {
        val currentTime = System.currentTimeMillis()
        if (isSelf) {
            if (currentTime - mSelfLowQualityTime > MIN_DURATION_SHOW_LOW_QUALITY) {
                Toast.makeText(
                    mContext,
                    R.string.trtccalling_self_network_low_quality,
                    Toast.LENGTH_SHORT
                ).show()
                mSelfLowQualityTime = currentTime
            }
        } else {
            if (currentTime - mOtherPartyLowQualityTime > MIN_DURATION_SHOW_LOW_QUALITY) {
                Toast.makeText(
                    mContext, R.string.trtccalling_other_party_network_low_quality,
                    Toast.LENGTH_SHORT
                ).show()
                mOtherPartyLowQualityTime = currentTime
            }
        }
    }

    protected open fun isDestroyed(): Boolean {
        var isDestroyed = false
        if (mContext is Activity && (mContext as Activity).isDestroyed) {
            isDestroyed = true
        }
        return isDestroyed
    }

    private fun initListener() {
        val notification =
            ITUINotification { key, subKey, param ->
                if (TUIConstants.TUILogin.EVENT_LOGIN_STATE_CHANGED == key && TUIConstants.TUILogin.EVENT_SUB_KEY_USER_KICKED_OFFLINE == subKey) {
                    ToastUtils.showShort(mContext!!.getString(R.string.trtccalling_user_kicked_offline))
                    DLRTCStartManager.getInstance().hangup()
                    finish()
                }
                if (TUIConstants.TUILogin.EVENT_LOGIN_STATE_CHANGED == key && TUIConstants.TUILogin.EVENT_SUB_KEY_USER_SIG_EXPIRED == subKey) {
                    ToastUtils.showShort(mContext!!.getString(R.string.trtccalling_user_sig_expired))
                    DLRTCStartManager.getInstance().hangup()
                    finish()
                }
            }
        TUICore.registerEvent(
            TUIConstants.TUILogin.EVENT_LOGIN_STATE_CHANGED,
            TUIConstants.TUILogin.EVENT_SUB_KEY_USER_KICKED_OFFLINE,
            notification
        )
        TUICore.registerEvent(
            TUIConstants.TUILogin.EVENT_LOGIN_STATE_CHANGED,
            TUIConstants.TUILogin.EVENT_SUB_KEY_USER_SIG_EXPIRED,
            notification
        )
    }

    open fun showUserToast(userId: String?, msgId: Int) {
        if (TextUtils.isEmpty(userId)) {
            MPTimber.tag(TAG_LOG).d("showUserToast userId is empty")
            return
        }
        DLRTCCallingInfoManager.instance.getUserInfoByUserId(userId,
            object : DLRTCCallingInfoManager.Companion.UserCallback {
                override fun onSuccess(model: DLRTCUserModel?) {
                    if (null == model || TextUtils.isEmpty(model.userName)) {
                        ToastUtils.showLong(mContext!!.getString(msgId, userId))
                    } else {
                        ToastUtils.showLong(mContext!!.getString(msgId, model.userName))
                    }
                }

                override fun onFailed(code: Int, msg: String?) {
                    ToastUtils.showLong(mContext!!.getString(msgId, userId))
                }
            })
    }
}