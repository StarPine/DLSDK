package com.dl.playfun.ui.base;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;

import com.dl.playfun.R;
import com.dl.playfun.manager.LocaleManager;
import com.dl.playfun.ui.mine.vipsubscribe.VipSubscribeFragment;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.dl.playfun.widget.dialog.loading.DialogProgress;
import com.dl.playfun.widget.progress.MPCircleProgressBar;
import com.gyf.immersionbar.ImmersionBar;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.Map;

/**
 * Author: 彭石林
 * Time: 2022/7/29 17:05
 * Description: This is BaseActivity
 */
public abstract class BaseActivity<V extends ViewDataBinding, VM extends BaseViewModel> extends me.goldze.mvvmhabit.base.BaseActivity<V, VM> {
    private KProgressHUD hud;
    private KProgressHUD progressHud;
    private DialogProgress dialogProgress;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.setLocal(newBase));
    }

    /**
     * 就算你在Manifest.xml设置横竖屏切换不重走生命周期。横竖屏切换还是会走这里

     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if(newConfig!=null){
            LocaleManager.setLocal(this);
        }
        super.onConfigurationChanged(newConfig);
        LocaleManager.setLocal(this);
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        LocaleManager.setLocal(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusView();
        View statusView = findViewById(R.id.status_bar_view);
        if (statusView != null) {
            ImmersionBar.setStatusBarView(this, statusView);
        }
        if (viewModel != null) {
            viewModel.onViewCreated();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setStatusView() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//实现状态栏图标和文字颜色为暗色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    @Override
    protected void registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack();
        viewModel.getMuc().showHudEvent.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                showHud(s);
            }
        });
        viewModel.getMuc().showProgressHudEvent.observe(this, new Observer<Map<String, Object>>() {
            @Override
            public void onChanged(Map<String, Object> map) {
                String title = (String) map.get("title");
                int progress = (int) map.get("progress");
                showProgressHud(title, progress);
            }
        });
        viewModel.getMuc().dismissHudEvent.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void v) {
                dismissHud();
            }
        });

        viewModel.getMuc().startFragmentEvent.observe(this, new Observer<Map<String, Object>>() {
            @Override
            public void onChanged(Map<String, Object> params) {
                String canonicalName = (String) params.get(BaseViewModel.ParameterField.FRAGMENT_NAME);
                Bundle bundle = (Bundle) params.get(BaseViewModel.ParameterField.BUNDLE);
                startOtherActivity(canonicalName, bundle);
            }
        });
    }

    protected void startOtherActivity(String canonicalName, Bundle bundle) {
        Intent intent = new Intent(this, OtherFragmentActivity.class);
        intent.putExtra(BaseViewModel.ParameterField.FRAGMENT_NAME,canonicalName);
        intent.putExtra(BaseViewModel.ParameterField.BUNDLE,canonicalName);
        startActivity(intent);
    }

    public void showHud() {
        showHud("");
    }

    private void showHud(String title) {
        if (progressHud != null && progressHud.isShowing()) {
            progressHud.dismiss();
        }

        if (hud == null) {
            ProgressBar progressBar = new ProgressBar(getContext());
            progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);

            hud = KProgressHUD.create(this.getContext())
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setBackgroundColor(getResources().getColor(R.color.hud_background))
                    .setLabel(title)
                    .setCustomView(progressBar)
                    .setSize(100, 100)
                    .setCancellable(false);
        }
        hud.show();
    }

    public void showProgressHud(String title, int progress) {
        if (hud != null && hud.isShowing()) {
            hud.dismiss();
        }
        if (progressHud == null) {
            progressHud = KProgressHUD.create(getContext())
                    .setStyle(KProgressHUD.Style.BAR_DETERMINATE)
//                    .setBackgroundColor(getResources().getColor(R.color.hud_background))
                    .setCancellable(false)
//                    .setSize(100, 100)
                    .setMaxProgress(100)
                    .setCustomView(new MPCircleProgressBar(getContext()))
                    .show();
        }
        progressHud.setLabel(title);
        progressHud.setProgress(progress);
        if (!progressHud.isShowing()) {
            progressHud.show();
        }
    }

    public void dismissHud() {
        if (hud != null && hud.isShowing()) {
            hud.dismiss();
        }
        if (progressHud != null && progressHud.isShowing()) {
            progressHud.dismiss();
        }
    }

    public Context getContext(){
        return this;
    }
}
