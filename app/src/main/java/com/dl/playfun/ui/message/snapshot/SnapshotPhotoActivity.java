package com.dl.playfun.ui.message.snapshot;

import android.os.Bundle;
import android.util.Log;

import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.GlideEngine;
import com.dl.playfun.databinding.ActivitySnapshotPhotoSettingBinding;
import com.dl.playfun.ui.base.BaseActivity;
import com.dl.playfun.utils.AutoSizeUtils;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.qcloud.tuicore.TUILogin;

/**
 * Author: 彭石林
 * Time: 2022/9/9 12:02
 * Description: This is SnapshotPhotoActivity
 */
public class SnapshotPhotoActivity extends BaseActivity<ActivitySnapshotPhotoSettingBinding,SnapshotPhotoViewModel> {


    private String imgPath = null;
    private boolean snapshot = false;
    private boolean isVideo = false;

    private SnapshotPhotoDialog snapshotPhotoDialog;
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
        AutoSizeUtils.applyAdapt(this.getResources());
        return R.layout.activity_snapshot_photo_setting;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initParam() {
        super.initParam();
        imgPath = getIntent().getStringExtra("imgPath");
        snapshot = getIntent().getBooleanExtra("snapshot",false);
        isVideo = getIntent().getBooleanExtra("isVideo",false);
    }

    @Override
    public SnapshotPhotoViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(getApplication());
        return ViewModelProviders.of(this, factory).get(SnapshotPhotoViewModel.class);
    }

    @Override
    public void initData() {
        super.initData();
        viewModel.isVideoSetting.set(snapshot);
        if(isVideo){

        }else{
            //选择的是图片
            Log.e("当前选择图片地址：",String.valueOf(imgPath));
            GlideEngine.createGlideEngine().loadImage(this,imgPath,binding.imgContent,binding.imgLong);
        }

    }



    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.settingEvent.observe(this, unused -> {
            if(snapshotPhotoDialog==null){
                snapshotPhotoDialog = new SnapshotPhotoDialog(this);
            }
            snapshotPhotoDialog.show();
        });
    }

    @Override
    public void onDestroy() {
        if(snapshotPhotoDialog!=null){
            snapshotPhotoDialog.dismiss();
            snapshotPhotoDialog = null;
        }
        super.onDestroy();
    }

}
