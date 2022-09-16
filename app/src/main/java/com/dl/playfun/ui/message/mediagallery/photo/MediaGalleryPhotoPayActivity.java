package com.dl.playfun.ui.message.mediagallery.photo;

import static com.blankj.utilcode.util.SnackbarUtils.dismiss;
import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.GlideEngine;
import com.dl.playfun.databinding.ActivityMediaGalleryPhotoBinding;
import com.dl.playfun.entity.MediaPayPerConfigEntity;
import com.dl.playfun.transformations.MvBlurTransformation;
import com.dl.playfun.ui.base.BaseActivity;
import com.dl.playfun.ui.message.mediagallery.SnapshotPhotoActivity;
import com.dl.playfun.utils.AutoSizeUtils;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.utils.StringUtil;
import com.luck.picture.lib.listener.OnImageCompleteCallback;
import com.luck.picture.lib.tools.MediaUtils;
import com.luck.picture.lib.widget.longimage.ImageSource;
import com.luck.picture.lib.widget.longimage.ImageViewState;
import com.luck.picture.lib.widget.longimage.SubsamplingScaleImageView;
import com.tencent.qcloud.tuicore.custom.entity.MediaGalleryEditEntity;
import com.tencent.qcloud.tuikit.tuichat.component.photoview.view.PhotoView;

/**
 * Author: 彭石林
 * Time: 2022/9/14 11:20
 * Description: This is MediaGalleryPhotoPayActivity
 */
public class MediaGalleryPhotoPayActivity extends BaseActivity<ActivityMediaGalleryPhotoBinding,MediaGalleryPhotoPayViewModel> {
    private static final String TAG = "MediaGalleryPhotoPay";
    private MediaGalleryEditEntity mediaGalleryEditEntity;
    //倒计时
    private CountDownTimer downTimer;
    @Override
    public int initContentView(Bundle savedInstanceState) {
        AutoSizeUtils.applyAdapt(getResources());
        return R.layout.activity_media_gallery_photo;
    }

    @Override
    public int initVariableId() {
        return BR.photoViewModel;
    }

    /**
     * @Desc TODO()
     * @author 彭石林
     * @parame [isPayState 是否付费, isVideoSetting 是否 视频, srcPath 文件信息]
     * @return android.content.Intent
     * @Date 2022/9/14
     */
    public static Intent createIntent(Context mContext, MediaGalleryEditEntity mediaGalleryEditEntity){
        Intent snapshotIntent = new Intent(mContext, MediaGalleryPhotoPayActivity.class);
        snapshotIntent.putExtra("mediaGalleryEditEntity",mediaGalleryEditEntity);
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
    public void initParam() {
        super.initParam();
        mediaGalleryEditEntity = (MediaGalleryEditEntity) getIntent().getSerializableExtra("mediaGalleryEditEntity");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AutoSizeUtils.closeAdapt(getResources());
        stopTimer();
    }

    @Override
    public void initData() {
        super.initData();
        Log.e(TAG,"当前传递的内容为："+String.valueOf(mediaGalleryEditEntity==null));
        if(mediaGalleryEditEntity!=null){
            Log.e(TAG,"当前传递的内容为："+String.valueOf(mediaGalleryEditEntity.toString()));
            //快照
            if(mediaGalleryEditEntity.isStateSnapshot()){
                GlideEngine.createGlideEngine().loadImage(this, StringUtil.getFullImageUrl(mediaGalleryEditEntity.getSrcPath()), binding.imgContent, binding.imgLong,true, new GlideEngine.LoadProgressCallback() {
                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        showHud();
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        dismissHud();
                    }

                    @Override
                    public void setResource(boolean imgLong) {
                        dismissHud();
                        binding.rlContainer.setVisibility(View.VISIBLE);
                        viewModel.snapshotLockState.set(true);
                    }
                });
                Log.e(TAG,"当前oss文件地址："+StringUtil.getFullImageUrl(mediaGalleryEditEntity.getSrcPath()));
            }else{
                Log.e(TAG,"当前oss文件地址："+StringUtil.getFullImageUrl(mediaGalleryEditEntity.getSrcPath()));
                GlideEngine.createGlideEngine().loadImage(this, StringUtil.getFullImageUrl(mediaGalleryEditEntity.getSrcPath()), binding.imgContent, binding.imgLong,false, new GlideEngine.LoadProgressCallback() {
                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        showHud();
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        dismissHud();
                    }

                    @Override
                    public void setResource(boolean imgLong) {
                        dismissHud();
                    }
                });
            }
        }
    }

    @Override
    public MediaGalleryPhotoPayViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(getApplication());
        return ViewModelProviders.of(this, factory).get(MediaGalleryPhotoPayViewModel.class);
    }

    /**
     * 开始计时
     */
    public void startTimer() {
        //倒计时15秒，一次1秒
        downTimer = new CountDownTimer(2 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                    viewModel.snapshotTimeText.set((millisUntilFinished / 1000)+"s");
            }
            @Override
            public void onFinish() {
                stopTimer();
                viewModel.evaluationState.set(true);
            }
        };
        downTimer.start();
    }

    /**
     * 停止计时
     */
    public void stopTimer() {
        if (downTimer != null) {
            downTimer.cancel();
            downTimer = null;
        }
    }


}
