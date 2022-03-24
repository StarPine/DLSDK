package com.dl.playfun.ui.program.programdetail;

import static com.dl.playfun.ui.radio.radiohome.RadioViewModel.RadioRecycleType_Topical;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseDisposableObserver;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.IsChatEntity;
import com.dl.playfun.entity.TopicalListEntity;
import com.dl.playfun.event.RadioadetailEvent;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.observable.RefreshLoadMoreUIChangeObservable;
import com.dl.playfun.ui.mine.broadcast.myprogram.ProgramItemViewModel;
import com.dl.playfun.utils.ChatUtils;
import com.dl.playfun.utils.FileUploadUtils;
import com.dl.playfun.utils.ListUtils;
import com.dl.playfun.viewmodel.BaseViewModel;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.base.MultiItemViewModel;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.tatarka.bindingcollectionadapter2.ItemBinding;
import me.tatarka.bindingcollectionadapter2.OnItemBind;

/**
 * @author wulei
 */
public class ProgramDetailViewModel extends BaseViewModel<AppRepository> {
    private static final String ProgramRecycleType_Head = "head";
    private static final String ProgramRecycleType_Dynamic = "dynamic";
    //    public ObservableField<TopicalListEntity> topicalListEntityObservableField = new ObservableField<>();
    public boolean isVip;
    public int sex;
    public int userId;
    public String avatar;
    public ObservableField<Boolean> isDetele = new ObservableField<>(false);
    public ObservableField<Boolean> isSelf = new ObservableField<>(false);
    public int id;
    public Integer certification = null;

    //明细用户点击人员
    public String getUserId = null;
    public String getNickname = null;
    public UIChangeObservable uc = new UIChangeObservable();
    //给RecyclerView添加ObservableList
    public ObservableList<MultiItemViewModel> observableList = new ObservableArrayList<>();
    //RecyclerView多布局添加ItemBinding
    public ItemBinding<MultiItemViewModel> itemBinding = ItemBinding.of(new OnItemBind<MultiItemViewModel>() {
        @Override
        public void onItemBind(ItemBinding itemBinding, int position, MultiItemViewModel item) {
            //通过item的类型, 动态设置Item加载的布局
            String itemType = (String) item.getItemType();
            if (ProgramRecycleType_Head.equals(itemType)) {
                //设置头布局
                itemBinding.set(BR.viewModel, R.layout.item_program);
            } else if (ProgramRecycleType_Dynamic.equals(itemType)) {
                //设置左布局
                itemBinding.set(BR.viewModel, R.layout.item_sign_up);
            }
        }
    });
    RefreshLoadMoreUIChangeObservable rmuc = new RefreshLoadMoreUIChangeObservable();

