package com.dl.playfun.ui.radio.radiohome;

import static com.dl.playfun.app.AppConfig.FEMALE;
import static com.dl.playfun.app.AppConfig.MALE;
import static com.dl.playfun.ui.radio.radiohome.RadioViewModel.RadioRecycleType_New;
import static com.dl.playfun.ui.userdetail.report.ReportUserFragment.ARG_REPORT_TYPE;
import static com.dl.playfun.ui.userdetail.report.ReportUserFragment.ARG_REPORT_USER_ID;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.StringUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.Injection;
import com.dl.playfun.databinding.FragmentRadioBinding;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.event.MessageTagEvent;
import com.dl.playfun.helper.DialogHelper;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseRefreshFragment;
import com.dl.playfun.ui.certification.certificationfemale.CertificationFemaleFragment;
import com.dl.playfun.ui.certification.certificationmale.CertificationMaleFragment;
import com.dl.playfun.ui.mine.broadcast.myprogram.ProgramItemViewModel;
import com.dl.playfun.ui.mine.broadcast.mytrends.TrendItemViewModel;
import com.dl.playfun.ui.radio.issuanceprogram.IssuanceProgramFragment;
import com.dl.playfun.ui.userdetail.report.ReportUserFragment;
import com.dl.playfun.utils.ChatUtils;
import com.dl.playfun.utils.PictureSelectorUtil;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.widget.RadioFilterView;
import com.dl.playfun.widget.dialog.MMAlertDialog;
import com.dl.playfun.widget.dialog.MVDialog;
import com.google.gson.reflect.TypeToken;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.zyyoona7.popup.EasyPopup;
import com.zyyoona7.popup.XGravity;
import com.zyyoona7.popup.YGravity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.jessyan.autosize.AutoSizeCompat;
import me.jessyan.autosize.internal.CustomAdapt;

/**
 * @author wulei
 */
public class RadioFragment extends BaseRefreshFragment<FragmentRadioBinding, RadioViewModel> implements RadioFilterView.RadioFilterListener, CustomAdapt {
    private TextView tvCreate;
    private Context mContext;
    private EasyPopup mCirclePop;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvCreate = view.findViewById(R.id.tv_create);
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AutoSizeCompat.autoConvertDensityOfGlobal(RadioFragment.this.getResources());
        return R.layout.fragment_radio;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void onSupportInvisible() {
        super.onSupportInvisible();
        try {
            GSYVideoManager.releaseAllVideos();
        } catch (Exception e) {

        }
    }

