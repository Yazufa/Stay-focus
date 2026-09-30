package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.Schedule
import com.example.data.math.MathQuestGenerator
import com.example.data.math.QuestLevelInfo
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import com.example.util.InstalledApp
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SchedulesScreen(
    viewModel: MainViewModel
) {
    val schedules by viewModel.schedules.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var scheduleToEdit by remember { mutableStateOf<Schedule?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (schedules.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Slate900)
                        .border(1.dp, CardBorderDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = IndigoLight,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Belum Ada Jadwal Kunci",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Buat jadwal untuk mengunci aplikasi produktivitas Anda pada jam kerja atau belajar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate400,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        scheduleToEdit = null
                        showEditDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah Jadwal Pertama", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Kelola Jadwal Kunci",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Aplikasi akan otomatis diblokir saat jadwal berlangsung.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }

                items(schedules, key = { it.id }) { schedule ->
                    ScheduleItemCard(
                        schedule = schedule,
                        onToggle = { viewModel.toggleSchedule(schedule) },
                        onEdit = {
                            scheduleToEdit = schedule
                            showEditDialog = true
                        },
                        onDelete = { viewModel.deleteSchedule(schedule.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        FloatingActionButton(
            onClick = {
                scheduleToEdit = null
                showEditDialog = true
            },
            containerColor = IndigoPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_schedule_fab"),
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Jadwal")
        }
    }

    if (showEditDialog) {
        ScheduleEditDialog(
            schedule = scheduleToEdit,
            installedApps = installedApps,
            onDismiss = { showEditDialog = false },
            onSave = { newSchedule ->
                if (scheduleToEdit == null) {
                    viewModel.saveSchedule(newSchedule)
                } else {
                    viewModel.updateSchedule(newSchedule.copy(id = scheduleToEdit!!.id))
                }
                showEditDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduleItemCard(
    schedule: Schedule,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dayNames = mapOf(1 to "Sen", 2 to "Sel", 3 to "Rab", 4 to "Kam", 5 to "Jum", 6 to "Sab", 7 to "Min")
    val selectedDays = schedule.getDaysList()
    val lockedAppsCount = schedule.getPackagesList().size
    val levelInfo = MathQuestGenerator.getLevelInfo(schedule.questLevel)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
        border = BorderStroke(1.dp, CardBorderDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${schedule.startTime} - ${schedule.endTime}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyanAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Switch(
                    checked = schedule.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = IndigoPrimary,
                        uncheckedThumbColor = Slate400,
                        uncheckedTrackColor = Slate800
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Days chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (1..7).forEach { day ->
                    val isActive = selectedDays.contains(day)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isActive) IndigoPrimary.copy(alpha = 0.25f) else Slate900)
                            .border(1.dp, if (isActive) IndigoLight else CardBorderDark, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = dayNames[day] ?: "",
                            fontSize = 11.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) CyanAccent else Slate400
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(IndigoPrimary.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Lvl ${levelInfo.level}: ${levelInfo.name}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$lockedAppsCount app terkunci",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Slate400, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = RoseDanger, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduleEditDialog(
    schedule: Schedule?,
    installedApps: List<InstalledApp>,
    onDismiss: () -> Unit,
    onSave: (Schedule) -> Unit
) {
    var name by remember { mutableStateOf(schedule?.name ?: "Jam Fokus Kerja") }
    var startTime by remember { mutableStateOf(schedule?.startTime ?: "08:00") }
    var endTime by remember { mutableStateOf(schedule?.endTime ?: "17:00") }
    var selectedDays by remember { mutableStateOf(schedule?.getDaysList()?.toSet() ?: setOf(1, 2, 3, 4, 5)) }
    var questLevel by remember { mutableIntStateOf(schedule?.questLevel ?: 1) }
    var selectedPackages by remember { mutableStateOf(schedule?.getPackagesList()?.toSet() ?: emptySet()) }

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showAppPicker by remember { mutableStateOf(false) }

    val dayNames = mapOf(1 to "Sen", 2 to "Sel", 3 to "Rab", 4 to "Kam", 5 to "Jum", 6 to "Sab", 7 to "Min")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackgroundDark,
        title = {
            Text(
                text = if (schedule == null) "Tambah Jadwal Kunci" else "Edit Jadwal Kunci",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. NENTUIN APLIKASI YANG DI-BAN (TAROH DI PALING ATAS!)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        border = BorderStroke(1.dp, if (selectedPackages.isNotEmpty()) CyanAccent.copy(alpha = 0.5f) else IndigoPrimary.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Apps, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Aplikasi yang Di-Ban", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selectedPackages.isNotEmpty()) CyanAccent.copy(alpha = 0.2f) else Slate800)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${selectedPackages.size} Aplikasi Dipilih",
                                        color = if (selectedPackages.isNotEmpty()) CyanAccent else Slate400,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (selectedPackages.isEmpty()) "Tentukan aplikasi yang ingin diblokir selama jadwal ini aktif." else "Aplikasi yang dipilih akan langsung diblokir dan mewajibkan quest MTK untuk dibuka.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { showAppPicker = true },
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pilih Aplikasi yang Di-Ban", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // 2. NAMA JADWAL
                item {
                    Text("Nama Jadwal", style = MaterialTheme.typography.labelMedium, color = Slate400)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth().testTag("schedule_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = CardBorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Slate200
                        )
                    )
                }

                // 3. JAM MULAI & SELESAI
                item {
                    Text("Jam Mulai & Selesai (Clock Picker)", style = MaterialTheme.typography.labelMedium, color = Slate400)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Start Time Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate900)
                                .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
                                .clickable { showStartTimePicker = true }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Mulai", fontSize = 10.sp, color = Slate400)
                                    Text(startTime, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        // End Time Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate900)
                                .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp))
                                .clickable { showEndTimePicker = true }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = IndigoLight, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Selesai", fontSize = 10.sp, color = Slate400)
                                    Text(endTime, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // 4. HARI AKTIF
                item {
                    Text("Hari Aktif", style = MaterialTheme.typography.labelMedium, color = Slate400)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..7).forEach { day ->
                            val isSelected = selectedDays.contains(day)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) IndigoPrimary else Slate900)
                                    .border(1.dp, if (isSelected) IndigoLight else CardBorderDark, RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = dayNames[day] ?: "",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else Slate400
                                )
                            }
                        }
                    }
                }

                // 5. TINGKAT KESULITAN MATEMATIKA (SLIDER NYAMPING DENGAN TITIK2 & CONTOH DI ATASNYA)
                item {
                    MathDifficultyHorizontalSlider(
                        currentLevel = questLevel,
                        onLevelChange = { questLevel = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newSched = Schedule(
                        name = name.ifBlank { "Jadwal Fokus" },
                        daysOfWeek = selectedDays.sorted().joinToString(","),
                        startTime = startTime,
                        endTime = endTime,
                        lockedPackages = selectedPackages.joinToString(","),
                        questLevel = questLevel,
                        isEnabled = true
                    )
                    onSave(newSched)
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_schedule_button")
            ) {
                Text("Simpan Jadwal", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Batal", color = Slate400)
            }
        }
    )

    if (showStartTimePicker) {
        val parts = startTime.split(":")
        val initialH = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val initialM = parts.getOrNull(1)?.toIntOrNull() ?: 0
        MaterialTimePickerDialog(
            title = "Pilih Jam Mulai",
            initialHour = initialH,
            initialMinute = initialM,
            onDismiss = { showStartTimePicker = false },
            onConfirm = { h, m ->
                startTime = "%02d:%02d".format(h, m)
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        val parts = endTime.split(":")
        val initialH = parts.getOrNull(0)?.toIntOrNull() ?: 17
        val initialM = parts.getOrNull(1)?.toIntOrNull() ?: 0
        MaterialTimePickerDialog(
            title = "Pilih Jam Selesai",
            initialHour = initialH,
            initialMinute = initialM,
            onDismiss = { showEndTimePicker = false },
            onConfirm = { h, m ->
                endTime = "%02d:%02d".format(h, m)
                showEndTimePicker = false
            }
        )
    }

    if (showAppPicker) {
        AppSelectionDialog(
            allApps = installedApps,
            initiallySelected = selectedPackages,
            onDismiss = { showAppPicker = false },
            onConfirm = { chosen ->
                selectedPackages = chosen
                showAppPicker = false
            }
        )
    }
}

@Composable
fun MathDifficultyHorizontalSlider(
    currentLevel: Int,
    onLevelChange: (Int) -> Unit
) {
    val levelInfo = MathQuestGenerator.getLevelInfo(currentLevel)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Slate900)
            .border(1.dp, CardBorderDark, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Tingkat Kesulitan MTK",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Geser slider ke titik batas kesulitan",
                    fontSize = 11.sp,
                    color = Slate400
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(IndigoPrimary.copy(alpha = 0.3f))
                    .border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Level $currentLevel: ${levelInfo.name}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // POSISI CONTOHNYA DI ATAS SLIDE NYA (DEFAULT TAMPILKAN LEVEL YANG PALING MUDAH LEVEL 1)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate950),
            border = BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Contoh Soal (Level $currentLevel - ${levelInfo.name}):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AmberWarning.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+${levelInfo.pointsBonus} Poin",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberWarning
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Formula preview with typewriter animated text
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate900)
                        .border(1.dp, CyanAccent.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TypewriterText(
                        text = levelInfo.example,
                        fontSize = 17.sp,
                        color = CyanAccent,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tipe Soal: ${levelInfo.description}",
                    fontSize = 11.sp,
                    color = Slate200
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SLIDER NYAMPING DENGAN TITIK-TITIK BATAS KESULITAN
        Slider(
            value = currentLevel.toFloat(),
            onValueChange = { onLevelChange(it.toInt().coerceIn(1, 6)) },
            valueRange = 1f..6f,
            steps = 4,
            colors = SliderDefaults.colors(
                thumbColor = CyanAccent,
                activeTrackColor = IndigoPrimary,
                inactiveTrackColor = Slate800,
                activeTickColor = Color.White,
                inactiveTickColor = Slate400
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // TITIK-TITIK & LABEL BATAS TINGKAT KESULITAN DI BAWAH SLIDER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..6).forEach { lvl ->
                val isSelected = currentLevel == lvl
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onLevelChange(lvl) }
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 14.dp else 10.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) CyanAccent else if (lvl < currentLevel) IndigoLight else Slate700)
                            .border(1.dp, if (isSelected) Color.White else Color.Transparent, CircleShape)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (lvl) {
                            1 -> "1\nMudah"
                            6 -> "6\nLegenda"
                            else -> "$lvl"
                        },
                        fontSize = 9.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) CyanAccent else Slate400,
                        lineHeight = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TypewriterText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    color: Color = Color.White,
    fontWeight: FontWeight = FontWeight.Normal
) {
    var displayedLength by remember(text) { mutableIntStateOf(0) }

    LaunchedEffect(text) {
        while (true) {
            displayedLength = 0
            for (i in 1..text.length) {
                displayedLength = i
                delay(90)
            }
            delay(1600)
        }
    }

    Text(
        text = text.take(displayedLength),
        fontSize = fontSize,
        color = color,
        fontWeight = fontWeight,
        fontFamily = FontFamily.Monospace
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialTimePickerDialog(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        title = {
            Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(
                    state = state,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = CardBackgroundDark,
                        selectorColor = CyanAccent,
                        timeSelectorSelectedContainerColor = IndigoPrimary,
                        timeSelectorUnselectedContainerColor = Slate800,
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContentColor = Slate200
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(state.hour, state.minute) },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Text("Pilih", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal", color = Slate400)
            }
        }
    )
}

@Composable
fun AppSelectionDialog(
    allApps: List<InstalledApp>,
    initiallySelected: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(initiallySelected) }
    var filterType by remember { mutableIntStateOf(0) } // 0: Semua, 1: Pengguna/PlayStore, 2: VPN/Sistem

    val filtered = remember(allApps, searchQuery, filterType) {
        allApps.filter { app ->
            val matchesSearch = searchQuery.isBlank() ||
                    app.appName.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)

            val matchesType = when (filterType) {
                1 -> !app.isSystemApp
                2 -> app.isSystemApp || app.packageName.contains("vpn", ignoreCase = true) || app.packageName.contains("onedot", ignoreCase = true)
                else -> true
            }
            matchesSearch && matchesType
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        title = {
            Text("Pilih Aplikasi untuk Dikunci", fontWeight = FontWeight.Bold, color = Color.White)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(440.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari (misal: 1.1.1.1, TikTok, VPN)...", color = Slate400, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = CardBorderDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Slate200
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Semua", "Aplikasi User", "Sistem & VPN").forEachIndexed { idx, label ->
                        val isCurr = filterType == idx
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCurr) IndigoPrimary else Slate800)
                                .clickable { filterType = idx }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(label, fontSize = 11.sp, color = if (isCurr) Color.White else Slate400, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pilih Semua (${filtered.size})",
                        color = CyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            selected = selected + filtered.map { it.packageName }.toSet()
                        }
                    )
                    Text(
                        text = "Kosongkan",
                        color = RoseDanger,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            selected = emptySet()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filtered, key = { it.packageName }) { app ->
                        val isChecked = selected.contains(app.packageName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selected = if (isChecked) selected - app.packageName else selected + app.packageName
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    selected = if (it) selected + app.packageName else selected - app.packageName
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = IndigoPrimary,
                                    uncheckedColor = Slate400
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(app.appName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    if (app.isSystemApp) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Slate800)
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("SISTEM", fontSize = 9.sp, color = Slate400, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text(app.packageName, color = Slate400, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selected) },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Text("Pilih (${selected.size})", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal", color = Slate400)
            }
        }
    )
}
