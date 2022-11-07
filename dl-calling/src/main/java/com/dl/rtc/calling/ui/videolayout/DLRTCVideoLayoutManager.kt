package com.dl.rtc.calling.ui.videolayout

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.dl.rtc.calling.model.bean.DLRTCLayoutEntity
import com.dl.rtc.calling.util.VideoLayoutUtils
import java.util.*

/**
 * Module:   TRTCVideoViewLayout
 * <p>
 * Function: {@link TXCloudVideoView} 的管理类
 * <p>
 * 1.在多人通话中，您的布局可能会比较复杂，Demo 也是如此，因此需要统一的管理类进行管理，这样子有利于写出高可维护的代码
 * <p>
 * 2.Demo 中提供堆叠布局、宫格布局两种展示方式；若您的项目也有相关的 UI 交互，您可以参考实现代码，能够快速集成。
 * <p>
 * 3.堆叠布局：{@link DLRTCVideoLayoutManager#makeFloatLayout()} 思路是初始化一系列的 x、y、padding、margin 组合 LayoutParams 直接对 View 进行定位
 * <p>
 * 4.宫格布局：{@link DLRTCVideoLayoutManager#makeGirdLayout(boolean)} 思路与堆叠布局一致，也是初始化一些列的 LayoutParams 直接对 View 进行定位
 * <p>
 * 5.如何实现管理：
 * A. 使用{@link TRTCLayoutEntity} 实体类，保存 {@link TRTCVideoLayout} 的分配信息，能够与对应的用户绑定起来，方便管理与更新UI
 * B. {@link TRTCVideoLayout} 专注实现业务 UI 相关的，控制逻辑放在此类中
 * <p>
 * 6.布局切换，见 {@link DLRTCVideoLayoutManager#switchMode()}
 * <p>
 * 7.堆叠布局与宫格布局参数，见{@link Utils} 工具类
 */
