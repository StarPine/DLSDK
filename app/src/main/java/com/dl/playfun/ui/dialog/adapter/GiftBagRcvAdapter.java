package com.dl.playfun.ui.dialog.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dl.playfun.entity.GiftBagAdapterEntity;
import com.dl.playfun.entity.GiftBagEntity;
import com.dl.playfun.R;

import java.util.List;

/**
 * Author: 彭石林
 * Time: 2021/12/7 16:32
 * Description: This is GiftBagRcvAdapter
 */
public class GiftBagRcvAdapter extends RecyclerView.Adapter<GiftBagRcvAdapter.GiftBagRcvHolder> {

    private final Context mContext;
    private final List<GiftBagAdapterEntity> itemData;

    private OnClickRcvDetailListener onClickDetailListener = null;

    private final boolean isDarkShow;

    private final int layoutRes;

    public GiftBagRcvAdapter(RecyclerView recyclerView, List<GiftBagAdapterEntity> dataList, int layoutRes, boolean isDarkShow) {
        this.mContext = recyclerView.getContext();
        this.itemData = dataList;
        this.layoutRes = layoutRes;
        this.isDarkShow = isDarkShow;
    }

    @NonNull
    @Override
    public GiftBagRcvHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_gift_bag_item_rcv, parent, false);
        return new GiftBagRcvHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiftBagRcvHolder holder, int position) {
        GiftBagRcvHolder itemViewHolder = holder;
        if(itemData!=null && itemData.size()>0){
            int index = position;
            GridLayoutManager layoutManage = new GridLayoutManager(mContext, 5);
            itemViewHolder.rcvDetail.setLayoutManager(layoutManage);
            GiftBagDetailAdapter giftBagDetailAdapter = new GiftBagDetailAdapter(itemViewHolder.rcvDetail, itemData.get(index).getGiftBagEntity(), layoutRes, isDarkShow);
            giftBagDetailAdapter.setOnClickListener((position1, itemEntity, detail_layout) -> {
                if(onClickDetailListener!=null){
                    onClickDetailListener.clickRcvDetailCheck(position1, itemEntity, detail_layout,index);
                }
            });
            itemViewHolder.rcvDetail.setAdapter(giftBagDetailAdapter);
        }
    }

    @Override
    public int getItemCount() {
        if(itemData==null){
            return 0;
        }
        return itemData.size();
    }

    class GiftBagRcvHolder extends RecyclerView.ViewHolder {
        RecyclerView rcvDetail;

        private GiftBagRcvHolder(View itemView) {
            super(itemView);
            rcvDetail = itemView.findViewById(R.id.rcv_detail);
        }
    }

    public void setOnClickListener(OnClickRcvDetailListener onClickListener){
        this.onClickDetailListener = onClickListener;
    }
    public interface  OnClickRcvDetailListener{
        void clickRcvDetailCheck(int position, GiftBagEntity.GiftEntity itemEntity, LinearLayout detail_layout, int rcvPosition);
    }
}
