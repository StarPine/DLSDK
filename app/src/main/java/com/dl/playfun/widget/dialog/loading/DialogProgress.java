package com.dl.playfun.widget.dialog.loading;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.dl.playfun.R;
import com.dl.playfun.ui.base.BaseDialog;
import com.dl.playfun.widget.progress.MPCircleProgressBar;

/**
 * Author: 彭石林
 * Time: 2022/9/12 18:34
 * Description: This is DialogProgress
 */
public class DialogProgress extends BaseDialog {

    View contentView;

    MPCircleProgressBar mpCircleProgressBar;

    public DialogProgress(Context context) {
        super(context);
        initView(context);
    }

    void initView(Context context) {
        contentView = LayoutInflater.from(context).inflate(R.layout.dialog_mp_loading, null);
        mpCircleProgressBar = contentView.findViewById(R.id.progress);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
    }

    public void show() {
        //设置背景透明,去四个角
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(contentView);
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

    public void setProgress(int progress){
        if(isShowing()){
            if(progress>=100){
                dismiss();
            }else{
                if(mpCircleProgressBar!=null){
                    mpCircleProgressBar.SetCurrent(progress);
                }
            }
        }else{
            if(progress<100){
                if(mpCircleProgressBar!=null){
                    mpCircleProgressBar.SetCurrent(progress);
                }
                show();
            }
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
