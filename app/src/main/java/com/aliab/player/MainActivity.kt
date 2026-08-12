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

    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasAudioPermission = granted
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
