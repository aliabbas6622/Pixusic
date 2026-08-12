package com.aliab.player.ui.albums

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aliab.player.model.Song
import com.aliab.player.ui.artwork.AlbumArt
import com.aliab.player.ui.formatDisplayName
import com.aliab.player.ui.formatTime
import com.aliab.player.ui.songs.SongOverflowMenu

/**
 * Album detail: centred artwork, album/artist meta, Play All / Shuffle, then track list.
 * Each track row has a ⋮ overflow with Play Next / Add to Queue.
 */
@Composable
fun AlbumDetailScreen(
    albumId: Long,
    tracks: List<Song>,
    onBack: () -> Unit,
    onTrackClick: (Int) -> Unit,
    onPlayAll: (() -> Unit)? = null,
    onShuffleAll: (() -> Unit)? = null,
    onAddNext: ((Song) -> Unit)? = null,
    onAddToQueueEnd: ((Song) -> Unit)? = null,
    onShare: ((Song) -> Unit)? = null,
    onShowDetails: ((Song) -> Unit)? = null,
    onDeleteSong: ((Song) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        val album = tracks.firstOrNull()?.album
        if (album == null || tracks.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Album not found", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            return@Box
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Header: back + label + artwork + meta + action buttons
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
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
                            text = "ALBUM",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.size(48.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    AlbumArt(
                        albumId = albumId,
                        modifier = Modifier
                            .widthIn(max = 240.dp)
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(20.dp),
                                ambientColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                                spotColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.20f),
                            ),
                        shape = RoundedCornerShape(20.dp),
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = formatDisplayName(album),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = formatDisplayName(tracks.first().artist),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        text = "${tracks.size} songs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action buttons row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Button(
                            onClick = { onPlayAll?.invoke() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onSurface,
                                contentColor = MaterialTheme.colorScheme.surface,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Play All", style = MaterialTheme.typography.labelLarge)
                        }
                        OutlinedButton(
                            onClick = { onShuffleAll?.invoke() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shuffle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                "Shuffle",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        thickness = 0.5.dp,
                    )
                }
            }

            // Track list
            itemsIndexed(tracks, key = { _, song -> song.id }) { index, song ->
                TrackRow(
                    trackNumber = song.trackNumber,
                    title = song.title,
                    durationMs = song.durationMs,
                    onClick = { onTrackClick(index) },
                    onAddNext = onAddNext?.let { cb -> { cb(song) } },
                    onAddToQueueEnd = onAddToQueueEnd?.let { cb -> { cb(song) } },
                    onShare = onShare?.let { cb -> { cb(song) } },
                    onShowDetails = onShowDetails?.let { cb -> { cb(song) } },
                    onDeleteSong = onDeleteSong?.let { cb -> { cb(song) } },
                )
                if (index < tracks.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 60.dp, end = 24.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun TrackRow(
    trackNumber: Int?,
    title: String,
    durationMs: Long,
    onClick: () -> Unit,
    onAddNext: (() -> Unit)? = null,
    onAddToQueueEnd: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onShowDetails: (() -> Unit)? = null,
    onDeleteSong: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(onClick = onClick)
            .padding(start = 24.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = trackNumber?.toString() ?: "·",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.widthIn(min = 32.dp),
        )
        Text(
            text = formatDisplayName(title),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        )
        Text(
            text = formatTime(durationMs),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (onAddNext != null || onAddToQueueEnd != null ||
            onShare != null || onShowDetails != null || onDeleteSong != null
        ) {
            SongOverflowMenu(
                onPlayNext = onAddNext,
                onAddToQueue = onAddToQueueEnd,
                onShare = onShare,
                onDetails = onShowDetails,
                onDelete = onDeleteSong,
            )
        }
    }
}
