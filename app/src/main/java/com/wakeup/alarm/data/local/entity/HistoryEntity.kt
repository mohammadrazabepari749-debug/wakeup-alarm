package com.wakeup.alarm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wakeup.alarm.domain.model.HistoryRecord

@Entity(tableName = "history_records")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alarmId: Long,
    val scheduledTimeMs: Long,
    val completionTimeMs: Long,
    val requiredDurationSeconds: Int,
    val isSuccessful: Boolean
) {
    fun toDomain(): HistoryRecord {
        return HistoryRecord(
            id = id,
            alarmId = alarmId,
            scheduledTimeMs = scheduledTimeMs,
            completionTimeMs = completionTimeMs,
            requiredDurationSeconds = requiredDurationSeconds,
            isSuccessful = isSuccessful
        )
    }

    companion object {
        fun fromDomain(record: HistoryRecord): HistoryEntity {
            return HistoryEntity(
                id = record.id,
                alarmId = record.alarmId,
                scheduledTimeMs = record.scheduledTimeMs,
                completionTimeMs = record.completionTimeMs,
                requiredDurationSeconds = record.requiredDurationSeconds,
                isSuccessful = record.isSuccessful
            )
        }
    }
}
