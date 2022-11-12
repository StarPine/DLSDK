package com.dl.playfun.ui.coinpusher;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ResizeWidthAnimation extends Animation {

    private final int mWidth;
    private final int mStartWidth;

    private final int mHeight;
    private final int mStartHeight;

    private final View mView;

    public ResizeWidthAnimation(View view, int width,int height) {
        mView = view;
        mWidth = width;
        mHeight = height;
        mStartWidth = view.getWidth();
        mStartHeight = view.getHeight();
    }

    @Override

    protected void applyTransformation(float interpolatedTime, Transformation t) {

        int newWidth = mStartWidth + (int) ((mWidth - mStartWidth) * interpolatedTime);
        int newHeight = mStartHeight + (int) ((mHeight - mStartHeight) * interpolatedTime);
        mView.getLayoutParams().width = newWidth;
        mView.getLayoutParams().height = newHeight;
        mView.requestLayout();

    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(mWidth, mHeight, parentWidth, parentHeight);
    }

    @Override

    public boolean willChangeBounds() {
        return true;
    }

}