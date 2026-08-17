package com.wakeup.alarm.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.wakeup.alarm.data.local.entity.AlarmEntity
import com.wakeup.alarm.data.local.entity.HistoryEntity

@Database(entities = [AlarmEntity::class, HistoryEntity::class], version = 1, exportSchema = false)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AlarmDatabase? = null

        fun getInstance(context: Context): AlarmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlarmDatabase::class.java,
                    "wakeup_alarm_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
