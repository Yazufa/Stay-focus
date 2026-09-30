package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.components.DurationWheelPickerDialog
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.theme.*
import com.example.util.PermissionHelper

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToSchedules: () -> Unit,
    onOpenHealthCheck: () -> Unit = onNavigateToSettings
) {
    val context = LocalContext.current
    val userProgress by viewModel.userProgress.collectAsState()
    val isMasterLockEnabled by viewModel.isMasterLockEnabled.collectAsState()
    val isRestModeActive by viewModel.isRestModeActive.collectAsState()
    val restEndTimestamp by viewModel.restModeEndTimestamp.collectAsState()
    val restDurationMinutes by viewModel.restDurationMinutes.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val permissions by viewModel.permissionsState.collectAsState()
    val currentTime by viewModel.currentTimeMillis.collectAsState()

    var showDurationWheel by remember { mutableStateOf(false) }

    val hasAllPermissions = permissions.isAllGranted

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Streak & Poin Hero Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Streak Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("streak_card"),
                    colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                    border = BorderStroke(1.dp, Brush.linearGradient(listOf(CardBorderDark, AmberWarning.copy(alpha = 0.5f)))),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(AmberWarning.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = AmberWarning,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Streak", style = MaterialTheme.typography.labelSmall, color = Slate400)
                            Text(
                                text = "${userProgress?.streakDays ?: 0} Hari",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }

                // Points Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("points_card"),
                    colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                    border = BorderStroke(1.dp, Brush.linearGradient(listOf(CardBorderDark, CyanAccent.copy(alpha = 0.5f)))),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CyanAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Poin",
                                tint = CyanAccent,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Poin Fokus", style = MaterialTheme.typography.labelSmall, color = Slate400)
                            Text(
                                text = "${userProgress?.totalPoints ?: 0}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // System Health Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenHealthCheck() }
                    .testTag("health_check_dashboard_card"),
                colors = CardDefaults.cardColors(
                    containerColor = if (hasAllPermissions) CardBackgroundDark else RoseDanger.copy(alpha = 0.15f)
                ),
                border = if (hasAllPermissions) {
                    BorderStroke(1.dp, CardBorderDark)
                } else {
                    BorderStroke(1.dp, Brush.horizontalGradient(listOf(RoseDanger, AmberWarning)))
                },
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (hasAllPermissions) EmeraldSuccess.copy(alpha = 0.15f) else RoseDanger.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = if (hasAllPermissions) EmeraldSuccess else RoseDanger,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Cek Kesehatan Sistem & Izin",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (hasAllPermissions) "Semua izin aktif • Blokir instan siap" else "Perlu tindakan agar blokir instan & anti-hapus aktif",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasAllPermissions) Slate400 else AmberWarning
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (hasAllPermissions) Slate400 else RoseDanger,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Master Focus Switch Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(CardBorderDark, IndigoPrimary.copy(alpha = 0.4f)))),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (isMasterLockEnabled) IndigoPrimary.copy(alpha = 0.2f) else Slate800),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isMasterLockEnabled) CyanAccent else Slate400,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Proteksi Aplikasi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isMasterLockEnabled) "Pengunci aktif sesuai jadwal" else "Pengunci dijeda sementara",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isMasterLockEnabled) EmeraldSuccess else Slate400
                            )
                        }
                    }

                    Switch(
                        checked = isMasterLockEnabled,
                        onCheckedChange = { viewModel.setMasterLock(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = IndigoPrimary,
                            uncheckedThumbColor = Slate400,
                            uncheckedTrackColor = Slate800
                        ),
                        modifier = Modifier.testTag("master_lock_switch")
                    )
                }
            }
        }

        // Mode Istirahat (Break Mode) Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(CardBorderDark, CyanAccent.copy(alpha = 0.4f)))),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(CyanAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bedtime,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Mode Istirahat",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Buka aplikasi whitelist sementara",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )
                            }
                        }

                        if (isRestModeActive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EmeraldSuccess.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("BERJALAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isRestModeActive) {
                        val remainingMillis = (restEndTimestamp - currentTime).coerceAtLeast(0L)
                        val remainingSec = remainingMillis / 1000
                        val min = remainingSec / 60
                        val sec = remainingSec % 60
                        val totalDurationSec = (restDurationMinutes * 60).toFloat()
                        val progress = if (totalDurationSec > 0) remainingSec.toFloat() / totalDurationSec else 0f

                        Text(
                            text = "Sisa waktu istirahat: %02d:%02d".format(min, sec),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = CyanAccent,
                            trackColor = Slate800
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.toggleRestMode(restDurationMinutes) },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseDanger)
                        ) {
                            Text("Akhiri Istirahat Sekarang", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.toggleRestMode(restDurationMinutes) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("start_rest_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Slate950),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Bedtime, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mulai ($restDurationMinutes Mnt)", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showDurationWheel = true },
                                modifier = Modifier
                                    .height(46.dp)
                                    .testTag("change_duration_button"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CardBorderDark)
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = "Ubah Durasi", tint = CyanAccent)
                            }
                        }
                    }
                }
            }
        }

        // Active Schedules Quick Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSchedules() },
                colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                border = BorderStroke(1.dp, CardBorderDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(IndigoLight.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = IndigoLight, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Jadwal Kunci Aktif", fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                text = "${schedules.count { it.isEnabled }} dari ${schedules.size} jadwal menyala",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Buka Jadwal",
                        tint = Slate400
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showDurationWheel) {
        DurationWheelPickerDialog(
            title = "Atur Durasi Mode Istirahat",
            initialTotalMinutes = restDurationMinutes,
            onDismiss = { showDurationWheel = false },
            onConfirm = { chosen ->
                viewModel.setRestDurationMinutes(chosen)
                showDurationWheel = false
            }
        )
    }
}
