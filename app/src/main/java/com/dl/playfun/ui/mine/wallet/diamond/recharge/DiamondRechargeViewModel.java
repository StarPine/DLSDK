package com.dl.playfun.ui.mine.wallet.diamond.recharge;

import android.app.Application;
import android.icu.text.UFormat;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.CreateOrderEntity;
import com.dl.playfun.entity.DiamondInfoEntity;
import com.dl.playfun.entity.GoodsEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.viewmodel.BaseViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * @Name： PlayFun_Google
 * @Description：
 * @Author： liaosf
 * @Date： 2022/6/15 18:23
 * 修改备注：
 */
public class DiamondRechargeViewModel extends BaseViewModel<AppRepository> {

    public BindingRecyclerViewAdapter<DiamondRechargeItemViewModel> diamondRechargeAdapter = new BindingRecyclerViewAdapter<>();
    public ObservableList<DiamondRechargeItemViewModel> diamondRechargeList = new ObservableArrayList<>();
    public ItemBinding<DiamondRechargeItemViewModel> diamondRechargeItem = ItemBinding.of(BR.viewModel, R.layout.item_diamond_recharge);

    public ObservableField<GoodsEntity> selectedGoodsEntity = new ObservableField<>();
    public ObservableField<DiamondInfoEntity> diamondInfo = new ObservableField<>();
    public String orderNumber = null;
    public int selectedPosition = -1;
    public SingleLiveEvent<String> payOnClick = new SingleLiveEvent();
    public SingleLiveEvent<GoodsEntity> finsh = new SingleLiveEvent();


    /**
     * 确认支付
     */
    public BindingCommand confirmPayOnClick = new BindingCommand(() -> {
        createOrder();
    });

    public DiamondRechargeViewModel(@NonNull @NotNull Application application, AppRepository model) {
        super(application, model);
    }

    public void itemClick(int position, GoodsEntity goodsEntity) {
        for (DiamondRechargeItemViewModel itemViewModel : diamondRechargeList) {
            itemViewModel.itemEntity.get().setSelected(false);
        }
        selectedGoodsEntity.set(goodsEntity);
        AppContext.instance().logEvent("Recharge_" + (position + 1));
        diamondRechargeList.get(position).itemEntity.get().setSelected(true);
        selectedPosition = position;
    }


    public void createOrder() {
        if (selectedPosition < 0){
            ToastUtils.showShort(R.string.playfun_please_choose_top_up_package);
            return;
        }
        model.createOrder(selectedGoodsEntity.get().getId(), 1, 2, null)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<CreateOrderEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CreateOrderEntity> response) {
                        orderNumber = response.getData().getOrderNumber();
                        payOnClick.postValue(selectedGoodsEntity.get().getGoogleGoodsId());
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

    /**
     * 回调结果给后台
     *
     * @param packageName
     * @param productId
     * @param token
     * @param event
     */
    public void paySuccessNotify(String packageName, List<String> productId, String token, Integer event) {
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
                        finsh.postValue(selectedGoodsEntity.get());
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


    public boolean isMale(){
        return ConfigManager.getInstance().isMale();
    }

    public String getTotalCoin(DiamondInfoEntity diamondInfoEntity){
        String total = "";
        if (diamondInfoEntity == null){
            return total;
        }
        int totalCoin = diamondInfoEntity.getTotalCoin();
        if (totalCoin > 999999){
            total = "999999+";
        }else {
            total = totalCoin+"";
        }
        return total;
    }

    public void rechargeList() {
        model.goods()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<DiamondInfoEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<DiamondInfoEntity> response) {
                        DiamondInfoEntity infoEntity = response.getData();
                        diamondInfo.set(infoEntity);
                        List<GoodsEntity> data = infoEntity.getList();
                        for (GoodsEntity goodsEntity : data) {
                            DiamondRechargeItemViewModel itemViewModel = new DiamondRechargeItemViewModel(DiamondRechargeViewModel.this,goodsEntity);
                            diamondRechargeList.add(itemViewModel);
                        }
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

}
