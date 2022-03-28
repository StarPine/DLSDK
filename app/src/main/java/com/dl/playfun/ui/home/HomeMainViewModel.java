package com.dl.playfun.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.entity.SystemConfigTaskEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.event.LoadEvent;
import com.dl.playfun.event.MessageTagEvent;
import com.dl.playfun.event.OnlineChangeEvent;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.dl.playfun.R;
import com.dl.playfun.ui.dialog.CityChooseDialog;
import com.dl.playfun.ui.home.search.SearchFragment;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.RxSubscriptions;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * @author wulei
 */
public class HomeMainViewModel extends BaseViewModel<AppRepository> {

    public ObservableField<Boolean> showLocationAlert = new ObservableField<>(false);
    public ObservableField<Boolean> isCurrentPage = new ObservableField<>(false);
    //推荐用户弹窗
    public ObservableField<Integer> cityId = new ObservableField<>();
    public ObservableField<String> regionTitle = new ObservableField<>(StringUtils.getString(R.string.playfun_tab_female_1));
    public ObservableField<Boolean> gender = new ObservableField<>();//false:女 ，true: 男
    public ObservableField<Boolean> online = new ObservableField<>(true);
    public ObservableField<Boolean> locationService = new ObservableField<>(true);
    public ObservableField<Double> lat = new ObservableField<>();//纬度
    public ObservableField<Double> lng = new ObservableField<>();//经度
    public ObservableField<ConfigItemEntity> SelConfigItemEntity = new ObservableField<>();
    public List<ConfigItemEntity> list_chooseCityItem = new ArrayList<>();
    public UIChangeObservable uc = new UIChangeObservable();

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
    });
    /**
     * 在线优先改变
     */
    public BindingCommand<Boolean> onlineOnCheckedChangeCommand = new BindingCommand<>(isChecked -> {
        online.set(isChecked);
        if (isChecked) {
            AppContext.instance().logEvent(AppsFlyerEvent.Nearby_Online_First);
        }
        RxBus.getDefault().post(new OnlineChangeEvent(isChecked));
    });
    //城市按钮的点击事件
    public BindingCommand cityOnClickCommand = new BindingCommand(() -> {
//            CityChoiceDialog dialog = new CityChoiceDialog();
        AppContext.instance().logEvent(AppsFlyerEvent.Nearby_Nearby_2);
        start(CityChooseDialog.class.getCanonicalName());
    });
    /**
     * 点击性别
     */
    public BindingCommand genderOnClickCommand = new BindingCommand(() -> {
        uc.genderCheckedChange.postValue(gender.get() ? AppConfig.FEMALE : AppConfig.MALE);
        AppContext.instance().logEvent(AppsFlyerEvent.Nearby_Change_gender);
        gender.set(!gender.get());
    });
    /**
     * 点击选择城市弹窗内容值
     */
    public BindingCommand clickLocationSelCommand = new BindingCommand(() -> {
        uc.clickLocationSel.call();
    });
    /**
     * 确定城市选择
     */
    public BindingCommand clickbtnConfirm = new BindingCommand(() -> {
        uc.clickLocationConfirm.call();
    });
    public BindingCommand regionOnClickCommand = new BindingCommand(() -> uc.clickRegion.call());
    //消费者
    private Disposable MessageTagReceive, CountDownTimerReceive, loadReceive;

    public HomeMainViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
        //gender.set(repository.readUserData().getSex() != 1);
        userSex = repository.readUserData().getSex();
        list_chooseCityItem.addAll(model.readCityConfig());
        SelConfigItemEntity.set(model.readCityConfig().get(0));
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

    }

    @Override
    public void removeRxBus() {
        super.removeRxBus();
        RxSubscriptions.remove(MessageTagReceive);
        RxSubscriptions.remove(CountDownTimerReceive);
        RxSubscriptions.remove(loadReceive);
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();
//        SystemConfigTaskEntity systemConfigTaskEntity = ConfigManager.getInstance().getTaskConfig();
//        if (!ObjectUtils.isEmpty(systemConfigTaskEntity) && !StringUtils.isTrimEmpty(systemConfigTaskEntity.getEntryLabel())) {
//            uc.loadSysConfigTask.setValue(systemConfigTaskEntity);
//        }
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

    public class UIChangeObservable {
        public SingleLiveEvent clickRegion = new SingleLiveEvent<>();
        public SingleLiveEvent<Integer> genderCheckedChange = new SingleLiveEvent<>();
        public SingleLiveEvent clickLocationSel = new SingleLiveEvent();
        public SingleLiveEvent clickLocationConfirm = new SingleLiveEvent();
        public SingleLiveEvent clickToMessageDetail = new SingleLiveEvent();
        //加载任务中心系统配置
        public SingleLiveEvent<SystemConfigTaskEntity> loadSysConfigTask = new SingleLiveEvent<>();
        //打开批量搭讪接口
        public SingleLiveEvent<String> clickAccountDialog = new SingleLiveEvent<>();
        public SingleLiveEvent<Boolean> isLoad = new SingleLiveEvent<>();
    }

}