package com.dl.playfun.ui.program.programdetail;

import static com.dl.playfun.ui.userdetail.report.ReportUserFragment.ARG_REPORT_TYPE;
import static com.dl.playfun.ui.userdetail.report.ReportUserFragment.ARG_REPORT_USER_ID;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
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
import com.dl.playfun.databinding.FragmentProgramDetailBinding;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.helper.DialogHelper;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.ui.certification.certificationfemale.CertificationFemaleFragment;
import com.dl.playfun.ui.certification.certificationmale.CertificationMaleFragment;
import com.dl.playfun.ui.mine.broadcast.myprogram.ProgramItemViewModel;
import com.dl.playfun.ui.mine.vipsubscribe.VipSubscribeFragment;
import com.dl.playfun.ui.userdetail.report.ReportUserFragment;
import com.dl.playfun.utils.PictureSelectorUtil;
import com.dl.playfun.widget.coinpaysheet.CoinPaySheet;
import com.dl.playfun.widget.coinrechargesheet.GameCoinTopupSheetView;
import com.dl.playfun.widget.dialog.MVDialog;
import com.google.gson.reflect.TypeToken;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.zyyoona7.popup.EasyPopup;
import com.zyyoona7.popup.XGravity;
import com.zyyoona7.popup.YGravity;

import java.util.List;
import java.util.Map;

import me.goldze.mvvmhabit.utils.ToastUtils;


/**
 * @author wulei
 */
public class ProgramDetailFragment extends BaseToolbarFragment<FragmentProgramDetailBinding, ProgramDetailViewModel> {
    public static final String ARG_PROGRAM_ID = "arg_program_id";

    private int id;
    private EasyPopup mCirclePop;
    private ImageView videoPlayer;
    private final boolean playStatus = false;

