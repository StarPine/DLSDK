package com.dl.playfun.ui.message.mediagallery.photo;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.GlideEngine;
import com.dl.playfun.databinding.ActivityMediaGalleryPhotoBinding;
import com.dl.playfun.ui.base.BaseActivity;
import com.dl.playfun.utils.AutoSizeUtils;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.utils.StringUtil;
import com.tencent.qcloud.tuicore.custom.CustomDrawableUtils;
import com.tencent.qcloud.tuicore.custom.entity.MediaGalleryEditEntity;

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
        ImmersionBarUtils.setupStatusBar(this, false, false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ImmersionBarUtils.setupStatusBar(this, true, false);
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
        viewModel.mediaGalleryEditEntity = mediaGalleryEditEntity;
        Log.e(TAG,"当前传递的内容为："+String.valueOf(mediaGalleryEditEntity==null));
        if(mediaGalleryEditEntity!=null){
            Log.e(TAG,"当前传递的内容为："+String.valueOf(mediaGalleryEditEntity.toString()));
            //快照 并且不是自己查看 加蒙版
            if(mediaGalleryEditEntity.isStateSnapshot() && !mediaGalleryEditEntity.isSelfSend()){
                viewModel.mediaGalleryEvaluationQry(mediaGalleryEditEntity.getMsgKeyId(),mediaGalleryEditEntity.getToUserId());
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
        //好评
        binding.llLike.setOnClickListener(v -> {
            Integer evaluationType = viewModel.evaluationLikeEvent.getValue();
            if(evaluationType !=null && evaluationType == 0){
                viewModel.mediaGalleryEvaluationPut(mediaGalleryEditEntity.getMsgKeyId(),mediaGalleryEditEntity.getToUserId(),2);
            }
        });
        binding.llNoLike.setOnClickListener(v -> {
            Integer evaluationType = viewModel.evaluationLikeEvent.getValue();
            if(evaluationType !=null && evaluationType == 0){
                //差评
                viewModel.mediaGalleryEvaluationPut(mediaGalleryEditEntity.getMsgKeyId(),mediaGalleryEditEntity.getToUserId(),1);
            }
        });
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        //解锁事件
        viewModel.snapshotLockEvent.observe(this, unused -> {
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
                    viewModel.snapshotLockState.set(false);
                    //图片加载成功开始倒计时
                    startTimer();
                }
            });
        });
        //当前评价状态
        viewModel.evaluationLikeEvent.observe(this, state -> {
            //评价，0未评价，1差评，2好评
            if(state == 1){
                generateDrawable(binding.llNoLike,null,22,null,null,R.color.playfun_shape_radius_start_color,R.color.playfun_shape_radius_end_color);
                generateDrawable(binding.llLike,R.color.black,22,R.color.purple_text,1,null,null);
            }else if(state == 2){
                generateDrawable(binding.llLike,null,22,null,null,R.color.playfun_shape_radius_start_color,R.color.playfun_shape_radius_end_color);
                generateDrawable(binding.llNoLike,R.color.black,22,R.color.purple_text,1,null,null);
            }else{
                generateDrawable(binding.llLike,R.color.black,22,R.color.purple_text,1,null,null);
                generateDrawable(binding.llNoLike,R.color.black,22,R.color.purple_text,1,null,null);
            }
        });
    }

    void generateDrawable(View view,Integer drawableColor,Integer drawableCornersRadius,Integer drawableStrokeColor, Integer drawableStrokeWidth,Integer drawableStartColor, Integer drawableEndColor){
        CustomDrawableUtils.generateDrawable(view, getColorFromResource(drawableColor),
                drawableCornersRadius,null,null,null,null,
                getColorFromResource(drawableStartColor),getColorFromResource(drawableEndColor),drawableStrokeWidth,getColorFromResource(drawableStrokeColor));
    }

    Integer getColorFromResource(Integer resourceId) {
        if (resourceId==null) {
            return null;
        } else {
            return getContext().getResources().getColor(resourceId);
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
        viewModel.snapshotTimeState.set(true);
        //倒计时15秒，一次1秒
        downTimer = new CountDownTimer(6 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                    viewModel.snapshotTimeText.set((millisUntilFinished / 1000)+"s");
            }
            @Override
            public void onFinish() {
                stopTimer();
                //再次模糊图片
                GlideEngine.createGlideEngine().loadImage(getContext(), StringUtil.getFullImageUrl(mediaGalleryEditEntity.getSrcPath()), binding.imgContent, binding.imgLong,true, new GlideEngine.LoadProgressCallback() {
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
        //倒计时补可见
        viewModel.snapshotTimeState.set(false);
        //查询评价接口失败。不让继续评价
        if(viewModel.evaluationLikeEvent.getValue()!=null){
            //评价弹出
            viewModel.evaluationState.set(true);
        }
    }


}
