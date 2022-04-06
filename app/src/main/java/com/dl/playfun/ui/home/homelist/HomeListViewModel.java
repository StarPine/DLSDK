package com.dl.playfun.ui.home.homelist;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseListEmptyObserver;
import com.dl.playfun.data.source.http.response.BaseListDataResponse;
import com.dl.playfun.entity.ParkItemEntity;
import com.dl.playfun.event.AddBlackListEvent;
import com.dl.playfun.event.CityChangeEvent;
import com.dl.playfun.event.LocationChangeEvent;
import com.dl.playfun.event.OnlineChangeEvent;
import com.dl.playfun.ui.home.HomeMainViewModel;
import com.dl.playfun.ui.viewmodel.BaseParkItemViewModel;
import com.dl.playfun.ui.viewmodel.BaseParkViewModel;

import io.reactivex.disposables.Disposable;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.RxSubscriptions;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * @author wulei
 */
public class HomeListViewModel extends BaseParkViewModel<AppRepository> {

    public ObservableField<Integer> type = new ObservableField<>(1);

    public HomeMainViewModel homeMainViewModel;

    //订阅者
    private Disposable mSubscription;
    private Disposable mLocationSubscription;
    private Disposable mCitySubscription;
    private Disposable mAddBlackListSubscription;

    //搭讪失败。充值钻石
    public SingleLiveEvent<Void> sendAccostFirstError = new SingleLiveEvent<>();
    public SingleLiveEvent<Integer> loadLoteAnime = new SingleLiveEvent<>();

    public HomeListViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
    }


    @Override
    public void registerRxBus() {
        super.registerRxBus();
        mSubscription = RxBus.getDefault().toObservable(OnlineChangeEvent.class)
                .subscribe(event -> {
                    startRefresh();
                });
        mLocationSubscription = RxBus.getDefault().toObservable(LocationChangeEvent.class)
                .subscribe(event -> {
                    startRefresh();
                });

        mCitySubscription = RxBus.getDefault().toObservable(CityChangeEvent.class)
                .subscribe(cityChangeEvent -> {
                    startRefresh();
                });
        mAddBlackListSubscription = RxBus.getDefault().toObservable(AddBlackListEvent.class)
                .subscribe(cityChangeEvent -> {
                    startRefresh();
                });

        //将订阅者加入管理站
        RxSubscriptions.add(mSubscription);
        RxSubscriptions.add(mLocationSubscription);
        RxSubscriptions.add(mCitySubscription);
        RxSubscriptions.add(mAddBlackListSubscription);
    }

    @Override
    public void removeRxBus() {
        super.removeRxBus();
        //将订阅者从管理站中移除
        RxSubscriptions.remove(mSubscription);
        RxSubscriptions.remove(mLocationSubscription);
        RxSubscriptions.remove(mCitySubscription);
        RxSubscriptions.remove(mAddBlackListSubscription);
    }

    @Override
    public void AccostFirstSuccess(ParkItemEntity itemEntity, int position) {
        if (itemEntity == null) {//提醒充值钻石
            sendAccostFirstError.call();
        } else {
            loadLoteAnime.postValue(position);

//            ChatUtils.chatUser(itemEntity.getId(), itemEntity.getNickname(), HomeListViewModel.this);
        }
    }

    @Override
    public void onLazyInitView() {
        super.onLazyInitView();
        loadDatas(1);
    }

    @Override
    public void loadDatas(int page) {
        if (homeMainViewModel == null) {
            return;
        }
        if (currentPage == 1 && observableList.size()>0) {
            observableList.clear();
        }
        model.homeList(homeMainViewModel.cityId.get(),
                type.get(),
                homeMainViewModel.online.get() ? 1 : 0,
                homeMainViewModel.gender.get()? 1 : 0,
                null,
                homeMainViewModel.lng.get(),
                homeMainViewModel.lat.get(),
                page
        )
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .subscribe(new BaseListEmptyObserver<BaseListDataResponse<ParkItemEntity>>(this) {
                    @Override
                    public void onSuccess(BaseListDataResponse<ParkItemEntity> response) {
                        Log.e("请求返回数据",response.getData().getData().size()+"");
                        super.onSuccess(response);
                        int sex = model.readUserData().getSex();
                        for (ParkItemEntity itemEntity : response.getData().getData()) {
                            BaseParkItemViewModel item = new BaseParkItemViewModel(HomeListViewModel.this, sex, itemEntity);
                            observableList.add(item);
                        }
                    }

                    @Override
                    public void onComplete() {
                        stopRefreshOrLoadMore();
                    }
                });
    }

}
