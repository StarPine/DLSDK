package com.dl.playfun.ui.dialog.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dl.playfun.R;
import com.dl.playfun.entity.CrystalGiftBagAdapterEntity;
import com.dl.playfun.entity.GiftBagEntity;

import java.util.List;

/**
 * @author Shuotao Gong
 * @time 2022/11/19
 */
public class CrystalGiftBagRcvAdapter extends RecyclerView.Adapter<CrystalGiftBagRcvAdapter.CrystalGiftBagRcvHolder> {

    private final Context mContext;
    private List<CrystalGiftBagAdapterEntity> itemData = null;

    private OnClickRcvDetailListener onClickDetailListener = null;

    private boolean isDarkShow = false;

    public CrystalGiftBagRcvAdapter(RecyclerView recyclerView, List<CrystalGiftBagAdapterEntity> dataList, boolean isDarkShow) {
        this.mContext = recyclerView.getContext();
        this.itemData = dataList;
        this.isDarkShow = isDarkShow;
    }

    @NonNull
    @Override
    public CrystalGiftBagRcvHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_gift_bag_item_rcv, parent, false);
        return new CrystalGiftBagRcvHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CrystalGiftBagRcvHolder holder, int position) {
        if (itemData != null && itemData.size() > 0) {
            GridLayoutManager layoutManage = new GridLayoutManager(mContext, 5);
            holder.rcvDetail.setLayoutManager(layoutManage);
            CrystalGiftBagDetailAdapter giftBagDetailAdapter = new CrystalGiftBagDetailAdapter(holder.rcvDetail, itemData.get(position).getCrystalGiftEntity(), isDarkShow);
            giftBagDetailAdapter.setOnClickListener((position1, itemEntity, detail_layout) -> {
                if (onClickDetailListener != null) {
                    onClickDetailListener.clickRcvDetailCheck(position1, itemEntity, detail_layout, position);
                }
            });
            holder.rcvDetail.setAdapter(giftBagDetailAdapter);
        }
    }

    @Override
    public int getItemCount() {
        if (itemData == null) {
            return 0;
        }
        return itemData.size();
    }

    class CrystalGiftBagRcvHolder extends RecyclerView.ViewHolder {
        RecyclerView rcvDetail;

        private CrystalGiftBagRcvHolder(View itemView) {
            super(itemView);
            rcvDetail = itemView.findViewById(R.id.rcv_detail);
        }
    }

    public void setOnClickListener(OnClickRcvDetailListener onClickListener) {
        this.onClickDetailListener = onClickListener;
    }

    public interface OnClickRcvDetailListener {
        void clickRcvDetailCheck(int position, GiftBagEntity.CrystalGift itemEntity, LinearLayout detail_layout, int rcvPosition);
    }
}

