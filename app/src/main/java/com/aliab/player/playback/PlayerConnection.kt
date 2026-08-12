package com.aliab.player.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.aliab.player.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executor

/**
 * The UI's only route to [PlaybackService]. It owns a MediaController, not an ExoPlayer, so
 * activity recreation never owns or interrupts background audio.
 */
class PlayerConnection(context: Context) : AutoCloseable {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    private val _uiState = MutableStateFlow(PlaybackUiState())

    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()

    private var controller: MediaController? = null
    private var positionUpdateJob: Job? = null
    private var positionUpdatesEnabled = false
    private var isClosed = false

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            val queueChanged = events.contains(Player.EVENT_TIMELINE_CHANGED) ||
                events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)
            publishState(player, refreshQueue = queueChanged)
            refreshPositionUpdates()
        }

        override fun onMediaItemTransition(item: MediaItem?, reason: Int) {
            // Only a natural progression to the next track counts as the end of the current one.
            // The callback also fires for timeline replacement (playQueue/setQueue), user seeks
            // and shuffle-driven changes — none of which should trip an "end of track" timer.
            if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO ||
                reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT
            ) {
                onTrackChanged?.invoke()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            // The queue's last track (repeat off) never triggers a media-item transition, only
            // the ended state — so it must be detected here.
            if (playbackState == Player.STATE_ENDED) {
                onTrackChanged?.invoke()
            }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int,
        ) {
            // Repeat-ONE loops the same item with no transition and no ended state; the loop
            // point surfaces as an auto-transition position discontinuity.
            if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                onTrackChanged?.invoke()
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (!isPlaying) {
                // Pause is a checkpoint: persist position now so resume lands where the user left
                // off even if the process is killed before the next track-change. We re-read
                // currentPosition here because the published UI snapshot may be up to 500ms stale.
                controller?.let { c ->
                    // When the whole queue has ended, position == duration; persisting that would
                    // make a future restore a dead no-op at the end of the last track.
                    val duration = c.duration
                    val playbackEnded = c.playbackState == Player.STATE_ENDED
                    if (!playbackEnded && (duration == C.TIME_UNSET || c.currentPosition < duration)) {
                        onPause?.invoke(c.currentPosition.coerceAtLeast(0L))
                    }
                }
            }
        }
    }

    /** Optional callback fired whenever the active media item changes. */
    var onTrackChanged: (() -> Unit)? = null

    /** Optional callback fired when the player transitions from playing → paused. */
    var onPause: ((positionMs: Long) -> Unit)? = null

    private val controllerFuture = MediaController.Builder(
        appContext,
        SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java)),
    ).buildAsync()

    init {
        controllerFuture.addListener(
            {
                if (!isClosed && !controllerFuture.isCancelled) {
                    runCatching { controllerFuture.get() }
                        .onSuccess { connectedController ->
                            if (isClosed) {
                                connectedController.release()
                            } else {
                                controller = connectedController
                                connectedController.addListener(listener)
                                publishState(connectedController)
                                refreshPositionUpdates()
                            }
                        }
                        .onFailure {
                            // The service may not be available yet. State remains disconnected and a
                            // new app-scoped connection can be created by the host if it is restarted.
                            _uiState.value = PlaybackUiState()
                        }
                }
            },
            Executor { runnable -> scope.launch { runnable.run() } },
        )
    }

    /** Starts the only periodic work in this class while a progress indicator is visible. */
    @MainThread
    fun setPositionUpdatesEnabled(enabled: Boolean) {
        positionUpdatesEnabled = enabled
        refreshPositionUpdates()
    }

    fun play() = controller?.play()

    fun pause() = controller?.pause()

    fun togglePlayPause() {
        controller?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs.coerceAtLeast(0L))
    }

    fun seekToQueueItem(index: Int, positionMs: Long = 0L) {
        controller?.takeIf { index in 0 until it.mediaItemCount }
            ?.seekTo(index, positionMs.coerceAtLeast(0L))
    }

    fun skipToNext() = controller?.seekToNextMediaItem()

    fun skipToPrevious() = controller?.seekToPreviousMediaItem()

    fun setShuffleEnabled(enabled: Boolean) {
        controller?.shuffleModeEnabled = enabled
    }

    fun setRepeatMode(mode: PlaybackRepeatMode) {
        controller?.repeatMode = mode.playerValue
    }

    fun setVolume(volume: Float) {
        controller?.volume = volume.coerceIn(0f, 1f)
    }

    /** Replaces the queue and begins playback from [startIndex]. */
    fun playQueue(songs: List<Song>, startIndex: Int = 0, positionMs: Long = 0L) {
        if (songs.isEmpty()) return
        controller?.let {
            val safeIndex = startIndex.coerceIn(0, songs.lastIndex)
            it.setMediaItems(songs.map { song -> song.toMediaItem() }, safeIndex, positionMs.coerceAtLeast(0L))
            it.prepare()
            it.play()
        }
    }

    /** Replaces the queue without changing its play/pause intent. */
    fun setQueue(songs: List<Song>, startIndex: Int = 0, positionMs: Long = 0L) {
        controller?.let {
            if (songs.isEmpty()) {
                it.clearMediaItems()
            } else {
                it.setMediaItems(
                    songs.map { song -> song.toMediaItem() },
                    startIndex.coerceIn(0, songs.lastIndex),
                    positionMs.coerceAtLeast(0L),
                )
                it.prepare()
            }
        }
    }

    /** Inserts a song immediately after the active item (or at the end when nothing is active). */
    fun addNext(song: Song) {
        controller?.let {
            val insertionIndex = (it.currentMediaItemIndex + 1).coerceIn(0, it.mediaItemCount)
            it.addMediaItem(insertionIndex, song.toMediaItem())
        }
    }

    fun addToQueueEnd(song: Song) {
        controller?.addMediaItem(song.toMediaItem())
    }

    fun removeQueueItem(index: Int) {
        controller?.takeIf { index in 0 until it.mediaItemCount }?.removeMediaItem(index)
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        controller?.takeIf {
            fromIndex in 0 until it.mediaItemCount && toIndex in 0 until it.mediaItemCount
        }?.moveMediaItem(fromIndex, toIndex)
    }

    fun clearQueue() = controller?.clearMediaItems()

    override fun close() {
        if (isClosed) return
        isClosed = true
        positionUpdateJob?.cancel()
        positionUpdateJob = null
        controller?.removeListener(listener)
        controller?.release()
        controller = null
        if (!controllerFuture.isDone) controllerFuture.cancel(false)
        _uiState.value = PlaybackUiState()
    }

    @MainThread
    private fun refreshPositionUpdates() {
        if (isClosed || !positionUpdatesEnabled || controller?.isPlaying != true) {
            positionUpdateJob?.cancel()
            positionUpdateJob = null
            return
        }
        if (positionUpdateJob?.isActive == true) return

        positionUpdateJob = scope.launch {
            while (isActive) {
                val activeController = controller?.takeIf { it.isPlaying } ?: break
                publishState(activeController)
                delay(POSITION_UPDATE_INTERVAL_MS)
            }
        }
    }

    // The queue is rebuilt only when it actually changes; the 500 ms position tick must not
    // allocate a fresh list of every queue item (potentially thousands) twice a second.
    private var cachedQueue: List<Song> = emptyList()

    @MainThread
    private fun publishState(player: Player, refreshQueue: Boolean = false) {
        if (refreshQueue || cachedQueue.size != player.mediaItemCount) {
            cachedQueue = List(player.mediaItemCount) { index -> player.getMediaItemAt(index).toSong() }
        }
        _uiState.value = PlaybackUiState(
            isConnected = true,
            currentSong = player.currentMediaItem?.toSong(),
            isPlaying = player.isPlaying,
            repeatMode = PlaybackRepeatMode.fromPlayerValue(player.repeatMode),
            shuffleEnabled = player.shuffleModeEnabled,
            durationMs = player.duration.coerceAtLeast(0L),
            positionMs = player.currentPosition.coerceAtLeast(0L),
            queue = cachedQueue,
            currentQueueIndex = player.currentMediaItemIndex,
        )
    }

    private companion object {
        const val POSITION_UPDATE_INTERVAL_MS = 500L
        const val EXTRA_SONG_ID = "com.aliab.player.song.id"
        const val EXTRA_ALBUM_ID = "com.aliab.player.song.album_id"
        const val EXTRA_DURATION_MS = "com.aliab.player.song.duration_ms"
        const val EXTRA_TRACK_NUMBER = "com.aliab.player.song.track_number"
        const val EXTRA_DISC_NUMBER = "com.aliab.player.song.disc_number"
        const val EXTRA_YEAR = "com.aliab.player.song.year"
        const val EXTRA_MIME_TYPE = "com.aliab.player.song.mime_type"
        const val EXTRA_DATE_MODIFIED = "com.aliab.player.song.date_modified"
    }

    private fun Song.toMediaItem(): MediaItem {
        val extras = Bundle().apply {
            putLong(EXTRA_SONG_ID, id)
            putLong(EXTRA_ALBUM_ID, albumId)
            putLong(EXTRA_DURATION_MS, durationMs)
            putInt(EXTRA_TRACK_NUMBER, trackNumber ?: -1)
            putInt(EXTRA_DISC_NUMBER, discNumber ?: -1)
            putInt(EXTRA_YEAR, year ?: -1)
            putString(EXTRA_MIME_TYPE, mimeType)
            putLong(EXTRA_DATE_MODIFIED, dateModified)
        }
        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumTitle(album)
                    .setExtras(extras)
                    .build(),
            )
            .build()
    }

    private fun MediaItem.toSong(): Song {
        val extras = mediaMetadata.extras
        fun optionalInt(key: String): Int? = extras?.getInt(key, -1)?.takeIf { it >= 0 }
        return Song(
            id = extras?.getLong(EXTRA_SONG_ID, mediaId.toLongOrNull() ?: -1L)
                ?: mediaId.toLongOrNull() ?: -1L,
            uri = localConfiguration?.uri ?: Uri.EMPTY,
            title = mediaMetadata.title?.toString().orEmpty(),
            artist = mediaMetadata.artist?.toString().orEmpty(),
            album = mediaMetadata.albumTitle?.toString().orEmpty(),
            albumId = extras?.getLong(EXTRA_ALBUM_ID, -1L) ?: -1L,
            durationMs = extras?.getLong(EXTRA_DURATION_MS, 0L) ?: 0L,
            trackNumber = optionalInt(EXTRA_TRACK_NUMBER),
            discNumber = optionalInt(EXTRA_DISC_NUMBER),
            year = optionalInt(EXTRA_YEAR),
            mimeType = extras?.getString(EXTRA_MIME_TYPE),
            dateModified = extras?.getLong(EXTRA_DATE_MODIFIED, 0L) ?: 0L,
        )
    }
}
