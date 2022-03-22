package com.dl.playfun.ui.mine.profile;

import android.app.Dialog;
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

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.bigkoo.pickerview.view.TimePickerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.contrarywind.view.WheelView;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.databinding.FragmentEditProfileBinding;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.entity.OccupationConfigItemEntity;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.ui.mine.adapter.HopeAdapter;
import com.dl.playfun.ui.view.wheelview.DlOptionsPickerBuilder;
import com.dl.playfun.ui.view.wheelview.DlOptionsPickerView;
import com.dl.playfun.utils.DateUtil;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.utils.PictureSelectorUtil;
import com.dl.playfun.widget.dialog.MVDialog;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author wulei
 * 个人资料/个人信息
 */
public class EditProfileFragment extends BaseToolbarFragment<FragmentEditProfileBinding, EditProfileViewModel> {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ImmersionBarUtils.setupStatusBar(this, true, true);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_edit_profile;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public EditProfileViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(EditProfileViewModel.class);
    }

    @Override
    public void initData() {
        super.initData();
        AppContext.instance().logEvent(AppsFlyerEvent.Edit_Profile);
        binding.edtNickname.setOnFocusChangeListener((view, b) -> {
            if (!b && !AppContext.instance().appRepository.readUserData().getNickname().equals(binding.edtNickname.getText().toString())) {
                viewModel.checkNickname(binding.edtNickname.getText().toString());
            }
        });
        binding.edtNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() >10){
                    ToastUtils.showShort(R.string.name_number_too_long);
                    CharSequence charSequence = s.subSequence(0, 10);
                    binding.edtNickname.setText(charSequence);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void clearNicknameFocus() {
        if (binding.edtNickname.isFocused()) {
            binding.edtNickname.clearFocus();
        }
    }

    @Override
    public void initViewObservable() {
        viewModel.uc.clickAvatar.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void aVoid) {
                chooseAvatar();
                clearNicknameFocus();
            }
        });
        viewModel.uc.clickCity.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                shouChooseCity();
                clearNicknameFocus();
            }
        });
        viewModel.uc.clickBirthday.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                shouChooseBirthday();
                clearNicknameFocus();
            }
        });
        viewModel.uc.clickHeight.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                shouChooseHeight();
                clearNicknameFocus();
            }
        });
        viewModel.uc.clickHope.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                shouChooseHope();
                clearNicknameFocus();
            }
        });
        viewModel.uc.clickUnlock.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                shouChooseUnlock();
                clearNicknameFocus();
            }
        });

        viewModel.uc.clickOccupation.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                shouChooseOccupation();
                clearNicknameFocus();
            }
        });
        viewModel.uc.clickProgram.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                shouChooseProgram();
                clearNicknameFocus();
            }
        });
        viewModel.uc.clickUploadingHead.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {

            }
        });
        viewModel.uc.clickWeight.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                shouChooseWeight();
                clearNicknameFocus();
            }
        });
        viewModel.uc.nicknameOccupy.observe(this, s -> MVDialog.getInstance(EditProfileFragment.this.getContext())
                .setContent(String.format(getString(R.string.nickname_occupy), s))
                .setConfirmOnlick(dialog -> {
                    binding.edtNickname.setText(s);
                })
                .chooseType(MVDialog.TypeEnum.CENTERWARNED)
                .show());
        viewModel.uc.showFlagClick.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                MVDialog.getInstance(EditProfileFragment.this.getContext())
                        .setTitle(getString(R.string.user_proflt))
                        .setContent(getString(R.string.user_proflt1))
                        .setConfirmText(getString(R.string.dialog_set_withdraw_account_confirm))
                        .setConfirmOnlick(dialog -> {
                            pop();
                        })
                        .setNotClose(true)
                        .chooseType(MVDialog.TypeEnum.CENTER)
                        .show();
            }
        });
    }

    private void chooseAvatar() {
        if (viewModel.userDataEntity.get() == null) {
            return;
        }
        PictureSelectorUtil.selectImageAndCrop(mActivity, true, 1, 1, new OnResultCallbackListener<LocalMedia>() {
            @Override
            public void onResult(List<LocalMedia> result) {
                viewModel.saveAvatar(result.get(0).getCutPath());
            }

            @Override
            public void onCancel() {
            }
        });
    }

    //选择城市dialog
    public void shouChooseCity() {
        if (viewModel.userDataEntity.get() == null) {
            return;
        }
        MVDialog.ChooseCity chooseCity = new MVDialog.ChooseCity() {
            @Override
            public void clickListItem(Dialog dialog, List<Integer> ids) {
                viewModel.userDataEntity.get().setPermanentCityIds(ids);
            }
        };
        MVDialog.getCityDialog(this.getContext(), viewModel.city, viewModel.userDataEntity.get().getPermanentCityIds(), chooseCity);
    }

    //选择职业dialog
    public void shouChooseOccupation() {
        if (viewModel.userDataEntity.get() == null) {
            return;
        }
//        MVDialog.ChooseOccupation chooseOccupation = new MVDialog.ChooseOccupation() {
//            @Override
//            public void clickListItem(Dialog dialog, OccupationConfigItemEntity item) {
//                viewModel.userDataEntity.get().setOccupationId(item.getId());
//            }
//        };
//        MVDialog.getOccupationDialog(this.getContext(), viewModel.occupation, viewModel.userDataEntity.get().getOccupationId(), chooseOccupation);
        MVDialog.ChooseOccupation chooseOccupation = new MVDialog.ChooseOccupation() {
            @Override
            public void clickListItem(Dialog dialog, OccupationConfigItemEntity.ItemEntity item) {
                viewModel.userDataEntity.get().setOccupationId(item.getId());

            }

//            @Override
//            public void clickListItem(Dialog dialog, OccupationConfigItemEntity item) {
//                viewModel.userDataEntity.get().setOccupationId(item.getId());
//            }

//            @Override
//            public void clickListItem(Dialog dialog, OccupationConfigItemEntity.ItemEntity item) {
//                viewModel.userDataEntity.get().setOccupationId(item.getId());
//            }
        };
        MVDialog.getOccupationDialog(this.getContext(), viewModel.occupation, viewModel.userDataEntity.get().getOccupationId() == null ? -1 : viewModel.userDataEntity.get().getOccupationId(), chooseOccupation);
    }

    //选择生日dialog
    public void shouChooseBirthday() {
        if (viewModel.userDataEntity.get() == null) {
            return;
        }
        Calendar selectedDate = Calendar.getInstance();
        if (viewModel.userDataEntity.get().getBirthdayCal() != null) {
            selectedDate = viewModel.userDataEntity.get().getBirthdayCal();
        }
        Calendar startDate = Calendar.getInstance();
        startDate.set(1931, 0, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(DateUtil.getYear() - 18, DateUtil.getMonth() - 1, DateUtil.getCurrentMonthDay());
        TimePickerView pvTime = new TimePickerBuilder(this.getContext(), (date, v) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            viewModel.userDataEntity.get().setBirthday(calendar);
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

    //选择身高dialog
    private void shouChooseHeight() {// 弹出选择器
        if (viewModel.userDataEntity.get() == null) {
            return;
        }
        final List<String> options1Items = new ArrayList<>();
        int posion = 0;
        for (int i = 0; i < viewModel.height.size(); i++) {
            options1Items.add(viewModel.height.get(i).getName());
            if (viewModel.userDataEntity.get().getHeight().intValue() == viewModel.height.get(i).getId().intValue()) {
                posion = i;
            }
        }
        OptionsPickerView pvOptions = new OptionsPickerBuilder(this.getContext(), new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                viewModel.userDataEntity.get().setHeight(viewModel.height.get(options1).getId());
            }
        })
                .setCancelText(getString(R.string.cancel))//取消按钮文字
                .setSubmitText(getString(R.string.confirm))//确认按钮文字
                .setContentTextSize(14)//滚轮文字大小
                .setSubCalSize(14)
                .setTitleSize(14)//标题文字大小
                .setTitleText(getString(R.string.height))//标题文字
                .setOutSideCancelable(true)//点击屏幕，点在控件外部范围时，是否取消显示
                .setTitleColor(getResources().getColor(R.color.gray_dark))//标题文字颜色
                .setSubmitColor(getResources().getColor(R.color.purple))//确定按钮文字颜色
                .setCancelColor(getResources().getColor(R.color.purple))//取消按钮文字颜色
                .setTextColorCenter(getResources().getColor(R.color.purple))//设置选中项的颜色
                .setTextColorOut(getResources().getColor(R.color.gray_light))//设置没有被选中项的颜色
                .setTitleBgColor(0xffffffff)//标题背景颜色 Night mode
                .setBgColor(0xffffffff)//滚轮背景颜色 Night mode
                .setItemVisibleCount(5)//设置最大可见数目
                .setLineSpacingMultiplier(2.8f)
                .setSelectOptions(posion)  //设置默认选中项
                .isDialog(true)//f是否显示为对话框样式
                .build();
        pvOptions.setPicker(options1Items);//一级选择器

        Dialog mDialog = pvOptions.getDialog();
        if (mDialog != null) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);

            params.leftMargin = 0;
            params.rightMargin = 0;
            params.width = getResources().getDisplayMetrics().widthPixels;
            pvOptions.getDialogContainerLayout().setLayoutParams(params);

            Window dialogWindow = mDialog.getWindow();
            if (dialogWindow != null) {
                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);//修改动画样式
                dialogWindow.setGravity(Gravity.BOTTOM);//改成Bottom,底部显示
            }
        }

        pvOptions.show();
    }

    //选择体重dialog
    private void shouChooseWeight() {// 弹出选择器
        if (viewModel.userDataEntity.get() == null) {
            return;
        }
        final List<String> options1Items = new ArrayList<>();
        int posion = 0;
        for (int i = 0; i < viewModel.weight.size(); i++) {
            options1Items.add(viewModel.weight.get(i).getName());
            if (viewModel.userDataEntity.get().getWeight().intValue() == viewModel.weight.get(i).getId().intValue()) {
                posion = i;
            }
        }
        OptionsPickerView pvOptions = new OptionsPickerBuilder(this.getContext(), new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                viewModel.userDataEntity.get().setWeight(viewModel.weight.get(options1).getId());
            }
        })
                .setCancelText(getString(R.string.cancel))//取消按钮文字
                .setSubmitText(getString(R.string.confirm))//确认按钮文字
                .setContentTextSize(14)//滚轮文字大小
                .setSubCalSize(14)
                .setTitleSize(14)//标题文字大小
                .setTitleText(getString(R.string.weight))//标题文字
                .setOutSideCancelable(true)//点击屏幕，点在控件外部范围时，是否取消显示
                .setTitleColor(getResources().getColor(R.color.gray_dark))//标题文字颜色
                .setSubmitColor(getResources().getColor(R.color.purple))//确定按钮文字颜色
                .setCancelColor(getResources().getColor(R.color.purple))//取消按钮文字颜色
                .setTextColorCenter(getResources().getColor(R.color.purple))//设置选中项的颜色
                .setTextColorOut(getResources().getColor(R.color.gray_light))//设置没有被选中项的颜色
                .setItemVisibleCount(5)//设置最大可见数目
                .setLineSpacingMultiplier(2.8f)
                .setSelectOptions(posion)  //设置默认选中项
                .isDialog(true)//f是否显示为对话框样式
                .build();

        Dialog mDialog = pvOptions.getDialog();
        if (mDialog != null) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);

            params.leftMargin = 0;
            params.rightMargin = 0;
            params.width = getResources().getDisplayMetrics().widthPixels;
            pvOptions.getDialogContainerLayout().setLayoutParams(params);

            Window dialogWindow = mDialog.getWindow();
            if (dialogWindow != null) {
                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);//修改动画样式
                dialogWindow.setGravity(Gravity.BOTTOM);//改成Bottom,底部显示
            }
        }

        pvOptions.setPicker(options1Items);//一级选择器
        pvOptions.show();
    }

    //选择社群账号价格
    private void shouChooseUnlock() {// 弹出选择器
        if (viewModel.sociaAccountEntity.get() == null) {
            return;
        }
        final List<String> options1Items = new ArrayList<>();
//        options1Items.add("88新台幣");
//        options1Items.add("888新台幣");
//        options1Items.add(String.format("%.2f",888.0));
        int posion = 0;
        for (int i = 0; i < viewModel.sociaAccountEntity.get().getPriceInfos().size(); i++) {
            options1Items.add(String.format("%.2f",viewModel.sociaAccountEntity.get().getPriceInfos().get(i).getPrice()));
            if (viewModel.sociaAccountEntity.get().getSelectedLevel() == viewModel.price.get(i).getLevel()) {
                posion = i;
            }
        }

        DlOptionsPickerView pvOptions = new DlOptionsPickerBuilder(this.getContext(), new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
//                viewModel.sociaAccountEntity.get().setSelectedLevel(viewModel.price.get(options1).getLevel());
                viewModel.updateSocialLevel(viewModel.price.get(options1).getLevel());
            }
        })
                .setCancelText(getString(R.string.cancel))//取消按钮文字
                .setSubmitText(getString(R.string.confirm))//确认按钮文字
                .setContentTextSize(14)//滚轮文字大小
                .setSubCalSize(14)
                .setTitleSize(14)//标题文字大小
                .setTitleText(getString(R.string.unlock_price))//标题文字
                .setOutSideCancelable(true)//点击屏幕，点在控件外部范围时，是否取消显示
                .setTitleColor(getResources().getColor(R.color.gray_dark))//标题文字颜色
                .setSubmitColor(getResources().getColor(R.color.purple))//确定按钮文字颜色
                .setCancelColor(getResources().getColor(R.color.purple))//取消按钮文字颜色
                .setTextColorCenter(getResources().getColor(R.color.purple))//设置选中项的颜色
                .setTextColorOut(getResources().getColor(R.color.gray_light))//设置没有被选中项的颜色
                .setBottomTipText(String.format(getString(R.string.handling_fee),viewModel.sociaAccountEntity.get().getPercent()))
                .setItemVisibleCount(5)//设置最大可见数目
                .setLineSpacingMultiplier(2.8f)
                .setSelectOptions(posion)  //设置默认选中项
                .isDialog(true)//f是否显示为对话框样式
                .build();

        Dialog mDialog = pvOptions.getDialog();
        if (mDialog != null) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);

            params.leftMargin = 0;
            params.rightMargin = 0;
            params.width = getResources().getDisplayMetrics().widthPixels;
            pvOptions.getDialogContainerLayout().setLayoutParams(params);

            Window dialogWindow = mDialog.getWindow();
            if (dialogWindow != null) {
                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);//修改动画样式
                dialogWindow.setGravity(Gravity.BOTTOM);//改成Bottom,底部显示
            }
        }

        pvOptions.setPicker(options1Items);//一级选择器
        pvOptions.show();
    }




    //选择期望对象
    private void shouChooseHope() {
        if (viewModel.userDataEntity.get() == null) {
            return;
        }
        ArrayList<ConfigItemEntity> hopeEntities = new ArrayList<>();
        hopeEntities.addAll(viewModel.hope);
        List<Integer> strArr = viewModel.userDataEntity.get().getHopeObjectIds();
        for (ConfigItemEntity config : hopeEntities) {
            if (strArr != null && strArr.size() > 0) {
                for (int i = 0; i < strArr.size(); i++) {
                    if (strArr.get(i) != null && config.getId() != null && strArr.get(i).intValue() == config.getId().intValue()) {
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
        title.setText(getString(R.string.fragment_edit_profile_hope));
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
                List<Integer> valuesIds = new ArrayList<>();
                for (int i = 0; i < hopeEntities.size(); i++) {
                    if (hopeEntities.get(i).getIsChoose()) {
                        valuesIds.add(hopeEntities.get(i).getId());
                    }
                }
                viewModel.userDataEntity.get().setHopeObjectIds(valuesIds);
                bottomDialog.dismiss();
            }
        });
        bottomDialog.show();
    }

    //选择交友节目
    private void shouChooseProgram() {
        if (viewModel.userDataEntity.get() == null) {
            return;
        }
        ArrayList<ConfigItemEntity> programEntities = new ArrayList<>();
        programEntities.addAll(viewModel.program);
        List<Integer> strArr = viewModel.userDataEntity.get().getProgramIds();
        for (ConfigItemEntity config : programEntities) {
            if (strArr != null && strArr.size() > 0) {
                for (int i = 0; i < strArr.size(); i++) {
                    if (strArr.get(i) != null && config.getThemeId() != null && strArr.get(i).intValue() == config.getThemeId().intValue()) {
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
        title.setText(getString(R.string.fragment_edit_profile_program));
        RecyclerView recy = contentView.findViewById(R.id.recy_dialog);
        recy.setLayoutManager(new LinearLayoutManager(this.getContext()));
        final HopeAdapter hopeAdapter = new HopeAdapter(programEntities);
        recy.setAdapter(hopeAdapter);
        recy.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                programEntities.get(i).setIsChoose(!programEntities.get(i).getIsChoose());
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
                for (int i = 0; i < programEntities.size(); i++) {
                    if (programEntities.get(i).getIsChoose()) {
                        valuesIds.add(programEntities.get(i).getThemeId());
                    }
                }
                viewModel.userDataEntity.get().setProgramIds(valuesIds);
                bottomDialog.dismiss();
            }
        });
        bottomDialog.show();
    }

}
