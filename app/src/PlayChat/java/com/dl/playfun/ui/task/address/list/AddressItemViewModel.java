package com.dl.playfun.ui.task.address.list;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dl.playfun.entity.AddressEntity;
import com.dl.playfun.event.AddressEvent;
import com.dl.playfun.ui.task.address.AddressEditFragment;
import com.dl.playfun.utils.ApiUitl;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import me.goldze.mvvmhabit.base.MultiItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;

/**
 * Author: 彭石林
 * Time: 2021/8/14 0:20
 * Description: This is AddressItemViewModel
 */
public class AddressItemViewModel extends MultiItemViewModel<AddressListViewModel> {

    public ObservableField<AddressEntity> itemEntity = new ObservableField<>();
    public int isDefault = 0;
    //去使用
    public BindingCommand toSubmit = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            AddressEntity entity = itemEntity.get();
            ApiUitl.$address = entity;
            if (entity.getIsDefault().intValue() != isDefault) {
                viewModel.updateAddress(itemEntity.get());
            }
            RxBus.getDefault().post(new AddressEvent());
            viewModel.pop();
        }
    });
    //选中
    public BindingCommand selChecked = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            for (AddressItemViewModel addModel : viewModel.observableList) {
                if (addModel.itemEntity.get().getId().intValue() == itemEntity.get().getId().intValue()) {
                    itemEntity.get().setChecked(true, itemEntity.get().getIsDefault().intValue() == 1 ? 0 : 1);
                } else {
                    addModel.itemEntity.get().setChecked(false, 0);
                }
                viewModel.adapter.notifyDataSetChanged();
            }
        }
    });
    //编辑
    public BindingCommand editAddress = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            Bundle bundle = new Bundle();
            bundle.putBoolean("edit", true);
            bundle.putInt("keyId", itemEntity.get().getId());
            viewModel.start(AddressEditFragment.class.getCanonicalName(), bundle);
        }
    });
    //删除
    public BindingCommand removeddress = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            int index = viewModel.observableList.indexOf(AddressItemViewModel.this);
            Map<String, Integer> data = new HashMap<>();
            data.put("id", itemEntity.get().getId());
            data.put("index", index);
            viewModel.uc.alertDelteAddress.setValue(data);
        }
    });

    public AddressItemViewModel(@NonNull @NotNull AddressListViewModel viewModel, AddressEntity itemEntity) {
        super(viewModel);
        this.itemEntity.set(itemEntity);
        this.isDefault = itemEntity.getIsDefault().intValue();
    }

    public String getAddress() {
        return itemEntity.get().getCity() + "  " + itemEntity.get().getAre() + itemEntity.get().getAddress();
    }
}
