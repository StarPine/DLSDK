package com.mvsee.myapplication;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.dl.playfun.api.AuthLoginResultListener;
import com.dl.playfun.api.PlayFunAuthUserEntity;
import com.dl.playfun.api.PlayFunLoginViewHorizontal;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.entity.GamePayEntity;
import com.dl.playfun.entity.GamePhotoAlbumEntity;
import com.dl.playfun.event.GameHeartBeatEvent;
import com.dl.playfun.event.GameLoginExpiredEvent;
import com.dl.playfun.event.LoginExpiredEvent;
import com.dl.playfun.event.UserDisableEvent;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.MainContainerActivity;
import com.dl.playfun.widget.dialog.MVDialog;
import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.dl.playfun.api.PlayFunUserApiUtil;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.Injection;
import com.dl.playfun.app.config.TbarCenterImgConfig;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.AuthLoginUserEntity;
import com.dl.playfun.entity.RoleInfoEntity;
import com.dl.playfun.entity.TokenEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.ui.PlayFunFloatImgView;
import com.dl.playfun.utils.ApiUitl;

import java.util.ArrayList;
import java.util.List;

import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Author: 彭石林
 * Description: This is MainActivity
 */
public class MainActivity extends Activity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private Context mContext;
    private boolean loginSuccess = false;

    private Button btn_register;
    private Button queren;
    private PlayFunFloatImgView to_play_fun;
    private EditText edit_text;
    private RadioGroup radioGroupId;
    private LinearLayout linearLayout;
    private PlayFunLoginViewHorizontal dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        btn_register = findViewById(R.id.btn_register);
        to_play_fun = findViewById(R.id.to_play_fun);
        edit_text = findViewById(R.id.edit_text);
        radioGroupId = findViewById(R.id.radioGroupId);
        linearLayout = findViewById(R.id.linearLayout);
        queren = findViewById(R.id.queren);
        btn_register.setOnClickListener(this);
        radioGroupId.setOnCheckedChangeListener(this);
        TbarCenterImgConfig.getInstance().setImgSrcPath(R.drawable.miao_you);

        queren.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Injection.provideDemoRepository().v2Login(edit_text.getText().toString(), "000666", ApiUitl.getAndroidId())
                        .compose(RxUtils.schedulersTransformer())
                        .compose(RxUtils.exceptionTransformer())
                        .subscribe(new BaseObserver<BaseDataResponse<UserDataEntity>>() {
                            @Override
                            public void onSuccess(BaseDataResponse<UserDataEntity> response) {
                                UserDataEntity  authLoginUserEntity = response.getData();
                                if (authLoginUserEntity != null && authLoginUserEntity.getIsNewUser() != null && authLoginUserEntity.getIsNewUser().intValue() == 1) {
                                    AppContext.instance().logEvent(AppsFlyerEvent.register_start);
                                    Injection.provideDemoRepository().saveIsNewUser(true);
                                }
                                TokenEntity tokenEntity = new TokenEntity(authLoginUserEntity.getToken(),authLoginUserEntity.getUserID(),authLoginUserEntity.getUserSig(), authLoginUserEntity.getIsContract());
                                ConfigManager.getInstance().getAppRepository().saveLoginInfo(tokenEntity);
                                AppContext.instance().logEvent(AppsFlyerEvent.LOG_IN_WITH_PHONE_NUMBER);
                                AppContext.instance().mFirebaseAnalytics.setUserId(String.valueOf(authLoginUserEntity.getId()));
                                AppContext.instance().appRepository.saveUserData(authLoginUserEntity);
                                if (authLoginUserEntity.getCertification() == 1) {
                                    AppContext.instance().appRepository.saveNeedVerifyFace(true);
                                }
                                loginSuccess = true;
                                to_play_fun.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            }
        });
        registerRxBus();

