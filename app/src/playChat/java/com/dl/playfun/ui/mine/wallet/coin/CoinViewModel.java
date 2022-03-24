package com.dl.playfun.ui.mine.wallet.coin;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;

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
import com.dl.playfun.entity.CoinWalletEntity;
import com.dl.playfun.entity.UserCoinItemEntity;
import com.dl.playfun.entity.WithdrawNumberEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.main.MainFragment;
import com.dl.playfun.viewmodel.BaseRefreshViewModel;
import com.dl.playfun.widget.emptyview.EmptyState;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * @author wulei
 */
public class CoinViewModel extends BaseRefreshViewModel<AppRepository> {

    public BindingRecyclerViewAdapter<CoinItemViewModel> adapter = new BindingRecyclerViewAdapter<>();

    public ObservableField<CoinWalletEntity> coinWalletEntity = new ObservableField<>();

    public ObservableList<CoinItemViewModel> observableList = new ObservableArrayList<>();

    public ObservableField<String> paypalAccount = new ObservableField<>(StringUtils.getString(R.string.fragment_cash_withdraw_account_bind));
    public ObservableField<String> emptyText = new ObservableField<>();
    public ObservableField<Boolean> isShowEmpty = new ObservableField<Boolean>(false);
    //RecyclerView多布局添加ItemBinding
    public ItemBinding<CoinItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_coin_earnings);
    UIChangeObservable uc = new UIChangeObservable();
    public BindingCommand withdrawAccountOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            uc.clickSetWithdrawAccount.call();
        }
    });
    //跳轉到任務中心
    public BindingCommand doingTasknClickCommand = new BindingCommand(() -> {
        AppConfig.homePageName = "navigation_rank";
        popTo(MainFragment.class.getCanonicalName());
    });
    //提領點擊事件
    public BindingCommand withdrawOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            if (coinWalletEntity.get() == null) {
                return;
            }
            if (StringUtils.isEmpty(coinWalletEntity.get().getAccountNumber())) {
                ToastUtils.showShort(R.string.warn_paypal_account_not_null);
                return;
            }
            loadWithdrawNumber();
//            if (coinWalletEntity.get().getCanCoin() < model.readSystemConfig().getCashOutServiceFee()) {
//                ToastUtils.showShort(String.format(getString(R.string.warn_balance_insufficient), model.readSystemConfig().getCashOutServiceFee()));
//            } else {
//                loadWithdrawNumber();
//            }
        }
    });

    public boolean getIsMale(){
        return ConfigManager.getInstance().isMale();
    }

    public CoinViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
    }

    @Override
    public void onEnterAnimationEnd() {
        super.onEnterAnimationEnd();
        startRefresh();
    }

    @Override
    public void loadDatas(int page) {
        if (page == 1) {
            loadCoinWallet();
        }
        if(ConfigManager.getInstance().isMale()){
            emptyText.set(StringUtils.getString(R.string.coin_fragment_empty_male));
        }else {
            emptyText.set(StringUtils.getString(R.string.coin_fragment_empty_female));
        }
        model.userCoinEarnings(page)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseListEmptyObserver<BaseListDataResponse<UserCoinItemEntity>>(this) {
                    @Override
                    public void onSuccess(BaseListDataResponse<UserCoinItemEntity> response) {
                        if (response.getData() == null || response.getData().getTotal() == 0) {
                            isShowEmpty.set(true);
                            stateModel.setEmptyState(EmptyState.EMPTY);
                        } else {
                            isShowEmpty.set(false);
                            stateModel.setEmptyState(EmptyState.NORMAL);
                        }

                        if (page == 1) {
                            observableList.clear();
                        }
                        for (UserCoinItemEntity entity : response.getData().getData()) {
                            CoinItemViewModel item = new CoinItemViewModel(CoinViewModel.this, entity);
                            observableList.add(item);
                        }

                    }

                    @Override
                    public void onError(RequestException e) {
                        isShowEmpty.set(true);
                        onComplete();
                    }

                    @Override
                    public void onComplete() {
                        stopRefreshOrLoadMore();
                    }
                });
    }

    public void loadWithdrawNumber() {
        model.getWithdrawNumber()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<WithdrawNumberEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<WithdrawNumberEntity> response) {
                        uc.clickWithdraw.postValue(response.getData());
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public void loadCoinWallet() {
        model.coinWallet()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<CoinWalletEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CoinWalletEntity> response) {
                        coinWalletEntity.set(response.getData());
                        if (!StringUtils.isEmpty(response.getData().getAccountNumber())) {
                            paypalAccount.set(String.format("%s(%s)", response.getData().getAccountNumber(), response.getData().getRealName()));
                        }
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public void setWithdrawAccount(String account, String name) {
        model.setWithdrawAccount(name, account)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ToastUtils.showShort(R.string.setting_success);
                        coinWalletEntity.get().setAccountNumber(account);
                        coinWalletEntity.get().setRealName(name);
                        paypalAccount.set(String.format("%s(%s)", account, name));
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public void cashOut() {
        model.coinCashOut()
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        uc.withdrawComplete.call();
                        startRefresh();
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public class UIChangeObservable {
        public SingleLiveEvent<Void> clickSetWithdrawAccount = new SingleLiveEvent<>();
        public SingleLiveEvent<WithdrawNumberEntity> clickWithdraw = new SingleLiveEvent<>();
        public SingleLiveEvent<Void> withdrawComplete = new SingleLiveEvent<>();
    }

}