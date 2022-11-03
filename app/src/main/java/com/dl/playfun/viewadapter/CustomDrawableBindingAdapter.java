package com.dl.playfun.viewadapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import androidx.databinding.BindingAdapter;

public class CustomDrawableBindingAdapter {
    /**
     * requireAll 是意思是是否需要绑定全部参数, false为否
     */

    /**
     * 自定义drawable shape实现
     */
    @BindingAdapter(value = {
            "drawable_color",
            "drawable_cornersRadius",
            "drawable_radius_leftTop",
            "drawable_radius_rightTop",
            "drawable_radius_leftBottom",
            "drawable_radius_rightBottom",
            "drawable_gradient_startColor",
            "drawable_gradient_endColor",
            "drawable_stroke_width",
            "drawable_stroke_color",
            "drawable_alpha",
            "drawable_orientation"
    }, requireAll = false)
    public static void generateDrawable(View view,
                                        Integer drawable_color,
                                        Integer drawable_cornersRadius,
                                        Integer drawable_radius_leftTop,
                                        Integer drawable_radius_rightTop,
                                        Integer drawable_radius_leftBottom,
                                        Integer drawable_radius_rightBottom,
                                        Integer drawable_gradient_startColor,
                                        Integer drawable_gradient_endColor,
                                        Integer drawable_stroke_width,
                                        Integer drawable_stroke_color,
                                        Integer drawable_alpha,
                                        GradientDrawable.Orientation orientation
    ) {
        final Context mContext = view.getContext();
        GradientDrawable roundRect = new GradientDrawable();
        roundRect.setShape(GradientDrawable.RECTANGLE);
        float leftTop = 0;
        if (drawable_radius_leftTop != null) {
            leftTop = dip2px(mContext, drawable_radius_leftTop);
        }

        float rightTop = 0;
        if (drawable_radius_rightTop != null) {
            rightTop = dip2px(mContext, drawable_radius_rightTop);
        }

        float rightBottom = 0;
        if (drawable_radius_rightBottom != null) {
            rightBottom = dip2px(mContext, drawable_radius_rightBottom);
        }

        float leftBottom = 0;
        if (drawable_radius_leftBottom != null) {
            leftBottom = dip2px(mContext, drawable_radius_leftBottom);
        }
        if (drawable_radius_leftTop != null || drawable_radius_rightTop != null || drawable_radius_rightBottom != null || drawable_radius_leftBottom != null) {
            //矩形的圆角弧度 四个圆角的顺序为左上，右上，右下，左下 但是这里需要8个。每次设置两个
            roundRect.setCornerRadii(new float[]{leftTop, leftTop,
                    rightTop, rightTop,
                    rightBottom, rightBottom,
                    leftBottom, leftBottom
            });
        }
        //单独设置矩形的圆角弧度
        if (drawable_cornersRadius != null) {
            roundRect.setCornerRadius(dip2px(mContext, drawable_cornersRadius));
        }
        //设置外框
        if(drawable_stroke_width !=null && drawable_stroke_color!=null){
            roundRect.setStroke((int) dip2px(mContext, drawable_stroke_width),drawable_stroke_color);
        }
        if (drawable_color != null) {
            roundRect.setColor(drawable_color);
        }
        if (drawable_gradient_startColor != null && drawable_gradient_endColor != null) {
            int[] colors = new int[2];
            colors[0] = drawable_gradient_startColor;
            colors[1] = drawable_gradient_endColor;
            roundRect.setColors(colors);
        }
        if(orientation!=null){
            roundRect.setOrientation(orientation);
        }
        //设置透明度
        if(drawable_alpha!=null){
            roundRect.setAlpha(drawable_alpha);
        }
        view.setBackground(roundRect);

    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static float dip2px(Context mContext, float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (dpValue * scale + 0.5f);
    }

}
