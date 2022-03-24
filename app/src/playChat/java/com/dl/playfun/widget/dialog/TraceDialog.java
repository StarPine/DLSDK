package com.dl.playfun.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;

/**
 * Author: 彭石林
 * Time: 2021/8/3 16:09
 * Description: This is TraceDialog
 */
public class TraceDialog {
    private static volatile TraceDialog INSTANCE;
    private Context context;
    private TypeEnum CHOOSRTYPE;
    private Dialog dialog;

    private String titleString = "";
    private String contentString = "";
    private String confirmText = "";
    private String confirmTwoText = "";
    private String cannelText = "";
    private int titleSize = 0;


    private ConfirmOnclick confirmOnclick;
    private ConfirmTwoOnclick confirmTwoOnclick;

    private CannelOnclick cannelOnclick;

    private TraceDialog(Context context) {
        this.context = context;
    }

    public static TraceDialog getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TraceDialog.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TraceDialog(context);
                }
            }
        } else {
            init(context);
        }
        return INSTANCE;
    }

    /**
     * 从新初始化值
     *
     * @param context
     */
    private static void init(Context context) {
        INSTANCE.context = context;
        INSTANCE.titleString = "";
        INSTANCE.contentString = "";
        INSTANCE.confirmText = "";
        INSTANCE.confirmTwoText = "";
        INSTANCE.cannelText = "";
        INSTANCE.confirmOnclick = null;
        INSTANCE.confirmTwoOnclick = null;
        INSTANCE.cannelOnclick = null;
    }

    /**
     * 最后第二步才选项弹框样式
     *
     * @return
     */
    public TraceDialog chooseType(TypeEnum typeEnum) {
        this.CHOOSRTYPE = typeEnum;
        if (typeEnum == TypeEnum.CENTER) {
            this.dialog = getcenterDialog();
        }
        return INSTANCE;
    }

    /**
     * 显示弹框
     */
    public void show() {
        if (this.CHOOSRTYPE == TypeEnum.BOTTOMLIST) {
            this.dialog.show();
        } else if (this.CHOOSRTYPE == TypeEnum.CENTENTLIST) {

        } else if (this.CHOOSRTYPE == TypeEnum.CENTER) {
            this.dialog.show();
        } else if (this.CHOOSRTYPE == TypeEnum.CENTERWARNED) {
            this.dialog.show();
        } else if (this.CHOOSRTYPE == TypeEnum.BOTTOMCOMMENT) {
            this.dialog.show();
        } else if (this.CHOOSRTYPE == TypeEnum.SET_MONEY) {
            this.dialog.show();
        }
    }

    public boolean isShowing() {
        if (this.dialog != null) {
            boolean isS = this.dialog.isShowing();
            return this.dialog.isShowing();
        }
        return false;
    }

    /**
     * 取消弹框
     */
    public void dismiss() {
        if (this.CHOOSRTYPE == TypeEnum.BOTTOMLIST) {
            this.dialog.dismiss();
        } else if (this.CHOOSRTYPE == TypeEnum.CENTENTLIST) {

        } else if (this.CHOOSRTYPE == TypeEnum.CENTER) {
            this.dialog.dismiss();
        } else if (this.CHOOSRTYPE == TypeEnum.CENTERWARNED) {
            this.dialog.dismiss();
        } else if (this.CHOOSRTYPE == TypeEnum.BOTTOMCOMMENT) {
            this.dialog.dismiss();
        } else if (this.CHOOSRTYPE == TypeEnum.SET_MONEY) {
            this.dialog.dismiss();
        }

    }

    /**
     * 设置
     *
     * @param titleString
     * @return
     */
    public TraceDialog setTitle(String titleString) {
        this.titleString = titleString;
        return INSTANCE;
    }

    public TraceDialog setContent(String content) {
        this.contentString = content;
        return INSTANCE;
    }

    public TraceDialog setTitleSize(int size) {
        this.titleSize = size;
        return INSTANCE;
    }



    /**
     * 设置确定按钮文案
     *
     * @param confirmText
     * @return
     */
    public TraceDialog setConfirmText(String confirmText) {
        this.confirmText = confirmText;
        return INSTANCE;
    }

    /**
     * 设置第二个按钮文案
     *
     * @param confirmTwoText
     * @return
     */
    public TraceDialog setConfirmTwoText(String confirmTwoText) {
        this.confirmTwoText = confirmTwoText;
        return INSTANCE;
    }

    public TraceDialog setCannelText(String cannelText) {
        this.cannelText = cannelText;
        return INSTANCE;
    }

    /**
     * 设置确认按钮点击
     *
     * @param confirmOnclick
     * @return
     */
    public TraceDialog setConfirmOnlick(ConfirmOnclick confirmOnclick) {
        this.confirmOnclick = confirmOnclick;
        return INSTANCE;
    }

    public TraceDialog setConfirmTwoOnlick(ConfirmTwoOnclick confirmTwoOnclick) {
        this.confirmTwoOnclick = confirmTwoOnclick;
        return INSTANCE;
    }

    public TraceDialog setCannelOnclick(CannelOnclick cannelOnclick) {
        this.cannelOnclick = cannelOnclick;
        return INSTANCE;
    }

    /**
     * 获取
     *
     * @return
     */
    private Dialog getcenterDialog() {
        Dialog bottomDialog = new Dialog(context, R.style.BottomDialog);
        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_trace, null);
        bottomDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        contentView.setLayoutParams(layoutParams);
        bottomDialog.getWindow().setGravity(Gravity.CENTER);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);

        TextView title = contentView.findViewById(R.id.tv_title);
        Button confirmBtn = contentView.findViewById(R.id.confirm);
        Button cannelwBtn = contentView.findViewById(R.id.cannel);
        if (titleString.equals("")) {
            title.setVisibility(View.GONE);
        } else {
            title.setVisibility(View.VISIBLE);
            title.setText(titleString);
        }
        if (titleSize != 0){
            title.setTextSize(titleSize);
        }

        if (StringUtils.isEmpty(confirmText)) {
            confirmBtn.setVisibility(View.GONE);
        } else {
            confirmBtn.setVisibility(View.VISIBLE);
            confirmBtn.setText(confirmText);
        }

        if (StringUtils.isEmpty(cannelText)) {
            cannelwBtn.setVisibility(View.GONE);
        } else {
            cannelwBtn.setVisibility(View.VISIBLE);
            cannelwBtn.setText(cannelText);
        }

        cannelwBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cannelOnclick != null) {
                    cannelOnclick.cannel(bottomDialog);
                }
                bottomDialog.dismiss();
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ToastUtils.showShort("确定");
                if (confirmOnclick != null) {
                    confirmOnclick.confirm(bottomDialog);
                }
                bottomDialog.dismiss();
            }
        });
        return bottomDialog;
    }

    public Dialog TraceVipDialog() {
        Dialog bottomDialog = new Dialog(context, R.style.BottomDialog);
        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_trace_vip, null);
        bottomDialog.setContentView(contentView);
        TextView title = contentView.findViewById(R.id.tv_title);
        TextView tv_content = contentView.findViewById(R.id.tv_content);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        contentView.setLayoutParams(layoutParams);
        bottomDialog.getWindow().setGravity(Gravity.CENTER);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);

        Button confirmBtn = contentView.findViewById(R.id.confirm);
        if (!StringUtils.isEmpty(titleString)) {
            title.setText(titleString);
        }
        if (!StringUtils.isEmpty(contentString)) {
            tv_content.setText(contentString);
        }

        if (StringUtils.isEmpty(confirmText)) {
            confirmBtn.setVisibility(View.GONE);
        } else {
            confirmBtn.setVisibility(View.VISIBLE);
            confirmBtn.setText(confirmText);
        }
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ToastUtils.showShort("确定");
                if (confirmOnclick != null) {
                    confirmOnclick.confirm(bottomDialog);
                }
                bottomDialog.dismiss();
            }
        });
        return bottomDialog;
    }

    public Dialog AlertTaskBonus() {
        Dialog bottomDialog = new Dialog(context, R.style.BottomDialog);
        View contentView = LayoutInflater.from(context).inflate(R.layout.alert_task_gold, null);
        bottomDialog.setContentView(contentView);
        bottomDialog.getWindow().setGravity(Gravity.CENTER);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        TextView title = contentView.findViewById(R.id.tv_title);
        TextView tv_content = contentView.findViewById(R.id.tv_content);

        if (!StringUtils.isEmpty(titleString)) {
            title.setText(titleString);
        } else {
            title.setVisibility(View.INVISIBLE);
        }

        if (!StringUtils.isEmpty(contentString)) {
            tv_content.setText(contentString);
        } else {
            tv_content.setVisibility(View.GONE);
        }

        Button confirmBtn = contentView.findViewById(R.id.confirm);
        Button confirmtowBtn = contentView.findViewById(R.id.two_confirm);

        if (!StringUtils.isEmpty(confirmText)) {
            confirmBtn.setText(confirmText);
        }
        if (!StringUtils.isEmpty(confirmTwoText)) {
            confirmtowBtn.setText(confirmTwoText);
        } else {
            confirmtowBtn.setVisibility(View.GONE);
        }

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmOnclick != null) {
                    confirmOnclick.confirm(bottomDialog);
                }
                bottomDialog.dismiss();
            }
        });

        confirmtowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmTwoOnclick != null) {
                    confirmTwoOnclick.confirmTwo(bottomDialog);
                }
                bottomDialog.dismiss();
            }
        });

        return bottomDialog;
    }

    public Dialog verticalButtonDialog() {
        Dialog bottomDialog = new Dialog(context, R.style.BottomDialog);
        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_vertical_two_button, null);
        bottomDialog.setContentView(contentView);
        bottomDialog.getWindow().setGravity(Gravity.CENTER);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        TextView title = contentView.findViewById(R.id.tv_title);
        TextView tv_content = contentView.findViewById(R.id.tv_content);

        if (!StringUtils.isEmpty(titleString)) {
            title.setText(titleString);
        } else {
            title.setVisibility(View.INVISIBLE);
        }

        if (!StringUtils.isEmpty(contentString)) {
            tv_content.setText(contentString);
        } else {
            tv_content.setVisibility(View.GONE);
        }

        Button button_top = contentView.findViewById(R.id.button_top);
        Button button_below = contentView.findViewById(R.id.button_below);

        if (!StringUtils.isEmpty(confirmText)) {
            button_top.setText(confirmText);
        }
        if (!StringUtils.isEmpty(confirmTwoText)) {
            button_below.setText(confirmTwoText);
        } else {
            button_below.setVisibility(View.GONE);
        }

        button_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmOnclick != null) {
                    confirmOnclick.confirm(bottomDialog);
                }
                bottomDialog.dismiss();
            }
        });

        button_below.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmTwoOnclick != null) {
                    confirmTwoOnclick.confirmTwo(bottomDialog);
                }
                bottomDialog.dismiss();
            }
        });

        return bottomDialog;
    }

    public Dialog AlertTaskBonuSubHint(boolean ishowVip) {
        Dialog bottomDialog = new Dialog(context, R.style.BottomDialog);
        View contentView = LayoutInflater.from(context).inflate(R.layout.alert_task_gold1, null);
        bottomDialog.setContentView(contentView);
        bottomDialog.getWindow().setGravity(Gravity.CENTER);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        TextView title = contentView.findViewById(R.id.tv_title);
        LinearLayout tv_content = contentView.findViewById(R.id.content);

        TextView bottom_vip = contentView.findViewById(R.id.bottom_vip);
        if (!StringUtils.isEmpty(titleString)) {
            title.setText(titleString);
        } else {
            title.setVisibility(View.INVISIBLE);
        }

        if (ishowVip) {
            tv_content.setVisibility(View.VISIBLE);
            bottom_vip.setVisibility(View.VISIBLE);
        } else {
            tv_content.setVisibility(View.GONE);
            bottom_vip.setVisibility(View.GONE);
        }

        Button confirmBtn = contentView.findViewById(R.id.confirm);
        Button cannelBtn = contentView.findViewById(R.id.cannel);

        if (!StringUtils.isEmpty(confirmText)) {
            confirmBtn.setText(confirmText);
        }
        if (!StringUtils.isEmpty(cannelText)) {
            cannelBtn.setText(cannelText);
        } else {
            cannelBtn.setVisibility(View.GONE);
        }

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmOnclick != null) {
                    confirmOnclick.confirm(bottomDialog);
                }
            }
        });

        cannelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cannelOnclick != null) {
                    cannelOnclick.cannel(bottomDialog);
                }
            }
        });

        return bottomDialog;
    }

    //非VIP弹窗
    public Dialog AlertAudioPlayer(Integer money) {
        Dialog bottomDialog = new Dialog(context, R.style.BottomDialog);
        View contentView = LayoutInflater.from(context).inflate(R.layout.alert_audio_success, null);
        bottomDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        contentView.setLayoutParams(layoutParams);
        bottomDialog.getWindow().setGravity(Gravity.CENTER);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.setCanceledOnTouchOutside(true);
        bottomDialog.setCancelable(false);
        Button confirmBtn = contentView.findViewById(R.id.btn_confirm);
        TextView cannelBtn = contentView.findViewById(R.id.btn_cannel);
        TextView titleFen = contentView.findViewById(R.id.title_fen);
        titleFen.setText(String.format(StringUtils.getString(R.string.radio_daily_atendance_fen1), money));

        ImageView cannelImage = contentView.findViewById(R.id.iv_dialog_close);
        cannelImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomDialog.dismiss();
            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ToastUtils.showShort("确定");
                if (confirmOnclick != null) {
                    confirmOnclick.confirm(bottomDialog);
                }
                bottomDialog.dismiss();
            }
        });
        cannelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cannelOnclick != null) {
                    cannelOnclick.cannel(bottomDialog);
                }
                bottomDialog.dismiss();
            }
        });
        return bottomDialog;
    }

    //弹出权限获取提示
    public Dialog AlertCallAudioPermissions() {
        Dialog bottomDialog = new Dialog(context, R.style.BottomDialog);
        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_permissions_media_video, null);
        bottomDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        contentView.setLayoutParams(layoutParams);
        bottomDialog.getWindow().setGravity(Gravity.CENTER);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.setCanceledOnTouchOutside(true);
        bottomDialog.setCancelable(false);

        Button confirmBtn = contentView.findViewById(R.id.btn_confirm);

        ImageView cannelImage = contentView.findViewById(R.id.iv_dialog_close);

        cannelImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cannelOnclick != null) {
                    cannelOnclick.cannel(bottomDialog);
                }
                bottomDialog.dismiss();
            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ToastUtils.showShort("确定");
                if (confirmOnclick != null) {
                    confirmOnclick.confirm(bottomDialog);
                }
                bottomDialog.dismiss();
            }
        });
        return bottomDialog;
    }

    //女性真人认证体现提示
    public Dialog alertCertificationGirl() {
        Dialog bottomDialog = new Dialog(context, R.style.BottomDialog);
        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_certification_girl_tw_money, null);
        bottomDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        contentView.setLayoutParams(layoutParams);
        bottomDialog.getWindow().setGravity(Gravity.CENTER);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.setCanceledOnTouchOutside(true);
        bottomDialog.setCancelable(false);

        Button confirmBtn = contentView.findViewById(R.id.btn_confirm);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomDialog.dismiss();
            }
        });
        //设置背景透明,去四个角
        bottomDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        //设置宽度充满屏幕
        Window window = bottomDialog.getWindow();
        window.setGravity(Gravity.CENTER); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        return bottomDialog;
    }

    /**
     * @Desc TODO(签到成功弹窗)
     * @author 彭石林
     * @parame [isCard, text, message]
     * @return android.app.Dialog
    * @Date 2021/12/11
    */
    public Dialog AlertTaskSignDay(Integer isCard,String text,String message){
        Dialog bottomDialog = new Dialog(context, R.style.BottomDialog);
        View contentView = LayoutInflater.from(context).inflate(R.layout.alert_daily_attendance_success, null);
        bottomDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        contentView.setLayoutParams(layoutParams);
        bottomDialog.getWindow().setGravity(Gravity.CENTER);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);

        ImageView title_img = contentView.findViewById(R.id.title_img);
        if(isCard==1){
            title_img.setImageResource(R.drawable.task_sign_card_img);
        }
        TextView title_fen = contentView.findViewById(R.id.title_fen);
        title_fen.setText(String.valueOf(text));
        TextView content_text = contentView.findViewById(R.id.content_text);
        content_text.setText(String.valueOf(message));

        Button confirmBtn = contentView.findViewById(R.id.btn_confirm);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmOnclick != null) {
                    confirmOnclick.confirm(bottomDialog);
                }else{
                    bottomDialog.dismiss();
                }
            }
        });
        ImageView iv_dialog_close = contentView.findViewById(R.id.iv_dialog_close);
        iv_dialog_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cannelOnclick != null) {
                    cannelOnclick.cannel(bottomDialog);
                } else {
                    bottomDialog.dismiss();
                }
            }
        });
        //设置背景透明,去四个角
        bottomDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        //设置宽度充满屏幕
        Window window = bottomDialog.getWindow();
        window.setGravity(Gravity.CENTER); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        return bottomDialog;
    }

    /**
     * @Desc TODO(註冊完成弹窗)
     * @author lsf
     * @return android.app.Dialog
     * @Date 2021/12/28
     */
    public Dialog newUserRegisComplete(){
        Dialog dialog = new Dialog(context, R.style.UpdateAppDialog);
        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_new_user_regis_completed, null);
        dialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        contentView.setLayoutParams(layoutParams);
        dialog.getWindow().setGravity(Gravity.CENTER);
        Button confirmBtn = contentView.findViewById(R.id.btn_confirm);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmOnclick != null) {
                    confirmOnclick.confirm(dialog);
                }else{
                    dialog.dismiss();
                }
            }
        });
        ImageView iv_dialog_close = contentView.findViewById(R.id.iv_dialog_close);
        iv_dialog_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cannelOnclick != null) {
                    cannelOnclick.cannel(dialog);
                } else {
                    dialog.dismiss();
                }
            }
        });


        return dialog;
    }

    /**
     * @Desc TODO(首次收入弹窗)
     * @author 彭石林
     * @parame [isCard, text, message]
     * @return android.app.Dialog
     * @Date 2021/12/11
     */
    public Dialog AlertTaskMoney(Drawable drawable,String tip, String text, String message){
        Dialog bottomDialog = new Dialog(context, R.style.BottomDialog);
        View contentView = LayoutInflater.from(context).inflate(R.layout.alert_daily_attendance_success2, null);
        bottomDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        contentView.setLayoutParams(layoutParams);
        bottomDialog.getWindow().setGravity(Gravity.CENTER);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        TextView tip_tv = contentView.findViewById(R.id.tip_tv);
        tip_tv.setText(tip);
        TextView tip_tv2 = contentView.findViewById(R.id.tip_tv2);
        tip_tv2.setText(text);
        TextView title_fen = contentView.findViewById(R.id.title_fen);
        title_fen.setText(message);
        RelativeLayout relativeLayout = contentView.findViewById(R.id.no_vip_unlock);
        relativeLayout.setBackground(drawable);

        Button confirmBtn = contentView.findViewById(R.id.btn_confirm);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmOnclick != null) {
                    confirmOnclick.confirm(bottomDialog);
                }else{
                    bottomDialog.dismiss();
                }
            }
        });
        ImageView iv_dialog_close = contentView.findViewById(R.id.iv_dialog_close);
        iv_dialog_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cannelOnclick != null) {
                    cannelOnclick.cannel(bottomDialog);
                } else {
                    bottomDialog.dismiss();
                }
            }
        });
        return bottomDialog;
    }

    /**
     * 样式类型
     * 再设置其他数据和文案后在选择这个样式，然后调用
     */

    public enum TypeEnum {
        BOTTOMLIST,//底部列表
        CENTENTLIST,//中部列表
        CENTER,//中部提示
        CENTERWARNED,//中部警告
        BOTTOMCOMMENT,//底部评论列表
        //        BOTTOMLIST,//底部列表
        SET_MONEY//设置价格
    }


    public interface ConfirmOnclick {
        void confirm(Dialog dialog);
    }

    public interface ConfirmTwoOnclick {
        void confirmTwo(Dialog dialog);
    }

    public interface CannelOnclick {
        void cannel(Dialog dialog);
    }
}
