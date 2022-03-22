package com.dl.playfun.ui.radio.radiohome;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dl.playfun.entity.AdItemEntity;
import com.dl.playfun.utils.ExceptionReportUtils;

import me.goldze.mvvmhabit.base.ItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;

/**
 * @author wulei
 */
public class RadioAdItemViewModel extends ItemViewModel<RadioViewModel> {
    public ObservableField<AdItemEntity> itemEntity = new ObservableField<>();
    public BindingCommand itemClick = new BindingCommand(() -> {
        try {
            int position = viewModel.radioItems.indexOf(RadioAdItemViewModel.this);
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });

    public RadioAdItemViewModel(@NonNull RadioViewModel viewModel, AdItemEntity itemEntity) {
        super(viewModel);
        this.itemEntity.set(itemEntity);
    }
}
