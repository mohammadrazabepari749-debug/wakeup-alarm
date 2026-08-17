package com.wakeup.alarm.data.repository

import com.wakeup.alarm.data.local.db.AlarmDao
import com.wakeup.alarm.data.local.db.HistoryDao
import com.wakeup.alarm.data.local.entity.AlarmEntity
import com.wakeup.alarm.data.local.entity.HistoryEntity
import com.wakeup.alarm.domain.model.Alarm
import com.wakeup.alarm.domain.model.HistoryRecord
import com.wakeup.alarm.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AlarmRepositoryImpl(
    private val alarmDao: AlarmDao,
    private val historyDao: HistoryDao
) : AlarmRepository {

    override fun getAllAlarms(): Flow<List<Alarm>> {
        return alarmDao.getAllAlarms().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getEnabledAlarms(): List<Alarm> {
        return alarmDao.getEnabledAlarms().map { it.toDomain() }
    }

    override suspend fun getAlarmById(id: Long): Alarm? {
        return alarmDao.getAlarmById(id)?.toDomain()
    }

    override suspend fun insertAlarm(alarm: Alarm): Long {
        return alarmDao.insertAlarm(AlarmEntity.fromDomain(alarm))
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(AlarmEntity.fromDomain(alarm))
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        alarmDao.deleteAlarm(AlarmEntity.fromDomain(alarm))
    }

    override fun getAllHistory(): Flow<List<HistoryRecord>> {
        return historyDao.getAllHistory().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertHistory(record: HistoryRecord): Long {
        return historyDao.insertHistory(HistoryEntity.fromDomain(record))
    }
}
