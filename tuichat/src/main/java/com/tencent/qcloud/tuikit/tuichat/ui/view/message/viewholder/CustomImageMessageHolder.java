package com.tencent.qcloud.tuikit.tuichat.ui.view.message.viewholder;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tencent.qcloud.tuicore.component.imageEngine.impl.GlideEngine;
import com.tencent.qcloud.tuikit.tuichat.R;
import com.tencent.qcloud.tuikit.tuichat.TUIChatConstants;
import com.tencent.qcloud.tuikit.tuichat.TUIChatService;
import com.tencent.qcloud.tuikit.tuichat.bean.message.CustomImageMessageBean;
import com.tencent.qcloud.tuikit.tuichat.bean.message.TUIMessageBean;
import com.tencent.qcloud.tuikit.tuichat.component.imagevideoscan.ImageVideoScanActivity;

import java.io.Serializable;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * @Name： PlayFun_Google
 * @Description：
 * @Author： liaosf
 * @Date： 2022/6/29 15:47
 * 修改备注：DL add 自定义图片holder
 */
public class CustomImageMessageHolder extends MessageContentHolder{

    private final ImageView customImage;
    private static final int DEFAULT_MAX_SIZE = 540;
    private static final int DEFAULT_RADIUS = 10;


    public CustomImageMessageHolder(View itemView) {
        super(itemView);
        customImage = rootView.findViewById(R.id.iv_custom_image);
    }

    @Override
    public void layoutVariableViews(TUIMessageBean msg, int position) {
        msgArea.setBackground(null);
        customImage.setLayoutParams(getImageParams(customImage.getLayoutParams(), (CustomImageMessageBean) msg));
        String imagePath = ((CustomImageMessageBean) msg).getDataPath();
        GlideEngine.loadCornerImageWithoutPlaceHolder(customImage, imagePath, null, DEFAULT_RADIUS);

        customImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onImageClick(msg);
                }
            }
        });

        customImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onMessageLongClick(view, position, msg);
                }
                return true;
            }
        });
    }

    @Override
    public int getVariableLayout() {
        return R.layout.custom_image_message_layout;
    }

    private ViewGroup.LayoutParams getImageParams(ViewGroup.LayoutParams params, final CustomImageMessageBean msg) {
        if (msg.getImgWidth() == 0 || msg.getImgHeight() == 0) {
            return params;
        }
        if (msg.getImgWidth() > msg.getImgHeight()) {
            params.width = DEFAULT_MAX_SIZE;
            params.height = DEFAULT_MAX_SIZE * msg.getImgHeight() / msg.getImgWidth();
        } else {
            params.width = DEFAULT_MAX_SIZE * msg.getImgWidth() / msg.getImgHeight();
            params.height = DEFAULT_MAX_SIZE;
        }
        return params;
    }

}
