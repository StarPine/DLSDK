
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.luck.picture.lib.tools;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SPUtils {
    private static final Map<String, SPUtils> SP_UTILS_MAP = new HashMap();
    private SharedPreferences sp;

    public static SPUtils getPictureSpUtils() {
        return getInstance("PictureSpUtils");
    }

    public static SPUtils getInstance() {
        return getInstance("", 0);
    }

    public static SPUtils getInstance(int mode) {
        return getInstance("", mode);
    }

    public static SPUtils getInstance(String spName) {
        return getInstance(spName, 0);
    }

    public static SPUtils getInstance(String spName, int mode) {
        if (isSpace(spName)) {
            spName = "spUtils";
        }

        SPUtils spUtils = (SPUtils)SP_UTILS_MAP.get(spName);
        if (spUtils == null) {
            Class var3 = SPUtils.class;
            synchronized(SPUtils.class) {
                spUtils = (SPUtils)SP_UTILS_MAP.get(spName);
                if (spUtils == null) {
                    spUtils = new SPUtils(spName, mode);
                    SP_UTILS_MAP.put(spName, spUtils);
                }
            }
        }

        return spUtils;
    }

    private SPUtils(String spName) {
        this.sp = getApplicationByReflect().getSharedPreferences(spName, 0);
    }

    private SPUtils(String spName, int mode) {
        this.sp = getApplicationByReflect().getSharedPreferences(spName, mode);
    }

    public void put(@NonNull String key, String value) {
        this.put(key, value, false);
    }

    public void put(@NonNull String key, String value, boolean isCommit) {
        if (isCommit) {
            this.sp.edit().putString(key, value).commit();
        } else {
            this.sp.edit().putString(key, value).apply();
        }

    }

    public String getString(@NonNull String key) {
        return this.getString(key, "");
    }

    public String getString(@NonNull String key, String defaultValue) {
        return this.sp.getString(key, defaultValue);
    }

    public void put(@NonNull String key, int value) {
        this.put(key, value, false);
    }

    public void put(@NonNull String key, int value, boolean isCommit) {
        if (isCommit) {
            this.sp.edit().putInt(key, value).commit();
        } else {
            this.sp.edit().putInt(key, value).apply();
        }

    }

    public int getInt(@NonNull String key) {
        return this.getInt(key, -1);
    }

    public int getInt(@NonNull String key, int defaultValue) {
        return this.sp.getInt(key, defaultValue);
    }

    public void put(@NonNull String key, long value) {
        this.put(key, value, false);
    }

    public void put(@NonNull String key, long value, boolean isCommit) {
        if (isCommit) {
            this.sp.edit().putLong(key, value).commit();
        } else {
            this.sp.edit().putLong(key, value).apply();
        }

    }

    public long getLong(@NonNull String key) {
        return this.getLong(key, -1L);
    }

    public long getLong(@NonNull String key, long defaultValue) {
        return this.sp.getLong(key, defaultValue);
    }

    public void put(@NonNull String key, float value) {
        this.put(key, value, false);
    }

    public void put(@NonNull String key, float value, boolean isCommit) {
        if (isCommit) {
            this.sp.edit().putFloat(key, value).commit();
        } else {
            this.sp.edit().putFloat(key, value).apply();
        }

    }

    public float getFloat(@NonNull String key) {
        return this.getFloat(key, -1.0F);
    }

    public float getFloat(@NonNull String key, float defaultValue) {
        return this.sp.getFloat(key, defaultValue);
    }

    public void put(@NonNull String key, boolean value) {
        this.put(key, value, false);
    }

    public void put(@NonNull String key, boolean value, boolean isCommit) {
        if (isCommit) {
            this.sp.edit().putBoolean(key, value).commit();
        } else {
            this.sp.edit().putBoolean(key, value).apply();
        }

    }

    public boolean getBoolean(@NonNull String key) {
        return this.getBoolean(key, false);
    }

    public boolean getBoolean(@NonNull String key, boolean defaultValue) {
        return this.sp.getBoolean(key, defaultValue);
    }

    public void put(@NonNull String key, Set<String> value) {
        this.put(key, value, false);
    }

    public void put(@NonNull String key, Set<String> value, boolean isCommit) {
        if (isCommit) {
            this.sp.edit().putStringSet(key, value).commit();
        } else {
            this.sp.edit().putStringSet(key, value).apply();
        }

    }

    public Set<String> getStringSet(@NonNull String key) {
        return this.getStringSet(key, Collections.emptySet());
    }

    public Set<String> getStringSet(@NonNull String key, Set<String> defaultValue) {
        return this.sp.getStringSet(key, defaultValue);
    }

    public Map<String, ?> getAll() {
        return this.sp.getAll();
    }

    public boolean contains(@NonNull String key) {
        return this.sp.contains(key);
    }

    public void remove(@NonNull String key) {
        this.remove(key, false);
    }

    public void remove(@NonNull String key, boolean isCommit) {
        if (isCommit) {
            this.sp.edit().remove(key).commit();
        } else {
            this.sp.edit().remove(key).apply();
        }

    }

    public void clear() {
        this.clear(false);
    }

    public void clear(boolean isCommit) {
        if (isCommit) {
            this.sp.edit().clear().commit();
        } else {
            this.sp.edit().clear().apply();
        }

    }

    private static boolean isSpace(String s) {
        if (s == null) {
            return true;
        } else {
            int i = 0;

            for(int len = s.length(); i < len; ++i) {
                if (!Character.isWhitespace(s.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }

    private static Application getApplicationByReflect() {
        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object thread = activityThread.getMethod("currentActivityThread").invoke((Object)null);
            Object app = activityThread.getMethod("getApplication").invoke(thread);
            if (app == null) {
                throw new NullPointerException("u should init first");
            }

            return (Application)app;
        } catch (NoSuchMethodException var3) {
            var3.printStackTrace();
        } catch (IllegalAccessException var4) {
            var4.printStackTrace();
        } catch (InvocationTargetException var5) {
            var5.printStackTrace();
        } catch (ClassNotFoundException var6) {
            var6.printStackTrace();
        }

        throw new NullPointerException("u should init first");
    }
}
