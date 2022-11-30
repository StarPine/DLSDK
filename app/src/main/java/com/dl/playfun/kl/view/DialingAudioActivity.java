package com.dl.playfun.kl.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.lib.util.log.MPTimber;
import com.dl.playfun.BR;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.entity.CallUserRoomInfoEntity;
import com.dl.playfun.event.AudioCallingCancelEvent;
import com.dl.playfun.kl.CallChatingConstant;
import com.dl.playfun.kl.viewmodel.AudioCallingViewModel2;
import com.dl.manager.LocaleManager;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.coinpusher.dialog.CoinPusherDialogAdapter;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.rtc.calling.base.DLRTCCalling;
import com.dl.rtc.calling.manager.DLRTCStartShowUIManager;
import com.dl.rtc.calling.model.DLRTCCallingConstants;
import com.dl.playfun.R;
import com.dl.playfun.databinding.ActivityCallWaiting2Binding;
import com.luck.picture.lib.permissions.PermissionChecker;

import java.math.BigDecimal;
import java.math.RoundingMode;

import me.goldze.mvvmhabit.base.BaseActivity;
import me.goldze.mvvmhabit.bus.RxBus;

public class DialingAudioActivity extends BaseActivity<ActivityCallWaiting2Binding, AudioCallingViewModel2> {

    //拨打方UserId
    private String inviteUserID;
    //接听方
    private String acceptUserID;
    //当前是否是拨打人
    private boolean inviteSelf;
    private DLRTCCalling.Role role;
    private int rtcInviteRoomId;

    private String inviteExtJson = null;
    private CallUserRoomInfoEntity _callUserRoomInfoEntity = null;

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
    protected void onResume() {
        super.onResume();
        ImmersionBarUtils.setupStatusBar(this, false, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ImmersionBarUtils.setupStatusBar(this, true, true);
    }

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_call_waiting2;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public AudioCallingViewModel2 initViewModel() {
        //使用自定义的ViewModelFactory来创建ViewModel，如果不重写该方法，则默认会调用LoginViewModel(@NonNull Application application)构造方法
        AppViewModelFactory factory = AppViewModelFactory.getInstance(getApplication());
        return ViewModelProviders.of(this, factory).get(AudioCallingViewModel2.class);
    }

    @Override
    public void initParam() {
        super.initParam();
        Intent intent = getIntent();
        role = (DLRTCCalling.Role) intent.getExtras().get(DLRTCCallingConstants.PARAM_NAME_ROLE);
        //被动接收
        acceptUserID = intent.getStringExtra(DLRTCCallingConstants.DLRTCAcceptUserID);
        //主动呼叫
        inviteUserID = intent.getStringExtra(DLRTCCallingConstants.DLRTCInviteUserID);
        inviteSelf = intent.getBooleanExtra(DLRTCCallingConstants.DLRTCInviteSelf,false);
        rtcInviteRoomId = intent.getIntExtra(DLRTCCallingConstants.RTCInviteRoomID,0);
        inviteExtJson = intent.getStringExtra(DLRTCCallingConstants.inviteExtJson);

    }

    @Override
    public void initData() {
        super.initData();
        DLRTCStartShowUIManager.Companion.getInstance().startRing();
        viewModel.CallingRoomId = rtcInviteRoomId;
        viewModel.mRole = role;
        viewModel.isCallBinding.set(role == DLRTCCalling.Role.CALL);
        if (role == DLRTCCalling.Role.CALL) {//主动呼叫
            viewModel.getCallingUserInfo(null, acceptUserID);
        } else {//被动接听
            viewModel.getCallingUserInfo(null, inviteUserID);
        }
        if(!StringUtils.isEmpty(inviteExtJson)){
            _callUserRoomInfoEntity = GsonUtils.fromJson(inviteExtJson,CallUserRoomInfoEntity.class);
            callPromptText();
        }
        toPermissionIntent.launch(Manifest.permission.RECORD_AUDIO);
    }
    //处理通话中提示
    private  void callPromptText(){
        if(_callUserRoomInfoEntity != null && !StringUtils.isTrimEmpty(_callUserRoomInfoEntity.getPayerImId())){
            Log.e("当前处理通话中tishi",_callUserRoomInfoEntity.toString());
            //不是付费方：收益方
            if(!_callUserRoomInfoEntity.getPayerImId().equals(ConfigManager.getInstance().getUserImID())){
               String audioHint =  getString(R.string.playfun_call_earnings_audio_hint);
                StringBuilder stringBuffer = new StringBuilder();
                BigDecimal profitPerMinutes = _callUserRoomInfoEntity.getProfitPerMinutes();
                if(null != profitPerMinutes){
                    stringBuffer.append(String.format(audioHint,profitPerMinutes.setScale(2, RoundingMode.HALF_UP))).append("\n");
                    stringBuffer.append(getString(R.string.playfun_call_earnings_hint));
                    viewModel.callPromptText.set(stringBuffer.toString());
                }
            }else{
                String payAudioHint =  getString(R.string.playfun_call_pay_audio_hint);
                StringBuilder stringBuffer = new StringBuilder();
                BigDecimal coinPerMinutes = _callUserRoomInfoEntity.getCoinPerMinutes();
                if(null != coinPerMinutes){
                    stringBuffer.append(String.format(payAudioHint,String.valueOf(coinPerMinutes.intValue()))).append("\n");
                    viewModel.callPromptText.set(stringBuffer.toString());
                }
            }
        }
    }

    //单个权限申请监听
    ActivityResultLauncher<String> toPermissionIntent = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        //获取单个权限成功
        if(!result){
            alertPermissions(R.string.playfun_permissions_audio_txt2);
        }
    });
    private void alertPermissions(@StringRes int stringResId){
        //获取语音权限失败
        CoinPusherDialogAdapter.getDialogPermissions(DialingAudioActivity.this, stringResId, _success -> {
            if(_success){
                PermissionChecker.launchAppDetailsSettings(DialingAudioActivity.this);
            }else {
                viewModel.cancelCallClick();
            }
        }).show();
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.backViewEvent.observe(this, unused -> {
            finish();
        });
        viewModel.startAudioActivity.observe(this, roomId -> {
            viewModel.updateCallingStatus(CallChatingConstant.enterRoom);
            //关闭当前页面
            finish();
            overridePendingTransition(R.anim.anim_zoom_in, R.anim.anim_stay);
            //有人进入房间回调
            DLRTCStartShowUIManager.Companion.getInstance().inviteUserAccept();
            Intent intent = new Intent(DialingAudioActivity.this,AudioCallChatingActivity.class);
            intent.putExtra(DLRTCCallingConstants.DLRTCInviteUserID, inviteUserID);
            intent.putExtra(DLRTCCallingConstants.DLRTCAcceptUserID, acceptUserID);
            intent.putExtra(DLRTCCallingConstants.PARAM_NAME_ROLE, role);
            intent.putExtra(DLRTCCallingConstants.RTCInviteRoomID, roomId);
            intent.putExtra(DLRTCCallingConstants.DLRTCInviteSelf, inviteSelf);
            intent.putExtra("CallUserRoomInfoEntity",_callUserRoomInfoEntity);
            intent.putExtra("CallingInviteInfoField",viewModel.callingInviteInfoField.get());
            startActivity(intent);

        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        viewModel.cancelCallClick();
    }
}
