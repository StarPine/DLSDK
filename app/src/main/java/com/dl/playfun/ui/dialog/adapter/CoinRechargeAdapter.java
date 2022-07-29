package com.dl.playfun.ui.dialog.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ColorUtils;
import com.dl.playfun.R;
import com.dl.playfun.entity.GoodsEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wulei
 */
public class CoinRechargeAdapter extends RecyclerView.Adapter<CoinRechargeAdapter.RecyclerHolder> {

    private final Context mContext;
    private List<GoodsEntity> dataList = new ArrayList<>();

    private CoinRechargeAdapterListener coinRechargeAdapterListener = null;

    public CoinRechargeAdapter(RecyclerView recyclerView) {
        this.mContext = recyclerView.getContext();
    }

    public CoinRechargeAdapterListener getCoinRechargeAdapterListener() {
        return coinRechargeAdapterListener;
    }

    public void setCoinRechargeAdapterListener(CoinRechargeAdapterListener coinRechargeAdapterListener) {
        this.coinRechargeAdapterListener = coinRechargeAdapterListener;
    }

    public void setData(List<GoodsEntity> goodsList) {
        this.dataList = goodsList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_coin_recharge, parent, false);
        return new RecyclerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerHolder holder, int position) {
        GoodsEntity goodsEntity = dataList.get(position);
        holder.good_text.setText(goodsEntity.getGoodsName());
        holder.good_name.setText(goodsEntity.getSymbol() + goodsEntity.getSalePrice());
        holder.good_lable.setText(goodsEntity.getDiscountLabel());
        //是否推荐
        if (goodsEntity.getIsRecommend() != null && goodsEntity.getIsRecommend() == 1) {
            holder.layout1.setBackgroundResource(R.drawable.coin_recharge_img_custom_backop);
            holder.first_layout.setBackgroundResource(R.drawable.coin_recharge_img_custom_title);
            holder.good_text.setTextColor(ColorUtils.getColor(R.color.white));
            holder.img1.setBackgroundResource(R.drawable.coin_recharge_img_item_1);
            holder.is_first.setVisibility(View.VISIBLE);
            holder.btn_sub.setBackgroundResource(R.drawable.coin_recharge_img_btn2);
            holder.good_name.setTextColor(ColorUtils.getColor(R.color.purple_text));
        } else {
            holder.layout1.setBackgroundResource(R.drawable.coin_recharge_img_custom);
            holder.first_layout.setBackgroundResource(R.drawable.coin_recharge_img_custom_title2);
            holder.good_text.setTextColor(ColorUtils.getColor(R.color.black));
            holder.img1.setBackgroundResource(R.drawable.coin_recharge_img_item_2);
            holder.is_first.setVisibility(View.GONE);
            holder.btn_sub.setBackgroundResource(R.drawable.coin_recharge_img_btn);
            holder.good_name.setTextColor(ColorUtils.getColor(R.color.white));
        }
        if (goodsEntity.getIsFirst() != null && goodsEntity.getIsFirst() == 1) { //是否首冲
            holder.good_lable.setText(goodsEntity.getFirstText());
            holder.first_layout.setVisibility(View.VISIBLE);
        } else {
            holder.first_layout.setVisibility(View.GONE);
        }

        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(v -> {
            if (coinRechargeAdapterListener != null) {
                int p = (int) v.getTag();
                coinRechargeAdapterListener.onBuyClick(v, p);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public interface CoinRechargeAdapterListener {
        void onBuyClick(View view, int position);
    }

    class RecyclerHolder extends RecyclerView.ViewHolder {
        LinearLayout layout1;
        LinearLayout first_layout;
        TextView good_lable;
        TextView good_text;
        ImageView img1;
        LinearLayout btn_sub;
        ImageView is_first;
        TextView good_name;

        private RecyclerHolder(View itemView) {
            super(itemView);
            layout1 = itemView.findViewById(R.id.layout1);
            first_layout = itemView.findViewById(R.id.first_layout);
            good_lable = itemView.findViewById(R.id.good_lable);
            good_text = itemView.findViewById(R.id.good_text);
            img1 = itemView.findViewById(R.id.img1);
            btn_sub = itemView.findViewById(R.id.btn_sub);
            is_first = itemView.findViewById(R.id.is_first);
            good_name = itemView.findViewById(R.id.good_name);
        }
    }
}