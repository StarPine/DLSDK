package com.dl.playfun.ui.coinpusher.dialog;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import androidx.databinding.DataBindingUtil;

import com.dl.playfun.R;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.databinding.DialogCoinpusherConverDetailBinding;
import com.dl.playfun.entity.CoinPusherConverInfoEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseDialog;
import com.dl.playfun.ui.coinpusher.dialog.adapter.CoinPusherCapsuleADetailAdapter;
import com.dl.playfun.utils.ToastCenterUtils;

import java.util.List;

import me.goldze.mvvmhabit.binding.viewadapter.recyclerview.LayoutManagers;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * Author: 彭石林
 * Time: 2022/8/25 14:35
 * Description: This is CoinPusherConvertCapsuleDialog
 */
public class CoinPusherConvertCapsuleDialog extends BaseDialog {

    private final Context mContext;

    private DialogCoinpusherConverDetailBinding binding;
    //宝盒适配器
    private CoinPusherCapsuleADetailAdapter coinPusherCapsuleADetailAdapter;

    private int SEL_COIN_PUSHER_CAPSULE  = 0;

    private ItemConvertListener itemConvertListener;

    public ItemConvertListener getItemConvertListener() {
        return itemConvertListener;
    }

    public void setItemConvertListener(ItemConvertListener itemConvertListener) {
        this.itemConvertListener = itemConvertListener;
    }

    public CoinPusherConvertCapsuleDialog(Activity activity, List<CoinPusherConverInfoEntity.GoldCoinInfo.GoldCoinItem> itemData) {
        super(activity);
        this.mContext = activity;
        super.setMActivity(activity);
        initView();
        coinPusherCapsuleADetailAdapter.setItemData(itemData);
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_coinpusher_conver_detail, null, false);
        binding.imgClose.setOnClickListener(v -> dismiss());
        //支持LiveData绑定xml，数据改变，UI自动会更新
        binding.setLifecycleOwner(this);
        //表格布局
        LayoutManagers.LayoutManagerFactory layoutManagerFactory = LayoutManagers.grid(3);
        binding.rcvList.setLayoutManager(layoutManagerFactory.create(binding.rcvList));
        coinPusherCapsuleADetailAdapter = new CoinPusherCapsuleADetailAdapter();
        binding.rcvList.setAdapter(coinPusherCapsuleADetailAdapter);

        coinPusherCapsuleADetailAdapter.setOnItemClickListener(position -> {
            if(SEL_COIN_PUSHER_CAPSULE!=position){
                coinPusherCapsuleADetailAdapter.setDefaultSelect(position);
                SEL_COIN_PUSHER_CAPSULE = position;
            }
        });
        binding.tvSub.setOnClickListener(v -> {
            if(SEL_COIN_PUSHER_CAPSULE!=-1){
                CoinPusherConverInfoEntity.GoldCoinInfo.GoldCoinItem itemEntity = coinPusherCapsuleADetailAdapter.getItemData(SEL_COIN_PUSHER_CAPSULE);
                if(itemEntity!=null){
                    convertCoinPusherGoldsCoin(itemEntity.getId(),itemEntity.getValue(),itemEntity.getType());
                }
            }
        });
    }


    public void show() {
        //设置背景透明,去四个角
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(binding.getRoot());
        //设置宽度充满屏幕
        Window window = getWindow();
        window.setGravity(Gravity.CENTER); //可设置dialog的位置
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

    private void convertCoinPusherGoldsCoin(final Integer id,final Integer amount,Integer type){
        ConfigManager.getInstance().getAppRepository()
                .convertCoinPusherGoldsCoin(id,type)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHud())
                .subscribe(new BaseObserver<BaseResponse>(){
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {
                        if(getItemConvertListener()!=null){
                            getItemConvertListener().success(amount);
                        }
                    }

                    @Override
                    public void onError(RequestException e) {
                        super.onError(e);
                        if(e.getCode()!=null && e.getCode().intValue()==21001 ){//钻石余额不足
                            ToastCenterUtils.showToast(R.string.playfun_dialog_exchange_integral_total_text3);
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHud();
                    }

                });
    }

    public interface ItemConvertListener {
        void success(int value);
    }
}
