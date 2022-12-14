package com.dl.playfun.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.BuildConfig;
import com.dl.playfun.R;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.event.ToastUIEvent;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.utils.StringUtil;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * Author: ?????????
 * Time: 2022/7/21 17:34
 * Description: ????????????????????? ?????????AppLocation??????????????? ?????? ????????????
 */
public class BillingClientLifecycle implements LifecycleObserver, BillingClientStateListener,PurchasesUpdatedListener {
    private static final String TAG = "BillingClientLifecycle";
    private static volatile BillingClientLifecycle INSTANCE;
    private final Application app;

    private BillingClient billingClient;

    //???????????????????????? 10 ???
    private long Connection_retry_time = 10;

    //?????????????????????????????????
    private long mqttConnectCreateLastTime = 0L;

    //??????????????????????????????????????????????????????
    private boolean connectionSuccessful = false;

    //????????????-??????inapp
    private boolean queryPurchasesAsyncToastInApp = false;
    private boolean queryPurchasesAsyncToastSUBS = false;
    private long queryPurchasesAsyncToastINAppTime = 0L;
    private long queryPurchasesAsyncToastSUBSTime = 0L;

    public boolean isConnectionSuccessful() {
        return connectionSuccessful;
    }
    //??????????????????
    public SingleLiveEvent<Boolean> CONNECTION_SUCCESS = new SingleLiveEvent<>();
    //??????????????????????????????
    public SingleLiveEvent<BillingPurchasesState> PAYMENT_SUCCESS = new SingleLiveEvent<>();
    //??????????????????????????????
    public SingleLiveEvent<BillingPurchasesState> PAYMENT_FAIL = new SingleLiveEvent<>();
    //????????????????????????
    public SingleLiveEvent<BillingPurchasesState> PurchaseHistory = new SingleLiveEvent<>();

    private BillingClientLifecycle(Application app) {
        this.app = app;
    }

    public static BillingClientLifecycle getInstance(Application app) {
        if (INSTANCE == null) {
            synchronized (BillingClientLifecycle.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BillingClientLifecycle(app);
                }
            }
        }
        return INSTANCE;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {
        Log.d(TAG, "ON_CREATE");
        mqttConnectCreateLastTime = System.currentTimeMillis() / 1000;
        //Lifecycle?????????????????? ???????????? ???????????????????????????
        initBillingClient();
    }

