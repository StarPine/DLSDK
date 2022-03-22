package com.dl.playfun.ui.viewmodel;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.entity.ParkItemEntity;
import com.dl.playfun.manager.PermissionManager;
import com.dl.playfun.ui.userdetail.detail.UserDetailFragment;
import com.dl.playfun.utils.ChatUtils;
import com.dl.playfun.utils.ExceptionReportUtils;
import com.dl.playfun.utils.TimeUtils;

import me.goldze.mvvmhabit.base.MultiItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author wulei
 */
public class BaseParkItemViewModel extends MultiItemViewModel<BaseParkViewModel> {
    public ObservableField<Boolean> collectEnable = new ObservableField<>();
    public ObservableField<ParkItemEntity> itemEntity = new ObservableField<>();
    //条目的点击事件
    public BindingCommand itemClick = new BindingCommand(() -> {
        try {
            if (PermissionManager.getInstance().canCheckUserDetail(itemEntity.get().getSex())) {
                AppContext.instance().logEvent(AppsFlyerEvent.Nearby_Follow);
                Bundle bundle = UserDetailFragment.getStartBundle(itemEntity.get().getId());
                viewModel.start(UserDetailFragment.class.getCanonicalName(), bundle);
            } else {
                if (itemEntity.get().getSex() == 1) {
                    ToastUtils.showShort(R.string.men_cannot_men_detail);
                } else {
                    ToastUtils.showShort(R.string.lady_cannot_lady_detail);
                }
            }
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });
    public BindingCommand accostOnClickCommand = new BindingCommand(() -> {
        try {
            //拿到position
            if (itemEntity.get().getIsAccost() == 1) {
                ChatUtils.chatUser(itemEntity.get().getId(), itemEntity.get().getNickname(), viewModel);
                AppContext.instance().logEvent(AppsFlyerEvent.homepage_chat);
            } else {
                int position = viewModel.observableList.indexOf(BaseParkItemViewModel.this);
                viewModel.putAccostFirst(position);
                AppContext.instance().logEvent(AppsFlyerEvent.homepage_accost);
            }

        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });

    public BaseParkItemViewModel(@NonNull BaseParkViewModel viewModel, int sex, ParkItemEntity itemEntity) {
        super(viewModel);
        this.collectEnable.set(itemEntity.getSex() != sex);
        this.itemEntity.set(itemEntity);
    }

    public String getDistance() {
        String distance = StringUtils.getString(R.string.unknown);
        Double d = itemEntity.get().getDistance();
        if (d != null) {
            if (d == -1) {
                distance = StringUtils.getString(R.string.unknown);
            } else if (d == -2) {
                distance = StringUtils.getString(R.string.keep);
            } else {
                if (d > 1000) {
                    double df = d / 1000;
                    if (df > 999) {
                        distance = String.format(">%.0fkm", df);
                    } else {
                        distance = String.format("%.1fkm", df);
                    }
                } else {
                    distance = String.format("%sm", d.intValue());
                }
            }
        }
        return distance;
    }

    public String getOnlineStatus() {
        String onlineStatus = StringUtils.getString(R.string.unknown);
        if (itemEntity.get().getCallingStatus() == 0){
            if (itemEntity.get().getIsOnline() == -1) {
                onlineStatus = StringUtils.getString(R.string.keep);
            } else if (itemEntity.get().getIsOnline() == 1) {
                onlineStatus = StringUtils.getString(R.string.on_line);
            } else if (itemEntity.get().getIsOnline() == 0) {
                if (StringUtils.isEmpty(itemEntity.get().getOfflineTime())) {
                    onlineStatus = StringUtils.getString(R.string.unknown);
                } else {
                    onlineStatus = TimeUtils.getFriendlyTimeSpan(itemEntity.get().getOfflineTime());
                }
            }
        }else if (itemEntity.get().getCallingStatus() == 1){
            onlineStatus = StringUtils.getString(R.string.calling);
        }else if (itemEntity.get().getCallingStatus() == 2){
            onlineStatus = StringUtils.getString(R.string.in_video);
        }

        return onlineStatus;
    }

    public int onLineColor(ParkItemEntity itemEntity){
        if (itemEntity == null)return -1;
        if (itemEntity.getCallingStatus() == 0){
            if (itemEntity.getIsOnline() == 1) {
                return AppContext.instance().getResources().getColor(R.color.green2);
            }
        }else {
            return AppContext.instance().getResources().getColor(R.color.red_9);
        }
        return AppContext.instance().getResources().getColor(R.color.text_9EA1B0);
    }

    public int isRealManVisible() {
        if (itemEntity.get().getIsVip() == 1) {
            if (itemEntity.get().getSex() == 1) {
                return View.VISIBLE;
            } else {
                return View.GONE;
            }
        }
        if (itemEntity.get().getCertification() == 1) {
            return View.VISIBLE;
        }
        return View.GONE;
    }

    public int isVipVisible() {
        if (itemEntity.get().getSex() == 1 && itemEntity.get().getIsVip() == 1) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    public int isGoddessVisible() {
        if (itemEntity.get().getSex() == 0 && itemEntity.get().getIsVip() == 1) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    public String getAgeAndConstellation() {
        return String.format(StringUtils.getString(R.string.age_and_constellation), itemEntity.get().getAge(), itemEntity.get().getConstellation());
    }

    public int isPaidAlbum() {
        if (itemEntity.get().getAlbumType() == 2) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    public int isAuthAlbum() {
        if (itemEntity.get().getAlbumType() == 3) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

}
