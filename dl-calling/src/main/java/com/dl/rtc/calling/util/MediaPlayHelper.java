package com.dl.rtc.calling.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;


import com.dl.lib.util.log.MPTimber;

import java.io.File;
import java.io.IOException;

public class MediaPlayHelper {
    private static final String TAG = "MediaPlayHelper";

    private final Context mContext;
    private final MediaPlayer mMediaPlayer;
    private Handler mHandler;
    private int mResId;   //资源ID,apk内置资源
    private String mResPath; //资源路径,apk沙盒地址,例如:/sdcard/android/data/com.tencent.trtc/files/rain.mp3
    private Uri mResUrl;  //网络资源,例如:https://web.sdk.qcloud.com/component/TUIKit/assets/uni-app/calling-bell-1.mp3

    public MediaPlayHelper(Context context) {
        mContext = context;
        mMediaPlayer = new MediaPlayer();
        mResId = -1;
        mResPath = "";
    }

    public void start(String path) {
        start(path, -1, 0);
    }

    public void start(int resId) {
        start(resId, 0);
    }

    public void start(int resId, long duration) {
        start("", resId, duration);
    }

    private void start(String resPath, final int resId, long duration) {
        preHandler();
        if (TextUtils.isEmpty(resPath) && (-1 == resId)) {
            MPTimber.tag(TAG).d(" empty source ,please set media resource");
            return;
        }
        if ((-1 != resId && (mResId == resId)) || (!TextUtils.isEmpty(resPath) && TextUtils.equals(mResPath, resPath))) {
             MPTimber.tag(TAG).d("the same media source, ignore");
            return;
        }
        AssetFileDescriptor afd0 = null;
         MPTimber.tag(TAG).d(" music start resPath: " + resPath + " ,resId: " + resId);
        if (!TextUtils.isEmpty(resPath) && isUrl(resPath)) {
            Uri tempUrl = Uri.parse(resPath);
            if (tempUrl.equals(mResUrl)) {
                 MPTimber.tag(TAG).d(" the same resUrl, ignore");
                return;
            }
            mResUrl = Uri.parse(resPath);
        } else if (!TextUtils.isEmpty(resPath) && new File(resPath).exists()) {
            mResPath = resPath;
        } else if (-1 != resId) {
            mResId = resId;
            afd0 = mContext.getResources().openRawResourceFd(resId);
            if (afd0 == null) {
                return;
            }
        }

        final AssetFileDescriptor afd = afd0;
        final Uri finalResUrl = mResUrl;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.setOnCompletionListener(null);
                mMediaPlayer.reset();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    if (null != afd) {
                         MPTimber.tag(TAG).d("play resId:" + resId);
                        mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    } else if (null != finalResUrl) {
                         MPTimber.tag(TAG).d("play resUrl:" + finalResUrl);
                        mMediaPlayer.setDataSource(mContext, finalResUrl);
                    } else if (!TextUtils.isEmpty(mResPath)) {
                         MPTimber.tag(TAG).d("play resPath:" + mResPath);
                        mMediaPlayer.setDataSource(mResPath);
                    } else {
                         MPTimber.tag(TAG).d("invalid Source");
                        return;
                    }
                } catch (Exception e) {
                    MPTimber.tag(TAG).e(Log.getStackTraceString(e));
                    
                }
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        stop();
                    }
                });
                try {
                    mMediaPlayer.prepare();
                } catch (IOException e) {
                    MPTimber.tag(TAG).e(Log.getStackTraceString(e));
                }
                mMediaPlayer.start();

                //临时新增，设置重复播放
                mMediaPlayer.setLooping(true);
            }
        });
        if (duration > 0) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stop();
                }
            }, duration);
        }
    }

    private boolean isUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private void preHandler() {
        if (null != mHandler) {
            return;
        }
        HandlerThread thread = new HandlerThread("Handler-MediaPlayer");
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    public int getResId() {
        return mResId;
    }

    public void stop() {
        if (null == mHandler) {
             MPTimber.tag(TAG).d("mediaPlayer not start");
            return;
        }
        if ((-1 == getResId()) && TextUtils.isEmpty(mResPath) && (null == mResUrl)) {
             MPTimber.tag(TAG).d("cannot stop empty resource");
            return;
        }
        mHandler.post(() -> {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mResId = -1;
            mResPath = "";
            mResUrl = null;
        });
    }
}
