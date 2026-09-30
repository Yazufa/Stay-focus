package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.Schedule
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
import com.example.util.InstalledApp

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
                    .padding(horizontal = 16.dp),
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

        // Floating Action Button
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
                            .background(EmeraldSuccess.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Level ${schedule.questLevel}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$lockedAppsCount aplikasi dikunci",
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
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mulai (HH:mm)", style = MaterialTheme.typography.labelMedium, color = Slate400)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = startTime,
                                onValueChange = { startTime = it },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndigoPrimary,
                                    unfocusedBorderColor = CardBorderDark,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Slate200
                                )
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Selesai (HH:mm)", style = MaterialTheme.typography.labelMedium, color = Slate400)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = endTime,
                                onValueChange = { endTime = it },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndigoPrimary,
                                    unfocusedBorderColor = CardBorderDark,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Slate200
                                )
                            )
                        }
                    }
                }

                item {
                    val levelDesc = when (questLevel) {
                        1 -> "Level 1: Tambah/Kurang Satuan (Mudah)"
                        2 -> "Level 2: Tambah/Kurang Puluhan (Sedang)"
                        3 -> "Level 3: Hitungan 3 Angka (Menengah)"
                        4 -> "Level 4: Perkalian Dasar (Tantangan)"
                        else -> "Level 5: Campuran + - × ÷ (Ekstrem)"
                    }
                    Text("Tingkat Level Quest MTK", style = MaterialTheme.typography.labelMedium, color = Slate400)
                    Text(levelDesc, style = MaterialTheme.typography.bodySmall, color = CyanAccent, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = questLevel.toFloat(),
                        onValueChange = { questLevel = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = IndigoPrimary)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Aplikasi Terkunci", style = MaterialTheme.typography.labelMedium, color = Slate400)
                        Text(
                            text = "${selectedPackages.size} aplikasi dipilih",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { showAppPicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Apps, contentDescription = null, tint = IndigoLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pilih Aplikasi yang Dikunci", color = Color.White)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newSched = Schedule(
                        name = name.ifBlank { "Jadwal Fokus" },
                        daysOfWeek = selectedDays.sorted().joinToString(","),
                        startTime = startTime.ifBlank { "08:00" },
                        endTime = endTime.ifBlank { "17:00" },
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
fun AppSelectionDialog(
    allApps: List<InstalledApp>,
    initiallySelected: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(initiallySelected) }

    val filtered = remember(allApps, searchQuery) {
        if (searchQuery.isBlank()) allApps
        else allApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        title = {
            Text("Pilih Aplikasi untuk Dikunci", fontWeight = FontWeight.Bold, color = Color.White)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari aplikasi...", color = Slate400, fontSize = 13.sp) },
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pilih Semua",
                        color = CyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            selected = allApps.map { it.packageName }.toSet()
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

                Spacer(modifier = Modifier.height(8.dp))

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
                                Text(app.appName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
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
