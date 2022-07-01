package com.dl.playfun.ui.login.register;

import android.content.Intent;
import android.os.Bundle;
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
import com.dl.playfun.databinding.FragmentRegisterBinding;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.ui.login.LoginViewModel;
import com.dl.playfun.utils.ImmersionBarUtils;
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
public class RegisterFragment extends BaseToolbarFragment<FragmentRegisterBinding, LoginViewModel> {

    public Integer Google_Code = 101;
    CallbackManager callbackManager;
    GoogleSignInOptions gso;
    GoogleSignInClient googleSignInClient;
    private LoginManager loginManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ImmersionBarUtils.setupStatusBar(this, true, true);
        return view;
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_register;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public LoginViewModel initViewModel() {
        //faceBook登录管理
        loginManager = LoginManager.getInstance();
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(LoginViewModel.class);
    }

    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
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
        binding.facebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collection<String> collection = new ArrayList<String>();
                collection.add("email");
                loginManager.logIn(RegisterFragment.this, collection);
            }
        });
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
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
                        // App code
                        ToastUtils.showShort(R.string.playfun_error_facebook);
                    }
                });
        GoogleLogin();
        binding.googleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent, Google_Code);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
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
                String str = signInAccount.getEmail() + "\n"
                        + signInAccount.getId() + "\n" +
                        signInAccount.getAccount().name + "\n" +
                        signInAccount.getDisplayName() + "\n" +
                        signInAccount.getGivenName() + "\n";
                viewModel.authLogin(signInAccount.getId(), "google", null, null, null, null);
                AppContext.instance().logEvent(AppsFlyerEvent.LOG_IN_WITH_GOOGLE);
            } else {
                Log.e("account", "si为空:" + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.showShort(R.string.playfun_error_google);
        }

    }

}
