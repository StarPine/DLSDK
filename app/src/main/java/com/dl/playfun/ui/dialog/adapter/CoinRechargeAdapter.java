package com.dl.playfun.ui.dialog.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dl.playfun.entity.GoodsEntity;
import com.dl.playfun.R;

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
        holder.tvName.setText(goodsEntity.getGoodsName());
        holder.tvPrice.setText(goodsEntity.getPayPrice());
        holder.tvOldPrice.setText(goodsEntity.getPayPrice());
        if(goodsEntity.getIsFirst()!=null && goodsEntity.getIsFirst().intValue()==1){
            holder.firstHint.setText(goodsEntity.getFirstText());
            holder.firstHint.setVisibility(View.VISIBLE);
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
        TextView tvName = null;
        TextView tvOldPrice = null;
        TextView tvPrice = null;
        TextView firstHint = null;

        private RecyclerHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvOldPrice = itemView.findViewById(R.id.tv_old_price);
            tvPrice = itemView.findViewById(R.id.tv_price);
            firstHint = itemView.findViewById(R.id.first_hint);
        }
    }
}