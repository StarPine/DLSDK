package com.dl.playfun.viewadapter;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.dl.playfun.utils.StringUtil;

import me.goldze.mvvmhabit.utils.StringUtils;

/**
 * @author wulei
 */
public class ImageViewAdapter {
    @BindingAdapter(value = {"imagePath", "imageThumbPath", "LocalImagePath", "imagePlaceholderRes", "imageErrorPlaceholderRes", "addWaterMark"}, requireAll = false)
    public static void setImageUri(ImageView imageView, String imagePath, String imageThumbPath, String LocalImagePath, int imagePlaceholderRes, int imageErrorPlaceholderRes, boolean addWaterMark) {
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
                    url = StringUtil.getFullImageUrl(imagePath);
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

    @BindingAdapter(value = {"imagePathEd", "imagePathNo", "checked", "imagePlaceholderRes", "imageErrorPlaceholderRes"}, requireAll = false)
    public static void checkVipImg(ImageView imageView, String imagePathEd, String imagePathNo,boolean checked, int imagePlaceholderRes, int imageErrorPlaceholderRes){
        if(checked){
            Glide.with(imageView.getContext()).load(StringUtil.getFullImageUrl(imagePathEd))
                    .error(imageErrorPlaceholderRes)
                    .placeholder(imagePlaceholderRes)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
        }else{
            Glide.with(imageView.getContext()).load(StringUtil.getFullImageUrl(imagePathNo))
                    .error(imageErrorPlaceholderRes)
                    .placeholder(imagePlaceholderRes)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
        }
    }

    @BindingAdapter(value = {"stateActivated"}, requireAll = false)
    public static void setStateActivated(ImageView imageView, boolean stateActivated){
        imageView.setActivated(stateActivated);
    }


}
