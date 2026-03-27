package com.airport.quiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

// 設計系統顏色 Token
val primaryIndigo = Color(0xFF4F46E5)
val primaryIndigoLight = Color(0xFF818CF8)
val slate900 = Color(0xFF0F172A)
val slate700 = Color(0xFF334155)
val slate500 = Color(0xFF64748B)

val bgGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFE0E7FF), Color(0xFFF8FAFC))
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // 全域背景漸層
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgGradient)
                ) {
                    AppContent()
                }
            }
        }
    }
}

@Composable
fun AppContent(viewModel: QuizViewModel = viewModel()) {
    var isAuthenticated by remember { mutableStateOf(false) }

    Crossfade(targetState = isAuthenticated, label = "auth_crossfade") { auth ->
        if (!auth) {
            PasswordView(onAuthenticated = { isAuthenticated = true })
        } else {
            AnimatedContent(
                targetState = Pair(viewModel.isStarted, viewModel.showResults),
                label = "quiz_navigation"
            ) { (started, showResults) ->
                if (!started) {
                    StartView { viewModel.startQuiz() }
                } else if (showResults) {
                    ResultsView(viewModel = viewModel, onReset = { viewModel.isStarted = false })
                } else {
                    QuizView(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun PasswordView(onAuthenticated: () -> Unit) {
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Lock",
                    tint = primaryIndigo,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 16.dp)
                )
                
                Text(
                    "安全驗證",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = slate900,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("請輸入進入密碼") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryIndigo,
                        focusedLabelColor = primaryIndigo
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                
                AnimatedVisibility(visible = errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                Button(
                    onClick = {
                        if (password.isBlank()) return@Button
                        isLoading = true
                        errorMessage = null
                        
                        scope.launch {
                            try {
                                val remotePassword = withContext(Dispatchers.IO) {
                                    val timestamp = System.currentTimeMillis()
                                    URL("https://raw.githubusercontent.com/Chu0019/-/main/password.txt?t=$timestamp").readText().trim()
                                }
                                if (password.trim() == remotePassword) {
                                    onAuthenticated()
                                } else {
                                    errorMessage = "密碼錯誤，請重新輸入"
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                errorMessage = "無法連線驗證密碼，請檢查網路連線狀態"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading && password.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryIndigo),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("登入", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StartView(onStart: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "✈️",
                fontSize = 80.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                "桃園國際機場",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = slate500,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "空側駕駛許可證測驗",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = slate900,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // 資訊卡片
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoRow(icon = "📚", text = "完整題庫 304 題")
                    InfoRow(icon = "🎲", text = "隨機抽取 20 題模擬測驗")
                    InfoRow(icon = "✅", text = "80分及格標準")
                }
            }

            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = primaryIndigo),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Text("開始測驗", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun InfoRow(icon: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 20.sp, modifier = Modifier.width(32.dp))
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = slate700)
    }
}

@Composable
fun QuizView(viewModel: QuizViewModel) {
    val currentQ = viewModel.questions[viewModel.currentIndex]
    
    // 平滑進度條動畫
    val progress by animateFloatAsState(
        targetValue = (viewModel.currentIndex + 1).toFloat() / viewModel.questions.size.toFloat(),
        animationSpec = tween(durationMillis = 500),
        label = "progress_anim"
    )
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 頂部進度區塊
        Card(
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        "題目 ${viewModel.currentIndex + 1} / ${viewModel.questions.size}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryIndigo
                    )
                    Text(
                        "${((viewModel.currentIndex + 1) * 100 / viewModel.questions.size)}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = slate500
                    )
                }
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = primaryIndigo,
                    trackColor = primaryIndigo.copy(alpha = 0.1f)
                )
            }
        }
        
        // 考題內容
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = viewModel.currentIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }.using(SizeTransform(clip = false))
                },
                label = "question_transition"
            ) { targetIndex ->
                val q = viewModel.questions[targetIndex]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Text(
                        text = q.q,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = slate900,
                        lineHeight = 32.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    if (q.image != null) {
                        val context = LocalContext.current
                        val imageResId = context.resources.getIdentifier(q.image, "drawable", context.packageName)
                        if (imageResId != 0) {
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .padding(bottom = 24.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    q.shuffledOptions.forEachIndexed { optIndex, option ->
                        if (option.isNotEmpty()) {
                            OptionButton(
                                text = option,
                                isSelected = viewModel.selectedAnswers[targetIndex] == optIndex,
                                onClick = { viewModel.setAnswer(optIndex) }
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }
            }
        }
        
        // 底部導航
        Card(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (viewModel.currentIndex > 0) {
                    OutlinedButton(
                        onClick = { viewModel.currentIndex -= 1 },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, primaryIndigo.copy(alpha = 0.2f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryIndigo),
                        modifier = Modifier.size(56.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
                
                Button(
                    onClick = {
                        if (viewModel.currentIndex < viewModel.questions.size - 1) {
                            viewModel.currentIndex += 1
                        } else {
                            viewModel.submit()
                        }
                    },
                    enabled = viewModel.selectedAnswers[viewModel.currentIndex] != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.currentIndex < viewModel.questions.size - 1) primaryIndigo else Color(0xFF10B981)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = if (viewModel.currentIndex < viewModel.questions.size - 1) "下一題" else "交卷計分",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OptionButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(if (isSelected) Color(0xFFEEF2FF) else Color.White, label = "bg")
    val borderColor by animateColorAsState(if (isSelected) primaryIndigo else Color.Transparent, label = "border")
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isSelected) 0.dp else 2.dp, RoundedCornerShape(16.dp))
            .background(bgColor, RoundedCornerShape(16.dp))
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 17.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) primaryIndigo else slate700,
                modifier = Modifier.weight(1f),
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.White, CircleShape)
                    .border(2.dp, if (isSelected) primaryIndigo else Color.LightGray.copy(alpha=0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isSelected,
                    enter = scaleIn(),
                    exit = scaleOut()
                ) {
                    Box(modifier = Modifier.size(16.dp).background(primaryIndigo, CircleShape))
                }
            }
        }
    }
}

@Composable
fun ResultsView(viewModel: QuizViewModel, onReset: () -> Unit) {
    // 圓環動畫
    val animatedScore by animateFloatAsState(
        targetValue = viewModel.score.toFloat() / 20f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "score_anim"
    )
    
    val isPassed = viewModel.score >= 16
    val resultColor = if (isPassed) Color(0xFF10B981) else Color(0xFFF59E0B)
    
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "測驗成績",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = slate900,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                            // 背景環
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = Color.LightGray.copy(alpha = 0.3f),
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            // 進度環
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = resultColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f * animatedScore,
                                    useCenter = false,
                                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${viewModel.score * 5}",
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Black,
                                    color = slate900
                                )
                                Text(
                                    text = "分",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = slate500
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = if (isPassed) "恭喜通過標核！" else "不及格，請再加油。",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = resultColor
                        )
                        Text(
                            text = "答對 ${viewModel.score} 題 / 總計 20 題",
                            fontSize = 16.sp,
                            color = slate500,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                
                Text(
                    "答題詳情",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = slate900,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
            
            val sortedIndices = viewModel.questions.indices.sortedBy { index ->
                if (viewModel.selectedAnswers[index] == viewModel.questions[index].correctIndex) 1 else 0
            }
            
            items(sortedIndices.size) { i ->
                val qIndex = sortedIndices[i]
                val q = viewModel.questions[qIndex]
                val selected = viewModel.selectedAnswers[qIndex]
                val isCorrect = selected == q.correctIndex
                
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha=0.9f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${qIndex + 1}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                q.q,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = slate900,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (isCorrect) Icons.Filled.Check else Icons.Filled.Close,
                                contentDescription = null,
                                tint = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                        
                        Column(modifier = Modifier.padding(start = 40.dp)) {
                            Row(modifier = Modifier.padding(bottom = 6.dp)) {
                                Text("您的回答：", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isCorrect) slate500 else Color(0xFFEF4444))
                                Text(selected?.let { q.shuffledOptions[it] } ?: "未作答", fontSize = 14.sp, color = if (isCorrect) slate500 else Color(0xFFEF4444))
                            }
                            
                            Row {
                                Text("正確答案：", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                Text(q.shuffledOptions[q.correctIndex], fontSize = 14.sp, color = Color(0xFF10B981))
                            }
                        }
                    }
                }
            }
        }
        
        // 底部按鈕
        Card(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha=0.9f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.startQuiz() },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, primaryIndigo),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("重新考題", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryIndigo)
                }
                
                Button(
                    onClick = onReset,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryIndigo),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("返回主畫面", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
