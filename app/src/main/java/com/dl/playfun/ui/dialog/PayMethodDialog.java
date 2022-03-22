package com.dl.playfun.ui.dialog;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ConvertUtils;
import com.dl.playfun.R;

/**
 * 選擇支付方式对话框
 *
 * @author wulei
 */
public class PayMethodDialog extends BaseDialogFragment implements View.OnClickListener {

    public static final int PAY_METHOD_GOOGLE_PAY = 1001;
    public static final int PAY_METHOD_MYCARD = 1002;
    private final String payPrice;
    private TextView tvPrice;
    private RadioButton rbMyCard;
    private RadioButton rbGooglePay;
    private Button btnConfirm;
    private ImageView ivClose;
    private PayMethodDialogListener payMethodDialogListener;

    public PayMethodDialog(String payPrice) {
        this.payPrice = payPrice;
    }

    public PayMethodDialogListener getPayMethodDialogListener() {
        return payMethodDialogListener;
    }

    public void setPayMethodDialogListener(PayMethodDialogListener payMethodDialogListener) {
        this.payMethodDialogListener = payMethodDialogListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.MyDialog);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvPrice = view.findViewById(R.id.tv_pay_price);
        rbMyCard = view.findViewById(R.id.rb_mycard);
        rbGooglePay = view.findViewById(R.id.rb_googlepay);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        ivClose = view.findViewById(R.id.iv_dialog_close);

        ivClose.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);

        tvPrice.setText(String.format("TWD %s", payPrice));

    }

    @Override
    public void onStart() {
        super.onStart();
        mWindow.setGravity(Gravity.CENTER);
        mWindow.setLayout(mWidthAndHeight[0] - ConvertUtils.dp2px(48), ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setBackgroundDrawableResource(R.color.transparent);
    }

    @Override
    protected int setLayoutId() {
        return R.layout.dialog_pay_method;
    }

    @Override
    protected boolean isImmersionBarEnabled() {
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, mWidthAndHeight[1] / 2);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.iv_dialog_close) {
            if (payMethodDialogListener != null) {
                payMethodDialogListener.onCancelClick(this);
            } else {
                dismiss();
            }
        } else if (view.getId() == R.id.btn_confirm) {
            if (payMethodDialogListener == null) {
                return;
            }
            if (rbGooglePay.isChecked()) {
                payMethodDialogListener.onConfirmClick(this, PAY_METHOD_GOOGLE_PAY);
            } else if (rbMyCard.isChecked()) {
                payMethodDialogListener.onConfirmClick(this, PAY_METHOD_MYCARD);
            }
        }
    }

    public interface PayMethodDialogListener {

        void onConfirmClick(PayMethodDialog dialog, int payMethod);

        void onCancelClick(PayMethodDialog dialog);
    }
}
