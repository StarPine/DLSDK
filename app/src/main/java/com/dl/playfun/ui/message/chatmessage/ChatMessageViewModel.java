package com.dl.playfun.ui.message.chatmessage;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.BrowseNumberEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.ui.mine.trace.man.TraceManFragment;
import com.dl.playfun.viewmodel.BaseViewModel;

import java.util.List;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * @author wulei
 */
public class ChatMessageViewModel extends BaseViewModel<AppRepository> {

    UIChangeObservable uc = new UIChangeObservable();

    public ObservableField<String> NewNumberText = new ObservableField<String>();
    //跳转谁看过我
    public BindingCommand traceOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            AppContext.instance().logEvent(AppsFlyerEvent.chat_seen_me);
            Bundle bundle = new Bundle();
            bundle.putInt("userId", model.readUserData().getId());
            start(TraceManFragment.class.getCanonicalName(), bundle);
            NewNumberText.set(null);
        }
    });

    public ChatMessageViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
        getLocalUserData();
    }
    //请求谁看过我、粉丝间隔时间
    private Long intervalTime = null;

    //获取当前用户数据
    public void getLocalUserData() {
        uc.localUserDataEntity.postValue(model.readUserData());
    }

    public void newsBrowseNumber() {
        long dayTime = System.currentTimeMillis();
        if (intervalTime != null && (dayTime / 1000) - intervalTime.longValue() <= 2) {
            return;
        }
        if (intervalTime == null) {
            intervalTime = dayTime / 1000;
        }
        model.newsBrowseNumber()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<BrowseNumberEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<BrowseNumberEntity> browseNumberEntity) {
                        uc.loadBrowseNumber.setValue(browseNumberEntity.getData());
                    }
                });
    }

    public class UIChangeObservable {
        public SingleLiveEvent<List<Integer>> askUseChatNumber = new SingleLiveEvent<>();
        public SingleLiveEvent<Integer> useChatNumberSuccess = new SingleLiveEvent<>();
        //查询本机用户资料
        public SingleLiveEvent<UserDataEntity> localUserDataEntity = new SingleLiveEvent<>();
        public SingleLiveEvent<BrowseNumberEntity> loadBrowseNumber = new SingleLiveEvent<>();
    }

}
