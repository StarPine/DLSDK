package com.dl.playfun.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.util.Log;

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
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.blankj.utilcode.util.ObjectUtils;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;

/**
 * Author: 彭石林
 * Time: 2022/7/21 17:34
 * Description: 谷歌支付工具类 建议在AppLocation中进行初始 跟随 声明周期
 */
public class BillingClientLifecycle implements LifecycleObserver, BillingClientStateListener,PurchasesUpdatedListener {
    private static final String TAG = "BillingClientLifecycle";
    private static volatile BillingClientLifecycle INSTANCE;
    private final Application app;

    private BillingClient billingClient;

    //重新连接间隔时间 10 秒
    private long Connection_retry_time = 10;

    //页面创建第一次连接时间
    private long mqttConnectCreateLastTime = 0L;

    //判断当前谷歌商店服务是否处于连接成功
    private boolean connectionSuccessful = false;

    public boolean isConnectionSuccessful() {
        return connectionSuccessful;
    }

    //付款成功回调
    public SingleLiveEvent<Purchase> PAYMENT_SUCCESS = new SingleLiveEvent<>();
    //付款异常回调
    public SingleLiveEvent<Purchase> PAYMENT_FAIL = new SingleLiveEvent<>();

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
        //Lifecycle声明周期监听 页面创建 初始化建立谷歌连接
        initBillingClient();
    }

    public void initBillingClient() {
        billingClient = BillingClient.newBuilder(app)
                .setListener(this)
                .enablePendingPurchases() // Not used for subscriptions.
                .build();
        //判断是否连接
        if (!billingClient.isReady()) {
            billingClient.startConnection(this);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume(){
        //页面再次可见时再次进入判断。防止弱引用持有回收
        //页面处于可见状态最后依次连接时间
        long mqttConnectResumeLastTime = System.currentTimeMillis() / 1000;
        if(mqttConnectResumeLastTime != 0L && mqttConnectCreateLastTime != 0L){
            //页面可见时间 - 页面创建时间 < 10秒。不在进行连接。避免重复创建
            if(mqttConnectResumeLastTime - mqttConnectCreateLastTime  <= 10){
                return;
            }
        }
        //页面再次可见时再次进入判断。防止弱引用持有回收
        if(billingClient == null){
            initBillingClient();
        }else {
            //判断是否连接
            if(!billingClient.isReady()){
                billingClient.startConnection(this);
            }
        }
    }

    /*******************************谷歌连接回调************************************/
    @SuppressLint("CheckResult")
    @Override
    public void onBillingServiceDisconnected() {
        //断开连接
        connectionSuccessful = false;
        //间隔 N 秒再次发送连接
        Observable.timer(Connection_retry_time, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(s -> {
                    billingClient.startConnection(this);
                });
    }

    @Override
    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
        //连接成功
        if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
            // The BillingClient is ready. You can query purchases here.
            connectionSuccessful = true;
        }
    }

    /**
    * @Desc TODO(Google Play 会调用 onPurchasesUpdated()，以将购买操作的结果传送给实现 PurchasesUpdatedListener 接口的)
    * @author 彭石林
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
                    //确认购买交易，不然三天后会退款给用户 而且此时也没有支付成功
                    if (!purchase.isAcknowledged()) {
                        acknowledgePurchase(purchase);
                    }
                } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                    //需要用户确认 可能是非正常操作处理购买
                    Log.i(TAG, "Purchase pending,need to check");
                }
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            //用户取消
            Log.i(TAG, "Purchase cancel");
            AppContext.instance().logEvent(AppsFlyerEvent.vip_google_start_cancel);
        } else {
            //支付错误
            Log.i(TAG, "Pay result error,code=" + billingResult.getResponseCode() + "\nerrorMsg=" + billingResult.getDebugMessage());
            AppContext.instance().logEvent(AppsFlyerEvent.vip_google_play_result+billingResult.getResponseCode());
        }
    }

    /*******************************外部购买商品掉用************************************/
    /**
    * @Desc TODO(根据类型查询可供购买的商品--这里改成同步返回)
    * @author 彭石林
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
        //定义线程等待。改成同步回调
        final CountDownLatch parserCtl = new CountDownLatch(1);
        billingClient.querySkuDetailsAsync(params, (billingResult, skuDetails) -> {
            //执行api成功
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                skuDetailsList.set(skuDetails);
            }
            parserCtl.countDown();
        });
        parserCtl.await();
        return skuDetailsList.get();
    }
    /**
    * @Desc TODO(查询商品是否存在并开始购买)
    * @author 彭石林
    * @parame [params, activity]
    * @Date 2022/7/21
    */
    public void  querySkuDetailsAsync(SkuDetailsParams.Builder params , Activity activity){
        Log.e(TAG,"查询商品是否存在并开始购买："+params.toString());
        long startTime = System.currentTimeMillis() /1000;
        //查询商品是否存在
        billingClient.querySkuDetailsAsync(params.build(), (billingResult, skuDetailsList) -> {
            long endTime = System.currentTimeMillis() /1000;
            Log.e(TAG,"查询商品时间消耗："+(endTime-startTime));
            Log.e(TAG,"查询商品："+billingResult.getResponseCode()+"===="+ (skuDetailsList != null ? skuDetailsList.size() : 0));
            //执行api成功
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                //成功找到商品
                if (ObjectUtils.isNotEmpty(skuDetailsList)) {
                    for (SkuDetails skuDetails : skuDetailsList) {
                        String sku = skuDetails.getSku();
                        String price = skuDetails.getPrice();
                        Log.i(TAG, "Sku=" + sku + ",price=" + price);
                        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(skuDetails)
                                .build();
                        int responseCode = billingClient.launchBillingFlow(activity, flowParams).getResponseCode();
                        if (responseCode == BillingClient.BillingResponseCode.OK) {
                            Log.i(TAG, "成功啟動google支付");
                        } else {
                            Log.i(TAG, "LaunchBillingFlow Fail,code=" + responseCode);
                        }
                    }
                }
            } else {
                PAYMENT_FAIL.postValue(null);
                Log.i(TAG, "Get SkuDetails Failed,Msg=" + billingResult.getDebugMessage());
            }
        });
    }

    //确认订单
    private void acknowledgePurchase(final Purchase purchase) {
        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = billingResult -> {
            //确认购买成功
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.i(TAG, "Acknowledge purchase success");
                PAYMENT_SUCCESS.postValue(purchase);
            } else {
                //确认购买失败 原因多种：掉线、超时、无网络、用户主动关闭支付处理窗体
                PAYMENT_FAIL.postValue(purchase);
            }
        };
        billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
    }
}
