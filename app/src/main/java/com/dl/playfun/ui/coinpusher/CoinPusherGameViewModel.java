package com.dl.playfun.ui.coinpusher;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableInt;

import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
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
    //关闭页面点击
    public BindingCommand<Void> gameCloseView = new BindingCommand<>(()->gameUI.backViewApply.call());

    public BindingCommand<Void> playCoinClick = new BindingCommand<>(() -> {
        playingCoinPusherThrowCoin(coinPusherDataInfoEntity.getRoomInfo().getRoomId());
    });

    public BindingCommand<Void> playPusherActClick = new BindingCommand<>(() -> {
        playingCoinPusherAct(coinPusherDataInfoEntity.getRoomInfo().getRoomId());
    });
    //投币
    public void playingCoinPusherThrowCoin(Integer roomId){
        gamePlayingState = loadingPlayer;
        model.playingCoinPusherThrowCoin(roomId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> {
                    loadingShow();
                    //禁用投币按钮
                    gameUI.playingBtnEnable.postValue(false);
                })
                .subscribe(new BaseObserver<BaseDataResponse<CoinPusherBalanceDataEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CoinPusherBalanceDataEntity> coinPusherDataEntityResponse) {
                        CoinPusherBalanceDataEntity coinPusherBalanceDataEntity = coinPusherDataEntityResponse.getData();
                        if(ObjectUtils.isNotEmpty(coinPusherBalanceDataEntity)){
                            totalMoney.set(coinPusherBalanceDataEntity.getTotalGold());
                            gameUI.resetDownTimeEvent.postValue(null);
                        }
                        gameUI.playingBtnEnable.postValue(true);
                        gamePlayingState = null;
                    }

                    @Override
                    public void onError(RequestException e) {
                        //余额不足
                        if(e.getCode() == 21001){
                            gameUI.payDialogViewEvent.call();
                            gameUI.playingBtnEnable.postValue(true);
                            //清除当前投币状态
                            gamePlayingState = null;
                        }else if(e.getCode() == 72000){
                            //中奖--置灰并停止倒计时
                            gameUI.playingBtnEnable.postValue(false);
                            //开始落币
                            gamePlayingState = CustomConstants.CoinPusher.START_WINNING;
                            gameUI.cancelDownTimeEvent.postValue(null);
                        }else{
                            gameUI.playingBtnEnable.postValue(true);
                            //清除当前投币状态
                            gamePlayingState = null;
                        }
                        super.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        loadingHide();
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
        return moneyNum > 99999 ? "99999+" : moneyNum+"";
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
        public SingleLiveEvent<CoinPusherGamePlayingEvent> toastCenter = new SingleLiveEvent<>();
        //禁止投币按钮操作
        public SingleLiveEvent<Boolean> playingBtnEnable = new SingleLiveEvent<>();
        //返回上一页
        public SingleLiveEvent<Void> backViewApply = new SingleLiveEvent<>();
        //余额不足。弹出充值弹窗
        public SingleLiveEvent<Void> payDialogViewEvent = new SingleLiveEvent<>();
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
                        switch (coinPusherGamePlayingEvent.getState()) {
                            case CustomConstants.CoinPusher.START_WINNING:
                                //开始落币
                                gamePlayingState = CustomConstants.CoinPusher.START_WINNING;
                                gameUI.cancelDownTimeEvent.postValue(null);
                                gameUI.playingBtnEnable.postValue(false);
                                break;
                            case CustomConstants.CoinPusher.END_WINNING:
                                //落币结束
                                gamePlayingState = null;
                                gameUI.resetDownTimeEvent.postValue(null);
                                gameUI.playingBtnEnable.postValue(true);
                                break;
                            case CustomConstants.CoinPusher.DROP_COINS:
                                //落币奖励
                                //gamePlayingState = null;
                                gameUI.toastCenter.postValue(coinPusherGamePlayingEvent);
                                break;
                            case CustomConstants.CoinPusher.LITTLE_GAME_WINNING:
                                //中奖 小游戏（叠叠乐、小玛利）
                                gamePlayingState = CustomConstants.CoinPusher.START_WINNING;
                                gameUI.cancelDownTimeEvent.postValue(null);
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
