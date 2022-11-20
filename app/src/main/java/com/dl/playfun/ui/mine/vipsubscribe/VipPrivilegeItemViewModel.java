package com.dl.playfun.ui.mine.vipsubscribe;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dl.playfun.entity.VipPackageItemEntity;

import me.goldze.mvvmhabit.base.MultiItemViewModel;

/**
 * 特权item
 *
 * @author wulei
 */
public class VipPrivilegeItemViewModel extends MultiItemViewModel<VipSubscribeViewModel> {

    public ObservableField<VipPackageItemEntity.PrivilegesBean> itemEntity = new ObservableField<>();


    public VipPrivilegeItemViewModel(@NonNull VipSubscribeViewModel viewModel, VipPackageItemEntity.PrivilegesBean itemEntity) {
        super(viewModel);
        this.itemEntity.set(itemEntity);
    }

}
