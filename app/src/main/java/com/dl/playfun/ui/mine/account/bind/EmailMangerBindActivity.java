package com.dl.playfun.ui.mine.account.bind;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.FragmentUtils;
import com.dl.playfun.R;
import com.dl.playfun.api.PlayFunAuthUserEntity;
import com.dl.playfun.api.login.email.LoginEmailPwdViewFragment;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.ArrayList;
import java.util.List;

public class EmailMangerBindActivity extends AppCompatActivity {
    //加载进度条
    private KProgressHUD hud;
    private List<Fragment> mFragments = new ArrayList<>();

    private boolean isBindEmail = false;

    private void initParam(){
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            isBindEmail = bundle.getBoolean("bindEmail");
        }
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initParam();
        this.setContentView(R.layout.actitvity_email_manger_dialog);
        if(isBindEmail){
            if(FragmentUtils.findFragment(getSupportFragmentManager(), AccountEmailBoundFragment.class)!=null){
                mFragments.add(FragmentUtils.findFragment(getSupportFragmentManager(), AccountEmailBoundFragment.class));
            }else{
                mFragments.add(new AccountEmailBoundFragment());
            }
            if(FragmentUtils.findFragment(getSupportFragmentManager(), AccountEmailBoundPwdFragment.class)!=null){
                mFragments.add(FragmentUtils.findFragment(getSupportFragmentManager(), AccountEmailBoundPwdFragment.class));
            }else{
                mFragments.add(new AccountEmailBoundPwdFragment());
            }
        }else{
            mFragments.add(new AccountEmailUpdatePwdFragment());
        }
        FragmentUtils.add(getSupportFragmentManager(), mFragments, R.id.fl_container, 0);
    }



    public void showFragment(int idx){
        FragmentUtils.showHide(idx,mFragments);
    }

    public void setResultPoP(PlayFunAuthUserEntity playFunAuthUserEntity){
        Intent intent = getIntent();
        intent.putExtra("authUser", playFunAuthUserEntity);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onDestroy(){
        FragmentUtils.removeAll(getSupportFragmentManager());
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(FragmentUtils.getTopShow(getSupportFragmentManager()) instanceof LoginEmailPwdViewFragment){
            showFragment(0);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }


    void showHUD(){
        if (hud == null) {
            ProgressBar progressBar = new ProgressBar(this);
            progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(com.dl.playfun.R.color.white), PorterDuff.Mode.SRC_IN);

            hud = KProgressHUD.create(this)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setBackgroundColor(getResources().getColor(com.dl.playfun.R.color.hud_background))
                    .setLabel(null)
                    .setCustomView(progressBar)
                    .setSize(100, 100)
                    .setCancellable(false);
        }
        hud.show();
    }

    void dismissHud() {
        if (hud != null && hud.isShowing()) {
            hud.dismiss();
        }
    }
}
