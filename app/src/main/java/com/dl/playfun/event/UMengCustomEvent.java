package com.dl.playfun.event;

public class UMengCustomEvent {

    /**
     * 用户主页-发讯息
     */
    public static final String EVENT_USER_DETAIL_CHAT = "user_detail-chat";
    /**
     * 用户主页-直接私讯她
     */
    public static final String EVENT_USER_DETAIL_PRIVATE_CHAT = "user_detail-private_chat";
    /**
     * 用户主页-社群账号
     */
    public static final String EVENT_USER_DETAIL_CHAT_ACCOUNT = "user_detail-chat_account";
    /**
     * 用户主页-社群账号-解鎖
     */
    public static final String EVENT_USER_DETAIL_CHAT_ACCOUNT_UNLOCK = "user_detail-chat_account-unlock";
    /**
     * 广播-点赞
     */
    public static final String EVENT_BROADCAST_LIKE = "broadcast-like";
    /**
     * 广播-留言
     */
    public static final String EVENT_BROADCAST_COMMENT = "broadcast-comment";
    /**
     * 广播-发布
     */
    public static final String EVENT_BROADCAST_PUBLISH = "broadcast-publish";
    /**
     * 广播-发布-找约会
     */
    public static final String EVENT_BROADCAST_PUBLISH_PROGRAM = "broadcast-publish-program";
    /**
     * 广播-发布-发动态
     */
    public static final String EVENT_BROADCAST_PUBLISH_DYNAMIC = "broadcast-publish-dynamic";
    /**
     * 我的广播-发布
     */
    public static final String EVENT_MY_BROADCAST_PUBLISH = "mybroadcast-publish";
    /**
     * 我的广播-发布-找约会
     */
    public static final String EVENT_MY_BROADCAST_PUBLISH_PROGRAM = "mybroadcast-publish-program";
    /**
     * 我的广播-发布-发动态
     */
    public static final String EVENT_MY_BROADCAST_PUBLISH_DYNAMIC = "mybroadcast-publish-dynamic";
    /**
     * 广播-发动态-发布
     */
    public static final String EVENT_BROADCAST_DYNAMIC_PUBLISH = "broadcast-dynamic-publish";
    /**
     * 广播-发约会-发布
     */
    public static final String EVENT_BROADCAST_PROGRAM_PUBLISH = "broadcast-program-publish";
    /**
     * 个人中心-vip
     */
    public static final String EVENT_USER_CENTER_VIP = "user_center-vip";
    /**
     * 个人中心-vip-立即开通
     */
    public static final String EVENT_USER_CENTER_VIP_OPEN = "user_center-vip-open";
    /**
     * 支付-储值
     */
    public static final String EVENT_PAY_RECHARGE = "pay-recharge";
    /**
     * 支付-储值-任意一个钻石
     */
    public static final String EVENT_PAY_RECHARGE_TOKEN = "pay-recharge-token";

    /**
     * 支付-确认支付
     */
    public static final String EVENT_PAY_CONFIRM = "pay-confirm";

    /**
     * 首页弹出广告
     */
    public static final String EVENT_HOME_POP_AD = "home-pop-ad";

    /**
     * 电台弹出广告
     */
    public static final String EVENT_RADIO_POP_AD = "radio-pop-ad";

    /**
     * 个人中心广告
     */
    public static final String EVENT_USER_CENTER_TOP_AD = "user-center-top-ad";

    private String eventId;

    public UMengCustomEvent(String eventId) {
        this.eventId = eventId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
