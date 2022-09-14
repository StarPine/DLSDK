package com.dl.playfun.ui.message.mediagallery;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.databinding.DataBindingUtil;

import com.dl.playfun.R;
import com.dl.playfun.databinding.DialogSnapshotPhotoSettingBinding;
import com.dl.playfun.ui.base.BaseDialog;

import java.util.ArrayList;

/**
 * Author: 彭石林
 * Time: 2022/9/9 17:55
 * Description: This is SnapshotPhotoDialog
 */
public class SnapshotPhotoDialog extends BaseDialog {
    private DialogSnapshotPhotoSettingBinding binding;

    public SnapshotPhotoDialog(Context context) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_snapshot_photo_setting, null, false);
        setCancelable(true);
        init();
    }
    private void init() {
        ArrayList<String> listData = new ArrayList<>();
        listData.add("100");
        listData.add("200");
        listData.add("300");
        listData.add("400");
        listData.add("500");
        binding.seekbarPhoto.initData(listData, 4);
        binding.seekbarPhoto.setProgress(1);
        binding.seekbarPhotoView.setMax(4);
        binding.seekbarPhotoView.setProgress(1);

        //文字调整进度条宽度测量
        binding.seekbarPhoto.setMeasureWidthCallBack(width -> {
            ViewGroup.LayoutParams layoutParams = binding.seekbarPhotoView.getLayoutParams();
            layoutParams.width = width;
            binding.seekbarPhotoView.setLayoutParams(layoutParams);
        });
        binding.seekbarPhotoView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.seekbarPhoto.setProgress(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            //拖动条停止拖动的时候调用
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
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
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        super.show();
    }
}
