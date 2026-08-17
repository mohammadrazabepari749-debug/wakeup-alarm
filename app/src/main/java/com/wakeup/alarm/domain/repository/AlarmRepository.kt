package com.wakeup.alarm.domain.repository

import com.wakeup.alarm.domain.model.Alarm
import com.wakeup.alarm.domain.model.HistoryRecord
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun getAllAlarms(): Flow<List<Alarm>>
    suspend fun getEnabledAlarms(): List<Alarm>
    suspend fun getAlarmById(id: Long): Alarm?
    suspend fun insertAlarm(alarm: Alarm): Long
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun deleteAlarm(alarm: Alarm)
    fun getAllHistory(): Flow<List<HistoryRecord>>
    suspend fun insertHistory(record: HistoryRecord): Long
}
