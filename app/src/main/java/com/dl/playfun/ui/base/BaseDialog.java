package com.dl.playfun.ui.base;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.dl.playfun.R;
import com.kaopiz.kprogresshud.KProgressHUD;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Author: 彭石林
 * Time: 2021/9/29 14:41
 * Description: This is BaseDialog
 */
public class BaseDialog  extends Dialog implements Consumer<Disposable>, LifecycleOwner {

    private KProgressHUD hud;

    private CompositeDisposable mCompositeDisposable;

    private Activity mActivity;

    private final LifecycleRegistry mLifecycle = new LifecycleRegistry(this);

    public Activity getMActivity() {
        return mActivity;
    }

    public void setMActivity(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public BaseDialog(Context context) {
        super(context);
        init();
    }

    public BaseDialog(Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    private void init() {
        mCompositeDisposable = new CompositeDisposable();
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

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mLifecycle;
    }

    /**
     * {@link DialogInterface.OnDismissListener}
     */
//    @Override
//    public void onDismiss(DialogInterface dialog) {
//        mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
    }

    private void showHud(String title) {
        if(getMActivity()==null){
            return;
        }
        if (hud == null) {
            ProgressBar progressBar = new ProgressBar(getMActivity());
            progressBar.getIndeterminateDrawable().setColorFilter(getContext().getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);

            hud = KProgressHUD.create(getMActivity())
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setBackgroundColor(getContext().getResources().getColor(R.color.hud_background))
                    .setLabel(title)
                    .setCustomView(progressBar)
                    .setSize(100, 100)
                    .setCancellable(false);
        }
        hud.show();
    }

    public void showHud() {
        showHud("");
    }

    public void dismissHud() {
        if(getMActivity()==null){
            return;
        }
        if (hud != null && hud.isShowing()) {
            hud.dismiss();
        }
    }
}
