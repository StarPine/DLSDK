package com.dl.playfun.ui.radio.radiohome;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.AdItemEntity;
import com.dl.playfun.entity.BroadcastEntity;
import com.dl.playfun.entity.BroadcastListEntity;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.entity.EjectEntity;
import com.dl.playfun.entity.EjectSignInEntity;
import com.dl.playfun.entity.MessageTagEntity;
import com.dl.playfun.entity.SystemConfigEntity;
import com.dl.playfun.entity.SystemRoleMoneyConfigEntity;
import com.dl.playfun.entity.ThemeAdEntity;
import com.dl.playfun.entity.ThemeItemEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.event.BadioEvent;
import com.dl.playfun.event.CountDownTimerEvent;
import com.dl.playfun.event.LikeChangeEvent;
import com.dl.playfun.event.MessageTagEvent;
import com.dl.playfun.event.RadioadetailEvent;
import com.dl.playfun.event.TaskListEvent;
import com.dl.playfun.event.TaskMainTabEvent;
import com.dl.playfun.event.TaskTypeStatusEvent;
import com.dl.playfun.event.UMengCustomEvent;
import com.dl.playfun.event.UserUpdateVipEvent;
import com.dl.playfun.helper.JumpHelper;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.mine.broadcast.myprogram.ProgramItemViewModel;
import com.dl.playfun.ui.mine.broadcast.mytrends.TrendItemViewModel;
import com.dl.playfun.ui.radio.issuanceprogram.IssuanceProgramFragment;
import com.dl.playfun.ui.radio.programlist.ProgramListFragment;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.utils.ExceptionReportUtils;
import com.dl.playfun.utils.FileUploadUtils;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.utils.Utils;
import com.dl.playfun.viewmodel.BaseRefreshViewModel;
import com.shuyu.gsyvideoplayer.GSYVideoManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.base.MultiItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.RxSubscriptions;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.ItemBinding;
import me.tatarka.bindingcollectionadapter2.OnItemBind;

/**
 * @author wulei
 */
public class RadioViewModel extends BaseRefreshViewModel<AppRepository> {
    //vip充值成功回调
    public boolean EventVipSuccess = false;
    public static Integer SignWinningDay = -1;
    public static final String RadioRecycleType_New = "new";
    public static final String RadioRecycleType_Topical = "topical";
    public static final String RadioRecycleType_trace = "emptyTrace";
    private static final String TAG = "签到领取会员";
    private final int consumeImmediately = 0;
    private final Integer pay_good_day = 7;
    //推荐用户弹窗
    //推荐用户弹窗
    public ObservableField<Boolean> isShowMessageTag = new ObservableField<>(false);
    public ObservableField<MessageTagEntity> messageTagEntity = new ObservableField<>();
    public ObservableField<String> countDownTimerUi = new ObservableField<>();
    public int userId;
    public String avatar;
    public UIChangeObservable radioUC = new UIChangeObservable();
    public Integer type = 1;
    public Integer cityId = null;
    public Integer sexId = null;
    public Integer IsCollect = 1;
    public boolean CollectFlag = false;
    public Integer certification = null;
    public boolean collectReLoad = false;
    public ObservableField<List<AdItemEntity>> adItemEntityObservableField = new ObservableField<>(new ArrayList<>());

