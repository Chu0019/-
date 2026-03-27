package com.airport.quiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CircularProgressIndicator

val primaryIndigo = Color(0xFF5C6BC0)
val slate900 = Color(0xFF0F172A)
val slate700 = Color(0xFF334155)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF1F5F9)
                ) {
                    AppContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppContent(viewModel: QuizViewModel = viewModel()) {
    var isAuthenticated by remember { mutableStateOf(false) }

    Crossfade(targetState = isAuthenticated) { auth ->
        if (!auth) {
            PasswordView(onAuthenticated = { isAuthenticated = true })
        } else {
            AnimatedContent(targetState = Triple(viewModel.isStarted, viewModel.showResults, viewModel.currentIndex)) { (started, showResults, _) ->
                if (!started) {
                    StartView {
                        viewModel.startQuiz()
                    }
                } else if (showResults) {
                    ResultsView(viewModel = viewModel, onReset = {
                        viewModel.isStarted = false
                    })
                } else {
                    QuizView(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun StartView(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "桃園機場空側駕駛許可證測驗",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = slate900,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            "完整題庫 304 題隨機測驗",
            fontSize = 16.sp,
            color = slate700,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = primaryIndigo),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("開始測驗", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PasswordView(onAuthenticated: () -> Unit) {
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "桃園機場駕駛許可證測驗\n安全驗證",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = slate900,
            modifier = Modifier.padding(bottom = 24.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("請輸入進入密碼") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )
        
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
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
                            URL("https://raw.githubusercontent.com/Chu0019/-/main/password.txt").readText().trim()
                        }
                        if (password == remotePassword) {
                            onAuthenticated()
                        } else {
                            errorMessage = "密碼錯誤，請重新輸入"
                        }
                    } catch (e: Exception) {
                        errorMessage = "無法連線驗證密碼，請檢查網路連線狀態"
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading && password.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = primaryIndigo),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("登入", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuizView(viewModel: QuizViewModel) {
    val currentQ = viewModel.questions[viewModel.currentIndex]
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "題目 ${viewModel.currentIndex + 1} / ${viewModel.questions.size}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = primaryIndigo
            )
            LinearProgressIndicator(
                progress = (viewModel.currentIndex + 1).toFloat() / viewModel.questions.size.toFloat(),
                modifier = Modifier.width(100.dp),
                color = primaryIndigo,
            )
        }
        
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                currentQ.q,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = slate900,
                lineHeight = 30.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            if (currentQ.image != null) {
                val context = LocalContext.current
                val imageResId = context.resources.getIdentifier(currentQ.image, "drawable", context.packageName)
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
            
            currentQ.shuffledOptions.forEachIndexed { index, option ->
                if (option.isNotEmpty()) {
                    OptionButton(
                        text = option,
                        isSelected = viewModel.selectedAnswers[viewModel.currentIndex] == index,
                        onClick = { viewModel.setAnswer(index) }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (viewModel.currentIndex > 0) {
                OutlinedButton(
                    onClick = { viewModel.currentIndex -= 1 },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(56.dp)
                ) {
                    Text("←")
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
                    containerColor = if (viewModel.currentIndex < viewModel.questions.size - 1) primaryIndigo else Color(0xFF22C55E)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(56.dp).weight(1f)
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

@Composable
fun OptionButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, if (isSelected) primaryIndigo else Color.LightGray),
        color = if (isSelected) primaryIndigo.copy(alpha = 0.1f) else Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 17.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) primaryIndigo else slate700,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.Transparent, CircleShape)
                    .border(2.dp, if (isSelected) primaryIndigo else Color.LightGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(modifier = Modifier.size(14.dp).background(primaryIndigo, CircleShape))
                }
            }
        }
    }
}

@Composable
fun ResultsView(viewModel: QuizViewModel, onReset: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "測驗結果",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = slate900
                    )
                    Text(
                        "得分: ${viewModel.score} / 20",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (viewModel.score >= 18) Color(0xFF22C55E) else Color.Red
                    )
                }
            }
            
            val sortedIndices = viewModel.questions.indices.sortedBy { index ->
                if (viewModel.selectedAnswers[index] == viewModel.questions[index].correctIndex) 1 else 0
            }
            
            items(sortedIndices.size) { i ->
                val qIndex = sortedIndices[i]
                val q = viewModel.questions[qIndex]
                val selected = viewModel.selectedAnswers[qIndex]
                
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "第 ${qIndex + 1} 題",
                            fontSize = 14.sp,
                            color = primaryIndigo,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            q.q,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = slate900,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Row {
                            Text("您的回答：", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (selected == q.correctIndex) Color.Gray else Color.Red)
                            Text(selected?.let { q.shuffledOptions[it] } ?: "未作答", fontSize = 14.sp, color = if (selected == q.correctIndex) Color.Gray else Color.Red)
                        }
                        
                        Row(modifier = Modifier.padding(top = 4.dp)) {
                            Text("正確答案：", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF22C55E))
                            Text(q.shuffledOptions[q.correctIndex], fontSize = 14.sp, color = Color(0xFF22C55E))
                        }
                    }
                }
            }
        }
        
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.startQuiz() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
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
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("返回主畫面", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
