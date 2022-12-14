package com.faceunity.nama.repo;

import com.faceunity.core.controller.facebeauty.FaceBeautyParam;
import com.faceunity.core.entity.FUBundleData;
import com.faceunity.core.model.facebeauty.FaceBeauty;
import com.faceunity.core.model.facebeauty.FaceBeautyBlurTypeEnum;
import com.faceunity.core.model.facebeauty.FaceBeautyFilterEnum;
import com.faceunity.nama.R;
import com.faceunity.nama.entity.FaceBeautyBean;
import com.faceunity.nama.entity.FaceBeautyFilterBean;
import com.faceunity.nama.entity.ModelAttributeData;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * DESC：美颜数据构造
 * Created on 2021/3/27
 */
public class FaceBeautySource {

    public static String BUNDLE_FACE_BEAUTIFICATION = "graphics" + File.separator + "face_beautification.bundle";



    /**
     * 获取默认推荐美颜模型
     *
     * @return
     */
    public static FaceBeauty getDefaultFaceBeauty() {
        FaceBeauty recommendFaceBeauty = new FaceBeauty(new FUBundleData(BUNDLE_FACE_BEAUTIFICATION));
        //滤镜名称
        recommendFaceBeauty.setFilterName(FaceBeautyFilterEnum.ZIRAN_4);
        //滤镜强度
        recommendFaceBeauty.setFilterIntensity(0.25);
        /*美肤*/
        recommendFaceBeauty.setBlurType(FaceBeautyBlurTypeEnum.FineSkin);
        //锐化程度 范围 [0-1]
        recommendFaceBeauty.setSharpenIntensity(0.5);
        //美白程度 范围 [0-2]
        recommendFaceBeauty.setColorIntensity(0.7);
        //红润程度 范围 [0-2]
        recommendFaceBeauty.setRedIntensity(0.5);
        //磨皮程度 范围[0-6]
        recommendFaceBeauty.setBlurIntensity(1.8);
        /*美型*/
        //变形程度  范围 [0-1]
        recommendFaceBeauty.setFaceShapeIntensity(1.0);
        //大眼程度 范围 [0-1]
        recommendFaceBeauty.setEyeEnlargingIntensityV2(0.5);
        //V脸程度 范围 [0-1]
        recommendFaceBeauty.setCheekVIntensity(0.5);
        //瘦鼻程度 范围 [0-1]
        recommendFaceBeauty.setNoseIntensityV2(0.5);
        //额头调整程度V2 /范围[0-1]，0-0.5 是变小，0.5-1 是变大
        recommendFaceBeauty.setForHeadIntensityV2(0.2);
        //嘴巴调整程度 [0-1]，0-0.5是变小，0.5-1是变大
        recommendFaceBeauty.setMouthIntensityV2(0.4);
        //下巴调整程度 范围 [0-1]，0-0.5是变小，0.5-1是变大
        recommendFaceBeauty.setChinIntensity(0.15);

        //亮眼 50
        recommendFaceBeauty.setEyeBrightIntensity(0.5);
        //美牙 50
        recommendFaceBeauty.setToothIntensity(0.5);
        //去黑眼圈 100
        recommendFaceBeauty.setRemovePouchIntensity(1);
        //去法令纹 100
        recommendFaceBeauty.setRemoveLawPatternIntensity(1);

        //【美型】
        //窄脸 50
        recommendFaceBeauty.setCheekNarrowIntensity(0.5);
        recommendFaceBeauty.setCheekNarrowIntensityV2(0.5);
        //短脸 10
        recommendFaceBeauty.setCheekShortIntensity(0.1);
        //瘦颧骨 50
        recommendFaceBeauty.setCheekBonesIntensity(0.5);
        //瘦下颌骨 20
        recommendFaceBeauty.setLowerJawIntensity(0.2);
        //大眼 50
        recommendFaceBeauty.setEyeEnlargingIntensity(0.5);
        recommendFaceBeauty.setEyeEnlargingIntensityV2(0.5);
        //圆眼 20
        recommendFaceBeauty.setEyeCircleIntensity(0.2);
        //下巴 15
        recommendFaceBeauty.setChinIntensity(0.15);
        //额头 20
        recommendFaceBeauty.setForHeadIntensity(0.2);
        recommendFaceBeauty.setForHeadIntensityV2(0.2);
        //瘦鼻 50
        recommendFaceBeauty.setNoseIntensity(0.5);
        recommendFaceBeauty.setNoseIntensityV2(0.5);
       // 嘴型 15
        recommendFaceBeauty.setMouthIntensity(0.15);
        recommendFaceBeauty.setMouthIntensityV2(0.15);
        //开眼角 30
        recommendFaceBeauty.setCanthusIntensity(0.3);
        //眼距 -10
        recommendFaceBeauty.setEyeSpaceIntensity(0.4);
        //缩人中 -10
        recommendFaceBeauty.setPhiltrumIntensity(0.4);
        //微笑嘴角 40
        recommendFaceBeauty.setSmileIntensity(0.4);
        //【滤镜】
        //自然4 25
        return recommendFaceBeauty;
    }


