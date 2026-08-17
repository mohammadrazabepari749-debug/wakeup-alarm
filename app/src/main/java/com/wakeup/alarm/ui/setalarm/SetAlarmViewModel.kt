package com.wakeup.alarm.ui.setalarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wakeup.alarm.domain.model.Alarm
import com.wakeup.alarm.domain.repository.AlarmRepository
import com.wakeup.alarm.util.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class SetAlarmViewModel(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler,
    private val alarmId: Long?
) : ViewModel() {

    private val _hour = MutableStateFlow(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
    val hour: StateFlow<Int> = _hour.asStateFlow()

    private val _minute = MutableStateFlow(Calendar.getInstance().get(Calendar.MINUTE))
    val minute: StateFlow<Int> = _minute.asStateFlow()

    private val _label = MutableStateFlow("Wake Up!")
    val label: StateFlow<String> = _label.asStateFlow()

    private val _daysOfWeek = MutableStateFlow<Set<Int>>(emptySet())
    val daysOfWeek: StateFlow<Set<Int>> = _daysOfWeek.asStateFlow()

    private val _verificationDurationSeconds = MutableStateFlow(120)
    val verificationDurationSeconds: StateFlow<Int> = _verificationDurationSeconds.asStateFlow()

    private val _vibrate = MutableStateFlow(true)
    val vibrate: StateFlow<Boolean> = _vibrate.asStateFlow()

    init {
        if (alarmId != null && alarmId > 0) {
            viewModelScope.launch {
                repository.getAlarmById(alarmId)?.let { alarm ->
                    _hour.value = alarm.hour
                    _minute.value = alarm.minute
                    _label.value = alarm.label
                    _daysOfWeek.value = alarm.daysOfWeek
                    _verificationDurationSeconds.value = alarm.verificationDurationSeconds
                    _vibrate.value = alarm.vibrate
                }
            }
        }
    }

    fun setTime(h: Int, m: Int) {
        _hour.value = h
        _minute.value = m
    }

    fun setLabel(l: String) {
        _label.value = l
    }

    fun toggleDay(day: Int) {
        val current = _daysOfWeek.value.toMutableSet()
        if (current.contains(day)) {
            current.remove(day)
        } else {
            current.add(day)
        }
        _daysOfWeek.value = current
    }

    fun setDuration(seconds: Int) {
        _verificationDurationSeconds.value = seconds
    }

    fun setVibrate(v: Boolean) {
        _vibrate.value = v
    }

    fun saveAlarm(onSaved: () -> Unit) {
        viewModelScope.launch {
            val alarm = Alarm(
                id = alarmId ?: 0,
                hour = _hour.value,
                minute = _minute.value,
                label = _label.value,
                isEnabled = true,
                daysOfWeek = _daysOfWeek.value,
                verificationDurationSeconds = _verificationDurationSeconds.value,
                vibrate = _vibrate.value
            )

            val savedId = if (alarm.id > 0) {
                repository.updateAlarm(alarm)
                alarm.id
            } else {
                repository.insertAlarm(alarm)
            }

            val savedAlarm = alarm.copy(id = savedId)
            scheduler.schedule(savedAlarm)
            onSaved()
        }
    }

    class Factory(
        private val repository: AlarmRepository,
        private val scheduler: AlarmScheduler,
        private val alarmId: Long?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SetAlarmViewModel(repository, scheduler, alarmId) as T
        }
    }
}
