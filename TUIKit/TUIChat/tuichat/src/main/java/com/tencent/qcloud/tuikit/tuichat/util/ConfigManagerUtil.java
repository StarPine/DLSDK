package com.tencent.qcloud.tuikit.tuichat.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @Description：
 * @Author： liaosf
 * @Date： 2022/1/15 15:55
 * 修改备注：
 */
public class ConfigManagerUtil {
    private static ConfigManagerUtil mCacheManager;
    private final String cryptKey = "playfun@2020";
    private static final String KEY_GAME_CONFIG = "key_game_config";
    private static final String KEY_PLAY_GAME_FLAG = "key_play_game_flag";
    private static final String KEY_IS_CHAT_PUSH = "key_is_chat_push";
    private final MMKV kv = MMKV.mmkvWithID("cache", MMKV.SINGLE_PROCESS_MODE, cryptKey);
    private Gson gson;

    public static ConfigManagerUtil getInstance() {
        if (mCacheManager == null) {
            synchronized (ConfigManagerUtil.class) {
                if (mCacheManager == null) {
                    mCacheManager = new ConfigManagerUtil();
                }
            }
        }
        return mCacheManager;
    }

    public Boolean readChatPushStatus() {
        int isChatPush = kv.decodeInt(KEY_IS_CHAT_PUSH);
        return isChatPush == 1;
    }

    public boolean isPlayGameFlag() {
        return kv.decodeBool(KEY_PLAY_GAME_FLAG,false);
    }

    public void putPlayGameFlag(boolean playGameFlag) {
        kv.encode(KEY_PLAY_GAME_FLAG, playGameFlag);
    }

    public static boolean isEmpty(final CharSequence obj) {
        return obj == null || obj.toString().length() == 0;
    }

    public static boolean isEmpty(final Collection obj) {
        return obj == null || obj.isEmpty();
    }
}
