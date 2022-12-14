package com.dl.playfun.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.Purchase;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.Utils;
import com.dl.playfun.R;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseDisposableObserver;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.AllConfigEntity;
import com.dl.playfun.entity.LocalGooglePayCache;
import com.dl.playfun.entity.SwitchesEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.event.LoginExpiredEvent;
import com.dl.playfun.tim.TUIUtils;
import com.dl.playfun.ui.MainContainerActivity;
import com.dl.playfun.utils.ExceptionReportUtils;
import com.faceunity.nama.FURenderer;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.MaterialHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshFooter;
import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.DefaultRefreshFooterCreator;
import com.scwang.smart.refresh.layout.listener.DefaultRefreshHeaderCreator;
import com.scwang.smart.refresh.layout.listener.DefaultRefreshInitializer;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMConversationListener;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMSDKConfig;
import com.tencent.imsdk.v2.V2TIMSDKListener;
import com.tencent.imsdk.v2.V2TIMUserFullInfo;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.tencent.mmkv.MMKV;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import me.goldze.mvvmhabit.base.BaseApplication;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.crash.CaocConfig;
import me.goldze.mvvmhabit.utils.KLog;
import me.goldze.mvvmhabit.utils.RxUtils;


/**
 * @author wulei
 */
public class AppContext extends Application {

    public static final String TAG = "AppContext";
    public static String currPage = "not_in";
    public static boolean isHomePage = false;
    public static boolean isShowNotPaid = false;
    public static boolean isCalling = false;
    public static Handler sUiThreadHandler;
    private static AppContext instance;
    private static Thread sUiThread;