    public static Bundle getStartBundle(int id) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_PROGRAM_ID, id);
        return bundle;
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_program_detail;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initParam() {
        super.initParam();
        id = getArguments().getInt(ARG_PROGRAM_ID, 0);
    }

    @Override
    public ProgramDetailViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        ProgramDetailViewModel programDetailViewModel = ViewModelProviders.of(this, factory).get(ProgramDetailViewModel.class);
        programDetailViewModel.setId(id);
        return programDetailViewModel;
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
    public void onDestroy() {
        super.onDestroy();
        try {
            GSYVideoManager.releaseAllVideos();
        }catch (Exception e) {

        }
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        //新增播放视频
        viewModel.uc.clickPlayersVideo.observe(this, new Observer<Map<String, String>>() {
            @Override
            public void onChanged(Map<String, String> objMap) {
                try {
                }catch (Exception e) {

                }

            }
        });
        viewModel.uc.clickMore.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                Integer broadcastId = Integer.valueOf(((Map<String, String>) o).get("broadcastId"));
                mCirclePop = EasyPopup.create()
                        .setContentView(ProgramDetailFragment.this.getContext(), R.layout.more_item)
//                        .setAnimationStyle(R.style.RightPopAnim)
                        //是否允许点击PopupWindow之外的地方消失
                        .setFocusAndOutsideEnable(true)
                        .setDimValue(0)
                        .setWidth(350)
                        .apply();

                LinearLayoutManager layoutManager = (LinearLayoutManager) binding.rcvProgram.getLayoutManager();
                final View child = layoutManager.findViewByPosition(0);
                if (child != null) {
                    mCirclePop.showAtAnchorView(child.findViewById(R.id.iv_more), YGravity.BELOW, XGravity.ALIGN_RIGHT, 0, 0);
                }
                TextView stop = mCirclePop.findViewById(R.id.tv_stop);
                boolean isSelf = false;
                if (viewModel.userId == ((ProgramItemViewModel) viewModel.observableList.get(0)).topicalListEntityObservableField.get().getUser().getId()) {
                    stop.setText(((ProgramItemViewModel) viewModel.observableList.get(0)).topicalListEntityObservableField.get().getBroadcast().getIsComment() == 0 ? getString(R.string.playfun_fragment_issuance_program_no_comment) : getString(R.string.playfun_open_comment));
                    TextView tvDetele = mCirclePop.findViewById(R.id.tv_detele);
                    tvDetele.setText(getString(R.string.playfun_delete_program));
                    isSelf = true;
                } else {
                    mCirclePop.findViewById(R.id.tv_detele).setVisibility(View.GONE);
                    stop.setText(getString(R.string.playfun_report_user_title));
                    isSelf = false;
                }
                boolean finalIsSelf = isSelf;
                stop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (finalIsSelf) {
                            viewModel.setComment();
                        } else {
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
                        MVDialog.getInstance(ProgramDetailFragment.this.getContext())
                                .setContent(getString(R.string.playfun_confirm_delete_program))
                                .chooseType(MVDialog.TypeEnum.CENTER)
                                .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
                                    @Override
                                    public void confirm(MVDialog dialog) {
                                        viewModel.deleteTopical();
                                        dialog.dismiss();
                                    }
                                })
                                .chooseType(MVDialog.TypeEnum.CENTER)
                                .show();
                        mCirclePop.dismiss();
                    }
                });
            }
        });

        viewModel.uc.clickLike.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                if (((ProgramItemViewModel) viewModel.observableList.get(0)).topicalListEntityObservableField.get().getIsGive() == 0) {
                    viewModel.topicalGive();
                } else {
                    ToastUtils.showShort(R.string.playfun_already);
                }
            }
        });
        viewModel.uc.clickComment.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                viewModel.initUserDate();
                String id = ((Map<String, String>) o).get("id");
                String toUserId = ((Map<String, String>) o).get("toUseriD");
                String toUserName = ((Map<String, String>) o).get("toUserName");
                if (viewModel.isVip || (viewModel.sex == 0 && viewModel.certification == 1)) {
                    MVDialog.getInstance(ProgramDetailFragment.this.getContext())
                            .seCommentConfirm(new MVDialog.ConfirmComment() {
                                @Override
                                public void clickListItem(Dialog dialog, String comment) {
                                    if (StringUtils.isEmpty(comment)) {
                                        ToastUtils.showShort(R.string.playfun_warn_input_comment);
                                        return;
                                    }
                                    dialog.dismiss();
                                    viewModel.topicalComment(Integer.valueOf(id), comment, toUserId != null ? Integer.valueOf(toUserId) : null, toUserName);
                                }
                            })
                            .chooseType(MVDialog.TypeEnum.BOTTOMCOMMENT)
                            .show();
                } else {
                    DialogHelper.showNotVipCommentDialog(ProgramDetailFragment.this);
                }

            }
        });

        viewModel.uc.clickSignUp.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                MVDialog.getInstance(ProgramDetailFragment.this.getContext())
                        .setContent(getString(R.string.playfun_end_porgram))
                        .chooseType(MVDialog.TypeEnum.CENTER)
                        .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
                            @Override
                            public void confirm(MVDialog dialog) {
                                viewModel.TopicalFinish();
                                dialog.dismiss();
                            }
                        })
                        .chooseType(MVDialog.TypeEnum.CENTER)
                        .show();
            }
        });

        viewModel.uc.clickCheck.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                viewModel.initUserDate();
                if (viewModel.certification == 1) {
                    MVDialog.getInstance(ProgramDetailFragment.this.getContext())
                            .setTitele(getString(R.string.playfun_report_send_photo_titile))
                            .chooseType(MVDialog.TypeEnum.CENTER)
                            .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
                                @Override
                                public void confirm(MVDialog dialog) {
                                    chooseAvatar();
                                    dialog.dismiss();
                                }
                            })
                            .chooseType(MVDialog.TypeEnum.CENTER)
                            .show();
                } else {
                    MVDialog.getInstance(ProgramDetailFragment.this.getContext())
                            .setTitele(getString(R.string.playfun_authentication_free_sign_up))
                            .setConfirmText(getString(R.string.playfun_mine_once_certification))
                            .chooseType(MVDialog.TypeEnum.CENTER)
                            .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
                                @Override
                                public void confirm(MVDialog dialog) {
                                    if (viewModel.sex == AppConfig.MALE) {
                                        viewModel.start(CertificationMaleFragment.class.getCanonicalName());
                                        return;
                                    } else if (viewModel.sex == AppConfig.FEMALE) {
                                        viewModel.start(CertificationFemaleFragment.class.getCanonicalName());
                                        return;
                                    }
                                    com.blankj.utilcode.util.ToastUtils.showShort(R.string.playfun_sex_unknown);
                                    dialog.dismiss();
                                }
                            })
                            .chooseType(MVDialog.TypeEnum.CENTER)
                            .show();
                }
            }
        });
        viewModel.uc.signUpSucceed.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                MVDialog.getInstance(ProgramDetailFragment.this.getContext())
                        .setContent(getString(R.string.playfun_sign_up_after_call_you))
                        .chooseType(MVDialog.TypeEnum.CENTER)
                        .setConfirmText(getString(R.string.playfun_roger))
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
        viewModel.uc.clickReport.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                MVDialog.getInstance(ProgramDetailFragment.this.getContext())
                        .setContent(getString(R.string.playfun_confirm_report_title))
                        .chooseType(MVDialog.TypeEnum.CENTER)
                        .setConfirmText(getString(R.string.playfun_confirm_report))
                        .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
                            @Override
                            public void confirm(MVDialog dialog) {
                                viewModel.signUpReport((Integer) o);
                                dialog.dismiss();
                            }
                        })
                        .chooseType(MVDialog.TypeEnum.CENTER)
                        .show();
            }
        });
        viewModel.uc.clickImage.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                Integer position = Integer.valueOf(((Map<String, String>) o).get("position"));
                String listStr = ((Map<String, String>) o).get("images");
                List<String> images = GsonUtils.fromJson(listStr, new TypeToken<List<String>>() {
                }.getType());
                PictureSelectorUtil.previewImage(ProgramDetailFragment.this.getContext(), images, position);
            }
        });
        viewModel.uc.clickPayChat.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer coinPrice) {
                payCheckChat(coinPrice);
            }
        });

        viewModel.uc.clickVipChat.observe(this, integer -> vipCheckChat(integer, ConfigManager.getInstance().getImMoney()));
    }

    private void chooseAvatar() {
        PictureSelectorUtil.selectImage(mActivity, true, 1, new OnResultCallbackListener<LocalMedia>() {
            @Override
            public void onResult(List<LocalMedia> result) {
                viewModel.imagUpload(result.get(0).getCompressPath());
            }

            @Override
            public void onCancel() {
            }
        });
    }

    private void payCheckChat(Integer coinPrice) {
        String btn1 = "";
        String title = "";
        UserDataEntity userDataEntity = ConfigManager.getInstance().getAppRepository().readUserData();
        int sex = userDataEntity.getSex();
        if (sex == AppConfig.MALE) {
            title = getString(R.string.playfun_to_chat_her);
            btn1 = getString(R.string.playfun_to_be_member_free_chat);
        } else {
            title = getString(R.string.playfun_to_chat_he);
            if (userDataEntity.getCertification() == 1) {
                btn1 = getString(R.string.playfun_to_be_goddess_free_chat);
            } else {
                btn1 = getString(R.string.playfun_warn_no_certification);
            }
        }
        if (!(userDataEntity.getIsVip() == 1)) {
            MVDialog.getInstance(ProgramDetailFragment.this.getContext())
                    .setContent(title)
                    .setConfirmText(btn1)
                    .setConfirmTwoText(String.format(getString(R.string.playfun_paid_viewing_private_chat), coinPrice))
                    .setConfirmOnlick(dialog -> {
                        dialog.dismiss();
                        if (sex == AppConfig.MALE) {
                            viewModel.start(VipSubscribeFragment.class.getCanonicalName());
                        } else {
                            viewModel.start(CertificationFemaleFragment.class.getCanonicalName());
                        }
                    })
                    .setConfirmTwoOnclick(dialog -> {
                        dialog.dismiss();
                        showCoinPaySheet(false, userDataEntity.getId().toString());
                    })
                    .chooseType(MVDialog.TypeEnum.CENTER)
                    .show();
        } else {
            MVDialog.getInstance(ProgramDetailFragment.this.getContext())
                    .setContent(title)
                    .setConfirmText(String.format(getString(R.string.playfun_paid_viewing_private_chat), coinPrice))
                    .setConfirmOnlick(dialog -> {
                        dialog.dismiss();
                        showCoinPaySheet(false, userDataEntity.getId().toString());
                    })
                    .chooseType(MVDialog.TypeEnum.CENTER)
                    .show();
        }
    }

    private void showCoinPaySheet(boolean autoPay, String userIds) {
        new CoinPaySheet.Builder(mActivity).setPayParams(6, Integer.parseInt(userIds), getString(R.string.playfun_check_detail), autoPay, new CoinPaySheet.CoinPayDialogListener() {
            @Override
            public void onPaySuccess(CoinPaySheet sheet, String orderNo, Integer payPrice) {
                sheet.dismiss();
                ToastUtils.showShort(R.string.playfun_pay_success);
                viewModel.chatPaySuccess();
            }

            @Override
            public void onRechargeSuccess(GameCoinTopupSheetView gameCoinTopupSheetView) {
                // 充值成功，再次唤起浮层，且自动支付
                try {
                    AppContext.runOnUIThread(gameCoinTopupSheetView::dismiss, 100);
                    AppContext.runOnUIThread(() -> showCoinPaySheet(true, userIds), 500);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }).build().show();
    }

    private void vipCheckChat(Integer number, Integer coinPrice) {
        if (number <= 0) {
            // 实际上不会走到这个分支？
            MVDialog.getInstance(ProgramDetailFragment.this.getContext())
                    .setContent(String.format(getString(R.string.playfun_pay_diamond_content), coinPrice))
                    .setConfirmText(String.format(getString(R.string.playfun_pay_diamond), coinPrice))
                    .setConfirmOnlick(dialog -> {
                        dialog.dismiss();
                        new CoinPaySheet.Builder(mActivity).setPayParams(6, viewModel.userId, getString(R.string.playfun_check_detail), false, new CoinPaySheet.CoinPayDialogListener() {
                            @Override
                            public void onPaySuccess(CoinPaySheet sheet, String orderNo, Integer payPrice) {
                                sheet.dismiss();
                                ToastUtils.showShort(R.string.playfun_pay_success);
                                viewModel.chatPaySuccess();
                            }

                            @Override
                            public void onRechargeSuccess(GameCoinTopupSheetView gameCoinTopupSheetView) {
                                // do nothing
                            }
                        }).build().show();
                    })
                    .chooseType(MVDialog.TypeEnum.CENTER)
                    .show();
        } else {
            MVDialog.getInstance(ProgramDetailFragment.this.getContext())
                    .setContent(String.format(getString(R.string.playfun_use_one_chance_chat), number))
                    .setConfirmText(getString(R.string.playfun_use_one_chance))
                    .setConfirmOnlick(dialog -> {
                        dialog.dismiss();
                        viewModel.useVipChat(Integer.parseInt(viewModel.getUserId), 1, 1);
                    })
                    .chooseType(MVDialog.TypeEnum.CENTER)
                    .show();
        }
    }

}
