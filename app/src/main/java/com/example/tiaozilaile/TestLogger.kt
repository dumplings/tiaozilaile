package com.example.tiaozilaile

import android.content.Context

/**
 * 日志功能测试类
 */
object TestLogger {
    
    /**
     * 测试日志记录功能
     */
    fun testLogging(context: Context) {
        // 初始化日志管理器
        LogManager.initialize(context)
        
        // 记录各种类型的日志
        LogManager.logInfo("TestLogger", "开始日志功能测试")
        LogManager.logDebug("TestLogger", "这是调试信息")
        LogManager.logWarning("TestLogger", "这是警告信息")
        
        // 测试错误日志
        try {
            throw RuntimeException("测试异常")
        } catch (e: Exception) {
            LogManager.logError("TestLogger", "捕获到测试异常", e)
        }
        
        LogManager.logInfo("TestLogger", "日志功能测试完成")
        
        // 输出日志统计信息
        val size = LogManager.getLogFileSize()
        val lines = LogManager.getLogLineCount()
        LogManager.logInfo("TestLogger", "日志文件大小: $size bytes, 行数: $lines")
    }
    
    /**
     * 测试邮件发送功能
     */
    fun testEmailSending(context: Context) {
        LogManager.logInfo("TestLogger", "测试邮件发送功能")
        
        // 检查邮件功能是否可用
        val isAvailable = EmailSender.isEmailAvailable(context)
        LogManager.logInfo("TestLogger", "邮件功能可用: $isAvailable")
        
        if (isAvailable) {
            // 发送测试邮件
            EmailSender.sendLogsByEmail(context)
            LogManager.logInfo("TestLogger", "邮件发送请求已发出")
        } else {
            LogManager.logWarning("TestLogger", "设备不支持邮件发送")
        }
    }
    
    /**
     * 测试日志滚动功能
     */
    fun testLogRolling(context: Context) {
        LogManager.initialize(context)
        
        // 生成大量日志以测试滚动功能
        for (i in 1..2100) {
            LogManager.logInfo("TestLogger", "测试日志行 $i")
        }
        
        val finalLines = LogManager.getLogLineCount()
        LogManager.logInfo("TestLogger", "滚动测试完成，最终行数: $finalLines")
    }
}