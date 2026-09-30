package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Smartphone
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.util.PermissionHelper

@Composable
fun HealthCheckDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val permissions by viewModel.permissionsState.collectAsState()
    val simulateAll by viewModel.simulateAllPermissions.collectAsState()
    var isXiaomiGuideExpanded by remember { mutableStateOf(false) }

    val deviceAdminLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshPermissions()
    }

    val allActive = permissions.isAllGranted

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackgroundDark,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (allActive) EmeraldSuccess.copy(alpha = 0.2f) else AmberWarning.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = if (allActive) EmeraldSuccess else AmberWarning,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Cek Kesehatan Sistem",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                        Text(
                            text = if (allActive) "Semua sistem optimal" else "Perlu perhatian segera",
                            fontSize = 11.sp,
                            color = if (allActive) EmeraldSuccess else AmberWarning
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.refreshPermissions() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Muat Ulang",
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Pastikan semua izin aktif agar pemblokiran aplikasi instan (< 1 detik) dan proteksi anti-hapus berjalan sempurna.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )

                // Quick Mode Pengujian / Emulator Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (simulateAll) EmeraldSuccess.copy(alpha = 0.15f) else Slate900
                    ),
                    border = BorderStroke(1.dp, if (simulateAll) EmeraldSuccess else CardBorderDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Mode Uji / Emulator",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (simulateAll) {
                                    Text(
                                        text = "HIJAU SEMPURNA",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = EmeraldSuccess
                                    )
                                }
                            }
                            Text(
                                text = "Aktifkan otomatis semua status izin agar hijau sempurna di emulator.",
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        androidx.compose.material3.Switch(
                            checked = simulateAll,
                            onCheckedChange = { viewModel.setSimulateAllPermissions(it) },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldSuccess
                            )
                        )
                    }
                }

                // 1. Accessibility Service
                HealthItemCard(
                    title = "Layanan Aksesibilitas (Instan)",
                    subtitle = "Deteksi pembukaan aplikasi seketika tanpa jeda",
                    isActive = permissions.hasAccessibility,
                    icon = Icons.Default.Accessibility,
                    onAction = { PermissionHelper.requestAccessibilityPermission(context) }
                )

                // 2. Overlay
                HealthItemCard(
                    title = "Tampilan di Atas Aplikasi (Overlay)",
                    subtitle = "Menampilkan tirai quest matematika penghalang",
                    isActive = permissions.hasOverlay,
                    icon = Icons.Default.Layers,
                    onAction = { PermissionHelper.requestOverlayPermission(context) }
                )

                // 3. Usage Access
                HealthItemCard(
                    title = "Akses Data Penggunaan (Usage Access)",
                    subtitle = "Mencatat durasi pemakaian nyata & statistik fokus",
                    isActive = permissions.hasUsageStats,
                    icon = Icons.Default.DataUsage,
                    onAction = { PermissionHelper.requestUsageStatsPermission(context) }
                )

                // 4. Device Admin
                HealthItemCard(
                    title = "Device Admin (Proteksi Anti-Hapus)",
                    subtitle = "Mencegah aplikasi di-uninstall langsung oleh pengguna",
                    isActive = permissions.isDeviceAdmin,
                    icon = Icons.Default.AdminPanelSettings,
                    onAction = {
                        android.widget.Toast.makeText(context, "Membuka aktivasi Device Admin...", android.widget.Toast.LENGTH_SHORT).show()
                        try {
                            val intent = PermissionHelper.getDeviceAdminIntent(context)
                            deviceAdminLauncher.launch(intent)
                        } catch (e: Exception) {
                            PermissionHelper.requestDeviceAdmin(context)
                        }
                    }
                )

                // 5. Battery Optimization
                HealthItemCard(
                    title = "Baterai Tanpa Batasan (Hemat Baterai)",
                    subtitle = "Mencegah Android mematikan proses pemantau di latar belakang",
                    isActive = permissions.isBatteryIgnored,
                    icon = Icons.Default.BatterySaver,
                    onAction = { PermissionHelper.requestIgnoreBatteryOptimizations(context) }
                )

                // Panduan Khusus HyperOS / Xiaomi (MIUI)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isXiaomiGuideExpanded = !isXiaomiGuideExpanded },
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(IndigoPrimary.copy(alpha = 0.5f), CyanAccent.copy(alpha = 0.3f))))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Smartphone, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Panduan Khusus HyperOS / Xiaomi",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                            Icon(
                                imageVector = if (isXiaomiGuideExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = Slate400
                            )
                        }

                        AnimatedVisibility(visible = isXiaomiGuideExpanded) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                Text(
                                    text = "Agar MIUI / HyperOS tidak mematikan stayfocus di latar belakang, lakukan 4 langkah ini:",
                                    fontSize = 12.sp,
                                    color = Slate200
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                XiaomiStepItem(
                                    step = "1",
                                    title = "Autostart (Mulai Otomatis)",
                                    desc = "Buka Keamanan Xiaomi > Kelola Izin > Mulai Otomatis > Aktifkan stayfocus."
                                )
                                XiaomiStepItem(
                                    step = "2",
                                    title = "Penghemat Baterai: Tanpa Batasan",
                                    desc = "Buka Info Aplikasi stayfocus > Penghemat Baterai > Pilih 'Tidak ada batasan'."
                                )
                                XiaomiStepItem(
                                    step = "3",
                                    title = "Kunci Aplikasi di Recent Apps",
                                    desc = "Buka menu recent apps / tugas terkini, tekan lama kartu stayfocus, lalu tap ikon Gembok (Lock)."
                                )
                                XiaomiStepItem(
                                    step = "4",
                                    title = "Jendela Pop-up di Latar Belakang",
                                    desc = "Pada Kelola Izin, aktifkan 'Tampilkan jendela sembul saat berjalan di latar belakang'."
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { PermissionHelper.openAppDetailsOrXiaomi(context) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Buka Pengaturan Keamanan / Info Aplikasi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Tutup", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun HealthItemCard(
    title: String,
    subtitle: String,
    isActive: Boolean,
    icon: ImageVector,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = BorderStroke(1.dp, if (isActive) EmeraldSuccess.copy(alpha = 0.3f) else RoseDanger.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isActive) EmeraldSuccess.copy(alpha = 0.15f) else RoseDanger.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isActive) EmeraldSuccess else RoseDanger,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White)
                    Text(text = subtitle, fontSize = 10.sp, color = Slate400, maxLines = 2)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isActive) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(EmeraldSuccess.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("AKTIF", color = EmeraldSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Aktifkan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun XiaomiStepItem(
    step: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(CyanAccent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(step, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(desc, fontSize = 11.sp, color = Slate400)
        }
    }
}
