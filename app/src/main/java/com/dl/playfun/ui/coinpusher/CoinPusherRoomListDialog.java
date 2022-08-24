package com.dl.playfun.ui.coinpusher;

import android.app.Activity;
import android.app.Dialog;
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
import com.dl.playfun.databinding.DialogCityChooseBinding;
import com.dl.playfun.databinding.DialogCoinpusherListBinding;
import com.dl.playfun.entity.CoinPusherRoomInfoEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseDialog;

import java.util.List;

import me.goldze.mvvmhabit.binding.viewadapter.recyclerview.LayoutManagers;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * Author: 彭石林
 * Time: 2022/8/19 11:16
 * Description: 推币机弹窗选择页面
 */
public class CoinPusherRoomListDialog extends BaseDialog {
    private DialogCoinpusherListBinding binding;
    private final Context mContext;
    private CoinPusherRoomTagAdapter coinPusherRoomTagAdapter;
    private CoinPusherRoomListAdapter coinPusherRoomListAdapter;

    public CoinPusherRoomListDialog(Activity activity) {
        super(activity);
        this.mContext = activity;
        super.setMActivity(activity);
        initView();
        loadData();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_coinpusher_list, null, false);
        //支持LiveData绑定xml，数据改变，UI自动会更新
        binding.setLifecycleOwner(this);
        // 设备等级分类
        LayoutManagers.LayoutManagerFactory rcvTitleLayoutManager= LayoutManagers.linear(LinearLayoutManager.HORIZONTAL,false);
        binding.rcvTitle.setLayoutManager(rcvTitleLayoutManager.create(binding.rcvTitle));
        coinPusherRoomTagAdapter = new CoinPusherRoomTagAdapter();
        binding.rcvTitle.setAdapter(coinPusherRoomTagAdapter);
        //表格布局
        LayoutManagers.LayoutManagerFactory layoutManagerFactory = LayoutManagers.grid(2);
        binding.rcvContent.setLayoutManager(layoutManagerFactory.create(binding.rcvContent));
        coinPusherRoomListAdapter = new CoinPusherRoomListAdapter();
        binding.rcvContent.setAdapter(coinPusherRoomListAdapter);
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

    public void loadData() {
        ConfigManager.getInstance().getAppRepository().qryCoinPusherRoomList()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(dispose -> showHud())
                .subscribe(new BaseObserver<BaseDataResponse<CoinPusherRoomInfoEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CoinPusherRoomInfoEntity> coinPusherRoomInfoEntityResponse) {
                        CoinPusherRoomInfoEntity coinPusherRoomInfoEntity = coinPusherRoomInfoEntityResponse.getData();
                        List<CoinPusherRoomInfoEntity.DeviceTag> deviceTagList = coinPusherRoomInfoEntity.getTypeArr();
                        if(ObjectUtils.isNotEmpty(deviceTagList)){
                            coinPusherRoomTagAdapter.setItemData(deviceTagList);
                        }
                    }
                    @Override
                    public void onComplete() {
                        dismissHud();
                    }
                });

    }
}
