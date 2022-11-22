package com.dl.playfun.ui.dialog.adapter;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dl.playfun.R;

import java.util.List;

/**
 * @author Shuotao Gong
 * @time 2022/11/21
 */
public class GiftNumberSelectorAdapter extends RecyclerView.Adapter<GiftNumberSelectorAdapter.GiftNumberSelectorViewHolder> {

    List<Integer> giftNumbers;

    ItemOnClickListener listener;

    @NonNull
    @Override
    public GiftNumberSelectorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_gift_number_selector_item, null, false);
        return new GiftNumberSelectorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiftNumberSelectorViewHolder holder, int position) {
        holder.item.setText(giftNumbers.get(position).toString());
        if (position == 0) {
            listener.onClick(holder);
        }
        holder.root.setOnClickListener((v) -> {
            listener.onClick(holder);
        });
    }

    @Override
    public int getItemCount() {
        return giftNumbers.size();
    }

    public List<Integer> getGiftNumbers() {
        return giftNumbers;
    }

    public void setGiftNumbers(List<Integer> giftNumbers) {
        this.giftNumbers = giftNumbers;
    }

    public ItemOnClickListener getListener() {
        return listener;
    }

    public void setListener(ItemOnClickListener listener) {
        this.listener = listener;
    }

    public static class GiftNumberSelectorViewHolder extends RecyclerView.ViewHolder {

        public View root;
        public TextView item;

        public GiftNumberSelectorViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.getRootView();
            item = itemView.findViewById(R.id.selector_item);
        }
    }

    public interface ItemOnClickListener {
        void onClick(GiftNumberSelectorViewHolder holder);
    }

}
