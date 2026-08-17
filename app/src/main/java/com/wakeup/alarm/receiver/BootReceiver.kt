package com.wakeup.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.wakeup.alarm.data.local.db.AlarmDatabase
import com.wakeup.alarm.data.repository.AlarmRepositoryImpl
import com.wakeup.alarm.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("BootReceiver", "Re-scheduling alarms due to action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val pendingResult = goAsync()
            val database = AlarmDatabase.getInstance(context)
            val repository = AlarmRepositoryImpl(database.alarmDao(), database.historyDao())
            val scheduler = AlarmScheduler(context)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val enabledAlarms = repository.getEnabledAlarms()
                    for (alarm in enabledAlarms) {
                        scheduler.schedule(alarm)
                    }
                    Log.d("BootReceiver", "Successfully re-scheduled ${enabledAlarms.size} alarms.")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error re-scheduling alarms: ${e.message}")
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
