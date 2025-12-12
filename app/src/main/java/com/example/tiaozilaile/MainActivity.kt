package com.example.tiaozilaile

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiaozilaile.ui.theme.TiaozilaileTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TiaozilaileTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var phoneNumber by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    val rippleAnimation = remember { Animatable(0f) }
    
    // 从SharedPreferences加载保存的电话号码
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }
    
    // 初始化时加载保存的电话号码
    val savedPhoneNumber = remember {
        sharedPreferences.getString("phone_number", "") ?: ""
    }
    
    if (phoneNumber.isEmpty() && savedPhoneNumber.isNotEmpty()) {
        phoneNumber = savedPhoneNumber
    }
    
    // 波纹动画效果
    LaunchedEffect(isListening) {
        if (isListening) {
            rippleAnimation.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1500),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            rippleAnimation.snapTo(0f)
        }
    }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            // 电话号码输入框
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { if (isEditing) phoneNumber = it },
                label = { Text("电话号码") },
                placeholder = { Text("请输入电话号码") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                enabled = isEditing,
                modifier = Modifier.padding(bottom = 48.dp)
            )
            
            // 编辑/保存按钮
            Button(
                onClick = {
                    if (isEditing) {
                        // 保存电话号码到SharedPreferences
                        sharedPreferences.edit()
                            .putString("phone_number", phoneNumber)
                            .apply()
                    }
                    isEditing = !isEditing
                },
                modifier = Modifier.padding(bottom = 64.dp)
            ) {
                Text(if (isEditing) "保存" else "编辑")
            }
            
            // 大的圆形按钮与波纹效果
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // 波纹效果
                if (isListening) {
                    Canvas(
                        modifier = Modifier.size(160.dp)
                    ) {
                        val radius = 80.dp.toPx() * rippleAnimation.value
                        drawCircle(
                            color = Color.Red.copy(alpha = 0.3f),
                            radius = radius,
                            style = Stroke(width = 4f)
                        )
                    }
                }
                
                // 大的圆形按钮
                Button(
                    onClick = {
                        isListening = !isListening
                        if (isListening) {
                            // TODO: 启动监听功能
                        } else {
                            // TODO: 停止监听功能
                        }
                    },
                    enabled = phoneNumber.isNotEmpty() && !isEditing,
                    modifier = Modifier.size(140.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListening) Color.Red else ButtonDefaults.buttonColors().containerColor
                    )
                ) {
                    Text(
                        text = if (isListening) "监听中" else "启动",
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    TiaozilaileTheme {
        MainScreen()
    }
}