    /**
     * 初始化美肤参数
     *
     * @return ArrayList<FaceBeautyBean>
     */
    public static ArrayList<FaceBeautyBean> buildSkinParams() {
        ArrayList<FaceBeautyBean> params = new ArrayList<>();
        params.add(new FaceBeautyBean(
                        FaceBeautyParam.BLUR_INTENSITY, R.string.beauty_box_heavy_blur_fine,
                        R.drawable.icon_beauty_skin_buffing_close_selector, R.drawable.icon_beauty_skin_buffing_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.COLOR_INTENSITY, R.string.beauty_box_color_level,
                        R.drawable.icon_beauty_skin_color_close_selector, R.drawable.icon_beauty_skin_color_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.RED_INTENSITY, R.string.beauty_box_red_level,
                        R.drawable.icon_beauty_skin_red_close_selector, R.drawable.icon_beauty_skin_red_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.SHARPEN_INTENSITY, R.string.beauty_box_sharpen,
                        R.drawable.icon_beauty_skin_sharpen_close_selector, R.drawable.icon_beauty_skin_sharpen_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.EYE_BRIGHT_INTENSITY, R.string.beauty_box_eye_bright,
                        R.drawable.icon_beauty_skin_eyes_bright_close_selector, R.drawable.icon_beauty_skin_eyes_bright_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.TOOTH_WHITEN_INTENSITY, R.string.beauty_box_tooth_whiten,
                        R.drawable.icon_beauty_skin_teeth_close_selector, R.drawable.icon_beauty_skin_teeth_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.REMOVE_POUCH_INTENSITY, R.string.beauty_micro_pouch,
                        R.drawable.icon_beauty_skin_dark_circles_close_selector, R.drawable.icon_beauty_skin_dark_circles_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.REMOVE_NASOLABIAL_FOLDS_INTENSITY, R.string.beauty_micro_nasolabial,
                        R.drawable.icon_beauty_skin_wrinkle_close_selector, R.drawable.icon_beauty_skin_wrinkle_open_selector
                )
        );
        return params;
    }

