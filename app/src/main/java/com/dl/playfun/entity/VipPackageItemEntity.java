package com.dl.playfun.entity;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.dl.playfun.BR;
import com.google.gson.annotations.SerializedName;

/**
 * @author wulei
 */
public class VipPackageItemEntity extends BaseObservable {

    /**
     * id : 7
     * goods_name : 半个月
     * tag_price : 236.00
     * price : 118.00
     */

    private int id;
    @SerializedName("goods_name")
    private String goodsName;
    @SerializedName("monthly_price")
    private String monthlyPrice;
    @SerializedName("pay_price")
    private String payPrice;
    @SerializedName("is_recommend")
    private Integer isRecommend;
    @SerializedName("google_goods_id")
    private String googleGoodsId;
    @SerializedName("gold_price")
    private String goldPrice;
    @SerializedName("gold_tag_price")
    private String goldTagPrice;
    private Boolean isSelected;
    @SerializedName("actual_value")
    private Integer actualValue;

    private String price;//美元价格
    //vip 每日价格
    @SerializedName("day_price")
    private String dayPrice;

    //标题文案
    @SerializedName("discount_label")
    private String discountLabel;

    //产品图片
    @SerializedName("pic_img")
    private String picImg;
    @SerializedName("select_img")
    private String selectImg;//选中状态下的图片

    @SerializedName("is_first")
    private Integer isFirst;
    @SerializedName("first_text")
    private String firstText;

    //赠送转世
    @SerializedName("give_coin")
    private Integer giveCoin;

    //货币符号
    private String symbol;
    //vip-原价
    @SerializedName("original_price")
    private String originalPrice;
    //vip--销售价格、通用
    @SerializedName("sale_price")
    private String salePrice;


    //首充状态（H5传递）
    private Integer purchased;

    public String getDiscountLabel() {
        return discountLabel;
    }

    public void setDiscountLabel(String discountLabel) {
        this.discountLabel = discountLabel;
    }

    public String getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(String originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(String salePrice) {
        this.salePrice = salePrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }


    public Integer getGiveCoin() {
        return giveCoin;
    }

    public void setGiveCoin(Integer giveCoin) {
        this.giveCoin = giveCoin;
    }

    public Integer getIsFirst() {
        return isFirst;
    }

    public void setIsFirst(Integer isFirst) {
        this.isFirst = isFirst;
    }

    public String getFirstText() {
        return firstText;
    }

    public void setFirstText(String firstText) {
        this.firstText = firstText;
    }

    public String getPicImg() {
        return picImg;
    }

    public void setPicImg(String picImg) {
        this.picImg = picImg;
    }

    public String getSelectImg() {
        return selectImg;
    }

    public void setSelectImg(String selectImg) {
        this.selectImg = selectImg;
    }


    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Integer getActualValue() {
        return actualValue;
    }

    public void setActualValue(Integer actualValue) {
        this.actualValue = actualValue;
    }

    public String getDayPrice() {
        return dayPrice;
    }

    public void setDayPrice(String dayPrice) {
        this.dayPrice = dayPrice;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(String monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public String getPayPrice() {
        return payPrice;
    }

    public void setPayPrice(String payPrice) {
        this.payPrice = payPrice;
    }

    public Integer getIsRecommend() {
        return isRecommend;
    }

    public void setIsRecommend(Integer isRecommend) {
        this.isRecommend = isRecommend;
    }

    public String getGoogleGoodsId() {
        return googleGoodsId;
    }

    public void setGoogleGoodsId(String googleGoodsId) {
        this.googleGoodsId = googleGoodsId;
    }

    public String getGoldPrice() {
        return goldPrice;
    }

    public void setGoldPrice(String goldPrice) {
        this.goldPrice = goldPrice;
    }

    public String getGoldTagPrice() {
        return goldTagPrice;
    }

    public void setGoldTagPrice(String goldTagPrice) {
        this.goldTagPrice = goldTagPrice;
    }

    @Bindable
    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
        notifyPropertyChanged(BR.selected);
    }

    public Integer getPurchased() {
        return purchased;
    }

    public void setPurchased(Integer purchased) {
        this.purchased = purchased;
    }

    @Override
    public String toString() {
        return "VipPackageItemEntity{" +
                "id=" + id +
                ", goodsName='" + goodsName + '\'' +
                ", monthlyPrice='" + monthlyPrice + '\'' +
                ", payPrice='" + payPrice + '\'' +
                ", isRecommend=" + isRecommend +
                ", googleGoodsId='" + googleGoodsId + '\'' +
                ", goldPrice='" + goldPrice + '\'' +
                ", goldTagPrice='" + goldTagPrice + '\'' +
                ", isSelected=" + isSelected +
                ", actualValue=" + actualValue +
                ", price='" + price + '\'' +
                ", dayPrice='" + dayPrice + '\'' +
                ", picImg='" + picImg + '\'' +
                ", selectImg='" + selectImg + '\'' +
                ", isFirst=" + isFirst +
                ", firstText='" + firstText + '\'' +
                ", giveCoin=" + giveCoin +
                ", purchased=" + purchased +
                '}';
    }
}
