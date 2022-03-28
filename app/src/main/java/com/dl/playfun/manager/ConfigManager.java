package com.dl.playfun.manager;


import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.EaringlSwitchUtil;
import com.dl.playfun.entity.EvaluateObjEntity;
import com.dl.playfun.entity.GameConfigEntity;
import com.dl.playfun.entity.SystemConfigContentEntity;
import com.dl.playfun.entity.SystemConfigEntity;
import com.dl.playfun.entity.SystemConfigTaskEntity;

import java.util.List;

/**
 * 配置管理
 *
 * @author wulei
 */
public class ConfigManager {

    private static ConfigManager mCacheManager;

    private final List<EvaluateObjEntity> evaluateConfigs;

    private final SystemConfigEntity systemConfigEntity;

    private ConfigManager() {
        evaluateConfigs = AppContext.instance().appRepository.readEvaluateConfig();
        systemConfigEntity = AppContext.instance().appRepository.readSystemConfig();
    }

    public static ConfigManager getInstance() {
        if (mCacheManager == null) {
            synchronized (ConfigManager.class) {
                if (mCacheManager == null) {
                    mCacheManager = new ConfigManager();
                }
            }
        }
        return mCacheManager;
    }

    public static void DesInstance() {
        if (mCacheManager != null) {
            synchronized (ConfigManager.class) {
                if (mCacheManager != null) {
                    mCacheManager = null;
                }
            }
        }
    }

    public SystemConfigEntity getSystemConfigEntity() {
        return systemConfigEntity;
    }

    public EvaluateObjEntity getEvaluateConfigById(int id) {
        EvaluateObjEntity entity = null;
        for (EvaluateObjEntity evaluateConfig : evaluateConfigs) {
            if (evaluateConfig.getId().intValue() == id) {
                entity = evaluateConfig;
            }
        }
        return entity;
    }

