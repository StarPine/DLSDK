package com.dl.playfun.ui.radio.radiohome;

import static com.dl.playfun.ui.userdetail.report.ReportUserFragment.ARG_REPORT_TYPE;
import static com.dl.playfun.ui.userdetail.report.ReportUserFragment.ARG_REPORT_USER_ID;

import android.app.Activity;
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
import com.dl.playfun.entity.CoinExchangePriceInfo;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.entity.GameCoinBuy;
import com.dl.playfun.entity.RadioFilterItemEntity;
import com.dl.playfun.helper.DialogHelper;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseRefreshFragment;
import com.dl.playfun.ui.dialog.CityChooseDialog;
import com.dl.playfun.ui.mine.broadcast.mytrends.TrendItemViewModel;
import com.dl.playfun.ui.userdetail.detail.UserDetailFragment;
import com.dl.playfun.ui.userdetail.report.ReportUserFragment;
import com.dl.playfun.utils.AutoSizeUtils;
import com.dl.playfun.utils.PictureSelectorUtil;
import com.dl.playfun.viewadapter.CustomRefreshHeader;
import com.dl.playfun.widget.coinrechargesheet.CoinExchargeItegralPayDialog;
import com.dl.playfun.widget.coinrechargesheet.GameCoinExchargeSheetView;
import com.dl.playfun.widget.dialog.MMAlertDialog;
import com.dl.playfun.widget.dialog.MVDialog;
import com.dl.playfun.widget.dialog.TraceDialog;
import com.dl.playfun.widget.dropdownfilterpop.DropDownFilterPopupWindow;
import com.google.gson.reflect.TypeToken;
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
public class RadioFragment extends BaseRefreshFragment<FragmentRadioBinding, RadioViewModel> {
    private Context mContext;
    private EasyPopup mCirclePop;

    private CityChooseDialog cityChooseDialog;

    private List<RadioFilterItemEntity> radioFilterListData;
    private DropDownFilterPopupWindow radioFilterPopup;
    private Integer radioFilterCheckIndex;
    private List<ConfigItemEntity> citys;

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
        binding.refreshLayout.setRefreshHeader(new CustomRefreshHeader(getContext()));
        citys = ConfigManager.getInstance().getAppRepository().readCityConfig();
        ConfigItemEntity nearItemEntity = new ConfigItemEntity();
        nearItemEntity.setId(-1);
        nearItemEntity.setName(getStringByResId(R.string.playfun_tab_female_1));
        citys.add(0, nearItemEntity);

