package com.dl.playfun.ui.coinpusher;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dl.lib.util.log.MPTimber;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.ActivityCoinpusherGameBinding;
import com.dl.playfun.entity.CallGameCoinPusherEntity;
import com.dl.playfun.entity.CallUserInfoEntity;
import com.dl.playfun.entity.CoinPusherBalanceDataEntity;
import com.dl.playfun.entity.GiftBagEntity;
import com.dl.playfun.entity.GoodsEntity;
import com.dl.playfun.kl.CallChatingConstant;
import com.dl.playfun.kl.view.AudioCallChatingActivity;
import com.dl.playfun.kl.view.CallingVideoActivity;
import com.dl.playfun.kl.view.DialingAudioActivity;
import com.dl.playfun.kl.viewmodel.UITRTCCallingDelegate;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.manager.V2TIMCustomManagerUtil;
import com.dl.playfun.ui.base.BaseActivity;
import com.dl.playfun.ui.coinpusher.dialog.CoinPusherConvertDialog;
import com.dl.playfun.ui.coinpusher.dialog.CoinPusherDialogAdapter;
import com.dl.playfun.ui.coinpusher.dialog.CoinPusherGameHistoryDialog;
import com.dl.playfun.ui.coinpusher.dialog.CoinPusherHelpDialog;
import com.dl.playfun.ui.dialog.GiftBagDialog;
import com.dl.playfun.utils.AutoSizeUtils;
import com.dl.playfun.utils.CoinPusherApiUtil;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.utils.Utils;
import com.dl.playfun.widget.coinrechargesheet.CoinRechargeSheetView;
import com.dl.playfun.widget.custom.BaseSVGACallback;
import com.dl.playfun.widget.image.CircleImageView;
import com.dl.rtc.calling.DLRTCFloatWindowService;
import com.dl.rtc.calling.base.DLRTCCalling;
import com.dl.rtc.calling.base.impl.DLRTCInternalListenerManager;
import com.dl.rtc.calling.manager.DLRTCInterceptorCall;
import com.dl.rtc.calling.manager.DLRTCStartShowUIManager;
import com.dl.rtc.calling.manager.DLRTCVideoManager;
import com.dl.rtc.calling.model.DLRTCCallingConstants;
import com.dl.rtc.calling.model.DLRTCDataMessageType;
import com.dl.rtc.calling.model.DLRTCSignalingManager;
import com.dl.rtc.calling.ui.videolayout.DLRTCVideoLayout;
import com.dl.rtc.calling.ui.videolayout.DLRTCVideoLayoutManager;
import com.dl.rtc.calling.ui.videolayout.VideoLayoutFactory;
import com.faceunity.nama.data.FaceUnityDataFactory;
import com.faceunity.nama.ui.FaceUnityView;
import com.jakewharton.rxbinding2.view.RxView;
import com.luck.picture.lib.permissions.PermissionChecker;
import com.misterp.toast.SnackUtils;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.tencent.custom.GiftEntity;
import com.tencent.custom.tmp.CustomDlTempMessage;
import com.tencent.qcloud.tuicore.TUILogin;
import com.tencent.qcloud.tuicore.custom.CustomConstants;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloudDef;
import com.wangsu.libwswebrtc.WsWebRTCObserver;
import com.wangsu.libwswebrtc.WsWebRTCParameters;
import com.wangsu.libwswebrtc.WsWebRTCPortalReport;
import com.wangsu.libwswebrtc.WsWebRTCView;

import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import me.goldze.mvvmhabit.base.AppManager;
import me.goldze.mvvmhabit.utils.StringUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Author: 彭石林
 * Time: 2022/8/26 11:07
 * Description: 推币机游戏页面
 */
public class CoinPusherGameActivity extends BaseActivity<ActivityCoinpusherGameBinding,CoinPusherGameViewModel> {

    //倒计时 30秒
    public static CountDownTimer downTimer = null;

    private static final String TAG = "CoinPusherGameActivity";
    //玩法说明
    private CoinPusherHelpDialog dialogCoinPusherHelp = null;
    //兑换列表
    private CoinPusherConvertDialog coinPusherConvertDialog = null;
    //历史记录
    private CoinPusherGameHistoryDialog coinPusherGameHistoryDialog = null;

    //倒计时30秒
    private  long downTimeMillisInFuture = 30;
    //倒计时剩余多少时间提示
    private  long downTimeMillisHint = 10;
    //提示状态标识
    private boolean downTimeMillisHintFlag = false;


    //充值弹窗
    private CoinRechargeSheetView coinRechargeSheetView;

    //接听语音视频拦截
    private DlRtcInterceptorCall dlRtcInterceptorCall;
    //用于做通话时长统计
    private Handler mHandler = new Handler();
    private Runnable timerRunnable = null;
    private FaceUnityView faceUnityView;

    private GameCallEntity _gameCallEntity;

    //围观状态-默认是false
    private boolean _circusesStatus = false;

    private String _customerId = "";
    private String _streamUrl = "";
    private Integer _totalGold = 0;
    private Integer _payGameMoney = 0;
    //游戏房间ID
    public Integer _gameRoomId = 0;
    public int _levelId;
    public String _nickname;
    //对端的用户信息-其他页面跳转过来。
    public CallUserInfoEntity _callUserInfoEntity = null;