    public ProgramDetailViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
        isDetele.set(false);
        initUserDate();
    }

    public void initUserDate() {
        isVip = model.readUserData().getIsVip() == 1;
        sex = model.readUserData().getSex();
        userId = model.readUserData().getId();
        avatar = model.readUserData().getAvatar();
        certification = model.readUserData().getCertification();
    }

    public void setId(int id) {
        this.id = id;
    }

    private void topicalDetail() {
        model.topicalDetail(id)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseDisposableObserver<BaseDataResponse<TopicalListEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<TopicalListEntity> response) {
                        isDetele.set(false);
                        observableList.clear();
                        if (response.isSuccess() && response.getData() != null && response.getData().getUser().getId() == userId) {
                            isSelf.set(true);
                        }
                        MultiItemViewModel item = new ProgramItemViewModel(ProgramDetailViewModel.this, response.getData());
                        //条目类型为头布局
                        item.multiItemType(ProgramRecycleType_Head);
                        observableList.add(item);
                        if (!ListUtils.isEmpty(response.getData().getSigns())) {
                            for (int i = 0; i < response.getData().getSigns().size(); i++) {
                                MultiItemViewModel dynamic1 = new ProgramDetailDynamicItemViewModel(ProgramDetailViewModel.this, response.getData().getSigns().get(i));
                                //条目类型为头布局
                                dynamic1.multiItemType(ProgramRecycleType_Dynamic);
                                observableList.add(dynamic1);
                            }
                        }
                    }

                    @Override
                    public void onError(RequestException e) {
                        if (e.getCode() == 10013) {
                            isDetele.set(true);
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                        rmuc.finishRefreshing.call();
                    }
                });

    }

    //节目点赞
    public void topicalGive() {
        model.TopicalGive(((ProgramItemViewModel) observableList.get(0)).topicalListEntityObservableField.get().getId())
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ToastUtils.showShort(R.string.give_success);
                        ((ProgramItemViewModel) observableList.get(0)).addGiveUser();
                        RadioadetailEvent radioadetailEvent = new RadioadetailEvent();
                        radioadetailEvent.setId(id);
                        radioadetailEvent.setRadioaType(RadioRecycleType_Topical);
                        radioadetailEvent.setType(6);
                        RxBus.getDefault().post(radioadetailEvent);
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
                        ((ProgramItemViewModel) observableList.get(0)).addComment(id, content, toUserId, toUserName, model.readUserData().getNickname());
                        RadioadetailEvent radioadetailEvent = new RadioadetailEvent();
                        radioadetailEvent.setId(id);
                        radioadetailEvent.setRadioaType(RadioRecycleType_Topical);
                        radioadetailEvent.setType(5);
                        radioadetailEvent.setContent(content);
                        radioadetailEvent.setToUserId(toUserId);
                        radioadetailEvent.setToUserName(toUserName);
                        RxBus.getDefault().post(radioadetailEvent);
                    }


                    @Override
                    public void onError(RequestException e) {
                        if (e.getCode() == 10016) {
                            ToastUtils.showShort(StringUtils.getString(R.string.comment_close));
                            ((ProgramItemViewModel) observableList.get(0)).topicalListEntityObservableField.get().getBroadcast().setIsComment(1);
                            RadioadetailEvent radioadetailEvent = new RadioadetailEvent();
                            radioadetailEvent.setId(id);
                            radioadetailEvent.setRadioaType(RadioRecycleType_Topical);
                            radioadetailEvent.setType(2);
                            radioadetailEvent.setIsComment(1);
                            RxBus.getDefault().post(radioadetailEvent);
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //节目结束报名
    public void TopicalFinish() {
        model.TopicalFinish(((ProgramItemViewModel) observableList.get(0)).topicalListEntityObservableField.get().getId())
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ((ProgramItemViewModel) observableList.get(0)).topicalListEntityObservableField.get().setIsEnd(1);
                        RadioadetailEvent radioadetailEvent = new RadioadetailEvent();
                        radioadetailEvent.setId(id);
                        radioadetailEvent.setRadioaType(RadioRecycleType_Topical);
                        radioadetailEvent.setType(4);
                        RxBus.getDefault().post(radioadetailEvent);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //我要报名
    public void report(String imags) {
        model.singUp(((ProgramItemViewModel) observableList.get(0)).topicalListEntityObservableField.get().getId(), imags)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ToastUtils.showShort(R.string.sign_up_success);
                        ((ProgramItemViewModel) observableList.get(0)).report();
                        RadioadetailEvent radioadetailEvent = new RadioadetailEvent();
                        radioadetailEvent.setId(id);
                        radioadetailEvent.setRadioaType(RadioRecycleType_Topical);
                        radioadetailEvent.setType(3);
                        RxBus.getDefault().post(radioadetailEvent);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public void imagUpload(String filePath) {
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
                        report(fileKey);
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
    public void setComment() {
        model.setComment(((ProgramItemViewModel) observableList.get(0)).topicalListEntityObservableField.get().getBroadcast().getId(),
                ((ProgramItemViewModel) observableList.get(0)).topicalListEntityObservableField.get().getBroadcast().getIsComment() == 0 ? 1 : 0)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ToastUtils.showShort(((ProgramItemViewModel) observableList.get(0)).topicalListEntityObservableField.get().getBroadcast().getIsComment() == 1 ? StringUtils.getString(R.string.open_comment_success) : StringUtils.getString(R.string.close_success));
                        ((ProgramItemViewModel) observableList.get(0)).topicalListEntityObservableField.get().getBroadcast().setIsComment(
                                ((ProgramItemViewModel) observableList.get(0)).topicalListEntityObservableField.get().getBroadcast().getIsComment() == 0 ? 1 : 0);

                        RadioadetailEvent radioadetailEvent = new RadioadetailEvent();
                        radioadetailEvent.setId(id);
                        radioadetailEvent.setRadioaType(RadioRecycleType_Topical);
                        radioadetailEvent.setType(2);
                        radioadetailEvent.setIsComment(((ProgramItemViewModel) observableList.get(0)).topicalListEntityObservableField.get().getBroadcast().getIsComment() == 0 ? 1 : 0);
                        RxBus.getDefault().post(radioadetailEvent);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //举报
    public void signUpReport(int id) {
        model.signUpReport(id)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ToastUtils.showShort(R.string.report_success);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //删除节目
    public void deleteTopical() {
        model.deleteTopical(((ProgramItemViewModel) observableList.get(0)).topicalListEntityObservableField.get().getId())
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        RadioadetailEvent radioadetailEvent = new RadioadetailEvent();
                        radioadetailEvent.setId(id);
                        radioadetailEvent.setRadioaType(RadioRecycleType_Topical);
                        radioadetailEvent.setType(1);
                        RxBus.getDefault().post(radioadetailEvent);
                        pop();
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    @Override
    public void onEnterAnimationEnd() {
        super.onEnterAnimationEnd();
        topicalDetail();
    }

    public void isChat(int userId, int type, String getUserIds, String getNicknames) {
        getUserId = getUserIds;
        getNickname = getNicknames;
        model.isChat(userId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<IsChatEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<IsChatEntity> response) {
                        if (type == 1) {//私聊他  彭石林2021-3-26修改
                            //判断如果是男用户，且不是VIP状态下
                            if (response.getData().getIsChant() == 1 || (model.readUserData().getSex() == AppConfig.MALE && model.readUserData().getIsVip() == 0)) {
                                ChatUtils.chatUser(String.format("user_%s", getUserId), getNickname, ProgramDetailViewModel.this);
                            } else {
                                if (response.getData().getChatNumber() > 0) {
                                    uc.clickVipChat.postValue(response.getData().getChatNumber());
                                } else {
                                    uc.clickPayChat.postValue(ConfigManager.getInstance().getImMoney());
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

    /**
     * 使用VIP解鎖機會
     *
     * @param userId
     * @param type        1 聊天 & 解鎖社交賬號  2 相冊
     * @param operateType 1 聊天 2 相冊 3 解鎖社交賬號
     */
    public void useVipChat(int userId, int type, int operateType) {
        model.useVipChat(userId, type)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        if (operateType == 1) {
                            chatPaySuccess();
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    /**
     * 解锁联系方式成功
     */
    public void chatPaySuccess() {
        addFriend();
    }

    public void addFriend() {
        if (getUserId == null) {
            return;
        }
        ChatUtils.chatUser(String.format("user_%s", getUserId), getNickname, this);
    }

    public class UIChangeObservable {
        public SingleLiveEvent clickMore = new SingleLiveEvent<>();
        public SingleLiveEvent clickLike = new SingleLiveEvent<>();
        public SingleLiveEvent clickComment = new SingleLiveEvent<>();
        public SingleLiveEvent clickSignUp = new SingleLiveEvent<>();
        public SingleLiveEvent clickCheck = new SingleLiveEvent<>();
        public SingleLiveEvent signUpSucceed = new SingleLiveEvent<>();
        public SingleLiveEvent clickReport = new SingleLiveEvent<>();
        public SingleLiveEvent clickImage = new SingleLiveEvent<>();
        public SingleLiveEvent clickIisDelete = new SingleLiveEvent<>();

        public SingleLiveEvent<Integer> clickPayChat = new SingleLiveEvent<>();
        public SingleLiveEvent<Integer> clickVipChat = new SingleLiveEvent<>();

        public SingleLiveEvent<Map<String, String>> clickPlayersVideo = new SingleLiveEvent<>();
    }

}