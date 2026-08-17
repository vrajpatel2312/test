package com.udaypatel.wavelock

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var projectionManager: MediaProjectionManager

    private val recordAudioPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchProjectionRequest()
        } else {
            statusText.text = "Microphone permission is required for audio capture."
        }
    }

    // Note: Android reuses the screen-capture consent dialog for
    // AudioPlaybackCaptureConfiguration. The system wording will mention
    // "recording/casting" even though we only read audio, not the screen.
    private val projectionRequest = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            AudioCaptureService.start(this, result.resultCode, result.data!!)
            statusText.text = "Audio capture running. Now set the wallpaper."
        } else {
            statusText.text = "Audio capture permission denied."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        projectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        findViewById<Button>(R.id.grantAudioButton).setOnClickListener {
            requestNotificationPermissionIfNeeded()
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED
            ) {
                launchProjectionRequest()
            } else {
                recordAudioPermission.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        }

        findViewById<Button>(R.id.setWallpaperButton).setOnClickListener {
            openLiveWallpaperPicker()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001
                )
            }
        }
    }

    private fun launchProjectionRequest() {
        projectionRequest.launch(projectionManager.createScreenCaptureIntent())
    }

    private fun openLiveWallpaperPicker() {
        try {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(this, VisualizerWallpaperService::class.java)
            )
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Open Settings > Wallpaper to select WaveLock manually.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
