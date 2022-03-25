package com.tencent.coustom;

import com.google.gson.annotations.SerializedName;

/**
 * Author: 彭石林
 * Time: 2021/12/20 21:56
 * Description: This is CustomTextEntity
 */
public class CustomIMTextEntity {
    //文本内容
    private String content;
    //关键字变色
    private String key;
    //颜色 如 ：#FFA72DFE
    private String color;
    //事件级别
    private int event = -1;
    //文本朝向 ： left左  right右  center 中
    private String gravity;
    //文本朝向距离 只在left、right有用
    private Integer margin;
    //收入价格
    private String price;
    //提示是否需要充值
    private Integer isRemindPay;
    //名字
    private String toName;
    //发送人
    private Integer toUserId;

    @SerializedName("text_profit")
    private String textProfit;

    private String msgID;

    private Integer isRefundMoney;

    public Integer getIsRefundMoney() {
        return isRefundMoney;
    }

    public void setIsRefundMoney(Integer isRefundMoney) {
        this.isRefundMoney = isRefundMoney;
    }

    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }

    public String getTextProfit() {
        return textProfit;
    }

    public void setTextProfit(String textProfit) {
        this.textProfit = textProfit;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public Integer getToUserId() {
        return toUserId;
    }

    public void setToUserId(Integer toUserId) {
        this.toUserId = toUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public String getGravity() {
        return gravity;
    }

    public void setGravity(String gravity) {
        this.gravity = gravity;
    }

    public Integer getMargin() {
        return margin;
    }

    public void setMargin(Integer margin) {
        this.margin = margin;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Integer getIsRemindPay() {
        return isRemindPay;
    }

    public void setIsRemindPay(Integer isRemindPay) {
        this.isRemindPay = isRemindPay;
    }
}
