package com.mvsee.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Messenger;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.app.Injection;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.AuthLoginUserEntity;
import com.dl.playfun.entity.TokenEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.ui.WebUrlViewActivity;
import com.dl.playfun.ui.mine.webdetail.WebDetailFragment;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.jessyan.autosize.internal.CustomAdapt;

/**
 * Author: 彭石林
 * Time: 2022/1/8 18:34
 * Description: This is DialogLogin
 */
public class DialogLoginApi extends DialogFragment implements Consumer<Disposable>, CustomAdapt {
    private KProgressHUD hud;
    //facebook登录成功返回
    private AccessToken $facebookAccessToken = null;
    private GoogleSignInAccount $googleSignInAccount = null;

    private CompositeDisposable mCompositeDisposable;

    public Integer Google_Code = 101;
    CallbackManager callbackManager;
    GoogleSignInOptions gso;
    GoogleSignInClient googleSignInClient;
    private LoginManager loginManager;

    private LoginResultListener loginResultListener= null;
    private Context mContext;

    private Activity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }
    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        if (window != null) {
            //设置窗体背景色透明
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            //设置宽高
            WindowManager.LayoutParams layoutParams = window.getAttributes();
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            //透明度
            layoutParams.dimAmount = 0.6f;
            //位置
            layoutParams.gravity = Gravity.CENTER;
            window.setAttributes(layoutParams);
        }
    }

    public void setLoginResultListener(LoginResultListener loginResultListener){
        this.loginResultListener = loginResultListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(com.dl.playfun.R.layout.dialog_login_fragment, container, false);
        init(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //去除Dialog默认头部
        Dialog dialog = getDialog();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setWindowAnimations(com.dl.playfun.R.style.BottomDialog_Animation);
    }

    @SuppressLint("WrongConstant")
    private void init(View viewRoot){
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    Log.e("starpine", "tuic");
                    dismiss();
                    return true;
                }
                return false;
            }
        });
        mContext = viewRoot.getContext();
        try {
            PermissionUtils.permission(Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE).callback(new PermissionUtils.FullCallback() {
                @Override
                public void onGranted(List<String> permissionsGranted) {
                }

                @Override
                public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                }
            }).request();
        } catch (Exception e) {//防止不分机型因为权限获取系统不支持

        }

        TextView text_view1 = viewRoot.findViewById(com.dl.playfun.R.id.text_view1);
        text_view1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DialogLoginApi.this.mActivity, WebUrlViewActivity.class);
                intent.putExtra("arg_web_url",AppConfig.TERMS_OF_SERVICE_URL);
                startActivity(intent);
            }
        });
        TextView text_view2 = viewRoot.findViewById(com.dl.playfun.R.id.text_view2);
        text_view2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DialogLoginApi.this.mActivity, WebUrlViewActivity.class);
                intent.putExtra("arg_web_url",AppConfig.PRIVACY_POLICY_URL);
                startActivity(intent);
            }
        });
        ImageView faceBookLoginButton = viewRoot.findViewById(com.dl.playfun.R.id.facebook_login_button);
        ImageView googleLoginButton = viewRoot.findViewById(com.dl.playfun.R.id.google_login_button);
        CheckBox loginCheckBox = viewRoot.findViewById(com.dl.playfun.R.id.login_check);
        TextView text_view = viewRoot.findViewById(com.dl.playfun.R.id.text_view);
        text_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginCheckBox.setChecked(true);
            }
        });
        mCompositeDisposable = new CompositeDisposable();

        //faceBook登录管理
        loginManager = LoginManager.getInstance();
        callbackManager = CallbackManager.Factory.create();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (isLoggedIn && loginManager != null) {
            loginManager.logOut();
            //viewModel.authLogin(accessToken.getUserId(), "facebook", null, null, null);
        }
        /**
         * 谷歌退出登录
         */
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
        if (account != null && googleSignInClient != null) {
            googleSignInClient.signOut();
        }
        faceBookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!loginCheckBox.isChecked()) {
                    ToastUtils.showShort(com.dl.playfun.R.string.playfun_warn_agree_terms);
                    return;
                }
                Collection<String> collection = new ArrayList<String>();
                collection.add("email");
                loginManager.logIn(DialogLoginApi.this, collection);
            }
        });
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        $facebookAccessToken = loginResult.getAccessToken();
                        // App code
                        String userId = loginResult.getAccessToken().getUserId();
