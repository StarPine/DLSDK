package com.dl.playfun.ui.radio.radiohome;

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

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.databinding.FragmentRadioBinding;
import com.dl.playfun.entity.RadioTwoFilterItemEntity;
import com.dl.playfun.helper.DialogHelper;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseRefreshFragment;
import com.dl.playfun.ui.mine.broadcast.mytrends.TrendItemViewModel;
import com.dl.playfun.ui.radio.issuanceprogram.IssuanceProgramFragment;
import com.dl.playfun.ui.userdetail.report.ReportUserFragment;
import com.dl.playfun.utils.AutoSizeUtils;
import com.dl.playfun.utils.PictureSelectorUtil;
import com.dl.playfun.widget.RadioFilterView;
import com.dl.playfun.widget.dialog.MMAlertDialog;
import com.dl.playfun.widget.dialog.MVDialog;
import com.dl.playfun.widget.dialog.TraceDialog;
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

import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author wulei
 */
public class RadioFragment extends BaseRefreshFragment<FragmentRadioBinding, RadioViewModel> implements RadioFilterView.RadioFilterListener {
    private Context mContext;
    private EasyPopup mCirclePop;
    private List<RadioFilterView.RadioFilterItemEntity> sexs;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AutoSizeUtils.applyAdapt(this.getResources());
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
        sexs = new ArrayList<>();
        //sexs.add(new RadioFilterView.RadioFilterItemEntity<>(getString(R.string.any_gender), null));
        sexs.add(new RadioFilterView.RadioFilterItemEntity<>(getString(R.string.playfun_radio_selected_zuiz),2));
        sexs.add(new RadioFilterView.RadioFilterItemEntity<>(getString(R.string.playfun_just_look_lady), 0));
        sexs.add(new RadioFilterView.RadioFilterItemEntity<>(getString(R.string.playfun_just_look_man), 1));

        //viewModel.loadGameCity();


        binding.radioFilterView.setRadioFilterListener(this);

