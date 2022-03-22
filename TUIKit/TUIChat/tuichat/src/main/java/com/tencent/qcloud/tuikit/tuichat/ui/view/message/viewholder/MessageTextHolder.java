package com.tencent.qcloud.tuikit.tuichat.ui.view.message.viewholder;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.tencent.coustom.ConfigUrl;
import com.tencent.coustom.CustomIMTextEntity;
import com.tencent.coustom.EvaluateItemEntity;
import com.tencent.coustom.GiftEntity;
import com.tencent.coustom.IMGsonUtils;
import com.tencent.coustom.PhotoAlbumEntity;
import com.tencent.coustom.PhotoAlbumItemEntity;
import com.tencent.coustom.PhotoAlbumItemRecyclerAdapter;
import com.tencent.qcloud.tuikit.tuichat.R;
import com.tencent.qcloud.tuikit.tuichat.TUIChatService;
import com.tencent.qcloud.tuikit.tuichat.bean.MessageInfo;
import com.tencent.qcloud.tuikit.tuichat.component.face.FaceManager;
import com.tencent.qcloud.tuikit.tuichat.ui.view.message.MessageRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageTextHolder extends MessageContentHolder {

    private TextView msgBodyText;
    private TextView chat_tips_tv;
    private FrameLayout msg_content_fl_custom;

    public MessageTextHolder(View itemView) {
        super(itemView);
    }

    public static boolean isJSON2(String str) {
        boolean result = false;
        try {
            new Gson().fromJson(str, Map.class);
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return result;

    }

    /**
     * @return java.util.List<T>
     * @Desc TODO(将字符串转成List集合)
     * @author 彭石林
     * @parame [jsonString, cls]
     * @Date 2021/8/13
     */
    public static <T> List<T> getObjectList(String jsonString, Class<T> cls) {
        List<T> list = new ArrayList<T>();
        try {
            Gson gson = new Gson();
            JsonArray arry = new JsonParser().parse(jsonString).getAsJsonArray();
            for (JsonElement jsonElement : arry) {
                list.add(gson.fromJson(jsonElement, cls));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public int getVariableLayout() {
        return R.layout.message_adapter_content_text;
    }

    @Override
    public void initVariableViews() {
        msgBodyText = rootView.findViewById(R.id.msg_body_tv);
        chat_tips_tv = rootView.findViewById(R.id.chat_tips_tv);
        msg_content_fl_custom = rootView.findViewById(R.id.msg_content_fl_custom);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void layoutVariableViews(MessageInfo msg, int position) {
        msgBodyText.setVisibility(View.VISIBLE);
        if (msg.getExtra() != null) {
            String text = String.valueOf(msg.getExtra());
            if (isJSON2(text) && text.indexOf("type") != -1) {//做自定义通知判断
                Map<String, Object> map_data = new Gson().fromJson(String.valueOf(msg.getExtra()), Map.class);
                if (map_data != null && map_data.get("type") != null && map_data.get("type").equals("toast_local")) {
                    rootView.findViewById(R.id.custom_error_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.photo_album_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.im_evaluation_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.custom_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.user_content).setVisibility(View.GONE);
                    rootView.findViewById(R.id.full_toast).setVisibility(View.VISIBLE);
                    chat_tips_tv.setTextSize(properties.getChatContextFontSize());
                    if (map_data.get("status") != null && map_data.get("status").equals("3") || map_data.get("status").equals("2")) {//发送真人认证提示 :已经发送过
                        String value = map_data.get("text").toString();
                        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(value);
                        ForegroundColorSpan blueSpan = new ForegroundColorSpan(Color.BLUE);
                        stringBuilder.setSpan(blueSpan, value.length() - 4, value.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        chat_tips_tv.setText(stringBuilder);
                    } else {
                        FaceManager.handlerEmojiText(chat_tips_tv, map_data.get("text").toString(), false);
                    }
                    chat_tips_tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onItemLongClickListener.onToastVipText(msg);
                        }
                    });
                } else if (map_data != null && map_data.get("type") != null && map_data.get("type").equals("message_tag")) {//推送用户标签
                    rootView.findViewById(R.id.custom_error_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.photo_album_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.im_evaluation_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.user_content).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.custom_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.full_toast).setVisibility(View.GONE);
                    FaceManager.handlerEmojiText(msgBodyText, String.valueOf(map_data.get("text")), false);
                    setBackColor(msg);
                } else if (map_data != null && map_data.get("type") != null && map_data.get("type").equals("message_photo")) {//弹窗相册
                    rootView.findViewById(R.id.custom_error_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.full_toast).setVisibility(View.GONE);
                    rootView.findViewById(R.id.user_content).setVisibility(View.GONE);
                    rootView.findViewById(R.id.im_evaluation_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.custom_layout).setVisibility(View.GONE);
                    msg_content_fl_custom.setVisibility(View.GONE);
                    msgContentFrame.setVisibility(View.GONE);
                    rootView.findViewById(R.id.photo_album_layout).setVisibility(View.VISIBLE);
                    if (map_data.get("data") != null) {
                        PhotoAlbumEntity photoAlbumEntity = IMGsonUtils.fromJson(String.valueOf(map_data.get("data")), PhotoAlbumEntity.class);//new Gson().fromJson(String.valueOf(map_data.get("data")), PhotoAlbumEntity.class);
                        if (photoAlbumEntity != null) {
                            int sex = photoAlbumEntity.getSex();
                            int isVip = photoAlbumEntity.getIsVip();
                            int certification = photoAlbumEntity.getCertification();
                            TextView conversation_title = rootView.findViewById(R.id.conversation_title);
                            conversation_title.setText(photoAlbumEntity.getNickname());
                            //加载头像
                            ImageView photo_album_img = rootView.findViewById(R.id.photo_album_img);
                            Glide.with(TUIChatService.getAppContext()).load(ConfigUrl.getFullImageUrl(photoAlbumEntity.getAvatar()))
                                    .error(R.drawable.photo_album_img_default)
                                    .placeholder(R.drawable.photo_album_img_default)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(photo_album_img);
                            if(sex==1){
                                if(isVip==1){
                                    ImageView ic_vip = rootView.findViewById(R.id.iv_vip);
                                    ic_vip.setImageResource(R.drawable.ic_vip);
                                    rootView.findViewById(R.id.iv_vip).setVisibility(View.VISIBLE);
                                }else{
                                    rootView.findViewById(R.id.iv_vip).setVisibility(View.GONE);
                                }
                                if(certification==1){
                                    rootView.findViewById(R.id.certification).setVisibility(View.VISIBLE);
                                }else{

                                }
                            }
                            else{//女性用户
                                if(isVip==1){
                                    ImageView ic_vip = rootView.findViewById(R.id.iv_vip);
                                    ic_vip.setImageResource(R.drawable.ic_goddess);
                                    rootView.findViewById(R.id.iv_vip).setVisibility(View.VISIBLE);
                                }else{
                                    rootView.findViewById(R.id.iv_vip).setVisibility(View.GONE);
                                }
                                if(certification==1){
                                    if(isVip==1){
                                        rootView.findViewById(R.id.certification).setVisibility(View.GONE);
                                    }else{
                                        rootView.findViewById(R.id.certification).setVisibility(View.VISIBLE);
                                    }
                                }else{
                                    rootView.findViewById(R.id.certification).setVisibility(View.GONE);
                                }
                            }
                            RecyclerView recyclerView = rootView.findViewById(R.id.photo_album_rcv);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(rootView.getContext());
                            linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
                            recyclerView.setLayoutManager(linearLayoutManager);
                            PhotoAlbumItemRecyclerAdapter adapter = new PhotoAlbumItemRecyclerAdapter(photoAlbumEntity.getImg());
                            recyclerView.setAdapter(adapter);
                            adapter.setOnItemClickListener(new PhotoAlbumItemRecyclerAdapter.OnItemClickListener() {
                                @Override
                                public void onClick(View v, int pos, PhotoAlbumItemEntity itemEntity) {
                                    //onItemClickListener.openUserImage(itemEntity);
                                    onItemLongClickListener.clickToUserMain();
                                }
                            });
                            rootView.findViewById(R.id.photo_album_right_acc).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //onItemClickListener.toUserHome();
                                    onItemLongClickListener.clickToUserMain();
                                }
                            });
                            rootView.findViewById(R.id.photo_album_layout_item).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    onItemLongClickListener.clickToUserMain();
                                }
                            });
                            rootView.findViewById(R.id.photo_album_layout).setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    onItemLongClickListener.clickToUserMain();
                                }
                            });
                        }
                    }
                }else if(map_data != null && map_data.get("type") != null && map_data.get("type").equals("message_evaluate")){//评价
                    rootView.findViewById(R.id.full_toast).setVisibility(View.GONE);
                    rootView.findViewById(R.id.user_content).setVisibility(View.GONE);
                    rootView.findViewById(R.id.photo_album_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.custom_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.custom_error_layout).setVisibility(View.GONE);
                    if(!MessageRecyclerView.sex){
                        rootView.findViewById(R.id.im_evaluation_layout_sex).setBackgroundResource(R.drawable.im_evaluation_layout_male);
                    }else{
                        rootView.findViewById(R.id.im_evaluation_layout_sex).setBackgroundResource(R.drawable.im_evaluation_layout_gril);
                    }
                    rootView.findViewById(R.id.im_evaluation_layout).setVisibility(View.VISIBLE);
                    if(map_data.get("data")!=null){
                        List<EvaluateItemEntity> photoAlbumList = getObjectList(String.valueOf(map_data.get("data")), EvaluateItemEntity.class);
                        if(photoAlbumList!=null){
                            EvaluateItemEntity evaluateItemEntity1 = photoAlbumList.get(0);
                            EvaluateItemEntity evaluateItemEntity2 = photoAlbumList.get(1);
                            EvaluateItemEntity evaluateItemEntity3 = photoAlbumList.get(2);
                            TextView evaluation_tag1 = rootView.findViewById(R.id.evaluation_tag1);
                            TextView evaluation_tag2 = rootView.findViewById(R.id.evaluation_tag2);
                            TextView evaluation_tag3 = rootView.findViewById(R.id.evaluation_tag3);
                            TextView evaluation_tag4 = rootView.findViewById(R.id.evaluation_tag4);
                            evaluation_tag1.setText(evaluateItemEntity1.getName());
                            evaluation_tag2.setText(evaluateItemEntity2.getName());
                            evaluation_tag3.setText(evaluateItemEntity3.getName());
                            evaluation_tag1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    onItemLongClickListener.onClickEvaluate(position,msg,evaluateItemEntity1,false);
                                }
                            });
                            evaluation_tag2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    onItemLongClickListener.onClickEvaluate(position,msg,evaluateItemEntity2,false);
                                }
                            });
                            evaluation_tag3.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    onItemLongClickListener.onClickEvaluate(position,msg,evaluateItemEntity3,false);
                                }
                            });
                            evaluation_tag4.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    onItemLongClickListener.onClickEvaluate(position, msg, null, true);
                                }
                            });
                        }
                    }
                } else if (map_data != null && map_data.get("type") != null && map_data.get("type").equals("message_gift")) {//礼物
                    rootView.findViewById(R.id.im_evaluation_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.photo_album_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.user_content).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.full_toast).setVisibility(View.GONE);
                    rootView.findViewById(R.id.custom_error_layout).setVisibility(View.GONE);
                    msgContentFrame.setVisibility(View.GONE);
                    msg_content_fl_custom.setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.custom_layout).setVisibility(View.GONE);
                    GiftEntity giftEntity = IMGsonUtils.fromJson(String.valueOf(map_data.get("data")), GiftEntity.class);
                    if (giftEntity != null) {
                        View GiftView = View.inflate(rootView.getContext(), R.layout.custom_gift_view, null);
                        RelativeLayout custom_gift_layout = GiftView.findViewById(R.id.custom_gift_layout);
                        custom_gift_layout.setBackground(null);
                        ImageView gift_img = GiftView.findViewById(R.id.gift_img);
                        TextView gift_text = GiftView.findViewById(R.id.gift_text);
                        TextView gift_title = GiftView.findViewById(R.id.gift_title);
                        if (!MessageRecyclerView.isFlagTipMoney()) {
                            GiftView.findViewById(R.id.custom_gift_hint_text).setVisibility(View.GONE);
                        } else {
                            GiftView.findViewById(R.id.custom_gift_hint_text).setVisibility(View.VISIBLE);
                        }
                        if (msg.isSelf()) {
                            gift_title.setText(rootView.getContext().getString(R.string.custom_gift_left_title));
                            gift_title.setTextColor(rootView.getContext().getColor(R.color.gift_right_color));
                            gift_text.setTextColor(rootView.getContext().getColor(R.color.gift_right_txt_color));
                            custom_gift_layout.setBackground(rootView.getContext().getDrawable(R.drawable.custom_right_gift_backdrop));
                            GiftView.findViewById(R.id.custom_gift_hint_text).setVisibility(View.GONE);
                        } else {
                            gift_title.setText(rootView.getContext().getString(R.string.custom_gift_right_title));
                            gift_title.setTextColor(rootView.getContext().getColor(R.color.gift_left_color));
                            gift_text.setTextColor(rootView.getContext().getColor(R.color.gift_left_txt_color));
                            custom_gift_layout.setBackground(rootView.getContext().getDrawable(R.drawable.custom_left_gift_backdrop));
                            TextView custom_gift_hint_text = GiftView.findViewById(R.id.custom_gift_hint_text);
                            GiftView.findViewById(R.id.custom_gift_hint_text).setVisibility(View.VISIBLE);
                            if (MessageRecyclerView.sex) {
                                if (giftEntity.getProfitDiamond() != null) {
                                    String custom_message_txt7 = rootView.getContext().getString(R.string.custom_message_txt7);
                                    custom_gift_hint_text.setText(String.format(custom_message_txt7, giftEntity.getProfitDiamond().toString()));
                                }
                            } else {
                                if (giftEntity.getProfitTwd() != null) {
                                    String custom_message_txt2 = rootView.getContext().getString(R.string.custom_message_txt2);
                                    custom_gift_hint_text.setText(String.format(custom_message_txt2, String.format("%.2f", giftEntity.getProfitTwd())));
                                }
                            }
                        }
                        gift_text.setText(giftEntity.getTitle() + " x" + giftEntity.getAmount());
                        Glide.with(TUIChatService.getAppContext())
                                .load(ConfigUrl.getFullImageUrl(giftEntity.getImgPath()))
                                .error(R.drawable.photo_album_rcv_item_def_img)
                                .placeholder(R.drawable.photo_album_rcv_item_def_img)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(gift_img);
                        msg_content_fl_custom.addView(GiftView);
                    }
                } else if (map_data != null && map_data.get("type") != null && (map_data.get("type").equals("message_custom") || map_data.get("type").equals("message_tracking"))) {//自定义消息体
                    msg_content_fl_custom.setVisibility(View.GONE);
                    msgContentFrame.setVisibility(View.GONE);
                    rootView.findViewById(R.id.custom_error_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.custom_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.im_evaluation_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.photo_album_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.user_content).setVisibility(View.GONE);
                    rootView.findViewById(R.id.full_toast).setVisibility(View.GONE);
                    rootView.findViewById(R.id.custom_layout).setVisibility(View.VISIBLE);
                    TextView customHintText = rootView.findViewById(R.id.custom_hint_text);
                    CustomIMTextEntity customIMTextEntity = IMGsonUtils.fromJson(String.valueOf(map_data.get("data")), CustomIMTextEntity.class);

                    if (map_data.get("type").equals("message_tracking")) {
                        rootView.findViewById(R.id.custom_layout).setVisibility(View.GONE);
                        rootView.findViewById(R.id.custom_sufficient_view).setVisibility(View.GONE);
                    } else {
                        if (customIMTextEntity != null) {
                            if (customIMTextEntity.getInGame() != null) {
                                TextView customIngame = rootView.findViewById(R.id.custom_ingame_text);
                                if (!msg.isSelf()){
                                    customIngame.setText(rootView.getContext().getString(R.string.opponent_in_game));
                                }else {
                                    customIngame.setText(rootView.getContext().getString(R.string.call_ended));
                                }
                                customIngame.setVisibility(View.VISIBLE);
                            }

                            if (customIMTextEntity.getContent() == null) {
                                if (!MessageRecyclerView.isFlagTipMoney()) {
                                    rootView.findViewById(R.id.custom_layout).setVisibility(View.GONE);
                                } else {
                                    rootView.findViewById(R.id.custom_layout).setVisibility(View.VISIBLE);
                                }
                                if (MessageRecyclerView.sex) {
                                    //余额不足-需要充值
                                    if (customIMTextEntity.getIsRemindPay() != null && customIMTextEntity.getIsRemindPay().intValue() > 0) {
                                        FrameLayout custom_sufficient_view = rootView.findViewById(R.id.custom_sufficient_view);
                                        View custom_not_sufficient_view = View.inflate(rootView.getContext(), R.layout.custom_not_sufficient_view, null);
                                        LinearLayout male_hint_layout = custom_not_sufficient_view.findViewById(R.id.male_hint_layout);
                                        male_hint_layout.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                onItemLongClickListener.onClickDialogRechargeShow();
                                            }
                                        });
                                        customHintText.setVisibility(View.GONE);
                                        custom_sufficient_view.addView(custom_not_sufficient_view);
                                        custom_sufficient_view.setVisibility(View.VISIBLE);
                                    } else {
                                        customHintText.setVisibility(View.VISIBLE);
                                        rootView.findViewById(R.id.custom_sufficient_view).setVisibility(View.GONE);
                                        rootView.findViewById(R.id.custom_layout).setVisibility(View.GONE);
                                    }

                                } else {
                                    rootView.findViewById(R.id.custom_sufficient_view).setVisibility(View.GONE);
                                    if (Double.valueOf(customIMTextEntity.getPrice()).doubleValue() > 0) {
                                        String custom_message_txt2 = rootView.getContext().getString(R.string.custom_message_txt2);
                                        customHintText.setText(String.format(custom_message_txt2, customIMTextEntity.getPrice()));
                                    } else {
                                        if (customIMTextEntity.getIsRemindPay() != null && customIMTextEntity.getIsRemindPay().intValue() > 0) {
                                            String custom_message_txt3 = rootView.getContext().getString(R.string.custom_message_txt3);
                                            customHintText.setText(custom_message_txt3);
                                        } else {
                                            rootView.findViewById(R.id.custom_layout).setVisibility(View.GONE);
                                        }
                                    }
                                    customHintText.setVisibility(View.VISIBLE);
                                }
                            } else {
                                String likeText = "一鍵追蹤她，不再失聯噢 已追蹤";
                                if (MessageRecyclerView.addLikeMsgId != null && msg.getId().equals(MessageRecyclerView.addLikeMsgId)) {
                                    customIMTextEntity.setContent(likeText);
                                    customIMTextEntity.setEvent(-1);
                                    customIMTextEntity.setKey(null);
                                }
                                if (customIMTextEntity.getKey() != null) {
                                    //</font>
                                    if (customIMTextEntity.getContent().indexOf("<font>") != -1) {
                                        String fontText = "<font color='" + customIMTextEntity.getColor() + "'>" + customIMTextEntity.getKey() + "</font>";
                                        String content = customIMTextEntity.getContent();
                                        String CDATAText = content.replace("<font>" + customIMTextEntity.getKey() + "</font>", fontText);

                                        customHintText.setText(Html.fromHtml(CDATAText));
                                    } else {
                                        customHintText.setText(matcherSearchText(customIMTextEntity.getColor(), customIMTextEntity.getContent(), customIMTextEntity.getKey()));
                                    }
                                } else {
                                    customHintText.setText(customIMTextEntity.getContent());
                                }
                                if (customIMTextEntity.getGravity() != null) {
                                    LinearLayout.LayoutParams customHintTextLayoutParams = (LinearLayout.LayoutParams) customHintText.getLayoutParams();
                                    if (customIMTextEntity.getGravity().equals("left")) {
                                        customHintTextLayoutParams.gravity = Gravity.START;
                                        customHintTextLayoutParams.leftMargin = dip2px(rootView.getContext(), customIMTextEntity.getMargin());
                                    } else if (customIMTextEntity.getGravity().equals("right")) {
                                        customHintTextLayoutParams.gravity = Gravity.END;
                                        customHintTextLayoutParams.rightMargin = dip2px(rootView.getContext(), customIMTextEntity.getMargin());
                                    } else {
                                        customHintTextLayoutParams.gravity = Gravity.CENTER;
                                    }
                                    customHintText.setLayoutParams(customHintTextLayoutParams);
                                }

                                customHintText.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        onItemLongClickListener.onClickCustomText(position, msg, customIMTextEntity);
                                    }
                                });
                            }
                        }
                    }

                } else if (map_data != null && map_data.get("type") != null && map_data.get("type").equals("send_male_error")) {//自定义消息体
                    rootView.findViewById(R.id.custom_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.custom_error_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.im_evaluation_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.photo_album_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.user_content).setVisibility(View.GONE);
                    rootView.findViewById(R.id.full_toast).setVisibility(View.GONE);
                    rootView.findViewById(R.id.custom_error_layout).setVisibility(View.VISIBLE);
                    LinearLayout male_hint_error_layout = rootView.findViewById(R.id.male_hint_error_layout);
                    //点击唤醒充值
                    male_hint_error_layout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onItemLongClickListener.onClickDialogRechargeShow();
                        }
                    });
                } else if (map_data != null && map_data.get("type") != null && map_data.get("type").equals("chat_earnings")) {//收益提示
                    msg_content_fl_custom.setVisibility(View.GONE);
                    msgContentFrame.setVisibility(View.GONE);
                    rootView.findViewById(R.id.custom_error_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.im_evaluation_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.photo_album_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.user_content).setVisibility(View.GONE);
                    rootView.findViewById(R.id.full_toast).setVisibility(View.GONE);
                    rootView.findViewById(R.id.custom_layout).setVisibility(View.VISIBLE);
                    if (!MessageRecyclerView.isFlagTipMoney()) {
                        rootView.findViewById(R.id.custom_layout).setVisibility(View.GONE);
                    } else {
                        rootView.findViewById(R.id.custom_layout).setVisibility(View.VISIBLE);
                    }
                    TextView customHintText = rootView.findViewById(R.id.custom_hint_text);
                    CustomIMTextEntity customIMTextEntity = IMGsonUtils.fromJson(String.valueOf(map_data.get("data")), CustomIMTextEntity.class);
                    String custom_message_txt2 = "";
                    LinearLayout.LayoutParams customHintTextLayoutParams = (LinearLayout.LayoutParams) customHintText.getLayoutParams();
                    if (customIMTextEntity.getIsRefundMoney() != null) {
                        if (MessageRecyclerView.sex) {
                            custom_message_txt2 = rootView.getContext().getString(R.string.custom_message_txt_male);
                            if (!msg.isSelf()) {
                                customHintTextLayoutParams.gravity = Gravity.END;
                                customHintTextLayoutParams.rightMargin = dip2px(rootView.getContext(), 62);
                            } else {
                                customHintTextLayoutParams.gravity = Gravity.START;
                                customHintTextLayoutParams.leftMargin = dip2px(rootView.getContext(), 62);
                            }
                            customHintText.setText(custom_message_txt2);
                        } else {
                            custom_message_txt2 = rootView.getContext().getString(R.string.custom_message_txt_girl);
                            if (!msg.isSelf()) {
                                customHintTextLayoutParams.gravity = Gravity.END;
                                customHintTextLayoutParams.rightMargin = dip2px(rootView.getContext(), 62);
                            } else {
                                customHintTextLayoutParams.gravity = Gravity.START;
                                customHintTextLayoutParams.leftMargin = dip2px(rootView.getContext(), 62);
                            }
                            customHintText.setText(custom_message_txt2);
                        }
                    } else {
                        if (MessageRecyclerView.isCertification()) {
                            custom_message_txt2 = rootView.getContext().getString(R.string.custom_message_txt2);
                            customHintTextLayoutParams.gravity = Gravity.END;
                            customHintTextLayoutParams.rightMargin = dip2px(rootView.getContext(), 62);
                            customHintText.setText(String.format(custom_message_txt2, customIMTextEntity.getTextProfit()));
                        } else {
                            customIMTextEntity.setEvent(11);
                            custom_message_txt2 = rootView.getContext().getString(R.string.custom_message_txt1);
                            customHintTextLayoutParams.gravity = Gravity.CENTER;
                            customHintTextLayoutParams.rightMargin = dip2px(rootView.getContext(), 0);
                            customHintText.setText(matcherSearchText("#A72DFE", String.format(custom_message_txt2, customIMTextEntity.getTextProfit()), rootView.getContext().getString(R.string.custom_message_txt1_key)));
                        }
                    }

                    customHintText.setLayoutParams(customHintTextLayoutParams);
                    customHintText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onItemLongClickListener.onClickCustomText(position, msg, customIMTextEntity);
                        }
                    });
                } else {
                    rootView.findViewById(R.id.custom_error_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.custom_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.im_evaluation_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.photo_album_layout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.user_content).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.full_toast).setVisibility(View.GONE);
                    FaceManager.handlerEmojiText(msgBodyText, msg.getExtra().toString(), false);
                }
            } else {//正常接收消息
                View custom_gift_layout_max = rootView.findViewById(R.id.custom_gift_layout_max);
                if (custom_gift_layout_max != null) {
                    custom_gift_layout_max.setVisibility(View.GONE);
                }
                rootView.findViewById(R.id.custom_error_layout).setVisibility(View.GONE);
                rootView.findViewById(R.id.custom_layout).setVisibility(View.GONE);
                msg_content_fl_custom.setVisibility(View.GONE);
                msgContentFrame.setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.im_evaluation_layout).setVisibility(View.GONE);
                rootView.findViewById(R.id.photo_album_layout).setVisibility(View.GONE);
                rootView.findViewById(R.id.user_content).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.full_toast).setVisibility(View.GONE);
                String extra = msg.getExtra().toString();
                //信令消息
                if (msg.getTimMessage() != null && msg.getTimMessage().getCustomElem() != null && msg.getCustomElemData() != null) {
                    Map<String, Object> signallingData = IMGsonUtils.fromJson(new String(msg.getCustomElemData()), Map.class);
                    String customElem = new String(msg.getCustomElemData());
                    //音视频通话
                    if (customElem.indexOf("av_call") != -1) {
                        if (signallingData.get("actionType") != null) {
                            Map<String, Object> callData = IMGsonUtils.fromJson(String.valueOf(signallingData.get("data")), Map.class);
                            if (callData != null) {
                                int actionType = Double.valueOf(String.valueOf(signallingData.get("actionType"))).intValue();
                                int callType = Double.valueOf(String.valueOf(callData.get("call_type"))).intValue();
                                if (msg.isSelf()) {
                                    rootView.findViewById(R.id.left_call_img).setVisibility(View.GONE);
                                    if (actionType == 1) {//发起通话
                                        rootView.findViewById(R.id.right_call_img).setBackgroundResource(callType == 1 ? R.drawable.custom_audio_right_img_1 : R.drawable.custom_video_right_img_1);
                                        rootView.findViewById(R.id.right_call_img).setVisibility(View.VISIBLE);
                                    } else if (actionType == 2) {//取消通话
                                        rootView.findViewById(R.id.right_call_img).setBackgroundResource(callType == 1 ? R.drawable.custom_audio_right_img_2 : R.drawable.custom_video_right_img_1);
                                        rootView.findViewById(R.id.right_call_img).setVisibility(View.VISIBLE);
                                    } else if (actionType == 4) {//接听电话
                                        rootView.findViewById(R.id.right_call_img).setBackgroundResource(callType == 1 ? R.drawable.custom_audio_right_img_2 : R.drawable.custom_video_right_img_1);
                                        rootView.findViewById(R.id.right_call_img).setVisibility(View.VISIBLE);
                                    } else if (actionType == 7) {//接听电话
                                        rootView.findViewById(R.id.right_call_img).setBackgroundResource(callType == 1 ? R.drawable.custom_audio_right_img_2 : R.drawable.custom_video_right_img_1);
                                        rootView.findViewById(R.id.right_call_img).setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    rootView.findViewById(R.id.right_call_img).setVisibility(View.GONE);
                                    if (actionType == 1) {//发起通话
                                        rootView.findViewById(R.id.left_call_img).setBackgroundResource(callType == 1 ? R.drawable.custom_audio_left_img_1 : R.drawable.custom_video_left_img_1);
                                        rootView.findViewById(R.id.left_call_img).setVisibility(View.VISIBLE);
                                    } else if (actionType == 2) {//取消通话
                                        rootView.findViewById(R.id.left_call_img).setBackgroundResource(callType == 1 ? R.drawable.custom_audio_left_img_2 : R.drawable.custom_video_left_img_1);
                                        rootView.findViewById(R.id.left_call_img).setVisibility(View.VISIBLE);
                                    } else if (actionType == 4) {//接听电话
                                        rootView.findViewById(R.id.left_call_img).setBackgroundResource(callType == 1 ? R.drawable.custom_audio_left_img_2 : R.drawable.custom_video_left_img_1);
                                        rootView.findViewById(R.id.left_call_img).setVisibility(View.VISIBLE);
                                    } else if (actionType == 7) {//接听电话
                                        rootView.findViewById(R.id.left_call_img).setBackgroundResource(callType == 1 ? R.drawable.custom_audio_left_img_2 : R.drawable.custom_video_left_img_1);
                                        rootView.findViewById(R.id.left_call_img).setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    rootView.findViewById(R.id.right_call_img).setVisibility(View.GONE);
                    rootView.findViewById(R.id.left_call_img).setVisibility(View.GONE);
                }
                if (extra != null && extra.indexOf("href") != -1 && extra.indexOf("</a>") != -1) {
                    CharSequence charSequence = Html.fromHtml(extra);
                    msgBodyText.setText(charSequence);
                    msgBodyText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onItemLongClickListener.onTextTOWebView(msg);
                        }
                    });
                } else {
                    FaceManager.handlerEmojiText(msgBodyText, msg.getExtra().toString(), false);
                }
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
    }
    public void setBackColor(MessageInfo msg) {
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

    /**
     * 正则匹配 返回值是一个SpannableString 即经过变色处理的数据
     */
    private SpannableString matcherSearchText(String color, String text, String keyword) {
        if (text == null || TextUtils.isEmpty(text)) {
            return SpannableString.valueOf("");
        }
        SpannableString spannableString = new SpannableString(text);
        //条件 keyword
        Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
        //匹配
        Matcher matcher = pattern.matcher(spannableString);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            //ForegroundColorSpan 需要new 不然也只能是部分变色
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(color)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        //返回变色处理的结果
        return spannableString;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public int imUserIdToSystemUserId(String userId) {
        try {
            String strId = userId.replaceFirst("ru_", "");
            return Integer.parseInt(strId);
        } catch (Exception e) {
            return 0;
        }
    }

}
