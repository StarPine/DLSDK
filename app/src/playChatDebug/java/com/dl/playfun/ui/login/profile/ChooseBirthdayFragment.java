package com.dl.playfun.ui.login.profile;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.view.TimePickerView;
import com.contrarywind.view.WheelView;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentChooseBirthdayBinding;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.utils.DateUtil;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.utils.StringUtil;

import java.util.Calendar;

/**
 * @ClassName ChooseBirthdayFragment
 * @Description TODO
 * @Author 彭石林
 * @Date 2021/6/23 16:29
 * @Phone 16620350375
 * @email 15616314565@163.com
 * @Version 1.0
 **/
public class ChooseBirthdayFragment extends BaseToolbarFragment<FragmentChooseBirthdayBinding, ProfileViewModel> {
    String UserBirthday;
    Integer sex;
    String name;

    public static Bundle getStartBundle(Integer sex, String name) {
        Bundle bundle = new Bundle();
        bundle.putInt("sex", sex);
        bundle.putString("name", name);
        return bundle;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ImmersionBarUtils.setupStatusBar(this, true, true);
        return view;
    }


    @Override
    public int initContentView(LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return R.layout.fragment_choose_birthday;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public ProfileViewModel initViewModel() {
        //使用自定义的ViewModelFactory来创建ViewModel，如果不重写该方法，则默认会调用LoginViewModel(@NonNull Application application)构造方法
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(ProfileViewModel.class);
    }

    @Override
    public void initParam() {
        super.initParam();
        sex = getArguments().getInt("sex");
        name = getArguments().getString("name");
    }

    @Override
    public void initViewObservable() {
        if (!StringUtil.isEmpty(name)) {
            viewModel.UserName.set(name);
            binding.birthdayName.setText(name + "，");
        }
        if (sex != null) {
            if (sex == 0) {
                binding.birthdayTitle.setText(R.string.choose_birthday_title2);
            }
            viewModel.UserSex.set(sex);
        }
        viewModel.uc.clickBirthday.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                shouChooseBirthday();
            }
        });
        viewModel.uc.clickConfirmBirthday.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {

            }
        });
    }

    //选择生日dialog
    public void shouChooseBirthday() {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(1995, 0, 1);
        Calendar startDate = Calendar.getInstance();
        startDate.set(1931, 0, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(DateUtil.getYear() - 18, DateUtil.getMonth() - 1, DateUtil.getCurrentMonthDay());
        TimePickerView pvTime = new TimePickerBuilder(this.getContext(), (date, v) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int month = (calendar.get(Calendar.MONTH) + 1);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            String months = month < 10 ? "0" + month : String.valueOf(month);
            String days = day < 10 ? "0" + day : String.valueOf(day);
            String UserBirthdays = (calendar.get(Calendar.YEAR)) + getString(R.string.year) + months + getString(R.string.month) + days + getString(R.string.daily);
            UserBirthday = (calendar.get(Calendar.YEAR)) + "-" + month + "-" + day;
            viewModel.UserBirthday.set(UserBirthday);
            viewModel.UserBirthdays.set(UserBirthdays);
        })
                .setType(new boolean[]{true, true, true, false, false, false})//分别对应年月日时分秒，默认全部显示
                .setCancelText(getString(R.string.cancel))//取消按钮文字
                .setSubmitText(getString(R.string.confirm))//确认按钮文字
                .setContentTextSize(14)//滚轮文字大小
                .setSubCalSize(14)
                .setTitleSize(14)//标题文字大小
                .setTitleText(getString(R.string.fragment_edit_profile_brithday))//标题文字
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
                .setItemVisibleCount(5)//设置最大可见数目
                .setDividerType(WheelView.DividerType.WRAP)
                .setLineSpacingMultiplier(2.8f)
                .setLabel(getString(R.string.year), getString(R.string.month), getString(R.string.daily), getString(R.string.hour), getString(R.string.minute), getString(R.string.second))
                .isDialog(true)//f是否显示为对话框样式
                .build();
        Dialog mDialog = pvTime.getDialog();
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
                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);//修改动画样式
                dialogWindow.setGravity(Gravity.BOTTOM);//改成Bottom,底部显示
            }
        }
        pvTime.show();
    }
}