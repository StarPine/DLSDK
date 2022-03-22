package com.dl.playfun.ui.program.programsubject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.ui.program.chooseprogramsite.ChooseProgramSiteFragment;
import com.dl.playfun.utils.ExceptionReportUtils;
import com.dl.playfun.viewmodel.BaseViewModel;

import me.goldze.mvvmhabit.base.MultiItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;


/**
 * @author wulei
 */
public class ProgramChooseItemViewModel extends MultiItemViewModel<BaseViewModel> {
    public ObservableField<ConfigItemEntity> configItemEntityObservableField = new ObservableField<>();
    public BindingCommand itemClick = new BindingCommand(() -> {
        try {
            Bundle bundle = new Bundle();
            bundle.putInt("programId", configItemEntityObservableField.get().getId());
            viewModel.start(ChooseProgramSiteFragment.class.getCanonicalName(), bundle);
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });

    public ProgramChooseItemViewModel(@NonNull BaseViewModel viewModel, ConfigItemEntity configItemEntity) {
        super(viewModel);
        this.configItemEntityObservableField.set(configItemEntity);
    }

}