    @Override
    public RadioViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(RadioViewModel.class);
    }


    /**
     * @return void
     * @Desc TODO(页面再次进入)
     * @author 彭石林
     * @parame [hidden]
     * @Date 2021/8/4
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        try {
            GSYVideoManager.releaseAllVideos();
        } catch (Exception e) {

        }

    }

    @Override
    public void initData() {
        super.initData();
        List<RadioFilterView.RadioFilterItemEntity> times = new ArrayList<>();
        times.add(new RadioFilterView.RadioFilterItemEntity<>(getString(R.string.issuance_time), 1));
        times.add(new RadioFilterView.RadioFilterItemEntity<>(getString(R.string.activity_time), 2));
//        times.add(new RadioFilterView.RadioFilterItemEntity<>("最近距离", 3));

        List<RadioFilterView.RadioFilterItemEntity> sexs = new ArrayList<>();
        //sexs.add(new RadioFilterView.RadioFilterItemEntity<>(getString(R.string.any_gender), null));
        sexs.add(new RadioFilterView.RadioFilterItemEntity<>(getString(R.string.radio_selected_zuiz),2));
        sexs.add(new RadioFilterView.RadioFilterItemEntity<>(getString(R.string.just_look_lady), 0));
        sexs.add(new RadioFilterView.RadioFilterItemEntity<>(getString(R.string.just_look_man), 1));

        List<ConfigItemEntity> citys = Injection.provideDemoRepository().readCityConfig();
        List<RadioFilterView.RadioFilterItemEntity> regions = new ArrayList<>();
        regions.add(new RadioFilterView.RadioFilterItemEntity<>(getString(R.string.any_area), null));
        for (ConfigItemEntity city : citys) {
            regions.add(new RadioFilterView.RadioFilterItemEntity<>(city.getName(), city));
        }
        binding.radioFilterView.setFilterData(times, sexs, regions);
        binding.radioFilterView.setRadioFilterListener(this);
        if (viewModel != null) {
            try {
                //binding.radioFilterView.sexClick(0);
//                if (ConfigManager.getInstance() != null && ConfigManager.getInstance().isMale()) {
//                    binding.radioFilterView.sexClick(1);
//                } else {
//                    binding.radioFilterView.sexClick(2);
//                }
            } catch (Exception e) {

            }
        }

        binding.rcvRadio.setOnScrollListener(new RecyclerView.OnScrollListener() {
            final boolean scrollState = false;
            public int firstVisibleItem, lastVisibleItem, visibleCount;

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
//                switch (newState) {
//                    case RecyclerView.SCROLL_STATE_IDLE: //滚动停止
//                        scrollState = false;
//                        try{
//                            autoPlayVideo(recyclerView);
//                        }catch (Exception e) {
//
//                        }
//                        break;
//                    case RecyclerView.SCROLL_STATE_DRAGGING: //手指拖动
//                        scrollState = true;
//                        break;
//                    case RecyclerView.SCROLL_STATE_SETTLING: //惯性滚动
//                        scrollState = true;
//                        break;
//                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                visibleCount = lastVisibleItem - firstVisibleItem;

                //大于0说明有播放
                if (GSYVideoManager.instance().getPlayPosition() >= 0) {
                    //当前播放的位置
                    int position = GSYVideoManager.instance().getPlayPosition();
                    //对应的播放列表TAG
                    if (GSYVideoManager.instance().getPlayTag().equals("SampleCoverVideoPlayer")
                            && (position < firstVisibleItem || position > lastVisibleItem)) {
                        //如果滑出去了上面和下面就是否，和今日头条一样
                        GSYVideoManager.releaseAllVideos();
                        viewModel.adapter.notifyDataSetChanged();
                    }
                }
            }
        });
        viewModel.loadHttpData();
    }

    @Override
    public void onResume() {
        super.onResume();
        GSYVideoManager.onResume();
        AppContext.isHomePage = true;
        AppContext.isShowNotPaid = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        GSYVideoManager.onPause();
        AppContext.isShowNotPaid = false;
        AppContext.isHomePage = false;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
    }


    @Override
    public void initViewObservable() {
        super.initViewObservable();
        AppContext.instance().logEvent(AppsFlyerEvent.Broadcast);
        mContext = this.getContext();
        viewModel.radioUC.clickToMessageDetail.observe(this, o -> {
            String userId = "user_" + viewModel.messageTagEntity.get().getUserId();
            String nickname = viewModel.messageTagEntity.get().getNickname();
            String textMessage = viewModel.messageTagEntity.get().getMsg();
            RxBus.getDefault().post(new MessageTagEvent(null, false));
            ChatUtils.chatUser(userId, nickname, viewModel, textMessage);
        });

        /**
         * 节目发布
         */
        viewModel.radioUC.programSubject.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                if (viewModel.themes != null) {
                    AppContext.instance().logEvent(AppsFlyerEvent.Dating);
                    viewModel.start(IssuanceProgramFragment.class.getCanonicalName());
//                    ProgramSubjectChooseDialog dialog = new ProgramSubjectChooseDialog(viewModel.themes);
//                    dialog.show(getChildFragmentManager(), ProgramSubjectChooseDialog.class.getCanonicalName());
//                    dialog.setProgramSubjectChooseDialogListener((dialog1, itemEntity) -> {
//                        dialog1.dismiss();
//                        int idx = viewModel.themes.indexOf(itemEntity);
//                        AppContext.instance().logEvent(AppsFlyerEvent.Dating_subject+(idx+1));
//                        //Bundle bundle = SearchProgramSiteFragment.getStartBundle(itemEntity);
//                       // viewModel.start(SearchProgramSiteFragment.class.getCanonicalName(), bundle);
//                        Bundle bundle = IssuanceProgramFragment.getStartBundle(itemEntity,null);
//                        viewModel.start(IssuanceProgramFragment.class.getCanonicalName(), bundle);
//                    });
                }
            }
        });


        viewModel.radioUC.clickMore.observe(this, o -> {
            Integer position = Integer.valueOf(((Map<String, String>) o).get("position"));
            String type = ((Map<String, String>) o).get("type");
            Integer broadcastId = Integer.valueOf(((Map<String, String>) o).get("broadcastId"));
            mCirclePop = EasyPopup.create()
                    .setContentView(RadioFragment.this.getContext(), R.layout.more_item)
//                        .setAnimationStyle(R.style.RightPopAnim)
                    //是否允许点击PopupWindow之外的地方消失
                    .setFocusAndOutsideEnable(true)
                    .setDimValue(0)
                    .setWidth(350)
                    .apply();

            LinearLayoutManager layoutManager = (LinearLayoutManager) binding.rcvRadio.getLayoutManager();
            final View child = layoutManager.findViewByPosition(position);
            if (child != null) {
                mCirclePop.showAtAnchorView(child.findViewById(R.id.iv_more), YGravity.BELOW, XGravity.ALIGN_RIGHT, 0, 0);
            }
            TextView stop = mCirclePop.findViewById(R.id.tv_stop);

            boolean isSelf = false;
            if (type.equals(RadioRecycleType_New)) {
                if (viewModel.userId == ((TrendItemViewModel) viewModel.radioItems.get(position)).newsEntityObservableField.get().getUser().getId()) {
                    stop.setText(((TrendItemViewModel) viewModel.radioItems.get(position)).newsEntityObservableField.get().getBroadcast().getIsComment() == 0 ? getString(R.string.fragment_issuance_program_no_comment) : getString(R.string.open_comment));
                    isSelf = true;
                } else {
                    mCirclePop.findViewById(R.id.tv_detele).setVisibility(View.GONE);
                    stop.setText(getString(R.string.report_user_title));
                    isSelf = false;
                }
            } else {
                if (viewModel.userId == ((ProgramItemViewModel) viewModel.radioItems.get(position)).topicalListEntityObservableField.get().getUserId()) {
                    stop.setText(((ProgramItemViewModel) viewModel.radioItems.get(position)).topicalListEntityObservableField.get().getBroadcast().getIsComment() == 0 ? getString(R.string.fragment_issuance_program_no_comment) : getString(R.string.open_comment));
                    TextView tvDetele = mCirclePop.findViewById(R.id.tv_detele);
                    tvDetele.setText(getString(R.string.delete_program));
                    isSelf = true;
                } else {
                    mCirclePop.findViewById(R.id.tv_detele).setVisibility(View.GONE);
                    stop.setText(getString(R.string.report_user_title));
                    isSelf = false;
                }
            }

            boolean finalIsSelf = isSelf;
            stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (finalIsSelf) {
                        viewModel.setComment(position, type);
                    } else {
                        AppContext.instance().logEvent(AppsFlyerEvent.Report);
                        Bundle bundle = new Bundle();
                        bundle.putString(ARG_REPORT_TYPE, "broadcast");
                        bundle.putInt(ARG_REPORT_USER_ID, broadcastId);
                        startContainerActivity(ReportUserFragment.class.getCanonicalName(), bundle);
                    }
                    mCirclePop.dismiss();
                }
            });
            mCirclePop.findViewById(R.id.tv_detele).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MVDialog.getInstance(RadioFragment.this.getContext())
                            .setContent(type.equals(RadioRecycleType_New) ? getString(R.string.comfirm_delete_trend) : getString(R.string.confirm_delete_program))
                            .chooseType(MVDialog.TypeEnum.CENTER)
                            .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
                                @Override
                                public void confirm(MVDialog dialog) {
                                    if (type.equals(RadioRecycleType_New)) {
                                        viewModel.deleteNews(position);
                                    } else {
                                        viewModel.deleteTopical(position);
                                    }

                                    dialog.dismiss();
                                }
                            })
                            .chooseType(MVDialog.TypeEnum.CENTER)
                            .show();
                    mCirclePop.dismiss();
                }
            });

        });

        viewModel.radioUC.clickLike.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                Integer position = Integer.valueOf(((Map<String, String>) o).get("position"));
                String type = ((Map<String, String>) o).get("type");
                if (type.equals(RadioRecycleType_New)) {
                    if (((TrendItemViewModel) viewModel.radioItems.get(position)).newsEntityObservableField.get().getIsGive() == 0) {
                        viewModel.newsGive(position);
                    } else {
                        ToastUtils.showShort(R.string.already);
                    }
                } else {
                    if (((ProgramItemViewModel) viewModel.radioItems.get(position)).topicalListEntityObservableField.get().getIsGive() == 0) {
                        viewModel.topicalGive(position);
                    } else {
                        ToastUtils.showShort(R.string.already);
                    }
                }
            }
        });
        viewModel.radioUC.clickComment.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                viewModel.initUserDate();
                String id = ((Map<String, String>) o).get("id");
                String toUserId = ((Map<String, String>) o).get("toUseriD");
                String type = ((Map<String, String>) o).get("type");
                String toUserName = ((Map<String, String>) o).get("toUserName");
                if (AppContext.instance().appRepository.readUserData().getIsVip() == 1 || (AppContext.instance().appRepository.readUserData().getSex() == FEMALE && AppContext.instance().appRepository.readUserData().getCertification() == 1)) {
                    MVDialog.getInstance(RadioFragment.this.getContext())
                            .seCommentConfirm(new MVDialog.ConfirmComment() {
                                @Override
                                public void clickListItem(Dialog dialog, String comment) {
                                    if (StringUtils.isEmpty(comment)) {
                                        ToastUtils.showShort(R.string.warn_input_comment);
                                        return;
                                    }
                                    dialog.dismiss();
                                    if (type.equals(RadioRecycleType_New)) {
                                        viewModel.newsComment(Integer.valueOf(id), comment, toUserId != null ? Integer.valueOf(toUserId) : null, toUserName);
                                    } else {
                                        viewModel.topicalComment(Integer.valueOf(id), comment, toUserId != null ? Integer.valueOf(toUserId) : null, toUserName);
                                    }
                                }
                            })
                            .chooseType(MVDialog.TypeEnum.BOTTOMCOMMENT)
                            .show();
                } else {
                    DialogHelper.showNotVipCommentDialog(RadioFragment.this);
//                    if (AppContext.instance().appRepository.readUserData().getSex() == MALE) {
//                        DialogHelper.showNotVipCommentDialog(RadioFragment.this);
//                    } else {
//                    MVDialog.getInstance(RadioFragment.this.getContext())
//                            .setTitele(getString(R.string.authentication_free_sign_up))
//                            .setConfirmText(getString(R.string.mine_once_certification))
//                            .chooseType(MVDialog.TypeEnum.CENTER)
//                            .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
//                                @Override
//                                public void confirm(MVDialog dialog) {
//                                    if (AppContext.instance().appRepository.readUserData().getSex() == MALE) {
//                                        viewModel.start(CertificationMaleFragment.class.getCanonicalName());
//                                        return;
//                                    } else if (AppContext.instance().appRepository.readUserData().getSex() == FEMALE) {
//                                        viewModel.start(CertificationFemaleFragment.class.getCanonicalName());
//                                        return;
//                                    }
//                                    com.blankj.utilcode.util.ToastUtils.showShort(R.string.sex_unknown);
//                                    dialog.dismiss();
//                                }
//                            })
//                            .chooseType(MVDialog.TypeEnum.CENTER)
//                            .show();
//                    }
                }

            }
        });

        viewModel.radioUC.clickSignUp.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                MVDialog.getInstance(RadioFragment.this.getContext())
                        .setContent(getString(R.string.end_porgram))
                        .chooseType(MVDialog.TypeEnum.CENTER)
                        .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
                            @Override
                            public void confirm(MVDialog dialog) {
                                viewModel.TopicalFinish((Integer) o);
                                dialog.dismiss();
                            }
                        })
                        .chooseType(MVDialog.TypeEnum.CENTER)
                        .show();
            }
        });

        viewModel.radioUC.clickCheck.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                viewModel.initUserDate();
                if (AppContext.instance().appRepository.readUserData().getCertification() == 1) {
                    MVDialog.getInstance(RadioFragment.this.getContext())
                            .setTitle(getString(R.string.report_send_photo_titile))
                            .setConfirmText(getString(R.string.dialog_set_withdraw_account_confirm))
                            .chooseType(MVDialog.TypeEnum.CENTER)
                            .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
                                @Override
                                public void confirm(MVDialog dialog) {
                                    chooseAvatar((Integer) o);
                                    dialog.dismiss();
                                }
                            })
                            .chooseType(MVDialog.TypeEnum.CENTER)
                            .show();
                } else {
                    MVDialog.getInstance(RadioFragment.this.getContext())
                            .setTitle(getString(R.string.authentication_free_sign_up))
                            .setConfirmText(getString(R.string.mine_once_certification))
                            .chooseType(MVDialog.TypeEnum.CENTER)
                            .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
                                @Override
                                public void confirm(MVDialog dialog) {
                                    if (AppContext.instance().appRepository.readUserData().getSex() == MALE) {
                                        viewModel.start(CertificationMaleFragment.class.getCanonicalName());
                                        return;
                                    } else if (AppContext.instance().appRepository.readUserData().getSex() == FEMALE) {
                                        viewModel.start(CertificationFemaleFragment.class.getCanonicalName());
                                        return;
                                    }
                                    com.blankj.utilcode.util.ToastUtils.showShort(R.string.sex_unknown);
                                    dialog.dismiss();
                                }
                            })
                            .chooseType(MVDialog.TypeEnum.CENTER)
                            .show();
                }
            }
        });
        viewModel.radioUC.signUpSucceed.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                MVDialog.getInstance(RadioFragment.this.getContext())
                        .setContent(getString(R.string.sign_up_after_call_you))
                        .chooseType(MVDialog.TypeEnum.CENTER)
                        .setConfirmText(getString(R.string.roger))
                        .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
                            @Override
                            public void confirm(MVDialog dialog) {
                                dialog.dismiss();
                            }
                        })
                        .chooseType(MVDialog.TypeEnum.CENTER)
                        .show();
            }
        });
        viewModel.radioUC.clickImage.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                Integer position = Integer.valueOf(((Map<String, String>) o).get("position"));
                String listStr = ((Map<String, String>) o).get("images");
                List<String> images = GsonUtils.fromJson(listStr, new TypeToken<List<String>>() {
                }.getType());
                PictureSelectorUtil.previewImage(RadioFragment.this.getContext(), images, position);
            }
        });
        viewModel.radioUC.loadLast.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (viewModel.themes.size() > 1 && viewModel.themes.get(1) != null) {
                    //binding.theme6.setBackground(ImageUtils.byteToDrawable(viewModel.themes.get(1).getIcon()));
                    //GlideEngine.createGlideEngine().loadImage(mContext, StringUtil.getFullImageUrl(viewModel.themes.get(1).getIcon()),binding.theme6);
                    Glide.with(mContext)
                            .load(StringUtil.getFullImageUrl(viewModel.themes.get(1).getIcon()))
                            .apply(new RequestOptions()
                                    .placeholder(mContext.getResources().getDrawable(R.drawable.error_img_theme2))
                                    .error(mContext.getResources().getDrawable(R.drawable.error_img_theme2)))
                            .into(binding.theme6);
                }
                if (viewModel.themes.size() > 2 && viewModel.themes.get(2) != null) {
                    //binding.theme7.setBackground(ImageUtils.byteToDrawable(viewModel.themes.get(2).getIcon()));
                    //GlideEngine.createGlideEngine().loadImage(mContext, StringUtil.getFullImageUrl(viewModel.themes.get(2).getIcon()),binding.theme7);
                    Glide.with(mContext)
                            .load(StringUtil.getFullImageUrl(viewModel.themes.get(2).getIcon()))
                            .apply(new RequestOptions()
                                    .placeholder(mContext.getResources().getDrawable(R.drawable.error_img_theme2))
                                    .error(mContext.getResources().getDrawable(R.drawable.error_img_theme2)))
                            .into(binding.theme7);
                }
                if (viewModel.themes.size() > 3 && viewModel.themes.get(3) != null) {
                    //binding.theme8.setBackground(ImageUtils.byteToDrawable(viewModel.themes.get(3).getIcon()));
                    //GlideEngine.createGlideEngine().loadImage(mContext, StringUtil.getFullImageUrl(viewModel.themes.get(3).getIcon()),binding.theme8);
                    Glide.with(mContext)
                            .load(StringUtil.getFullImageUrl(viewModel.themes.get(3).getIcon()))
                            .apply(new RequestOptions()
                                    .placeholder(mContext.getResources().getDrawable(R.drawable.error_img_theme2))
                                    .error(mContext.getResources().getDrawable(R.drawable.error_img_theme2)))
                            .into(binding.theme8);
                }
                if (viewModel.themes.size() > 4 && viewModel.themes.get(4) != null) {
                    // binding.theme9.setBackground(ImageUtils.byteToDrawable(viewModel.themes.get(4).getIcon()));
                    //GlideEngine.createGlideEngine().loadImage(mContext,StringUtil.getFullImageUrl(viewModel.themes.get(4).getIcon()),binding.theme9);
                    Glide.with(mContext)
                            .load(StringUtil.getFullImageUrl(viewModel.themes.get(4).getIcon()))
                            .apply(new RequestOptions()
                                    .placeholder(mContext.getResources().getDrawable(R.drawable.error_img_theme2))
                                    .error(mContext.getResources().getDrawable(R.drawable.error_img_theme2)))
                            .into(binding.theme9);
                }
                if (viewModel.themes.size() > 5 && viewModel.themes.get(5) != null) {
                    //GlideEngine.createGlideEngine().loadImage(mContext,StringUtil.getFullImageUrl(viewModel.themes.get(5).getIcon()),binding.theme10);
                    Glide.with(mContext)
                            .load(StringUtil.getFullImageUrl(viewModel.themes.get(5).getIcon()))
                            .apply(new RequestOptions()
                                    .placeholder(mContext.getResources().getDrawable(R.drawable.error_img_theme1))
                                    .error(mContext.getResources().getDrawable(R.drawable.error_img_theme1)))
                            .into(binding.theme10);
                }
            }
        });

        NotificationManagerCompat notification = NotificationManagerCompat.from(getContext());
        boolean isEnabled = notification.areNotificationsEnabled();
        if (!isEnabled) {
            //未打开通知
            MMAlertDialog.DialogNotification(getContext(), true, new MMAlertDialog.DilodAlertInterface() {
                @Override
                public void confirm(DialogInterface dialog, int which, int sel_Index) {
                    dialog.dismiss();
                    Intent intent = new Intent();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                        intent.putExtra("android.provider.extra.APP_PACKAGE", getContext().getPackageName());
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {  //5.0
                        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                        intent.putExtra("app_package", getContext().getPackageName());
                        intent.putExtra("app_uid", getContext().getApplicationInfo().uid);
                        startActivity(intent);
                    } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {  //4.4
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                    } else if (Build.VERSION.SDK_INT >= 15) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                        intent.setData(Uri.fromParts("package", getContext().getPackageName(), null));
                    }
                    startActivity(intent);
                }

                @Override
                public void cancel(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
    }

    private void chooseAvatar(int position) {
        PictureSelectorUtil.selectImage(mActivity, true, 1, new OnResultCallbackListener<LocalMedia>() {
            @Override
            public void onResult(List<LocalMedia> result) {
                viewModel.imagUpload(result.get(0).getCompressPath(), position);
            }

            @Override
            public void onCancel() {
            }
        });
    }

    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        super.onEnterAnimationEnd(savedInstanceState);
    }

    @Override
    public void onPublishTimeSelected(RadioFilterView radioFilterView, int position, RadioFilterView.RadioFilterItemEntity obj) {
        if(viewModel.CollectFlag && viewModel.IsCollect==0){
            if (ConfigManager.getInstance() != null && ConfigManager.getInstance().isMale()) {
                binding.radioFilterView.sexClick(1);
            } else {
                binding.radioFilterView.sexClick(2);
            }
        }
        viewModel.setType((Integer) obj.getData());
        try {
            if (((Integer) obj.getData()).intValue() == 1) {
                AppContext.instance().logEvent(AppsFlyerEvent.Post_Time);
            } else {
                AppContext.instance().logEvent(AppsFlyerEvent.Dating_Time);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onSexSelected(RadioFilterView radioFilterView, int position, RadioFilterView.RadioFilterItemEntity obj) {
        if (obj.getData() == null) {
            viewModel.setSexId(null);
        } else {
            if (((Integer) obj.getData()).intValue() == 0) {
                AppContext.instance().logEvent(AppsFlyerEvent.Male_Only);
            } else {
                AppContext.instance().logEvent(AppsFlyerEvent.Female_Only);
            }
            if(((Integer)obj.getData()).intValue() == 2){//追踪的人
                binding.radioFilterView.timesClick(0);
                viewModel.type = 1;//发布时间
                binding.radioFilterView.cityClick(0);
                viewModel.cityId = null;
                viewModel.setIsCollect(1);
                AppContext.instance().logEvent(AppsFlyerEvent.Follow_Only);
            }else{
                viewModel.setSexId((Integer) obj.getData());
            }
        }
    }

    @Override
    public void onRegionSelected(RadioFilterView radioFilterView, int position, RadioFilterView.RadioFilterItemEntity obj) {
        if(viewModel.CollectFlag && viewModel.IsCollect==0){
            if (ConfigManager.getInstance() != null && ConfigManager.getInstance().isMale()) {
                binding.radioFilterView.sexClick(1);
            } else {
                binding.radioFilterView.sexClick(2);
            }
        }
        if (obj.getData() == null) {
            viewModel.setCityId(null);
        } else {
            viewModel.setCityId(((ConfigItemEntity) obj.getData()).getId());
        }
    }

    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return 360;
    }
}
