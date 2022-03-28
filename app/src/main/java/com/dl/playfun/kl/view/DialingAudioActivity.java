package com.dl.playfun.kl.view;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.entity.CallingInviteInfo;
import com.dl.playfun.event.AudioCallingCancelEvent;
import com.dl.playfun.kl.viewmodel.AudioCallingViewModel2;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.utils.ChatUtils;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.widget.dialog.TraceDialog;
import com.google.gson.Gson;
import com.dl.playfun.R;
import com.dl.playfun.databinding.ActivityCallWaiting2Binding;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.liteav.trtccalling.model.TUICalling;
import com.tencent.liteav.trtccalling.model.util.TUICallingConstants;

import me.goldze.mvvmhabit.base.BaseActivity;
import me.goldze.mvvmhabit.bus.RxBus;
import me.jessyan.autosize.internal.CustomAdapt;
import me.tatarka.bindingcollectionadapter2.BR;

public class DialingAudioActivity extends BaseActivity<ActivityCallWaiting2Binding, AudioCallingViewModel2> implements CustomAdapt {

    private CallingInviteInfo callingInviteInfo;
    //拨打方UserId
    private String callUserId;
    private String toId;
    private Integer roomId;
    private TUICalling.Role role;


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
        role = (TUICalling.Role) intent.getExtras().get(TUICallingConstants.PARAM_NAME_ROLE);
        //被动接收
        String[] userIds = intent.getExtras().getStringArray(TUICallingConstants.PARAM_NAME_USERIDS);
        if (userIds != null && userIds.length > 0) {
            toId = userIds[0];
        }
        //主动呼叫
        callUserId = intent.getExtras().getString(TUICallingConstants.PARAM_NAME_SPONSORID);
        String userData = intent.getExtras().getString("userProfile");
        if (userData != null) {
            callingInviteInfo = new Gson().fromJson(userData, CallingInviteInfo.class);
        }
    }

    @Override
    public void initData() {
        super.initData();
        if (role == TUICalling.Role.CALL) {//主动呼叫
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
            viewModel.getCallingInvitedInfo(1, ChatUtils.imUserIdToSystemUserId(callUserId));
        }
        try {
            new RxPermissions(this)
                    .request(Manifest.permission.RECORD_AUDIO)
                    .subscribe(granted -> {
                        if (granted) {

                        } else {
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
        viewModel.backViewEvent.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {
                finish();
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        RxBus.getDefault().post(new AudioCallingCancelEvent());
    }

    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return 360;
    }
}
