package com.tencent.qcloud.tuikit.tuichat.ui.view.message.viewholder;

import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tencent.coustom.ConfigUrl;
import com.tencent.coustom.CustomIMTextEntity;
import com.tencent.coustom.GiftEntity;
import com.tencent.coustom.IMGsonUtils;
import com.tencent.coustom.PhotoAlbumEntity;
import com.tencent.coustom.PhotoAlbumItemRecyclerAdapter;
import com.tencent.qcloud.tuikit.tuichat.R;
import com.tencent.qcloud.tuikit.tuichat.TUIChatConstants;
import com.tencent.qcloud.tuikit.tuichat.TUIChatService;
import com.tencent.qcloud.tuikit.tuichat.bean.message.TUIMessageBean;
import com.tencent.qcloud.tuikit.tuichat.component.face.FaceManager;
import com.tencent.qcloud.tuikit.tuichat.ui.view.MyImageSpan;
import com.tencent.qcloud.tuikit.tuichat.ui.view.message.MessageRecyclerView;
import com.tencent.qcloud.tuikit.tuichat.util.TUIChatUtils;

import java.util.Map;

/**
 * 修改备注：自定义json文本信息类型
 * @Name： PlayFun_Google
 * @Description：
 * @Author： liaosf
 * @Date： 2022/7/4 11:13
 *
 */
public class CustomTextMessageHolder extends TextMessageHolder {
    private ImageView mLeftView, mRightView;
    boolean isCharger = true;

    public CustomTextMessageHolder(View itemView) {
        super(itemView);
        mLeftView = itemView.findViewById(R.id.left_icon);
        mRightView = itemView.findViewById(R.id.right_icon);
    }

    @Override
    public int getVariableLayout() {
        return R.layout.message_adapter_content_json_text;
    }

    @Override
    public void layoutVariableViews(TUIMessageBean msg, int position) {

        initView();
        String extra = msg.getExtra();
        Log.i("starpine","custiom"+msg.getV2TIMMessage());
        if (TUIChatUtils.isJSON2(extra)) {//自定义json文本消息
            String type = TUIChatUtils.json2Massage(extra, "type");
            if (type == null){
                setContentLayoutVisibility(false);
                return;
            }
            switch (type) {
                case TUIChatConstants.CoustomMassageType.MESSAGE_GIFT:
                    setGiftMessageItemView(msg, extra);
                    break;
                case TUIChatConstants.CoustomMassageType.CHAT_EARNINGS:
                case TUIChatConstants.CoustomMassageType.MESSAGE_CUSTOM:
                case TUIChatConstants.CoustomMassageType.MESSAGE_TRACKING:
                    setCustomTypeItemView(extra,type);
                    break;
                case TUIChatConstants.CoustomMassageType.MESSAGE_TAG:
                    FaceManager.handlerEmojiText(msgBodyText, TUIChatUtils.json2Massage(extra, "text"), false);
                    setBackColor(msg);
                    break;
                case TUIChatConstants.CoustomMassageType.MESSAGE_PHOTO:
                    setPhotoItemView(extra,position,msg);
                    break;
                case TUIChatConstants.CoustomMassageType.MESSAGE_CALLINGBUSY:
                    setCallingBusyItemView(msg,extra);
                    break;
                case TUIChatConstants.CoustomMassageType.SEND_VIOLATION_MESSAGE:
                    setViolationItemView(msg,extra,position);
                    break;
                default:
                    setContentLayoutVisibility(false);
                    break;

            }
        } else {
            super.layoutVariableViews(msg, position);
        }
    }

    private void initView() {
        mLeftView.setVisibility(View.GONE);
        mRightView.setVisibility(View.GONE);
    }

    private void setViolationItemView(TUIMessageBean msg, String extra, int position) {
        FaceManager.handlerEmojiText(msgBodyText, TUIChatUtils.json2Massage(extra, "text"), false);
        setBackColor(msg);
        String busyContent = appContext.getString(R.string.custom_send_violation_message_tip);
        profitTip.setText(busyContent);
        profitTip.setVisibility(View.VISIBLE);
        statusImage.setVisibility(View.VISIBLE);
        sendingProgress.setVisibility(View.GONE);
    }