    public void initBillingClient() {
        billingClient = BillingClient.newBuilder(app)
                .setListener(this)
                .enablePendingPurchases() // Not used for subscriptions.
                .build();
        //??????????????????
        if (!billingClient.isReady()) {
            billingClient.startConnection(this);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume(){
        //?????????????????????????????????????????????????????????????????????
        //????????????????????????????????????????????????
        long mqttConnectResumeLastTime = System.currentTimeMillis() / 1000;
        if(mqttConnectResumeLastTime != 0L && mqttConnectCreateLastTime != 0L){
            //?????????????????? - ?????????????????? < 10?????????????????????????????????????????????
            if(mqttConnectResumeLastTime - mqttConnectCreateLastTime  <= 10){
                return;
            }
        }
        //?????????????????????????????????????????????????????????????????????
        if(billingClient == null){
            initBillingClient();
        }else {
            //??????????????????
            if(!billingClient.isReady()){
                billingClient.startConnection(this);
            }
        }
    }

    /*******************************??????????????????************************************/
    @SuppressLint("CheckResult")
    @Override
    public void onBillingServiceDisconnected() {
        //????????????
        connectionSuccessful = false;
        //?????? N ?????????????????????
        Observable.timer(Connection_retry_time, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(s -> {
                    billingClient.startConnection(this);
                });
    }

    @Override
    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
        //????????????
        if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
            // The BillingClient is ready. You can query purchases here.
            connectionSuccessful = true;
            CONNECTION_SUCCESS.postValue(true);
        }else{
            CONNECTION_SUCCESS.postValue(false);
        }
    }
    /**
    * @Desc TODO(??????????????????????????????????????????????????????)
    * @author ?????????
    * @parame [SkuType]
    * @return void
    * @Date 2022/7/29
    */
    public void queryPurchaseHistoryAsync(String SkuType){
        billingClient.queryPurchaseHistoryAsync(SkuType, (billingResult, purchaseHistoryRecordList) -> {
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
                                    pack = BuildConfig.APPLICATION_ID;
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
                            .compose(RxUtils.exceptionTransformer())
                            .compose(RxUtils.schedulersTransformer())
                            .subscribe(new BaseObserver<BaseResponse>() {
                                @Override
                                public void onSuccess(BaseResponse baseResponse) {
                                }
                                @Override
                                public void onComplete() {
                                }
                            });
                }
            }
        });
    }

    /**
    * @Desc TODO(Google Play ????????? onPurchasesUpdated()????????????????????????????????????????????? PurchasesUpdatedListener ?????????)
    * @author ?????????
    * @parame [billingResult, list]
    * @return void
    * @Date 2022/7/21
    */
    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> listPurchase) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && ObjectUtils.isNotEmpty(listPurchase) ) {
            for (final Purchase purchase : listPurchase) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    // Acknowledge purchase and grant the item to the user
                    Log.i(TAG, "Purchase success");
                    //?????????????????????????????????????????????????????? ?????????????????????????????????
                    if (!purchase.isAcknowledged()) {
                        try {
                            AppContext.instance().logEvent(AppsFlyerEvent.pay_success);
                        }catch (Exception ignored) {

                        }
                        PAYMENT_SUCCESS.postValue(getBillingPurchasesState(0,BillingPurchasesState.BillingFlowNode.purchasesUpdated,purchase));
                        ElkLogEventReport.reportBillingClientModule.reportBillingClientPayment(String.valueOf(BillingPurchasesState.BillingFlowNode.purchasesUpdated),0);
                        acknowledgePurchase(purchase);
                    }
                } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                    //?????????????????? ????????????????????????????????????
                    Log.i(TAG, "Purchase pending,need to check");
                }
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            //????????????
            Log.i(TAG, "Purchase cancel");
            PAYMENT_FAIL.postValue(getBillingPurchasesState(billingResult.getResponseCode(),BillingPurchasesState.BillingFlowNode.purchasesUpdated,null));
            ElkLogEventReport.reportBillingClientModule.reportBillingClientPayment(String.valueOf(BillingPurchasesState.BillingFlowNode.purchasesUpdated),billingResult.getResponseCode());
        } else {
            //????????????
            Log.i(TAG, "Pay result error,code=" + billingResult.getResponseCode() + "\nerrorMsg=" + billingResult.getDebugMessage());
            PAYMENT_FAIL.postValue(getBillingPurchasesState(billingResult.getResponseCode(),BillingPurchasesState.BillingFlowNode.purchasesUpdated,null));
            ElkLogEventReport.reportBillingClientModule.reportBillingClientPayment(String.valueOf(BillingPurchasesState.BillingFlowNode.purchasesUpdated),billingResult.getResponseCode());
        }
    }

    /*******************************????????????????????????************************************/
    /**
    * @Desc TODO(???????????????????????????????????????--????????????????????????)
    * @author ?????????
    * @parame []
    * @return void
    * @Date 2022/7/21
    */
    public List<SkuDetails> querySkuDetailsAsync(String type,List<String> SkuIdList) throws InterruptedException {
        AtomicReference<List<SkuDetails>> skuDetailsList = new AtomicReference<>();
        SkuDetailsParams params = SkuDetailsParams.newBuilder()
                .setType(type)
                .setSkusList(SkuIdList)
                .build();
        //???????????????????????????????????????
        final CountDownLatch parserCtl = new CountDownLatch(1);
        billingClient.querySkuDetailsAsync(params, (billingResult, skuDetails) -> {
            //??????api??????
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                skuDetailsList.set(skuDetails);
            }
            parserCtl.countDown();
        });
        parserCtl.await();
        return skuDetailsList.get();
    }
    /**
     * @Desc TODO(???????????????????????????????????????--????????????????????????)
     * @author ?????????
     * @parame []
     * @return void
     * @Date 2022/7/21
     */
    public void querySkuDetailsAsync(SkuDetailsParams.Builder params, SkuDetailsResponseListener skuDetailsResponseListener) {
        billingClient.querySkuDetailsAsync(params.build(), skuDetailsResponseListener);
    }
    /**
    * @Desc TODO(???????????????????????????????????????)
    * @author ?????????
    * @parame [params, activity]
    * @Date 2022/7/21
    */
    public void  querySkuDetailsLaunchBillingFlow(SkuDetailsParams.Builder params , Activity activity,String orderNumber){
        Log.e(TAG,"??????????????????????????????????????????"+params.toString());
        long startTime = System.currentTimeMillis() /1000;
        //????????????????????????
        billingClient.querySkuDetailsAsync(params.build(), (billingResult, skuDetailsList) -> {
            long endTime = System.currentTimeMillis() /1000;
            Log.e(TAG,"???????????????????????????"+(endTime-startTime));
            Log.e(TAG,"???????????????"+billingResult.getResponseCode()+"===="+ (skuDetailsList != null ? skuDetailsList.size() : 0));
            //??????api??????
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                //??????????????????
                if (ObjectUtils.isNotEmpty(skuDetailsList)) {
                    ElkLogEventReport.reportBillingClientModule.reportBillingClientPayment(String.valueOf(BillingPurchasesState.BillingFlowNode.querySkuDetails),0);
                    PAYMENT_SUCCESS.postValue(getBillingPurchasesState(0,BillingPurchasesState.BillingFlowNode.querySkuDetails,null));
                    for (SkuDetails skuDetails : skuDetailsList) {
                        String sku = skuDetails.getSku();
                        String price = skuDetails.getPrice();
                        Log.i(TAG, "Sku=" + sku + ",price=" + price);
                        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(skuDetails)
                                .setObfuscatedAccountId(ConfigManager.getInstance().getUserId())
                                .setObfuscatedProfileId(orderNumber)
                                .build();
                        int responseCode = billingClient.launchBillingFlow(activity, flowParams).getResponseCode();
                        if (responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.i(TAG, "????????????google??????");
                            PAYMENT_SUCCESS.postValue(getBillingPurchasesState(responseCode,BillingPurchasesState.BillingFlowNode.launchBilling,null));

                        } else {
                            PAYMENT_FAIL.postValue(getBillingPurchasesState(responseCode,BillingPurchasesState.BillingFlowNode.launchBilling,null));
                            Log.i(TAG, "LaunchBillingFlow Fail,code=" + responseCode);
                        }
                        ElkLogEventReport.reportBillingClientModule.reportBillingClientPayment(String.valueOf(BillingPurchasesState.BillingFlowNode.launchBilling),responseCode);
                    }
                }else {
                    PAYMENT_FAIL.postValue(getBillingPurchasesState(billingResult.getResponseCode(),BillingPurchasesState.BillingFlowNode.querySkuDetails,null));
                    ElkLogEventReport.reportBillingClientModule.reportBillingClientPayment(String.valueOf(BillingPurchasesState.BillingFlowNode.querySkuDetails),billingResult.getResponseCode());
                }
            } else {
                ElkLogEventReport.reportBillingClientModule.reportBillingClientPayment(String.valueOf(BillingPurchasesState.BillingFlowNode.querySkuDetails),billingResult.getResponseCode());
                //??????api??????
                PAYMENT_FAIL.postValue(getBillingPurchasesState(billingResult.getResponseCode(),BillingPurchasesState.BillingFlowNode.querySkuDetails,null));
                Log.i(TAG, "Get SkuDetails Failed,Msg=" + billingResult.getDebugMessage());
            }
        });
    }

    //????????????????????????
    private void acknowledgePurchase(final Purchase purchase) {
        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = billingResult -> {
            //Log.e(TAG,"???????????????????????????"+billingResult.getResponseCode());
            //??????????????????
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.i(TAG, "Acknowledge purchase success");
                PAYMENT_SUCCESS.postValue(getBillingPurchasesState(0,BillingPurchasesState.BillingFlowNode.acknowledgePurchase,purchase));
                ElkLogEventReport.reportBillingClientModule.reportBillingClientPayment(String.valueOf(BillingPurchasesState.BillingFlowNode.acknowledgePurchase),billingResult.getResponseCode());
            } else {
                //?????????????????????????????? ?????????????????????????????????????????????????????????????????????????????????
                PAYMENT_FAIL.postValue(getBillingPurchasesState(billingResult.getResponseCode(),BillingPurchasesState.BillingFlowNode.acknowledgePurchase,purchase));
            }
            ElkLogEventReport.reportBillingClientModule.reportBillingClientPayment(String.valueOf(BillingPurchasesState.BillingFlowNode.acknowledgePurchase),billingResult.getResponseCode());
        };
        billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
    }

    //?????????????????????????????????????????????
    public void queryAndConsumePurchase(String SkuType){
        //queryPurchases() ??????????????? Google Play ???????????????????????????????????????????????????
        billingClient.queryPurchaseHistoryAsync(SkuType,
                (billingResult, purchaseHistoryRecordList) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchaseHistoryRecordList != null) {
                        for (PurchaseHistoryRecord purchaseHistoryRecord : purchaseHistoryRecordList) {
                            // Process the result.
                            //??????????????????????????????????????????????????????
                            try {
                                Purchase purchase = new Purchase(purchaseHistoryRecord.getOriginalJson(), purchaseHistoryRecord.getSignature());
                                Log.e(TAG,SkuType+"===="+purchaseHistoryRecord.getSkus()+"=???????????????????????????"+purchase.toString());
                                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                    //????????? ????????????
                                    consumePurchaseHistory(purchase);
                                    //??????????????????
                                    if (!purchase.isAcknowledged()) {
                                        acknowledgeHistoryPurchase(purchase);
                                    }
                                    //TODO??????????????????????????????????????????????????????????????????????????????App?????????
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }
    //????????????
    private void consumePurchaseHistory(final Purchase purchase) {
        ConsumeParams.Builder consumeParams = ConsumeParams.newBuilder();
        consumeParams.setPurchaseToken(purchase.getPurchaseToken());
        billingClient.consumeAsync(consumeParams.build(), (billingResult, purchaseToken) -> {
            Log.i(TAG, "onConsumeResponse, code=" + billingResult.getResponseCode());
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.i(TAG, "onConsumeResponse,code=BillingResponseCode.OK");
                PurchaseHistory.postValue(getBillingPurchasesState(0,BillingPurchasesState.BillingFlowNode.queryPurchaseHistory,purchase));
                reportOrderPurchase(purchase);
            } else {
                //?????????????????????????????????????????????
                Log.i(TAG, "onConsumeResponse=getDebugMessage==" + billingResult.getDebugMessage());
            }
        });
    }
    //????????????????????????
    private void acknowledgeHistoryPurchase(final Purchase purchase) {
        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = billingResult -> {
            //Log.e(TAG,"???????????????????????????"+billingResult.getResponseCode());
            //??????????????????
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.i(TAG, "Acknowledge purchase success");
                reportOrderPurchase(purchase);
                PurchaseHistory.postValue(getBillingPurchasesState(0,BillingPurchasesState.BillingFlowNode.acknowledgePurchase,purchase));
            } else {
                //?????????????????????????????? ?????????????????????????????????????????????????????????????????????????????????
                PurchaseHistory.postValue(getBillingPurchasesState(billingResult.getResponseCode(),BillingPurchasesState.BillingFlowNode.acknowledgePurchase,purchase));
            }
            ElkLogEventReport.reportBillingClientModule.reportBillingClientHistory(String.valueOf(BillingPurchasesState.BillingFlowNode.acknowledgePurchase),billingResult.getResponseCode());
        };
        billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
    }

    //????????????????????????
    private BillingPurchasesState getBillingPurchasesState(int billingResponseCode, BillingPurchasesState.BillingFlowNode billingFlowNode, Purchase purchase){
        return new BillingPurchasesState(billingResponseCode,billingFlowNode,purchase);
    }

    /**
     * ???????????? ?????????????????????????????????????????????????????????google???????????????????????????????????????????????????????????????
     */
    public void queryPurchasesAsync(String SkuType){
        PurchasesResponseListener mPurchasesResponseListener = (billingResult, purchasesResult) -> {
            if(billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) return;
            if(purchasesResult!=null && !purchasesResult.isEmpty()){
                for (Purchase purchase : purchasesResult) {
                    if(purchase!=null){
                        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                            //????????? ????????????
                            consumePurchaseHistory(purchase);
                            //??????????????????
                            if (!purchase.isAcknowledged()) {
                                acknowledgeHistoryPurchase(purchase);
                            }
                        }
                    }
                }
            }
        };
        billingClient.queryPurchasesAsync(SkuType,mPurchasesResponseListener);
    }

    /**
     * ???????????? ?????????????????????????????????????????????????????????google???????????????????????????????????????????????????????????????
     */
    public void queryPurchasesAsyncToast(String SkuType){
        long currentTimeMillis = System.currentTimeMillis() / 1000;

        if(SkuType.equals(BillingClient.SkuType.INAPP)){
            if(queryPurchasesAsyncToastInApp && currentTimeMillis - queryPurchasesAsyncToastINAppTime<60){
                RxBus.getDefault().post(new ToastUIEvent(R.string.playfun_pay_buy_reports_error_2,Toast.LENGTH_SHORT));
                return;
            }else{
                queryPurchasesAsyncToastInApp = true;
                queryPurchasesAsyncToastINAppTime = System.currentTimeMillis() / 1000;
            }
        }
        if(SkuType.equals(BillingClient.SkuType.SUBS)){
            if(queryPurchasesAsyncToastSUBS && currentTimeMillis - queryPurchasesAsyncToastSUBSTime<60){
                RxBus.getDefault().post(new ToastUIEvent(R.string.playfun_pay_buy_reports_error_2,Toast.LENGTH_SHORT));
                return;
            }else{
                queryPurchasesAsyncToastSUBS = true;
                queryPurchasesAsyncToastSUBSTime = System.currentTimeMillis() / 1000;
            }
        }

        //queryPurchases() ??????????????? Google Play ???????????????????????????????????????????????????
        billingClient.queryPurchaseHistoryAsync(SkuType,
                (billingResult, purchaseHistoryRecordList) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchaseHistoryRecordList != null) {
                        if(purchaseHistoryRecordList.isEmpty()){
                            RxBus.getDefault().post(new ToastUIEvent(R.string.playfun_pay_buy_reports_empty,Toast.LENGTH_SHORT));
                            return;
                        }
                        int count = 0;
                        for (PurchaseHistoryRecord purchaseHistoryRecord : purchaseHistoryRecordList) {
                            // Process the result.
                            //??????????????????????????????????????????????????????
                            try {
                                Purchase purchase = new Purchase(purchaseHistoryRecord.getOriginalJson(), purchaseHistoryRecord.getSignature());
                                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                    //????????? ????????????
                                    consumePurchaseHistory(purchase);
                                    //??????????????????
                                    if (!purchase.isAcknowledged()) {
                                        count ++;
                                        acknowledgeHistoryPurchase(purchase);
                                    }
                                    //TODO??????????????????????????????????????????????????????????????????????????????App?????????
                                }
                            } catch (JSONException e) {
                                Log.e(TAG,"Error message???"+e.getMessage());
                            }
                        }
                        if(count > 0){
                            RxBus.getDefault().post(new ToastUIEvent(R.string.playfun_pay_buy_reports,Toast.LENGTH_SHORT));
                            queryPurchasesAsync(SkuType);
                        }else{
                            RxBus.getDefault().post(new ToastUIEvent(R.string.playfun_pay_buy_reports_empty,Toast.LENGTH_SHORT));
                        }
                    }else{
                        RxBus.getDefault().post(new ToastUIEvent(R.string.playfun_pay_buy_reports_error,Toast.LENGTH_SHORT));
                    }
                });
    }

    /**
    * @Desc TODO(??????-????????????)
    * @author ?????????
    * @parame [purchase]
    * @return void
    * @Date 2022/10/18
    */
    public void reportOrderPurchase(Purchase purchase){
        Date endTime = new Date();
        Date beginTime = ApiUitl.toDayMinTwo(endTime);
        Date date = new Date();
        date.setTime(purchase.getPurchaseTime());
        if (purchase.isAcknowledged()) {
            if (ApiUitl.belongCalendar(date, beginTime, endTime)) {
                String pack = purchase.getPackageName();
                if (StringUtil.isEmpty(pack)) {
                    pack = BuildConfig.APPLICATION_ID;
                }
                Map<String, Object> mapData = new HashMap<>();
                mapData.put("token", purchase.getPurchaseToken());
                mapData.put("sku", purchase.getSkus().toString());
                mapData.put("package", pack);
                ConfigManager.getInstance().localeOrderReport(mapData);
            }
        }
    }

}
