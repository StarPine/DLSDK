package com.dl.playfun.ui.mine.trace.list;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.ui.mine.trace.TraceViewModel;
import com.dl.playfun.ui.radio.issuanceprogram.IssuanceProgramFragment;
import com.dl.playfun.widget.dialog.TraceDialog;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.databinding.FragmentTraceListBinding;

import me.goldze.mvvmhabit.utils.StringUtils;

/**
 * Author: 彭石林
 * Time: 2021/8/3 11:32
 * Description: This is TraceListFragment
 */
public class TraceListFragment extends BaseToolbarFragment<FragmentTraceListBinding, TraceListViewModel> {
    public static final String ARG_HOME_LIST_GENDER = "arg_trace_list_gender";

    private int grends;
    private TraceViewModel traceViewModel;

    public static TraceListFragment newInstance(int grend) {
        TraceListFragment fragment = new TraceListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_HOME_LIST_GENDER, grend);
        fragment.setArguments(bundle);
        return fragment;
    }

    public TraceViewModel getTraceViewModel() {
        return traceViewModel;
    }

    public void setTraceListViewModel(TraceViewModel traceViewModel) {
        this.traceViewModel = traceViewModel;
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_trace_list;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initParam() {
        super.initParam();
        grends = getArguments().getInt(ARG_HOME_LIST_GENDER, 0);
    }

    @Override
    public TraceListViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        TraceListViewModel traceListViewModel = ViewModelProviders.of(this, factory).get(TraceListViewModel.class);
        traceListViewModel.grend = grends;
        traceListViewModel.loadDatas(1);
        if (grends == 0) {
            traceListViewModel.stateModel.emptyText.set(StringUtils.getString(R.string.playfun_mine_trace_empty));
        } else {
            traceListViewModel.stateModel.emptyText.set(StringUtils.getString(R.string.playfun_mine_fans_empty));
        }
        return traceListViewModel;
    }

    @Override
    public void initViewObservable() {
        viewModel.traceViewModel = this.traceViewModel;
        viewModel.uc.emptyText.observe(this,unused -> {
            if (grends == 1){
                String content = binding.empty.tvMsg.getText().toString();
                String target = mActivity.getString(R.string.playfun_mine_fans_empty2);
                SpannableString spannableString = new SpannableString(content);
                setServiceTips(spannableString, binding.empty.tvMsg, content, target);
            }
        });
        viewModel.uc.clickDelLike.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer unused) {
                TraceDialog.getInstance(TraceListFragment.this.getContext())
                        .setTitle(getString(R.string.playfun_mine_trace_delike))
                        .setCannelText(getString(R.string.playfun_mine_trace_delike_cannel))
                        .setConfirmText(getString(R.string.playfun_mine_trace_delike_confirm))
                        .chooseType(TraceDialog.TypeEnum.CENTER)
                        .setConfirmOnlick(new TraceDialog.ConfirmOnclick() {
                            @Override
                            public void confirm(Dialog dialog) {
                                dialog.dismiss();
                                viewModel.delLike(unused);
                            }
                        }).show();
            }
        });
        viewModel.uc.loadRefresh.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {
                //binding.refreshLayout.finishRefreshWithNoMoreData();
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

    private SpannableString setServiceTips(SpannableString spannableString, TextView tvTips, String content, String key) {
        UnderlineSpan colorSpan = new UnderlineSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(mActivity.getResources().getColor(R.color.colorAccent));//设置颜色
//                ds.setUnderlineText(false); //去掉下划线
            }
        };
        ClickableSpan clickableSpan = new ClickableSpan() {

            @Override
            public void onClick(@NonNull View widget) {
                    viewModel.start(IssuanceProgramFragment.class.getCanonicalName());
            }
        };
        int tips = content.indexOf(key);
        spannableString.setSpan(clickableSpan, tips, tips + key.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        spannableString.setSpan(colorSpan, tips, tips + key.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        tvTips.setMovementMethod(LinkMovementMethod.getInstance());
        tvTips.setText(spannableString);
        return spannableString;
    }
}
