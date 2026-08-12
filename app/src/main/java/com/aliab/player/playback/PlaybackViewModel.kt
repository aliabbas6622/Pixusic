package com.aliab.player.playback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliab.player.model.Song
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Activity-scoped owner of the [PlayerConnection]. Rebuilding the connection is cheap: playback
 * itself lives in [PlaybackService], so closing the controller here never stops audio.
 */
class PlaybackViewModel(private val connection: PlayerConnection) : ViewModel() {

    val uiState: StateFlow<PlaybackUiState> = connection.uiState

    /**
     * Called by [PlayerConnection] on pause with the exact current position. The host wires this
     * to whatever persistence layer owns the queue snapshot — we don't import that here so this
     * class stays free of the data layer.
     */
    var onPausePersist: ((positionMs: Long) -> Unit)? = null

    init {
        connection.onTrackChanged = ::onTrackChanged
        connection.onPause = { positionMs -> onPausePersist?.invoke(positionMs) }
    }

    private fun onTrackChanged() {
        checkEndOfTrackSleepTimer()
    }

    fun playQueue(songs: List<Song>, startIndex: Int) = connection.playQueue(songs, startIndex)

    fun setQueue(songs: List<Song>, startIndex: Int = 0, positionMs: Long = 0L) = connection.setQueue(songs, startIndex, positionMs)

    fun togglePlayPause() = connection.togglePlayPause()

    fun skipToNext() = connection.skipToNext()

    fun skipToPrevious() = connection.skipToPrevious()

    fun seekTo(positionMs: Long) = connection.seekTo(positionMs)

    fun setShuffleEnabled(enabled: Boolean) = connection.setShuffleEnabled(enabled)

    fun setRepeatMode(mode: PlaybackRepeatMode) = connection.setRepeatMode(mode)

    fun setPositionUpdatesEnabled(enabled: Boolean) = connection.setPositionUpdatesEnabled(enabled)

    fun seekToQueueItem(index: Int, positionMs: Long = 0L) = connection.seekToQueueItem(index, positionMs)

    fun addNext(song: Song) = connection.addNext(song)

    fun addToQueueEnd(song: Song) = connection.addToQueueEnd(song)

    fun moveQueueItem(fromIndex: Int, toIndex: Int) = connection.moveQueueItem(fromIndex, toIndex)

    fun removeQueueItem(index: Int) = connection.removeQueueItem(index)

    /** Drops every occurrence of [songId] from the live queue (e.g. after the file was deleted). */
    fun removeSongFromQueue(songId: Long) {
        val state = connection.uiState.value
        val indices = state.queue.mapIndexedNotNull { index, song ->
            if (song.id == songId) index else null
        }
        // Remove from the end so earlier indices stay valid. If the current item is removed,
        // Media3 advances to the next track automatically.
        indices.sortedDescending().forEach { connection.removeQueueItem(it) }
    }

    fun clearQueue() = connection.clearQueue()

    private val _sleepTimerRemainingMs = MutableStateFlow<Long?>(null)
    val sleepTimerRemainingMs: StateFlow<Long?> = _sleepTimerRemainingMs.asStateFlow()

    private var sleepTimerJob: Job? = null
    private var isEndOfTrackSleepTimer = false

    fun startSleepTimer(durationMinutes: Int, fadeOut: Boolean = true) {
        cancelSleepTimer()
        isEndOfTrackSleepTimer = false
        val totalMs = durationMinutes * 60 * 1000L
        sleepTimerJob = viewModelScope.launch {
            var remaining = totalMs
            val stepMs = 1000L
            while (remaining > 0) {
                _sleepTimerRemainingMs.value = remaining
                delay(stepMs)
                remaining -= stepMs
                if (fadeOut && remaining <= 10_000L && remaining > 0) {
                    val volume = (remaining.toFloat() / 10_000L).coerceIn(0f, 1f)
                    connection.setVolume(volume)
                }
            }
            _sleepTimerRemainingMs.value = null
            connection.pause()
            connection.setVolume(1.0f)
        }
    }

    fun startSleepTimerEndOfTrack() {
        cancelSleepTimer()
        isEndOfTrackSleepTimer = true
        _sleepTimerRemainingMs.value = -1L // Indicates end-of-track mode
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        isEndOfTrackSleepTimer = false
        _sleepTimerRemainingMs.value = null
        connection.setVolume(1.0f)
    }

    fun checkEndOfTrackSleepTimer() {
        if (isEndOfTrackSleepTimer) {
            cancelSleepTimer()
            connection.pause()
        }
    }

    fun setVolume(volume: Float) = connection.setVolume(volume)

    override fun onCleared() {
        cancelSleepTimer()
        connection.close()
    }
}
