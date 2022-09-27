package com.dl.playfun.widget.dialog.loading;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.dl.playfun.R;
import com.dl.playfun.ui.base.BaseDialog;
import com.tencent.qcloud.tuicore.custom.CustomDrawableUtils;

/**
 * Author: 彭石林
 * Time: 2022/9/27 18:00
 * Description: This is DialogLoading
 */
public class DialogLoading extends BaseDialog {

    View contentView;

    public DialogLoading(Context context) {
        super(context);
        initView(context);
    }

    void initView(Context context) {
        contentView = LayoutInflater.from(context).inflate(R.layout.dialog_mp_loading, null);
        FrameLayout flLayout = contentView.findViewById(R.id.fl_layout);
        if(flLayout!=null){
            CustomDrawableUtils.generateDrawable(flLayout, getColorFromResource(R.color.white),
                    8,null,null,null,null,
                    null,null,null,null,90,null);
        }
        setCanceledOnTouchOutside(false);
        setCancelable(false);
    }

    Integer getColorFromResource(Integer resourceId) {
        if (resourceId==null) {
            return null;
        } else {
            return getContext().getResources().getColor(resourceId);
        }
    }
    public void show() {
        //设置背景透明,去四个角
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(contentView);
        //设置宽度充满屏幕
        Window window = getWindow();
        window.setGravity(Gravity.CENTER); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

}