open class DLRTCVideoLayoutManager
    @JvmOverloads constructor(context: Context, attributes: AttributeSet? = null, defStyleAttr: Int = 0) :
    RelativeLayout(context, attributes, defStyleAttr) {
    private val TAG = "DLRTCVideoLayoutManager"

    companion object{
        val MODE_FLOAT = 1 // 前后堆叠模式

        val MODE_GRID = 2 // 九宫格模式

        val MAX_USER = 9
    }

    private var mFloatParamList: ArrayList<LayoutParams>? = null
    private var mGrid4ParamList: ArrayList<LayoutParams>? = null
    private var mGrid9ParamList: ArrayList<LayoutParams>? = null
    private var mCount = 0
    private var mMode = 0
    private var mSelfUserId: String? = null
    private var mContext: Context? = null
    private var mVideoFactory: VideoLayoutFactory? = null

    /**
     * ===============================View相关===============================
     */
    init{
        mContext = context
    }

    open fun initVideoFactory(factory: VideoLayoutFactory) {
        mVideoFactory = factory
        initView(mContext!!)
    }
    private fun initView(context: Context) {
        Log.i(TAG, "initView: ")
        if (null == mVideoFactory) {
            return
        }
        if (null == mVideoFactory!!.mLayoutEntityList) {
            mVideoFactory!!.mLayoutEntityList = LinkedList()
        }
        // 默认为堆叠模式
        mMode = MODE_FLOAT
        post { makeFloatLayout() }
    }

    open fun setMySelfUserId(userId: String) {
        mSelfUserId = userId
    }

    /**
     * 宫格布局与悬浮布局切换
     *
     * @return
     */
    open fun switchMode(): Int {
        if (mMode == MODE_FLOAT) {
            mMode = MODE_GRID
            makeGirdLayout(true)
        } else {
            mMode = MODE_FLOAT
            makeFloatLayout()
        }
        return mMode
    }

    /**
     * 根据 userId 找到已经分配的 View
     *
     * @param userId
     * @return
     */
    open fun findCloudView(userId: String?): DLRTCVideoLayout? {
        if (userId == null) return null
        for (layoutEntity in mVideoFactory!!.mLayoutEntityList!!) {
            if (layoutEntity.userId == userId) {
                return layoutEntity.layout
            }
        }
        return null
    }

    /**
     * 根据 userId 分配对应的 view
     *
     * @param userId
     * @return
     */
    open fun allocCloudVideoView(userId: String?): DLRTCVideoLayout? {
        if (userId == null) return null
        if (mCount > DLRTCVideoLayoutManager.MAX_USER) {
            return null
        }
        val layoutEntity = DLRTCLayoutEntity()
        layoutEntity.userId = userId
        layoutEntity.layout = DLRTCVideoLayout(mContext!!)
        layoutEntity.layout!!.visibility = (VISIBLE)
        initGestureListener(layoutEntity.layout!!)
        mVideoFactory!!.mLayoutEntityList!!.add(layoutEntity)
        addView(layoutEntity.layout)
        mCount++
        switchModeInternal()
        return layoutEntity.layout
    }

    private fun switchModeInternal() {
        if (mCount == 2) {
            mMode = MODE_FLOAT
            makeFloatLayout()
            return
        }
        if (mCount == 3) {
            mMode = MODE_GRID
            makeGirdLayout(true)
            return
        }
        if (mCount >= 4 && mMode == MODE_GRID) {
            makeGirdLayout(true)
            return
        }
    }

    private fun initGestureListener(layout: DLRTCVideoLayout) {
        val detector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                layout.performClick()
                return false
            }

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (!layout.isMoveAble()) return false
                val params: ViewGroup.LayoutParams = layout.getLayoutParams()
                // 当 TRTCVideoView 的父容器是 RelativeLayout 的时候，可以实现拖动
                if (params is LayoutParams) {
                    val layoutParams = layout.getLayoutParams() as LayoutParams
                    val newX = (layoutParams.leftMargin + (e2.x - e1.x)).toInt()
                    val newY = (layoutParams.topMargin + (e2.y - e1.y)).toInt()
                    if (newX >= 0 && newX <= width - layout.getWidth() && newY >= 0 && newY <= height - layout.getHeight()) {
                        layoutParams.leftMargin = newX
                        layoutParams.topMargin = newY
                        layout.setLayoutParams(layoutParams)
                    }
                }
                return true
            }
        })
        layout.setOnTouchListener(OnTouchListener { v, event -> detector.onTouchEvent(event) })
    }


    /**
     * 根据 userId 和 视频类型，回收对应的 view
     *
     * @param userId
     */
    open fun recyclerCloudViewView(userId: String?) {
        if (userId == null) return
        if (mMode == MODE_FLOAT) {
            val entity: DLRTCLayoutEntity =
                mVideoFactory!!.mLayoutEntityList!![mVideoFactory!!.mLayoutEntityList!!.size - 1]
            // 当前离开的是处于0号位的人，那么需要将我换到这个位置
            if (userId == entity.userId) {
                makeFullVideoView(mSelfUserId!!)
            }
        } else {
        }
        val iterator: MutableIterator<*> = mVideoFactory!!.mLayoutEntityList!!.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next() as DLRTCLayoutEntity
            if (item.userId == userId) {
                removeView(item.layout)
                iterator.remove()
                mCount--
                break
            }
        }
        switchModeInternal()
    }

    /**
     * 隐藏所有音量的进度条
     */
    open fun hideAllAudioVolumeProgressBar() {
        for (entity in mVideoFactory!!.mLayoutEntityList!!) {
            entity.layout!!.setAudioVolumeProgressBarVisibility(GONE)
        }
    }

    /**
     * 显示所有音量的进度条
     */
    open fun showAllAudioVolumeProgressBar() {
        for (entity in mVideoFactory!!.mLayoutEntityList!!) {
            entity.layout!!.setAudioVolumeProgressBarVisibility(VISIBLE)
        }
    }

    /**
     * 设置当前音量
     *
     * @param userId
     * @param audioVolume
     */
    open fun updateAudioVolume(userId: String?, audioVolume: Int) {
        if (userId == null) return
        for (entity in mVideoFactory!!.mLayoutEntityList!!) {
            if (entity.layout!!.visibility == VISIBLE) {
                if (userId == entity.userId) {
                    entity.layout!!.setAudioVolumeProgress(audioVolume)
                }
            }
        }
    }

    private fun findEntity(layout: DLRTCVideoLayout): DLRTCLayoutEntity? {
        for (entity in mVideoFactory!!.mLayoutEntityList!!) {
            if (entity.layout === layout) return entity
        }
        return null
    }

    private fun findEntity(userId: String): DLRTCLayoutEntity? {
        for (entity in mVideoFactory!!.mLayoutEntityList!!) {
            if (entity.userId == userId) return entity
        }
        return null
    }

    /**
     * 切换到九宫格布局
     *
     * @param needUpdate 是否需要更新布局
     */
    private fun makeGirdLayout(needUpdate: Boolean) {
        if (mGrid4ParamList == null || mGrid4ParamList!!.size == 0 || mGrid9ParamList == null || mGrid9ParamList!!.size == 0) {
            mGrid4ParamList =
                VideoLayoutUtils.initGrid4Param(
                    context, width, height
                )
            mGrid9ParamList =
                VideoLayoutUtils.initGrid9Param(
                    context, width, height
                )
        }
        if (needUpdate) {
            val paramList: ArrayList<LayoutParams> = if(mCount <= 4 ){
                mGrid4ParamList!!
            }else{
                mGrid9ParamList!!
            }
            var layoutIndex = 1
            for (i in mVideoFactory!!.mLayoutEntityList!!.indices) {
                val entity: DLRTCLayoutEntity =
                    mVideoFactory!!.mLayoutEntityList!![i]
                entity.layout!!.setMoveAble(false)
                entity.layout!!.setOnClickListener(null)
                // 我自己要放在布局的左上角
                if (entity.userId == mSelfUserId) {
                    entity.layout!!.setLayoutParams(paramList[0])
                } else if (layoutIndex < paramList.size) {
                    entity.layout!!.setLayoutParams(paramList[layoutIndex++])
                }
            }
        }
    }


    /**
     * ===============================九宫格布局相关===============================
     */

    /**
     * ===============================九宫格布局相关===============================
     */
    /**
     * 切换到堆叠布局：
     * 1. 如果堆叠布局参数未初始化先进行初始化：大画面+左右各三个画面
     * 2. 修改布局参数
     */
    private fun makeFloatLayout() {
        // 初始化堆叠布局的参数
        if (mFloatParamList.isNullOrEmpty()) {
            mFloatParamList =
                VideoLayoutUtils.initFloatParamList(
                    context, width, height
                )
        }

        // 根据堆叠布局参数，将每个view放到适当的位置，后加入的放在最大位
        val size = mVideoFactory!!.mLayoutEntityList!!.size
        for (i in 0 until size) {
            val entity = mVideoFactory!!.mLayoutEntityList!![size - i - 1]
            val layoutParams = mFloatParamList!!.get(i)
            entity.layout!!.setLayoutParams(layoutParams)
            entity.layout!!.setMoveAble(i != 0)
            addFloatViewClickListener(entity)
            bringChildToFront(entity.layout)
        }
    }

    /**
     * ===============================堆叠布局相关===============================
     */

    /**
     * ===============================堆叠布局相关===============================
     */
    /**
     * 对堆叠布局情况下的 View 添加监听器
     *
     *
     * 用于点击切换两个 View 的位置
     */
    private fun addFloatViewClickListener(entity: DLRTCLayoutEntity) {
        val userId: String = entity.userId
        entity.layout?.setOnClickListener {
            if (!TextUtils.isEmpty(userId)) {
                makeFullVideoView(userId)
            }
        }
    }

    /**
     * 堆叠模式下，将 userId 的 view 换到 0 号位，全屏化渲染
     *
     * @param userId
     */
    private fun makeFullVideoView(userId: String) {
        Log.i(TAG, "makeFullVideoView: from = $userId")
        val entity = findEntity(userId)
        mVideoFactory!!.mLayoutEntityList!!.remove(entity)
        mVideoFactory!!.mLayoutEntityList!!.addLast(entity)
        makeFloatLayout()
    }

    /**
     * 切换双方视图层级和位置
     */
    open fun switchVideoView() {
        Collections.reverse(mVideoFactory!!.mLayoutEntityList)
        makeFloatLayout()
    }
}