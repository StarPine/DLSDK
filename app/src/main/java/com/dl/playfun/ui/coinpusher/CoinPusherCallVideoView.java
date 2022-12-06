package com.dl.playfun.ui.coinpusher;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.dl.playfun.R;
import com.dl.rtc.calling.base.DLRTCCalling;
import com.dl.rtc.calling.ui.BaseDLRTCCallView;
import com.dl.rtc.calling.ui.videolayout.DLRTCVideoLayoutManager;
import com.dl.rtc.calling.ui.videolayout.VideoLayoutFactory;
import com.tencent.trtc.TRTCCloudDef;

import java.util.ArrayList;

/**
 * Author: 彭石林
 * Time: 2022/11/10 14:51
 * Description: This is CoinPusherCallVideoView
 */
public class CoinPusherCallVideoView extends BaseDLRTCCallView {
    //视频渲染view
    private DLRTCVideoLayoutManager mLayoutManagerRtc;

    public CoinPusherCallVideoView(@NonNull Context context, @NonNull DLRTCCalling.Role role, @NonNull DLRTCCalling.Type type, @NonNull String[] userIDs, @NonNull String sponsorID, VideoLayoutFactory videoLayoutFactory) {
        super(context, role, type, userIDs, sponsorID, null, false);
        mLayoutManagerRtc.initVideoFactory(videoLayoutFactory);
    }

    @Override
    public void onNetworkQuality(@Nullable TRTCCloudDef.TRTCQuality localQuality, @Nullable ArrayList<TRTCCloudDef.TRTCQuality> remoteQuality) {

    }

    @Override
    protected void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.jm_trtccalling_videocall_activity_call_main, this);
        mLayoutManagerRtc = findViewById(R.id.trtc_layout_manager);
    }

    @Override
    public void onError(int code, String msg) {
        //发生了错误，报错并退出该页面
        ToastUtils.showLong(getContext().getString(R.string.trtccalling_toast_call_error_msg, code, msg));
        //stopCameraAndFinish();
    }
}
