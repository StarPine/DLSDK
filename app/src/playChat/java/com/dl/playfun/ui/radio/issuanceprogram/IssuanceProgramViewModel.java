package com.dl.playfun.ui.radio.issuanceprogram;

import android.app.Application;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;

import com.aliyun.svideo.crop.bean.AlivcCropOutputParam;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseDisposableObserver;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.entity.DatingObjItemEntity;
import com.dl.playfun.entity.StatusEntity;
import com.dl.playfun.entity.ThemeItemEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.event.BadioEvent;
import com.dl.playfun.event.SelectMediaSourcesEvent;
import com.dl.playfun.event.UMengCustomEvent;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.radio.radiohome.RadioThemeItemViewModel;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.utils.FileUploadUtils;
import com.dl.playfun.utils.ListUtils;
import com.dl.playfun.utils.Utils;
import com.dl.playfun.viewmodel.BaseViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.binding.command.BindingConsumer;
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
public class IssuanceProgramViewModel extends BaseViewModel<AppRepository> {
    //消费者
    private Disposable MediaStoreDisposable,AlivcCropOutputDisposable;
    //用户选择媒体文件：图片/视频
    public ObservableField<String> selectMediaPath = new ObservableField<>();
    //用户选择心情、主题
    public ObservableField<String> selThemeItemName = new ObservableField<>("#" + StringUtils.getString(R.string.mood_item_id1));
    public DatingObjItemEntity $datingObjItemEntity;
    //心情选中
    public ObservableBoolean moolCheck = new ObservableBoolean(true);
    //约会内容
    public BindingRecyclerViewAdapter<RadioThemeItemViewModel> themeAdapter = new BindingRecyclerViewAdapter<>();
    public ItemBinding<RadioThemeItemViewModel> themeItemBinding = ItemBinding.of(BR.viewModel, R.layout.item_radio_theme);
    public ObservableList<RadioThemeItemViewModel> themeItems = new ObservableArrayList<>();
    //约会对象
    public BindingRecyclerViewAdapter<RadioDatingItemViewModel> objAdapter = new BindingRecyclerViewAdapter<>();
    public ItemBinding<RadioDatingItemViewModel> objItemBinding = ItemBinding.of(BR.viewModel, R.layout.item_radio_dating);
    public ObservableList<RadioDatingItemViewModel> objItems = new ObservableArrayList<>();
    //内容
    public ObservableField<String> programDesc = new ObservableField<>();
    public ObservableField<ConfigItemEntity> chooseCityItem = new ObservableField<>();
    public ObservableField<String> addressName = new ObservableField<>();
    public ObservableField<String> address = new ObservableField<>();
    public ObservableField<Double> lat = new ObservableField<>();
    public ObservableField<Double> lng = new ObservableField<>();

    public ObservableField<List<Integer>> hope_object = new ObservableField<>();
    public ObservableField<String> start_date = new ObservableField<>(Utils.formatday.format(new Date()));
    public List<String> images = new ArrayList<>();
    public ObservableField<Integer> is_comment = new ObservableField<>(0);
    public ObservableField<Integer> is_hide = new ObservableField<>(0);
    public List<ConfigItemEntity> list_chooseCityItem = new ArrayList<>();
    //    城市
    public List<ConfigItemEntity> time = new ArrayList<>();
    //    期望对象
    public List<ConfigItemEntity> hope = new ArrayList<>();
    public Integer sex;
    public ConfigManager configManager;

    //是否能发布约会
    public boolean isPlaying = false;

