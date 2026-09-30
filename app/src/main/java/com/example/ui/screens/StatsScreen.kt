package com.example.ui.screens

import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.UnlockLog
import com.example.ui.MainViewModel
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ChartBarData(
    val label: String,
    val attempts: Int,
    val questsPassed: Int
)

@Composable
fun StatsScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val stats by viewModel.stats.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()

    var timeFilterTab by remember { mutableIntStateOf(0) } // 0: Hari ini, 1: 7 Hari
    var selectedBarIndex by remember { mutableIntStateOf(-1) }

    val totalAttempts = stats.sumOf { it.openAttempts }
    val totalQuestsPassed = stats.sumOf { it.questsPassed }
    val totalReasonsApproved = stats.sumOf { it.reasonsApproved }

    // Real Chart Data computed from Room logs
    val chartData = remember(recentLogs, timeFilterTab) {
        computeRealChartData(recentLogs, timeFilterTab)
    }
    val hasChartData = chartData.any { it.attempts > 0 || it.questsPassed > 0 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Statistik & Analisis Fokus",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Pantau data nyata percobaan buka, quest lolos, dan durasi aplikasi.",
                style = MaterialTheme.typography.bodySmall,
                color = Slate400
            )
        }

        // Summary metric cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Terblokir",
                    count = totalAttempts.toString(),
                    icon = Icons.Default.Block,
                    accentColor = RoseDanger
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Quest MTK",
                    count = totalQuestsPassed.toString(),
                    icon = Icons.Default.Calculate,
                    accentColor = IndigoLight
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Alasan AI",
                    count = totalReasonsApproved.toString(),
                    icon = Icons.Default.AutoAwesome,
                    accentColor = CyanAccent
                )
            }
        }

        // Interactive Weekly / Daily Chart Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("interactive_chart_card"),
                colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                border = BorderStroke(1.dp, Brush.linearGradient(listOf(CardBorderDark, IndigoPrimary.copy(alpha = 0.3f)))),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Grafik Aktivitas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (hasChartData) "Ketuk batang grafik untuk melihat detail angka" else "Data akumulasi otomatis saat aplikasi terblokir",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab: Hari ini vs 7 Hari
                    TabRow(
                        selectedTabIndex = timeFilterTab,
                        containerColor = Slate900,
                        contentColor = Color.White,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[timeFilterTab]),
                                color = CyanAccent
                            )
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, CardBorderDark, RoundedCornerShape(8.dp))
                    ) {
                        Tab(
                            selected = timeFilterTab == 0,
                            onClick = {
                                timeFilterTab = 0
                                selectedBarIndex = -1
                            },
                            text = { Text("Hari Ini", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = timeFilterTab == 1,
                            onClick = {
                                timeFilterTab = 1
                                selectedBarIndex = -1
                            },
                            text = { Text("7 Hari Terakhir", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!hasChartData) {
                        // Empty State for Chart
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate900),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.BarChart, contentDescription = null, tint = Slate800, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Belum ada data, mulai pakai hari ini",
                                    color = Slate400,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        // Real Canvas Chart with Y-axis labels and Tap detection
                        RealInteractiveChart(
                            chartData = chartData,
                            selectedIndex = selectedBarIndex,
                            onBarSelected = { idx -> selectedBarIndex = idx }
                        )

                        // Selected Bar Details Card
                        if (selectedBarIndex in chartData.indices) {
                            val selectedData = chartData[selectedBarIndex]
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(IndigoPrimary.copy(alpha = 0.15f))
                                    .border(1.dp, CyanAccent, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${selectedData.label}:",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${selectedData.attempts}x Terblokir • ${selectedData.questsPassed}x Quest Lolos",
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top Apps Duration and Block Frequency Breakdown
        item {
            Text(
                text = "Durasi & Percobaan per Aplikasi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (stats.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada data, mulai pakai hari ini", color = Slate400, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(stats, key = { it.packageName }) { appStat ->
                val durationFormatted = formatDuration(appStat.totalDurationMillis)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                    border = BorderStroke(1.dp, CardBorderDark),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = appStat.appName,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HourglassTop, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Durasi fokus: $durationFormatted • ${appStat.questsPassed} MTK lolos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(IndigoPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${appStat.openAttempts}x dicegah",
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Recent Logs
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Riwayat Percobaan Terakhir",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        if (recentLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada data riwayat, mulai pakai hari ini", color = Slate400, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(recentLogs.take(15), key = { it.id }) { log ->
                val timeStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, CardBorderDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(log.appName, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp)
                            Text(
                                text = "${if (log.method == "MATH_QUEST") "Quest MTK" else "Alasan AI"} • $timeStr",
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (log.success) EmeraldSuccess.copy(alpha = 0.2f) else RoseDanger.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (log.success) "+${log.pointsEarned} Poin" else "Gagal",
                                color = if (log.success) EmeraldSuccess else RoseDanger,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun RealInteractiveChart(
    chartData: List<ChartBarData>,
    selectedIndex: Int,
    onBarSelected: (Int) -> Unit
) {
    val maxVal = (chartData.maxOfOrNull { maxOf(it.attempts, it.questsPassed) } ?: 10).coerceAtLeast(5)

    Row(modifier = Modifier.fillMaxWidth()) {
        // Y-axis Labels
        Column(
            modifier = Modifier.height(150.dp).padding(end = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            Text(maxVal.toString(), fontSize = 10.sp, color = Slate400)
            Text((maxVal / 2).toString(), fontSize = 10.sp, color = Slate400)
            Text("0", fontSize = 10.sp, color = Slate400)
        }

        // Canvas Chart
        Column(modifier = Modifier.weight(1f)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .pointerInput(chartData) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val count = chartData.size
                            if (count > 0) {
                                val slotWidth = width / count
                                val clickedIdx = (offset.x / slotWidth).toInt().coerceIn(0, count - 1)
                                onBarSelected(clickedIdx)
                            }
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height - 20f
                val count = chartData.size
                val slotWidth = canvasWidth / count
                val barWidth = (slotWidth * 0.55f).coerceIn(12f, 32f)

                // Draw Y-axis guideline
                for (i in 0..2) {
                    val y = canvasHeight * (i / 2f)
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1f
                    )
                }

                // Draw Bars
                chartData.forEachIndexed { index, data ->
                    val x = index * slotWidth + (slotWidth - barWidth) / 2f
                    val valRatio = (data.attempts.toFloat() / maxVal).coerceIn(0f, 1f)
                    val barHeight = (valRatio * canvasHeight).coerceAtLeast(4f)
                    val y = canvasHeight - barHeight

                    val isSelected = index == selectedIndex
                    val brush = if (isSelected) {
                        Brush.verticalGradient(listOf(CyanAccent, EmeraldSuccess))
                    } else {
                        Brush.verticalGradient(listOf(IndigoLight, IndigoPrimary))
                    }

                    drawRoundRect(
                        brush = brush,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // X-axis Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                chartData.forEachIndexed { idx, data ->
                    Text(
                        text = data.label,
                        fontSize = 10.sp,
                        color = if (idx == selectedIndex) CyanAccent else Slate400,
                        fontWeight = if (idx == selectedIndex) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

private fun computeRealChartData(logs: List<UnlockLog>, tab: Int): List<ChartBarData> {
    val now = Calendar.getInstance()

    return if (tab == 0) {
        // "Hari Ini": 6 time slots of 4 hours each
        val slots = listOf("00-04", "04-08", "08-12", "12-16", "16-20", "20-24")
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayLogs = logs.filter { it.timestamp >= todayStart }

        slots.mapIndexed { idx, label ->
            val slotStart = todayStart + (idx * 4L * 3600_000L)
            val slotEnd = slotStart + (4L * 3600_000L)
            val slotLogs = todayLogs.filter { it.timestamp in slotStart until slotEnd }
            ChartBarData(
                label = label,
                attempts = slotLogs.size,
                questsPassed = slotLogs.count { it.method == "MATH_QUEST" && it.success }
            )
        }
    } else {
        // "7 Hari Terakhir"
        val dayFormat = SimpleDateFormat("EEE", Locale("id", "ID"))
        val list = mutableListOf<ChartBarData>()

        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val start = dayCal.timeInMillis
            val end = start + (24L * 3600_000L)
            val dayLogs = logs.filter { it.timestamp in start until end }

            list.add(
                ChartBarData(
                    label = dayFormat.format(dayCal.time),
                    attempts = dayLogs.size,
                    questsPassed = dayLogs.count { it.method == "MATH_QUEST" && it.success }
                )
            )
        }
        list
    }
}

private fun formatDuration(millis: Long): String {
    if (millis <= 0) return "0 m"
    val mins = millis / (1000 * 60)
    val hrs = mins / 60
    return if (hrs > 0) "${hrs}j ${mins % 60}m" else "${mins}m"
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
        border = BorderStroke(1.dp, Brush.linearGradient(listOf(CardBorderDark, accentColor.copy(alpha = 0.3f)))),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(count, fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White)
            Text(title, style = MaterialTheme.typography.labelSmall, color = Slate400)
        }
    }
}
