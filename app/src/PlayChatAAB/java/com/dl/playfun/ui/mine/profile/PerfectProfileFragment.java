package com.dl.playfun.ui.mine.profile;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.view.TimePickerView;
import com.blankj.utilcode.util.ToastUtils;
import com.contrarywind.view.WheelView;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentPerfectProfileBinding;
import com.dl.playfun.ui.base.BaseFragment;
import com.dl.playfun.ui.login.register.RegisterSexFragment;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.utils.DateUtil;
import com.dl.playfun.utils.PictureSelectorUtil;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import java.util.Calendar;
import java.util.List;


/**
 * Author: 彭石林
 * Time: 2022/4/4 11:19
 * Description: This is PerfectProfileFragment
 */
public class PerfectProfileFragment extends BaseFragment<FragmentPerfectProfileBinding, PerfectProfileViewModel> {

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_perfect_profile;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initData() {
        super.initData();

    }

    @Override
    public PerfectProfileViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(PerfectProfileViewModel.class);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        if (AppConfig.overseasUserEntity != null) {
            viewModel.UserName.set(AppConfig.overseasUserEntity.getName());
            viewModel.UserAvatar.set(AppConfig.overseasUserEntity.getPhoto());
        }
        viewModel.uc.clickAvatar.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void aVoid) {
                clearNicknameFocus();
                chooseAvatar();
            }
        });

        viewModel.uc.clickBirthday.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                clearNicknameFocus();
                shouChooseBirthday();
            }
        });

        viewModel.uc.verifyAvatar.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                if (AppConfig.overseasUserEntity != null) {
                    saveOverseas();
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("avatar", viewModel.UserAvatar.get());
                    bundle.putString("name", viewModel.UserName.get());
                    viewModel.start(RegisterSexFragment.class.getCanonicalName(), bundle);
                }
            }
        });
    }

    /**
     * @return void
     * @Desc TODO(讲第三方头像转成本地头像)
     * @author 彭石林
     * @parame []
     * @Date 2022/7/15
     */
    public void saveOverseas() {
        binding.imgAvatar.buildDrawingCache(true);
        binding.imgAvatar.buildDrawingCache();
        Bitmap bitmap = binding.imgAvatar.getDrawingCache();
        String filename = ApiUitl.getDiskCacheDir(getContext()) + "/Overseas" + ApiUitl.getDateTimeFileName() + ".jpg";
        ApiUitl.saveBitmap(bitmap, filename, flag -> {
            if (flag) {
                Bundle bundle = new Bundle();
                bundle.putString("avatar", filename);
                bundle.putString("name", viewModel.UserName.get());
                viewModel.start(RegisterSexFragment.class.getCanonicalName(), bundle);
            } else {
                ToastUtils.showShort(R.string.playfun_fragment_perfect_avatar1);
            }
        });
    }

    //选择头像
    private void chooseAvatar() {
        PictureSelectorUtil.selectImageAndCrop(mActivity, true, 1, 1, new OnResultCallbackListener<LocalMedia>() {
            @Override
            public void onResult(List<LocalMedia> result) {
                clearNicknameFocus();
                viewModel.UserAvatar.set(result.get(0).getCutPath());
            }

            @Override
            public void onCancel() {
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
            viewModel.UserBirthday.set((calendar.get(Calendar.YEAR)) + "-" + months + "-" + days);
        })
                .setType(new boolean[]{true, true, true, false, false, false})//分别对应年月日时分秒，默认全部显示
                .setCancelText(getString(R.string.cancel))//取消按钮文字
                .setSubmitText(getString(R.string.confirm))//确认按钮文字
                .setContentTextSize(14)//滚轮文字大小
                .setSubCalSize(14)
                .setTitleSize(14)//标题文字大小
                .setTitleText(getString(R.string.playfun_fragment_perfect_brithday))//标题文字
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
                .setLabel("", "", "", "", "", "")
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
                dialogWindow.setWindowAnimations(R.style.picker_view_slide_anim);//修改动画样式
                dialogWindow.setGravity(Gravity.BOTTOM);//改成Bottom,底部显示
            }
        }
        pvTime.show();
    }


    private void clearNicknameFocus() {
        if (binding.editNickname.isFocused()) {
            binding.editNickname.clearFocus();
        }
    }
}
