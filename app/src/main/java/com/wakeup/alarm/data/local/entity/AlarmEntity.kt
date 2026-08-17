package com.wakeup.alarm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wakeup.alarm.domain.model.Alarm

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val isEnabled: Boolean,
    val daysOfWeekCsv: String,
    val verificationDurationSeconds: Int,
    val ringtoneUri: String?,
    val vibrate: Boolean
) {
    fun toDomain(): Alarm {
        val days = if (daysOfWeekCsv.isBlank()) {
            emptySet()
        } else {
            daysOfWeekCsv.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        }
        return Alarm(
            id = id,
            hour = hour,
            minute = minute,
            label = label,
            isEnabled = isEnabled,
            daysOfWeek = days,
            verificationDurationSeconds = verificationDurationSeconds,
            ringtoneUri = ringtoneUri,
            vibrate = vibrate
        )
    }

    companion object {
        fun fromDomain(alarm: Alarm): AlarmEntity {
            return AlarmEntity(
                id = alarm.id,
                hour = alarm.hour,
                minute = alarm.minute,
                label = alarm.label,
                isEnabled = alarm.isEnabled,
                daysOfWeekCsv = alarm.daysOfWeek.sorted().joinToString(","),
                verificationDurationSeconds = alarm.verificationDurationSeconds,
                ringtoneUri = alarm.ringtoneUri,
                vibrate = alarm.vibrate
            )
        }
    }
}
