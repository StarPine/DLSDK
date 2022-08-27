//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.luck.picture.lib.language;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

import com.luck.picture.lib.tools.SPUtils;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class PictureLanguageUtils {
    private static final String KEY_LOCALE = "KEY_LOCALE";
    private static final String VALUE_FOLLOW_SYSTEM = "VALUE_FOLLOW_SYSTEM";

    private PictureLanguageUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static void setAppLanguage(Context context, int languageId) {
        WeakReference<Context> contextWeakReference = new WeakReference(context);
        applyLanguage((Context)contextWeakReference.get(), context.getApplicationContext().getResources().getConfiguration().locale);
//        if (languageId >= 0) {
//            applyLanguage((Context)contextWeakReference.get(), context.getApplicationContext().getResources().getConfiguration().locale);
//        } else {
//            setDefaultLanguage((Context)contextWeakReference.get());
//        }

    }

    private static void applyLanguage(@NonNull Context context, @NonNull Locale locale) {
        applyLanguage(context, locale, false);
    }

    private static void applyLanguage(@NonNull Context context, @NonNull Locale locale, boolean isFollowSystem) {
        if (isFollowSystem) {
            SPUtils.getPictureSpUtils().put("KEY_LOCALE", "VALUE_FOLLOW_SYSTEM");
        } else {
            String localLanguage = locale.getLanguage();
            String localCountry = locale.getCountry();
            SPUtils.getPictureSpUtils().put("KEY_LOCALE", localLanguage + "$" + localCountry);
        }

        updateLanguage(context, locale);
    }

    private static void updateLanguage(Context context, Locale locale) {
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        Locale contextLocale = config.locale;
        if (!equals(contextLocale.getLanguage(), locale.getLanguage()) || !equals(contextLocale.getCountry(), locale.getCountry())) {
            DisplayMetrics dm = resources.getDisplayMetrics();
            if (VERSION.SDK_INT >= 17) {
                config.setLocale(locale);
                context.createConfigurationContext(config);
            } else {
                config.locale = locale;
            }

            resources.updateConfiguration(config, dm);
        }
    }

    private static void setDefaultLanguage(Context context) {
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        if (VERSION.SDK_INT >= 17) {
            config.setLocale(config.locale);
            context.createConfigurationContext(config);
        }

        resources.updateConfiguration(config, dm);
    }

    private static boolean equals(CharSequence s1, CharSequence s2) {
        if (s1 == s2) {
            return true;
        } else {
            int length;
            if (s1 != null && s2 != null && (length = s1.length()) == s2.length()) {
                if (s1 instanceof String && s2 instanceof String) {
                    return s1.equals(s2);
                } else {
                    for(int i = 0; i < length; ++i) {
                        if (s1.charAt(i) != s2.charAt(i)) {
                            return false;
                        }
                    }

                    return true;
                }
            } else {
                return false;
            }
        }
    }
}
