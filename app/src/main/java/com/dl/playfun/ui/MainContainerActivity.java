package com.dl.playfun.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.svideo.crop.bean.AlivcCropOutputParam;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.R;
import com.dl.playfun.api.PlayFunUserApiUtil;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.event.GameHeartBeatEvent;
import com.dl.playfun.event.GameLoginExpiredEvent;
import com.dl.playfun.event.LoginExpiredEvent;
import com.dl.playfun.event.UserDisableEvent;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.tim.TUIUtils;
import com.dl.playfun.ui.base.MySupportActivity;
import com.dl.playfun.ui.main.MainFragment;
import com.dl.playfun.ui.mine.profile.PerfectProfileFragment;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.utils.TimeUtils;
import com.dl.playfun.widget.dialog.MVDialog;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.qcloud.tuicore.util.ConfigManagerUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.RxSubscriptions;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.jessyan.autosize.AutoSizeConfig;
import me.yokeyword.fragmentation.ISupportFragment;
import me.yokeyword.fragmentation.anim.DefaultHorizontalAnimator;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

/**
 * @author wulei
 */
public class MainContainerActivity extends MySupportActivity {
    // 再点一次退出程序时间设置
    private static final long WAIT_TIME = 2000L;
    /**
     * 谷歌支付连接
     */
    public BillingClient billingClient;
    public boolean billingConnection = false;
    private long TOUCH_TIME = 0;
    private MVDialog userDisableDialog;
    private MVDialog loginExpiredDialog;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_container);
        AutoSizeConfig.getInstance().setCustomFragment(true);
        ImmersionBarUtils.setupStatusBar(this, true, false);

        UserDataEntity userDataEntity = ConfigManager.getInstance().getAppRepository().readUserData();
        if (userDataEntity != null && userDataEntity.getSex()!=null && userDataEntity.getSex().intValue()<0) {
            Observable.just("")
                    .delay(1500, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    // Be notified on the main thread
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> {
                        //startWithPop(ChooseSexFragment.class.getCanonicalName());
                        //栈内查找 如果存在及复用
                        if(findFragment(PerfectProfileFragment.class)!=null){
                            loadRootFragment(R.id.fl_container, findFragment(PerfectProfileFragment.class));
                        }else{
                            loadRootFragment(R.id.fl_container, new PerfectProfileFragment());
                        }
                    });
        } else {
            //栈内查找 如果存在及复用
            if(findFragment(MainFragment.class)!=null){
                loadRootFragment(R.id.fl_container, findFragment(MainFragment.class));
            }else{
                loadRootFragment(R.id.fl_container, new MainFragment());
            }
        }
            //loadRootFragment(R.id.fl_container, new SplashFragment());

        registerRxBus();
        //getAndroiodScreenProperty();
        billingClient = BillingClient.newBuilder(this.getBaseContext()).setListener(new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(@NonNull @NotNull BillingResult billingResult, @Nullable @org.jetbrains.annotations.Nullable List<Purchase> purchases) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    //用户取消
                    // Log.i(TAG, "Purchase cancel");
                } else {
                    //支付错误
                    //Log.i(TAG, "Pay result error,code=" + billingResult.getResponseCode() + "\nerrorMsg=" + billingResult.getDebugMessage());
                }
            }
        }).enablePendingPurchases().build();
        //连接google服务器
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull @NotNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    billingConnection = true;
                    //每次进行重连的时候都应该消耗之前缓存的商品，不然可能会导致用户支付不了
                    queryAndConsumePurchase();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                //通过调用 startConnection() 方法在下一个请求// Google Play 时重新启动连接。
                billingConnection = false;
            }
        });
        mHandler.postDelayed(heartbeatRunnable, 7000);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {  //把操作放在用户点击的时候
                if (KeyboardUtils.isSoftInputVisible(this)) {
                    View v = getCurrentFocus();      //得到当前页面的焦点,ps:有输入框的页面焦点一般会被输入框占据
                    if (isShouldHideKeyboard(v, ev)) { //判断用户点击的是否是输入框以外的区域
                        KeyboardUtils.hideSoftInput(this);
                    }
                }
            }
            if (ev != null) {
                return super.dispatchTouchEvent(ev);
            }
        } catch (Exception e) {

        }

        return false;
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {  //判断得到的焦点控件是否包含EditText
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],    //得到输入框在屏幕中上下左右的位置
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            // 点击位置如果是EditText的区域，忽略它，不收起键盘。
            return !(event.getX() > left) || !(event.getX() < right)
                    || !(event.getY() > top) || !(event.getY() < bottom);
        }
        // 如果焦点不是EditText则忽略
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==-1){
            if(AppConfig.isCorpAliyun){
                AlivcCropOutputParam alivcCropOutputParam = (AlivcCropOutputParam) data.getSerializableExtra(AlivcCropOutputParam.RESULT_KEY_OUTPUT_PARAM);
                if(alivcCropOutputParam!=null){
                    RxBus.getDefault().post(alivcCropOutputParam);
                }

            }
            AppConfig.isCorpAliyun = false;
        }
    }

    private void registerRxBus() {
        RxBus.getDefault().toObservable(UserDisableEvent.class)
                .subscribe(event -> {
                    if (userDisableDialog == null) {
                        userDisableDialog = MVDialog.getInstance(MainContainerActivity.this)
                                .setTitele(getString(R.string.playfun_dialog_user_disable_title))
                                .setContent(getString(R.string.playfun_dialog_user_disable_content))
                                .setConfirmText(getString(R.string.playfun_dialog_user_disable_btn_text))
                                .setCancelable(true)
                                .setConfirmOnlick(dialog -> {
                                    //跳转到登录界面
                                    //startWithPopTo(new LoginFragment(), MainContainerActivity.class, true);
                                })
                                .chooseType(MVDialog.TypeEnum.CENTERWARNED);
                    }
                    if (!userDisableDialog.isShowing()) {
                        userDisableDialog.show();
                    }
                });

        //登錄過期
        Disposable loginExpiredRe = RxBus.getDefault().toObservable(LoginExpiredEvent.class)
                .subscribe(event -> {

                    if (AppConfig.userClickOut) {
                        return;
                    }
                    ConfigManager.getInstance().getAppRepository().logout();
                    if (loginExpiredDialog == null) {
                        loginExpiredDialog = MVDialog.getInstance(this)
                                .setContent(getString(R.string.playfun_again_login))
                                .setConfirmText(getString(R.string.playfun_confirm))
                                .setCancelable(true)
                                .setNotClose(true)
                                .setConfirmOnlick(dialog -> {
                                    dialog.dismiss();
                                    TUIUtils.logout(new V2TIMCallback() {
                                        @Override
                                        public void onSuccess() {
                                            RxBus.getDefault().post(new GameLoginExpiredEvent());
                                            //返回游戏
                                            toPlayGameView();
                                        }

                                        @Override
                                        public void onError(int i, String s) {
                                            //返回游戏
                                            RxBus.getDefault().post(new GameLoginExpiredEvent());
                                            toPlayGameView();
                                        }
                                    });
                                })
                                .chooseType(MVDialog.TypeEnum.CENTERWARNED);
                    }
                    if (!loginExpiredDialog.isShowing()) {
                        loginExpiredDialog.show();
                    }
                });
        RxSubscriptions.add(loginExpiredRe);

//        RxBus.getDefault().toObservable(UMengCustomEvent.class)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(event -> {
//                    MobclickAgent.onEvent(MainContainerActivity.this, event.getEventId());
//                });
    }

    @Override
    public FragmentAnimator onCreateFragmentAnimator() {
        // 设置横向(和安卓4.x动画相同)
        return new DefaultHorizontalAnimator();
        // 设置无动画
        // return new DefaultNoAnimator();
        // 设置自定义动画
        // return new FragmentAnimator(enter,exit,popEnter,popExit);

        // 默认竖向(和安卓5.0以上的动画相同)
//        return super.onCreateFragmentAnimator();
    }

    @Override
    public void onBackPressedSupport() {
        ISupportFragment topFragment = getTopFragment();

        // 主页的Fragment
        if (topFragment instanceof MainFragment) {
        }

        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            super.onBackPressedSupport();
        } else {
            if (System.currentTimeMillis() - TOUCH_TIME < WAIT_TIME) {
                //返回游戏
                toPlayGameView();
            } else {
                TOUCH_TIME = System.currentTimeMillis();
                ToastUtils.showShort(R.string.playfun_exit_app);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try{
            AppContext.instance().mFirebaseAnalytics.setCurrentScreen(this, "Screen Name", this.getClass().getSimpleName());
        }catch(Exception e){

        }
        //MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //MobclickAgent.onPause(this);
    }

    //查询最近的购买交易，并消耗商品
    public void queryAndConsumePurchase() {
        //queryPurchases() 方法会使用 Google Play 商店应用的缓存，而不会发起网络请求
        //  Purchase.PurchasesResult mResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS);
        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS,
                new PurchaseHistoryResponseListener() {
                    @Override
                    public void onPurchaseHistoryResponse(BillingResult billingResult,
                                                          List<PurchaseHistoryRecord> purchaseHistoryRecordList) {
                        //开始连接进入以下：
                        if (purchaseHistoryRecordList != null) {
                            List<Map> purchaseList = new ArrayList<>();
                            Date endTime = new Date();
                            Date beginTime = ApiUitl.toDayMinTwo(endTime);
                            for (PurchaseHistoryRecord purchaseHistoryRecord : purchaseHistoryRecordList) {
                                try {
                                    Purchase purchase = new Purchase(purchaseHistoryRecord.getOriginalJson(), purchaseHistoryRecord.getSignature());
                                    Date date = new Date();
                                    date.setTime(purchase.getPurchaseTime());
                                    if (purchase.isAcknowledged()) {
                                        if (ApiUitl.belongCalendar(date, beginTime, endTime)) {
                                            String pack = purchase.getPackageName();
                                            if (StringUtil.isEmpty(pack)) {
                                                //pack = BuildConfig.APPLICATION_ID;
                                            }
                                            Map<String, Object> maps = new HashMap<>();
                                            maps.put("orderId", purchase.getOrderId());
                                            maps.put("token", purchase.getPurchaseToken());
                                            maps.put("sku", purchase.getSkus().toString());
                                            maps.put("package", pack);
                                            purchaseList.add(maps);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            UserDataEntity userDataEntity = ConfigManager.getInstance().getAppRepository().readUserData();
                            if (userDataEntity == null || userDataEntity.getId() == null) {
                                return;
                            }
                            if (!ObjectUtils.isEmpty(purchaseList) && purchaseList.size() > 0) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("data", purchaseList);
                                ConfigManager.getInstance().getAppRepository().repoetLocalGoogleOrder(map)
                                        .compose(RxUtils.schedulersTransformer())
                                        .compose(RxUtils.exceptionTransformer())
                                        .subscribe(new BaseObserver<BaseResponse>() {
                                            @Override
                                            public void onSuccess(BaseResponse baseResponse) {
                                            }

                                            @Override
                                            public void onComplete() {
                                                billingClient.endConnection();
                                            }
                                        });
                            }
                        }
                    }

                });
        //购买商品上报补偿机制
        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP,
                new PurchaseHistoryResponseListener() {
                    @Override
                    public void onPurchaseHistoryResponse(BillingResult billingResult,
                                                          List<PurchaseHistoryRecord> purchaseHistoryRecordList) {
                        //开始连接进入以下：
                        if (purchaseHistoryRecordList != null) {
                            List<Map> purchaseList = new ArrayList<>();
                            Date endTime = new Date();
                            Date beginTime = ApiUitl.toDayMinTwo(endTime);
                            for (PurchaseHistoryRecord purchaseHistoryRecord : purchaseHistoryRecordList) {
                                try {
                                    Purchase purchase = new Purchase(purchaseHistoryRecord.getOriginalJson(), purchaseHistoryRecord.getSignature());
                                    Date date = new Date();
                                    date.setTime(purchase.getPurchaseTime());
                                    if (purchase.isAcknowledged()) {
                                        if (ApiUitl.belongCalendar(date, beginTime, endTime)) {
                                            String pack = purchase.getPackageName();
                                            if (StringUtil.isEmpty(pack)) {
                                                //pack = BuildConfig.APPLICATION_ID;
                                            }
                                            Map<String, Object> maps = new HashMap<>();
                                            maps.put("orderId", purchase.getOrderId());
                                            maps.put("token", purchase.getPurchaseToken());
                                            maps.put("sku", purchase.getSkus().toString());
                                            maps.put("package", pack);
                                            purchaseList.add(maps);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            UserDataEntity userDataEntity = ConfigManager.getInstance().getAppRepository().readUserData();
                            if (userDataEntity == null || userDataEntity.getId() == null) {
                                return;
                            }
                            if (!ObjectUtils.isEmpty(purchaseList) && purchaseList.size() > 0) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("data", purchaseList);
                                ConfigManager.getInstance().getAppRepository().repoetLocalGoogleOrder(map)
                                        .compose(RxUtils.schedulersTransformer())
                                        .compose(RxUtils.exceptionTransformer())
                                        .subscribe(new BaseObserver<BaseResponse>() {
                                            @Override
                                            public void onSuccess(BaseResponse baseResponse) {
                                            }

                                            @Override
                                            public void onComplete() {
                                                billingClient.endConnection();
                                            }
                                        });
                            }
                        }
                    }

                });
    }

    /**
    * @Desc TODO(返回游戏)
    * @author 彭石林
    * @parame []
    * @return void
    * @Date 2022/1/11
    */
    public void toPlayGameView() {
        PlayFunUserApiUtil.getInstance().toPlayGameView(this);
    }

    private Runnable heartbeatRunnable = new Runnable() {
        @Override
        public void run() {
            RxBus.getDefault().post(new GameHeartBeatEvent(TimeUtils.getCurrentTime()));
            //7秒发送一次
            mHandler.postDelayed(heartbeatRunnable, 7000);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        ConfigManagerUtil.getInstance().putPlayGameFlag(true);
        if (mHandler != null) {
            mHandler.removeCallbacks(heartbeatRunnable);
            mHandler = null;
        }
    }
}
