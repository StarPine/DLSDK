package com.dl.playfun.ui.message.snapshot;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.utils.FileUploadUtils;
import com.dl.playfun.utils.Utils;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.luck.picture.lib.entity.LocalMedia;
import com.tencent.custom.tmp.CustomDlTempMessage;
import com.tencent.qcloud.tuicore.custom.CustomConstants;
import com.tencent.qcloud.tuikit.tuichat.TUIChatConstants;
import com.tencent.qcloud.tuikit.tuichat.bean.CustomImageMessage;
import com.tencent.qcloud.tuikit.tuichat.bean.message.TUIMessageBean;
import com.tencent.qcloud.tuikit.tuichat.util.ChatMessageBuilder;

import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Author: 彭石林
 * Time: 2022/9/9 12:03
 * Description: This is SnapshotPhotoViewModel
 */
public class SnapshotPhotoViewModel extends BaseViewModel<AppRepository> {
    public ObservableBoolean isVideoSetting = new ObservableBoolean(false);
    public ObservableBoolean isBurn = new ObservableBoolean(false);

    public SingleLiveEvent<Void> settingEvent = new SingleLiveEvent<>();

    public SnapshotPhotoViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
    }

    public BindingCommand settingClick = new BindingCommand(()->{
        settingEvent.call();
    });

    public BindingCommand burnOnClickCommand = new BindingCommand(() -> {
    });

    //上传文件
    public BindingCommand<Void> clickReportFile = new BindingCommand(() -> {

    });

    //上传文件到阿里云
    public void uploadFileOSS(final LocalMedia localMedia){
        final String filePath = localMedia.getCompressPath();
        Observable.just(filePath)
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribeOn(Schedulers.io())
                .map((Function<String, String>) s -> {
                    String fileName = AppConfig.OSS_CUSTOM_FILE_NAME_CHAT +"/"+ Utils.formatYYMMSS.format(new Date());
                    return FileUploadUtils.ossUploadFileCustom(FileUploadUtils.FILE_TYPE_IMAGE, filePath, fileName, new FileUploadUtils.FileUploadProgressListener() {
                        @Override
                        public void fileCompressProgress(int progress) {
                            showProgressHUD(String.format("%ss", progress), progress);
                        }

                        @Override
                        public void fileUploadProgress(int progress) {
                            showProgressHUD(String.format("%ss", progress), progress);
                        }
                    });
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String fileKey) {
                        dismissHUD();
                        if(fileKey!=null){
                            //用IM框架默认的图片类型
//                            TUIMessageBean info = ChatMessageBuilder.buildImageMessage(Uri.fromFile(new File(filePath)));
                            //用自定义图片类型
//                            CustomImageMessage customImageMessage = new CustomImageMessage();
//                            customImageMessage.version = TUIChatConstants.version;
//                            customImageMessage.setImgPath(fileKey);
//                            customImageMessage.setImgWidth(localMedia.getWidth());
//                            customImageMessage.setImgHeight(localMedia.getHeight());
//
//
//                            CustomDlTempMessage.MsgModuleInfo msgModuleInfo = new CustomDlTempMessage.MsgModuleInfo();
//                            msgModuleInfo.setMsgModuleName(CustomConstants.MediaGallery.MODULE_NAME);
//                            //消息内容体
//                            CustomDlTempMessage.MsgBodyInfo msgBodyInfo = new CustomDlTempMessage.MsgBodyInfo();
//                            msgBodyInfo.setCustomMsgType(CustomConstants.MediaGallery.PHOTO_GALLERY);
//                            msgBodyInfo.setCustomMsgBody(customImageMessage);
//                            msgModuleInfo.setContentBody(msgBodyInfo);
//
//                            CustomDlTempMessage customDlTempMessage = new CustomDlTempMessage();
//                            customDlTempMessage.setContentBody(msgModuleInfo);
//
//                            String data = GsonUtils.toJson(customDlTempMessage);
//                            TUIMessageBean info = ChatMessageBuilder.buildCustomMessage(data, null, null);
//                            uc.signUploadSendMessage.postValue(info);
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissHUD();
                        ToastUtils.showShort(R.string.playfun_upload_failed);
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

}
