@file:Suppress("DEPRECATION", "ExperimentalMaterialApi", "UnstableApi")

package com.example.tiaozilaile

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.provider.Telephony
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.app.NotificationCompat

/**
 * 短信监听服务
 * 使用前台服务+动态广播接收器实现后台持续监听
 */
@androidx.annotation.RequiresApi(Build.VERSION_CODES.O)
class SmsListenerService : Service() {
    
    // 监听状态回调接口
    interface SmsListenerCallback {
        fun onSmsReceived(fromNumber: String, message: String)
    }
    
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "sms_listener_channel"
        private const val NOTIFICATION_ID = 1
        private const val SMS_RECEIVED_ACTION = Telephony.Sms.Intents.SMS_RECEIVED_ACTION
        
        private var callback: SmsListenerCallback? = null
        
        fun setCallback(listener: SmsListenerCallback) {
            callback = listener
        }
        
        fun removeCallback() {
            callback = null
        }
    }
    
    private lateinit var smsReceiver: SmsReceiver
    private var targetPhoneNumber: String? = null
    
    override fun onCreate() {
        super.onCreate()
        smsReceiver = SmsReceiver()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        targetPhoneNumber = intent?.getStringExtra("target_phone_number")
        
        try {
            // 创建通知渠道
            createNotificationChannel()
            
            // 注册短信接收广播
            val filter = IntentFilter(SMS_RECEIVED_ACTION)
            filter.priority = IntentFilter.SYSTEM_HIGH_PRIORITY
            registerReceiver(smsReceiver, filter)
            
            // 启动前台服务
            startForeground(NOTIFICATION_ID, createNotification())
        } catch (e: Exception) {
            e.printStackTrace()
            // 如果启动失败，停止服务
            stopSelf()
        }
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(smsReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        removeCallback()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "短信监听服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "用于后台监听指定号码的短信"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Suppress("ExperimentalMaterialApi")
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        @Suppress("ExperimentalMaterialApi")
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("条子来了")
            .setContentText("正在监听短信")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    /**
     * 短信接收广播接收器
     */
    private inner class SmsReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == SMS_RECEIVED_ACTION) {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                if (messages.isNotEmpty()) {
                    val messageBody = StringBuilder()
                    var fromNumber: String? = null
                    
                    for (message in messages) {
                        messageBody.append(message.messageBody)
                        if (fromNumber == null) {
                            fromNumber = message.originatingAddress
                        }
                    }
                    
                    // 检查是否是指定号码的短信
                    if (fromNumber != null && targetPhoneNumber != null && 
                        fromNumber.contains(targetPhoneNumber!!)) {
                        
                        // 触发回调
                        callback?.onSmsReceived(fromNumber, messageBody.toString())
                    }
                }
            }
        }
    }
}

/**
 * 短信监听管理器
 */
object SmsListenerManager {
    
    /**
     * 启动短信监听服务
     */
    fun startListening(context: Context, targetPhoneNumber: String) {
        val intent = Intent(context, SmsListenerService::class.java).apply {
            putExtra("target_phone_number", targetPhoneNumber)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
    
    /**
     * 停止短信监听服务
     */
    fun stopListening(context: Context) {
        val intent = Intent(context, SmsListenerService::class.java)
        context.stopService(intent)
    }
    
    /**
     * 设置短信接收回调
     */
    fun setCallback(callback: SmsListenerService.SmsListenerCallback) {
        SmsListenerService.setCallback(callback)
    }
    
    /**
     * 移除回调
     */
    fun removeCallback() {
        SmsListenerService.removeCallback()
    }
}