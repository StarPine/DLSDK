package com.dl.playfun.widget.dialog;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.entity.VipPackageItemEntity;
import com.dl.playfun.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName VipItemVipItemRecyclerAdapter
 * @Description TODO
 * @Author 彭石林
 * @Date 2021/3/29 14:25
 * @Phone 16620350375
 * @email 15616314565@163.com
 * @Version 1.0
 **/
public class VipItemRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public OnItemListener onItemListener;
    List<VipPackageItemEntity> dataBeanList = new ArrayList<>();
    private int defItem = -1;

    public VipItemRecyclerAdapter(List<VipPackageItemEntity> list) {
        dataBeanList = list;
    }

    /**
     * 不是一次性遍历完list集合的，是根据RecyclerView的滑动逐步加载的
     * 每一次遍历，都会调用onCreateViewHolder(）方法
     *
     * @param holder   onCreateViewHolder()方法返回holder
     * @param position list集合遍历的位置
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        VipItemViewHolder vipItemViewHolder = ((VipItemViewHolder) holder);
        if (dataBeanList != null && dataBeanList.size() > 0) {
            holder.itemView.setSelected(defItem == position);
            if (defItem == position) {
                vipItemViewHolder.sel_vip_item.setBackgroundResource(R.drawable.vip_alert_sel);
            } else {
                vipItemViewHolder.sel_vip_item.setBackgroundResource(R.drawable.vip_alert_none);
            }
            vipItemViewHolder.goodsName.setText(dataBeanList.get(position).getGoodsName());
            vipItemViewHolder.payPrice.setText(dataBeanList.get(position).getPayPrice().replace(StringUtils.getString(R.string.vip_alert_my), StringUtils.getString(R.string.vip_alert_empty)));

            VipPackageItemEntity entity = dataBeanList.get(position);

            if(!StringUtils.isEmpty(entity.getGoodsLabel())){
                vipItemViewHolder.gold_price.setVisibility(View.VISIBLE);
                vipItemViewHolder.gold_price.setText(entity.getGoodsLabel());
            }else{
                vipItemViewHolder.gold_price.setVisibility(View.GONE);
                vipItemViewHolder.gold_price.setText("");
            }
//
//
//            if (StringUtil.isEmpty(dataBeanList.get(position).getGoldPrice()) || dataBeanList.get(position).getGoldPrice().equals("0")) {
//                vipItemViewHolder.gold_price.setVisibility(View.GONE);
//            } else {
//                vipItemViewHolder.gold_price.setVisibility(View.VISIBLE);
//                vipItemViewHolder.gold_price.setText(String.format(StringUtils.getString(R.string.vip_alert_gold_price), dataBeanList.get(position).getGoldPrice()));
//            }



            if (StringUtil.isEmpty(dataBeanList.get(position).getGoldTagPrice()) || dataBeanList.get(position).getGoldTagPrice().equals("0")) {
                vipItemViewHolder.gold_tag_price.setVisibility(View.GONE);
            } else {
                vipItemViewHolder.gold_tag_price.setVisibility(View.VISIBLE);
                vipItemViewHolder.gold_tag_price.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //中划线
                vipItemViewHolder.gold_tag_price.setText(String.format(StringUtils.getString(R.string.vip_alert_yj_price), dataBeanList.get(position).getGoldTagPrice()));
            }

//        if(dataBeanList.get(position).getdayPrice().length()==0){
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//            params.topMargin = 60;
//            vipItemViewHolder.payPrice.setLayoutParams(params);
//        }
            vipItemViewHolder.dayPrice.setText(String.format(StringUtils.getString(R.string.vip_alert_goldPrice, dataBeanList.get(position).getDayPrice())));
        }
    }

    @Override//创建ViewHolder引用暂存类
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vip_alert_chat_detail, parent, false);
        VipItemViewHolder holder = new VipItemViewHolder(view);
        /**
         * 自动向上转型，所以子类的对象holder，能赋给父类RecyclerView.ViewHolder的引用
         * 父类创建的对象的内存肯定是 <= 子类创建的对象的内存，因为子类对象=父类对象+子类扩充的属性和方法
         * 当子类对象自动向上转型赋给父类引用时，子类扩充的属性和方法是被屏蔽的，因为父类的引用没有指向这些
         * 属性的指针，但是当子类重写了父类的方法（只是方法）时，这个父类的引用可以调用这个被重写的方法，
         * 因为他们有了如下指向：父类的引用-->父类的方法-->父类被重写的方法
         * 这就是Java的----多态
         */
        return holder;
    }

    @Override//遍历list集合的长度
    public int getItemCount() {
        if (dataBeanList == null) {
            return 0;
        }
        return dataBeanList.size();
    }

    @Override//onCreateViewHolder(ViewGroup parent, int viewType)中的viewType参数由此获得
    public int getItemViewType(int position) {
        return position;
    }


    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    public void setDefSelect(int position) {
        this.defItem = position;
        notifyDataSetChanged();
    }

    public interface OnItemListener {
        void onClick(View v, int pos, VipPackageItemEntity vipPackageItemEntity);
    }

    public class VipItemViewHolder extends RecyclerView.ViewHolder {
        TextView goodsName;
        TextView payPrice;
        TextView dayPrice;
        TextView gold_price;//立省
        TextView gold_tag_price;
        RelativeLayout sel_vip_item;

        public VipItemViewHolder(@NonNull View itemView) {
            super(itemView);
            goodsName = itemView.findViewById(R.id.goodsName);
            payPrice = itemView.findViewById(R.id.payPrice);
            dayPrice = itemView.findViewById(R.id.dayPrice);
            sel_vip_item = itemView.findViewById(R.id.sel_vip_item);
            gold_price = itemView.findViewById(R.id.gold_price);
            gold_tag_price = itemView.findViewById(R.id.gold_tag_price);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemListener != null) {
                        onItemListener.onClick(v, getLayoutPosition(), dataBeanList.get(getLayoutPosition()));
                    }
                }
            });
        }
    }
}