package com.aliab.player.ui.nowplaying

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
    modifier: Modifier = Modifier,
) {
    DisposableEffect(Unit) {
        onPositionUpdatesEnabled(true)
        onDispose { onPositionUpdatesEnabled(false) }
    }

    Box(modifier = modifier.fillMaxSize()) {
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
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
                Spacer(modifier = Modifier.size(48.dp))
            }

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

            // ── Utility row: Lyrics | Queue | More ──────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf("Lyrics", "Queue", "More").forEachIndexed { index, label ->
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .height(14.dp)
                                .size(width = 1.dp, height = 14.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        )
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
