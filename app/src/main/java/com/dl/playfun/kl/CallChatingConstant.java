package com.dl.playfun.kl;

import com.blankj.utilcode.util.GsonUtils;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.utils.ApiUitl;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.utils.RxUtils;


public class CallChatingConstant {

    //目前服务器端只处理 -1,102, 103, 104
    // 取消通话(拿了房间号但没调腾讯，自定义的状态码)
    public static final int room_chanel= -1;
    //解散房间
    public static final int dissolveRoom = 102;
    //进入房间
    public static final int enterRoom = 103;
    //开始推送音频数据
    public static final int pusherAudioStart = 10000;

    //更新通话状态
    public static void updateCallingStatus(int roomId, String roomIdStr, int eventType){
//        roomId	否	Integer	房间ID
//        roomIdStr	否	String	字符串形式的房间ID
//        eventType	是	Integer
        Map<String,Object> mapData = new HashMap<>();
        mapData.put("roomId",roomId);
        mapData.put("roomIdStr",roomIdStr);
        mapData.put("eventType",eventType);
        ConfigManager.getInstance().getAppRepository().updateCallingStatus(ApiUitl.getBody(GsonUtils.toJson(mapData)))
                .subscribeOn(Schedulers.io())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {

                    }

                    @Override
                    public void onError(RequestException e) {
                    }
                });
    }

    /**
     * 通话中 -推币机进入、退出围观
     * @param roomId
     * @param toUserId
     * @param type
     */
    public static void updateCoinPusherWatchRoom(Integer roomId, Integer toUserId, int type){
        ConfigManager.getInstance().getAppRepository().coinPusherWatchRoom(roomId, toUserId, type)
                .subscribeOn(Schedulers.io())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {

                    }

                    @Override
                    public void onError(RequestException e) {
                    }
                });
    }

}
