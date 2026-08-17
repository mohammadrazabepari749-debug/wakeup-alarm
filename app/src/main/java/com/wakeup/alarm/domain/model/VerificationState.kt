package com.wakeup.alarm.domain.model

sealed class VerificationStatus {
    object Idle : VerificationStatus()
    object Ringing : VerificationStatus()
    object FaceSearching : VerificationStatus()
    object EyesSearching : VerificationStatus()
    object Verifying : VerificationStatus()
    object Paused : VerificationStatus()
    object Success : VerificationStatus()
}

data class VerificationState(
    val status: VerificationStatus = VerificationStatus.Idle,
    val isFaceDetected: Boolean = false,
    val isLeftEyeOpen: Boolean = false,
    val isRightEyeOpen: Boolean = false,
    val eyeOpenProbabilityLeft: Float = 0f,
    val eyeOpenProbabilityRight: Float = 0f,
    val remainingSeconds: Int = 120,
    val totalDurationSeconds: Int = 120,
    val progressPercentage: Float = 0f,
    val feedbackMessage: String = "Position face in camera preview"
)
