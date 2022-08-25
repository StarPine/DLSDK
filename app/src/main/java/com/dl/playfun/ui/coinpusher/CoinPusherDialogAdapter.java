package com.dl.playfun.ui.coinpusher;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import androidx.databinding.DataBindingUtil;

import com.dl.playfun.R;
import com.dl.playfun.databinding.DialogCoinpusherConverBinding;
import com.dl.playfun.databinding.DialogCoinpusherConverDetailBinding;
import com.dl.playfun.databinding.DialogCoinpusherHintBinding;

/**
 * Author: 彭石林
 * Time: 2022/8/22 15:58
 * Description: This is CoinpusherDialogAdapter
 */
public class CoinPusherDialogAdapter {
    /**
    * @Desc TODO(没有投币提示30自动关闭)
    * @author 彭石林
    * @parame [mContext]
    * @return android.app.Dialog
    * @Date 2022/8/22
    */
    public static Dialog getDialogCoinpusherHint(Context mContext){
        Dialog dialog = new Dialog(mContext);
        dialog.setCancelable(true);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        DialogCoinpusherHintBinding binding = DataBindingUtil.inflate(inflater, R.layout.dialog_coinpusher_list, null, false);
        binding.tvSub.setOnClickListener(v -> dialog.dismiss());
        //设置背景透明,去四个角
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(binding.getRoot());
        //设置宽度充满屏幕
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        return dialog;
    }
}
