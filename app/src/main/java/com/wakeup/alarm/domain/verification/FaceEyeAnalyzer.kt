package com.wakeup.alarm.domain.verification

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceEyeAnalyzer(
    private val onAnalysisResult: (FaceAnalysisResult) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL_LANDMARKS)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL_CLASSIFICATIONS)
        .setMinFaceSize(0.15f)
        .enableTracking()
        .build()

    private val detector = FaceDetection.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                if (faces.isEmpty()) {
                    onAnalysisResult(FaceAnalysisResult.NoFace)
                } else {
                    val primaryFace = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
                    if (primaryFace != null) {
                        val leftEyeOpenProb = primaryFace.leftEyeOpenProbability ?: -1f
                        val rightEyeOpenProb = primaryFace.rightEyeOpenProbability ?: -1f
                        val isLeftEyeOpen = leftEyeOpenProb > EYE_OPEN_THRESHOLD
                        val isRightEyeOpen = rightEyeOpenProb > EYE_OPEN_THRESHOLD

                        onAnalysisResult(
                            FaceAnalysisResult.FaceDetected(
                                face = primaryFace,
                                leftEyeOpenProb = leftEyeOpenProb,
                                rightEyeOpenProb = rightEyeOpenProb,
                                isLeftEyeOpen = isLeftEyeOpen,
                                isRightEyeOpen = isRightEyeOpen,
                                areBothEyesOpen = isLeftEyeOpen && isRightEyeOpen
                            )
                        )
                    } else {
                        onAnalysisResult(FaceAnalysisResult.NoFace)
                    }
                }
            }
            .addOnFailureListener {
                onAnalysisResult(FaceAnalysisResult.NoFace)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    companion object {
        const val EYE_OPEN_THRESHOLD = 0.45f
    }
}

sealed class FaceAnalysisResult {
    object NoFace : FaceAnalysisResult()
    data class FaceDetected(
        val face: Face,
        val leftEyeOpenProb: Float,
        val rightEyeOpenProb: Float,
        val isLeftEyeOpen: Boolean,
        val isRightEyeOpen: Boolean,
        val areBothEyesOpen: Boolean
    ) : FaceAnalysisResult()
}
