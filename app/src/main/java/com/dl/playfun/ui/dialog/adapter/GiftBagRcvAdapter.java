package com.dl.playfun.ui.dialog.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dl.playfun.entity.GiftBagEntity;
import com.dl.playfun.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: 彭石林
 * Time: 2021/12/7 16:32
 * Description: This is GiftBagRcvAdapter
 */
public class GiftBagRcvAdapter extends RecyclerView.Adapter<GiftBagRcvAdapter.GiftBagRcvHolder> {

    private final Context mContext;
    private final List<List<GiftBagEntity.GiftEntity>> itemData;

    private GiftBagDetailAdapter.OnClickDetailListener onClickDetailListener = null;

    private final boolean isDarkShow;

    private final int layoutRes;

    private Map<GiftBagRcvHolder, GiftBagDetailAdapter> adapterMap = new HashMap<>();

    /**
     * @param context 上下文
     * @param dataList 数据列表
     * @param layoutRes 礼物布局
     * @param isDarkShow 是否暗黑模式
     */
    public GiftBagRcvAdapter(Context context, List<List<GiftBagEntity.GiftEntity>> dataList, int layoutRes, boolean isDarkShow) {
        this.mContext = context;
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
        if(itemData!=null && itemData.size()>0){
            GridLayoutManager layoutManage = new GridLayoutManager(mContext, 5);
            holder.rcvDetail.setLayoutManager(layoutManage);
            GiftBagDetailAdapter giftBagDetailAdapter = new GiftBagDetailAdapter(mContext, itemData.get(position), layoutRes, isDarkShow);
            adapterMap.put(holder, giftBagDetailAdapter);
            giftBagDetailAdapter.setOnClickListener((entity) -> {
                for (GiftBagRcvHolder key: adapterMap.keySet()) {
                    if (key == holder) continue;
                    adapterMap.get(key).cleanSelect();
                }
                onClickDetailListener.clickDetailCheck(entity);
            });
            holder.rcvDetail.setAdapter(giftBagDetailAdapter);
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

    public void setOnClickListener(GiftBagDetailAdapter.OnClickDetailListener onClickListener){
        this.onClickDetailListener = onClickListener;
    }
}
