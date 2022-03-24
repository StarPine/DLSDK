package com.dl.playfun.ui.mine.broadcast.mytrends.givelist;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dl.playfun.R;
import com.dl.playfun.entity.BaseUserBeanEntity;
import com.dl.playfun.manager.PermissionManager;
import com.dl.playfun.ui.userdetail.detail.UserDetailFragment;
import com.dl.playfun.utils.ExceptionReportUtils;
import com.dl.playfun.viewmodel.BaseViewModel;

import me.goldze.mvvmhabit.base.MultiItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.utils.ToastUtils;


/**
 * @author litchi
 */
public class GiveListItemViewModel extends MultiItemViewModel<BaseViewModel> {
    public ObservableField<BaseUserBeanEntity> baseUserBeanEntityObservableField = new ObservableField<>();
    public BindingCommand itemClick = new BindingCommand(() -> {
        try {
            if (PermissionManager.getInstance().canCheckUserDetail(baseUserBeanEntityObservableField.get().getSex())) {
                Bundle bundle = UserDetailFragment.getStartBundle(baseUserBeanEntityObservableField.get().getId());
                viewModel.start(UserDetailFragment.class.getCanonicalName(), bundle);
            } else {
                if (baseUserBeanEntityObservableField.get().getSex() == 1) {
                    ToastUtils.showShort(R.string.man_ont_check_man_detail);
                } else {
                    ToastUtils.showShort(R.string.madam_ont_check_madam_detail);
                }
            }
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });

    public GiveListItemViewModel(@NonNull BaseViewModel viewModel, BaseUserBeanEntity baseUserBeanEntity) {
        super(viewModel);
        this.baseUserBeanEntityObservableField.set(baseUserBeanEntity);
    }
}