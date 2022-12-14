package com.dl.playfun.widget.dialog;

import android.graphics.Typeface;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.dl.playfun.entity.OccupationConfigItemEntity;
import com.dl.playfun.R;

import java.util.List;

public class OccputonItemAdapter extends BaseQuickAdapter<OccupationConfigItemEntity.ItemEntity, BaseViewHolder> {

    public OccputonItemAdapter(@Nullable List<OccupationConfigItemEntity.ItemEntity> data) {
        super(R.layout.item_occupation_detail, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, OccupationConfigItemEntity.ItemEntity item) {
        baseViewHolder.setText(R.id.tv_content, item.getName())
                .setTextColor(R.id.tv_content, item.isChoose() ? 0xffA72DFE : 0xff333333);
        ((TextView) baseViewHolder.getView(R.id.tv_content)).setTypeface(null, item.isChoose() ? Typeface.BOLD : Typeface.NORMAL);
    }
}
