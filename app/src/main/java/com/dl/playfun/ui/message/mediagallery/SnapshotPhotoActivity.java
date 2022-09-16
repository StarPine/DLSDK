package com.dl.playfun.ui.message.mediagallery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.GlideEngine;
import com.dl.playfun.databinding.ActivitySnapshotPhotoSettingBinding;
import com.dl.playfun.entity.MediaPayPerConfigEntity;
import com.tencent.qcloud.tuicore.custom.entity.MediaGalleryEditEntity;
import com.dl.playfun.ui.base.BaseActivity;
import com.dl.playfun.utils.AutoSizeUtils;
import com.dl.playfun.utils.ImmersionBarUtils;

import java.util.List;

/**
 * Author: 彭石林
 * Time: 2022/9/9 12:02
 * Description: This is SnapshotPhotoActivity
 */
public class SnapshotPhotoActivity extends BaseActivity<ActivitySnapshotPhotoSettingBinding,SnapshotPhotoViewModel> {


    private String srcPath = null;
    private boolean isPayState = false;
    private boolean isVideo = false;

    private MediaPayPerConfigEntity.itemTagEntity mediaPriceTmpConfig;
    private SnapshotPhotoDialog snapshotPhotoDialog;

    /**
    * @Desc TODO()
    * @author 彭石林
    * @parame [isPayState 是否付费, isVideoSetting 是否 视频, srcPath 文件信息]
    * @return android.content.Intent
    * @Date 2022/9/14
    */
    public static Intent createIntent(Context mContext, boolean isPayState, boolean isVideoSetting, String srcPath,MediaPayPerConfigEntity.itemTagEntity mediaPriceTmpConfig){
        Intent snapshotIntent = new Intent(mContext,SnapshotPhotoActivity.class);
        snapshotIntent.putExtra("isPayState",isPayState);
        snapshotIntent.putExtra("isVideo",isVideoSetting);
        snapshotIntent.putExtra("srcPath",srcPath);
        snapshotIntent.putExtra("mediaPriceTmpConfig",mediaPriceTmpConfig);
        return snapshotIntent;
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
        Intent intent = getIntent();
        if(intent != null){
            srcPath = intent.getStringExtra("srcPath");
            isPayState = intent.getBooleanExtra("isPayState",false);
            isVideo = intent.getBooleanExtra("isVideo",false);
            mediaPriceTmpConfig = (MediaPayPerConfigEntity.itemTagEntity) intent.getSerializableExtra("mediaPriceTmpConfig");
        }
    }

    @Override
    public SnapshotPhotoViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(getApplication());
        return ViewModelProviders.of(this, factory).get(SnapshotPhotoViewModel.class);
    }

    @Override
    public void initData() {
        super.initData();
        viewModel.isVideoSetting.set(isVideo);
        viewModel.srcPath.set(srcPath);
        viewModel.isPayState.set(isPayState);
        if(isVideo){

        }else{
            //选择的是图片
            Log.e("当前选择图片地址：",String.valueOf(srcPath));
            GlideEngine.createGlideEngine().loadImage(this, srcPath,binding.imgContent,binding.imgLong);
        }

    }



    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.settingEvent.observe(this, unused -> {
            if(snapshotPhotoDialog==null){
                snapshotPhotoDialog = new SnapshotPhotoDialog(this,mediaPriceTmpConfig);
            }
            snapshotPhotoDialog.show();
        });
        //返回页面数据
        viewModel.setResultDataEvent.observe(this,filePath -> {
            MediaGalleryEditEntity mediaGalleryEditEntity = new MediaGalleryEditEntity();
            mediaGalleryEditEntity.setVideoSetting(isVideo);
            mediaGalleryEditEntity.setStatePay(isPayState);
            mediaGalleryEditEntity.setSrcPath(filePath);
            mediaGalleryEditEntity.setStateSnapshot(viewModel.isBurn.get());
            Intent intent = new Intent();
            intent.putExtra("mediaGalleryEditEntity", mediaGalleryEditEntity);
            setResult(2001,intent);
            finish();
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