    public BindingCommand removeMediaPath = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            selectMediaPath.set(null);
        }
    });

    public BindingCommand<Boolean> isConnectionChangeCommand = new BindingCommand<>(new BindingConsumer<Boolean>() {
        @Override
        public void call(Boolean isChecked) {
            is_comment.set(isChecked ? 1 : 0);
            AppContext.instance().logEvent(AppsFlyerEvent.Set_as_Private);
        }
    });
    public BindingCommand<Boolean> isHideChangeCommand = new BindingCommand<>(new BindingConsumer<Boolean>() {
        @Override
        public void call(Boolean isChecked) {
            is_hide.set(isChecked ? 1 : 0);
            AppContext.instance().logEvent(AppsFlyerEvent.Hide_to_same_sex_users);
        }
    });
    UIChangeObservable uc = new UIChangeObservable();
    //跳转视频、图片剪辑页面
    public BindingCommand toClipImageVideoView = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            //start(ClipImageVideoFragmentCopy.class.getCanonicalName());
            uc.startVideoActivity.call();
        }
    });
    //    选择期望对象
    public BindingCommand chooseHope = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            uc.clickHope.call();
        }
    });
    //    选择日期
    public BindingCommand chooseDay = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            uc.clickDay.call();
        }
    });
    //    选择地点
    public BindingCommand chooseAddress = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            uc.clickAddress.call();
        }
    });
    ThemeItemEntity selThemeItem;
    //发布
    public BindingCommand issuanceClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            if (!moolCheck.get()) {
                if (ListUtils.isEmpty(hope_object.get())) {
                    ToastUtils.showShort(R.string.please_choose_hope);
                    return;
                }
                if (chooseCityItem.get() == null) {
                    ToastUtils.showShort(R.string.please_select_location);
                    return;
                }
            }

            AppContext.instance().logEvent(AppsFlyerEvent.Post2);
            if (moolCheck.get()) {
                publishCheck(1);
            } else {
                publishCheck(2);
            }
            RxBus.getDefault().post(new UMengCustomEvent(UMengCustomEvent.EVENT_BROADCAST_PROGRAM_PUBLISH));
        }
    });

    public IssuanceProgramViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
        hope.addAll(model.readHopeObjectConfig());
        time.addAll(model.readProgramTimeConfig());
        list_chooseCityItem.addAll(model.readCityConfig());
        sex = model.readUserData().getSex();
        configManager = ConfigManager.getInstance();
        //控制主题是否可以默认选中
        boolean flag = false;
        //添加心情手动写死
        ThemeItemEntity themeItemEntityMool = new ThemeItemEntity();
        themeItemEntityMool.setIcon(null);
        themeItemEntityMool.setId(-1);
        themeItemEntityMool.setTitle("心情");
        themeItemEntityMool.setThemeId(-1);
        themeItemEntityMool.setSmallIcon(AppContext.instance().getResources().getResourceName(R.mipmap.dating_obj_mood_img));
        themeItemEntityMool.setSelect(true);
        RadioThemeItemViewModel radioMoolItemViewModel = new RadioThemeItemViewModel(this, themeItemEntityMool);
        themeItems.add(radioMoolItemViewModel);
        //添加心情手动写死
        for (ConfigItemEntity configItemEntity : model.readThemeConfig()) {
            ThemeItemEntity themeItemEntity = new ThemeItemEntity();
            themeItemEntity.setIcon(configItemEntity.getIcon());
            themeItemEntity.setId(configItemEntity.getId());
            themeItemEntity.setTitle(configItemEntity.getName());
            themeItemEntity.setThemeId(configItemEntity.getThemeId());
            themeItemEntity.setSmallIcon(configItemEntity.getSmallIcon());
            if (flag) {
                themeItemEntity.setSelect(true);
                selThemeItem = themeItemEntity;
                //chooseProgramItem.set(themeItemEntity);//初始化赋值默认选中
                flag = false;
            } else {
                themeItemEntity.setSelect(false);
            }
            RadioThemeItemViewModel radioThemeItemViewModel = new RadioThemeItemViewModel(this, themeItemEntity);
            themeItems.add(radioThemeItemViewModel);
        }
    }

    public void sendConfirm() {
        String mediaPath = selectMediaPath.get();
        if (!StringUtils.isEmpty(mediaPath)) {
            uploadAvatar(mediaPath);
        } else {
            if (moolCheck.get()) {
                topicalCreateMood();
            } else {
                topicalCreate();
            }
        }
    }

    public void OnClickItem(DatingObjItemEntity datingObjItemEntity) {
        int type = datingObjItemEntity.getType();
        if (type == 0) {
            List<Integer> list = new ArrayList<>();
            list.add(datingObjItemEntity.getId());
            hope_object.set(list);
        }
        selThemeItemName.set("#" + datingObjItemEntity.getName());
        $datingObjItemEntity = datingObjItemEntity;
    }

    public void OnClickTheme(ThemeItemEntity themeItemEntity) {
        if (themeItemEntity != null) {
            if(themeItemEntity.getId()!=-1){
                moolCheck.set(false);
            }else{
                moolCheck.set(true);
                selThemeItemName.set("#" + $datingObjItemEntity.getName());
                return;
            }
            uc.checkDatingText.setValue(themeItemEntity.getTitle());
            selThemeItem = themeItemEntity;
            selThemeItemName.set("#" + themeItemEntity.getTitle());
            //chooseProgramItem.set(themeItemEntity);
        }
    }

    private void publishCheck(final int type) {
        model.publishCheck(type)
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
                            if (sex == 1) {
                                if (model.readUserData().getIsVip() != 1) {
                                    uc.clickNotVip.setValue(type);
                                    return;
                                }
                            } else {
//                                if (model.readUserData().getCertification() != 1) {
//                                    uc.clickNotVip.call();
//                                    return;
//                                }
                                //是女的就能發廣播
                                //sendConfirm();
                            }
                            sendConfirm();
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
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
                        if (filePath.endsWith(".mp4")) {
                            return FileUploadUtils.ossUploadFileVideo("Issuance/", FileUploadUtils.FILE_TYPE_IMAGE, s, null);
                        } else {
                            return FileUploadUtils.ossUploadFile("Issuance/", FileUploadUtils.FILE_TYPE_IMAGE, s);
                        }

                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String fileKey) {
                        dismissHUD();
                        if(fileKey!=null){
                            if (fileKey.endsWith(".mp4")) {
                                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                                try {
                                    //根据url获取缩略图
                                    retriever.setDataSource(selectMediaPath.get());
                                    //获得第一帧图片
                                    Bitmap bitmap = retriever.getFrameAtTime(1);
                                    ApiUitl.saveBitmap(AppContext.instance(), bitmap, "PlayChat" + ApiUitl.getDateTimeFileName() + ".jpg", new ApiUitl.CallBackUploadFileNameCallback() {
                                        @Override
                                        public void success(String fileName) {
                                            //取视频第一帧图片保存成功后再次上报发送
                                            File deleteFile = new File(filePath);
                                            deleteFile.delete();
                                            selectMediaPath.set(fileKey);
                                            uploadAvatar(fileName);
                                        }
                                    });
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } finally {
                                    retriever.release();
                                }
                            } else {
                                File deleteFile = new File(filePath);
                                deleteFile.delete();
                                images.add(fileKey);
                                if (moolCheck.get()) {
                                    topicalCreateMood();
                                } else {
                                    topicalCreate();
                                }
                            }
                        }

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

    private void topicalCreate() {
        String video = null;
        if(!StringUtils.isEmpty(selectMediaPath.get())){
            if(selectMediaPath.get().endsWith(".mp4")){
                video = selectMediaPath.get();
            }
        }
        String startStr = start_date.get().replace(StringUtils.getString(R.string.year), "-").replace(StringUtils.getString(R.string.month), "-").replace(StringUtils.getString(R.string.daily), "");
        model.topicalCreate(selThemeItem.getThemeId(), programDesc.get(), address.get(), hope_object.get(), startStr,
                7, images, is_comment.get(), is_hide.get(), addressName.get(), chooseCityItem.get().getId(), lng.get(), lat.get(), video)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseDisposableObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        ToastUtils.showShort(R.string.issuance_success);
                        RxBus.getDefault().post(new BadioEvent(1));
                        UserDataEntity userDataEntity = model.readUserData();
                        if (ObjectUtils.isEmpty(userDataEntity.getPermanentCityIds())) {
                            List<Integer> list = new ArrayList<>();
                            list.add(1);
                            userDataEntity.setPermanentCityIds(list);
                            model.saveUserData(userDataEntity);
                        }
                        pop();
                    }

                    @Override
                    public void onError(RequestException e) {
                        ToastUtils.showShort(e.getMessage());
                        images.clear();
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public void topicalCreateMood() {
        String video = null;
        if(!StringUtils.isEmpty(selectMediaPath.get())){
            if(selectMediaPath.get().endsWith(".mp4")){
                video = selectMediaPath.get();
            }
        }
        String startStr = start_date.get().replace(StringUtils.getString(R.string.year), "-").replace(StringUtils.getString(R.string.month), "-").replace(StringUtils.getString(R.string.daily), "");
        model.topicalCreateMood(programDesc.get(), startStr, images == null ? null : images, is_comment.get(), is_hide.get(), lng.get(), lat.get(), video, $datingObjItemEntity.getId())
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseDisposableObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        ToastUtils.showShort(R.string.issuance_success);
                        RxBus.getDefault().post(new BadioEvent(1));
                        UserDataEntity userDataEntity = model.readUserData();
                        if (ObjectUtils.isEmpty(userDataEntity.getPermanentCityIds())) {
                            List<Integer> list = new ArrayList<>();
                            list.add(1);
                            userDataEntity.setPermanentCityIds(list);
                            model.saveUserData(userDataEntity);
                        }
                        pop();
                    }

                    @Override
                    public void onError(RequestException e) {
                        ToastUtils.showShort(e.getMessage());
                        images.clear();
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //检测是否有历史约会
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
                        isPlaying = true;
                    }
                    @Override
                    public void onError(RequestException e) {
                        dismissHUD();
                        isPlaying = false;
                    }
                });
    }

    public class UIChangeObservable {
        public SingleLiveEvent clickHope = new SingleLiveEvent<>();
        public SingleLiveEvent clickDay = new SingleLiveEvent<>();
        public SingleLiveEvent<Integer> clickNotVip = new SingleLiveEvent<>();
        public SingleLiveEvent clickTheme = new SingleLiveEvent<>();
        public SingleLiveEvent clickAddress = new SingleLiveEvent<>();
        //选中心情、约会内容
        public SingleLiveEvent<String> checkDatingText = new SingleLiveEvent<>();
        //跳转Activity
        public SingleLiveEvent startVideoActivity = new SingleLiveEvent<>();
    }

    @Override
    public void registerRxBus() {
        super.registerRxBus();
        MediaStoreDisposable =  RxBus.getDefault().toObservable(SelectMediaSourcesEvent.class).subscribe(selectMediaSourcesEvent -> {
            selectMediaPath.set(selectMediaSourcesEvent.getPath());
        });
        AlivcCropOutputDisposable = RxBus.getDefault().toObservable(AlivcCropOutputParam.class).subscribe(alivcCropOutputParam -> {
            selectMediaPath.set(alivcCropOutputParam.getOutputPath());
        });
    }

    @Override
    public void removeRxBus() {
        super.removeRxBus();
        RxSubscriptions.remove(MediaStoreDisposable);
        RxSubscriptions.remove(AlivcCropOutputDisposable);
    }


}