package com.dl.playfun.ui.mine.task.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;

import com.dl.playfun.data.AppRepository;
import com.dl.playfun.event.TaskMainTabEvent;
import com.dl.playfun.viewmodel.BaseViewModel;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.RxSubscriptions;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * Author: 彭石林
 * Time: 2021/11/6 15:31
 * Description: This is TaskMainViewModel
 */
public class TaskMainViewModel extends BaseViewModel<AppRepository> {

    public ObservableBoolean fragmentShow = new ObservableBoolean(true);

    public UIChangeObservable uc = new UIChangeObservable();
    //消费者
    private Disposable taskMainTabEventReceive;

    public TaskMainViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
    }

    @Override
    public void registerRxBus() {
        super.registerRxBus();
        taskMainTabEventReceive = RxBus.getDefault().toObservable(TaskMainTabEvent.class)
                .compose(RxUtils.exceptionTransformer())
                .compose(RxUtils.schedulersTransformer())
                .subscribe(new Consumer() {
                    @Override
                    public void accept(Object o) throws Exception {
                        uc.taskCenterHidden.postValue(((TaskMainTabEvent) o).isTaskHiddenFlag());
                    }
                });
        //将订阅者加入管理站
        RxSubscriptions.add(taskMainTabEventReceive);
    }

    @Override
    public void removeRxBus() {
        super.removeRxBus();
        //将订阅者从管理站中移除
        RxSubscriptions.remove(taskMainTabEventReceive);
    }

    public class UIChangeObservable {
        public SingleLiveEvent<Boolean> taskCenterHidden = new SingleLiveEvent<>();
    }

}
