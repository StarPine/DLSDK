package com.dl.playfun.ui.message.mediagallery;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.databinding.DataBindingUtil;

import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.R;
import com.dl.playfun.databinding.DialogSnapshotPhotoSettingBinding;
import com.dl.playfun.entity.MediaPayPerConfigEntity;
import com.dl.playfun.ui.base.BaseDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: 彭石林
 * Time: 2022/9/9 17:55
 * Description: This is SnapshotPhotoDialog
 */
public class SnapshotPhotoDialog extends BaseDialog {

    private final DialogSnapshotPhotoSettingBinding binding;

    private MediaPayPerConfigEntity.itemTagEntity mediaPriceTmpConfig;

    private SnapshotListener snapshotListener;

    private String checkPrice;

    public SnapshotListener getSnapshotListener() {
        return snapshotListener;
    }

    public void setSnapshotListener(SnapshotListener snapshotListener) {
        this.snapshotListener = snapshotListener;
    }

    public SnapshotPhotoDialog(Context context, MediaPayPerConfigEntity.itemTagEntity mediaPriceTmpConfig) {
        super(context);
        this.mediaPriceTmpConfig = mediaPriceTmpConfig;
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_snapshot_photo_setting, null, false);
        setCancelable(true);
        init();
    }
    private void init() {
        ArrayList<String> listData = new ArrayList<>();
        if(ObjectUtils.isNotEmpty(mediaPriceTmpConfig)){
            List<MediaPayPerConfigEntity.ItemEntity> itemData = mediaPriceTmpConfig.getContent();
            for (MediaPayPerConfigEntity.ItemEntity itemSet : itemData) {
                listData.add(itemSet.getProfit().toString());
            }
            if(ObjectUtils.isNotEmpty(listData)){
                int size = listData.size();
                binding.seekbarPhoto.initData(listData, size-1);
                binding.seekbarPhoto.setProgress(0);
                binding.seekbarPhotoView.setMax(size-1);
                binding.seekbarPhotoView.setProgress(0);
                MediaPayPerConfigEntity.ItemEntity itemEntity = mediaPriceTmpConfig.getContent().get(0);
                binding.tvCoin.setText(itemEntity.getCoin());
                binding.tvMoney.setText(itemEntity.getProfit().toString());
            }
        }

        //文字调整进度条宽度测量
        binding.seekbarPhoto.setMeasureWidthCallBack(width -> {
            ViewGroup.LayoutParams layoutParams = binding.seekbarPhotoView.getLayoutParams();
            layoutParams.width = width;
            Log.e("当前测量宽度控件","==========="+width);
            binding.seekbarPhotoView.setLayoutParams(layoutParams);
        });
        binding.seekbarPhotoView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.seekbarPhoto.setProgress(seekBar.getProgress());
                if(getSnapshotListener()!=null){
                    if(ObjectUtils.isNotEmpty(mediaPriceTmpConfig) && ObjectUtils.isNotEmpty(mediaPriceTmpConfig.getContent())){
                        MediaPayPerConfigEntity.ItemEntity itemEntity = mediaPriceTmpConfig.getContent().get(progress);
                        checkPrice = itemEntity.getProfit().toString();
                        binding.tvCoin.setText(itemEntity.getCoin());
                        binding.tvMoney.setText(checkPrice);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            //拖动条停止拖动的时候调用
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        binding.tvBtn.setOnClickListener(v ->{
            if(getSnapshotListener()!=null){
                getSnapshotListener().confirm(checkPrice);
            }
            dismiss();
        });
    }

    public void show() {
        //设置背景透明,去四个角
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(binding.getRoot());
        //设置宽度充满屏幕
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        super.show();
    }

    public interface SnapshotListener{
        void confirm(String price);
    }
}
