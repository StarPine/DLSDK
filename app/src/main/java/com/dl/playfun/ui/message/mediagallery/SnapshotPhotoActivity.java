package com.dl.playfun.ui.message.mediagallery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.GlideEngine;
import com.dl.playfun.databinding.ActivitySnapshotPhotoSettingBinding;
import com.dl.playfun.entity.MediaPayPerConfigEntity;
import com.dl.playfun.manager.ConfigManager;
import com.tencent.qcloud.tuicore.custom.entity.MediaGalleryEditEntity;
import com.dl.playfun.ui.base.BaseActivity;
import com.dl.playfun.utils.AutoSizeUtils;
import com.dl.playfun.utils.ImmersionBarUtils;

import java.math.BigDecimal;

import me.goldze.mvvmhabit.utils.StringUtils;

/**
 * Author: 彭石林
 * Time: 2022/9/9 12:02
 * Description: This is SnapshotPhotoActivity
 */
public class SnapshotPhotoActivity extends BaseActivity<ActivitySnapshotPhotoSettingBinding,SnapshotPhotoViewModel> {
    //本地价格模板
    private final String localPriceConfigSettingKey = "MediaGalleryPhotoSettingKey";
    private MediaPayPerConfigEntity.ItemEntity localCheckItemEntity = null;

    private String srcLocalPath = null;
    private boolean isPayState = false;

    private MediaPayPerConfigEntity.itemTagEntity mediaPriceTmpConfig;
    private SnapshotPhotoDialog snapshotPhotoDialog;

    private MediaPayPerConfigEntity.ItemEntity checkItemEntity;
    private Integer configId;

    /**
    * @Desc TODO()
    * @author 彭石林
    * @parame [isPayState 是否付费, isVideoSetting 是否 视频, srcPath 文件信息]
    * @return android.content.Intent
    * @Date 2022/9/14
    */
    public static Intent createIntent(Context mContext, boolean isPayState, String srcPath,MediaPayPerConfigEntity.itemTagEntity mediaPriceTmpConfig){
        Intent snapshotIntent = new Intent(mContext,SnapshotPhotoActivity.class);
        snapshotIntent.putExtra("isPayState",isPayState);
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
            srcLocalPath = intent.getStringExtra("srcPath");
            isPayState = intent.getBooleanExtra("isPayState",false);
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
        viewModel.srcPath.set(srcLocalPath);
        viewModel.isPayState.set(isPayState);

        String localPriceValue = ConfigManager.getInstance().getAppRepository().readKeyValue(localPriceConfigSettingKey);
        if(!StringUtils.isEmpty(localPriceValue)){
            localCheckItemEntity = GsonUtils.fromJson(localPriceValue, MediaPayPerConfigEntity.ItemEntity.class);
        }
        if(ObjectUtils.isNotEmpty(localCheckItemEntity)){
            checkItemEntity = localCheckItemEntity;
        }else{
            checkItemEntity = mediaPriceTmpConfig.getContent().get(0);
        }
        //选择的是图片
        GlideEngine.createGlideEngine().loadImage(this, srcLocalPath,binding.imgContent,binding.imgLong,R.drawable.playfun_loading_logo_placeholder_max,R.drawable.playfun_loading_logo_error);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.settingEvent.observe(this, unused -> {
            if(snapshotPhotoDialog==null){
                snapshotPhotoDialog = new SnapshotPhotoDialog(this,mediaPriceTmpConfig,localCheckItemEntity);
                snapshotPhotoDialog.setSnapshotListener((itemEntity, configId) -> {
                    checkItemEntity = itemEntity;
                    this.configId = configId;
                    ConfigManager.getInstance().getAppRepository().putKeyValue(localPriceConfigSettingKey,GsonUtils.toJson(checkItemEntity));
                });
            }
            snapshotPhotoDialog.show();
        });
        //返回页面数据
        viewModel.setResultDataEvent.observe(this,filePath -> {
            MediaGalleryEditEntity mediaGalleryEditEntity = new MediaGalleryEditEntity();
            mediaGalleryEditEntity.setVideoSetting(false);
            mediaGalleryEditEntity.setStatePay(isPayState);
            mediaGalleryEditEntity.setAndroidLocalSrcPath(srcLocalPath);
            if(checkItemEntity!=null && isPayState){
                mediaGalleryEditEntity.setUnlockPrice(new BigDecimal(checkItemEntity.getCoin()));
                mediaGalleryEditEntity.setMsgRenvenue(checkItemEntity.getProfit());
                mediaGalleryEditEntity.setConfigId(configId);
                mediaGalleryEditEntity.setConfigIndex(checkItemEntity.getConfigIndexString());
            }
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
