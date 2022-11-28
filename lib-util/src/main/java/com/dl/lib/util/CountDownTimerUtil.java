package com.dl.lib.util;

import android.os.Handler;
import android.os.Message;

/**
 * 倒计时-开始、暂停、重置、取消、销毁
 */
public abstract class CountDownTimerUtil {
    private static final int MSG_RUN = 1;

    private final long mCountdownInterval;// 定时间隔，以毫秒计
    private long mTotalTime;// 定时时间
    private long mRemainTime;// 剩余时间

    // 构造函数
    public CountDownTimerUtil(long millisInFuture, long countDownInterval) {
        mTotalTime = millisInFuture;
        mCountdownInterval = countDownInterval;
        mRemainTime = millisInFuture;
    }

    // 取消计时
    public final void cancel() {
        mHandler.removeMessages(MSG_RUN);
    }

    // 重新开始计时
    public final void resume() {
        mHandler.sendMessageAtFrontOfQueue(mHandler.obtainMessage(MSG_RUN));
    }

    // 暂停计时
    public final void pause() {
        mHandler.removeMessages(MSG_RUN);
    }

    // 开始计时
    public synchronized final CountDownTimerUtil start() {
        if (mRemainTime <= 0) {// 计时结束后返回
            onFinish();
            return this;
        }
        // 设置计时间隔
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_RUN), mCountdownInterval);
        return this;
    }

    public abstract void onTick(long millisUntilFinished, int percent);// 计时中

    public abstract void onFinish();// 计时结束

    // 通过handler更新android UI，显示定时时间
    protected Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            synchronized (CountDownTimerUtil.this) {
                if (msg.what == MSG_RUN) {
                    mRemainTime = mRemainTime - mCountdownInterval;

                    if (mRemainTime <= 0) {
                        onFinish();
                    } else if (mRemainTime < mCountdownInterval) {
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_RUN), mRemainTime);
                    } else {

                        onTick(mRemainTime, Long.valueOf(100 * (mTotalTime - mRemainTime) / mTotalTime).intValue());

                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_RUN),
                                mCountdownInterval);
                    }
                }
            }
            return false;
        }
    });

}
