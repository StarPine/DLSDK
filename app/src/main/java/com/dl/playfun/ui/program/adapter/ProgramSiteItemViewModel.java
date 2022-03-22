package com.dl.playfun.ui.program.adapter;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dl.playfun.utils.ExceptionReportUtils;
import com.dl.playfun.ui.program.base.BaseProgramSiteViewModel;

import me.goldze.mvvmhabit.base.ItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;

/**
 * @author wulei
 */
public class ProgramSiteItemViewModel extends ItemViewModel<BaseProgramSiteViewModel> {

    public ObservableField<String> name = new ObservableField<>();
    public ObservableField<String> address = new ObservableField<>();
    public ObservableField<Double> lat = new ObservableField<>();
    public ObservableField<Double> lng = new ObservableField<>();
    //条目的点击事件
    public BindingCommand itemClick = new BindingCommand(() -> {
        try {
            int position = viewModel.observableList.indexOf(ProgramSiteItemViewModel.this);
            viewModel.onItemClick(position);
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });

    public ProgramSiteItemViewModel(@NonNull BaseProgramSiteViewModel viewModel, String name, String address, Double lat, Double lng) {
        super(viewModel);
        this.name.set(name);
        this.address.set(address);
        this.lat.set(lat);
        this.lng.set(lng);
    }

}
