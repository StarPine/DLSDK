package com.dl.playfun.ui.radio.publishdynamic;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseDisposableObserver;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.StatusEntity;
import com.dl.playfun.event.BadioEvent;
import com.dl.playfun.event.UMengCustomEvent;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.utils.FileUploadUtils;
import com.dl.playfun.viewmodel.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.binding.command.BindingConsumer;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author wulei
 */
public class PublishDynamicViewModel extends BaseViewModel<AppRepository> {
    public ObservableField<String> content = new ObservableField<>();
    public ObservableField<Integer> is_comment = new ObservableField<>(0);
    public ObservableField<Integer> is_hide = new ObservableField<>(0);
    public List<String> images = new ArrayList<>();
    public List<String> filePaths = new ArrayList<>();
    public int sex;
    public ConfigManager configManager;
    public BindingCommand<Boolean> isConnectionChangeCommand = new BindingCommand<>(new BindingConsumer<Boolean>() {
        @Override
        public void call(Boolean isChecked) {
            is_comment.set(isChecked ? 1 : 0);
        }
    });
    public BindingCommand<Boolean> isHideChangeCommand = new BindingCommand<>(new BindingConsumer<Boolean>() {
        @Override
        public void call(Boolean isChecked) {
            is_hide.set(isChecked ? 1 : 0);
        }
    });
    UIChangeObservable uc = new UIChangeObservable();
    //发布
    public BindingCommand issuanceClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            hideKeyboard();
            if (StringUtils.isEmpty(content.get()) && filePaths.isEmpty()) {
                ToastUtils.showShort(R.string.dynamic_empty_warn);
                return;
            }
            publishCheck();
            RxBus.getDefault().post(new UMengCustomEvent(UMengCustomEvent.EVENT_BROADCAST_DYNAMIC_PUBLISH));
        }
    });

    public PublishDynamicViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
        sex = model.readUserData().getSex();
        configManager = ConfigManager.getInstance();
    }

    private void publishCheck() {
        model.publishCheck(1)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<StatusEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<StatusEntity> response) {
                        dismissHUD();
                        if (response.getData().getStatus() == 1) {
                            sendConfirm();
                        } else {
                            if (model.readUserData().getSex() == 1) {
                                if (model.readUserData().getIsVip() != 1) {
                                    uc.clickNotVip.call();
                                    return;
                                }
                            }
//                            else {
//                                if (model.readUserData().getCertification() != 1) {
//                                    uc.clickNotVip.call();
//                                    return;
//                                }
//                            }
                            //女士都能發動態
                            sendConfirm();
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public void sendConfirm() {
        if (filePaths != null && filePaths.size() > 0) {
            for (int i = 0; i < filePaths.size(); i++) {
                uploadAvatar(filePaths.get(i));
            }
        } else {
            newsCreate();
        }
    }

    public void setFilePaths(List<String> filePaths) {
        this.filePaths = filePaths;
    }

    public void delFilePaths(String filePath) {
        if (filePaths != null) {
            for (int i = 0; i < filePaths.size(); i++) {
                if (filePaths.get(i).equals(filePath)) {
                    filePaths.remove(i);
                }
            }
        }
    }

    public void uploadAvatar(String filePath) {
        Observable.just(filePath)
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribeOn(Schedulers.io())
                .map(new Function<String, String>() {
                    @Override
                    public String apply(String s) throws Exception {
                        return FileUploadUtils.ossUploadFile("radio/", FileUploadUtils.FILE_TYPE_IMAGE, s);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String fileKey) {
                        dismissHUD();
                        images.add(fileKey);
                        newsCreate();
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

    //发布动态
    private void newsCreate() {
        if (filePaths != null && filePaths.size() > 0) {
            if (filePaths.size() != images.size()) {
                return;
            }
        }
        model.newsCreate(content.get(), images, is_comment.get(), is_hide.get())
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseDisposableObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        ToastUtils.showShort(R.string.issuance_success);
                        RxBus.getDefault().post(new BadioEvent(0));
                        pop();
                    }

                    @Override
                    public void onError(RequestException e) {
                        images.clear();
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public class UIChangeObservable {
        public SingleLiveEvent clickNotVip = new SingleLiveEvent<>();
    }

}