package com.dl.playfun.ui.mine.account.bind;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.api.PlayFunAuthUserEntity;
import com.dl.playfun.api.login.email.LoginEmailMangerActivity;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.TokenEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.widget.custom.InputTextManager;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

public class AccountEmailBoundFragment extends Fragment implements Consumer<Disposable>, View.OnClickListener{
    private Activity mActivity;
    private Context mContext;
    //邮箱
    private EditText edtEmail;
    //邮箱验证码
    private EditText editCode;
    //获取邮箱验证码
    private Button btnCode;
    //提交绑定
    private Button btnSubmit;
    //关闭弹窗
    private AppCompatImageView imgClose;

    private CompositeDisposable mCompositeDisposable;

    /**
     * 倒计时60秒，一次1秒
     */
    private CountDownTimer downTimer;
    private boolean isDownTime;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = (Activity) context;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(getLayoutResId(), container, false);
        initView(view);
        return view;
    }
    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.dialog_account_email_bound;
    }
    private void initView(View view){
        edtEmail = view.findViewById(R.id.edit_email);
        editCode = view.findViewById(R.id.edit_code);
        btnCode = view.findViewById(R.id.btn_code);
        btnSubmit = view.findViewById(R.id.btn_submit);
        imgClose = view.findViewById(R.id.img_close);
        imgClose.setOnClickListener(this);
        btnCode.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        InputTextManager.with(mActivity)
                .addView(edtEmail)
                .setMain(btnCode)
                .build();
        InputTextManager.with(mActivity)
                .addView(edtEmail)
                .addView(editCode)
                .setMain(btnSubmit)
                .build();
    }
    protected void addSubscribe(Disposable disposable) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(disposable);
    }

    @Override
    public void accept(Disposable disposable) throws Exception {
        if (disposable != null) {
            addSubscribe(disposable);
        }
    }

    @Override
    public void onDestroyView() {
        cancelDownTime();
        super.onDestroyView();
        //View销毁时会执行，同时取消所有异步任务
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
        }
    }
    //验证码倒计时
    public void verifyCodeDownTime(){
        downTimer = new CountDownTimer(60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                isDownTime = true;
                btnCode.setText((millisUntilFinished / 1000 )+"s");
            }

            @Override
            public void onFinish() {
                isDownTime = false;
                btnCode.setText("获取");
            }
        }.start();
    }


    private void showHUD() {
        if (mActivity instanceof EmailMangerBindActivity) {
            ((EmailMangerBindActivity) mActivity).showHUD();
        }
    }

    public void dismissHud() {
        if (mActivity instanceof EmailMangerBindActivity) {
            ((EmailMangerBindActivity) mActivity).dismissHud();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_code) {
            sendUserEmailCode(edtEmail.getText().toString());
            return;
        }else if (v.getId() == R.id.btn_submit){
            bindUserEmail(edtEmail.getText().toString(),editCode.getText().toString());
        }else if(v.getId() == R.id.img_close){
            mActivity.finish();
        }
    }
    public void cancelDownTime(){
        if(isDownTime){
            downTimer.cancel();
            downTimer = null;
        }
    }

    /**
     * 发送邮箱验证码
     * @param userEmailCode
     */
    public void sendUserEmailCode(String userEmailCode){
        ConfigManager.getInstance().getAppRepository()
                .sendEmailCode(userEmailCode)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>(){
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {
                        dismissHud();
                        verifyCodeDownTime();
                    }
                    @Override
                    public void onError(RequestException e) {
                        dismissHud();
                    }
                });
    }

    /**
     * 绑定用户邮箱
     * @param userEmail
     * @param userCode
     */
    public void bindUserEmail(String userEmail,String userCode){
        if(StringUtils.isTrimEmpty(userEmail)){
            ToastUtils.showShort("请填写邮箱");
            return;
        }
        if(StringUtils.isTrimEmpty(userCode)){
            ToastUtils.showShort("请填写验证码");
            return;
        }
        ConfigManager.getInstance().getAppRepository()
                .bindUserEmail(userEmail,userCode,null,1)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>(){
                    @Override
                    public void onSuccess(BaseResponse baseResponse) {
                        if (mActivity instanceof EmailMangerBindActivity) {
                            ((EmailMangerBindActivity) mActivity).showFragment(1);
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHud();
                    }
                });
    }
}
