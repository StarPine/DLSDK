package com.dl.rtc.calling.ui.videolayout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.dl.rtc.calling.R
import com.dl.rtc.calling.ui.common.RoundCornerImageView
import com.tencent.rtmp.ui.TXCloudVideoView

/**
 *Author: 彭石林
 *Time: 2022/11/5 14:23
 * Description: This is DLRTCVideoLayout
 */
open class DLRTCVideoLayout @JvmOverloads
        constructor(context: Context, attributes: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attributes, defStyleAttr) {
    private var mMoveAble = false
    private var mTCCloudViewTRTC: TXCloudVideoView? = null
    private var mProgressAudio: ProgressBar? = null
    private var mImageHead: RoundCornerImageView? = null
    private var mTextUserName: TextView? = null

    init {
        initView()
        isClickable = true
    }

    open fun getVideoView(): TXCloudVideoView? {
        return mTCCloudViewTRTC
    }

    open fun getHeadImg(): RoundCornerImageView? {
        return mImageHead
    }

    open fun getUserNameTv(): TextView? {
        return mTextUserName
    }

    open fun setVideoAvailable(available: Boolean) {
        if (available) {
            mTCCloudViewTRTC!!.visibility = VISIBLE
        } else {
            mTCCloudViewTRTC!!.visibility = GONE
        }
    }

    open fun setRemoteIconAvailable(available: Boolean) {
        mImageHead!!.visibility = if (available) VISIBLE else GONE
        mTextUserName!!.visibility = if (available) VISIBLE else GONE
    }

    open fun setAudioVolumeProgress(progress: Int) {
        mProgressAudio?.progress = progress
    }

    open fun setAudioVolumeProgressBarVisibility(visibility: Int) {
        mProgressAudio?.visibility = visibility
    }

    private  fun initView() {
        LayoutInflater.from(context)
            .inflate(R.layout.dlrtccalling_videocall_item_user_layout, this, true)
        mTCCloudViewTRTC = findViewById<View>(R.id.trtc_tc_cloud_view) as TXCloudVideoView
        mProgressAudio = findViewById<View>(R.id.progress_bar_audio) as ProgressBar
        mImageHead =
            findViewById<View>(R.id.iv_avatar) as RoundCornerImageView
        mTextUserName = findViewById<View>(R.id.tv_user_name) as TextView
    }

    open fun isMoveAble(): Boolean {
        return mMoveAble
    }

    open fun setMoveAble(enable: Boolean) {
        mMoveAble = enable
    }

    open fun setUserName(userName: String?) {
        mTextUserName!!.text = userName
    }

    open fun setUserNameColor(color: Int) {
        mTextUserName!!.setTextColor(color)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }
}