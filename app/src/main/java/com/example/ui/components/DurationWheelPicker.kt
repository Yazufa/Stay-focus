package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBackgroundDark
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun DurationWheelPickerDialog(
    title: String = "Pilih Durasi",
    initialTotalMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (totalMinutes: Int) -> Unit
) {
    val initialHours = (initialTotalMinutes / 60).coerceIn(0, 12)
    val initialMins = (initialTotalMinutes % 60).coerceIn(0, 59)

    var selectedHour by remember { mutableIntStateOf(initialHours) }
    var selectedMinute by remember { mutableIntStateOf(if (initialMins == 0 && initialHours == 0) 10 else initialMins) }

    val hoursList = remember { (0..12).toList() }
    val minutesList = remember { (0..59).toList() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackgroundDark,
        title = {
            Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Gulir untuk memilih jam dan menit durasi",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Current chosen duration preview badge
                val totalMins = selectedHour * 60 + selectedMinute
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(IndigoPrimary.copy(alpha = 0.2f))
                        .border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val previewText = when {
                        selectedHour > 0 && selectedMinute > 0 -> "$selectedHour Jam $selectedMinute Menit ($totalMins Menit)"
                        selectedHour > 0 -> "$selectedHour Jam ($totalMins Menit)"
                        else -> "$selectedMinute Menit"
                    }
                    Text(
                        text = previewText,
                        color = CyanAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scroll Wheel Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hours Column Wheel
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("JAM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate400)
                        Spacer(modifier = Modifier.height(6.dp))
                        WheelColumn(
                            items = hoursList,
                            selectedValue = selectedHour,
                            onValueChange = { selectedHour = it },
                            labelSuffix = "j"
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(120.dp)
                            .background(CardBorderDark)
                    )

                    // Minutes Column Wheel
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("MENIT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate400)
                        Spacer(modifier = Modifier.height(6.dp))
                        WheelColumn(
                            items = minutesList,
                            selectedValue = selectedMinute,
                            onValueChange = { selectedMinute = it },
                            labelSuffix = "m"
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val total = maxOf(1, selectedHour * 60 + selectedMinute)
                    onConfirm(total)
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Terapkan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Batal", color = Slate400)
            }
        }
    )
}

@Composable
private fun WheelColumn(
    items: List<Int>,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    labelSuffix: String
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedValue) {
        val index = items.indexOf(selectedValue)
        if (index >= 0) {
            val target = maxOf(0, index - 1)
            listState.animateScrollToItem(target)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Slate900)
            .border(1.dp, CardBorderDark, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items) { value ->
                val isSelected = value == selectedValue
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) IndigoPrimary else Color.Transparent)
                        .clickable { onValueChange(value) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "%02d %s".format(value, labelSuffix),
                        fontSize = if (isSelected) 16.sp else 13.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                        color = if (isSelected) Color.White else Slate400
                    )
                }
            }
        }
    }
}
