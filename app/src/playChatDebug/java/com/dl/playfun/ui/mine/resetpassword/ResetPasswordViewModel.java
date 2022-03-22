package com.dl.playfun.ui.mine.resetpassword;

import android.app.Application;
import android.os.CountDownTimer;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.viewmodel.BaseViewModel;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author wulei
 */
public class ResetPasswordViewModel extends BaseViewModel<AppRepository> {

    public ObservableField<String> mobile = new ObservableField<>("");
    public ObservableField<String> code = new ObservableField<>("");
    public ObservableField<String> password = new ObservableField<>("");
    //推送设置按钮的点击事件
    public BindingCommand resetOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            if (TextUtils.isEmpty(mobile.get())) {
                ToastUtils.showShort(R.string.please_iput_phone);
                return;
            }
            if (TextUtils.isEmpty(code.get())) {
                ToastUtils.showShort(R.string.please_input_code);
                return;
            }
            if (TextUtils.isEmpty(password.get())) {
                ToastUtils.showShort(R.string.please_input_new_password);
                return;
            }
            if (password.get().length() < 8) {
                ToastUtils.showShort(R.string.please_input_password_min_len);
                return;
            }
            resetPassword();
        }
    });
    public ObservableField<String> downTimeStr = new ObservableField<>(StringUtils.getString(R.string.send_code));
    private boolean isDownTime = false;
    /**
     * 发送短信按钮点击事件
     */
    public BindingCommand sendSmsOnClickCommand = new BindingCommand(() -> reqVerifyCode());

    public ResetPasswordViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
    }

    private void reqVerifyCode() {
        if (TextUtils.isEmpty(mobile.get())) {
            ToastUtils.showShort(R.string.please_iput_phone);
            return;
        }

        if (!isDownTime) {
            model.verifyCodePost(mobile.get())
                    .compose(RxUtils.schedulersTransformer())
                    .compose(RxUtils.exceptionTransformer())
                    .doOnSubscribe(this)
                    .doOnSubscribe(disposable -> showHUD())
                    .subscribe(new BaseObserver<BaseResponse>() {
                        @Override
                        public void onSuccess(BaseResponse response) {
                            dismissHUD();
                            /**
                             * 倒计时60秒，一次1秒
                             */
                            CountDownTimer timer = new CountDownTimer(60 * 1000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    isDownTime = true;
                                    downTimeStr.set(StringUtils.getString(R.string.again_send) + "（" + millisUntilFinished / 1000 + "）");
                                }

                                @Override
                                public void onFinish() {
                                    isDownTime = false;
                                    downTimeStr.set(StringUtils.getString(R.string.again_send));
                                }
                            }.start();
                        }

                        @Override
                        public void onComplete() {
                            super.onComplete();
                            dismissHUD();
                        }
                    });
        }
    }

    /**
     * 修改密码
     */
    private void resetPassword() {
        model.resetPassword(mobile.get(), Integer.valueOf(code.get()), password.get())
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ToastUtils.showShort(R.string.alter_success);
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                        //pop();
                    }
                });
    }
}