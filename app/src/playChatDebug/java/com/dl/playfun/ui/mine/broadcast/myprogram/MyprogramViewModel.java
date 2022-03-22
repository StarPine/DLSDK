package com.dl.playfun.ui.mine.broadcast.myprogram;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseDisposableObserver;
import com.dl.playfun.data.source.http.observer.BaseListEmptyObserver;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseListDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.TopicalListEntity;
import com.dl.playfun.event.BadioEvent;
import com.dl.playfun.event.RadioadetailEvent;
import com.dl.playfun.ui.radio.issuanceprogram.IssuanceProgramFragment;
import com.dl.playfun.viewmodel.BaseRefreshViewModel;

import io.reactivex.disposables.Disposable;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.RxSubscriptions;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

public class MyprogramViewModel extends BaseRefreshViewModel<AppRepository> {
    public boolean isVip;
    public Integer sex;
    public int userId;
    public String avatar;
    public Integer certification = null;
    public UIChangeObservable uc = new UIChangeObservable();
    public BindingCommand issuanceCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            checkTopical();
        }
    });
    public BindingRecyclerViewAdapter<ProgramItemViewModel> adapter = new BindingRecyclerViewAdapter<>();
    public ObservableList<ProgramItemViewModel> observableList = new ObservableArrayList<>();
    public ItemBinding<ProgramItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_program);
    private Disposable badioEvent;
    private Disposable radioadetailEvent;

    public MyprogramViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
        isVip = model.readUserData().getIsVip() == 1;
        sex = model.readUserData().getSex();
        userId = model.readUserData().getId();
        avatar = model.readUserData().getAvatar();
        certification = model.readUserData().getCertification();

        stateModel.setEmptyRetryCommand(StringUtils.getString(R.string.my_program_no_show), null, null);
    }

    @Override
    public void loadDatas(int page) {
        getTopicalList();
    }

    //跳转发布界面
    public BindingCommand toProgramVIew = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            start(IssuanceProgramFragment.class.getCanonicalName());
        }
    });

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        startRefresh();
    }

    @Override
    public void registerRxBus() {
        super.registerRxBus();
        badioEvent = RxBus.getDefault().toObservable(BadioEvent.class)
                .subscribe(event -> {
                    currentPage = 1;
                    getTopicalList();
                });
        radioadetailEvent = RxBus.getDefault().toObservable(RadioadetailEvent.class)
                .subscribe(event -> {
                    for (int i = 0; i < observableList.size(); i++) {
                        if (observableList.get(i).topicalListEntityObservableField.get().getId() == event.getId()) {
                            switch (event.getType()) {//1:删除 2：评论关闭开启 3：报名成功 4：节目结束报名 5：评论  6：点赞
                                case 1:
                                    observableList.remove(i);
                                    break;
                                case 2:
                                    observableList.get(i).topicalListEntityObservableField.get().getBroadcast().setIsComment(event.isComment);
                                    break;
                                case 3:
                                    observableList.get(i).report();
                                    break;
                                case 4:
                                    observableList.get(i).topicalListEntityObservableField.get().setIsEnd(1);
                                    break;
                                case 5:
                                    observableList.get(i).addComment(event.getId(), event.content, event.toUserId, event.toUserName, model.readUserData().getNickname());
                                    break;
                                case 6:
                                    observableList.get(i).addGiveUser();
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                });
    }

    @Override
    public void removeRxBus() {
        super.removeRxBus();
        RxSubscriptions.remove(badioEvent);
        RxSubscriptions.remove(radioadetailEvent);
    }

    public void checkTopical() {
        model.checkTopical()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        uc.clickPublish.call();
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //获节目列表
    private void getTopicalList() {
        model.getTopicalList(null, currentPage)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseListEmptyObserver<BaseListDataResponse<TopicalListEntity>>(this) {
                    @Override
                    public void onSuccess(BaseListDataResponse<TopicalListEntity> response) {
                        super.onSuccess(response);
                        stateModel.setEmptyBroadcastCommand(StringUtils.getString(R.string.my_all_broadcast_empty), R.drawable.my_all_broadcast_empty_img, R.color.all_broadcast_empty,StringUtils.getString(R.string.task_fragment_task_new10),toProgramVIew);
                        if (currentPage == 1) {
                            observableList.clear();
                        }
                        if (response.getData().getData() != null) {
                            for (int i = 0; i < response.getData().getData().size(); i++) {
                                ProgramItemViewModel item = new ProgramItemViewModel(MyprogramViewModel.this, response.getData().getData().get(i));
                                observableList.add(item);
                            }
                        }
                    }

                    @Override
                    public void onComplete() {
                        stopRefreshOrLoadMore();
                    }
                });
    }

    //删除节目
    public void deleteTopical(int posion) {
        model.deleteTopical(observableList.get(posion).topicalListEntityObservableField.get().getId())
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        observableList.remove(posion);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //开启/关闭评论
    public void setComment(int posion) {
        model.setComment(observableList.get(posion).topicalListEntityObservableField.get().getBroadcast().getId(),
                observableList.get(posion).topicalListEntityObservableField.get().getBroadcast().getIsComment() == 0 ? 1 : 0)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ToastUtils.showShort(observableList.get(posion).topicalListEntityObservableField.get().getBroadcast().getIsComment() == 1 ? StringUtils.getString(R.string.open_comment_success) : StringUtils.getString(R.string.close_success));
                        observableList.get(posion).topicalListEntityObservableField.get().getBroadcast().setIsComment(
                                observableList.get(posion).topicalListEntityObservableField.get().getBroadcast().getIsComment() == 0 ? 1 : 0);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //节目点赞
    public void topicalGive(int posion) {
        model.TopicalGive(observableList.get(posion).topicalListEntityObservableField.get().getId())
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ToastUtils.showShort(R.string.give_success);
                        observableList.get(posion).addGiveUser();
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //节目评论
    public void topicalComment(Integer id, String content, Integer toUserId, String toUserName) {
        model.topicalComment(id, content, toUserId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseDisposableObserver() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        ToastUtils.showShort(R.string.comment_success);
                        for (int i = 0; i < observableList.size(); i++) {
                            if (id == observableList.get(i).topicalListEntityObservableField.get().getId()) {
                                observableList.get(i).addComment(id, content, toUserId, toUserName, model.readUserData().getNickname());
                            }
                        }
                    }

                    @Override
                    public void onError(RequestException e) {
                        if (e.getCode() == 10016) {
                            ToastUtils.showShort(StringUtils.getString(R.string.comment_close));
                            for (int i = 0; i < observableList.size(); i++) {
                                if (id == observableList.get(i).topicalListEntityObservableField.get().getId()) {
                                    observableList.get(i).topicalListEntityObservableField.get().getBroadcast().setIsComment(1);
                                }
                            }
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //节目结束报名
    public void topicalFinish(int posion) {
        model.TopicalFinish(observableList.get(posion).topicalListEntityObservableField.get().getId())
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver() {
                    @Override
                    public void onSuccess(BaseResponse response) {
//                        ToastUtils.showShort("评论成功");
                        observableList.get(posion).topicalListEntityObservableField.get().setIsEnd(1);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public class UIChangeObservable {
        public SingleLiveEvent clickMore = new SingleLiveEvent<>();
        public SingleLiveEvent clickLike = new SingleLiveEvent<>();
        public SingleLiveEvent clickComment = new SingleLiveEvent<>();
        public SingleLiveEvent clickSignUp = new SingleLiveEvent<>();
        public SingleLiveEvent clickImage = new SingleLiveEvent<>();
        public SingleLiveEvent<Void> clickPublish = new SingleLiveEvent<>();
//        public SingleLiveEvent signUpSucceed = new SingleLiveEvent<>();
    }
}
