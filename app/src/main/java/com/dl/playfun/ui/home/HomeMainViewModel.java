package com.dl.playfun.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseListEmptyObserver;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseListDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.AdBannerEntity;
import com.dl.playfun.entity.AdItemEntity;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.entity.ParkItemEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.event.AddBlackListEvent;
import com.dl.playfun.event.CityChangeEvent;
import com.dl.playfun.event.LoadEvent;
import com.dl.playfun.event.LocationChangeEvent;
import com.dl.playfun.event.OnlineChangeEvent;
import com.dl.playfun.ui.home.search.SearchFragment;
import com.dl.playfun.ui.viewmodel.BaseParkItemViewModel;
import com.dl.playfun.ui.viewmodel.BaseParkViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.RxSubscriptions;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * @author wulei
 */
public class HomeMainViewModel extends BaseParkViewModel<AppRepository> {

    public BindingRecyclerViewAdapter<HomeMainTabItemViewModel> adapterTab = new BindingRecyclerViewAdapter<>();
    public ObservableList<HomeMainTabItemViewModel> observableListTab = new ObservableArrayList<>();
    public ItemBinding<HomeMainTabItemViewModel> itemTabBinding = ItemBinding.of(BR.viewModel, R.layout.item_main_tab);

    public BindingRecyclerViewAdapter<HomeMainBannerItemViewModel> adapterBanner = new BindingRecyclerViewAdapter<>();
    public ObservableList<HomeMainBannerItemViewModel> observableBanner = new ObservableArrayList<>();
    public ItemBinding<HomeMainBannerItemViewModel> itemBannerBinding = ItemBinding.of(BR.viewModel, R.layout.item_main_banner);
    //位置选择文字
    public ObservableField<String> regionTitle = new ObservableField<>(StringUtils.getString(R.string.playfun_tab_female_1));

    public ObservableField<Boolean> showLocationAlert = new ObservableField<>(false);
    //推荐用户弹窗
    public ObservableField<Integer> cityId = new ObservableField<>();
    public ObservableField<Boolean> gender = new ObservableField<>();//false:女 ，true: 男
    public ObservableField<Boolean> online = new ObservableField<>(true);
    public ObservableField<Boolean> locationService = new ObservableField<>(true);
    public ObservableField<Double> lat = new ObservableField<>();//纬度
    public ObservableField<Double> lng = new ObservableField<>();//经度
    public List<ConfigItemEntity> list_chooseCityItem = new ArrayList<>();
    //男女-选项卡内容
    public ObservableField<Integer> type = new ObservableField<>(1);


    public UIChangeObservable uc = new UIChangeObservable();

    //订阅者
    private Disposable mSubscription;
    private Disposable mLocationSubscription;
    private Disposable mCitySubscription;
    private Disposable mAddBlackListSubscription;

    public int lastTabClickIdx = -1;


    public Integer userSex = null;
    //搜索按钮的点击事件
    public BindingCommand searchOnClickCommand = new BindingCommand(() -> {
        AppContext.instance().logEvent(AppsFlyerEvent.Nearby_Search);
        start(SearchFragment.class.getCanonicalName());
    }
    );
    public BindingCommand toTaskClickCommand = new BindingCommand(() -> {
        uc.clickAccountDialog.setValue("0");
        AppContext.instance().logEvent(AppsFlyerEvent.homepage_batch_accost);
//        uc.starActivity.call();
    });

    public BindingCommand regionOnClickCommand = new BindingCommand(() -> uc.clickRegion.call());
    /**
     * 在线优先改变
     */
    public BindingCommand onlineOnCheckedChangeCommand = new BindingCommand<>(() -> {
        online.set(!online.get());
        if (online.get()) {
            AppContext.instance().logEvent(AppsFlyerEvent.Nearby_Online_First);
        }
        RxBus.getDefault().post(new OnlineChangeEvent(online.get()));
    });
    /**
     * 点击性别
     */
    public BindingCommand genderOnClickCommand = new BindingCommand(() -> {
        observableListTab.clear();
        AppContext.instance().logEvent(AppsFlyerEvent.Nearby_Change_gender);
        gender.set(!gender.get());
        initGenderTab();
        onRefreshCommand.execute();
    });

    public void initGenderTab() {
        List<HomeMainTabItemViewModel> listData = new ArrayList<>();
        if(gender.get()){
            Map<String,Object> map1 = new HashMap<>();
            map1.put("type",1);
            map1.put("text",StringUtils.getString(R.string.playfun_tab_male_1));
            HomeMainTabItemViewModel homeMainItemViewModel1 = new HomeMainTabItemViewModel(this,map1,true);
            lastTabClickIdx = 0;
            listData.add(homeMainItemViewModel1);
            Map<String,Object> map2 = new HashMap<>();
            map2.put("type",4);
            map2.put("text",StringUtils.getString(R.string.playfun_tab_male_2));
            HomeMainTabItemViewModel homeMainItemViewModel2 = new HomeMainTabItemViewModel(this,map2,false);
            listData.add(homeMainItemViewModel2);
        }else{
            Map<String,Object> map1 = new HashMap<>();
            map1.put("type",1);
            map1.put("text",StringUtils.getString(R.string.playfun_tab_female_3));
            HomeMainTabItemViewModel homeMainItemViewModel1 = new HomeMainTabItemViewModel(this,map1,true);
            lastTabClickIdx = 0;
            listData.add(homeMainItemViewModel1);
            Map<String,Object> map2 = new HashMap<>();
            map2.put("type",2);
            map2.put("text",StringUtils.getString(R.string.playfun_tab_female_1));
            HomeMainTabItemViewModel homeMainItemViewModel2 = new HomeMainTabItemViewModel(this,map2,false);
            listData.add(homeMainItemViewModel2);
            Map<String,Object> map3 = new HashMap<>();
            map3.put("type",3);
            map3.put("text",StringUtils.getString(R.string.playfun_tab_female_2));
            HomeMainTabItemViewModel homeMainItemViewModel3 = new HomeMainTabItemViewModel(this,map3,false);
            listData.add(homeMainItemViewModel3);
        }
        observableListTab.addAll(listData);
    }

