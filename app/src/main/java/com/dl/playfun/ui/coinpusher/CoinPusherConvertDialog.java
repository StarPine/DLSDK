package com.dl.playfun.ui.coinpusher;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.R;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.databinding.DialogCoinpusherConverBinding;
import com.dl.playfun.entity.CoinPusherConverInfoEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseDialog;

import me.goldze.mvvmhabit.binding.viewadapter.recyclerview.LayoutManagers;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * Author: 彭石林
 * Time: 2022/8/23 12:22
 * Description: This is CoinPusherConvertDialog
 */
public class CoinPusherConvertDialog  extends BaseDialog {

    private final Context mContext;
    private DialogCoinpusherConverBinding binding;
    //宝盒适配器
    private CoinPusherCapsuleAdapter coinPusherCapsuleAdapter;

    private int SEL_COIN_PUSHER_CAPSULE  = -1;

    public CoinPusherConvertDialog(Activity activity) {
        super(activity);
        this.mContext = activity;
        super.setMActivity(activity);
        initView();
        onClickListener();
        loadData();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_coinpusher_conver, null, false);
        //支持LiveData绑定xml，数据改变，UI自动会更新
        binding.setLifecycleOwner(this);
        //表格布局
        LayoutManagers.LayoutManagerFactory layoutManagerFactory = LayoutManagers.grid(2);
        binding.rcvCapsule.setLayoutManager(layoutManagerFactory.create(binding.rcvCapsule));
        coinPusherCapsuleAdapter = new CoinPusherCapsuleAdapter();
        binding.rcvCapsule.setAdapter(coinPusherCapsuleAdapter);
    }
    //点击事件初始化
    private void onClickListener(){
        //关闭当前弹窗
        binding.imgClose.setOnClickListener(v-> dismiss());
        binding.tvTabCapsule.setOnClickListener(v->{
            if(binding.llCapsuleLayout.getVisibility() == View.GONE){
                binding.tvTabConver.setTextColor(ColorUtils.getColor(R.color.play_chat_gray_3));
                binding.tvTabCapsule.setTextColor(ColorUtils.getColor(R.color.black));
                binding.llCapsuleLayout.setVisibility(View.VISIBLE);
                binding.llConverLayout.setVisibility(View.GONE);
            }
        });
        binding.tvTabConver.setOnClickListener(v->{
            if(binding.llConverLayout.getVisibility() == View.GONE){
                binding.tvTabCapsule.setTextColor(ColorUtils.getColor(R.color.play_chat_gray_3));
                binding.tvTabConver.setTextColor(ColorUtils.getColor(R.color.black));
                binding.llConverLayout.setVisibility(View.VISIBLE);
                binding.llCapsuleLayout.setVisibility(View.GONE);
            }
        });

        coinPusherCapsuleAdapter.setOnItemClickListener(position -> {
            if(SEL_COIN_PUSHER_CAPSULE!=-1){
                if(SEL_COIN_PUSHER_CAPSULE!=position){
                    coinPusherCapsuleAdapter.setDefaultSelect(position);
                    SEL_COIN_PUSHER_CAPSULE = position;
                }
            }else{
                SEL_COIN_PUSHER_CAPSULE = position;
                coinPusherCapsuleAdapter.setDefaultSelect(SEL_COIN_PUSHER_CAPSULE);
            }

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

    private void loadData(){
        ConfigManager.getInstance().getAppRepository().qryCoinPusherConverList()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHud())
                .subscribe(new BaseObserver<BaseDataResponse<CoinPusherConverInfoEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<CoinPusherConverInfoEntity> converInfoEntityResponse) {
                        CoinPusherConverInfoEntity coinPusherConvertInfo = converInfoEntityResponse.getData();
                        if(ObjectUtils.isNotEmpty(coinPusherConvertInfo)){
                            //给宝盒列表数据-展示
                            if(ObjectUtils.isNotEmpty(coinPusherConvertInfo.getGoldCoinList())){
                                coinPusherCapsuleAdapter.setItemData(coinPusherConvertInfo.getGoldCoinList());
                                binding.tvCapsuleHint.setText(coinPusherConvertInfo.getGoldTips());
                            }
                        }
                    }
                    @Override
                    public void onComplete() {
                        dismissHud();
                    }
                });

    }

    @Override
    public void showHud() {
        super.showHud();
    }

    @Override
    public void dismissHud() {
        super.dismissHud();
    }
}
