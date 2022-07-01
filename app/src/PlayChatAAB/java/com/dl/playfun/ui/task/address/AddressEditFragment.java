package com.dl.playfun.ui.task.address;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentAddressEditBinding;
import com.dl.playfun.entity.AddressCityEntity;
import com.dl.playfun.entity.AddressCityItemEntity;
import com.dl.playfun.entity.AddressEntity;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.widget.dialog.AddressDialog;

import java.util.List;

import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Author: 彭石林
 * Time: 2021/8/13 14:57
 * Description: 添加/修改收获地址
 */
public class AddressEditFragment extends BaseToolbarFragment<FragmentAddressEditBinding, AddressEditViewModel> {

    boolean editAddress = false;
    Integer keyId = -1;

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return R.layout.fragment_address_edit;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public AddressEditViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(AddressEditViewModel.class);
    }

    @Override
    public void initParam() {
        super.initParam();
        editAddress = getArguments().getBoolean("edit", false);
        keyId = getArguments().getInt("keyId", 0);
    }

    @Override
    public void initData() {
        super.initData();
        String jsonData = ApiUitl.getAssetsJson(getContext(), "city_reginos.json");
        List<AddressCityEntity> list = ApiUitl.getObjectList(jsonData, AddressCityEntity.class);
        for (AddressCityEntity addressCityEntity : list) {
            viewModel.addressCityEntity.add(addressCityEntity);
        }
        if (editAddress) {
            viewModel.editAddress = true;
            viewModel.getAddress(keyId);
        } else {
            AddressEntity addressEntity = new AddressEntity();
            addressEntity.setId(1);
            viewModel.addressEntity.set(addressEntity);
        }
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        //选择城市
        viewModel.uc.clickLocationSel.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {
                String city = viewModel.CityName.get() == null ? null : viewModel.CityName.get().getCity();
                AddressDialog.getCityDialogs(mActivity, viewModel.addressCityEntity, city, new AddressDialog.AddessCityChooseCity() {
                    @Override
                    public void clickListItem(Dialog dialog, AddressCityEntity address) {
                        dialog.dismiss();
                        if (!ObjectUtils.isEmpty(viewModel.CityName.get()) && ObjectUtils.equals(viewModel.CityName.get(), address)) {
                            return;
                        } else {
                            viewModel.CityItemName.set(null);
                        }
                        viewModel.CityName.set(address);
                    }
                });
            }
        });

        //选择城市
        viewModel.uc.clickLocationItemSel.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {
                if (viewModel.CityName.get() == null) {
                    ToastUtils.showShort(R.string.address_error1);
                    return;
                }
                String city = viewModel.CityItemName.get() == null ? null : viewModel.CityItemName.get().getRegion();
                AddressDialog.getCityItemDialogs(mActivity, viewModel.CityName.get().getRegions(), city, new AddressDialog.AddessCityItemChooseCity() {
                    @Override
                    public void clickListItem(Dialog dialog, AddressCityItemEntity address) {
                        dialog.dismiss();
                        viewModel.CityItemName.set(address);
                    }
                });
            }
        });
    }
}
