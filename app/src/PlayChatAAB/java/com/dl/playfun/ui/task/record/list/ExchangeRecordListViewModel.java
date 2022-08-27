package com.dl.playfun.ui.task.record.list;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.SkuDetails;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
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
import com.dl.playfun.utils.Utils;
import com.dl.playfun.viewmodel.BaseViewModel;

import org.jetbrains.annotations.NotNull;

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
                ChatUtils.chatUser(AppConfig.CHAT_SERVICE_USER_ID_SEND,0, StringUtils.getString(R.string.playfun_chat_service_name), ExchangeRecordListViewModel.this);
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
    public String orderNumber = null;
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
                        uc.querySkuOrderEvent.postValue(google_goods_id);
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


    /**
     * 回调结果给后台
     *
     * @param packageName
     * @param productId
     * @param token
     * @param event
     */
    public void paySuccessNotify(String packageName, List<String> productId, String token, Integer event) {
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
                        ToastUtils.showShort(StringUtils.getString(R.string.playfun_pay_success));
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
        //拉起订阅
        public SingleLiveEvent<String> querySkuOrderEvent = new SingleLiveEvent<>();
    }
    /*=====谷歌支付核心代码=====*/
}
