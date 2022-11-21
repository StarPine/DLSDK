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
import com.dl.playfun.R;
import com.dl.playfun.entity.GiftBagEntity;
import com.dl.playfun.utils.StringUtil;
import com.tencent.qcloud.tuikit.tuichat.TUIChatService;

import java.util.List;

/**
 * @author Shuotao Gong
 * @time 2022/11/19
 */
public class CrystalGiftBagDetailAdapter extends RecyclerView.Adapter<CrystalGiftBagDetailAdapter.ItemViewHolder>{

    private final Context mContext;
    private List<GiftBagEntity.CrystalGift> listData = null;

    private CrystalGiftBagDetailAdapter.OnClickDetailListener onClickDetailListener = null;
    private boolean isDarkShow = false;


    public CrystalGiftBagDetailAdapter(RecyclerView recyclerView, List<GiftBagEntity.CrystalGift> data, boolean isDarkShow) {
        this.mContext = recyclerView.getContext();
        this.listData = data;
        this.isDarkShow = isDarkShow;
    }

    @NonNull
    @Override
    public CrystalGiftBagDetailAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_crystal_gift_bag_item_detail, parent, false);
        return new CrystalGiftBagDetailAdapter.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        if (listData != null && listData.size() > 0) {
            GiftBagEntity.CrystalGift itemEntity = listData.get(position);
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
                    onClickDetailListener.clickDetailCheck(position, itemEntity, holder.detail_layout);
                }
            });
            if (listData.get(position).getFirst()) {
                onClickDetailListener.clickDetailCheck(position, itemEntity, holder.detail_layout);
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

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView icon_url;
        TextView title;
        TextView money;
        LinearLayout detail_layout;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            detail_layout = itemView.findViewById(R.id.detail_layout);
            icon_url = itemView.findViewById(R.id.icon_url);
            title = itemView.findViewById(R.id.title);
            money = itemView.findViewById(R.id.money);
        }
    }

    public void setOnClickListener(CrystalGiftBagDetailAdapter.OnClickDetailListener onClickListener){
        this.onClickDetailListener = onClickListener;
    }

    public interface  OnClickDetailListener{
        void clickDetailCheck(int position,GiftBagEntity.CrystalGift itemEntity,LinearLayout detail_layout);
    }
}

