package com.dl.playfun.ui.message.mediagallery.photo;

import static com.blankj.utilcode.util.SnackbarUtils.dismiss;
import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
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
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.utils.StringUtil;
import com.luck.picture.lib.tools.MediaUtils;
import com.luck.picture.lib.widget.longimage.ImageSource;
import com.luck.picture.lib.widget.longimage.ImageViewState;
import com.luck.picture.lib.widget.longimage.SubsamplingScaleImageView;
import com.tencent.qcloud.tuicore.custom.entity.MediaGalleryEditEntity;

/**
 * Author: 彭石林
 * Time: 2022/9/14 11:20
 * Description: This is MediaGalleryPhotoPayActivity
 */
public class MediaGalleryPhotoPayActivity extends BaseActivity<ActivityMediaGalleryPhotoBinding,MediaGalleryPhotoPayViewModel> {
    private static final String TAG = "MediaGalleryPhotoPay";
    private MediaGalleryEditEntity mediaGalleryEditEntity;
    @Override
    public int initContentView(Bundle savedInstanceState) {
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
    public void initData() {
        super.initData();
        Log.e(TAG,"当前传递的内容为："+String.valueOf(mediaGalleryEditEntity==null));
        viewModel.evaluationState.set(true);
        if(mediaGalleryEditEntity!=null){
            Log.e(TAG,"当前传递的内容为："+String.valueOf(mediaGalleryEditEntity.toString()));
            //快照
            if(mediaGalleryEditEntity.isStateSnapshot()){
                showHud();
                Log.e(TAG,"当前oss文件地址："+StringUtil.getFullImageUrl(mediaGalleryEditEntity.getSrcPath()));
                Glide.with(getContext())
                        .asBitmap()
                        .apply(bitmapTransform(new MvBlurTransformation(85)))
                        .load(StringUtil.getFullImageUrl(mediaGalleryEditEntity.getSrcPath()))
                        .into(new ImageViewTarget<Bitmap>(binding.imgContent) {
                            @Override
                            protected void setResource(@Nullable Bitmap resource) {
                                dismissHud();
                                binding.rlContainer.setVisibility(View.VISIBLE);
                                binding.imgFuzzy.setVisibility(View.GONE);
                                if (resource != null) {
                                    boolean eqLongImage = MediaUtils.isLongImg(resource.getWidth(),
                                            resource.getHeight());
                                    binding.imgContent.setVisibility(eqLongImage ? View.VISIBLE : View.GONE);
                                    binding.imgLong.setVisibility(eqLongImage ? View.GONE : View.VISIBLE);
                                    if (eqLongImage) {
                                        // 加载长图
                                        binding.imgLong.setQuickScaleEnabled(true);
                                        binding.imgLong.setZoomEnabled(true);
                                        binding.imgLong.setPanEnabled(true);
                                        binding.imgLong.setDoubleTapZoomDuration(100);
                                        binding.imgLong.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
                                        binding.imgLong.setDoubleTapZoomDpi(SubsamplingScaleImageView.ZOOM_FOCUS_CENTER);
                                        binding.imgLong.setImage(ImageSource.bitmap(resource),
                                                new ImageViewState(0, new PointF(0, 0), 0));
                                    } else {
                                        // 普通图片
                                        binding.imgContent.setImageBitmap(resource);
                                    }
                                }
                            }
                        });
            }else{
                Log.e(TAG,"当前oss文件地址："+StringUtil.getFullImageUrl(mediaGalleryEditEntity.getSrcPath()));
                Glide.with(getContext())
                        .asBitmap()
                        .load(StringUtil.getFullImageUrl(mediaGalleryEditEntity.getSrcPath()))
                        .into(new ImageViewTarget<Bitmap>(binding.imgContent) {
                            @Override
                            protected void setResource(@Nullable Bitmap resource) {
                                dismissHud();
                                Log.e(TAG,"当前图片资源："+String.valueOf(resource==null));
                                binding.rlContainer.setVisibility(View.VISIBLE);
                                binding.imgFuzzy.setVisibility(View.GONE);
                                if (resource != null) {
                                    boolean eqLongImage = MediaUtils.isLongImg(resource.getWidth(),
                                            resource.getHeight());
                                    binding.imgContent.setVisibility(eqLongImage ? View.VISIBLE : View.GONE);
                                    binding.imgLong.setVisibility(eqLongImage ? View.GONE : View.VISIBLE);
                                    if (eqLongImage) {
                                        // 加载长图
                                        binding.imgLong.setQuickScaleEnabled(true);
                                        binding.imgLong.setZoomEnabled(true);
                                        binding.imgLong.setPanEnabled(true);
                                        binding.imgLong.setDoubleTapZoomDuration(100);
                                        binding.imgLong.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
                                        binding.imgLong.setDoubleTapZoomDpi(SubsamplingScaleImageView.ZOOM_FOCUS_CENTER);
                                        binding.imgLong.setImage(ImageSource.bitmap(resource),
                                                new ImageViewState(0, new PointF(0, 0), 0));
                                    } else {
                                        // 普通图片
                                        binding.imgContent.setImageBitmap(resource);
                                    }
                                }
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


    private void loadImageCallback() {
        Glide.with(getContext())
                .asBitmap()
                .load(path)
                .into(new ImageViewTarget<Bitmap>(binding.cropImageView) {
                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                    }

                    @Override
                    protected void setResource(@Nullable Bitmap resource) {
                        if (resource != null) {
                            // 普通图片
                            int imgWidth = resource.getWidth();
                            int ImgHeight = resource.getHeight();
                            $layoutParams.width = imgWidth;
                            $layoutParams.height = ImgHeight;
                            binding.cropImageView.setLayoutParams($layoutParams);
                            binding.cropImageView.setImageBitmap(resource);
                        }
                    }
                });
    }


}
