package com.tencent.qcloud.tuikit.tuichat.ui.view.message.viewholder;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.tencent.custom.IMGsonUtils;
import com.tencent.custom.MvBlurTransformation;
import com.tencent.custom.PhotoGalleryPayEntity;
import com.tencent.custom.VideoGalleryPayEntity;
import com.tencent.custom.tmp.CustomDlTempMessage;
import com.tencent.qcloud.tuicore.TUIThemeManager;
import com.tencent.qcloud.tuicore.custom.CustomConstants;
import com.tencent.qcloud.tuicore.custom.CustomDrawableUtils;
import com.tencent.qcloud.tuicore.custom.entity.MediaGalleryEditEntity;
import com.tencent.qcloud.tuikit.tuichat.R;
import com.tencent.qcloud.tuikit.tuichat.TUIChatService;
import com.tencent.qcloud.tuikit.tuichat.bean.CustomImageMessage;
import com.tencent.qcloud.tuikit.tuichat.bean.message.TUIMessageBean;
import com.tencent.qcloud.tuikit.tuichat.util.TUIChatUtils;

import java.util.Map;

/**
 * Author: 彭石林
 * Time: 2022/9/8 11:49
 * Description: 自定义消息模板view渲染层
 */
public class CustomDlTempMessageHolder extends MessageContentHolder{
    private static final String TAG = "DlTempMessageHolder";
    private final FrameLayout flTmpLayout;
    public CustomDlTempMessageHolder(View itemView) {
        super(itemView);
        flTmpLayout = itemView.findViewById(R.id.fl_tmp_layout);
    }

    @Override
    public int getVariableLayout() {
        return R.layout.custom_dl_tmp_message_layout;
    }

