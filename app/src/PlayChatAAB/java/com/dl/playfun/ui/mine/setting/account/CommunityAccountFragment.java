package com.dl.playfun.ui.mine.setting.account;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.entity.OverseasUserEntity;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.ui.login.GoogleApiError;
import com.dl.playfun.databinding.FragmentSettingAccountBinding;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
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
 * @ClassName CommunityAccount
 * @Description TODO
 * @Author 彭石林
 * @Date 2021/4/29 10:31
 * @Phone 16620350375
 * @email 15616314565@163.com
 * @Version 1.0
 **/
public class CommunityAccountFragment extends BaseToolbarFragment<FragmentSettingAccountBinding,CommunityAccountModel> {

    public Integer Google_Code = 101;
    CallbackManager callbackManager;
    GoogleSignInOptions gso;
    GoogleSignInClient googleSignInClient;
    private LoginManager loginManager;

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_setting_account;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public CommunityAccountModel initViewModel() {
        //faceBook登录管理
        loginManager = LoginManager.getInstance();
        //使用自定义的ViewModelFactory来创建ViewModel，如果不重写该方法，则默认会调用LoginViewModel(@NonNull Application application)构造方法
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(CommunityAccountModel.class);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
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
        binding.facebookLogin.setOnClickListener(view -> {
            if(viewModel.userBindInfoEntity.get() ==null){
                return;
            }
            if(viewModel.userBindInfoEntity.get().getBindAuth()>0){
                return;
            }
            Collection<String> collection = new ArrayList<>();
            collection.add("email");
            loginManager.logIn(CommunityAccountFragment.this, collection);
        });
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        String userId = loginResult.getAccessToken().getUserId();
                        // App code
                        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(@Nullable JSONObject jsonObject, @Nullable GraphResponse graphResponse) {
                                try {
                                    String token_for_business = null;
                                    if (!jsonObject.isNull("token_for_business")) {
                                        token_for_business = jsonObject.getString("token_for_business");
                                    }
                                    viewModel.bindAccount(2,userId, "facebook", token_for_business);
                                } catch (Exception e) {
                                    Log.e("获取facebook关键资料", "异常原因: " + e.getMessage());
                                    // App code
                                    ToastUtils.showShort(R.string.playfun_error_facebook);
                                }
                            }
                        });
                        Bundle paramters = new Bundle();
                        paramters.putString("fields", "token_for_business,email");
                        request.setParameters(paramters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        // App code
                        Log.e("FaceBook登录", "取消登录");
                    }

                    @Override
                    public void onError(@NonNull FacebookException exception) {
                        // App code
                        ToastUtils.showShort(R.string.playfun_error_facebook);
                    }
                });
        GoogleLogin();
        binding.googleLogin.setOnClickListener(v -> {
            if(viewModel.userBindInfoEntity.get() ==null){
                return;
            }
            if(viewModel.userBindInfoEntity.get().getBindAuth()>0){
                return;
            }
            Intent intent = googleSignInClient.getSignInIntent();
            toGoogleLoginIntent.launch(intent);
        });

    }

    ActivityResultLauncher<Intent> toGoogleLoginIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

        if (result.getData() != null) {
            Task<GoogleSignInAccount> signedInAccountFromIntent = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
            handleResult(signedInAccountFromIntent);
        }
    });

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
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
                viewModel.bindAccount(3,signInAccount.getId(), "google",null);
            } else {
                ToastUtils.showShort(R.string.playfun_error_google);
            }

        } catch (ApiException e) {
            e.printStackTrace();
            GoogleApiError.toastError(e.getStatusCode());
        }

    }
}