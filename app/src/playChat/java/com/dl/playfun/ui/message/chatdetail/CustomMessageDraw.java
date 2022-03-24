package com.dl.playfun.ui.message.chatdetail;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.transformations.MvBlurTransformation;
import com.dl.playfun.utils.ChatUtils;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.widget.roundedimageview.RoundedImageView;
import com.google.gson.Gson;
import com.tencent.imsdk.v2.V2TIMCustomElem;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.qcloud.tuikit.tuichat.bean.CallModel;
import com.tencent.qcloud.tuikit.tuichat.bean.MessageInfo;
import com.tencent.qcloud.tuikit.tuichat.ui.interfaces.ICustomMessageViewGroup;
import com.tencent.qcloud.tuikit.tuichat.ui.interfaces.IOnCustomMessageDrawListener;

/**
 * @author litchi
 */
public class CustomMessageDraw implements IOnCustomMessageDrawListener {

    private static final int DEFAULT_MAX_SIZE = 540;

    private final CustomMessageListener customMessageListener;

    public CustomMessageDraw(CustomMessageListener customMessageListener) {
        this.customMessageListener = customMessageListener;
    }

    /**
     * 自定义消息渲染时，会调用该方法，本方法实现了自定义消息的创建，以及交互逻辑
     *
     * @param parent 自定义消息显示的父 View，需要把创建的自定义消息 View 添加到 parent 里
     * @param info   消息的具体信息
     */
    @Override
    public void onDraw(ICustomMessageViewGroup parent, MessageInfo info,int position) {
        try {
            // 获取到自定义消息的 JSON 数据
            if (info.getTimMessage().getElemType() != V2TIMMessage.V2TIM_ELEM_TYPE_CUSTOM) {
                return;
            }
            V2TIMMessage timMessage = info.getTimMessage();
            int senderUserID = ChatUtils.imUserIdToSystemUserId(timMessage.getSender());
            CallModel callModel = CallModel.convert2VideoCallData(timMessage);
            if (callModel == null) {
                V2TIMCustomElem elem = timMessage.getCustomElem();
                // 自定义的 JSON 数据，需要解析成 bean 实例
                String customMsg = new String(elem.getData());
                if (customMsg == null || customMsg.length() == 0) {
                    return;
                }
                final CustomMessageData customMessageData = new Gson().fromJson(customMsg, CustomMessageData.class);
                //   CustomMessageData customMessageData =  JSONObject.parseObject(customMsg,CustomMessageData.class);
                if (customMessageData == null) {
                    return;
                }
                customMessageData.setMsgId(timMessage.getMsgID());
                Log.d("CustomMessageDrwa", timMessage.getMsgID());
                customMessageData.setSenderUserID(senderUserID);
                if (customMessageData.type == CustomMessageData.TYPE_CUSTOM_IMAGE) {//自定义图片发送
                    View view = LayoutInflater.from(AppContext.instance()).inflate(R.layout.chat_custom_image_message, null, false);
                    parent.addMessageContentView(view);
                    ImageView content_image_iv = view.findViewById(R.id.content_image_iv);
                    Glide.with(AppContext.instance())
                            .asBitmap()
                            .load(StringUtil.getFullImageUrl(customMessageData.getImgPath()))
                            .error(R.drawable.chat_custom_image_error)
                            .placeholder(R.drawable.chat_custom_image_load)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(new ImageViewTarget<Bitmap>(content_image_iv) {
                                @Override
                                protected void setResource(@Nullable Bitmap resource) {
                                    if (resource != null) {
                                        content_image_iv.setLayoutParams(getImageParams(content_image_iv.getLayoutParams(), resource.getWidth(), resource.getHeight()));
                                        content_image_iv.setImageBitmap(resource);
                                    }
                                }
                            });
                    view.setTag(customMessageData);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CustomMessageData customMessageData1 = (CustomMessageData) v.getTag();
                            if (customMessageListener != null) {
                                customMessageListener.onImageClick(customMessageData1);
                            }
                        }
                    });
                }
                if (customMessageData.type == CustomMessageData.TYPE_LOCATION) {
                    View view = LayoutInflater.from(AppContext.instance()).inflate(R.layout.chat_location_message, null, false);
                    parent.addMessageContentView(view);
                    // 自定义消息 View 的实现，这里仅仅展示文本信息，并且实现超链接跳转
                    TextView textView = view.findViewById(R.id.tv_name);
                    textView.setText(customMessageData.getText());
                    view.setTag(customMessageData);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CustomMessageData customMessageData1 = (CustomMessageData) v.getTag();
                            if (customMessageListener != null) {
                                customMessageListener.onLocationMessageClick(customMessageData1);
                            }
                        }
                    });

                } else if (customMessageData.type == CustomMessageData.TYPE_RED_PACKAGE) {
                    View view = LayoutInflater.from(AppContext.instance()).inflate(R.layout.chat_red_package_message, null, false);
                    parent.addMessageContentView(view);
                } else if (customMessageData.type == CustomMessageData.TYPE_COIN_RED_PACKAGE) {
                    View view = LayoutInflater.from(AppContext.instance()).inflate(R.layout.chat_coin_red_package_message, null, false);
                    parent.addMessageContentView(view);

                    TextView textView = view.findViewById(R.id.tv_desc);
                    TextView tvState = view.findViewById(R.id.tv_state);
                    textView.setText(customMessageData.getText());
                    int status = AppContext.instance().appRepository.readCahtCustomMessageStatus(customMessageData.getMsgId());
                    if (status == 0) {
                        if (senderUserID == AppContext.instance().appRepository.readUserData().getId()) {
                            tvState.setText(R.string.chat_get_red_package_wait_rec);
                        } else {
                            tvState.setText(R.string.chat_get_red_package);
                        }
                    } else if (status == 2) {
                        //已领取
                        if (senderUserID == AppContext.instance().appRepository.readUserData().getId()) {
                            tvState.setText(R.string.redpackage_be_received);
                        } else {
                            tvState.setText(R.string.redpackage_already_received);
                        }
                    } else if (status == 3) {
                        //已过期
                        tvState.setText(R.string.redpackage_overdue);
                    }
                    view.setTag(customMessageData);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CustomMessageData customMessageData1 = (CustomMessageData) v.getTag();
                            if (customMessageListener != null) {
                                customMessageListener.onCoinRedPackageMessageClick(customMessageData1);
                            }
                        }
                    });
                } else if (customMessageData.type == CustomMessageData.TYPE_BURN) {
                    View view = LayoutInflater.from(AppContext.instance()).inflate(R.layout.chat_burn_message, null, false);
                    parent.addMessageContentView(view);

                    String imgPath = customMessageData.getImgPath();
                    RoundedImageView ivImg = view.findViewById(R.id.round_image_view);
                    TextView tvBurned = view.findViewById(R.id.tv_burned);
                    TextView tvType = view.findViewById(R.id.tv_type);
                    TextView tvCheck = view.findViewById(R.id.tv_check);
                    ImageView ivBurnTag = view.findViewById(R.id.iv_burn_tag);

                    ivImg.setBorderWidth((float) SizeUtils.dp2px(2));
                    int status = AppContext.instance().appRepository.readCahtCustomMessageStatus(customMessageData.getMsgId());
                    if (status == 1) {
                        ivImg.setBorderColor(ColorUtils.getColor(R.color.gray_light));
                        tvCheck.setVisibility(View.GONE);
                        tvType.setVisibility(View.GONE);
                        tvBurned.setVisibility(View.VISIBLE);
                        ivBurnTag.setBackgroundResource(R.drawable.photo_mark_gray_left_big);
                    } else {
                        ivImg.setBorderColor(ColorUtils.getColor(R.color.red_7c));
                        tvCheck.setVisibility(View.VISIBLE);
                        tvType.setVisibility(View.VISIBLE);
                        tvBurned.setVisibility(View.GONE);
                        ivBurnTag.setBackgroundResource(R.drawable.photo_mark_red_left_big);
                    }
                    view.setTag(customMessageData);

                    Glide.with(AppContext.instance().getApplicationContext())
                            .load(StringUtil.getFullThumbImageUrl(imgPath))
                            .apply(bitmapTransform(new MvBlurTransformation(50)))
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.wait_background)
                                    .error(R.drawable.default_placeholder_img))
                            .into(ivImg);
                    if (status != 1) {
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CustomMessageData customMessageData1 = (CustomMessageData) v.getTag();
                                if (customMessageListener != null) {
                                    customMessageListener.onBurnMessageClick(customMessageData1);
                                }
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface CustomMessageListener {

        void onLocationMessageClick(CustomMessageData customMessageData);

        void onCoinRedPackageMessageClick(CustomMessageData customMessageData);

        void onBurnMessageClick(CustomMessageData customMessageData);

        void onImageClick(CustomMessageData customMessageData);
    }

    private ViewGroup.LayoutParams getImageParams(ViewGroup.LayoutParams params, final int width, final int height) {
        if (width == 0 || height == 0) {
            return params;
        }
        if (width > height) {
            params.width = DEFAULT_MAX_SIZE;
            params.height = DEFAULT_MAX_SIZE * height / width;
        } else {
            params.width = DEFAULT_MAX_SIZE * width / height;
            params.height = DEFAULT_MAX_SIZE;
        }
        return params;
    }

}

