package com.dl.playfun.ui.coinpusher;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableInt;

import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.CoinPusherBalanceDataEntity;
import com.dl.playfun.entity.CoinPusherDataInfoEntity;
import com.dl.playfun.event.CoinPusherGamePlayingEvent;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.tencent.qcloud.tuicore.custom.CustomConstants;

import io.reactivex.disposables.Disposable;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.RxSubscriptions;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * Author: 彭石林
 * Time: 2022/8/26 11:08
 * Description: This is CoinPusherGameViewModel
 */
public class CoinPusherGameViewModel extends BaseViewModel <AppRepository> {
    //加载状态
    public final String loadingPlayer = "loadingPlayer";
    //游戏状态
    public String gamePlayingState;

    //livedata页面交互
    public UIChangeObservable gameUI = new UIChangeObservable();
    public ObservableInt totalMoney = new ObservableInt(0);
    public CoinPusherDataInfoEntity coinPusherDataInfoEntity;

    //消费者
    private Disposable coinPusherGamePlayingSubscription;

    public CoinPusherGameViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
    }

    public BindingCommand playCoinClick = new BindingCommand(() -> {
        playingCoinPusherThrowCoin(coinPusherDataInfoEntity.getRoomInfo().getRoomId());
    });

    public BindingCommand playPusherActClick = new BindingCommand(() -> {
        playingCoinPusherAct(coinPusherDataInfoEntity.getRoomInfo().getRoomId());
    });
    //投币
    public void playingCoinPusherThrowCoin(Integer roomId){
        gamePlayingState = loadingPlayer;
        model.playingCoinPusherThrowCoin(roomId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> loadingShow())
                .subscribe(new BaseObserver<BaseDataResponse<CoinPusherBalanceDataEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CoinPusherBalanceDataEntity> coinPusherDataEntityResponse) {
                        CoinPusherBalanceDataEntity coinPusherBalanceDataEntity = coinPusherDataEntityResponse.getData();
                        if(ObjectUtils.isNotEmpty(coinPusherBalanceDataEntity)){
                            totalMoney.set(coinPusherBalanceDataEntity.getTotalGold());
                            gameUI.resetDownTimeEvent.postValue(null);
                        }
                    }

                    @Override
                    public void onComplete() {
                        loadingHide();
                        gamePlayingState = null;
                    }
                });
    }
    //操作雨刷
    public void playingCoinPusherAct(Integer roomId){
        model.playingCoinPusherAct(roomId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> loadingShow())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {

                    }

                    @Override
                    public void onComplete() {
                        loadingHide();
                    }
                });
    }
    //结束游戏
    public void playingCoinPusherClose(Integer roomId){
        model.playingCoinPusherClose(roomId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {

                    }
                });
    }

    //查询用户当前余额
    public void qryUserGameBalance(){
        model.qryUserGameBalance()
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<CoinPusherBalanceDataEntity>>(){
                    @Override
                    public void onSuccess(BaseDataResponse<CoinPusherBalanceDataEntity> coinPusherBalanceDataEntityResponse) {
                        CoinPusherBalanceDataEntity coinPusherBalanceDataEntity = coinPusherBalanceDataEntityResponse.getData();
                        if(ObjectUtils.isNotEmpty(coinPusherBalanceDataEntity)){
                            totalMoney.set(coinPusherBalanceDataEntity.getTotalGold());
                            gameUI.resetDownTimeEvent.postValue(null);
                        }
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        dismissHUD();
                    }
                });
    }

    public String tvTotalMoneyRefresh(int moneyNum){
        return moneyNum > 99999 ? moneyNum+"+" : moneyNum+"";
    }

    public static class UIChangeObservable{
        //取消倒计时
        public SingleLiveEvent<Void> cancelDownTimeEvent = new SingleLiveEvent<>();
        //重置倒计时
        public SingleLiveEvent<Void> resetDownTimeEvent = new SingleLiveEvent<>();
        //开始显示loading进度条
        public SingleLiveEvent<Void> loadingShow = new SingleLiveEvent<>();
        //关闭进度条Loading显示
        public SingleLiveEvent<Void> loadingHide = new SingleLiveEvent<>();
        //toast弹窗居中
        public SingleLiveEvent<String> toastCenter = new SingleLiveEvent<>();
        //禁止投币按钮操作
        public SingleLiveEvent<Boolean> playingBtnEnable = new SingleLiveEvent<>();
    }
    //显示loading
    public void loadingShow(){
        gameUI.loadingShow.call();
    }
    //隐藏loading
    public void loadingHide(){
        gameUI.loadingHide.call();
    }

    @Override
    public void registerRxBus() {
        coinPusherGamePlayingSubscription = RxBus.getDefault().toObservable(CoinPusherGamePlayingEvent.class)
                .subscribe(coinPusherGamePlayingEvent -> {
                    if(coinPusherGamePlayingEvent!=null){
                        //开始落币
                        switch (coinPusherGamePlayingEvent.getState()) {
                            case CustomConstants.CoinPusher.START_WINNING:
                                gamePlayingState = CustomConstants.CoinPusher.START_WINNING;
                                gameUI.cancelDownTimeEvent.postValue(null);
                                gameUI.playingBtnEnable.setValue(false);
                                break;
                            case CustomConstants.CoinPusher.END_WINNING:
                                //落币结束
                                gamePlayingState = null;
                                //重新开始落币
                                gameUI.resetDownTimeEvent.postValue(null);
                                gameUI.playingBtnEnable.setValue(true);
                                break;
                            case CustomConstants.CoinPusher.DROP_COINS:
                                gamePlayingState = null;
                                //中奖落币
                                String textContent = StringUtils.getString(R.string.playfun_coinpusher_coin_text_reward);
                                gameUI.toastCenter.postValue(String.format(textContent, coinPusherGamePlayingEvent.getGoldNumber()));
                                break;
                        }
                    }
                });
        //将订阅者加入管理站
        RxSubscriptions.add(coinPusherGamePlayingSubscription);
    }

    @Override
    public void removeRxBus() {
        RxSubscriptions.remove(coinPusherGamePlayingSubscription);
    }


}
