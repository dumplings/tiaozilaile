package com.example.tiaozilaile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiaozilaile.ui.theme.TiaozilaileTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TiaozilaileTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SettingsScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var logContent by remember { mutableStateOf("加载中...") }
    var logFileSize by remember { mutableStateOf(0L) }
    var logLineCount by remember { mutableStateOf(0) }
    
    // 加载日志信息
    fun loadLogInfo() {
        logContent = LogManager.getLogContent()
        logFileSize = LogManager.getLogFileSize()
        logLineCount = LogManager.getLogLineCount()
    }
    
    // 初始化时加载日志信息
    loadLogInfo()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "设置",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // 日志管理卡片
        Card(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "日志管理",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                // 日志统计信息
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("文件大小: ${logFileSize} bytes")
                    Text("日志行数: $logLineCount")
                }
                
                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            EmailSender.sendLogsByEmail(context)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("发送日志邮件")
                    }
                    
                    Button(
                        onClick = {
                            LogManager.clearLogs()
                            loadLogInfo()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("清空日志")
                    }
                }
                
                Button(
                    onClick = {
                        loadLogInfo()
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("刷新日志信息")
                }
                
                // 日志预览
                Text(
                    text = "日志预览（最多显示最后100行）:",
                    fontWeight = FontWeight.Medium
                )
                
                val previewLines = logContent.lines().takeLast(100)
                Text(
                    text = previewLines.joinToString("\n"),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }
        }
        
        // 应用信息卡片
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "应用信息",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text("版本: 1.0.0")
                Text("邮件支持: dumplings0415@gmail.com")
                Text("日志记录: 最多2000行滚动记录")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    TiaozilaileTheme {
        SettingsScreen()
    }
}