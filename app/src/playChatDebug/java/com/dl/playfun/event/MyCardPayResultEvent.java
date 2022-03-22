package com.dl.playfun.event;

/**
 * @author litchi
 */
public class MyCardPayResultEvent {
    public static final int PAY_SUCCESS = 1;
    public static final int PAY_CANCEL = 2;
    public static final int PAY_ERROR = 3;

    private int status;
    private String orderNo;
    private String errorMsg;

    public MyCardPayResultEvent() {
    }

    public MyCardPayResultEvent(int status, String orderNo, String errorMsg) {
        this.status = status;
        this.orderNo = orderNo;
        this.errorMsg = errorMsg;
    }

    public static int getPaySuccess() {
        return PAY_SUCCESS;
    }

    public static int getPayCancel() {
        return PAY_CANCEL;
    }

    public static int getPayError() {
        return PAY_ERROR;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
