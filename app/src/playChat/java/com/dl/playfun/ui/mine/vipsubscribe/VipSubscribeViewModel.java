package com.dl.playfun.ui.mine.vipsubscribe;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseEmptyObserver;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseListDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.CreateOrderEntity;
import com.dl.playfun.entity.LocalGooglePayCache;
import com.dl.playfun.entity.SystemConfigEntity;
import com.dl.playfun.entity.SystemRoleMoneyConfigEntity;
import com.dl.playfun.entity.TaskAdEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.entity.VipPackageItemEntity;
import com.dl.playfun.event.UserUpdateVipEvent;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.utils.Utils;
import com.dl.playfun.viewmodel.BaseViewModel;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * @author wulei
 */
public class VipSubscribeViewModel extends BaseViewModel<AppRepository> {
    public static final String TAG = "VipSubscribeViewModel";

    //福袋
    public ObservableField<List<TaskAdEntity>> adItemVipEntityList = new ObservableField<>(new ArrayList<>());

    private final int consumeImmediately = 0;
    private final int consumeDelay = 1;

    public List<VipPackageItemEntity> vipPackageItemEntityList = new ArrayList<>();


    public BindingRecyclerViewAdapter<VipSubscribeItemViewModel> adapter = new BindingRecyclerViewAdapter<>();

    public ObservableList<VipSubscribeItemViewModel> observableList = new ObservableArrayList<>();

