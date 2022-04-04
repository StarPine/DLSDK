package com.dl.playfun.ui.mine.blacklist;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.entity.BlackEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.manager.PermissionManager;
import com.dl.playfun.utils.ExceptionReportUtils;
import com.dl.playfun.ui.userdetail.detail.UserDetailFragment;

import me.goldze.mvvmhabit.base.MultiItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author wulei
 */
public class BlackListItemViewModel extends MultiItemViewModel<BlacklistViewModel> {

    public ObservableField<BlackEntity> itemEntity = new ObservableField<>();

    public ObservableField<Boolean> isCancel = new ObservableField<>(false);
    //条目的点击事件
    public BindingCommand itemClick = new BindingCommand(() -> {
        try {
            if (PermissionManager.getInstance().VerifyJumpUserDetailView(itemEntity.get().getUser().getSex())) {
                Bundle bundle = UserDetailFragment.getStartBundle(itemEntity.get().getUser().getId());
                viewModel.start(UserDetailFragment.class.getCanonicalName(), bundle);
            } else {
                ToastUtils.showShort(R.string.playfun_userdetail_same_sex);
            }
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });
    public BindingCommand delBlackClick = new BindingCommand(() -> {
        try {
            int position = viewModel.observableList.indexOf(BlackListItemViewModel.this);
            if (!isCancel.get()) {
                viewModel.delBlackList(position);
            } else {
                viewModel.addBlackList(position);
            }
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });

    public BlackListItemViewModel(@NonNull BlacklistViewModel viewModel, BlackEntity itemEntity) {
        super(viewModel);
        this.itemEntity.set(itemEntity);
    }

    public int isRealManVisible() {
        if (itemEntity.get().getUser().getIsVip() != 1) {
            if (itemEntity.get().getUser().getCertification() == 1) {
                return View.VISIBLE;
            } else {
                return View.GONE;
            }
        }else {
            return View.GONE;
        }
    }

    public int isVipVisible() {
        if (itemEntity.get().getUser().getSex() != null && itemEntity.get().getUser().getSex() == 1 && itemEntity.get().getUser().getIsVip() == 1) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    public int isGoddessVisible() {
        if (itemEntity.get().getUser().getSex() == 0 && itemEntity.get().getUser().getIsVip() == 1) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }
}
