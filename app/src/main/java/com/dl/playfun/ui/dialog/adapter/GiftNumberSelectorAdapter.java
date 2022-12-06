package com.dl.playfun.ui.dialog.adapter;

import android.content.Context;
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
import java.util.Locale;

/**
 * @author Shuotao Gong
 * @time 2022/11/21
 */
public class GiftNumberSelectorAdapter extends RecyclerView.Adapter<GiftNumberSelectorAdapter.GiftNumberSelectorViewHolder> {

    private final Context context;

    private final List<Integer> giftNumbers;

    private final ItemOnClickListener listener;

    private final boolean isDarkShow;

    private GiftNumberSelectorViewHolder selected;

    /**
     * @param giftNumbers 礼物可选数量
     * @param isDarkShow 是否暗黑模式
     * @param listener 点击回调
     */
    public GiftNumberSelectorAdapter(Context context, List<Integer> giftNumbers, boolean isDarkShow, ItemOnClickListener listener) {
        this.context = context;
        this.giftNumbers = giftNumbers;
        this.isDarkShow = isDarkShow;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GiftNumberSelectorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_gift_number_selector_item, null, false);
        return new GiftNumberSelectorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiftNumberSelectorViewHolder holder, int position) {
        holder.item.setText(String.format(Locale.getDefault(), "%d", giftNumbers.get(position)));

        holder.root.setOnClickListener(v -> itemOnSelect(position, holder));
        if (position == 0) {
            itemOnSelect(position, holder);
        }
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

    private void itemOnSelect(int position, GiftNumberSelectorViewHolder holder) {
        if (selected != null) {
            selected.root.setBackground(null);
            selected.item.setTextColor(ContextCompat.getColor(context, R.color.color_text_333333));
        }
        selected = holder;
        holder.root.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_bg_gift_number_selector_item));
        holder.item.setTextColor(ContextCompat.getColor(context, R.color.color_bg_gift_number_selector_item));
        listener.onClick(giftNumbers.get(position));
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
        void onClick(int selectedNumber);
    }

}
