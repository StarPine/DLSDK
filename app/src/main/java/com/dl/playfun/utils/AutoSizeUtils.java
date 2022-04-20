package com.dl.playfun.utils;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: 彭石林
 * Time: 2022/2/19 14:48
 * Description: This is AutoSize
 */
public class AutoSizeUtils {
    public static final int WIDTH_DP = 0x25;
    public static final int WIDTH_PT = 0x35;
    public static final int HEIGHT_PT = 0x45;

    private static final float widthSize = 360f;

    @IntDef({WIDTH_DP, WIDTH_PT, HEIGHT_PT})
    @Retention(RetentionPolicy.CLASS)
    @interface ScreenMode {
    }

    private AutoSizeUtils() {
    }

    private static List<Field> sMetricsFields;
    private static DisplayMetrics systemDm;


    public static Resources applyAdapt(final Resources resources) {
        DisplayMetrics activityDm = resources.getDisplayMetrics();
        if (null == systemDm) {
            systemDm = Resources.getSystem().getDisplayMetrics();
        }
        change(WIDTH_DP, resources, activityDm, systemDm, widthSize);
        //兼容其他手机
        if (sMetricsFields == null) {
            sMetricsFields = new ArrayList<>();
            Class resCls = resources.getClass();
            Field[] declaredFields = resCls.getDeclaredFields();
            while (declaredFields.length > 0) {
                for (Field field : declaredFields) {
                    if (field.getType().isAssignableFrom(DisplayMetrics.class)) {
                        field.setAccessible(true);
                        DisplayMetrics tmpDm = getMetricsFromField(resources, field);
                        if (tmpDm != null) {
                            sMetricsFields.add(field);
                            change(WIDTH_DP, resources, tmpDm, systemDm, widthSize);
                        }
                    }
                }
                resCls = resCls.getSuperclass();
                if (resCls != null) {
                    declaredFields = resCls.getDeclaredFields();
                } else {
                    break;
                }
            }
        } else {
            for (Field metricsField : sMetricsFields) {
                try {
                    DisplayMetrics dm = (DisplayMetrics) metricsField.get(resources);
                    if (dm != null) change(WIDTH_DP, resources, dm, systemDm, widthSize);
                } catch (Exception e) {
//                    Log.e("ScreenHelper", "applyMetricsFields: " + e);
                }
            }
        }
        return resources;
    }


    private static void change(@ScreenMode int screenMode, final Resources resources, DisplayMetrics activityDm, DisplayMetrics systemDm, float size) {
        switch (screenMode) {
            case WIDTH_DP:
                adaptWidthPixels(resources, activityDm, systemDm, size);
                break;
            case HEIGHT_PT:
                adaptHeightXdpi(resources, size, systemDm);
                break;
            case WIDTH_PT:
                adaptWidthXdpi(resources, size, systemDm);
                break;
        }
    }

    private static void adaptWidthPixels(Resources resources, DisplayMetrics activityDm, DisplayMetrics systemDm, float designWidthPixels) {
        //确保设备在横屏和竖屏的显示大小,确保 dp 的大小值
        if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            //横屏以屏幕的宽度来适配，确保 dp 的大小值
            activityDm.density = activityDm.widthPixels * 1.0f / designWidthPixels;
        } else {
            //竖屏以屏幕的高度来适配
            activityDm.density = activityDm.heightPixels * 1.0f / designWidthPixels;
        }
        //确保字体的显示大小
        activityDm.scaledDensity = activityDm.density * (systemDm.scaledDensity / systemDm.density);
        //确保设置的 dpi
        activityDm.densityDpi = (int) (160 * activityDm.density);
    }

    /**
     * Adapt for the horizontal screen, and call it in [android.app.Activity.getResources].
     */
    private static void adaptWidthXdpi(Resources resources, float designWidth, DisplayMetrics systemDm) {
        resources.getDisplayMetrics().xdpi = (systemDm.widthPixels * 72f) / designWidth;
    }

    /**
     * Adapt for the vertical screen, and call it in [android.app.Activity.getResources].
     */
    private static void adaptHeightXdpi(Resources resources, float designHeight, DisplayMetrics systemDm) {
        resources.getDisplayMetrics().xdpi = (systemDm.heightPixels * 72f) / designHeight;
    }

    /**
     * @param resources The resources.
     * @return the resource
     */
    public static Resources closeAdapt(Resources resources) {
        DisplayMetrics activityDm = resources.getDisplayMetrics();
        if (null == systemDm) {
            systemDm = Resources.getSystem().getDisplayMetrics();
        }
        resetResources(activityDm, systemDm);
        //兼容其他手机
        if (sMetricsFields == null) {
            sMetricsFields = new ArrayList<>();
            Class resCls = resources.getClass();
            Field[] declaredFields = resCls.getDeclaredFields();
            while (declaredFields.length > 0) {
                for (Field field : declaredFields) {
                    if (field.getType().isAssignableFrom(DisplayMetrics.class)) {
                        field.setAccessible(true);
                        DisplayMetrics tmpDm = getMetricsFromField(resources, field);
                        if (tmpDm != null) {
                            sMetricsFields.add(field);
                            resetResources(tmpDm, systemDm);
                        }
                    }
                }
                resCls = resCls.getSuperclass();
                if (resCls != null) {
                    declaredFields = resCls.getDeclaredFields();
                } else {
                    break;
                }
            }
        } else {
            for (Field metricsField : sMetricsFields) {
                try {
                    DisplayMetrics dm = (DisplayMetrics) metricsField.get(resources);
                    if (dm != null) resetResources(dm, systemDm);
                } catch (Exception e) {
//                    Log.e("ScreenHelper", "applyMetricsFields: " + e);
                }
            }
        }
        return resources;
    }

    private static void resetResources(DisplayMetrics activityDm, DisplayMetrics systemDm) {
        activityDm.xdpi = systemDm.xdpi;
        activityDm.density = systemDm.density;
        activityDm.scaledDensity = systemDm.scaledDensity; //确保字体的显示大小
        activityDm.densityDpi = systemDm.densityDpi;//确保设置的 dpi
    }


    private static DisplayMetrics getMetricsFromField(final Resources resources, final Field field) {
        try {
            return (DisplayMetrics) field.get(resources);
        } catch (Exception e) {
//            Log.e("ScreenHelper", "getMetricsFromField: " + e);
            return null;
        }
    }
}