package com.dl.playfun.ui.mine.wallet;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.CoinWalletEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.mine.wallet.coin.CoinFragment;
import com.dl.playfun.ui.mine.wallet.girl.TwDollarMoneyFragment;
import com.dl.playfun.viewmodel.BaseViewModel;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * @author wulei
 */
public class WalletViewModel extends BaseViewModel<AppRepository> {
    //账户钻石余额
    public ObservableField<String> totalCoin = new ObservableField<String>();
    //账户台币余额
    public ObservableField<String> totalProfit = new ObservableField<>();

    public WalletViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
    }

    public BindingCommand clickCoinMoneyView = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            start(CoinFragment.class.getCanonicalName());
        }
    });


    public BindingCommand clickGirlMoneyView = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            start(TwDollarMoneyFragment.class.getCanonicalName());
        }
    });

    public void getUserAccount(){
        model.getUserAccount()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<CoinWalletEntity>>(){

                    @Override
                    public void onSuccess(BaseDataResponse<CoinWalletEntity> coinWalletEntityBaseDataResponse) {
                        CoinWalletEntity coinWalletEntity = coinWalletEntityBaseDataResponse.getData();
                        if(coinWalletEntity!=null){
                            totalCoin.set(String.valueOf(coinWalletEntity.getTotalCoin()));
                            totalProfit.set(String.format("%.2f", coinWalletEntity.getTotalProfit()));
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public boolean getTipMoneyShowFlag() {
        return ConfigManager.getInstance().getTipMoneyShowFlag();
    }

}