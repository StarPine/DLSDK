package com.dl.rtc.calling.ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.dl.rtc.calling.R

/**
 *Author: 彭石林
 *Time: 2022/11/5 14:27
 * Description: This is RoundCornerImageView
 */
open class RoundCornerImageView
    @JvmOverloads constructor(context: Context, attributes: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatImageView(context, attributes, defStyleAttr){
    private var mWidth = 0f
    private var mHeight = 0f
    private val mDefaultRadius = 0
    private var mRadius = 0
    private var mLeftTopRadius = 0
    private var mRightTopRadius = 0
    private var mRightBottomRadius = 0
    private var mLeftBottomRadius = 0
    init {
        if (attributes != null) {
            init(context,attributes)
        }
    }
     fun init(context: Context, attrs: AttributeSet) {
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        // 读取配置
        val array = context.obtainStyledAttributes(attrs, R.styleable.DLRTCCallingRoundCornerImageView)
        mRadius = array.getDimensionPixelOffset(
            R.styleable.DLRTCCallingRoundCornerImageView_radius,
            mDefaultRadius
        )
        mLeftTopRadius = array.getDimensionPixelOffset(
            R.styleable.DLRTCCallingRoundCornerImageView_left_top_radius,
            mDefaultRadius
        )
        mRightTopRadius = array.getDimensionPixelOffset(
            R.styleable.DLRTCCallingRoundCornerImageView_right_top_radius,
            mDefaultRadius
        )
        mRightBottomRadius = array.getDimensionPixelOffset(
            R.styleable.DLRTCCallingRoundCornerImageView_right_bottom_radius,
            mDefaultRadius
        )
        mLeftBottomRadius = array.getDimensionPixelOffset(
            R.styleable.DLRTCCallingRoundCornerImageView_left_bottom_radius,
            mDefaultRadius
        )

        //如果四个角的值没有设置，那么就使用通用的radius的值。
        if (mDefaultRadius == mLeftTopRadius) {
            mLeftTopRadius = mRadius
        }
        if (mDefaultRadius == mRightTopRadius) {
            mRightTopRadius = mRadius
        }
        if (mDefaultRadius == mRightBottomRadius) {
            mRightBottomRadius = mRadius
        }
        if (mDefaultRadius == mLeftBottomRadius) {
            mLeftBottomRadius = mRadius
        }
        array.recycle()
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mWidth = width.toFloat()
        mHeight = height.toFloat()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        // 只有图片的宽高大于设置的圆角距离的时候才进行裁剪
        val maxLeft = Math.max(mLeftTopRadius, mLeftBottomRadius)
        val maxRight = Math.max(mRightTopRadius, mRightBottomRadius)
        val minWidth = maxLeft + maxRight
        val maxTop = Math.max(mLeftTopRadius, mRightTopRadius)
        val maxBottom = Math.max(mLeftBottomRadius, mRightBottomRadius)
        val minHeight = maxTop + maxBottom
        if (mWidth >= minWidth && mHeight > minHeight) {
            val path = Path()
            //四个角：右上，右下，左下，左上
            path.moveTo(mLeftTopRadius.toFloat(), 0f)
            path.lineTo(mWidth - mRightTopRadius, 0f)
            path.quadTo(mWidth, 0f, mWidth, mRightTopRadius.toFloat())
            path.lineTo(mWidth, mHeight - mRightBottomRadius)
            path.quadTo(mWidth, mHeight, mWidth - mRightBottomRadius, mHeight)
            path.lineTo(mLeftBottomRadius.toFloat(), mHeight)
            path.quadTo(0f, mHeight, 0f, mHeight - mLeftBottomRadius)
            path.lineTo(0f, mLeftTopRadius.toFloat())
            path.quadTo(0f, 0f, mLeftTopRadius.toFloat(), 0f)
            canvas.clipPath(path)
        }
        super.onDraw(canvas)
    }
}