    /**
     * 初始化美型参数
     *
     * @return ArrayList<FaceBeautyBean>
     */
    public static ArrayList<FaceBeautyBean> buildShapeParams() {
        ArrayList<FaceBeautyBean> params = new ArrayList<>();
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.CHEEK_THINNING_INTENSITY, R.string.beauty_box_cheek_thinning,
                        R.drawable.icon_beauty_shape_face_cheekthin_close_selector, R.drawable.icon_beauty_shape_face_cheekthin_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.CHEEK_V_INTENSITY, R.string.beauty_box_cheek_v,
                        R.drawable.icon_beauty_shape_face_v_close_selector, R.drawable.icon_beauty_shape_face_v_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.CHEEK_NARROW_INTENSITY_V2, R.string.beauty_box_cheek_narrow,
                        R.drawable.icon_beauty_shape_face_narrow_close_selector, R.drawable.icon_beauty_shape_face_narrow_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.CHEEK_SHORT_INTENSITY, R.string.beauty_box_cheek_short,
                        R.drawable.icon_beauty_shape_face_short_close_selector, R.drawable.icon_beauty_shape_face_short_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.CHEEK_SMALL_INTENSITY_V2, R.string.beauty_box_cheek_small,
                        R.drawable.icon_beauty_shape_face_little_close_selector, R.drawable.icon_beauty_shape_face_little_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.INTENSITY_CHEEKBONES_INTENSITY, R.string.beauty_box_cheekbones,
                        R.drawable.icon_beauty_shape_cheek_bones_close_selector, R.drawable.icon_beauty_shape_cheek_bones_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.INTENSITY_LOW_JAW_INTENSITY, R.string.beauty_box_lower_jaw,
                        R.drawable.icon_beauty_shape_lower_jaw_close_selector, R.drawable.icon_beauty_shape_lower_jaw_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.EYE_ENLARGING_INTENSITY_V2, R.string.beauty_box_eye_enlarge,
                        R.drawable.icon_beauty_shape_enlarge_eye_close_selector, R.drawable.icon_beauty_shape_enlarge_eye_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.EYE_CIRCLE_INTENSITY, R.string.beauty_box_eye_circle,
                        R.drawable.icon_beauty_shape_round_eye_close_selector, R.drawable.icon_beauty_shape_round_eye_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.CHIN_INTENSITY, R.string.beauty_box_intensity_chin,
                        R.drawable.icon_beauty_shape_chin_close_selector, R.drawable.icon_beauty_shape_chin_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.FOREHEAD_INTENSITY_V2, R.string.beauty_box_intensity_forehead,
                        R.drawable.icon_beauty_shape_forehead_close_selector, R.drawable.icon_beauty_shape_forehead_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.NOSE_INTENSITY_V2, R.string.beauty_box_intensity_nose,
                        R.drawable.icon_beauty_shape_thin_nose_close_selector, R.drawable.icon_beauty_shape_thin_nose_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.MOUTH_INTENSITY_V2, R.string.beauty_box_intensity_mouth,
                        R.drawable.icon_beauty_shape_mouth_close_selector, R.drawable.icon_beauty_shape_mouth_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.CANTHUS_INTENSITY, R.string.beauty_micro_canthus,
                        R.drawable.icon_beauty_shape_open_eyes_close_selector, R.drawable.icon_beauty_shape_open_eyes_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.EYE_SPACE_INTENSITY, R.string.beauty_micro_eye_space,
                        R.drawable.icon_beauty_shape_distance_close_selector, R.drawable.icon_beauty_shape_distance_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.EYE_ROTATE_INTENSITY, R.string.beauty_micro_eye_rotate,
                        R.drawable.icon_beauty_shape_angle_close_selector, R.drawable.icon_beauty_shape_angle_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.LONG_NOSE_INTENSITY, R.string.beauty_micro_long_nose,
                        R.drawable.icon_beauty_shape_proboscis_close_selector, R.drawable.icon_beauty_shape_proboscis_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.PHILTRUM_INTENSITY, R.string.beauty_micro_philtrum,
                        R.drawable.icon_beauty_shape_shrinking_close_selector, R.drawable.icon_beauty_shape_shrinking_open_selector
                )
        );
        params.add(
                new FaceBeautyBean(
                        FaceBeautyParam.SMILE_INTENSITY, R.string.beauty_micro_smile,
                        R.drawable.icon_beauty_shape_smile_close_selector, R.drawable.icon_beauty_shape_smile_open_selector
                )
        );
        return params;
    }

    /**
     * 初始化参数扩展列表
     *
     * @return HashMap<String, ModelAttributeData>
     */
    public static HashMap<String, ModelAttributeData> buildModelAttributeRange() {
        HashMap<String, ModelAttributeData> params = new HashMap<>();
        /*美肤*/
        //美白程度 (8.2.0之后不维护) 范围 [0-2]，推荐 0.2
        params.put(FaceBeautyParam.COLOR_INTENSITY, new ModelAttributeData(0.7, 0.0, 0.0, 1.0));
        params.put(FaceBeautyParam.COLOR_INTENSITY_M2,new ModelAttributeData(0.7, 0.0, 0.0, 1.0));
        //磨皮程度
        params.put(FaceBeautyParam.BLUR_INTENSITY, new ModelAttributeData(1.8, 0.0, 0.0, 6.0));
        //红润程度
        params.put(FaceBeautyParam.RED_INTENSITY, new ModelAttributeData(0.5, 0.0, 0.0, 1.0));
        //锐化程度
        params.put(FaceBeautyParam.SHARPEN_INTENSITY, new ModelAttributeData(0.5, 0.0, 0.0, 1.0));
        //亮眼程度
        params.put(FaceBeautyParam.EYE_BRIGHT_INTENSITY, new ModelAttributeData(0.5, 0.0, 0.0, 1.0));
        //美牙程度
        params.put(FaceBeautyParam.TOOTH_WHITEN_INTENSITY, new ModelAttributeData(0.5, 0.0, 0.0, 1.0));
        //去黑眼圈强度
        params.put(FaceBeautyParam.REMOVE_POUCH_INTENSITY, new ModelAttributeData(1.0, 0.0, 0.0, 1.0));
        params.put(FaceBeautyParam.REMOVE_POUCH_INTENSITY_M2,new ModelAttributeData(1.0, 0.0, 0.0, 1.0));
        //去法令纹强度
        params.put(FaceBeautyParam.REMOVE_NASOLABIAL_FOLDS_INTENSITY, new ModelAttributeData(1.0, 0.0, 0.0, 1.0));
        params.put(FaceBeautyParam.REMOVE_NASOLABIAL_FOLDS_INTENSITY_M2, new ModelAttributeData(1.0, 0.0, 0.0, 1.0));
        /*美型*/
        //变形程度
        params.put(FaceBeautyParam.FACE_SHAPE_INTENSITY, new ModelAttributeData(1.0, 0.0, 0.0, 1.0));
        //瘦脸程度
        params.put(FaceBeautyParam.CHEEK_THINNING_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        //长脸程度
        params.put(FaceBeautyParam.CHEEK_LONG_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        //圆脸程度
        params.put(FaceBeautyParam.CHEEK_CIRCLE_INTENSITY, new ModelAttributeData(0.0, 0.0, 0.0, 1.0));
        //V脸程度
        params.put(FaceBeautyParam.CHEEK_V_INTENSITY, new ModelAttributeData(0.5, 0.0, 0.0, 1.0));
        //窄脸程度 V2 历史问题窄脸V2不删
        params.put(FaceBeautyParam.CHEEK_NARROW_INTENSITY_V2, new ModelAttributeData(0.5, 0.0, 0.0, 1.0));
        //短脸程度
        params.put(FaceBeautyParam.CHEEK_SHORT_INTENSITY, new ModelAttributeData(0.1, 0.0, 0.0, 1.0));
        //小脸程度 V2 历史问题小脸V2
        params.put(FaceBeautyParam.CHEEK_SMALL_INTENSITY_V2, new ModelAttributeData(0.1, 0.0, 0.0, 1.0));
        //瘦颧骨
        params.put(FaceBeautyParam.INTENSITY_CHEEKBONES_INTENSITY, new ModelAttributeData(0.5, 0.0, 0.0, 1.0));
        //瘦下颌骨
        params.put(FaceBeautyParam.INTENSITY_LOW_JAW_INTENSITY, new ModelAttributeData(0.2, 0.0, 0.0, 1.0));
        //大眼程度V2
        params.put(FaceBeautyParam.EYE_ENLARGING_INTENSITY, new ModelAttributeData(0.5, 0.0, 0.0, 1.0));
        params.put(FaceBeautyParam.EYE_ENLARGING_INTENSITY_V2, new ModelAttributeData(0.5, 0.0, 0.0, 1.0));
        params.put(FaceBeautyParam.EYE_ENLARGING_INTENSITY_M3, new ModelAttributeData(0.5, 0.0, 0.0, 1.0));
        //圆眼程度
        params.put(FaceBeautyParam.EYE_CIRCLE_INTENSITY, new ModelAttributeData(0.2, 0.0, 0.0, 1.0));
        //下巴调整程度
        params.put(FaceBeautyParam.CHIN_INTENSITY, new ModelAttributeData(0.15, 0.5, 0.0, 1.0));
        //额头调整程度 V2 历史遗留问题额头V2需要保留
        params.put(FaceBeautyParam.FOREHEAD_INTENSITY_V2, new ModelAttributeData(0.2, 0.5, 0.0, 1.0));
        params.put(FaceBeautyParam.FOREHEAD_INTENSITY_M2, new ModelAttributeData(0.2, 0.5, 0.0, 1.0));
        //瘦鼻程度 历史遗留问题瘦鼻V2需要保留
        params.put(FaceBeautyParam.NOSE_INTENSITY_V2, new ModelAttributeData(0.5, 0.0, 0.0, 1.0));
        params.put(FaceBeautyParam.NOSE_INTENSITY_M2, new ModelAttributeData(0.5, 0.0, 0.0, 1.0));
        //嘴巴调整程度V2 (8.2.0之后不维护) 历史遗留问题嘴巴调整程度V2需要保留
        params.put(FaceBeautyParam.MOUTH_INTENSITY_V2, new ModelAttributeData(0.15, 0.5, 0.0, 1.0));
        params.put(FaceBeautyParam.MOUTH_INTENSITY_M3, new ModelAttributeData(0.15, 0.5, 0.0, 1.0));
        //开眼角强度
        params.put(FaceBeautyParam.CANTHUS_INTENSITY, new ModelAttributeData(0.3, 0.0, 0.0, 1.0));
        //眼睛间距
        params.put(FaceBeautyParam.EYE_SPACE_INTENSITY, new ModelAttributeData(0.4, 0.5, 0.0, 1.0));
        //眼睛角度
        params.put(FaceBeautyParam.EYE_ROTATE_INTENSITY, new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
        //鼻子长度
        params.put(FaceBeautyParam.LONG_NOSE_INTENSITY, new ModelAttributeData(0.5, 0.5, 0.0, 1.0));
        //调节人中
        params.put(FaceBeautyParam.PHILTRUM_INTENSITY, new ModelAttributeData(0.4, 0.5, 0.0, 1.0));
        //微笑嘴角强度
        params.put(FaceBeautyParam.SMILE_INTENSITY, new ModelAttributeData(0.4, 0.0, 0.0, 1.0));
        return params;
    }

    /**
     * 初始化滤镜参数
     *
     * @return ArrayList<FaceBeautyFilterBean>
     */
    public static ArrayList<FaceBeautyFilterBean> buildFilters() {
        ArrayList<FaceBeautyFilterBean> filters = new ArrayList<>();
        //【滤镜】
        //自然4 25
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ORIGIN, R.mipmap.icon_beauty_filter_cancel, R.string.origin, 0.0));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZIRAN_1, R.mipmap.icon_beauty_filter_natural_1, R.string.ziran_1));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZIRAN_2, R.mipmap.icon_beauty_filter_natural_2, R.string.ziran_2));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZIRAN_3, R.mipmap.icon_beauty_filter_natural_3, R.string.ziran_3));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZIRAN_4, R.mipmap.icon_beauty_filter_natural_4, R.string.ziran_4));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZIRAN_5, R.mipmap.icon_beauty_filter_natural_5, R.string.ziran_5));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZIRAN_6, R.mipmap.icon_beauty_filter_natural_6, R.string.ziran_6));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZIRAN_7, R.mipmap.icon_beauty_filter_natural_7, R.string.ziran_7));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZIRAN_8, R.mipmap.icon_beauty_filter_natural_8, R.string.ziran_8));

        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZHIGANHUI_1, R.mipmap.icon_beauty_filter_texture_gray_1, R.string.zhiganhui_1));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZHIGANHUI_2, R.mipmap.icon_beauty_filter_texture_gray_2, R.string.zhiganhui_2));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZHIGANHUI_3, R.mipmap.icon_beauty_filter_texture_gray_3, R.string.zhiganhui_3));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZHIGANHUI_4, R.mipmap.icon_beauty_filter_texture_gray_4, R.string.zhiganhui_4));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZHIGANHUI_5, R.mipmap.icon_beauty_filter_texture_gray_5, R.string.zhiganhui_5));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZHIGANHUI_6, R.mipmap.icon_beauty_filter_texture_gray_6, R.string.zhiganhui_6));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZHIGANHUI_7, R.mipmap.icon_beauty_filter_texture_gray_7, R.string.zhiganhui_7));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.ZHIGANHUI_8, R.mipmap.icon_beauty_filter_texture_gray_8, R.string.zhiganhui_8));

        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.MITAO_1, R.mipmap.icon_beauty_filter_peach_1, R.string.mitao_1));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.MITAO_2, R.mipmap.icon_beauty_filter_peach_2, R.string.mitao_2));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.MITAO_3, R.mipmap.icon_beauty_filter_peach_3, R.string.mitao_3));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.MITAO_4, R.mipmap.icon_beauty_filter_peach_4, R.string.mitao_4));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.MITAO_5, R.mipmap.icon_beauty_filter_peach_5, R.string.mitao_5));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.MITAO_6, R.mipmap.icon_beauty_filter_peach_6, R.string.mitao_6));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.MITAO_7, R.mipmap.icon_beauty_filter_peach_7, R.string.mitao_7));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.MITAO_8, R.mipmap.icon_beauty_filter_peach_8, R.string.mitao_8));

        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.BAILIANG_1, R.mipmap.icon_beauty_filter_bailiang_1, R.string.bailiang_1));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.BAILIANG_2, R.mipmap.icon_beauty_filter_bailiang_2, R.string.bailiang_2));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.BAILIANG_3, R.mipmap.icon_beauty_filter_bailiang_3, R.string.bailiang_3));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.BAILIANG_4, R.mipmap.icon_beauty_filter_bailiang_4, R.string.bailiang_4));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.BAILIANG_5, R.mipmap.icon_beauty_filter_bailiang_5, R.string.bailiang_5));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.BAILIANG_6, R.mipmap.icon_beauty_filter_bailiang_6, R.string.bailiang_6));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.BAILIANG_7, R.mipmap.icon_beauty_filter_bailiang_7, R.string.bailiang_7));

        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.FENNEN_1, R.mipmap.icon_beauty_filter_fennen_1, R.string.fennen_1));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.FENNEN_2, R.mipmap.icon_beauty_filter_fennen_2, R.string.fennen_2));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.FENNEN_3, R.mipmap.icon_beauty_filter_fennen_3, R.string.fennen_3));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.FENNEN_5, R.mipmap.icon_beauty_filter_fennen_5, R.string.fennen_5));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.FENNEN_6, R.mipmap.icon_beauty_filter_fennen_6, R.string.fennen_6));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.FENNEN_7, R.mipmap.icon_beauty_filter_fennen_7, R.string.fennen_7));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.FENNEN_8, R.mipmap.icon_beauty_filter_fennen_8, R.string.fennen_8));

        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.LENGSEDIAO_1, R.mipmap.icon_beauty_filter_lengsediao_1, R.string.lengsediao_1));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.LENGSEDIAO_2, R.mipmap.icon_beauty_filter_lengsediao_2, R.string.lengsediao_2));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.LENGSEDIAO_3, R.mipmap.icon_beauty_filter_lengsediao_3, R.string.lengsediao_3));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.LENGSEDIAO_4, R.mipmap.icon_beauty_filter_lengsediao_4, R.string.lengsediao_4));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.LENGSEDIAO_7, R.mipmap.icon_beauty_filter_lengsediao_7, R.string.lengsediao_7));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.LENGSEDIAO_8, R.mipmap.icon_beauty_filter_lengsediao_8, R.string.lengsediao_8));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.LENGSEDIAO_11, R.mipmap.icon_beauty_filter_lengsediao_11, R.string.lengsediao_11));

        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.NUANSEDIAO_1, R.mipmap.icon_beauty_filter_nuansediao_1, R.string.nuansediao_1));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.NUANSEDIAO_2, R.mipmap.icon_beauty_filter_nuansediao_2, R.string.nuansediao_2));

        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.GEXING_1, R.mipmap.icon_beauty_filter_gexing_1, R.string.gexing_1));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.GEXING_2, R.mipmap.icon_beauty_filter_gexing_2, R.string.gexing_2));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.GEXING_3, R.mipmap.icon_beauty_filter_gexing_3, R.string.gexing_3));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.GEXING_4, R.mipmap.icon_beauty_filter_gexing_4, R.string.gexing_4));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.GEXING_5, R.mipmap.icon_beauty_filter_gexing_5, R.string.gexing_5));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.GEXING_7, R.mipmap.icon_beauty_filter_gexing_7, R.string.gexing_7));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.GEXING_10, R.mipmap.icon_beauty_filter_gexing_10, R.string.gexing_10));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.GEXING_11, R.mipmap.icon_beauty_filter_gexing_11, R.string.gexing_11));

        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.XIAOQINGXIN_1, R.mipmap.icon_beauty_filter_xiaoqingxin_1, R.string.xiaoqingxin_1));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.XIAOQINGXIN_3, R.mipmap.icon_beauty_filter_xiaoqingxin_3, R.string.xiaoqingxin_3));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.XIAOQINGXIN_4, R.mipmap.icon_beauty_filter_xiaoqingxin_4, R.string.xiaoqingxin_4));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.XIAOQINGXIN_6, R.mipmap.icon_beauty_filter_xiaoqingxin_6, R.string.xiaoqingxin_6));

        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.HEIBAI_1, R.mipmap.icon_beauty_filter_heibai_1, R.string.heibai_1));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.HEIBAI_2, R.mipmap.icon_beauty_filter_heibai_2, R.string.heibai_2));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.HEIBAI_3, R.mipmap.icon_beauty_filter_heibai_3, R.string.heibai_3));
        filters.add(new FaceBeautyFilterBean(FaceBeautyFilterEnum.HEIBAI_4, R.mipmap.icon_beauty_filter_heibai_4, R.string.heibai_4));

        return filters;
    }

}