        viewModel.loadHttpData();
    }

    @Override
    public void onPause() {
        super.onPause();
        GSYVideoManager.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        GSYVideoManager.onResume();
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
        //初始化加载选项列表
        binding.radioFilterView.setFilterData(sexs, null);
        viewModel.radioUC.getRadioTwoFilterItemEntity.observe(this, new Observer<List<RadioTwoFilterItemEntity>>() {
            @Override
            public void onChanged(List<RadioTwoFilterItemEntity> radioTwoFilterItemEntities) {
                List<RadioTwoFilterItemEntity> regions = new ArrayList<>();
                List<RadioTwoFilterItemEntity.CityBean> cityBeans = new ArrayList<>();

                cityBeans.add(new RadioTwoFilterItemEntity.CityBean(0, getResources().getString(R.string.playfun_text_all)));
                regions.add(new RadioTwoFilterItemEntity(0, getResources().getString(R.string.playfun_text_all),cityBeans));

                if (radioTwoFilterItemEntities != null && radioTwoFilterItemEntities.size()>0){
                    for (RadioTwoFilterItemEntity gameCity : radioTwoFilterItemEntities) {
                        regions.add(gameCity);
                    }
                }
                binding.radioFilterView.setFilterData(sexs, regions);
            }
        });
        //放大图片
        viewModel.radioUC.zoomInp.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String drawable) {
                TraceDialog.getInstance(getContext())
                        .getImageDialog(mContext,drawable).show();
            }
        });
        /**
         * 节目发布
         */
        viewModel.radioUC.programSubject.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                viewModel.start(IssuanceProgramFragment.class.getCanonicalName());
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
            if (type.equals(RadioViewModel.RadioRecycleType_New)) {
                if (viewModel.userId == ((TrendItemViewModel) viewModel.radioItems.get(position)).newsEntityObservableField.get().getUser().getId()) {
                    stop.setText(((TrendItemViewModel) viewModel.radioItems.get(position)).newsEntityObservableField.get().getBroadcast().getIsComment() == 0 ? getString(R.string.playfun_fragment_issuance_program_no_comment) : getString(R.string.playfun_open_comment));
                    stop.setVisibility(View.GONE);
                    isSelf = true;
                } else {
                    mCirclePop.findViewById(R.id.tv_detele).setVisibility(View.GONE);
                    stop.setText(getString(R.string.playfun_report_user_title));
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
                            .setContent(type.equals(RadioViewModel.RadioRecycleType_New) ? getString(R.string.playfun_comfirm_delete_trend) : getString(R.string.playfun_confirm_delete_program))
                            .chooseType(MVDialog.TypeEnum.CENTER)
                            .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
                                @Override
                                public void confirm(MVDialog dialog) {
                                    if (type.equals(RadioViewModel.RadioRecycleType_New)) {
                                        viewModel.deleteNews(position);
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
                if (type.equals(RadioViewModel.RadioRecycleType_New)) {
                    if (((TrendItemViewModel) viewModel.radioItems.get(position)).newsEntityObservableField.get().getIsGive() == 0) {
                        viewModel.newsGive(position);
                    } else {
                        ToastUtils.showShort(R.string.playfun_already);
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
                if (ConfigManager.getInstance().getAppRepository().readUserData().getIsVip() == 1 || (ConfigManager.getInstance().getAppRepository().readUserData().getSex() == AppConfig.FEMALE && ConfigManager.getInstance().getAppRepository().readUserData().getCertification() == 1)) {
                    MVDialog.getInstance(RadioFragment.this.getContext())
                            .seCommentConfirm(new MVDialog.ConfirmComment() {
                                @Override
                                public void clickListItem(Dialog dialog, String comment) {
                                    if (StringUtils.isEmpty(comment)) {
                                        ToastUtils.showShort(R.string.playfun_warn_input_comment);
                                        return;
                                    }
                                    dialog.dismiss();
                                    if (type.equals(RadioViewModel.RadioRecycleType_New)) {
                                        viewModel.newsComment(Integer.valueOf(id), comment, toUserId != null ? Integer.valueOf(toUserId) : null, toUserName);
                                    }
                                }
                            })
                            .chooseType(MVDialog.TypeEnum.BOTTOMCOMMENT)
                            .show();
                } else {
                    DialogHelper.showNotVipCommentDialog(RadioFragment.this);
//                    if (ConfigManager.getInstance().getAppRepository().readUserData().getSex() == MALE) {
//                        DialogHelper.showNotVipCommentDialog(RadioFragment.this);
//                    } else {
//                    MVDialog.getInstance(RadioFragment.this.getContext())
//                            .setTitele(getString(R.string.authentication_free_sign_up))
//                            .setConfirmText(getString(R.string.mine_once_certification))
//                            .chooseType(MVDialog.TypeEnum.CENTER)
//                            .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
//                                @Override
//                                public void confirm(MVDialog dialog) {
//                                    if (ConfigManager.getInstance().getAppRepository().readUserData().getSex() == MALE) {
//                                        viewModel.start(CertificationMaleFragment.class.getCanonicalName());
//                                        return;
//                                    } else if (ConfigManager.getInstance().getAppRepository().readUserData().getSex() == FEMALE) {
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
                viewModel.type = 1;//发布时间
                binding.radioFilterView.sexClick(0);
                viewModel.cityId = null;
                viewModel.gameId = null;
                viewModel.setIsCollect(1);
                AppContext.instance().logEvent(AppsFlyerEvent.Follow_Only);
            }else{
                viewModel.setSexId((Integer) obj.getData());
            }
        }
    }

    @Override
    public void onRegionSelected(RadioFilterView radioFilterView, int position, Integer gameId,Integer cityId) {
        if(viewModel.CollectFlag && viewModel.IsCollect==0){
            if (ConfigManager.getInstance() != null && ConfigManager.getInstance().isMale()) {
                binding.radioFilterView.sexClick(1);
            } else {
                binding.radioFilterView.sexClick(2);
            }
        }
        viewModel.setCityId(gameId,cityId);
    }

}
