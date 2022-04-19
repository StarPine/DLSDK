package com.dl.playfun.api;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.Injection;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.GamePayEntity;
import com.dl.playfun.entity.GamePhotoAlbumEntity;
import com.dl.playfun.entity.RoleInfoEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.MainContainerActivity;
import com.dl.playfun.utils.AESUtil;
import com.dl.playfun.utils.ApiUitl;
import com.tencent.qcloud.tuicore.util.ConfigManagerUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * Author: 彭石林
 * Time: 2022/1/17 15:12
 * Description: This is PlayFunUserApiUtil
 */
public class PlayFunUserApiUtil {
    private static PlayFunUserApiUtil INSTANCE = null;

    public static PlayFunUserApiUtil getInstance(){
        if(INSTANCE==null){
            synchronized (PlayFunUserApiUtil.class){
                INSTANCE = new PlayFunUserApiUtil();
            }
        }
        return  INSTANCE;
    }
    /**
    * @Desc TODO(保存游戏配置)
    * @author 彭石林
    * @parame [appGameConfig]
    * @return void
    * @Date 2022/4/19
    */
    public void saveGameConfigSetting(@NonNull @NotNull AppGameConfig appGameConfig){
        ConfigManager.getInstance().getAppRepository().saveGameConfigSetting(appGameConfig);
    }

    /**
    * @Desc TODO(保存游戏区服id、角色ID到本地添加注入)
    * @author 彭石林
    * @parame [serverId, roleId]
    * @return void
    * @Date 2022/2/12
    */
    public void saveSourcesHead(@NonNull @NotNull String serverId, @NonNull @NotNull String roleId){
        HashMap<String,String> mapData = new HashMap<>();
        mapData.put("serverId", AESUtil.encrypt_AES(AppConfig.GAME_SOURCES_AES_KEY, serverId, AppConfig.GAME_SOURCES_AES_KEY));
        mapData.put("roleId",AESUtil.encrypt_AES(AppConfig.GAME_SOURCES_AES_KEY, roleId, AppConfig.GAME_SOURCES_AES_KEY));
        Injection.provideDemoRepository().putKeyValue(AppConfig.GAME_SOURCES_HEADER_KEY,GsonUtils.toJson(mapData));
    }

    /**
    * @Desc TODO(保存游戏Activity路径。供playfun返回调用)
    * @author 彭石林
    * @parame [activityName]
    * @return void
    * @Date 2022/3/29
    */
    public void saveSourcesGameActivityName(@NonNull @NotNull String activityName){
        Injection.provideDemoRepository().putKeyValue(AppConfig.GAME_SOURCES_ACTIVITY_NAME,activityName);
    }

    /**
    * @Desc TODO(PlayFun返回游戏页面)
    * @author 彭石林
    * @parame [mContext]
    * @return void
    * @Date 2022/3/29
    */
    public void toPlayGameView(Context mContext){
        String gameActivityName = Injection.provideDemoRepository().readKeyValue(AppConfig.GAME_SOURCES_ACTIVITY_NAME);
        AppContext.instance().setGameState(1);
        ConfigManagerUtil.getInstance().putPlayGameFlag(true);
        if(gameActivityName!=null){
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(mContext.getApplicationContext(), gameActivityName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
        ((Activity)mContext).finish();
    }

    /**
    * @Desc TODO(游戏跳转进入playFun页面)
    * @author 彭石林
    * @parame [mContext]
    * @return void
    * @Date 2022/1/24
    */
    public void startPlayFunActivity(Context mContext){
        //是否在游戏中
        ConfigManagerUtil.getInstance().putPlayGameFlag(false);
        //游戏中
        AppContext.instance().setGameState(-1);
        Intent intent = new Intent(mContext, MainContainerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //出栈动画
        //((Activity)mContext).overridePendingTransition(R.anim.slide_in_left, R.anim.slide_in_left);

        mContext.startActivity(intent);
    }

    /**
    * @Desc TODO(获取相册列表)
    * @author 彭石林
    * @parame [serverId, roleId, gamePhotoAlbumListener]
    * @return void
    * @Date 2022/1/24
    */
    public void getGamePhotoAlbumList(@NonNull @NotNull String serverId, @NonNull @NotNull String roleId, GamePhotoAlbumListener gamePhotoAlbumListener){
        Injection.provideDemoRepository().getGamePhotoAlbumList(serverId,roleId)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<GamePhotoAlbumEntity>>(){
                    @Override
                    public void onSuccess(BaseDataResponse<GamePhotoAlbumEntity> response) {
                        if(ObjectUtils.isEmpty(response.getData().getImageUrls())){
                            gamePhotoAlbumListener.onSuccess(new ArrayList<>());
                        }else{
                            gamePhotoAlbumListener.onSuccess(response.getData().getImageUrls());
                        }
                    }
                    public void onError(RequestException e) {
                        gamePhotoAlbumListener.OnError(e);
                    }
                });
    }
    /**
    * @Desc TODO(提交角色接口)
    * @author 彭石林
    * @parame [roleInfoEntity, commitRoleInfoListener]
    * @return void
    */
    public void commitRoleInfo(RoleInfoEntity roleInfoEntity,CommitRoleInfoListener commitRoleInfoListener){
        Injection.provideDemoRepository().commitRoleInfo(ApiUitl.getBody(GsonUtils.toJson(roleInfoEntity)))
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseResponse>(){
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {
                        commitRoleInfoListener.RoleInfoCallback(true);
                    }
                    @Override
                    public void onError(RequestException e) {
                        commitRoleInfoListener.RoleInfoCallback(false);
                    }
                });
    }

    /**
    * @Desc TODO(游戏支付验签接口)
    * @author 彭石林
    * @parame [gamePayEntity, commitGamePayListener]
    * @return void
    * @Date 2022/2/10
    */
    public void commitGamePaySuccessNotify(GamePayEntity gamePayEntity,CommitGamePayListener commitGamePayListener){
        Injection.provideDemoRepository()
                .GamePaySuccessNotify(
                        gamePayEntity.getPackageName(),
                        gamePayEntity.getOrderNumber(),
                        gamePayEntity.getProductId(),
                        gamePayEntity.getToken(),
                        1,
                        gamePayEntity.getEvent(),
                        gamePayEntity.getServerId(),
                        gamePayEntity.getRoleId())
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseResponse>(){
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {
                        commitGamePayListener.onSuccess();
                    }
                    @Override
                    public void onError(RequestException e) {
                        commitGamePayListener.onError(e);
                    }
                });
    }

    public interface CommitGamePayListener{
        void onSuccess();
        void onError(RequestException e);
    }

    public interface GamePhotoAlbumListener{
        void onSuccess(List<String> listData);
        void OnError(RequestException e);
    }

    public interface CommitRoleInfoListener{
        void RoleInfoCallback(boolean flagType);
    }
}
