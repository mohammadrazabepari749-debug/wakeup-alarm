package com.wakeup.alarm.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wakeup.alarm.domain.model.Alarm
import com.wakeup.alarm.domain.repository.AlarmRepository
import com.wakeup.alarm.util.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler
) : ViewModel() {

    val alarms: StateFlow<List<Alarm>> = repository.getAllAlarms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubsubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleAlarm(alarm: Alarm, isEnabled: Boolean) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = isEnabled)
            repository.updateAlarm(updated)
            if (isEnabled) {
                scheduler.schedule(updated)
            } else {
                scheduler.cancel(updated.id)
            }
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            scheduler.cancel(alarm.id)
            repository.deleteAlarm(alarm)
        }
    }

    fun createQuickTestAlarm(testDurationSeconds: Int = 10) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance().apply {
                add(Calendar.SECOND, 5)
            }
            val testAlarm = Alarm(
                hour = calendar.get(Calendar.HOUR_OF_DAY),
                minute = calendar.get(Calendar.MINUTE),
                label = "Test Alarm (5s)",
                isEnabled = true,
                verificationDurationSeconds = testDurationSeconds
            )
            val id = repository.insertAlarm(testAlarm)
            scheduler.schedule(testAlarm.copy(id = id))
        }
    }

    class Factory(
        private val repository: AlarmRepository,
        private val scheduler: AlarmScheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository, scheduler) as T
        }
    }
}