    public List<ThemeItemEntity> themes = null;
    /*谷歌支付*/
    public BillingClient billingClient;
    public SkuDetails goodSkuDetails = null;
    public SingleLiveEvent<String> clickPay = new SingleLiveEvent();
    public BindingRecyclerViewAdapter<MultiItemViewModel> adapter = new BindingRecyclerViewAdapter<>();
    public ObservableList<MultiItemViewModel> radioItems = new ObservableArrayList<>();
    public ItemBinding<MultiItemViewModel> radioItemBinding = ItemBinding.of(new OnItemBind<MultiItemViewModel>() {
        @Override
        public void onItemBind(ItemBinding itemBinding, int position, MultiItemViewModel item) {
            //通过item的类型, 动态设置Item加载的布局
            String itemType = (String) item.getItemType();
            if (RadioRecycleType_New.equals(itemType)) {
                //设置new
                itemBinding.set(BR.viewModel, R.layout.item_trend);
            } else if (RadioRecycleType_Topical.equals(itemType)) {
//                设置topical
                itemBinding.set(BR.viewModel, R.layout.item_program);
            } else if (RadioRecycleType_trace.equals(itemType)) {
                //设置看追踪列表为空
                itemBinding.set(BR.viewModel, R.layout.item_radio_trace_empty);
            }
        }
    });
    public BindingCommand hideMessageTagOnClick = new BindingCommand(() -> {
        isShowMessageTag.set(false);
        AppContext.instance().logEvent(AppsFlyerEvent.daily_recommend__close);
        RxBus.getDefault().post(new MessageTagEvent(null, false));
        ApiUitl.recommendMsg(3);
    });
    public BindingCommand toMessageTagUserOnClick = new BindingCommand(() -> {
        isShowMessageTag.set(false);
        ApiUitl.recommendMsg(2);
        radioUC.clickToMessageDetail.call();
    });
    /**
     * 发布按钮的点击事件
     */
    public BindingCommand publishOnClickCommand = new BindingCommand(() -> {
        AppContext.instance().logEvent(AppsFlyerEvent.Post1);
        RxBus.getDefault().post(new UMengCustomEvent(UMengCustomEvent.EVENT_BROADCAST_PUBLISH));
        AppContext.instance().logEvent(AppsFlyerEvent.Post_Dating);
        RxBus.getDefault().post(new UMengCustomEvent(UMengCustomEvent.EVENT_BROADCAST_PUBLISH_PROGRAM));
        radioUC.programSubject.call();
        //radioUC.clickPublish.call();
    });
    /*谷歌支付*/
    /**
     * 主题6的点击事件
     */
    public BindingCommand theme6OnClick = new BindingCommand(() -> {
        try {
            AppContext.instance().logEvent(AppsFlyerEvent.Dating_2);
            Bundle bundle = ProgramListFragment.getStartBundle(themes.get(1).getId(), themes.get(1).getThemeId(), themes.get(1).getTitle(), themes.get(1).getKeyWord());
            start(ProgramListFragment.class.getCanonicalName(), bundle);
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });
    /**
     * 主题6的点击事件
     */
    public BindingCommand theme7OnClick = new BindingCommand(() -> {
        try {
            AppContext.instance().logEvent(AppsFlyerEvent.Dating_3);
            Bundle bundle = ProgramListFragment.getStartBundle(themes.get(2).getId(), themes.get(2).getThemeId(), themes.get(2).getTitle(), themes.get(2).getKeyWord());
            start(ProgramListFragment.class.getCanonicalName(), bundle);
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });
    /**
     * 主题7的点击事件
     */
    public BindingCommand theme8OnClick = new BindingCommand(() -> {
        try {
            AppContext.instance().logEvent(AppsFlyerEvent.Dating_4);
            Bundle bundle = ProgramListFragment.getStartBundle(themes.get(3).getId(), themes.get(3).getThemeId(), themes.get(3).getTitle(), themes.get(3).getKeyWord());
            start(ProgramListFragment.class.getCanonicalName(), bundle);
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });
    /**
     * 主题8的点击事件
     */
    public BindingCommand theme9OnClick = new BindingCommand(() -> {
        try {
            AppContext.instance().logEvent(AppsFlyerEvent.Dating_5);
            Bundle bundle = ProgramListFragment.getStartBundle(themes.get(4).getId(), themes.get(4).getThemeId(), themes.get(4).getTitle(), themes.get(4).getKeyWord());
            start(ProgramListFragment.class.getCanonicalName(), bundle);
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });
    /**
     * 主题9的点击事件
     */
    public BindingCommand theme10OnClick = new BindingCommand(() -> {
        try {
            AppContext.instance().logEvent(AppsFlyerEvent.Dating_6);
            Bundle bundle = ProgramListFragment.getStartBundle(themes.get(5).getId(), themes.get(5).getThemeId(), themes.get(5).getTitle(), themes.get(5).getKeyWord());
            start(ProgramListFragment.class.getCanonicalName(), bundle);
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });
    //banner点击
    public BindingCommand<Integer> onBannerClickCommand = new BindingCommand<>(index -> {
        try {
            AdItemEntity adItemEntity = adItemEntityObservableField.get().get(index);
            AppContext.instance().logEvent("banner_" + adItemEntity.getId());
            if (!StringUtils.isEmpty(adItemEntityObservableField.get().get(index).getLink())) {
                try {
                    String link = String.valueOf(adItemEntityObservableField.get().get(index).getLink());
                    if (link != null && link.indexOf("theme") != -1) {//本次点击是主题
                        AppContext.instance().logEvent(AppsFlyerEvent.Dating_1);
                        String[] theme_detail = link.split("-");
                        int ids = Integer.parseInt(theme_detail[1]);
                        int themeId = Integer.parseInt(theme_detail[2]);
                        String themeName = theme_detail.length >= 2 ? theme_detail[3] : null;
                        String keyword = theme_detail.length >= 3 ? theme_detail[4] : null;
                        Bundle bundle = ProgramListFragment.getStartBundle(ids, themeId, themeName, keyword);
                        start(ProgramListFragment.class.getCanonicalName(), bundle);
                    } else {
                        JumpHelper.jump(RadioViewModel.this, adItemEntityObservableField.get().get(index).getLink());
                        //Bundle bundle = WebDetailFragment.getStartBundle(adItemEntityObservableField.get().get(index).getLink());
                        //  start(WebDetailFragment.class.getCanonicalName(), bundle);
                    }
                } catch (Exception e) {
                    ExceptionReportUtils.report(e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    });
    //消费者
    private Disposable MessageTagReceive, CountDownTimerReceive;
    private Integer default_sex = null;
    private Disposable badioEvent;
    private Disposable radioadetailEvent;
    private Disposable UserUpdateVipEvent, taskTypeStatusEvent;
    private String orderNumber = null;
    private String google_goods_id = null;
    private Disposable likeChangeEventDisposable;
    private boolean isFirstComment = false;
    private boolean isFirstLike = false;

    public RadioViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
        initUserDate();
    }

    public void initUserDate() {
        userId = model.readUserData().getId();
        avatar = model.readUserData().getAvatar();
        certification = model.readUserData().getCertification();
    }

    //跳转发布界面
    public BindingCommand toProgramVIew = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            start(IssuanceProgramFragment.class.getCanonicalName());
        }
    });


    @Override
    public void registerRxBus() {
        super.registerRxBus();
        //动态改变
        likeChangeEventDisposable = RxBus.getDefault().toObservable(LikeChangeEvent.class)
                .subscribe(event -> {
                    if (event != null) {
                        collectReLoad = true;
                    }
                });
        badioEvent = RxBus.getDefault().toObservable(BadioEvent.class)
                .subscribe(event -> {
                    currentPage = 1;
                    getBroadcast(1);
                });
        radioadetailEvent = RxBus.getDefault().toObservable(RadioadetailEvent.class)
                .subscribe(event -> {
                    for (int i = 0; i < radioItems.size(); i++) {
                        if (event.getRadioaType().equals(RadioRecycleType_Topical)) {
                            if (radioItems.get(i) instanceof ProgramItemViewModel) {
                                if (((ProgramItemViewModel) radioItems.get(i)).topicalListEntityObservableField.get().getId() == event.getId()) {
                                    switch (event.getType()) {//1:删除 2：评论关闭开启 3：报名成功 4：节目结束报名 5：评论  6：点赞
                                        case 1:
                                            radioItems.remove(i);
                                            break;
                                        case 2:
                                            ((ProgramItemViewModel) radioItems.get(i)).topicalListEntityObservableField.get().getBroadcast().setIsComment(event.isComment);
                                            break;
                                        case 3:
                                            ((ProgramItemViewModel) radioItems.get(i)).report();
                                            break;
                                        case 4:
                                            ((ProgramItemViewModel) radioItems.get(i)).topicalListEntityObservableField.get().setIsEnd(1);
                                            break;
                                        case 5:
                                            ((ProgramItemViewModel) radioItems.get(i)).addComment(event.getId(), event.content, event.toUserId, event.toUserName, model.readUserData().getNickname());
                                            break;
                                        case 6:
                                            ((ProgramItemViewModel) radioItems.get(i)).addGiveUser();
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
                        } else {
                            if (radioItems.get(i) instanceof TrendItemViewModel) {
                                if (((TrendItemViewModel) radioItems.get(i)).newsEntityObservableField.get().getId() == event.getId()) {
                                    switch (event.getType()) {//1:删除 2：评论关闭开启 3：报名 4：节目结束报名 5：评论  6：点赞
                                        case 1:
                                            radioItems.remove(i);
                                            break;
                                        case 2:
                                            ((TrendItemViewModel) radioItems.get(i)).newsEntityObservableField.get().getBroadcast().setIsComment(event.isComment);
                                            break;
                                        case 5:
                                            ((TrendItemViewModel) radioItems.get(i)).addComment(event.getId(), event.content, event.toUserId, event.toUserName, model.readUserData().getNickname());
                                            break;
                                        case 6:
                                            ((TrendItemViewModel) radioItems.get(i)).addGiveUser();
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
                        }
                    }
                });
        MessageTagReceive = RxBus.getDefault().toObservable(MessageTagEvent.class)
                .subscribe(event -> {
                    messageTagEntity.set(event.getMessageTagEntity());
                    isShowMessageTag.set(event.isShow());
                    if (event.isShow() == false) {
                        AppContext.instance().logEvent(AppsFlyerEvent.daily_recommend_click);
                        if (ApiUitl.issendMessageTag == false) {
                            ApiUitl.issendMessageTag = true;
                            String id = String.valueOf(model.readUserData().getId());
                            recommendMsg(id, String.valueOf(2), ConfigManager.getInstance().getRecommendTwoTime() * 1000);
                        }
                    }
                });
        CountDownTimerReceive = RxBus.getDefault().toObservable(CountDownTimerEvent.class)
                .subscribe(countDownTimerEvent -> {
                    countDownTimerUi.set(countDownTimerEvent.getText());
                });
        UserUpdateVipEvent = RxBus.getDefault().toObservable(com.dl.playfun.event.UserUpdateVipEvent.class)
                .subscribe(userUpdateVipEvent -> {
                    EventVipSuccess = true;
                });
        taskTypeStatusEvent = RxBus.getDefault().toObservable(TaskTypeStatusEvent.class)
                .subscribe(taskTypeStatusEvent -> {
                    isFirstComment = taskTypeStatusEvent.getDayCommentNews() == 0;
                    isFirstLike = taskTypeStatusEvent.getDayGiveNews() == 0;
                });
    }

    /**
     * @return void
     * @Desc TODO(跳转任务中心页面)
     * @author 彭石林
     * @parame []
     * @Date 2021/9/30
     */
    public void toTaskCenter() {
        RxBus.getDefault().post(new TaskMainTabEvent(false, true));
        //start(TaskCenterFragment.class.getCanonicalName());
    }

    private void recommendMsg(String userId, String num, long timeOut) {
        AppContext.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, String> maps = new HashMap<>();
                    maps.put("userId", userId);
                    maps.put("date", Utils.formatday.format(new Date()));
                    maps.put("num", num);
                    model.saveMessageTagUser(maps);
                    AppContext.instance().recommendMsg();
                    AppContext.sUiThreadHandler.removeCallbacks(this);
                } catch (Exception e) {
                    AppContext.sUiThreadHandler.removeCallbacks(this);
                }
            }
        }, timeOut);
    }

    @Override
    public void removeRxBus() {
        super.removeRxBus();
        RxSubscriptions.remove(likeChangeEventDisposable);
        RxSubscriptions.remove(badioEvent);
        RxSubscriptions.remove(radioadetailEvent);
        RxSubscriptions.remove(MessageTagReceive);
        RxSubscriptions.remove(CountDownTimerReceive);
        RxSubscriptions.remove(taskTypeStatusEvent);
    }

    //初始化
    public void loadHttpData() {
        super.onEnterAnimationEnd();
        loadDatas(1);
    }

    public void setType(Integer type) {
        this.type = type;
        CollectFlag = false;
        getBroadcast(1);
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
        CollectFlag = false;
        getBroadcast(1);
    }

    public void setSexId(Integer sexId) {
        this.sexId = sexId;
        this.IsCollect = 0;
        CollectFlag = false;
        getBroadcast(1);
    }

    public void setIsCollect(Integer collect) {
        this.IsCollect = collect;
        this.sexId = null;
        CollectFlag = false;
        getBroadcast(1);
    }

    @Override
    public void loadDatas(int page) {
        loadThemeAd();
        try {
            if (IsCollect == null) {
                if (default_sex == null) {
                    default_sex = 2;
                    IsCollect = 1;
                    sexId = null;
                }
            }
        } catch (Exception e) {

        }
        //用户新触发了追踪事件。并且离开页面前。页面保持选项在查看追踪的人m
        if (collectReLoad && CollectFlag) {
            setIsCollect(1);
        } else {
            if (page == 1 && CollectFlag && IsCollect == 0) {
                sexId = null;
                CollectFlag = false;
                IsCollect = 1;
            }
            getBroadcast(page);
        }
    }

    /**
     * 电台首页
     */
    private void getBroadcast(int page) {
        try {
            GSYVideoManager.releaseAllVideos();
        } catch (Exception e) {

        }
        model.getBroadcastHome(sexId, cityId, null, null, IsCollect, type, page)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<BroadcastListEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<BroadcastListEntity> response) {
                        if (!CollectFlag) {
                            if (page == 1) {
                                radioItems.clear();
                            }
                        }
                        if (response.getData() != null) {
                            //真人集合
                            int realIndex = 0;
                            List<BroadcastEntity> listReal = response.getData().getRealData();
                            //机器人集合
                            List<BroadcastEntity> listUntrue = response.getData().getUntrueData();
                            //是否有追踪的人
                            Integer collectListEmpty = response.getData().getIsCollect();
                            //开始遍历次数
                            int position = 0;
                            if (IsCollect == 1 && page == 1 && CollectFlag) {
                                radioItems.clear();
                                CollectFlag = true;
                                IsCollect = 0;
                                if (ConfigManager.getInstance() != null && ConfigManager.getInstance().isMale()) {
                                    sexId = 0;
                                } else {
                                    sexId = 1;
                                }
                                String emptyText = null;
                                if (radioItems == null || radioItems.size() == 0) {
                                    if (collectListEmpty != null && collectListEmpty >= 1) {//有追踪的人
                                        emptyText = StringUtils.getString(R.string.radio_list_trace_empty2);
                                    } else {//没有追踪的人
                                        emptyText = StringUtils.getString(R.string.radio_list_trace_empty);
                                    }
                                } else {
                                    emptyText = StringUtils.getString(R.string.radio_list_trace_empty2);
                                }
                                // 动态
                                RadioTraceEmptyItemViewModel trendItemViewModel = new RadioTraceEmptyItemViewModel(RadioViewModel.this, emptyText);
                                trendItemViewModel.multiItemType(RadioRecycleType_trace);
                                radioItems.add(trendItemViewModel);
                            }
                            //机器人不为空
                            if (!ObjectUtils.isEmpty(listUntrue) && listUntrue.size() > 0) {
                                for (BroadcastEntity broadcastEntity : listUntrue) {
                                    position++;
                                    if (broadcastEntity.getNews() != null) {
//                                动态
                                        TrendItemViewModel trendItemViewModel = new TrendItemViewModel(RadioViewModel.this, broadcastEntity);
                                        trendItemViewModel.multiItemType(RadioRecycleType_New);
                                        radioItems.add(trendItemViewModel);
                                    } else {
//                                节目
                                        ProgramItemViewModel programItemViewModel = new ProgramItemViewModel(RadioViewModel.this, broadcastEntity);
                                        programItemViewModel.multiItemType(RadioRecycleType_Topical);
                                        radioItems.add(programItemViewModel);
                                    }
                                    if (position % 2 == 0) {
                                        if (listReal.size() > realIndex + 1) {
                                            BroadcastEntity broadcastEntityReal = listReal.get(realIndex);
                                            if (broadcastEntityReal.getNews() != null) {
                                                //动态
                                                TrendItemViewModel trendItemViewModelReal = new TrendItemViewModel(RadioViewModel.this, broadcastEntityReal);
                                                trendItemViewModelReal.multiItemType(RadioRecycleType_New);
                                                radioItems.add(trendItemViewModelReal);
                                            } else {
                                                // 节目
                                                ProgramItemViewModel programItemViewModelReal = new ProgramItemViewModel(RadioViewModel.this, broadcastEntityReal);
                                                programItemViewModelReal.multiItemType(RadioRecycleType_Topical);
                                                radioItems.add(programItemViewModelReal);
                                            }
                                            realIndex++;
                                        }
                                    }
                                }
                                if (realIndex == 0 && listReal.size() > 1) {
                                    for (BroadcastEntity broadcastEntity : listReal) {
                                        if (broadcastEntity.getNews() != null) {
                                            // 动态
                                            TrendItemViewModel trendItemViewModel = new TrendItemViewModel(RadioViewModel.this, broadcastEntity);
                                            trendItemViewModel.multiItemType(RadioRecycleType_New);
                                            radioItems.add(trendItemViewModel);
                                        } else {
                                            //节目
                                            ProgramItemViewModel programItemViewModel = new ProgramItemViewModel(RadioViewModel.this, broadcastEntity);
                                            programItemViewModel.multiItemType(RadioRecycleType_Topical);
                                            radioItems.add(programItemViewModel);
                                        }
                                    }
                                }
                            } else {
                                //真人集合不为空
                                if (!ObjectUtils.isEmpty(listReal) && listReal.size() > 0) {
                                    for (BroadcastEntity broadcastEntity : listReal) {
                                        if (broadcastEntity.getNews() != null) {
                                            // 动态
                                            TrendItemViewModel trendItemViewModel = new TrendItemViewModel(RadioViewModel.this, broadcastEntity);
                                            trendItemViewModel.multiItemType(RadioRecycleType_New);
                                            radioItems.add(trendItemViewModel);
                                        } else {
                                            //节目
                                            ProgramItemViewModel programItemViewModel = new ProgramItemViewModel(RadioViewModel.this, broadcastEntity);
                                            programItemViewModel.multiItemType(RadioRecycleType_Topical);
                                            radioItems.add(programItemViewModel);
                                        }
                                    }
                                } else {
                                    if (IsCollect == 1 && !CollectFlag) {
                                        CollectFlag = true;
                                        IsCollect = 0;
                                        if (ConfigManager.getInstance() != null && ConfigManager.getInstance().isMale()) {
                                            sexId = 0;
                                        } else {
                                            sexId = 1;
                                        }
                                        String emptyText = null;
                                        if (radioItems == null || radioItems.size() == 0) {
                                            if (collectListEmpty != null && collectListEmpty >= 1) {//有追踪的人
                                                emptyText = StringUtils.getString(R.string.radio_list_trace_empty2);
                                            } else {//没有追踪的人
                                                emptyText = StringUtils.getString(R.string.radio_list_trace_empty);
                                            }
                                        } else {
                                            emptyText = StringUtils.getString(R.string.radio_list_trace_empty2);
                                        }
                                        // 动态
                                        RadioTraceEmptyItemViewModel trendItemViewModel = new RadioTraceEmptyItemViewModel(RadioViewModel.this, emptyText);
                                        trendItemViewModel.multiItemType(RadioRecycleType_trace);
                                        radioItems.add(trendItemViewModel);
                                        getBroadcast(1);
                                    }
                                }
                            }
                            if (IsCollect == 1 && !CollectFlag) {
                                int listRealSize = ObjectUtils.isEmpty(listReal) ? 0 : listReal.size();
                                int listUntrueSize = ObjectUtils.isEmpty(listUntrue) ? 0 : listUntrue.size();

                                if ((listRealSize + listUntrueSize) < 2) {
                                    CollectFlag = true;
                                    IsCollect = 0;
                                    if (ConfigManager.getInstance() != null && ConfigManager.getInstance().isMale()) {
                                        sexId = 0;
                                    } else {
                                        sexId = 1;
                                    }
                                    String emptyText = null;
                                    if (radioItems == null || radioItems.size() == 0) {
                                        if (collectListEmpty != null && collectListEmpty >= 1) {//有追踪的人
                                            emptyText = StringUtils.getString(R.string.radio_list_trace_empty2);
                                        } else {//没有追踪的人
                                            emptyText = StringUtils.getString(R.string.radio_list_trace_empty);
                                        }
                                    } else {
                                        emptyText = StringUtils.getString(R.string.radio_list_trace_empty2);
                                    }
                                    // 动态
                                    RadioTraceEmptyItemViewModel trendItemViewModel = new RadioTraceEmptyItemViewModel(RadioViewModel.this, emptyText);
                                    trendItemViewModel.multiItemType(RadioRecycleType_trace);
                                    radioItems.add(trendItemViewModel);
                                    getBroadcast(1);
                                }
                            }
                        }
                    }

                    @Override
                    public void onComplete() {
                        stopRefreshOrLoadMore();
                        collectReLoad = false;
                    }
                });
    }

    private void loadThemeAd() {
        model.getThemeAdList()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<ThemeAdEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<ThemeAdEntity> response) {
                        boolean adIsNull = false;
                        boolean flag = false;
                        if (response.getData().getAd() == null || response.getData().getAd().isEmpty()) {
                            adIsNull = true;
                        } else {
                            adItemEntityObservableField.set(response.getData().getAd());
                        }
                        themes = response.getData().getThem();
                        //model.saveThemeConfig((themes);
                        radioUC.loadLast.postValue(true);
                        List<ConfigItemEntity> configItemEntityList = new ArrayList<>();
                        for (ThemeItemEntity itemEntity : themes) {
                            ConfigItemEntity configItemEntity = new ConfigItemEntity();
                            configItemEntity.setThemeId(itemEntity.getThemeId());
                            configItemEntity.setId(itemEntity.getId());
                            configItemEntity.setIcon(itemEntity.getIcon());
                            configItemEntity.setName(itemEntity.getTitle());
                            configItemEntity.setSmallIcon(itemEntity.getSmallIcon());
                            configItemEntityList.add(configItemEntity);
                            if (itemEntity.getIsTop().intValue() == 1 && flag == false) {
                                List<AdItemEntity> list = new ArrayList<>();
                                AdItemEntity adItemEntity = new AdItemEntity();
                                adItemEntity.setImg(itemEntity.getIcon());
                                adItemEntity.setLink("theme-" + itemEntity.getId() + "-" + itemEntity.getThemeId() + "-" + itemEntity.getTitle() + "-" + itemEntity.getKeyWord());
                                adItemEntity.setTitle(itemEntity.getTitle());
                                list.add(adItemEntity);
                                if (adIsNull) {
                                    flag = true;
                                    adItemEntityObservableField.set(list);
                                } else {
                                    flag = true;
                                    list.addAll(response.getData().getAd());
                                }
                                adItemEntityObservableField.set(list);
                                adIsNull = true;
                            }
                        }
                        model.saveThemeConfig(configItemEntityList);
                    }

                    @Override
                    public void onComplete() {
//                        stopRefreshOrLoadMore();
                    }
                });
    }

    //动态点赞
    public void newsGive(int posion) {
        model.newsGive(((TrendItemViewModel) radioItems.get(posion)).newsEntityObservableField.get().getId())
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ToastUtils.showShort(R.string.give_success);
                        if (isFirstLike){
                            RxBus.getDefault().post(new TaskListEvent());
                        }
                        ((TrendItemViewModel) radioItems.get(posion)).addGiveUser();
                        AppContext.instance().logEvent(AppsFlyerEvent.Like);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //节目点赞
    public void topicalGive(int posion) {
        model.TopicalGive(((ProgramItemViewModel) radioItems.get(posion)).topicalListEntityObservableField.get().getId())
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ToastUtils.showShort(R.string.give_success);

                        ((ProgramItemViewModel) radioItems.get(posion)).addGiveUser();
                        AppContext.instance().logEvent(AppsFlyerEvent.Like);
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
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ToastUtils.showShort(R.string.comment_success);
                        if (isFirstComment){//每天第一次留言
                            RxBus.getDefault().post(new TaskListEvent());
                        }
                        for (int i = 0; i < radioItems.size(); i++) {
                            if (radioItems.get(i) instanceof TrendItemViewModel) {
                                if (id == ((TrendItemViewModel) radioItems.get(i)).newsEntityObservableField.get().getId()) {
                                    AppContext.instance().logEvent(AppsFlyerEvent.Message);
                                    ((TrendItemViewModel) radioItems.get(i)).addComment(id, content, toUserId, toUserName, model.readUserData().getNickname());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(RequestException e) {
                        if (e.getCode() == 10016) {
                            ToastUtils.showShort(StringUtils.getString(R.string.comment_close));
                            for (int i = 0; i < radioItems.size(); i++) {
                                if (radioItems.get(i) instanceof TrendItemViewModel) {
                                    if (id == ((TrendItemViewModel) radioItems.get(i)).newsEntityObservableField.get().getId()) {
                                        AppContext.instance().logEvent(AppsFlyerEvent.Message);
                                        ((TrendItemViewModel) radioItems.get(i)).newsEntityObservableField.get().getBroadcast().setIsComment(1);
                                    }
                                }
                            }
                        } else {
                            if (e.getMessage() != null) {
                                ToastUtils.showShort(e.getMessage());
                            } else {
                                ToastUtils.showShort(R.string.error_http_internal_server_error);
                            }

                            super.onError(e);
                        }
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
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ToastUtils.showShort(R.string.comment_success);
                        for (int i = 0; i < radioItems.size(); i++) {
                            if (radioItems.get(i) instanceof ProgramItemViewModel) {
                                if (id == ((ProgramItemViewModel) radioItems.get(i)).topicalListEntityObservableField.get().getId()) {
                                    AppContext.instance().logEvent(AppsFlyerEvent.Message);
                                    ((ProgramItemViewModel) radioItems.get(i)).addComment(id, content, toUserId, toUserName, model.readUserData().getNickname());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(RequestException e) {
                        if (e.getCode() == 10016) {
                            ToastUtils.showShort(StringUtils.getString(R.string.comment_close));
                            for (int i = 0; i < radioItems.size(); i++) {
                                if (radioItems.get(i) instanceof ProgramItemViewModel) {
                                    if (id == ((ProgramItemViewModel) radioItems.get(i)).topicalListEntityObservableField.get().getId()) {
                                        AppContext.instance().logEvent(AppsFlyerEvent.Message);
                                        ((ProgramItemViewModel) radioItems.get(i)).topicalListEntityObservableField.get().getBroadcast().setIsComment(1);
                                    }
                                }
                            }
                        } else {
                            super.onError(e);
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //节目结束报名
    public void TopicalFinish(int posion) {
        model.TopicalFinish(((ProgramItemViewModel) radioItems.get(posion)).topicalListEntityObservableField.get().getId())
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ((ProgramItemViewModel) radioItems.get(posion)).topicalListEntityObservableField.get().setIsEnd(1);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //我要报名
    public void report(int posion, String imags) {
        model.singUp(((ProgramItemViewModel) radioItems.get(posion)).topicalListEntityObservableField.get().getId(), imags)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ToastUtils.showShort(R.string.sign_up_success);
                        ((ProgramItemViewModel) radioItems.get(posion)).report();
                        AppContext.instance().logEvent(AppsFlyerEvent.Apply);
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
    public void setComment(int posion, String type) {
        int broadcastId;
        int isComment;
        if (type.equals(RadioRecycleType_Topical)) {
            broadcastId = ((ProgramItemViewModel) radioItems.get(posion)).topicalListEntityObservableField.get().getBroadcast().getId();
            isComment = ((ProgramItemViewModel) radioItems.get(posion)).topicalListEntityObservableField.get().getBroadcast().getIsComment();
        } else {
            broadcastId = ((TrendItemViewModel) radioItems.get(posion)).newsEntityObservableField.get().getBroadcast().getId();
            isComment = ((TrendItemViewModel) radioItems.get(posion)).newsEntityObservableField.get().getBroadcast().getIsComment();
        }
        model.setComment(broadcastId,
                isComment == 0 ? 1 : 0)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        if (type.equals(RadioRecycleType_Topical)) {
                            ToastUtils.showShort(((ProgramItemViewModel) radioItems.get(posion)).topicalListEntityObservableField.get().getBroadcast().getIsComment() == 1 ? StringUtils.getString(R.string.open_comment_success) : StringUtils.getString(R.string.close_success));
                            ((ProgramItemViewModel) radioItems.get(posion)).topicalListEntityObservableField.get().getBroadcast().setIsComment(
                                    ((ProgramItemViewModel) radioItems.get(posion)).topicalListEntityObservableField.get().getBroadcast().getIsComment() == 0 ? 1 : 0);
                        } else {
                            ToastUtils.showShort(((TrendItemViewModel) radioItems.get(posion)).newsEntityObservableField.get().getBroadcast().getIsComment() == 1 ? StringUtils.getString(R.string.open_comment_success) : StringUtils.getString(R.string.close_success));
                            ((TrendItemViewModel) radioItems.get(posion)).newsEntityObservableField.get().getBroadcast().setIsComment(
                                    ((TrendItemViewModel) radioItems.get(posion)).newsEntityObservableField.get().getBroadcast().getIsComment() == 0 ? 1 : 0);
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //删除动态
    public void deleteNews(int posion) {
        model.deleteNews(((TrendItemViewModel) radioItems.get(posion)).newsEntityObservableField.get().getId())
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        radioItems.remove(posion);
                        try {
                            GSYVideoManager.releaseAllVideos();
                        } catch (Exception e) {

                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    //删除节目
    public void deleteTopical(int posion) {
        model.deleteTopical(((ProgramItemViewModel) radioItems.get(posion)).topicalListEntityObservableField.get().getId())
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        radioItems.remove(posion);
                        try {
                            GSYVideoManager.releaseAllVideos();
                        } catch (Exception e) {

                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    /**
     * @return void
     * @Desc TODO(加载每日签到弹窗)
     * @author 彭石林
     * @parame []
     * @Date 2021/8/6
     */
    public void getEjectconfig() {
        model.getEjectconfig()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<EjectEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<EjectEntity> ejectEntityBaseDataResponse) {
                        if (!ObjectUtils.isEmpty(ejectEntityBaseDataResponse.getData())) {
                            EjectEntity entity = ejectEntityBaseDataResponse.getData();
                            if (entity.getIsSignIn() != null && entity.getIsSignIn().intValue() != 1) {
                                radioUC.loadEjectEntity.setValue(entity);
                            }
                        }
                    }
                });
    }

    /**
     * @return void
     * @Desc TODO(签到成功)
     * @author 彭石林
     * @parame []
     * @Date 2021/8/6
     */
    public void reportEjectSignIn() {
        model.reportEjectSignIn()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<EjectSignInEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<EjectSignInEntity> ejectSignInEntity) {
                        if (!ObjectUtils.isEmpty(ejectSignInEntity)) {
                            EjectSignInEntity signInEntity = ejectSignInEntity.getData();
                            radioUC.reporSignInSuccess.setValue(signInEntity);
                        }
                    }

                    @Override
                    public void onError(RequestException e) {

                    }
                });
    }

    /*=====谷歌支付核心代码=====*/
    //根据iD查询谷歌商品。并且订购它 7days_free
    public void querySkuOrder(String goodId) {
        ArrayList<String> goodList = new ArrayList<>();
        goodList.add(goodId);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(goodList).setType(BillingClient.SkuType.SUBS);
        if (billingClient == null) {
            ToastUtils.showShort(R.string.invite_web_detail_error);
            return;
        }
        billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(@NonNull @NotNull BillingResult billingResult, @Nullable @org.jetbrains.annotations.Nullable List<SkuDetails> skuDetailsList) {
                int responseCode = billingResult.getResponseCode();
                String debugMessage = billingResult.getDebugMessage();
                switch (responseCode) {
                    case BillingClient.BillingResponseCode.OK:
                        Log.i(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                        if (skuDetailsList == null) {
                            //订阅找不到
                            Log.w(TAG, "onSkuDetailsResponse: null SkuDetails list");
                            ToastUtils.showShort(R.string.invite_web_detail_error2);
                        } else {
                            for (SkuDetails skuDetails : skuDetailsList) {
                                if (skuDetails.getSku().equals(goodId)) {
                                    goodSkuDetails = skuDetails;
                                    clickPay.postValue(skuDetails.getSku());
                                    break;
                                }
                            }
                        }
                        break;
                    case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                    case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                    case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                    case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                    case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                    case BillingClient.BillingResponseCode.ERROR:
                        Log.e(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                        break;
                    case BillingClient.BillingResponseCode.USER_CANCELED:
                        Log.i(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                        break;
                    // These response codes are not expected.
                    case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                    case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                    case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
                    default:
                        Log.wtf(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                }
            }
        });
    }

    public void createOrder() {
        model.freeSevenDay(2, 2)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> {
                    showHUD();
                })
                .subscribe(new BaseObserver<BaseDataResponse<Map<String, String>>>() {

                    @Override
                    public void onSuccess(BaseDataResponse<Map<String, String>> mapBaseDataResponse) {
                        orderNumber = mapBaseDataResponse.getData().get("order_number");
                        google_goods_id = mapBaseDataResponse.getData().get("google_goods_id");
                        querySkuOrder(google_goods_id);
                    }

                    @Override
                    public void onError(RequestException e) {
                        dismissHUD();
                        ToastUtils.showShort(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                        super.onComplete();
                    }
                });
    }

    //查询最近的购买交易，并消耗商品
    public void queryAndConsumePurchase() {
        //queryPurchases() 方法会使用 Google Play 商店应用的缓存，而不会发起网络请求
        this.showHUD();

        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP,
                new PurchaseHistoryResponseListener() {
                    @Override
                    public void onPurchaseHistoryResponse(BillingResult billingResult,
                                                          List<PurchaseHistoryRecord> purchaseHistoryRecordList) {
                        dismissHUD();
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchaseHistoryRecordList != null) {
                            for (PurchaseHistoryRecord purchaseHistoryRecord : purchaseHistoryRecordList) {
                                // Process the result.
                                //确认购买交易，不然三天后会退款给用户
                                try {
                                    Purchase purchase = new Purchase(purchaseHistoryRecord.getOriginalJson(), purchaseHistoryRecord.getSignature());
                                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                        //消耗品 开始消耗
                                        consumePuchase(purchase, consumeImmediately);
                                        //确认购买交易
                                        if (!purchase.isAcknowledged()) {
                                            acknowledgePurchase(purchase);
                                        }
                                        //TODO：这里可以添加订单找回功能，防止变态用户付完钱就杀死App的这种
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                });
    }

    //消耗商品
    private void consumePuchase(final Purchase purchase, final int state) {
        this.showHUD();
        String packageName = purchase.getPackageName();
        if (StringUtil.isEmpty(packageName)) {
            packageName = AppContext.instance().getApplicationInfo().packageName;
        }
        String sku = purchase.getSkus().toString();
        String pToken = purchase.getPurchaseToken();
        ConsumeParams.Builder consumeParams = ConsumeParams.newBuilder();
        consumeParams.setPurchaseToken(purchase.getPurchaseToken());
        final String finalPackageName = packageName;
        billingClient.consumeAsync(consumeParams.build(), (billingResult, purchaseToken) -> {
            Log.i(TAG, "onConsumeResponse, code=" + billingResult.getResponseCode());
            dismissHUD();
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.i(TAG, "onConsumeResponse,code=BillingResponseCode.OK");
                paySuccessNotify(finalPackageName, sku, pToken, billingResult.getResponseCode());
            } else {
                //如果消耗不成功，那就再消耗一次
                Log.i(TAG, "onConsumeResponse=getDebugMessage==" + billingResult.getDebugMessage());
            }
        });
    }

    //确认订单
    private void acknowledgePurchase(Purchase purchase) {
        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Acknowledge purchase success");
                } else {
                    Log.i(TAG, "Acknowledge purchase failed,code=" + billingResult.getResponseCode() + ",\nerrorMsg=" + billingResult.getDebugMessage());
                }

            }
        };
        billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
    }

    /**
     * 回调结果给后台
     *
     * @param packageName
     * @param productId
     * @param token
     * @param event
     */
    public void paySuccessNotify(String packageName, String productId, String token, Integer event) {
        if (event.intValue() == BillingClient.BillingResponseCode.OK) {
            UserDataEntity userDataEntity = model.readUserData();
            userDataEntity.setIsVip(1);
            model.saveUserData(userDataEntity);
            SystemConfigEntity systemConfigEntity = model.readSystemConfig();
            SystemRoleMoneyConfigEntity sysManUserConfigEntity = systemConfigEntity.getManUser();
            sysManUserConfigEntity.setSendMessagesNumber(-1);
            systemConfigEntity.setManUser(sysManUserConfigEntity);
            SystemRoleMoneyConfigEntity sysManRealConfigEntity = systemConfigEntity.getManReal();
            sysManRealConfigEntity.setSendMessagesNumber(-1);
            systemConfigEntity.setManReal(sysManRealConfigEntity);
            SystemRoleMoneyConfigEntity sysManVipConfigEntity = systemConfigEntity.getManVip();
            sysManVipConfigEntity.setSendMessagesNumber(-1);
            systemConfigEntity.setManVip(sysManVipConfigEntity);
            model.saveSystemConfig(systemConfigEntity);
        }
        model.paySuccessNotify(packageName, orderNumber, productId, token, 2, event)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> {
                    showHUD();
                })
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ToastUtils.showShort(StringUtils.getString(R.string.pay_success));
                        try {
                            if (SignWinningDay != null) {
                                if (SignWinningDay.intValue() == 3) {
                                    AppContext.instance().logEvent(AppsFlyerEvent.sign_day3_vip);
                                } else if (SignWinningDay.intValue() == 7) {
                                    AppContext.instance().logEvent(AppsFlyerEvent.sign_day7_vip);
                                }
                            }
                            RxBus.getDefault().post(new UserUpdateVipEvent(Utils.formatday.format(Utils.addDate(new Date(), pay_good_day)), 1));
                        } catch (Exception e) {

                        }
                    }

                    @Override
                    public void onError(RequestException e) {

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
        public SingleLiveEvent programSubject = new SingleLiveEvent<>();
        public SingleLiveEvent clickImage = new SingleLiveEvent<>();
        public SingleLiveEvent<Boolean> loadLast = new SingleLiveEvent<>();
        public SingleLiveEvent clickToMessageDetail = new SingleLiveEvent();
        //每日签到弹窗加载
        public SingleLiveEvent<EjectEntity> loadEjectEntity = new SingleLiveEvent<>();
        public SingleLiveEvent<EjectSignInEntity> reporSignInSuccess = new SingleLiveEvent<>();
        //追踪的人消息列表清空
        public SingleLiveEvent<Boolean> emptyLayoutShow = new SingleLiveEvent<>();
    }
    /*=====谷歌支付核心代码=====*/


}