package com.dl.playfun.manager;


import android.text.TextUtils;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.EaringlSwitchUtil;
import com.dl.playfun.app.Injection;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.EvaluateObjEntity;
import com.dl.playfun.entity.GameConfigEntity;
import com.dl.playfun.entity.SystemConfigContentEntity;
import com.dl.playfun.entity.SystemConfigEntity;
import com.dl.playfun.entity.SystemConfigTaskEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.utils.ApiUitl;

import java.util.List;
import java.util.Map;

import io.reactivex.schedulers.Schedulers;

/**
 * 配置管理
 *
 * @author wulei
 */
public class ConfigManager {

    private static ConfigManager mCacheManager;


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


    /**
     * @return java.lang.String
     * @Desc TODO(获取个人资料id)
     * @author 彭石林
     * @parame []
     * @Date 2022/6/7
     */
    public String getUserId() {
        UserDataEntity userDataEntity = getAppRepository().readUserData();
        if (userDataEntity == null) {
            return null;
        }
        return String.valueOf(userDataEntity.getId());
    }

    private static final class AppRepositoryHolder {
        static final AppRepository appRepository = Injection.provideDemoRepository();
    }

    /**
     * @Desc TODO(获取操作API库)
     * @author 彭石林
     * @parame []
     * @return com.dl.playfun.data.AppRepository
     * @Date 2022/3/31
     */
    public AppRepository getAppRepository(){
        return AppRepositoryHolder.appRepository;
    }

    /**
    * @Desc TODO(查询用户登录渠道)
    * @author 彭石林
    * @parame []
    * @return java.lang.Integer
    * @Date 2022/11/15
    */
    public Integer getLoginSource(){
        String loginType = getAppRepository().readKeyValue(AppConfig.LOGIN_TYPE);
        Integer source = null;
        if(TextUtils.isEmpty(loginType)){
            return source;
        }
        switch (loginType) {
            case "facebook":
                source = 1;
                break;
            case "google":
                source = 2;
                break;
            case "phone":
                source = 4;
                break;
        }
        return source;
    }

