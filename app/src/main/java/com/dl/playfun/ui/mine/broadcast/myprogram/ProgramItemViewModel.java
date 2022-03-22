package com.dl.playfun.ui.mine.broadcast.myprogram;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.entity.BaseUserBeanEntity;
import com.dl.playfun.entity.BroadcastBeanEntity;
import com.dl.playfun.entity.BroadcastEntity;
import com.dl.playfun.entity.CommentEntity;
import com.dl.playfun.entity.GiveUserBeanEntity;
import com.dl.playfun.entity.TopicalListEntity;
import com.dl.playfun.event.ZoomInPictureEvent;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.mine.broadcast.myall.MyAllBroadcastViewModel;
import com.dl.playfun.ui.mine.broadcast.mytrends.CommentItemViewModel;
import com.dl.playfun.ui.mine.broadcast.mytrends.HeadItemViewModel;
import com.dl.playfun.ui.mine.broadcast.mytrends.ImageItemViewModel;
import com.dl.playfun.utils.ExceptionReportUtils;
import com.dl.playfun.utils.ListUtils;
import com.dl.playfun.utils.SystemDictUtils;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.ui.program.programdetail.ProgramDetailFragment;
import com.dl.playfun.ui.program.programdetail.ProgramDetailViewModel;
import com.dl.playfun.ui.radio.radiohome.RadioViewModel;
import com.dl.playfun.ui.userdetail.detail.UserDetailFragment;
import com.dl.playfun.ui.userdetail.locationmaps.LocationMapsFragment;
import com.shuyu.gsyvideoplayer.GSYVideoManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.goldze.mvvmhabit.base.MultiItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.binding.command.BindingConsumer;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.goldze.mvvmhabit.utils.Utils;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

import static com.blankj.utilcode.util.ColorUtils.getColor;
import static com.dl.playfun.ui.radio.radiohome.RadioViewModel.RadioRecycleType_Topical;

/**
 * @author wulei
 */
