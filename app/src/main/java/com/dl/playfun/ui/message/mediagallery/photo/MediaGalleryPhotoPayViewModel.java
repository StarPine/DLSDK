package com.dl.playfun.ui.message.mediagallery.photo;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.tencent.qcloud.tuicore.custom.entity.MediaGalleryEditEntity;

import me.goldze.mvvmhabit.base.BaseModel;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * Author: 彭石林
 * Time: 2022/9/14 14:23
 * Description: This is MediaGalleryPhotoPayViewModel
 */
public class MediaGalleryPhotoPayViewModel extends BaseViewModel<AppRepository> {

    private MediaGalleryEditEntity mediaGalleryEditEntity;

    //是否需要评价
    public ObservableBoolean evaluationState = new ObservableBoolean(false);

    //评价状态：喜欢 or 不喜欢
    public ObservableBoolean evaluationLikeState = new ObservableBoolean(false);
    //快照可见倒计时状态
    public ObservableBoolean snapshotTimeState = new ObservableBoolean(false);
    //快照可见倒计时时间
    public ObservableField<String> snapshotTimeText = new ObservableField<>();
    //提示解锁
    public ObservableBoolean snapshotLockState = new ObservableBoolean(false);
    //查看解锁按钮
    public BindingCommand<Void> clickUnLock = new BindingCommand<>(() -> {
        if(mediaGalleryEditEntity!=null){
            mediaGallerySnapshotUnLock(mediaGalleryEditEntity.getMsgKeyId(),mediaGalleryEditEntity.getToUserId());
        }
    });


    //返回上一页
    public BindingCommand<Void> onBackViewClick = new BindingCommand<>(this::finish);

    public MediaGalleryPhotoPayViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
    }

    public void mediaGallerySnapshotUnLock(String msgKey,Integer toUserId){
        model.mediaGallerySnapshotUnLock(msgKey,toUserId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {
                        snapshotLockState.set(true);
                    }
                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }
}
