package com.dl.playfun.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
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
import com.dl.playfun.manager.LocaleManager;
import com.dl.playfun.tim.TUIUtils;
import com.dl.playfun.ui.base.MySupportActivity;
import com.dl.playfun.ui.main.MainFragment;
import com.dl.playfun.ui.mine.profile.PerfectProfileFragment;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.utils.AutoSizeUtils;
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
import me.yokeyword.fragmentation.ISupportFragment;
import me.yokeyword.fragmentation.anim.DefaultHorizontalAnimator;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

/**
 * @author wulei
 */
public class MainContainerActivity extends MySupportActivity {
    // ????????????????????????????????????
    private static final long WAIT_TIME = 2000L;
    /**
     * ??????????????????
     */
    public BillingClient billingClient;
    public boolean billingConnection = false;
    private long TOUCH_TIME = 0;
    private MVDialog userDisableDialog;
    private MVDialog loginExpiredDialog;

    private Handler mHandler = new Handler();


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.setLocal(newBase));
    }

    /**
     * ????????????Manifest.xml??????????????????????????????????????????????????????????????????????????????

     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if(newConfig!=null){
            LocaleManager.setLocal(this);
        }
        super.onConfigurationChanged(newConfig);
            LocaleManager.setLocal(this);
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        LocaleManager.setLocal(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AutoSizeUtils.applyAdapt(this.getResources());
        setContentView(R.layout.activity_main_container);
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
                        //???????????? ?????????????????????
                        if(findFragment(PerfectProfileFragment.class)!=null){
                            loadRootFragment(R.id.fl_container, findFragment(PerfectProfileFragment.class));
                        }else{
                            loadRootFragment(R.id.fl_container, new PerfectProfileFragment());
                        }
                    });
        } else {
            //???????????? ?????????????????????
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
                    //????????????
                    // Log.i(TAG, "Purchase cancel");
                } else {
                    //????????????
                    //Log.i(TAG, "Pay result error,code=" + billingResult.getResponseCode() + "\nerrorMsg=" + billingResult.getDebugMessage());
                }
            }
        }).enablePendingPurchases().build();
        //??????google?????????
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull @NotNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    billingConnection = true;
                    //?????????????????????????????????????????????????????????????????????????????????????????????????????????
                    queryAndConsumePurchase();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                //???????????? startConnection() ????????????????????????// Google Play ????????????????????????
                billingConnection = false;
            }
        });
        mHandler.postDelayed(heartbeatRunnable, 7000);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {  //????????????????????????????????????
                if (KeyboardUtils.isSoftInputVisible(this)) {
                    View v = getCurrentFocus();      //???????????????????????????,ps:??????????????????????????????????????????????????????
                    if (isShouldHideKeyboard(v, ev)) { //??????????????????????????????????????????????????????
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
     * ??????EditText???????????????????????????????????????????????????????????????????????????????????????????????????EditText??????????????????
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {  //???????????????????????????????????????EditText
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],    //????????????????????????????????????????????????
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            // ?????????????????????EditText??????????????????????????????????????????
            return !(event.getX() > left) || !(event.getX() < right)
                    || !(event.getY() > top) || !(event.getY() < bottom);
        }
        // ??????????????????EditText?????????
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
                                    //?????????????????????
                                    //startWithPopTo(new LoginFragment(), MainContainerActivity.class, true);
                                })
                                .chooseType(MVDialog.TypeEnum.CENTERWARNED);
                    }
                    if (!userDisableDialog.isShowing()) {
                        userDisableDialog.show();
                    }
                });

        //????????????
        Disposable loginExpiredRe = RxBus.getDefault().toObservable(LoginExpiredEvent.class)
                .compose(RxUtils.schedulersTransformer())
                .subscribe(event -> {

                    if (AppConfig.userClickOut) {
                        return;
                    }
                    if(this.isFinishing()){
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
                                            //????????????
                                            toPlayGameView();
                                        }

                                        @Override
                                        public void onError(int i, String s) {
                                            //????????????
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
        // ????????????(?????????4.x????????????)
        return new DefaultHorizontalAnimator();
        // ???????????????
        // return new DefaultNoAnimator();
        // ?????????????????????
        // return new FragmentAnimator(enter,exit,popEnter,popExit);

        // ????????????(?????????5.0?????????????????????)
//        return super.onCreateFragmentAnimator();
    }

    @Override
    public void onBackPressedSupport() {
        ISupportFragment topFragment = getTopFragment();

        // ?????????Fragment
        if (topFragment instanceof MainFragment) {
        }

        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            super.onBackPressedSupport();
        } else {
            if (System.currentTimeMillis() - TOUCH_TIME < WAIT_TIME) {
                //????????????
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

    //?????????????????????????????????????????????
    public void queryAndConsumePurchase() {
        //queryPurchases() ??????????????? Google Play ???????????????????????????????????????????????????
        //  Purchase.PurchasesResult mResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS);
        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS,
                new PurchaseHistoryResponseListener() {
                    @Override
                    public void onPurchaseHistoryResponse(BillingResult billingResult,
                                                          List<PurchaseHistoryRecord> purchaseHistoryRecordList) {
                        //???????????????????????????
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
        //??????????????????????????????
        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP,
                new PurchaseHistoryResponseListener() {
                    @Override
                    public void onPurchaseHistoryResponse(BillingResult billingResult,
                                                          List<PurchaseHistoryRecord> purchaseHistoryRecordList) {
                        //???????????????????????????
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
    * @Desc TODO(????????????)
    * @author ?????????
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
            //7???????????????
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
