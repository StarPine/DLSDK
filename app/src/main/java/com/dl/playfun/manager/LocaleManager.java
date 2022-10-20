package com.dl.playfun.manager;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import com.blankj.utilcode.util.StringUtils;
import com.dl.lib.util.CommSharedUtil;
import com.dl.lib.util.MMKVUtil;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;

import java.util.Locale;

import me.goldze.mvvmhabit.utils.Utils;

public class LocaleManager {
    public static final String dlAppLanguageLocal = "dlAppLanguageLocal";

    /**
     * 获取系统的locale
     *
     * @return Locale对象
     */
    public static Locale getSystemLocale(Context mContext) {
        String localeText = readLocalCache(mContext);
        if(StringUtils.isEmpty(localeText)){
            localeText = mContext.getString(R.string.playfun_local_language_val);
        }
        return new Locale(localeText);
    }

    public static void putLocalCacheApply(String local){
       // MMKVUtil.getInstance().putKeyValue(dlAppLanguageLocal,local);
        CommSharedUtil.getInstance(AppContext.instance()).putString(dlAppLanguageLocal,local);
    }

    public static String readLocalCache(Context mContext){
        return CommSharedUtil.getInstance(mContext).getString(dlAppLanguageLocal);
        //return MMKVUtil.getInstance().readKeyValue(dlAppLanguageLocal);
    }


    public static Context setLocal(Context context) {
        return updateResources(context, getSystemLocale(context));
    }

    private static Context updateResources(Context context, Locale locale) {
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        if (Build.VERSION.SDK_INT >= 17) {
            config.setLocale(locale);
            context = context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
        return context;
    }

    public static void onConfigurationChanged(Context context){
        setLocal(context);
    }

}
