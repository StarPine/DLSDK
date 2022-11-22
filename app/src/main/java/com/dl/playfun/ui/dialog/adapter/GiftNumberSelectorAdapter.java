package com.dl.playfun.ui.dialog.adapter;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.dl.playfun.R;

import java.util.List;

/**
 * @author Shuotao Gong
 * @time 2022/11/21
 */
public class GiftNumberSelectorAdapter extends RecyclerView.Adapter<GiftNumberSelectorAdapter.GiftNumberSelectorViewHolder> {

    private List<Integer> giftNumbers;

    private ItemOnClickListener listener;

    private boolean isDarkShow;

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
        if (isDarkShow) {
            holder.item.setTextColor(ContextCompat.getColor(holder.root.getContext(), R.color.color_text_9897B3));
        } else {
            holder.item.setTextColor(ContextCompat.getColor(holder.root.getContext(), R.color.color_text_333333));
        }
    }

    @Override
    public int getItemCount() {
        return giftNumbers.size();
    }

    public void setGiftNumbers(List<Integer> giftNumbers) {
        this.giftNumbers = giftNumbers;
    }

    public void setListener(ItemOnClickListener listener) {
        this.listener = listener;
    }

    public void setDarkShow(boolean darkShow) {
        isDarkShow = darkShow;
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
