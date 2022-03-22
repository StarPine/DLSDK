package com.dl.playfun.ui.mine.trace.man;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.databinding.FragmentMineTraceManBinding;
import com.dl.playfun.event.TraceEmptyEvent;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.widget.coinpaysheet.CoinPaySheet;
import com.dl.playfun.widget.coinrechargesheet.GameCoinTopupSheetView;
import com.dl.playfun.widget.dialog.TraceDialog;

import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.jessyan.autosize.internal.CustomAdapt;

/**
 * Author: 彭石林
 * Time: 2021/8/4 12:26
 * Description: This is TraceManFragment
 */
public class TraceManFragment extends BaseToolbarFragment<FragmentMineTraceManBinding, TraeManViewModel> implements CustomAdapt {
    Dialog vipDialog = null;
    private Integer userId;

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return R.layout.fragment_mine_trace_man;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public TraeManViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(TraeManViewModel.class);
    }

    @Override
    public void initParam() {
        super.initParam();
        userId = getArguments().getInt("userId");
    }

    @Override
    public void initData() {
        super.initData();
//
//        BlurringView  blurredView = (BlurringView)binding.blurringViews;
//        View layout = binding.bottomLayout;
//        //给出了模糊视图并刷新模糊视图。
//        blurredView.setBlurredView(layout);
//        blurredView.invalidate();
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
        if (!hidden) {
            try {
                long before = System.currentTimeMillis();
                if (viewModel.expireTime - (before / 1000) > 0) {

                } else {
                    for (TraceManItemViewModel traeManViewModel : viewModel.observableList) {
                        traeManViewModel.isPlay = 0;
                    }
                    viewModel.adapter.notifyDataSetChanged();
                }

            } catch (Exception e) {
            }
        }
    }

    @Override
    public void initViewObservable() {
        RxBus.getDefault().post(new TraceEmptyEvent());
        viewModel.uc.loadRefresh.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {
                binding.title.setText(String.format(StringUtils.getString(R.string.playfun_mine_trace_man_title1),viewModel.totalCount));
                //binding.confirm.setText(String.format(StringUtils.getString(R.string.mine_trace_man_title3), viewModel.totalCount));
                if (viewModel.isPlay != null && viewModel.isPlay == 1) {
                    binding.refreshLayout.setEnableRefresh(true);
                    binding.refreshLayout.setEnableLoadMore(true);
                    binding.btnConfirm.setVisibility(View.GONE);
                    AppContext.instance().logEvent(AppsFlyerEvent.visitor_unlocked);
                } else {
                    binding.refreshLayout.setEnableRefresh(false);
                    binding.refreshLayout.setEnableLoadMore(false);
                    binding.btnConfirm.setVisibility(View.VISIBLE);
                    AppContext.instance().logEvent(AppsFlyerEvent.visitor_locked);
                }
                if (ObjectUtils.isEmpty(viewModel.observableList) || viewModel.observableList.size() < 1) {
                    binding.btnConfirm.setVisibility(View.GONE);
                }
            }
        });
        viewModel.uc.clickVip.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {
                if (vipDialog != null && vipDialog.isShowing()) {
                    return;
                }
                vipDialog = TraceDialog.getInstance(TraceManFragment.this.getContext())
                        .chooseType(TraceDialog.TypeEnum.CENTER)
                        .setConfirmText(String.format(StringUtils.getString(R.string.playfun_mine_trace_man_play_confirm), ConfigManager.getInstance().GetViewUseBrowseMoney()))
                        .setConfirmOnlick(new TraceDialog.ConfirmOnclick() {
                            @Override
                            public void confirm(Dialog dialog) {
                                dialog.dismiss();
                                new CoinPaySheet.Builder(mActivity).setPayParams(13, userId, getString(R.string.playfun_mine_trace_man_play_title), false, new CoinPaySheet.CoinPayDialogListener() {
                                    @Override
                                    public void onPaySuccess(CoinPaySheet sheet, String orderNo, Integer payPrice) {
                                        sheet.dismiss();
                                        AppContext.instance().logEvent(AppsFlyerEvent.unlock_my_visitor);
                                        ToastUtils.showShort(R.string.playfun_pay_success);
                                        viewModel.isPlay = 1;
                                        viewModel.currentPage = 1;
                                        binding.btnConfirm.setVisibility(View.GONE);
                                        viewModel.loadDatas(1);
                                    }

                                    @Override
                                    public void onRechargeSuccess(GameCoinTopupSheetView gameCoinTopupSheetView) {
                                        // do nothing
                                    }
                                }).build().show();
                            }
                        }).TraceVipDialog();
                vipDialog.show();
            }
        });

        viewModel.uc.startRefreshing.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                //结束刷新
                binding.refreshLayout.autoRefresh();
            }
        });

        //监听下拉刷新完成
        viewModel.uc.finishRefreshing.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                //结束刷新
                binding.refreshLayout.finishRefresh(100);
            }
        });

        //监听上拉加载完成
        viewModel.uc.finishLoadmore.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                //结束刷新
                binding.refreshLayout.finishLoadMore(100);
            }
        });
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
