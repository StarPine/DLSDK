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
import com.dl.playfun.entity.CoinPusherRoomHistoryEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseDialog;
import com.dl.playfun.ui.coinpusher.dialog.adapter.CoinPusherGameHistoryAdapter;
import com.dl.playfun.widget.recyclerview.LineManagers;

import java.util.List;

import me.goldze.mvvmhabit.binding.viewadapter.recyclerview.LayoutManagers;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * Author: 彭石林
 * Time: 2022/8/26 15:13
 * Description: This is CoinPusherGameHistroyDialog
 */
public class CoinPusherGameHistoryDialog extends BaseDialog {

    private DialogCoinpusherListHistoryBinding binding;

    private final Context mContext;

    private Integer roomId;

    private CoinPusherGameHistoryAdapter coinPusherCapsuleAdapter;

    public CoinPusherGameHistoryDialog(Activity activity,Integer roomId) {
        super(activity);
        super.setMActivity(activity);
        this.mContext = activity;
        this.roomId = roomId;
        initView();
        loadData(roomId);
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_coinpusher_list_history, null, false);
        //支持LiveData绑定xml，数据改变，UI自动会更新
        binding.setLifecycleOwner(this);
        //行布局
        LayoutManagers.LayoutManagerFactory layoutManagerFactory= LayoutManagers.linear(LinearLayoutManager.VERTICAL,false);
        binding.rcvList.setLayoutManager(layoutManagerFactory.create(binding.rcvList));
        binding.rcvList.addItemDecoration(LineManagers.horizontal(1,15,0).create(binding.rcvList));
        coinPusherCapsuleAdapter = new CoinPusherGameHistoryAdapter();
        binding.rcvList.setAdapter(coinPusherCapsuleAdapter);
        binding.imgClose.setOnClickListener(v ->dismiss());
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
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
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
                    }
                });
    }

}
