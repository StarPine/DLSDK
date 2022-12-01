package com.dl.playfun.viewadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;

import com.blankj.utilcode.util.SizeUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.dl.playfun.R;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.utils.StringUtil;

import jp.wasabeef.glide.transformations.BlurTransformation;
import me.goldze.mvvmhabit.utils.StringUtils;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

/**
 * @author wulei
 */
public class ImageViewAdapter {
    @SuppressLint("CheckResult")
    @BindingAdapter(value = {"imagePath", "imageThumbPath", "LocalImagePath", "imagePlaceholderRes", "imageErrorPlaceholderRes", "resizeH", "resizeW","imgRadius", "addWaterMark"}, requireAll = false)
    public static void setImageUri(ImageView imageView, String imagePath, String imageThumbPath, String LocalImagePath, int imagePlaceholderRes, int imageErrorPlaceholderRes,Integer resizeH,Integer resizeW,Integer imgRadius, boolean addWaterMark) {

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
            RequestOptions overrideOptions;
            if(imgRadius!=null){
                //设置图片圆角角度
                RoundedCorners roundedCorners = new RoundedCorners(dp2px(imageView.getContext(),imgRadius));
                overrideOptions = RequestOptions.bitmapTransform(roundedCorners);
            }else{
                overrideOptions = new RequestOptions();
                if(resizeW != null && resizeW > 0 && resizeH != null && resizeH > 0){
                    //通过RequestOptions扩展功能,override:采样率,因为ImageView就这么大,可以压缩图片,降低内存消耗
                    overrideOptions.override(dp2px(imageView.getContext(),resizeW), dp2px(imageView.getContext(),resizeH));
                }

            }



            RequestManager requestManager = Glide.with(imageView.getContext());
            if (isVideo) {
                requestManager.setDefaultRequestOptions(
                        overrideOptions
                                .frame(1)
                                .centerCrop()
                );
            }
            if (!StringUtil.isEmpty(LocalImagePath)) {
                requestManager.load(imageView.getContext().getResources().getIdentifier(LocalImagePath, "mipmap", imageView.getContext().getPackageName()))
                        .apply(overrideOptions
                                .placeholder(imagePlaceholderRes)
                                .error(imageErrorPlaceholderRes))
                        .into(imageView);
                return;
            }
            requestManager.load(url)
                    .apply(overrideOptions
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

    @BindingAdapter(value = {"imageBlurPath","isBlur","addWaterMark"}, requireAll = false)
    public static void setImageBlurPath(ImageView imageView, String imageBlurPath, boolean isBlur, boolean addWaterMark) {
        try {
            String url = "";
            if (!StringUtils.isEmpty(imageBlurPath)) {
                url = StringUtil.getFullThumbImageUrl(imageBlurPath);
            }
            if (addWaterMark) {
                url = StringUtil.getFullImageWatermarkUrl(imageBlurPath);
            }
            if (isBlur){
                Glide.with(imageView.getContext())
                        .load(url)
                        .apply(bitmapTransform(new BlurTransformation(100)))
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.black_background)
                                .error(R.drawable.black_background))
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(imageView);
            }else {
                Glide.with(imageView.getContext())
                        .load(url)
                        .fitCenter()
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.black_background)
                                .error(R.drawable.black_background))
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(imageView);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
    @SuppressLint("CheckResult")
    @BindingAdapter(value = {"imageItemPath", "imageItemPlaceholderRes", "imageItemErrorPlaceholderRes", "resizeH", "resizeW"}, requireAll = true)
    public static void setHomeListItemImageUrl(ImageView imageView, String imagePath, int imageItemPlaceholderRes, int imageItemErrorPlaceholderRes,Integer resizeH,Integer resizeW){
        String oosResize = checkResizeProper(imageView.getContext(),resizeH, resizeW);
        RequestOptions overrideOptions = new RequestOptions();
        if(resizeW != null && resizeW > 0 && resizeH != null && resizeH > 0){
            //通过RequestOptions扩展功能,override:采样率,因为ImageView就这么大,可以压缩图片,降低内存消耗
            overrideOptions.override(dp2px(imageView.getContext(),resizeW), dp2px(imageView.getContext(),resizeH));
        }
        Glide.with(imageView.getContext()).load(StringUtil.getFullImageUrl(imagePath)+oosResize)
                .error(imageItemErrorPlaceholderRes)
                .placeholder(imageItemPlaceholderRes)
                .apply(overrideOptions)
                .into(imageView);
    }

    //针对相册页面临时提供 解决本地文件预览问题
    @BindingAdapter(value = {"imageItemPhotoPath", "imageItemPhotoPlaceholderRes", "imageItemPhotoErrorPlaceholderRes","isLocalFile"}, requireAll = true)
    public static void setPhotoItemImageUrl(ImageView imageView, String imageItemPhotoPath, int imageItemPhotoPlaceholderRes, int imageItemPhotoErrorPlaceholderRes,Boolean isLocalFile){
        if(!isLocalFile){
            Glide.with(imageView.getContext()).load(StringUtil.getFullImageUrl(imageItemPhotoPath))
                    .fitCenter()//防止图片被拉伸
                    .error(imageItemPhotoErrorPlaceholderRes)
                    .placeholder(imageItemPhotoPlaceholderRes)
                    .into(imageView);
        }else{
            Glide.with(imageView.getContext()).load(imageItemPhotoPath)
                    .fitCenter()//防止图片被拉伸
                    .error(imageItemPhotoErrorPlaceholderRes)
                    .placeholder(imageItemPhotoPlaceholderRes)
                    .into(imageView);
        }

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
