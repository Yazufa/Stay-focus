package com.example.ui.lockscreen

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.math.MathQuestGenerator
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class LockScreenActivity : ComponentActivity() {

    private val viewModel: LockScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: "Aplikasi"
        val questLevel = intent.getIntExtra(EXTRA_QUEST_LEVEL, 1)
        val isAntiUninstall = intent.getBooleanExtra(EXTRA_IS_ANTI_UNINSTALL, false)

        viewModel.initialize(packageName, appName, questLevel, isAntiUninstall)

        setContent {
            com.example.ui.theme.MyApplicationTheme(darkTheme = true) {
                LockScreenContent(
                    appName = appName,
                    viewModel = viewModel,
                    isAntiUninstall = isAntiUninstall,
                    onGoHome = { navigateToHome() },
                    onUnlockCompleted = { finish() }
                )
            }
        }
    }

    private fun navigateToHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_QUEST_LEVEL = "extra_quest_level"
        const val EXTRA_IS_ANTI_UNINSTALL = "extra_is_anti_uninstall"
    }
}

@Composable
fun LockScreenContent(
    appName: String,
    viewModel: LockScreenViewModel,
    isAntiUninstall: Boolean,
    onGoHome: () -> Unit,
    onUnlockCompleted: () -> Unit
) {
    BackHandler { onGoHome() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val questLevel by viewModel.questLevel.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val enteredAnswer by viewModel.enteredAnswer.collectAsState()
    val isWrong by viewModel.isAnswerWrong.collectAsState()
    val questCompleted by viewModel.questCompleted.collectAsState()
    val earnedPoints by viewModel.earnedPoints.collectAsState()

    val reasonState by viewModel.reasonState.collectAsState()
    val dailyBypassUsed by viewModel.dailyBypassUsed.collectAsState()
    val dailyBypassLimit by viewModel.dailyBypassLimit.collectAsState()

    var reasonInput by remember { mutableStateOf("") }

    val shakeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isWrong) {
        if (isWrong) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 350
                    0f at 0
                    -25f at 50
                    25f at 100
                    -20f at 150
                    20f at 200
                    -10f at 250
                    10f at 300
                    0f at 350
                }
            )
        }
    }

    Scaffold(
        containerColor = Slate950,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedButton(
                    onClick = onGoHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("close_app_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate400),
                    border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(Slate800, Slate700)))
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isAntiUninstall) "Tutup & Kembali ke Beranda" else "Tutup Aplikasi & Tetap Fokus",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Lock Hero Header
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                (if (isAntiUninstall) RoseDanger else IndigoPrimary).copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Slate900)
                        .border(1.5.dp, if (isAntiUninstall) RoseDanger else IndigoPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAntiUninstall) Icons.Default.Security else Icons.Default.Lock,
                        contentDescription = "Terkunci",
                        tint = if (isAntiUninstall) RoseDanger else CyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isAntiUninstall) "PERLINDUNGAN ANTI-HAPUS" else "Fokus Terjaga",
                style = MaterialTheme.typography.labelLarge,
                color = if (isAntiUninstall) RoseDanger else CyanAccent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = appName,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (isAntiUninstall)
                    "Akses pengaturan/uninstall dicegah. Selesaikan quest untuk konfirmasi jeda 24 jam."
                else
                    "Aplikasi ini dibatasi oleh jadwal fokus Anda.",
                style = MaterialTheme.typography.bodySmall,
                color = Slate400,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Only show AI reason tab if NOT anti-uninstall mode
            if (!isAntiUninstall) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Slate900,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = IndigoPrimary
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Quest MTK", fontWeight = FontWeight.Bold)
                            }
                        },
                        modifier = Modifier.testTag("tab_math_quest")
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Alasan (AI)", fontWeight = FontWeight.Bold)
                            }
                        },
                        modifier = Modifier.testTag("tab_reason_ai")
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // TAB 0: MATH QUEST (KEYPAD-BASED)
            if (selectedTabIndex == 0 || isAntiUninstall) {
                if (questCompleted) {
                    VictoryCard(
                        earnedPoints = earnedPoints,
                        isAntiUninstall = isAntiUninstall,
                        onUnlock = {
                            if (isAntiUninstall) onGoHome() else onUnlockCompleted()
                        }
                    )
                } else if (questions.isNotEmpty()) {
                    val currentQ = questions.getOrElse(currentQuestionIndex) { questions.first() }
                    val levelInfo = MathQuestGenerator.getLevelInfo(questLevel)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                        border = BorderStroke(
                            1.dp,
                            Brush.linearGradient(
                                listOf(CardBorderDark, if (isAntiUninstall) RoseDanger.copy(alpha = 0.5f) else IndigoPrimary.copy(alpha = 0.5f))
                            )
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Level ${levelInfo.level}: ${levelInfo.name}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isAntiUninstall) RoseDanger else CyanAccent,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Soal ${currentQuestionIndex + 1}/${questions.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Slate400,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = (currentQuestionIndex + 1).toFloat() / questions.size.toFloat(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (isAntiUninstall) RoseDanger else IndigoPrimary,
                                trackColor = Slate800,
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Math Question Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Slate900)
                                    .border(1.dp, CardBorderDark, RoundedCornerShape(14.dp))
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${currentQ.prompt} = ?",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Entered Answer Display Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isWrong) RoseDanger.copy(alpha = 0.15f) else Slate950)
                                    .border(
                                        1.5.dp,
                                        if (isWrong) RoseDanger else if (enteredAnswer.isNotEmpty()) CyanAccent else Slate800,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (enteredAnswer.isEmpty()) {
                                    Text(
                                        text = "Ketik jawaban...",
                                        fontSize = 18.sp,
                                        color = Slate700,
                                        fontWeight = FontWeight.Normal
                                    )
                                } else {
                                    Text(
                                        text = enteredAnswer,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }

                            if (isWrong) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = RoseDanger, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Jawaban salah! Soal baru diganti otomatis.",
                                        color = RoseDanger,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Custom On-Screen Keypad
                            NumericKeypad(
                                onDigitClick = { digit -> viewModel.appendDigit(digit) },
                                onBackspace = { viewModel.deleteDigit() },
                                onClear = { viewModel.clearDigits() }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Interactive Slide to Confirm / Submit
                            SlideToConfirmButton(
                                isEnabled = enteredAnswer.isNotBlank(),
                                onConfirm = {
                                    viewModel.submitAnswer { isCorrect, isFinished ->
                                        if (isFinished) {
                                            if (isAntiUninstall) onGoHome() else onUnlockCompleted()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // TAB 1: REASON (AI JUDGE)
            if (selectedTabIndex == 1 && !isAntiUninstall) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                    border = BorderStroke(1.dp, Brush.linearGradient(listOf(CardBorderDark, CyanAccent.copy(alpha = 0.5f)))),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Buka dengan Alasan Produktif",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Daily quota chip
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate900)
                                .border(1.dp, CardBorderDark, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            val remaining = (dailyBypassLimit - dailyBypassUsed).coerceAtLeast(0)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Sisa Kuota Bypass Hari Ini:", style = MaterialTheme.typography.bodySmall, color = Slate400)
                                Text(
                                    text = "$remaining / $dailyBypassLimit kali",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remaining > 0) EmeraldSuccess else RoseDanger
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Tuliskan alasan spesifik dan mendesak mengapa Anda harus mengakses $appName saat ini:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate200,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = reasonInput,
                            onValueChange = {
                                reasonInput = it
                                if (reasonState !is ReasonState.Idle) viewModel.resetReasonState()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("reason_input_field"),
                            placeholder = {
                                Text("Contoh: Perlu membalas pesan penting dari dosen/klien mengenai tugas...", color = Slate400, fontSize = 13.sp)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = CardBorderDark,
                                focusedContainerColor = Slate900,
                                unfocusedContainerColor = Slate900,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Slate200
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        when (val state = reasonState) {
                            is ReasonState.Idle -> {
                                val remaining = dailyBypassLimit - dailyBypassUsed
                                Button(
                                    onClick = {
                                        viewModel.evaluateReason(reasonInput) {
                                            // Handled in success state
                                        }
                                    },
                                    enabled = reasonInput.trim().length >= 5 && remaining > 0,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("submit_reason_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Slate950)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Evaluasi Alasan dengan AI", fontWeight = FontWeight.Bold)
                                }
                            }
                            is ReasonState.Loading -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(color = CyanAccent, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("AI sedang menganalisis alasan Anda...", color = CyanAccent, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            is ReasonState.Success -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = EmeraldSuccess.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, EmeraldSuccess),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Alasan Disetujui!", fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(state.result.comment, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = onUnlockCompleted,
                                            modifier = Modifier.fillMaxWidth().height(46.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Masuk ke Aplikasi Sekarang", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            is ReasonState.Rejected -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = RoseDanger.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, RoseDanger),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Warning, contentDescription = null, tint = RoseDanger)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Alasan Ditolak oleh AI", fontWeight = FontWeight.Bold, color = RoseDanger)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(state.result.comment, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = { selectedTabIndex = 0 },
                                            modifier = Modifier.fillMaxWidth().height(46.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Calculate, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Selesaikan Quest MTK Saja", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            is ReasonState.Error -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Slate900),
                                    border = BorderStroke(1.dp, RoseDanger),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(state.message, color = RoseDanger, style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = { selectedTabIndex = 0 },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Beralih ke Quest MTK", color = CyanAccent)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NumericKeypad(
    onDigitClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "⌫")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowKeys.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when (key) {
                                    "C" -> Slate900
                                    "⌫" -> Slate900
                                    else -> Slate900
                                }
                            )
                            .border(1.dp, CardBorderDark, RoundedCornerShape(10.dp))
                            .clickable {
                                when (key) {
                                    "C" -> onClear()
                                    "⌫" -> onBackspace()
                                    else -> onDigitClick(key)
                                }
                            }
                            .testTag("keypad_$key"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (key == "⌫") {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "Hapus",
                                tint = RoseDanger,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = key,
                                fontSize = if (key == "C") 16.sp else 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (key == "C") AmberWarning else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SlideToConfirmButton(
    isEnabled: Boolean,
    onConfirm: () -> Unit
) {
    val thumbSize = 52.dp
    var offsetX by remember { mutableFloatStateOf(0f) }
    val maxPx = 450f

    val animatedOffset by animateFloatAsState(targetValue = offsetX, label = "offset")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(if (isEnabled) Slate900 else Slate900.copy(alpha = 0.5f))
            .border(
                1.dp,
                if (isEnabled) IndigoPrimary.copy(alpha = 0.6f) else CardBorderDark,
                RoundedCornerShape(28.dp)
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = if (isEnabled) "Geser untuk konfirmasi  ➔" else "Ketik jawaban dahulu",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isEnabled) Slate200 else Slate400,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .size(thumbSize)
                .padding(3.dp)
                .clip(CircleShape)
                .background(if (isEnabled) IndigoPrimary else Slate700)
                .draggable(
                    state = rememberDraggableState { delta ->
                        if (isEnabled) {
                            offsetX = (offsetX + delta).coerceIn(0f, maxPx)
                        }
                    },
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        if (offsetX > maxPx * 0.65f) {
                            offsetX = 0f
                            onConfirm()
                        } else {
                            offsetX = 0f
                        }
                    }
                )
                .testTag("slide_confirm_thumb"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Geser",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun VictoryCard(
    earnedPoints: Int,
    isAntiUninstall: Boolean,
    onUnlock: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(EmeraldSuccess, CyanAccent))),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(EmeraldSuccess.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (isAntiUninstall) "Verifikasi Anti-Hapus Lolos!" else "Quest MTK Berhasil!",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isAntiUninstall)
                    "Anda telah menyelesaikan quest level tertinggi. Masa jeda aman 24 jam kini dimulai. Kembali ke Pengaturan untuk memantau waktu jeda."
                else
                    "Kerja bagus! Otak Anda telah teraktivasi. Anda mendapatkan +$earnedPoints poin fokus.",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate200,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onUnlock,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("enter_unlocked_app_button"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (isAntiUninstall) "Kembali ke Beranda" else "Buka Aplikasi Sekarang (15 Menit)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
