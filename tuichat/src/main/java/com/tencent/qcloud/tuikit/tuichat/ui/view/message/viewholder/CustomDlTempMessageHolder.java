package com.tencent.qcloud.tuikit.tuichat.ui.view.message.viewholder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tencent.custom.IMGsonUtils;
import com.tencent.custom.PhotoGalleryPayEntity;
import com.tencent.custom.tmp.CustomDlTempMessage;
import com.tencent.qcloud.tuicore.TUIThemeManager;
import com.tencent.qcloud.tuicore.custom.CustomConstants;
import com.tencent.qcloud.tuikit.tuichat.R;
import com.tencent.qcloud.tuikit.tuichat.TUIChatService;
import com.tencent.qcloud.tuikit.tuichat.bean.CustomImageMessage;
import com.tencent.qcloud.tuikit.tuichat.bean.message.TUIMessageBean;
import com.tencent.qcloud.tuikit.tuichat.util.TUIChatUtils;

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
                    imgLoad(itemView.getContext(), flTmpLayout, customImageMessageBean);
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
        View defaultView = View.inflate(context, R.layout.test_custom_message_layout1, null);
        TextView textView = defaultView.findViewById(R.id.test_custom_message_tv);
        TextView linkView = defaultView.findViewById(R.id.link_tv);
        linkView.setVisibility(View.GONE);
        if (!isSelf) {
            textView.setTextColor(textView.getResources().getColor(TUIThemeManager.getAttrResId(textView.getContext(), R.attr.chat_other_custom_msg_text_color)));
            linkView.setTextColor(textView.getResources().getColor(TUIThemeManager.getAttrResId(textView.getContext(), R.attr.chat_other_custom_msg_link_color)));
        } else {
            textView.setTextColor(textView.getResources().getColor(TUIThemeManager.getAttrResId(textView.getContext(), R.attr.chat_self_custom_msg_text_color)));
            linkView.setTextColor(textView.getResources().getColor(TUIThemeManager.getAttrResId(textView.getContext(), R.attr.chat_self_custom_msg_link_color)));
        }
        textView.setText(R.string.no_support_msg);
        msgContentFrame.setClickable(true);
        rootView.addView(defaultView);
    }
    //测试自定义图片渲染
    public void imgLoad(Context context, FrameLayout rootView, PhotoGalleryPayEntity customImageMessageBean){
        View customImageView = View.inflate(context, R.layout.tmp_message_photo_gallery_layout, null);
        ImageView imgContent = customImageView.findViewById(R.id.img_content);
        String imagePath = TUIChatUtils.getFullImageUrl(customImageMessageBean.getImgPath());
        Glide.with(TUIChatService.getAppContext())
                .asBitmap()
                .load(imagePath)
                .error(R.drawable.chat_custom_image_error)
                .centerCrop()
                .placeholder(R.drawable.chat_custom_image_load)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgContent);
        rootView.addView(customImageView);
    }

}
