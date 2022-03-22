package com.dl.playfun.ui.dialog.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dl.playfun.entity.CoinExchangePriceInfo;
import com.dl.playfun.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wulei
 */
public class GameCoinExchargeAdapter extends RecyclerView.Adapter<GameCoinExchargeAdapter.RecyclerHolder> {

    private final Context mContext;
    private List<CoinExchangePriceInfo> dataList = new ArrayList<>();

    private CoinRechargeAdapterListener coinRechargeAdapterListener = null;

    public GameCoinExchargeAdapter(RecyclerView recyclerView) {
        this.mContext = recyclerView.getContext();
    }

    public CoinRechargeAdapterListener getCoinRechargeAdapterListener() {
        return coinRechargeAdapterListener;
    }

    public void setCoinRechargeAdapterListener(CoinRechargeAdapterListener coinRechargeAdapterListener) {
        this.coinRechargeAdapterListener = coinRechargeAdapterListener;
    }

    public void setData(List<CoinExchangePriceInfo> goodsList) {
        this.dataList = goodsList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_game_coin_excharge, parent, false);
        return new RecyclerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerHolder holder, int position) {
        CoinExchangePriceInfo goodsEntity = dataList.get(position);
        holder.tvJmPrice.setText(String.valueOf(goodsEntity.getCoins()));
        holder.tvGamePrice.setText(String.valueOf(goodsEntity.getGameCoins()));
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
        TextView tvJmPrice = null;
        TextView tvGamePrice = null;


        private RecyclerHolder(View itemView) {
            super(itemView);
            tvGamePrice = itemView.findViewById(R.id.tv_game_price);
            tvJmPrice = itemView.findViewById(R.id.tv_jm_price);
        }
    }
}