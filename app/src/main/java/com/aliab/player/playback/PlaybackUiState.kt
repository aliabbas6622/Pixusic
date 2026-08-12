package com.aliab.player.playback

import androidx.media3.common.Player
import com.aliab.player.model.Song

/** Values shown by player UI. This is a snapshot, never a mutable player object. */
data class PlaybackUiState(
    val isConnected: Boolean = false,
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val repeatMode: PlaybackRepeatMode = PlaybackRepeatMode.Off,
    val shuffleEnabled: Boolean = false,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val queue: List<Song> = emptyList(),
    val currentQueueIndex: Int = -1,
) {
    val queueSize: Int
        get() = queue.size
}

enum class PlaybackRepeatMode(@param:Player.RepeatMode internal val playerValue: Int) {
    Off(Player.REPEAT_MODE_OFF),
    One(Player.REPEAT_MODE_ONE),
    All(Player.REPEAT_MODE_ALL),
    ;

    internal companion object {
        fun fromPlayerValue(@Player.RepeatMode value: Int): PlaybackRepeatMode =
            entries.firstOrNull { it.playerValue == value } ?: Off
    }
}