//        GamePayEntity gamePayEntity = new GamePayEntity();
//        gamePayEntity.setPackageName("com.ru"); //purchase.getPackageName()
//        gamePayEntity.setOrderNumber("20220210"); //游戏订单号
//        gamePayEntity.setProductId(null); //purchase.getSkus() 订单产品ID
//        gamePayEntity.setToken("tokenasdasdasd"); //purchase.getPurchaseToken(); 订单token
//        gamePayEntity.setEvent(1); //billingResult.getResponseCode() 支付状态
//        gamePayEntity.setServerId("12312312"); //区服ID
//        gamePayEntity.setRoleId("12313"); //角色ID
//        PlayFunUserApiUtil.getInstance().commitGamePaySuccessNotify(gamePayEntity, new PlayFunUserApiUtil.CommitGamePayListener() {
//            @Override
//            public void onSuccess() {
//                //接口调用成功。后台会效验通知发货接口到游戏后台
//            }
//
//            @Override
//            public void onError(RequestException e) {
//                //接口调用事变
//                //e.getMessage() 异常信息
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        to_play_fun.setVisibility(View.GONE);
    }

    /**
     * 加载用户资料
     */
    private void loadProfile() {
        //RaJava模拟登录
        Injection.provideDemoRepository().getUserData()
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<UserDataEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<UserDataEntity> response) {
                        UserDataEntity userDataEntity = response.getData();
                        AppContext.instance().mFirebaseAnalytics.setUserId(String.valueOf(userDataEntity.getId()));
                        ConfigManager.getInstance().getAppRepository().saveUserData(userDataEntity);
                        if (userDataEntity.getCertification() == 1) {
                            ConfigManager.getInstance().getAppRepository().saveNeedVerifyFace(true);
                        }
                        loginSuccess = true;
                        to_play_fun.setVisibility(View.VISIBLE);
                        //PlayFunUserApiUtil.getInstance().startPlayFunActivity(mContext);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register:
                commitRoleInfo();
                break;
        }
    }

    public void commitRoleInfo() {
        //PlayFunUserApiUtil.getInstance().saveSourcesHead("区服id","角色id");
//                 serverId 是 String 区服ID ， 最多45个字符
//                *roleId 是 String 角色ID ， 最多45个字符
//                *roleName 是 String 角色名称 ， 最多100个字符
//                *avatarUrl 是 String 角色头像URL地址 ， 最多1000个字符)
//        RoleInfoEntity roleInfoEntity = new RoleInfoEntity();
//        roleInfoEntity.setServerId("m000123"); //String 区服ID ， 最多45个字符
//        roleInfoEntity.setRoleId("my1001"); //String 角色ID ， 最多45个字符
//        roleInfoEntity.setRoleName("喵游测试2");//String 角色名称 ， 最多100个字符
//        roleInfoEntity.setAvatarUrl("https://img-blog.csdnimg.cn/20190124200328756.jpg");//String 角色头像URL地址 ， 最多1000个字符)
//        PlayFunUserApiUtil.getInstance().commitRoleInfo(roleInfoEntity, new PlayFunUserApiUtil.CommitRoleInfoListener() {
//            @Override
//            public void RoleInfoCallback(boolean flagType) {
//                if (flagType) {
//                    Log.e("提交角色返回成功", flagType + "==========");
//                } else {
//                    Log.e("提交角色返回失败", flagType + "==========");
//                }
//            }
//        });
        String serverId = null;// serverId 是 String 区服ID 必传
        String roleId = null; // roleId 是 String 角色ID 必传
        PlayFunUserApiUtil.getInstance().getGamePhotoAlbumList(serverId,roleId,new PlayFunUserApiUtil.GamePhotoAlbumListener(){
            @Override
            public void onSuccess(List<String> listData) {
                //返回的是 https全路径
                Log.e("查看相册成功",String.valueOf(listData));
            }

            @Override
            public void OnError(RequestException e) {
                Log.e("请求失败","============");
            }
        });
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.haiwai:
                linearLayout.setVisibility(View.GONE);
                if (dialog == null)
                    dialog = new PlayFunLoginViewHorizontal();
                dialog.show(getFragmentManager(), "DialogLoginApi");
                dialog.setLoginResultListener(new AuthLoginResultListener() {
                    @Override
                    public void authLoginSuccess(PlayFunAuthUserEntity playFunAuthUserEntity) {
                        dialog.dismiss();
                        //PlayFunUserApiUtil.getInstance().startPlayFunActivity(mContext);
                        to_play_fun.setVisibility(View.VISIBLE);
                        Log.e("是否绑定过游戏",playFunAuthUserEntity.getIsBindGame()+"=======");
                        //登录渠道  1 facebook登录 2 google  3 Line  4 VK  5 游客
                        Log.e("登录成功返回信息","登录渠道："+playFunAuthUserEntity.getTypeLogin());
                        Log.e("登录成功返回信息","详细内容："+playFunAuthUserEntity.toString());
                    }

                    @Override
                    public void authLoginError(int code, int type, String message) {
                        Log.e("登录成功返回信息",code+"=="+type+"失败："+message);
                    }
                });
                break;
            case R.id.phone:
                linearLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void registerRxBus() {
        RxBus.getDefault().toObservable(GameHeartBeatEvent.class)
                .subscribe(gameHeartBeatEvent -> {
                    Log.e("订阅心跳消息通知为",gameHeartBeatEvent.getDoTime());
                });
        RxBus.getDefault().toObservable(GameLoginExpiredEvent.class)
                .subscribe(gameHeartBeatEvent -> {
                    //playfun账号掉线。（其它手机使用当前google、facebook登录。会触发）
                    //可延迟0.5秒调用。避免playfun页面返回到游戏页面时。出现页面跳转问题
                });
    }
}
