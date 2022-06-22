package com.dl.playfun.ui.mine.wallet;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dl.playfun.app.AppConfig;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.GameCoinWalletEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.mine.wallet.coin.CoinFragment;
import com.dl.playfun.ui.mine.wallet.girl.TwDollarMoneyFragment;
import com.dl.playfun.ui.mine.webview.FukubukuroViewFragment;
import com.dl.playfun.viewmodel.BaseViewModel;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * @author wulei
 */
public class WalletViewModel extends BaseViewModel<AppRepository> {
    //账户钻石余额
    public ObservableField<String> totalCoin = new ObservableField<String>();
    //账户货币币余额
    public ObservableField<String> totalProfit = new ObservableField<>();
    // 货币名称
    public ObservableField<String> coinName = new ObservableField<>();
    // 游戏币余额
    public ObservableField<String> totalGameCoin = new ObservableField<>();

    public SingleLiveEvent<Void> certification = new SingleLiveEvent<>();

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

    public BindingCommand clickJoyMaskCoinDetail = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
//            start(CoinFragment.class.getCanonicalName());
        }
    });

    public BindingCommand withdrawonClickCommand = new BindingCommand(() -> {
        //没有进行真人认证
        try {
            Bundle bundle = new Bundle();
            bundle.putString("link", AppConfig.WEB_BASE_URL + "reflect");
            start(FukubukuroViewFragment.class.getCanonicalName(), bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    });

    public void getUserAccount(){
        model.getUserAccountPageInfo()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<GameCoinWalletEntity>>(){

                    @Override
                    public void onSuccess(BaseDataResponse<GameCoinWalletEntity> coinWalletEntityBaseDataResponse) {
                        GameCoinWalletEntity gameCoinWalletEntity = coinWalletEntityBaseDataResponse.getData();
                        if(gameCoinWalletEntity!=null){
                            totalCoin.set(String.valueOf(gameCoinWalletEntity.getTotalCoins()));
                            totalProfit.set(String.format("%.2f", gameCoinWalletEntity.getTotalProfit()));
                            coinName.set(gameCoinWalletEntity.getCurrencyName());
                            totalGameCoin.set(String.valueOf(gameCoinWalletEntity.getTotalAppCoins()));
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

    public boolean isMaleHideFlag(){
       return model.readUserData().getSex()==1;
    }

}