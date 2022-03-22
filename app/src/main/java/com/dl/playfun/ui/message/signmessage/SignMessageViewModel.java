package com.dl.playfun.ui.message.signmessage;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseListEmptyObserver;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseListDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.entity.SignMessageEntity;
import com.dl.playfun.viewmodel.BaseRefreshViewModel;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.ui.program.programdetail.ProgramDetailFragment;

import java.util.List;

import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * @author wulei
 */
public class SignMessageViewModel extends BaseRefreshViewModel<AppRepository> {

    private final List<ConfigItemEntity> programTimeConfigs;

    public BindingRecyclerViewAdapter<SignMessageItemViewModel> adapter = new BindingRecyclerViewAdapter<>();
    public ObservableList<SignMessageItemViewModel> observableList = new ObservableArrayList<>();
    public ItemBinding<SignMessageItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_sign_message);
    UIChangeObservable uc = new UIChangeObservable();

    public SignMessageViewModel(@NonNull Application application, AppRepository appRepository) {
        super(application, appRepository);
        programTimeConfigs = model.readProgramTimeConfig();
    }

    @Override
    public void onEnterAnimationEnd() {
        super.onEnterAnimationEnd();
        startRefresh();
    }

    public String getProgramTimeConfigNameById(int id) {
        ConfigItemEntity configItemEntity = null;
        for (ConfigItemEntity programTimeConfig : programTimeConfigs) {
            if (programTimeConfig.getId().intValue() == id) {
                configItemEntity = programTimeConfig;
                break;
            }
        }
        if (configItemEntity != null) {
            return configItemEntity.getName();
        }
        return String.valueOf(id);
    }

    public void itemClick(int position) {
        Bundle bundle = ProgramDetailFragment.getStartBundle(observableList.get(position).itemEntity.get().getTopicalId());
        start(ProgramDetailFragment.class.getCanonicalName(), bundle);

    }

    @Override
    public void loadDatas(int page) {
        model.getMessageSign(page)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .subscribe(new BaseListEmptyObserver<BaseListDataResponse<SignMessageEntity>>(this) {
                    @Override
                    public void onSuccess(BaseListDataResponse<SignMessageEntity> response) {
                        super.onSuccess(response);
                        if (page == 1) {
                            observableList.clear();
                        }
                        List<SignMessageEntity> list = response.getData().getData();
                        for (SignMessageEntity entity : list) {
                            SignMessageItemViewModel item = new SignMessageItemViewModel(SignMessageViewModel.this, entity);
                            observableList.add(item);
                        }
                    }

                    @Override
                    public void onComplete() {
                        stopRefreshOrLoadMore();
                    }
                });
    }

    public void deleteMessage(int position) {
        model.deleteMessage("sign", observableList.get(position).itemEntity.get().getId())
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        observableList.remove(position);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public class UIChangeObservable {
        public SingleLiveEvent<Integer> clickDelete = new SingleLiveEvent<>();
    }


}
