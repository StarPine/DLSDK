package com.dl.playfun.ui.radio.issuanceprogram;

import static com.dl.playfun.app.AppConfig.FEMALE;
import static com.dl.playfun.app.AppConfig.MALE;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.crop.CropMediaActivity;
import com.aliyun.svideosdk.common.struct.common.AliyunSnapVideoParam;
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode;
import com.aliyun.svideosdk.common.struct.common.VideoQuality;
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.blankj.utilcode.util.StringUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.contrarywind.view.WheelView;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.databinding.FragmentIssuanceProgramBinding;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.entity.DatingObjItemEntity;
import com.dl.playfun.entity.ThemeItemEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.ui.certification.certificationfemale.CertificationFemaleFragment;
import com.dl.playfun.ui.certification.certificationmale.CertificationMaleFragment;
import com.dl.playfun.ui.mine.adapter.HopeAdapter;
import com.dl.playfun.ui.mine.vipsubscribe.VipSubscribeFragment;
import com.dl.playfun.ui.userdetail.detail.UserDetailFragment;
import com.dl.playfun.utils.DateUtil;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.widget.coinpaysheet.CoinPaySheet;
import com.dl.playfun.widget.coinrechargesheet.CoinRechargeSheetView;
import com.dl.playfun.widget.dialog.MVDialog;
import com.dl.playfun.widget.dialog.TraceDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import me.goldze.mvvmhabit.utils.ToastUtils;
import me.jessyan.autosize.internal.CustomAdapt;

/**
 * @author wulei
 */
public class IssuanceProgramFragment extends BaseToolbarFragment<FragmentIssuanceProgramBinding, IssuanceProgramViewModel> implements CustomAdapt {
    public static final String ARG_PROGRAM_ENTITY = "arg_program_entity";
    public static final String ARG_CHOOSE_CITY = "arg_choose_city";
    public static final String ARG_ADDRESS_NAME = "arg_address_name";
    public static final String ARG_ADDRESS = "arg_address";
    public static final String ARG_ADDRESS_LAT = "arg_address_lat";
    public static final String ARG_ADDRESS_LNG = "arg_address_lng";

    private ThemeItemEntity themeItemEntity;
    private ConfigItemEntity city;
    private String addressName, address;
    private Double lat, lng;

    private TimePickerView pvTime;

    public static Bundle getStartBundle(ThemeItemEntity program, ConfigItemEntity city) {
        if (program == null) {
            ToastUtils.showShort(R.string.parameter_error);
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_PROGRAM_ENTITY, program);
        if (city != null) {
            bundle.putParcelable(ARG_CHOOSE_CITY, city);
        }
        return bundle;
    }

    public static Bundle getStartBundle(ThemeItemEntity program, ConfigItemEntity city, String addressName, String address, Double lat, Double lng) {
        if (program == null || city == null || addressName == null) {
            ToastUtils.showShort(R.string.parameter_error);
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_PROGRAM_ENTITY, program);
        bundle.putParcelable(ARG_CHOOSE_CITY, city);
        bundle.putString(ARG_ADDRESS_NAME, addressName);
        bundle.putString(ARG_ADDRESS, address);
        if (lat != null) {
            bundle.putDouble(ARG_ADDRESS_LAT, lat);
        }
        if (lng != null) {
            bundle.putDouble(ARG_ADDRESS_LNG, lng);
        }
        return bundle;
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_issuance_program;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public IssuanceProgramViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        IssuanceProgramViewModel issuanceProgramViewModel = ViewModelProviders.of(this, factory).get(IssuanceProgramViewModel.class);
        //issuanceProgramViewModel.chooseProgramItem.set(themeItemEntity);
        issuanceProgramViewModel.chooseCityItem.set(city);
        issuanceProgramViewModel.addressName.set(addressName);
        issuanceProgramViewModel.address.set(address);
        issuanceProgramViewModel.lat.set(lat);
        issuanceProgramViewModel.lng.set(lng);
        return issuanceProgramViewModel;
    }

    @Override
    public void initParam() {
        super.initParam();
        themeItemEntity = getArguments().getParcelable(ARG_PROGRAM_ENTITY);
        city = getArguments().getParcelable(ARG_CHOOSE_CITY);
        addressName = getArguments().getString(ARG_ADDRESS_NAME);
        address = getArguments().getString(ARG_ADDRESS);
        lat = getArguments().getDouble(ARG_ADDRESS_LAT);
        lng = getArguments().getDouble(ARG_ADDRESS_LNG);
    }

