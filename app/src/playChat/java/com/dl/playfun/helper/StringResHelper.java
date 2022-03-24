package com.dl.playfun.helper;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;

public class StringResHelper {

    private static int readSex() {
        return AppContext.instance().appRepository.readUserData().getSex();
    }

    public static String getCommentDialogTitle() {
        if (readSex() == AppConfig.MALE) {
            return StringUtils.getString(R.string.only_member_comment);
        } else {
            if (AppContext.instance().appRepository.readUserData().getCertification() == 1) {
                return StringUtils.getString(R.string.dialog_goddess_comment_title);
            } else {
                return StringUtils.getString(R.string.warn_no_certification);
            }
        }
    }

    public static String getCommentDialogBtnText() {
        if (readSex() == AppConfig.MALE) {
            return StringUtils.getString(R.string.to_be_member_comment);
        } else {
            if (AppContext.instance().appRepository.readUserData().getCertification() == 1) {
                return StringUtils.getString(R.string.dialog_goddess_comment_button_text);
            } else {
                return StringUtils.getString(R.string.dialog_goddess_comment_cert_button_text);
            }
        }
    }
}
