package com.dl.playfun.viewadapter;

import android.content.Context;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.loader.ImageLoader;

import java.util.List;

/**
 * @author wulei
 */
public class BannerPicAdapter {
    @BindingAdapter(value = {"BannerPicData"}, requireAll = false)
    public static void setImageUri(Banner banner, List<Integer> pics) {
        try {
            //设置banner样式
            banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
            //设置图片加载器
            banner.setImageLoader(new ImageLoader() {
                @Override
                public void displayImage(Context context, Object path, ImageView imageView) {
                    imageView.setImageResource(Integer.parseInt(path.toString()));
                }
            });
            //设置图片集合
            banner.setImages(pics);
            //设置banner动画效果
            banner.setBannerAnimation(Transformer.Default);
            //设置自动轮播，默认为true
            banner.isAutoPlay(false);
            //设置轮播时间
//            banner.setDelayTime(2500);
            //设置指示器位置（当banner模式中有指示器时）
            banner.setIndicatorGravity(BannerConfig.CENTER);
            //banner设置方法全部调用完毕时最后调用
            banner.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
