package com.dl.playfun.ui.radio.radiohome;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dl.playfun.R;
import com.dl.playfun.entity.ThemeItemEntity;
import com.dl.playfun.ui.radio.issuanceprogram.IssuanceProgramViewModel;
import com.dl.playfun.utils.ExceptionReportUtils;

import me.goldze.mvvmhabit.base.ItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author wulei
 */
public class RadioThemeItemViewModel extends ItemViewModel<IssuanceProgramViewModel> {
    public ObservableField<ThemeItemEntity> itemEntity = new ObservableField<>();
    //条目的点击事件
    public BindingCommand itemClick = new BindingCommand(() -> {
        try {
            //不能发布约会。取消事件继续执行
            if(!viewModel.isPlaying){
                ToastUtils.showShort(R.string.issuance_text_error);
                return;
            }
            for (int i = 0; i < viewModel.themeItems.size(); i++) {
                RadioThemeItemViewModel radioThemeItemEntity = viewModel.themeItems.get(i);
                int oldId = radioThemeItemEntity.itemEntity.get().getId().intValue();
                int newId = itemEntity.get().getId().intValue();
                if (oldId == newId) {
                    viewModel.themeItems.get(i).itemEntity.get().setSelect(true);
                    viewModel.OnClickTheme(itemEntity.get());
                } else {
                    viewModel.themeItems.get(i).itemEntity.get().setSelect(false);
                }
                viewModel.themeAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });

    public RadioThemeItemViewModel(@NonNull IssuanceProgramViewModel viewModel, ThemeItemEntity itemEntity) {
        super(viewModel);
        this.itemEntity.set(itemEntity);
    }
}
