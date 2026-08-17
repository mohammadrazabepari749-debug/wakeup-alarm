package com.wakeup.alarm.domain.model

data class HistoryRecord(
    val id: Long = 0,
    val alarmId: Long,
    val scheduledTimeMs: Long,
    val completionTimeMs: Long,
    val requiredDurationSeconds: Int,
    val isSuccessful: Boolean
)