    private void setCallingBusyItemView(TUIMessageBean msg, String extra) {
        String busyContent = appContext.getString(R.string.custom_message_book_next_call);
        profitTip.setText(busyContent);
        profitTip.setVisibility(View.VISIBLE);
        Map callData = IMGsonUtils.fromJson(TUIChatUtils.json2Massage(extra, "data"), Map.class);
        int callType = Double.valueOf(String.valueOf(callData.get("callingType"))).intValue();
        if (properties.getChatContextFontSize() != 0) {
            msgBodyText.setTextSize(properties.getChatContextFontSize());
        }
        if (msg.isSelf()) {
            mRightView.setBackgroundResource(callType == 1 ?
                    R.drawable.custom_audio_right_img_2 : R.drawable.custom_video_right_img_1);
            mRightView.setVisibility(View.VISIBLE);
            msgBodyText.setText(appContext.getString(R.string.custom_message_other_busy));
            if (properties.getRightChatContentFontColor() != 0) {
                msgBodyText.setTextColor(properties.getRightChatContentFontColor());
            }
        } else {
            mLeftView.setBackgroundResource(callType == 1 ?
                    R.drawable.custom_audio_left_img_2 : R.drawable.custom_video_left_img_1);
            mLeftView.setVisibility(View.VISIBLE);
            msgBodyText.setText(appContext.getString(R.string.custom_message_busy_missed));
            if (properties.getLeftChatContentFontColor() != 0) {
                msgBodyText.setTextColor(properties.getLeftChatContentFontColor());
            }
        }
    }

    private void setPhotoItemView(String extra, int position, TUIMessageBean msg) {
        hideTimeView();
        View photoView = View.inflate(appContext, R.layout.message_adapter_content_photo, null);

        ImageView ic_vip = photoView.findViewById(R.id.iv_vip);
        ImageView ic_certification = photoView.findViewById(R.id.iv_certification);
        TextView conversation_title = photoView.findViewById(R.id.conversation_title);
        ImageView photo_album_img = photoView.findViewById(R.id.photo_album_img);
        PhotoAlbumEntity photoAlbumEntity = IMGsonUtils.fromJson(TUIChatUtils.json2Massage(extra, "data"), PhotoAlbumEntity.class);
        if (photoAlbumEntity != null) {
            int sex = photoAlbumEntity.getSex();
            int isVip = photoAlbumEntity.getIsVip();
            int certification = photoAlbumEntity.getCertification();
            conversation_title.setText(photoAlbumEntity.getNickname());
            //加载头像
            Glide.with(TUIChatService.getAppContext()).load(ConfigUrl.getFullImageUrl(photoAlbumEntity.getAvatar()))
                    .error(R.drawable.photo_album_img_default)
                    .placeholder(R.drawable.photo_album_img_default)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(photo_album_img);
            if (sex == 1) {
                ic_vip.setImageResource(R.drawable.ic_vip);
                ic_vip.setVisibility(isVip == 1 ? View.VISIBLE : View.GONE);
                if (certification == 1) {
                    ic_certification.setVisibility(View.VISIBLE);
                } else {

                }
            } else {//女性用户
                ic_vip.setImageResource(R.drawable.ic_goddess);
                ic_vip.setVisibility(isVip == 1 ? View.VISIBLE : View.GONE);
                if (certification == 1) {
                    ic_certification.setVisibility(isVip == 1 ? View.GONE : View.VISIBLE);
                } else {
                    ic_certification.setVisibility(View.GONE);
                }
            }
            RecyclerView recyclerView = photoView.findViewById(R.id.photo_album_rcv);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(photoView.getContext());
            linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            PhotoAlbumItemRecyclerAdapter adapter = new PhotoAlbumItemRecyclerAdapter(photoAlbumEntity.getImg());
            recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener((v, pos, itemEntity) -> {
                if (onItemClickListener != null)
                    onItemClickListener.clickToUserMain();
            });
            photoView.findViewById(R.id.photo_album_layout).setOnClickListener(v -> {
                if (onItemClickListener != null)
                    onItemClickListener.clickToUserMain();
            });
        }
        customJsonMsgContentFrame.addView(photoView);
    }

    private void setCustomTypeItemView(String extra, String type) {
        hideWithAvatarView();
        View tipView = getTipItemView();
        TextView custom_tip_text = tipView.findViewById(R.id.custom_tip_text);
        CustomIMTextEntity customIMTextEntity = IMGsonUtils.fromJson(TUIChatUtils.json2Massage(extra, "data"), CustomIMTextEntity.class);
        if (customIMTextEntity != null) {

            //系统提示
            if (!TextUtils.isEmpty(customIMTextEntity.getContent())) {
                custom_tip_text.setText(Html.fromHtml(customIMTextEntity.getContent()));
            }

            //收益相关
            if (type.equals(TUIChatConstants.CoustomMassageType.CHAT_EARNINGS)){
                //收益退回提示
                if (customIMTextEntity.getIsRefundMoney() != null) {
                    if (isCharger) {//收益方
                        custom_tip_text.setText(appContext.getString(R.string.custom_message_txt_girl));
                    }else {//付费方
                        custom_tip_text.setText(appContext.getString(R.string.custom_message_txt_male));
                    }
                }else {
                    //过滤收益消息--隐藏
                    setContentLayoutVisibility(false);
                }
            }

            //余额不足提示
            if (customIMTextEntity.getIsRemindPay() != null && customIMTextEntity.getIsRemindPay().intValue() > 0) {
                tipView = View.inflate(appContext, R.layout.custom_not_sufficient_view, null);
                LinearLayout male_hint_layout = tipView.findViewById(R.id.male_hint_layout);
                male_hint_layout.setOnClickListener(v -> {
                    if (onItemClickListener != null)
                        onItemClickListener.onClickDialogRechargeShow();
                });
            }

            customJsonMsgContentFrame.addView(tipView);
        }
    }

