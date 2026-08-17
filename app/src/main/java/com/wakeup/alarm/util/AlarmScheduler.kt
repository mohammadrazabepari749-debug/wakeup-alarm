package com.wakeup.alarm.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.wakeup.alarm.domain.model.Alarm
import com.wakeup.alarm.receiver.AlarmReceiver
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: Alarm) {
        if (!alarm.isEnabled) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("AlarmScheduler", "Cannot schedule exact alarms! Permission missing.")
            }
        }

        val triggerTimeMs = calculateNextTriggerTimeMs(alarm.hour, alarm.minute, alarm.daysOfWeek)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALARM
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, alarm.label)
            putExtra(AlarmReceiver.EXTRA_VERIFICATION_DURATION, alarm.verificationDurationSeconds)
            putExtra(AlarmReceiver.EXTRA_RINGTONE_URI, alarm.ringtoneUri)
            putExtra(AlarmReceiver.EXTRA_VIBRATE, alarm.vibrate)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, com.wakeup.alarm.ui.MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            alarm.id.toInt(),
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTimeMs, showPendingIntent)
        try {
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d("AlarmScheduler", "Alarm scheduled for ID ${alarm.id} at $triggerTimeMs ms")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "Failed to schedule exact alarm: ${e.message}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
            }
        }
    }

    fun cancel(alarmId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("AlarmScheduler", "Alarm cancelled for ID $alarmId")
    }

    companion object {
        fun calculateNextTriggerTimeMs(hour: Int, minute: Int, daysOfWeek: Set<Int>): Long {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val now = Calendar.getInstance()

            if (daysOfWeek.isEmpty()) {
                if (calendar.before(now)) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            } else {
                val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
                var daysUntilNext = 0
                while (true) {
                    val candidateDay = ((currentDay - 1 + daysUntilNext) % 7) + 1
                    if (daysOfWeek.contains(candidateDay)) {
                        if (daysUntilNext > 0 || calendar.after(now)) {
                            calendar.add(Calendar.DAY_OF_YEAR, daysUntilNext)
                            break
                        }
                    }
                    daysUntilNext++
                }
            }

            return calendar.timeInMillis
        }
    }
}
