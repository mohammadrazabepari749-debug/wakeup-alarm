package com.wakeup.alarm.ui.alarm

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wakeup.alarm.domain.model.HistoryRecord
import com.wakeup.alarm.domain.model.VerificationState
import com.wakeup.alarm.domain.model.VerificationStatus
import com.wakeup.alarm.domain.repository.AlarmRepository
import com.wakeup.alarm.domain.verification.FaceAnalysisResult
import com.wakeup.alarm.domain.verification.VerificationEngine
import com.wakeup.alarm.service.AlarmService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AlarmViewModel(
    private val repository: AlarmRepository,
    private val alarmId: Long,
    private val verificationDurationSeconds: Int
) : ViewModel() {

    val engine = VerificationEngine(
        scope = viewModelScope,
        totalDurationSeconds = verificationDurationSeconds
    )

    val verificationState: StateFlow<VerificationState> = engine.state

    init {
        engine.start()

        viewModelScope.launch {
            verificationState.collect { state ->
                if (state.status == VerificationStatus.Success) {
                    onVerificationSuccess()
                }
            }
        }
    }

    fun onCameraFrame(result: FaceAnalysisResult) {
        engine.processFrame(result)
    }

    private fun onVerificationSuccess() {
        viewModelScope.launch {
            val historyRecord = HistoryRecord(
                alarmId = alarmId,
                scheduledTimeMs = System.currentTimeMillis(),
                completionTimeMs = System.currentTimeMillis(),
                requiredDurationSeconds = verificationDurationSeconds,
                isSuccessful = true
            )
            repository.insertHistory(historyRecord)
        }
    }

    fun stopAlarmService(context: Context) {
        val intent = Intent(context, AlarmService::class.java).apply {
            action = AlarmService.ACTION_STOP_ALARM
        }
        context.startService(intent)
    }

    override fun onCleared() {
        engine.stop()
        super.onCleared()
    }

    class Factory(
        private val repository: AlarmRepository,
        private val alarmId: Long,
        private val durationSeconds: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AlarmViewModel(repository, alarmId, durationSeconds) as T
        }
    }
}
