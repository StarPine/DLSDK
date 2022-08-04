package com.dl.playfun.widget.coinrechargesheet;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.app.Injection;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.CoinWalletEntity;
import com.dl.playfun.entity.GoodsEntity;
import com.dl.playfun.ui.dialog.BaseDialogFragment;
import com.dl.playfun.ui.dialog.adapter.CoinRechargeAdapter;
import com.dl.playfun.ui.mine.wallet.recharge.RechargeActivity;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Author: 彭石林
 * Time: 2022/8/3 18:44
 * Description: This is CoinRechargeFragmentView
 */
public class CoinRechargeFragmentView extends BaseDialogFragment implements View.OnClickListener,CoinRechargeAdapter.CoinRechargeAdapterListener{
    private final AppCompatActivity mActivity;
    private RecyclerView recyclerView;
    private TextView tvBalance;
    private ImageView ivRefresh;
    private CoinRechargeAdapter adapter;
    private List<GoodsEntity> mGoodsList;

    public CoinRechargeFragmentView(AppCompatActivity activity){
        this.mActivity = activity;
    }
    /**
     * 初始化
     *
     */
    private void init(View viewCurrent) {
        recyclerView = viewCurrent.findViewById(R.id.recycler_view);
        tvBalance = viewCurrent.findViewById(R.id.tv_balance);
        ivRefresh = viewCurrent.findViewById(R.id.iv_refresh);
        ivRefresh.setOnClickListener(this);

        adapter = new CoinRechargeAdapter(recyclerView);
        adapter.setCoinRechargeAdapterListener(this);
        recyclerView.setLayoutManager(new GridLayoutManager(viewCurrent.getContext(), 2));
        recyclerView.setAdapter(adapter);
        loadBalance();
        //查询商品价格
        loadGoods();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.MyDialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
        }
        mWindow.setGravity(Gravity.BOTTOM);
        mWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setBackgroundDrawableResource(R.color.transparent);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    @Override
    protected int setLayoutId() {
        return R.layout.view_coin_recharge_sheet;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    /**
     * 初始化数据
     */
    @Override
    protected void initData() {

    }

    @Override
    public void onBuyClick(View view, int position) {
        Intent intent = new Intent(mActivity, RechargeActivity.class);
        toGooglePlayIntent.launch(intent);
    }

    ActivityResultLauncher<Intent> toGooglePlayIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getData() != null) {

        }
    });

    @SuppressLint("StringFormatMatches")
    private void loadBalance() {
        Injection.provideDemoRepository()
                .coinWallet()
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<CoinWalletEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CoinWalletEntity> response) {
                        tvBalance.setText(String.format(StringUtils.getString(R.string.playfun_x_coin), response.getData().getTotalCoin()));
                    }

                    @Override
                    public void onError(RequestException e) {
                        ToastUtils.showShort(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        dismiss();
                    }
                });
    }

    private void loadGoods() {
        Injection.provideDemoRepository()
                .goods()
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<List<GoodsEntity>>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<List<GoodsEntity>> response) {
                        mGoodsList = response.getData();
                        //adapter.setData(mGoodsList);
//                        querySkuList(response.getData());
                    }

                    @Override
                    public void onComplete() {
                        dismiss();
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
}