public class ProgramItemViewModel extends MultiItemViewModel<BaseViewModel> {
    //    public boolean isVip;
//    public int sex;
    public int userId;
    public String avatar;
    public ObservableField<TopicalListEntity> topicalListEntityObservableField = new ObservableField<>();
    public ObservableField<Boolean> isDetail = new ObservableField<>(false);
    public ObservableField<Boolean> isProgramDetail = new ObservableField<>(false);
    public ObservableField<String> singStr = new ObservableField<>("0");
    public boolean isSelf = false;
    public boolean pIdWy = false;
    public ObservableField<Boolean> isShowComment = new ObservableField<>(false);
    public ObservableField<Integer> colorval = new ObservableField<>(getColor(R.color.gray_dark));
    //    public ObservableField<String> positonStr = new ObservableField<>();
    public ObservableField<Integer> pointPositon = new ObservableField<>(0);
    //ViewPager切换监听
    public BindingCommand<Integer> onPageSelectedCommand = new BindingCommand<>(new BindingConsumer<Integer>() {
        @Override
        public void call(Integer index) {
            pointPositon.set(index);
//            positonStr.set(String.format("%s/%s", index + 1, topicalListEntityObservableField.get().getImages().size()));
        }
    });
    // 点赞
    public ObservableList<HeadItemViewModel> itemList = new ObservableArrayList<>();
    public ItemBinding<HeadItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_head);
    // 暂时无用
    public ObservableList<ImageItemViewModel> imageItemList = new ObservableArrayList<>();
    public ItemBinding<ImageItemViewModel> imageItemBinding = ItemBinding.of(BR.viewModel, R.layout.item_image);
    // 评论
    public ObservableList<CommentItemViewModel> commentItemList = new ObservableArrayList<>();
    public ItemBinding<CommentItemViewModel> commentItemBinding = ItemBinding.of(BR.viewModel, R.layout.item_comment);
    //更多的点击事件
    public BindingCommand moreClick = new BindingCommand(() -> {
        try {
            if (viewModel instanceof MyprogramViewModel) {
                int position = ((MyprogramViewModel) viewModel).observableList.indexOf(ProgramItemViewModel.this);
                ((MyprogramViewModel) viewModel).uc.clickMore.setValue(position);
            } else if (viewModel instanceof RadioViewModel) {
                int position = ((RadioViewModel) viewModel).radioItems.indexOf(ProgramItemViewModel.this);
                Map<String, String> data = new HashMap<>();
                data.put("position", String.valueOf(position));
                data.put("type", RadioRecycleType_Topical);
                data.put("broadcastId", String.valueOf(topicalListEntityObservableField.get().getBroadcast().getId()));
                ((RadioViewModel) viewModel).radioUC.clickMore.setValue(data);
            } else if (viewModel instanceof ProgramDetailViewModel) {
                Map<String, String> data = new HashMap<>();
                data.put("broadcastId", String.valueOf(topicalListEntityObservableField.get().getBroadcast().getId()));
                ((ProgramDetailViewModel) viewModel).uc.clickMore.setValue(data);
            } else if (viewModel instanceof MyAllBroadcastViewModel) {
                int position = ((MyAllBroadcastViewModel) viewModel).observableList.indexOf(ProgramItemViewModel.this);
                Map<String, String> data = new HashMap<>();
                data.put("position", String.valueOf(position));
                data.put("type", RadioRecycleType_Topical);
                data.put("broadcastId", String.valueOf(topicalListEntityObservableField.get().getBroadcast().getId()));
                ((MyAllBroadcastViewModel) viewModel).uc.clickMore.setValue(data);
            }
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });
    //点赞点击事件
    public BindingCommand likeClick = new BindingCommand(() -> {
        try {
            if (viewModel instanceof MyprogramViewModel) {
                int position = ((MyprogramViewModel) viewModel).observableList.indexOf(ProgramItemViewModel.this);
                ((MyprogramViewModel) viewModel).uc.clickLike.setValue(position);
            } else if (viewModel instanceof RadioViewModel) {
                int position = ((RadioViewModel) viewModel).radioItems.indexOf(ProgramItemViewModel.this);
                Map<String, String> data = new HashMap<>();
                data.put("position", String.valueOf(position));
                data.put("type", RadioRecycleType_Topical);
                ((RadioViewModel) viewModel).radioUC.clickLike.setValue(data);
            } else if (viewModel instanceof ProgramDetailViewModel) {
                ((ProgramDetailViewModel) viewModel).uc.clickLike.call();
            } else if (viewModel instanceof MyAllBroadcastViewModel) {
                int position = ((MyAllBroadcastViewModel) viewModel).observableList.indexOf(ProgramItemViewModel.this);
                Map<String, String> data = new HashMap<>();
                data.put("position", String.valueOf(position));
                data.put("type", RadioRecycleType_Topical);
                ((MyAllBroadcastViewModel) viewModel).uc.clickLike.setValue(data);
            }
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });
    //评论点击事件
    public BindingCommand commentClick = new BindingCommand(() -> {
        try {
            int sex = AppContext.instance().appRepository.readUserData().getSex();
            if (topicalListEntityObservableField.get().getBroadcast().getIsComment() == 1) {
                ToastUtils.showShort(R.string.playfun_comment_close);
                return;
            }
            if (topicalListEntityObservableField.get().getIsEnd() == 1) {
                ToastUtils.showShort(R.string.playfun_program_over);
                return;
            }
            if (viewModel instanceof MyprogramViewModel) {
                if (((MyprogramViewModel) viewModel).userId == topicalListEntityObservableField.get().getUser().getId()) {
                    ToastUtils.showShort(R.string.playfun_self_ont_comment_broadcast);
                } else {
                    Map<String, String> data = new HashMap<>();
                    data.put("id", String.valueOf(topicalListEntityObservableField.get().getId()));
                    data.put("toUseriD", null);
                    data.put("toUserName", null);
                    ((MyprogramViewModel) viewModel).uc.clickComment.setValue(data);
                }
            } else if (viewModel instanceof RadioViewModel) {
                if (((RadioViewModel) viewModel).userId == topicalListEntityObservableField.get().getUser().getId()) {
                    ToastUtils.showShort(R.string.playfun_self_ont_comment_broadcast);
                } else {
                    Map<String, String> data = new HashMap<>();
                    data.put("id", String.valueOf(topicalListEntityObservableField.get().getId()));
                    data.put("toUseriD", null);
                    data.put("type", RadioRecycleType_Topical);
                    data.put("toUserName", null);
                    ((RadioViewModel) viewModel).radioUC.clickComment.setValue(data);
                }
            } else if (viewModel instanceof ProgramDetailViewModel) {
                if (((ProgramDetailViewModel) viewModel).userId == topicalListEntityObservableField.get().getUser().getId()) {
                    ToastUtils.showShort(R.string.playfun_self_ont_comment_broadcast);
                } else {
                    Map<String, String> data = new HashMap<>();
                    data.put("id", String.valueOf(topicalListEntityObservableField.get().getId()));
                    data.put("toUseriD", null);
                    data.put("toUserName", null);
                    ((ProgramDetailViewModel) viewModel).uc.clickComment.setValue(data);
                }
            } else if (viewModel instanceof MyAllBroadcastViewModel) {
                if (((MyAllBroadcastViewModel) viewModel).userId == topicalListEntityObservableField.get().getUser().getId()) {
                    ToastUtils.showShort(R.string.playfun_self_ont_comment_broadcast);
                } else {
                    Map<String, String> data = new HashMap<>();
                    data.put("id", String.valueOf(topicalListEntityObservableField.get().getId()));
                    data.put("toUseriD", null);
                    data.put("toUserName", null);
                    ((MyAllBroadcastViewModel) viewModel).uc.clickComment.setValue(data);
                }
            }
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });
    //结束报名点击事件
    public BindingCommand signUpClick = new BindingCommand(() -> {
        try {
            if (topicalListEntityObservableField.get().getIsEnd() == 1) {
                ToastUtils.showShort(R.string.playfun_program_over);
                return;
            }
            if (viewModel instanceof MyprogramViewModel) {
                int position = ((MyprogramViewModel) viewModel).observableList.indexOf(ProgramItemViewModel.this);
                ((MyprogramViewModel) viewModel).uc.clickSignUp.setValue(position);
            } else if (viewModel instanceof RadioViewModel) {
                int position = ((RadioViewModel) viewModel).radioItems.indexOf(ProgramItemViewModel.this);
                ((RadioViewModel) viewModel).radioUC.clickSignUp.setValue(position);
            } else if (viewModel instanceof ProgramDetailViewModel) {
                int position = ((ProgramDetailViewModel) viewModel).observableList.indexOf(ProgramItemViewModel.this);
                ((ProgramDetailViewModel) viewModel).uc.clickSignUp.setValue(position);
            } else if (viewModel instanceof MyAllBroadcastViewModel) {
                int position = ((MyAllBroadcastViewModel) viewModel).observableList.indexOf(ProgramItemViewModel.this);
                ((MyAllBroadcastViewModel) viewModel).uc.clickSignUp.setValue(position);
            }
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });
    //查看报名点击事件
    public BindingCommand checkClick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            try {
                if (userId == topicalListEntityObservableField.get().getUser().getId() && !isDetail.get()) {
                    Bundle bundle = ProgramDetailFragment.getStartBundle(topicalListEntityObservableField.get().getId());
                    viewModel.start(ProgramDetailFragment.class.getCanonicalName(), bundle);
                } else {
                    if (topicalListEntityObservableField.get().getIsSign() == 1) {
                        ToastUtils.showShort(R.string.playfun_you_have_signed_up);
                        return;
                    }
                    if (topicalListEntityObservableField.get().getIsEnd() == 1) {
                        ToastUtils.showShort(R.string.playfun_program_over);
                        return;
                    }
                    int sex = AppContext.instance().appRepository.readUserData().getSex();
                    if (topicalListEntityObservableField.get().getUser().getSex() == sex) {
                        if (sex == AppConfig.MALE) {
                            ToastUtils.showShort(R.string.playfun_warn_male_not_sign_up_program);
                        } else {
                            ToastUtils.showShort(R.string.playfun_warn_female_not_sign_up_program);
                        }
                        return;
                    }

                    if (viewModel instanceof RadioViewModel) {
                        int position = ((RadioViewModel) viewModel).radioItems.indexOf(ProgramItemViewModel.this);
                        ((RadioViewModel) viewModel).radioUC.clickCheck.setValue(position);
                    } else if (viewModel instanceof ProgramDetailViewModel) {
                        int position = ((ProgramDetailViewModel) viewModel).observableList.indexOf(ProgramItemViewModel.this);
                        ((ProgramDetailViewModel) viewModel).uc.clickCheck.setValue(position);
                    } else if (viewModel instanceof MyAllBroadcastViewModel) {
                        int position = ((MyAllBroadcastViewModel) viewModel).observableList.indexOf(ProgramItemViewModel.this);
                        ((MyAllBroadcastViewModel) viewModel).uc.clickCheck.setValue(position);
                    }
                }

            } catch (Exception e) {
                ExceptionReportUtils.report(e);
            }
        }
    });
    //导航点击事件
    public BindingCommand addressClick = new BindingCommand(() -> {
        try {
            if (!topicalListEntityObservableField.get().getAddressName().equals(StringUtils.getString(R.string.playfun_determined))) {
                Bundle bundle = LocationMapsFragment.getStartBundle(topicalListEntityObservableField.get().getAddressName(),
                        topicalListEntityObservableField.get().getAddress(), topicalListEntityObservableField.get().getLatitude(),
                        topicalListEntityObservableField.get().getLongitude());
                viewModel.start(LocationMapsFragment.class.getCanonicalName(), bundle);
            }
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });
    //头像的点击事件
    public BindingCommand avatarClick = new BindingCommand(() -> {
        try {
            AppContext.instance().logEvent(AppsFlyerEvent.User_Page_2);
            Bundle bundle = UserDetailFragment.getStartBundle(topicalListEntityObservableField.get().getUser().getId());
            viewModel.start(UserDetailFragment.class.getCanonicalName(), bundle);
            GSYVideoManager.releaseAllVideos();
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });
    public BindingCommand imageClick = new BindingCommand(() -> {
        //放大图片
        if (!ListUtils.isEmpty(topicalListEntityObservableField.get().getImages())) {
            RxBus.getDefault().post(new ZoomInPictureEvent(topicalListEntityObservableField.get().getImages().get(0)));
        } else {
            RxBus.getDefault().post(new ZoomInPictureEvent(topicalListEntityObservableField.get().getUser().getAvatar()));
        }
    });

    // 节目详细
    public BindingCommand detailClick = new BindingCommand(() -> {
        try {
            if (!(viewModel instanceof ProgramDetailViewModel)) {
                Bundle bundle = ProgramDetailFragment.getStartBundle(topicalListEntityObservableField.get().getId());
                viewModel.start(ProgramDetailFragment.class.getCanonicalName(), bundle);
            } else {
                avatarClick.execute();
            }
            GSYVideoManager.releaseAllVideos();
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });
    //节目点击播放视频
    public BindingCommand playVideo = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            try {
                int position = ((ProgramDetailViewModel) viewModel).observableList.indexOf(ProgramItemViewModel.this);
                Map<String, String> data = new HashMap<>();
                data.put("position", String.valueOf(position));
                data.put("type", RadioRecycleType_Topical);
                data.put("videoSrc", topicalListEntityObservableField.get().getVideo());
                data.put("broadcastId", String.valueOf(topicalListEntityObservableField.get().getBroadcast().getId()));
                ((ProgramDetailViewModel) viewModel).uc.clickPlayersVideo.setValue(data);
            } catch (Exception e) {
            }
        }
    });

    public ProgramItemViewModel(@NonNull BaseViewModel viewModel, TopicalListEntity topicalListEntity) {
        super(viewModel);
        pIdWy = ConfigManager.getInstance().getUserParentId() != null && topicalListEntity.getUserId() != 0 && topicalListEntity.getUserId() == ConfigManager.getInstance().getUserParentId().intValue();
        topicalListEntity.setpId(ConfigManager.getInstance().getUserParentId());
        this.topicalListEntityObservableField.set(topicalListEntity);
        if (viewModel instanceof MyprogramViewModel) {
            isSelf = true;
        }
        init();
    }

    public ProgramItemViewModel(@NonNull BaseViewModel viewModel, BroadcastEntity broadcastEntity) {
        super(viewModel);
        TopicalListEntity topicalListEntity = new TopicalListEntity();
        topicalListEntity.setId(broadcastEntity.getTopical().getId());
        topicalListEntity.setCreatedAt(broadcastEntity.getCreatedAt());
        topicalListEntity.setIsGive(broadcastEntity.getTopical().getIsGive());
        topicalListEntity.setComment(broadcastEntity.getTopical().getComment());
        topicalListEntity.setHopeObject(broadcastEntity.getTopical().getHopeObject());
        topicalListEntity.setAddress(broadcastEntity.getTopical().getAddress());
        topicalListEntity.setThemeId(broadcastEntity.getTopical().getThemeId());
        topicalListEntity.setStartDate(broadcastEntity.getTopical().getStartDate());
        topicalListEntity.setEndTime(broadcastEntity.getTopical().getEndTime());
        topicalListEntity.setUserId(broadcastEntity.getUserId());
        topicalListEntity.setSignCount(broadcastEntity.getTopical().getSignCount());
        topicalListEntity.setIsSign(broadcastEntity.getTopical().getIsSign());
        topicalListEntity.setCityId(broadcastEntity.getTopical().getCityId());
        topicalListEntity.setAddressName(broadcastEntity.getTopical().getAddressName());
        topicalListEntity.setLatitude(broadcastEntity.getTopical().getLatitude());
        topicalListEntity.setLongitude(broadcastEntity.getTopical().getLongitude());
        topicalListEntity.setIsEnd(broadcastEntity.getTopical().getIsEnd());
        topicalListEntity.setDescribe(broadcastEntity.getTopical().getDescribe());
        topicalListEntity.setVideo(broadcastEntity.getTopical().getVideo());
        topicalListEntity.setCommentNumber(broadcastEntity.getTopical().getCommentNumber());
        BaseUserBeanEntity userBean = new BaseUserBeanEntity();
        userBean.setAvatar(broadcastEntity.getAvatar());
        userBean.setId(broadcastEntity.getUserId());
        userBean.setIsVip(broadcastEntity.getIsVip());
        userBean.setNickname(broadcastEntity.getNickname());
        userBean.setSex(broadcastEntity.getSex());
        userBean.setCertification(broadcastEntity.getCertification());
        topicalListEntity.setUser(userBean);
        topicalListEntity.setContent(broadcastEntity.getTopical().getContent());
        if (broadcastEntity.getTopical().getGiveUserBean() != null) {
            List<GiveUserBeanEntity> giveUserBeanList = new ArrayList<>();
            for (int i = 0; i < broadcastEntity.getTopical().getGiveUserBean().size(); i++) {
                giveUserBeanList.add(new GiveUserBeanEntity(broadcastEntity.getTopical().getGiveUserBean().get(i).getIdX(), broadcastEntity.getTopical().getGiveUserBean().get(i).getAvatar()));
            }
            topicalListEntity.setGive_user(giveUserBeanList);
        }

        BroadcastBeanEntity broadcastBean = new BroadcastBeanEntity();
        broadcastBean.setId(broadcastEntity.getId());
        broadcastBean.setIsComment(broadcastEntity.getIsComment());
        broadcastBean.setGiveCount(broadcastEntity.getGiveCount());
        topicalListEntity.setBroadcast(broadcastBean);

        topicalListEntity.setImages(broadcastEntity.getTopical().getImages());
        topicalListEntity.setpId(ConfigManager.getInstance().getUserParentId());
        pIdWy = ConfigManager.getInstance().getUserParentId() != null && topicalListEntity.getUserId() != 0 && topicalListEntity.getUserId() == ConfigManager.getInstance().getUserParentId().intValue();
        topicalListEntityObservableField.set(topicalListEntity);
        init();
    }

    private void init() {
        if (viewModel instanceof MyprogramViewModel) {
//            isVip = ((MyprogramViewModel) viewModel).isVip;
//            sex = ((MyprogramViewModel) viewModel).sex;
            userId = ((MyprogramViewModel) viewModel).userId;
            avatar = ((MyprogramViewModel) viewModel).avatar;
        } else if (viewModel instanceof RadioViewModel) {
//            isVip = ((RadioViewModel) viewModel).isVip;
//            sex = ((RadioViewModel) viewModel).sex;
            userId = ((RadioViewModel) viewModel).userId;
            avatar = ((RadioViewModel) viewModel).avatar;
        } else if (viewModel instanceof ProgramDetailViewModel) {
//            isVip = ((ProgramDetailViewModel) viewModel).isVip;
//            sex = ((ProgramDetailViewModel) viewModel).sex;
            userId = ((ProgramDetailViewModel) viewModel).userId;
            avatar = ((ProgramDetailViewModel) viewModel).avatar;
            isProgramDetail.set(true);
        } else if (viewModel instanceof MyAllBroadcastViewModel) {
//            isVip = ((ProgramListViewModel) viewModel).isVip;
//            sex = ((ProgramListViewModel) viewModel).sex;
            userId = ((MyAllBroadcastViewModel) viewModel).userId;
            avatar = ((MyAllBroadcastViewModel) viewModel).avatar;
        }

        if (userId == topicalListEntityObservableField.get().getUser().getId()) {
            singStr.set(topicalListEntityObservableField.get().getSignCount() + "");
            colorval.set(getColor(R.color.gray_dark));
            if (viewModel instanceof ProgramDetailViewModel) {
                isDetail.set(true);
            }
        } else {
            isDetail.set(false);
            if (topicalListEntityObservableField.get().getIsSign() == 1) {
                singStr.set(topicalListEntityObservableField.get().getSignCount() + "");
                colorval.set(getColor(R.color.purple));
            } else {
                singStr.set(topicalListEntityObservableField.get().getSignCount() + "");
                colorval.set(getColor(R.color.gray_dark));
            }

        }

//        if (topicalListEntityObservableField.get().getImages() != null) {
//            positonStr.set(String.format("%s/%s", 1, topicalListEntityObservableField.get().getImages().size()));
//            for (int i = 0; i < topicalListEntityObservableField.get().getImages().size(); i++) {
//                ImageItemViewModel ivItem = new ImageItemViewModel(viewModel, topicalListEntityObservableField.get().getImages().get(i), i, topicalListEntityObservableField.get().getImages());
//                imageItemList.add(ivItem);
//            }
//        }

        if (topicalListEntityObservableField.get().getGive_user() != null) {
            for (int i = 0; i < topicalListEntityObservableField.get().getGive_user().size(); i++) {
                if (i < 13) {
                    HeadItemViewModel item = new HeadItemViewModel(viewModel, topicalListEntityObservableField.get().getGive_user().get(i).getAvatar(),
                            topicalListEntityObservableField.get().getGive_user().get(i).getId(),
                            topicalListEntityObservableField.get().getGive_user().get(i).getSex(),
                            0,
                            HeadItemViewModel.Type_Topical, topicalListEntityObservableField.get().getId()
                    );
                    itemList.add(item);
                } else if (i == 13) {
                    HeadItemViewModel item = new HeadItemViewModel(viewModel, topicalListEntityObservableField.get().getGive_user().get(i).getAvatar(),
                            topicalListEntityObservableField.get().getGive_user().get(i).getId(),
                            topicalListEntityObservableField.get().getGive_user().get(i).getSex(),
                            topicalListEntityObservableField.get().getGiveCount() - 14,
                            HeadItemViewModel.Type_Topical, topicalListEntityObservableField.get().getId()
                    );
                    itemList.add(item);
                }
            }
        }

        if (topicalListEntityObservableField.get().getComment() != null) {
            for (int i = 0; i < topicalListEntityObservableField.get().getComment().size(); i++) {
                if (isProgramDetail.get()) {
                    CommentItemViewModel commentItemViewModel = new CommentItemViewModel(viewModel, topicalListEntityObservableField.get().getComment().get(i),
                            topicalListEntityObservableField.get().getId(), RadioRecycleType_Topical,
                            topicalListEntityObservableField.get().getUser().getId() == userId, false);
                    commentItemList.add(commentItemViewModel);
                } else {
                    if (i < 5) {
                        CommentItemViewModel commentItemViewModel = new CommentItemViewModel(viewModel, topicalListEntityObservableField.get().getComment().get(i),
                                topicalListEntityObservableField.get().getId(), RadioRecycleType_Topical,
                                topicalListEntityObservableField.get().getUser().getId() == userId, false);
                        commentItemList.add(commentItemViewModel);
                    }
                }

            }
            if (topicalListEntityObservableField.get().getComment().size() > 5 && !isProgramDetail.get()) {
                CommentItemViewModel commentItemViewModel = new CommentItemViewModel(viewModel, topicalListEntityObservableField.get().getComment().get(0),
                        topicalListEntityObservableField.get().getId(), RadioRecycleType_Topical,
                        topicalListEntityObservableField.get().getUser().getId() == userId, true);
                commentItemList.add(commentItemViewModel);
            }
        }
        if (ListUtils.isEmpty(topicalListEntityObservableField.get().getComment())) {
            isShowComment.set(false);
        } else {
            isShowComment.set(true);
        }

    }

    public void addGiveUser() {
        if (topicalListEntityObservableField.get().getGive_user() == null) {
            topicalListEntityObservableField.get().setGive_user(new ArrayList<>());
        }
        GiveUserBeanEntity giveUserBeanEntity = new GiveUserBeanEntity(userId, avatar);
        topicalListEntityObservableField.get().getGive_user().add(giveUserBeanEntity);
        topicalListEntityObservableField.get().setGiveSize(topicalListEntityObservableField.get().getGive_user().size());
        topicalListEntityObservableField.get().setIsGive(1);
        topicalListEntityObservableField.get().getBroadcast().setGiveCount(topicalListEntityObservableField.get().getBroadcast().getGiveCount() + 1);

        if (topicalListEntityObservableField.get().getGiveCount() < 13) {
            HeadItemViewModel item = new HeadItemViewModel(viewModel, avatar, userId,
                    AppContext.instance().appRepository.readUserData().getSex(),
                    0,
                    HeadItemViewModel.Type_New, topicalListEntityObservableField.get().getId()
            );
            itemList.add(item);
        }
    }

    public void addComment(Integer id, String content, Integer toUserId, String toUserName, String userName) {
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setContent(content);
        commentEntity.setId(id);
        commentEntity.setUserId(userId);
        CommentEntity.UserBean userBean = new CommentEntity.UserBean();
        userBean.setId(userId);
        userBean.setNickname(userName);
        commentEntity.setUser(userBean);
        if (toUserName != null) {
            CommentEntity.TouserBean touserBean = new CommentEntity.TouserBean();
            touserBean.setId(toUserId);
            touserBean.setNickname(toUserName);
            commentEntity.setTouser(touserBean);
        }
        if (ListUtils.isEmpty(topicalListEntityObservableField.get().getComment())) {
            List<CommentEntity> commentEntities = new ArrayList<>();
            commentEntities.add(commentEntity);
            topicalListEntityObservableField.get().setComment(commentEntities);
        } else {
            topicalListEntityObservableField.get().getComment().add(commentEntity);
        }

        if (topicalListEntityObservableField.get().getComment().size() > 5 && !isProgramDetail.get()) {
            if (topicalListEntityObservableField.get().getComment().size() < 7) {
                CommentItemViewModel commentItemViewModel = new CommentItemViewModel(viewModel, topicalListEntityObservableField.get().getComment().get(0),
                        topicalListEntityObservableField.get().getId(), RadioRecycleType_Topical,
                        topicalListEntityObservableField.get().getUser().getId() == userId, true);
                commentItemList.add(commentItemViewModel);
            }
        } else {
            CommentItemViewModel commentItemViewModel = new CommentItemViewModel(viewModel, commentEntity, topicalListEntityObservableField.get().getId(),
                    RadioRecycleType_Topical, topicalListEntityObservableField.get().getUser().getId() == userId, false);
            commentItemList.add(commentItemViewModel);
        }
        if (ListUtils.isEmpty(topicalListEntityObservableField.get().getComment())) {
            isShowComment.set(false);
        } else {
            isShowComment.set(true);
        }
    }

    public void report() {
        topicalListEntityObservableField.get().setIsSign(1);
        topicalListEntityObservableField.get().setSignCount(topicalListEntityObservableField.get().getSignCount() + 1);
        singStr.set(topicalListEntityObservableField.get().getSignCount() + "");
        colorval.set(getColor(R.color.purple));
    }

    public String getProgramName() {
        String programName = "";
        programName = SystemDictUtils.getProgramThemeById(topicalListEntityObservableField.get().getThemeId());
        return programName;
    }

    public Integer getIseNdHintShow(Integer sign) {
        if (topicalListEntityObservableField.get() == null || sign == null) {
            return View.INVISIBLE;
        }
        //我的动态
        if (sign == 1) {
            return View.INVISIBLE;
        } else if (userId == topicalListEntityObservableField.get().getUser().getId()) {
            return View.INVISIBLE;
        } else if (topicalListEntityObservableField.get().getIsEnd() == 1) {
            return View.INVISIBLE;
        } else {
            return View.VISIBLE;
        }
    }

    //约会报名显示
    public Integer getIsDatingShow() {
        if (topicalListEntityObservableField.get() == null) {
            return View.GONE;
        }
        if (userId == topicalListEntityObservableField.get().getUser().getId()) {
            return View.GONE;
        } else if (topicalListEntityObservableField.get().getIsEnd() == 1) {
            return View.GONE;
        } else {
            return View.VISIBLE;
        }
    }

    public Drawable getIsDatingShowImg(Integer sign) {
        if (topicalListEntityObservableField.get() == null) {
            return Utils.getContext().getDrawable(R.drawable.icon_radio_item_apply_ed);
        }
        if (sign == 1) {//已经报名
            return Utils.getContext().getDrawable(R.drawable.icon_radio_item_apply_no);
        } else {
            return Utils.getContext().getDrawable(R.drawable.icon_radio_item_apply_ed);
        }
    }

    //约会结束按钮显示
    public Integer getIsDatingEndShow(Integer isEnd) {
        if (topicalListEntityObservableField.get() == null) {
            return View.GONE;
        }
        if (userId == topicalListEntityObservableField.get().getUser().getId() && isEnd == 0) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    //约会已经结束展示
    public Integer getIsDatingEnd(Integer isEnd) {
        if (topicalListEntityObservableField.get() == null) {
            return View.GONE;
        }
        if (isEnd == 1) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    public Integer getPosition() {
        int position = 0;
        if (viewModel instanceof MyprogramViewModel) {
            position = ((MyprogramViewModel) viewModel).observableList.indexOf(ProgramItemViewModel.this);
        } else if (viewModel instanceof RadioViewModel) {
            position = ((RadioViewModel) viewModel).radioItems.indexOf(ProgramItemViewModel.this);
        } else if (viewModel instanceof ProgramDetailViewModel) {
            position = ((ProgramDetailViewModel) viewModel).observableList.indexOf(ProgramItemViewModel.this);
        } else if (viewModel instanceof MyAllBroadcastViewModel) {
            position = ((MyAllBroadcastViewModel) viewModel).observableList.indexOf(ProgramItemViewModel.this);
        }
        return position;
    }


}