    private View getTipItemView() {
        return View.inflate(appContext, R.layout.message_adapter_content_server_tip, null);
    }

    /**
     * 礼物消息
     *  @param msg
     * @param extra
     */
    private void setGiftMessageItemView(TUIMessageBean msg, String extra) {
        msgArea.setBackground(null);
        GiftEntity giftEntity = IMGsonUtils.fromJson(String.valueOf
                (TUIChatUtils.json2Massage(extra, "data")), GiftEntity.class);
        if (giftEntity != null) {
            View giftView = View.inflate(appContext, R.layout.custom_gift_view, null);
            RelativeLayout custom_gift_layout = giftView.findViewById(R.id.custom_gift_layout);
            ImageView gift_img = giftView.findViewById(R.id.gift_img);
            TextView gift_text = giftView.findViewById(R.id.gift_text);
            TextView gift_title = giftView.findViewById(R.id.gift_title);
            TextView custom_gift_hint_text = giftView.findViewById(R.id.custom_gift_hint_text);

            if (msg.isSelf()) {
                gift_title.setText(appContext.getString(R.string.custom_gift_left_title));
                gift_title.setTextColor(appContext.getResources().getColor(R.color.gift_right_color));
                gift_text.setTextColor(appContext.getResources().getColor(R.color.gift_right_txt_color));
                custom_gift_layout.setBackground(appContext.getDrawable(R.drawable.custom_right_gift_backdrop));
            } else {
                gift_title.setText(appContext.getString(R.string.custom_gift_right_title));
                gift_title.setTextColor(appContext.getResources().getColor(R.color.gift_left_color));
                gift_text.setTextColor(appContext.getResources().getColor(R.color.gift_left_txt_color));
                custom_gift_layout.setBackground(appContext.getDrawable(R.drawable.custom_left_gift_backdrop));
                if (giftEntity.getProfitTwd() != null) {
                    double total = giftEntity.getProfitTwd() * giftEntity.getAmount();
                    String custom_message_txt2 = appContext.getString(R.string.profit);
                    if (!MessageRecyclerView.isCertification()) {
                        custom_message_txt2 = appContext.getString(R.string.custom_message_txt2_test2);
                        custom_gift_hint_text.setOnClickListener(v -> {
                            if (onItemClickListener != null)
                                onItemClickListener.onClickCustomText();
                        });
                    }
                    SpannableString iconSpannable = matcherSearchText("#A72DFE", String.format(custom_message_txt2,
                            String.format("%.2f", total)), appContext.getString(R.string.custom_message_txt1_key));
                    iconSpannable.setSpan(new MyImageSpan(appContext, R.drawable.icon_crystal),
                            0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    custom_gift_hint_text.setText(iconSpannable);
                }
            }
            if (!MessageRecyclerView.isFlagTipMoney() || msg.isSelf()) {
                custom_gift_hint_text.setVisibility(View.GONE);
            } else {
                custom_gift_hint_text.setVisibility(View.VISIBLE);
            }
            gift_text.setText(giftEntity.getTitle() + " x" + giftEntity.getAmount());
            Glide.with(TUIChatService.getAppContext())
                    .load(ConfigUrl.getFullImageUrl(giftEntity.getImgPath()))
                    .error(R.drawable.photo_album_rcv_item_def_img)
                    .placeholder(R.drawable.photo_album_rcv_item_def_img)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(gift_img);
            msgContentReservFrame.addView(giftView);
            msgContentFrame.setVisibility(View.GONE);
        }
    }

    public void setBackColor(TUIMessageBean msg) {
        if (properties.getChatContextFontSize() != 0) {
            msgBodyText.setTextSize(properties.getChatContextFontSize());
        }
        if (msg.isSelf()) {
            if (properties.getRightChatContentFontColor() != 0) {
                msgBodyText.setTextColor(properties.getRightChatContentFontColor());
            }
        } else {
            if (properties.getLeftChatContentFontColor() != 0) {
                msgBodyText.setTextColor(properties.getLeftChatContentFontColor());
            }
        }
    }

}
