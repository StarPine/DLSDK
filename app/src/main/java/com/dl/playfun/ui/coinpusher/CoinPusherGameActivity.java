package com.dl.playfun.ui.coinpusher;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.ActivityCoinpusherGameBinding;
import com.dl.playfun.manager.LocaleManager;
import com.dl.playfun.ui.base.BaseActivity;
import com.dl.playfun.ui.coinpusher.dialog.CoinPusherConvertDialog;
import com.dl.playfun.ui.coinpusher.dialog.CoinPusherDialogAdapter;
import com.dl.playfun.ui.coinpusher.dialog.CoinPusherGameHistoryDialog;
import com.dl.playfun.utils.AutoSizeUtils;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.utils.TimeUtils;
import com.dl.playfun.utils.ToastCenterUtils;
import com.tencent.liteav.trtccalling.ui.floatwindow.FloatWindowService;
import com.wangsu.libwswebrtc.WsWebRTCObserver;
import com.wangsu.libwswebrtc.WsWebRTCParameters;
import com.wangsu.libwswebrtc.WsWebRTCPortalReport;
import com.wangsu.libwswebrtc.WsWebRTCView;

import java.nio.ByteBuffer;

/**
 * Author: 彭石林
 * Time: 2022/8/26 11:07
 * Description: 推币机游戏页面
 */
public class CoinPusherGameActivity extends BaseActivity<ActivityCoinpusherGameBinding,CoinPusherGameViewModel> {

    //倒计时 30秒
    public static CountDownTimer downTimer = null;

    private final String TAG = "CoinPusherGameActivity";

    private Dialog dialogCoinPusherHelp = null;

    //倒计时30秒
    private final long downTimeMillisInFuture = 20 * 1000;
    //倒计时剩余多少时间提示
    private final long downTimeMillisHint = 10 * 1000;
    //提示状态标识
    private boolean downTimeMillisHintFlag = false;