    //位置是否是空的
    public boolean isLocation() {
        try {
            if (AppContext.instance().appRepository.readUserData() != null) {
                return AppContext.instance().appRepository.readUserData().getPermanentCityIds().isEmpty();
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }

    //获取任务中心配置
    public SystemConfigTaskEntity getTaskConfig() {
        return AppContext.instance().appRepository.readSystemConfigTask();
    }

    //收益开关
    public boolean getTipMoneyShowFlag() {
        return AppContext.instance().appRepository.readSwitches(EaringlSwitchUtil.KEY_TIPS).intValue() == 1;
    }

    //收入开关
    public boolean getRemoveImMessageFlag() {
        return AppContext.instance().appRepository.readSwitches(EaringlSwitchUtil.REMOVE_IM_MESSAGE).intValue() == 1;
    }



    /**
     * 返回用户上级ID
     *
     * @return
     */
    public Integer getUserParentId() {
        return AppContext.instance().appRepository.readUserData().getpId();
    }

    /**
     * 是否男性
     *
     * @return
     */
    public boolean isMale() {
        return AppContext.instance().appRepository.readUserData().getSex() == 1;
    }

    /**
     * 获取个人头像
     *
     * @return
     */
    public String getAvatar() {
        return AppContext.instance().appRepository.readUserData().getAvatar();
    }


    /**
     * 是否新用户
     */
    public boolean isNewUser(){
        return AppContext.instance().appRepository.readIsNewUser();
    }

    /**
     * 是否VIP
     *
     * @return
     */
    public boolean isVip() {
        return AppContext.instance().appRepository.readUserData().getIsVip() == 1;
    }

    /**
     * 是否真人
     *
     * @return
     */
    public boolean isCertification() {
        return AppContext.instance().appRepository.readUserData().getCertification() == 1;
    }

    //    man_user 男性普通用户 man_real 男性真人 man_vip 男性会员 woman_user 女性普通用户 woman_real 女性真人 woman_vip 女神
//    ImMoney 私聊价格 ImgTime 阅后即时间 ImNumber 聊天次数 NewsMoney 发动态价格 TopicalMoney 发节目价格 BrowseHomeNumber 可浏览主页次

    /**
     * 私聊价格
     *
     * @return
     */
    public int getImMoney() {
        int money = 999;
        if (isMale()) {
            if (isVip()) {
                money = systemConfigEntity.getManVip().getImMoney();
            } else if (isCertification()) {
                money = systemConfigEntity.getManReal().getImMoney();
            } else {
                money = systemConfigEntity.getManUser().getImMoney();
            }
        } else {
            if (isVip()) {
                money = systemConfigEntity.getWomanVip().getImMoney();
            } else if (isCertification()) {
                money = systemConfigEntity.getWomanReal().getImMoney();
            } else {
                money = systemConfigEntity.getWomanUser().getImMoney();
            }
        }
        return money;
    }

    /**
     * 阅后即焚时间
     *
     * @return
     */
    public int getBurnTime() {
        int time = 3;
        if (isMale()) {
            if (isVip()) {
                time = systemConfigEntity.getManVip().getImgTime();
            } else if (isCertification()) {
                time = systemConfigEntity.getManReal().getImgTime();
            } else {
                time = systemConfigEntity.getManUser().getImgTime();
            }
        } else {
            if (isVip()) {
                time = systemConfigEntity.getWomanVip().getImgTime();
            } else if (isCertification()) {
                time = systemConfigEntity.getWomanReal().getImgTime();
            } else {
                time = systemConfigEntity.getWomanUser().getImgTime();
            }
        }
        return time;
    }

    /**
     * 根据gamechannel获取游戏图标
     * @param gameChannel
     * @return
     */
    public String getGameUrl(String gameChannel){
        if (ObjectUtils.isEmpty(gameChannel)){
            return "";
        }
        List<GameConfigEntity> gameConfigEntities = AppContext.instance().appRepository.readGameConfig();

        if (ObjectUtils.isEmpty(gameConfigEntities)){
            return "";
        }
        String gameUrl= "";
        for (int i = 0; i < gameConfigEntities.size(); i++) {
            GameConfigEntity configItemEntity = gameConfigEntities.get(i);
            if (configItemEntity.getId().equals(gameChannel)){
//            if (configItemEntity.getId().equals("1642158125")){//模拟显示
                gameUrl = configItemEntity.getUrl();
            }
        }
        return gameUrl;
    }

    /**
     * 获取发送信息次数
     *
     * @return
     */
    public Integer getSendMessagesNumber() {
        Integer num = 0;
        if (isMale()) {
            if (isVip()) {
                num = systemConfigEntity.getManVip().getSendMessagesNumber();
            } else if (isCertification()) {
                num = systemConfigEntity.getManReal().getSendMessagesNumber();
            } else {
                num = systemConfigEntity.getManUser().getSendMessagesNumber();
            }
        } else {
            if (isVip()) {
                num = systemConfigEntity.getWomanVip().getSendMessagesNumber();
            } else if (isCertification()) {
                num = systemConfigEntity.getWomanReal().getSendMessagesNumber();
            } else {
                num = systemConfigEntity.getWomanUser().getSendMessagesNumber();
            }
        }
        return num;
    }

    /**
     * 获取消息显示次数
     *
     * @return
     */
    public Integer getViewMessagesNumber() {
        Integer num = 0;
        if (isMale()) {
            if (isVip()) {
                num = systemConfigEntity.getManVip().getViewMessagesNumber();
            } else if (isCertification()) {
                num = systemConfigEntity.getManReal().getViewMessagesNumber();
            } else {
                num = systemConfigEntity.getManUser().getViewMessagesNumber();
            }
        } else {
            if (isVip()) {
                num = systemConfigEntity.getWomanVip().getViewMessagesNumber();
            } else if (isCertification()) {
                num = systemConfigEntity.getWomanReal().getViewMessagesNumber();
            } else {
                num = systemConfigEntity.getWomanUser().getViewMessagesNumber();
            }
        }
        return num;
    }

    /**
     * 读取line的时间
     *
     * @return
     */
    public Integer getReadLineTime() {
        Integer num = systemConfigEntity.getContent().getReadLineTime();
        return num;
    }

    /**
     * 读取主页消耗次数
     *
     * @return
     */
    public Integer getGetUserHomeMoney() {
        return systemConfigEntity.getContent().getGetUserHomeMoney();
    }

    /**
     * 每日推荐弹窗(开启关闭)
     *
     * @return
     */
    public boolean getRecommendClose() {
        SystemConfigContentEntity systemConfigContentEntity = systemConfigEntity.getContent();
        if (systemConfigContentEntity != null) {
            return systemConfigContentEntity.getRecommendClose() == null || systemConfigContentEntity.getRecommendClose().intValue() == 0;
        } else {
            return true;
        }
    }

    /**
     * 每日推荐弹窗-自动关闭倒计时（15s）
     *
     * @return
     */
    public int getRecommendCloseTime() {
        SystemConfigContentEntity systemConfigContentEntity = systemConfigEntity.getContent();
        if (systemConfigContentEntity != null) {
            if (systemConfigContentEntity.getRecommendCloseTime() != null) {
                return systemConfigContentEntity.getRecommendCloseTime().intValue();
            } else {
                return 15;
            }
        } else {
            return 15;
        }
    }

    /**
     * 第一次出现弹窗时间（登录后30s）
     *
     * @return
     */
    public int getRecommendOneTime() {
        SystemConfigContentEntity systemConfigContentEntity = systemConfigEntity.getContent();
        if (systemConfigContentEntity != null) {
            if (systemConfigContentEntity.getRecommendOneTime() != null) {
                return systemConfigContentEntity.getRecommendOneTime().intValue();
            } else {
                return 30;
            }
        } else {
            return 30;
        }
    }

    /**
     * 第二次出现弹窗时间（登录后5min）
     *
     * @return
     */
    public int getRecommendTwoTime() {
        SystemConfigContentEntity systemConfigContentEntity = systemConfigEntity.getContent();
        if (systemConfigContentEntity != null) {
            if (systemConfigContentEntity.getRecommendTwoTime() != null) {
                return systemConfigContentEntity.getRecommendTwoTime().intValue();
            } else {
                return (300);
            }
        } else {
            return (300);
        }
    }

    /**
     * 聊天次数
     *
     * @return
     */
    public int getImNumber() {
        int number = 0;
        if (isMale()) {
            if (isVip()) {
                number = systemConfigEntity.getManVip().getImNumber();
            } else if (isCertification()) {
                number = systemConfigEntity.getManReal().getImNumber();
            } else {
                number = systemConfigEntity.getManUser().getImNumber();
            }
        } else {
            if (isVip()) {
                number = systemConfigEntity.getWomanVip().getImNumber();
            } else if (isCertification()) {
                number = systemConfigEntity.getWomanReal().getImNumber();
            } else {
                number = systemConfigEntity.getWomanUser().getImNumber();
            }
        }
        return number;
    }

    /**
     * @return java.lang.Integer
     * @Desc TODO(解锁谁看过我钻石消耗)
     * @author 彭石林
     * @parame []
     * @Date 2021/8/4
     */
    public Integer GetViewUseBrowseMoney() {
        return systemConfigEntity.getContent().getGetViewUseBrowseMoney();
    }

    /**
     * 发动态价格
     *
     * @return
     */
    public int getNewsMoney() {
        int money = 999;
        if (isMale()) {
            if (isVip()) {
                money = systemConfigEntity.getManVip().getNewsMoney();
            } else if (isCertification()) {
                money = systemConfigEntity.getManReal().getNewsMoney();
            } else {
                money = systemConfigEntity.getManUser().getNewsMoney();
            }
        } else {
            if (isVip()) {
                money = systemConfigEntity.getWomanVip().getNewsMoney();
            } else if (isCertification()) {
                money = systemConfigEntity.getWomanReal().getNewsMoney();
            } else {
                money = systemConfigEntity.getWomanUser().getNewsMoney();
            }
        }
        return money;
    }

    /**
     * 发节目价格
     *
     * @return
     */
    public int getTopicalMoney() {
        int money = 999;
        if (isMale()) {
            if (isVip()) {
                money = systemConfigEntity.getManVip().getTopicalMoney();
            } else if (isCertification()) {
                money = systemConfigEntity.getManReal().getTopicalMoney();
            } else {
                money = systemConfigEntity.getManUser().getTopicalMoney();
            }
        } else {
            if (isVip()) {
                money = systemConfigEntity.getWomanVip().getTopicalMoney();
            } else if (isCertification()) {
                money = systemConfigEntity.getWomanReal().getTopicalMoney();
            } else {
                money = systemConfigEntity.getWomanUser().getTopicalMoney();
            }
        }
        return money;
    }

    /**
     * 每天最大浏览主页次数
     *
     * @return
     */
    public int getMaxBrowseHomeNumber() {
        int number = 0;
        if (isMale()) {
            if (isVip()) {
                number = systemConfigEntity.getManVip().getBrowseHomeNumber();
            } else if (isCertification()) {
                number = systemConfigEntity.getManReal().getBrowseHomeNumber();
            } else {
                number = systemConfigEntity.getManUser().getBrowseHomeNumber();
            }
        } else {
            if (isVip()) {
                number = systemConfigEntity.getWomanVip().getBrowseHomeNumber();
            } else if (isCertification()) {
                number = systemConfigEntity.getWomanReal().getBrowseHomeNumber();
            } else {
                number = systemConfigEntity.getWomanUser().getBrowseHomeNumber();
            }
        }
        return number;
    }

    public String getUnLockAccountMoney() {
        try {
            return systemConfigEntity.getContent().getGetAccountMoney();
        } catch (Exception e) {
            return "";
        }
    }

    public String getUnLockAccountMoneyVip() {
        try {
            return systemConfigEntity.getContent().getGetAccountMoneyVip();
        } catch (Exception e) {
            return "";
        }
    }
}