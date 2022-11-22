package com.dl.playfun.ui.dialog.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dl.playfun.entity.GiftBagEntity;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.R;
import com.tencent.qcloud.tuikit.tuichat.TUIChatService;

import java.util.List;

/**
 * Author: 彭石林
 * Time: 2021/12/7 17:09
 * Description: This is GiftBagDetailAdapter
 */
public class GiftBagDetailAdapter extends RecyclerView.Adapter<GiftBagDetailAdapter.GiftItemViewHolder>{

    private final Context mContext;
    private final List<? extends GiftBagEntity.GiftEntity> listData;

    private OnClickDetailListener onClickDetailListener = null;
    private final boolean isDarkShow;

    private final int layoutRes;

    public GiftBagDetailAdapter(RecyclerView recyclerView, List<? extends GiftBagEntity.GiftEntity> data, int layoutRes, boolean isDarkShow) {
        this.mContext = recyclerView.getContext();
        this.listData = data;
        this.layoutRes = layoutRes;
        this.isDarkShow = isDarkShow;
    }

    @NonNull
    @Override
    public GiftItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(layoutRes, parent, false);
        return new GiftItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiftItemViewHolder holder, int position) {
        if (listData != null && listData.size() > 0) {
            int index = position;
            GiftBagEntity.GiftEntity itemEntity = listData.get(index);
            holder.title.setText(itemEntity.getName());
            if (isDarkShow) {
                holder.title.setTextColor(Color.parseColor("#F1F2F9"));
            } else {
                holder.title.setTextColor(Color.parseColor("#333333"));
            }
            holder.money.setText(String.valueOf(itemEntity.getMoney()));
            Glide.with(TUIChatService.getAppContext()).load(StringUtil.getFullImageUrl(itemEntity.getImg()))
                    .error(R.drawable.default_avatar)
                    .placeholder(R.drawable.default_avatar)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.icon_url);
            holder.detail_layout.setOnClickListener(v -> {
                if (onClickDetailListener != null) {
                    onClickDetailListener.clickDetailCheck(index, itemEntity, holder.detail_layout);
                }
            });
            if (listData.get(position).isFirst()) {
                onClickDetailListener.clickDetailCheck(index, itemEntity, holder.detail_layout);
            }
        }

    }

    @Override
    public int getItemCount() {
        if(listData==null){
            return 0;
        }
        return listData.size();
    }

    public static class GiftItemViewHolder extends RecyclerView.ViewHolder {
        ImageView icon_url;
        TextView title;
        TextView money;
        LinearLayout detail_layout;

        public GiftItemViewHolder(@NonNull View itemView) {
            super(itemView);
            detail_layout = itemView.findViewById(R.id.detail_layout);
            icon_url = itemView.findViewById(R.id.icon_url);
            title = itemView.findViewById(R.id.title);
            money = itemView.findViewById(R.id.money);
        }
    }

    public interface OnClickDetailListener {
        void clickDetailCheck(int position, GiftBagEntity.GiftEntity itemEntity, LinearLayout detail_layout);
    }

    public void setOnClickListener(OnClickDetailListener onClickListener){
        this.onClickDetailListener = onClickListener;
    }

}
