package com.dl.playfun.ui.coinpusher.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import androidx.databinding.DataBindingUtil;

import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.R;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.databinding.DialogCoinpusherListHistoryBinding;
import com.dl.playfun.entity.CoinPusherRoomDeviceInfo;
import com.dl.playfun.entity.CoinPusherRoomHistoryEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseDialog;
import com.dl.playfun.ui.coinpusher.dialog.adapter.CoinPusherGameHistoryAdapter;
import com.dl.playfun.viewadapter.CustomRefreshHeader;

import java.lang.reflect.Field;
import java.util.List;

import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.StringUtils;
import me.tatarka.bindingcollectionadapter2.LayoutManagers;

/**
 * Author: 彭石林
 * Time: 2022/8/26 15:13
 * Description: This is CoinPusherGameHistroyDialog
 */
public class CoinPusherGameHistoryDialog extends BaseDialog {

    private DialogCoinpusherListHistoryBinding binding;

    private final Context mContext;

    private Integer roomId;
    private int levelId;
    private String nickname;

    private CoinPusherGameHistoryAdapter coinPusherCapsuleAdapter;

    public CoinPusherGameHistoryDialog(Context context,Integer _roomId,int _levelId,String _nickname) {
        super(context);
        this.mContext = context;
        this.roomId = _roomId;
        this.levelId = _levelId;
        this.nickname = _nickname;
        initView();
        startRefreshDataInfo();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_coinpusher_list_history, null, false);
        if(!TextUtils.isEmpty(nickname)){
            binding.tvTitle.setText(String.format(StringUtils.getString(R.string.playfun_coinpusher_history_text2), levelId + " " + nickname));
        }
        //支持LiveData绑定xml，数据改变，UI自动会更新
        binding.setLifecycleOwner(this);
        //行布局
        LayoutManagers.LayoutManagerFactory layoutManagerFactory= LayoutManagers.linear();
        binding.rcvList.setLayoutManager(layoutManagerFactory.create(binding.rcvList));
        coinPusherCapsuleAdapter = new CoinPusherGameHistoryAdapter();
        binding.rcvList.setAdapter(coinPusherCapsuleAdapter);
        binding.imgClose.setOnClickListener(v ->dismiss());
        CustomRefreshHeader customRefreshHeader = new CustomRefreshHeader(getContext());
        customRefreshHeader.setTvContent(StringUtils.getString(R.string.playfun_coinpusher_history_text1));
        binding.refreshLayout.setRefreshHeader(customRefreshHeader);
        binding.refreshLayout.setEnableLoadMore(false);
        binding.refreshLayout.setOnRefreshListener(v->startRefreshDataInfo());
    }

    public void show() {
        //设置背景透明,去四个角
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(binding.getRoot());
        //设置宽度充满屏幕
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.setWindowAnimations(R.style.BottomDialog_Animation);
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        window.setStatusBarColor(Color.TRANSPARENT);
        // 解决 状态栏变色的bug
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    @SuppressLint("PrivateApi")
                    Class<?> decorViewClazz = Class.forName("com.android.internal.policy.DecorView");
                    Field field = decorViewClazz.getDeclaredField("mSemiTransparentStatusBarColor");
                    field.setAccessible(true);
                    // 去掉高版本蒙层改为透明
                    field.setInt(window.getDecorView(), Color.TRANSPARENT);
                } catch (Exception ignored) {
                }
            }
        }
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }


    @Override
    public void showHud() {
        super.showHud();
    }

    @Override
    public void dismissHud() {
        super.dismissHud();
    }

    public void loadData(Integer roomId){
        ConfigManager.getInstance().getAppRepository().qryCoinPusherRoomHistory(roomId)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(dismiss -> showHud())
                .subscribe(new BaseObserver<BaseDataResponse<List<CoinPusherRoomHistoryEntity>>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<List<CoinPusherRoomHistoryEntity>> listBaseDataResponse) {
                        List<CoinPusherRoomHistoryEntity> listData = listBaseDataResponse.getData();
                        if(ObjectUtils.isNotEmpty(listData)){
                            coinPusherCapsuleAdapter.setItemData(listData);
                        }

                    }
                    @Override
                    public void onComplete() {
                        dismissHud();
                        stopRefreshOrLoadMore();
                    }
                });
    }

    private void startRefreshDataInfo(){
        binding.refreshLayout.autoRefresh();
        loadData(roomId);
    }
    private void stopRefreshOrLoadMore(){
        //结束刷新
        binding.refreshLayout.finishRefresh(true);
    }

}