    private SVGAImageView giftEffects;
    @Override
    protected void onResume() {
        super.onResume();
        DLRTCFloatWindowService.stopService(this);
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

    public static Intent getStartActivityIntent(Context mContext,String customerId,String streamUrl,
                                                Integer totalGold,Integer payGameMoney,long outTime,long countdown,
                                                Integer roomId,int levelId,String nickname) {
        Intent  intent = new Intent(mContext,CoinPusherGameActivity.class);
        intent.putExtra("customerId",customerId);
        intent.putExtra("streamUrl",streamUrl);
        intent.putExtra("totalGold",totalGold);
        intent.putExtra("payGameMoney",payGameMoney);
        intent.putExtra("outTime",outTime);
        intent.putExtra("countdown",countdown);
        intent.putExtra("roomId",roomId);
        intent.putExtra("levelId",levelId);
        intent.putExtra("nickname",nickname);
        return intent;
    }

    @Override
    public void initParam() {
        super.initParam();
       Intent intent =  getIntent();
       if(intent!=null){
           Log.e(TAG,"接收页面传值："+intent.toString());
           //倒计时多少时间结束游戏
           downTimeMillisInFuture = intent.getLongExtra("outTime",30);
           ////倒计时剩余多少时间提示
           downTimeMillisHint = intent.getLongExtra("countdown",10);
           _gameCallEntity = (GameCallEntity)intent.getSerializableExtra("GameCallEntity");
           _circusesStatus = intent.getBooleanExtra("_circusesStatus",false);
           _customerId = intent.getStringExtra("customerId");
           _streamUrl = intent.getStringExtra("streamUrl");
           _totalGold = intent.getIntExtra("totalGold",0);
           _payGameMoney = intent.getIntExtra("payGameMoney",0);
           _gameRoomId = intent.getIntExtra("roomId",0);
           _levelId = intent.getIntExtra("levelId",0);
           _nickname = intent.getStringExtra("nickname");
           _callUserInfoEntity = (CallUserInfoEntity)intent.getSerializableExtra("CallUserInfoEntity");
       }
        initListener();
    }

    @Override
    public CoinPusherGameViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(getApplication());
        return ViewModelProviders.of(this, factory).get(CoinPusherGameViewModel.class);
    }

    private void gameInit() {
        LoadingVideoShow(true);
        WsWebRTCView webrtcView = binding.WebRtcSurfaceView;
        WsWebRTCParameters webrtcParam = new WsWebRTCParameters();
        //设置客户 id,由网宿分配给客户的 id 字符串
        webrtcParam.setCustomerID(_customerId);
        //设置播放流
        webrtcParam.setStreamUrl(_streamUrl);//http://webrtc.pull.azskj.cn/live/tbtest-39.sdp
        //设置是否使用 dtls 加密，默认加密，false：加密；true：不加密
        webrtcParam.disableDTLS(false);
        //设置视频是否使用硬解，默认硬解，false：软解；true：硬解
        webrtcParam.enableHw(false);
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
        webrtcParam.setLoggable((s, i, s1) -> {});
        WsWebRTCObserver observer = new WsWebRTCObserver() {
            @Override
            public void onWsWebrtcError(String s, ErrCode errCode) {
                if(errCode == ErrCode.ERR_CODE_WEBRTC_DISCONN){
                    //链接断开
                    SnackUtils.showCenterShort(getContentShowView(),StringUtils.getString(R.string.playfun_network_text));
                }
                Log.e(TAG,"onWsWebrtcError："+s+"============"+errCode.toString());
            }

            @Override
            public void onFirstPacketReceived(int i) {
                Log.e(TAG,"onFirstPacketReceived："+i);
            }

            @Override
            public void onFirstFrameRendered() {
            }

            @Override
            public void onResolutionRatioChanged(int i, int i1) {
            }

            @Override
            public void onPortalReport(WsWebRTCPortalReport wsWebRTCPortalReport) {
                //Log.e(TAG,"onPortalReport："+wsWebRTCPortalReport.toString());
            }

            @Override
            public void onNotifyCaton(int i) {
                Log.e(TAG,"onNotifyCaton："+i);
            }

            @Override
            public void onEventSEIReceived(ByteBuffer byteBuffer) {
            }

            @Override
            public void onEventConnected() {
                LoadingVideoShow(false);
            }
        };
        webrtcView.initilize(webrtcParam, observer);
        webrtcView.start();
    }

