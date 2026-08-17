package com.wakeup.alarm.util

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.wakeup.alarm.domain.verification.FaceAnalysisResult
import com.wakeup.alarm.domain.verification.FaceEyeAnalyzer
import java.util.concurrent.Executors

class CameraXHelper(private val context: Context) {

    private val cameraExecutor = Executors.newSingleThreadExecutor()

    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onAnalysisResult: (FaceAnalysisResult) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(
                    cameraExecutor,
                    FaceEyeAnalyzer(onAnalysisResult)
                )

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

            } catch (e: Exception) {
                Log.e("CameraXHelper", "Use case binding failed: ${e.message}", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun shutdown() {
        cameraExecutor.shutdown()
    }
}
