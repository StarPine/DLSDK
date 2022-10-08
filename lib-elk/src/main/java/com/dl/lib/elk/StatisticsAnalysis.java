package com.dl.lib.elk;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.StringUtils;
import com.dl.lib.elk.log.AppLogPackHelper;
import com.dl.lib.util.MPDeviceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计分析上报
 */
public class StatisticsAnalysis {
    private static final String TAG = StatisticsAnalysis.class.getSimpleName();
    private static final int COLLECT_COUNT_DEFAULT = 5;
    private static int COLLECT_COUNT = COLLECT_COUNT_DEFAULT;
    private static boolean SEND_IMMEDIATELY = true;
    private static final List<String> statisticsList = new ArrayList<>();

    public static void init(boolean immediately) {
        SEND_IMMEDIATELY = immediately;
        COLLECT_COUNT = COLLECT_COUNT_DEFAULT;
    }

    public static void doSendStatistics(final Map<String, String> statisticsMap) {
        addOccurTime(statisticsMap);
        doSendStatistics(SEND_IMMEDIATELY, statisticsMap);
    }

    private static void doSendStatistics(boolean immediately, Map<String, String> statisticsMap) {

        doSendStatistics(immediately, statisticsMap, false);
    }

    private static void doSendStatistics(boolean immediately, Map<String, String> statisticsMap, boolean withHbArg) {
        if ( statisticsMap == null || statisticsMap.size() == 0) {
            return;
        }
        statisticsMap.putAll(MPDeviceUtils.getElkAndroidData());
        String statisticsString = AppLogPackHelper.getStatisticsString(statisticsMap);
        if (TextUtils.isEmpty(statisticsString)) {
            return;
        }

        if (immediately) {
            StatisticsManager.getInstance().sendStatistics( statisticsString, withHbArg);
        } else {
            saveStatistics(statisticsString);
        }
    }

    private static synchronized void saveStatistics(String string) {
        if (TextUtils.isEmpty(string)) {
            return;
        }

        statisticsList.add(string);
        int configCollectCount = 0;
        if (StatisticsManager.getInstance().getStatisticsConfig() != null) {
            configCollectCount = StatisticsManager.getInstance().getStatisticsConfig().getLgsCollectCount();
        }
        if (configCollectCount == 0) {
            configCollectCount = COLLECT_COUNT;
        }
        if (statisticsList.size() >= configCollectCount) {
            String statisticsString = TextUtils.join("\n", statisticsList);
            StatisticsManager.getInstance().sendStatistics(statisticsString);
            statisticsList.clear();
        }
    }

    public static synchronized void sendSaveStatistics() {
        if (statisticsList.size() > 0) {
            String statisticsString = TextUtils.join("\n", statisticsList);
            StatisticsManager.getInstance().sendStatistics(statisticsString);
            statisticsList.clear();
        }
    }

    private static void addOccurTime(Map<String, String> statisticsMap) {
        if (statisticsMap != null && statisticsMap.size() > 0) {
            String otm = statisticsMap.get("otm");
            if (StringUtils.isEmpty(otm)) {
                statisticsMap.put("otm", String.valueOf(System.currentTimeMillis()));
            }
        }
    }

    // 通用模块-----------------------------------------------------------------

    /**
     * 点击事件
     *
     * @param dt   当前在哪个页面
     * @param et   页面上的哪一个分类
     * @param ct   页面上的哪一个模块
     */
    public static void commonClick(String dt, String et, String ct) {
        doSendStatistics(AppLogPackHelper.getCommonEventMessage(dt, et, ct));
    }

}
