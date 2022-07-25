package com.dl.playfun.ui.home;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dl.playfun.entity.AdItemEntity;

import me.goldze.mvvmhabit.base.MultiItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;

/**
 * Author: 彭石林
 * Time: 2022/7/25 14:41
 * Description: 首页广告banner图
 */
public class HomeMainBannerItemViewModel extends MultiItemViewModel<HomeMainViewModel> {

    public ObservableField<AdItemEntity> itemEntity = new ObservableField<>();

    public HomeMainBannerItemViewModel(@NonNull HomeMainViewModel viewModel,AdItemEntity adItemEntity) {
        super(viewModel);
        this.itemEntity.set(adItemEntity);
    }

    public BindingCommand clickBanner = new BindingCommand(() -> {

    });
}
