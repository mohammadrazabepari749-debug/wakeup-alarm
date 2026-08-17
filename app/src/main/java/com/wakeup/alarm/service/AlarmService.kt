package com.wakeup.alarm.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.wakeup.alarm.R
import com.wakeup.alarm.receiver.AlarmReceiver
import com.wakeup.alarm.ui.alarm.AlarmActivity

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_ALARM) {
            stopAlarm()
            stopSelf()
            return START_NOT_STICKY
        }

        val alarmId = intent?.getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, -1L) ?: -1L
        val label = intent?.getStringExtra(AlarmReceiver.EXTRA_ALARM_LABEL) ?: "Wake Up!"
        val duration = intent?.getIntExtra(AlarmReceiver.EXTRA_VERIFICATION_DURATION, 120) ?: 120
        val ringtoneUriString = intent?.getStringExtra(AlarmReceiver.EXTRA_RINGTONE_URI)
        val shouldVibrate = intent?.getBooleanExtra(AlarmReceiver.EXTRA_VIBRATE, true) ?: true

        createNotificationChannel()
        val notification = buildFullScreenNotification(alarmId, label, duration)

        startForeground(NOTIFICATION_ID, notification)

        startSoundAndVibration(ringtoneUriString, shouldVibrate)

        return START_STICKY
    }

    private fun startSoundAndVibration(ringtoneUriString: String?, shouldVibrate: Boolean) {
        try {
            val soundUri = if (!ringtoneUriString.isNull_or_empty()) {
                Uri.parse(ringtoneUriString)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, soundUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to start mediaPlayer: ${e.message}")
        }

        if (shouldVibrate) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibrator = vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }

                val pattern = longArrayOf(0, 500, 500)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, 0)
                }
            } catch (e: Exception) {
                Log.e("AlarmService", "Failed to start vibration: ${e.message}")
            }
        }
    }

    private fun stopAlarm() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("AlarmService", "Error stopping mediaPlayer: ${e.message}")
        }

        try {
            vibrator?.cancel()
            vibrator = null
        } catch (e: Exception) {
            Log.e("AlarmService", "Error stopping vibrator: ${e.message}")
        }
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm High Priority Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm ringing notifications"
                setSound(null, null)
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildFullScreenNotification(alarmId: Long, label: String, duration: Int): Notification {
        val alarmActivityIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmReceiver.EXTRA_VERIFICATION_DURATION, duration)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId.toInt(),
            alarmActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("ALARM RINGING!")
            .setContentText(label)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "wakeup_alarm_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP_ALARM = "com.wakeup.alarm.ACTION_STOP_ALARM"

        private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
    }
}
