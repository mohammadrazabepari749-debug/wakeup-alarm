package com.wakeup.alarm.domain.verification

import com.wakeup.alarm.domain.model.VerificationState
import com.wakeup.alarm.domain.model.VerificationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VerificationEngine(
    private val scope: CoroutineScope,
    private val totalDurationSeconds: Int = 120
) {
    private val _state = MutableStateFlow(
        VerificationState(
            status = VerificationStatus.Idle,
            remainingSeconds = totalDurationSeconds,
            totalDurationSeconds = totalDurationSeconds
        )
    )
    val state: StateFlow<VerificationState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var isVerifiedAndFinished = false

    fun start() {
        if (isVerifiedAndFinished) return
        _state.value = _state.value.copy(
            status = VerificationStatus.FaceSearching,
            feedbackMessage = "Look into the front camera..."
        )
        startTimerLoop()
    }

    fun processFrame(result: FaceAnalysisResult) {
        if (isVerifiedAndFinished) return

        when (result) {
            is FaceAnalysisResult.NoFace -> {
                _state.value = _state.value.copy(
                    status = VerificationStatus.Paused,
                    isFaceDetected = false,
                    isLeftEyeOpen = false,
                    isRightEyeOpen = false,
                    eyeOpenProbabilityLeft = 0f,
                    eyeOpenProbabilityRight = 0f,
                    feedbackMessage = "Face not detected! Align with camera."
                )
            }
            is FaceAnalysisResult.FaceDetected -> {
                val bothEyesOpen = result.areBothEyesOpen
                if (bothEyesOpen) {
                    _state.value = _state.value.copy(
                        status = VerificationStatus.Verifying,
                        isFaceDetected = true,
                        isLeftEyeOpen = result.isLeftEyeOpen,
                        isRightEyeOpen = result.isRightEyeOpen,
                        eyeOpenProbabilityLeft = result.leftEyeOpenProb,
                        eyeOpenProbabilityRight = result.rightEyeOpenProb,
                        feedbackMessage = "Verified! Keep looking..."
                    )
                } else {
                    _state.value = _state.value.copy(
                        status = VerificationStatus.Paused,
                        isFaceDetected = true,
                        isLeftEyeOpen = result.isLeftEyeOpen,
                        isRightEyeOpen = result.isRightEyeOpen,
                        eyeOpenProbabilityLeft = result.leftEyeOpenProb,
                        eyeOpenProbabilityRight = result.rightEyeOpenProb,
                        feedbackMessage = "Open both eyes wide!"
                    )
                }
            }
        }
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = scope.launch(Dispatchers.Default) {
            while (!isVerifiedAndFinished) {
                delay(1000)
                val currentState = _state.value

                if (currentState.status == VerificationStatus.Verifying) {
                    val newRemaining = (currentState.remainingSeconds - 1).coerceAtLeast(0)
                    val elapsed = totalDurationSeconds - newRemaining
                    val progress = (elapsed.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)

                    if (newRemaining == 0) {
                        isVerifiedAndFinished = true
                        _state.value = currentState.copy(
                            status = VerificationStatus.Success,
                            remainingSeconds = 0,
                            progressPercentage = 1.0f,
                            feedbackMessage = "WAKE-UP VERIFIED! GREAT JOB!"
                        )
                        break
                    } else {
                        _state.value = currentState.copy(
                            remainingSeconds = newRemaining,
                            progressPercentage = progress
                        )
                    }
                }
            }
        }
    }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
    }
}
