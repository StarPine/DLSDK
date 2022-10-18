package com.dl.playfun.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.databinding.DataBindingUtil;

import com.dl.playfun.R;
import com.dl.playfun.databinding.AlertMediagalleryLockHintBinding;
import com.dl.playfun.databinding.DialogAccountBindPhoneHintBinding;

/**
 * Author: 彭石林
 * Time: 2022/10/17 15:24
 * Description: This is UserBehaviorDialog
 */
public class UserBehaviorDialog {

    /**
    * @Desc TODO(提示用户绑定手机号)
    * @author 彭石林
    * @parame [mContext]
    * @return android.app.Dialog
    * @Date 2022/10/17
    */
    public static Dialog getUserBindPhonesDialog(Context mContext,ClickListener clickListener) {
        Dialog bottomDialog = new Dialog(mContext);
        bottomDialog.setCanceledOnTouchOutside(true);
        bottomDialog.setCancelable(true);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        DialogAccountBindPhoneHintBinding binding = DataBindingUtil.inflate(inflater, R.layout.dialog_account_bind_phone_hint, null, false);
        bottomDialog.setContentView(binding.getRoot());
        bottomDialog.getWindow().setGravity(Gravity.CENTER);

        binding.tvBtn.setOnClickListener(v -> {
            if(clickListener!=null){
                clickListener.onConfirm();
            }
            bottomDialog.dismiss();
        });

        binding.tvCancel.setOnClickListener(v -> {
            bottomDialog.dismiss();
        });

        //设置背景透明,去四个角
        bottomDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        //设置宽度充满屏幕
        Window window = bottomDialog.getWindow();
        window.setGravity(Gravity.CENTER); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.ShowImageDialogAnimation);
        return bottomDialog;
    }

    /**
    * @Desc TODO(用户首次点击付费内容时。解锁提示)
    * @author 彭石林
    * @parame [mContext, clickListener]
    * @return android.app.Dialog
    * @Date 2022/10/18
    */
    public static Dialog getUserMediaGalleryDialog(Context mContext,String title,String coin,boolean isSnapshot,ClickListener clickListener) {
        Dialog bottomDialog = new Dialog(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        AlertMediagalleryLockHintBinding binding = DataBindingUtil.inflate(inflater, R.layout.alert_mediagallery_lock_hint, null, false);
        bottomDialog.setContentView(binding.getRoot());
        bottomDialog.getWindow().setGravity(Gravity.CENTER);
        bottomDialog.setCanceledOnTouchOutside(true);
        bottomDialog.setCancelable(true);
        //快照
        if(isSnapshot){
            binding.llSnapoet.setVisibility(View.VISIBLE);
            binding.tvTitle.setVisibility(View.GONE);
        }else{
            binding.llSnapoet.setVisibility(View.GONE);
            binding.tvTitle.setVisibility(View.VISIBLE);
            binding.tvTitle.setText(title);
        }
        binding.tvCoin.setText(coin);

        binding.tvBtn.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onConfirm();
            }
            bottomDialog.dismiss();
        });

        binding.tvCancel.setOnClickListener(v -> {
            bottomDialog.dismiss();
        });

        //设置背景透明,去四个角
        bottomDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        //设置宽度充满屏幕
        Window window = bottomDialog.getWindow();
        window.setGravity(Gravity.CENTER); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.ShowImageDialogAnimation);
        return bottomDialog;
    }

    public interface ClickListener {
        void onConfirm();
    }
}
