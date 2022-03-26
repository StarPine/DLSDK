package com.dl.playfun.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Author: 彭石林
 * Time: 2021/12/20 14:35
 * Description: IM聊天价格配置
 */
public class PriceConfigEntity {
    @SerializedName("is_follow")
    private Integer isFollow;
    @SerializedName("is_pay")
    private Integer isPay;
    private Current current;
    private Current other;

    public Integer getIsFollow() {
        return isFollow;
    }

    public void setIsFollow(Integer isFollow) {
        this.isFollow = isFollow;
    }

    public Current getCurrent() {
        return current;
    }

    public void setCurrent(Current current) {
        this.current = current;
    }

    public Current getOther() {
        return other;
    }

    public void setOther(Current other) {
        this.other = other;
    }

    public Integer getIsPay() {
        return isPay;
    }

    public void setIsPay(Integer isPay) {
        this.isPay = isPay;
    }

    //男
    public class Current{
            private Integer balance;
            private Integer sex;
            @SerializedName("prop_total")
            private Integer propTotal;
            @SerializedName("charge_msg_number")
            private Integer chargeMsgNumber;
            @SerializedName("refund_msg_number")
            private Integer refundMsgNumber;
            private Integer certification;

        private String videoProfitTips;
            private String audioProfitTips;
            @SerializedName("text_price")
            private Integer textPrice;
            //是否是首次收益
            @SerializedName("first_im_msg")
            private Integer firstImMsg;

        public Integer getBalance() {
            return balance;
        }

        public void setBalance(Integer balance) {
            this.balance = balance;
        }

        public Integer getSex() {
            return sex;
        }

        public void setSex(Integer sex) {
            this.sex = sex;
        }

        public Integer getPropTotal() {
            return propTotal;
        }

        public void setPropTotal(Integer propTotal) {
            this.propTotal = propTotal;
        }

        public Integer getChargeMsgNumber() {
            return chargeMsgNumber;
        }

        public void setChargeMsgNumber(Integer chargeMsgNumber) {
            this.chargeMsgNumber = chargeMsgNumber;
        }

        public Integer getRefundMsgNumber() {
            return refundMsgNumber;
        }

        public void setRefundMsgNumber(Integer refundMsgNumber) {
            this.refundMsgNumber = refundMsgNumber;
        }

        public Integer getCertification() {
            return certification;
        }

        public void setCertification(Integer certification) {
            this.certification = certification;
        }

        public String getVideoProfitTips() {
            return videoProfitTips;
        }

        public void setVideoProfitTips(String videoProfitTips) {
            this.videoProfitTips = videoProfitTips;
        }

        public String getAudioProfitTips() {
            return audioProfitTips;
        }

        public void setAudioProfitTips(String audioProfitTips) {
            this.audioProfitTips = audioProfitTips;
        }

        public Integer getTextPrice() {
            return textPrice;
        }

        public void setTextPrice(Integer textPrice) {
            this.textPrice = textPrice;
        }

        public Integer getFirstImMsg() {
            return firstImMsg;
        }

        public void setFirstImMsg(Integer firstImMsg) {
            this.firstImMsg = firstImMsg;
        }

        @Override
        public String toString() {
            return "Current{" +
                    "balance=" + balance +
                    ", sex=" + sex +
                    ", propTotal=" + propTotal +
                    ", chargeMsgNumber=" + chargeMsgNumber +
                    ", refundMsgNumber=" + refundMsgNumber +
                    ", certification=" + certification +
                    ", videoProfitTips='" + videoProfitTips + '\'' +
                    ", audioProfitTips='" + audioProfitTips + '\'' +
                    ", textPrice=" + textPrice +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "PriceConfigEntity{" +
                "isFollow=" + isFollow +
                ", isPay=" + isPay +
                ", current=" + current +
                ", other=" + other +
                '}';
    }
}
