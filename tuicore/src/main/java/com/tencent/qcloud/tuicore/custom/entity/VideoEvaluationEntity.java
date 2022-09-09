package com.tencent.qcloud.tuicore.custom.entity;

import java.io.Serializable;

/**
 * 修改备注：视讯推送评价
 *
 * @Name： PlayFun_Google
 * @Description：
 * @Author： liaosf
 * @Date： 2022/9/9 12:28
 */
public class VideoEvaluationEntity implements Serializable {
    private int videoCallPushLogId;
    private String avatar;

    public int getVideoCallPushLogId() {
        return videoCallPushLogId;
    }

    public void setVideoCallPushLogId(int videoCallPushLogId) {
        this.videoCallPushLogId = videoCallPushLogId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
