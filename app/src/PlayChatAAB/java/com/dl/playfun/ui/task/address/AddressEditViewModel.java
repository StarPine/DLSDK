package com.dl.playfun.ui.task.address;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.R;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.AddressCityEntity;
import com.dl.playfun.entity.AddressCityItemEntity;
import com.dl.playfun.entity.AddressEntity;
import com.dl.playfun.viewmodel.BaseViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RegexUtils;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.StringUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Author: 彭石林
 * Time: 2021/8/13 14:57
 * Description: This is AddressViewModel
 */
public class AddressEditViewModel extends BaseViewModel<AppRepository> {

    public ObservableField<AddressEntity> addressEntity = new ObservableField<>();
    public ObservableField<Boolean> isDefault = new ObservableField<>(false);
    public ObservableField<AddressCityEntity> CityName = new ObservableField<>();
    public ObservableField<AddressCityItemEntity> CityItemName = new ObservableField<>();

    public List<AddressCityEntity> addressCityEntity = new ArrayList<>();
    //是否为修改收获地址
    public boolean editAddress = false;
    //添加地址成功
    public BindingCommand subAddress = new BindingCommand(() -> {
        if (addressEntity.get() == null) {
            ToastUtils.showShort(R.string.address_error);
            return;
        }
        AddressEntity entity = addressEntity.get();
        if (StringUtils.isTrimEmpty(entity.getContacts())) {
            ToastUtils.showShort(R.string.address_name_hint);
            return;
        }
        if (entity.getContacts().trim().length() > 8) {
            ToastUtils.showShort(R.string.address_error2);
            return;
        }
        if (StringUtils.isTrimEmpty(entity.getPhone())) {
            ToastUtils.showShort(R.string.address_phone_hint);
            return;
        }
        if (!RegexUtils.isMatch("^-?[0-9]+", entity.getPhone())) {
            ToastUtils.showShort(R.string.address_error3);
            return;
        }
        if (entity.getPhone().trim().length() != 10) {
            ToastUtils.showShort(R.string.address_error3);
            return;
        }

        if (StringUtils.isTrimEmpty(entity.getAddress())) {
            ToastUtils.showShort(R.string.address_error);
            return;
        }
        if (entity.getAddress().trim().length() > 100) {
            ToastUtils.showShort(R.string.address_error4);
            return;
        }
        if (ObjectUtils.isEmpty(CityName.get())) {
            ToastUtils.showShort(R.string.address_error1);
            return;
        }

        if (ObjectUtils.isEmpty(CityItemName.get())) {
            ToastUtils.showShort(R.string.address_city_hint2);
            return;
        }
        if (editAddress) {
            model.updateAddress(entity.getId(), entity.getContacts(), CityName.get().getCity(), CityItemName.get().getRegion(), entity.getAddress(), entity.getPhone(), isDefault.get().booleanValue() == true ? 1 : 0)
                    .doOnSubscribe(this)
                    .compose(RxUtils.schedulersTransformer())
                    .compose(RxUtils.exceptionTransformer())
                    .doOnSubscribe(disposable -> showHUD())
                    .subscribe(new BaseObserver<BaseResponse>() {
                        @Override
                        public void onSuccess(BaseResponse baseResponse) {
                            ToastUtils.showShort(R.string.address_edit_success);
                        }

                        @Override
                        public void onComplete() {
                            dismissHUD();
                            pop();
                        }
                    });
        } else {
            model.createAddress(entity.getContacts(), CityName.get().getCity(), CityItemName.get().getRegion(), entity.getAddress(), entity.getPhone(), isDefault.get().booleanValue() == true ? 1 : 0)
                    .doOnSubscribe(this)
                    .compose(RxUtils.schedulersTransformer())
                    .compose(RxUtils.exceptionTransformer())
                    .doOnSubscribe(disposable -> showHUD())
                    .subscribe(new BaseObserver<BaseResponse>() {
                        @Override
                        public void onSuccess(BaseResponse baseResponse) {
                            ToastUtils.showShort(R.string.address_add_success);
                        }

                        @Override
                        public void onComplete() {
                            dismissHUD();
                            pop();
                        }
                    });
        }
    });
    UIChangeObservable uc = new UIChangeObservable();
    /**
     * 点击选择城市弹窗内容值
     */
    public BindingCommand clickLocationSelCommand = new BindingCommand(() -> {
        uc.clickLocationSel.call();
    });
    /**
     * 点击选择城市弹窗内容值
     */
    public BindingCommand clickLocationItemSelCommand = new BindingCommand(() -> {
        uc.clickLocationItemSel.call();
    });

    public AddressEditViewModel(@NonNull @NotNull Application application, AppRepository model) {
        super(application, model);
    }

    //获取地址
    public void getAddress(Integer ids) {
        model.getAddress(ids)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<AddressEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<AddressEntity> addressEntityBaseDataResponse) {
                        AddressEntity entity = addressEntityBaseDataResponse.getData();
                        addressEntity.set(entity);
                        String city = entity.getCity();
                        String are = entity.getAre();
                        isDefault.set(entity.getIsDefault() == 1);
                        for (AddressCityEntity cityEntity : addressCityEntity) {
                            if (city.equals(cityEntity.getCity())) {
                                CityName.set(cityEntity);
                                for (AddressCityItemEntity itemEntity : cityEntity.getRegions()) {
                                    if (itemEntity.getRegion().equals(are)) {
                                        CityItemName.set(itemEntity);
                                        break;
                                    }
                                }
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
        public SingleLiveEvent<Void> clickLocationSel = new SingleLiveEvent();
        public SingleLiveEvent<Void> clickLocationItemSel = new SingleLiveEvent();
    }
}
