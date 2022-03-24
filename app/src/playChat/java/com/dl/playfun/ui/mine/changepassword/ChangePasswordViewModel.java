package com.dl.playfun.ui.mine.changepassword;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dl.playfun.R;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.ui.mine.resetpassword.ResetPasswordFragment;
import com.dl.playfun.viewmodel.BaseViewModel;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author wulei
 */
public class ChangePasswordViewModel extends BaseViewModel<AppRepository> {

    public ObservableField<String> oldPassword = new ObservableField<>();
    public ObservableField<String> newPassword = new ObservableField<>();
    //隐私设置按钮的点击事件
    public BindingCommand forgotPasswordOnClickCommand = new BindingCommand(() -> start(ResetPasswordFragment.class.getCanonicalName()));
    //推送设置按钮的点击事件
    public BindingCommand commitOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            if (TextUtils.isEmpty(oldPassword.get())) {
                ToastUtils.showShort(R.string.please_input_old_passwrod);
                return;
            }
            if (TextUtils.isEmpty(newPassword.get())) {
                ToastUtils.showShort(R.string.please_input_new_password);
                return;
            }
            if (newPassword.get().length() < 8 || oldPassword.get().length() < 8) {
                ToastUtils.showShort(R.string.please_input_password_min_len);
                return;
            }
            hideKeyboard();
            updataPassword();
        }
    });

    public ChangePasswordViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
    }

    /**
     * 修改密码
     */
    private void updataPassword() {
        model.password(oldPassword.get(), newPassword.get())
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ToastUtils.showShort(R.string.alter_success);
                        pop();
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }
}