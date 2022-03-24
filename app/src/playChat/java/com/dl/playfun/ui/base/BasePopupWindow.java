package com.dl.playfun.ui.base;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.PopupWindow;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class BasePopupWindow extends PopupWindow implements Consumer<Disposable> {

    private CompositeDisposable mCompositeDisposable;

    public BasePopupWindow(Context context) {
        super(context);
        init();
    }

    public BasePopupWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BasePopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public BasePopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public BasePopupWindow() {
        init();
    }

    public BasePopupWindow(View contentView) {
        super(contentView);
        init();
    }

    public BasePopupWindow(int width, int height) {
        super(width, height);
        init();
    }

    public BasePopupWindow(View contentView, int width, int height) {
        super(contentView, width, height);
        init();
    }

    public BasePopupWindow(View contentView, int width, int height, boolean focusable) {
        super(contentView, width, height, focusable);
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

}
