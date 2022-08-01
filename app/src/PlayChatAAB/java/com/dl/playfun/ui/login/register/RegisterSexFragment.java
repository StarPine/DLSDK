package com.dl.playfun.ui.login.register;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.ToastUtils;
import com.contrarywind.view.WheelView;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentRegisterSexBinding;
import com.dl.playfun.ui.base.BaseFragment;
import com.dl.playfun.ui.mine.profile.PerfectProfileViewModel;
import com.dl.playfun.utils.AutoSizeUtils;
import com.dl.playfun.utils.DateUtil;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.utils.Utils;
import com.dl.playfun.widget.dialog.MMAlertDialog;
import com.dl.playfun.widget.dialog.MVDialog;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;


/**
 * Author: 彭石林
 * Time: 2022/7/8 15:26
 * Description: This is RegisterSexFragment
 */
public class RegisterSexFragment extends BaseFragment<FragmentRegisterSexBinding, PerfectProfileViewModel>{
    RegisterChooseTimeView registerChooseTimeView;
    private String avatar;
    private String name;

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AutoSizeUtils.applyAdapt(getResources(),false);
        return R.layout.fragment_register_sex;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public PerfectProfileViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(PerfectProfileViewModel.class);
    }

    @Override
    public void initParam() {
        super.initParam();
        Bundle bundle = getArguments();
        if (bundle != null) {
            avatar = bundle.getString("avatar");
            name = bundle.getString("name");
        }
    }

    @Override
    public void initData() {
        super.initData();
        viewModel.UserAvatar.set(avatar);
        viewModel.UserName.set(name);
        initSelectDate();
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.uc.showAlertHint.observe(this, o -> {
            boolean code = viewModel.getCode();
            if (!code) {
                MVDialog.AlertInviteWrite(mActivity, true, new MVDialog.DilodAlertMessageInterface() {
                    @Override
                    public void confirm(DialogInterface dialog, int which, int sel_Index, String swiftMessageEntity) {
                        dialog.dismiss();
                        if (StringUtil.isEmpty(swiftMessageEntity)) {
                            viewModel.loadProfile(true);
                        } else {
                            AppContext.instance().pushInvite(swiftMessageEntity, 2, null);
                            viewModel.loadProfile(true);
                        }
                    }

                    @Override
                    public void cancel(DialogInterface dialog, int which) {

                    }
                }).show();
            } else {
                MMAlertDialog.RegUserAlert(mActivity, true, (dialog, which) -> {
                    dialog.dismiss();
                    viewModel.loadProfile(true);
                });
            }
        });
        viewModel.uc.clickChooseMale.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                if (viewModel.UserSex.get() == null) {
                    ToastUtils.showShort(R.string.playfun_fragment_perfect_sex_hint2);
                }
                viewModel.UserSex.set(1);
                binding.maleIcon.setAlpha(1f);
                binding.maleIconCheck.setVisibility(View.VISIBLE);
                binding.femaleIcon.setAlpha(0.5f);
                binding.femaleIconCheck.setVisibility(View.GONE);
            }
        });
        viewModel.uc.clickChooseGirl.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                if (viewModel.UserSex.get() == null) {
                    ToastUtils.showShort(R.string.playfun_fragment_perfect_sex_hint2);
                }
                viewModel.UserSex.set(0);
                binding.maleIcon.setAlpha(0.5f);
                binding.maleIconCheck.setVisibility(View.GONE);
                binding.femaleIcon.setAlpha(1f);
                binding.femaleIconCheck.setVisibility(View.VISIBLE);
            }
        });
        viewModel.uc.getClickBirthday.observe(this, o -> {
            Date timeDate = registerChooseTimeView.getTimeData();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(timeDate);
            int month = (calendar.get(Calendar.MONTH) + 1);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            String months = month < 10 ? "0" + month : String.valueOf(month);
            String days = day < 10 ? "0" + day : String.valueOf(day);
            viewModel.UserBirthday.set((calendar.get(Calendar.YEAR)) + "-" + months + "-" + days);

            viewModel.saveAvatar(viewModel.UserAvatar.get());
        });
    }

    public void initSelectDate() {
        Calendar selectedDate = Calendar.getInstance();
        if (viewModel.UserBirthday.get() != null) {
            try {
                selectedDate.setTime(Objects.requireNonNull(Utils.formatday.parse(viewModel.UserBirthday.get())));
            } catch (ParseException e) {
                Log.e("当前默认选择时间异常为", e.getMessage());
                e.printStackTrace();
            }
        }
        // 时间转轮 自定义控件
        LinearLayout timePickerView = binding.optionspicker;
        Calendar startDate = Calendar.getInstance();
        startDate.set(1931, 0, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(DateUtil.getYear() - 18, DateUtil.getMonth() - 1, DateUtil.getCurrentMonthDay());
        ChooseTimeVIewBuilder pvTime = new ChooseTimeVIewBuilder(this.getContext(), null)
                .setType(new boolean[]{true, true, true, false, false, false})//分别对应年月日时分秒，默认全部显示
                .setCancelText(getString(R.string.cancel))//取消按钮文字
                .setSubmitText(getString(R.string.confirm))//确认按钮文字
                .setContentTextSize(14)//滚轮文字大小
                .setSubCalSize(14)
                .setTitleSize(14)//标题文字大小
                .setTitleText(getString(R.string.playfun_fragment_edit_profile_brithday))//标题文字
                .setTextColorCenter(getResources().getColor(R.color.purple))//设置选中项的颜色
                .setTextColorOut(getResources().getColor(R.color.gray_light))//设置没有被选中项的颜色
                .setTitleBgColor(0xffffffff)//标题背景颜色 Night mode
                .setBgColor(0xffffffff)//滚轮背景颜色 Night mode
                .setDate(selectedDate)// 如果不设置的话，默认是系统时间*/
                .setRangDate(startDate, endDate)//起始终止年月日设定
                .setItemVisibleCount(5)//设置最大可见数目
                .setDividerType(WheelView.DividerType.WRAP)
                .setLineSpacingMultiplier(2.8f)
                .setLabel(getString(R.string.playfun_year), getString(R.string.playfun_month), getString(R.string.playfun_daily), getString(R.string.playfun_hour), getString(R.string.playfun_minute), getString(R.string.playfun_second));
        registerChooseTimeView = new RegisterChooseTimeView(pvTime.build(), timePickerView);
    }

    @Override
    public void onDestroy() {
        AutoSizeUtils.closeAdapt(getResources());
        super.onDestroy();
    }

}
