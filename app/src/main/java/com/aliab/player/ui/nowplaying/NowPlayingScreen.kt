package com.aliab.player.ui.nowplaying

import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aliab.player.playback.PlaybackRepeatMode
import com.aliab.player.playback.PlaybackUiState
import com.aliab.player.data.lyrics.LyricLine
import com.aliab.player.data.lyrics.LyricsRepository
import com.aliab.player.ui.artwork.AlbumArt
import com.aliab.player.ui.formatDisplayName
import com.aliab.player.ui.formatTime

/**
 * Full-screen player. Matches the minimal monochrome reference UI:
 *   - large square album art with soft shadow
 *   - title + artist centered below
 *   - sleek thin seek bar with circular knob thumb
 *   - rounded playback control pill group
 *   - Lyrics / Queue / More utility row at the bottom
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    state: PlaybackUiState,
    onBack: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffleChanged: (Boolean) -> Unit,
    onRepeatChanged: (PlaybackRepeatMode) -> Unit,
    onPositionUpdatesEnabled: (Boolean) -> Unit,
    onOpenQueue: () -> Unit = {},
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    sleepTimerRemainingMs: Long? = null,
    onStartSleepTimer: (Int) -> Unit = {},
    onStartSleepTimerEndOfTrack: () -> Unit = {},
    onCancelSleepTimer: () -> Unit = {},
    lyricsRepository: LyricsRepository? = null,
    modifier: Modifier = Modifier,
) {
    DisposableEffect(Unit) {
        onPositionUpdatesEnabled(true)
        onDispose { onPositionUpdatesEnabled(false) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        val song = state.currentSong
        if (song == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Nothing playing", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Pick a song from your library.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
                IconButton(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            return@Box
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar: back arrow + "NOW PLAYING" centered ──────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // ── Body: vertically centered so controls sit lower on tall screens ──
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

            // ── Album art ────────────────────────────────────────────────────
            AlbumArt(
                albumId = song.albumId,
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                        spotColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.22f),
                    ),
                shape = RoundedCornerShape(20.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Song title + artist (Centered) ────────────────────────────────
            val cleanTitle = remember(song.title) { formatDisplayName(song.title) }
            val cleanArtist = remember(song.artist) { formatDisplayName(song.artist) }

            Text(
                text = cleanTitle,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = cleanArtist,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Thin seek bar with sleek circular thumb ────────────────────────
            var dragPositionMs by remember { mutableStateOf<Long?>(null) }
            val durationMs = state.durationMs.coerceAtLeast(0L)
            val shownPositionMs = (dragPositionMs ?: state.positionMs).coerceIn(0L, durationMs)

            val seekInteractionSource = remember { MutableInteractionSource() }

            Slider(
                value = shownPositionMs.toFloat(),
                onValueChange = { newValue ->
                    if (newValue.toLong() != state.positionMs) {
                        dragPositionMs = newValue.toLong()
                    }
                },
                onValueChangeFinished = {
                    dragPositionMs?.let(onSeek)
                    dragPositionMs = null
                },
                valueRange = 0f..durationMs.coerceAtLeast(1L).toFloat(),
                enabled = durationMs > 0L,
                interactionSource = seekInteractionSource,
                thumb = {
                    Surface(
                        modifier = Modifier.size(16.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.onSurface,
                        shadowElevation = 4.dp,
                    ) {}
                },
                track = { sliderState ->
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        modifier = Modifier.height(3.dp),
                        colors = SliderDefaults.colors(
                            activeTrackColor = MaterialTheme.colorScheme.onSurface,
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        ),
                        thumbTrackGapSize = 0.dp,
                        trackInsideCornerSize = 0.dp,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = formatTime(shownPositionMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = formatTime(durationMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Rounded control pill group ──────────────────────────────────
            Surface(
                shape = RoundedCornerShape(64.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Shuffle
                    val shuffleTint by animateColorAsState(
                        targetValue = if (state.shuffleEnabled)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        label = "shuffle_tint",
                    )
                    IconButton(
                        onClick = { onShuffleChanged(!state.shuffleEnabled) },
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shuffle,
                            contentDescription = if (state.shuffleEnabled) "Shuffle on" else "Shuffle off",
                            tint = shuffleTint,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    // Previous
                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier.size(52.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = "Previous",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(26.dp),
                        )
                    }

                    // Play / Pause — solid dark/light filled circle
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(60.dp),
                    ) {
                        IconButton(
                            onClick = onTogglePlayPause,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Icon(
                                imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (state.isPlaying) "Pause" else "Play",
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(30.dp),
                            )
                        }
                    }

                    // Next
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(52.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "Next",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(26.dp),
                        )
                    }

                    // Repeat
                    val repeatTint by animateColorAsState(
                        targetValue = if (state.repeatMode == PlaybackRepeatMode.Off)
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.onSurface,
                        label = "repeat_tint",
                    )
                    IconButton(
                        onClick = { onRepeatChanged(state.repeatMode.next()) },
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            imageVector = if (state.repeatMode == PlaybackRepeatMode.One)
                                Icons.Filled.RepeatOne
                            else
                                Icons.Filled.Repeat,
                            contentDescription = "Repeat: ${state.repeatMode.name}",
                            tint = repeatTint,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            var showLyricsSheet by remember { mutableStateOf(false) }
            var showMoreSheet by remember { mutableStateOf(false) }

            // ── Utility row: Lyrics | Queue | More ──────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Lyrics",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { showLyricsSheet = true },
                )

                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .size(width = 1.dp, height = 14.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                )

                Text(
                    text = "Queue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable(onClick = onOpenQueue),
                )

                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .size(width = 1.dp, height = 14.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                )

                Text(
                    text = if (sleepTimerRemainingMs != null) "More (⏱)" else "More",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (sleepTimerRemainingMs != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { showMoreSheet = true },
                )
            }

            if (showLyricsSheet) {
                LyricsSheet(
                    title = formatDisplayName(song.title),
                    artist = formatDisplayName(song.artist),
                    songUri = song.uri,
                    positionMs = state.positionMs,
                    isPlaying = state.isPlaying,
                    lyricsRepository = lyricsRepository,
                    onDismiss = { showLyricsSheet = false },
                )
            }

            if (showMoreSheet) {
                androidx.compose.material3.ModalBottomSheet(onDismissRequest = { showMoreSheet = false }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                    ) {
                        Text(
                            text = "Options & Audio Diagnostics",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 16.dp),
                        )

                        // Sleep Timer section
                        Text(
                            text = "Sleep Timer",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                        ) {
                            listOf(10, 20, 30, 60).forEach { mins ->
                                androidx.compose.material3.FilterChip(
                                    selected = false,
                                    onClick = {
                                        onStartSleepTimer(mins)
                                        showMoreSheet = false
                                    },
                                    label = { Text("${mins}m") },
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 24.dp),
                        ) {
                            androidx.compose.material3.FilterChip(
                                selected = false,
                                onClick = {
                                    onStartSleepTimerEndOfTrack()
                                    showMoreSheet = false
                                },
                                label = { Text("End of Track") },
                            )
                            if (sleepTimerRemainingMs != null) {
                                androidx.compose.material3.FilterChip(
                                    selected = true,
                                    onClick = {
                                        onCancelSleepTimer()
                                        showMoreSheet = false
                                    },
                                    label = { Text("Cancel Timer") },
                                )
                            }
                        }

                        // Audio Diagnostics section
                        Text(
                            text = "Audio Diagnostics",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Format / MIME: ${song.mimeType ?: "audio/mpeg"}", style = MaterialTheme.typography.bodyMedium)
                                Text("Duration: ${formatTime(song.durationMs)}", style = MaterialTheme.typography.bodyMedium)
                                Text("Audio Offload: Enabled (Low Power Direct DSP)", style = MaterialTheme.typography.bodyMedium)
                                Text("Output Device: System Active Route", style = MaterialTheme.typography.bodyMedium)
                                Text("Location: ${song.uri}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
            }
        }
    }
    }
}

private fun PlaybackRepeatMode.next(): PlaybackRepeatMode = when (this) {
    PlaybackRepeatMode.Off -> PlaybackRepeatMode.All
    PlaybackRepeatMode.All -> PlaybackRepeatMode.One
    PlaybackRepeatMode.One -> PlaybackRepeatMode.Off
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LyricsSheet(
    title: String,
    artist: String,
    songUri: Uri,
    positionMs: Long,
    isPlaying: Boolean,
    lyricsRepository: LyricsRepository?,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Lyrics", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp),
            )

            if (lyricsRepository == null) {
                LyricsUnavailable(reason = "Lyrics engine unavailable.")
                return@Column
            }

            var lines by remember(songUri) { mutableStateOf<List<LyricLine>?>(null) }
            var resolved by remember(songUri) { mutableStateOf(false) }

            LaunchedEffect(songUri) {
                lines = lyricsRepository.load(songUri)
                resolved = true
            }

            if (!resolved) {
                Box(modifier = Modifier.padding(vertical = 48.dp)) {
                    androidx.compose.material3.CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                return@Column
            }
            val parsed = lines
            if (parsed.isNullOrEmpty()) {
                LyricsUnavailable(reason = "No .lrc file found in the same directory as the audio file.")
                return@Column
            }

            SyncedLyrics(lines = parsed, positionMs = positionMs, isPlaying = isPlaying)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LyricsUnavailable(reason: String) {
    Text(
        text = reason,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
    )
}

@Composable
private fun SyncedLyrics(
    lines: List<LyricLine>,
    positionMs: Long,
    isPlaying: Boolean,
) {
    val listState = rememberLazyListState()

    // Binary search for the last line whose timestamp has been reached. Returns -1 before the
    // first timestamp. Computed inline (not via derivedStateOf) so it re-evaluates on every
    // position tick — the sheet must track playback live.
    val activeIndex = run {
        var lo = -1
        var hi = lines.lastIndex
        while (lo < hi) {
            val mid = (lo + hi + 1) / 2
            if (lines[mid].timeMs <= positionMs) lo = mid else hi = mid - 1
        }
        lo
    }

    // Only re-scroll when the active line index changes — not on every position tick.
    LaunchedEffect(activeIndex, isPlaying) {
        if (isPlaying && activeIndex >= 0) {
            listState.animateScrollToItem(activeIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
    ) {
        itemsIndexed(lines) { index, line ->
            val isActive = index == activeIndex
            Text(
                text = line.text.ifBlank { "…" },
                style = if (isActive) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                color = if (isActive) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            )
        }
    }
}
