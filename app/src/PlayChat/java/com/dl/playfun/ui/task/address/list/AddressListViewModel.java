package com.dl.playfun.ui.task.address.list;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseListEmptyObserver;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseListDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.AddressEntity;
import com.dl.playfun.ui.task.address.AddressEditFragment;
import com.dl.playfun.viewmodel.BaseViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * Author: 彭石林
 * Time: 2021/8/14 0:19
 * Description: This is AddressListViewModel
 */
public class AddressListViewModel extends BaseViewModel<AppRepository> {

    //刷新
    public int currentPage = 1;

    public BindingRecyclerViewAdapter<AddressItemViewModel> adapter = new BindingRecyclerViewAdapter<>();
    public ObservableList<AddressItemViewModel> observableList = new ObservableArrayList<>();
    public ItemBinding<AddressItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.fragment_address_item);


    public UIChangeObservable uc = new UIChangeObservable();
    public BindingCommand toCreateAddress = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            start(AddressEditFragment.class.getCanonicalName());
        }
    });
    //下拉刷新
    public BindingCommand onRefreshCommand = new BindingCommand(() -> {
        currentPage = 1;
        loadDatas(currentPage);
    });
    public BindingCommand onLoadMoreCommand = new BindingCommand(() -> nextPage());

    public AddressListViewModel(@NonNull @NotNull Application application, AppRepository model) {
        super(application, model);
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        loadDatas(1);
    }

    protected void nextPage() {
        currentPage++;
        loadDatas(currentPage);
    }

    /**
     * 停止下拉刷新或加载更多动画
     */
    protected void stopRefreshOrLoadMore() {
        if (currentPage == 1) {
            uc.finishRefreshing.call();
        } else {
            uc.finishLoadmore.call();
        }
    }

    public void loadDatas(int page) {
        getAddressList(page);
    }

    //获取地址列表
    public void getAddressList(int page) {
        model.getAddressList(page)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseListEmptyObserver<BaseListDataResponse<AddressEntity>>(this) {
                    @Override
                    public void onSuccess(BaseListDataResponse<AddressEntity> baseListData) {
                        super.onSuccess(baseListData);
                        if (currentPage == 1) {
                            observableList.clear();
                        }
                        for (AddressEntity itemEntity : baseListData.getData().getData()) {
                            AddressItemViewModel model = new AddressItemViewModel(AddressListViewModel.this, itemEntity);
                            observableList.add(model);
                        }

                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                        stopRefreshOrLoadMore();
                    }
                });
    }

    //删除指定地址
    public void removeAddress(Integer id, int index) {
        model.removeAddress(id)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {
                        observableList.remove(index);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //修改收获地址
    public  void updateAddress(AddressEntity entity) {
        model.updateAddress(entity.getId(), entity.getContacts(), entity.getCity(), entity.getAre(), entity.getAddress(), entity.getPhone(), entity.getIsDefault())
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public class UIChangeObservable {
        //下拉刷新开始
        public SingleLiveEvent<Void> startRefreshing = new SingleLiveEvent<>();
        //下拉刷新完成
        public SingleLiveEvent<Void> finishRefreshing = new SingleLiveEvent<>();
        //上拉加载完成
        public SingleLiveEvent<Void> finishLoadmore = new SingleLiveEvent<>();

        //删除地址
        public SingleLiveEvent<Map<String, Integer>> alertDelteAddress = new SingleLiveEvent<>();
    }

}
