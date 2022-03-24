package com.dl.playfun.ui.radio.publishdynamic;

import static com.dl.playfun.app.AppConfig.FEMALE;
import static com.dl.playfun.app.AppConfig.MALE;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentPublishDynamicBinding;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.ui.certification.certificationfemale.CertificationFemaleFragment;
import com.dl.playfun.ui.certification.certificationmale.CertificationMaleFragment;
import com.dl.playfun.ui.mine.vipsubscribe.VipSubscribeFragment;
import com.dl.playfun.utils.ExceptionReportUtils;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.widget.coinpaysheet.CoinPaySheet;
import com.dl.playfun.widget.coinrechargesheet.CoinRechargeSheetView;
import com.dl.playfun.widget.dialog.MVDialog;
import com.dl.playfun.widget.picchoose.PicChooseItemEntity;
import com.dl.playfun.widget.picchoose.PicChooseView;

import java.util.ArrayList;
import java.util.List;

import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author wulei
 */
public class PublishDynamicFragment extends BaseToolbarFragment<FragmentPublishDynamicBinding, PublishDynamicViewModel> {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ImmersionBarUtils.setupStatusBar(this, true, false);
        return view;
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_publish_dynamic;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public PublishDynamicViewModel initViewModel() {
        //使用自定义的ViewModelFactory来创建ViewModel，如果不重写该方法，则默认会调用LoginViewModel(@NonNull Application application)构造方法
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(PublishDynamicViewModel.class);
    }

    @Override
    public void initViewObservable() {

        viewModel.uc.clickNotVip.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                int sex = AppContext.instance().appRepository.readUserData().getSex();
                MVDialog.getInstance(PublishDynamicFragment.this.getContext())
                        .setContent(getString(R.string.issuance_tends))
                        .setConfirmText(sex == 1 ? getString(R.string.to_be_member_issuance) : getString(R.string.author_free_issuance))
                        .setConfirmTwoText(getString(R.string.pay_issuance) + "（" + ConfigManager.getInstance().getNewsMoney() + getString(R.string.element) + "）")
                        .chooseType(MVDialog.TypeEnum.CENTER)
                        .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
                            @Override
                            public void confirm(MVDialog dialog) {
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
                        .setConfirmTwoOnclick(new MVDialog.ConfirmTwoOnclick() {
                            @Override
                            public void confirm(MVDialog dialog) {
                                showDialog();
                            }
                        })
                        .chooseType(MVDialog.TypeEnum.CENTER)
                        .show();
            }
        });
        binding.postContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int lineCount = binding.postContent.getLineCount();
                if (lineCount >= 6) {
                    ToastUtils.showShort(R.string.publish_dynamic_toast);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                int lineCount = binding.postContent.getLineCount();
                try {
                    int editStart = binding.postContent.getSelectionStart();//开始的字符位置
                    int editEnd = binding.postContent.getSelectionEnd();//结束的字符位置
                    if (lineCount >= 6) {
                        editable.delete(editStart - 1, editEnd);//删除
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showDialog() {
        try {
            new CoinPaySheet.Builder(mActivity).setPayParams(8, AppContext.instance().appRepository.readUserData().getId(), getString(R.string.issuance_tends), false, new CoinPaySheet.CoinPayDialogListener() {
                @Override
                public void onPaySuccess(CoinPaySheet sheet, String orderNo, Integer payPrice) {
                    sheet.dismiss();
                    ToastUtils.showShort(R.string.pay_success);
                    viewModel.sendConfirm();
                }

                @Override
                public void onRechargeSuccess(CoinRechargeSheetView rechargeSheetView) {

                }
            }).build().show();
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    }

    @Override
    public void initData() {
        super.initData();
        binding.picChooseView.setMaxSelectNum(1);
        binding.picChooseView.setGridCount(3);
        binding.picChooseView.setOnMediaOperateListener(new PicChooseView.OnMediaOperateListener() {
            @Override
            public void onMediaChooseCancel() {

            }

            @Override
            public void onMediaChoosed(List<PicChooseItemEntity> medias) {
                if (medias != null && !medias.isEmpty()) {
                    List<String> filePaths = new ArrayList<>();
                    for (PicChooseItemEntity media : medias) {
                        filePaths.add(media.getSrc());
                    }
                    viewModel.setFilePaths(filePaths);
                }
            }

            @Override
            public void onMediaDelete(List<PicChooseItemEntity> medias, PicChooseItemEntity delMedia) {
                viewModel.delFilePaths(delMedia.getSrc());
            }


        });
    }

}