    //消费者
    private Disposable loadReceive;

    public HomeMainViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
        try {
            userSex = repository.readUserData().getSex();
            gender.set(repository.readUserData().getSex() != 1);
        }catch (Exception e) {
            userSex = 1;
            gender.set(true);
        }
        initGenderTab();
        list_chooseCityItem.addAll(model.readCityConfig());
    }

    public void titleRcvItemClick(int idx,int checkType) {
        if (observableListTab.isEmpty()) {
            return;
        }
        if (lastTabClickIdx == -1) {
            observableListTab.get(idx).checked.set(true);
            type.set(checkType);
            lastTabClickIdx = idx;
            onRefreshCommand.execute();
        } else {
            if (idx != lastTabClickIdx) {
                observableListTab.get(lastTabClickIdx).checked.set(false);
                observableListTab.get(idx).checked.set(true);
                type.set(checkType);
                lastTabClickIdx = idx;
                onRefreshCommand.execute();
            }
        }
        //展示首页广告位
        getAdListBannber();
    }

    public void isBindCity(Integer city_id) {
        model.isBindCity(city_id)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {
                        UserDataEntity userDataEntity = model.readUserData();
                        ArrayList<Integer> list = new ArrayList<>();
                        list.add(city_id);
                        userDataEntity.setPermanentCityIds(list);
                        model.saveUserData(userDataEntity);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public UserDataEntity loadLocalUserData() {
        return model.readUserData();
    }

    @Override
    public void registerRxBus() {
        super.registerRxBus();
        loadReceive = RxBus.getDefault().toObservable(LoadEvent.class)
                .subscribe(countDownTimerEvent -> {
                    uc.isLoad.postValue(countDownTimerEvent.isLoad());
                });
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
        RxSubscriptions.remove(loadReceive);
        RxSubscriptions.remove(mSubscription);
        RxSubscriptions.remove(mLocationSubscription);
        RxSubscriptions.remove(mCitySubscription);
        RxSubscriptions.remove(mAddBlackListSubscription);
    }


    @Override
    public void onLazyInitView() {
        super.onLazyInitView();
        loadDatas(1);
    }

    @Override
    public void AccostFirstSuccess(ParkItemEntity itemEntity, int position) {
        if (itemEntity == null) {//提醒充值钻石
            uc.sendAccostFirstError.call();
        } else {
            //loadLoteAnime.postValue(position);
//            ChatUtils.chatUser(itemEntity.getId(), itemEntity.getNickname(), HomeListViewModel.this);
        }
    }

    //批量搭讪
    public void putAccostList(List<Integer> userIds) {
        model.putAccostList(userIds)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {

                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    @Override
    public void loadDatas(int page) {

        if (currentPage == 1 && observableList.size()>0) {
            observableList.clear();
        }
        model.homeList(cityId.get(),
                        type.get(),
                        online.get() ? 1 : 0,
                        gender.get()? 1 : 0,
                        null,
                        lng.get(),
                        lat.get(),
                        page
                )
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .subscribe(new BaseListEmptyObserver<BaseListDataResponse<ParkItemEntity>>(this) {
                    @Override
                    public void onSuccess(BaseListDataResponse<ParkItemEntity> response) {
                        super.onSuccess(response);
                        int sex = model.readUserData().getSex();
                        for (ParkItemEntity itemEntity : response.getData().getData()) {
                            BaseParkItemViewModel item = new BaseParkItemViewModel(HomeMainViewModel.this, sex, itemEntity);
                            observableList.add(item);
                        }
                    }

                    @Override
                    public void onComplete() {
                        stopRefreshOrLoadMore();
                    }
                });
    }

    /**
    * @Desc TODO(获取首页广告位)
    * @author 彭石林
    * @parame []
    * @return void
    * @Date 2022/7/25
    */
    public void getAdListBannber(){
        model.getMainAdBannerList(1)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<AdBannerEntity>>(){

                    @Override
                    public void onSuccess(BaseDataResponse<AdBannerEntity> listBaseDataResponse) {
                        if( listBaseDataResponse.getData()!=null){
                            List<AdItemEntity> listData = listBaseDataResponse.getData().getDataList();
                            for (AdItemEntity adItemEntity : listData){
                                HomeMainBannerItemViewModel homeItemBanner = new HomeMainBannerItemViewModel(HomeMainViewModel.this,adItemEntity);
                                observableBanner.add(homeItemBanner);
                            }
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public class UIChangeObservable {
        //选择位置
        public SingleLiveEvent<Void> clickRegion = new SingleLiveEvent<>();
        //打开批量搭讪接口
        public SingleLiveEvent<String> clickAccountDialog = new SingleLiveEvent<>();
        public SingleLiveEvent<Boolean> isLoad = new SingleLiveEvent<>();
        public SingleLiveEvent<Void> starActivity = new SingleLiveEvent<>();
        //搭讪失败。充值钻石
        public SingleLiveEvent<Void> sendAccostFirstError = new SingleLiveEvent<>();
        public SingleLiveEvent<Integer> loadLoteAnime = new SingleLiveEvent<>();
    }

}