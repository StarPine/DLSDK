package com.dl.rtc.calling

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.blankj.utilcode.util.ServiceUtils

/**
 *Author: 彭石林
 *Time: 2022/11/3 15:32
 * Description: This is DLRTCCallService
 */
class DLRTCCallService : Service() {

    private val NOTIFICATION_ID : Int = 1001

    override fun onCreate() {
        super.onCreate()
        // 获取服务通知
        val notification = createForegroundNotification()
        //将服务置于启动状态 ,NOTIFICATION_ID指的是创建的通知的ID
        startForeground(NOTIFICATION_ID, notification)
    }
    companion object{
        fun start(context: Context) {
            if (ServiceUtils.isServiceRunning(DLRTCCallService::class.java)) {
                return
            }
            val starter = Intent(context, DLRTCCallService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(starter)
            } else {
                context.startService(starter)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, DLRTCCallService::class.java)
            context.stopService(intent)
        }
    }

    private fun createForegroundNotification(): Notification {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // 唯一的通知通道的id.
        val notificationChannelId = "notification_channel_id_01"
        // Android8.0以上的系统，新建消息通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //用户可见的通道名称
            val channelName = "TRTC Foreground Service Notification"
            //通道的重要程度
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(
                notificationChannelId, channelName, importance
            )
            notificationChannel.description = "Channel description"
            //震动
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val builder = NotificationCompat.Builder(this, notificationChannelId)
        //创建通知并返回
        return builder.build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }
}