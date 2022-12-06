package com.dl.rtc.calling

import android.animation.ValueAnimator
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import com.dl.lib.util.log.MPTimber
import com.dl.rtc.calling.ui.BaseDLRTCCallView
import com.tencent.qcloud.tuicore.Status

/**
 *Author: 彭石林
 *Time: 2022/11/5 18:14
 * Description: This is DLRTCFloatWindowService
 */
class DLRTCFloatWindowService : Service() {

    companion object{
        private val TAG_LOG = "DLRTCFloatWindowService"
        var mStartIntent: Intent? = null
        var mContext: Context? = null

        var mWindowManager: WindowManager? = null
        var mWindowLayoutParams: WindowManager.LayoutParams? = null
        var mCallView: BaseDLRTCCallView? = null
        //屏幕宽度
        private var mScreenWidth = 0
        ////悬浮窗宽度
        private var mWidth = 0

        @JvmStatic
        fun startFloatService(context: Context, callView: BaseDLRTCCallView) {
            MPTimber.tag(TAG_LOG).i( "startFloatService")
            mContext = context
            mCallView = callView
            mStartIntent = Intent(context, DLRTCFloatWindowService::class.java)
            context.startService(mStartIntent)
        }
        @JvmStatic
        fun stopService(context: Context) {
            MPTimber.tag(TAG_LOG).i("stopService: startIntent = $mStartIntent")
            if (null != mStartIntent) {
                context.stopService(mStartIntent)
            }
        }
    }



    override fun onCreate() {
        super.onCreate()
        initWindow()
    }

    override fun onBind(intent: Intent?): IBinder {
        return FloatBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Status.mIsShowFloatWindow = false
        MPTimber.tag(TAG_LOG).i("onDestroy: mCallView = $mCallView")
        if (null != mCallView) {
            // 移除悬浮窗口
            mWindowManager!!.removeView(mCallView)
            mCallView = null
        }
    }

    /**
     * 设置悬浮窗基本参数（位置、宽高等）
     */
    private fun initWindow() {
        mWindowManager = applicationContext.getSystemService(WINDOW_SERVICE) as WindowManager
        //屏幕宽度
        mScreenWidth = mWindowManager!!.getDefaultDisplay().width
        //设置好悬浮窗的参数
        mWindowLayoutParams = getWindowParams()
        // 添加悬浮窗的视图
        MPTimber.tag(TAG_LOG).i("initWindow: mCallView = $mCallView")
        if (null != mCallView) {
//            mCallView.setBackgroundResource(R.drawable.trtccalling_bg_floatwindow_left);
            mWindowManager!!.addView(mCallView, mWindowLayoutParams)
            mCallView!!.setOnTouchListener(FloatingListener())
        }
    }

    private fun getWindowParams(): WindowManager.LayoutParams {
        mWindowLayoutParams = WindowManager.LayoutParams()
        mWindowLayoutParams?.apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            flags = flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
            flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
            // 悬浮窗默认显示以左上角为起始坐标
            gravity = Gravity.START or Gravity.TOP
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            x = mScreenWidth - mCallView!!.measuredWidth
            y = mWindowManager!!.defaultDisplay.height / 2
            //设置悬浮窗宽高
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            format = PixelFormat.TRANSPARENT
        }
        //悬浮窗的开始位置，设置从左上角开始，所以屏幕左上角是x=0,y=0.
        mCallView!!.measure(0, 0)
        return mWindowLayoutParams as WindowManager.LayoutParams
    }

    //======================================悬浮窗Touch和贴边事件=============================//

    class FloatBinder : Binder() {
        val service: FloatBinder
            get() = this
    }

    private class FloatingListener : OnTouchListener {
        //开始触控的坐标，移动时的坐标（相对于屏幕左上角的坐标）
        private var mTouchStartX = 0
        private var mTouchStartY = 0
        private var mTouchCurrentX = 0
        private var mTouchCurrentY = 0

        //开始触控的坐标和结束时的坐标（相对于屏幕左上角的坐标）
        private var mStartX = 0
        private var mStartY = 0
        private var mStopX = 0
        private var mStopY = 0

        //标记悬浮窗是否移动，防止移动后松手触发了点击事件
        private var mIsMove = false
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mIsMove = false
                    mTouchStartX = event.rawX.toInt() //触摸点相对屏幕显示器左上角的坐标
                    mTouchStartY = event.rawY.toInt()
                    //悬浮窗不是全屏的,因此不能用getX()标记开始点,getX()是触摸点相对自身左上角的坐标
                    mStartX = event.rawX.toInt()
                    mStartY = event.rawY.toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    mTouchCurrentX = event.rawX.toInt()
                    mTouchCurrentY = event.rawY.toInt()
                    mWindowLayoutParams?.apply {
                        x += mTouchCurrentX - mTouchStartX
                        y += mTouchCurrentY - mTouchStartY
                    }
                    if (null != mCallView) {
                        mWindowManager?.updateViewLayout(
                            mCallView,
                            mWindowLayoutParams
                        )
                    }
                    mTouchStartX = mTouchCurrentX
                    mTouchStartY = mTouchCurrentY
                }
                MotionEvent.ACTION_UP -> {
                    mStopX = event.rawX.toInt()
                    mStopY = event.rawY.toInt()
                    if (Math.abs(mStartX - mStopX) >= 5 || Math.abs(mStartY - mStopY) >= 5) {
                        mIsMove = true
                        if (null != mCallView) {
                            mWidth = mCallView!!.width
                            //超出一半屏幕右移
                            if (mTouchCurrentX > mScreenWidth / 2) {
                                startScroll(mStopX, mScreenWidth - mWidth, false)
                            } else {
                                startScroll(mStopX, 0, true)
                            }
                        }
                    }
                }
                else -> {}
            }
            //移动事件拦截
            return mIsMove
        }

        //悬浮窗贴边动画
        private fun startScroll(start: Int, end: Int, isLeft: Boolean) {
            mWidth = mCallView!!.width
            calculateHeight()
            val valueAnimator =
                ValueAnimator.ofFloat((start - mWidth / 2).toFloat(), end.toFloat()).setDuration(300)
            valueAnimator.addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                mWindowLayoutParams!!.x = animatedValue.toInt()
                if (mCallView != null) {
                    mWindowManager!!.updateViewLayout(mCallView, mWindowLayoutParams)
                }
            }
            valueAnimator.start()
        }
        //计算高度,防止悬浮窗上下越界
        private fun calculateHeight() {
            val height: Int = mCallView!!.height
            val screenHeight = mWindowManager!!.defaultDisplay.height
            //获取系统状态栏的高度
            val resourceId: Int = mContext!!.resources!!.getIdentifier("status_bar_height", "dimen", "android")
            val statusBarHeight: Int = mContext!!.resources.getDimensionPixelSize(resourceId)
            if (mWindowLayoutParams!!.y < 0) {
                mWindowLayoutParams!!.y = 0
            } else if (mWindowLayoutParams!!.y > screenHeight - height - statusBarHeight) {
                mWindowLayoutParams!!.y = screenHeight - height - statusBarHeight
            }
        }
    }

}