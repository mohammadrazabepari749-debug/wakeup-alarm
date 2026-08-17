package com.wakeup.alarm.ui.alarm

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.wakeup.alarm.domain.model.VerificationStatus
import com.wakeup.alarm.ui.theme.DarkBackground
import com.wakeup.alarm.ui.theme.DangerRed
import com.wakeup.alarm.ui.theme.PrimaryNeon
import com.wakeup.alarm.ui.theme.SuccessGreen
import com.wakeup.alarm.ui.theme.SurfaceDark
import com.wakeup.alarm.ui.theme.TextPrimary
import com.wakeup.alarm.ui.theme.TextSecondary
import com.wakeup.alarm.ui.theme.WarningOrange
import com.wakeup.alarm.util.CameraXHelper
import com.wakeup.alarm.util.PermissionHelper
import java.util.Locale

@Composable
fun AlarmScreen(
    label: String,
    viewModel: AlarmViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val verificationState by viewModel.verificationState.collectAsState()

    val cameraXHelper = remember { CameraXHelper(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (!PermissionHelper.hasCameraPermission(context)) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraXHelper.shutdown()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        if (verificationState.status == VerificationStatus.Success) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = SuccessGreen,
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "WAKE-UP COMPLETE!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "You are fully awake and ready for your day!",
                    fontSize = 18.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(40.dp))
                Button(
                    onClick = {
                        viewModel.stopAlarmService(context)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Dismiss Alarm",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBackground
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "ALARM RINGING",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = DangerRed,
                    letterSpacing = 2.sp
                )
                Text(
                    text = label,
                    fontSize = 20.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AndroidView(
                            factory = { ctx ->
                                PreviewView(ctx).also { previewView ->
                                    cameraXHelper.startCamera(
                                        lifecycleOwner = lifecycleOwner,
                                        previewView = previewView,
                                        onAnalysisResult = { result ->
                                            viewModel.onCameraFrame(result)
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (verificationState.status == VerificationStatus.Verifying) {
                                        SuccessGreen.copy(alpha = 0.15f)
                                    } else {
                                        Color.Transparent
                                    }
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BadgeStatus(
                        label = "Face Detected",
                        isActive = verificationState.isFaceDetected
                    )
                    BadgeStatus(
                        label = "Eyes Open",
                        isActive = verificationState.isLeftEyeOpen && verificationState.isRightEyeOpen
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (verificationState.status == VerificationStatus.Verifying) {
                            SuccessGreen.copy(alpha = 0.2f)
                        } else {
                            SurfaceDark
                        }
                    )
                ) {
                    Text(
                        text = verificationState.feedbackMessage,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (verificationState.status == VerificationStatus.Verifying) SuccessGreen else WarningOrange,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                val remainingSeconds = verificationState.remainingSeconds
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60
                val totalMinutes = verificationState.totalDurationSeconds / 60
                val totalSecs = verificationState.totalDurationSeconds % 60

                Text(
                    text = String.format(Locale.getDefault(), "%02d:%02d / %02d:%02d", minutes, seconds, totalMinutes, totalSecs),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNeon
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { verificationState.progressPercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = PrimaryNeon,
                    trackColor = SurfaceDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Keep face in camera preview to stop alarm",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun BadgeStatus(label: String, isActive: Boolean) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) SuccessGreen.copy(alpha = 0.2f) else SurfaceDark
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.Check else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isActive) SuccessGreen else DangerRed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                color = if (isActive) SuccessGreen else TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}