    @Override
    public void initViewObservable() {
        loadDatingDetail();
        viewModel.checkTopical();
        viewModel.uc.startVideoActivity.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                AliyunSnapVideoParam mCropParam = new AliyunSnapVideoParam.Builder()
                        .setFrameRate(30)
                        .setGop(250)
                        .setFilterList(null)
                        .setCropMode(VideoDisplayMode.SCALE)
                        .setVideoQuality(VideoQuality.HD)
                        .setVideoCodec(VideoCodecs.H264_HARDWARE)
                        .setResolutionMode(0)
                        .setRatioMode(1)
                        .setCropMode(VideoDisplayMode.SCALE)
                        .setNeedRecord(false)
                        .setMinVideoDuration(3000)
                        .setMaxVideoDuration(60 * 1000 * 1000)
                        .setMinCropDuration(3000)
                        .setSortMode(AliyunSnapVideoParam.SORT_MODE_MERGE)
                        .build();
                AppConfig.isCorpAliyun = true;
                CropMediaActivity.startCropForResult(_mActivity, 2002, mCropParam);
            }
        });
        viewModel.uc.checkDatingText.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {

            }
        });
        viewModel.uc.clickHope.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                shouChooseHope();
            }
        });
        viewModel.uc.clickDay.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                shouChooseDay();
            }
        });
        viewModel.uc.clickNotVip.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(final Integer typeDating) {
                if(typeDating.intValue()==1){
                    int sex = ConfigManager.getInstance().isMale()?1:0;
                    TraceDialog.getInstance(IssuanceProgramFragment.this.getContext())
                            .setTitle(getString(R.string.issuance_tends))
                            .setConfirmText(sex == 1 ? getString(R.string.to_be_member_issuance) : getString(R.string.author_free_issuance))
                            .setConfirmTwoText(getString(R.string.pay_issuance) + "（" + ConfigManager.getInstance().getNewsMoney() + getString(R.string.element) + "）")
                            .setConfirmOnlick(new TraceDialog.ConfirmOnclick() {
                                @Override
                                public void confirm(Dialog dialog) {
                                    if (sex == 1) {
                                        viewModel.start(VipSubscribeFragment.class.getCanonicalName());
                                    } else {
                                        if (sex == MALE) {
                                            viewModel.start(CertificationMaleFragment.class.getCanonicalName());
                                            return;
                                        } else if (sex == FEMALE) {
                                            viewModel.start(CertificationFemaleFragment.class.getCanonicalName());
                                            return;
                                        }
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .setConfirmTwoOnlick(new TraceDialog.ConfirmTwoOnclick() {
                                @Override
                                public void confirmTwo(Dialog dialog) {
                                    dialog.dismiss();
                                    showDialog(typeDating);
                                }
                            })
                            .verticalButtonDialog().show();

                }else{
                    TraceDialog.getInstance(IssuanceProgramFragment.this.getContext())
                            .setTitle(getString(R.string.fragment_issuance_program_title))
                            .setConfirmText(viewModel.sex == 1 ? getString(R.string.to_be_member_issuance) : getString(R.string.author_free_issuance))
                            .setConfirmTwoText(getString(R.string.pay_issuance) + "（" + viewModel.configManager.getTopicalMoney() + getString(R.string.element) + "）")
                            .setConfirmOnlick(new TraceDialog.ConfirmOnclick() {
                                @Override
                                public void confirm(Dialog dialog) {
                                    if (viewModel.sex == 1) {
                                        viewModel.start(VipSubscribeFragment.class.getCanonicalName());
                                    } else {
                                        if (viewModel.sex != null) {
                                            if (viewModel.sex == AppConfig.MALE) {
                                                viewModel.start(CertificationMaleFragment.class.getCanonicalName());
                                                return;
                                            } else if (viewModel.sex == AppConfig.FEMALE) {
                                                viewModel.start(CertificationFemaleFragment.class.getCanonicalName());
                                                return;
                                            }
                                        }
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .setConfirmTwoOnlick(new TraceDialog.ConfirmTwoOnclick() {
                                @Override
                                public void confirmTwo(Dialog dialog) {
                                    showDialog(typeDating);
                                }
                            })
                            .verticalButtonDialog().show();
                }
            }
        });
        viewModel.uc.clickTheme.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {

            }
        });
        viewModel.uc.clickAddress.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                //选择城市
                MVDialog.raDioChooseCity chooseOccupation = new MVDialog.raDioChooseCity() {

                    @Override
                    public void clickListItem(Dialog dialog, ConfigItemEntity configItemEntity) {
                        try {
                            AppContext.instance().logEvent(AppsFlyerEvent.Location);
                            city = configItemEntity;
                            viewModel.chooseCityItem.set(city);
                        } catch (Exception e) {

                        }
                    }
                };
                MVDialog.getCityDialog(getContext(), viewModel.list_chooseCityItem, viewModel.chooseCityItem.get() == null ? null : viewModel.chooseCityItem.get().getId(), chooseOccupation);
            }
        });
    }

    // 目标Fragment调用setFragmentResult()后，在其出栈时，会回调该方法
    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle data) {
        super.onFragmentResult(requestCode, resultCode, data);
        try {
            city = data.getParcelable(ARG_CHOOSE_CITY);
            addressName = data.getString(ARG_ADDRESS_NAME);
            address = data.getString(ARG_ADDRESS);
            lat = data.getDouble(ARG_ADDRESS_LAT);
            lng = data.getDouble(ARG_ADDRESS_LNG);
            viewModel.chooseCityItem.set(city);
            viewModel.addressName.set(addressName);
            viewModel.address.set(address);
            viewModel.lat.set(lat);
            viewModel.lng.set(lng);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showDialog(int type) {
        int payType = 0;
        String titles = StringUtils.getString(R.string.issuance_tends);
        if(type==1){//动态
            payType = 8;
            titles = StringUtils.getString(R.string.issuance_tends);
        }else{
            payType = 9;//约会
            titles = StringUtils.getString(R.string.send_show);
        }

        new CoinPaySheet.Builder(mActivity).setPayParams(payType, AppContext.instance().appRepository.readUserData().getId(), titles, false, new CoinPaySheet.CoinPayDialogListener() {
            @Override
            public void onPaySuccess(CoinPaySheet sheet, String orderNo, Integer payPrice) {
                sheet.dismiss();
                ToastUtils.showShort(R.string.pay_success);
                viewModel.sendConfirm();
            }

            @Override
            public void onRechargeSuccess(CoinRechargeSheetView rechargeSheetView) {
                // do nothing
            }
        }).build().show();
    }

    @Override
    public void initData() {
        super.initData();
        binding.postContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.tvSize.setText(charSequence.length()+"/120");
                viewModel.programDesc.set(String.valueOf(charSequence));
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    //选择期望对象
    private void shouChooseHope() {
        ArrayList<ConfigItemEntity> hopeEntities = new ArrayList<>();
        hopeEntities.addAll(viewModel.hope);
        List<Integer> strArr = viewModel.hope_object.get();
        if (strArr != null && strArr.size() > 0) {
            for (ConfigItemEntity config : hopeEntities) {
                for (int i = 0; i < strArr.size(); i++) {
                    if (strArr.get(i).intValue() == config.getId()) {
                        config.setIsChoose(true);
                        break;
                    } else {
                        config.setIsChoose(false);
                    }
                }
            }
        }
        final Dialog bottomDialog = new Dialog(this.getContext(), R.style.BottomDialog);
        View contentView = LayoutInflater.from(this.getContext()).inflate(R.layout.dialog_content_normal, null);
        bottomDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = getResources().getDisplayMetrics().widthPixels;
        contentView.setLayoutParams(layoutParams);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        TextView title = contentView.findViewById(R.id.tv_dialog_title);
        title.setText(getString(R.string.user_detail_hope_obj));
        RecyclerView recy = contentView.findViewById(R.id.recy_dialog);
        recy.setLayoutManager(new LinearLayoutManager(this.getContext()));
        final HopeAdapter hopeAdapter = new HopeAdapter(hopeEntities);
        recy.setAdapter(hopeAdapter);
        recy.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                hopeEntities.get(i).setIsChoose(!hopeEntities.get(i).getIsChoose());
                hopeAdapter.notifyDataSetChanged();
            }
        });
        contentView.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomDialog.dismiss();
            }
        });
        contentView.findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ToastUtils.showShort("确定");
                List<Integer> valuesIds = new ArrayList<>();
                for (int i = 0; i < hopeEntities.size(); i++) {
                    if (hopeEntities.get(i).getIsChoose()) {
                        valuesIds.add(hopeEntities.get(i).getId());
                    }
                }
                viewModel.hope_object.set(valuesIds);
                bottomDialog.dismiss();
                AppContext.instance().logEvent(AppsFlyerEvent.Ideal_Person);
            }
        });
        bottomDialog.show();
    }

    //    选择日期
    private void shouChooseDay() {
        Dialog mDialog = null;
        Calendar selectedDate = Calendar.getInstance();
        Calendar startDate = Calendar.getInstance();
        startDate.set(DateUtil.getYear(), DateUtil.getMonth() - 1, DateUtil.getCurrentMonthDay());
        Calendar endDate = Calendar.getInstance();
        endDate.set(Calendar.DAY_OF_YEAR, endDate.get(Calendar.DAY_OF_YEAR) + 6);
        //endDate.set(DateUtil.getYear(), DateUtil.getMonth() -1, DateUtil.getCurrentMonthDay()+7);
        if (StringUtil.isEmpty(viewModel.start_date.get()) || viewModel.start_date.get().equals(getString(R.string.dialog_day_choose_on_time))) {
            selectedDate.set(DateUtil.getYear(), DateUtil.getMonth() - 1, DateUtil.getCurrentMonthDay());
        } else {
            String birthdayStr = viewModel.start_date.get().replace(getString(R.string.year), "-").replace(getString(R.string.month), "-").replace(getString(R.string.daily), "");
            String[] day = birthdayStr.split("-");
            selectedDate.set(Integer.valueOf(day[0]), Integer.valueOf(day[1]) - 1, Integer.valueOf(day[2]));
        }
        TimePickerView pvTime = new TimePickerBuilder(this.getContext(), new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                String months = String.valueOf(calendar.get(Calendar.MONTH) + 1);
                if (months.length() < 2) {
                    months = "0" + months;
                }
                String monthsDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                if (monthsDay.length() < 2) {
                    monthsDay = "0" + monthsDay;
                }
                viewModel.start_date.set((calendar.get(Calendar.YEAR)) + "-" + months + "-" + monthsDay);
            }
        })
