package com.aliab.player.playback

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.aliab.player.MainActivity
import com.aliab.player.R

/**
 * Process-wide owner of local audio playback.
 *
 * The player intentionally lives in a [MediaSessionService], not an Activity, so Android can
 * keep legitimate playback running when the app UI is recreated or sent to the background.
 */
@UnstableApi
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        // Media3 posts its media notification on this channel at LOW importance by default, which
        // can hide the transport controls from the lock screen. Pre-create it at HIGH (still
        // silent) so the controls always show.
        createMediaNotificationChannel()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        // Offload sends decoding to the DSP (near-zero CPU); REQUIRED fails playback instead of
        // silently falling back to a ~40% software decode, so we can verify offload availability.
        player.setTrackSelectionParameters(
            player.trackSelectionParameters
                .buildUpon()
                .setAudioOffloadPreferences(
                    TrackSelectionParameters.AudioOffloadPreferences.Builder()
                        .setAudioOffloadMode(
                            TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_REQUIRED,
                        )
                        .build(),
                )
                .build(),
        )

        // Tapping the media notification / lock-screen card opens the app.
        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivity)
            .build()
    }

    private fun createMediaNotificationChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            MEDIA_CHANNEL_ID,
            getString(R.string.media_notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            setShowBadge(false)
            setSound(null, null)
        }
        manager.createNotificationChannel(channel)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        mediaSession?.let { session ->
            session.player.release()
            session.release()
        }
        mediaSession = null
        super.onDestroy()
    }

    private companion object {
        /** The channel id Media3's DefaultMediaNotificationProvider posts its notification on. */
        const val MEDIA_CHANNEL_ID = "default_channel_id"
    }
}
