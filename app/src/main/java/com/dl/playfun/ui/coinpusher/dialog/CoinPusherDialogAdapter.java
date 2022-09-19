package com.dl.playfun.ui.coinpusher.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.StringRes;
import androidx.databinding.DataBindingUtil;

import com.dl.playfun.R;
import com.dl.playfun.databinding.DialogCoinpusherConverBinding;
import com.dl.playfun.databinding.DialogCoinpusherConverDetailBinding;
import com.dl.playfun.databinding.DialogCoinpusherHelpBinding;
import com.dl.playfun.databinding.DialogCoinpusherHintBinding;
import com.dl.playfun.databinding.DialogCoinpusherHintRetainBinding;

import me.goldze.mvvmhabit.utils.StringUtils;

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
    public static Dialog getDialogCoinPusherHint(Context mContext){
        Dialog dialog = new Dialog(mContext);
        dialog.setCancelable(true);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        DialogCoinpusherHintBinding binding = DataBindingUtil.inflate(inflater, R.layout.dialog_coinpusher_hint, null, false);
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
        dialog.show();
        return dialog;
    }
    /**
    * @Desc TODO(帮助文档)
    * @author 彭石林
    * @parame [mContext, label, content]
    * @return android.app.Dialog
    * @Date 2022/8/26
    */
    public static Dialog getDialogCoinPusherHelp(Context mContext,String label,String content){
        Dialog dialog = new Dialog(mContext);
        dialog.setCancelable(true);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        DialogCoinpusherHelpBinding binding = DataBindingUtil.inflate(inflater, R.layout.dialog_coinpusher_help, null, false);
        binding.imgClose.setOnClickListener(v -> dialog.dismiss());
        if(!StringUtils.isEmpty(label)){
            binding.tvLable.setText(label);
        }
        if(!StringUtils.isEmpty(content)){
            binding.tvContent.setText(label);
        }
        //设置背景透明,去四个角
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(binding.getRoot());
        //设置宽度充满屏幕
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        return dialog;
    }

    /**
    * @Desc TODO(正在投币中退出挽留提示)
    * @author 彭石林
    * @parame [mContext]
    * @return android.app.Dialog
    * @Date 2022/9/6
    */
    public static Dialog getDialogCoinPusherRetainHint(Context mContext,@StringRes int ContentResId,CoinPusherDialogListener coinPusherDialogListener){
        Dialog dialog = new Dialog(mContext);
        dialog.setCancelable(true);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        DialogCoinpusherHintRetainBinding binding = DataBindingUtil.inflate(inflater, R.layout.dialog_coinpusher_hint_retain, null, false);
        binding.tvSub.setOnClickListener(v -> {
            if(coinPusherDialogListener!=null){
                coinPusherDialogListener.onConfirm(dialog);
            }
        });
        binding.tvContent.setText(ContentResId);
        binding.tvCancel.setOnClickListener(v -> dialog.dismiss());
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
        dialog.show();
        return dialog;
    }

    public interface CoinPusherDialogListener {
        default void onConfirm(Dialog dialog) {

        }
    }


}
