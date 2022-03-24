package com.dl.playfun.ui.userdetail.remark;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.UserRemarkEntity;
import com.dl.playfun.event.UserRemarkChangeEvent;
import com.dl.playfun.viewmodel.BaseViewModel;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * @author wulei
 */
public class RemarkUserViewModel extends BaseViewModel<AppRepository> {

    public ObservableField<Integer> userId = new ObservableField<>();
    public ObservableField<UserRemarkEntity> detailEntity = new ObservableField<>(new UserRemarkEntity());
    //完成按钮的点击事件
    public BindingCommand confirmOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            updateRemark();
        }
    });

    public RemarkUserViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        loadRemark();
    }

    private void loadRemark() {
        model.getUserRemark(userId.get())
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<UserRemarkEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<UserRemarkEntity> response) {
                        if (response.getData() != null) {
                            detailEntity.set(response.getData());
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    private void updateRemark() {
        model.userRemark(userId.get(), detailEntity.get().getNickname(), detailEntity.get().getDesc())
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        RxBus.getDefault().post(new UserRemarkChangeEvent(userId.get(), detailEntity.get().getNickname()));
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                        pop();
                    }
                });
    }
}