//                .setLayoutRes(R.layout.coustom_time_picker, new CustomListener() {
//            @Override
//            public void customLayout(View view) {
//                Button btnSubmit = view.findViewById(R.id.btnSubmit);
//                btnSubmit.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        WheelView year = view.findViewById(R.id.year);
//                        WheelView month = view.findViewById(R.id.month);
//                        WheelView day = view.findViewById(R.id.day);
//                        int yeadValue = (int)year.getAdapter().getItem(year.getCurrentItem());
//                        int monthValue = (int)month.getAdapter().getItem(month.getCurrentItem());
//                        int dayValue = (int)day.getAdapter().getItem(day.getCurrentItem());
//                        viewModel.start_date.set(yeadValue+ getString(R.string.year) + monthValue + getString(R.string.month) + dayValue + getString(R.string.daily));
//                        view.setVisibility(View.GONE);
//                    }
//                });
//                Button btnCancel = view.findViewById(R.id.btnCancel);
//                btnSubmit.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        finalMDialog1.dismiss();
//                    }
//                });
//            }
//        })
                .setType(new boolean[]{true, true, true, false, false, false})//分别对应年月日时分秒，默认全部显示
                .setCancelText(getString(R.string.cancel))//取消按钮文字
                .setSubmitText(getString(R.string.confirm))//确认按钮文字
                .setContentTextSize(14)//滚轮文字大小
                .setSubCalSize(14)
                .setTitleSize(14)//标题文字大小
                .setTitleText(getString(R.string.fragment_issuance_program_choose_day))//标题文字
                .setOutSideCancelable(true)//点击屏幕，点在控件外部范围时，是否取消显示
                .setTitleColor(getResources().getColor(R.color.gray_dark))//标题文字颜色
                .setSubmitColor(getResources().getColor(R.color.purple))//确定按钮文字颜色
                .setCancelColor(getResources().getColor(R.color.purple))//取消按钮文字颜色
                .setTextColorCenter(getResources().getColor(R.color.purple))//设置选中项的颜色
                .setTextColorOut(getResources().getColor(R.color.gray_light))//设置没有被选中项的颜色
                .setTitleBgColor(0xffffffff)//标题背景颜色 Night mode
                .setBgColor(0xffffffff)//滚轮背景颜色 Night mode
                .setDate(selectedDate)// 如果不设置的话，默认是系统时间*/
                .setRangDate(startDate, endDate)//起始终止年月日设定
                .setItemVisibleCount(7)//设置最大可见数目
                .setDividerType(WheelView.DividerType.WRAP)
                .setLineSpacingMultiplier(2.8f)
                .setLabel(getString(R.string.year), getString(R.string.month), getString(R.string.daily), getString(R.string.hour), getString(R.string.minute), getString(R.string.second))
                .isDialog(true)//f是否显示为对话框样式
                .build();
        mDialog = pvTime.getDialog();
        if (mDialog != null) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);

            params.leftMargin = 0;
            params.rightMargin = 0;
            pvTime.getDialogContainerLayout().setLayoutParams(params);

            Window dialogWindow = mDialog.getWindow();
            if (dialogWindow != null) {
                dialogWindow.setWindowAnimations(R.style.picker_view_slide_anim);//修改动画样式
                dialogWindow.setGravity(Gravity.BOTTOM);//改成Bottom,底部显示
            }
        }
        pvTime.show();
    }

    private void loadDatingDetail() {

        DatingObjItemEntity datingObjItemEntity5 = new DatingObjItemEntity();
        datingObjItemEntity5.setType(0);
        datingObjItemEntity5.setId(1);
        datingObjItemEntity5.setName(StringUtils.getString(R.string.mood_item_id1));
        datingObjItemEntity5.setSelect(true);
        datingObjItemEntity5.setIconChecked(getResources().getResourceName(R.mipmap.dating_obj_mood1_img));
        List<Integer> hope = new ArrayList<>();
        hope.add(1);
        viewModel.hope_object.set(hope);
        viewModel.$datingObjItemEntity = datingObjItemEntity5;
        DatingObjItemEntity datingObjItemEntity1 = new DatingObjItemEntity();
        datingObjItemEntity1.setType(0);
        datingObjItemEntity1.setId(2);
        datingObjItemEntity1.setName(StringUtils.getString(R.string.mood_item_id2));
        datingObjItemEntity1.setIconChecked(getResources().getResourceName(R.mipmap.dating_obj_mood2_img));
        DatingObjItemEntity datingObjItemEntity2 = new DatingObjItemEntity();
        datingObjItemEntity2.setType(0);
        datingObjItemEntity2.setId(3);
        datingObjItemEntity2.setName(StringUtils.getString(R.string.mood_item_id3));
        datingObjItemEntity2.setIconChecked(getResources().getResourceName(R.mipmap.dating_obj_mood3_img));
        DatingObjItemEntity datingObjItemEntity3 = new DatingObjItemEntity();
        datingObjItemEntity3.setType(0);
        datingObjItemEntity3.setId(4);
        datingObjItemEntity3.setName(StringUtils.getString(R.string.mood_item_id4));
        datingObjItemEntity3.setIconChecked(getResources().getResourceName(R.mipmap.dating_obj_mood4_img));
        DatingObjItemEntity datingObjItemEntity4 = new DatingObjItemEntity();
        datingObjItemEntity4.setType(0);
        datingObjItemEntity4.setId(5);
        datingObjItemEntity4.setName(StringUtils.getString(R.string.mood_item_id5));
        datingObjItemEntity4.setIconChecked(getResources().getResourceName(R.mipmap.dating_obj_mood5_img));


        DatingObjItemEntity datingObjItemEntity = new DatingObjItemEntity();
        datingObjItemEntity.setType(0);
        datingObjItemEntity.setId(6);
        datingObjItemEntity.setName(StringUtils.getString(R.string.mood_item_id6));
        datingObjItemEntity.setIconChecked(getResources().getResourceName(R.mipmap.dating_obj_mood6_img));

        RadioDatingItemViewModel radioThemeItemViewMode0 = new RadioDatingItemViewModel(viewModel, datingObjItemEntity);
        RadioDatingItemViewModel radioThemeItemViewMode1 = new RadioDatingItemViewModel(viewModel, datingObjItemEntity1);
        RadioDatingItemViewModel radioThemeItemViewMode2 = new RadioDatingItemViewModel(viewModel, datingObjItemEntity2);
        RadioDatingItemViewModel radioThemeItemViewMode3 = new RadioDatingItemViewModel(viewModel, datingObjItemEntity3);
        RadioDatingItemViewModel radioThemeItemViewMode4 = new RadioDatingItemViewModel(viewModel, datingObjItemEntity4);
        RadioDatingItemViewModel radioThemeItemViewMode5 = new RadioDatingItemViewModel(viewModel, datingObjItemEntity5);
        viewModel.objItems.add(radioThemeItemViewMode5);
        viewModel.objItems.add(radioThemeItemViewMode1);
        viewModel.objItems.add(radioThemeItemViewMode2);
        viewModel.objItems.add(radioThemeItemViewMode3);
        viewModel.objItems.add(radioThemeItemViewMode4);
        viewModel.objItems.add(radioThemeItemViewMode0);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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