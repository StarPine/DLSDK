package com.dl.playfun.ui.webview;

import android.app.Application;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.app.Injection;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.CoinWalletEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.viewmodel.BaseViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Author: 彭石林
 * Time: 2021/9/16 14:07
 * Description: This is PlayGameViewModel
 */
public class PlayGameViewModel extends BaseViewModel<AppRepository> {
    public UIChangeObservable uc = new UIChangeObservable();

    public PlayGameViewModel(@NonNull @NotNull Application application, AppRepository model) {
        super(application, model);
    }

    public String getToken() {
        return model.readLoginInfo().getToken();
    }

    //查询当前钻石余额
    public void getBalance() {
        Injection.provideDemoRepository()
                .coinWallet()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<CoinWalletEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CoinWalletEntity> response) {
                        uc.sendWebBalance.postValue(response.getData().getTotalCoin());
                    }

                    @Override
                    public void onError(RequestException e) {
                        ToastUtils.showShort(e.getMessage());
                        uc.sendWebBalance.postValue(-1);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                    }
                });
    }

    /**
     * @return void
     * @Desc TODO(获取本地用户信息接口)
     * @author 彭石林
     * @parame []
     * @Date 2021/9/16
     */
    public void getUserData() {
        UserDataEntity userDataEntity = model.readUserData();
        if (!ObjectUtils.isEmpty(userDataEntity)) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", userDataEntity.getId());
            userData.put("name", userDataEntity.getNickname());
            userData.put("avater", StringUtil.getFullImageUrl(userDataEntity.getAvatar()));
            uc.sendWebUserData.postValue(userData);
        } else {
            uc.sendWebUserData.postValue(null);
        }
    }

    public class UIChangeObservable {
        //加载任务中心系统配置
        public SingleLiveEvent<Integer> sendWebBalance = new SingleLiveEvent<>();
        //回传数据给到Web端
        public SingleLiveEvent<Map<String, Object>> sendWebUserData = new SingleLiveEvent<>();
    }
}
