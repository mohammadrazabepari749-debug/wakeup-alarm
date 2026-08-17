package com.wakeup.alarm.ui.setalarm

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeup.alarm.ui.theme.DarkBackground
import com.wakeup.alarm.ui.theme.PrimaryNeon
import com.wakeup.alarm.ui.theme.SurfaceDark
import com.wakeup.alarm.ui.theme.TextPrimary
import com.wakeup.alarm.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetAlarmScreen(
    viewModel: SetAlarmViewModel,
    onBackClick: () -> Unit
) {
    val hour by viewModel.hour.collectAsState()
    val minute by viewModel.minute.collectAsState()
    val label by viewModel.label.collectAsState()
    val daysOfWeek by viewModel.daysOfWeek.collectAsState()
    val verificationDuration by viewModel.verificationDurationSeconds.collectAsState()
    val vibrate by viewModel.vibrate.collectAsState()

    val timePickerState = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = false
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Alarm", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.setTime(timePickerState.hour, timePickerState.minute)
                        viewModel.saveAlarm(onSaved = onBackClick)
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = PrimaryNeon)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = SurfaceDark,
                    selectorColor = PrimaryNeon,
                    containerColor = SurfaceDark,
                    timeSelectorSelectedContainerColor = PrimaryNeon,
                    timeSelectorSelectedContentColor = DarkBackground,
                    timeSelectorUnselectedContainerColor = SurfaceDark,
                    timeSelectorUnselectedContentColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = label,
                onValueChange = { viewModel.setLabel(it) },
                label = { Text("Label") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryNeon,
                    unfocusedBorderColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = PrimaryNeon,
                    unfocusedLabelColor = TextSecondary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Face Verification Duration",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Duration user must look at camera with eyes open:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val durations = listOf(10, 30, 60, 120)
                        durations.forEach { sec ->
                            val isSelected = verificationDuration == sec
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PrimaryNeon else DarkBackground)
                                    .clickable { viewModel.setDuration(sec) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${sec}s",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) DarkBackground else TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                dayLabels.forEachIndexed { index, dayName ->
                    val dayInt = index + 1
                    val isSelected = daysOfWeek.contains(dayInt)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) PrimaryNeon else SurfaceDark)
                            .clickable { viewModel.toggleDay(dayInt) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayName.take(1),
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) DarkBackground else TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vibrate", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    Switch(
                        checked = vibrate,
                        onCheckedChange = { viewModel.setVibrate(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DarkBackground,
                            checkedTrackColor = PrimaryNeon
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.setTime(timePickerState.hour, timePickerState.minute)
                    viewModel.saveAlarm(onSaved = onBackClick)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Alarm", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBackground)
            }
        }
    }
}
