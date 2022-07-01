package com.dl.playfun.ui.task.record;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.ui.message.coinredpackagedetail.CoinRedPackageDetailViewModel;
import com.dl.playfun.ui.mine.webdetail.WebDetailFragment;
import com.dl.playfun.utils.ChatUtils;
import com.dl.playfun.utils.ExceptionReportUtils;
import com.dl.playfun.viewmodel.BaseViewModel;

import org.jetbrains.annotations.NotNull;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;

/**
 * Author: 彭石林
 * Time: 2021/8/12 11:09
 * Description: 兑换记录处理层
 */
public class TaskExchangeRecordViewModel extends BaseViewModel<AppRepository> {

    //跳转帮助界面
    public BindingCommand helpOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            Bundle bundle = WebDetailFragment.getStartBundle(AppConfig.ExchangeRecord_URL);
            start(WebDetailFragment.class.getCanonicalName(), bundle);
        }
    });
    //咨询客服
    public BindingCommand toUseAdminMessage = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            try {
                ChatUtils.chatUser(AppConfig.CHAT_SERVICE_USER_ID_SEND,0, StringUtils.getString(R.string.playfun_chat_service_name), TaskExchangeRecordViewModel.this);
            } catch (Exception e) {
                ExceptionReportUtils.report(e);
            }
        }
    });

    public TaskExchangeRecordViewModel(@NonNull @NotNull Application application, AppRepository model) {
        super(application, model);
    }
}