    public ItemBinding<VipSubscribeItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_vip_subscribe);

    public ObservableField<Boolean> dialogShow = new ObservableField<>(false);


    public ObservableField<String> memberEndTime = new ObservableField<>();

    public ObservableField<Integer> selectedPosition = new ObservableField<>(-1);

    public ObservableField<VipPackageItemEntity> checkedVipPackageItemEntity = new ObservableField<>();

    public BillingClient billingClient;
    public String orderNumber;
    public Integer pay_good_day = 0;
    public BindingCommand adOnClickCommand = new BindingCommand(() -> dialogShow.set(true));
    //确定
    public BindingCommand buyOnClickCommand = new BindingCommand(() -> dialogShow.set(true));
    public BindingCommand dialogBgOnClickCommand = new BindingCommand(() -> dialogShow.set(false));
    UIChangeObservable uc = new UIChangeObservable();
    private Integer ActualValue;
    public BindingCommand confirmOnClickCommand = new BindingCommand(() -> rechargeCreateOrder());

    public VipPackageItemEntity $vipPackageItemEntity;

    public VipSubscribeViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
        UserDataEntity userDataEntity = model.readUserData();
        if (userDataEntity.getIsVip() == 1 && userDataEntity != null && userDataEntity.getEndTime() != null) {
            memberEndTime.set(StringUtils.getString(R.string.valid_time) + userDataEntity.getEndTime());
        }
    }

    public TaskAdEntity $taskEntity1;

    public void itemClick(int position,VipPackageItemEntity vipPackageItemEntity) {
        for (VipSubscribeItemViewModel vipSubscribeItemViewModel : observableList) {
            vipSubscribeItemViewModel.itemEntity.get().setSelected(false);
        }
        $vipPackageItemEntity = vipPackageItemEntity;
        checkedVipPackageItemEntity.set(vipPackageItemEntity);
        AppContext.instance().logEvent("VIP_" + (position + 1));
        pay_good_day = observableList.get(position).itemEntity.get().getActualValue();
        observableList.get(position).itemEntity.get().setSelected(true);
        selectedPosition.set(position);
    }

    //获取个人资料
    private void loadProfile() {
        //RaJava模拟登录
        model.getUserData()
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .subscribe(new BaseObserver<BaseDataResponse<UserDataEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<UserDataEntity> response) {
                        UserDataEntity userDataEntity = response.getData();
                        model.saveUserData(userDataEntity);
                        if (userDataEntity.getIsVip() == 1 && userDataEntity != null && userDataEntity.getEndTime() != null) {
                            memberEndTime.set(StringUtils.getString(R.string.valid_time) + userDataEntity.getEndTime());
                        }
                    }
                });
    }

    public void loadPackage() {
        model.vipPackages()
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .subscribe(new BaseEmptyObserver<BaseDataResponse<List<VipPackageItemEntity>>>(this) {

                    @Override
                    public void onSuccess(BaseDataResponse<List<VipPackageItemEntity>> response) {
                        super.onSuccess(response);
                        observableList.clear();
                        vipPackageItemEntityList = response.getData();
                        //querySubs(response.getData());
                        if (observableList.size() == 0) {
                            for (int i = 0; i < vipPackageItemEntityList.size(); i++) {
                                VipPackageItemEntity vipPackage = vipPackageItemEntityList.get(i);
                                VipSubscribeItemViewModel item = new VipSubscribeItemViewModel(VipSubscribeViewModel.this, vipPackage, null);
                                observableList.add(item);
                                if (vipPackage.getIsRecommend() == 1) {
                                    vipPackage.setSelected(true);
                                    pay_good_day = vipPackage.getActualValue();
                                    checkedVipPackageItemEntity.set(vipPackage);
                                    selectedPosition.set(i);
                                }
                            }
                        }
                    }
                });
    }

    private void createOrder(int goodsId, String payCode) {
        model.createOrder(goodsId, 2, 2, null)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> {
                    showHUD();
                })
                .subscribe(new BaseObserver<BaseDataResponse<CreateOrderEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CreateOrderEntity> response) {
                        dismissHUD();
                        pay_good_day = response.getData().getActual_value();
                        ActualValue = response.getData().getActual_value();
                        orderNumber = response.getData().getOrderNumber();
                        uc.clickPay.postValue(payCode);
                    }

                    @Override
                    public void onError(RequestException e) {
                        dismissHUD();
                        ToastUtils.showShort(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                        super.onComplete();
                    }
                });
    }


    private void rechargeCreateOrder() {
        if (selectedPosition.get() < 0) {
            ToastUtils.showShort(R.string.please_choose_top_up_package);
            return;
        }
        try {
            AppContext.instance().logEvent(AppsFlyerEvent.Get_vip);
            String payCode = observableList.get(selectedPosition.get()).itemEntity.get().getGoogleGoodsId();
            createOrder(observableList.get(selectedPosition.get()).itemEntity.get().getId(), payCode);
            //uc.clickPay.postValue(payCode);
        } catch (Exception e) {
            AppContext.instance().logEvent(AppsFlyerEvent.vip_google_arouse_error);
            ToastUtils.showShort(e.getMessage());
        }

//
//        VipPackageItemEntity entity = observableList.get(selectedPosition).itemEntity.get();
//        model.createOrder(entity.getId(), 2)
//                .compose(RxUtils.schedulersTransformer())
//                .compose(RxUtils.exceptionTransformer())
//                .doOnSubscribe(this)
//                .subscribe(new BaseObserver<BaseDataResponse<CreateOrderEntity>>() {
//                    @Override
//                    public void onSuccess(BaseDataResponse<CreateOrderEntity> response) {
//                        payOrder(response.getData().getOrderNumber());
//                    }
//                });
    }

    public void paySuccessNotify(String packageName, String productId, String token, Integer event) {
        if (event.intValue() == 0) {
            UserDataEntity userDataEntity = model.readUserData();
            userDataEntity.setIsVip(1);
            userDataEntity.setEndTime(Utils.formatday.format(Utils.addDate(new Date(), pay_good_day == null ? 0 : pay_good_day)));
            model.saveUserData(userDataEntity);
            SystemConfigEntity systemConfigEntity = model.readSystemConfig();
            SystemRoleMoneyConfigEntity sysManUserConfigEntity = systemConfigEntity.getManUser();
            sysManUserConfigEntity.setSendMessagesNumber(-1);
            systemConfigEntity.setManUser(sysManUserConfigEntity);
            SystemRoleMoneyConfigEntity sysManRealConfigEntity = systemConfigEntity.getManReal();
            sysManRealConfigEntity.setSendMessagesNumber(-1);
            systemConfigEntity.setManReal(sysManRealConfigEntity);
            SystemRoleMoneyConfigEntity sysManVipConfigEntity = systemConfigEntity.getManVip();
            sysManVipConfigEntity.setSendMessagesNumber(-1);
            systemConfigEntity.setManVip(sysManVipConfigEntity);
            model.saveSystemConfig(systemConfigEntity);
        }

        model.paySuccessNotify(packageName, orderNumber, productId, token, 2, event)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> {
                    showHUD();
                })
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        if (event.intValue() == 0) {
                            ToastUtils.showShort(StringUtils.getString(R.string.pay_success));
                            model.clearGooglePayCache();
                            Integer sucb = uc.successBack.getValue();
                            if (sucb != null) {
                                sucb += 1;
                            } else {
                                sucb = 1;
                            }
                            uc.successBack.postValue(sucb);
                            RxBus.getDefault().post(new UserUpdateVipEvent(Utils.formatday.format(Utils.addDate(new Date(), pay_good_day)), 1));
                        }

                        //pop();
                        //loadProfile();
                    }

                    @Override
                    public void onError(RequestException e) {
                        if (event.intValue() != 0) {
                            return;
                        }
                        LocalGooglePayCache localGooglePayCache = new LocalGooglePayCache();
                        localGooglePayCache.setOrderNumber(orderNumber);
                        localGooglePayCache.setPackageName(packageName);
                        localGooglePayCache.setProductId(productId);
                        localGooglePayCache.setToken(token);
                        localGooglePayCache.setType(2);
                        localGooglePayCache.setUserId(model.readUserData().getId());
                        model.saveGooglePlay(localGooglePayCache);
                        model.paySuccessNotify(localGooglePayCache.getPackageName(), localGooglePayCache.getOrderNumber(), localGooglePayCache.getProductId(), localGooglePayCache.getToken(), 2, event)
                                .doOnSubscribe(VipSubscribeViewModel.this)
                                .compose(RxUtils.schedulersTransformer())
                                .compose(RxUtils.exceptionTransformer())
                                .subscribe(new BaseObserver<BaseResponse>() {
                                    @Override
                                    public void onSuccess(BaseResponse baseResponse) {
                                        model.clearGooglePayCache();
                                    }

                                    @Override
                                    public void onComplete() {
                                        model.clearGooglePayCache();
                                        Integer sucb = uc.successBack.getValue();
                                        if (sucb != null) {
                                            sucb += 1;
                                        } else {
                                            sucb = 1;
                                        }
                                        uc.successBack.postValue(sucb);
                                        RxBus.getDefault().post(new UserUpdateVipEvent(Utils.formatday.format(Utils.addDate(new Date(), pay_good_day)), 1));
                                        //pop();

                                    }
                                });
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //查询最近的购买交易，并消耗商品
    public void queryAndConsumePurchase() {
        //queryPurchases() 方法会使用 Google Play 商店应用的缓存，而不会发起网络请求
        this.showHUD();
        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS,
                new PurchaseHistoryResponseListener() {
                    @Override
                    public void onPurchaseHistoryResponse(BillingResult billingResult,
                                                          List<PurchaseHistoryRecord> purchaseHistoryRecordList) {
                        dismissHUD();
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchaseHistoryRecordList != null) {
                            for (PurchaseHistoryRecord purchaseHistoryRecord : purchaseHistoryRecordList) {
                                // Process the result.
                                //确认购买交易，不然三天后会退款给用户
                                try {
                                    Purchase purchase = new Purchase(purchaseHistoryRecord.getOriginalJson(), purchaseHistoryRecord.getSignature());
                                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                        //消耗品 开始消耗
                                        consumePuchase(purchase, consumeImmediately);
                                        //确认购买交易
                                        if (!purchase.isAcknowledged()) {
                                            acknowledgePurchase(purchase);
                                        }
                                        //TODO：这里可以添加订单找回功能，防止变态用户付完钱就杀死App的这种
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                });
    }

    //消耗商品
    private void consumePuchase(final Purchase purchase, final int state) {
        this.showHUD();
        String packageName = purchase.getPackageName();
        if (StringUtil.isEmpty(packageName)) {
            packageName = AppContext.instance().getApplicationInfo().packageName;
        }
        String sku = purchase.getSku();
        String pToken = purchase.getPurchaseToken();
        ConsumeParams.Builder consumeParams = ConsumeParams.newBuilder();
        consumeParams.setPurchaseToken(purchase.getPurchaseToken());
        final String finalPackageName = packageName;
        billingClient.consumeAsync(consumeParams.build(), (billingResult, purchaseToken) -> {
            Log.i(TAG, "onConsumeResponse, code=" + billingResult.getResponseCode());
            dismissHUD();
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.i(TAG, "onConsumeResponse,code=BillingResponseCode.OK");
                paySuccessNotify(finalPackageName, sku, pToken, billingResult.getResponseCode());
                //paySuccessNotify(finalPackageName, orderNumber, sku, pToken);
//                    if (state == consumeImmediately) {
//                        viewModel.paySuccessNotify(orderId, packageName, sku, purchaseToken);
//                    }
            } else {
                //如果消耗不成功，那就再消耗一次
                Log.i(TAG, "onConsumeResponse=getDebugMessage==" + billingResult.getDebugMessage());
//                if (state == consumeDelay && billingResult.getDebugMessage().contains("Server error, please try again")) {
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            queryAndConsumePurchase();
//                        }
//                    }, 5 * 1000);
//                }
            }
        });
    }

    //确认订单
    private void acknowledgePurchase(Purchase purchase) {
        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Acknowledge purchase success");
                } else {
                    Log.i(TAG, "Acknowledge purchase failed,code=" + billingResult.getResponseCode() + ",\nerrorMsg=" + billingResult.getDebugMessage());
                }

            }
        };
        billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
    }

    public TaskAdEntity $taskEntity2;
    public TaskAdEntity $taskEntity3;
    public TaskAdEntity $taskEntity4;
    public TaskAdEntity $taskEntity5;
    public TaskAdEntity $taskEntity6;
    public TaskAdEntity $taskEntity7;

    @Override
    public void onEnterAnimationEnd() {
        super.onEnterAnimationEnd();

        loadProfile();
        rechargeVipList();
    }

    //获取会员中心图片配置接口
    public void rechargeVipList() {
        model.rechargeVipList()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseListDataResponse<TaskAdEntity>>() {
                    @Override
                    public void onSuccess(BaseListDataResponse<TaskAdEntity> taskAdEntityBaseListDataResponse) {
                        List<TaskAdEntity> list = taskAdEntityBaseListDataResponse.getData().getData();
                        List<TaskAdEntity> adList = new ArrayList<TaskAdEntity>();
                        if (ObjectUtils.isEmpty(list)) {
                            return;
                        }
                        // 广告类型 3banner图 4卡片图
                        for (TaskAdEntity taskAdEntity : list) {
                            if (taskAdEntity.getAdType() == 3) {
                                adList.add(taskAdEntity);
                            } else if (taskAdEntity.getAdType() == 4) {
                                switch (taskAdEntity.getSort()) {
                                    case 1:
                                        $taskEntity1 = taskAdEntity;
                                        break;
                                    case 2:
                                        $taskEntity2 = taskAdEntity;
                                        break;
                                    case 3:
                                        $taskEntity3 = taskAdEntity;
                                        break;
                                    case 4:
                                        $taskEntity4 = taskAdEntity;
                                        break;
                                    case 5:
                                        $taskEntity5 = taskAdEntity;
                                        break;
                                    case 6:
                                        $taskEntity6 = taskAdEntity;
                                        break;
                                    case 7:
                                        $taskEntity7 = taskAdEntity;
                                        break;
                                }
                            }
                        }
                        uc.loadVipImageNoEmpty.call();
                        if (!ObjectUtils.isEmpty(adList)) {
                            adItemVipEntityList.set(adList);
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });

    }

    public class UIChangeObservable {
        public SingleLiveEvent<String> clickPay = new SingleLiveEvent<>();
        public SingleLiveEvent<Integer> successBack = new SingleLiveEvent<>();
        public SingleLiveEvent<Void> loadVipImageNoEmpty = new SingleLiveEvent<>();
    }


}