    private Integer roomId;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.setLocal(newBase));
    }

    /**
     * 就算你在Manifest.xml设置横竖屏切换不重走生命周期。横竖屏切换还是会走这里

     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
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
    protected void onResume() {
        super.onResume();
        FloatWindowService.stopService(this);
        AppContext.isCalling = true;
        ImmersionBarUtils.setupStatusBar(this, false, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ImmersionBarUtils.setupStatusBar(this, true, true);
    }



    @Override
    public int initContentView(Bundle savedInstanceState) {
        AutoSizeUtils.applyAdapt(getResources());
        return R.layout.activity_coinpusher_game;
    }

    @Override
    public int initVariableId() {
        return BR.gameViewModel;
    }

    @Override
    public void initParam() {
        super.initParam();
       Bundle bundle =  getIntent().getExtras();
       if(bundle!=null){
           roomId = bundle.getInt("roomId",-1);
       }
    }

    @Override
    public CoinPusherGameViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(getApplication());
        return ViewModelProviders.of(this, factory).get(CoinPusherGameViewModel.class);
    }

    private void gameInit() {
        WsWebRTCView webrtcView = binding.WebRtcSurfaceView;
        WsWebRTCParameters webrtcParam = new WsWebRTCParameters();
        //设置客户 id,由网宿分配给客户的 id 字符串
        webrtcParam.setCustomerID("sessionid_test");
        //设置播放流
        webrtcParam.setStreamUrl("http://webrtc.pull.azskj.cn/live/tbtest-39.sdp");//http://webrtc.pull.azskj.cn/live/tbtest-39.sdp
        //设置是否使用 dtls 加密，默认加密，false：加密；true：不加密
        webrtcParam.disableDTLS(false);
        //设置视频是否使用硬解，默认硬解，false：软解；true：硬解
        webrtcParam.enableHw(true);
        //设置音频使用格式,默认 OPUS,音频格式类型见 WsWebRTCParameters 类定义
        webrtcParam.setAudioFormat(WsWebRTCParameters.OPUS);
        //设置连接超时时间，默认 5s，单位 ms
        webrtcParam.setConnTimeOutInMs(10000);
        //设置音频 JitterBuffer 队列最大报文数，影响时延，默认 50
        webrtcParam.setAudioJitterBufferMaxPackets(50);
        //设置是否开启追帧，默认开启，false：不开启；true：开启
        webrtcParam.enableAudioJitterBufferFastAccelerate(true);
        //设置统计值回调频率，默认 1s，单位 ms
        webrtcParam.setPortalReportPeriodInMs(1000);
        //设置 rtc 日志等级，默认 LOG_NONE，日志等级类型见 WsWebRTCParameters
        webrtcParam.setLoggingLevel(WsWebRTCParameters.LOG_INFO);
        //设置 rtc 日志回调函数，loggable：WsWebRTCParameters.Loggable 对象，
        webrtcParam.setLoggable((s, i, s1) -> {

        });
        WsWebRTCObserver observer = new WsWebRTCObserver() {
            @Override
            public void onWsWebrtcError(String s, ErrCode errCode) {
                Log.e(TAG,"onWsWebrtcError："+s+"============"+errCode.toString());
            }

            @Override
            public void onFirstPacketReceived(int i) {
                Log.e(TAG,"onFirstPacketReceived："+i);
            }

            @Override
            public void onFirstFrameRendered() {
                Log.e(TAG,"onFirstFrameRendered");
            }

            @Override
            public void onResolutionRatioChanged(int i, int i1) {
                Log.e(TAG,"onResolutionRatioChanged："+i+"========"+i1);
            }

            @Override
            public void onPortalReport(WsWebRTCPortalReport wsWebRTCPortalReport) {
                Log.e(TAG,"onPortalReport："+wsWebRTCPortalReport.toString());
            }

            @Override
            public void onNotifyCaton(int i) {
                Log.e(TAG,"onNotifyCaton："+i);
            }

            @Override
            public void onEventSEIReceived(ByteBuffer byteBuffer) {
                Log.e(TAG,"onEventSEIReceived");
            }

            @Override
            public void onEventConnected() {
                Log.e(TAG,"onEventConnected");
            }
        };
        webrtcView.initilize(webrtcParam, observer);
        webrtcView.start();
    }

    @Override
    public void initData() {
        super.initData();
        viewModel.roomId = 5;
        binding.imgHelp.setOnClickListener(v->{
            if(dialogCoinPusherHelp==null){
                dialogCoinPusherHelp = CoinPusherDialogAdapter.getDialogCoinPusherHelp(this,null,null);
            }
            dialogCoinPusherHelp.show();
        });
        binding.imgConvert.setOnClickListener(v ->{
            CoinPusherConvertDialog coinPusherConvertDialog = new CoinPusherConvertDialog(this);
            coinPusherConvertDialog.setItemConvertListener(money -> viewModel.totalMoney.set(viewModel.totalMoney.get() + money));
            coinPusherConvertDialog.show();
        });
        binding.imgHistroy.setOnClickListener(v ->{
            CoinPusherGameHistoryDialog coinPusherGameHistoryDialog = new CoinPusherGameHistoryDialog(this,viewModel.roomId);
            coinPusherGameHistoryDialog.show();
        });
        binding.btnPlaying.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                binding.svgaPlayer.setVisibility(View.VISIBLE);
                binding.svgaPlayer.startAnimation();
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                binding.svgaPlayer.stopAnimation();
                binding.svgaPlayer.setVisibility(View.GONE);
            }
            return false;
        });
        gameInit();
        //开始倒计时
        downTime();
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.gameUI.resetDownTimeEvent.observe(this, unused -> {
            //取消倒计时
            cancelDownTimer();
            //重新开始倒计时
            downTime();
        });
    }

    @Override
    public  void onDestroy() {
        viewModel.playingCoinPusherClose(viewModel.roomId);
        try {
            //暂停播放。释放资源
            binding.WebRtcSurfaceView.stop();
            binding.WebRtcSurfaceView.uninitilize();
        }catch (Exception ignored) {

        }
        super.onDestroy();
    }

    //倒计时开始
    private void downTime() {
        downTimer = new CountDownTimer(downTimeMillisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(!downTimeMillisHintFlag){
                    if(millisUntilFinished <= downTimeMillisHint){
                        downTimeMillisHintFlag = true;
                        ToastCenterUtils.showShort(String.format(StringUtils.getString(R.string.playfun_coinpusher_text_downtime),millisUntilFinished/1000));
                    }
                }

            }

            @Override
            public void onFinish() {
                AppConfig.CoinPusherGameNotPushed = true;
                finish();
            }
        };
        downTimer.start();
    }
    //取消倒计时
    private void cancelDownTimer() {
        downTimeMillisHintFlag = false;
        if(downTimer!=null){
            downTimer.cancel();
            downTimer = null;
        }
    }
}
