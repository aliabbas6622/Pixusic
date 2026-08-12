package com.aliab.player.playback

import androidx.lifecycle.ViewModel
import com.aliab.player.model.Song
import kotlinx.coroutines.flow.StateFlow

/**
 * Activity-scoped owner of the [PlayerConnection]. Rebuilding the connection is cheap: playback
 * itself lives in [PlaybackService], so closing the controller here never stops audio.
 */
class PlaybackViewModel(private val connection: PlayerConnection) : ViewModel() {

    val uiState: StateFlow<PlaybackUiState> = connection.uiState

    fun playQueue(songs: List<Song>, startIndex: Int) = connection.playQueue(songs, startIndex)

    fun togglePlayPause() = connection.togglePlayPause()

    fun skipToNext() = connection.skipToNext()

    fun skipToPrevious() = connection.skipToPrevious()

    fun seekTo(positionMs: Long) = connection.seekTo(positionMs)

    fun setShuffleEnabled(enabled: Boolean) = connection.setShuffleEnabled(enabled)

    fun setRepeatMode(mode: PlaybackRepeatMode) = connection.setRepeatMode(mode)

    fun setPositionUpdatesEnabled(enabled: Boolean) = connection.setPositionUpdatesEnabled(enabled)

    fun seekToQueueItem(index: Int, positionMs: Long = 0L) = connection.seekToQueueItem(index, positionMs)

    fun removeQueueItem(index: Int) = connection.removeQueueItem(index)

    fun clearQueue() = connection.clearQueue()

    override fun onCleared() {
        connection.close()
    }
}