    @Override
    public void layoutVariableViews(TUIMessageBean msg, int position) {
        if(flTmpLayout!=null && flTmpLayout.getChildCount()>1){
            flTmpLayout.removeAllViews();
            flTmpLayout.setBackgroundColor(Color.TRANSPARENT);
        }
        CustomDlTempMessage customDlTempMessage =  IMGsonUtils.fromJson(new String(msg.getCustomElemData()), CustomDlTempMessage.class);
        if(customDlTempMessage == null){
            defaultLayout(itemView.getContext(),flTmpLayout,msg.isSelf());
            return;
        }
        //判断模块
        if(customDlTempMessage.getContentBody()!=null && !TextUtils.isEmpty(customDlTempMessage.getContentBody().getMsgModuleName())){
            String moduleName = customDlTempMessage.getContentBody().getMsgModuleName();
            if(TextUtils.isEmpty(moduleName)){
                defaultLayout(itemView.getContext(),flTmpLayout,msg.isSelf());
                return;
            }
            CustomDlTempMessage.MsgBodyInfo msgModuleInfo = customDlTempMessage.getContentBody().getContentBody();
            //红包照片模块
            if (CustomConstants.MediaGallery.MODULE_NAME.equals(moduleName)) {
                //照片内容
                if(CustomConstants.MediaGallery.PHOTO_GALLERY.equals(msgModuleInfo.getCustomMsgType())){
                    PhotoGalleryPayEntity customImageMessageBean = IMGsonUtils.fromJson(IMGsonUtils.toJson(msgModuleInfo.getCustomMsgBody()),PhotoGalleryPayEntity.class);
                    LoadMediaGalleryPhoto(msg.getV2TIMMessage().getMsgID(),itemView.getContext(), flTmpLayout, customImageMessageBean,msg.getV2TIMMessage().getCloudCustomData());
                }else if(CustomConstants.MediaGallery.VIDEO_GALLERY.equals(msgModuleInfo.getCustomMsgType())){
                    VideoGalleryPayEntity customVideoMessageBean = IMGsonUtils.fromJson(IMGsonUtils.toJson(msgModuleInfo.getCustomMsgBody()),VideoGalleryPayEntity.class);
                    LoadMediaGalleryVideo(msg.getV2TIMMessage().getMsgID(),itemView.getContext(), flTmpLayout, customVideoMessageBean,msg.getV2TIMMessage().getCloudCustomData());
                }else{
                    //默认展示解析不出的模板提示
                    defaultLayout(itemView.getContext(),flTmpLayout,msg.isSelf());
                }
            }else{
                //默认展示解析不出的模板提示
                defaultLayout(itemView.getContext(),flTmpLayout,msg.isSelf());
            }
        }else{
            defaultLayout(itemView.getContext(),flTmpLayout,msg.isSelf());
        }

    }
    //默认消息模板
    public void defaultLayout(Context context,FrameLayout rootView,boolean isSelf){
        View defaultView = View.inflate(context, R.layout.tmp_message_default_layout, null);
        FrameLayout frameLayout = defaultView.findViewById(R.id.container);
        TextView textContent = defaultView.findViewById(R.id.tv_content);
        if(frameLayout!=null){
            if (properties.getLeftBubble() != null && properties.getLeftBubble().getConstantState() != null) {
                frameLayout.setBackground(properties.getLeftBubble().getConstantState().newDrawable());
            } else {
                frameLayout.setBackgroundResource(TUIThemeManager.getAttrResId(itemView.getContext(), R.attr.chat_bubble_other_bg));
            }
        }
        if (!isSelf) {
            textContent.setTextColor(textContent.getResources().getColor(TUIThemeManager.getAttrResId(textContent.getContext(), R.attr.chat_other_custom_msg_text_color)));
        } else {
            textContent.setTextColor(textContent.getResources().getColor(TUIThemeManager.getAttrResId(textContent.getContext(), R.attr.chat_self_custom_msg_text_color)));
        }

        String txt = getContext().getString(R.string.dl_tmp_default_text);
        String txt2 = getContext().getString(R.string.dl_tmp_default_text2);
        int whiteLength = txt.length() - txt2.length();
        SpannableString stringBuilder = new SpannableString(txt);
        ForegroundColorSpan whiteSpan = new ForegroundColorSpan(getContext().getResources().getColor(R.color.black));
        ForegroundColorSpan redSpan = new ForegroundColorSpan(getContext().getResources().getColor(R.color.purple_be63));
        stringBuilder.setSpan(whiteSpan, 0, whiteLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        stringBuilder.setSpan(redSpan, whiteLength, txt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        stringBuilder.setSpan(new UnderlineSpan(), whiteLength, txt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textContent.setText(stringBuilder);
        msgContentFrame.setClickable(true);
        rootView.addView(defaultView);
    }
    //测试自定义图片渲染
    public void LoadMediaGalleryPhoto(String IMKey,Context context, FrameLayout rootView, PhotoGalleryPayEntity photoGalleryPayEntity,String customElemData){
        Map<String,Object> mapData = null;
        Log.e(TAG,"当前穿透字段内容为:"+String.valueOf(mapData));
        View customImageView = View.inflate(context, R.layout.tmp_message_photo_gallery_layout, null);
        customImageView.setBackgroundColor(Color.TRANSPARENT);
        customImageView.setLayoutParams(new FrameLayout.LayoutParams(-2,-2));
        FrameLayout fLTlLayout = customImageView.findViewById(R.id.fl_tl_layout);
        //底部布局
        RelativeLayout rlLayout = customImageView.findViewById(R.id.rl_layout);
        boolean stateSnapshot = false;
        //付费照片
        if(photoGalleryPayEntity.isStatePhotoPay()){
            rlLayout.setVisibility(View.VISIBLE);
            //快照
            stateSnapshot  = photoGalleryPayEntity.isStateSnapshot();
            fLTlLayout.setVisibility(stateSnapshot ? View.VISIBLE : View.GONE);
            //当前收费价格
            TextView tvCoin = customImageView.findViewById(R.id.tv_coin);
            if(tvCoin!=null){
                if(photoGalleryPayEntity.getUnlockPrice()!=null && photoGalleryPayEntity.getUnlockPrice().intValue() > 0){
                    tvCoin.setVisibility(View.VISIBLE);
                    tvCoin.setText(String.valueOf(photoGalleryPayEntity.getUnlockPrice().intValue()));
                }else{
                    tvCoin.setVisibility(View.GONE);
                }
            }
            CustomDrawableUtils.generateDrawable(fLTlLayout, Color.parseColor("#717477"),
                    null,8,null,null,8,
                    null,null,null,null);
        }else{
            //非付费照片
            //   普通照片： stateSnapshot = false
            //   普通快照： stateSnapshot = true
            if(photoGalleryPayEntity.isStateSnapshot()){
                stateSnapshot = true;
                fLTlLayout.setVisibility(View.VISIBLE);
                CustomDrawableUtils.generateDrawable(fLTlLayout, Color.parseColor("#717477"),
                        null,8,null,null,8,
                        null,null,null,null);
            }else{
                //隐藏快照标识
                fLTlLayout.setVisibility(View.GONE);
            }
            rlLayout.setVisibility(View.GONE);
        }
        RequestOptions override;
        if(stateSnapshot){
            override = RequestOptions.bitmapTransform(new MvBlurTransformation(100)).override(dp2px(getContext(),86), dp2px(getContext(),154));
        }else{
            //通过RequestOptions扩展功能,override:采样率,因为ImageView就这么大,可以压缩图片,降低内存消耗
            override = new RequestOptions().override(dp2px(getContext(),86), dp2px(getContext(),154));
        }
        ImageView imgContent = customImageView.findViewById(R.id.img_content);
        String imagePath = TUIChatUtils.getFullImageUrl(photoGalleryPayEntity.getImgPath());
        Glide.with(TUIChatService.getAppContext())
                .load(imagePath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(override.error(R.drawable.chat_custom_image_error).placeholder(R.drawable.chat_custom_image_load))
                .into(imgContent);
        rootView.addView(customImageView);
        rootView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                MediaGalleryEditEntity mediaGalleryEditEntity = new MediaGalleryEditEntity();
                mediaGalleryEditEntity.setMsgKeyId(IMKey);
                mediaGalleryEditEntity.setSrcPath(photoGalleryPayEntity.getImgPath());
                mediaGalleryEditEntity.setVideoSetting(false);
                mediaGalleryEditEntity.setUnlockPrice(photoGalleryPayEntity.getUnlockPrice());
                mediaGalleryEditEntity.setStatePay(photoGalleryPayEntity.isStatePhotoPay());
                mediaGalleryEditEntity.setStateSnapshot(photoGalleryPayEntity.isStateSnapshot());
                onItemClickListener.onMediaGalleryClick(IMKey,mediaGalleryEditEntity);
            }
        });
    }

    //测试自定义图片渲染
    public void LoadMediaGalleryVideo(String IMKey,Context context, FrameLayout rootView, VideoGalleryPayEntity videoGalleryPayEntity,String customElemData){
        Map<String,Object> mapData = null;
        if(!TextUtils.isEmpty(customElemData)){
            mapData = IMGsonUtils.fromJson(customElemData, Map.class);
        }
        Log.e(TAG,"当前穿透字段内容为:"+String.valueOf(mapData));
        View customImageView = View.inflate(context, R.layout.tmp_message_video_gallery_layout, null);
        customImageView.setBackgroundColor(Color.TRANSPARENT);
        customImageView.setLayoutParams(new FrameLayout.LayoutParams(-2,-2));
        //底部布局
        RelativeLayout rlLayout = customImageView.findViewById(R.id.rl_layout);
        //付费视频
        if(videoGalleryPayEntity.isStateVideoPay()){
            rlLayout.setVisibility(View.VISIBLE);
            //当前收费价格
            TextView tvCoin = customImageView.findViewById(R.id.tv_coin);
            if(tvCoin!=null){
                if(videoGalleryPayEntity.getUnlockPrice()!=null && videoGalleryPayEntity.getUnlockPrice().intValue() > 0){
                    tvCoin.setVisibility(View.VISIBLE);
                    tvCoin.setText(String.valueOf(videoGalleryPayEntity.getUnlockPrice().intValue()));
                }else{
                    tvCoin.setVisibility(View.GONE);
                }
            }
        }else{
            //非付费视频
            rlLayout.setVisibility(View.GONE);
        }
        //设置图片圆角角度
        RoundedCorners roundedCorners = new RoundedCorners(dp2px(getContext(),8));
        //通过RequestOptions扩展功能,override:采样率,因为ImageView就这么大,可以压缩图片,降低内存消耗
        RequestOptions override = RequestOptions.bitmapTransform(roundedCorners).override(dp2px(getContext(),86), dp2px(getContext(),154));
        ImageView imgContent = customImageView.findViewById(R.id.img_content);
        String imagePath = TUIChatUtils.getFullImageUrl(videoGalleryPayEntity.getSrcPath());
        Glide.with(TUIChatService.getAppContext())
                .load(imagePath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(override.error(R.drawable.chat_custom_image_error).placeholder(R.drawable.chat_custom_image_load))
                .into(imgContent);
        rootView.addView(customImageView);
        rootView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                MediaGalleryEditEntity mediaGalleryEditEntity = new MediaGalleryEditEntity();
                mediaGalleryEditEntity.setMsgKeyId(IMKey);
                mediaGalleryEditEntity.setSrcPath(videoGalleryPayEntity.getSrcPath());
                mediaGalleryEditEntity.setVideoSetting(true);
                mediaGalleryEditEntity.setUnlockPrice(videoGalleryPayEntity.getUnlockPrice());
                mediaGalleryEditEntity.setStatePay(videoGalleryPayEntity.isStateVideoPay());
                onItemClickListener.onMediaGalleryClick(IMKey,mediaGalleryEditEntity);
            }
        });
    }

    private Context getContext(){
        return itemView.getContext();
    }
    private int dp2px(Context context, float dpValue) {
        final float densityScale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * densityScale + 0.5f);
    }
}
