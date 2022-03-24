package com.dl.playfun.viewadapter;

import android.content.Context;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dl.playfun.R;
import com.dl.playfun.entity.AdItemEntity;
import com.dl.playfun.utils.StringUtil;
import com.youth.banner.loader.ImageLoader;

public class GlideImageLoader extends ImageLoader {
    final Handler myHandler = new Handler();

    @Override
    public void displayImage(Context context, Object o, ImageView imageView) {
        AdItemEntity adItemEntity = (AdItemEntity) o;
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                int dp = 0;
                if (adItemEntity.getLink() != null && adItemEntity.getLink().indexOf("dp") != -1) {
                    String title = adItemEntity.getLink();
                    title = title.substring(title.lastIndexOf("dp") + 2);
                    dp = (int) dp2px(context, Integer.parseInt(title));
                    if (layoutParams == null) {
                        layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp);
                    }
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    layoutParams.height = dp;
                    imageView.setLayoutParams(layoutParams);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }

            }
        });
        //Glide 加载图片简单用法
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