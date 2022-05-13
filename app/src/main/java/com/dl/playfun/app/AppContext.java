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
        //设置全局默认配置（优先级最低，会被其他设置覆盖）
        SmartRefreshLayout.setDefaultRefreshInitializer(new DefaultRefreshInitializer() {
            @Override
            public void initialize(@NonNull Context context, @NonNull RefreshLayout layout) {
                //开始设置全局的基本参数（可以被下面的DefaultRefreshHeaderCreator覆盖）
                //取消内容不满一页时开启上拉加载功能
                layout.setEnableLoadMoreWhenContentNotFull(false);
                //回弹动画时长（毫秒）
                layout.setReboundDuration(300);
//                layout.setFooterHeight(100);
                //是否在加载的时候禁止列表的操作
                layout.setDisableContentWhenLoading(false);
                //是否启用越界拖动（仿苹果效果）
                layout.setEnableOverScrollDrag(true);
                layout.setPrimaryColorsId(R.color.colorPrimary, android.R.color.white);
            }
        });

        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
                return new MaterialHeader(context);
            }
        });

        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @Override
            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
                //指定为经典Footer，默认是 BallPulseFooter
                ClassicsFooter footer = new ClassicsFooter(context);
                footer.REFRESH_FOOTER_PULLING = context.getString(R.string.playfun_srl_footer_pulling);//"上拉加载更多";
                footer.REFRESH_FOOTER_RELEASE = context.getString(R.string.playfun_srl_footer_release);//"释放立即加载";
                footer.REFRESH_FOOTER_LOADING = context.getString(R.string.playfun_srl_footer_loading);//"正在加载...";
                footer.REFRESH_FOOTER_REFRESHING = context.getString(R.string.playfun_srl_footer_refreshing);//"正在刷新...";
                footer.REFRESH_FOOTER_FINISH = context.getString(R.string.playfun_srl_footer_finish);//"加载完成";
                footer.REFRESH_FOOTER_FAILED = context.getString(R.string.playfun_srl_footer_failed);//"加载失败";
                footer.REFRESH_FOOTER_NOTHING = context.getString(R.string.playfun_srl_footer_nothing);//"没有更多数据了";
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
     * 在主线程执行
     *
     * @param work Runnable对象
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
        //注册美颜渲染
        FURenderer.getInstance().setup(this);

        try {
            File cacheDir = new File(this.getApplicationContext().getExternalCacheDir().getPath(), "https");
            HttpResponseCache.install(cacheDir, 1024 * 1024 * 128);
        } catch (IOException e) {
            e.printStackTrace();
        }
        instance = this;
        // 主线程
        sUiThread = Thread.currentThread();
        // 主线程的Handler
        sUiThreadHandler = new Handler();
        BaseApplication.setApplication(this);

        FirebaseApp firebase = FirebaseApp.initializeApp(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //是否开启日志打印
        KLog.init(AppConfig.isDebug);

        Utils.init(this);

        MMKV.initialize(this);

        appRepository = Injection.provideDemoRepository();

        //配置全局异常崩溃操作
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //背景模式,开启沉浸式
                .enabled(true) //是否启动全局异常捕获
                .showErrorDetails(AppConfig.isDebug) //是否显示错误详细信息
                .showRestartButton(true) //是否显示重启按钮
                .trackActivities(AppConfig.isDebug) //是否跟踪Activity
                .minTimeBetweenCrashesMs(2000) //崩溃的间隔时间(毫秒)
                .errorDrawable(R.mipmap.play_fun_launcher) //错误图标
                .restartActivity(MainContainerActivity.class) //重新启动后的activity
                //.errorActivity(YourCustomErrorActivity.class) //崩溃后的错误activity
                //.eventListener(new YourCustomEventListener()) //崩溃后的错误监听
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



    //上报事件-金额
    public void logEvent(String eventName, String money,Purchase purchase) {
        validateGooglePlayLog(purchase,money);
        logEvent(eventName);
    }

    //上报普通事件
    public void logEvent(String eventName) {

        Bundle bundleEvent = new Bundle();
        bundleEvent.putLong(eventName, System.currentTimeMillis());
        bundleEvent.putString("key", "value");
        mFirebaseAnalytics.logEvent(eventName, bundleEvent);
    }

    public void validateGooglePlayLog(Purchase purchase,String money) {
//        context：应用程序/活动上下文
//        publicKey：从 Google Play Console 获得的许可证密钥
//        signature:从data.INAPP_DATA_SIGNATUREonActivityResult
//        purchaseData:从data.INAPP_PURCHASE_DATAonActivityResult
//        price: 购买价格，应来源于 skuDetails.getStringArrayList("DETAILS_LIST")
//        currency: 购买货币，应来源于 skuDetails.getStringArrayList("DETAILS_LIST")
//        additionalParameters - 要记录的其他事件参数
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
//                if (bundle != null) { // 若bundle不为空则程序异常结束
//                    // 重启整个程序
//                    Intent intent = new Intent(activity, SplashActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                }
            }

            @Override
            public void onActivityStarted(Activity activity) {
                foregroundActivities++;
                if (foregroundActivities == 1 && !isChangingConfiguration) {
                    // 应用切到前台
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
                    // 应用切到后台
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
        if (appRepository.readSystemConfig() == null) {
            AllConfigEntity allConfigEntity = GsonUtils.fromJson(AppConfig.CONFIG_DEFAULT, AllConfigEntity.class);
            appRepository.saveProgramTimeConfig(allConfigEntity.getProgramTime());
            appRepository.saveHeightConfig(allConfigEntity.getHeight());
            appRepository.saveWeightConfig(allConfigEntity.getWeight());
            appRepository.saveReportReasonConfig(allConfigEntity.getReportReason());
            appRepository.saveFemaleEvaluateConfig(allConfigEntity.getEvaluate().getEvaluateFemale());
            appRepository.saveMaleEvaluateConfig(allConfigEntity.getEvaluate().getEvaluateMale());
            appRepository.saveHopeObjectConfig(allConfigEntity.getHopeObject());
            appRepository.saveOccupationConfig(allConfigEntity.getOccupation());
            appRepository.saveCityConfig(allConfigEntity.getCity());
            appRepository.saveSystemConfig(allConfigEntity.getConfig());
            appRepository.saveGameConfig(allConfigEntity.getGame());

        }
        appRepository.getAllConfig()
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseDisposableObserver<BaseDataResponse<AllConfigEntity>>() {

                    @Override
                    public void onSuccess(BaseDataResponse<AllConfigEntity> response) {
                        try {
                            appRepository.saveProgramTimeConfig(response.getData().getProgramTime());
                            appRepository.saveHeightConfig(response.getData().getHeight());
                            appRepository.saveWeightConfig(response.getData().getWeight());
                            appRepository.saveReportReasonConfig(response.getData().getReportReason());
                            appRepository.saveFemaleEvaluateConfig(response.getData().getEvaluate().getEvaluateFemale());
                            appRepository.saveMaleEvaluateConfig(response.getData().getEvaluate().getEvaluateMale());
                            appRepository.saveHopeObjectConfig(response.getData().getHopeObject());
                            appRepository.saveOccupationConfig(response.getData().getOccupation());
                            appRepository.saveCityConfig(response.getData().getCity());
                            appRepository.saveSystemConfig(response.getData().getConfig());
                            appRepository.saveSystemConfigTask(response.getData().getTask());
                            appRepository.saveDefaultHomePageConfig(response.getData().getDefaultHomePage());
                            appRepository.saveGameConfig(response.getData().getGame());
                            appRepository.putSwitches(EaringlSwitchUtil.KEY_TIPS, response.getData().getIsTips());
                        } catch (Exception e) {
                            ExceptionReportUtils.report(e);
                        }
                    }

                    @Override
                    public void onError(RequestException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }

                });
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
                        Log.e("上报推送Token", "成功");
                    }

                    @Override
                    public void onError(RequestException e) {
                        e.printStackTrace();
                        Log.e("上报推送Token", "失败");
                    }

                    @Override
                    public void onComplete() {
                        System.out.println();
                    }

                });
    }


    /**
    * @Desc TODO(修改游戏在线状态)
    * @author 彭石林
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