        radioFilterListData = new ArrayList<>();
        radioFilterListData.add(new RadioFilterItemEntity<>(getString(R.string.playfun_radio_selected_zuiz),2));
        radioFilterListData.add(new RadioFilterItemEntity<>(getString(R.string.playfun_just_look_lady), 0));
        radioFilterListData.add(new RadioFilterItemEntity<>(getString(R.string.playfun_just_look_man), 1));
        radioFilterPopup =  new DropDownFilterPopupWindow(mActivity, radioFilterListData);
        radioFilterCheckIndex = 0;
        radioFilterPopup.setSelectedPosition(radioFilterCheckIndex);
        radioFilterPopup.setOnItemClickListener((popupWindow, position) -> {
            popupWindow.dismiss();
            RadioFilterItemEntity obj =radioFilterListData.get(position);
            radioFilterCheckIndex = position;
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
                    viewModel.cityId = null;
                    viewModel.gameId = null;
                    viewModel.setIsCollect(1);
                    AppContext.instance().logEvent(AppsFlyerEvent.Follow_Only);
                }else{
                    viewModel.setSexId((Integer) obj.getData());
                }
                viewModel.tarckingTitle.set(obj.getName());
            }
        });

        viewModel.loadHttpData();
        viewModel.getAdUserBanner();
        viewModel.getAdListBanner();
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
        //开始播放
        viewModel.radioUC.startBannerEvent.observe(this, unused -> binding.rcvAduser.setPlaying(true));
        //点击banner切换
        viewModel.radioUC.clickBannerIdx.observe(this, integer -> {
            binding.rcvAduser.scrollToPosition(integer);
        });
        //弹起充值引导
        viewModel.radioUC.sendDialogViewEvent.observe(this, event -> {
            paySelectionboxChoose(false);
        });
        //对方忙线
        viewModel.radioUC.otherBusy.observe(this, o -> {
            TraceDialog.getInstance(getContext())
                    .chooseType(TraceDialog.TypeEnum.CENTER)
                    .setTitle(StringUtils.getString(R.string.playfun_other_busy_title))
                    .setContent(StringUtils.getString(R.string.playfun_other_busy_text))
                    .setConfirmText(StringUtils.getString(R.string.playfun_mine_trace_delike_confirm))
                    .setConfirmOnlick(new TraceDialog.ConfirmOnclick() {
                        @Override
                        public void confirm(Dialog dialog) {

                            dialog.dismiss();
                        }
                    }).TraceVipDialog().show();
        });
        //选择 追踪的人 男 女
        viewModel.radioUC.clickTacking.observe(this, unused -> {
            radioFilterPopup.setSelectedPosition(radioFilterCheckIndex);
            radioFilterPopup.showAsDropDown(binding.llTracking);
        });
        //选择城市
        viewModel.radioUC.clickRegion.observe(this, unused -> {
            if(cityChooseDialog==null){
                cityChooseDialog = new CityChooseDialog(getContext(),citys,viewModel.cityId);
            }
            cityChooseDialog.show();
            cityChooseDialog.setCityChooseDialogListener((dialog1, itemEntity) -> {
                viewModel.cityId = itemEntity.getId();
                viewModel.regionTitle.set(itemEntity.getName());
                binding.refreshLayout.autoRefresh();
                dialog1.dismiss();

            } );
        });
        //放大图片
        viewModel.radioUC.zoomInp.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String drawable) {
                TraceDialog.getInstance(getContext())
                        .getImageDialog(mContext,drawable).show();
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

    //支付框样式选择
    private void paySelectionboxChoose(boolean b) {
        if (ConfigManager.getInstance().isMale()) {
            if (ConfigManager.getInstance().isVip()) {
                googleCoinValueBox();
            } else {
                dialogRechargeShow(b);
            }
        } else {
            googleCoinValueBox();
        }
    }

    private void googleCoinValueBox() {
        CoinExchargeItegralPayDialog coinExchargeItegralPayDialog = new CoinExchargeItegralPayDialog(getContext(),mActivity);
        coinExchargeItegralPayDialog.show();
        coinExchargeItegralPayDialog.setCoinRechargeSheetViewListener(new CoinExchargeItegralPayDialog.CoinRechargeSheetViewListener() {
            @Override
            public void onPaySuccess(CoinExchargeItegralPayDialog sheetView, GameCoinBuy sel_goodsEntity) {
                sheetView.endGooglePlayConnect();
                sheetView.dismiss();
                MVDialog.getInstance(getContext())
                        .setTitele(getStringByResId(R.string.playfun_recharge_coin_success))
                        .setConfirmText(getStringByResId(R.string.playfun_confirm))
                        .setConfirmOnlick(dialog -> {
                            dialog.dismiss();
                        })
                        .chooseType(MVDialog.TypeEnum.CENTER)
                        .show();
            }

            @Override
            public void onPayFailed(CoinExchargeItegralPayDialog sheetView, String msg) {
                sheetView.dismiss();
                ToastUtils.showShort(msg);
                AppContext.instance().logEvent(AppsFlyerEvent.Failed_to_top_up);
            }
        });

    }

    //弹出钻石充值
    private void dialogRechargeShow(boolean isGiftSend) {
        if (!isGiftSend) {
            AppContext.instance().logEvent(AppsFlyerEvent.im_topup);
        }
        AppContext.instance().logEvent(AppsFlyerEvent.Top_up);
        GameCoinExchargeSheetView coinRechargeSheetView = new GameCoinExchargeSheetView(mActivity);
        coinRechargeSheetView.show();
        coinRechargeSheetView.setCoinRechargeSheetViewListener(new GameCoinExchargeSheetView.CoinRechargeSheetViewListener() {
            @Override
            public void onPaySuccess(GameCoinExchargeSheetView sheetView, CoinExchangePriceInfo sel_goodsEntity) {
                sheetView.dismiss();
            }

            @Override
            public void onPayFailed(GameCoinExchargeSheetView sheetView, String msg) {
                sheetView.dismiss();
                com.blankj.utilcode.util.ToastUtils.showShort(msg);
            }
        });
    }

    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        super.onEnterAnimationEnd(savedInstanceState);
    }

}
