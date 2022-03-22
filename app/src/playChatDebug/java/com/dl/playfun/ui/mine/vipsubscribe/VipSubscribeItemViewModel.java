package com.dl.playfun.ui.mine.vipsubscribe;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.android.billingclient.api.SkuDetails;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.entity.VipPackageItemEntity;
import com.dl.playfun.utils.ExceptionReportUtils;

import me.goldze.mvvmhabit.base.MultiItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.utils.Utils;

/**
 * 会员充值
 *
 * @author wulei
 */
public class VipSubscribeItemViewModel extends MultiItemViewModel<VipSubscribeViewModel> {

    public ObservableField<VipPackageItemEntity> itemEntity = new ObservableField<>();
    public ObservableField<SkuDetails> skuDetails = new ObservableField<>();

    public String num_ling = "0";
    //条目的点击事件
    public BindingCommand itemClick = new BindingCommand(() -> {
        try {
            int position = viewModel.observableList.indexOf(VipSubscribeItemViewModel.this);
            viewModel.itemClick(position,itemEntity.get());
        } catch (Exception e) {
            ExceptionReportUtils.report(e);
        }
    });

    public VipSubscribeItemViewModel(@NonNull VipSubscribeViewModel viewModel, VipPackageItemEntity itemEntity, SkuDetails skuDetails) {
        super(viewModel);
        this.itemEntity.set(itemEntity);
        this.skuDetails.set(skuDetails);
    }

    //获取标题文字
    public String getTitleText(){
        VipPackageItemEntity entity = itemEntity.get();
        if(StringUtils.isEmpty(entity.getGoodsLabel())){
            return "";
        }else{
            return entity.getGoodsLabel();
        }
    }
    //控制标题是否隐藏
    public int getTitleShow(){
        VipPackageItemEntity entity = itemEntity.get();
        if(!StringUtils.isEmpty(entity.getGoodsLabel())){
            return View.VISIBLE;
        }else{
            return View.GONE;
        }
    }

    //获取推荐弹窗标题
    public Drawable getRecommendImg(boolean select){
        if(select){
            return Utils.getContext().getDrawable(R.drawable.img_vip_sub_item_recommend_select);
        }else{
            return Utils.getContext().getDrawable(R.drawable.img_vip_sub_item_recommend_normal);
        }
    }
    //推荐角标显示隐藏
    public Integer isRecommendShow(){
        VipPackageItemEntity entity = itemEntity.get();
        if(entity.getIsRecommend()!=null && entity.getIsRecommend()==1){
            return View.VISIBLE;
        }
        return View.GONE;
    }

}
