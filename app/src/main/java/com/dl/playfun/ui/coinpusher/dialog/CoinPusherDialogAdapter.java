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
import com.dl.playfun.databinding.AlertPermissionsCoinpusherBinding;
import com.dl.playfun.databinding.DialogCoinpusherHelpBinding;
import com.dl.playfun.databinding.DialogCoinpusherHintBinding;
import com.dl.playfun.databinding.DialogCoinpusherHintRetainBinding;
import com.dl.playfun.ui.webview.WebHomeFragment;

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
    * @Date 2022/8/22
    */
    public static void getDialogCoinPusherHint(Context mContext){
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
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.show();
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
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.show();
        return dialog;
    }

    /**
     * @Desc TODO(游戏中没有音视频弹出权限提示)
     * @author 彭石林
     * @parame [mContext]
     * @return android.app.Dialog
     * @Date 2022/9/6
     */
    public static Dialog getDialogPermissions(Context mContext,@StringRes int ContentResId,PermissionDialogListener permissionDialogListener){
        Dialog dialog = new Dialog(mContext);
        dialog.setCancelable(true);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        AlertPermissionsCoinpusherBinding binding = DataBindingUtil.inflate(inflater, R.layout.alert_permissions_coinpusher, null, false);
        binding.tvSub.setOnClickListener(v -> {
            if(permissionDialogListener!=null){
                permissionDialogListener.callback(true);
            }
        });
        binding.tvTitle.setText(ContentResId);
        binding.imgClose.setOnClickListener(v -> {
            if(permissionDialogListener!=null){
                permissionDialogListener.callback(false);
            }
            dialog.dismiss();
        });
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
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        return dialog;
    }


    public interface CoinPusherDialogListener {
        default void onConfirm(Dialog dialog) {

        }
    }

    public interface PermissionDialogListener{
        void callback(boolean _success);
    }


}
