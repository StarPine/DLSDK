package com.dl.rtc.calling.model

class DLRTCStartModel {
  ///当前model的类型，参考DLRTCDataMessageType的值
  var rtcDataMessageType : String = ""
  ///接收方的用户ID，发起方是自己，接收方就是对端的id
  ///如果发起方是对端，接收方就是自己的用户ID
  var acceptUserId : String = ""
  ///发起邀请的用户ID,发起方是自己，就是自己的用户id
  ///发起方是对端，就是对端的用户ID
  var inviteUserId : String = ""
  ///发起邀请之后，腾讯生成的一个邀请id，需要每次都传过来，用于校验
  var inviteId : String = ""
  ///发起类型的值，是语音邀请，还是连麦邀请，详见DLInviteRTCType的值
  var rtcInviteType : String = ""
  ///发起邀请，生成的RTC房间号，字符串类型，用于后续的扩展
  var rtcInviteStrRoomId : String = ""
  ///发起邀请，生成的RTC房间号，字符串类型，用于后续的扩展
  var rtcInviteRoomId : Int = 0

  override fun toString(): String {
    return "DLRTCStartModel(rtcDataMessageType='$rtcDataMessageType', acceptUserId='$acceptUserId', inviteUserId='$inviteUserId', inviteId='$inviteId', rtcInviteType='$rtcInviteType', rtcInviteStrRoomId='$rtcInviteStrRoomId', rtcInviteRoomId=$rtcInviteRoomId)"
  }


}