package com.dl.playfun.ui.mine.account.bind;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.dl.playfun.R;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.widget.custom.InputTextManager;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import me.goldze.mvvmhabit.utils.RxUtils;

public class AccountEmailBoundPwdFragment extends Fragment implements Consumer<Disposable>, View.OnClickListener{

    private Activity mActivity;
    private Context mContext;
    //密码1
    private EditText pwd;
    //密码2
    private EditText pwd2;
    //提交绑定
    private Button btnSubmit;
    //关闭弹窗
    private AppCompatImageView imgClose;

    private CompositeDisposable mCompositeDisposable;

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

    private void initView(View view){
        pwd = view.findViewById(R.id.edit_email_pwd);
        pwd2 = view.findViewById(R.id.edit_email_pwd2);
        btnSubmit = view.findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(this);
        imgClose = view.findViewById(R.id.img_close);
        imgClose.setOnClickListener(this);
        InputTextManager.with(this.getActivity())
                .addView(pwd)
                .addView(pwd2)
                .setMain(btnSubmit)
                .build();
    }

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.dialog_account_email_bound_pwd;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_submit){
            String text = pwd.getText().toString();
            String text2 = pwd2.getText().toString();
            if(text.trim().equals(text2.trim())){
                bindUserEmail(text);
            }else{
                ToastUtils.showShort("两次密码不一致");
            }
        }else if(v.getId() == R.id.img_close){
            mActivity.finish();
        }
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

    /**
     * 绑定用户密码
     */
    public void bindUserEmail(String pwd){
        ConfigManager.getInstance().getAppRepository()
                .bindUserEmail(null,null,pwd,2)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<UserDataEntity>>(){
                    @Override
                    public void onSuccess(BaseDataResponse<UserDataEntity> authLoginUserEntityBaseDataResponse) {
                        UserDataEntity userDataEntity = ConfigManager.getInstance().getAppRepository().readUserData();
                        if(userDataEntity!=null){
                            userDataEntity.setIsPassword(1);
                            ConfigManager.getInstance().getAppRepository().saveUserData(userDataEntity);
                        }
                        //关闭act弹窗
                        mActivity.finish();
                    }
                    @Override
                    public void onComplete() {
                        dismissHud();
                    }
                });
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
}
