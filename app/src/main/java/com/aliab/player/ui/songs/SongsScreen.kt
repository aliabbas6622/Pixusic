package com.aliab.player.ui.songs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aliab.player.model.Song
import com.aliab.player.ui.artwork.AlbumArt
import com.aliab.player.ui.formatDisplayName
import com.aliab.player.ui.formatTime

/** The Songs tab: full track list with instant search filtering. */
@Composable
fun SongsScreen(
    songs: List<Song>,
    isLoading: Boolean,
    contentPadding: PaddingValues,
    onSongClick: (Int) -> Unit,
    onAddNext: (Song) -> Unit = {},
    onAddToQueueEnd: (Song) -> Unit = {},
    currentSort: com.aliab.player.data.settings.SongSort = com.aliab.player.data.settings.SongSort.TITLE,
    isAscending: Boolean = true,
    onSortChanged: (com.aliab.player.data.settings.SongSort, Boolean) -> Unit = { _, _ -> },
    onOpenSettings: () -> Unit = {},
    onAddToPlaylist: ((Song) -> Unit)? = null,
    onShare: (Song) -> Unit = {},
    onShowDetails: (Song) -> Unit = {},
    onDeleteSong: (Song) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    // O(1) lookup from song id to its index in the full list — avoids an O(n) indexOf per row
    // (which made fast scrolling of large libraries O(n²) overall).
    val songIndexById = remember(songs) { songs.withIndex().associate { it.value.id to it.index } }
    val filteredSongs = remember(songs, searchQuery) {
        if (searchQuery.isBlank()) {
            songs
        } else {
            val query = searchQuery.trim().lowercase()
            songs.filter {
                it.title.lowercase().contains(query) ||
                    it.artist.lowercase().contains(query) ||
                    it.album.lowercase().contains(query)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        var showSortDialog by remember { mutableStateOf(false) }

        // Header Row with Title + Count + Sort Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 4.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Songs",
                    style = MaterialTheme.typography.headlineMedium,
                )
                if (songs.isNotEmpty()) {
                    Text(
                        text = if (searchQuery.isBlank()) "${songs.size} songs" else "${filteredSongs.size} of ${songs.size} songs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.padding(bottom = 2.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (songs.isNotEmpty()) {
                IconButton(
                    onClick = { showSortDialog = true },
                    modifier = Modifier.padding(bottom = 2.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Sort,
                        contentDescription = "Sort options",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        if (showSortDialog) {
            SortOptionsDialog(
                currentSort = currentSort,
                isAscending = isAscending,
                onDismiss = { showSortDialog = false },
                onSelectSort = { sort, asc ->
                    onSortChanged(sort, asc)
                    showSortDialog = false
                },
            )
        }

        // Sleek Minimal Search Box
        if (songs.isNotEmpty()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search songs, artists...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            )
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }

            songs.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier.widthIn(max = 300.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "No songs yet",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "Your music will appear here after the library is scanned.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            filteredSongs.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No matching songs",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(filteredSongs, key = { _, song -> song.id }) { index, song ->
                        val originalIndex = songIndexById[song.id] ?: index
                        SongRow(
                            song = song,
                            onClick = { onSongClick(originalIndex) },
                            onAddNext = { onAddNext(song) },
                            onAddToQueueEnd = { onAddToQueueEnd(song) },
                            onAddToPlaylist = if (onAddToPlaylist != null) { { onAddToPlaylist(song) } } else null,
                            onShare = { onShare(song) },
                            onShowDetails = { onShowDetails(song) },
                            onDeleteSong = { onDeleteSong(song) },
                        )
                        if (index < filteredSongs.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 80.dp, end = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SongRow(
    song: Song,
    onClick: () -> Unit,
    onAddNext: () -> Unit = {},
    onAddToQueueEnd: () -> Unit = {},
    onAddToPlaylist: (() -> Unit)? = null,
    onShare: () -> Unit = {},
    onShowDetails: () -> Unit = {},
    onDeleteSong: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlbumArt(
            albumId = song.albumId,
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(10.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp),
        ) {
            Text(
                text = formatDisplayName(song.title),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatDisplayName(song.artist),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Text(
            text = formatTime(song.durationMs),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SongOverflowMenu(
            onPlayNext = onAddNext,
            onAddToQueue = onAddToQueueEnd,
            onAddToPlaylist = onAddToPlaylist,
            onShare = onShare,
            onDetails = onShowDetails,
            onDelete = onDeleteSong,
        )
    }
}

@Composable
private fun SortOptionsDialog(
    currentSort: com.aliab.player.data.settings.SongSort,
    isAscending: Boolean,
    onDismiss: () -> Unit,
    onSelectSort: (com.aliab.player.data.settings.SongSort, Boolean) -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Sort Songs",
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                com.aliab.player.data.settings.SongSort.entries.forEach { sort ->
                    val isSelected = sort == currentSort
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 44.dp)
                            .clickable { onSelectSort(sort, isAscending) }
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = when (sort) {
                                com.aliab.player.data.settings.SongSort.TITLE -> "Title"
                                com.aliab.player.data.settings.SongSort.ARTIST -> "Artist"
                                com.aliab.player.data.settings.SongSort.ALBUM -> "Album"
                                com.aliab.player.data.settings.SongSort.DATE_ADDED -> "Date Added"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        if (isSelected) {
                            Text(
                                text = if (isAscending) "↑ Asc" else "↓ Desc",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .clickable { onSelectSort(sort, !isAscending) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Close", color = MaterialTheme.colorScheme.onSurface)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}
