package com.wakeup.alarm

import android.app.Application
import com.wakeup.alarm.data.local.db.AlarmDatabase
import com.wakeup.alarm.data.repository.AlarmRepositoryImpl
import com.wakeup.alarm.domain.repository.AlarmRepository
import com.wakeup.alarm.util.AlarmScheduler

class WakeUpApplication : Application() {

    lateinit var repository: AlarmRepository
        private set

    lateinit var alarmScheduler: AlarmScheduler
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AlarmDatabase.getInstance(this)
        repository = AlarmRepositoryImpl(database.alarmDao(), database.historyDao())
        alarmScheduler = AlarmScheduler(this)
    }
}
