package com.dl.playfun.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.dl.playfun.R;

/**
 * 修改备注：
 *
 * @Name： PlayFun_Google
 * @Description：
 * @Author： liaosf
 * @Date： 2022/8/12 11:26
 */
public class ExclusiveAccostDialog{
    private static volatile ExclusiveAccostDialog INSTANCE;
    private Context mContext;
    private ExclusiveAccostDialog.DialogOnClickListener onClickListener;

    public static ExclusiveAccostDialog getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ExclusiveAccostDialog.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ExclusiveAccostDialog(context);
                }
            }
        } else {
            init(context);
        }
        return INSTANCE;
    }

    private static void init(Context context) {
        INSTANCE.mContext = context;
    }

    private ExclusiveAccostDialog(Context context) {
        this.mContext = context;
    }

    public ExclusiveAccostDialog setConfirmOnClick(ExclusiveAccostDialog.DialogOnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return INSTANCE;
    }

    /**
     * 编辑搭讪文本内容dialog
     *
     * @return
     */
    public Dialog editAccostContentDialog(String content) {
        Dialog dialog = new Dialog(mContext, R.style.BottomDialog);
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_text_accost, null);
        dialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        contentView.setLayoutParams(layoutParams);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);

        Button confirmBtn = contentView.findViewById(R.id.btn_confirm);
        TextView wordCount = contentView.findViewById(R.id.tv_word_count);
        EditText edAccostText = contentView.findViewById(R.id.et_accost);
        ImageView close = contentView.findViewById(R.id.iv_close);
        if (!TextUtils.isEmpty(content)){
            edAccostText.setText(content);
            wordCount.setText(String.format(mContext.getString(R.string.playfun_text_accost_number_of_fonts),edAccostText.getText().length()+""));
        }else {
            wordCount.setText(String.format(mContext.getString(R.string.playfun_text_accost_number_of_fonts),"0"));

        }
        close.setOnClickListener(v -> dialog.dismiss());
        edAccostText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                wordCount.setText(String.format(mContext.getString(R.string.playfun_text_accost_number_of_fonts),edAccostText.getText().length()+""));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        confirmBtn.setOnClickListener(v -> {
            if (onClickListener != null){
                onClickListener.OnConfirm(dialog,edAccostText.getText().toString());
            }
        });
        return dialog;
    }

    public interface DialogOnClickListener {

        default void close() {
        }

        void OnConfirm(Dialog dialog, String content);
    }
}
