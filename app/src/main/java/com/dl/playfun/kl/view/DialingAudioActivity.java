package com.dl.playfun.kl.view;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.BR;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.entity.CallingInviteInfo;
import com.dl.playfun.event.AudioCallingCancelEvent;
import com.dl.playfun.kl.viewmodel.AudioCallingViewModel2;
import com.dl.playfun.manager.ConfigManager;
import com.dl.manager.LocaleManager;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.widget.dialog.TraceDialog;
import com.dl.rtc.calling.base.DLRTCCalling;
import com.dl.rtc.calling.manager.DLRTCStartShowUIManager;
import com.dl.rtc.calling.model.DLRTCCallingConstants;
import com.google.gson.Gson;
import com.dl.playfun.R;
import com.dl.playfun.databinding.ActivityCallWaiting2Binding;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.imsdk.v2.V2TIMManager;

import me.goldze.mvvmhabit.base.BaseActivity;
import me.goldze.mvvmhabit.bus.RxBus;

public class DialingAudioActivity extends BaseActivity<ActivityCallWaiting2Binding, AudioCallingViewModel2> {

    private CallingInviteInfo callingInviteInfo;
    //拨打方UserId
    private String callUserId;
    private String toId;
    private Integer roomId;
    private DLRTCCalling.Role role;

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
        String userIds = intent.getExtras().getString(DLRTCCallingConstants.PARAM_NAME_USERIDS);
        if (userIds != null) {
            toId = userIds;
        }
        //主动呼叫
        callUserId = intent.getExtras().getString(DLRTCCallingConstants.PARAM_NAME_SPONSORID);
        String userData = intent.getExtras().getString("userProfile");
        if (userData != null) {
            callingInviteInfo = new Gson().fromJson(userData, CallingInviteInfo.class);
        }

    }

    @Override
    public void initData() {
        super.initData();
        if (role == DLRTCCalling.Role.CALL) {//主动呼叫
            callUserId = V2TIMManager.getInstance().getLoginUser();
            if (callingInviteInfo != null) {
                viewModel.init(callUserId, toId, role, callingInviteInfo.getRoomId());
                viewModel.callingInviteInfoField.set(callingInviteInfo);
                if (callingInviteInfo.getUserProfileInfo().getSex() == 1 && ConfigManager.getInstance().getTipMoneyShowFlag()) {
                    if (!ObjectUtils.isEmpty(callingInviteInfo.getMessages()) && callingInviteInfo.getMessages().size() > 0) {
                        String valueData = "";
                        for (String value : callingInviteInfo.getMessages()) {
                            valueData += value + "\n";
                        }
                        viewModel.maleBinding.set(valueData);
                    }
                }
                viewModel.start();
            }
        } else {//被动接听
            toId = V2TIMManager.getInstance().getLoginUser();
            viewModel.init(callUserId, toId, role);
            viewModel.getCallingInvitedInfo(1, callUserId);
        }
        try {
            new RxPermissions(this)
                    .request(Manifest.permission.RECORD_AUDIO)
                    .subscribe(granted -> {
                        if (granted) {

                        } else {
                            if(this.isFinishing()){
                                return;
                            }
                            TraceDialog.getInstance(DialingAudioActivity.this)
                                    .setCannelOnclick(new TraceDialog.CannelOnclick() {
                                        @Override
                                        public void cannel(Dialog dialog) {
                                            viewModel.cancelCallClick();
                                        }
                                    })
                                    .setConfirmOnlick(new TraceDialog.ConfirmOnclick() {
                                        @Override
                                        public void confirm(Dialog dialog) {
                                            new RxPermissions(DialingAudioActivity.this)
                                                    .request(Manifest.permission.RECORD_AUDIO)
                                                    .subscribe(granted -> {
                                                        if (!granted) {
                                                            viewModel.cancelCallClick();
                                                        }
                                                    });
                                        }
                                    }).AlertCallAudioPermissions().show();
                        }
                    });
        } catch (Exception e) {

        }
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.backViewEvent.observe(this, unused -> finish());

        viewModel.startAudioActivity.observe(this, roomId -> {
            //有人进入房间回调
            DLRTCStartShowUIManager.Companion.getInstance().inviteUserAccept();
            Intent intent = new Intent(DialingAudioActivity.this,AudioCallChatingActivity.class);
            intent.putExtra("fromUserId", callUserId);
            intent.putExtra("toUserId", toId);
            intent.putExtra("mRole", role);
            intent.putExtra("roomId", roomId);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.anim_zoom_in, R.anim.anim_stay);
        });


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        RxBus.getDefault().post(new AudioCallingCancelEvent());
    }
}