    //位置是否是空的
    public boolean isLocation() {
        try {
            if (getAppRepository().readUserData() != null) {
                return getAppRepository().readUserData().getPermanentCityIds().isEmpty();
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }

    //获取任务中心配置
    public SystemConfigTaskEntity getTaskConfig() {
        return getAppRepository().readSystemConfigTask();
    }

    //收益开关 打开显示收益
    public boolean getTipMoneyShowFlag() {
        return getAppRepository().readSwitches(EaringlSwitchUtil.KEY_TIPS) == 1;
    }

    //收入开关
    public boolean getRemoveImMessageFlag() {
        return getAppRepository().readSwitches(EaringlSwitchUtil.REMOVE_IM_MESSAGE).intValue() == 1;
    }

    //广场-屏蔽动态开关
    public boolean getBroadcastSquareDislikeFlag(){
        return getAppRepository().readSwitches(EaringlSwitchUtil.KEY_SQUARE_DISLIKE) == 1;
    }

    /**
    * @Desc TODO(获取发送付费照片的快照预览时间)
    * @author 彭石林
    * @parame []
    * @return long
    * @Date 2022/9/21
    */
    public long mediaGallerySnapshotUnLockTime() {
        long snapshotTime = 2;
        int multiply = 3;
        //当前用户信息
        UserDataEntity userDataEntity = getAppRepository().readUserData();
        if(userDataEntity==null){
            return snapshotTime;
        }
        //配置标
        SystemConfigEntity readSystemConfig = getAppRepository().readSystemConfig();
        if(readSystemConfig==null){
            return snapshotTime;
        }
        //普通预览时间
        Integer localSnapshotTime = readSystemConfig.getPhotoSnapshotTime();
        //vip预览时间
        Integer localSnapshotVIPTime = readSystemConfig.getPhotoSnapshotVIPTime();
        if(userDataEntity.getSex() != null && userDataEntity.getSex()==1){//男性
            if(userDataEntity.getIsVip()!=null && userDataEntity.getIsVip()==1){//是vip
                //VIP预览快照过期时间
                if(localSnapshotVIPTime == null){
                    //普通预览时间不存在 vip可预览时间 = 普通时间*3
                    return localSnapshotTime == null ? snapshotTime : snapshotTime * multiply;
                }else{
                    return localSnapshotVIPTime;
                }
            }else{
                return localSnapshotTime == null ? snapshotTime : localSnapshotTime;
            }
        }else{//女性
            return localSnapshotTime == null ? snapshotTime : localSnapshotTime;
        }
    }
    /**
    * @Desc TODO(交友意愿入口开关)
    * @author 彭石林
    * @return boolean
    * @Date 2022/10/18
    */
    public boolean interestSwitch(){
        //配置标
        SystemConfigEntity readSystemConfig = getAppRepository().readSystemConfig();
        if(readSystemConfig==null){
            return false;
        }
        //交友意愿
        Integer interestSwitch = readSystemConfig.getInterest();
        if(interestSwitch != null){
            return interestSwitch == 1;
        }
        return false;
    }

    /**
    * @Desc TODO(获取交友意愿跳转H5)
    * @author 彭石林
    * @parame []
    * @return java.lang.String
    * @Date 2022/10/19
    */
    public String getInterestWebUrl(){
        try {
            return getAppRepository().readApiConfigManagerEntity().getPlayChatWebUrl()+"/friendsWill/";
        }catch(Exception e){
            return null;
        }
    }

    /**
     * 返回用户上级ID
     *
     * @return
     */
    public Integer getUserParentId() {
        return getAppRepository().readUserData().getpId();
    }

    /**
     * @Desc TODO(获取当前用户IM id)
     * @author 彭石林
     * @parame []
     * @return java.lang.String
     * @Date 2022/4/2
     */
    public String getUserImID(){
        return  getAppRepository().readUserData().getImUserId();
    }

    /**
     * 是否男性
     *
     * @return
     */
    public boolean isMale() {
        boolean isMale = false;
        try {
            isMale = getAppRepository().readUserData().getSex() == 1;
        }catch(Exception ignored){

        }
        return isMale;
    }

    /**
     * 获取个人头像
     *
     * @return
     */
    public String getAvatar() {
        return getAppRepository().readUserData().getAvatar();
    }


    /**
     * 是否新用户
     */
    public boolean isNewUser(){
        return getAppRepository().readIsNewUser();
    }

    /**
     * 是否VIP
     *
     * @return
     */
    public boolean isVip() {
        return getAppRepository().readUserData().getIsVip() == 1;
    }

    /**
     * 是否真人
     *
     * @return
     */
    public boolean isCertification() {
        try{
            return getAppRepository().readUserData().getCertification() == 1;
        }catch (Exception e){
            return false;
        }

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
                money = getAppRepository().readSystemConfig().getManVip().getImMoney();
            } else if (isCertification()) {
                money = getAppRepository().readSystemConfig().getManReal().getImMoney();
            } else {
                money = getAppRepository().readSystemConfig().getManUser().getImMoney();
            }
        } else {
            if (isVip()) {
                money = getAppRepository().readSystemConfig().getWomanVip().getImMoney();
            } else if (isCertification()) {
                money = getAppRepository().readSystemConfig().getWomanReal().getImMoney();
            } else {
                money = getAppRepository().readSystemConfig().getWomanUser().getImMoney();
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
        try{
            if (isMale()) {
                if (isVip()) {
                    time = getAppRepository().readSystemConfig().getManVip().getImgTime();
                } else if (isCertification()) {
                    time = getAppRepository().readSystemConfig().getManReal().getImgTime();
                } else {
                    time = getAppRepository().readSystemConfig().getManUser().getImgTime();
                }
            } else {
                if (isVip()) {
                    time = getAppRepository().readSystemConfig().getWomanVip().getImgTime();
                } else if (isCertification()) {
                    time = getAppRepository().readSystemConfig().getWomanReal().getImgTime();
                } else {
                    time = getAppRepository().readSystemConfig().getWomanUser().getImgTime();
                }
            }
        }catch (Exception ignored){

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
        List<GameConfigEntity> gameConfigEntities = getAppRepository().readGameConfig();

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
                num = getAppRepository().readSystemConfig().getManVip().getSendMessagesNumber();
            } else if (isCertification()) {
                num = getAppRepository().readSystemConfig().getManReal().getSendMessagesNumber();
            } else {
                num = getAppRepository().readSystemConfig().getManUser().getSendMessagesNumber();
            }
        } else {
            if (isVip()) {
                num = getAppRepository().readSystemConfig().getWomanVip().getSendMessagesNumber();
            } else if (isCertification()) {
                num = getAppRepository().readSystemConfig().getWomanReal().getSendMessagesNumber();
            } else {
                num = getAppRepository().readSystemConfig().getWomanUser().getSendMessagesNumber();
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
                num = getAppRepository().readSystemConfig().getManVip().getViewMessagesNumber();
            } else if (isCertification()) {
                num = getAppRepository().readSystemConfig().getManReal().getViewMessagesNumber();
            } else {
                num = getAppRepository().readSystemConfig().getManUser().getViewMessagesNumber();
            }
        } else {
            if (isVip()) {
                num = getAppRepository().readSystemConfig().getWomanVip().getViewMessagesNumber();
            } else if (isCertification()) {
                num = getAppRepository().readSystemConfig().getWomanReal().getViewMessagesNumber();
            } else {
                num = getAppRepository().readSystemConfig().getWomanUser().getViewMessagesNumber();
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
        Integer num = getAppRepository().readSystemConfig().getContent().getReadLineTime();
        return num;
    }

    /**
     * 读取主页消耗次数
     *
     * @return
     */
    public Integer getGetUserHomeMoney() {
        return getAppRepository().readSystemConfig().getContent().getGetUserHomeMoney();
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
                number = getAppRepository().readSystemConfig().getManVip().getImNumber();
            } else if (isCertification()) {
                number = getAppRepository().readSystemConfig().getManReal().getImNumber();
            } else {
                number = getAppRepository().readSystemConfig().getManUser().getImNumber();
            }
        } else {
            if (isVip()) {
                number = getAppRepository().readSystemConfig().getWomanVip().getImNumber();
            } else if (isCertification()) {
                number = getAppRepository().readSystemConfig().getWomanReal().getImNumber();
            } else {
                number = getAppRepository().readSystemConfig().getWomanUser().getImNumber();
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
        return getAppRepository().readSystemConfig().getContent().getGetViewUseBrowseMoney();
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
                money = getAppRepository().readSystemConfig().getManVip().getNewsMoney();
            } else if (isCertification()) {
                money = getAppRepository().readSystemConfig().getManReal().getNewsMoney();
            } else {
                money = getAppRepository().readSystemConfig().getManUser().getNewsMoney();
            }
        } else {
            if (isVip()) {
                money = getAppRepository().readSystemConfig().getWomanVip().getNewsMoney();
            } else if (isCertification()) {
                money = getAppRepository().readSystemConfig().getWomanReal().getNewsMoney();
            } else {
                money = getAppRepository().readSystemConfig().getWomanUser().getNewsMoney();
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
                money = getAppRepository().readSystemConfig().getManVip().getTopicalMoney();
            } else if (isCertification()) {
                money = getAppRepository().readSystemConfig().getManReal().getTopicalMoney();
            } else {
                money = getAppRepository().readSystemConfig().getManUser().getTopicalMoney();
            }
        } else {
            if (isVip()) {
                money = getAppRepository().readSystemConfig().getWomanVip().getTopicalMoney();
            } else if (isCertification()) {
                money = getAppRepository().readSystemConfig().getWomanReal().getTopicalMoney();
            } else {
                money = getAppRepository().readSystemConfig().getWomanUser().getTopicalMoney();
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
                number = getAppRepository().readSystemConfig().getManVip().getBrowseHomeNumber();
            } else if (isCertification()) {
                number = getAppRepository().readSystemConfig().getManReal().getBrowseHomeNumber();
            } else {
                number = getAppRepository().readSystemConfig().getManUser().getBrowseHomeNumber();
            }
        } else {
            if (isVip()) {
                number = getAppRepository().readSystemConfig().getWomanVip().getBrowseHomeNumber();
            } else if (isCertification()) {
                number = getAppRepository().readSystemConfig().getWomanReal().getBrowseHomeNumber();
            } else {
                number = getAppRepository().readSystemConfig().getWomanUser().getBrowseHomeNumber();
            }
        }
        return number;
    }

    public String getUnLockAccountMoney() {
        try {
            return getAppRepository().readSystemConfig().getContent().getGetAccountMoney();
        } catch (Exception e) {
            return "";
        }
    }

    public String getUnLockAccountMoneyVip() {
        try {
            return getAppRepository().readSystemConfig().getContent().getGetAccountMoneyVip();
        } catch (Exception e) {
            return "";
        }
    }

    /**
    * @Desc TODO(上报)
    * @author 彭石林
    * @parame [mapData]
    * @return void
    * @Date 2022/10/18
    */
    public void localeOrderReport(Map mapData){
        getAppRepository().localeOrderReport(ApiUitl.getBody(GsonUtils.toJson(mapData)))
                .subscribeOn(Schedulers.io())
                .subscribe(new BaseObserver<BaseResponse>(){
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {

                    }

                    @Override
                    public void onError(RequestException e) {
                    }
                });
    }
}