package com.dl.playfun.ui.login.register;

import android.app.Application;

import androidx.annotation.NonNull;

import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.CreateOrderEntity;
import com.dl.playfun.entity.GoodsEntity;
import com.dl.playfun.entity.LocalGooglePayCache;
import com.dl.playfun.entity.SystemConfigEntity;
import com.dl.playfun.entity.SystemRoleMoneyConfigEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.viewmodel.BaseViewModel;

import java.util.List;

import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Author: 彭石林
 * Time: 2022/10/18 18:05
 * Description: This is FriendsWillViewModel
 */
public class FriendsWillWebViewViewModel extends BaseViewModel<AppRepository> {

    public String orderNumber = null;

    public GoodsEntity goodsEntity;

    public SingleLiveEvent<String> payOnClick = new SingleLiveEvent<>();

    public FriendsWillWebViewViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
    }

    public void createOrder() {
        model.createOrder(goodsEntity.getId(), 1, 2, null)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<CreateOrderEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CreateOrderEntity> response) {
                        orderNumber = response.getData().getOrderNumber();
                        payOnClick.postValue(goodsEntity.getGoogleGoodsId());
                    }

                    @Override
                    public void onError(RequestException e) {
                        ToastUtils.showShort(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public void paySuccessNotify(String packageName, List<String> productId, String token, Integer event) {

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
                    }

                    @Override
                    public void onError(RequestException e) {

                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }
}