//                        facebook登录
                        //viewModel.authLogin(userId, "facebook", null, null, null);
                        authLoginPost(userId,"facebook");
                        AppContext.instance().logEvent(AppsFlyerEvent.LOG_IN_WITH_FACEBOOK);
                    }

                    @Override
                    public void onCancel() {
                        // App code
                        Log.e("FaceBook登录", "取消登录");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.e("FaceBook登入异常返回:", exception.getMessage());
                        loginResultListener.authLoginError(-1,1,exception.getMessage());
                        // App code
                        ToastUtils.showShort(com.dl.playfun.R.string.playfun_error_facebook);
                    }
                });
        GoogleLogin();
        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!loginCheckBox.isChecked()) {
                    ToastUtils.showShort(com.dl.playfun.R.string.playfun_warn_agree_terms);
                    return;
                }
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent, Google_Code);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

        if (requestCode == Google_Code) {
            Task<GoogleSignInAccount> signedInAccountFromIntent = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleResult(signedInAccountFromIntent);
        }
    }
    //谷歌登录
    private void GoogleLogin() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(getContext(), gso);
    }

    private void handleResult(Task<GoogleSignInAccount> googleData) {
        try {
            GoogleSignInAccount signInAccount = googleData.getResult(ApiException.class);
            if (signInAccount != null) {
//                String str =   signInAccount.getEmail()+"\n"
//                        +signInAccount.getId()+"\n"+
//                        signInAccount.getAccount().name+"\n"+
//                        signInAccount.getDisplayName()+"\n"+
//                        signInAccount.getGivenName()+"\n";
                //谷歌登录成功回调
               // viewModel.authLogin(signInAccount.getId(), "google", null, null, null);
                if(loginResultListener!=null){
                    $googleSignInAccount = signInAccount;
                    authLoginPost(signInAccount.getId(),"google");
                }
                AppContext.instance().logEvent(AppsFlyerEvent.LOG_IN_WITH_GOOGLE);
            } else {
                Log.e("account", "si" + "\n");
            }

        } catch (ApiException e) {
            String errorMessage = StringUtils.getString(R.string.playfun_error_google);
            switch (e.getStatusCode()) {
                case 2:
                    ToastUtils.showLong(com.dl.playfun.R.string.playfun_error_google_2);
                    errorMessage = StringUtils.getString(R.string.playfun_error_google_2);
                    break;
                case 3:
                    ToastUtils.showLong(com.dl.playfun.R.string.playfun_error_google_3);
                    errorMessage = StringUtils.getString(R.string.playfun_error_google_3);
                    break;
                case 4:
                    ToastUtils.showLong(com.dl.playfun.R.string.playfun_error_google_4);
                    errorMessage = StringUtils.getString(R.string.playfun_error_google_4);
                    break;
                case 5:
                    ToastUtils.showLong(com.dl.playfun.R.string.playfun_error_google_5);
                    errorMessage = StringUtils.getString(R.string.playfun_error_google_5);
                    break;
                case 6:
                    ToastUtils.showLong(com.dl.playfun.R.string.playfun_error_google_6);
                    errorMessage = StringUtils.getString(R.string.playfun_error_google_6);
                    break;
                case 7:
                    ToastUtils.showLong(com.dl.playfun.R.string.playfun_error_google_7);
                    errorMessage = StringUtils.getString(R.string.playfun_error_google_7);
                    break;
                case 8:
                    ToastUtils.showLong(com.dl.playfun.R.string.playfun_error_google_8);
                    errorMessage = StringUtils.getString(R.string.playfun_error_google_8);
                    break;
                case 13:
                    ToastUtils.showLong(com.dl.playfun.R.string.playfun_error_google_13);
                    errorMessage = StringUtils.getString(R.string.playfun_error_google_13);
                    break;
                case 14:
                    ToastUtils.showLong(com.dl.playfun.R.string.playfun_error_google_14);
                    errorMessage = StringUtils.getString(R.string.playfun_error_google_14);
                    break;
                case 15:
                    ToastUtils.showLong(com.dl.playfun.R.string.playfun_error_google_15);
                    errorMessage = StringUtils.getString(R.string.playfun_error_google_15);
                    break;
                case 16:
                    ToastUtils.showLong(com.dl.playfun.R.string.playfun_error_google_16);
                    errorMessage = StringUtils.getString(R.string.playfun_error_google_16);
                    break;
                case 17:
                    ToastUtils.showLong(com.dl.playfun.R.string.playfun_error_google_17);
                    errorMessage = StringUtils.getString(R.string.playfun_error_google_17);
                    break;
                case 20:
                    ToastUtils.showLong(com.dl.playfun.R.string.playfun_error_google_20);
                    errorMessage = StringUtils.getString(R.string.playfun_error_google_20);
                    break;
                case 21:
                    ToastUtils.showLong(com.dl.playfun.R.string.playfun_error_google_21);
                    errorMessage = StringUtils.getString(R.string.playfun_error_google_21);
                    break;
                case 22:
                    ToastUtils.showLong(com.dl.playfun.R.string.playfun_error_google_22);
                    errorMessage = StringUtils.getString(R.string.playfun_error_google_22);
                    break;
                case 12500:
                    ToastUtils.showLong(com.dl.playfun.R.string.playfun_error_google_12500);
                    errorMessage = StringUtils.getString(R.string.playfun_error_google_12500);
                    break;
                default:
                    ToastUtils.showLong(com.dl.playfun.R.string.playfun_error_google);
                    errorMessage = StringUtils.getString(R.string.playfun_error_google);
                    break;
            }
            loginResultListener.authLoginError(e.getStatusCode(),2,errorMessage);
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

    @Override
    public void dismiss() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
        }
        super.dismiss();
    }

    public Context getContext(){
        return mContext;
    }

    //第三方登录
    public void authLoginPost(String authId,String type){
        AppContext.instance().appRepository.authLoginPost(authId,type)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<AuthLoginUserEntity>>(){
                    @Override
                    public void onSuccess(BaseDataResponse<AuthLoginUserEntity> authLoginUserEntityBaseDataResponse) {
                        AuthLoginUserEntity authLoginUserEntity = authLoginUserEntityBaseDataResponse.getData();
                        authLoginUserEntity.setAuthUserId(authId);
                        if(type.equals("facebook")){
                            authLoginUserEntity.setTypeLogin(1);
                        }else{
                            authLoginUserEntity.setTypeLogin(2);
                        }
                        TokenEntity tokenEntity = new TokenEntity(authLoginUserEntity.getToken(),authLoginUserEntity.getUserID(),authLoginUserEntity.getUserSig(), authLoginUserEntity.getIsContract());
                        AppContext.instance().appRepository.saveLoginInfo(tokenEntity);
                        if(authLoginUserEntity!=null){
                            loadProfile(authLoginUserEntity);
                        }

                    }
                    @Override
                    public void onComplete() {
                        dismissHud();
                    }
                });
    }

    /**
     * 加载用户资料
     */
    private void loadProfile(AuthLoginUserEntity authLoginUserEntity) {
        //RaJava模拟登录
        Injection.provideDemoRepository().getUserData()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<UserDataEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<UserDataEntity> response) {
                        UserDataEntity userDataEntity = response.getData();
                        AppContext.instance().mFirebaseAnalytics.setUserId(String.valueOf(userDataEntity.getId()));
                        AppContext.instance().appRepository.saveUserData(userDataEntity);
                        if (userDataEntity.getCertification() == 1) {
                            AppContext.instance().appRepository.saveNeedVerifyFace(true);
                        }
                        if(loginResultListener!=null){
                            loginResultListener.authLoginSuccess(authLoginUserEntity,$facebookAccessToken,$googleSignInAccount);
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return 360;
    }

    public interface LoginResultListener {
        void authLoginSuccess(AuthLoginUserEntity authLoginUserEntity,AccessToken accessToken,GoogleSignInAccount googleSignInAccount);
        void authLoginError(int code,int type,String message);
    }

    private void showHUD(){

        if (hud == null) {
            ProgressBar progressBar = new ProgressBar(getContext());
            progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(com.dl.playfun.R.color.white), PorterDuff.Mode.SRC_IN);

            hud = KProgressHUD.create(mActivity)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setBackgroundColor(getResources().getColor(com.dl.playfun.R.color.hud_background))
                    .setLabel(null)
                    .setCustomView(progressBar)
                    .setSize(100, 100)
                    .setCancellable(false);
        }
        hud.show();
    }

    public void dismissHud() {
        if (hud != null && hud.isShowing()) {
            hud.dismiss();
        }
    }
}
