package com.dl.playfun.ui.radio.radiohome.item;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dl.playfun.entity.AdUserItemEntity;
import com.dl.playfun.ui.radio.radiohome.RadioViewModel;

import me.goldze.mvvmhabit.base.MultiItemViewModel;

/**
 * Author: 彭石林
 * Time: 2022/7/26 19:17
 * Description: This is RadioItemBannerVideoViewModel
 */
public class RadioItemBannerVideoViewModel extends MultiItemViewModel<RadioViewModel> {

    public ObservableField<AdUserItemEntity> adUserItemEntity = new ObservableField<>();

    public RadioItemBannerVideoViewModel(@NonNull RadioViewModel viewModel,AdUserItemEntity adUserItemEntity) {
        super(viewModel);
        this.adUserItemEntity.set(adUserItemEntity);
    }
}
