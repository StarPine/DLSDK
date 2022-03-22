package com.dl.playfun.utils;

import android.content.Context;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.R;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.entity.OccupationConfigItemEntity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StringUtil {
    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.equals("");
    }

    /**
     * 大写转小写
     */
    public static String toLowerCasea(String str) {
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if ('A' <= chars[i] && chars[i] <= 'Z') {
                chars[i] += 32;
            }
        }
        return String.valueOf(chars);
    }

    /**
     * @param str
     * @return
     */
    public static String toLowerCaseA(String str) {
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if ('a' <= chars[i] && chars[i] <= 'z') {
                chars[i] -= 32;
            }
        }
        return String.valueOf(chars);
    }

    //
    public static String getConfigValue(List<Integer> ids, List<ConfigItemEntity> data) {
        if (ids == null) {
            return "";
        }
        String result = "";
        if (data.size() != 0) {
            for (ConfigItemEntity config : data) {
                for (int i = 0; i < ids.size(); i++) {
                    if (ids.get(i).intValue() == config.getId()) {
                        if (i == ids.size() - 1) {
                            result += config.getName();
                        } else {
                            result = result + config.getName() + "/";
                        }
                    }

                }
            }
        }
        return result;
    }

    public static String getOccputionValue(int id, List<OccupationConfigItemEntity> data) {
        String result = "";
        if (data.size() != 0) {
            for (OccupationConfigItemEntity config : data) {
                for (OccupationConfigItemEntity.ItemEntity item : config.getItem()
                ) {
                    if (id == item.getId()) {
                        result = item.getName();
                    }
                }
            }
        }
        return result;
    }

    public static String getFullImageUrl(String imgPath) {
        if (imgPath == null) {
            return "";
        } else if (imgPath.toLowerCase().startsWith("images/")) {
            if (imgPath.endsWith(".mp4")) {
                return AppConfig.IMAGE_BASE_URL + imgPath;
            } else {
                return AppConfig.IMAGE_BASE_URL + imgPath;
            }
        }
        return imgPath;
    }

    public static String getFullAudioUrl(String urlPath) {
        if (urlPath == null) {
            return "";
        } else {
            return AppConfig.IMAGE_BASE_URL + urlPath;
        }
    }

    public static String getFullImageWatermarkUrl(String imgPath) {
        if (imgPath == null) {
            return "";
        } else if (imgPath.toLowerCase().startsWith("images/")) {
            if (imgPath.endsWith(".mp4")) {
                return AppConfig.IMAGE_BASE_URL + imgPath;
            } else {
                return AppConfig.IMAGE_BASE_URL + imgPath + "?x-oss-process=style/watermark";
            }
        }
        return imgPath;
    }

    public static String getFullThumbImageUrl(String imgPath) {
        if (imgPath == null) {
            return "";
        } else if (imgPath.toLowerCase().startsWith("images/")) {
            if (imgPath.endsWith(".mp4")) {
                String url = AppConfig.IMAGE_BASE_URL + imgPath;
                return AppConfig.IMAGE_BASE_URL + imgPath;
            } else {
                return AppConfig.IMAGE_BASE_URL + imgPath + "?x-oss-process=style/thumb";
            }
        }
        return imgPath;
    }

    public static String getWeekengString(int weekeng, Context context) {
        String str = "";
        switch (weekeng) {
            case 1:
                str = StringUtils.getString(R.string.playfun_monday);
                break;
            case 2:
                str = StringUtils.getString(R.string.playfun_tuesday);
                break;
            case 3:
                str = StringUtils.getString(R.string.playfun_wednesday);
                break;
            case 4:
                str = StringUtils.getString(R.string.playfun_thursday);
                break;
            case 5:
                str = StringUtils.getString(R.string.playfun_friday);
                break;
            case 6:
                str = StringUtils.getString(R.string.playfun_saturday);
                break;
            case 7:
                str = StringUtils.getString(R.string.playfun_sunday);
                break;
        }
        return str;
    }

    public static String getDayString(String time) {
        String str = "";
        if (time != null) {
            long timeStamp = TimeUtils.string2Millis(time, TimeUtils.getSafeDateFormat("yyyy-MM-dd"));
            if (Utils.isManilaApp(AppContext.instance())) {
                Calendar calDate = Calendar.getInstance();
                calDate.setTimeInMillis(timeStamp);
                String dayNumberSuffix = getDayNumberSuffix(calDate.get(Calendar.DAY_OF_MONTH));
                DateFormat dateFormat = new SimpleDateFormat("MMMM d'" + dayNumberSuffix + "'", Locale.ENGLISH);
                str = TimeUtils.millis2String(timeStamp, dateFormat);
            } else {
                str = TimeUtils.millis2String(timeStamp, TimeUtils.getSafeDateFormat("M月d日"));
            }
        }
        return str;
    }

    public static String getDayNumberSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    /**
     * @return java.lang.String
     * @Desc TODO(根据ID获取心情文案)
     * @author 彭石林
     * @parame [id]
     * @Date 2021/10/18
     */
    public static String getDatingObjItem(int id) {
        String str = StringUtils.getString(R.string.playfun_user_detail_ta_dynamic);
        switch (id) {
            case 1:
                str = StringUtils.getString(R.string.playfun_mood_item_id1);
                break;
            case 2:
                str = StringUtils.getString(R.string.playfun_mood_item_id2);
                break;
            case 3:
                str = StringUtils.getString(R.string.playfun_mood_item_id3);
                break;
            case 4:
                str = StringUtils.getString(R.string.playfun_mood_item_id4);
                break;
            case 5:
                str = StringUtils.getString(R.string.playfun_mood_item_id5);
                break;
            case 6:
                str = StringUtils.getString(R.string.playfun_mood_item_id6);
                break;
        }
        return str;
    }

}
