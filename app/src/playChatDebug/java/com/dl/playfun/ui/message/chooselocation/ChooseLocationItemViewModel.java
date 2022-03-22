package com.dl.playfun.ui.message.chooselocation;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dl.playfun.utils.ExceptionReportUtils;

import me.goldze.mvvmhabit.base.ItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;

/**
 * @author wulei
 */
public class ChooseLocationItemViewModel extends ItemViewModel<ChooseLocationViewModel> {

    public ObservableField<String> name = new ObservableField<>();
    public ObservableField<String> address = new ObservableField<>();
    public ObservableField<Double> lat = new ObservableField<>();
    public ObservableField<Double> lng = new ObservableField<>();
    public ObservableField<Boolean> chooseed = new ObservableField<>(false);
    public BindingCommand itemClick = new BindingCommand(() -> {
        try {
            int position = viewModel.observableList.indexOf(ChooseLocationItemViewModel.this);
            viewModel.onItemClick(position);
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });

    public ChooseLocationItemViewModel(@NonNull ChooseLocationViewModel viewModel, String name, String address, Double lat, Double lng, Boolean isChoose) {
        super(viewModel);
        this.name.set(name);
        this.address.set(address);
        this.lat.set(lat);
        this.lng.set(lng);
        this.chooseed.set(isChoose);
    }

}
