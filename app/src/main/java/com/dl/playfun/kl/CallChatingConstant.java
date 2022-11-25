package com.dl.playfun.kl;

import com.blankj.utilcode.util.GsonUtils;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.utils.ApiUitl;

import java.util.HashMap;
import java.util.Map;


public class CallChatingConstant {

    //目前服务器端只处理 -1,102, 103, 104
    // 取消通话(拿了房间号但没调腾讯，自定义的状态码)
    public static final int room_chanel= -1;
    //创建房间
    public static final int chanel= 101;
    //解散房间
    public static final int dissolveRoom = 102;
    //进入房间
    public static final int enterRoom = 103;
    //退出房间
    public static final int exitRoom = 104;
    //切换角色
    public static final int changeUser = 105;
    //开始推送视频数据
    public static final int pusherVideoStart = 201;
    //停止推送视频数据
    public static final int pusherVideoStop = 202;
    //开始推送音频数据
    public static final int pusherAudioStart = 203;
    //停止推送音频数据
    public static final int pusherAudioStop = 204;
    //开始推送辅路数据
    public static final int pusherSideRoadStart = 205;
    //停止推送辅路数据
    public static final int pusherSideRoadStop = 206;

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
