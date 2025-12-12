package com.example.tiaozilaile

import android.content.Context
import android.os.Process
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.locks.ReentrantLock

/**
 * 日志管理器
 * 实现滚动记录，最多保存2000行日志
 */
object LogManager {
    private const val MAX_LOG_LINES = 2000
    private const val LOG_FILE_NAME = "app_log.txt"
    private val lock = ReentrantLock()
    private var logFile: File? = null
    
    // 自定义异常处理器
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null
    
    /**
     * 初始化日志系统
     */
    fun initialize(context: Context) {
        logFile = File(context.filesDir, LOG_FILE_NAME)
        
        // 保存默认异常处理器
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        // 设置自定义异常处理器
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // 记录崩溃日志
            logError("CRASH", "应用崩溃: ${throwable.message}", throwable)
            
            // 调用默认处理器
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
    
    /**
     * 记录信息日志
     */
    fun logInfo(tag: String, message: String) {
        log("INFO", tag, message, null)
    }
    
    /**
     * 记录错误日志
     */
    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        log("ERROR", tag, message, throwable)
    }
    
    /**
     * 记录警告日志
     */
    fun logWarning(tag: String, message: String) {
        log("WARN", tag, message, null)
    }
    
    /**
     * 记录调试日志
     */
    fun logDebug(tag: String, message: String) {
        log("DEBUG", tag, message, null)
    }
    
    private fun log(level: String, tag: String, message: String, throwable: Throwable?) {
        lock.lock()
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            var logEntry = "[$timestamp] [$level] [$tag] $message"
            
            if (throwable != null) {
                logEntry += "\n${Log.getStackTraceString(throwable)}"
            }
            
            // 同时输出到系统日志
            when (level) {
                "ERROR" -> Log.e(tag, message, throwable)
                "WARN" -> Log.w(tag, message, throwable)
                "INFO" -> Log.i(tag, message)
                "DEBUG" -> Log.d(tag, message)
            }
            
            // 写入文件
            writeToFile(logEntry)
        } finally {
            lock.unlock()
        }
    }
    
    private fun writeToFile(logEntry: String) {
        val file = logFile ?: return
        
        try {
            if (!file.exists()) {
                file.createNewFile()
            }
            
            // 检查文件行数，如果超过限制则滚动删除
            val lines = file.readLines()
            if (lines.size >= MAX_LOG_LINES) {
                // 删除前50%的日志行
                val linesToKeep = lines.drop(lines.size / 2)
                file.writeText(linesToKeep.joinToString("\n") + "\n")
            }
            
            // 追加新日志
            FileOutputStream(file, true).use { fos ->
                fos.write("$logEntry\n".toByteArray())
            }
        } catch (e: Exception) {
            Log.e("LogManager", "写入日志文件失败", e)
        }
    }
    
    /**
     * 获取所有日志内容
     */
    fun getLogContent(): String {
        val file = logFile ?: return ""
        
        return try {
            if (file.exists()) {
                file.readText()
            } else {
                "暂无日志记录"
            }
        } catch (e: Exception) {
            "读取日志失败: ${e.message}"
        }
    }
    
    /**
     * 清空日志文件
     */
    fun clearLogs() {
        lock.lock()
        try {
            logFile?.delete()
        } finally {
            lock.unlock()
        }
    }
    
    /**
     * 获取日志文件大小
     */
    fun getLogFileSize(): Long {
        return logFile?.length() ?: 0
    }
    
    /**
     * 获取日志行数
     */
    fun getLogLineCount(): Int {
        val file = logFile ?: return 0
        
        return try {
            if (file.exists()) {
                file.readLines().size
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }
}