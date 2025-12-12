package com.example.tiaozilaile

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * 邮件发送工具类
 */
object EmailSender {
    
    /**
     * 通过邮件发送日志文件
     */
    fun sendLogsByEmail(context: Context, recipient: String = "dumplings0415@gmail.com") {
        try {
            val logContent = LogManager.getLogContent()
            
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
                putExtra(Intent.EXTRA_SUBJECT, "条子来了 - 应用日志报告")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "以下是应用的运行日志：\n\n" +
                    "日志文件大小: ${LogManager.getLogFileSize()} bytes\n" +
                    "日志行数: ${LogManager.getLogLineCount()}\n\n" +
                    "--- 日志内容开始 ---\n" +
                    logContent +
                    "\n--- 日志内容结束 ---"
                )
            }
            
            // 检查是否有邮件应用可以处理此Intent
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(Intent.createChooser(intent, "选择邮件应用"))
            } else {
                // 如果没有邮件应用，尝试使用其他方式分享
                shareLogsViaOtherApps(context, logContent)
            }
        } catch (e: Exception) {
            LogManager.logError("EmailSender", "发送邮件失败: ${e.message}", e)
        }
    }
    
    /**
     * 通过其他应用分享日志
     */
    private fun shareLogsViaOtherApps(context: Context, logContent: String) {
        try {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    "条子来了应用日志\n\n" +
                    "日志大小: ${LogManager.getLogFileSize()} bytes\n" +
                    "日志行数: ${LogManager.getLogLineCount()}\n\n" +
                    "请将以下内容发送到 dumplings0415@gmail.com\n\n" +
                    logContent
                )
            }
            
            context.startActivity(Intent.createChooser(shareIntent, "分享日志"))
        } catch (e: Exception) {
            LogManager.logError("EmailSender", "分享日志失败: ${e.message}", e)
        }
    }
    
    /**
     * 检查设备是否支持邮件发送
     */
    fun isEmailAvailable(context: Context): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.resolveActivity(context.packageManager) != null
        } catch (e: Exception) {
            false
        }
    }
}