package com.dl.playfun.ui.radio.programlist;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseDisposableObserver;
import com.dl.playfun.data.source.http.observer.BaseListEmptyObserver;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseListDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.BroadcastEntity;
import com.dl.playfun.entity.ThemeItemEntity;
import com.dl.playfun.event.BadioEvent;
import com.dl.playfun.event.RadioadetailEvent;
import com.dl.playfun.ui.mine.broadcast.myprogram.ProgramItemViewModel;
import com.dl.playfun.ui.radio.issuanceprogram.IssuanceProgramFragment;
import com.dl.playfun.utils.FileUploadUtils;
import com.dl.playfun.viewmodel.BaseRefreshViewModel;
import com.shuyu.gsyvideoplayer.GSYVideoManager;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.RxSubscriptions;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * @author wulei
 */
public class ProgramListViewModel extends BaseRefreshViewModel<AppRepository> {


    public ObservableField<Boolean> online = new ObservableField<>(false);
    public ObservableField<Integer> time = new ObservableField<>();

    public ObservableField<ThemeItemEntity> themeItemEntity = new ObservableField<>();//主题详情

    public Integer type = 1;
    public Integer cityId = null;
    public Integer sexId = null;
    public boolean isVip;
    public int sex;
    public int userId;
    public String avatar;
    public Integer themeId;
    public String themeName;
    public String keyword;
    public Integer certification = null;
    public UIChangeObservable uc = new UIChangeObservable();
    public ObservableList<ProgramItemViewModel> observableList = new ObservableArrayList<>();
    public ItemBinding<ProgramItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_program);
    public BindingRecyclerViewAdapter<ProgramItemViewModel> adapter = new BindingRecyclerViewAdapter<>();

    public BindingCommand back = new BindingCommand(() -> {
        pop();
    });
    //搜索按钮的点击事件
    public BindingCommand onlineOnClickCommand = new BindingCommand(() ->
    {

    });
    //发布按钮
    public BindingCommand publishOnClickCommand = new BindingCommand(() -> {
        checkTopical();
    });
    private Disposable badioEvent;
    private Disposable radioadetailEvent;

    public ProgramListViewModel(@NonNull Application application, AppRepository appRepository) {
        super(application, appRepository);
        initUserDate();
    }

    public void initUserDate() {
        isVip = model.readUserData().getIsVip() == 1;
        sex = model.readUserData().getSex();
        userId = model.readUserData().getId();
        avatar = model.readUserData().getAvatar();
        certification = model.readUserData().getCertification();
    }

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
                    loadDatas(1);
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

    public void setType(Integer type) {
        this.type = type;
        startRefresh();
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
        startRefresh();
    }

    public void setSexId(Integer sexId) {
        this.sexId = sexId;
        startRefresh();
    }

    @Override
    public void loadDatas(int page) {
        try {
            GSYVideoManager.releaseAllVideos();
        } catch (Exception e) {

        }
        model.broadcast(type, themeId, online.get() ? 1 : 0, cityId, sexId, page)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseListEmptyObserver<BaseListDataResponse<BroadcastEntity>>(this) {
                    @Override
                    public void onSuccess(BaseListDataResponse<BroadcastEntity> response) {
                        super.onSuccess(response);
                        if (page == 1) {
                            observableList.clear();
                        }
                        if (response.getData().getData() != null) {
                            for (BroadcastEntity broadcastEntity : response.getData().getData()) {
//                                节目
                                ProgramItemViewModel programItemViewModel = new ProgramItemViewModel(ProgramListViewModel.this, broadcastEntity);
                                observableList.add(programItemViewModel);
                            }
                        }
                    }

                    @Override
                    public void onComplete() {
                        stopRefreshOrLoadMore();
                    }
                });
    }

    /**
     * 获取主题详情
     *
     * @param cityId
     */
    public void getThemeDetail(Integer cityId) {
        model.getThemeDetail(cityId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(o -> {
                    showHUD();
                })
                .subscribe(new BaseObserver<BaseDataResponse<ThemeItemEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<ThemeItemEntity> themeItemEntityBaseDataResponse) {
                        ThemeItemEntity themeItem = themeItemEntityBaseDataResponse.getData();
                        themeItemEntity.set(themeItem);
                        uc.loadThemeDetailSrc.call();


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
                .subscribe(new BaseDisposableObserver<BaseResponse>() {
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
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        observableList.get(posion).topicalListEntityObservableField.get().setIsEnd(1);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //    //我要报名
    public void report(int posion, String imags) {
        model.singUp(observableList.get(posion).topicalListEntityObservableField.get().getId(), imags)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ToastUtils.showShort(R.string.sign_up_success);
                        observableList.get(posion).report();
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public void imagUpload(String filePath, int posion) {
        Observable.just(filePath)
                .doOnSubscribe(this)
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribeOn(Schedulers.io())
                .map((Function<String, String>) s -> FileUploadUtils.ossUploadFile("radio/", FileUploadUtils.FILE_TYPE_IMAGE, s))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String fileKey) {
                        report(posion, fileKey);
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissHUD();
                        ToastUtils.showShort(R.string.upload_failed);
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
                        (observableList.get(posion)).topicalListEntityObservableField.get().getBroadcast().setIsComment(
                                observableList.get(posion).topicalListEntityObservableField.get().getBroadcast().getIsComment() == 0 ? 1 : 0);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
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
                        ThemeItemEntity itemEntity = new ThemeItemEntity();
                        itemEntity.setId(themeId);
                        itemEntity.setTitle(themeName);
                        itemEntity.setKeyWord(keyword);
                        //Bundle bundle = SearchProgramSiteFragment.getStartBundle(itemEntity);
                        // start(SearchProgramSiteFragment.class.getCanonicalName(), bundle);
                        Bundle bundle = IssuanceProgramFragment.getStartBundle(itemEntity, null);
                        start(IssuanceProgramFragment.class.getCanonicalName(), bundle);
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
        public SingleLiveEvent clickCheck = new SingleLiveEvent<>();
        public SingleLiveEvent signUpSucceed = new SingleLiveEvent<>();
        public SingleLiveEvent clickImage = new SingleLiveEvent<>();
        public SingleLiveEvent loadThemeDetailSrc = new SingleLiveEvent();
    }

}
