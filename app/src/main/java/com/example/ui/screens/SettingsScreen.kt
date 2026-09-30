package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.math.MathQuestGenerator
import com.example.data.math.MathQuestion
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardBackgroundDark
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.PermissionHelper

@Composable
fun SettingsScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val permissions by viewModel.permissionsState.collectAsState()
    val questionsPerQuest by viewModel.questionsPerQuest.collectAsState()
    val dailyBypassLimit by viewModel.dailyBypassLimit.collectAsState()
    val restDurationMinutes by viewModel.restDurationMinutes.collectAsState()
    val restWhitelist by viewModel.restWhitelist.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()
    val userProgress by viewModel.userProgress.collectAsState()
    val currentTime by viewModel.currentTimeMillis.collectAsState()

    var showWhitelistPicker by remember { mutableStateOf(false) }
    var apiKeyInput by remember(customApiKey) { mutableStateOf(customApiKey) }
    var showApiKeyPassword by remember { mutableStateOf(false) }
    var showDeactivationQuestDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pengaturan & Sistem",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Konfigurasi tingkat kesulitan quest, izin sistem, dan proteksi anti-hapus.",
                style = MaterialTheme.typography.bodySmall,
                color = Slate400
            )
        }

        // Section 1: Izin & Onboarding Sistem
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                border = BorderStroke(1.dp, Brush.linearGradient(listOf(CardBorderDark, IndigoPrimary.copy(alpha = 0.3f)))),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = IndigoLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Status Izin Sistem", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    PermissionRow(
                        title = "Layanan Aksesibilitas",
                        description = "Deteksi instan aplikasi terlarang",
                        icon = Icons.Default.Accessibility,
                        isGranted = permissions.hasAccessibility,
                        onRequest = { PermissionHelper.requestAccessibilityPermission(context) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PermissionRow(
                        title = "Tampilkan di Atas Aplikasi (Overlay)",
                        description = "Menampilkan layar pengunci penghalang",
                        icon = Icons.Default.Layers,
                        isGranted = permissions.hasOverlay,
                        onRequest = { PermissionHelper.requestOverlayPermission(context) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PermissionRow(
                        title = "Akses Penggunaan (Usage Stats)",
                        description = "Mencatat durasi & statistik fokus",
                        icon = Icons.Default.DataUsage,
                        isGranted = permissions.hasUsageStats,
                        onRequest = { PermissionHelper.requestUsageStatsPermission(context) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PermissionRow(
                        title = "Device Admin (Anti-Hapus)",
                        description = "Mencegah aplikasi di-uninstall sembarangan",
                        icon = Icons.Default.AdminPanelSettings,
                        isGranted = permissions.isDeviceAdmin,
                        onRequest = { PermissionHelper.requestDeviceAdmin(context) }
                    )
                }
            }
        }

        // Section 2: Konfigurasi Quest MTK & Bypass
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                border = BorderStroke(1.dp, Brush.linearGradient(listOf(CardBorderDark, CyanAccent.copy(alpha = 0.3f)))),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = CyanAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Konfigurasi Quest & Alasan", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Jumlah Soal
                    Text("Jumlah Soal per Quest: $questionsPerQuest soal", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp)
                    Slider(
                        value = questionsPerQuest.toFloat(),
                        onValueChange = { viewModel.setQuestionsPerQuest(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = IndigoPrimary),
                        modifier = Modifier.testTag("questions_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Batas Alasan Harian
                    Text("Batas Buka via Alasan: $dailyBypassLimit kali per hari", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp)
                    Slider(
                        value = dailyBypassLimit.toFloat(),
                        onValueChange = { viewModel.setDailyBypassLimit(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = IndigoPrimary),
                        modifier = Modifier.testTag("bypass_limit_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Durasi Istirahat
                    Text("Durasi Mode Istirahat: $restDurationMinutes menit", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp)
                    Slider(
                        value = restDurationMinutes.toFloat(),
                        onValueChange = { viewModel.setRestDurationMinutes(it.toInt()) },
                        valueRange = 5f..60f,
                        steps = 10,
                        colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = IndigoPrimary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showWhitelistPicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Apps, contentDescription = null, tint = CyanAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Whitelist Aplikasi Istirahat (${restWhitelist.size})", color = Color.White)
                    }
                }
            }
        }

        // Section 3: Gemini API Key
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                border = BorderStroke(1.dp, CardBorderDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = AmberWarning)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kunci API Gemini (AI Judge)", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Kunci disimpan secara aman via EncryptedSharedPreferences untuk mengevaluasi alasan pembukaan aplikasi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        modifier = Modifier.fillMaxWidth().testTag("api_key_input"),
                        placeholder = { Text("Masukkan Gemini API Key...", color = Slate400) },
                        singleLine = true,
                        visualTransformation = if (showApiKeyPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showApiKeyPassword = !showApiKeyPassword }) {
                                Icon(
                                    imageVector = if (showApiKeyPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle visibility",
                                    tint = Slate400
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberWarning,
                            unfocusedBorderColor = CardBorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Slate200
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.saveCustomApiKey(apiKeyInput)
                            Toast.makeText(context, "API Key berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberWarning, contentColor = Slate900),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Simpan Kunci API", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 4: Anti-Hapus & Device Admin Control
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                border = BorderStroke(1.dp, Brush.linearGradient(listOf(CardBorderDark, RoseDanger.copy(alpha = 0.4f)))),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LockReset, contentDescription = null, tint = RoseDanger)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Perlindungan Anti-Hapus", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Mencegah pencopotan aplikasi secara impulsif. Untuk menonaktifkan, Anda harus membuktikan komitmen dengan menyelesaikan Quest Matematika Level 5 (Ekstrem) dan menunggu masa jeda aman 24 jam.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate200
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!permissions.isDeviceAdmin) {
                        Button(
                            onClick = { PermissionHelper.requestDeviceAdmin(context) },
                            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("enable_device_admin_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Aktifkan Device Admin Sekarang", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        val requestedAt = userProgress?.antiUninstallRequestedAt ?: 0L
                        val mathPassed = userProgress?.antiUninstallMathPassed ?: false

                        if (!mathPassed || requestedAt <= 0L) {
                            OutlinedButton(
                                onClick = { showDeactivationQuestDialog = true },
                                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("request_deactivation_button"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberWarning),
                                border = BorderStroke(1.dp, AmberWarning),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.LockReset, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ajukan Penonaktifan Proteksi", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            val waitDuration = 24 * 60 * 60 * 1000L // 24 hours in ms
                            val elapsed = currentTime - requestedAt
                            val remaining = (waitDuration - elapsed).coerceAtLeast(0L)

                            if (remaining > 0) {
                                val remainingHours = remaining / (1000 * 60 * 60)
                                val remainingMins = (remaining / (1000 * 60)) % 60
                                val remainingSecs = (remaining / 1000) % 60

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Slate900),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = AmberWarning)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Masa Jeda 24 Jam Berjalan", fontWeight = FontWeight.Bold, color = AmberWarning)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "%02d jam : %02d menit : %02d detik tersisa".format(remainingHours, remainingMins, remainingSecs),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        OutlinedButton(
                                            onClick = { viewModel.cancelAntiUninstall() },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldSuccess)
                                        ) {
                                            Text("Batalkan & Tetap Disiplin", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            } else {
                                // 24 hours elapsed! Safe to deactivate
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        "Masa jeda 24 jam telah selesai. Anda sekarang diizinkan untuk menonaktifkan Device Admin.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EmeraldSuccess
                                    )
                                    Button(
                                        onClick = {
                                            PermissionHelper.removeDeviceAdmin(context)
                                            viewModel.cancelAntiUninstall()
                                            Toast.makeText(context, "Device Admin dinonaktifkan.", Toast.LENGTH_SHORT).show()
                                            viewModel.refreshPermissions()
                                        },
                                        modifier = Modifier.fillMaxWidth().height(46.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = RoseDanger),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Nonaktifkan Device Admin Sekarang", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showWhitelistPicker) {
        AppSelectionDialog(
            allApps = installedApps,
            initiallySelected = restWhitelist,
            onDismiss = { showWhitelistPicker = false },
            onConfirm = { chosen ->
                viewModel.setRestWhitelist(chosen)
                showWhitelistPicker = false
            }
        )
    }

    if (showDeactivationQuestDialog) {
        DeactivationMathQuestDialog(
            onDismiss = { showDeactivationQuestDialog = false },
            onPassed = {
                viewModel.completeAntiUninstallMathQuest()
                showDeactivationQuestDialog = false
                Toast.makeText(context, "Lolos Level 5! Masa jeda 24 jam dimulai.", Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
fun PermissionRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isGranted) EmeraldSuccess.copy(alpha = 0.15f) else Slate800),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGranted) EmeraldSuccess else Slate400,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 13.sp)
                Text(description, color = Slate400, fontSize = 11.sp)
            }
        }

        if (isGranted) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(EmeraldSuccess.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("AKTIF", color = EmeraldSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick = onRequest,
                modifier = Modifier.height(32.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
            ) {
                Text("Beri Izin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DeactivationMathQuestDialog(
    onDismiss: () -> Unit,
    onPassed: () -> Unit
) {
    val questions = remember { MathQuestGenerator.generateQuestions(level = 5, count = 3) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var isWrong by remember { mutableStateOf(false) }

    val currentQ = questions[currentIndex]

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackgroundDark,
        title = {
            Text("Verifikasi Quest MTK Level 5 (Ekstrem)", fontWeight = FontWeight.Bold, color = Color.White)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Selesaikan 3 soal campuran Level 5 untuk memulai masa jeda penonaktifan proteksi anti-hapus.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text("Soal ${currentIndex + 1} dari ${questions.size}", fontWeight = FontWeight.Bold, color = CyanAccent)
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate900)
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${currentQ.prompt} = ?",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    currentQ.options.chunked(2).forEach { rowOpts ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowOpts.forEach { opt ->
                                val isSelected = selectedAnswer == opt
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) IndigoPrimary else Slate900)
                                        .border(1.dp, if (isSelected) CyanAccent else CardBorderDark, RoundedCornerShape(10.dp))
                                        .clickable {
                                            selectedAnswer = opt
                                            isWrong = false
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(opt.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }

                if (isWrong) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Jawaban salah! Coba hitung kembali.", color = RoseDanger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedAnswer == currentQ.correctAnswer) {
                        if (currentIndex + 1 < questions.size) {
                            currentIndex += 1
                            selectedAnswer = null
                            isWrong = false
                        } else {
                            onPassed()
                        }
                    } else {
                        isWrong = true
                    }
                },
                enabled = selectedAnswer != null,
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Text(if (currentIndex + 1 < questions.size) "Soal Berikutnya" else "Verifikasi Lolos")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal", color = Slate400)
            }
        }
    )
}
