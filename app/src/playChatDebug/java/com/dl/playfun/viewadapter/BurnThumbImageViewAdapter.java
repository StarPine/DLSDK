package com.dl.playfun.viewadapter;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.dl.playfun.R;
import com.dl.playfun.transformations.MvBlurTransformation;
import com.dl.playfun.utils.StringUtil;

/**
 * @author wulei
 */
public class BurnThumbImageViewAdapter {
    @BindingAdapter(value = {"burnThumbImgPath", "isBurn", "burnStatus"}, requireAll = false)
    public static void setImageUri(ImageView imageView, String burnThumbImgPath, boolean isBurn, int burnStatus) {
        try {
            boolean isVideo = burnThumbImgPath.toLowerCase().endsWith(".mp4");
            String fullUrl = StringUtil.getFullThumbImageUrl(burnThumbImgPath);
            RequestManager requestManager = Glide.with(imageView.getContext());
            if (isVideo) {
                requestManager.setDefaultRequestOptions(
                        new RequestOptions()
                                .frame(1)
                                .centerCrop()
                );
            }
            if (isBurn) {
                requestManager.load(fullUrl)
                        .apply(bitmapTransform(new MvBlurTransformation(25)))
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.default_placeholder_img)
                                .error(R.drawable.default_placeholder_img))
                        .into(imageView);
            } else {
                requestManager.load(fullUrl)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.default_placeholder_img)
                                .error(R.drawable.default_placeholder_img))
                        .into(imageView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
