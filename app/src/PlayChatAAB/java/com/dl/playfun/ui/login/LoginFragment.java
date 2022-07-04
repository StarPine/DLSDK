package com.dl.playfun.ui.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.databinding.FragmentLoginBinding;
import com.dl.playfun.ui.base.BaseFragment;
import com.dl.playfun.ui.login.LoginViewModel;
import com.dl.playfun.utils.AutoSizeUtils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author wulei
 */
public class LoginFragment extends BaseFragment<FragmentLoginBinding, LoginViewModel>  {

    public Integer Google_Code = 101;
    CallbackManager callbackManager;
    GoogleSignInOptions gso;
    GoogleSignInClient googleSignInClient;
    private LoginManager loginManager;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AutoSizeUtils.applyAdapt(getResources());
        return R.layout.fragment_login;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public LoginViewModel initViewModel() {
        TelephonyManager telephonyMngr = (TelephonyManager) mActivity.getSystemService(Context.TELEPHONY_SERVICE);
        Log.e("当前国家区号",telephonyMngr.getSimCountryIso().toUpperCase()+"====="+telephonyMngr.toString());
        AppContext.instance().logEvent(AppsFlyerEvent.Login_screen);
        //faceBook登录管理
        loginManager = LoginManager.getInstance();
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(LoginViewModel.class);
    }

    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        super.onEnterAnimationEnd(savedInstanceState);
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
        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!viewModel.agree.get()) {
                    ToastUtils.showShort(R.string.playfun_warn_agree_terms);
                    return;
                }
                Collection<String> collection = new ArrayList<String>();
                collection.add("email");
                collection.add("public_profile");
                loginManager.logIn(LoginFragment.this, collection);
            }
        });
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        String userId = loginResult.getAccessToken().getUserId();
                        String accessToken = loginResult.getAccessToken().getToken();
                        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(@Nullable JSONObject jsonObject, @Nullable GraphResponse graphResponse) {
                                try {
                                    viewModel.authLogin(loginResult.getAccessToken().getUserId(), "facebook", null, null, null, jsonObject.getString("token_for_business"));
                                    AppContext.instance().logEvent(AppsFlyerEvent.LOG_IN_WITH_FACEBOOK);
                                } catch (Exception e) {
                                    Log.e("获取facebook关键资料", "异常原因: " + e.getMessage());
                                    // App code
                                    ToastUtils.showShort(R.string.playfun_error_facebook);
                                }
                            }
                        });
                        Bundle paramters = new Bundle();
                        paramters.putString("fields", "token_for_business");
                        request.setParameters(paramters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        // App code
                        Log.e("FaceBook登录", "取消登录");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.e("FaceBook登入异常返回:", exception.getMessage());
                        // App code
                        ToastUtils.showShort(R.string.playfun_error_facebook);
                    }
                });
        GoogleLogin();
        binding.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!viewModel.agree.get()) {
                    ToastUtils.showShort(R.string.playfun_warn_agree_terms);
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
                viewModel.authLogin(signInAccount.getId(), "google", null, null, null, null);
                AppContext.instance().logEvent(AppsFlyerEvent.LOG_IN_WITH_GOOGLE);
            } else {
                Log.e("account", "si" + "\n");
            }

        } catch (ApiException e) {
            switch (e.getStatusCode()) {
                case 2:
                    ToastUtils.showLong(R.string.playfun_error_google_2);
                    break;
                case 3:
                    ToastUtils.showLong(R.string.playfun_error_google_3);
                    break;
                case 4:
                    ToastUtils.showLong(R.string.playfun_error_google_4);
                    break;
                case 5:
                    ToastUtils.showLong(R.string.playfun_error_google_5);
                    break;
                case 6:
                    ToastUtils.showLong(R.string.playfun_error_google_6);
                    break;
                case 7:
                    ToastUtils.showLong(R.string.playfun_error_google_7);
                    break;
                case 8:
                    ToastUtils.showLong(R.string.playfun_error_google_8);
                    break;
                case 13:
                    ToastUtils.showLong(R.string.playfun_error_google_13);
                    break;
                case 14:
                    ToastUtils.showLong(R.string.playfun_error_google_14);
                    break;
                case 15:
                    ToastUtils.showLong(R.string.playfun_error_google_15);
                    break;
                case 16:
                    ToastUtils.showLong(R.string.playfun_error_google_16);
                    break;
                case 17:
                    ToastUtils.showLong(R.string.playfun_error_google_17);
                    break;
                case 20:
                    ToastUtils.showLong(R.string.playfun_error_google_20);
                    break;
                case 21:
                    ToastUtils.showLong(R.string.playfun_error_google_21);
                    break;
                case 22:
                    ToastUtils.showLong(R.string.playfun_error_google_22);
                    break;
                case 12500:
                    ToastUtils.showLong(R.string.playfun_error_google_12500);
                    break;
                default:
                    ToastUtils.showLong(R.string.playfun_error_google);
                    break;
            }
//            Log.e("谷歌登录异常2",e.getLocalizedMessage());
            //ToastUtils.showShort(R.string.error_google);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
