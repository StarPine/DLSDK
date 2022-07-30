package com.dl.playfun.ui.message;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dl.playfun.app.AppConfig;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.MessageGroupEntity;
import com.dl.playfun.event.MainTabEvent;
import com.dl.playfun.event.MessageCountChangeEvent;
import com.dl.playfun.event.MessageCountChangeTagEvent;
import com.dl.playfun.event.SystemMessageCountChangeEvent;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.message.pushsetting.PushSettingFragment;
import com.dl.playfun.ui.message.systemmessagegroup.SystemMessageGroupFragment;
import com.dl.playfun.ui.message.systemmessagegroup.SystemMessageGroupItemViewModel;
import com.dl.playfun.ui.message.systemmessagegroup.SystemMessageGroupViewModel;
import com.dl.playfun.ui.mine.webview.WebViewFragment;
import com.dl.playfun.viewmodel.BaseViewModel;

import java.util.List;

import io.reactivex.disposables.Disposable;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.RxSubscriptions;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * @author wulei
 */
public class MessageMainViewModel extends BaseViewModel<AppRepository> {

    //顶部切换tab按键选中
    public ObservableField<Integer> tabSelectSystemMessage = new ObservableField<>(0);
    public SingleLiveEvent<Boolean> tabSelectEvent = new SingleLiveEvent<>();

    public ObservableField<Integer> chatMessageCount = new ObservableField<>(0);
    public ObservableField<Integer> systemMessageCount = new ObservableField<>(0);
    //推送设置按钮的点击事件
    public BindingCommand pushSettingOnClickCommand = new BindingCommand(() -> start(PushSettingFragment.class.getCanonicalName()));

    private Disposable mSubscription, MessageCountTagSubscription,mainTabEventReceive;

    public BindingCommand toTaskClickCommand = new BindingCommand(() -> {
//        AppContext.instance().logEvent(AppsFlyerEvent.im_ad_id);
//        //start(TaskCenterFragment.class.getCanonicalName());
//        RxBus.getDefault().post(new TaskMainTabEvent(false,true));
        try {
            Bundle bundle = new Bundle();
            if (ConfigManager.getInstance().isMale()) {
                bundle.putString("link", AppConfig.WEB_BASE_URL + "introduction_man");
            } else {
                bundle.putString("link", AppConfig.WEB_BASE_URL + "introduction_woman");
            }
            start(WebViewFragment.class.getCanonicalName(), bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    });

    //tab切换按键
    public BindingCommand toLeftTabClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            int flag = tabSelectSystemMessage.get();
            if (flag == 1) {
                tabSelectSystemMessage.set(0);
                tabSelectEvent.postValue(true);
            }
        }
    });
    //tab切换按键
    public BindingCommand toRightTabClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            start(SystemMessageGroupFragment.class.getCanonicalName());
        }
    });

    public MessageMainViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();
    }

    @Override
    public void registerRxBus() {
        super.registerRxBus();
        mSubscription = RxBus.getDefault().toObservable(SystemMessageCountChangeEvent.class)
                .subscribe(systemMessageCountChangeEvent -> {
                    systemMessageCount.set(systemMessageCountChangeEvent.getCount());
                    notifyMessageCountChange();
                });
        MessageCountTagSubscription = RxBus.getDefault().toObservable(MessageCountChangeTagEvent.class)
                .subscribe(messageCountChangeTagEvent -> {
                    if (messageCountChangeTagEvent.getTextCount() != null) {
                        chatMessageCount.set(messageCountChangeTagEvent.getTextCount());
                        notifyMessageCountChange();
                    }
                });
        mainTabEventReceive = RxBus.getDefault().toObservable(MainTabEvent.class).subscribe(event -> {
            if (event.getTabName().equals("message")){
                int flag = tabSelectSystemMessage.get();
                if (flag == 1) {
                    tabSelectSystemMessage.set(0);
                    tabSelectEvent.postValue(true);
                }
            }
        });

    }

    @Override
    public void removeRxBus() {
        super.removeRxBus();
        RxSubscriptions.remove(mSubscription);
        RxSubscriptions.remove(mainTabEventReceive);
    }

    public void loadDatas() {
        model.getMessageList()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<List<MessageGroupEntity>>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<List<MessageGroupEntity>> response) {
                        for (MessageGroupEntity datum : response.getData()) {
                            systemMessageCount.set(systemMessageCount.get() + datum.getUnreadNumber());
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void notifyMessageCountChange() {
        RxBus.getDefault().post(new MessageCountChangeEvent(chatMessageCount.get()));
    }

    public String addString(Integer integer) {

        String s = String.valueOf(integer);
        if (integer > 99) {
            s = "99+";
        }
        return s;
    }
}