package com.aliab.player

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.aliab.player.ui.PlayerApp

class MainActivity : ComponentActivity() {
    private var hasAudioPermission by mutableStateOf(false)

    /** Only ask for notifications once per process, after audio access is already granted. */
    private var notificationPermissionRequested = false

    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasAudioPermission = granted
        if (granted) requestNotificationPermissionIfNeeded()
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _ ->
        // Media playback still runs without it; the media notification is simply hidden.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hasAudioPermission = hasAudioPermission()

        setContent {
            PlayerApp(
                audioPermissionGranted = hasAudioPermission,
                onRequestPermission = ::requestAudioPermission,
            )
        }

        // Android 13+ blocks notifications (including media controls) until POST_NOTIFICATIONS
        // is granted. Ask right after audio access is granted — not on top of the audio screen.
        requestNotificationPermissionIfNeeded()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (notificationPermissionRequested) return
        if (!hasAudioPermission()) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionRequested = true
            return
        }
        notificationPermissionRequested = true
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun onResume() {
        super.onResume()
        hasAudioPermission = hasAudioPermission()
    }

    private fun requestAudioPermission() {
        if (!hasAudioPermission()) {
            audioPermissionLauncher.launch(audioPermission())
        }
    }

    private fun hasAudioPermission(): Boolean = ContextCompat.checkSelfPermission(
        this,
        audioPermission(),
    ) == PackageManager.PERMISSION_GRANTED

    private fun audioPermission(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}
