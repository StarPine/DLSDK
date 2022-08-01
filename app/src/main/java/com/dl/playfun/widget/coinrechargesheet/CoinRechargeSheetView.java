package com.dl.playfun.widget.coinrechargesheet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dl.playfun.R;
import com.dl.playfun.app.Injection;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.CoinWalletEntity;
import com.dl.playfun.entity.GoodsEntity;
import com.dl.playfun.ui.base.BasePopupWindow;
import com.dl.playfun.ui.dialog.adapter.CoinRechargeAdapter;
import com.dl.playfun.ui.mine.wallet.recharge.RechargeActivity;

import java.util.List;

import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author litchi
 */
public class CoinRechargeSheetView extends BasePopupWindow implements View.OnClickListener, CoinRechargeAdapter.CoinRechargeAdapterListener {
    public static final String TAG = "CoinRechargeSheetView";

    private final AppCompatActivity mActivity;
    private View mPopView;
    private RecyclerView recyclerView;
    private TextView tvBalance;
    private ImageView ivRefresh;
    private ViewGroup loadingView;
    private CoinRechargeAdapter adapter;
    private List<GoodsEntity> mGoodsList;

    public CoinRechargeSheetView(AppCompatActivity activity) {
        super(activity);
        this.mActivity = activity;
        init(activity);
        setPopupWindow();
    }

    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        mPopView = inflater.inflate(R.layout.view_coin_recharge_sheet, null);
        recyclerView = mPopView.findViewById(R.id.recycler_view);
        tvBalance = mPopView.findViewById(R.id.tv_balance);
        ivRefresh = mPopView.findViewById(R.id.iv_refresh);
        loadingView = mPopView.findViewById(R.id.rl_loading);
        ivRefresh.setOnClickListener(this);

        adapter = new CoinRechargeAdapter(recyclerView);
        adapter.setCoinRechargeAdapterListener(this);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        recyclerView.setAdapter(adapter);

        loadBalance();
        //查询商品价格
        loadGoods();
    }

    /**
     * 设置窗口的相关属性
     */
    @SuppressLint("InlinedApi")
    private void setPopupWindow() {
        this.setContentView(mPopView);// 设置View
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);// 设置弹出窗口的宽
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);// 设置弹出窗口的高
        this.setOutsideTouchable(false);//设置popupwindow外部可点击
        this.setFocusable(true);// 设置弹出窗口可
        this.setTouchable(true);
        this.setAnimationStyle(R.style.popup_window_anim);// 设置动画
        this.setBackgroundDrawable(new ColorDrawable(0x00000000));// 设置背景透明
    }

    @Override
    public void dismiss() {
        if (mActivity != null && !mActivity.isFinishing()) {
            Window dialogWindow = mActivity.getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            lp.alpha = 1.0f;
            dialogWindow.setAttributes(lp);
        }
        super.dismiss();
    }

    public void show() {
        super.showLifecycle();
        if (mActivity != null && !mActivity.isFinishing()) {
            Window dialogWindow = mActivity.getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            lp.alpha = 0.5f;
            dialogWindow.setAttributes(lp);
        }
        this.showAtLocation(mActivity.getWindow().getDecorView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    @SuppressLint("StringFormatMatches")
    private void loadBalance() {
        Injection.provideDemoRepository()
                .coinWallet()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<CoinWalletEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CoinWalletEntity> response) {
                        tvBalance.setText(String.format(mActivity.getResources().getString(R.string.playfun_x_coin), response.getData().getTotalCoin()));
                    }

                    @Override
                    public void onError(RequestException e) {
                        ToastUtils.showShort(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                    }
                });
    }

    private void loadGoods() {
        Injection.provideDemoRepository()
                .goods()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<List<GoodsEntity>>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<List<GoodsEntity>> response) {
                        loadingView.setVisibility(View.GONE);
                        mGoodsList = response.getData();
                        adapter.setData(mGoodsList);
//                        querySkuList(response.getData());
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_refresh) {
            loadBalance();
            loadGoods();
        }
    }

    @Override
    public void onBuyClick(View view, int position) {
        GoodsEntity goodsEntity = mGoodsList.get(position);
        startActivity(goodsEntity);
        dismiss();
    }

    private void startActivity(GoodsEntity goodsEntity) {
        Intent intent = new Intent(mActivity, RechargeActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("Goods_info", goodsEntity);
        intent.putExtras(bundle);
        mActivity.startActivity(intent);
    }
}