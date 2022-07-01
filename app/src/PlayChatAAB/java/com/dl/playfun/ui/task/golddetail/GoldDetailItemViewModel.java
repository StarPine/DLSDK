package com.dl.playfun.ui.task.golddetail;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.ColorUtils;
import com.dl.playfun.R;
import com.dl.playfun.entity.GoldDetailEntity;

import org.jetbrains.annotations.NotNull;

import me.goldze.mvvmhabit.base.MultiItemViewModel;

/**
 * Author: 彭石林
 * Time: 2021/8/11 11:31
 * Description: This is GoldDetailItemViewModel
 */
public class GoldDetailItemViewModel extends MultiItemViewModel<GoldDetailViewModel> {

    public ObservableField<GoldDetailEntity> itemEntity = new ObservableField<>();

    public GoldDetailItemViewModel(@NonNull @NotNull GoldDetailViewModel viewModel, GoldDetailEntity entity) {
        super(viewModel);
        itemEntity.set(entity);
    }

    public String getMoney() {
        Integer money = itemEntity.get().getMoney();
        if (money != null && money.intValue() > 0) {
            return "+" + money;
        }
        return String.valueOf(money);
    }

    public int getMoneyColor() {
        Integer money = itemEntity.get().getMoney();
        if (money != null && money.intValue() > 0) {
            return ColorUtils.getColor(R.color.red_8);
        }
        return ColorUtils.getColor(R.color.black);
    }
}
