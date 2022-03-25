package com.dl.playfun.ui.mine.task.record.list;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseListEmptyObserver;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseListDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.AddressEntity;
import com.dl.playfun.entity.ExchangeEntity;
import com.dl.playfun.entity.SystemConfigEntity;
import com.dl.playfun.entity.SystemRoleMoneyConfigEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.event.AddressEvent;
import com.dl.playfun.event.UserUpdateVipEvent;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.utils.ChatUtils;
import com.dl.playfun.utils.ExceptionReportUtils;
import com.dl.playfun.utils.ListUtils;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.utils.Utils;
import com.dl.playfun.viewmodel.BaseViewModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.RxSubscriptions;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * Author: 彭石林
 * Time: 2021/8/12 11:36
 * Description: This is ExchangeRecordListViewModel
 */
public class ExchangeRecordListViewModel extends BaseViewModel<AppRepository> {
    private static final String TAG = "兑换记录签到领取会员";
    private final int consumeImmediately = 0;
    private final Integer pay_good_day = 7;
    //是否全选
    public ObservableField<Boolean> checkAll = new ObservableField<Boolean>(false);
    public BindingRecyclerViewAdapter<ExchangeRecordItemViewModel> adapter = new BindingRecyclerViewAdapter<>();
    public ObservableList<ExchangeRecordItemViewModel> observableList = new ObservableArrayList<>();
    public ItemBinding<ExchangeRecordItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_task_record);
    //刷新
    public int currentPage = 1;
    public Integer grend = 0;
    //放置主键
    public List<Integer> sub_key;
    public UIChangeObservable uc = new UIChangeObservable();
    /*谷歌支付*/
    public BillingClient billingClient;
    public SkuDetails goodSkuDetails = null;
    public SingleLiveEvent<String> clickPay = new SingleLiveEvent();
    //全选
    public BindingCommand clickSelAll = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            int len = observableList.size();
            if (len < 1) {
                return;
            }
            boolean check = checkAll.get().booleanValue();
            for (int i = 0; i < len; i++) {
                observableList.get(i).agree.set(check);
            }

        }
    });
    //立即提货
    public BindingCommand clickBtnConfirm = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            sub_key = new ArrayList<>();
            for (int i = 0; i < observableList.size(); i++) {
                ExchangeRecordItemViewModel exchangeRecordItemViewModel = observableList.get(i);
                if (exchangeRecordItemViewModel.agree.get().booleanValue()) {
                    sub_key.add(exchangeRecordItemViewModel.itemEntity.get().getId());
                }
            }
            if (ListUtils.isEmpty(sub_key) || sub_key.size() == 0) {
                ToastUtils.showShort(R.string.task_exchange_record_fragment_check_empty);
            } else {
                getAddress(null);
            }

        }
    });
    //咨询客服
    public BindingCommand toUseAdminMessage = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            try {
                ChatUtils.chatUser(AppConfig.CHAT_SERVICE_USER_ID, StringUtils.getString(R.string.chat_service_name), ExchangeRecordListViewModel.this);
            } catch (Exception e) {
                ExceptionReportUtils.report(e);
            }
        }
    });
    //下拉刷新
    public BindingCommand onRefreshCommand = new BindingCommand(() -> {
        currentPage = 1;
        loadDatas(currentPage);
    });
    public BindingCommand onLoadMoreCommand = new BindingCommand(() -> nextPage());
    private Disposable AddressSubscription;
    private String orderNumber = null;
    private String google_goods_id = null;

    public ExchangeRecordListViewModel(@NonNull @NotNull Application application, AppRepository model) {
        super(application, model);
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        loadDatas(1);
    }

    @Override
    public void registerRxBus() {
        super.registerRxBus();
        AddressSubscription = RxBus.getDefault().toObservable(AddressEvent.class)
                .subscribe(event -> {
                    //接受到观察者模式事件
                    if (ApiUitl.$address != null) {//条件判断是否是选择收获地址点击某个地址使用
                        uc.checkAddressed.setValue(ApiUitl.$address);
                    }
                });
    }

    @Override
    public void removeRxBus() {
        super.removeRxBus();
        RxSubscriptions.remove(AddressSubscription);
    }

    //单选-回调
    public void isSelectedAll() {
        int len = observableList.size();
        int total = 0;
        for (int i = 0; i < len; i++) {
            if (observableList.get(i).agree.get().booleanValue()) {
                total++;
            }
        }
        if (total == len) {
            checkAll.set(true);
        } else {
            checkAll.set(false);
        }
    }

    protected void nextPage() {
        currentPage++;
        loadDatas(currentPage);
    }

    /**
     * 停止下拉刷新或加载更多动画
     */
    protected void stopRefreshOrLoadMore() {
        if (currentPage == 1) {
            uc.finishRefreshing.call();
        } else {
            uc.finishLoadmore.call();
        }
    }

    public void loadDatas(int page) {
        qryExchange(page, grend);
    }

    public void qryExchange(int page, int type) {
        model.qryExchange(page, type)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseListEmptyObserver<BaseListDataResponse<ExchangeEntity>>(ExchangeRecordListViewModel.this) {
                    @Override
                    public void onSuccess(BaseListDataResponse<ExchangeEntity> exchangeEntityBaseList) {
                        super.onSuccess(exchangeEntityBaseList);
                        stateModel.setEmptyRetryCommand(StringUtils.getString(R.string.task_exchange_record_fragment_hint), R.drawable.empy_list_exchange_record, R.color.empty_list_hint);
                        if (currentPage == 1) {
                            observableList.clear();
                        }
                        for (ExchangeEntity itemEntity : exchangeEntityBaseList.getData().getData()) {
                            ExchangeRecordItemViewModel item = new ExchangeRecordItemViewModel(ExchangeRecordListViewModel.this, itemEntity, grend);
                            observableList.add(item);
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                        stopRefreshOrLoadMore();
                    }
                });

    }

    //获取地址
    public void getAddress(Integer id) {
        model.getAddress(id)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<AddressEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<AddressEntity> addressEntityBaseDataResponse) {
                        uc.checkAddressed.setValue(addressEntityBaseDataResponse.getData());
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //子模块点击获取地址
    public void getAddress(Integer id, int index) {
        model.getAddress(id)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<AddressEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<AddressEntity> addressEntityBaseDataResponse) {
                        AddressEntity entity = addressEntityBaseDataResponse.getData();
                        if (entity != null) {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(entity.getContacts() + "  " + entity.getPhone() + "\n")
                                    .append(entity.getCity() + "  " + entity.getAre() + "\n")
                                    .append(entity.getAddress());
                            observableList.get(index).isDetailText.set(stringBuilder.toString());
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //立即提货
    public void subSupply(List<Integer> exchange_ids, Integer address_id) {
        model.subSupply(exchange_ids, address_id)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {
                        uc.subSupplySuccess.call();
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    /*=====谷歌支付核心代码=====*/
    //根据iD查询谷歌商品。并且订购它 7days_free
    public void querySkuOrder(String goodId) {
        ArrayList<String> goodList = new ArrayList<>();
        goodList.add(goodId);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(goodList).setType(BillingClient.SkuType.SUBS);
        if (billingClient == null) {
            ToastUtils.showShort(R.string.invite_web_detail_error);
            return;
        }
        billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(@NonNull @NotNull BillingResult billingResult, @Nullable @org.jetbrains.annotations.Nullable List<SkuDetails> skuDetailsList) {
                int responseCode = billingResult.getResponseCode();
                String debugMessage = billingResult.getDebugMessage();
                switch (responseCode) {
                    case BillingClient.BillingResponseCode.OK:
                        Log.i(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                        if (skuDetailsList == null) {
                            //订阅找不到
                            Log.w(TAG, "onSkuDetailsResponse: null SkuDetails list");
                            ToastUtils.showShort(R.string.invite_web_detail_error2);
                        } else {
                            for (SkuDetails skuDetails : skuDetailsList) {
                                if (skuDetails.getSku().equals(goodId)) {
                                    goodSkuDetails = skuDetails;
                                    clickPay.postValue(skuDetails.getSku());
                                    break;
                                }
                            }
                        }
                        break;
                    case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                    case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                    case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                    case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                    case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                    case BillingClient.BillingResponseCode.ERROR:
                        Log.e(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                        break;
                    case BillingClient.BillingResponseCode.USER_CANCELED:
                        Log.i(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                        break;
                    // These response codes are not expected.
                    case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                    case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                    case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
                    default:
                        Log.wtf(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                }
            }
        });
    }

    public void createOrder(Integer goods_type) {
        model.freeSevenDay(2, goods_type)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> {
                    showHUD();
                })
                .subscribe(new BaseObserver<BaseDataResponse<Map<String, String>>>() {

                    @Override
                    public void onSuccess(BaseDataResponse<Map<String, String>> mapBaseDataResponse) {
                        orderNumber = mapBaseDataResponse.getData().get("order_number");
                        google_goods_id = mapBaseDataResponse.getData().get("google_goods_id");
                        querySkuOrder(google_goods_id);
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

    //查询最近的购买交易，并消耗商品
    public void queryAndConsumePurchase() {
        //queryPurchases() 方法会使用 Google Play 商店应用的缓存，而不会发起网络请求
        this.showHUD();

        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP,
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
        String sku = purchase.getSkus().toString();
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
            } else {
                //如果消耗不成功，那就再消耗一次
                Log.i(TAG, "onConsumeResponse=getDebugMessage==" + billingResult.getDebugMessage());
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

    /**
     * 回调结果给后台
     *
     * @param packageName
     * @param productId
     * @param token
     * @param event
     */
    public void paySuccessNotify(String packageName, String productId, String token, Integer event) {
        if (event.intValue() == BillingClient.BillingResponseCode.OK) {
            UserDataEntity userDataEntity = model.readUserData();
            userDataEntity.setIsVip(1);
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
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> {
                    showHUD();
                })
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ToastUtils.showShort(StringUtils.getString(R.string.pay_success));
                        try {
                            RxBus.getDefault().post(new UserUpdateVipEvent(Utils.formatday.format(Utils.addDate(new Date(), pay_good_day)), 1));
                        } catch (Exception e) {

                        }
                    }

                    @Override
                    public void onError(RequestException e) {

                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                        loadDatas(1);
                    }
                });
    }

    /*谷歌支付*/
    public class UIChangeObservable {
        //下拉刷新开始
        public SingleLiveEvent<Void> startRefreshing = new SingleLiveEvent<>();
        //下拉刷新完成
        public SingleLiveEvent<Void> finishRefreshing = new SingleLiveEvent<>();
        //上拉加载完成
        public SingleLiveEvent<Void> finishLoadmore = new SingleLiveEvent<>();
        //选择收获地址
        public SingleLiveEvent<AddressEntity> checkAddressed = new SingleLiveEvent<>();
        //提货成功
        public SingleLiveEvent<Void> subSupplySuccess = new SingleLiveEvent<>();

        public SingleLiveEvent<Integer> toSubVipPlay = new SingleLiveEvent<>();
    }
    /*=====谷歌支付核心代码=====*/
}
