package com.dl.playfun.ui.message.chooselocation;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import com.blankj.utilcode.util.GsonUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.GoogleNearPoiBean;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.viewmodel.BaseRefreshViewModel;

import java.util.HashMap;
import java.util.Map;

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
public class ChooseLocationViewModel extends BaseRefreshViewModel<AppRepository> {

    public double currentLat, currentLng;
    public ChooseLocationItemViewModel chooseLocationItemViewModel;
    public BindingRecyclerViewAdapter<ChooseLocationItemViewModel> adapter = new BindingRecyclerViewAdapter<>();
    public ObservableList<ChooseLocationItemViewModel> observableList = new ObservableArrayList<>();
    public ItemBinding<ChooseLocationItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_choose_location);
    UIChangeObservable uc = new UIChangeObservable();
    public BindingCommand sendOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            uc.clickSend.call();
        }
    });
    public BindingCommand searchOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            uc.clickSearch.call();
        }
    });
    private String nextPageToken = null;

    public ChooseLocationViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
    }

    public void onItemClick(int position) {
        for (ChooseLocationItemViewModel chooseLocationItemViewModel : observableList) {
            chooseLocationItemViewModel.chooseed.set(false);
        }
        observableList.get(position).chooseed.set(true);
        uc.itemChooseed.postValue(position);
    }

    @Override
    public void loadDatas(int page) {
        nearPlaceSearch(currentLat, currentLng);
    }

    public void research(String name, String address, Double lat, Double lng) {
        chooseLocationItemViewModel = new ChooseLocationItemViewModel(ChooseLocationViewModel.this, name, address, lat, lng, true);
        nextPageToken = null;
        nearPlaceSearch(lat, lng);
    }

    public void research(Double lat, Double lng) {
        nextPageToken = null;
        nearPlaceSearch(lat, lng);
    }

    public void nearPlaceSearch(double lat, double lng) {
        currentLat = lat;
        currentLng = lng;
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("pagetoken", nextPageToken);
        dataMap.put("location", (Double.valueOf(lat) == null) ? null : String.format("%s,%s", lat, lng));
        dataMap.put("radius", 2000);
        model.nearSearchPlace(ApiUitl.getBody(GsonUtils.toJson(dataMap)))
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .subscribe(new BaseObserver<BaseDataResponse<GoogleNearPoiBean>>() {

                    @Override
                    public void onError(Throwable e) {
                        if (nextPageToken == null) {
                            observableList.clear();
                            if (chooseLocationItemViewModel != null) {
                                observableList.add(0, chooseLocationItemViewModel);
                            }
                        }
                        ToastUtils.showShort(R.string.google_map_error);
                        dismissHUD();
                        stopRefreshOrLoadMore();
                    }

                    @Override
                    public void onSuccess(BaseDataResponse<GoogleNearPoiBean> googleNearPoiBeanBaseDataResponse) {
                        GoogleNearPoiBean googlePoiBean = googleNearPoiBeanBaseDataResponse.getData();
                        if (nextPageToken == null) {
                            observableList.clear();
                            if (chooseLocationItemViewModel != null) {
                                observableList.add(0, chooseLocationItemViewModel);
                            }
                        }
                        nextPageToken = googlePoiBean.getNext_page_token();
                        if ("OK".equals(googlePoiBean.getStatus())) {
                            for (GoogleNearPoiBean.ResultsBean result : googlePoiBean.getResults()) {
                                ChooseLocationItemViewModel chooseLocationItemViewModel = new ChooseLocationItemViewModel(ChooseLocationViewModel.this, result.getName(), result.getVicinity(), result.getGeometry().getLocation().getLat(), result.getGeometry().getLocation().getLng(), false);
                                observableList.add(chooseLocationItemViewModel);
                            }
                        }
                    }

                    @Override
                    public void onComplete() {
                        stopRefreshOrLoadMore();
                    }

                });
    }

    public class UIChangeObservable {
        public SingleLiveEvent<Void> clickSend = new SingleLiveEvent<>();
        public SingleLiveEvent<Integer> itemChooseed = new SingleLiveEvent<>();
        public SingleLiveEvent<Void> clickSearch = new SingleLiveEvent<>();
    }
}