    static {
        //????????????????????????????????????????????????????????????????????????
        SmartRefreshLayout.setDefaultRefreshInitializer(new DefaultRefreshInitializer() {
            @Override
            public void initialize(@NonNull Context context, @NonNull RefreshLayout layout) {
                //??????????????????????????????????????????????????????DefaultRefreshHeaderCreator?????????
                //???????????????????????????????????????????????????
                layout.setEnableLoadMoreWhenContentNotFull(false);
                //??????????????????????????????
                layout.setReboundDuration(300);
//                layout.setFooterHeight(100);
                //?????????????????????????????????????????????
                layout.setDisableContentWhenLoading(false);
                //?????????????????????????????????????????????
                layout.setEnableOverScrollDrag(true);
                layout.setPrimaryColorsId(R.color.colorPrimary, android.R.color.white);
            }
        });

        //???????????????Header?????????
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
                return new MaterialHeader(context);
            }
        });

        //???????????????Footer?????????
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @Override
            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
                //???????????????Footer???????????? BallPulseFooter
                ClassicsFooter footer = new ClassicsFooter(context);
                footer.REFRESH_FOOTER_PULLING = context.getString(R.string.playfun_srl_footer_pulling);//"??????????????????";
                footer.REFRESH_FOOTER_RELEASE = context.getString(R.string.playfun_srl_footer_release);//"??????????????????";
                footer.REFRESH_FOOTER_LOADING = context.getString(R.string.playfun_srl_footer_loading);//"????????????...";
                footer.REFRESH_FOOTER_REFRESHING = context.getString(R.string.playfun_srl_footer_refreshing);//"????????????...";
                footer.REFRESH_FOOTER_FINISH = context.getString(R.string.playfun_srl_footer_finish);//"????????????";
                footer.REFRESH_FOOTER_FAILED = context.getString(R.string.playfun_srl_footer_failed);//"????????????";
                footer.REFRESH_FOOTER_NOTHING = context.getString(R.string.playfun_srl_footer_nothing);//"?????????????????????";
                footer.setDrawableSize(20);
                return footer;
            }
        });

    }

    public AppRepository appRepository;
    public FirebaseAnalytics mFirebaseAnalytics;

    public static AppContext instance() {
        return instance;
    }

    /**
     * ??????????????????
     *
     * @param work Runnable??????
     */
    public static void runOnUIThread(Runnable work) {
        if (Thread.currentThread() != sUiThread) {
            sUiThreadHandler.post(work);
        } else {
            work.run();
        }
    }

    public static void runOnUIThread(Runnable work, long delayMillis) {
        sUiThreadHandler.postDelayed(work, delayMillis);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //??????????????????
        FURenderer.getInstance().setup(this);

        try {
            File cacheDir = new File(this.getApplicationContext().getExternalCacheDir().getPath(), "https");
            HttpResponseCache.install(cacheDir, 1024 * 1024 * 128);
        } catch (IOException e) {
            e.printStackTrace();
        }
        instance = this;
        // ?????????
        sUiThread = Thread.currentThread();
        // ????????????Handler
        sUiThreadHandler = new Handler();
        BaseApplication.setApplication(this);

        FirebaseApp firebase = FirebaseApp.initializeApp(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //????????????????????????
        KLog.init(AppConfig.isDebug);

        Utils.init(this);

        MMKV.initialize(this);

        appRepository = Injection.provideDemoRepository();

        //??????????????????????????????
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //????????????,???????????????
                .enabled(true) //??????????????????????????????
                .showErrorDetails(AppConfig.isDebug) //??????????????????????????????
                .showRestartButton(true) //????????????????????????
                .trackActivities(AppConfig.isDebug) //????????????Activity
                .minTimeBetweenCrashesMs(2000) //?????????????????????(??????)
                .errorDrawable(R.mipmap.play_fun_launcher) //????????????
                .restartActivity(MainContainerActivity.class) //??????????????????activity
                //.errorActivity(YourCustomErrorActivity.class) //??????????????????activity
                //.eventListener(new YourCustomEventListener()) //????????????????????????
                .apply();
        initActivityLifecycleCallbacks();

        initIM(AppConfig.IM_APP_KEY);
        loadAllConfig();
    }

    private void initIM(Integer IM_APP_KEY) {

        TUIUtils.init(this, IM_APP_KEY, new V2TIMSDKConfig(), new V2TIMSDKListener() {
            @Override
            public void onConnecting() {
                super.onConnecting();
            }

            @Override
            public void onConnectSuccess() {
                super.onConnectSuccess();
            }

            @Override
            public void onConnectFailed(int code, String error) {
                super.onConnectFailed(code, error);
            }

            @Override
            public void onKickedOffline() {
                super.onKickedOffline();
                RxBus.getDefault().post(new LoginExpiredEvent());
            }

            @Override
            public void onUserSigExpired() {
                super.onUserSigExpired();
                RxBus.getDefault().post(new LoginExpiredEvent());
            }

            @Override
            public void onSelfInfoUpdated(V2TIMUserFullInfo info) {
                super.onSelfInfoUpdated(info);
            }
        });
    }



    //????????????-??????
    public void logEvent(String eventName, String money,Purchase purchase) {
        validateGooglePlayLog(purchase,money);
        logEvent(eventName);
    }

    //??????????????????
    public void logEvent(String eventName) {

        Bundle bundleEvent = new Bundle();
        bundleEvent.putLong(eventName, System.currentTimeMillis());
        bundleEvent.putString("key", "value");
        mFirebaseAnalytics.logEvent(eventName, bundleEvent);
    }

    public void validateGooglePlayLog(Purchase purchase,String money) {
//        context???????????????/???????????????
//        publicKey?????? Google Play Console ????????????????????????
//        signature:???data.INAPP_DATA_SIGNATUREonActivityResult
//        purchaseData:???data.INAPP_PURCHASE_DATAonActivityResult
//        price: ??????????????????????????? skuDetails.getStringArrayList("DETAILS_LIST")
//        currency: ??????????????????????????? skuDetails.getStringArrayList("DETAILS_LIST")
//        additionalParameters - ??????????????????????????????
        Map<String, String> eventValues = new HashMap<>();
        eventValues.put("some_parameter", "some_value");
        //eventValues.put(AFInAppEventParameterName.CONTENT_TYPE, eventName);
    }

    private void initActivityLifecycleCallbacks() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            private final V2TIMConversationListener unreadListener = new V2TIMConversationListener() {
                @Override
                public void onTotalUnreadMessageCountChanged(long totalUnreadCount) {
                    // HUAWEIHmsMessageService.updateBadge(DemoApplication.this, (int) totalUnreadCount);
                }
            };
            private int foregroundActivities = 0;
            private boolean isChangingConfiguration;

            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                KLog.i(TAG, "onActivityCreated bundle: " + bundle);
//                if (bundle != null) { // ???bundle??????????????????????????????
//                    // ??????????????????
//                    Intent intent = new Intent(activity, SplashActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                }
            }

            @Override
            public void onActivityStarted(Activity activity) {
                foregroundActivities++;
                if (foregroundActivities == 1 && !isChangingConfiguration) {
                    // ??????????????????
                    KLog.i(TAG, "application enter foreground");
                    V2TIMManager.getOfflinePushManager().doForeground(new V2TIMCallback() {
                        @Override
                        public void onError(int code, String desc) {
                            KLog.e(TAG, "doForeground err = " + code + ", desc = " + desc);
                        }
                        @Override
                        public void onSuccess() {
                            KLog.i(TAG, "doForeground success");
                        }
                    });
                    V2TIMManager.getConversationManager().removeConversationListener(unreadListener);
                    //MessageNotification.getInstance().cancelTimeout();
                }
                isChangingConfiguration = false;
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                foregroundActivities--;
                if (foregroundActivities == 0) {
                    // ??????????????????
                    Log.i(TAG, "application enter background");
                    V2TIMManager.getConversationManager().getTotalUnreadMessageCount(new V2TIMValueCallback<Long>() {
                        @Override
                        public void onSuccess(Long aLong) {
                            int totalCount = aLong.intValue();
                            V2TIMManager.getOfflinePushManager().doBackground(totalCount, new V2TIMCallback() {
                                @Override
                                public void onError(int code, String desc) {
                                    Log.e(TAG, "doBackground err = " + code + ", desc = " + desc);
                                }

                                @Override
                                public void onSuccess() {
                                    Log.i(TAG, "doBackground success");
                                }
                            });
                        }

                        @Override
                        public void onError(int code, String desc) {

                        }
                    });

                    V2TIMManager.getConversationManager().addConversationListener(unreadListener);
                }
                isChangingConfiguration = activity.isChangingConfigurations();
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    private void loadAllConfig() {
        LocalGooglePayCache localGooglePayCache = appRepository.readGooglePlay();
        if (localGooglePayCache != null) {
            UserDataEntity userDataEntity = appRepository.readUserData();
            if (userDataEntity == null) {
                return;
            }
            if (userDataEntity.getId() != localGooglePayCache.getUserId()) {
                return;
            }
            appRepository.paySuccessNotify(localGooglePayCache.getPackageName(), localGooglePayCache.getOrderNumber(), localGooglePayCache.getProductId(), localGooglePayCache.getToken(), 2, 50)
                    .compose(RxUtils.schedulersTransformer())
                    .compose(RxUtils.exceptionTransformer())
                    .subscribe(new BaseObserver<BaseResponse>() {
                        @Override
                        public void onSuccess(BaseResponse baseResponse) {
                            appRepository.clearGooglePayCache();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }

    public void pushDeviceToken(String deviceToken) {
        appRepository.pushDeviceToken(deviceToken, AppConfig.VERSION_NAME)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseDisposableObserver<BaseResponse>() {

                    @Override
                    public void onSuccess(BaseResponse response) {
                        Log.e("????????????Token", "??????");
                    }

                    @Override
                    public void onError(RequestException e) {
                        e.printStackTrace();
                        Log.e("????????????Token", "??????");
                    }

                    @Override
                    public void onComplete() {
                        System.out.println();
                    }

                });
    }


    /**
    * @Desc TODO(????????????????????????)
    * @author ?????????
    * @parame [state]
    * @return void
    * @Date 2022/1/15
    */
    public void setGameState(int state) {
        appRepository.setGameState(state)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseResponse>(){

                    @Override
                    public void onSuccess(BaseResponse baseResponse) {

                    }
                });
    }

}
