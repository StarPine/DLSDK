package com.dl.playfun.ui.mine.broadcast.mytrends.trenddetail;

import static com.dl.playfun.app.AppConfig.MALE;
import static com.dl.playfun.ui.mine.broadcast.mytrends.HeadItemViewModel.Type_New;
import static com.dl.playfun.ui.radio.radiohome.RadioViewModel.RadioRecycleType_New;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;

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
import com.dl.playfun.entity.CommentEntity;
import com.dl.playfun.entity.GiveUserBeanEntity;
import com.dl.playfun.entity.NewsEntity;
import com.dl.playfun.event.RadioadetailEvent;
import com.dl.playfun.event.UMengCustomEvent;
import com.dl.playfun.ui.mine.broadcast.mytrends.CommentItemViewModel;
import com.dl.playfun.ui.mine.broadcast.mytrends.HeadItemViewModel;
import com.dl.playfun.ui.mine.broadcast.mytrends.ImageItemViewModel;
import com.dl.playfun.ui.userdetail.detail.UserDetailFragment;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.utils.ListUtils;
import com.dl.playfun.viewmodel.BaseViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.binding.command.BindingConsumer;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

public class TrendDetailViewModel extends BaseViewModel<AppRepository> {
    public ObservableField<NewsEntity> newsEntityObservableField = new ObservableField<>();
    public int userId;
    public String avatar;
    public int sex;
    public ObservableField<Boolean> isDetele = new ObservableField<>(false);
    public ObservableField<Boolean> isSelf = new ObservableField<>(false);
    public ObservableField<Boolean> isShowComment = new ObservableField<>(false);
    //    public ObservableField<String> positonStr = new ObservableField<>();
    public ObservableField<Integer> pointPositon = new ObservableField<>(0);
    //ViewPager切换监听
    public BindingCommand<Integer> onPageSelectedCommand = new BindingCommand<>(new BindingConsumer<Integer>() {
        @Override
        public void call(Integer index) {
            pointPositon.set(index);
//            positonStr.set(String.format("%s/%s", index + 1, newsEntityObservableField.get().getImages().size()));
        }
    });
    public UIChangeObservable uc = new UIChangeObservable();
    public ObservableList<HeadItemViewModel> itemList = new ObservableArrayList<>();
    public ItemBinding<HeadItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_head);
    public ObservableList<ImageItemViewModel> imageItemList = new ObservableArrayList<>();
    public ItemBinding<ImageItemViewModel> imageItemBinding = ItemBinding.of(BR.viewModel, R.layout.item_image);
    public ObservableList<CommentItemViewModel> commentItemList = new ObservableArrayList<>();
    public ItemBinding<CommentItemViewModel> commentItemBinding = ItemBinding.of(BR.viewModel, R.layout.item_comment);

    //播放视频
    public BindingCommand playView = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            uc.playVideoView.call();
        }
    });
    //更多的点击事件
    public BindingCommand moreClick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            uc.clickMore.call();
        }
    });
    //点赞点击事件
    public BindingCommand likeClick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            if (newsEntityObservableField.get().getIsGive() == 0) {
                AppContext.instance().logEvent(AppsFlyerEvent.Like_2);
                RxBus.getDefault().post(new UMengCustomEvent(UMengCustomEvent.EVENT_BROADCAST_LIKE));
                newsGive();
            } else {
                ToastUtils.showShort(R.string.already);
            }
        }
    });
    //评论点击事件
    public BindingCommand commentClick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            if (newsEntityObservableField.get() == null || newsEntityObservableField.get().getBroadcast() == null) {
                return;
            }
            if (newsEntityObservableField.get().getBroadcast().getIsComment() == 1) {
                ToastUtils.showShort(R.string.comment_close);
                return;
            }
            if (newsEntityObservableField.get().getUser().getSex() == sex) {
                if (sex == MALE) {
                    ToastUtils.showShort(R.string.warn_male_not_comment_dynamic);
                } else {
                    ToastUtils.showShort(R.string.warn_female_not_comment_dynamic);
                }
                return;
            }
            if (userId == newsEntityObservableField.get().getUser().getId()) {
                ToastUtils.showShort(R.string.self_ont_comment_broadcast);
            } else {
                AppContext.instance().logEvent(AppsFlyerEvent.Message_2);
                Map<String, String> data = new HashMap<>();
                data.put("id", String.valueOf(newsEntityObservableField.get().getId()));
                data.put("toUseriD", null);
                uc.clickComment.setValue(data);
                RxBus.getDefault().post(new UMengCustomEvent(UMengCustomEvent.EVENT_BROADCAST_COMMENT));
            }
        }
    });
    //头像的点击事件
    public BindingCommand avatarClick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {

            if (newsEntityObservableField.get() != null && newsEntityObservableField.get().getUser() != null && newsEntityObservableField.get().getUser().getSex().intValue() == model.readUserData().getSex().intValue()) {
                ToastUtils.showShort(newsEntityObservableField.get().getUser().getSex() == 0 ? StringUtils.getString(R.string.madam_ont_check_madam_detail) : StringUtils.getString(R.string.man_ont_check_man_detail));
            } else if (newsEntityObservableField.get() != null && newsEntityObservableField.get().getUser() != null) {
                AppContext.instance().logEvent(AppsFlyerEvent.User_Page_1);
                Bundle bundle = UserDetailFragment.getStartBundle(newsEntityObservableField.get().getUser().getId());
                start(UserDetailFragment.class.getCanonicalName(), bundle);
            }
        }
    });

    public TrendDetailViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
        userId = model.readUserData().getId();
        isDetele.set(false);
        avatar = model.readUserData().getAvatar();
        sex = model.readUserData().getSex();
    }

    private void init() {
//        if (newsEntityObservableField.get().getImages() != null) {
//            positonStr.set(String.format("%s/%s", 1, newsEntityObservableField.get().getImages().size()));
//            for (int i = 0; i < newsEntityObservableField.get().getImages().size(); i++) {
//                ImageItemViewModel ivItem = new ImageItemViewModel(this, newsEntityObservableField.get().getImages().get(i), i, newsEntityObservableField.get().getImages());
//                imageItemList.add(ivItem);
//            }
//        }
        commentItemList.clear();
        itemList.clear();
        if (newsEntityObservableField.get().getGive_user() != null) {
            for (int i = 0; i < newsEntityObservableField.get().getGive_user().size(); i++) {
                if (i < 13) {
                    HeadItemViewModel item = new HeadItemViewModel(this, newsEntityObservableField.get().getGive_user().get(i).getAvatar(),
                            newsEntityObservableField.get().getGive_user().get(i).getId(),
                            newsEntityObservableField.get().getGive_user().get(i).getSex(),
                            0,
                            Type_New, newsEntityObservableField.get().getId()
                    );
                    itemList.add(item);
                } else if (i == 13) {
                    HeadItemViewModel item = new HeadItemViewModel(this, newsEntityObservableField.get().getGive_user().get(i).getAvatar(),
                            newsEntityObservableField.get().getGive_user().get(i).getId(),
                            newsEntityObservableField.get().getGive_user().get(i).getSex(),
                            newsEntityObservableField.get().getGiveCount() - 14,
                            Type_New, newsEntityObservableField.get().getId()
                    );
                    itemList.add(item);
                }
            }
        }

        if (newsEntityObservableField.get().getComment() != null) {
            for (int i = 0; i < newsEntityObservableField.get().getComment().size(); i++) {
                if (i < 5 || ApiUitl.isShow) {
                    CommentItemViewModel commentItemViewModel = new CommentItemViewModel(this, newsEntityObservableField.get().getComment().get(i),
                            newsEntityObservableField.get().getId(), RadioRecycleType_New, newsEntityObservableField.get().getUser().getId() == userId, false);
                    commentItemList.add(commentItemViewModel);
                } else {
                    CommentItemViewModel commentItemViewModel = new CommentItemViewModel(this, newsEntityObservableField.get().getComment().get(i),
                            newsEntityObservableField.get().getId(), RadioRecycleType_New, newsEntityObservableField.get().getUser().getId() == userId, true);
                    commentItemList.add(commentItemViewModel);
                    break;
                }

            }
        }

        if (ListUtils.isEmpty(newsEntityObservableField.get().getComment())) {
            isShowComment.set(false);
        } else {
            isShowComment.set(true);
        }

    }

    @Override
    public void onEnterAnimationEnd() {
        super.onEnterAnimationEnd();
        newsDetail();
    }

    public void newsDetail() {
        model.newsDetail(newsEntityObservableField.get().getId())
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseDisposableObserver<BaseDataResponse<NewsEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<NewsEntity> response) {
                        newsEntityObservableField.set(response.getData());
                        isDetele.set(false);
                        if (response.isSuccess() && response.getData() != null && response.getData().getUser().getId() == userId) {
                            isSelf.set(true);
                        }
                        init();
//                        uc.clickIisDelete.call();
                    }

                    @Override
                    public void onError(RequestException e) {
                        if (e.getCode() == 10013) {
                            isDetele.set(true);
//                            uc.clickIisDelete.call();
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });

    }

    //删除动态
    public void deleteNews() {
        model.deleteNews(newsEntityObservableField.get().getId())
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        RadioadetailEvent radioadetailEvent = new RadioadetailEvent();
                        radioadetailEvent.setId(newsEntityObservableField.get().getId());
                        radioadetailEvent.setRadioaType(RadioRecycleType_New);
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

    //开启/关闭评论
    public void setComment() {
        model.setComment(newsEntityObservableField.get().getBroadcast().getId(),
                newsEntityObservableField.get().getBroadcast().getIsComment() == 0 ? 1 : 0)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ToastUtils.showShort(newsEntityObservableField.get().getBroadcast().getIsComment() == 1 ? StringUtils.getString(R.string.open_comment_success) : StringUtils.getString(R.string.close_success));
                        newsEntityObservableField.get().getBroadcast().setIsComment(
                                newsEntityObservableField.get().getBroadcast().getIsComment() == 0 ? 1 : 0);
                        RadioadetailEvent radioadetailEvent = new RadioadetailEvent();
                        radioadetailEvent.setId(newsEntityObservableField.get().getId());
                        radioadetailEvent.setRadioaType(RadioRecycleType_New);
                        radioadetailEvent.setType(2);
                        radioadetailEvent.setIsComment(newsEntityObservableField.get().getBroadcast().getIsComment() == 0 ? 1 : 0);
                        RxBus.getDefault().post(radioadetailEvent);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //动态点赞
    public void newsGive() {
        model.newsGive(newsEntityObservableField.get().getId())
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ToastUtils.showShort(R.string.give_success);
                        if (newsEntityObservableField.get().getGive_user() == null) {
                            newsEntityObservableField.get().setGive_user(new ArrayList<>());
                        }
                        GiveUserBeanEntity giveUserBeanEntity = new GiveUserBeanEntity(userId, avatar);
                        newsEntityObservableField.get().getGive_user().add(giveUserBeanEntity);
                        newsEntityObservableField.get().setGiveSize(newsEntityObservableField.get().getGive_user().size());
                        newsEntityObservableField.get().setIsGive(1);
                        newsEntityObservableField.get().getBroadcast().setGiveCount(newsEntityObservableField.get().getBroadcast().getGiveCount() + 1);
                        if (newsEntityObservableField.get().getGiveCount() < 13) {
                            HeadItemViewModel item = new HeadItemViewModel(TrendDetailViewModel.this, avatar, userId,
                                    sex,
                                    0,
                                    Type_New, newsEntityObservableField.get().getId()
                            );
                            itemList.add(item);
                        }
                        RadioadetailEvent radioadetailEvent = new RadioadetailEvent();
                        radioadetailEvent.setId(newsEntityObservableField.get().getId());
                        radioadetailEvent.setRadioaType(RadioRecycleType_New);
                        radioadetailEvent.setType(6);
                        RxBus.getDefault().post(radioadetailEvent);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //动态评论
    public void newsComment(Integer id, String content, Integer toUserId, String toUserName) {
        model.newsComment(id, content, toUserId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseDisposableObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        ToastUtils.showShort(R.string.comment_success);
                        if (newsEntityObservableField.get().getComment() == null) {
                            newsEntityObservableField.get().setComment(new ArrayList<>());
                        }
                        CommentEntity commentEntity = new CommentEntity();
                        commentEntity.setContent(content);
                        commentEntity.setId(id);
                        commentEntity.setUserId(userId);
                        CommentEntity.UserBean userBean = new CommentEntity.UserBean();
                        userBean.setId(userId);
                        userBean.setNickname(model.readUserData().getNickname());
                        commentEntity.setUser(userBean);
                        if (toUserName != null) {
                            CommentEntity.TouserBean touserBean = new CommentEntity.TouserBean();
                            touserBean.setId(toUserId);
                            touserBean.setNickname(toUserName);
                            commentEntity.setTouser(touserBean);
                        }
                        newsEntityObservableField.get().getComment().add(commentEntity);
                        CommentItemViewModel commentItemViewModel = new CommentItemViewModel(TrendDetailViewModel.this, commentEntity, newsEntityObservableField.get().getId(),
                                RadioRecycleType_New, newsEntityObservableField.get().getUser().getId() == userId, false);
                        commentItemList.add(commentItemViewModel);

                        if (ListUtils.isEmpty(newsEntityObservableField.get().getComment())) {
                            isShowComment.set(false);
                        } else {
                            isShowComment.set(true);
                        }

                        RadioadetailEvent radioadetailEvent = new RadioadetailEvent();
                        radioadetailEvent.setId(id);
                        radioadetailEvent.setRadioaType(RadioRecycleType_New);
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
                            newsEntityObservableField.get().getBroadcast().setIsComment(1);
                            RadioadetailEvent radioadetailEvent = new RadioadetailEvent();
                            radioadetailEvent.setId(id);
                            radioadetailEvent.setRadioaType(RadioRecycleType_New);
                            radioadetailEvent.setType(2);
                            radioadetailEvent.setIsComment(1);
                            RxBus.getDefault().post(radioadetailEvent);
                        } else {
                            ToastUtils.showShort(e.getMessage() == null ? "" : e.getMessage());
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public class UIChangeObservable {
        public SingleLiveEvent clickMore = new SingleLiveEvent<>();
        public SingleLiveEvent clickComment = new SingleLiveEvent<>();
        public SingleLiveEvent clickImage = new SingleLiveEvent<>();
        //        public SingleLiveEvent clickIisDelete = new SingleLiveEvent<>();
        public SingleLiveEvent<Void> playVideoView = new SingleLiveEvent();
    }
}