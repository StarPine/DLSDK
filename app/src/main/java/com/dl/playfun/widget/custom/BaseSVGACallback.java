package com.dl.playfun.widget.custom;

import com.opensource.svgaplayer.SVGACallback;

public abstract class BaseSVGACallback implements SVGACallback {

    @Override
    public void onFinished() {
        onComplete();
    }

    @Override
    public void onPause() {
        onPauseOrRepeat(true);
    }

    @Override
    public void onRepeat() {
        onPauseOrRepeat(false);
    }

    @Override
    public void onStep(int i, double v) {
        onStepCallBack(i, v);
    }

    public void onStepCallBack(int i, double v) {

    }

    public void onPauseOrRepeat(boolean pause) {

    }

    public abstract void onComplete ();

}
