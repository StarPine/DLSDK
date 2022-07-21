package com.dl.playfun.viewadapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.utils.StringUtil;

import me.goldze.mvvmhabit.utils.StringUtils;

/**
 * @author wulei
 */
public class ImageViewAdapter {
    @BindingAdapter(value = {"imagePath", "imageThumbPath", "LocalImagePath", "imagePlaceholderRes", "imageErrorPlaceholderRes", "resizeH", "resizeW", "addWaterMark"}, requireAll = false)
    public static void setImageUri(ImageView imageView, String imagePath, String imageThumbPath, String LocalImagePath, int imagePlaceholderRes, int imageErrorPlaceholderRes,Integer resizeH,Integer resizeW, boolean addWaterMark) {

        try {
            String url = "";
            boolean isVideo = false;
            if (imageThumbPath != null && imageThumbPath.toLowerCase().endsWith(".mp4")) {
                isVideo = true;
            }
            if (!StringUtils.isEmpty(imageThumbPath)) {
                url = StringUtil.getFullThumbImageUrl(imageThumbPath);
            } else {
                if (addWaterMark) {
                    url = StringUtil.getFullImageWatermarkUrl(imagePath);
                } else {
                    url = StringUtil.getFullImageUrl(imagePath) + checkResizeProper(imageView.getContext(),resizeH, resizeW);
                }
            }
            RequestManager requestManager = Glide.with(imageView.getContext());
            if (isVideo) {
                requestManager.setDefaultRequestOptions(
                        new RequestOptions()
                                .frame(1)
                                .centerCrop()
                );
            }
            if (!StringUtil.isEmpty(LocalImagePath)) {
                requestManager.load(imageView.getContext().getResources().getIdentifier(LocalImagePath, "mipmap", imageView.getContext().getPackageName()))
                        .apply(new RequestOptions()
                                .placeholder(imagePlaceholderRes)
                                .error(imageErrorPlaceholderRes))
                        .into(imageView);
                return;
            }
            requestManager.load(url)
                    .apply(new RequestOptions()
                            .placeholder(imagePlaceholderRes)
                            .error(imageErrorPlaceholderRes))
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
    * @Desc TODO(根据宽高)
    * @author 彭石林
    * @parame [resizeH, resizeW]
    * @return java.lang.String
    * @Date 2022/7/21
    */
    private static String checkResizeProper(Context context,Integer resizeH,Integer resizeW){
        String value = "";
        if(resizeH == null && resizeW == null){
            return value;
        }else{
            int h = 0,w = 0;
            value = AppConfig.OSS_END_RESIZE;
            if(resizeH!=null){
                h = dp2px(context,resizeH);
            }
            if(resizeW!=null){
                w = dp2px(context,resizeW);
            }
            return String.format(value,String.valueOf(h == 0 ? w : h),String.valueOf(w == 0 ? h : w));
        }
    }

    @BindingAdapter(value = {"animateImage"}, requireAll = false)
    public static void CoustemAnimateImage(ImageView imageView, boolean animateImage) {
        if (animateImage) {
            final Animation animation = new AlphaAnimation(1, 0);
            animation.setDuration(1000);
            animation.setInterpolator(new LinearInterpolator());
            animation.setRepeatCount(Animation.INFINITE);
            animation.setRepeatMode(Animation.REVERSE);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            imageView.setAnimation(animation);
        }
    }

    @BindingAdapter(value = {"stateActivated"}, requireAll = false)
    public static void setStateActivated(ImageView imageView, boolean stateActivated){
        imageView.setActivated(stateActivated);
    }

    //针对附近页面单独提供。解决缩放问题
    @BindingAdapter(value = {"imageItemPath", "imageItemPlaceholderRes", "imageItemErrorPlaceholderRes"}, requireAll = false)
    public static void setHomeListItemImageUrl(ImageView imageView, String imagePath, int imageItemPlaceholderRes, int imageItemErrorPlaceholderRes){
        Glide.with(imageView.getContext()).asBitmap().load(StringUtil.getFullImageUrl(imagePath))
                .override(dp2px(imageView.getContext(),76),dp2px(imageView.getContext(),76))
                .error(imageItemErrorPlaceholderRes)
                .placeholder(imageItemPlaceholderRes)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new ImageViewTarget<Bitmap>(imageView) {
                    @Override
                    protected void setResource(@Nullable Bitmap resource) {
                        if (resource != null) {
                            imageView.setImageBitmap(resource);
                        }
                    }
                });
    }

    /***
     * 根据手机的分辨率从 dip 的单位 转成为 px(像素)
     * @param dpValue
     * @return
     */
    public static int dp2px(Context mContext, float dpValue) {
        if(mContext==null){
            return 0;
        }
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