    private void LoadingVideoShow(boolean isShow){
        binding.flLayoutLoadingVideo.post(()-> binding.flLayoutLoadingVideo.setVisibility(isShow ? View.VISIBLE : View.GONE));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void initData() {
        super.initData();
        if(_callUserInfoEntity!=null){
            viewModel.otherCallInfoEntity.set(_callUserInfoEntity);
        }
        viewModel._gameRoomId = _gameRoomId;
        viewModel.circuseeStatus.set(_circusesStatus);
        viewModel.totalMoney.set(_totalGold);
        binding.tvMoneyHint.setText(String.format(StringUtils.getString(R.string.playfun_coinpusher_game_text_2),_payGameMoney));
        doubleClick(binding.imgHelp);
        doubleClick(binding.rlCoin);
        doubleClick(binding.imgHistroy);
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
        //围观者
        if(!_circusesStatus){
            //开始倒计时
            downTime();
        }
        if(_gameCallEntity!=null){
            viewModel.gameCallEntity = _gameCallEntity;
            viewModel.callingDropped.set(false);
            gameCalling();
        }
        giftEffects = binding.giftEffects;
    }


    @SuppressLint("CheckResult")
    public void doubleClick(View view){
        RxView.clicks(view)
                .throttleFirst(1, TimeUnit.SECONDS)//1秒钟内只允许点击1次
                .subscribe(o -> {
                    if (view.getId() == binding.imgHelp.getId()) {
                        String helpWebUrl = ConfigManager.getInstance().getAppRepository().readApiConfigManagerEntity().getPlayChatWebUrl()+"/coinPusherGame/helpExplain/";
                        dialogCoinPusherHelp = new CoinPusherHelpDialog(CoinPusherGameActivity.this,helpWebUrl );
                        dialogCoinPusherHelp.setCanceledOnTouchOutside(false);
                        dialogCoinPusherHelp.setOnDismissListener(dialog -> {
                            if(dialogCoinPusherHelp != null){
                                dialogCoinPusherHelp.dismissHud();
                            }
                        });
                        if(!otherDigLogIsShowing()){
                            dialogCoinPusherHelp.show();
                        }

                    }else if(view.getId() == binding.imgHistroy.getId()){
                        if(coinPusherGameHistoryDialog == null){
                            coinPusherGameHistoryDialog = new CoinPusherGameHistoryDialog(CoinPusherGameActivity.this,viewModel._gameRoomId,_levelId,_nickname);
                            coinPusherGameHistoryDialog.setOnDismissListener(dialog -> {
                                if(coinPusherGameHistoryDialog!=null){
                                    coinPusherGameHistoryDialog.dismissHud();
                                }
                            });
                            coinPusherGameHistoryDialog.setCanceledOnTouchOutside(false);
                        }else{
                            coinPusherGameHistoryDialog.loadData(viewModel._gameRoomId);
                        }
                        if(!otherDigLogIsShowing()){
                            coinPusherGameHistoryDialog.show();
                        }
                    }else if(view.getId() == binding.rlCoin.getId()){
                        //弹出兑换
                        coinPusherConvertDialogShow();
                    }
                });
    }

    /**
     * @return
     */
    //是否有其它dialog正在显示
    private boolean otherDigLogIsShowing() {
        if(coinPusherGameHistoryDialog!=null && coinPusherGameHistoryDialog.isShowing()){
            return true;
        }
        if(dialogCoinPusherHelp!=null && dialogCoinPusherHelp.isShowing()){
            return true;
        }
        if(coinPusherConvertDialog!=null && coinPusherConvertDialog.isShowing()){
            return true;
        }
        return false;
    }


    /**
     * 获取当前窗体显示的view
     */
    public View getContentShowView() {
        if(coinPusherGameHistoryDialog!=null && coinPusherGameHistoryDialog.isShowing()){
            return coinPusherGameHistoryDialog.getWindow().getDecorView();
        }
        if(dialogCoinPusherHelp!=null && dialogCoinPusherHelp.isShowing()){
            return dialogCoinPusherHelp.getWindow().getDecorView();
        }
        if(coinPusherConvertDialog!=null && coinPusherConvertDialog.isShowing()){
            return coinPusherConvertDialog.getWindow().getDecorView();
        }
        return binding.getRoot();
    }

    private void coinPusherConvertDialogShow(){
        //购买兑换金币
        if(coinPusherConvertDialog == null){
            coinPusherConvertDialog = new CoinPusherConvertDialog(this);
            coinPusherConvertDialog.setCanceledOnTouchOutside(false);
            coinPusherConvertDialog.setItemConvertListener(new CoinPusherConvertDialog.ItemConvertListener() {
                @Override
                public void convertSuccess(CoinPusherBalanceDataEntity coinPusherBalanceDataEntity) {
                    viewModel.totalMoney.set(coinPusherBalanceDataEntity.getTotalGold());
                    //取消倒计时
                    cancelDownTimer();
                    //重新开始倒计时
                    downTime();
                }

                @Override
                public void buyError() {
                    //关闭当前弹窗
                    coinPusherConvertDialog.dismiss();
                    //购买充值
                    payCoinRechargeDialog();
                }
            });
        }else{
            coinPusherConvertDialog.loadData();
        }
        if(!otherDigLogIsShowing()){
            coinPusherConvertDialog.show();
        }
    }

    @SuppressLint("ObjectAnimatorBinding")
    @Override
    public void initViewObservable() {
        super.initViewObservable();
        //返回上一页
        viewModel.gameUI.backViewApply.observe(this, unused -> {
            interceptBackPressed();
        });
        //取消倒计时
        viewModel.gameUI.resetDownTimeEvent.observe(this, unused -> {
            //取消倒计时
            cancelDownTimer();
            //重新开始倒计时
            downTime();
        });
        //开始显示loading
        viewModel.gameUI.loadingShow.observe(this,unused->{
            binding.flLayoutLoading.post(()->{
                if(binding.flLayoutLoading.getVisibility() != View.VISIBLE){
                    binding.flLayoutLoading.setVisibility(View.VISIBLE);
                }

            });
        });
        //取消显示loading
        viewModel.gameUI.loadingHide.observe(this,unused->{
            binding.flLayoutLoading.post(()->{
                if(binding.flLayoutLoading.getVisibility() != View.GONE){
                    binding.flLayoutLoading.setVisibility(View.GONE);
                }

            });
        });
        //toast弹窗居中
        viewModel.gameUI.toastCenter.observe(this,coinPusherGamePlayingEvent->{
            //中奖落币
            String textContent = com.blankj.utilcode.util.StringUtils.getString(R.string.playfun_coinpusher_coin_text_reward);
            String valueText = String.format(textContent, coinPusherGamePlayingEvent.getGoldNumber());
            viewModel.totalMoney.set(coinPusherGamePlayingEvent.getTotalGold());
            SnackUtils.showCenterShort(getContentShowView(),valueText);
        });
        //是否禁用投币按钮
        viewModel.gameUI.playingBtnEnable.observe(this, flag ->{
            binding.btnPlaying.setEnabled(flag);
            if(flag){
                binding.btnPlaying.setTextColor(ColorUtils.getColor(R.color.black));
            }else{
                binding.btnPlaying.setTextColor(ColorUtils.getColor(R.color.yellow_548));
            }
        });
        //取消倒计时
        viewModel.gameUI.cancelDownTimeEvent.observe(this, unused -> {
            //取消倒计时
            cancelDownTimer();
        });
        //拉起充值弹窗
        viewModel.gameUI.payDialogViewEvent.observe(this, unused -> coinPusherConvertDialogShow());
        //开关推币机音频开关
        viewModel.gameUI.muteEnabledEvent.observe(this, aBoolean -> {
            try {
                binding.WebRtcSurfaceView.mute(aBoolean);
            }catch (Exception ignored) {

            }
        });
        //用户拨打状态折叠效果
        viewModel.gameUI.triangleEvent.observe(this,aBoolean -> {
            //折叠
            if(aBoolean){
                binding.rlCallingUserLayout.findViewById(R.id.fl_triangle_off).setVisibility(View.GONE);
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_coinpush_fold_off);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        binding.rlCallingUserLayout.findViewById(R.id.fl_triangle_on).setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                binding.rlCallingUserLayout.findViewById(R.id.fl_triangle_off).startAnimation(animation);
            }else{
                //展开
                binding.rlCallingUserLayout.findViewById(R.id.fl_triangle_on).setVisibility(View.GONE);
                binding.rlCallingUserLayout.findViewById(R.id.fl_triangle_off).setVisibility(View.VISIBLE);
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_coinpush_fold_on);
                binding.rlCallingUserLayout.findViewById(R.id.fl_triangle_off).startAnimation(animation);
            }
        });
        //点击放大缩小视讯弹窗
        viewModel.gameUI.callZoomOuViewEvent.observe(this, aBoolean -> {
            //放大
            ResizeWidthAnimation anim;
            if(aBoolean){
                anim = new ResizeWidthAnimation(binding.rlVideoCallLayout, Utils.dip2px(getContext(), 230), Utils.dip2px(getContext(), 307));
            }else{
                //缩小
                anim = new ResizeWidthAnimation(binding.rlVideoCallLayout, Utils.dip2px(getContext(), 115), Utils.dip2px(getContext(), 154));
            }
            anim.setDuration(50);
            anim.setFillAfter(true);
            binding.rlVideoCallLayout.startAnimation(anim);
        });
        //效验权限
        viewModel.gameUI.callCheckPermissionEvent.observe(this,unused -> {
            GameCallEntity _gameCallEntity = viewModel.gameCallEntity;
            Log.e("开始效验权限","==============="+(_gameCallEntity==null));
            if(_gameCallEntity != null){
                if(_gameCallEntity.getCallingType()!=null && _gameCallEntity.getCallingType() == DLRTCDataMessageType.DLInviteRTCType.dl_rtc_audio){
                    toPermissionIntent.launch(Manifest.permission.RECORD_AUDIO);
                }else{
                    launcherPermissionArray.launch(new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA});
                }
            }
        });
        //进房成功开始计时
        viewModel.gameUI.acceptCallingEvent.observe(this, unused -> {
            TimeCallMessage();
        });
        //挂断电话处理UI
        viewModel.gameUI.hangupCallingEvent.observe(this, unused -> {
            initRlCallingUserLayout();
        });
        //发送礼物弹窗
        viewModel.gameUI.sendGiftBagEvent.observe(this,unused -> {
            GiftBagDialog giftBagDialog = new GiftBagDialog(getContext(), false,  0);
            giftBagDialog.setGiftOnClickListener(new GiftBagDialog.GiftOnClickListener() {
                @Override
                public void sendGiftClick(Dialog dialog, int number, GiftBagEntity.GiftEntity giftEntity) {
                    dialog.dismiss();
                    viewModel.sendUserGift(dialog, giftEntity, viewModel.otherCallInfoEntity.get().getId(), number);
                }

                @Override
                public void rechargeStored(Dialog dialog) {
                    dialog.dismiss();
                    payCoinRechargeDialog();
                }
            });
            giftBagDialog.show();
        });
        //发送礼物效果展示
        viewModel.gameUI.sendUserGiftAnim.observe(this, stringObjectMap -> {
            int account = (int) stringObjectMap.get("account");
            GiftBagEntity.GiftEntity giftEntity = (GiftBagEntity.GiftEntity) stringObjectMap.get("giftEntity");
            if(giftEntity != null){
                //启动播放礼物飘屏
                startAcceptHeadAnimotion(true,giftEntity.getImg(),giftEntity.getLink(),account);
            }

//            //启动横幅动画
//            startSendBannersAnimotion(giftEntity, account);
//            //启动头像动画
//            startSendHeadAnimotion(giftEntity);
        });
        //接收礼物效果展示
        viewModel.gameUI.acceptUserGift.observe(this, giftEntity -> {
            int account = giftEntity.getAmount();
            //启动播放礼物飘屏
            startAcceptHeadAnimotion(false,giftEntity.getImgPath(),giftEntity.getSvgaPath(),account);
        });
    }

    @Override
    public  void onDestroy() {
        AutoSizeUtils.closeAdapt(getResources());
        recycleHandles();
        if(downTimer!=null){
            downTimer.cancel();
            downTimer = null;
        }
        destroyListener();
        CoinPusherApiUtil.endGamePaying(viewModel._gameRoomId);
        try {
            if(coinRechargeSheetView!=null){
                if(coinRechargeSheetView.isShowing()){
                    coinRechargeSheetView.dismiss();
                }
                coinRechargeSheetView = null;
            }
            if(dialogCoinPusherHelp != null){
                dialogCoinPusherHelp.destroy();
                if(dialogCoinPusherHelp.isShowing()){
                    dialogCoinPusherHelp.dismiss();
                }
                dialogCoinPusherHelp = null;
            }
            if(coinPusherConvertDialog != null){
                if(coinPusherConvertDialog.isShowing()){
                    coinPusherConvertDialog.dismiss();
                }
                coinPusherConvertDialog.dismiss();
                coinPusherConvertDialog = null;
            }
            if(coinPusherGameHistoryDialog != null){
                if(coinPusherGameHistoryDialog.isShowing()){
                    coinPusherGameHistoryDialog.dismiss();
                }
                coinPusherGameHistoryDialog = null;
            }
            //暂停播放。释放资源
            binding.WebRtcSurfaceView.stop();
            binding.WebRtcSurfaceView.uninitilize();
        }catch (Exception ignored) {

        }
        super.onDestroy();
    }

    /**
    * @Desc TODO(拦截返回按键)
    * @author 彭石林
    * @parame []
    * @Date 2022/9/6
    */
    @Override
    public void onBackPressed() {
        interceptBackPressed();
    }

    private void interceptBackPressed(){
        if(viewModel!=null){
            if(viewModel.gamePlayingState==null){ //状态为空
                if(viewModel.gameCallEntity != null){
                    Log.e("进入返回语音页面","=======================");
                    callingInterceptApply();
                }
                super.onBackPressed();
            }else{
                Integer stringResId = null;
                if(viewModel.gamePlayingState.equals(viewModel.loadingPlayer)){//投币状态
                    stringResId = R.string.playfun_coinpusher_hint_retain;
                }else if(viewModel.gamePlayingState.equals(CustomConstants.CoinPusher.START_WINNING)){ //落币状态
                    stringResId = R.string.playfun_coinpusher_hint_retain2;
                }else if(viewModel.gamePlayingState.equals(CustomConstants.CoinPusher.LITTLE_GAME_WINNING)){
                    //小游戏提示
                    stringResId = R.string.playfun_coinpusher_hint_retain3;
                }
                if(stringResId != null){
                    CoinPusherDialogAdapter.getDialogCoinPusherRetainHint(this, stringResId, new CoinPusherDialogAdapter.CoinPusherDialogListener() {
                        @Override
                        public void onConfirm(Dialog dialog) {
                            dialog.dismiss();
                            //结束当前页面
                            finish();
                        }
                    }).show();
                }
            }
        }else{
            super.onBackPressed();
        }
    }
    /**
    * @Desc TODO(通话中返回页面处理)
    * @author 彭石林
    * @parame []
    * @Date 2022/11/10
    */
    private void callingInterceptApply() {
        CallGameCoinPusherEntity callGameCoinPusherEntity = new CallGameCoinPusherEntity();
        callGameCoinPusherEntity.setState(CallGameCoinPusherEntity.leaveGame);
        callGameCoinPusherEntity.setCircuses(_circusesStatus);
        CustomDlTempMessage customDlTempMessage = V2TIMCustomManagerUtil.buildDlTempMessage(CustomConstants.CoinPusher.MODULE_NAME,CustomConstants.CoinPusher.CALL_GO_GAME_WINNING,callGameCoinPusherEntity);
        DLRTCSignalingManager.INSTANCE.sendC2CCustomMessage(GsonUtils.toJson(customDlTempMessage),viewModel.gameCallEntity.getCallingRole() ==  DLRTCCalling.Role.CALL? viewModel.gameCallEntity.getAcceptUserId() : viewModel.gameCallEntity.getInviteUserId());
        Intent intent = null;
        if (viewModel.gameCallEntity.getCallingType() == DLRTCDataMessageType.DLInviteRTCType.dl_rtc_audio) {
            Activity audioCallChatingAct = AppManager.getAppManager().getActivity(AudioCallChatingActivity.class);
            //通话中页面不存在
            if(audioCallChatingAct == null){
                intent = new Intent(getContext(), AudioCallChatingActivity.class);
                CallChatingConstant.updateCoinPusherWatchRoom(_gameRoomId, viewModel.otherCallInfoEntity.get().getId(),2);
            }
            //返回拨打中页面
            if(viewModel.callingOnTheLine.get()){
                intent = new Intent(getContext(), DialingAudioActivity.class);
            }
            if(intent!=null){
                intent.putExtra("isRestart", true);
            }

        } else {
            intent = new Intent(getContext(), CallingVideoActivity.class);
            //返回拨打中页面
            intent.putExtra("isRestart", viewModel.callingOnTheLine.get());
            intent.putExtra(DLRTCCallingConstants.PARAM_NAME_ROLE,DLRTCCalling.Role.CALL);

        }
        if(intent != null){
            intent.putExtra(DLRTCCallingConstants.DLRTCInviteUserID, viewModel.gameCallEntity.getInviteUserId());
            intent.putExtra(DLRTCCallingConstants.DLRTCAcceptUserID, viewModel.gameCallEntity.getAcceptUserId());
            intent.putExtra(DLRTCCallingConstants.PARAM_NAME_ROLE, viewModel.gameCallEntity.getCallingRole());
            intent.putExtra(DLRTCCallingConstants.RTCInviteRoomID,viewModel.gameCallEntity.getRoomId());
            intent.putExtra("CallingInviteInfoField",viewModel.otherCallInfoEntity.get());
            intent.putExtra("timeCount", viewModel.mTimeCount);
            intent.putExtra("isRestart", true);
            getContext().startActivity(intent);
        }
    }

    //倒计时开始
    private void downTime() {
        downTimer = new CountDownTimer(downTimeMillisInFuture * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(!downTimeMillisHintFlag){
                    if(millisUntilFinished / 1000 <= downTimeMillisHint){
                        downTimeMillisHintFlag = true;
                        SnackUtils.showCenterShort(getContentShowView(),String.format(StringUtils.getString(R.string.playfun_coinpusher_text_downtime),millisUntilFinished/1000));
                    }
                }
            }

            @Override
            public void onFinish() {
                if(downTimeMillisHintFlag){
                    AppConfig.CoinPusherGameNotPushed = true;
                }
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
    //充值弹窗
    private void payCoinRechargeDialog(){
        if (coinRechargeSheetView == null){
            coinRechargeSheetView = new CoinRechargeSheetView(this);
            coinRechargeSheetView.setClickListener(new CoinRechargeSheetView.ClickListener() {
                @Override
                public void paySuccess(GoodsEntity goodsEntity) {
                    //充值成功  查询当前用户余额
                    //viewModel.qryUserGameBalance();
                }
            });
        }
        if (!coinRechargeSheetView.isShowing()){
            coinRechargeSheetView.show();
        }
    }

    private void initListener(){
        dlRtcInterceptorCall = new DlRtcInterceptorCall();
        DLRTCInterceptorCall.Companion.getInstance().addDelegateInterceptorCall(dlRtcInterceptorCall);
        DLRTCInternalListenerManager.Companion.getInstance().addDelegate(mTRTCCallingListener);
    }
    private void destroyListener() {
        DLRTCInterceptorCall.Companion.getInstance().removeDelegateInterceptorCall(dlRtcInterceptorCall);
        DLRTCInternalListenerManager.Companion.getInstance().removeDelegate(mTRTCCallingListener);

    }
    /**
     * @Desc TODO(监听RTC信令回调)
     * @author 彭石林
     * @Date 2022/11/9
     */

    private UITRTCCallingDelegate mTRTCCallingListener = new UITRTCCallingDelegate() {
        @Override
        public void onError(int code, String msg) {
            initRlCallingUserLayout();
            MPTimber.tag(TAG).d("EmptyTRTCCallingDelegate onError code："+code+" , msg："+msg);
            ToastUtils.showLong(AppContext.instance().getString(R.string.trtccalling_toast_call_error_msg, code, msg));
        }

        //用户进入房间
        @Override
        public void onUserEnter(String userId) {
            viewModel.callingOnTheLine.set(false);
            viewModel.callingDropped.set(false);
            MPTimber.tag(TAG).d("EmptyTRTCCallingDelegate onUserEnter userId："+userId);
            binding.rlReceiveCall.setVisibility(View.GONE);
            gameCalling();
        }

        @Override
        public void onUserLeave(String userId) {
            MPTimber.tag(TAG).d("EmptyTRTCCallingDelegate onUserLeave userId："+userId);
        }

        @Override
        public void onReject(String userId) {
            initRlCallingUserLayout();
            ToastUtils.showShort(AppContext.instance().getString(R.string.playfun_the_other_party_refuses_to_answer));
            MPTimber.tag(TAG).d("EmptyTRTCCallingDelegate onReject userId："+userId);
        }

        @Override
        public void onLineBusy(String userId) {
            initRlCallingUserLayout();
            MPTimber.tag(TAG).d("EmptyTRTCCallingDelegate onReject userId："+userId);
            ToastUtils.showShort(AppContext.instance().getString(R.string.playfun_the_other_party_is_on_a_call));
        }

        @Override
        public void onCallingCancel() {
            initRlCallingUserLayout();
            //用户取消拨打
            MPTimber.tag(TAG).d("EmptyTRTCCallingDelegate onCallingCancel ");
        }

        @Override
        public void onCallingTimeout() {
            viewModel.callingOnTheLine.set(false);
            //拨打超时
            MPTimber.tag(TAG).d("EmptyTRTCCallingDelegate onCallingTimeout ");
            initRlCallingUserLayout();
        }

        @Override
        public void onCallEnd() {
            recycleHandles();
            MPTimber.tag(TAG).d("EmptyTRTCCallingDelegate onCallEnd ");
            ToastUtils.showShort(AppContext.instance().getString(R.string.playfun_call_ended));
            viewModel.callingDropped.set(true);
            //如果是围观方。应该结束游戏
            if(_circusesStatus){
                finish();
            }else{
                initRlCallingUserLayout();
            }

            //如果是围观方。挂断电挂应该关闭页面
        }

        @Override
        public void onNetworkQuality(TRTCCloudDef.TRTCQuality localQuality, ArrayList<TRTCCloudDef.TRTCQuality> remoteQuality) {
            //网络状态
            MPTimber.tag(TAG).d("EmptyTRTCCallingDelegate onNetworkQuality  ");
        }

        @Override
        public void onTryToReconnect() {
            //重新连接
            MPTimber.tag(TAG).d("EmptyTRTCCallingDelegate onTryToReconnect  ");
        }
    };
    // 初始化回拨业务UI
    private void initRlCallingUserLayout(){
        viewModel.gameCallEntity.setCalling(false);
        viewModel.callingOnTheLine.set(false);
        binding.getRoot().post(()->{
            binding.rlVideoCallLayout.setVisibility(View.GONE);
            binding.gameCallingVideoLayout.rlVideoAllLayout.setVisibility(View.GONE);
            binding.rlReceiveCall.setVisibility(View.GONE);
            if(viewModel.gameCallEntity!=null && viewModel.gameCallEntity.getCallingType()!=null){
                //视频通话
                if(viewModel.gameCallEntity.getCallingType() == DLRTCDataMessageType.DLInviteRTCType.dl_rtc_video){
                    if(faceUnityView != null){
                        binding.flFaceView.removeView(faceUnityView);
                    }
                    //显示用户头像可进行回拨
                    binding.rlCallingUserLayout.findViewById(R.id.img_call_audio_or_video).setActivated(false);
                }else{
                    //显示用户头像可进行回拨
                    binding.rlCallingUserLayout.findViewById(R.id.img_call_audio_or_video).setActivated(true);
                }
                binding.rlVideoCallLayout.setVisibility(View.GONE);
                binding.gameCallingVideoLayout.rlVideoAllLayout.setVisibility(View.GONE);
                binding.rlCallingUserLayout.findViewById(R.id.img_call_audio_or_video).setVisibility(View.VISIBLE);
                binding.rlCallingUserLayout.setVisibility(View.VISIBLE);
                binding.rlCallingUserLayout.findViewById(R.id.fl_triangle_off).setVisibility(View.GONE);
                binding.rlCallingUserLayout.findViewById(R.id.fl_triangle_on).setVisibility(View.GONE);
            };
        });
    }

    private final class DlRtcInterceptorCall implements  DLRTCInterceptorCall.InterceptorCall{
        /**
        * @Desc TODO(接听电话信令拦截)
        * @author 彭石林
        * @parame [userIDs, type, roomId, data, isFromGroup, sponsorID]
        * @return void
        * @Date 2022/11/11
        */
        @Override
        public void receiveCall(@NonNull String acceptUserId, @NonNull String inviteUserId, @NonNull DLRTCDataMessageType.DLInviteRTCType type, int roomId, @Nullable String data) {
            MPTimber.tag(TAG).d("音视频接听拦截：receiveCall ");
            MPTimber.tag(TAG).d("音视频接听拦截： roomId = "+roomId+" acceptUserId = "+acceptUserId+" data = "+data +" inviteUserId = "+inviteUserId +" type="+type.toString());
            GameCallEntity gameCallEntity = new GameCallEntity();
            gameCallEntity.setRoomId(roomId);
            gameCallEntity.setCallingRole(DLRTCCalling.Role.CALLED);
            gameCallEntity.setCallingType(type);
            gameCallEntity.setInviteUserId(inviteUserId);
            gameCallEntity.setAcceptUserId(acceptUserId);
            viewModel.gameCallEntity = gameCallEntity;
            binding.rlReceiveCall.setVisibility(View.VISIBLE);
            viewModel.getCallingUserInfo(null, gameCallEntity.getInviteUserId());
        }
    }


    //单个权限申请监听
    ActivityResultLauncher<String> toPermissionIntent = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        //获取单个权限成功
        if(result){
            //获取权限后接听电话
            DLRTCStartShowUIManager.Companion.getInstance().inviteUserAccept();
        }else{
            alertPermissions(R.string.playfun_permissions_audio_txt2);
        }
    });
    //多个权限申请监听
    ActivityResultLauncher<String[]> launcherPermissionArray = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                if (result.get(Manifest.permission.CAMERA) != null && result.get(Manifest.permission.RECORD_AUDIO) != null) {
                    if (Objects.requireNonNull(result.get(Manifest.permission.CAMERA)).equals(true) && Objects.requireNonNull(result.get(Manifest.permission.RECORD_AUDIO)).equals(true)) {
                        //权限全部获取到之后的动作
                        DLRTCStartShowUIManager.Companion.getInstance().inviteUserAccept();
                    } else {
                        //有权限没有获取到的动作
                        alertPermissions(R.string.playfun_permissions_video2);
                    }
                }
            });

    private void alertPermissions(@StringRes int stringResId){
        //获取语音权限失败
        CoinPusherDialogAdapter.getDialogPermissions(getContext(), stringResId, _success -> {
            if(_success){
                PermissionChecker.launchAppDetailsSettings(getContext());
            }
        }).show();
    }
    private void recycleHandles() {
        if (mHandler != null) {
            mHandler.removeCallbacks(timerRunnable);
            mHandler = null;
        }
    }
    /**
     * 每秒执行一次
     */
    public void TimeCallMessage() {
        if (timerRunnable != null) {
            return;
        }
        timerRunnable = () -> {
            viewModel.mTimeCount++;
            if (viewModel.mTimeCount % 30 == 0){
                viewModel.getRoomStatus(viewModel.gameCallEntity.getRoomId());
            }

            mHandler.postDelayed(timerRunnable, 1000);
        };
        mHandler.postDelayed(timerRunnable, 1000);
    }
    //处理通话中进入推币机的视频、语音页面处理
    private void gameCalling() {
        if(viewModel.gameCallEntity!=null && viewModel.gameCallEntity.getCallingType()!=null){
            viewModel.gameCallEntity.setCalling(true);
            //视频通话
            if(viewModel.gameCallEntity.getCallingType() == DLRTCDataMessageType.DLInviteRTCType.dl_rtc_video){
                viewModel.makeCallTypeField.set(false);
                binding.rlCallingUserLayout.setVisibility(View.GONE);
                binding.rlVideoCallLayout.setVisibility(View.VISIBLE);
                binding.gameCallingVideoLayout.rlVideoAllLayout.setVisibility(View.VISIBLE);
                //创建视频美颜渲染view
                faceUnityView = new FaceUnityView(getContext());
                faceUnityView.setBackgroundColor(ColorUtils.getColor(R.color.black));
                faceUnityView.bindDataFactory(new FaceUnityDataFactory(0));
                binding.flFaceView.addView(faceUnityView);
                //2.再打开摄像头
                DLRTCVideoLayoutManager dLRTCVideoLayoutManager = binding.rlVideoCallLayout.findViewById(R.id.rtc_layout_manager);
                dLRTCVideoLayoutManager.setEnabled(false);
                //2.再打开摄像头
                dLRTCVideoLayoutManager.initVideoFactory(new VideoLayoutFactory(getContext()));
                DLRTCVideoLayout videoLayout = dLRTCVideoLayoutManager.allocCloudVideoView(TUILogin.getLoginUser());
                if(videoLayout != null){
                    TXCloudVideoView txCloudVideoView = videoLayout.getVideoView();
                    ViewGroup.LayoutParams viewLayoutParams = (ViewGroup.LayoutParams)txCloudVideoView.getLayoutParams();
                    viewLayoutParams.height = 0;
                    viewLayoutParams.width = 0;
                    videoLayout.getVideoView().setLayoutParams(viewLayoutParams);
                    DLRTCVideoManager.Companion.getInstance().updateLocalView(txCloudVideoView);
                }
                String remoteViewUserId = TUILogin.getLoginUser().equals(viewModel.gameCallEntity.getInviteUserId()) ? viewModel.gameCallEntity.getAcceptUserId() :viewModel.gameCallEntity.getInviteUserId();
                //有用户的视频开启了
                DLRTCVideoLayout layout = dLRTCVideoLayoutManager.findCloudView(remoteViewUserId);
                if(layout == null){
                    layout =  dLRTCVideoLayoutManager.allocCloudVideoView(remoteViewUserId);
                }
                if (layout != null) {
                    DLRTCVideoManager.Companion.getInstance().updateRemoteView(remoteViewUserId, layout.getVideoView());
                }
            }else{//语音通话
                viewModel.makeCallTypeField.set(true);
                binding.rlVideoCallLayout.setVisibility(View.GONE);
                binding.gameCallingVideoLayout.rlVideoAllLayout.setVisibility(View.GONE);
                binding.rlCallingUserLayout.setVisibility(View.VISIBLE);
                binding.rlCallingUserLayout.findViewById(R.id.fl_triangle_off).setVisibility(View.VISIBLE);
                binding.rlCallingUserLayout.findViewById(R.id.fl_triangle_on).setVisibility(View.GONE);
                binding.rlCallingUserLayout.findViewById(R.id.img_call_audio_or_video).setVisibility(View.GONE);
            }
        }
    }
    /**
     * 展示右下角收益提示
     */
