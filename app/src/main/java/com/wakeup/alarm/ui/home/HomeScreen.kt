package com.wakeup.alarm.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeup.alarm.domain.model.Alarm
import com.wakeup.alarm.ui.theme.DarkBackground
import com.wakeup.alarm.ui.theme.PrimaryNeon
import com.wakeup.alarm.ui.theme.SurfaceDark
import com.wakeup.alarm.ui.theme.TextPrimary
import com.wakeup.alarm.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddAlarmClick: () -> Unit,
    onEditAlarmClick: (Long) -> Unit
) {
    val alarms by viewModel.alarms.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = PrimaryNeon,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = "WakeUp Alarms",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAlarmClick,
                containerColor = PrimaryNeon,
                contentColor = DarkBackground
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Alarm")
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Quick Test Alarm",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = "Triggers in 5 seconds with 10s face verification.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.createQuickTestAlarm(10) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = DarkBackground)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (alarms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No alarms set. Tap '+' to create one!",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmItemCard(
                            alarm = alarm,
                            onToggle = { isEnabled -> viewModel.toggleAlarm(alarm, isEnabled) },
                            onDelete = { viewModel.deleteAlarm(alarm) },
                            onClick = { onEditAlarmClick(alarm.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmItemCard(
    alarm: Alarm,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = alarm.getTimeFormatted(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (alarm.isEnabled) TextPrimary else TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (alarm.isEnabled) PrimaryNeon else TextSecondary
                    )
                    Text(
                        text = " • ${alarm.getDaysFormatted()} • ${alarm.verificationDurationSeconds}s verif.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Alarm",
                        tint = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DarkBackground,
                        checkedTrackColor = PrimaryNeon,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = DarkBackground
                    )
                )
            }
        }
    }
}
