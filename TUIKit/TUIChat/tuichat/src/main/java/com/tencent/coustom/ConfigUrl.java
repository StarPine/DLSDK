package com.tencent.coustom;

/**
 * Author: 彭石林
 * Time: 2021/10/22 17:14
 * Description: This is ConfigUrl
 */
public class ConfigUrl {
    private static final String IMAGE_BASE_URL = "https://img.joy-mask.com/";
    public static String getFullImageUrl(String imgPath) {
        if (imgPath == null) {
            return "";
        } else if (imgPath.toLowerCase().startsWith("images/")) {
            if (imgPath.endsWith(".mp4")) {
                return IMAGE_BASE_URL + imgPath;
            } else {
                return IMAGE_BASE_URL + imgPath;
            }
        }
        return imgPath;
    }
}
