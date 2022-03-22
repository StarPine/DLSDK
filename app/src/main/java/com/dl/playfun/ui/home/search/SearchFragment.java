package com.dl.playfun.ui.home.search;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.airbnb.lottie.LottieAnimationView;
import com.blankj.utilcode.util.KeyboardUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.databinding.FragmentSearchBinding;
import com.dl.playfun.entity.CoinExchangePriceInfo;
import com.dl.playfun.ui.base.BaseRefreshToolbarFragment;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.widget.coinrechargesheet.GameCoinExchargeSheetView;

import me.goldze.mvvmhabit.utils.ToastUtils;
import me.jessyan.autosize.internal.CustomAdapt;

/**
 * 搜索
 *
 * @author wulei
 */
public class SearchFragment extends BaseRefreshToolbarFragment<FragmentSearchBinding, SearchViewModel> implements CustomAdapt {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ImmersionBarUtils.setupStatusBar(this, true, false);
        return view;
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_search;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public SearchViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(SearchViewModel.class);
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        super.onEnterAnimationEnd(savedInstanceState);
        binding.edtSearch.requestFocus();
        KeyboardUtils.showSoftInput();
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.sendAccostFirstError.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {
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
                        ToastUtils.showShort(msg);
                        AppContext.instance().logEvent(AppsFlyerEvent.Failed_to_top_up);
                    }
                });
            }
        });
        //播放搭讪动画
        viewModel.loadLoteAnime.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer position) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) binding.rcvLayout.getLayoutManager();
                final View child = layoutManager.findViewByPosition(position);
                if (child != null) {
                    LottieAnimationView itemLottie = child.findViewById(R.id.item_lottie);
                    if (itemLottie != null) {
                        itemLottie.setImageAssetsFolder("images/");
                        itemLottie.addAnimatorListener(new AnimatorListenerAdapter() {

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                itemLottie.removeAnimatorListener(this);
                                itemLottie.setVisibility(View.GONE);
                            }
                        });
                        if (!itemLottie.isAnimating()) {
                            itemLottie.setVisibility(View.VISIBLE);
                            itemLottie.setAnimation(R.raw.accost_animation);
                            itemLottie.playAnimation();
                        }
                    }
                }
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
