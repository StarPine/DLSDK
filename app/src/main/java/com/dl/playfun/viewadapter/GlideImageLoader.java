package com.dl.playfun.viewadapter;

import android.content.Context;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dl.playfun.entity.AdItemEntity;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.R;
import com.youth.banner.loader.ImageLoader;

public class GlideImageLoader extends ImageLoader {
    final Handler myHandler = new Handler();

    @Override
    public void displayImage(Context context, Object o, ImageView imageView) {
        AdItemEntity adItemEntity = (AdItemEntity) o;
        //Glide.with(context).load(StringUtil.getFullImageUrl(adItemEntity.getImg())).into(imageView);
        Glide.with(context)
                .load(StringUtil.getFullImageUrl(adItemEntity.getImg()))
                .apply(new RequestOptions()
                        .placeholder(context.getResources().getDrawable(R.drawable.error_img_banner))
                        .error(context.getResources().getDrawable(R.drawable.error_img_banner)))
                .into(imageView);
    }

    public float dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }
}