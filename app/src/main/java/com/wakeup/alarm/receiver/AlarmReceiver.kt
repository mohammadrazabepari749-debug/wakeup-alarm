package com.wakeup.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.wakeup.alarm.service.AlarmService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("AlarmReceiver", "Received action: $action")

        if (action == ACTION_TRIGGER_ALARM) {
            val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
            val label = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: "Wake Up!"
            val duration = intent.getIntExtra(EXTRA_VERIFICATION_DURATION, 120)
            val ringtoneUri = intent.getStringExtra(EXTRA_RINGTONE_URI)
            val vibrate = intent.getBooleanExtra(EXTRA_VIBRATE, true)

            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra(EXTRA_ALARM_ID, alarmId)
                putExtra(EXTRA_ALARM_LABEL, label)
                putExtra(EXTRA_VERIFICATION_DURATION, duration)
                putExtra(EXTRA_RINGTONE_URI, ringtoneUri)
                putExtra(EXTRA_VIBRATE, vibrate)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    companion object {
        const val ACTION_TRIGGER_ALARM = "com.wakeup.alarm.ACTION_TRIGGER_ALARM"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_LABEL = "extra_alarm_label"
        const val EXTRA_VERIFICATION_DURATION = "extra_verification_duration"
        const val EXTRA_RINGTONE_URI = "extra_ringtone_uri"
        const val EXTRA_VIBRATE = "extra_vibrate"
    }
}
