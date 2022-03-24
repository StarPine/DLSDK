package com.dl.playfun.ui.mine.task.golddetail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;

import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseListDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.ExchangeIntegraOuterEntity;
import com.dl.playfun.entity.GoldDetailEntity;
import com.dl.playfun.viewmodel.BaseRefreshViewModel;

import org.jetbrains.annotations.NotNull;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * Author: 彭石林
 * Time: 2021/8/11 11:23
 * Description: 积分明细列表 GoldDetailViewModel
 */
public class GoldDetailViewModel extends BaseRefreshViewModel<AppRepository> {

    public ObservableField<String> totalMoney = new ObservableField<String>("0");

    public BindingRecyclerViewAdapter<GoldDetailItemViewModel> adapter = new BindingRecyclerViewAdapter<>();
    public ItemBinding<GoldDetailItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.task_center_gold_item_fragment);
    public ObservableList<GoldDetailItemViewModel> observableList = new ObservableArrayList<>();

    public UIChangeObservable goldUc = new UIChangeObservable();
    public BindingCommand toBack = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
//            ApiUitl.taskBottom = true;
//            pop();
            //查询钻石兑换积分列表
            getExchangeIntegraListData();
        }
    });

    /*
     * @Desc TODO(查询钻石兑换积分列表数据)
     * @author 彭石林
     * @parame []
     * @return void
     * @Date 2021/9/23
     */
    public void getExchangeIntegraListData(){
        model.getExchangeIntegraListData()
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> {
                    showHUD();
                })
                .subscribe(new BaseObserver<BaseDataResponse<ExchangeIntegraOuterEntity>>(){
                    @Override
                    public void onSuccess(BaseDataResponse<ExchangeIntegraOuterEntity> exchangeIntegraEntityBaseListDataResponse) {
                        dismissHUD();
                        ExchangeIntegraOuterEntity listData = exchangeIntegraEntityBaseListDataResponse.getData();
                        if(!ObjectUtils.isEmpty(listData)){
                            if(!ObjectUtils.isEmpty(listData.getData())){
                                goldUc.DialogExchangeIntegral.postValue(listData);
                            }
                        }
                    }
                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public GoldDetailViewModel(@NonNull @NotNull Application application, AppRepository model) {
        super(application, model);
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        loadDatas(1);
    }

    @Override
    public void loadDatas(int page) {
        getGoldList(page);
    }

    public void getGoldList(int page) {
        model.getGoldList(page)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseListDataResponse<GoldDetailEntity>>() {
                    @Override
                    public void onSuccess(BaseListDataResponse<GoldDetailEntity> exchangeEntityList) {
                        if (currentPage == 1) {
                            observableList.clear();
                        }
                        String total = exchangeEntityList.getData().getTotalMoney();
                        if (ObjectUtils.isEmpty(total)) {
                            totalMoney.set("0");
                        } else {
                            if (total.indexOf(".") != -1) {
                                totalMoney.set(total.substring(0, total.indexOf(".")));
                            } else {
                                totalMoney.set(total);
                            }
                        }
                        for (GoldDetailEntity goldDetailEntity : exchangeEntityList.getData().getData()) {
                            GoldDetailItemViewModel goldDetailItemViewModel = new GoldDetailItemViewModel(GoldDetailViewModel.this, goldDetailEntity);
                            observableList.add(goldDetailItemViewModel);
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                        stopRefreshOrLoadMore();
                    }
                });
    }

    /**
     * @Desc TODO(钻石购买积分)
     * @author 彭石林
     * @parame [id]
     * @return void
     * @Date 2021/9/23
     */
    public void ExchangeIntegraBuy(Integer id,int TotalCoin,int Coin){
        model.ExchangeIntegraBuy(id)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>(){
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {
                        totalMoney.set(String.valueOf(TotalCoin + Coin));
                        ToastUtils.showShort(R.string.dialog_exchange_integral_success);
                    }
                    @Override
                    public void onError(RequestException e) {
                        if(e.getMessage()!=null){
                            ToastUtils.showShort(e.getMessage());
                        }

                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });

    }

    public class UIChangeObservable {
        //弹出钻石兑换弹窗
        public SingleLiveEvent<ExchangeIntegraOuterEntity> DialogExchangeIntegral = new SingleLiveEvent<>();
        //弹出充值钻石弹窗
        public SingleLiveEvent<Void> DialogCoinExchangeIntegral = new SingleLiveEvent<>();
    }
}
