package ma.ensa.projet.camapp.ui.activities

import ma.ensa.projet.camapp.ui.activities.GalleryActivity


import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.*
import android.provider.MediaStore
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.video.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ma.ensa.projet.camapp.R
import ma.ensa.projet.camapp.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    // Declare variables for video recording and camera setup
    private var recording: Recording? = null
    private var isPhoto = true
    private lateinit var imageCapture: ImageCapture
    private lateinit var videoCapture: VideoCapture<Recorder>
    private lateinit var camera: Camera
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    // Define necessary permissions based on the Android version
    private val permissions = if (Build.VERSION.SDK_INT >= 33) {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    } else {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 14
        private const val AUDIO_PERMISSION_REQUEST_CODE = 1001
    }
    // Initialize the activity, check permissions, and start the camera
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (checkPermissions()) startCamera()
        setupListeners()
    }
    // Set up button click listeners for various camera actions
    private fun setupListeners() {
        binding.apply {
            flipCameraIB.setOnClickListener {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    CameraSelector.LENS_FACING_BACK
                } else {
                    CameraSelector.LENS_FACING_FRONT
                }
                startCamera()
            }

            changeCameraToVideoIB.setOnClickListener {
                isPhoto = !isPhoto
                updateCaptureMode()
            }

            captureIB.setOnClickListener {
                if (isPhoto) {
                    takePhoto()
                } else {
                    captureVideo()
                }
            }

            flashToggleIB.setOnClickListener {
                toggleFlashlight()
            }

            openGalleryButton.setOnClickListener {
                startActivity(GalleryActivity.newIntent(this@MainActivity))
            }
        }
    }

    private fun toggleFlashlight() {
        if (camera.cameraInfo.hasFlashUnit()) {
            val isFlashOn = camera.cameraInfo.torchState.value == TorchState.ON
            camera.cameraControl.enableTorch(!isFlashOn)
            binding.flashToggleIB.setImageResource(
                if (isFlashOn) R.drawable.flash_off else R.drawable.flash_on
            )
        } else {
            Toast.makeText(this, "Flash is not available", Toast.LENGTH_SHORT).show()
        }
    }
    // Update UI elements to reflect capture mode (photo or video)
    private fun updateCaptureMode() {
        binding.apply {
            changeCameraToVideoIB.setImageResource(
                if (isPhoto) R.drawable.ic_photo else R.drawable.ic_videocam
            )
            captureIB.setImageResource(
                if (isPhoto) R.drawable.camera else R.drawable.ic_start
            )
        }
    }
    // Check if all permissions are granted, otherwise request them
    private fun checkPermissions(): Boolean {
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        return if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                notGranted.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
            false
        } else true
    }

    private fun checkAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            AUDIO_PERMISSION_REQUEST_CODE
        )
    }
    // Handle permission results
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    startCamera()
                }
            }
            AUDIO_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    captureVideo()
                } else {
                    Toast.makeText(
                        this,
                        "Audio permission is required for video recording",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    // Start the camera using CameraX
    private fun startCamera() {
        ProcessCameraProvider.getInstance(this).apply {
            addListener({
                bindCamera(get())
            }, ContextCompat.getMainExecutor(this@MainActivity))
        }
    }
    // Bind camera use cases (preview, image capture, video capture) to the lifecycle
    private fun bindCamera(provider: ProcessCameraProvider? = null) {
        val cameraProvider = provider ?: return
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(binding.previewView.surfaceProvider)
        }

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
            .build()

        videoCapture = VideoCapture.withOutput(recorder)
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageCapture,
                videoCapture
            )
            setupTouchToFocus()
        } catch (e: Exception) {
            Toast.makeText(this, "Camera binding failed", Toast.LENGTH_SHORT).show()
        }
    }
    // Set up touch-to-focus functionality(using lambda function)
    private fun setupTouchToFocus() {
        binding.previewView.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val factory = binding.previewView.meteringPointFactory
                val point = factory.createPoint(event.x, event.y)
                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                    .setAutoCancelDuration(2, TimeUnit.SECONDS)
                    .build()
                camera.cameraControl.startFocusAndMetering(action)
                view.performClick()
            }
            true
        }
    }
    // Capture a photo and save it to the device storage
    private fun takePhoto() {
        val fileName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
            .format(System.currentTimeMillis()) + ".jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(
                        this@MainActivity,
                        "Photo saved",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(
                        this@MainActivity,
                        "Photo failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
    // Start or stop video recording
    private fun captureVideo() {
        val recording = this.recording
        if (recording != null) {
            recording.stop()
            this.recording = null
            return
        }

        val fileName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
            .format(System.currentTimeMillis()) + ".mp4"

        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX")
            }
        }

        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()

        try {
            if (checkAudioPermission()) {
                this.recording = videoCapture.output
                    .prepareRecording(this@MainActivity, mediaStoreOutput)
                    .apply {
                        withAudioEnabled()
                    }
                    .start(ContextCompat.getMainExecutor(this)) { event ->
                        when (event) {
                            is VideoRecordEvent.Start -> {
                                binding.captureIB.setImageResource(R.drawable.ic_stop)
                            }
                            is VideoRecordEvent.Finalize -> {
                                if (!event.hasError()) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Video saved successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Video capture failed: ${event.error}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                binding.captureIB.setImageResource(R.drawable.ic_start)
                                this@MainActivity.recording = null
                            }
                        }
                    }
            } else {
                requestAudioPermission()
            }
        } catch (e: SecurityException) {
            Toast.makeText(
                this@MainActivity,
                "Audio permission is required for video recording",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        recording?.stop()
    }
}