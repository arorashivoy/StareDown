package com.arorashivoy.staredown
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.Locale

class SinglePlayerActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateRunnable: Runnable

    private lateinit var username: String
    private lateinit var previewView: PreviewView
    private lateinit var blinkResult: TextView
    private lateinit var startBtn: Button
    private lateinit var countdownText: TextView
    private lateinit var sensorManager: SensorManager
    private var proximitySensor: Sensor? = null
    private var lightSensor: Sensor? = null

    private var cameraStarted = false
    private var cameraStopped = false
    private var startTime = 0L
    private var blinked = false

    private val proximityListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.values[0] >= proximitySensor?.maximumRange ?: 5f) {
                Toast.makeText(this@SinglePlayerActivity, "Move closer to the camera!", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    private val lightListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val lp = window.attributes
            val brightness = (event.values[0] / 1000).coerceIn(0.1f, 1.0f)
            lp.screenBrightness = brightness
            window.attributes = lp
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_single_player)

        username = intent.getStringExtra("username") ?: "unknown"

        // View initialization
        previewView = findViewById(R.id.previewView)
        blinkResult = findViewById(R.id.blinkResult)
        startBtn = findViewById(R.id.startBtn)
        countdownText = findViewById(R.id.countdownText)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        // Ask for camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            RequestCamera.newInstance().show(supportFragmentManager, "RequestCamera")
        }

        // Start button triggers countdown first
        startBtn.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                RequestCamera.newInstance().show(supportFragmentManager, "RequestCamera")
                return@setOnClickListener
            }


            startBtn.visibility = View.GONE
            blinkResult.visibility = View.VISIBLE
            startCountdownAndStartCamera()
        }

    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(proximityListener)
        sensorManager.unregisterListener(lightListener)
    }

    private fun beginBlinkTimer() {
        startTime = System.currentTimeMillis()
        cameraStarted = true
        cameraStopped = false
        blinked = false

        // Hide orange overlay and show camera
        findViewById<View>(R.id.coverOverlay)?.visibility = View.GONE

        // Start camera
        startCamera()

        proximitySensor?.let {
            sensorManager.registerListener(proximityListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        lightSensor?.let {
            sensorManager.registerListener(lightListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }


        // Start live blink info update
        updateRunnable = object : Runnable {
            override fun run() {
                if (!blinked && cameraStarted) {
                    val elapsed = System.currentTimeMillis() - startTime
                    val minutes = elapsed / 60000
                    val seconds = (elapsed % 60000) / 1000
                    val millis = elapsed % 1000
                    blinkResult.text = String.format(
                        Locale.US, "%02d:%02d:%03d", minutes, seconds, millis
                    )
                    handler.postDelayed(this, 50)
                }
            }
        }

        handler.post(updateRunnable)
    }

    private fun startCountdownAndStartCamera() {
        val countdownValues = listOf("3", "2", "1")
        var index = 0

        // Show countdown text in center
        countdownText.visibility = View.VISIBLE

        // Optional: Show orange overlay while camera is hidden
        findViewById<View>(R.id.coverOverlay)?.visibility = View.VISIBLE

        val countdownRunnable = object : Runnable {
            override fun run() {
                if (index < countdownValues.size) {
                    countdownText.text = countdownValues[index]
                    countdownText.scaleX = 0f
                    countdownText.scaleY = 0f
                    countdownText.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(400)
                        .withEndAction {
                            index++
                            handler.postDelayed(this, 600)
                        }.start()
                } else {
                    countdownText.visibility = View.GONE
                    beginBlinkTimer()
                }
            }
        }

        handler.post(countdownRunnable)
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

        @androidx.annotation.OptIn(ExperimentalGetImage::class)
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
                        Log.d("SHIVOY", "No faces detected")
                        Toast.makeText(this@SinglePlayerActivity, "Face not detected", Toast.LENGTH_SHORT).show()
                        blinkDetected()
                        return@addOnSuccessListener
                    }

                    if (!cameraStarted) {
                        imageProxy.close()
                        return@addOnSuccessListener
                    }

                    faces.forEach { face ->
                        val leftEyeOpen = face.leftEyeOpenProbability ?: 1f
                        val rightEyeOpen = face.rightEyeOpenProbability ?: 1f

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

        pushToLeaderboard(blinkDuration)

        runOnUiThread {
            handler.removeCallbacks(updateRunnable)
            sensorManager.unregisterListener(proximityListener)
            sensorManager.unregisterListener(lightListener)

            blinkResult.text = "Blink Detected!"
            // Move to next screen after short delay
            handler.postDelayed({
                val intent = Intent(this, ResultActivity::class.java)
                    .putExtra("blinkTime", blinkDuration)
                    .putExtra("username", username)
                startActivity(intent)
            }, 1500)  // delay 1.5 seconds to let user read the message

        }

        // Unbind camera to freeze frame
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this@SinglePlayerActivity)
        cameraProviderFuture.addListener({
            cameraProviderFuture.get().unbindAll()
        }, ContextCompat.getMainExecutor(this@SinglePlayerActivity))
    }

    private fun pushToLeaderboard(newScore: Long) {
        Log.d("SHIVOY", "Pushing to db")
        val dbRef = Firebase.database.reference.child("leaderboard").child(username)

        dbRef.get().addOnSuccessListener { snapshot ->
            val existing = snapshot.getValue(Leader::class.java)

            val topScores = mutableListOf<Long>()
            existing?.let {
                topScores.add(it.score)
                topScores.add(it.score2)
                topScores.add(it.score3)
            }

            topScores.add(newScore)
            topScores.sortDescending()  // Highest first

            val updatedLeader = Leader(
                username = username,
                score = topScores.getOrElse(0) { 0 },
                score2 = topScores.getOrElse(1) { 0 },
                score3 = topScores.getOrElse(2) { 0 }
            )

            dbRef.setValue(updatedLeader)
        }.addOnFailureListener {
            Log.e("SHIVOY", "Failed to fetch existing scores", it)
            // In case of error, save new score as top1
            val leader = Leader(username, newScore, 0, 0)
            dbRef.setValue(leader)
        }
    }
}