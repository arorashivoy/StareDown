package com.arorashivoy.staredown

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.Locale

class SinglePlayerActivity: AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateRunnable: Runnable

    private lateinit var previewView: PreviewView
    private lateinit var blinkInfo: TextView
    private lateinit var startBtn: Button
    private lateinit var redCameraCorners: View
    private lateinit var greenCameraCorners: View
    private var cameraStarted = false
    private var cameraStopped = false
    private var startTime = 0L
    private var blinked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_single_player)


        previewView = findViewById(R.id.previewView)
        blinkInfo = findViewById(R.id.blinkInfo)
        startBtn = findViewById(R.id.startBtn)
//        redCameraCorners = findViewById(R.id.redCameraCorners)
//        greenCameraCorners = findViewById(R.id.greenCameraCorners)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            RequestCamera.newInstance().show(supportFragmentManager, "RequestCamera")
        }
        else {
            startCamera()
        }

        startBtn.setOnClickListener{
            startTime = System.currentTimeMillis()
            cameraStarted = true
            cameraStopped = false
            blinked = false

            updateRunnable = object : Runnable {
                override fun run() {
                    if (!blinked && cameraStarted) {
                        val elapsed = System.currentTimeMillis() - startTime
                        val minutes = elapsed / 60000
                        val seconds = (elapsed % 60000) / 1000
                        val millis = elapsed % 1000
                        blinkInfo.text = String.format(
                            Locale.US,
                            "Time elapsed: %02d:%02d:%03d",
                            minutes, seconds, millis
                        )
                        handler.postDelayed(this, 50) // update every 50ms
                    }
                }
            }
            handler.post(updateRunnable)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(this), FaceAnalyzer())
                }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
        }, ContextCompat.getMainExecutor(this))
    }

    inner class FaceAnalyzer : ImageAnalysis.Analyzer {
        private val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .enableTracking()
                .build()
        )

        @OptIn(ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {

            val mediaImage = imageProxy.image ?: run {
                imageProxy.close()
                Log.d("SHIVOY", "ImageProxy is null")
                return
            }

            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isEmpty()) {
                        // TODO: Add view of no face detected maybe make corners red
                        Log.d("SHIVOY", "No faces detected")
//                        greenCameraCorners.visibility = View.GONE
//                        redCameraCorners.visibility = View.VISIBLE
                        return@addOnSuccessListener
                    } else {
                        Log.d("SHIVOY", "Faces detected: ${faces.size}")
//                        redCameraCorners.visibility = View.GONE
//                        greenCameraCorners.visibility = View.VISIBLE
                    }

                    if (!cameraStarted) {
                        imageProxy.close()
                        return@addOnSuccessListener
                    }

                    faces.forEach { face ->
                        val leftEyeOpen = face.leftEyeOpenProbability ?: 1f
                        val rightEyeOpen = face.rightEyeOpenProbability ?: 1f

//                        Log.d("SHIVOY", "Face detected: $face")

//                        Log.d("SHIVOY", "Left Eye Open: $leftEyeOpen, Right Eye Open: $rightEyeOpen")

                        val isCurrentlyBlinking = leftEyeOpen < 0.3f && rightEyeOpen < 0.3f

                        if (isCurrentlyBlinking && !blinked && !cameraStopped) {
                            blinkDetected()
                        }

                    }
                }
                .addOnFailureListener { /* ignore */ }
                .addOnCompleteListener { imageProxy.close() }
        }
    }

    private fun blinkDetected() {
        blinked = true
        cameraStopped = true

        val blinkDuration = System.currentTimeMillis() - startTime
        val minutes = blinkDuration / 60000
        val seconds = (blinkDuration % 60000) / 1000
        val millis = blinkDuration % 1000

        runOnUiThread {
            handler.removeCallbacks(updateRunnable)
            blinkInfo.text = String.format(
                Locale.US,
                "Blink detected!\nTime: %02d:%02d:%03d",
                minutes, seconds, millis
            )
        }

        // Unbind camera to freeze frame
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this@SinglePlayerActivity)
        cameraProviderFuture.addListener({
            cameraProviderFuture.get().unbindAll()
        }, ContextCompat.getMainExecutor(this@SinglePlayerActivity))
    }

}