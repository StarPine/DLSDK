package com.dl.playfun.ui.coinpusher.dialog;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.dl.playfun.widget.recyclerview.LineManagers;

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

    private final Integer roomId;
    private final CoinPusherRoomDeviceInfo coinPusherRoomDeviceInfo;

    private CoinPusherGameHistoryAdapter coinPusherCapsuleAdapter;

    public CoinPusherGameHistoryDialog(Context context,CoinPusherRoomDeviceInfo coinPusherRoomDeviceInfo) {
        super(context);
        this.mContext = context;
        this.roomId = coinPusherRoomDeviceInfo.getRoomId();
        this.coinPusherRoomDeviceInfo = coinPusherRoomDeviceInfo;
        initView();
        startRefreshDataInfo();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_coinpusher_list_history, null, false);
        if(coinPusherRoomDeviceInfo!=null){
            int levelId = coinPusherRoomDeviceInfo.getLevelId();
            String nickname = coinPusherRoomDeviceInfo.getNickname();
            binding.tvTitle.setText(String.format(StringUtils.getString(R.string.playfun_coinpusher_history_text2),String.valueOf(levelId+" "+nickname)));
        }
        //支持LiveData绑定xml，数据改变，UI自动会更新
        binding.setLifecycleOwner(this);
        //行布局
        LayoutManagers.LayoutManagerFactory layoutManagerFactory= LayoutManagers.linear();
        binding.rcvList.setLayoutManager(layoutManagerFactory.create(binding.rcvList));
        binding.rcvList.addItemDecoration(LineManagers.horizontal(1,15,0).create(binding.rcvList));
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
