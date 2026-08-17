package com.wakeup.alarm.ui.alarm

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.wakeup.alarm.WakeUpApplication
import com.wakeup.alarm.receiver.AlarmReceiver
import com.wakeup.alarm.ui.theme.WakeUpTheme

class AlarmActivity : ComponentActivity() {

    private var alarmId: Long = -1L
    private var label: String = "Wake Up!"
    private var durationSeconds: Int = 120

    private val viewModel: AlarmViewModel by viewModels {
        val app = application as WakeUpApplication
        AlarmViewModel.Factory(app.repository, alarmId, durationSeconds)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        showOverLockscreen()
        super.onCreate()

        alarmId = intent.getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, -1L)
        label = intent.getStringExtra(AlarmReceiver.EXTRA_ALARM_LABEL) ?: "Wake Up!"
        durationSeconds = intent.getIntExtra(AlarmReceiver.EXTRA_VERIFICATION_DURATION, 120)

        setContent {
            WakeUpTheme {
                AlarmScreen(
                    label = label,
                    viewModel = viewModel,
                    onDismiss = {
                        finish()
                    }
                )
            }
        }
    }

    private fun showOverLockscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
