package com.dl.playfun.ui.message.contact;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.entity.FrequentContactEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.userdetail.detail.UserDetailFragment;
import com.dl.playfun.utils.ChatUtils;
import com.dl.playfun.utils.ExceptionReportUtils;
import com.dl.playfun.utils.SystemDictUtils;

import java.util.Objects;

import me.goldze.mvvmhabit.base.MultiItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;

/**
 * Author: 彭石林
 * Time: 2022/10/17 18:22
 * Description: This is ItemOftenContactViewModel
 */
public class ItemOftenContactViewModel extends MultiItemViewModel<OftenContactViewModel> {

    public ObservableField<FrequentContactEntity.ItemEntity> itemEntity = new ObservableField<>();

    //单次搭讪成功
    public ObservableField<Boolean> accountCollect = new ObservableField<>();


    public ItemOftenContactViewModel(@NonNull OftenContactViewModel viewModel,FrequentContactEntity.ItemEntity itemEntity) {
        super(viewModel);
        this.itemEntity.set(itemEntity);
    }

    //条目的点击事件
    public final BindingCommand<Void> itemClick = new BindingCommand<>(() -> {
        try {
            int position = viewModel.observableList.indexOf(ItemOftenContactViewModel.this);
            Bundle bundle = UserDetailFragment.getStartBundle(itemEntity.get().getUserProfile().getId());
            bundle.putInt(UserDetailFragment.ARG_USER_DETAIL_POSITION,position);
            viewModel.start(UserDetailFragment.class.getCanonicalName(), bundle);
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });

    //搭讪 or  聊天
    public BindingCommand accostOnClickCommand = new BindingCommand(() -> {
        try {
            //拿到position
            if (itemEntity.get().getIsAccost() == 1) {
                ChatUtils.chatUser(itemEntity.get().getUserProfile().getImId(), itemEntity.get().getUserProfile().getId(), itemEntity.get().getUserProfile().getNickname(), viewModel);
                AppContext.instance().logEvent(AppsFlyerEvent.homepage_chat);
            } else {
                try {
                    //男女点击搭讪
                    AppContext.instance().logEvent(ConfigManager.getInstance().isMale() ? AppsFlyerEvent.greet_male : AppsFlyerEvent.greet_female);
                }catch (Exception ignored){

                }
                int position = viewModel.observableList.indexOf(ItemOftenContactViewModel.this);
                viewModel.putAccostFirst(position);
                AppContext.instance().logEvent(AppsFlyerEvent.homepage_accost);
            }

        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });

    public int isRealManVisible() {
        if (itemEntity.get().getUserProfile().getIsVip() != 1) {
            if (itemEntity.get().getUserProfile().getCertification() == 1) {
                return View.VISIBLE;
            } else {
                return View.GONE;
            }
        }else {
            return View.GONE;
        }
    }

    public int isVipVisible() {
        if (itemEntity.get().getUserProfile().getSex() == 1 && itemEntity.get().getUserProfile().getIsVip() == 1) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    public int isGoddessVisible() {
        if (itemEntity.get().getUserProfile().getSex() == 0 && itemEntity.get().getUserProfile().getIsVip() == 1) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    public String getAgeAndConstellation() {
        return String.format(StringUtils.getString(R.string.playfun_mine_age), itemEntity.get().getUserProfile().getAge());
    }

    public boolean isEmpty(String obj) {
        return obj == null || obj.length() == 0 || obj.equals("null");
    }

    //获取工作植页
    public String getOccupationByIdOnNull(){
        if(itemEntity.get()==null || Objects.requireNonNull(itemEntity.get().getUserProfile()).getOccupationId()==null){
            return null;
        }
        int occupationId = Objects.requireNonNull(itemEntity.get().getUserProfile()).getOccupationId();
        return SystemDictUtils.getOccupationByIdOnNull(occupationId);
    }

    public String getDistance() {
        String distance = StringUtils.getString(R.string.playfun_unknown);
        Integer d = itemEntity.get().getDistance();
        if (d != null) {
            if (d == -1) {
                distance = StringUtils.getString(R.string.playfun_unknown);
            } else if (d == -2) {
                distance = StringUtils.getString(R.string.playfun_keep);
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

    public Integer getDistanceShow() {
        Integer distance = View.GONE;
        Integer d = itemEntity.get().getDistance();
        if (d != null) {
            if (d == -1) {
                distance = View.GONE;
            } else {
                distance = View.VISIBLE;
            }
        }
        String onlineStatus = getOnlineStatus();
        if (onlineStatus == null){
            distance = View.GONE;
        }
        return distance;
    }

    public String getOnlineStatus() {
        String onlineStatus = null;
        if (itemEntity.get().getUserProfile().getCallingStatus() == 0){
            if (itemEntity.get().getUserProfile().getIsOnline() == -1) {
                onlineStatus = null;
            } else if (itemEntity.get().getUserProfile().getIsOnline() == 1) {
                onlineStatus = StringUtils.getString(R.string.playfun_on_line);
            } else if (itemEntity.get().getUserProfile().getIsOnline() == 0) {
                onlineStatus = null;
            }
        }else if (itemEntity.get().getUserProfile().getCallingStatus() == 1){
            onlineStatus = StringUtils.getString(R.string.playfun_calling);
        }else if (itemEntity.get().getUserProfile().getCallingStatus() == 2){
            onlineStatus = StringUtils.getString(R.string.playfun_in_video);
        }

        return onlineStatus;
    }

    public int onLineColor(FrequentContactEntity.ItemEntity itemEntity){
        if (itemEntity == null)return -1;
        if (itemEntity.getUserProfile().getCallingStatus() == 0){
            if (itemEntity.getUserProfile().getIsOnline() == 1) {
                return AppContext.instance().getResources().getColor(R.color.green2);
            }
        }else {
            return AppContext.instance().getResources().getColor(R.color.red_9);
        }
        return AppContext.instance().getResources().getColor(R.color.text_9EA1B0);
    }

}
