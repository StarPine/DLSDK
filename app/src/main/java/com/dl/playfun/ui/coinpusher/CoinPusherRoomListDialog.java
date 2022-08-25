package com.dl.playfun.ui.coinpusher;

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
import com.dl.playfun.databinding.DialogCoinpusherListBinding;
import com.dl.playfun.entity.CoinPusherRoomInfoEntity;
import com.dl.playfun.entity.CoinPusherRoomTagInfoEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseDialog;
import com.dl.playfun.viewadapter.CustomRefreshHeader;

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
    //当前用户金币余额
    private int totalMoney = 0;

    private int SEL_COIN_PUSHER_TAG_IDX = -1;

    private Integer checkedTagId = null;

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
        coinPusherRoomTagAdapter.setOnItemClickListener(position -> {
            if(SEL_COIN_PUSHER_TAG_IDX !=-1){
                if(SEL_COIN_PUSHER_TAG_IDX !=position){
                    coinPusherRoomTagAdapter.setDefaultSelect(position);
                    SEL_COIN_PUSHER_TAG_IDX = position;
                    checkedTagId = coinPusherRoomTagAdapter.getItemData(SEL_COIN_PUSHER_TAG_IDX).getId();
                }
            }else{
                SEL_COIN_PUSHER_TAG_IDX = position;
                coinPusherRoomTagAdapter.setDefaultSelect(SEL_COIN_PUSHER_TAG_IDX);
                checkedTagId = coinPusherRoomTagAdapter.getItemData(SEL_COIN_PUSHER_TAG_IDX).getId();
            }
            startRefreshDataInfo();
        });
        binding.rcvTitle.setAdapter(coinPusherRoomTagAdapter);
        //表格布局
        LayoutManagers.LayoutManagerFactory layoutManagerFactory = LayoutManagers.grid(2);
        binding.rcvContent.setLayoutManager(layoutManagerFactory.create(binding.rcvContent));
        coinPusherRoomListAdapter = new CoinPusherRoomListAdapter();
        binding.rcvContent.setAdapter(coinPusherRoomListAdapter);

        binding.refreshLayout.setRefreshHeader(new CustomRefreshHeader(getContext()));
        binding.refreshLayout.setEnableLoadMore(true);
        binding.refreshLayout.setOnRefreshListener(v->startRefreshDataInfo());

        binding.imgConvert.setOnClickListener(v ->{
            CoinPusherConvertDialog coinPusherConvertDialog = new CoinPusherConvertDialog(getMActivity());
            coinPusherConvertDialog.show();
        });
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
        ConfigManager.getInstance().getAppRepository().qryCoinPusherRoomTagList()
                 .doOnSubscribe(this)
                 .compose(RxUtils.schedulersTransformer())
                 .compose(RxUtils.exceptionTransformer())
                 .doOnSubscribe(dispose -> showHud())
                 .subscribe(new BaseObserver<BaseDataResponse<CoinPusherRoomTagInfoEntity>>() {
                     @Override
                     public void onSuccess(BaseDataResponse<CoinPusherRoomTagInfoEntity> coinPusherRoomTagInfoEntityResponse) {
                         CoinPusherRoomTagInfoEntity coinPusherRoomTagInfoEntity = coinPusherRoomTagInfoEntityResponse.getData();
                         if(ObjectUtils.isNotEmpty(coinPusherRoomTagInfoEntity)){
                             List<CoinPusherRoomTagInfoEntity.DeviceTag> deviceTagList = coinPusherRoomTagInfoEntity.getTypeArr();
                             if(ObjectUtils.isNotEmpty(deviceTagList)){
                                 coinPusherRoomTagAdapter.setItemData(deviceTagList);
                                 int idx = 0;
                                 for (CoinPusherRoomTagInfoEntity.DeviceTag tagData : deviceTagList) {
                                     if(tagData.getIsRecommend() ==1 ){
                                         checkedTagId = tagData.getId();
                                         break;
                                     }
                                     idx++;
                                 }
                                 coinPusherRoomTagAdapter.setDefaultSelect(idx);
                                 startRefreshDataInfo();
                             }
                         }
                     }
                 });
    }

    private void qryCoinPusherRoomList(Integer tagId){
        binding.refreshLayout.autoRefresh();
        ConfigManager.getInstance().getAppRepository().qryCoinPusherRoomList(tagId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(dispose -> showHud())
                .subscribe(new BaseObserver<BaseDataResponse<CoinPusherRoomInfoEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CoinPusherRoomInfoEntity> coinPusherRoomInfoEntityResponse) {
                        CoinPusherRoomInfoEntity coinPusherRoomInfoEntity = coinPusherRoomInfoEntityResponse.getData();
                        totalMoney = coinPusherRoomInfoEntity.getTotalGold();
                        tvTotalMoneyRefresh();
                        List<CoinPusherRoomInfoEntity.DeviceInfo> deviceInfoList = coinPusherRoomInfoEntity.getList();
                        if(ObjectUtils.isNotEmpty(deviceInfoList)){
                            coinPusherRoomListAdapter.setItemData(deviceInfoList);
                        }
                    }
                    @Override
                    public void onComplete() {
                        dismissHud();
                        stopRefreshOrLoadMore();
                    }
                });
    }

    private void tvTotalMoneyRefresh(){
        binding.tvTotalMoney.setText(totalMoney > 99999 ? totalMoney+"+" : totalMoney+"");
    }

    private void startRefreshDataInfo(){
        qryCoinPusherRoomList(checkedTagId);
    }
    private void stopRefreshOrLoadMore(){
        //结束刷新
        binding.refreshLayout.finishRefresh(true);
    }
}