//    private void setProfitTips() {
//        if (Boolean.FALSE.equals(viewModel.isShowCountdown.get()) && viewModel.payeeProfits > 0) {//对方余额不足没有展示
//            if (!viewModel.girlEarningsField.get()) {
//                viewModel.girlEarningsField.set(true);
//            }
//            String profit = viewModel.payeeProfits + "";
//            String girlEarningsTex = String.format(com.blankj.utilcode.util.StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt), profit);
//            SpannableString stringBuilder = new SpannableString(girlEarningsTex);
//            ForegroundColorSpan blueSpan = new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint1));
//            int index = girlEarningsTex.indexOf(profit);
//            stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.white)), 0, girlEarningsTex.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            stringBuilder.setSpan(blueSpan, index, index + profit.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            viewModel.girlEarningsText.set(stringBuilder);
//        }
//    }
    private void startSVGAnim(String svgaPath) {
        if (!com.blankj.utilcode.util.StringUtils.isEmpty(svgaPath)) {
            SVGAParser svgaParser = SVGAParser.Companion.shareParser();
            try {
                svgaParser.decodeFromURL(new URL(StringUtil.getFullAudioUrl(svgaPath)), new SVGAParser.ParseCompletion() {
                    @Override
                    public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                        giftEffects.setVisibility(View.VISIBLE);
                        giftEffects.setVideoItem(videoItem);
                        giftEffects.setLoops(1);
                        giftEffects.setCallback(new BaseSVGACallback(){
                            @Override
                            public void onComplete() {
                                //播放完成
                                giftEffects.setVisibility(View.GONE);
                            }
                        });
                        giftEffects.startAnimation();
                    }
                    @Override
                    public void onError() {
                    }
                }, null);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
    private void startAcceptBannersAnimotion(String imgPath, int account) {
        if (account > 1) {
            View streamerView = View.inflate(getContext(), R.layout.call_user_streamer_item, null);
            //用户头像
            CircleImageView userImg = streamerView.findViewById(R.id.user_img);
            //礼物图片
            ImageView giftImg = streamerView.findViewById(R.id.gift_img);
            //礼物图片数量
            ImageView giftNumImg = streamerView.findViewById(R.id.gift_num_img);
            //文案
            TextView tipText = streamerView.findViewById(R.id.tip_text);
            String sexText = viewModel.otherCallInfoEntity.get().getNickname();
            String messageText = com.blankj.utilcode.util.StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt1);
            String lastText = com.blankj.utilcode.util.StringUtils.getString(R.string.playfun_call_message_deatail_girl_txt17);
            String itemTextMessage = sexText + "\n" + messageText + lastText;
            SpannableString stringBuilder = new SpannableString(itemTextMessage);
            stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint2)), 0, sexText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.white)), sexText.length(), sexText.length() + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            stringBuilder.setSpan(new ForegroundColorSpan(ColorUtils.getColor(R.color.call_message_deatail_hint2)), sexText.length() + 2, itemTextMessage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tipText.setText(stringBuilder);
            Glide.with(getContext())
                    .asBitmap()
                    .load(StringUtil.getFullImageUrl(viewModel.otherCallInfoEntity.get().getAvatar()))
                    .error(R.drawable.default_avatar)
                    .placeholder(R.drawable.default_avatar)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(userImg);
            Glide.with(getContext())
                    .asBitmap()
                    .load(StringUtil.getFullImageUrl(imgPath))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(giftImg);
            if (account == 1) {
                giftNumImg.setImageResource(R.drawable.img_gift_num1);
            } else if (account == 10) {
                giftNumImg.setImageResource(R.drawable.img_gift_num10);
            } else if (account == 38) {
                giftNumImg.setImageResource(R.drawable.img_gift_num38);
            } else if (account == 66) {
                giftNumImg.setImageResource(R.drawable.img_gift_num66);
            } else if (account == 188) {
                giftNumImg.setImageResource(R.drawable.img_gift_num188);
            } else if (account == 520) {
                giftNumImg.setImageResource(R.drawable.img_gift_num520);
            } else if (account == 1314) {
                giftNumImg.setImageResource(R.drawable.img_gift_num1314);
            } else if (account == 3344) {
                giftNumImg.setImageResource(R.drawable.img_gift_num3344);
            }
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_call_audio_right_in_streamer);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    postRemoveView(binding.mainView, streamerView);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.leftMargin = Utils.dip2px(getContext(),14);
            layoutParams.topMargin = Utils.dip2px(getContext(),249);
            streamerView.setLayoutParams(layoutParams);
            binding.mainView.addView(streamerView);
            streamerView.startAnimation(animation);
        }
    }
    private void startAcceptHeadAnimotion(boolean self,String imgPath,String svgaPath, int account) {
        ImageView giftImageTrans = new ImageView(getContext());
        Animation animation;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Utils.dip2px(getContext(),40), Utils.dip2px(getContext(),40));
        if(self){
            animation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_call_coinpusher_receive_tip);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            //layoutParams.leftMargin = Utils.dip2px(getContext(),277);
            layoutParams.bottomMargin = Utils.dip2px(getContext(),71);
        }else{
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            animation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_call_coinpusher_send_tip);
            layoutParams.topMargin = Utils.dip2px(getContext(),135);
        }
        giftImageTrans.setLayoutParams(layoutParams);
        giftImageTrans.setScaleType(ImageView.ScaleType.CENTER_CROP);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                postRemoveView(binding.mainView, giftImageTrans);
                //启动SVG动画
                startSVGAnim(svgaPath);
                //启动横幅动画
                startAcceptBannersAnimotion(imgPath, account);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        Glide.with(getContext())
                .asBitmap()
                .load(StringUtil.getFullImageUrl(imgPath))
                .error(R.drawable.radio_program_list_content)
                .placeholder(R.drawable.radio_program_list_content)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(giftImageTrans);
        binding.mainView.addView(giftImageTrans);
        giftImageTrans.startAnimation(animation);
    }
    //异步移除view
    private void postRemoveView(ViewGroup viewGroup, View IiageTrans) {
        viewGroup.post(() -> viewGroup.removeView(IiageTrans));
    }
}
